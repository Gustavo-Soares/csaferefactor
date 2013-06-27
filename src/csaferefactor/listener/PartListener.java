package csaferefactor.listener;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

import csaferefactor.ProjectLogger;
import csaferefactor.SafeRefactorActivator;

public class PartListener implements IPartListener {

	private List<String> listeners = new ArrayList<String>();

	public PartListener(String title) {
		listeners.add(title);
	}

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
		
		if (!listeners.contains(page.getActiveEditor().getTitle())) {
			listeners.add(page.getActiveEditor().getTitle());
//			System.out.println("listerner ativado na classe: " + page.getActiveEditor().getTitle());
//			System.out.println(listeners);
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
				OutputStream stream = new ByteArrayOutputStream();
				PrintStream printStream = new PrintStream(stream);
				e.printStackTrace(printStream);
				SafeRefactorActivator.getDefault().log(stream.toString());
				printStream.flush();
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
