package com.ugav.algo.test;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Objects;

class TestObj {

    private final Method testMethod;
    private String testPrefix;

    TestObj(Method testMethod) {
	this.testMethod = Objects.requireNonNull(testMethod);
	testPrefix = null;
    }

    boolean invoke() throws Throwable {
	try {
	    return (Boolean) testMethod.invoke(null);
	} catch (InvocationTargetException e) {
	    throw e.getCause();
	} catch (IllegalAccessException | IllegalArgumentException e) {
	    System.out.println("Failed to execute test: " + testMethod.getName() + "." + testMethod.getName());
	    e.printStackTrace();
	    return false;
	}
    }

    String getTestPrefix() {
	if (testPrefix != null)
	    return testPrefix;

	String methodName = testMethod.getName();
	String classname = testMethod.getDeclaringClass().getName();
	return testPrefix = ("[" + classname + "." + methodName + "]");
    }

}
