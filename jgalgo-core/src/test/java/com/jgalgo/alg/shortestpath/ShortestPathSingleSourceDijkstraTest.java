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

package com.jgalgo.alg.shortestpath;

import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.TestBase;

public class ShortestPathSingleSourceDijkstraTest extends TestBase {

	@Test
	public void testRandGraphDirectedPositive() {
		final long seed = 0xb387c17b735d1f85L;
		ShortestPathSingleSourceTestUtils.testSsspPositive(new ShortestPathSingleSourceDijkstra(), true, seed);
	}

	@Test
	public void testSsspUndirectedPositive() {
		final long seed = 0x67693af00925a538L;
		ShortestPathSingleSourceTestUtils.testSsspPositive(new ShortestPathSingleSourceDijkstra(), false, seed);
	}

	@Test
	public void testRandGraphDirectedPositiveInt() {
		final long seed = 0x4c6096c679a03079L;
		ShortestPathSingleSourceTestUtils.testSsspDirectedPositiveInt(new ShortestPathSingleSourceDijkstra(), seed);
	}

	@Test
	public void testSsspUndirectedPositiveInt() {
		final long seed = 0x97997bc1c8243730L;
		ShortestPathSingleSourceTestUtils.testSsspUndirectedPositiveInt(new ShortestPathSingleSourceDijkstra(), seed);
	}

	@Test
	public void testSsspUndirectedCardinality() {
		final long seed = 0x72e22f78446fa4f2L;
		ShortestPathSingleSourceTestUtils.testSsspCardinality(new ShortestPathSingleSourceDijkstra(), false, seed);
	}

	@Test
	public void testSsspDirectedCardinality() {
		final long seed = 0x1dbbeb00978a3c46L;
		ShortestPathSingleSourceTestUtils.testSsspCardinality(new ShortestPathSingleSourceDijkstra(), true, seed);
	}

	@SuppressWarnings("boxing")
	@Test
	public void negativeWeights() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addEdge(0, 1);
		g.addEdge(1, 2);
		g.addEdge(2, 0);

		ShortestPathSingleSource algo = new ShortestPathSingleSourceDijkstra();
		assertThrows(IllegalArgumentException.class, () -> algo.computeShortestPaths(g, e -> -1, 0));
	}

}
