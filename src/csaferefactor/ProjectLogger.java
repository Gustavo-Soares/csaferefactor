package csaferefactor;

import java.io.File;
import java.io.IOException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import csaferefactor.runnable.VMInitializerRunnable;

import saferefactor.core.util.FileUtil;
import saferefactor.rmi.common.RemoteExecutor;

public class ProjectLogger {

	private final String sourceFolder;
	private int counter = 0;
	private String projectPath;

	private List<Snapshot> snapshotList = new LinkedList<Snapshot>();

	private static ProjectLogger instance;

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

	public static ProjectLogger getInstance() {
		if (instance == null)
			instance = new ProjectLogger();
		return instance;
	}

	public Snapshot log() throws IOException {

		Snapshot result = new Snapshot();

		// copying binary files to temp folder
		File generatedFolder = copyBinFilesToTmpFolder();
		result.setPath(generatedFolder.getAbsolutePath());
		result.setServerName(generatedFolder.getName());

		// crate generator server
		VMInitializerRunnable vmInitializer = new VMInitializerRunnable(
				result.getServerName(), result.getPath());
		// vmInitializer.schedule();
		// vmInitializer.setPriority(Job.SHORT);
		Future<?> submit = result.getExecutor().submit(vmInitializer);

		snapshotList.add(result);
		return result;
	}

	private File copyBinFilesToTmpFolder() throws IOException {
		File source = new File(sourceFolder);
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

	public void deleteSnapshot(int index, boolean removeFromArray) {
		synchronized (snapshotList) {

			Snapshot targetSnapshot = snapshotList.get(index);
			targetSnapshot.getExecutor().shutdownNow();
			// stop server

			try {
				Registry registry = LocateRegistry.getRegistry("localhost");
				RemoteExecutor generatorServer = (RemoteExecutor) registry
						.lookup(targetSnapshot.getServerName());
				generatorServer.exit();
			} catch (NotBoundException e) {
				// if the server is not loaded, do no to anything
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			// stop thread
			targetSnapshot.getExecutor().shutdownNow();
  
			// remove from List
			if (removeFromArray)
				snapshotList.remove(index);
			System.out.println("snapshot " + targetSnapshot.getServerName()
					+ "deleted");
		}
	}

	public void clean() {
		int i = 0;
		for (Iterator<Snapshot> iterator = this.snapshotList.iterator(); iterator
				.hasNext();) {
			Snapshot snapshot = iterator.next();
			deleteSnapshot(i, false);
			iterator.remove();
			i++;
		}
		System.out.println("List was cleaned");

	}

}
