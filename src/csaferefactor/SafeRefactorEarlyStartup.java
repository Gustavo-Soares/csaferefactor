package csaferefactor;

import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import csaferefactor.listener.PartListener;

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
				try {
					SafeRefactorActivator.getDefault().finishInit();
				} catch (RemoteException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}

			}
		});

	}

}
