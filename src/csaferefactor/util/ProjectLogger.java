package csaferefactor.util;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import saferefactor.core.util.FileUtil;

public class ProjectLogger {

	private final String sourceFolder;
	private int counter = 0;
	private String projectPath;
	private List<String> versions = new ArrayList<String>();

	private static ProjectLogger instance;

	private ProjectLogger() {

		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		IWorkspaceRoot root = workspace.getRoot();
		IPath location = root.getLocation();
		String str = location.toPortableString();

		IWorkbenchPage page = PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
		IEditorPart editorPart = page.getActiveEditor();
		IJavaElement javaElement = JavaUI.getEditorInputJavaElement(editorPart
				.getEditorInput());
		IJavaProject javaProject = javaElement.getJavaProject();

		projectPath = javaProject.getProject().getFullPath().toString();
		sourceFolder = str + projectPath + "/bin";
	}

	public static ProjectLogger getInstance() {
		if (instance == null)
			instance = new ProjectLogger();
		return instance;
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
		versions.add(targetFolder);
		return targetFolder;
	}

	public List<String> getVersions() {
		return versions;
	}


}
