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
package com.jgalgo.internal;

import java.util.concurrent.atomic.AtomicBoolean;

public class JGAlgoConfigNonFrozen {

	static boolean parallelByDefault = true;
	static boolean graphIdRandom = false;
	static boolean assertionsGraphsBipartitePartition = true;
	static boolean assertionsGraphsPositiveWeights = true;
	static boolean assertionsGraphIdCheck = true;
	static boolean assertionsIterNotEmpty = true;
	static boolean assertionsHeapsDecreaseKeyLegal = true;
	static boolean assertionsHeapsNotEmpty = true;
	static boolean assertionsHeapsMeldLegal = true;

	public static void setOption(String key, Object value) {
		if (OptionsFreezed.get())
			throw new IllegalStateException("options were already freezed");

		switch (key) {
			case "ParallelByDefault":
				parallelByDefault = ((Boolean) value).booleanValue();
				break;
			case "GraphIdRandom":
				graphIdRandom = ((Boolean) value).booleanValue();
				break;
			case "AssertionsGraphsBipartitePartition":
				assertionsGraphsBipartitePartition = ((Boolean) value).booleanValue();
				break;
			case "AssertionsGraphsPositiveWeights":
				assertionsGraphsPositiveWeights = ((Boolean) value).booleanValue();
				break;
			case "AssertionsGraphIdCheck":
				assertionsGraphIdCheck = ((Boolean) value).booleanValue();
				break;
			case "AssertionsIterNotEmpty":
				assertionsIterNotEmpty = ((Boolean) value).booleanValue();
				break;
			case "AssertionsHeapsDecreaseKeyLegal":
				assertionsHeapsDecreaseKeyLegal = ((Boolean) value).booleanValue();
				break;
			case "AssertionsHeapsNotEmpty":
				assertionsHeapsNotEmpty = ((Boolean) value).booleanValue();
				break;
			case "AssertionsHeapsMeldLegal":
				assertionsHeapsMeldLegal = ((Boolean) value).booleanValue();
				break;
			default:
				throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

	private static final AtomicBoolean OptionsFreezed = new AtomicBoolean(false);

	static void freezeOptions() {
		OptionsFreezed.set(true);
	}

}
