/**
 * 
 */
package csaferefactor.experiment;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import saferefactor.core.util.AntJavaCompiler;
import saferefactor.core.util.Compiler;
import saferefactor.core.util.Project;

/**
 * Contains useful methods to perform the {@link ExperimentTests}
 * 
 * @author Jeanderson Candido - <a href="mailto:jeandersonbc@gmail.com"
 *         target="new">jeandersonbc@gmail.com</a>
 * 
 */
public class Util {

	/**
	 * Shouldn't be public
	 */
	private Util() {
	}

	/**
	 * Compiles the given project to the build folder
	 * 
	 * @param project
	 *            The project to be compiled
	 * @throws MalformedURLException
	 * @throws FileNotFoundException
	 */
	public static void compileProject(Project project)
			throws MalformedURLException, FileNotFoundException {
		Compiler compiler = new AntJavaCompiler(
				System.getProperty("java.io.tmpdir"));
		compiler.setBinClasspath(project.getBuildFolder().getAbsolutePath());
		String pathname = project.getBuildFolder().getPath();
		compiler.compile(pathname, pathname);
	}

	/**
	 * Creates a project. The project, source, and build directories are all the
	 * same.
	 * 
	 * @param pathname
	 *            The path to source code
	 * @return A {@link Project} created from the given path
	 * @throws MalformedURLException
	 * @throws FileNotFoundException
	 */
	public static Project createProjectFromPath(String pathname)
			throws MalformedURLException, FileNotFoundException {
		File projectDir = new File(pathname);
		Project project = new Project();
		project.setSrcFolder(projectDir);
		project.setBuildFolder(projectDir);
		project.setProjectFolder(projectDir);
		Util.compileProject(project);

		return project;
	}

}
