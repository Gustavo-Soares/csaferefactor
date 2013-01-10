package csaferefactor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IElementChangedListener;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaElementDelta;

public class JavaElementChangedListener implements IElementChangedListener {
	
	private ExecutorService executor = Executors.newFixedThreadPool(1);
	
	public void elementChanged(ElementChangedEvent event) {
		
		IJavaElementDelta delta = event.getDelta();
		IJavaElement javaElement = delta.getElement();

		if (delta != null && javaElement != null
				&& javaElement instanceof ICompilationUnit
				
				&& ((delta.getFlags() & IJavaElementDelta.F_CHILDREN) != 0 || 				
				(delta.getFlags() & IJavaElementDelta.F_FINE_GRAINED) != 0)) {
		
			try {
				if (!executor.awaitTermination(0, TimeUnit.MILLISECONDS)) {
					executor.shutdownNow();
					executor = Executors.newFixedThreadPool(1);
					EvaluateTransformationJob job = new EvaluateTransformationJob("saferefactor", javaElement);
					executor.submit(job);		
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
									
		}

	}

	
}