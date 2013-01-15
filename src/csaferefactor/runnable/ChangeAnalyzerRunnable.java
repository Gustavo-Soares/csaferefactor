package csaferefactor.runnable;

import java.io.File;

import java.io.IOException;
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
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.designwizard.design.ClassNode;
import org.designwizard.design.MethodNode;
import org.designwizard.exception.InexistentEntityException;
import org.designwizard.main.DesignWizard;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jdt.core.IClassFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IOpenable;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ITypeHierarchy;
import org.eclipse.jdt.core.ITypeRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.compiler.batch.BatchCompiler;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.search.SearchEngine;
import org.eclipse.jdt.internal.corext.callhierarchy.CallHierarchy;
import org.eclipse.jdt.internal.corext.callhierarchy.MethodWrapper;

import csaferefactor.Activator;
import csaferefactor.ProjectLogger;
import csaferefactor.SafeRefactorPlugin;
import csaferefactor.Snapshot;
import csaferefactor.exception.CompilationException;
import csaferefactor.exception.ServerCreationException;
import csaferefactor.visitor.MethodVisitor;

import randoop.experiments.TargetMaker;
import saferefactor.core.Report;
import saferefactor.core.util.Constants;
import saferefactor.core.util.FileUtil;
import saferefactor.core.util.Project;
import saferefactor.rmi.client.CheckBehaviorChange;
import saferefactor.rmi.common.RemoteExecutor;

public class ChangeAnalyzerRunnable implements Runnable {

	private IJavaElement compilationUnit;
	private int sourceVersion;
	private int targetVersion;
	private CompilationUnit astRoot;
	private IMethod changedMethod;

	public ChangeAnalyzerRunnable(String name, IJavaElement javaElement,
			IMethod changedMethod) {
		this.compilationUnit = javaElement;
		this.changedMethod = changedMethod;
	}

