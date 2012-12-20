package csaferefactor;

import java.io.File;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;

import saferefactor.core.Parameters;
import saferefactor.core.Report;
import saferefactor.core.SafeRefactor;
import saferefactor.core.SafeRefactorImp;
import saferefactor.core.util.Project;
import saferefactor.core.util.ast.Method;

public class SafeRefactorJob extends Job {

	private static final String SAFEREFACTOR_MARKER = "csaferefactor.saferefactorproblem";
	private final int output;
	private final int input;
	private final List<String> versions;
	private IResource res = null;
//	private IJavaElement element = null;

	public SafeRefactorJob(String name, int input, int output,
			List<String> versions, IResource res) {
		super(name);
		this.input = input;
		this.output = output;
		this.versions = versions;
		this.res = res;

	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {

		//remove saferefactor old markers		
		try {
			IMarker[] findMarkers = res.findMarkers(SAFEREFACTOR_MARKER, true, 1);
			for (IMarker iMarker : findMarkers) {
				iMarker.delete();
				System.out.println("marker removed");
			}
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		//run saferefactor	
		Project targetP = new Project();
		File targetFolder = new File(versions.get(output - 1));
		targetP.setProjectFolder(targetFolder);
		targetP.setBuildFolder(targetFolder);
		targetP.setSrcFolder(targetFolder);
		Project sourceP = new Project();

		File sourceFolder = new File(versions.get(input - 1));
		sourceP.setProjectFolder(sourceFolder);
		sourceP.setBuildFolder(sourceFolder);
		sourceP.setSrcFolder(sourceFolder);
		Parameters parameters = new Parameters();
		parameters.setCompileProjects(false);
		parameters.setTimeLimit(1);
		parameters.setAnalyzeChangeMethods(true);

		File saferefactorJar = new File(Activator.getDefault()
				.getPluginFolder() + "/" + "lib/saferefactor-beta.jar");
		System.setProperty("extra.jars", saferefactorJar.getAbsolutePath());

		System.out.println(sourceP.getBuildFolder());
		System.out.println(targetP.getBuildFolder());
		try {
			SafeRefactor saferefactor = new SafeRefactorImp(sourceP, targetP,
					parameters);
			saferefactor.checkTransformation();
			Report report = saferefactor.getReport();

			if (!report.isRefactoring()) {
				List<Method> changedMethods = report
						.getChangedMethods();
				for (Method method : changedMethods) {
					createMarkerForResource(res, method);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
		return Status.OK_STATUS;
	}

	private void createMarkerForResource(IResource res, Method method)
			throws CoreException {
		 ICompilationUnit icu = JavaCore.createCompilationUnitFrom((IFile)
		 res);
		CompilationUnit compilationUnit = getCompilationUnit(
				icu, new NullProgressMonitor());
		MethodVisitor methodVisitor = new MethodVisitor(method.getSimpleName());
		compilationUnit.accept(methodVisitor);
		org.eclipse.jdt.core.dom.MethodDeclaration changedMethod = methodVisitor
				.getMethod();

		IMarker marker = res
				.createMarker(SAFEREFACTOR_MARKER);
		
//		 marker.setAttribute(IMarker.CHAR_START,
//		 changedMethod.getStartPosition());
		// marker.setAttribute(IMarker.CHAR_END,
		// changedMethod.getStartPosition() + changedMethod.getLength());
		marker.setAttribute("coolFactor", "ULTRA");
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_ERROR);
		marker.setAttribute(IMarker.MESSAGE,
				"The behavior of this method was changed");
		int lineNumber = compilationUnit.getLineNumber(changedMethod.getStartPosition());
		marker.setAttribute(IMarker.LINE_NUMBER,
				lineNumber);
		System.out.println("marker: " + marker.getId()  + " created to method: " + changedMethod.getName().getFullyQualifiedName());

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

//	private MessageConsole findConsole(String name) {
//		ConsolePlugin plugin = ConsolePlugin.getDefault();
//		IConsoleManager conMan = plugin.getConsoleManager();
//		IConsole[] existing = conMan.getConsoles();
//		for (int i = 0; i < existing.length; i++)
//			if (name.equals(existing[i].getName()))
//				return (MessageConsole) existing[i];
//		// no console found, so create a new one
//		MessageConsole myConsole = new MessageConsole(name, null);
//		conMan.addConsoles(new IConsole[] { myConsole });
//		return myConsole;
//	}
//

}
