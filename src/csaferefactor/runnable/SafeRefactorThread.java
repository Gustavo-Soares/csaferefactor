package csaferefactor.runnable;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URISyntaxException;
import java.rmi.AccessException;
import java.rmi.NotBoundException;
import java.rmi.RMISecurityManager;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.designwizard.design.ClassNode;
import org.designwizard.design.FieldNode;
import org.designwizard.design.MethodNode;
import org.designwizard.exception.InexistentEntityException;
import org.designwizard.main.DesignWizard;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.compiler.batch.BatchCompiler;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;

import saferefactor.core.Report;
import saferefactor.core.util.Constants;
import saferefactor.core.util.FileUtil;
import saferefactor.core.util.Project;
import saferefactor.rmi.client.CheckBehaviorChange;
import saferefactor.rmi.common.RemoteExecutor;
import csaferefactor.ProjectLogger;
import csaferefactor.SafeRefactorActivator;
import csaferefactor.Snapshot;
import csaferefactor.exception.CompilationException;
import csaferefactor.exception.PackageNotFoundException;
import csaferefactor.exception.ServerCreationException;

public class SafeRefactorThread extends Thread {

	private IJavaElement compilationUnit;
	private int sourceVersion;
	private int targetVersion;
	private CompilationUnit astRoot;
	private IJavaElement changedElement;
	private volatile boolean running = true;
	private int changeLine;

	public SafeRefactorThread(String name, IJavaElement javaElement,
			IJavaElement changedElement, int changeLine) {
		this.compilationUnit = javaElement;
		this.changedElement = changedElement;
		this.changeLine = changeLine;
	}

	public void run() {

		astRoot = parseCompilationUnit();

		boolean hasCompilationError = checkCompilationErrors(astRoot);

		if (!hasCompilationError) {
			sourceVersion = 0;
			targetVersion = 0;
			try {
				SafeRefactorActivator.getDefault().removeExistingPluginMarkers(
						(ICompilationUnit) compilationUnit);

				if (!isRunning())
					return;
				String classpath = logProject();

				if (!isRunning()) {
					deleteUnstableVersion();
					return;
				}

				try {
					updateBinariesForTheChangedAST(astRoot, classpath);

				} catch (PackageNotFoundException e) {
					IResource res = astRoot.getJavaElement().getResource();
					IMarker marker = res
							.createMarker(SafeRefactorActivator.SAFEREFACTOR_MARKER);
					marker.setAttribute("coolFactor", "ULTRA");
					marker.setAttribute(IMarker.SEVERITY,
							IMarker.SEVERITY_WARNING);

					String warning = "Couldn't check this change due to the following problem: "
							+ e.getMessage();

					marker.setAttribute(IMarker.MESSAGE, warning);
					int lineNumber = this.changeLine;
					marker.setAttribute(IMarker.LINE_NUMBER, lineNumber + 1);

				}
				if (!isRunning()) {
					deleteUnstableVersion();
					return;
				}
				Report saferefactoReport = compareBehaviorOfSnapshots();

				if (!isRunning()) {
					deleteUnstableVersion();
					return;
				}
				if (saferefactoReport.isRefactoring()) {
					// if no behavioral change, desconsider the old snapshot
					ProjectLogger.getInstance().deleteSnapshot(sourceVersion,
							true);
				} else {
					addMarkerOfBehavioralChanges(astRoot, saferefactoReport);
					// delete the target snapshot
					ProjectLogger.getInstance().deleteSnapshot(targetVersion,
							true);

				}
				if (!isRunning()) {
					deleteUnstableVersion();
					return;
				}

			} catch (IOException e) {
				OutputStream stream = new ByteArrayOutputStream();
				PrintStream printStream = new PrintStream(stream);
				e.printStackTrace(printStream);
				SafeRefactorActivator.getDefault().log(stream.toString());
				printStream.flush();
			} catch (CoreException e) {
				OutputStream stream = new ByteArrayOutputStream();
				PrintStream printStream = new PrintStream(stream);
				e.printStackTrace(printStream);
				SafeRefactorActivator.getDefault().log(stream.toString());
				printStream.flush();
			} catch (OperationCanceledException e) {
				OutputStream stream = new ByteArrayOutputStream();
				PrintStream printStream = new PrintStream(stream);
				e.printStackTrace(printStream);
				SafeRefactorActivator.getDefault().log(stream.toString());
				printStream.flush();
			} catch (ExecutionException e) {
				OutputStream stream = new ByteArrayOutputStream();
				PrintStream printStream = new PrintStream(stream);
				e.printStackTrace(printStream);
				SafeRefactorActivator.getDefault().log(stream.toString());
				printStream.flush();
			} catch (CompilationException e) {
				// if thread is interrupted, delete this snapshot
				// TODO when compilation error is found, how to show that to
				// user
				System.out.println("version does not compile: ");
				System.out.println(e.getMessage());
				deleteUnstableVersion();
			} catch (InexistentEntityException e) {
				OutputStream stream = new ByteArrayOutputStream();
				PrintStream printStream = new PrintStream(stream);
				e.printStackTrace(printStream);
				SafeRefactorActivator.getDefault().log(stream.toString());
				printStream.flush();
			} catch (NotBoundException e) {
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
			} catch (ServerCreationException e) {
				OutputStream stream = new ByteArrayOutputStream();
				PrintStream printStream = new PrintStream(stream);
				e.printStackTrace(printStream);
				SafeRefactorActivator.getDefault().log(stream.toString());
				printStream.flush();
			} catch (InterruptedException e) {
				System.out.println(e.getMessage());
				deleteUnstableVersion();
			}
		}

	}

