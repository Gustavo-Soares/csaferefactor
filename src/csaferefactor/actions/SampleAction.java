package csaferefactor.actions;

import java.io.IOException;

import javax.swing.JOptionPane;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.PlatformUI;

import csaferefactor.ProjectLogger;
import csaferefactor.SafeRefactorActivator;
import csaferefactor.listener.BuildListener;
import csaferefactor.listener.JavaElementChangedListener;
import csaferefactor.listener.PartListener;

/**
 * Our sample action implements workbench action delegate. The action proxy will
 * be created by the workbench and shown in the UI. When the user tries to use
 * the action, this delegate will be created and execution will be delegated to
 * it.
 * 
 * @see IWorkbenchWindowActionDelegate
 */
public class SampleAction implements IWorkbenchWindowActionDelegate {
	private IWorkbenchWindow window;

	/**
	 * The action has been activated. The argument of the method represents the
	 * 'real' action sitting in the workbench UI.
	 * 
	 * @see IWorkbenchWindowActionDelegate#run
	 */
	public void run(IAction action) {
		try {
			finishInit();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public void finishInit() throws IOException {
		IWorkbenchWindow activeWorkbenchWindow = SafeRefactorActivator
				.getDefault().getWorkbench().getActiveWorkbenchWindow();

		if (activeWorkbenchWindow == null)
			return;
		IWorkbenchPage page = activeWorkbenchWindow.getActivePage();

		// Checks if there's something to be listened
		if (page == null || page.getActiveEditor() == null) {
			JOptionPane.showMessageDialog(null,
					"There is no active editor to watch",
					"Couldn't initialize CSafeRefactor",
					JOptionPane.OK_CANCEL_OPTION);
			return;
		}
		SafeRefactorActivator.getDefault().configureRMI();

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

	/**
	 * Selection in the workbench has been changed. We can change the state of
	 * the 'real' action here if we want, but this can only happen after the
	 * delegate has been created.
	 * 
	 * @see IWorkbenchWindowActionDelegate#selectionChanged
	 */
	public void selectionChanged(IAction action, ISelection selection) {
	}

	/**
	 * We can use this method to dispose of any system resources we previously
	 * allocated.
	 * 
	 * @see IWorkbenchWindowActionDelegate#dispose
	 */
	public void dispose() {
	}

	/**
	 * We will cache window object in order to be able to provide parent shell
	 * for the message dialog.
	 * 
	 * @see IWorkbenchWindowActionDelegate#init
	 */
	public void init(IWorkbenchWindow window) {
		this.window = window;
	}
}