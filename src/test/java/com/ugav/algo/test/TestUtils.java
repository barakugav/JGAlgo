package com.ugav.algo.test;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import com.ugav.algo.Pair;

@SuppressWarnings("boxing")
class TestUtils {

	TestUtils() {
		throw new InternalError();
	}

	private static String formatString(Object... msgArgs) {
		StringBuilder s = new StringBuilder();
		for (Object msgArg : msgArgs)
			s.append(msgArg);
		return s.toString();
	}

	private static class TestFail extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

	static void assertEqFp(double expected, double actual, double precise, Object... msgArgs) {
		if (!doubleEql(expected, actual, precise)) {
			printTestStr("Unexpected value: ", expected, " != ", actual, "\n");
			testFail(msgArgs);
		}
	}

	static void assertEq(Object expected, Object actual, Object... msgArgs) {
		if (!Objects.equals(expected, actual)) {
			printTestStr("Unexpected value: ", expected, " != ", actual, "\n");
			testFail(msgArgs);
		}
	}

	static void assertTrue(boolean exp, Object... msgArgs) {
		if (!exp)
			testFail(msgArgs);
	}

	static void assertFalse(boolean exp, Object... msgArgs) {
		if (exp)
			testFail(msgArgs);
	}

	static void assertNull(Object obj, Object... msgArgs) {
		if (obj != null)
			testFail(msgArgs);
	}

	static void assertNonNull(Object obj, Object... msgArgs) {
		if (obj == null)
			testFail(msgArgs);
	}

	static void testFail() {
		testFail("");
	}

	static void testFail(Object... msgArgs) {
		testFail(formatString(msgArgs));
	}

	static void testFail(String msg) {
		printTestStr("Test failed:");
		printTestStr(msg);
		throw new TestFail();
	}

	static void printTestStr(Object... args) {
		StringBuilder builder = new StringBuilder();
		for (Object arg : args)
			builder.append(arg);
		String str = builder.toString();
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
		seedGenerators.put(testName, Pair.of(Long.valueOf(seed), null));
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

	@FunctionalInterface
	static interface TestRunnable {
		public void run(TestIterIdx testIter, int[] args);
	}

	static void runTestMultiple(Collection<Phase> phases, TestRunnable test) {
		int phaseIdx = 0;
		for (Phase phase : phases) {
			for (int iter = 0; iter < phase.repeat; iter++) {
				setTestMultipleIdxAttr(phaseIdx, iter);
				try {
					test.run(getTestMultipleIdx(), phase.args);
				} catch (TestFail e) {
					throw e;
				} catch (Throwable e) {
					e.printStackTrace();
					testFail("Failed at phase ", phaseIdx, " iter ", iter, "\n");
				}
			}
			phaseIdx++;
		}
	}

	private static void setTestMultipleIdxAttr(int phase, int iter) {
		TestRunner.getInstance().getCurrentTest().setAttribute("testIterIdx", new TestIterIdx(phase, iter));
	}

	static TestIterIdx getTestMultipleIdx() {
		return TestRunner.getInstance().getCurrentTest().getAttribute("testIterIdx");
	}

	static class TestIterIdx {
		final int phase, iter;

		private TestIterIdx(int phase, int iter) {
			this.phase = phase;
			this.iter = iter;
		}

		@Override
		public String toString() {
			return "P" + phase + " I" + iter;
		}
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
