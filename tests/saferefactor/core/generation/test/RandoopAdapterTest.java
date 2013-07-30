package saferefactor.core.generation.test;

import static org.junit.Assert.*;


import java.io.File;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.Test;

import saferefactor.core.generation.AbstractTestGeneratorAdapter;
import saferefactor.core.generation.RandoopAdapter;
import saferefactor.core.generation.RandoopAntAdapter;
import saferefactor.core.util.Constants;
import saferefactor.core.util.Project;
import saferefactor.core.util.ast.Method;
import saferefactor.core.util.ast.ConstructorImp;
import saferefactor.core.util.ast.MethodImp;

public class RandoopAdapterTest {

	@Test
	public void testGenerateTestsForMethodList() throws FileNotFoundException {

		Project projectToTest = new Project();
		projectToTest.setProjectFolder(new File("./test_data/subject14source"));
		projectToTest.setBuildFolder(new File("./test_data/subject14source/bin"));
		projectToTest.setSrcFolder(new File("./test_data/subject14source/src"));

		int timelimit = 1;
		String tmpFolder = System
				.getProperty("java.io.tmpdir");
		AbstractTestGeneratorAdapter randoopGen = new RandoopAntAdapter(
				projectToTest,tmpFolder);
		List<Method> methods = getMethods();
		List<String> additionalParameters = new ArrayList<String>();
		randoopGen.generateTestsForMethodList(methods, timelimit,
				additionalParameters);

		File testGenerated = new File(tmpFolder
				+ "/RandoopTest0.java");

		assertTrue(testGenerated.exists());
	}
	


	

	private List<Method> getMethods() {

		List<Method> result = new ArrayList<Method>();
		ConstructorImp aCons = new ConstructorImp();
		aCons.setSimpleName("A");
		aCons.setParameterList(new ArrayList<String>());
		ConstructorImp bCons = new ConstructorImp();
		bCons.setSimpleName("B");
		bCons.setParameterList(new ArrayList<String>());
		ConstructorImp cCons = new ConstructorImp();
		cCons.setSimpleName("C");
		cCons.setParameterList(new ArrayList<String>());

		Method method1 = new MethodImp();
		HashSet<String> method1AllowedClasses = new HashSet<String>();
		method1AllowedClasses.add("A");
		method1.setAllowedClasses(method1AllowedClasses);
		method1.setDeclaringClass("A");
		method1.setSimpleName("k");
		method1.setParameterList(new ArrayList<String>());

		Method method2 = new MethodImp();
		HashSet<String> method2AllowedClasses = new HashSet<String>();
		method2AllowedClasses.add("B");
		method2AllowedClasses.add("C");
		method2.setAllowedClasses(method2AllowedClasses);
		method2.setDeclaringClass("B");
		method2.setSimpleName("k");
		method2.setParameterList(new ArrayList<String>());

		Method method3 = new MethodImp();
		HashSet<String> method3AllowedClasses = new HashSet<String>();
		method3AllowedClasses.add("C");
		method3.setAllowedClasses(method3AllowedClasses);
		method3.setDeclaringClass("C");
		method3.setSimpleName("test");
		method3.setParameterList(new ArrayList<String>());

		Method method4 = new MethodImp();
		HashSet<String> method4AllowedClasses = new HashSet<String>();
		method4AllowedClasses.add("C");
		method4.setAllowedClasses(method4AllowedClasses);
		method4.setDeclaringClass("B");
		method4.setSimpleName("m");
		method4.setParameterList(new ArrayList<String>());

		result.add(aCons);
		result.add(bCons);
		result.add(cCons);
		result.add(method1);
		result.add(method2);
		result.add(method3);
		result.add(method4);

		return result;
	}
}
