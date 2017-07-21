package org.autorefactor.cli;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.autorefactor.refactoring.ApplyRefactoringsJob;
import org.autorefactor.refactoring.JavaProjectOptions;
import org.autorefactor.refactoring.JavaProjectOptionsImpl;
import org.autorefactor.refactoring.RefactoringRule;
import org.autorefactor.refactoring.Release;
import org.autorefactor.refactoring.rules.AddBracketsToControlStatementRefactoring;
import org.autorefactor.refactoring.rules.AggregateASTVisitor;
import org.autorefactor.refactoring.rules.AllRefactoringRules;
import org.autorefactor.refactoring.rules.AnnotationRefactoring;
import org.autorefactor.refactoring.rules.BigDecimalRefactoring;
import org.autorefactor.refactoring.rules.CollapseIfStatementRefactoring;
import org.autorefactor.refactoring.rules.CollectionContainsRefactoring;
import org.autorefactor.refactoring.rules.CommentsRefactoring;
import org.autorefactor.refactoring.rules.CommonCodeInIfElseStatementRefactoring;
import org.autorefactor.refactoring.rules.CommonIfInIfElseRefactoring;
import org.autorefactor.refactoring.rules.HotSpotIntrinsicedAPIsRefactoring;
import org.autorefactor.refactoring.rules.IfElseIfRefactoring;
import org.autorefactor.refactoring.rules.InvertEqualsRefactoring;
import org.autorefactor.refactoring.rules.MapRefactoring;
import org.autorefactor.refactoring.rules.NoAssignmentInIfConditionRefactoring;
import org.autorefactor.refactoring.rules.PrimitiveWrapperCreationRefactoring;
import org.autorefactor.refactoring.rules.PushNegationDownRefactoring;
import org.autorefactor.refactoring.rules.RemoveEmptyLinesRefactoring;
import org.autorefactor.refactoring.rules.RemoveFieldsDefaultValuesRefactoring;
import org.autorefactor.refactoring.rules.RemoveSemiColonRefactoring;
import org.autorefactor.refactoring.rules.RemoveUnnecessaryCastRefactoring;
import org.autorefactor.refactoring.rules.RemoveUnneededThisExpressionRefactoring;
import org.autorefactor.refactoring.rules.RemoveUselessNullCheckRefactoring;
import org.autorefactor.refactoring.rules.StringBuilderRefactoring;
import org.autorefactor.refactoring.rules.StringRefactoring;
import org.autorefactor.refactoring.rules.TestNGAssertRefactoring;
import org.autorefactor.refactoring.rules.TryWithResourceRefactoring;
import org.autorefactor.refactoring.rules.UseDiamondOperatorRefactoring;
import org.autorefactor.refactoring.rules.UseMultiCatchRefactoring;
import org.autorefactor.refactoring.rules.UseStringContainsRefactoring;
import org.autorefactor.refactoring.rules.VectorOldToNewAPIRefactoring;
import org.autorefactor.refactoring.rules.WorkWithNullCheckedExpressionFirstRefactoring;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;

/**
 * Command line interface (cli) for Autorefactor.
 *
 * The cli is realized as a headless eclipse plugin.
 *
 * To be able to invoke your code from the command line, you need to execute an
 * Eclipse instance that has your plug-in installed. In this part of the
 * tutorial, you will export your plug-in and you will install them in Eclipse.
 * In the Plug-in editor of MyFirstPlugin, navigate to the Overview tab. Click
 * on the Export Wizard link at the bottom right. Select the two plug-ins you
 * created so far (MyFirstPlugin and MyHeadlessPlugin), and put the path of the
 * dropins directory of your current Eclipse installation. This tutorial assumes
 * that you have installed Eclipse under
 * /home/barthelemy/eclipse_prog/eclipse3.5.1:
 *
 * Export all plugins to directory "dropins" in workspace.
 *
 * run with: AutoRefactor-work$ eclipse-neon-plugin-dev -nosplash -data
 * `pwd`/../workspace-run-cli -application org.autorefactor.cli.AutoRefactor
 * help -vmargs
 * -Dorg.eclipse.equinox.p2.reconciler.dropins.directory=`pwd`/../workspace-neon-work/dropins
 *
 * <pre>
 (cd ..;eclipse-neon-plugin-dev -nosplash --launcher.suppressErrors\
  -data `pwd`/workspace-run-cli -application org.autorefactor.cli.AutoRefactor --help\
   -vmargs -Dorg.eclipse.equinox.p2.reconciler.dropins.directory=`pwd`/workspace-neon-work/dropins)
 * </pre>
 *
 *
 *
 * Helpful information creating this plugin: - Autorefactor project setup
 * (https://github.com/JnRouvignac/AutoRefactor/wiki/Hacking-AutoRefactor) -
 * http://www.sable.mgill.ca/ppa/tut_4.html -
 * https://sdqweb.ipd.kit.edu/wiki/JDT_Tutorial:_Creating_Eclipse_Java_Projects_Programmatically
 *
 * eclipse command line parameters:
 * https://help.eclipse.org/neon/index.jsp?topic=%2Forg.eclipse.platform.doc.user%2Ftasks%2Frunning_eclipse.htm
 *
 * cal101@github, 07/2017
 */
