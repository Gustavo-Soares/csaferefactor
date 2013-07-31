package saferefactor.core.util.test;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;

import org.junit.Test;

import saferefactor.core.util.AntJavaCompiler;
import saferefactor.core.util.Compiler;
import saferefactor.core.util.EclipseCompiler;
import saferefactor.core.util.Project;

public class CompilerTest {

	@Test
	public void test() throws MalformedURLException, FileNotFoundException {
		
		Project project= new Project();
		project.setProjectFolder(new File("./test_data/subject14source"));
		project.setBuildFolder(new File("./test_data/subject14source/bin"));
		project.setSrcFolder(new File("./test_data/subject14source/src"));
						
		String tmpFolder = System
				.getProperty("java.io.tmpdir");
		Compiler compiler = new AntJavaCompiler(tmpFolder);
		compiler.setBinClasspath(project.getBuildFolder().getAbsolutePath());
		String testFolder = "./test_data/subject14source/test/";
		compiler.compile(testFolder , testFolder);
		
		File test1 = new File(testFolder,"RandoopTest.class");
		assertTrue(test1.exists());
		
		File test2 = new File(testFolder,"RandoopTest0.class");
		assertTrue(test2.exists());
		
	}
	
	@Test
	public void testCompileWithPackage() throws MalformedURLException, FileNotFoundException {
		
		String tmpFolder = System
				.getProperty("java.io.tmpdir");						
		Compiler compiler = new AntJavaCompiler(tmpFolder);
		compiler.compile("./test_data/compileWithPackage" , "./test_data/compileWithPackage");
		
		
		File test1 = new File("./test_data/compileWithPackage","Package_0/ClassId_1.class");
		assertTrue(test1.exists());
		
		File test2 = new File("./test_data/compileWithPackage","Package_1/ClassId_0.class");
		assertTrue(test2.exists());
		
		File test3 = new File("./test_data/compileWithPackage","Package_1/ClassId_2.class");
		assertTrue(test3.exists());
		
	}

	
	@Test
	public void testEclipseCompiler() throws MalformedURLException, FileNotFoundException {
		
		Project project= new Project();
		project.setProjectFolder(new File("./test_data/subject14source"));
		project.setBuildFolder(new File("./test_data/subject14source/bin"));
		project.setSrcFolder(new File("./test_data/subject14source/src"));
						
		Compiler compiler = new EclipseCompiler();
		compiler.setBinClasspath(project.getBuildFolder().getAbsolutePath());
		String testFolder = "./test_data/subject14source/test/";
		compiler.compile(testFolder , testFolder);
		
		File test1 = new File(testFolder,"RandoopTest.class");
		assertTrue(test1.exists());
		
		File test2 = new File(testFolder,"RandoopTest0.class");
		assertTrue(test2.exists());
		
	}

}
