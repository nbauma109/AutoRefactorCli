package org.autorefactor.cli;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import org.autorefactor.matcher.AstMatcherUtil;
import org.autorefactor.util.Pair;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

/**
 * Tools related to JDT dom AST.
 * 
 * @author cal
 */
@SuppressWarnings("restriction")
public class AstTools {

	static CompilationUnit parseCompilationUnit(String code, Refactor.RefactorTarget target) {
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
	    return (CompilationUnit) parser.createAST(null);
	}

	static void dumpProjectAsts(final File projectFile, final List<String> originalSourceFolders,
	        final Map<String, String> classPathVariables, final CommonArgs args, Predicate<String> fileFilter2)
	                throws JavaModelException, CoreException {
	    final boolean verbose = args.isVerbose();
	    final Pair<IWorkspace, IProject> projectCtx = WorkspaceUtil.prepareProject(projectFile, classPathVariables, verbose);
	    final IWorkspace workspace = projectCtx.getFirst();
	    final IProject project = projectCtx.getSecond();
	
	    final IJavaProject javaProject = JavaCore.create(project);
	    List<String> sourceFolders = new ArrayList<String>(originalSourceFolders);
	    if (sourceFolders.isEmpty()) {
	        sourceFolders.addAll(WorkspaceUtil.allProjectSourceFolders(javaProject));
	    }
	
	    if (verbose) {
	        System.out.println("dumpAst: starting");
	        System.out.println("dumpAst: source folders: " + sourceFolders);
	    }
	    try {
	        for (String src : sourceFolders) {
	            final IFolder sourceFolder = project.getFolder(src);
	            dumpFolderAsts(javaProject, sourceFolder, args.getIncludePattern(), args.isVerbose(), fileFilter2);  
	        }
	    } finally {
	        javaProject.save(null, true);
	        javaProject.close();
	        workspace.save(true, null);
	    }
	}

	private static void dumpFolderAsts(final IJavaProject project, final IFolder sourceFolder,
	        final Pattern filenamePattern, final boolean verbose, Predicate<String> filter2) throws CoreException {
	    final IPackageFragmentRoot pfr = project.getPackageFragmentRoot(sourceFolder);
	    FileUtil.walkMax(sourceFolder, 1000000, new IResourceVisitor() {
	        @Override
	        public boolean visit(IResource resource) throws CoreException {
	            try {
	                if ("java".equals(resource.getFileExtension())) {
	                    final String name = resource.getName();
	                    IPath relativePath = WorkspaceUtil.relativePath(sourceFolder, resource);
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
	                    if (!filter2.test(name)) {
	                        if (verbose) {
	                            System.out.println("    ignored");
	                        }
	                        return true;
	                    }
	                    dumpAst(new Refactor.RefactorTarget(pfr, resource, relativePath));
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	            return true;
	        }
	    });
	}

	private static void dumpAst(Refactor.RefactorTarget target) throws CoreException, Exception {
	    final IResource resource = target.getResource();
	    final String code = FileUtil.read(resource);
	    CompilationUnit dcu = parseCompilationUnit(code, target);
	    System.out.println(target.relativePath + ":");
	    AstMatcherUtil.dumpAst(dcu);
	}

}