	private void deleteUnstableVersion() {
		targetVersion = ProjectLogger.getInstance().getSnapshotList().size() - 1;
		System.out.println("Deleting unsatable version " + targetVersion
				+ " in the snapshotlist");
		ProjectLogger.getInstance().deleteSnapshot(targetVersion, true);
	}

	private void addMarkerOfBehavioralChanges(CompilationUnit astRoot,
			Report saferefactoReport) throws CoreException {
		// List<String> changedMethods2 =
		// saferefactoReport.getChangedMethods2();
		// String changes = saferefactoReport.getChanges();
		// String[] changesList = changes.split(";");
		Map<Integer, String> failedTests = saferefactoReport.getFailedTests();
		for (Entry<Integer, String> failedTest : failedTests.entrySet()) {
			Integer testId = failedTest.getKey();
			String testCase = failedTest.getValue();
			// System.out.println("behavioral change in the method: " + method);
			createMarkerForResource(astRoot.getJavaElement().getResource(),
					astRoot, testId, testCase);
		}
	}

	private Report compareBehaviorOfSnapshots() throws ExecutionException,
			JavaModelException, InexistentEntityException, AccessException,
			RemoteException, NotBoundException, URISyntaxException,
			IOException, ServerCreationException, InterruptedException {

		targetVersion = ProjectLogger.getInstance().getSnapshotList().size() - 1;

		List<String> methodsToTest = calculateImpactedMethods();

		List<Snapshot> snapshotList = ProjectLogger.getInstance()
				.getSnapshotList();

		String sourceFolder = snapshotList.get(sourceVersion).getPath();

		String targetPath = snapshotList.get(targetVersion).getPath();

		Project sourceP = configureProject(sourceFolder);

		Project targetP = configureProject(targetPath);

		System.out.println("Analyzing transformation: "
				+ sourceP.getBuildFolder().getName() + "-> "
				+ targetP.getBuildFolder().getName());

		Report report = evaluate(sourceP, targetP, methodsToTest);

		printResult(sourceP, targetP, report);

		return report;
	}

