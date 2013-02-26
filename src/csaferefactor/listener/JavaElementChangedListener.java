package csaferefactor.listener;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.internal.ui.javaeditor.JavaEditor;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.ui.IEditorPart;

import csaferefactor.SafeRefactorActivator;
import csaferefactor.ProjectLogger;
import csaferefactor.runnable.SafeRefactorThread;

public class JavaElementChangedListener implements IElementChangedListener {

	private IEditorPart activeEditor;
	
	
	private SafeRefactorThread saferefactorThread;

	public JavaElementChangedListener(IEditorPart activeEditor) {
		this.activeEditor = activeEditor;
	}

	public void elementChanged(ElementChangedEvent event) {

		IJavaElementDelta delta = event.getDelta();
		IJavaElement javaElement = delta.getElement();

		// only listen to events on compilationunits, and when they affect their
		// code
		if (delta != null && javaElement != null
				&& javaElement instanceof ICompilationUnit
				&& changedFilecontent(delta)) {

			try {
				ITextSelection sel = (ITextSelection) ((JavaEditor) this.activeEditor)
						.getSelectionProvider().getSelection();
				int offset = sel.getOffset();
				IJavaElement elementAt = ((ITypeRoot) javaElement)
						.getElementAt(offset);
				if (elementAt.getElementType() != IJavaElement.METHOD)
					return;
				IMethod changedMethod = (IMethod) elementAt;

				// if there is any thread running to check the behavior of
				// previous transformation, stop it, and ignore the unstable
				// version
				if (saferefactorThread != null && saferefactorThread.isAlive()) {
					System.out.println("canceling thread");
					saferefactorThread.setRunning(false);
					saferefactorThread.join();					
				}
				checkTransformation(javaElement, changedMethod);

			} catch (JavaModelException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private void checkTransformation(IJavaElement javaElement,
			IMethod changedMethod) {

		

		saferefactorThread = new SafeRefactorThread("saferefactor", javaElement, changedMethod);
		saferefactorThread.start();
//		executor = Executors.newSingleThreadExecutor();
		// Thread t = new Thread();
//		submit = executor.submit(changeAnalyzerRunnable);

	}

	private boolean changedFilecontent(IJavaElementDelta delta) {

		// other option, try to use IJavaElementDelta.F_CONTENT on the Java File
		// resource instead of the JavaElement
		return (delta.getFlags() & IJavaElementDelta.F_CHILDREN) != 0
				|| (delta.getFlags() & IJavaElementDelta.F_FINE_GRAINED) != 0;
	}

}