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
package com.jgalgo.alg;

import org.junit.jupiter.api.Test;

public class ShortestPathSTBidirectionalDijkstraTest {

	private static ShortestPathSingleSource sssp() {
		return ShortestPathSTTestUtils.ssspFromSpst(new ShortestPathSTBidirectionalDijkstra());
	}

	@Test
	public void testRandGraphDirectedPositive() {
		final long seed = 0x6cc179f14ce846ebL;
		ShortestPathSingleSourceTestUtils.testSSSPPositive(sssp(), true, seed);
	}

	@Test
	public void testSSSPUndirectedPositive() {
		final long seed = 0xc1b8a406eeebf0b3L;
		ShortestPathSingleSourceTestUtils.testSSSPPositive(sssp(), false, seed);
	}

	@Test
	public void testRandGraphDirectedPositiveInt() {
		final long seed = 0xa8f5fd9715bf8077L;
		ShortestPathSingleSourceTestUtils.testSSSPDirectedPositiveInt(sssp(), seed);
	}

	@Test
	public void testSSSPUndirectedPositiveInt() {
		final long seed = 0xbebff4437c47bf83L;
		ShortestPathSingleSourceTestUtils.testSSSPUndirectedPositiveInt(sssp(), seed);
	}

	@Test
	public void testSSSPUndirectedCardinality() {
		final long seed = 0x306bceca7951ff3bL;
		ShortestPathSingleSourceTestUtils.testSSSPCardinality(sssp(), false, seed);
	}

	@Test
	public void testSSSPDirectedCardinality() {
		final long seed = 0xf0938b03455c55aeL;
		ShortestPathSingleSourceTestUtils.testSSSPCardinality(sssp(), true, seed);
	}

}
