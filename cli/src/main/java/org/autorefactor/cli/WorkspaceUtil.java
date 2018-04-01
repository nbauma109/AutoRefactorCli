package org.autorefactor.cli;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.autorefactor.util.Pair;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceDescription;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

@SuppressWarnings("restriction")
public class WorkspaceUtil {

	static List<String> allProjectSourceFolders(final IJavaProject javaProject) throws JavaModelException {
	    List<String> src = new ArrayList<String>();
	    for (IClasspathEntry cp: javaProject.getRawClasspath()) {
	        if (cp.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
	            src.add(cp.getPath().makeRelativeTo(javaProject.getPath()).toString());
	        }
	    }
	    return src;
	}

	static IProject createProject(final IWorkspace workspace, final File projectFile, final boolean verbose)
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

	static IPath relativePath(final IFolder f, IResource resource) {
	    return resource.getFullPath().removeFirstSegments(f.getFullPath().segmentCount());
	}

	static void printProjectDetails(IProject p) throws JavaModelException {
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

	private static void disableAutoBuild(final IWorkspace workspace) throws CoreException {
	    IWorkspaceDescription desc = workspace.getDescription();
	    boolean isAutoBuilding = desc.isAutoBuilding();
	    if (isAutoBuilding) {
	        //System.out.println("eclipse workspace: disabling auto build");
	        desc.setAutoBuilding(false);
	        workspace.setDescription(desc);
	    }
	}

	public static Pair<IWorkspace, IProject> prepareProject(final File projectFile,
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

	static void printEclipseInfo(final File projectFile,
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

	/** Note: tries to be synchronous */
	@SuppressWarnings("unused")
	private static void save(final ICompilationUnit cu) {
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

	/** Note: tries to be synchronous */
	@SuppressWarnings("unused")
	private static void setContent(final IFile resource, String data) {
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

	@SuppressWarnings("unused")
	private static void refreshResource(final IResource resource) {
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
}
