package org.autorefactor.cli.script.jdt;

import java.io.File;
import java.util.function.Consumer;

import org.autorefactor.cli.CommonResolveArgs;
import org.autorefactor.cli.WorkspaceUtil;
import org.autorefactor.util.Pair;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.TypeDeclaration;

@SuppressWarnings("restriction")
public class JdtRefactor {

	// TODO: total hack ...
	public static void buildProject(final IWorkspace workspace, final IProject project, final NullProgressMonitor npm)
			throws CoreException {
		workspace.save(true, npm);
		project.refreshLocal(IResource.DEPTH_INFINITE, npm);
		/* @param kind the kind of build being requested. Valid values are:
		 *		<ul>
		 *		<li> <code>IncrementalProjectBuilder.FULL_BUILD</code> - indicates a full build.</li>
		 *		<li> <code>IncrementalProjectBuilder.INCREMENTAL_BUILD</code> - indicates an incremental build.</li>
		 * 		<li><code>CLEAN_BUILD</code>- indicates a clean request. Clean does
		 * 		not actually build anything, but rather discards all problems and build states.
		 *		</ul>
		 */
		// TODO: use workspace build?
		// not enough
		//project.build(IncrementalProjectBuilder.INCREMENTAL_BUILD, npm);
		project.build(IncrementalProjectBuilder.CLEAN_BUILD, npm);
		project.build(IncrementalProjectBuilder.FULL_BUILD, npm);
	}

	public static void applyRefactoringToType(IJavaProject javaProject, IPackageFragmentRoot pfr, 
				String packageName, String fileName, Consumer<IType> consumer) throws CoreException {
			//System.out.println("pfr=" + pfr);
			final IPackageFragment pf = pfr.getPackageFragment(packageName);
			//System.out.println("pf=" + pf);
	  	
			// TODO: is this code block needed?
			
			//String code = FileUtil.read(file);
			
			//final ICompilationUnit cu1 = pf.createCompilationUnit("OIDSearchResult.java", code, true, null);
			final ICompilationUnit icu = pf.getCompilationUnit(fileName);
			//icu.getBuffer().setContents(code);
			//icu.save(null, true);
	
			@SuppressWarnings("deprecation")
			ASTParser parser = ASTParser.newParser(AST.JLS8);
			parser.setProject(javaProject);
			parser.setResolveBindings(true);
			parser.setBindingsRecovery(true);
			parser.setSource(icu);
			parser.setKind(ASTParser.K_COMPILATION_UNIT);
			CompilationUnit cu = (CompilationUnit) parser.createAST(null);
			TypeDeclaration cl = (TypeDeclaration)cu.types().get(0);
	//		System.out.println("typeDecl.resolvedBindings=" + cl.resolveBinding());
			IType type = (IType)cl.resolveBinding().getJavaElement();
	//		System.out.println("type=" + type);
			consumer.accept(type);
		}

	public static void applyRefactorings(CommonResolveArgs args)
			throws CoreException, JavaModelException {
		final Pair<IWorkspace, IProject> p = WorkspaceUtil.prepareProject(new File(args.getProjectPath()), args.getClassPathVariables(), args.isVerbose());
		final IWorkspace workspace = p.getFirst();
		final NullProgressMonitor npm = new NullProgressMonitor();
		// TODO: check if needed
		workspace.save(true, npm);
		final IProject project = (IProject)p.getSecond();
		final IJavaProject javaProject = JavaCore.create(project);
	
		JdtRename.renameClass(javaProject, "src/main/java", "some.package", "Old.java", "New");
		JdtExtractType.extractInterface(javaProject, "src/main/java", "some.package", "SomeClass.java", "AnInterface");
	}

}
