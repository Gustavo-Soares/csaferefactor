package saferefactor.core.util.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.junit.Test;

import saferefactor.core.util.AntJavaCompiler;
import saferefactor.core.util.Compiler;
import saferefactor.core.util.EclipseCompiler;
import saferefactor.core.util.Project;

public class CompilerTest {

	@Test
	public void test() throws MalformedURLException, FileNotFoundException {
		
		Project project= new Project();
		project.setProjectFolder(new File("test/data/subject14source"));
		project.setBuildFolder(new File("test/data/subject14source/bin"));
		project.setSrcFolder(new File("test/data/subject14source/src"));
						
		String tmpFolder = System
				.getProperty("java.io.tmpdir");
		Compiler compiler = new AntJavaCompiler(tmpFolder);
		compiler.setBinClasspath(project.getBuildFolder().getAbsolutePath());
		String testFolder = "test/data/subject14source/test/";
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
		compiler.compile("test/data/compileWithPackage" , "test/data/compileWithPackage");
		
		
		File test1 = new File("test/data/compileWithPackage","Package_0/ClassId_1.class");
		assertTrue(test1.exists());
		
		File test2 = new File("test/data/compileWithPackage","Package_1/ClassId_0.class");
		assertTrue(test2.exists());
		
		File test3 = new File("test/data/compileWithPackage","Package_1/ClassId_2.class");
		assertTrue(test3.exists());
		
	}
	
	
	//feature removed.
//	@Test
//	public void testCompileSpecificCompiler() throws IOException {
//		
//		Properties config = new Properties();
//		File configFile = new File("test/data/compiler.properties");
//		config.load(new FileInputStream(configFile));
//					
//		String tmpFolder = System
//				.getProperty("java.io.tmpdir");
//		Compiler compiler = new AntJavaCompiler(tmpFolder);
//		String path = config.getProperty("COMPILER_JAVA_5");
//		compiler.setCompilerPath(path);
//		boolean compile = compiler.compile("test/data/compileWithPackage" , "test/data/compileWithPackage");
//		
//		assertTrue(compile);
//		
//		
//		File test1 = new File("test/data/compileWithPackage","Package_0/ClassId_1.class");
//		assertTrue(test1.exists());
//		
//		File test2 = new File("test/data/compileWithPackage","Package_1/ClassId_0.class");
//		assertTrue(test2.exists());
//		
//		File test3 = new File("test/data/compileWithPackage","Package_1/ClassId_2.class");
//		assertTrue(test3.exists());
//		
//	}
	
	
	//feature removed
//	@Test
//	public void testCompileSpecificBuilder() throws IOException {
//		
//		Properties config = new Properties();
//		File configFile = new File("test/data/compiler.properties");
//		config.load(new FileInputStream(configFile));
//						
//		String tmpFolder = System
//				.getProperty("java.io.tmpdir");
//		Compiler compiler = new AntJavaCompiler(tmpFolder);
//		String buildPath = config.getProperty("DIFFERENT_BUILDER");
//		compiler.setBuildPath(buildPath);
//		boolean compile = compiler.compile("test/data/compileWithPackage" , "test/data/compileWithPackage");
//		
//		assertTrue(compile);
//		
//		
//		File test1 = new File("test/data/compileWithPackage","Package_0/ClassId_1.class");
//		assertTrue(test1.exists());
//		
//		File test2 = new File("test/data/compileWithPackage","Package_1/ClassId_0.class");
//		assertTrue(test2.exists());
//		
//		File test3 = new File("test/data/compileWithPackage","Package_1/ClassId_2.class");
//		assertTrue(!test3.exists());
//		
//	}
	
	@Test
	public void testEclipseCompiler() throws MalformedURLException, FileNotFoundException {
		
		Project project= new Project();
		project.setProjectFolder(new File("test/data/subject14source"));
		project.setBuildFolder(new File("test/data/subject14source/bin"));
		project.setSrcFolder(new File("test/data/subject14source/src"));
						
		Compiler compiler = new EclipseCompiler();
		compiler.setBinClasspath(project.getBuildFolder().getAbsolutePath());
		String testFolder = "test/data/subject14source/test/";
		compiler.compile(testFolder , testFolder);
		
		File test1 = new File(testFolder,"RandoopTest.class");
		assertTrue(test1.exists());
		
		File test2 = new File(testFolder,"RandoopTest0.class");
		assertTrue(test2.exists());
		
	}

}
