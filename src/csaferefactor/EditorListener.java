package csaferefactor;


import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.ui.IPropertyListener;
import org.eclipse.ui.texteditor.ITextEditor;
import saferefactor.core.Parameters;
import saferefactor.core.Report;
import saferefactor.core.SafeRefactor;
import saferefactor.core.SafeRefactorImp;
import saferefactor.core.util.Project;

public class EditorListener implements IPropertyListener {

	private int counter = 0;
	private ProjectLogger logger;
	private List<String> versions; 
	
	public EditorListener() {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject("test");
		logger = new ProjectLogger(project);
		versions = new ArrayList<String>();
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
				if (counter % 2 == 0) {
					System.out.println("run saferefactor");
					Project targetP = new Project();
					File targetFolder = new File(versions.get(versions.size()-1));
					targetP.setProjectFolder(targetFolder);
					targetP.setBuildFolder(targetFolder);
					targetP.setSrcFolder(targetFolder);
					Project sourceP = new Project();
					
					File sourceFolder = new File(versions.get(versions.size()-2));
					sourceP.setProjectFolder(sourceFolder);
					sourceP.setBuildFolder(sourceFolder);
					sourceP.setSrcFolder(sourceFolder);
					Parameters parameters = new Parameters();
					parameters.setCompileProjects(false);
					
					File saferefactorJar = new File(Activator.getDefault().getPluginFolder() + "/" + "lib/saferefactor-1.1.6.jar");
					System.setProperty("extra.jars", saferefactorJar.getAbsolutePath());
					
					System.out.println(sourceP.getBuildFolder());
					System.out.println(targetP.getBuildFolder());
					SafeRefactor saferefactor = new SafeRefactorImp(sourceP, targetP,parameters);
					
					saferefactor.checkTransformation();
					Report report = saferefactor.getReport();
					System.out.println("Preserve behavior? : " + report.isRefactoring());
				}
					
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
