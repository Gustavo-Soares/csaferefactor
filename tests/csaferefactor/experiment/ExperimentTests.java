/**
 * 
 */
package csaferefactor.experiment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Assert;
import org.junit.Test;

import saferefactor.core.Report;
import saferefactor.core.SafeRefactor;
import saferefactor.core.SafeRefactorImp;
import saferefactor.core.util.Project;

/**
 * Access the <a
 * href="http://dsc.ufcg.edu.br/~spg/saferefactor/experiments.html"
 * target="new">experiment page</a> from SafeRefactor for more information
 * 
 * @author Jeanderson Candido - <a href="mailto:jeandersonbc@gmail.com"
 *         target="new">jeandersonbc@gmail.com</a>
 * 
 */
public class ExperimentTests {

	/**
	 * Subject 8: Push Down Method incorrectly handles super accesses
	 * 
	 */
	@Test
	public void testSubject8() throws Exception {
		runSafeRefactor("test_data/subject8source", "test_data/subject8target");
	}

	/**
	 * Subject 9: Renaming a class leads to undiagnosed shadowing
	 */
	@Test
	public void testSubject9() throws Exception {
		runSafeRefactor("test_data/subject9source", "test_data/subject9target");
	}

	/**
	 * Subject 10: Renaming a local variable leads to shadowing by field
	 */
	@Test
	public void testSubject10() throws Exception {
		runSafeRefactor("test_data/subject10source",
				"test_data/subject10target");
	}

	/**
	 * Subject 11: Renaming a methods leads to shadowing of statically imported
	 * method
	 */
	@Test
	public void testSubject11() {
		runSafeRefactor("test_data/subject11source",
				"test_data/subject11target");
	}

	/**
	 * Subject 12: Encapsulate field does not check for overriding problems
	 */
	@Test
	public void testSubject12() {
		runSafeRefactor("test_data/subject12source",
				"test_data/subject12target");
	}

	/**
	 * Subject 13: Extract Method performs a incorrect dataflow analysis
	 */
	@Test
	public void testSubject13() {
		runSafeRefactor("test_data/subject13source",
				"test_data/subject13target");
	}

	/**
	 * Subject 15: Push Down Method incorrectly handles field accesses
	 */
	@Test
	public void testSubject15() {
		runSafeRefactor("test_data/subject15source",
				"test_data/subject15target");
	}

	/**
	 * Runs {@link SafeRefactor}
	 * 
	 * @param path2Source
	 *            Path to the base version of a project
	 * @param path2Target
	 *            Path to the new version of a project
	 */
	public void runSafeRefactor(String path2Source, String path2Target) {
		try {
			Project source = Util.createProjectFromPath(path2Source);
			Project target = Util.createProjectFromPath(path2Target);

			SafeRefactor saferefactor = new SafeRefactorImp(source, target);
			saferefactor.checkTransformation();
			Report report = saferefactor.getReport();

			assertEquals(false, report.isRefactoring());
			assertTrue(report.getChanges().length() > 0);

		} catch (Exception e) {
			System.err.println("Source path: " + path2Source);
			System.err.println("Target path: " + path2Target);
			Assert.fail("Shouldn't caught exception");
		}

	}

}
