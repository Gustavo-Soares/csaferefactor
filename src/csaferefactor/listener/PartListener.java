package csaferefactor.listener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import csaferefactor.SafeRefactorActivator;
import csaferefactor.ProjectLogger;

public class PartListener implements IPartListener {

	private List<JavaElementChangedListener> listeners = new ArrayList<JavaElementChangedListener>();

	@Override
	public void partActivated(IWorkbenchPart part) {
		System.out.println("active");

		IWorkbenchWindow activeWorkbenchWindow = SafeRefactorActivator
				.getDefault().getWorkbench().getActiveWorkbenchWindow();

		if (activeWorkbenchWindow == null)
			return;
		IWorkbenchPage page = activeWorkbenchWindow.getActivePage();

		if (page == null)
			return;

		// set listener
		JavaElementChangedListener javaElementChangedListener = new JavaElementChangedListener(
				page.getActiveEditor());
		if (!listeners.contains(javaElementChangedListener)) {
			
			JavaCore.addElementChangedListener(javaElementChangedListener,
					ElementChangedEvent.POST_RECONCILE);

			IResourceChangeListener listener = new BuildListener(
					page.getActiveEditor());
			ResourcesPlugin.getWorkspace().addResourceChangeListener(listener,
					IResourceChangeEvent.POST_BUILD);
			// log current project
			try {
				ProjectLogger.getInstance().log();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	public void partBroughtToTop(IWorkbenchPart part) {

	}

	@Override
	public void partClosed(IWorkbenchPart part) {
	}

	@Override
	public void partDeactivated(IWorkbenchPart part) {
	}

	@Override
	public void partOpened(IWorkbenchPart part) {
	}

}
