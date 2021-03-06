package csaferefactor.eclipse;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;

import csaferefactor.core.SafeRefactorThread;

public class JavaElementChangedListener implements IElementChangedListener {

	private IEditorPart activeEditor;

	private SafeRefactorThread saferefactorThread;

	public JavaElementChangedListener(IEditorPart activeEditor) {
		this.activeEditor = activeEditor;
	}

	public void elementChanged(ElementChangedEvent event) {
		IJavaElementDelta delta = event.getDelta();
		if (delta != null) {

			final IJavaElement javaElement = delta.getElement();
			/*
			 * only listen to events on compilation units, and when they affect
			 * their code
			 */
			if (javaElement != null && javaElement instanceof ICompilationUnit
					&& changedFilecontent(delta)) {

				final JavaEditor javaEditor = (JavaEditor) this.activeEditor;

				Display.getDefault().syncExec(new Runnable() {

					@Override
					public void run() {
						ITextSelection sel = (ITextSelection) javaEditor
								.getSelectionProvider().getSelection();

						int offset = sel.getOffset();
						IJavaElement elementAt;
						try {
							elementAt = ((ITypeRoot) javaElement)
									.getElementAt(offset);

							/*
							 * According to the Eclipse API, elementAt will
							 * return null if there's no element (that is, a
							 * method, field, etc) at the given position. For
							 * instance, white spaces in a blank line.
							 */
							if (elementAt != null) {
								if (elementAt.getElementType() != IJavaElement.METHOD)
									return;
								IJavaElement changedElement = elementAt;

								/*
								 * if there is any thread running to check the
								 * behavior of previous transformation, stop it,
								 * and ignore the unstable version
								 */
								if (saferefactorThread != null
										&& saferefactorThread.isAlive()) {
									System.out.println("canceling thread");
									saferefactorThread.setRunning(false);
									saferefactorThread.join();
								}
								checkTransformation(javaElement,
										changedElement, sel.getStartLine());
							}
						} catch (JavaModelException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

					}

				});
			}
		}
	}

	private void checkTransformation(IJavaElement javaElement,
			IJavaElement changedElement, int changeLine) {

		saferefactorThread = new SafeRefactorThread("saferefactor",
				javaElement, changedElement, changeLine);
		saferefactorThread.start();
		// executor = Executors.newSingleThreadExecutor();
		// Thread t = new Thread();
		// submit = executor.submit(changeAnalyzerRunnable);

	}

	private boolean changedFilecontent(IJavaElementDelta delta) {

		// other option, try to use IJavaElementDelta.F_CONTENT on the Java File
		// resource instead of the JavaElement
		return (delta.getFlags() & IJavaElementDelta.F_CHILDREN) != 0
				|| (delta.getFlags() & IJavaElementDelta.F_FINE_GRAINED) != 0;
	}

}