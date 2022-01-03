package com.ugav.algo.test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import com.ugav.algo.Pair;

class TestUtils {

	private TestUtils() {
		throw new InternalError();
	}

	private static StackTraceElement getTestStackElement() {
		StackTraceElement[] elements = Thread.currentThread().getStackTrace();

		for (int i = 1; i < elements.length; i++)
			if (isTestMethod(elements[i]))
				return elements[i];

		throw new IllegalStateException();
	}

	private static boolean isTestMethod(StackTraceElement e) {
		Class<?> clazz;
		try {
			clazz = Class.forName(e.getClassName());
		} catch (ClassNotFoundException e1) {
			e1.printStackTrace();
			return false;
		}

		String methodName = e.getMethodName();
		for (Method method : clazz.getMethods())
			if (method.getName().equals(methodName))
				return method.isAnnotationPresent(Test.class);
		return false;
	}

	static String getTestName() {
		return getTestStackElement().getMethodName();
	}

	static String getTestClassName() {
		return getTestStackElement().getClassName();
	}

	static String getTestFullname() {
		StackTraceElement e = getTestStackElement();
		String methodName = e.getMethodName();
		String classname = e.getClassName();
		return classname + "." + methodName;
	}

	static String getTestPrefix() {
		return "[" + getTestFullname() + "]";
	}

	static void printTestStr(CharSequence s) {
		String str = s.toString();
		String prefix = getTestPrefix() + " ";

		boolean checkRemove = !str.endsWith(prefix);
		str = prefix + str.replaceAll("(\r\n|\n)", "$1" + prefix);
		if (checkRemove && str.endsWith(prefix))
			str = str.substring(0, str.length() - prefix.length());

		System.out.print(str);
	}

	static void printTestFailure() {
		printTestStr("failure!\n");
	}

	static void printTestPassed() {
		printTestStr("passed\n");
	}

	private static final Map<String, Pair<Long, Random>> seedGenerators = new HashMap<>();

	static void initTestRand(String testName) {
		initTestRand(testName, new Random().nextLong());
	}

	static void initTestRand(String testName, long seed) {
		seedGenerators.put(testName, Pair.valueOf(seed, null));
	}

	static void finalizeTestRand(String testName) {
		seedGenerators.remove(testName);
	}

	static boolean isTestRandUsed(String testName) {
		return seedGenerators.get(testName).e2 != null;
	}

	static long getTestRandBaseSeed(String testName) {
		return seedGenerators.get(testName).e1;

	}

	static long nextRandSeed() {
		Pair<Long, Random> generator = seedGenerators.get(getTestFullname());
		if (generator.e2 == null)
			generator.e2 = new Random(generator.e1 ^ 0x555bfc5796f83a2dL);
		return generator.e2.nextLong() ^ 0x3d61be24f3910c88L;
	}

}
