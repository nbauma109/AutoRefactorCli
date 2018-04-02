package org.autorefactor.cli;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.autorefactor.cli.dd.DDMin;
import org.autorefactor.cli.dd.DDMin.Result;
import org.autorefactor.matcher.AstMatcherUtil;
import org.autorefactor.refactoring.ApplyRefactoringsJob;
import org.autorefactor.refactoring.JavaProjectOptions;
import org.autorefactor.refactoring.JavaProjectOptionsImpl;
import org.autorefactor.refactoring.RefactoringRule;
import org.autorefactor.refactoring.Release;
import org.autorefactor.refactoring.rules.AddBracketsToControlStatementRefactoring;
import org.autorefactor.refactoring.rules.AggregateASTVisitor;
import org.autorefactor.refactoring.rules.AllRefactoringRules;
import org.autorefactor.refactoring.rules.AnnotationRefactoring;
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
import org.autorefactor.refactoring.rules.SetRatherThanMapRefactoring;
import org.autorefactor.refactoring.rules.StringBuilderRefactoring;
import org.autorefactor.refactoring.rules.TestNGAssertRefactoring;
import org.autorefactor.refactoring.rules.TryWithResourceRefactoring;
import org.autorefactor.refactoring.rules.UseDiamondOperatorRefactoring;
import org.autorefactor.refactoring.rules.UseMultiCatchRefactoring;
import org.autorefactor.refactoring.rules.UseStringContainsRefactoring;
import org.autorefactor.refactoring.rules.VectorOldToNewAPIRefactoring;
import org.autorefactor.refactoring.rules.WorkWithNullCheckedExpressionFirstRefactoring;
import org.autorefactor.util.Pair;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
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
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
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
 * Improvement ideas:
 * Get rid of plugin environment alog this way?
 * https://stackoverflow.com/questions/964747/how-can-i-use-the-java-eclipse-abstract-syntax-tree-in-a-project-outside-eclipse
 *
 * cal101@github, 07/2017
 *
 * Ignore Warnings regarding access to restricted plugin classes.
 */
