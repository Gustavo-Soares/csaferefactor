package continousrefactoring;

import java.io.File;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.MessageConsole;
import org.eclipse.ui.console.MessageConsoleStream;


import saferefactor.core.Parameters;
import saferefactor.core.Report;
import saferefactor.core.SafeRefactor;
import saferefactor.core.SafeRefactorImp;
import saferefactor.core.util.Project;

public class SafeRefactorJob extends Job {

	

	private final int output;
	private final int input;
	private final List<String> versions;

	public SafeRefactorJob(String name, int input, int output, List<String> versions) {
		super(name);
		this.input = input;
		this.output = output;
		this.versions = versions;
		
	}

	@Override
	protected IStatus run(IProgressMonitor monitor) {

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
		try {
			SafeRefactor saferefactor = new SafeRefactorImp(sourceP,
					targetP, parameters);
			saferefactor.checkTransformation();
			Report report = saferefactor.getReport();
			// System.out.println("Preserve behavior? : "
			// + report.isRefactoring());

			MessageConsole console = findConsole("saferefactor");
			MessageConsoleStream out = console.newMessageStream();
			out.println("v" + input + "-> v" + output + " is refactoring? " + report.isRefactoring());
		} catch (Exception e) {			
			e.printStackTrace();
		}		
		return Status.OK_STATUS;
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

}
