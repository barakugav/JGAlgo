package com.ugav.algo.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestRunner {

	public static void main(String[] args) {
		runTests();
	}

	public static boolean runTests() {
		Collection<TestObj> tests = getTests();

		boolean totalPassed = true;

		for (TestObj test : tests) {
			boolean passed;
			try {
				passed = test.invoke();
			} catch (Throwable e) {
				e.printStackTrace();
				passed = false;
			}
			System.out.println(test.getTestPrefix() + " " + (passed ? "passed" : "failure!"));

			totalPassed &= passed;
		}

		return totalPassed;
	}

	private static Collection<TestObj> getTests() {
		List<TestObj> tests = new ArrayList<>();
		for (Method testMethod : getTestMethods())
			tests.add(new TestObj(testMethod));
		return tests;
	}

	private static Collection<Method> getTestMethods() {
		Set<Method> testMethods = new HashSet<>();

		for (Class<?> testClass : TestList.TEST_CLASSES) {
			Collection<Method> classTests = getAnnotatedMethods(testClass, Test.class);
			for (Method classTest : classTests)
				testMethods.add(classTest);
		}

		List<Method> testMethodsSorted = new ArrayList<>(testMethods);
		testMethodsSorted.sort((m1, m2) -> {
			Class<?> c1 = m1.getDeclaringClass();
			Class<?> c2 = m2.getDeclaringClass();

			int c = c1.getName().compareTo(c2.getName());
			if (c != 0)
				return c;

			return m1.getName().compareTo(m2.getName());
		});

		return testMethodsSorted;
	}

	private static Collection<Method> getAnnotatedMethods(Class<?> testClass, Class<? extends Annotation> annotation) {
		Collection<Method> methods = new ArrayList<>();

		for (Method method : testClass.getDeclaredMethods()) {
			if (method.isAnnotationPresent(annotation)) {
				// Annotation annotInstance = method.getAnnotation(annotation);
				methods.add(method);
			}
		}

		return methods;
	}

}
