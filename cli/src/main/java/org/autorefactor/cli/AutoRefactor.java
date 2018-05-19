package org.autorefactor.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.autorefactor.cli.ast.AstMatchRule;
import org.autorefactor.cli.ast.AstMatcherBase;
import org.autorefactor.cli.script.jdt.JdtRefactor;
import org.autorefactor.refactoring.RefactoringRule;
import org.autorefactor.refactoring.rules.AllRefactoringRules;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;
import org.eclipse.jdt.core.JavaModelException;

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

    static final Pattern CLASSPATH_VARIABLE_ASSIGNMENT = Pattern.compile("^([^=]+)=(.*)$");

    @Override
    public Object start(IApplicationContext context) throws Exception {
        final String[] argv = (String[]) context.getArguments().get(IApplicationContext.APPLICATION_ARGS);
        try {
        	return run(argv);
        } catch (Exception e) {
        	e.printStackTrace(System.err);
            // prevent launcher messages
            System.setProperty(IApplicationContext.EXIT_DATA_PROPERTY, "");
            return -1;
        	
        }
    }
    
    private Integer run(String[] argv) throws Exception {    
        // System.out.println("args: " + Arrays.asList(argv));

        // parse command line arguments
        final Args args = new Args();
        final ApplyArgs applyArgs = new ApplyArgs();
        final ApplyDDArgs applyDDArgs = new ApplyDDArgs();
        final ListArgs listArgs = new ListArgs();
        final EclipseArgs eclipseArgs = new EclipseArgs();
        AstDumpArgs astDumpArgs = new AstDumpArgs();
        AstMatchArgs astMatchArgs = new AstMatchArgs();
        GenericResolveArgs renameClassArgs = new GenericResolveArgs();
        final JCommander argParser = JCommander.newBuilder().addObject(args)
                .addCommand("list", listArgs)
                .addCommand("apply", applyArgs)
                .addCommand("apply-dd", applyDDArgs)
                .addCommand("ast-dump", astDumpArgs)
                .addCommand("ast-match", astMatchArgs)
                .addCommand("rename-class", renameClassArgs)
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

        if (args.isExperimentalHelp()) {
            argParser.usage();
            return EXIT_OK;
        }
        if (args.isHelp()) {
            final JCommander argParserShort = JCommander.newBuilder().addObject(args)
                    .addCommand("list", listArgs)
                    .addCommand("apply", applyArgs)
                    .addCommand("ast-dump", astDumpArgs)
                    .addCommand("ast-match", astMatchArgs)
                    .addCommand("eclipse", eclipseArgs)
                    .build();
            argParserShort.usage();
            return EXIT_OK;
        }

        final boolean verbose = args.isVerbose();

        boolean usage = false;
        final String cmd = argParser.getParsedCommand();
        if ("apply".equals(cmd)) {
        	final String projectFile = applyArgs.getProjectPath();
        	if (projectFile != null) {
        		applyAutoRefactorings(new File(projectFile), applyArgs, applyArgs.getClassPathVariables(), verbose, applyArgs.getSourceLevel(), Rules.resolveRules(applyArgs.getRefactorings()), applyArgs.getExcludedRefactorings());
        	} else {
        		usage = true;
        	}
        } else if ("apply-dd".equals(cmd)) {
        	final String projectFile = applyDDArgs.getProjectPath();
    		if (projectFile != null) {
        		applyDeltaDebugRefactorings(new File(projectFile), applyDDArgs, applyDDArgs.getClassPathVariables(), verbose);
        	} else {
        		usage = true;
        	}
        } else if ("list".equals(cmd)) {
            listRefactorings();
        } else if ("ast-dump".equals(cmd)) {
            AstTools.dumpProjectAsts(new File(astDumpArgs.getProjectPath()), Collections.<String>emptyList(),  Collections.<String,String>emptyMap(),
                    astDumpArgs, ignoreFilesFilter());
        } else if ("ast-match".equals(cmd)) {
        	final String projectFile = astMatchArgs.getProjectPath();
        	if (projectFile != null) {
        		// TODO: catch and log Exceptions on top level 
        		applyAutoRefactorings(new File(projectFile), astMatchArgs, astMatchArgs.getClassPathVariables(), verbose, 
        				null, Arrays.asList(new AstMatchRule(AstMatcherBase.evaluateMatchExpression(astMatchArgs.getMatchPattern()))), 
        				Collections.emptyList());
        	} else {
        		usage = true;
        	}
        } else if ("rename-class".equals(cmd)) {
        	JdtRefactor.applyRefactorings(renameClassArgs);
        } else if ("eclipse".equals(cmd)) {
            WorkspaceUtil.printEclipseInfo(new File(eclipseArgs.getProjectPath()), null, verbose || eclipseArgs.isVerbose());
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

	@Override
    public void stop() {
    }

	private Predicate<String> ignoreFilesFilter() {
		return (s) -> !filesToIgnore.contains(s);
	}

	private void applyAutoRefactorings(final File projectFile, final CommonResolveArgs applyArgs, Map<String, String> classPathVariables, 
			final boolean verbose, String configuredSourceLevel, List<RefactoringRule> refactorings, List<String> excludedRefactorings)
			throws JavaModelException, CoreException {
		final boolean verboseApply = verbose || applyArgs.isVerbose();

		final List<String> sourceFolders = new ArrayList<String>(applyArgs.getSources());
		SourceLevel sourceLevel = configuredSourceLevel != null ? SourceLevel.fromValue(configuredSourceLevel) : null;
		EffApplyArgs effArgs = new EffApplyArgs(sourceLevel, applyArgs.getIncludePattern(), verboseApply);
		Refactor.refactorProject(projectFile, sourceFolders, classPathVariables,
				refactorings,
				excludedRefactorings,
				effArgs, (target, rules, args) -> { Refactor.refactorFile(target, rules); }, 
				ignoreFilesFilter());
	}

	private void applyDeltaDebugRefactorings(final File projectFile, final ApplyDDArgs applyDDArgs, Map<String, String> classPathVariables,
			final boolean verbose) throws JavaModelException, CoreException {
		final boolean verboseApply = verbose || applyDDArgs.isVerbose();

		final List<String> sourceFolders = new ArrayList<String>(applyDDArgs.getSources());
		SourceLevel sourceLevel = applyDDArgs.getSourceLevel() != null ? SourceLevel.fromValue(applyDDArgs.getSourceLevel()) : null;
		EffApplyArgs effArgs = new EffApplyArgs(sourceLevel,
				applyDDArgs.getIncludePattern(),
				applyDDArgs.getDeltaDebugTestExceptionPattern(),
				applyDDArgs.getDeltaDebugBeforeTestCodePattern(),
				applyDDArgs.getDeltaDebugBeforeTestCodeCommand(),
				applyDDArgs.getDeltaDebugTestCodePattern(),
				applyDDArgs.getDeltaDebugTestCodeCommand(),
				verboseApply, applyDDArgs.isDeltaDebug());
		DeltaDebugRefactor deltaDebugRefactor = new DeltaDebugRefactor();
		Refactor.refactorProject(projectFile, sourceFolders, classPathVariables,
				Rules.resolveRules(applyDDArgs.getRefactorings()),
				applyDDArgs.getExcludedRefactorings(),
				effArgs,
				(target, rules, args) -> { deltaDebugRefactor.deltaDebugRefactorFile(target, rules, args); },
				ignoreFilesFilter());
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

    private final Set<String> filesToIgnore = new HashSet<String>(Arrays.asList(
    		// TODO: try again with autorefactor 2.0
            // very slow or really hangup (Comment Formatter)
            "ShortKeyUtilN.java", "ShortKeyUtil.java", "BlowfishEngine30.java", "HotfolderManager.java",
            // for "all"
            "CollectionObject.java",
            // some buggy refactoring in "all"
            "LicenseCheck.java", "AutoDoc.java"));
}
