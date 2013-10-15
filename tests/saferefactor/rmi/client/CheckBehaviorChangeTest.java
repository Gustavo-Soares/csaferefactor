/**
 * 
 */
package saferefactor.rmi.client;

import static org.junit.Assert.fail;

import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;

import org.junit.Assert;
import org.junit.Test;

import saferefactor.core.Report;
import saferefactor.core.util.Project;
import saferefactor.rmi.common.Task;
import csaferefactor.core.CheckBehaviorChange;
import csaferefactor.experiment.Util;

/**
 * @author Jeanderson Candido - <a href="mailto:jeandersonbc@gmail.com"
 *         target="new">jeandersonbc@gmail.com</a>
 * 
 */
public class CheckBehaviorChangeTest {

	private Task<Report> checkBehaviorChanges;

	/**
	 * Test method for
	 * {@link csaferefactor.core.CheckBehaviorChange#execute()}.
	 * 
	 */
	@Test
	public void testBaseCase() {
		String classToTest = "test_data/base_case_source/methodsToTest.txt";
		String path2Source = "test_data/base_case_source";
		String path2Target = "test_data/base_case_target";
		setAnalysis(classToTest, path2Source, path2Target);

		Report report = null;
		try {
			report = checkBehaviorChanges.execute();
			Assert.assertNotNull(report);
			Assert.assertFalse(report.isRefactoring());

		} catch (NotBoundException e) {
			e.printStackTrace();
			fail("Shouldn't caught exception");
		}
	}

	/**
	 * Sets analysis by path and class under test
	 * 
	 * @param classToTest
	 *            The class to check behavioral changes
	 * @param path2Source
	 *            Path to base project version
	 * @param path2Target
	 *            Path to the new project version
	 * @throws MalformedURLException
	 * @throws FileNotFoundException
	 */
	private void setAnalysis(String classToTest, String path2Source,
			String path2Target) {

		Project targetProject = null, sourceProject = null;
		try {
			targetProject = Util.createProjectFromPath(path2Target);
			sourceProject = Util.createProjectFromPath(path2Source);
		} catch (Exception e) {
			e.printStackTrace();
			fail("Shouldn't caught exception");
		}
		checkBehaviorChanges = new CheckBehaviorChange(sourceProject,
				targetProject, classToTest);
	}

}
