package org.autorefactor.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

import org.autorefactor.refactoring.ApplyRefactoringsJob;
import org.autorefactor.refactoring.JavaProjectOptions;
import org.autorefactor.refactoring.JavaProjectOptionsImpl;
import org.autorefactor.refactoring.RefactoringRule;
import org.autorefactor.refactoring.Release;
import org.autorefactor.refactoring.rules.AggregateASTVisitor;
import org.autorefactor.util.Pair;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;

/**
 * Refactor file.
 */
@SuppressWarnings("restriction")
public class Refactor {

	/**
	 * Combined information. 
	 */
	static class RefactorTarget {
		private final IPackageFragmentRoot packageFragmentRoot;
		private final IResource resource;
		final IPath relativePath;
		
		public RefactorTarget(IPackageFragmentRoot packageFragmentRoot, IResource resource, IPath relativePath) {
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

	public static interface RefactorProcedure {
		void refactor(RefactorTarget target, List<RefactoringRule> rules, EffApplyArgs args) throws Exception;
	}

	/**
	 * Entered by apply and apply with delta debugging.
	 * @param fileFilter 
	 */
	static void refactorSourceFolder(final IJavaProject project,
			final Refactor.RefactorProcedure refactor,
			final IFolder sourceFolder,
	        final List<RefactoringRule> rules, final EffApplyArgs args, Predicate<String> fileFilter) throws CoreException {
	    final boolean verbose = args.verbose;
	    final IPackageFragmentRoot pfr = project.getPackageFragmentRoot(sourceFolder);
	    FileUtil.walkMax(sourceFolder, 1000000, new IResourceVisitor() {
	        @Override
	        public boolean visit(IResource resource) throws CoreException {
	            try {
	                if ("java".equals(resource.getFileExtension())) {
	                    final String name = resource.getName();
	                    IPath relativePath = WorkspaceUtil.relativePath(sourceFolder, resource);
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
	                    if (!fileFilter.test(name)) {
	                        if (verbose) {
	                            System.out.println("    ignored");
	                        }
	                        return true;
	                    }
	                    refactor.refactor(new RefactorTarget(pfr, resource, relativePath), rules, args);
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	            return true;
	        }
	    });
	}

	static SourceLevel sourceLevel(final IJavaProject project) throws JavaModelException {
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

	static void refactorProject(final File projectFile, final List<String> originalSourceFolders,
	        final Map<String, String> classPathVariables, List<RefactoringRule> refactorings, List<String> excludedRefactorings,
	        final EffApplyArgs args, Refactor.RefactorProcedure refactorProcedure, Predicate<String> fileFilter)
	                throws JavaModelException, CoreException {
	    final boolean verbose = args.verbose;
	    final Pair<IWorkspace, IProject> projectCtx = WorkspaceUtil.prepareProject(projectFile, classPathVariables, verbose);
	    final IWorkspace workspace = projectCtx.getFirst();
	    final IProject project = projectCtx.getSecond();
	
	    final IJavaProject javaProject = JavaCore.create(project);
	    List<String> sourceFolders = new ArrayList<String>(originalSourceFolders);
	    if (sourceFolders.isEmpty()) {
	        sourceFolders.addAll(WorkspaceUtil.allProjectSourceFolders(javaProject));
	    }
	
	    if (verbose) {
	        System.out.println("refactor: starting refactoring");
	        System.out.println("refactor: source folders: " + sourceFolders);
	    }
	    final SourceLevel sourceLevel = args.sourceLevel != null ? args.sourceLevel : sourceLevel(javaProject);
		final List<RefactoringRule> rules = Rules.filterRules(refactorings, excludedRefactorings, sourceLevel);
	    if (verbose) {
	        System.out.println("refactor: rules: " + rules);
	    }
	    try {
	        for (String src : sourceFolders) {
	            final IFolder sourceFolder = project.getFolder(src);
	            refactorSourceFolder(javaProject, refactorProcedure, sourceFolder, rules, args, fileFilter);
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

	static String packageName(IPath path) {
	    return path.removeLastSegments(1).toString().replace("/", ".");
	}

	static JavaProjectOptions newJavaProjectOptions(Release javaSE, int tabSize) {
	    final JavaProjectOptionsImpl options = new JavaProjectOptionsImpl();
	    options.setTabSize(tabSize);
	    options.setJavaSERelease(javaSE);
	    return options;
	}

	static void refactorFile(Refactor.RefactorTarget target, final List<RefactoringRule> rules) throws CoreException, Exception {
        final IResource resource = target.getResource();
        final String code = FileUtil.read(resource);
        final boolean didRefactor = refactorSourceCode(code, target, rules);
        if (didRefactor) {
            System.out.println(">> " + resource.getProjectRelativePath());
        }
    }

	/**
	 * @return didRefactor
	 */
	static boolean refactorSourceCode(String code, RefactorTarget target, final List<RefactoringRule> rules) throws Exception {
	    final IPackageFragment pf = target.getPackageFragmentRoot().getPackageFragment(packageName(target.getRelativePath()));
	    final String name = target.getResource().getName();
	    final IDocument doc = new Document(code);
	
	    // TODO: is this code block needed?
	    final IPackageFragment packageFragment = pf; //JavaCoreHelper.getPackageFragment(PACKAGE_NAME);
	    final ICompilationUnit cu = packageFragment.createCompilationUnit(name, code, true, null);
	    cu.getBuffer().setContents(code);
	    cu.save(null, true);
	
	    //ICompilationUnit cu = pf.getCompilationUnit(name);
	    // TODO: needed only for ddmin usage
	    //cu.getBuffer().setContents(code);
	    //save(cu);
	    return new ApplyRefactoringsJob(null, null, EnvUtil.SIMPLE_ENVIRONMENT).applyRefactoring(
	            doc, cu, new AggregateASTVisitor(rules),
	            newJavaProjectOptions(Release.javaSE("1.7.0"), 4), SubMonitor.convert(new NullProgressMonitor()));
	}
}
