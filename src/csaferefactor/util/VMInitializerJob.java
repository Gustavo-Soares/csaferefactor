package csaferefactor.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;

import saferefactor.core.util.Constants;

import csaferefactor.Activator;

public class VMInitializerJob implements Runnable {

	private String serverName;
	private String classpath;

	public VMInitializerJob(String name, String classpath) {
		this.serverName = name;
		this.classpath = classpath;
	}

	@Override
	public void run() {

//		String s = null;

		try {

			System.setSecurityManager(new RMISecurityManager());

			String saferefactorJar = Activator.getDefault()
					.getSafeRefactorJarPath();
			String binPath = Activator.getDefault().getBinPath();

			String securityPolicyPath = Activator.getDefault()
					.getSecurityPolicyPath();

			ProcessBuilder builder = new ProcessBuilder(new String[] {
					"java",
					"-cp",
					saferefactorJar + ":" + classpath,
					"-Djava.rmi.server.codebase=file:" + binPath + " file:"
							+ saferefactorJar + " file:" + classpath + "/",
					"-Djava.security.policy=file:" + securityPolicyPath,
					"saferefactor.rmi.server.RemoteExecutorImpl", serverName });

			builder.redirectErrorStream(true);
			Process p = builder.start();

			// System.out.println("Server " + serverName
			// + " generated with classpath: " + classpath + "!");

			 InputStream inputStream = p.getInputStream();
			BufferedReader stdInput = new BufferedReader(new
			 InputStreamReader(
			 inputStream));
			//
			// BufferedReader stdError = new BufferedReader(new
			// InputStreamReader(
			// p.getErrorStream()));
			//
			File outputFile = new File(classpath, "log.txt");
			
			FileOutputStream fos = new FileOutputStream(outputFile);
			// // read the output from the command
//			 System.out.println("Here is the standard output of the command:\n");
			 int c;
			  while ((c = inputStream.read()) != -1) {
				 fos.write(c);
			 }
			  stdInput.close();
			  fos.close();
			//
			// // read any errors from the attempted command
			// System.out
			// .println("Here is the standard error of the command (if any):\n");
			// while ((s = stdError.readLine()) != null) {
			// System.out.println(s);
			// }

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

	}

}
