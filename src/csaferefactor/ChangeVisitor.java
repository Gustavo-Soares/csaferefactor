package csaferefactor;

import java.io.File;
import java.rmi.RMISecurityManager;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.List;
import java.util.Random;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.jdt.core.dom.rewrite.ITrackedNodePosition;
import org.eclipse.jface.text.IDocument;
import org.eclipse.text.edits.TextEdit;

import randoop.ExecutableSequence;
import randoop.main.GenTests;
import saferefactor.core.Parameters;
import saferefactor.core.analysis.Report;
import saferefactor.core.analysis.TransformationAnalyzer;
import saferefactor.core.analysis.naive.ASMBasedAnalyzer;
import saferefactor.core.generation.RandoopAdapter;
import saferefactor.core.util.Constants;
import saferefactor.core.util.FileUtil;
import saferefactor.core.util.Project;
import saferefactor.core.util.ast.ConstructorImp;
import saferefactor.core.util.ast.Method;
import saferefactor.core.util.ast.MethodImp;
import saferefactor.rmi.client.CheckBehaviorChange;
import saferefactor.rmi.common.RemoteExecutor;

import csaferefactor.util.ProjectLogger;

class ChangeVisitor implements IResourceDeltaVisitor {

	private static final String SAFEREFACTOR_MARKER = "csaferefactor.saferefactorproblem";

	public boolean visit(IResourceDelta delta) {
		IResource res = delta.getResource();
		switch (delta.getKind()) {
		case IResourceDelta.ADDED:
			break;
		case IResourceDelta.REMOVED:
			break;
		case IResourceDelta.CHANGED:

//			if (chageJavaCode(delta, res)) {
//				try {
//					double start = System.currentTimeMillis();
//
//					removeExistingPluginMarkers(res);
//
//					// log project
//					ProjectLogger.getInstance().log();
//
//					// define projects
//					Project targetP = new Project();
//					File targetFolder = new File(ProjectLogger
//							.getInstance()
//							.getVersions()
//							.get(ProjectLogger.getInstance().getVersions()
//									.size() - 1));
//					targetP.setProjectFolder(targetFolder);
//					targetP.setBuildFolder(targetFolder);
//					targetP.setSrcFolder(targetFolder);
//
//					Project sourceP = new Project();
//
//					File sourceFolder = new File(ProjectLogger.getInstance()
//							.getVersions().get(0));
//					sourceP.setProjectFolder(sourceFolder);
//					sourceP.setBuildFolder(sourceFolder);
//					sourceP.setSrcFolder(sourceFolder);
//
//					// define parameters
//					Parameters parameters = new Parameters();
//					parameters.setCompileProjects(false);
//					parameters.setTimeLimit(1);
//					parameters.setAnalyzeChangeMethods(true);
//
//					// invoke saferefactor
//					Registry registry = LocateRegistry.getRegistry("localhost");
//					System.setSecurityManager(new RMISecurityManager());
//
//					RemoteExecutor generatorServer = (RemoteExecutor) registry
//							.lookup(SafeRefactorPlugin.SAFEREFACTOR_SERVER);
//
//					saferefactor.core.Report saferefactoReport = generatorServer
//							.executeTask(new CheckBehaviorChange(sourceP,
//									targetP, Activator.getDefault()
//											.getBinPath(), Activator
//											.getDefault()
//											.getSafeRefactorJarPath(),
//									Activator.getDefault()
//											.getSecurityPolicyPath()));
//
//					// TODO output the feedback
//					if (!saferefactoReport.isRefactoring()) {
//						// System.out.println("Behavioral change found!!");
//						// System.out.println("methods that changed: ");
//						List<String> changedMethods2 = saferefactoReport
//								.getChangedMethods2();
//						for (String method : changedMethods2) {
//							// ignore default contructor
//							if (!method.contains(".<init>()"))
//								createMarkerForResource(res, method);
//							// System.out.println(method);
//						}
//					}
//
//					double stop = System.currentTimeMillis();
//					double total = (stop - start) / 1000;
//					System.out.println("Total time (s): " + total);
//
//				} catch (Exception e) {
//
//					e.printStackTrace();
//				}
//
//				// try {
//				// System.out.println("resource mudou: " + res);
//				//
//				// ProjectLogger.getInstance().log();
//				// SafeRefactorJob srJob1 = new SafeRefactorJob(
//				// "saferefactor", 1, ProjectLogger.getInstance()
//				// .getVersions().size(), res);
//				// srJob1.schedule();
//				// } catch (Exception e) {
//				// e.printStackTrace();
//				// }
//
//			}

			break;
		}
		return true;
	}

	private void removeExistingPluginMarkers(IResource res) throws CoreException {
		// remove saferefactor old markers
		IMarker[] findMarkers = res.findMarkers(
				SAFEREFACTOR_MARKER, true, 1);
		for (IMarker iMarker : findMarkers) {
			iMarker.delete();
			System.out.println("marker removed");
		}
	}

	private void createMarkerForResource(IResource res, String method)
			throws CoreException {
		ICompilationUnit icu = JavaCore.createCompilationUnitFrom((IFile) res);
		CompilationUnit compilationUnit = getCompilationUnit(icu,
				new NullProgressMonitor());

		// trata o nome do m�todo para remover o nome da classe
		String[] methodNameParts = method.split("\\.");
		int length = methodNameParts.length;

		String target = methodNameParts[length - 1];
		System.out.println("method changed: " + target);
		MethodVisitor methodVisitor = new MethodVisitor(target);
		compilationUnit.accept(methodVisitor);
		org.eclipse.jdt.core.dom.MethodDeclaration changedMethod = methodVisitor
				.getMethod();

		IMarker marker = res.createMarker(SAFEREFACTOR_MARKER);

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
		System.out.println("marker: " + marker.getId() + " created to method: "
				+ changedMethod.getName().getFullyQualifiedName());

	}

	public static CompilationUnit getCompilationUnit(ICompilationUnit icu,
			IProgressMonitor monitor) {
		final ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(icu);
		parser.setResolveBindings(true);
		parser.setStatementsRecovery(true);
		parser.setBindingsRecovery(true);
		// parser.setIgnoreMethodBodies(true);
		// if (getCurrentInputKind() == ASTInputKindAction.USE_FOCAL) {
		// parser.setFocalPosition(offset);
		// }
		final CompilationUnit ret = (CompilationUnit) parser.createAST(monitor);
		return ret;
	}

	private boolean chageJavaCode(IResourceDelta delta, IResource res) {
		return res.getFileExtension() != null
				&& res.getFileExtension().equals("java")
				&& (delta.getFlags() & IResourceDelta.CONTENT) != 0;
	}
}