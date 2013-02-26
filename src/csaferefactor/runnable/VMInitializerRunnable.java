package csaferefactor.runnable;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.concurrent.Callable;

import saferefactor.core.util.Constants;

import csaferefactor.SafeRefactorActivator;

public class VMInitializerRunnable implements Callable<Boolean> {

	private String serverName;
	private String classpath;

	public VMInitializerRunnable(String name, String classpath) {
		this.serverName = name;
		this.classpath = classpath;
	}

	@Override
	public Boolean call() {

		try {

			System.setSecurityManager(new RMISecurityManager());

			String saferefactorJar = SafeRefactorActivator.getDefault()
					.getSafeRefactorJarPath();
			String binPath = SafeRefactorActivator.getDefault().getBinPath();

			String securityPolicyPath = SafeRefactorActivator.getDefault()
					.getSecurityPolicyPath();

			ProcessBuilder builder = new ProcessBuilder(
					new String[] {
							"java",
							"-cp",
							saferefactorJar
									+ ":"
									+ classpath
									+ ":/Users/gustavoas/workspaces/ncstate/saferefactoraj/test/data/jhotdraw_source/lib/",
							"-Djava.rmi.server.codebase=file:" + binPath
									+ " file:" + saferefactorJar + " file:"
									+ classpath + "/",
									"-Djava.awt.headless=true",
							"-Djava.security.policy=file:" + securityPolicyPath,
							"saferefactor.rmi.server.RemoteExecutorImpl",
							serverName });

			builder.redirectErrorStream(true);
			Process p = builder.start();

			// System.out.println("Server " + serverName
			// + " generated with classpath: " + classpath + "!");

			InputStream inputStream = p.getInputStream();
			BufferedReader stdInput = new BufferedReader(new InputStreamReader(
					inputStream));
			//
			// BufferedReader stdError = new BufferedReader(new
			// InputStreamReader(
			// p.getErrorStream()));
			//
			File outputFile = new File(classpath, "log.txt");

//			FileOutputStream fos = new FileOutputStream(outputFile);
			FileWriter fw = new FileWriter(outputFile);
			// // read the output from the command
			// System.out.println("Here is the standard output of the command:\n");
			String line;
			while ((line = stdInput.readLine()) != null) {
//				fos.write(line.getBytes());
				fw.write(line);
				fw.write("\n");
				
				// server was loaded correctly
//				if (line.equals("Server " + serverName + " loaded!")) {
//					return true;
//				}

			}
			stdInput.close();
			fw.close();
			//
			// // read any errors from the attempted command
			// System.out
			// .println("Here is the standard error of the command (if any):\n");
			// while ((s = stdError.readLine()) != null) {
			// System.out.println(s);
			// }

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		}
		return true;

	}

}
