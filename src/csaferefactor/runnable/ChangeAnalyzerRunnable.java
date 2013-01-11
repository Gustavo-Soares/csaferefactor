package csaferefactor.runnable;

import java.io.File;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.compiler.batch.BatchCompiler;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;

import csaferefactor.Activator;
import csaferefactor.ProjectLogger;
import csaferefactor.SafeRefactorPlugin;
import csaferefactor.Snapshot;
import csaferefactor.exception.CompilationException;
import csaferefactor.visitor.MethodVisitor;

import saferefactor.core.Report;
import saferefactor.core.util.Constants;
import saferefactor.core.util.FileUtil;

public class ChangeAnalyzerRunnable implements Runnable {

	private IJavaElement compilationUnit;
	private int sourceVersion;
	private int targetVersion;

	public ChangeAnalyzerRunnable(String name, IJavaElement javaElement) {
		this.compilationUnit = javaElement;
	}

	@Override
	public void run() {

		CompilationUnit astRoot = parseCompilationUnit();

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
					ProjectLogger.getInstance().deleteSnapshot(sourceVersion);
				} else {
					addMarkerToChangedMethods(astRoot, saferefactoReport);
					// delete the target snapshot
					ProjectLogger.getInstance().deleteSnapshot(targetVersion);
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
				// TODO: when compilation error is found, how to show that to
				// user
				System.out.println("version does not compile: ");
				System.out.println(e.getMessage());
				deleteUnstableVersion();
			}

		}

	}
	
	private void deleteUnstableVersion() {
		System.out.println("Deleting unsatable version " + targetVersion
				+ " in the snapshotlist");
		ProjectLogger.getInstance().deleteSnapshot(targetVersion);
	}

	private void addMarkerToChangedMethods(CompilationUnit astRoot,
			Report saferefactoReport) throws CoreException {
		List<String> changedMethods2 = saferefactoReport.getChangedMethods2();
		for (String method : changedMethods2) {
			// ignore default contructor
			if (!method.contains(".<init>()"))
				createMarkerForResource(astRoot.getJavaElement().getResource(),
						astRoot, method);
			// System.out.println(method);
		}
	}

	private Report compareBehaviorOfSnapshots() throws InterruptedException,
			ExecutionException {
		ExecutorService executor = Executors.newFixedThreadPool(1);

		targetVersion = ProjectLogger.getInstance().getSnapshotList().size() - 1;

		//TODO: does it really need to be a runnable? It seems that it does not. 
		SafeRefactorRunnable runnable1 = new SafeRefactorRunnable("runnable1",
				sourceVersion, targetVersion);
		Future submit = executor.submit(runnable1);
		submit.get();
		// if no behavioral change, remove head
		Report saferefactoReport = runnable1.getSaferefactoReport();
		return saferefactoReport;
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

	// private MessageConsole findConsole(String name) {
	// ConsolePlugin plugin = ConsolePlugin.getDefault();
	// IConsoleManager conMan = plugin.getConsoleManager();
	// IConsole[] existing = conMan.getConsoles();
	// for (int i = 0; i < existing.length; i++)
	// if (name.equals(existing[i].getName()))
	// return (MessageConsole) existing[i];
	// // no console found, so create a new one
	// MessageConsole myConsole = new MessageConsole(name, null);
	// conMan.addConsoles(new IConsole[] { myConsole });
	// return myConsole;
	// }
	//

}