	@Override
	public void run() {

		astRoot = parseCompilationUnit();

		boolean hasCompilationError = checkCompilationErrors(astRoot);

		if (!hasCompilationError) {
			sourceVersion = 0;
			targetVersion = 0;
			try {
				Activator.getDefault().removeExistingPluginMarkers(
						(ICompilationUnit) compilationUnit);

				String classpath = logProject();

				updateBinariesForTheChangedAST(astRoot, classpath);

				Report saferefactoReport = compareBehaviorOfSnapshots();

				if (saferefactoReport.isRefactoring()) {
					// if no behavioral change, desconsider the old snapshot
					ProjectLogger.getInstance().deleteSnapshot(sourceVersion,
							true);
				} else {
					addMarkerToChangedMethods(astRoot, saferefactoReport);
					// delete the target snapshot
					ProjectLogger.getInstance().deleteSnapshot(targetVersion,
							true);

				}

			} catch (IOException e) {
				e.printStackTrace();
			} catch (CoreException e) {
				e.printStackTrace();
			} catch (OperationCanceledException e) {
				e.printStackTrace();
			} catch (InterruptedException e) {
				// if thread is interrupted, delete this snapshot
				deleteUnstableVersion();

			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (CompilationException e) {
				// if thread is interrupted, delete this snapshot
				// TODO when compilation error is found, how to show that to
				// user
				System.out.println("version does not compile: ");
				System.out.println(e.getMessage());
				deleteUnstableVersion();
			} catch (InexistentEntityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TimeoutException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NotBoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ServerCreationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
	}

	private void deleteUnstableVersion() {
		targetVersion = ProjectLogger.getInstance().getSnapshotList().size() - 1;
		System.out.println("Deleting unsatable version " + targetVersion
				+ " in the snapshotlist");
		ProjectLogger.getInstance().deleteSnapshot(targetVersion, true);
	}

	private void addMarkerToChangedMethods(CompilationUnit astRoot,
			Report saferefactoReport) throws CoreException {
		List<String> changedMethods2 = saferefactoReport.getChangedMethods2();

		for (String method : changedMethods2) {
			System.out.println("behavioral change in the method: " + method);
			createMarkerForResource(astRoot.getJavaElement().getResource(),
					astRoot, method);
		}
	}

	private Report compareBehaviorOfSnapshots() throws InterruptedException,
			ExecutionException, JavaModelException, InexistentEntityException,
			TimeoutException, AccessException, RemoteException,
			NotBoundException, URISyntaxException, IOException,
			ServerCreationException {
		ExecutorService executor = Executors.newFixedThreadPool(1);

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
		System.out.println("Transformation: "
				+ sourceP.getBuildFolder().getName() + "-> "
				+ targetP.getBuildFolder().getName() + " is refactoring? "
				+ report.isRefactoring());
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

	private Report evaluate(Project sourceP, Project targetP, List<String> methodsToTest)
			throws RemoteException, NotBoundException, AccessException,
			URISyntaxException, IOException, InterruptedException,
			ExecutionException, ServerCreationException, TimeoutException {
		Registry registry = LocateRegistry.getRegistry("localhost");

		System.setSecurityManager(new RMISecurityManager());

		RemoteExecutor generatorServer = (RemoteExecutor) registry
				.lookup(ProjectLogger.getInstance().getSnapshotList()
						.get(sourceVersion).getServerName());

		String fileName = generateMethodListFile(methodsToTest);
		Future<Boolean> futureIsServerLoaded = ProjectLogger.getInstance()
				.getSnapshotList().get(targetVersion).getFutureIsServerLoaded();
//		Boolean isTargetServerLoaded = futureIsServerLoaded.get(3,
//				TimeUnit.SECONDS);
//		if (!isTargetServerLoaded)
//			throw new ServerCreationException(
//					"problem loading the target server");

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
			InexistentEntityException {
		List<String> result = new ArrayList<String>();

		String targetSignature = generateIMethodSignature();
		DesignWizard designWizard = getDesignWizardAnalyzer();
		MethodNode method = designWizard.getMethod(targetSignature);

		// add the target method to the list of methods to test
		result.add(toRandoopSignaturePattern(method));

		// add constructor dependences for the method
		List<String> constructorDependence = generateConstructorDependences(method
				.getDeclaringClass());
		result.addAll(constructorDependence);

		// add parameter dependences for the method
		for (ClassNode classNode : method.getParameters()) {
			result.addAll(generateConstructorDependences(classNode));
		}

		// get methods that call the targetMethod
		Set<MethodNode> callerMethods = method.getCallerMethods();

		for (MethodNode caller : callerMethods) {
			// add constructor dependences for the method
			result.addAll(generateConstructorDependences(caller
					.getDeclaringClass()));

			// add parameter dependences for the method
			for (ClassNode classNode : caller.getParameters()) {
				result.addAll(generateConstructorDependences(classNode));
			}
			result.add(toRandoopSignaturePattern(caller));
		}
		return result;
	}

	private List<String> generateConstructorDependences(ClassNode declaringClass) {
		List<String> result = new ArrayList<String>();

		Set<MethodNode> constructors = declaringClass.getConstructors();
		for (MethodNode constructor : constructors) {
			String randoopSignature = toRandoopSignaturePattern(constructor);
			result.add(randoopSignature);
		}
		return result;
	}

	private DesignWizard getDesignWizardAnalyzer() throws InterruptedException {
		DesignWizardThread designWizardThread = ProjectLogger.getInstance()
				.getSnapshotList().get(sourceVersion).getDesignWizardRunner();
		if (designWizardThread.isAlive())
			designWizardThread.join();
		DesignWizard designWizard = designWizardThread.getDesignWizard();
		return designWizard;
	}

	// private String toRandoopSignaturePattern(String targetSignature)
	// throws JavaModelException {
	// StringBuffer result = new StringBuffer();
	// if (changedMethod.isConstructor()) {
	// result.append("cons : ");
	// result.append(targetSignature);
	// } else {
	// result.append("method : ");
	// result.append(targetSignature);
	// result.append(" : ");
	// result.append(changedMethod.getDeclaringType()
	// .getFullyQualifiedName());
	// }
	// return result.toString();
	// }

	private String generateIMethodSignature() throws JavaModelException {

		String createMethodSignature = Signature.createMethodSignature(
				changedMethod.getParameterTypes(),
				changedMethod.getReturnType());

		String string = Signature.toString(createMethodSignature,
				changedMethod.getElementName(),
				changedMethod.getParameterNames(), true, false);
		String signature = changedMethod.getDeclaringType()
				.getFullyQualifiedName() + "." + string;

		return signature;
	}

	private String toRandoopSignaturePattern(MethodNode methodNode) {
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
		}
		return sb.toString();
	}

	private String logProject() throws IOException {
		Snapshot snapshot = ProjectLogger.getInstance().log();
		String classpath = snapshot.getPath();
		return classpath;
	}

	private void createMarkerForResource(IResource res,
			CompilationUnit compilationUnit, String method)
			throws CoreException {

		// trata o nome do método para remover o nome da classe
		String[] methodNameParts = method.split("\\.");
		int length = methodNameParts.length;

		String target = methodNameParts[length - 1];
		// System.out.println("method changed: " + target);
		MethodVisitor methodVisitor = new MethodVisitor(target);
		compilationUnit.accept(methodVisitor);
		org.eclipse.jdt.core.dom.MethodDeclaration changedMethod = methodVisitor
				.getMethod();

		IMarker marker = res
				.createMarker(SafeRefactorPlugin.SAFEREFACTOR_MARKER);

		// marker.setAttribute(IMarker.CHAR_START,
		// changedMethod.getStartPosition());
		// marker.setAttribute(IMarker.CHAR_END,
		// changedMethod.getStartPosition() + changedMethod.getLength());
		marker.setAttribute("coolFactor", "ULTRA");
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		marker.setAttribute(IMarker.MESSAGE,
				"The behavior of this method was changed");
		int lineNumber = compilationUnit.getLineNumber(changedMethod
				.getStartPosition());
		marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
		// System.out.println("marker: " + marker.getId() +
		// " created to method: "
		// + changedMethod.getName().getFullyQualifiedName());

	}

	private void updateBinariesForTheChangedAST(CompilationUnit astRoot,
			String classpath) throws CompilationException {
		// 2. make a file in a tmp folder for the new AST
		PackageDeclaration package1 = astRoot.getPackage();
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

		// System.out.println(astRoot.toString());
		FileUtil.makeFile(pathToSaveTheFile, astRoot.toString());
		// System.out.println(package1);

		// 3. compile it.
		// TODO need to get the right classpath of the project to
		// pass to the compiler
		CompilationProgress progress = null; // instantiate your
												// subclass

		Writer writer = new StringWriter();
		PrintWriter errorPrintWriter = new PrintWriter(writer);
		BatchCompiler.compile("-classpath " + classpath + " "
				+ pathToSaveTheFile + " -d " + classpath, new PrintWriter(
				System.out), errorPrintWriter, progress);
		if (writer.toString().contains("error"))
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

}
