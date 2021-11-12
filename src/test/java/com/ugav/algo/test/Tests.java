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
		if (elements[i].getMethodName().startsWith("test_")) {
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
	    return "[" + classname + "." + methodName + "]";
	}

	public static void printTestStr(CharSequence s) {
	    String str = s.toString();
	    String prefix = getTestPrefix() + " ";

	    boolean checkRemove = !str.endsWith(prefix);
	    str = prefix + str.replaceAll("(\r\n|\n)", "$1" + prefix);
	    if (checkRemove && str.endsWith(prefix))
		str = str.substring(0, str.length() - prefix.length());

	    System.out.print(str);
	}

	public static void printTestBegin() {
	    printTestStr("Test Start\n");
	}

	public static void printTestFailure() {
	    printTestStr("failure!\n");
	}

	public static void printTestPassed() {
	    printTestStr("passed\n");
	}

    }

}
