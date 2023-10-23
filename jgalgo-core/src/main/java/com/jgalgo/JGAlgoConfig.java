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

import com.jgalgo.internal.JGAlgoConfigNonFrozen;

/**
 * A global configuration class.
 * <p>
 * The class allow to configure multiple general options for the JGAlgo library, such as
 * {@link #setParallelByDefault(boolean)} for example. Upon the first algorithm that reads one of these options, the
 * options values will 'freezed' as they are, and no more modifications are allowed. This is done to allow the best
 * optimization of the compiler, for example to remove code that is not needed entirely.
 *
 * @author Barak Ugav
 */
public class JGAlgoConfig {

	private JGAlgoConfig() {}

	/**
	 * Enable/disable default parallel computations in all algorithms.
	 * <p>
	 * The default value is {@code true}.
	 *
	 * @param enable if {@code true}, some algorithm will use parallel computations by default
	 */
	public static void setParallelByDefault(boolean enable) {
		JGAlgoConfigNonFrozen.setOption(null, null);
	}

}
