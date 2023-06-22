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
			default:
				return null;
		}
	}

	final static boolean GraphIdRandom = false;

}
