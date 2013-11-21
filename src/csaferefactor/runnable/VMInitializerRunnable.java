package csaferefactor.runnable;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.util.concurrent.Callable;

import csaferefactor.SafeRefactorActivator;

/**
 * Creates a JVM to be executed separately.
 * 
 * @author SPG - <a href="http://www.dsc.ufcg.edu.br/~spg"
 *         target="_blank">Software Productivity Group</a>
 * @author Gustavo Soares
 * @author Jeanderson Candido
 */
public class VMInitializerRunnable implements Callable<Boolean> {

	private String securityPolicyPath;
	private String saferefactorJar;
	private ProcessBuilder builder;
	private StringBuffer codeBase;
	private String serverName;
	private String classpath;
	private String binPath;

	/**
	 * If true, the separated JVM will print its outputs in the current standard
	 * stream
	 */
	private boolean verbose;

	public VMInitializerRunnable(String name, String classpath) {
		this(name, classpath, false);
	}

	public VMInitializerRunnable(String name, String classpath, boolean verbose) {
		this.serverName = name;
		this.classpath = classpath.replaceAll("\\\\", "/");
		this.verbose = verbose;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.concurrent.Callable#call()
	 */
	@Override
	public Boolean call() {
		SafeRefactorActivator.getDefault().log("Created thread for server...");
		try {
			setSecurityManager();
			setJVMparameters();
			setJVMprocess();

			SafeRefactorActivator.getDefault().log("starting Server...");
			Process p = builder.start();
			SafeRefactorActivator.getDefault().log("Process submitted");
			if (verbose)
				readProcessInputStream(p);

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
		} catch (URISyntaxException e) {
			OutputStream stream = new ByteArrayOutputStream();
			PrintStream printStream = new PrintStream(stream);
			e.printStackTrace(printStream);
			SafeRefactorActivator.getDefault().log(stream.toString());
			printStream.flush();
		}
		return true;

	}

	/**
	 * Prints the input stream of the given process
	 * 
	 * @param process
	 *            The process with a input stream to be read
	 * @throws IOException
	 */
	private void readProcessInputStream(Process process) throws IOException {
		InputStream inputStream = process.getInputStream();
		BufferedReader stdInput = new BufferedReader(new InputStreamReader(
				inputStream));

		String line = null;
		while ((line = stdInput.readLine()) != null) {
			System.out.println(line);
		}
		stdInput.close();
	}

	/**
	 * Sets a process builder to create a separated JVM.
	 */
	private void setJVMprocess() {
		SafeRefactorActivator.getDefault().log("Setting process...");
		this.builder = new ProcessBuilder(new String[] { "java", "-cp",
				saferefactorJar + ";/" + classpath, codeBase.toString(),
				"-Djava.awt.headless=true",
				"-Djava.security.policy=file:" + securityPolicyPath,
				"saferefactor.rmi.server.RemoteExecutorImpl", serverName });

		builder.redirectErrorStream(true);
	}

	/**
	 * Sets the execution parameters for the remote JVM.
	 * 
	 * @throws URISyntaxException
	 * @throws IOException
	 */
	private void setJVMparameters() throws URISyntaxException, IOException {
		SafeRefactorActivator.getDefault().log("Assigning variables...");
		this.saferefactorJar = SafeRefactorActivator.getDefault()
				.getSafeRefactorJarPath();

		this.binPath = SafeRefactorActivator.getDefault().getBinPath();
		this.securityPolicyPath = SafeRefactorActivator.getDefault()
				.getSecurityPolicyPath();

		this.codeBase = new StringBuffer();
		codeBase.append("-Djava.rmi.server.codebase=file:/");
		File binFile = new File(binPath);
		if (!binFile.exists())
			binFile = new File(SafeRefactorActivator.getDefault()
					.getPluginFolder());

		codeBase.append(binFile.getAbsolutePath());
		codeBase.append("/");
		codeBase.append(" file:");
		codeBase.append(saferefactorJar);
		codeBase.append(" file:/");
		codeBase.append(classpath);
		codeBase.append("/");
	}

	/**
	 * Sets the security manager.
	 */
	private void setSecurityManager() {
		SafeRefactorActivator.getDefault().log("Initializing secury manager...");
		if (System.getSecurityManager() == null)
			System.setSecurityManager(new RMISecurityManager());
	}

}
