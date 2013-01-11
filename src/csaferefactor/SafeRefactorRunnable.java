package csaferefactor;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.eclipse.jdt.core.dom.CompilationUnit;

import saferefactor.core.Parameters;
import saferefactor.core.Report;
import saferefactor.core.util.Project;
import saferefactor.rmi.client.CheckBehaviorChange;
import saferefactor.rmi.common.RemoteExecutor;
import csaferefactor.util.ProjectLogger;

public class SafeRefactorRunnable implements Runnable {

	private int targetVersion;
	private int sourceVersion;
	private CompilationUnit astRoot;
	private Report saferefactoReport;

	public SafeRefactorRunnable(String name, int sourceVersion,
			int targetVersion, CompilationUnit astRoot) {

		this.sourceVersion = sourceVersion;
		this.targetVersion = targetVersion;
		this.astRoot = astRoot;

	}

	public boolean belongsTo(Object family) {
		return family == SafeRefactorPlugin.MY_FAMILY;
	}

	@Override
	public void run() {

		performSafeRefactor();

	}

	private void performSafeRefactor() {
		// perform saferefactor
		// setting up rmi
		Registry registry;
		try {
			// debug

			registry = LocateRegistry.getRegistry("localhost");

			System.setSecurityManager(new RMISecurityManager());

			Project targetP = new Project();

			String targetPath = ProjectLogger.getInstance().getSnapshotList()
					.get(targetVersion).getPath();

			File targetFolder = new File(targetPath);
			targetP.setProjectFolder(targetFolder);
			targetP.setBuildFolder(targetFolder);
			targetP.setSrcFolder(targetFolder);

			Project sourceP = new Project();

			File sourceFolder = new File(ProjectLogger.getInstance()
					.getSnapshotList().get(sourceVersion).getPath());
			sourceP.setProjectFolder(sourceFolder);
			sourceP.setBuildFolder(sourceFolder);
			sourceP.setSrcFolder(sourceFolder);

			// define parameters
			Parameters parameters = new Parameters();
			parameters.setCompileProjects(false);
			parameters.setTimeLimit(1);
			parameters.setAnalyzeChangeMethods(true);

			// invoke saferefactor
			setSaferefactoReport(new Report());

			RemoteExecutor generatorServer = (RemoteExecutor) registry
					.lookup(ProjectLogger.getInstance().getSnapshotList()
							.get(sourceVersion).getServerName());
			setSaferefactoReport(generatorServer
					.executeTask(new CheckBehaviorChange(sourceP, targetP,
							Activator.getDefault().getBinPath(), Activator
									.getDefault().getSafeRefactorJarPath(),
							Activator.getDefault().getSecurityPolicyPath())));

			// debug
			System.out.println("Transformation: "
					+ sourceP.getBuildFolder().getName() + "-> "
					+ targetP.getBuildFolder().getName() + " is refactoring? " + this.saferefactoReport.isRefactoring());

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

	public Report getSaferefactoReport() {
		return saferefactoReport;
	}

	public void setSaferefactoReport(Report saferefactoReport) {
		this.saferefactoReport = saferefactoReport;
	}

}
