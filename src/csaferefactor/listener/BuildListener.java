package csaferefactor.listener;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.texteditor.ITextEditor;

import csaferefactor.Activator;
import csaferefactor.ProjectLogger;
import csaferefactor.visitor.ChangeVisitor;


public class BuildListener implements IResourceChangeListener,
		IPropertyListener {

	private IEditorPart editorPart;

	public BuildListener(IEditorPart editorPart) {
		this.editorPart = editorPart;

	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {

		switch (event.getType()) {
		case IResourceChangeEvent.POST_BUILD:



			if (editorPart != null) {
				ITextEditor editor = (ITextEditor) editorPart
						.getAdapter(ITextEditor.class);

				if (editor != null && !editor.isDirty()) {

					// remove markers
					try {
						Activator.getDefault().removeExistingPluginmarkers(
								event.getDelta().getResource());
						ProjectLogger.getInstance().clean();
					} catch (CoreException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

				}
			}

			try {
				event.getDelta().accept(new ChangeVisitor());
			} catch (CoreException e1) {
				e1.printStackTrace();
			}

			break;
		}

	}

	@Override
	public void propertyChanged(Object source, int propId) {

		switch (propId) {
		case ITextEditor.PROP_DIRTY:

			break;

		default:
			break;
		}

	}

}
