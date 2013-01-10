package csaferefactor;

import java.io.IOException;
import java.rmi.RMISecurityManager;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import csaferefactor.util.ProjectLogger;
import csaferefactor.util.VMInitializerJob;

public class SafeRefactorPlugin {

	// public static final String SAFEREFACTOR_SERVER = "saferefactor";

	public static final String MY_FAMILY = "saferefactorJobFamily";

	public static final String SAFEREFACTOR_MARKER = "csaferefactor.saferefactorproblem";

	
	
	private IWorkbenchPage page;

	public SafeRefactorPlugin() {
		page = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
				.getActivePage();
	}

	public void start() throws IOException {
	
//		System.setSecurityManager(new RMISecurityManager());
		
		if (page == null)
			return;

		IEditorPart editorPart = page.getActiveEditor();

		if (editorPart == null)
			return;

		// log current project
		ProjectLogger.getInstance().log();

		// set listener
		JavaCore.addElementChangedListener(new JavaElementChangedListener(),
				ElementChangedEvent.POST_RECONCILE);

	}



}
