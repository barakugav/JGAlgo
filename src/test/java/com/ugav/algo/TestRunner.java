package com.ugav.algo;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@SuppressWarnings("boxing")
public class TestRunner {

	private TestContext currentTest;

	private TestRunner() {
	}

	private static final TestRunner INSTANCE = new TestRunner();

	static TestRunner getInstance() {
		return INSTANCE;
	}

	boolean runTests() {
		Collection<TestContext> tests = getTests();

		boolean totalPassed = true;
		long t0Total = System.currentTimeMillis();

		for (TestContext test : tests) {
			currentTest = test;
			totalPassed &= runTest();
			currentTest = null;
		}

		long runTime = System.currentTimeMillis() - t0Total;
		int runTimeMin = (int) (runTime / 60000);
		int runTimeSec = (int) (runTime / 1000) % 60;
		int runTimeCentisec = (int) (runTime / 10) % 100;
		System.out.println("\n" + String.format("[%02d:%02d:%02d]", runTimeMin, runTimeSec, runTimeCentisec)
				+ " Total: " + (totalPassed ? "passed." : "failure!"));

		return totalPassed;
	}

	private boolean runTest() {
		TestContext test = currentTest;
		String testName = test.getTestName();
		TestUtils.initTestRand(testName);
		long t0Test = System.currentTimeMillis();
		boolean passed;
		try {
			passed = test.run();
		} catch (Throwable e) {
			e.printStackTrace();
			passed = false;
		}
		if (!passed && TestUtils.isTestRandUsed(testName))
			System.out.println(test.getTestPrefix() + " Seed used: " + TestUtils.getTestRandBaseSeed(testName));
		TestUtils.finalizeTestRand(testName);

		long runTime = System.currentTimeMillis() - t0Test;
		int runTimeSec = (int) (runTime / 1000);
		int runTimeCentisec = (int) (runTime / 10) % 100;
		System.out.println(String.format("[%02d:%02d]", runTimeSec, runTimeCentisec) + test.getTestPrefix() + " "
				+ (passed ? "passed" : "failure!"));
		return passed;
	}

	TestContext getCurrentTest() {
		if (currentTest == null)
			throw new IllegalStateException();
		return currentTest;
	}

	private static Collection<TestContext> getTests() {
		Set<Method> testMethodsSet = new HashSet<>();
		for (Class<?> testClass : TestList.TEST_CLASSES)
			for (Method classTest : getAnnotatedMethods(testClass, Test.class))
				testMethodsSet.add(classTest);

		List<Method> testMethods = new ArrayList<>(testMethodsSet);
		testMethods.sort((m1, m2) -> {
			Class<?> c1 = m1.getDeclaringClass();
			Class<?> c2 = m2.getDeclaringClass();

			int c = c1.getName().compareToIgnoreCase(c2.getName());
			if (c != 0)
				return c;

			return m1.getName().compareToIgnoreCase(m2.getName());
		});

		List<TestContext> tests = new ArrayList<>();
		for (Method testMethod : testMethods)
			tests.add(new TestContext(testMethod));
		return tests;
	}

	private static Collection<Method> getAnnotatedMethods(Class<?> testClass, Class<? extends Annotation> annotation) {
		Collection<Method> methods = new ArrayList<>();
		for (Method method : testClass.getDeclaredMethods())
			if (method.isAnnotationPresent(annotation))
				methods.add(method);
		return methods;
	}

	static class TestContext {

		private final Method testMethod;
		private final Map<String, Object> userAttributes;

		TestContext(Method testMethod) {
			if (!Modifier.isStatic(testMethod.getModifiers()))
				throw new IllegalArgumentException("Test method must be static " + getTestName(testMethod));
			this.testMethod = testMethod;
			userAttributes = new HashMap<>();
		}

		private boolean run() throws Throwable {
			try {
				testMethod.invoke(null);
				return true;
			} catch (InvocationTargetException e) {
				throw e.getCause();
			} catch (RuntimeException e) {
				System.out.println("Test failed: " + testMethod.getName() + "." + testMethod.getName());
				e.printStackTrace();
			}
			return false;
		}

		String getTestName() {
			return getTestName(testMethod);
		}

		String getTestPrefix() {
			String className = testMethod.getDeclaringClass().getSimpleName();
			String methodName = testMethod.getName();
			return "[" + className + "." + methodName + "]";
		}

		void setAttribute(String key, Object value) {
			userAttributes.put(key, value);
		}

		@SuppressWarnings("unchecked")
		<V> V getAttribute(String key) {
			return (V) userAttributes.get(key);
		}

		private static String getTestName(Method testMethod) {
			String classname = testMethod.getDeclaringClass().getName();
			String methodName = testMethod.getName();
			return classname + "." + methodName;
		}

	}

	public static void main(String[] args) {
		getInstance().runTests();
	}

}
