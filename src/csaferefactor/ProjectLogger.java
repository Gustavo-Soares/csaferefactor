package csaferefactor;

import java.io.File;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Future;

import org.designwizard.main.DesignWizard;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import saferefactor.core.util.FileUtil;
import saferefactor.rmi.common.RemoteExecutor;
import csaferefactor.runnable.DesignWizardCallable;
import csaferefactor.runnable.VMInitializerRunnable;

public class ProjectLogger {

	private final String sourceFolder;
	private int counter = 0;
	private String projectPath;

	private List<Snapshot> snapshotList = new LinkedList<Snapshot>();

	private volatile static ProjectLogger instance = null;

	/**
	 * Private constructor for Singleton
	 */
	private ProjectLogger() {
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IPath location = root.getLocation();
		String str = location.toPortableString();

		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IEditorPart editorPart = page.getActiveEditor();
		IJavaElement javaElement = JavaUI.getEditorInputJavaElement(editorPart
				.getEditorInput());
		IJavaProject javaProject = javaElement.getJavaProject();

		projectPath = javaProject.getProject().getFullPath().toString();
		sourceFolder = str + projectPath + "/bin";
	}

	/**
	 * Singleton for ProjectLogger
	 * 
	 * @return The instance of this class
	 */
	public static ProjectLogger getInstance() {
		// double-checked locking
		if (instance == null) {
			synchronized (ProjectLogger.class) {
				if (instance == null)
					instance = new ProjectLogger();
			}
		}
		return instance;
	}

	public Snapshot log() throws IOException {

		Snapshot result = new Snapshot();

		DesignWizardCallable designWizardCallable = new DesignWizardCallable(
				ProjectLogger.getInstance().getSourceFolder());
		Future<DesignWizard> futureDesignWizard = SafeRefactorActivator
				.getDefault().getExecutor().submit(designWizardCallable);

		result.setFutureDesignWizard(futureDesignWizard);

		// copying binary files to temp folder
		File generatedFolder = copyBinFilesToTmpFolder();
		result.setPath(generatedFolder.getAbsolutePath());
		result.setServerName(generatedFolder.getName());

		// crate generator server
		VMInitializerRunnable vmInitializer = new VMInitializerRunnable(
				result.getServerName(), result.getPath());
		SafeRefactorActivator.getDefault().log("logando...");
		Future<Boolean> submit = SafeRefactorActivator.getDefault()
				.getExecutor().submit(vmInitializer);
		result.setFutureIsServerLoaded(submit);

		snapshotList.add(result);

		return result;
	}

	private File copyBinFilesToTmpFolder() throws IOException {
		File source = new File(getSourceFolder());
		String logDir = System.getProperty("java.io.tmpdir");
		String targetFolder = logDir + projectPath + counter;
		File target = new File(targetFolder);
		while (target.exists()) {
			counter++;
			targetFolder = logDir + projectPath + counter;
			target = new File(targetFolder);
		}
		target.mkdir();
		FileUtil.copyFolder(source, target);
		return target;
	}

	public List<Snapshot> getSnapshotList() {
		return snapshotList;
	}

	public void deleteSnapshot(int index, boolean deleteSnapshot) {
		synchronized (snapshotList) {

			Snapshot targetSnapshot = snapshotList.get(index);
			removeSnapshot(targetSnapshot, deleteSnapshot);
		}
	}

	private void removeSnapshot(Snapshot targetSnapshot, boolean deleteSnapshot) {
		// stop server

		try {
			Registry registry = LocateRegistry.getRegistry("localhost");
			RemoteExecutor generatorServer = (RemoteExecutor) registry
					.lookup(targetSnapshot.getServerName());
			generatorServer.exit();
		} catch (NotBoundException e) {
			// if the server is not loaded, do no to anything
		} catch (RemoteException e) {
			e.printStackTrace();
		}

		// stop thread
		targetSnapshot.getFutureIsServerLoaded().cancel(true);

		// remove from List
		if (deleteSnapshot)
			snapshotList.remove(targetSnapshot);
		System.out.println("snapshot " + targetSnapshot.getServerName()
				+ "deleted");
	}

	public void clean() {
		for (Iterator<Snapshot> iterator = this.snapshotList.iterator(); iterator
				.hasNext();) {
			Snapshot snapshot = iterator.next();
			removeSnapshot(snapshot, false);
			iterator.remove();
		}
		System.out.println("List was cleaned");
	}

	public String getSourceFolder() {
		return sourceFolder;
	}

}
