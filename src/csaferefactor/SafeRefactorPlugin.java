package csaferefactor;

import java.io.IOException;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import csaferefactor.listener.BuildListener;
import csaferefactor.listener.JavaElementChangedListener;


public class SafeRefactorPlugin {

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

		
		
		IWorkbenchWindow activeWorkbenchWindow = Activator.getDefault()
				.getWorkbench().getActiveWorkbenchWindow();

		if (activeWorkbenchWindow == null)
			return;
		IWorkbenchPage page = activeWorkbenchWindow.getActivePage();

		if (page == null)
			return;

		// set listener
		JavaCore.addElementChangedListener(new JavaElementChangedListener(page.getActiveEditor()),
				ElementChangedEvent.POST_RECONCILE);
		
		IResourceChangeListener listener = new BuildListener(page.getActiveEditor());
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener,
				IResourceChangeEvent.POST_BUILD);

	}

}