public class AutoRefactor implements IApplication {
    private enum SourceLevel {
        None(""), Java6("1.6"), Java7("1.7"), Java8("1.8"), Java9("1.9"), Max("1.100");

        private final String v;

        SourceLevel(String v) {
            this.v = v;
        }

        static SourceLevel fromValue(String v) {
            for (SourceLevel sl : values()) {
                if (v.equals(sl.v)) {
                    return sl;
                }
            }
            throw new IllegalArgumentException("could not find '" + v + "' in " + Arrays.asList(values()));
        }
    }

    private final Map<String, SourceLevel> sourceLevelPerClass;
    {
        Map<String, SourceLevel> m = new HashMap<String, SourceLevel>();
        m.put(UseMultiCatchRefactoring.class.getName(), SourceLevel.Java7);
        m.put(TryWithResourceRefactoring.class.getName(), SourceLevel.Java7);
        m.put(UseDiamondOperatorRefactoring.class.getName(), SourceLevel.Java7);
        sourceLevelPerClass = Collections.unmodifiableMap(m);
    }

    @Override
    public Object start(IApplicationContext context) throws Exception {
        final String[] argv = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        // System.out.println("args: " + Arrays.asList(argv));

        // parse command line arguments
        final Args args = new Args();
        final ApplyArgs applyArgs = new ApplyArgs();
        final ListArgs listArgs = new ListArgs();
        final EclipseArgs eclipseArgs = new EclipseArgs();
        final JCommander argParser = JCommander.newBuilder().addObject(args).addCommand("list", listArgs)
                .addCommand("apply", applyArgs).addCommand("eclipse", eclipseArgs).build();
        argParser.setProgramName("autorefactor");
        try {
            argParser.parse(argv);
        } catch (ParameterException e) {
            System.out.println("*** ERROR: " + e.getMessage());
            // prevent launcher messages
            System.setProperty(IApplicationContext.EXIT_DATA_PROPERTY, "");
            return -1;
        }

        if (args.isHelp()) {
            argParser.usage();
            return EXIT_OK;
        }

        final boolean verbose = args.isVerbose();

        boolean usage = false;
        final String cmd = argParser.getParsedCommand();
        if ("apply".equals(cmd)) {
            final boolean verboseApply = verbose || applyArgs.isVerbose();
            final Pattern variableAssigment = Pattern.compile("^([^=]+)=(.*)$");
            final Map<String, String> classPathVariables = new HashMap<String, String>();

            for (String s : applyArgs.getClassPathVariables()) {
                Matcher m = variableAssigment.matcher(s);
                if (m.matches()) {
                    classPathVariables.put(m.group(1), m.group(2));
                } else {
                    usage = true;
                }
            }

            final String projectFile = applyArgs.getProjectPath();
            final List<String> sourceFolders = new ArrayList<String>(applyArgs.getSources());
            if (projectFile != null) {
                refactorProject(new File(projectFile), sourceFolders, classPathVariables, applyArgs.getRefactorings(),
                        verboseApply);
            } else {
                usage = true;
            }
        } else if ("list".equals(cmd)) {
            listRefactorings();
        } else if ("eclipse".equals(cmd)) {
            refactorProject(new File(eclipseArgs.getProjectPath()), null, null, null,
                    verbose || eclipseArgs.isVerbose());
        } else {
            argParser.usage();
        }

        if (usage) {
            argParser.usage();
            // prevent launcher messages
            System.setProperty(IApplicationContext.EXIT_DATA_PROPERTY, "");
            return -1;
        } else {
            return IApplication.EXIT_OK;
        }
    }

