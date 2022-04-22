package com.ugav.algo.test;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.function.Predicate;

import com.ugav.algo.Pair;

class TestUtils {

	TestUtils() {
		throw new InternalError();
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

	static String getTestFullname() {
		StackTraceElement e = getTestStackElement();
		String methodName = e.getMethodName();
		String classname = e.getClassName();
		return classname + "." + methodName;
	}

	static String getTestPrefix() {
		String className;
		try {
			className = Class.forName(getTestStackElement().getClassName()).getSimpleName();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			className = "INVALIDCLASS";
		}
		return "[" + className + "." + getTestName() + "]";
	}

	private static final Map<String, Pair<Long, Random>> seedGenerators = new HashMap<>();

	static void initTestRand(long seed) {
		initTestRand(getTestFullname(), seed);
	}

	static void initTestRand(String testName) {
		initTestRand(testName, new Random().nextLong());
	}

	static void initTestRand(String testName, long seed) {
		seedGenerators.put(testName, Pair.valueOf(Long.valueOf(seed), null));
	}

	static void finalizeTestRand(String testName) {
		seedGenerators.remove(testName);
	}

	static boolean isTestRandUsed(String testName) {
		return seedGenerators.get(testName).e2 != null;
	}

	static long getTestRandBaseSeed(String testName) {
		return seedGenerators.get(testName).e1.longValue();
	}

	static class Phase {
		private final int repeat;
		private final int[] args;

		private Phase(int repeat, int[] args) {
			if (repeat < 0)
				throw new IllegalArgumentException();
			this.repeat = repeat;
			this.args = args;
		}

		static Phase of(int repeat, int... args) {
			return new Phase(repeat, args);
		}
	}

	static Phase phase(int repeat, int... args) {
		return Phase.of(repeat, args);
	}

	static boolean runTestMultiple(Collection<Phase> phases, Predicate<int[]> test) {
		int phaseIdx = 0;
		for (Phase phase : phases) {
			for (int iter = 0; iter < phase.repeat; iter++) {
				boolean passed;
				try {
					passed = test.test(phase.args);
				} catch (Throwable e) {
					e.printStackTrace();
					passed = false;
				}
				if (!passed) {
					printTestStr("Failed at phase " + phaseIdx + " iter " + iter + "\n");
					return false;
				}
			}
		}
		return true;
	}

	static long nextRandSeed() {
		Pair<Long, Random> generator = seedGenerators.get(getTestFullname());
		if (generator.e2 == null)
			generator.e2 = new Random(generator.e1.longValue() ^ 0x555bfc5796f83a2dL);
		return generator.e2.nextLong() ^ 0x3d61be24f3910c88L;
	}

	static boolean doubleEql(double a, double b, double precise) {
		if (a < b)
			return b - a < precise;
		if (a > b)
			return a - b < precise;
		return true;
	}

}
