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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.ints.IntList;

class ShortestPathAllPairsFloydWarshallTest extends TestBase {

	private static ShortestPathAllPairs algo() {
		return new ShortestPathAllPairsFloydWarshall();
	}

	@Test
	public void testRandGraphDirectedPositive() {
		final long seed = 0x80b8af9bfbd5e5d5L;
		ShortestPathAllPairsTestUtils.testAPSPPositive(algo(), true, true, seed);
	}

	@Test
	public void testSSSPUndirectedPositive() {
		final long seed = 0x307fc7bb8684a8b5L;
		ShortestPathAllPairsTestUtils.testAPSPPositive(algo(), false, true, seed);
	}

	@Test
	public void testRandGraphDirectedNegative() {
		final long seed = 0xd3037473c85e47b3L;
		ShortestPathAllPairsTestUtils.testAPSPDirectedNegative(algo(), true, seed);
	}

	@Test
	public void testRandGraphDirectedCardinality() {
		final long seed = 0xefc29ae984ef7a07L;
		ShortestPathAllPairsTestUtils.testAPSPCardinality(algo(), true, true, seed);
	}

	@Test
	public void testRandGraphUndirectedCardinality() {
		final long seed = 0xf301a8a350bea7c9L;
		ShortestPathAllPairsTestUtils.testAPSPCardinality(algo(), false, true, seed);
	}

	@Test
	public void testRandGraphDirectedPositiveVerticesSubset() {
		final long seed = 0xe80baf0d3f8d6c9fL;
		ShortestPathAllPairsTestUtils.testAPSPPositive(algo(), true, false, seed);
	}

	@Test
	public void testSSSPUndirectedPositiveVerticesSubset() {
		final long seed = 0xc3387b0aa27e9e2eL;
		ShortestPathAllPairsTestUtils.testAPSPPositive(algo(), false, false, seed);
	}

	@Test
	public void testRandGraphDirectedNegativeVerticesSubset() {
		final long seed = 0xf61927b74f792b85L;
		ShortestPathAllPairsTestUtils.testAPSPDirectedNegative(algo(), false, seed);
	}

	@Test
	public void testRandGraphDirectedCardinalityVerticesSubset() {
		final long seed = 0xb470820d21c06fe3L;
		ShortestPathAllPairsTestUtils.testAPSPCardinality(algo(), true, false, seed);
	}

	@Test
	public void testRandGraphUndirectedCardinalityVerticesSubset() {
		final long seed = 0xecb005fa68a74e0dL;
		ShortestPathAllPairsTestUtils.testAPSPCardinality(algo(), false, false, seed);
	}

	@Test
	public void undirectedNegativeSelfEdge() {
		IntGraph g = IntGraphFactory.newUndirected().allowSelfEdges().newGraph();
		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addEdge(1, 1, 0);

		NegativeCycleException exception =
				assertThrows(NegativeCycleException.class, () -> algo().computeAllShortestPaths(g, e -> -1));
		assertEquals(IntList.of(0), exception.cycle().edges());
	}

	@Test
	public void undirectedNegativeEdge() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addEdge(1, 2, 0);

		NegativeCycleException exception =
				assertThrows(NegativeCycleException.class, () -> algo().computeAllShortestPaths(g, e -> -1));
		assertEquals(IntList.of(0, 0), exception.cycle().edges());
	}

}
