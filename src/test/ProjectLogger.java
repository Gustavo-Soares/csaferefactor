package test;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;

import saferefactor.core.util.FileUtil;

public class ProjectLogger {

	private final IProject project;
	private final String sourceFolder;
	private int counter = 0;
	private String projectPath;
	
	public ProjectLogger(IProject project) {
		this.project = project;
		
		IWorkspace workspace = ResourcesPlugin.getWorkspace(); 
		IWorkspaceRoot root = workspace.getRoot(); 
		IPath location = root.getLocation(); 
		String str=location.toPortableString(); 
//		System.out.println("WORK SPACE PATH:"+str); 
		
		projectPath = project.getFullPath().toString();
//		System.out.println(projectPath.toFile().toString());
		sourceFolder = str + projectPath + "/bin";		
	}

	public String log() throws IOException {
		File source = new File(sourceFolder);
		String logDir = System.getProperty("java.io.tmpdir");
		String targetFolder = logDir + projectPath + counter;
		File target = new File(targetFolder);
		while (target.exists()) {
			counter++;
			targetFolder = logDir + projectPath + counter;
			target = new File(targetFolder);
		}
		target.mkdir();
		FileUtil.copyFolder(source, target);
		
		
		return targetFolder;
	}

}
