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
package com.jgalgo.bench;

import static com.jgalgo.internal.util.Range.range;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class JMHTester {

	private final List<String> includedPackages = new ArrayList<>();
	private final List<String> excludes = new ArrayList<>();

	public JMHTester includePackage(String packageName) {
		includedPackages.add(packageName);
		return this;
	}

	public JMHTester exclude(String regexp) {
		excludes.add(regexp);
		return this;
	}

	public void run() {
		Set<Class<?>> classes = new HashSet<>();

		/* includes */
		for (String packageName : includedPackages) {
			for (Class<?> clazz : getPackageClasses(packageName)) {
				List<Method> benchs = getBenchmarksMethods(clazz);
				if (benchs.isEmpty() || Modifier.isAbstract(clazz.getModifiers()))
					continue;
				classes.add(clazz);
			}
		}

		/* excludes */
		classes.removeIf(c -> excludes.stream().anyMatch(regexp -> c.getName().matches(regexp)));

		for (Class<?> clazz : classes) {
			for (Class<?> clazz2 : getClassSubClasses(clazz)) {
				List<Method> benchs = getBenchmarksMethods(clazz2);
				if (benchs.isEmpty() || Modifier.isAbstract(clazz2.getModifiers()))
					continue;
				System.out.println("Testing benchmarks in class " + clazz2.getName());
				for (Method bench : benchs)
					testBenchmark(bench);
			}
		}
	}

	private static List<Method> getBenchmarksMethods(Class<?> benchClass) {
		List<Method> benchs = new ArrayList<>();
		for (Method method : benchClass.getMethods())
			if (method.isAnnotationPresent(Benchmark.class))
				benchs.add(method);
		return benchs;
	}

	private static void testBenchmark(Method bench) {
		// System.out.println("Testing benchmark " + bench.getDeclaringClass().getName() + "." + bench.getName());
		try {
			if (!bench.isAnnotationPresent(Benchmark.class))
				throw new RuntimeException("Method " + bench.getName() + " is not a benchmark");

			Class<?> stateClass = bench.getDeclaringClass();
			Object state = stateClass.getConstructor().newInstance();

			initParamFields(state);

			/* call all setups */
			callAllFunctionsByAnnotation(state, Setup.class,
					(m1, m2) -> Integer.compare(m1.value().ordinal(), m2.value().ordinal()));

			/* call benchmark */
			bench.setAccessible(true);
			Parameter[] params = bench.getParameters();
			Object[] args = new Object[params.length];
			for (int i : range(args.length)) {
				if (params[i].getType().isAssignableFrom(Blackhole.class)) {
					args[i] = new Blackhole(
							"Today's password is swordfish. I understand instantiating Blackholes directly is dangerous.");
				} else {
					throw new RuntimeException("Unknown benchmark parameter type: " + params[i].getType());
				}
			}
			bench.invoke(state, args);

			/* call all teardowns */
			callAllFunctionsByAnnotation(state, TearDown.class,
					(m1, m2) -> Integer.compare(m1.value().ordinal(), m2.value().ordinal()));
		} catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException
				| SecurityException e) {
			throw new RuntimeException(e);
		}
	}

	private static void initParamFields(Object state) throws IllegalAccessException {
		for (Class<?> stateClass = state.getClass(); stateClass != null; stateClass = stateClass.getSuperclass()) {
			if (!stateClass.isAnnotationPresent(State.class))
				continue;
			for (Field field : stateClass.getDeclaredFields()) {
				Param param = field.getAnnotation(Param.class);
				if (param == null)
					continue;
				String val = param.value()[0];
				if (val == Param.BLANK_ARGS)
					continue;
				field.setAccessible(true);
				field.set(state, val);
			}
		}
	}

	private static <Ann extends Annotation> void callAllFunctionsByAnnotation(Object state, Class<Ann> annotation,
			Comparator<? super Ann> order) throws IllegalAccessException, InvocationTargetException {
		List<Class<?>> stateClasses = new ObjectArrayList<>();
		for (Class<?> stateClass = state.getClass(); stateClass != null; stateClass = stateClass.getSuperclass())
			stateClasses.add(stateClass);
		Collections.reverse(stateClasses);

		List<IntObjectPair<Method>> setups = new ArrayList<>();
		for (int depth : range(stateClasses.size()))
			for (Method setup : stateClasses.get(depth).getDeclaredMethods())
				if (setup.isAnnotationPresent(annotation))
					setups.add(IntObjectPair.of(depth, setup));
		setups.sort((m1, m2) -> {
			int c;
			if ((c = Integer.compare(m1.firstInt(), m2.firstInt())) != 0)
				return c;
			Ann ann1 = m1.second().getAnnotation(annotation);
			Ann ann2 = m2.second().getAnnotation(annotation);
			if ((c = order.compare(ann1, ann2)) != 0)
				return c;
			return 0;
		});
		for (IntObjectPair<Method> setupPair : setups) {
			Method setup = setupPair.second();
			setup.setAccessible(true);
			setup.invoke(state);
		}
	}

	private static Collection<Class<?>> getPackageClasses(String packageName) {
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

	private static Collection<Class<?>> getClassSubClasses(Class<?> clazz) {
		return getPackageClasses(clazz.getPackageName())
				.stream()
				.filter(c -> clazz.isAssignableFrom(c))
				.collect(Collectors.toList());
	}

	private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
		List<Class<?>> classes = new ObjectArrayList<>();
		if (!directory.exists())
			return classes;

		File[] files = directory.listFiles();
		for (File file : files) {
			if (file.isDirectory()) {
				assert !file.getName().contains(".");
				// classes.addAll(findClasses(file, packageName + "." + file.getName()));
			} else if (file.getName().endsWith(".class")) {
				classes
						.add(Class
								.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
			}
		}
		return classes;
	}

}
