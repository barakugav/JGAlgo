package com.ugav.algo.test;

import java.lang.reflect.Method;

public class TestUtils {

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

    public static void printTestFailure() {
	printTestStr("failure!\n");
    }

    public static void printTestPassed() {
	printTestStr("passed\n");
    }

}
