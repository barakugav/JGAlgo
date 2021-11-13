package com.ugav.algo.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

class TestObj {

    private final Method testMethod;
    private String testPrefix;

    TestObj(Method testMethod) {
	if (!Modifier.isStatic(testMethod.getModifiers()))
	    throw new IllegalArgumentException("Test method must be static " + getTestPrefix(testMethod));
	this.testMethod = testMethod;
	testPrefix = null;
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

    String getTestPrefix() {
	if (testPrefix != null)
	    return testPrefix;
	return testPrefix = getTestPrefix(testMethod);
    }

    private static String getTestPrefix(Method method) {
	String methodName = method.getName();
	String classname = method.getDeclaringClass().getName();
	return "[" + classname + "." + methodName + "]";
    }

}
