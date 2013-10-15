package csaferefactor.startup;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;

public class SafeRefactorEarlyStartup implements IStartup {

	@Override
	public void earlyStartup() {
		Display.getDefault().asyncExec(new Runnable() {
			/*
			 * (non-Javadoc)
			 * 
			 * @see java.lang.Runnable#run()
			 */
			public void run() {
//				try {
//					finishInit();
//				} catch (RemoteException e) {
//					OutputStream stream = new ByteArrayOutputStream();
//					PrintStream printStream = new PrintStream(stream);
//					e.printStackTrace(printStream);
//					SafeRefactorActivator.getDefault().log(stream.toString());
//					printStream.flush();
//				} catch (IOException e) {
//					OutputStream stream = new ByteArrayOutputStream();
//					PrintStream printStream = new PrintStream(stream);
//					e.printStackTrace(printStream);
//					SafeRefactorActivator.getDefault().log(stream.toString());
//					printStream.flush();
//				}

			}
		});

	}
	
	

	

}
