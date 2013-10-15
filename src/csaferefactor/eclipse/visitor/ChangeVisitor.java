package csaferefactor.eclipse.visitor;

import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import csaferefactor.startup.SafeRefactorActivator;
import csaferefactor.util.ProjectLogger;

public class ChangeVisitor implements IResourceDeltaVisitor {

	private IEditorPart editorPart;

	public ChangeVisitor(IEditorPart editorPart) {
		this.editorPart = editorPart;
	}

	public boolean visit(IResourceDelta delta) {
		if (delta == null)
			return false;
		if (delta.getKind() == IResourceDelta.CHANGED) {
			IResource res = delta.getResource();
			int flags = delta.getFlags();

			// if a java file was saved
			if (res != null && res.getFileExtension() != null
					&& res.getFileExtension().equals("java")
					&& (flags & IResourceDelta.CONTENT) != 0) {

				if (editorPart != null) {
					ITextEditor editor = (ITextEditor) editorPart
							.getAdapter(ITextEditor.class);

					if (editor != null && !editor.isDirty()) {
						// remove markers
						try {
							SafeRefactorActivator.getDefault()
									.removeExistingPluginmarkers(res);
							ProjectLogger.getInstance().clean();
							ProjectLogger.getInstance().log();

						} catch (CoreException e) {
							e.printStackTrace();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
		return true;
	}

}