package saferefactor.core.test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.File;

import org.junit.Test;

import saferefactor.core.Parameters;
import saferefactor.core.Report;
import saferefactor.core.SafeRefactor;
import saferefactor.core.SafeRefactorImp;
import saferefactor.core.util.Project;

public class SafeRefactorTest {
	
	@Test
	public void testCheckTransformationWithProjectsNotCompiled() throws Exception {
		Project source = new Project();
		source.setProjectFolder(new File("./test_data/notCompiled_source"));
		source.setBuildFolder(new File("./test_data/notCompiled_source/bin"));
		source.setSrcFolder(new File("./test_data/notCompiled_source/src"));

		Project target = new Project();
		target.setProjectFolder(new File("./test_data/notCompiled_target"));
		target.setBuildFolder(new File("./test_data/notCompiled_target/bin"));
		target.setSrcFolder(new File("./test_data/notCompiled_target/src"));

		SafeRefactor saferefactor = new SafeRefactorImp(source, target);
		saferefactor.checkTransformation();
		Report report = saferefactor.getReport();
		assertEquals(false, report.isRefactoring());
		assertTrue(report.getChanges().length() > 0);
		
		
	}
	
	@Test
	public void testCheckTransformationWithProjectsWithCompilationErrors() throws Exception {
		Project source = new Project();
		source.setProjectFolder(new File("./test_data/compilation_error_source"));
		source.setBuildFolder(new File("./test_data/notcompilation_error_source/bin"));
		source.setSrcFolder(new File("./test_data/compilation_error_source/src"));

		Project target = new Project();
		target.setProjectFolder(new File("./test_data/compilation_error_target"));
		target.setBuildFolder(new File("./test_data/compilation_error_target/bin"));
		target.setSrcFolder(new File("./test_data/compilation_error_target/src"));

		SafeRefactor saferefactor = new SafeRefactorImp(source, target);
		saferefactor.checkTransformation();
		Report report = saferefactor.getReport();
		assertEquals(false, report.isRefactoring());
		
		
	}
	

	
	
	

	@Test
	public void testCheckTransformationWithProjectsCompiled() throws Exception {
		
		//TODO fix these feature
//		Project source = new Project();
//		source.setProjectFolder(new File("./test_data/subject14source"));
//		source.setBuildFolder(new File("./test_data/subject14source/bin"));
//		source.setSrcFolder(new File("./test_data/subject14source/src"));
//
//		Project target = new Project();
//		target.setProjectFolder(new File("./test_data/subject14target"));
//		target.setBuildFolder(new File("./test_data/subject14target/bin"));
//		target.setSrcFolder(new File("./test_data/subject14target/src"));
//
//		Parameters parameters = new Parameters();
//		parameters.setCompileProjects(false);
//		parameters.setAnalyzeChangeMethods(true);
//		parameters.setTimeLimit(1);
//		
//		SafeRefactor saferefactor = new SafeRefactorImp(source, target, parameters);
//		saferefactor.checkTransformation();
//		Report report = saferefactor.getReport();
//		
//		assertEquals(false, report.isRefactoring());
//		
//		List<Method> methods =  report.getChangedMethods();
//		Method method = methods.get(0);
//		assertEquals("test", method.getSimpleName());
		
		
	}

	@Test
	public void testCheckTransformationWithProjectsCompiledForkFalse() throws Exception {
		Project source = new Project();
		source.setProjectFolder(new File("./test_data/subject14source"));
		source.setBuildFolder(new File("./test_data/subject14source/bin"));
		source.setSrcFolder(new File("./test_data/subject14source/src"));

		Project target = new Project();
		target.setProjectFolder(new File("./test_data/subject14target"));
		target.setBuildFolder(new File("./test_data/subject14target/bin"));
		target.setSrcFolder(new File("./test_data/subject14target/src"));

		Parameters parameters = new Parameters();
		parameters.setCompileProjects(false);
		parameters.getTestGeneratorParameters().add("fork=false");
		
		SafeRefactor saferefactor = new SafeRefactorImp(source, target, parameters);
		saferefactor.checkTransformation();
		Report report = saferefactor.getReport();
		
		assertEquals(false, report.isRefactoring());
		
	}
	
	
	
	@Test
	public void testCheckTransformationWithCoverage() throws Exception {
		Project source = new Project();
		source.setProjectFolder(new File("./test_data/subject14source"));
		source.setBuildFolder(new File("./test_data/subject14source/bin"));
		source.setSrcFolder(new File("./test_data/subject14source/src"));

		Project target = new Project();
		target.setProjectFolder(new File("./test_data/subject14target"));
		target.setBuildFolder(new File("./test_data/subject14target/bin"));
		target.setSrcFolder(new File("./test_data/subject14target/src"));

		Parameters parameters = new Parameters();
		parameters.setCompileProjects(false);
		parameters.setCheckCoverage(true);
		SafeRefactor saferefactor = new SafeRefactorImp(source, target,parameters  );
		saferefactor.checkTransformation();
		Report report = saferefactor.getReport();
		assertEquals(false, report.isRefactoring());
		assertNotNull(report.getChanges());
		assertEquals(78.0, report.getCoverage().getLineRate(), 1);
		
	}

	@Test
	public void testSafeRefactorFailOnRandomSubject() throws Exception {
		Project source = new Project();
		source.setProjectFolder(new File("./test_data/randomSubjectSource"));
		source.setBuildFolder(new File("./test_data/randomSubjectSource/bin"));
		source.setSrcFolder(new File("./test_data/randomSubjectSource/src"));

		Project target = new Project();
		target.setProjectFolder(new File("./test_data/randomSubjectTarget"));
		target.setBuildFolder(new File("./test_data/randomSubjectTarget/bin"));
		target.setSrcFolder(new File("./test_data/randomSubjectTarget/src"));		


		Parameters parameters = new Parameters();
		parameters.setCompileProjects(false);
		SafeRefactor saferefactor = new SafeRefactorImp(source, target,parameters);

		saferefactor.checkTransformation();
		Report report = saferefactor.getReport();
		assertEquals(false, report.isRefactoring());
	}

	@Test
	public void testExecuteTwiceOnSourceToAvoidRandomResults() throws Exception {
		Project source = new Project();
		source.setProjectFolder(new File("./test_data/randomSubjectSource"));
		source.setBuildFolder(new File("./test_data/randomSubjectSource/bin"));
		source.setSrcFolder(new File("./test_data/randomSubjectSource/src"));

		Project target = new Project();
		target.setProjectFolder(new File("./test_data/randomSubjectTarget"));
		target.setBuildFolder(new File("./test_data/randomSubjectTarget/bin"));
		target.setSrcFolder(new File("./test_data/randomSubjectTarget/src"));		

				
		Parameters parameters = new Parameters();
		parameters.setCompileProjects(false);
		parameters.setExecuteTwiceOnSource(true);
		SafeRefactor saferefactor = new SafeRefactorImp(source, target, parameters );

		saferefactor.checkTransformation();
		Report report = saferefactor.getReport();
		assertEquals(false, report.isRefactoring());
	}

}
