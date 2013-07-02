package csaferefactor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import saferefactor.rmi.common.RemoteExecutor;

/**
 * The activator class controls the plug-in life cycle
 */
public class SafeRefactorActivator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "CSafeRefactor"; //$NON-NLS-1$

	// The shared instance
	private static SafeRefactorActivator plugin;

	private Registry registry = null;

	private ExecutorService executor = Executors.newCachedThreadPool();

	public static final String SAFEREFACTOR_MARKER = "csaferefactor.saferefactorproblem";

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
	
	// FIXME CONSIDER BLANK SPACES IN PATH
	public String getUnixFriendlyPath() {
		URL url = getBundle().getEntry("/");
		try {
			url = FileLocator.toFileURL(url);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return url.getPath();
	}
	
	public String getWindowsFriendlyPath() {
		// TODO
		return "/C:/Users/Jeanderson/Documents/SUMMER~1/workspace/csaferefactor/";
	}

	/**
	 * @return The File Location of this plugin
	 */
	public String getPluginFolder() {
		String operatingSystemID = System.getProperty("os.name");
		if (operatingSystemID.toLowerCase().contains("windows")) {
			return getWindowsFriendlyPath();
		}
		return getUnixFriendlyPath();
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
		if (executor != null)
			this.executor = executor;
	}

	public void setRegistry(Registry registry) {
		if (registry != null)
			this.registry = registry;
	}

	public void log(String msg) {
		log(msg, null);
	}

	public void log(String msg, Exception e) {
		getLog().log(new Status(Status.INFO, PLUGIN_ID, Status.OK, msg, e));
	}

	public void configureRMI() throws RemoteException {

		// Creating a new RMI Registry
		this.registry = LocateRegistry.createRegistry(1099);

		/*
		 * PROPERTIES
		 */

		// Plug-in folder
		System.setProperty("plugin.folder", SafeRefactorActivator.getDefault()
				.getPluginFolder());

		// Security policy
		StringBuffer path = new StringBuffer(SafeRefactorActivator.getDefault()
				.getPluginFolder());
		System.setProperty("java.security.policy",
				path.append("/security.policy").toString());

		// Class path
		path = new StringBuffer();
		File binFile = new File(SafeRefactorActivator.getDefault()
				.getPluginFolder() + "/bin/");
		if (!binFile.exists())
			binFile = new File(SafeRefactorActivator.getDefault()
					.getPluginFolder());
		path.append("file:/");
		path.append(binFile.getAbsolutePath());
		path.append("/");
		path.append(" file:");
		path.append(SafeRefactorActivator.getDefault().getPluginFolder());
		path.append("lib/saferefactor-beta.jar");
		SafeRefactorActivator.getDefault().log(path.toString());

		System.setProperty("java.rmi.server.codebase", path.toString());
	}
}
