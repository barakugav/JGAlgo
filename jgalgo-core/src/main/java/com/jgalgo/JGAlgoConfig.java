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
package com.jgalgo;

import java.util.function.Supplier;

/**
 * A global configuration class.
 *
 * @author Barak Ugav
 */
public class JGAlgoConfig {

	private JGAlgoConfig() {}

	static boolean parallelByDefault = true;

	/**
	 * Enable/disable default parallel computations in all algorithms.
	 *
	 * @param enable if {@code true}, some algorithm will use parallel computations by default
	 */
	public static void setParallelByDefault(boolean enable) {
		parallelByDefault = enable;
	}

	/**
	 * Get a supplier of an option value.
	 * <p>
	 * Internal use only; do not use this method directly.
	 *
	 * @param  <O> the option value type
	 * @param  key the option key
	 * @return     supplier that can be used multiple times to retrieve the most updated option value
	 */
	@SuppressWarnings({ "rawtypes", "unchecked", "boxing" })
	public static <O> Supplier<O> getOption(String key) {
		switch (key) {
			case "GraphIdRandom":
				return (Supplier) (() -> GraphIdRandom);
			case "AssertionsGraphsBipartitePartition":
				return (Supplier) (() -> AssertionsGraphsBipartitePartition);
			case "AssertionsGraphsPositiveWeights":
				return (Supplier) (() -> AssertionsGraphsPositiveWeights);
			case "AssertionsGraphIdCheck":
				return (Supplier) (() -> AssertionsGraphIdCheck);
			case "AssertionsIterNotEmpty":
				return (Supplier) (() -> AssertionsIterNotEmpty);
			case "AssertionsHeapsDecreaseKeyLegal":
				return (Supplier) (() -> AssertionsHeapsDecreaseKeyLegal);
			case "AssertionsHeapsNotEmpty":
				return (Supplier) (() -> AssertionsHeapsNotEmpty);
			case "AssertionsHeapsMeldLegal":
				return (Supplier) (() -> AssertionsHeapsMeldLegal);
			default:
				return null;
		}
	}

	private static final boolean GraphIdRandom = false;

	private static final boolean AssertionsGraphsBipartitePartition = true;
	private static final boolean AssertionsGraphsPositiveWeights = true;
	private static final boolean AssertionsGraphIdCheck = true;

	private static final boolean AssertionsIterNotEmpty = true;

	private static final boolean AssertionsHeapsDecreaseKeyLegal = true;
	private static final boolean AssertionsHeapsNotEmpty = true;
	private static final boolean AssertionsHeapsMeldLegal = true;

}
