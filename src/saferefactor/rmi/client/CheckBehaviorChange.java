package saferefactor.rmi.client;

import java.io.File;
import java.io.Serializable;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Random;

import org.eclipse.core.runtime.jobs.Job;

import csaferefactor.util.ProjectLogger;
import randoop.ExecutableSequence;
import randoop.main.GenTests;
import randoop.main.RandoopTextuiException;
import saferefactor.core.Parameters;
import saferefactor.core.Report;
import saferefactor.core.analysis.TransformationAnalyzer;
import saferefactor.core.analysis.naive.ASMBasedAnalyzer;
import saferefactor.core.util.Constants;
import saferefactor.core.util.FileUtil;
import saferefactor.core.util.Project;
import saferefactor.core.util.ast.ConstructorImp;
import saferefactor.core.util.ast.Method;
import saferefactor.core.util.ast.MethodImp;
import saferefactor.rmi.common.RemoteExecutor;
import saferefactor.rmi.common.Task;
import saferefactor.rmi.common.VMInitializer;

public class CheckBehaviorChange implements Task<Report>, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1977315047044202868L;
	private static final String EXECUTOR = "executor";
	private Project targetP;
	private Project sourceP;
	private String polityPath;
	private String safeRefactorJarPath;
	private String binPath;

	public CheckBehaviorChange(Project sourceProject, Project targetProject, String binPath, String safeRefactorJarPath, String polityPath) {
		this.sourceP = sourceProject;
		this.targetP = targetProject;
		this.binPath = binPath;
		this.safeRefactorJarPath = safeRefactorJarPath;
		this.polityPath = polityPath;
	}

	@Override
	public Report execute() {
		Report result = new Report();

		// identify common methods
		TransformationAnalyzer analyzer = new ASMBasedAnalyzer(sourceP,
				targetP, Constants.SAFEREFACTOR_DIR);

		try {
			saferefactor.core.analysis.Report analysisReport = analyzer
					.analyze();

			// initiate execute sequence server
			Thread initializeVM = new Thread(new VMInitializer(
					CheckBehaviorChange.EXECUTOR, targetP.getBuildFolder()
							.getAbsolutePath(), binPath, safeRefactorJarPath, polityPath));
			initializeVM.start();

			// generate the tests
			String fileName = generateMethodListFile(analysisReport
					.getMethodsToTest());

			GenTests generator = new GenTests();
			String[] command = { "--methodlist=" + fileName, "--timelimit=1",
					"--log=filewriter", "--output-nonexec=true" };

			generator.handle(command);

			List<ExecutableSequence> sequences = generator.getSequences();

			// run the sequences on the server
			Registry registry;
			registry = LocateRegistry.getRegistry("localhost");
			System.setSecurityManager(new RMISecurityManager());

			// run the tests in the second vm
			RemoteExecutor server = (RemoteExecutor) registry.lookup(CheckBehaviorChange.EXECUTOR);
			List<ExecutableSequence> comparedSequence = server
					.executeTask(new SequenceExecution(sequences));

			// compare the results
			boolean changeBehavior = false;
			for (int i = 0; i < sequences.size(); i++) {
				
				if (!sequences.get(i).toCodeString()
						.equals(comparedSequence.get(i).toCodeString())) {
					changeBehavior = true;
				}
					
			}
			result.setRefactoring(!changeBehavior);

		} catch (RemoteException e) {

			e.printStackTrace();
		} catch (Exception e) {

			e.printStackTrace();
		}

		return result;
	}

	private String generateMethodListFile(List<Method> methods) {

		Random random = new Random();
		int choice = random.nextInt(2);
		System.out.println(choice);
		StringBuffer lines = new StringBuffer();
		if (choice == 0) {
			for (Method method : methods) {
				lines.append(method + "\n");
			}
		} else {
			for (Method method : methods) {
				if (method instanceof ConstructorImp)
					lines.append(method + "\n");
			}
			for (Method method : methods) {
				if (method instanceof MethodImp)
					lines.append(method + "\n");
			}

		}

		String fileName = Constants.SAFEREFACTOR_DIR + Constants.SEPARATOR
				+ "methodsToTest.txt";
		FileUtil.makeFile(fileName, lines.toString());
		return fileName;
	}

}