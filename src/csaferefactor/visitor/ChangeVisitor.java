package csaferefactor.visitor;

import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import csaferefactor.Activator;
import csaferefactor.ProjectLogger;

public class ChangeVisitor implements IResourceDeltaVisitor {

	private IEditorPart editorPart;

	public ChangeVisitor(IEditorPart editorPart) {
		this.editorPart = editorPart;
	}

	public boolean visit(IResourceDelta delta) {
		IResource res = delta.getResource();
		switch (delta.getKind()) {
		case IResourceDelta.ADDED:
			break;
		case IResourceDelta.REMOVED:
			break;
		case IResourceDelta.CHANGED:

			int flags = delta.getFlags();

			// if a java file was saved
			if (res != null && res.getFileExtension() != null && res.getFileExtension().equals("java")
					&& (flags & IResourceDelta.CONTENT) != 0) {

				ITextEditor editor = (ITextEditor) editorPart
						.getAdapter(ITextEditor.class);

				if (editor != null && !editor.isDirty()) {
					// remove markers
					try {
						Activator.getDefault().removeExistingPluginmarkers(res);
						ProjectLogger.getInstance().clean();
						ProjectLogger.getInstance().log();
					} catch (CoreException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
			break;
		}
		return true;
	}

}