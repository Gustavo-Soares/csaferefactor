package test;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;
import org.eclipse.ui.texteditor.ITextEditor;

import saferefactor.core.Parameters;
import saferefactor.core.Report;
import saferefactor.core.SafeRefactor;
import saferefactor.core.SafeRefactorImp;
import saferefactor.core.util.Project;

public class SafeRefactorChangeListener implements IResourceChangeListener,
		IPropertyListener {

	private int counter = 0;
	private ProjectLogger logger;
	private List<String> versions;
//	private List<String> versions2;
	

	public SafeRefactorChangeListener() throws IOException {
		IProject project = ResourcesPlugin.getWorkspace().getRoot()
				.getProject("test");
		logger = new ProjectLogger(project);
		versions = new ArrayList<String>();

		String path = logger.log();
		versions.add(path);
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {

		IResource res = event.getResource();
		switch (event.getType()) {
		case IResourceChangeEvent.POST_CHANGE:
			// System.out.println("Resources have changed.");
			// System.out.println(res.getFullPath());
			// event.getDelta().accept(new DeltaPrinter());
			break;
		case IResourceChangeEvent.PRE_BUILD:
			// System.out.println("Build about to run.");
			// System.out.println(res.getName());
			// event.getDelta().accept(new DeltaPrinter());
			break;
		case IResourceChangeEvent.POST_BUILD:
			// System.out.println("Build complete.");
			// System.out.println(res.getName());

			try {
				event.getDelta().accept(new DeltaPrinter());

				String path = logger.log();
				versions.add(path);
				SafeRefactorJob srJob1 = new SafeRefactorJob("saferefactor", versions.size() - 1,versions.size(),versions);
				srJob1.schedule();
//				runSafeRefactor(versions.size() - 1,versions.size());
				
				//run saferefactor between the last and the first
				if (versions.size() > 2) {
//					runSafeRefactor(1,versions.size());
					SafeRefactorJob srJob2 = new SafeRefactorJob("saferefactor", 1,versions.size(),versions);
					srJob2.schedule();
				}

				

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;
		}

	}

	private void runSafeRefactor(int input, int output) throws Exception {
		
		
		System.out.println("run saferefactor");
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

		File saferefactorJar = new File(Activator.getDefault()
				.getPluginFolder() + "/" + "lib/saferefactor-beta.jar");
		System.setProperty("extra.jars",
				saferefactorJar.getAbsolutePath());

		System.out.println(sourceP.getBuildFolder());
		System.out.println(targetP.getBuildFolder());
		SafeRefactor saferefactor = new SafeRefactorImp(sourceP,
				targetP, parameters);

		saferefactor.checkTransformation();
		Report report = saferefactor.getReport();
		// System.out.println("Preserve behavior? : "
		// + report.isRefactoring());

		MessageConsole console = findConsole("saferefactor");
		MessageConsoleStream out = console.newMessageStream();
		out.println("v" + input + "-> v" + output + " is refactoring? " + report.isRefactoring());
	}

	private MessageConsole findConsole(String name) {
		ConsolePlugin plugin = ConsolePlugin.getDefault();
		IConsoleManager conMan = plugin.getConsoleManager();
		IConsole[] existing = conMan.getConsoles();
		for (int i = 0; i < existing.length; i++)
			if (name.equals(existing[i].getName()))
				return (MessageConsole) existing[i];
		// no console found, so create a new one
		MessageConsole myConsole = new MessageConsole(name, null);
		conMan.addConsoles(new IConsole[] { myConsole });
		return myConsole;
	}

	@Override
	public void propertyChanged(Object source, int propId) {

		switch (propId) {
		case ITextEditor.PROP_DIRTY:
			String path;
			try {

				path = logger.log();
				versions.add(path);
				System.out.println("logged version to: " + path);
				counter++;
				// if (counter % 2 == 0) {
				// System.out.println("run saferefactor");
				// Project targetP = new Project();
				// File targetFolder = new File(
				// versions.get(versions.size() - 1));
				// targetP.setProjectFolder(targetFolder);
				// targetP.setBuildFolder(targetFolder);
				// targetP.setSrcFolder(targetFolder);
				// Project sourceP = new Project();
				//
				// File sourceFolder = new File(
				// versions.get(versions.size() - 2));
				// sourceP.setProjectFolder(sourceFolder);
				// sourceP.setBuildFolder(sourceFolder);
				// sourceP.setSrcFolder(sourceFolder);
				// Parameters parameters = new Parameters();
				// parameters.setCompileProjects(false);
				//
				// File saferefactorJar = new File(Activator.getDefault()
				// .getPluginFolder()
				// + "/"
				// + "lib/saferefactor-1.1.6.jar");
				// System.setProperty("extra.jars",
				// saferefactorJar.getAbsolutePath());
				//
				// System.out.println(sourceP.getBuildFolder());
				// System.out.println(targetP.getBuildFolder());
				// AbstractSafeRefactor saferefactor = new SafeRefactor(
				// sourceP, targetP, parameters);
				//
				// saferefactor.checkTransformation();
				// Report report = saferefactor.getReport();
				// System.out.println("Preserve behavior? : "
				// + report.isRefactoring());
				// }

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			break;

		default:
			break;
		}

	}

}
