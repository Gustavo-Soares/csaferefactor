package saferefactor.core.comparator.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import japa.parser.ParseException;

import java.io.IOException;
import java.util.Set;

import org.junit.Test;

import saferefactor.core.comparator.ComparatorImp;
import saferefactor.core.comparator.Report;
import saferefactor.core.comparator.TestComparator;
import saferefactor.core.util.ast.Method;

public class ComparatorImpTest {

	@Test
	public void test1() {
		TestComparator comparator = new ComparatorImp("./test_data/subject14source/fixedReport", "./test_data/subject14target/fixedReport");
		comparator.compare();
		Report report = comparator.getReport();
		assertFalse(report.isRefactoring());		
		assertTrue(report.getChanges().startsWith("RandoopTest0.test2 in source is SUCCESS while in target is FAILURE"));
	}
	
	@Test
	public void test2() throws ParseException, IOException {
		TestComparator comparator = new ComparatorImp("./test_data/subject14source/fixedReport", "./test_data/subject14target/fixedReport");
		String pathToTestSources = "./test_data/subject14target/fixedReport/";
		Set<Method> methods = comparator.identifyMethodsWithBehavioralChanges(pathToTestSources);
		Method methodImp = (Method) methods.toArray()[0];		
		assertTrue(methodImp.getSimpleName().equals("test"));		
	}	
	
	@Test
	public void test3() throws ParseException, IOException {
		TestComparator comparator = new ComparatorImp("./test_data/changedbehavior/tests/source", "./test_data/changedbehavior/tests/target");
		String pathToTestSources = "./test_data/changedbehavior/tests/";
		Set<Method> methods = comparator.identifyMethodsWithBehavioralChanges(pathToTestSources);
		Method methodImp = (Method) methods.toArray()[0];		
		assertTrue(methodImp.getSimpleName().equals("m"));		
	}	
}
