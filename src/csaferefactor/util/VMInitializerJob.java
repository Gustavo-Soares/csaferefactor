package csaferefactor.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.osgi.framework.Bundle;

import csaferefactor.Activator;

public class VMInitializerJob extends Job {

	private String serverName;
	private String classpath;

	public VMInitializerJob(String name, String serverName, String classpath) {
		super(name);
		this.serverName = serverName;
		this.classpath = classpath;
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {

		String s = null;

		// run rmiregistry
		try {
			// Registry registry = LocateRegistry.getRegistry("localhost");
			// if (registry == null)
			Registry registry = LocateRegistry.createRegistry(1099);
			System.setSecurityManager(new RMISecurityManager());

			String saferefactorJar = Activator.getDefault()
					.getSafeRefactorJarPath();
			String binPath = Activator.getDefault().getBinPath();

			String securityPolicyPath = Activator.getDefault()
					.getSecurityPolicyPath();

			Process p = Runtime
					.getRuntime()
					.exec(new String[] {
							"java",
							"-cp",
							saferefactorJar + ":" + classpath,
							"-Djava.rmi.server.codebase=file:" + binPath
									+ " file:" + saferefactorJar + " file:"
									+ classpath + "/",
							"-Djava.security.policy=file:" + securityPolicyPath,
							"saferefactor.rmi.server.RemoteExecutorImpl",
							serverName });

			System.out.println("Server " + serverName
					+ " generated with classpath: " + classpath + "!");

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(
					p.getInputStream()));

			BufferedReader stdError = new BufferedReader(new InputStreamReader(
					p.getErrorStream()));

			// read the output from the command
			System.out.println("Here is the standard output of the command:\n");
			while ((s = stdInput.readLine()) != null) {
				System.out.println(s);
			}

			// read any errors from the attempted command
			System.out
					.println("Here is the standard error of the command (if any):\n");
			while ((s = stdError.readLine()) != null) {
				System.out.println(s);
			}

		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return Status.OK_STATUS;
	}

}
