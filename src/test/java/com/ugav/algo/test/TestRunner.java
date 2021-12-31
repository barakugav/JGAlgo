package com.ugav.algo.test;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TestRunner {

	public static void main(String[] args) {
		runTests();
	}

	private static boolean runTest(TestObj test) {
		String testName = test.getTestName();
		TestUtils.initTestRand(testName);
		long t0Test = System.currentTimeMillis();
		boolean passed;
		try {
			passed = test.invoke();
		} catch (Throwable e) {
			e.printStackTrace();
			if (TestUtils.isTestRandUsed(testName))
				System.out.println(test.getTestPrefix() + " seed used: " + TestUtils.getTestRandBaseSeed(testName));
			passed = false;
		}
		TestUtils.finalizeTestRand(testName);

		long runTime = System.currentTimeMillis() - t0Test;
		int runTimeSec = (int) (runTime / 1000);
		int runTimeCentisec = (int) (runTime / 10) % 100;
		System.out.println(String.format("[%02d:%02d]", runTimeSec, runTimeCentisec) + test.getTestPrefix() + " "
				+ (passed ? "passed" : "failure!"));
		return passed;
	}

	public static boolean runTests() {
		Collection<TestObj> tests = getTests();

		boolean totalPassed = true;
		long t0Total = System.currentTimeMillis();

		for (TestObj test : tests)
			totalPassed &= runTest(test);

		long runTime = System.currentTimeMillis() - t0Total;
		int runTimeMin = (int) (runTime / 60000);
		int runTimeSec = (int) (runTime / 1000) % 60;
		int runTimeCentisec = (int) (runTime / 10) % 100;
		System.out.println("\n" + String.format("[%02d:%02d:%02d]", runTimeMin, runTimeSec, runTimeCentisec)
				+ " Total: " + (totalPassed ? "passed." : "failure!"));

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

	private static class TestObj {

		private final Method testMethod;

		TestObj(Method testMethod) {
			if (!Modifier.isStatic(testMethod.getModifiers()))
				throw new IllegalArgumentException("Test method must be static " + getTestName(testMethod));
			this.testMethod = testMethod;
		}

		boolean invoke() throws Throwable {
			try {
				return (Boolean) testMethod.invoke(null);
			} catch (InvocationTargetException e) {
				throw e.getCause();
			} catch (RuntimeException e) {
				System.out.println("Failed to execute test: " + testMethod.getName() + "." + testMethod.getName());
				e.printStackTrace();
				return false;
			}
		}

		String getTestName() {
			return getTestName(testMethod);
		}

		String getTestPrefix() {
			return "[" + getTestName() + "]";
		}

		private static String getTestName(Method testMethod) {
			String methodName = testMethod.getName();
			String classname = testMethod.getDeclaringClass().getName();
			return classname + "." + methodName;
		}

	}

}
