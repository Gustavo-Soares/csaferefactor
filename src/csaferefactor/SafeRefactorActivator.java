package csaferefactor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import csaferefactor.listener.BuildListener;
import csaferefactor.listener.JavaElementChangedListener;
import csaferefactor.listener.PartListener;

import saferefactor.rmi.common.RemoteExecutor;

/**
 * The activator class controls the plug-in life cycle
 */
public class SafeRefactorActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "test"; //$NON-NLS-1$

	// The shared instance
	private static SafeRefactorActivator plugin;

	private Registry registry = null;

	private ExecutorService executor = Executors.newCachedThreadPool();

	private boolean initComplete;

	public static final String SAFEREFACTOR_MARKER = "csaferefactor.saferefactorproblem";

	/**
	 * The constructor
	 */
	public SafeRefactorActivator() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
	 * )
	 */
	public void stop(BundleContext context) throws Exception {
		Registry registry = LocateRegistry.getRegistry("localhost");

		List<Snapshot> snapshotList = ProjectLogger.getInstance()
				.getSnapshotList();
		for (Snapshot snapshot : snapshotList) {
			RemoteExecutor generatorServer = (RemoteExecutor) registry
					.lookup(snapshot.getServerName());
			generatorServer.exit();
		}

		plugin = null;

		super.stop(context);
	}

	public void removeExistingPluginMarkers(ICompilationUnit compilationUnit)
			throws CoreException {
		IResource resource = compilationUnit.getResource();
		removeExistingPluginmarkers(resource);
	}

	public void removeExistingPluginmarkers(IResource resource)
			throws CoreException {
		IMarker[] findMarkers = resource.findMarkers(
				SafeRefactorActivator.SAFEREFACTOR_MARKER, true, 1);
		for (IMarker iMarker : findMarkers) {
			iMarker.delete();
			System.out.println("marker removed");
		}
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static SafeRefactorActivator getDefault() {
		return plugin;
	}

	/**
	 * Returns an image descriptor for the image file at the given plug-in
	 * relative path
	 * 
	 * @param path
	 *            the path
	 * @return the image descriptor
	 */
	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}

	/**
	 * @return The File Location of this plugin
	 */
	public String getPluginFolder() {
		URL url = getBundle().getEntry("/");
		try {
			url = Platform.asLocalURL(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return url.getPath();
	}

	public String getSafeRefactorJarPath() throws URISyntaxException,
			IOException {
		return getPath("lib/saferefactor-beta.jar");
	}

	public String getSecurityPolicyPath() throws URISyntaxException,
			IOException {
		return getPath("/server.policy");
	}

	public String getBinPath() throws URISyntaxException, IOException {
		return getPath("/bin/");
	}

	private String getPath(String relativePath) throws URISyntaxException,
			IOException {
		// File file = new File(getPluginFolder() + relativePath);

		// IPath path = new Path(relativePath);
		// URL pathUrl = FileLocator.find(getBundle(),
		// path, Collections.EMPTY_MAP);
		// File file = new File(FileLocator.toFileURL(
		// pathUrl).toURI());
		// String filePath = file.getAbsolutePath();
		return getPluginFolder() + relativePath;
	}

	public Registry getRegistry() {
		return registry;
	}

	public ExecutorService getExecutor() {
		return executor;
	}

	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}

	public void finishInit() throws IOException {
		configureRMI();

		IWorkbenchWindow activeWorkbenchWindow = SafeRefactorActivator
				.getDefault().getWorkbench().getActiveWorkbenchWindow();

		if (activeWorkbenchWindow == null)
			return;
		IWorkbenchPage page = activeWorkbenchWindow.getActivePage();

		if (page == null)
			return;

		// set listener
		JavaCore.addElementChangedListener(
				new JavaElementChangedListener(page.getActiveEditor()),
				ElementChangedEvent.POST_RECONCILE);

		IResourceChangeListener listener = new BuildListener(
				page.getActiveEditor());
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener,
				IResourceChangeEvent.POST_BUILD);
		// log current project
		ProjectLogger.getInstance().log();

		IPartListener partListener = new PartListener(page.getActiveEditor()
				.getTitle());
		final IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow();
		if (workbenchWindow != null) {
			IPartService partService = workbenchWindow.getPartService();
			partService.addPartListener(partListener);
		}

	}

	private void configureRMI() throws RemoteException {
		this.registry = LocateRegistry.createRegistry(1099);
		System.setProperty("plugin.folder", SafeRefactorActivator.getDefault()
				.getPluginFolder());
		System.setProperty("java.security.policy", SafeRefactorActivator
				.getDefault().getPluginFolder() + "/security.policy");

		StringBuffer classpath = new StringBuffer();
		File binFile = new File(SafeRefactorActivator.getDefault()
				.getPluginFolder() + "/bin/");
		if (!binFile.exists())
			binFile = new File(SafeRefactorActivator.getDefault()
				.getPluginFolder());
		classpath.append("file:");
		classpath.append(binFile.getAbsolutePath());
		classpath.append("/");
		classpath.append(" file:");
		classpath.append(SafeRefactorActivator.getDefault().getPluginFolder());
		classpath.append("lib/saferefactor-beta.jar");
		SafeRefactorActivator.getDefault().log(classpath.toString());

		System.setProperty("java.rmi.server.codebase", classpath.toString());
	}

	public void setRegistry(Registry registry) {
		this.registry = registry;
	}

	public void log(String msg) {
		log(msg, null);
	}

	public void log(String msg, Exception e) {
		getLog().log(new Status(Status.INFO, this.PLUGIN_ID, Status.OK, msg, e));
	}

}
