/**
 * 
 */
package csaferefactor.experiment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
	 * @throws Exception
	 */
	@Test
	public void testSubject8() throws Exception {
		Project source = Util.createProjectFromPath("test_data/subject8source");
		Project target = Util.createProjectFromPath("test_data/subject8target");

		SafeRefactor saferefactor = new SafeRefactorImp(source, target);
		saferefactor.checkTransformation();
		Report report = saferefactor.getReport();
		assertEquals(false, report.isRefactoring());
		assertTrue(report.getChanges().length() > 0);
	}

	/**
	 * Subject 9: Renaming a class leads to undiagnosed shadowing
	 * 
	 * @throws Exception
	 */
	@Test
	public void testSubject9() throws Exception {
		Project source = Util.createProjectFromPath("test_data/subject9source");
		Project target = Util.createProjectFromPath("test_data/subject9target");

		SafeRefactor saferefactor = new SafeRefactorImp(source, target);
		saferefactor.checkTransformation();
		Report report = saferefactor.getReport();
		assertEquals(false, report.isRefactoring());
		assertTrue(report.getChanges().length() > 0);
	}

}
