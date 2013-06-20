package csaferefactor;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jdt.core.ElementChangedEvent;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IPartService;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import csaferefactor.listener.BuildListener;
import csaferefactor.listener.JavaElementChangedListener;
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
