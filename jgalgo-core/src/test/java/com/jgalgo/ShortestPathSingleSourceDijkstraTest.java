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

import org.junit.jupiter.api.Test;
import com.jgalgo.internal.util.TestBase;

public class ShortestPathSingleSourceDijkstraTest extends TestBase {

	@Test
	public void testRandGraphDirectedPositive() {
		final long seed = 0xb387c17b735d1f85L;
		ShortestPathSingleSourceTestUtils.testSSSPPositive(new ShortestPathSingleSourceDijkstra(), true, seed);
	}

	@Test
	public void testSSSPUndirectedPositive() {
		final long seed = 0x67693af00925a538L;
		ShortestPathSingleSourceTestUtils.testSSSPPositive(new ShortestPathSingleSourceDijkstra(), false, seed);
	}

	@Test
	public void testRandGraphDirectedPositiveInt() {
		final long seed = 0x4c6096c679a03079L;
		ShortestPathSingleSourceTestUtils.testSSSPDirectedPositiveInt(new ShortestPathSingleSourceDijkstra(), seed);
	}

	@Test
	public void testSSSPUndirectedPositiveInt() {
		final long seed = 0x97997bc1c8243730L;
		ShortestPathSingleSourceTestUtils.testSSSPUndirectedPositiveInt(new ShortestPathSingleSourceDijkstra(), seed);
	}

	@Test
	public void testSSSPUndirectedCardinality() {
		final long seed = 0x72e22f78446fa4f2L;
		ShortestPathSingleSourceTestUtils.testSSSPCardinality(new ShortestPathSingleSourceDijkstra(), false, seed);
	}

	@Test
	public void testSSSPDirectedCardinality() {
		final long seed = 0x1dbbeb00978a3c46L;
		ShortestPathSingleSourceTestUtils.testSSSPCardinality(new ShortestPathSingleSourceDijkstra(), true, seed);
	}

}
