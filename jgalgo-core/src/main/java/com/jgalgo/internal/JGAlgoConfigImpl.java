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

public class JGAlgoConfigImpl {

	public static final boolean ParallelByDefault = JGAlgoConfigNonFrozen.parallelByDefault;
	public static final Object IntGraphDefaultIdBuilder = JGAlgoConfigNonFrozen.intGraphDefaultIdBuilder;
	public static final boolean AssertionsGraphsBipartitePartition =
			JGAlgoConfigNonFrozen.assertionsGraphsBipartitePartition;
	public static final boolean AssertionsGraphsPositiveWeights = JGAlgoConfigNonFrozen.assertionsGraphsPositiveWeights;
	public static final boolean AssertionsGraphsIsTree = JGAlgoConfigNonFrozen.assertionsGraphsIsTree;
	public static final boolean AssertionsGraphIdCheck = JGAlgoConfigNonFrozen.assertionsGraphIdCheck;
	public static final boolean AssertionsIterNotEmpty = JGAlgoConfigNonFrozen.assertionsIterNotEmpty;
	public static final boolean AssertionsHeapsDecreaseKeyLegal = JGAlgoConfigNonFrozen.assertionsHeapsDecreaseKeyLegal;
	public static final boolean AssertionsHeapsNotEmpty = JGAlgoConfigNonFrozen.assertionsHeapsNotEmpty;
	public static final boolean AssertionsHeapsMeldLegal = JGAlgoConfigNonFrozen.assertionsHeapsMeldLegal;

	static {
		JGAlgoConfigNonFrozen.freezeOptions();
	}

}
