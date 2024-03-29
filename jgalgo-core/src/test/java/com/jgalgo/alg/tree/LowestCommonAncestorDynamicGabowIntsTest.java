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

package com.jgalgo.alg.tree;

import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;

public class LowestCommonAncestorDynamicGabowIntsTest extends TestBase {

	@Test
	public void testFullBinaryTreesRandOps() {
		final long seed = 0xe7af3a14bd2eb884L;
		LowestCommonAncestorDynamicTestUtils.fullBinaryTreesRandOps(LowestCommonAncestorDynamicGabowInts::new, seed);
	}

	@Test
	public void testRandTrees() {
		final long seed = 0xae1b6bdbf6cd69dcL;
		LowestCommonAncestorDynamicTestUtils.randTrees(LowestCommonAncestorDynamicGabowInts::new, seed);
	}

}
