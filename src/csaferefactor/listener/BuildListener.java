package csaferefactor.listener;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.texteditor.ITextEditor;

import csaferefactor.visitor.ChangeVisitor;

/**
 * 
 * @author SPG - <a href="http://www.dsc.ufcg.edu.br/~spg"
 *         target="_blank">Software Productivity Group</a>
 * @author Gustavo Soares
 * @author Jeanderson Candido
 */
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

			try {
				event.getDelta().accept(new ChangeVisitor(editorPart));
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
