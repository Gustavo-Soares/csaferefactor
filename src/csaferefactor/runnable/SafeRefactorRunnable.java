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

import csaferefactor.Activator;
import csaferefactor.ProjectLogger;
import csaferefactor.SafeRefactorPlugin;
import csaferefactor.Snapshot;

import saferefactor.core.Report;
import saferefactor.core.util.Project;
import saferefactor.rmi.client.CheckBehaviorChange;
import saferefactor.rmi.common.RemoteExecutor;

public class SafeRefactorRunnable implements Runnable {

	private int targetVersion;
	private int sourceVersion;

	private Report saferefactoReport = new Report();

	public SafeRefactorRunnable(String name, int sourceVersion,
			int targetVersion) {

		this.sourceVersion = sourceVersion;
		this.targetVersion = targetVersion;

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

			evaluate(sourceP, targetP);

			printResult(sourceP, targetP);

		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotBoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
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
			URISyntaxException, IOException {
		Registry registry = LocateRegistry.getRegistry("localhost");

		System.setSecurityManager(new RMISecurityManager());

		RemoteExecutor generatorServer = (RemoteExecutor) registry
				.lookup(ProjectLogger.getInstance().getSnapshotList()
						.get(sourceVersion).getServerName());
		setSaferefactoReport(generatorServer
				.executeTask(new CheckBehaviorChange(sourceP, targetP,
						Activator.getDefault().getBinPath(), Activator
								.getDefault().getSafeRefactorJarPath(),
						Activator.getDefault().getSecurityPolicyPath())));
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
