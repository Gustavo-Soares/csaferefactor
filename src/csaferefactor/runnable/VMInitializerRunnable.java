package csaferefactor.runnable;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.concurrent.Callable;

import csaferefactor.SafeRefactorActivator;

public class VMInitializerRunnable implements Callable<Boolean> {

	private String serverName;
	private String classpath;

	public VMInitializerRunnable(String name, String classpath) {
		this.serverName = name;
		this.classpath = classpath.replaceAll("\\\\", "/");
	}

	@Override
	public Boolean call() {
		SafeRefactorActivator.getDefault().log("Thread para criar server...");
		try {
			SafeRefactorActivator.getDefault().log(
					"Inicializando secury manneger...");
			System.setSecurityManager(new RMISecurityManager());

			SafeRefactorActivator.getDefault().log("Atribuindo variaveis...");
			String saferefactorJar = SafeRefactorActivator.getDefault()
					.getSafeRefactorJarPath();
			String binPath = SafeRefactorActivator.getDefault().getBinPath();

			String securityPolicyPath = SafeRefactorActivator.getDefault()
					.getSecurityPolicyPath();

			// String codebaseCommand = "-Djava.rmi.server.codebase=file:"
			// + binPath + " file:" + saferefactorJar + " file:"
			// + classpath + "/";
			StringBuffer codeBase = new StringBuffer();
			codeBase.append("-Djava.rmi.server.codebase=file:/");
			File binFile = new File(binPath);
			if (!binFile.exists())
				binFile = new File(SafeRefactorActivator.getDefault().getPluginFolder());
			codeBase.append(binFile.getAbsolutePath());
			codeBase.append("/");
			codeBase.append(" file:");
			codeBase.append(saferefactorJar);
			codeBase.append(" file:/");
			codeBase.append(classpath);
			codeBase.append("/");

			SafeRefactorActivator.getDefault().log("Configurando builder...");
			ProcessBuilder builder = new ProcessBuilder(new String[] { "java",
					"-cp", saferefactorJar + ";/" + classpath,
					codeBase.toString(), "-Djava.awt.headless=true",
					"-Djava.security.policy=file:" + securityPolicyPath,
					"saferefactor.rmi.server.RemoteExecutorImpl", serverName });

			builder.redirectErrorStream(true);
			SafeRefactorActivator.getDefault().log("starting Server...");
			Process p = builder.start();
			SafeRefactorActivator.getDefault().log("Process submitted");

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

			// FileOutputStream fos = new FileOutputStream(outputFile);
			FileWriter fw = new FileWriter(outputFile);
			// // read the output from the command
			// System.out.println("Here is the standard output of the command:\n");
			String line;
			while ((line = stdInput.readLine()) != null) {
				// fos.write(line.getBytes());
				fw.write(line);
				fw.write("\n");

				// server was loaded correctly
				// if (line.equals("Server " + serverName + " loaded!")) {
				// return true;
				// }

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
			OutputStream stream = new ByteArrayOutputStream();
			PrintStream printStream = new PrintStream(stream);
			e.printStackTrace(printStream);
			SafeRefactorActivator.getDefault().log(stream.toString());
			printStream.flush();
		} catch (IOException e) {
			OutputStream stream = new ByteArrayOutputStream();
			PrintStream printStream = new PrintStream(stream);
			e.printStackTrace(printStream);
			SafeRefactorActivator.getDefault().log(stream.toString());
			printStream.flush();
		}
		return true;

	}

}
