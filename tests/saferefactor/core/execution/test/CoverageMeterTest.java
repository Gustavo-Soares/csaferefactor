package saferefactor.core.execution.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileNotFoundException;

import org.junit.Test;

import saferefactor.core.execution.CoverageDataReader.CoverageReport;
import saferefactor.core.execution.CoverageMeter;
import saferefactor.core.util.Project;

public class CoverageMeterTest {

	@Test
	public void testCheckCoverage() throws FileNotFoundException {
		Project project = new Project();
		project.setProjectFolder(new File("./test_data/subject14target"));
		project.setBuildFolder(new File("./test_data/subject14target/bin"));
		project.setSrcFolder(new File("./test_data/subject14target/src"));
		String tests = "./test_data/subject14source/test";

		CoverageMeter meter = new CoverageMeter(project, tests);
		meter.checkTestCoverage();
		CoverageReport coverageReport = meter.getCoverageReport();
		assertEquals(78.0, coverageReport.getLineRate(), 1);
		assertEquals(100.0, coverageReport.getClassRate(), 1);
		assertEquals(88.0, coverageReport.getMethodRate(), 1);
		assertEquals(76.0, coverageReport.getBlockRate(), 1);
		
		File testCoverage = new File(tests,"coverage");
		assertTrue(testCoverage.exists());
		
		File instrumentedCode = new File(project.getProjectFolder(),"outinstr");
		assertTrue(instrumentedCode.exists());

	}

}
