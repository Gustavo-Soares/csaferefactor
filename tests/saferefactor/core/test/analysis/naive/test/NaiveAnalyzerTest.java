package saferefactor.core.test.analysis.naive.test;

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Test;

import saferefactor.core.analysis.Report;
import saferefactor.core.analysis.TransformationAnalyzer;
import saferefactor.core.analysis.naive.ASMBasedAnalyzer;
import saferefactor.core.analysis.naive.ReflectionBasedAnalyzer;
import saferefactor.core.util.Project;
import saferefactor.core.util.ast.Method;


public class NaiveAnalyzerTest {

	@Test
	public void testAnalyze() throws Exception {

		Project source = new Project();
		source.setProjectFolder(new File("test/data/subject14source"));
		source.setBuildFolder(new File("test/data/subject14source/bin"));
		source.setSrcFolder(new File("test/data/subject14source/src"));

		Project target = new Project();
		target.setProjectFolder(new File("test/data/subject14target"));
		target.setBuildFolder(new File("test/data/subject14target/bin"));
		target.setSrcFolder(new File("test/data/subject14target/src"));

		String tmpFolder = System
				.getProperty("java.io.tmpdir");
		TransformationAnalyzer analyzer = new ReflectionBasedAnalyzer(
				source, target,tmpFolder);
		Report report = analyzer.analyze();

		assertEquals(8, report.getMethodsToTest().size());
		assertEquals(
				"[cons : A.<init>(), method : A.k() : A, cons : B.<init>(), method : B.k() : B;C, cons : C.<init>(), method : C.main([Ljava.lang.String;) : C, method : C.test() : C, method : B.m() : C]",
				report.getMethodsToTest().toString());

	}
	
	@Test
	public void testAsmBasedAnalyze() throws Exception {

		Project source = new Project();
		source.setProjectFolder(new File("test/data/subject14source"));
		source.setBuildFolder(new File("test/data/subject14source/bin"));
		source.setSrcFolder(new File("test/data/subject14source/src"));

		Project target = new Project();
		target.setProjectFolder(new File("test/data/subject14target"));
		target.setBuildFolder(new File("test/data/subject14target/bin"));
		target.setSrcFolder(new File("test/data/subject14target/src"));

		String tmpFolder = System
				.getProperty("java.io.tmpdir");
		TransformationAnalyzer analyzer = new ASMBasedAnalyzer(
				source, target,tmpFolder);
		Report report = analyzer.analyze();

		assertEquals(8, report.getMethodsToTest().size());
		assertEquals(
				"[method : B.k() : B, cons : B.<init>(), method : C.test() : C, cons : A.<init>(), method : A.k() : A, method : C.main([Ljava.lang.String;) : C, cons : C.<init>(), method : B.m() : B;C]",
				report.getMethodsToTest().toString());

	}



	@Test(expected = RuntimeException.class)
	public void testAnalyzeProjectNotFound() throws Exception {

		Project source = new Project();
		source.setProjectFolder(new File("test/data/subject14source"));
		source.setBuildFolder(new File("data/subject14source/bin"));
		source.setSrcFolder(new File("data/subject14source/src"));

		Project target = new Project();
		target.setProjectFolder(new File("test/data/subject14target"));
		target.setBuildFolder(new File("test/data/subject14target/bin"));
		target.setSrcFolder(new File("test/data/subject14target/src"));

		String tmpFolder = System
				.getProperty("java.io.tmpdir");
		
		TransformationAnalyzer analyzer = new ReflectionBasedAnalyzer(
				source, target,tmpFolder);
		Report report = analyzer.analyze();
		List<Method> methodsToTest = report.getMethodsToTest();
		for (Method abstractMethod : methodsToTest) {
			System.out.println(abstractMethod);
		}

	}

}
