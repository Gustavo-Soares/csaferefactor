package csaferefactor;

import java.io.IOException;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import csaferefactor.listener.JavaElementChangedListener;


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
		
		
		IWorkbenchWindow activeWorkbenchWindow = Activator.getDefault()
				.getWorkbench().getActiveWorkbenchWindow();

		if (activeWorkbenchWindow == null)
			return;
		IWorkbenchPage page = activeWorkbenchWindow.getActivePage();

		if (page == null)
			return;

//		IResourceChangeListener listener = new BuildListener(page.getActiveEditor());
//		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener,
//				IResourceChangeEvent.POST_BUILD);

	}

}
