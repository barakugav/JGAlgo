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
package com.jgalgo.alg.path;

import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;

public class ShortestPathStBidirectionalBfsTest extends TestBase {

	private static ShortestPathSingleSource sssp() {
		return ShortestPathStTestUtils.ssspFromSpst(new ShortestPathStBidirectionalBfs());
	}

	@Test
	public void testSsspUndirectedCardinality() {
		final long seed = 0x29450d7627d0f2acL;
		ShortestPathSingleSourceTestUtils.testSsspCardinality(sssp(), false, seed);
	}

	@Test
	public void testSsspDirectedCardinality() {
		final long seed = 0x8e50e159747d137cL;
		ShortestPathSingleSourceTestUtils.testSsspCardinality(sssp(), true, seed);
	}

}
