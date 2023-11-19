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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightsBool;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.TestBase;

public class MatchingBipartiteHopcroftKarpTest extends TestBase {

	@Test
	public void testRandBipartiteGraphs() {
		final long seed = 0x16f0491558fa62f8L;
		MatchingBipartiteTestUtils.randBipartiteGraphs(new MatchingCardinalityBipartiteHopcroftKarp(), seed);
	}

	@SuppressWarnings("boxing")
	@Test
	public void testMinimumMatching() {
		Graph<Integer, Integer> g = Graph.newUndirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addVertex(3);
		g.addEdge(0, 1, 0);
		g.addEdge(1, 2, 1);
		g.addEdge(2, 3, 2);
		g.addEdge(0, 3, 3);

		MatchingAlgo algo = new MatchingCardinalityBipartiteHopcroftKarp();
		Matching<Integer, Integer> matching = algo.computeMinimumMatching(g, null);
		assertTrue(matching.edges().isEmpty());
	}

	@Test
	public void nonBipartiteGraph() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(0);
		g.addVertex(1);
		g.addVertex(2);
		g.addEdge(0, 1);
		g.addEdge(1, 2);
		g.addEdge(2, 0);

		MatchingAlgo algo = new MatchingCardinalityBipartiteHopcroftKarp();
		assertThrows(IllegalArgumentException.class, () -> algo.computeMaximumMatching(g, null));

		IWeightsBool partition = g.addVerticesWeights(BipartiteGraphs.VertexBiPartitionWeightKey, boolean.class);
		partition.set(0, true);
		partition.set(1, false);
		partition.set(2, true);
		assertThrows(IllegalArgumentException.class, () -> algo.computeMaximumMatching(g, null));
	}

}
