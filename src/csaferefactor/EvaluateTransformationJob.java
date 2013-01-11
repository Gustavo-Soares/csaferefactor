package csaferefactor;

import java.io.BufferedOutputStream;
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
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobManager;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaModelMarker;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.CompilationProgress;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.compiler.batch.BatchCompiler;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

import csaferefactor.util.ProjectLogger;

import saferefactor.core.Parameters;
import saferefactor.core.Report;
import saferefactor.core.SafeRefactor;
import saferefactor.core.SafeRefactorImp;
import saferefactor.core.util.Constants;
import saferefactor.core.util.FileUtil;
import saferefactor.core.util.Project;
import saferefactor.core.util.ast.Method;
import saferefactor.rmi.client.CheckBehaviorChange;
import saferefactor.rmi.common.RemoteExecutor;
import saferefactor.rmi.common.VMInitializer;

public class EvaluateTransformationJob implements Runnable {

	private IJavaElement compilationUnit;

	public EvaluateTransformationJob(String name, IJavaElement javaElement) {
		// super(name);
		this.compilationUnit = javaElement;
	}

	// @Override
	// protected IStatus run(IProgressMonitor monitor) {
	//
	// evaluateTransformation();
	//
	// IJobManager jobMan = Job.getJobManager();
	// try {
	// jobMan.join(SafeRefactorPlugin.MY_FAMILY, null);
	// } catch (OperationCanceledException e) {
	// e.printStackTrace();
	// return Status.CANCEL_STATUS;
	//
	// } catch (InterruptedException e) {
	// e.printStackTrace();
	// return Status.CANCEL_STATUS;
	// }
	//
	// return Status.OK_STATUS;
	// }

	@Override
	public void run() {
		CompilationUnit astRoot = parseCompilationUnit();

		boolean hasCompilationError = checkCompilationErrors(astRoot);

		if (!hasCompilationError) {
			int sourceVersion = 0;
			int targetVersion = 0;
			try {

				Activator.getDefault().removeExistingPluginMarkers((ICompilationUnit) compilationUnit);

				// if snapshotList has more than 2 versions, remove second
				// version, which we are not interested in evaluate the behavior
//				if (ProjectLogger.getInstance().getSnapshotList().size() > 2)
//					ProjectLogger.getInstance().deleteSnapshot(1);

				// 1. log the project
				Snapshot snapshot = ProjectLogger.getInstance().log();
				String classpath = snapshot.getPath();

				updateBinariesForTheChangedAST(astRoot, classpath);

				ExecutorService executor = Executors.newFixedThreadPool(1);

				targetVersion = ProjectLogger.getInstance().getSnapshotList()
						.size() - 1;

				SafeRefactorRunnable runnable1 = new SafeRefactorRunnable(
						"runnable1", sourceVersion, targetVersion, astRoot);
				Future submit = executor.submit(runnable1);
				SafeRefactorRunnable runnable2 = null;
//				if (ProjectLogger.getInstance().getSnapshotList().size() > 2) {
//					sourceVersion = targetVersion - 1;
//					runnable2 = new SafeRefactorRunnable("runnable2",
//							sourceVersion, targetVersion, astRoot);
//					Future submit2 = executor.submit(runnable2);
//					submit2.get();
//				}
				submit.get();

				// if no behavioral change, remove head
				if (runnable1.getSaferefactoReport().isRefactoring()) {
					if ((runnable2 == null))
						ProjectLogger.getInstance().deleteSnapshot(0);
//					else if (runnable2.getSaferefactoReport().isRefactoring()) {
//						ProjectLogger.getInstance().deleteSnapshot(0);
//						ProjectLogger.getInstance().deleteSnapshot(1);
//					}

				} else {

					List<String> changedMethods2 = runnable1
							.getSaferefactoReport().getChangedMethods2();
					for (String method : changedMethods2) {
						// ignore default contructor
						if (!method.contains(".<init>()"))
							createMarkerForResource(astRoot.getJavaElement()
									.getResource(), astRoot, method);
						// System.out.println(method);
					}
					ProjectLogger.getInstance().deleteSnapshot(targetVersion);
				}
//				if (runnable2 != null
//						&& !runnable2.getSaferefactoReport().isRefactoring()) {
//					List<String> changedMethods2 = runnable2
//							.getSaferefactoReport().getChangedMethods2();
//					for (String method : changedMethods2) {
//						// ignore default contructor
//						if (!method.contains(".<init>()"))
//							createMarkerForResource(astRoot.getJavaElement()
//									.getResource(), astRoot, method);
//						// System.out.println(method);
//					}
//
//				}

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}  catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (OperationCanceledException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {

				// if thread is interrupted, delete this snapshot
				System.out.println("Deleting unsatable version "
						+ targetVersion + " in the snapshotlist");
				ProjectLogger.getInstance().deleteSnapshot(targetVersion);

			} catch (ExecutionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CompilationException e) {
				// if thread is interrupted, delete this snapshot
				System.out.println("version does not compile: ");
				System.out.println(e.getMessage());
				System.out.println("Deleting unsatable version "
						+ targetVersion + " in the snapshotlist");
				ProjectLogger.getInstance().deleteSnapshot(targetVersion);
			}

		}

	}

	private void createMarkerForResource(IResource res,
			CompilationUnit compilationUnit, String method)
			throws CoreException {

		// trata o nome do método para remover o nome da classe
		String[] methodNameParts = method.split("\\.");
		int length = methodNameParts.length;

		String target = methodNameParts[length - 1];
//		System.out.println("method changed: " + target);
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
//		System.out.println("marker: " + marker.getId() + " created to method: "
//				+ changedMethod.getName().getFullyQualifiedName());

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
			throw new CompilationException("compilation error " + writer.toString());
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
