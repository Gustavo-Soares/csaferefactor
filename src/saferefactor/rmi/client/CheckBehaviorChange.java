package saferefactor.rmi.client;

import java.io.Serializable;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import randoop.ExecutableSequence;
import randoop.main.GenTests;
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
	
	private Project targetP;
	private Project sourceP;
	private String classToTest;

	public CheckBehaviorChange(Project sourceProject, Project targetProject,
			String classToTest) {
		this.sourceP = sourceProject;
		this.targetP = targetProject;		
		this.classToTest = classToTest;
	}

	@Override
	public Report execute() {
		
		Report result = new Report();
		
		// identify common methods
//		TransformationAnalyzer analyzer = new ASMBasedAnalyzer(sourceP,
//				targetP, Constants.SAFEREFACTOR_DIR);

		try {
//			saferefactor.core.analysis.Report analysisReport = analyzer
//					.analyze();

			
			//creating unique id
			String sourceId = sourceP.getProjectFolder().getName();
			String targetId = targetP.getProjectFolder().getName();
			System.out.println("Checking transformation from " + sourceId + " to "  +  targetId);
			// initiate execute sequence server
//			String serverName = CheckBehaviorChange.EXECUTOR + sourceId + targetId;
//			Thread initializeVM = new Thread(new VMInitializer(
//					serverName, targetP.getBuildFolder()
//							.getAbsolutePath(), binPath, safeRefactorJarPath,
//					polityPath));
//			initializeVM.start();

			// generate the tests
//			String fileName = generateMethodListFile(analysisReport
//					.getMethodsToTest());

			GenTests generator = new GenTests();
			
			
			String path = this.classToTest;
			String[] command = { "--classlist=" + path, "--timelimit=15",
					"--log=filewriter", "--output-nonexec=true","--dont-output-tests=false","--junit-output-dir=/Users/gustavoas" };

			generator.handle(command);

			List<ExecutableSequence> sequences = generator.getSequences();

			// run the sequences on the server
			Registry registry;
			registry = LocateRegistry.getRegistry("localhost");
			System.setSecurityManager(new RMISecurityManager());

			// run the tests in the second vm			
			RemoteExecutor server = (RemoteExecutor) registry
					.lookup(targetP.getProjectFolder().getName());
			List<ExecutableSequence> comparedSequences = server
					.executeTask(new SequenceExecution(targetId, sequences));

			//close service
//			server.exit();
			
			// compare the results
			boolean changeBehavior = false;

			Set<String> changedMethods = new HashSet<String>();

			for (int i = 0; i < sequences.size(); i++) {
				if (!sequences.get(i).toCodeString()
						.equals(comparedSequences.get(i).toCodeString())) {
					ExecutableSequence sequence = sequences.get(i);
					ExecutableSequence comparedSequence = comparedSequences
							.get(i);
					Set<String> compare_checks = sequence
							.compare_checks(comparedSequence,true);
					changedMethods.addAll(compare_checks);
					changeBehavior = true;
				}

			}
			result.setRefactoring(!changeBehavior);
			ArrayList<String> changeMethodsList = new ArrayList<String>();
			changeMethodsList.addAll(changedMethods);
			result.setChangedMethods2(changeMethodsList);

			
			
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