    private void listRefactorings() {
        System.out.println("Available refactorings:");
        final List<RefactoringRule> rules = new ArrayList<RefactoringRule>(
                AllRefactoringRules.getAllRefactoringRules());
        final Comparator<RefactoringRule> bySimpleClassName = new Comparator<RefactoringRule>() {

            @Override
            public int compare(RefactoringRule o1, RefactoringRule o2) {
                return o1.getClass().getSimpleName().compareTo(o2.getClass().getSimpleName());
            }

        };
        Collections.sort(rules, bySimpleClassName);
        for (RefactoringRule rule : rules) {
            System.out.println("    " + rule.getClass().getSimpleName() + " - " + rule.getName()
                    + (rule.isByDefault() ? " (pre-configured)" : ""));
            final String description = TextUtil.formatLines(rule.getDescription(), 70).replace("\n", "\n        ")
                    .trim();
            System.out.println("        " + description);
        }
    }

    private void refactorProject(final File projectFile, final List<String> sourceFolders,
            final Map<String, String> classPathVariables, List<String> refactorings, final boolean verbose)
            throws JavaModelException, CoreException {

        if (!projectFile.exists()) {
            System.err.println("cannot access project file: " + projectFile.getAbsolutePath());
            throw new CoreException(new Status(0, "", "cannot access project file: " + projectFile.getAbsolutePath()));
        }

        final IWorkspace workspace = ResourcesPlugin.getWorkspace();
        if (classPathVariables != null) {
            for (Entry<String, String> e : classPathVariables.entrySet()) {
                JavaCore.setClasspathVariable(e.getKey(), new Path(e.getValue()), null);
            }
        }

        // import external project into workspace if needed
        if (verbose) {
            System.out.println("importing project");
        }
        IProject project = createProject(workspace, projectFile, verbose);
        if (verbose) {
            System.out.println("opening project");
        }
        project.open(null);
        if (verbose) {
            System.out.println("refreshing project");
        }
        project.refreshLocal(IResource.DEPTH_INFINITE, null);

        if (sourceFolders != null) {
            if (verbose) {
                System.out.println("starting refactoring");
            }
            final IJavaProject javaProject = JavaCore.create(project);
            final List<RefactoringRule> rules = selectRules(javaProject, refactorings);
            try {
                for (String src : sourceFolders) {
                    final IFolder sourceFolder = project.getFolder(src);
                    // refactor(javaProject, sourceFolder,
                    // Pattern.compile(".*MDContainer.java"), verbose);
                    // refactor(javaProject, sourceFolder,
                    // Pattern.compile(".*((FakeUserWebUtil)|(EditPageView3)|(BasePrioObservedQueue)
                    // |(BooleanConst)).*"),
                    // verbose);
                    // refactor(javaProject, sourceFolder,
                    // Pattern.compile(".*MultiCatch.*"), verbose);
                    refactor(javaProject, sourceFolder, Pattern.compile(".*"), rules, verbose);
                    // refactor(javaProject, sourceFolder,
                    // Pattern.compile(".*RBTreeFastSet2.*"), refactorings,
                    // verbose);
                    // refactor(javaProject, sourceFolder,
                    // verbose);
                }
            } finally {
                javaProject.save(null, true);
                javaProject.close();
                workspace.save(true, null);
            }
        } else {
            IWorkspaceRoot wsroot = workspace.getRoot();
            for (IProject p : wsroot.getProjects()) {
                printProjectDetails(p);
            }
        }
    }

    private IProject createProject(final IWorkspace workspace, final File projectFile, final boolean verbose)
            throws CoreException {
        IPath projectDotProjectFile = new Path(projectFile.getAbsolutePath());
        IProjectDescription projectDescription = workspace.loadProjectDescription(projectDotProjectFile);
        IProject project = workspace.getRoot().getProject(projectDescription.getName());
        if (!project.exists()) {
            if (verbose) {
                System.out.println("creating project");
            }
            project.create(projectDescription, null);
        }
        return project;
    }

    private final Set<String> filesToIgnore = new HashSet<String>(Arrays.asList(
            // very slow or really hangup (Comment Formatter)
            "ShortKeyUtilN.java", "ShortKeyUtil.java", "BlowfishEngine30.java", "HotfolderManager.java",
            // for "all"
            "CollectionObject.java",
            // some buggy refactoring in "all"
            "LicenseCheck.java", "AutoDoc.java"));

