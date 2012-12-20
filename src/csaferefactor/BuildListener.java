package csaferefactor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.texteditor.ITextEditor;

public class BuildListener implements IResourceChangeListener,
		IPropertyListener {

	private int counter = 0;
	private ProjectLogger logger;
	private List<String> versions;

	public BuildListener() throws IOException {

		IProject project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject("project1");
		logger = new ProjectLogger(project);
		versions = new ArrayList<String>();

		String path = logger.log();
		versions.add(path);

	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {

		switch (event.getType()) {
		case IResourceChangeEvent.POST_BUILD:

			try {
				event.getDelta().accept(new ChangeVisitor(logger, versions));
			} catch (CoreException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			break;
		}

	}

	@Override
	public void propertyChanged(Object source, int propId) {

		switch (propId) {
		case ITextEditor.PROP_DIRTY:
			String path;
			try {

				path = logger.log();
				versions.add(path);
				System.out.println("logged version to: " + path);
				counter++;

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;

		default:
			break;
		}

	}

}
