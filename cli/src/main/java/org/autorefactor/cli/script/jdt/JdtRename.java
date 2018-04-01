package org.autorefactor.cli.script.jdt;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameMethodProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameTypeProcessor;
import org.eclipse.jdt.internal.corext.refactoring.rename.RenameVirtualMethodProcessor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RefactoringParticipant;
import org.eclipse.ltk.core.refactoring.participants.RenameProcessor;
import org.eclipse.ltk.core.refactoring.participants.RenameRefactoring;
import org.eclipse.ltk.core.refactoring.participants.SharableParticipants;

/**
 * 
 * https://stackoverflow.com/questions/12968328/how-to-programmatically-rename-a-method-using-jdt
 */
@SuppressWarnings("restriction")
public class JdtRename {

	public static void renameClass(final IJavaProject javaProject, String srcFolder, String srcPackage, String srcFileName, String newName) throws CoreException {
		final IFolder sourceFolder = javaProject.getProject().getFolder(srcFolder);
	
		final IPackageFragmentRoot pfr = javaProject.getPackageFragmentRoot(sourceFolder);
		JdtRefactor.applyRefactoringToType(javaProject, pfr, srcPackage, srcFileName, 
				type ->	renameClass(type, newName));
	}
	

    public static void renameMethod(IMethod methodToRename ) throws CoreException {
    	// TODO: check headless rcp example application
    	// TODO: search for headless refactoring app.
    	
    	RenameMethodProcessor processor = new RenameVirtualMethodProcessor(methodToRename);
    	processor.setUpdateReferences(true);
    	processor.setNewElementName("newMethodName");

    	RenameRefactoring fRefactoring = new RenameRefactoring(processor);
    	Change fChange = fRefactoring.createChange(new NullProgressMonitor());
    	fChange.initializeValidationData(new NullProgressMonitor());
    	fChange.perform(new NullProgressMonitor());
    	
    	/*
    	// or
    	/*
    	RenameSupport handles all the UI to configure your refactoring, but since you're doing it programatically 
    	you just need the RenameRefactoring and the processor, configured using the various processor.set*() methods.
    	* /
    	RenameSupport support = RenameSupport.create(methodToRename, "newMethidName", 0);
    	//support.perform(null,null);
    	 *
    	 */
    }
    public static void renameClass(IType typeToRename, String newName ) {
    	// TODO: introduce semantic exception
		try {
			innerRenameClass(typeToRename, newName);
		} catch (CoreException e) {
			throw new IllegalStateException(e);
		}
    }
    	
    private static void innerRenameClass(IType typeToRename, String newName ) throws CoreException {
    	/*
    	// wait for running workbench (can we do without one?)
    	// https://www.eclipsecon.org/europe2016/content/testing-eclipse-plug-ins-lessons-field
    	if (!PlatformUI.isWorkbenchRunning()) {
    		WorkbenchAdvisor workbenchAdvisor = new WorkbenchAdvisor() {
    			@Override
    			public String getInitialWindowPerspectiveId() {
    				return null;
    			}
    		};
    		// TODO: improve
    		System.out.println("waiting for workbench");
    		try {
				Thread.sleep(15000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
       		System.out.println("workbench; running=" + PlatformUI.isWorkbenchRunning());
    		//PlatformUI.createAndRunWorkbench(PlatformUI.createDisplay(), workbenchAdvisor);
    	}
    	*/


    	final RenameTypeProcessor processor = new RenameTypeProcessor(typeToRename);
    	processor.setUpdateReferences(true);
    	processor.setNewElementName(newName);
    	
    	NullProgressMonitor npm = new NullProgressMonitor();
    	
    	// TODO: try to avoid this dance with some explicit bundle list.
    	//final RenameRefactoring fRefactoring = new RenameRefactoring(processor);
    	final RenameRefactoring fRefactoring = new RenameRefactoring(new UnsupportedRefactoringProcessor() {

    		@Override
    		public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
    				throws CoreException, OperationCanceledException {
    			return processor.checkInitialConditions(pm);
    		}

			@Override
    		public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context)
    				throws CoreException, OperationCanceledException {
    			return processor.checkFinalConditions(pm, context);
    		}

    		@Override
    		public RefactoringParticipant[] loadParticipants(RefactoringStatus status,
    				SharableParticipants sharedParticipants) throws CoreException {
    			return new RefactoringParticipant[0];
    		}

    		@Override
    		public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
    			return processor.createChange(pm);
    		}

			@Override
			public String getProcessorName() {
				return processor.getProcessorName();
			}
    	});

    	final RefactoringStatus checkResult1 = fRefactoring.checkInitialConditions(npm);
		System.out.println("initialConditions:" + checkResult1);
		if (!checkResult1.isOK()) {
			throw new IllegalStateException("refactoring initial precondition failed: " + checkResult1);
		}

		//fRefactoring.setProcessor(new UnsupportedRefactoringProcessor());
    	// without checkFinalCConditions NPE in createChange
    	// but with normal processor some workbench/ui is tried to be installed
    	final RefactoringStatus checkResult = fRefactoring.checkFinalConditions(npm);
    	
    	// does not change the effective processor of rename refactoring ...
    	//fRefactoring.setProcessor(processor);
    	
		System.out.println("finalConditions:" + checkResult);
		if (/*!checkResult.isOK() && */checkResult.hasFatalError()) {
			throw new IllegalStateException("refactoring precondition failed: " + checkResult);
		}
    	Change fChange = fRefactoring.createChange(npm);
    	fChange.initializeValidationData(npm);
    	fChange.perform(npm);
    }

    private static class UnsupportedRefactoringProcessor extends RenameProcessor {
		@Override
		public RefactoringParticipant[] loadParticipants(RefactoringStatus status, SharableParticipants sharedParticipants)
				throws CoreException {
			throw new UnsupportedOperationException("not implemented");
		}

		@Override
		public boolean isApplicable() throws CoreException {
			throw new UnsupportedOperationException("not implemented");
		}

		@Override
		public String getProcessorName() {
			throw new UnsupportedOperationException("not implemented");
		}

		@Override
		public String getIdentifier() {
			throw new UnsupportedOperationException("not implemented");
		}

		@Override
		public Object[] getElements() {
			throw new UnsupportedOperationException("not implemented");
		}

		@Override
		public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
			throw new UnsupportedOperationException("not implemented");
		}

		@Override
		public RefactoringStatus checkInitialConditions(IProgressMonitor pm)
				throws CoreException, OperationCanceledException {
			throw new UnsupportedOperationException("not implemented");
		}

		@Override
		public RefactoringStatus checkFinalConditions(IProgressMonitor pm, CheckConditionsContext context)
				throws CoreException, OperationCanceledException {
			throw new UnsupportedOperationException("not implemented");
		}
	}
}