	private void printResult(Project sourceP, Project targetP, Report report) {
		String message = "Transformation: "
				+ sourceP.getBuildFolder().getName() + "-> "
				+ targetP.getBuildFolder().getName() + " is refactoring? "
				+ report.isRefactoring();
		// System.out.println(message);
		SafeRefactorActivator.getDefault().log(message);
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

	private Report evaluate(Project sourceP, Project targetP,
			List<String> methodsToTest) throws RemoteException,
			NotBoundException, AccessException, URISyntaxException, IOException {
		Registry registry = LocateRegistry.getRegistry("localhost");

		System.setSecurityManager(new RMISecurityManager());

		RemoteExecutor generatorServer = (RemoteExecutor) registry
				.lookup(ProjectLogger.getInstance().getSnapshotList()
						.get(sourceVersion).getServerName());

		String fileName = generateMethodListFile(methodsToTest);
		// Future<Boolean> futureIsServerLoaded = ProjectLogger.getInstance()
		// .getSnapshotList().get(targetVersion).getFutureIsServerLoaded();
		// Boolean isTargetServerLoaded = futureIsServerLoaded.get(3,
		// TimeUnit.SECONDS);
		// if (!isTargetServerLoaded)
		// throw new ServerCreationException(
		// "problem loading the target server");

		// debug
		System.out.println("running task on server...");
		Report result = generatorServer.executeTask(new CheckBehaviorChange(
				sourceP, targetP, fileName));
		return result;
	}

	private String generateMethodListFile(List<String> methodsToTest) {

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

	private List<String> calculateImpactedMethods()
			throws InterruptedException, JavaModelException,
			InexistentEntityException, ExecutionException, IOException {
		List<String> result = new ArrayList<String>();

		DesignWizard designWizard = getDesignWizardAnalyzer();
		if (this.changedElement instanceof IField) {
			IField changedField = (IField) this.changedElement;
			FieldNode field = designWizard.getField(changedField.toString());

			// methods that use this field
			// Set<MethodNode> callerMethods = field.getCallerMethods();
		} else {

			IMethod changedMethod = (IMethod) this.changedElement;
			// String targetSignature = generateIMethodSignature(changedMethod);
			String createMethodSignature = Signature.createMethodSignature(
					changedMethod.getParameterTypes(),
					changedMethod.getReturnType());

			ClassNode declaringClass = designWizard.getClass(changedMethod
					.getDeclaringType().getFullyQualifiedName());
			String[] parameterTypes = Signature
					.getParameterTypes(createMethodSignature);

			MethodNode method = null;

			method = getMethodFromDesignWizard(changedMethod, declaringClass,
					parameterTypes, method);

			// add constructor dependences for the method
			List<String> constructorDependence = generateConstructorDependences(method
					.getDeclaringClass());
			result.addAll(constructorDependence);
			Set<ClassNode> subClasses = method.getDeclaringClass()
					.getSubClasses();
			List<String> classesThatInheriteTheMethod = new ArrayList<String>();
			for (ClassNode classNode : subClasses) {
				if (classNode.isAbstract())
					continue;
				Set<MethodNode> allMethods = classNode.getAllMethods();
				if (allMethods.contains(method)) {
					result.addAll(generateConstructorDependences(classNode));
					classesThatInheriteTheMethod.add(classNode.getClassName());
				}

			}
			// add the target method to the list of methods to test
			result.add(toRandoopSignaturePattern(method,
					classesThatInheriteTheMethod));

			// add parameter dependences for the method
			for (ClassNode classNode : method.getParameters()) {
				result.addAll(generateConstructorDependences(classNode));
			}

			// get methods that call the targetMethod
			// Set<MethodNode> callerMethods = method.getCallerMethods();
			//
			// for (MethodNode caller : callerMethods) {
			// // add constructor dependences for the method
			// result.addAll(generateConstructorDependences(caller
			// .getDeclaringClass()));
			//
			// // add parameter dependences for the method
			// for (ClassNode classNode : caller.getParameters()) {
			// result.addAll(generateConstructorDependences(classNode));
			// }
			// result.add(toRandoopSignaturePattern(caller));
			// }

		}

		return result;
	}

	private MethodNode getMethodFromDesignWizard(IMethod changedMethod,
			ClassNode declaringClass, String[] parameterTypes, MethodNode method) {
		Set<MethodNode> allMethods2 = declaringClass.getAllMethods();
		for (MethodNode methodNode : allMethods2) {
			if (!methodNode.getShortName().equals(
					changedMethod.getElementName()))
				continue;
			List<ClassNode> parameters = methodNode.getParameters();

			boolean isMethod = true;
			for (String parameterSignature : parameterTypes) {
				boolean match = false;
				for (ClassNode classNode : parameters) {
					if (classNode.getShortName().equals(
							Signature.toString(parameterSignature))) {
						match = true;
						break;
					}
				}
				if (!match)
					isMethod = false;
			}
			if (isMethod)
				method = methodNode;

		}
		return method;
	}

	private List<String> generateConstructorDependences(ClassNode declaringClass) {
		List<String> result = new ArrayList<String>();

		Set<MethodNode> constructors = declaringClass.getConstructors();
		for (MethodNode constructor : constructors) {

			String randoopSignature = toRandoopSignaturePattern(constructor,
					null);
			// get constructor dependences
			List<ClassNode> parameters = constructor.getParameters();
			for (ClassNode classNode : parameters) {
				List<String> constructorDependences = generateConstructorDependences(classNode);
				result.addAll(constructorDependences);
			}
			result.add(randoopSignature);
		}
		return result;
	}

	private DesignWizard getDesignWizardAnalyzer() throws ExecutionException,
			InterruptedException {
		Future<DesignWizard> futureDesignWizard = ProjectLogger.getInstance()
				.getSnapshotList().get(sourceVersion).getFutureDesignWizard();
		DesignWizard designWizard = futureDesignWizard.get();

		return designWizard;
	}

	private String toRandoopSignaturePattern(MethodNode methodNode,
			List<String> classesThatInheriteTheMethod) {
		StringBuffer sb = new StringBuffer();
		String signature = methodNode.toString();
		if (methodNode.isConstructor()) {
			sb.append("cons : ");
			sb.append(signature);
		} else {
			sb.append("method : ");
			sb.append(signature.substring(0, signature.length() - 1));
			sb.append(" : ");
			sb.append(methodNode.getDeclaringClass().getName());
			if (classesThatInheriteTheMethod != null
					&& classesThatInheriteTheMethod.size() > 0) {
				sb.append(";");
				for (int i = 0; i < classesThatInheriteTheMethod.size(); i++) {
					String subClass = classesThatInheriteTheMethod.get(i);
					sb.append(subClass);
					if (i < classesThatInheriteTheMethod.size() - 1)
						sb.append(";");
				}
			}
		}
		return sb.toString();
	}

	private String logProject() throws IOException {
		Snapshot snapshot = ProjectLogger.getInstance().log();
		String classpath = snapshot.getPath();
		return classpath;
	}

	private void createMarkerForResource(IResource res,
			CompilationUnit compilationUnit, Integer testId, String testCase)
			throws CoreException {

		IMarker marker = res
				.createMarker(SafeRefactorActivator.SAFEREFACTOR_MARKER);

		// marker.setAttribute(IMarker.CHAR_START,
		// changedMethod.getStartPosition());
		// marker.setAttribute(IMarker.CHAR_END,
		// changedMethod.getStartPosition() + changedMethod.getLength());
		marker.setAttribute("coolFactor", "ULTRA");
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);

		String warning = "Test"
				+ testId
				+ " passed before the change, but had a different result after the change";
		marker.setAttribute(IMarker.MESSAGE, warning);
		marker.setAttribute("testid", testId);
		marker.setAttribute("testcase", testCase);
		int lineNumber = this.changeLine;
		marker.setAttribute(IMarker.LINE_NUMBER, lineNumber + 1);

		// System.out.println("marker: " + marker.getId() +
		// " created to method: "
		// + changedMethod.getName().getFullyQualifiedName());

	}

	/**
	 * @param astRoot
	 * @param classpath
	 *            The class path to the compilation unit
	 * @throws CompilationException
	 *             is thrown if it has compilation errors
	 * @throws PackageNotFoundException
	 *             if not able to get the package
	 */
	private void updateBinariesForTheChangedAST(CompilationUnit astRoot,
			String classpath) throws CompilationException,
			PackageNotFoundException {
		PackageDeclaration package1 = astRoot.getPackage();
		if (package1 == null) {
			throw new PackageNotFoundException();
		}
		String packageName = package1.getName().getFullyQualifiedName();
		String packageRelativeFolder = packageName.replace(".",
				Constants.SEPARATOR);

		String packageAbsoluteFolder = classpath + Constants.SEPARATOR
				+ packageRelativeFolder + Constants.SEPARATOR;
		String pathToSaveTheFile = packageAbsoluteFolder
				+ astRoot.getTypeRoot().getElementName();

		String fileToRemove = pathToSaveTheFile.replaceAll("\\.java", ".class");
		File oldVersion = new File(fileToRemove);
		oldVersion.delete();
		FileUtil.makeFile(pathToSaveTheFile, astRoot.toString());
		compileProject(classpath, pathToSaveTheFile);
	}

	/**
	 * @param classpath
	 * @param pathToSaveTheFile
	 * @throws CompilationException
	 */
	private void compileProject(String classpath, String pathToSaveTheFile)
			throws CompilationException {

		CompilationProgress progress = null;
		Writer writer = new StringWriter();
		PrintWriter errorPrintWriter = new PrintWriter(writer);

		StringBuffer parameters = new StringBuffer();
		parameters.append("-1.7 -classpath ");
		parameters.append(classpath).append(" ");
		parameters.append(pathToSaveTheFile);
		parameters.append(" -d").append(classpath);

		boolean successfullyCompiled = BatchCompiler.compile(
				parameters.toString(), new PrintWriter(System.out),
				errorPrintWriter, progress);

		if (!successfullyCompiled)
			throw new CompilationException("compilation error "
					+ writer.toString());
	}

	private boolean checkCompilationErrors(CompilationUnit astRoot) {
		IProblem[] problems = astRoot.getProblems();
		boolean hasCompilationError = false;
		for (IProblem iProblem : problems) {
			if (iProblem.isError()) {
				hasCompilationError = true;
				break;
			}
		}
		return hasCompilationError;
	}

	private CompilationUnit parseCompilationUnit() {
		ASTParser parser = ASTParser.newParser(AST.JLS4);
		parser.setSource((ICompilationUnit) compilationUnit);
		parser.setResolveBindings(true);
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
		return astRoot;
	}

	public IMarker[] findJavaProblemMarkers(ICompilationUnit cu)
			throws CoreException {

		IResource javaSourceFile = cu.getUnderlyingResource();
		IMarker[] markers = javaSourceFile.findMarkers(
				IJavaModelMarker.JAVA_MODEL_PROBLEM_MARKER, true,
				IResource.DEPTH_INFINITE);
		return markers;
	}

	public boolean isRunning() {
		return running;
	}

	public synchronized void setRunning(boolean running) {
		this.running = running;
	}

}
