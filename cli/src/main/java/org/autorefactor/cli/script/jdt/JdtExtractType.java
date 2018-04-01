package org.autorefactor.cli.script.jdt;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.corext.codemanipulation.CodeGenerationSettings;
import org.eclipse.jdt.internal.corext.refactoring.structure.ExtractInterfaceProcessor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.ProcessorBasedRefactoring;

@SuppressWarnings("restriction")
public class JdtExtractType {

	public static void extractInterface(final IJavaProject javaProject, String srcFolder, String srcPackage, String srcFileName, String newName) throws CoreException {
		final IFolder sourceFolder = javaProject.getProject().getFolder(srcFolder);
	
		final IPackageFragmentRoot pfr = javaProject.getPackageFragmentRoot(sourceFolder);
		JdtRefactor.applyRefactoringToType(javaProject, pfr, srcPackage, srcFileName, 
				type ->	extractInterface(type, newName));
	}

    public static void extractInterface(IType sourceType, String newName ) {
    	// TODO: introduce semantic exception
		try {
			innerExtractInterface(sourceType, newName);
		} catch (CoreException e) {
			throw new IllegalStateException(e);
		}
    }
    
	private static void innerExtractInterface(IType sourceType, String newName ) throws CoreException {
    	IMethod[] methods = sourceType.getMethods();

    	IMember[] members = new IMember[1];
    	members[0] = methods[methods.length - 1];
    	
    	CodeGenerationSettings settings = new CodeGenerationSettings();
    	
    	final ExtractInterfaceProcessor processor = new ExtractInterfaceProcessor(sourceType, settings);
    	// generate annotations
    	processor.setAnnotations(true);
    	processor.setExtractedMembers(members);
    	processor.setTypeName(newName);
    	//processor.setReplace(replace);
    	
    	NullProgressMonitor npm = new NullProgressMonitor();
    	
    	final ProcessorBasedRefactoring fRefactoring = new ProcessorBasedRefactoring(processor);

    	final RefactoringStatus checkResult1 = fRefactoring.checkInitialConditions(npm);
		System.out.println("initialConditions:" + checkResult1);
		if (!checkResult1.isOK()) {
			throw new IllegalStateException("refactoring initial precondition failed: " + checkResult1);
		}

    	final RefactoringStatus checkResult = fRefactoring.checkFinalConditions(npm);
    	
		System.out.println("finalConditions:" + checkResult);
		if (/*!checkResult.isOK() && */checkResult.hasFatalError()) {
			throw new IllegalStateException("refactoring precondition failed: " + checkResult);
		}
    	Change fChange = fRefactoring.createChange(npm);
    	fChange.initializeValidationData(npm);
    	fChange.perform(npm);
    }
}
