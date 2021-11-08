package com.ugav.algo.test;

public class Tests {

    public static boolean runTests() {
	boolean passed = true;

	passed &= RMQTest.runTests();

	return passed;
    }

    public static void main(String[] args) {
	runTests();
    }

    public static class TestTemplate {

	private static StackTraceElement getTestStackElement() {
	    StackTraceElement[] elements = Thread.currentThread().getStackTrace();
	    StackTraceElement e = null;
	    for (int i = 1; i < elements.length; i++) {
		if (!elements[i].getClassName().equals(TestTemplate.class.getName())) {
		    e = elements[i];
		    break;
		}
	    }
	    if (e == null)
		throw new InternalError();
	    return e;

	}

	public static String getTestName() {
	    return getTestStackElement().getMethodName();
	}

	public static String getTestClassName() {
	    return getTestStackElement().getClassName();
	}

	public static String getTestPrefix() {
	    StackTraceElement e = getTestStackElement();
	    String methodName = e.getMethodName();
	    String classname = e.getClassName();
	    return "[" + classname + "][" + methodName + "]";
	}

	public static void printFailure() {
	    System.out.println(getTestPrefix() + " failure!");
	}

	public static void printPassed() {
	    System.out.println(getTestPrefix() + " passed");
	}

    }

}
