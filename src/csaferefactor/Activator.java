package csaferefactor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

import saferefactor.rmi.common.RemoteExecutor;

import csaferefactor.util.ProjectLogger;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "test"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
	private Registry registry = null;
	
	

	/**
	 * The constructor
	 */
	public Activator() {
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
		registry = LocateRegistry.createRegistry(1099);
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
		
		List<Snapshot> snapshotList = ProjectLogger.getInstance().getSnapshotList();
		for (Snapshot snapshot : snapshotList) {
			RemoteExecutor generatorServer = (RemoteExecutor) registry
					.lookup(snapshot.getServerName());
			generatorServer.exit();
		}
		
		IJobManager jobMan = Job.getJobManager();
		jobMan.cancel(SafeRefactorPlugin.MY_FAMILY);
		jobMan.join(SafeRefactorPlugin.MY_FAMILY, null);
		plugin = null;

		super.stop(context);
	}

	/**
	 * Returns the shared instance
	 * 
	 * @return the shared instance
	 */
	public static Activator getDefault() {
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
			// TODO Auto-generated catch block
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
		return getPath("/security.policy");
	}

	public String getBinPath() throws URISyntaxException, IOException {
		return getPath("/bin/");
	}

	private String getPath(String relativePath) throws URISyntaxException,
			IOException {
		IPath pathToSafeRefactorJar = new Path(relativePath);
		URL SafeRefactorJarUrl = FileLocator.find(getBundle(),
				pathToSafeRefactorJar, Collections.EMPTY_MAP);
		File saferefactorJarFile = new File(FileLocator.toFileURL(
				SafeRefactorJarUrl).toURI());
		String saferefactorJar = saferefactorJarFile.getAbsolutePath();
		return saferefactorJar;
	}

	public Registry getRegistry() {
		return registry;
	}



}
