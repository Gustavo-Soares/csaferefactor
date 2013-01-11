package csaferefactor.listener;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;

import csaferefactor.runnable.ChangeAnalyzerRunnable;

public class JavaElementChangedListener implements IElementChangedListener {

	private ExecutorService executor = Executors.newFixedThreadPool(1);

	public void elementChanged(ElementChangedEvent event) {

		IJavaElementDelta delta = event.getDelta();
		IJavaElement javaElement = delta.getElement();

		// only listen to events on compilationunits, and when they affect their
		// code
		if (delta != null && javaElement != null
				&& javaElement instanceof ICompilationUnit
				&& changedFilecontent(delta)) {

			try {
				// if there is any thread running to check the behavior of
				// previous transformation, stop it, and ignore the unstable
				// version
				if (!executor.awaitTermination(0, TimeUnit.MILLISECONDS)) {
					executor.shutdownNow();
					
					checkTransformation(javaElement);
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	private void checkTransformation(IJavaElement javaElement) {
		executor = Executors.newFixedThreadPool(1);
		ChangeAnalyzerRunnable changeAnalyzerRunnable = new ChangeAnalyzerRunnable("saferefactor",
				javaElement);
		executor.submit(changeAnalyzerRunnable);
	}

	private boolean changedFilecontent(IJavaElementDelta delta) {

		// other option, try to use IJavaElementDelta.F_CONTENT on the Java File
		// resource instead of the JavaElement
		return (delta.getFlags() & IJavaElementDelta.F_CHILDREN) != 0
				|| (delta.getFlags() & IJavaElementDelta.F_FINE_GRAINED) != 0;
	}

}