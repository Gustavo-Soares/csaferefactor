package csaferefactor;

import java.io.IOException;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import csaferefactor.util.ProjectLogger;
import csaferefactor.util.VMInitializerJob;

public class SafeRefactorPlugin {

	public static final String SAFEREFACTOR_SERVER = "saferefactor";
	

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

		IJavaElement javaElement = JavaUI.getEditorInputJavaElement(editorPart
				.getEditorInput());

		IJavaProject javaProject = javaElement.getJavaProject();

		// log current project
		ProjectLogger.getInstance().log();

		// crate generator server
		VMInitializerJob vmInitializer = new VMInitializerJob(
				"saferefactor_initializer", SafeRefactorPlugin.SAFEREFACTOR_SERVER,
				ProjectLogger.getInstance().getVersions().get(0));
		vmInitializer.schedule();
		vmInitializer.setPriority(Job.SHORT);

		// set listener
		IResourceChangeListener listener = new BuildListener(javaProject);
		ResourcesPlugin.getWorkspace().addResourceChangeListener(listener,
				IResourceChangeEvent.POST_BUILD);

	}

}