@SuppressWarnings("restriction")
public class AutoRefactor implements IApplication {

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
        AstDumpArgs astDumpArgs = new AstDumpArgs();
        final JCommander argParser = JCommander.newBuilder().addObject(args)
                .addCommand("list", listArgs)
                .addCommand("apply", applyArgs)
                .addCommand("ast-dump", astDumpArgs)
                .addCommand("eclipse", eclipseArgs)
                .build();
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
                SourceLevel sourceLevel = applyArgs.getSourceLevel() != null ? SourceLevel.fromValue(applyArgs.getSourceLevel()) : null;
                EffApplyArgs effArgs = new EffApplyArgs(sourceLevel,
                        Pattern.compile(applyArgs.getIncludeRe()),
                        applyArgs.getDeltaDebugTestExceptionPattern(),
                        applyArgs.getDeltaDebugBeforeTestCodePattern(),
                        applyArgs.getDeltaDebugBeforeTestCodeCommand(),
                        applyArgs.getDeltaDebugTestCodePattern(),
                        applyArgs.getDeltaDebugTestCodeCommand(),
                        verboseApply, applyArgs.isDeltaDebug());
                refactorProject(new File(projectFile), sourceFolders, classPathVariables,
                		applyArgs.getRefactorings(),
                		applyArgs.getExcludedRefactorings(),
                		effArgs);
            } else {
                usage = true;
            }
        } else if ("list".equals(cmd)) {
            listRefactorings();
        } else if ("ast-dump".equals(cmd)) {
            dumpAst(new File(astDumpArgs.getProjectPath()), Collections.<String>emptyList(),  Collections.<String,String>emptyMap(),
                    astDumpArgs);
        } else if ("eclipse".equals(cmd)) {
            printEclipseInfo(new File(eclipseArgs.getProjectPath()), null, verbose || eclipseArgs.isVerbose());
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

    private void refactorProject(final File projectFile, final List<String> originalSourceFolders,
            final Map<String, String> classPathVariables, List<String> refactorings, List<String> excludedRefactorings,
            final EffApplyArgs args)
                    throws JavaModelException, CoreException {
        final boolean verbose = args.verbose;
        final Pair<IWorkspace, IProject> projectCtx = prepareProject(projectFile, classPathVariables, verbose);
        final IWorkspace workspace = projectCtx.getFirst();
        final IProject project = projectCtx.getSecond();

        final IJavaProject javaProject = JavaCore.create(project);
        List<String> sourceFolders = new ArrayList<String>(originalSourceFolders);
        if (sourceFolders.isEmpty()) {
            sourceFolders.addAll(allProjectSourceFolders(javaProject));
        }

        if (verbose) {
            System.out.println("refactor: starting refactoring");
            System.out.println("refactor: source folders: " + sourceFolders);
        }
        final List<RefactoringRule> rules = selectRules(javaProject, refactorings, excludedRefactorings, args.sourceLevel);
        if (verbose) {
            System.out.println("refactor: rules: " + rules);
        }
        try {
            for (String src : sourceFolders) {
                final IFolder sourceFolder = project.getFolder(src);
                refactor(javaProject, sourceFolder, rules, args);
                //refactor(javaProject, sourceFolder, Pattern.compile(".*SpacePreparator.*"), rules, verbose);
                //refactor(javaProject, sourceFolder, Pattern.compile(".*TextEditsBuilder.*"), rules, verbose);
                //refactor(javaProject, sourceFolder, Pattern.compile(".*CharOperation.*"), rules, verbose);
                //refactor(javaProject, sourceFolder, Pattern.compile(".*FieldDeclaration.*"), rules, verbose);
                //refactor(javaProject, sourceFolder, Pattern.compile(".*ProblemReporter.*"), rules, verbose);
            }
        } finally {
            javaProject.save(null, true);
            javaProject.close();
            workspace.save(true, null);
        }
    }

    private void dumpAst(final File projectFile, final List<String> originalSourceFolders,
            final Map<String, String> classPathVariables, final AstDumpArgs args)
                    throws JavaModelException, CoreException {
        final boolean verbose = args.isVerbose();
        final Pair<IWorkspace, IProject> projectCtx = prepareProject(projectFile, classPathVariables, verbose);
        final IWorkspace workspace = projectCtx.getFirst();
        final IProject project = projectCtx.getSecond();

        final IJavaProject javaProject = JavaCore.create(project);
        List<String> sourceFolders = new ArrayList<String>(originalSourceFolders);
        if (sourceFolders.isEmpty()) {
            sourceFolders.addAll(allProjectSourceFolders(javaProject));
        }

        if (verbose) {
            System.out.println("dumpAst: starting");
            System.out.println("dumpAst: source folders: " + sourceFolders);
        }
        try {
            for (String src : sourceFolders) {
                final IFolder sourceFolder = project.getFolder(src);
                dumpAst(javaProject, sourceFolder, args.getIncludePattern(), args.isVerbose());
            }
        } finally {
            javaProject.save(null, true);
            javaProject.close();
            workspace.save(true, null);
        }
    }

    private void printEclipseInfo(final File projectFile,
            final Map<String, String> classPathVariables,
            boolean verbose)
                    throws JavaModelException, CoreException {
        final Pair<IWorkspace, IProject> projectCtx = prepareProject(projectFile, classPathVariables, verbose);
        final IWorkspace workspace = projectCtx.getFirst();

        IWorkspaceRoot wsroot = workspace.getRoot();
        for (IProject p : wsroot.getProjects()) {
            printProjectDetails(p);
        }
    }

    private Pair<IWorkspace, IProject> prepareProject(final File projectFile,
            final Map<String, String> classPathVariables, final boolean verbose)
            throws CoreException, JavaModelException {
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

        disableAutoBuild(workspace);

        // import external project into workspace if needed
        if (verbose) {
            System.out.println("refactor: importing project");
        }
        IProject project = createProject(workspace, projectFile, verbose);
        if (verbose) {
            System.out.println("refactor: opening project");
        }
        project.open(null);
        if (verbose) {
            System.out.println("refactor: refreshing project");
        }
        project.refreshLocal(IResource.DEPTH_INFINITE, null);

        return Pair.of(workspace, project);
    }

    private void disableAutoBuild(final IWorkspace workspace) throws CoreException {
        IWorkspaceDescription desc = workspace.getDescription();
        boolean isAutoBuilding = desc.isAutoBuilding();
        if (isAutoBuilding) {
            //System.out.println("eclipse workspace: disabling auto build");
            desc.setAutoBuilding(false);
            workspace.setDescription(desc);
        }
    }

    private List<String> allProjectSourceFolders(final IJavaProject javaProject) throws JavaModelException {
        List<String> src = new ArrayList<String>();
        for (IClasspathEntry cp: javaProject.getRawClasspath()) {
            if (cp.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                src.add(cp.getPath().makeRelativeTo(javaProject.getPath()).toString());
            }
        }
        return src;
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

    private static class Target {
    	private final IPackageFragmentRoot packageFragmentRoot;
    	private final IResource resource;
    	private final IPath relativePath;
		public Target(IPackageFragmentRoot packageFragmentRoot, IResource resource, IPath relativePath) {
			super();
			this.packageFragmentRoot = packageFragmentRoot;
			this.resource = resource;
			this.relativePath = relativePath;
		}
		public IPackageFragmentRoot getPackageFragmentRoot() {
			return packageFragmentRoot;
		}
		public IResource getResource() {
			return resource;
		}
		public IPath getRelativePath() {
			return relativePath;
		}
    }

    private void refactor(final IJavaProject project, final IFolder sourceFolder,
            final List<RefactoringRule> rules, final EffApplyArgs args) throws CoreException {
        final boolean verbose = args.verbose;
        final IPackageFragmentRoot pfr = project.getPackageFragmentRoot(sourceFolder);
        walkMax(sourceFolder, 1000000, new IResourceVisitor() {
            @Override
            public boolean visit(IResource resource) throws CoreException {
                try {
                    if ("java".equals(resource.getFileExtension())) {
                        final String name = resource.getName();
                        IPath relativePath = relativePath(sourceFolder, resource);
                        if (!args.sourceFileName.matcher(relativePath.toString()).matches()) {
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
                        if (args.deltaDebug) {
                            deltaDebugRefactorFile(new Target(pfr, resource, relativePath), resource, relativePath, rules, pfr, args);
                        } else {
                            refactorFile(new Target(pfr, resource, relativePath), rules);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
    }

    private void dumpAst(final IJavaProject project, final IFolder sourceFolder,
            final Pattern filenamePattern, final boolean verbose) throws CoreException {
        final IPackageFragmentRoot pfr = project.getPackageFragmentRoot(sourceFolder);
        walkMax(sourceFolder, 1000000, new IResourceVisitor() {
            @Override
            public boolean visit(IResource resource) throws CoreException {
                try {
                    if ("java".equals(resource.getFileExtension())) {
                        final String name = resource.getName();
                        IPath relativePath = relativePath(sourceFolder, resource);
                        if (!filenamePattern.matcher(relativePath.toString()).matches()) {
                            if (verbose) {
                                System.out.println("skipping " + resource.getProjectRelativePath());
                                // System.out.println("skipping " +
                                // resource.getRawLocationURI());
                            }
                            return true;
                        }
                        if (verbose) {
                            System.out.println("dumping " + resource.getProjectRelativePath());
                        }
                        if (filesToIgnore.contains(name)) {
                            if (verbose) {
                                System.out.println("    ignored");
                            }
                            return true;
                        }
                        dumpAst(new Target(pfr, resource, relativePath));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return true;
            }
        });
    }

    private String packageName(IPath path) {
        return path.removeLastSegments(1).toString().replace("/", ".");
    }

    private List<RefactoringRule> selectRules(final IJavaProject project, List<String> refactorings, List<String> excludedRefactorings, SourceLevel sourceLevel)
            throws JavaModelException {
        // new
        // AggregateASTVisitor(AllRefactoringRules.getAllRefactoringRules()),
        // new
        // AggregateASTVisitor(getAllUnbrokenRules()),
        // new AggregateASTVisitor(selectedRules()),
        return selectRules(refactorings, excludedRefactorings, sourceLevel != null ? sourceLevel : sourceLevel(project));
    }

    private SourceLevel sourceLevel(final IJavaProject project) throws JavaModelException {
        SourceLevel sourceLevel = SourceLevel.Max;
        for (IClasspathEntry e: project.getRawClasspath()) {
            if (e.getEntryKind() == IClasspathEntry.CPE_CONTAINER && String.valueOf(e.getPath()).contains("JRE_CONTAINER")) {
                try {
                    sourceLevel = SourceLevel.fromValue(e.getPath().lastSegment().replaceFirst(".*-", ""));
                } catch (IllegalArgumentException ignore) {
                    System.err.println("could not detect java source level from " + e.getPath().lastSegment() + " assuming java 8");
                    sourceLevel = SourceLevel.Java8;
                }
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
        // wrong changes (https://github.com/JnRouvignac/AutoRefactor/issues/300)
        removeByClass(rules, SetRatherThanMapRefactoring.class);
        return rules;
    }

    /**
     * Selected rules.
     *
     * @param refactorings
     *            the refactorings argument
     * @param excludedRefactorings
     * @param sourceLevel source level
     *
     * @return List of selected rules
     */
    private List<RefactoringRule> selectRules(List<String> refactorings, List<String> excludedRefactorings, SourceLevel sourceLevel) {
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
        if (!excludedRefactorings.isEmpty()) {
            System.out.println("exclude refactorings: " + excludedRefactorings);
            rules.removeAll(excludedRefactorings);
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
                //new StringRefactoring(), new BigDecimalRefactoring(),
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

	public interface TargetTest {
		Result apply(String code);
	}

    private void deltaDebugRefactorFile(final Target target, final IResource resource, final IPath relativePath,
            final List<RefactoringRule> originalRules, final IPackageFragmentRoot pfr,
            final EffApplyArgs args) throws CoreException, Exception {
        final String originalCode = read(resource);
        DDMin.Result testResult = testRefactorFile(originalCode, target, originalRules, args);
        if (testResult != DDMin.Result.Reproduced) {
            throw new IllegalStateException("cannot start: test case not reproducable");
        }
        final File file = resource.getLocation().toFile();
		try {
            // 1. reduce rules
			final List<RefactoringRule> rules = reduceRules(target, originalCode, originalRules, args);
			if (originalRules.size() > 1) {
				if (rules.size() < originalRules.size()) {
					System.out.println("reduced rule size from " + originalRules.size() + " to " + rules.size());
					System.out.println("applying rules: " + rules);
				} else {
					System.out.println("could not reduce reduced rule size " + originalRules.size());
				}
			}
	        final TargetTest test = new TargetTest() {
				@Override
				public Result apply(String code) {
					return testRefactorFile(code, target, rules, args);
	            }
	        };

            String code = originalCode;
			code = reduceWhitespace(code, test);
			code = splitLines(code, test);

			String previousCode = "";
            while(!code.equals(previousCode)) {
            	previousCode = code;
            	boolean loop;
            	// 2. lines + expressions
            	do {
            		// 2.1. reduce lines
            		code = reduceLines(code, target, test);
            		// 2.2. misc
            		String s = code;
            		code = tryReplacements(code, target, test);
            		loop = !s.equals(code);
            	} while (loop);
                // 3. reduce characters
                code = reduceCharacters(code, target, test);
                if (code.equals(previousCode)) {
        			code = reduceWhitespace(code, test);
        			code = splitLines(code, test);
                    code = tryReplacements(code, target, test);
                }
            }
            writeFile(new File(file.getAbsolutePath() + "-ddmin"), code);
            // TODO: hcak: this currently leaves refactored code in fs where we catch it
            test.apply(code);
            writeFile(new File(file.getAbsolutePath() + "-ddmin-result"), readFileToString(file.getAbsolutePath()));
        } finally {
            writeFile(file, originalCode);
        }
    }

	private List<RefactoringRule> reduceRules(final Target target, final String originalCode,
			final List<RefactoringRule> originalRules, final EffApplyArgs args) {
		final List<RefactoringRule> rules;
		if (originalRules.size() <= 1) {
		    rules = originalRules;
		} else {
		    rules = DDMin.ddMin(originalRules, 2, new DDMin.Predicate<RefactoringRule>() {
		        @Override
		        public Result apply(List<RefactoringRule> newRules) {
		            return testRefactorFile(originalCode, target, newRules, args);
		        }
		    });
		}
		return rules;
	}

    // TODO: apply ddmax when applying "all" fails.
	private String reduceWhitespace(String code, TargetTest test) {
		{
			final String newCode = removeLeadingWhitespace(code);
			if (!code.equals(newCode) && test.apply(newCode) == Result.Reproduced) {
				code = newCode;
			}
		}
		{
			final String newCode = removeTrailingWhitespace(code);
			if (!code.equals(newCode) && test.apply(newCode) == Result.Reproduced) {
				code = newCode;
			}
		}
		{
			final String newCode = collapseWhitespace(code);
			if (!code.equals(newCode) && test.apply(newCode) == Result.Reproduced) {
				code = newCode;
			}
		}
		return code;
	}

	public static String removeLeadingWhitespace(String code) {
		return code.replaceAll("(?m)^[ \t]+", "");
	}

	public static String removeTrailingWhitespace(String code) {
		return code.replaceAll("(?m)[ \t]+$", "");
	}

	public static String collapseWhitespace(String code) {
		return code.replaceAll("[ \t]+", " ");
	}

    // TODO: apply ddmax when applying "all" fails.
	public static String splitLines(String code, TargetTest test) {
		{
			// split before "{" and after "}" and after ")" if that is followed by a character (e.g. @asdf()hello)
			final String newCode = code.replaceAll("(?m)\\{$", "\n{")
					.replaceAll("\\}", "}\n")
					.replaceAll("\\)(\\s*[A-Za-z_])", ")\n$1")
					.replaceAll("\\n\\n+", "\n");
			if (!code.equals(newCode) && test.apply(newCode) == Result.Reproduced) {
				code = newCode;
			}
		}
		return code;
	}

	private String reduceLines(String code, final Target target, final TargetTest test) {
		final List<String> reducedLines = DDMin.ddMin(split(code), 2, new DDMin.Predicate<String>() {
		    @Override
		    public Result apply(List<String> a) {
		        final String newCode = join(a);
		        /*
		         * writeFile(new
		         * File(resource.getLocation().toFile().getAbsolutePath(
		         * )), newCode); refreshResource(resource);
		         */
		        // setContent((IFile)resource, newCode);
		        Result res = test.apply(newCode);
		        if (res == Result.Reproduced) {
		            writeFile(new File(target.getResource().getLocation().toFile().getAbsolutePath() + "-ddmin-latest"),
		                    newCode);
		        }
		        return res;
		    }
		});
		return join(reducedLines);
	}

	private String reduceCharacters(final String code, final Target target, final TargetTest test) {
		final List<Character> reducedChars = DDMin.ddMin(splitChars(code), 2,
		        new DDMin.Predicate<Character>() {
		            @Override
		            public Result apply(List<Character> a) {
		                final String newCode = joinChars(a);
		                Result res = test.apply(newCode);
		                if (res == Result.Reproduced) {
		                    writeFile(new File(
		                            target.getResource().getLocation().toFile().getAbsolutePath() + "-ddmin-latest"),
		                            newCode);
		                }
		                return res;
		            }
		        });
		return joinChars(reducedChars);
	}

	private static class Replace {
		final int start;
		final int end;
		public Replace(int start, int end) {
			this.start = start;
			this.end = end;
		}
		@Override
		public String toString() {
			return "Replace [start=" + start + ", end=" + end + "]";
		}
	}
	// can be optimized to not match "return ;"
	private static Pattern returns = Pattern.compile("(?!<[a-z\"])return[^a-z;][^;]+;");

	// or comparison
	private static Pattern assignment = Pattern.compile("=[^;]+;");

	private static Pattern mayBeGenericParam = Pattern.compile("<[a-zA-Z_0-9?]+>");

	// ascii only ...
	private static Pattern typeName = Pattern.compile("(?<![a-z\"])[A-Z][a-zA-Z_0-9]*");

	// var names longer than 2 characters
	// Note: matches more .... does not match function names (followed by "(")
	private static Pattern name = Pattern.compile("(?<![A-Za-z0-9_\"])(?!\\s*\\()[a-z_][a-zA-Z_0-9]{2,}");

	// null, true, false, ...
	private static Pattern ignore = Pattern.compile("(if|return|while|do|true|false|null)");

    // TODO: needs ddmax!
	// TODO: may introduce loops!
	public static String tryReplacements(final String code, final Target target, TargetTest test) {
		String replaced = code;
		replaced = tryReplace(replaced, returns, "return null;", target, test);
		replaced = tryReplace(replaced, returns, "return true;", target, test);
		replaced = tryReplace(replaced, returns, "return 1;", target, test);
		replaced = tryReplace(replaced, assignment, "=null;", target, test);
		replaced = tryReplace(replaced, assignment, "=true;", target, test);
		replaced = tryReplace(replaced, assignment, "=1;", target, test);
		replaced = tryReplace(replaced, mayBeGenericParam, " ", target, test);
		replaced = tryReplace(replaced, typeName, "Object", target, test);
		replaced = tryReplace(replaced, name, "q", target, test);
		return replaced;
	}

	private static String tryReplace(final String code, Pattern pattern, final String replacement, final Target target,
			final TargetTest test) {
		final List<Replace> edits = new ArrayList<Replace>();
		Matcher m = pattern.matcher(code);
		while (m.find()) {
			if (!ignore.matcher(m.group()).matches()) {
				edits.add(new Replace(m.start(), m.end()));
			}
		}
		//System.out.println("edits=" + edits);
		if (!edits.isEmpty()) {
			// TODO: we want max here!
			final List<Replace> min = DDMin.ddMin(edits, 1, new DDMin.Predicate<Replace>() {
				@Override
				public Result apply(List<Replace> a) {
					// TODO: direct max would be more efficient
					final String newCode = AutoRefactor.apply(code, minus(edits, a), replacement);
					Result res = test.apply(newCode);
					if (res == Result.Reproduced && target != null) {
						writeFile(new File(
								target.getResource().getLocation().toFile().getAbsolutePath() + "-ddmin-latest"),
								newCode);
					}
					return res;
				}
			});
			return AutoRefactor.apply(code, minus(edits, min), replacement);
		} else {
			return code;
		}
	}

	private static List<Replace> minus(final List<Replace> l, List<Replace> sub) {
		List<Replace>  complement = new ArrayList<Replace>(l);
		complement.removeAll(sub);
		return complement;
	}

	// edits are expected in increasing order, non overlapping
	private static String apply(String s, List<Replace> edits, String replacement) {
		int offset = 0;
		for (Replace edit: edits) {
			s = s.substring(0, edit.start + offset) + replacement + s.substring(edit.end + offset, s.length());
			offset += replacement.length() - (edit.end - edit.start);
		}
		return s;
	}

    private String read(final IResource resource) throws CoreException {
        return readFileToString(resource.getLocation().toFile().getAbsolutePath());
    }

    private static void writeFile(File file, final String content) {
        try {
        Writer w = new FileWriter(file);
        try {
            w.write(content);
            w.close();
            w = null;
        } finally {
            if (w != null) {
                try { w.close(); } catch (Exception e) {}
            }
        }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private DDMin.Result testRefactorFile(String code, Target target, final List<RefactoringRule> rules, final EffApplyArgs args) {
    	final IResource resource = target.getResource();
        DDMin.Result testResult = DDMin.Result.Unknown;
        try {
            // pre condition failure
            if (!matchesPreCondition(args, code, resource)) {
                return DDMin.Result.NotReproduced;
            }
            final boolean didRefactor = refactorSourceCode(code, target, rules);
            if (didRefactor) {
            	if (args.deltaDebugTestExceptionPattern != null) {
            		testResult = DDMin.Result.NotReproduced;
            	} else {
                    // System.out.println(codePattern + " tested on " + outCode);
                    testResult = matchesPostCondition(args, resource)
                	    	? DDMin.Result.Reproduced
                            : DDMin.Result.NotReproduced;
            	}
            } else {
            	//System.out.println("warn: no refactoring done");
                testResult = DDMin.Result.NotReproduced;
            }
            /*
        } catch (org.autorefactor.util.IllegalStateException e) {
            // TODO: cleanup
            if (e.getCause() instanceof MalformedTreeException) {
                testResult = DDMin.Result.Reproduced;
            } else {
                e.printStackTrace(System.err);
            }
            */
        } catch (Exception e) {
            final Throwable cause = e.getCause();
            final String s = e.toString() + (cause != null ? ";" + cause: "");
            final Pattern p = args.deltaDebugTestExceptionPattern;
            if (p != null && p.matcher(s).matches()) {
                testResult = DDMin.Result.Reproduced;
            } else {
                e.printStackTrace(System.err);
                System.err.println("delta debugging, testable exception: '" + s + "'");
                testResult = DDMin.Result.NotReproduced;
            }
        }
        return testResult;
    }

	private boolean matchesPreCondition(final EffApplyArgs args, String code, IResource resource) throws IOException {
		final Pattern p = args.deltaDebugBeforeTestCodePattern;
		if (p != null && !p.matcher(code).matches()) {
			return false;
		}
		final String cmd = args.deltaDebugBeforeTestCodeCommand;
		if (cmd != null) {
			File tmpFile = File.createTempFile("/tmp/autorefactor",  "");
			try {
				writeFile(tmpFile, code);
				if (executeCommand(cmd, tmpFile) != 0) {
					return false;
				}
			} finally {
				tmpFile.delete();
			}
		}
		return true;
	}

	private boolean matchesPostCondition(final EffApplyArgs args, IResource resource) throws Exception {
		final Pattern p = args.deltaDebugTestCodePattern;
		if (p != null) {
	        String code = read(resource);
			if (!p.matcher(code).matches()) {
				return false;
			}
		}
		final String cmd = args.deltaDebugTestCodeCommand;
		if (cmd != null && executeCommand(cmd, resource.getLocation().toFile()) != 0) {
			return false;
		}
		return true;
	}

    private int executeCommand(String beforeTestCodeCommand, File arg1) {
    	try {
    		// TODO: use proper exec to avoid stream stalls
    		String[] cmdArray = new String[] { beforeTestCodeCommand, arg1.getAbsolutePath() };
			Process p = Runtime.getRuntime().exec(cmdArray);
    		int exitCode = p.waitFor();
    		//System.out.println("exit code " + exitCode + " when executing " + Arrays.asList(cmdArray));
			return exitCode;
    	} catch (InterruptedException e) {
    		e.printStackTrace(System.err);
    		return 2;
    	} catch (IOException e) {
    		e.printStackTrace(System.err);
    		return 2;
    	}
	}

	private void dumpAst(Target target) throws CoreException, Exception {
	    final IResource resource = target.getResource();
	    final String code = read(resource);
        CompilationUnit dcu = parseCompilationUnit(code, target);
        System.out.println(target.relativePath + ":");
        AstMatcherUtil.dumpAst(dcu);
	}

    private void refactorFile(Target target, final List<RefactoringRule> rules) throws CoreException, Exception {
        final IResource resource = target.getResource();
        final String code = read(resource);
        final boolean didRefactor = refactorSourceCode(code, target, rules);
        if (didRefactor) {
            System.out.println(">> " + resource.getProjectRelativePath());
        }
    }

    /*
     * Some AST infos:
     *  http://www.programcreek.com/2014/01/how-to-resolve-bindings-when-using-eclipse-jdt-astparser/
     */
    /**
     *  @param target
     * @return didRefactor
     */
    private boolean refactorSourceCode(String code, Target target, final List<RefactoringRule> rules) throws Exception {
        final IDocument doc = new Document(code);
        final String name = target.getResource().getName();
        //root.createPackageFragment(packageName, true, null);
        IPackageFragment pf = target.getPackageFragmentRoot().getPackageFragment(packageName(target.getRelativePath()));
        /*
        CompilationUnit dcu = parseCompilationUnit(code, target);
        

        // TODO: ddmin only
        IProblem[] problems = dcu.getProblems();
        // TODO verbose
        //System.out.println("refactorSourceCode: " + name + " (" + target.relativePath + "), #problems: " + problems.length);
        int i = 0;
        boolean error = false;
        for(IProblem problem : problems) {
            error |= problem.isError();
            i++;
            if (i <= 1 || problem.isError()) {
                System.out.println("refactorSourceCode: problem: " + problem.getMessage() + ":" + problem.getSourceStart());
            }
            if (i == 1) {
                //System.out.println("    ...");
            } else if (error) {
                break;
            }
        }
        // TODO: ddmin only
        if (problems.length > 0 && error) {
            //System.out.println("refactorSourceCode: " + name + " (" + unitName + "), aborting on #problems: " + problems.length);
            //System.out.println("    refactorSourceCode: aborting on error");
            return false;
        }

        // Maybe others, too, like EnumDeclaration, AnnotationDeclaration
        //TypeDeclaration typeDeclaration = (TypeDeclaration)dcu.types().get(0);
        //if (typeDeclaration.getAST().hasResolvedBindings()SuperclassType().)
*/
        final IPackageFragment packageFragment = pf; //JavaCoreHelper.getPackageFragment(PACKAGE_NAME);
        final ICompilationUnit cu = packageFragment.createCompilationUnit(
                name, code, true, null);
        cu.getBuffer().setContents(code);
        cu.save(null, true);


        //ICompilationUnit cu = pf.getCompilationUnit(name);
        // TODO: needed only for ddmin usage
        //cu.getBuffer().setContents(code);
        //save(cu);
        return new ApplyRefactoringsJob(null, null, EnvUtil.SIMPLE_ENVIRONMENT).applyRefactoring(
                doc, cu, new AggregateASTVisitor(rules),
                // newJavaProjectOptions(Release.javaSE("1.5.0"),
                // 4));
                newJavaProjectOptions(Release.javaSE("1.7.0"), 4), SubMonitor.convert(new NullProgressMonitor()));
    }

    private CompilationUnit parseCompilationUnit(String code, Target target) {
        ASTParser parser = ASTParser.newParser(AST.JLS8);
        parser.setKind(ASTParser.K_COMPILATION_UNIT);
        // https://stackoverflow.com/questions/2017945/bindings-not-resolving-with-ast-processing-in-eclipse
        parser.setProject(target.getPackageFragmentRoot().getJavaProject());
        // TODO: compute?!?
        final String unitName = "/formatter/" + target.getRelativePath().toString();
        //final String unitName = relativePath.toString();
        parser.setUnitName(unitName);
        parser.setSource(code.toCharArray());

        parser.setResolveBindings(true);
        parser.setBindingsRecovery(false);
        CompilationUnit dcu = (CompilationUnit) parser.createAST(null);
        return dcu;
    }

    private void refreshResource(final IResource resource) {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            resource.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor() {


                @Override
                public void beginTask(String name, int totalWork) {
                    super.beginTask(name, totalWork);
                    System.out.println("refresh: begin task " + name + ", totalWork=" + totalWork);
                }

                @Override
                public void setCanceled(boolean cancelled) {
                    super.setCanceled(cancelled);
                    System.out.println("refresh: cancelled=" + cancelled);
                }

                @Override
                public void done() {
                    System.out.println("refresh: done");
                    latch.countDown();
                }

            });
            latch.await(5, TimeUnit.SECONDS);
        } catch (CoreException e) {
            throw new IllegalArgumentException(e);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Note: tries to be synchronous */
    private void setContent(final IFile resource, String data) {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            resource.setContents(new ByteArrayInputStream(data.getBytes("UTF-8")), true, false, new NullProgressMonitor() {


                @Override
                public void beginTask(String name, int totalWork) {
                    super.beginTask(name, totalWork);
                    System.out.println("setContent: begin task " + name + ", totalWork=" + totalWork);
                }

                @Override
                public void setCanceled(boolean cancelled) {
                    super.setCanceled(cancelled);
                    System.out.println("setContent: cancelled=" + cancelled);
                }

                @Override
                public void done() {
                    System.out.println("setContent: done");
                    latch.countDown();
                }

            });
            latch.await(5, TimeUnit.SECONDS);
        } catch (CoreException e) {
            throw new IllegalArgumentException(e);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException(e);
        }
    }

    /** Note: tries to be synchronous */
    private void save(final ICompilationUnit cu) {
        try {
            final CountDownLatch latch = new CountDownLatch(1);
            cu.save(new NullProgressMonitor() {


                @Override
                public void beginTask(String name, int totalWork) {
                    super.beginTask(name, totalWork);
                    System.out.println("setContent: begin task " + name + ", totalWork=" + totalWork);
                }

                @Override
                public void setCanceled(boolean cancelled) {
                    super.setCanceled(cancelled);
                    System.out.println("setContent: cancelled=" + cancelled);
                }

                @Override
                public void done() {
                    System.out.println("setContent: done");
                    latch.countDown();
                }

            }, true);
            latch.await(5, TimeUnit.SECONDS);
        } catch (CoreException e) {
            throw new IllegalArgumentException(e);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean LINE_MODE = true;

    private List<String> split(final String s) {
        if (LINE_MODE) {
            return new LinkedList<String>(Arrays.asList(s.split("\n")));
        } else {
            // very inefficient ... TODO
            List<String> l = new LinkedList<String>();
            for (int i = 0; i < s.length(); i++) {
                l.add(s.substring(i, i + 1));
            }
            return l;
        }
    }

    private String join(List<String> a) {
        if (LINE_MODE) {
            return join(a, "\n") + "\n";
        } else {
            return join(a, "");
        }
    }

    private String join(List<String> l, String sep) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (String s: l) {
            if (first) {
                first = false;
            } else {
                sb.append(sep);
            }
            sb.append(s);
        }
        return sb.toString();
    }

    private List<Character> splitChars(final String s) {
        // very inefficient ... TODO
        List<Character> l = new LinkedList<Character>();
        for (int i = 0; i < s.length(); i++) {
            l.add(s.charAt(i));
        }
        return l;
    }
    private String joinChars(List<Character> l) {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Character s: l) {
            sb.append(s);
        }
        return sb.toString();
    }
}
