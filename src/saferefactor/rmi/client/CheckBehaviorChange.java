package saferefactor.rmi.client;

import java.io.Serializable;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
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
import randoop.main.RandoopTextuiException;
import saferefactor.core.Report;
import saferefactor.core.util.Constants;
import saferefactor.core.util.FileUtil;
import saferefactor.core.util.Project;
import saferefactor.core.util.ast.ConstructorImp;
import saferefactor.core.util.ast.Method;
import saferefactor.core.util.ast.MethodImp;
import saferefactor.rmi.common.RemoteExecutor;
import saferefactor.rmi.common.Task;

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
	public Report execute() throws NotBoundException {

		Report result = new Report();

		try {

			String sourceId = sourceP.getProjectFolder().getName();
			String targetId = targetP.getProjectFolder().getName();
			System.out.println("Checking transformation from " + sourceId
					+ " to " + targetId);

			GenTests generator = new GenTests();

			String path = this.classToTest;
			String[] command = {

					"--methodlist=" + path,
					"--timelimit=1",
					"--log=" + sourceP.getProjectFolder() + Constants.SEPARATOR
							+ "filewriter", "--output-nonexec=true",
					"--dont-output-tests=true",
					"--junit-output-dir=/Users/gustavoas" };

			generator.handle(command);

			List<ExecutableSequence> sequences = generator.getSequences();

			// run the sequences on the server
			Registry registry;
			registry = LocateRegistry.getRegistry("localhost");
			System.setSecurityManager(new RMISecurityManager());

			// run the tests in the second vm
			RemoteExecutor server = getServer(registry);
			List<ExecutableSequence> comparedSequences = server
					.executeTask(new SequenceExecution(targetId, sequences));

			// close service
			// server.exit();

			// compare the results
			boolean changeBehavior = false;

			Set<String> changedMethods = new HashSet<String>();

			int fail = 0;
			System.out.println("comparando resultados");
//			StringBuffer results = new StringBuffer();
			for (int i = 0; i < sequences.size(); i++) {
				if (!sequences.get(i).toCodeString()
						.equals(comparedSequences.get(i).toCodeString())) {
//					results.append("Test");
					int testId = i + 1;
					result.getFailedTests().put(testId, sequences.get(i).toCodeString());
//					results.append(testId);
//					results.append(" passes before this change but has a different result after the change.");
//					results.append(";");
					fail++;

					ExecutableSequence sequence = sequences.get(i);
					ExecutableSequence comparedSequence = comparedSequences
							.get(i);
					Set<String> compare_checks = sequence.compare_checks(
							comparedSequence, true);
					changedMethods.addAll(compare_checks);
					changeBehavior = true;
				}
			}
			System.out.println("tests: " + sequences.size());
			System.out.println("different: " + fail);
			
			result.setRefactoring(!changeBehavior);
//			result.setChanges(results.toString());
			ArrayList<String> changeMethodsList = new ArrayList<String>();
			changeMethodsList.addAll(changedMethods);
			result.setChangedMethods2(changeMethodsList);

		} catch (RemoteException e) {

			e.printStackTrace();
		} catch (RandoopTextuiException e) {

			e.printStackTrace();
		}

		return result;
	}

	private RemoteExecutor getServer(Registry registry) throws RemoteException,
			AccessException {
		try {
			return (RemoteExecutor) registry.lookup(targetP.getProjectFolder()
					.getName());
		} catch (NotBoundException e) {
			return getServer(registry);
		}
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
