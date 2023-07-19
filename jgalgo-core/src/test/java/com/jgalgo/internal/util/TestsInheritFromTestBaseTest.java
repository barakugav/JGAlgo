/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jgalgo.internal.util;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import org.junit.jupiter.api.Test;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class TestsInheritFromTestBaseTest extends TestBase {

	private static final List<String> ExcludeList;
	static {
		List<String> excludeList = new ObjectArrayList<>();
		excludeList.add("AllExamplesTest");
		ExcludeList = Collections.unmodifiableList(excludeList);
	}

	@Test
	public void testAllTestsInheritFromTestBase() {
		String packageName = TestsInheritFromTestBaseTest.class.getPackageName();
		Collection<Class<?>> classes = getClasses(packageName);
		for (Class<?> clazz : classes) {
			if (clazz.isInterface() || !isClassContainsTests(clazz))
				continue;
			if (ExcludeList.contains(clazz.getSimpleName()))
				continue;
			assertTrue(isClassInheritFromTestBase(clazz),
					"Test class does not inherit from " + TestBase.class.getSimpleName() + ": " + clazz);
		}
	}

	private static Collection<Class<?>> getClasses(String packageName) {
		try {
			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			assert classLoader != null;
			String path = packageName.replace('.', '/');
			Enumeration<URL> resources = classLoader.getResources(path);
			List<File> dirs = new ObjectArrayList<>();
			while (resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				dirs.add(new File(resource.getFile()));
			}
			List<Class<?>> classes = new ObjectArrayList<>();
			for (File directory : dirs)
				classes.addAll(findClasses(directory, packageName));
			return classes;
		} catch (ClassNotFoundException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class<?>> classes = new ObjectArrayList<>();
		if (!directory.exists())
			return classes;

		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				classes.addAll(findClasses(file, packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				classes.add(
						Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
			}
		}
		return classes;
	}

	private static boolean isClassContainsTests(Class<?> clazz) {
		for (; clazz != Object.class; clazz = clazz.getSuperclass()) {
			for (Method method : clazz.getDeclaredMethods())
				if (method.isAnnotationPresent(Test.class))
					return true;
		}
		return false;
	}

	private static boolean isClassInheritFromTestBase(Class<?> clazz) {
		for (; clazz != Object.class; clazz = clazz.getSuperclass())
			if (TestBase.class.equals(clazz))
				return true;
		return false;
	}

}
