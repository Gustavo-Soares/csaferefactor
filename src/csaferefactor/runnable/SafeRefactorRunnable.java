package csaferefactor.runnable;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import csaferefactor.Activator;
import csaferefactor.ProjectLogger;
import csaferefactor.SafeRefactorPlugin;
import csaferefactor.Snapshot;
import csaferefactor.exception.ServerCreationException;

import saferefactor.core.Report;
import saferefactor.core.util.Constants;
import saferefactor.core.util.FileUtil;
import saferefactor.core.util.Project;
import saferefactor.core.util.ast.ConstructorImp;
import saferefactor.core.util.ast.Method;
import saferefactor.core.util.ast.MethodImp;
import saferefactor.rmi.client.CheckBehaviorChange;
import saferefactor.rmi.common.RemoteExecutor;

public class SafeRefactorRunnable implements Runnable {

	private int targetVersion;
	private int sourceVersion;

	private Report saferefactoReport = new Report();
	private List<String> methodsToTest;
	

	public SafeRefactorRunnable(String name, int sourceVersion,
			int targetVersion, List<String> methodsToTest) {

		this.sourceVersion = sourceVersion;
		this.targetVersion = targetVersion;
		this.methodsToTest = methodsToTest;
		

	}

	@Override
	public void run() {

		performSafeRefactor();

	}

	private void performSafeRefactor() {

		try {
			List<Snapshot> snapshotList = ProjectLogger.getInstance()
					.getSnapshotList();

			String sourceFolder = snapshotList.get(sourceVersion).getPath();

			String targetPath = snapshotList.get(targetVersion).getPath();

			Project sourceP = configureProject(sourceFolder);

			Project targetP = configureProject(targetPath);
			
			System.out.println("Analyzing transformation: "
					+ sourceP.getBuildFolder().getName() + "-> "
					+ targetP.getBuildFolder().getName());

			evaluate(sourceP, targetP);

			printResult(sourceP, targetP);

		} catch (RemoteException e) {
			e.printStackTrace();
		} catch (NotBoundException e) {
			e.printStackTrace();
		} catch (URISyntaxException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ExecutionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ServerCreationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TimeoutException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	

	private void printResult(Project sourceP, Project targetP) {
		System.out.println("Transformation: "
				+ sourceP.getBuildFolder().getName() + "-> "
				+ targetP.getBuildFolder().getName() + " is refactoring? "
				+ this.saferefactoReport.isRefactoring());
	}

	private void evaluate(Project sourceP, Project targetP)
			throws RemoteException, NotBoundException, AccessException,
			URISyntaxException, IOException, InterruptedException, ExecutionException, ServerCreationException, TimeoutException {
		Registry registry = LocateRegistry.getRegistry("localhost");

		System.setSecurityManager(new RMISecurityManager());

		RemoteExecutor generatorServer = (RemoteExecutor) registry
				.lookup(ProjectLogger.getInstance().getSnapshotList()
						.get(sourceVersion).getServerName());
		
		String fileName = generateMethodListFile();
		Future<Boolean> futureIsServerLoaded = ProjectLogger.getInstance().getSnapshotList().get(targetVersion).getFutureIsServerLoaded();
		Boolean isTargetServerLoaded = futureIsServerLoaded.get(3,TimeUnit.SECONDS);
		if (!isTargetServerLoaded)
			throw new ServerCreationException("problem loading the target server");
		
		//debug
		System.out.println("running task on server...");
		setSaferefactoReport(generatorServer
				.executeTask(new CheckBehaviorChange(sourceP, targetP,
						fileName)));
	}

	
	private String generateMethodListFile() {

		StringBuffer lines = new StringBuffer();
		
			for (String method : methodsToTest) {				
					lines.append(method);
					lines.append("\n");
			}

		String fileName = Constants.SAFEREFACTOR_DIR + Constants.SEPARATOR
				+ "methodsToTest.txt";
		FileUtil.makeFile(fileName, lines.toString());
		return fileName;
	}
	
	private Project configureProject(String targetPath) {
		Project targetP;
		targetP = new Project();
		File targetFolder = new File(targetPath);
		targetP.setProjectFolder(targetFolder);
		targetP.setBuildFolder(targetFolder);
		targetP.setSrcFolder(targetFolder);
		return targetP;
	}

	public Report getSaferefactoReport() {
		return saferefactoReport;
	}

	public void setSaferefactoReport(Report saferefactoReport) {
		this.saferefactoReport = saferefactoReport;
	}

}