    private void refactor(final IJavaProject project, final IFolder sourceFolder, final Pattern pattern,
            final List<RefactoringRule> rules, final boolean verbose) throws CoreException {
        final IPackageFragmentRoot pfr = project.getPackageFragmentRoot(sourceFolder);
        walkMax(sourceFolder, 1000000, new IResourceVisitor() {
            /** @Override */
            public boolean visit(IResource resource) throws CoreException {
                try {
                    if ("java".equals(resource.getFileExtension())) {
                        String name = resource.getName();
                        IPath relativePath = relativePath(sourceFolder, resource);
                        if (!pattern.matcher(relativePath.toString()).matches()) {
                            if (verbose) {
                                System.out.println("skipping " + resource.getProjectRelativePath());
                                // System.out.println("skipping " +
                                // resource.getRawLocationURI());
                            }
                            return true;
                        }
                        if (verbose) {
                            System.out.println("refactoring " + resource.getProjectRelativePath());
                        }
                        if (filesToIgnore.contains(name)) {
                            if (verbose) {
                                System.out.println("    ignored");
                            }
                            return true;
                        }
                        IPackageFragment pf = pfr.getPackageFragment(packageName(relativePath));
                        ICompilationUnit cu = pf.getCompilationUnit(name);
                        final IDocument doc = new Document(
                                readFileToString(resource.getLocation().toFile().getAbsolutePath()));
                        if (new ApplyRefactoringsJob(null, null, EnvUtil.SIMPLE_ENVIRONMENT).applyRefactoring(doc, cu,
                                new AggregateASTVisitor(rules),
                                // newJavaProjectOptions(Release.javaSE("1.5.0"),
                                // 4));
                                newJavaProjectOptions(Release.javaSE("1.7.0"), 4), null)) {
                            System.out.println(">> " + resource.getProjectRelativePath());
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }

            private String packageName(IPath path) {
                return path.removeLastSegments(1).toString().replace("/", ".");
            }
        });
    }

    private List<RefactoringRule> selectRules(final IJavaProject project, List<String> refactorings)
            throws JavaModelException {
        SourceLevel sourceLevel = sourceLevel(project);

        // new
        // AggregateASTVisitor(AllRefactoringRules.getAllRefactoringRules()),
        // new
        // AggregateASTVisitor(getAllUnbrokenRules()),
        // new AggregateASTVisitor(selectedRules()),
        final List<RefactoringRule> rules = selectRules(refactorings, sourceLevel);
        return rules;
    }

    private SourceLevel sourceLevel(final IJavaProject project) throws JavaModelException {
        SourceLevel sourceLevel = SourceLevel.Max;
        for (IClasspathEntry e: project.getRawClasspath()) {
            if (e.getEntryKind() == IClasspathEntry.CPE_CONTAINER) {
                sourceLevel = SourceLevel.fromValue(e.getPath().lastSegment().replaceFirst(".*-", ""));
                break;
            }
        }
        return sourceLevel;
    }

    /**
     * Selected rules.
     *
     * @return List of selected rules
     */
    public static List<RefactoringRule> selectedRules() {
        return new ArrayList<RefactoringRule>(Arrays.asList(
                // 170
                // new RemoveUnnecessaryLocalBeforeReturnRefactoring()
                // 160
                // new RemoveUnneededThisExpressionRefactoring()
                // 167,183,184
                // new StringBuilderRefactoring()
                new RemoveUnnecessaryCastRefactoring()));
    }

    /**
     * Open bugs:
     *
     * No not move end() before if() begin() if (needsBegin()) { end(); } else {
     * end(); }
     *
     * @return fules
     */
    public static List<RefactoringRule> filteredAllRules() {
        List<RefactoringRule> rules = new ArrayList<RefactoringRule>(AllRefactoringRules.getAllRefactoringRules());
        // leave that for formatter
        removeByClass(rules, RemoveEmptyLinesRefactoring.class);
        // leave that for formatter
        removeByClass(rules, CommentsRefactoring.class);
        // wrong semantic change
        removeByClass(rules, CommonCodeInIfElseStatementRefactoring.class);
        return rules;
    }

    /**
     * Selected rules.
     *
     * @param refactorings
     *            the refactorings argument
     * @param sourceLevel source level
     *
     * @return List of selected rules
     */
    private List<RefactoringRule> selectRules(List<String> refactorings, SourceLevel sourceLevel) {
        final List<RefactoringRule> allRules = AllRefactoringRules.getAllRefactoringRules();
        List<RefactoringRule> rules = new ArrayList<RefactoringRule>(allRules.size());
        for (String name : refactorings) {
            if ("all".equals(name)) {
                rules.clear();
                rules.addAll(allRules);
                break;
            }
            if ("all-cal-filter".equals(name)) {
                rules.clear();
                rules.addAll(filteredAllRules());
                break;
            }
            RefactoringRule rule = findBySimpleClassName(allRules, name);
            if (rule == null) {
                throw new IllegalArgumentException("could not find rule: " + name);
            }
            rules.add(rule);
        }
        Iterator<RefactoringRule> it = rules.iterator();
        while (it.hasNext()) {
            final RefactoringRule rule = it.next();
            SourceLevel rsl = sourceLevelPerClass.get(rule.getClass().getName());
            if (rsl != null && rsl.compareTo(sourceLevel) > 0) {
                System.out.println("disabled rule by source level '" + sourceLevel
                        + "': " + rule.getClass().getSimpleName() + " (" + rsl + ")");
                it.remove();
            }
        }
        return rules;
    }

    private static RefactoringRule findBySimpleClassName(List<RefactoringRule> rules, String name) {
        for (RefactoringRule rule : rules) {
            if (name.equals(rule.getClass().getSimpleName())) {
                return rule;
            }
        }
        return null;
    }

    private static void removeByClass(List<RefactoringRule> rules, Class<? extends RefactoringRule> clazz) {
        Iterator<RefactoringRule> it = rules.iterator();
        while (it.hasNext()) {
            if (clazz.isInstance(it.next())) {
                it.remove();
                break;
            }
        }
    }

    /**
     * The list of refactorings that don't create uncompilable code or don't
     * crash.
     *
     * @return rules
     */
    public static List<RefactoringRule> getAllUnbrokenRules() {
        return new ArrayList<RefactoringRule>(Arrays.asList(new RemoveUselessNullCheckRefactoring(),
                new WorkWithNullCheckedExpressionFirstRefactoring(), new VectorOldToNewAPIRefactoring(),
                // cal: https://github.com/JnRouvignac/AutoRefactor/issues/161 -
                // closed
                new PrimitiveWrapperCreationRefactoring(),
                // cal: maybe
                // https://github.com/JnRouvignac/AutoRefactor/issues/165 -
                // closed
                // cal: maybe Don't overoptimize the booleans #190
                // new BooleanRefactoring(),
                new AddBracketsToControlStatementRefactoring(), new InvertEqualsRefactoring(),
                // cal: https://github.com/JnRouvignac/AutoRefactor/issues/162 -
                // closed
                // cal: https://github.com/JnRouvignac/AutoRefactor/issues/181 -
                // jdt bug - compile error
                // cal: https://github.com/JnRouvignac/AutoRefactor/issues/182 -
                // closed
                // new SimplifyExpressionRefactoring(),
                // cal: https://github.com/JnRouvignac/AutoRefactor/issues/160 -
                // closed - but buggy
                new RemoveUnneededThisExpressionRefactoring(),
                // cal: Maybe
                // https://github.com/JnRouvignac/AutoRefactor/issues/168 -
                // closed
                new StringRefactoring(), new BigDecimalRefactoring(),
                // TODO JNR implement
                // new ForeachRefactoring(),
                // cal: https://github.com/JnRouvignac/AutoRefactor/issues/159
                // new DeadCodeEliminationRefactoring(),
                new CollapseIfStatementRefactoring(), new CommonCodeInIfElseStatementRefactoring(),
                // TODO JNR complete it
                // new GenerecizeRefactoring(),
                // cal: https://github.com/JnRouvignac/AutoRefactor/issues/163 -
                // duplicate
                // cal: https://github.com/JnRouvignac/AutoRefactor/issues/121 -
                // closed
                // cal: https://github.com/JnRouvignac/AutoRefactor/issues/189
                // new UseDiamondOperatorRefactoring(),
                // cal: https://github.com/JnRouvignac/AutoRefactor/issues/166 -
                // closed
                // cal: https://github.com/JnRouvignac/AutoRefactor/issues/188
                new UseMultiCatchRefactoring(), new CollectionContainsRefactoring(),
                // cal: https://github.com/JnRouvignac/AutoRefactor/issues/169 -
                // fixed
                // cal: https://github.com/JnRouvignac/AutoRefactor/issues/158
                // (AIOOBE)
                // cal: https://github.com/JnRouvignac/AutoRefactor/issues/179
                // (Illegal Identifier)
                // new CollectionRefactoring(),
                new MapRefactoring(), new NoAssignmentInIfConditionRefactoring(), new IfElseIfRefactoring(),
                new CommonIfInIfElseRefactoring(),
                // TODO JNR implement
                // new RemoveStupidIdiomaticPatternRefactoring(),
                // TODO JNR - to be completed
                // new ReduceVariableScopeRefactoring(),
                // cal: https://github.com/JnRouvignac/AutoRefactor/issues/167 -
                // closed
                // cal: https://github.com/JnRouvignac/AutoRefactor/issues/183 -
                // closed
                // cal: https://github.com/JnRouvignac/AutoRefactor/issues/184 -
                // closed
                new StringBuilderRefactoring(), new UseStringContainsRefactoring(), new PushNegationDownRefactoring(),
                // cal: https://github.com/JnRouvignac/AutoRefactor/issues/164
                // and it's slow
                // new CommentsRefactoring(),
                new RemoveFieldsDefaultValuesRefactoring(),
                // cal: https://github.com/JnRouvignac/AutoRefactor/issues/170 -
                // closed
                // cal: https://github.com/JnRouvignac/AutoRefactor/issues/191 -
                // plugin crash
                // new RemoveUnnecessaryLocalBeforeReturnRefactoring(),
                // cal: TODO: maybe wrong in combination (compile error)
                // new RemoveUnnecessaryCastRefactoring(),
                // cal: may cause infinite looping, late disable for now
                // new RemoveUselessModifiersRefactoring(),
                // TODO: new name?
                new HotSpotIntrinsicedAPIsRefactoring(), new AnnotationRefactoring(), new RemoveSemiColonRefactoring(),
                // FIXME it would be nice if it was only enabled when testng jar
                // is detected for the project
                new TestNGAssertRefactoring(), new RemoveEmptyLinesRefactoring()));
    }

    /**
     * Read file to String.
     *
     * TODO: use project encoding!
     *
     * @param filePath
     *            file path
     * @return String
     * @throws CoreException
     *             on I/O errors
     */
    public static String readFileToString(String filePath) throws CoreException {
        try {
            StringBuilder fileData = new StringBuilder(1000);
            BufferedReader reader = new BufferedReader(new FileReader(filePath));

            char[] buf = new char[10];
            int numRead = 0;
            while ((numRead = reader.read(buf)) != -1) {
                String readData = String.valueOf(buf, 0, numRead);
                fileData.append(readData);
                buf = new char[1024];
            }

            reader.close();

            return fileData.toString();
        } catch (IOException e) {
            throw new CoreException(new Status(0, "", "", e));
        }
    }

    private void printProjectDetails(IProject p) throws JavaModelException {
        PrintStream o = System.out;
        o.println("project " + p.getName());
        IJavaProject jp = JavaCore.create(p);
        o.println("    raw classpath:");
        for (IClasspathEntry s: jp.getRawClasspath()) {
            o.println("        " + s.getEntryKind() + " " + s.getContentKind()
                + " " + s.getPath() + " (" + s + ")");
        }
        // e.g. jars and directories
        o.println("    children:");
        for (IJavaElement s: jp.getChildren()) {
            o.println("        " + s.getElementType() + " " + s.getElementName()
                + " " + s.getPath() + " (" + s + ")");
        }
    }

    private void walkMax(final IFolder f, final int max, final IResourceVisitor visitor) throws CoreException {
        f.accept(new IResourceVisitor() {
            int count = max;

            /** @Override */
            public boolean visit(IResource resource) throws CoreException {
                return count-- > 0 && visitor.visit(resource);
            }
        });
    }

    private JavaProjectOptions newJavaProjectOptions(Release javaSE, int tabSize) {
        final JavaProjectOptionsImpl options = new JavaProjectOptionsImpl();
        options.setTabSize(tabSize);
        options.setJavaSERelease(javaSE);
        return options;
    }

    @Override
    public void stop() {
    }

    private IPath relativePath(final IFolder f, IResource resource) {
        return resource.getFullPath().removeFirstSegments(f.getFullPath().segmentCount());
    }
}
