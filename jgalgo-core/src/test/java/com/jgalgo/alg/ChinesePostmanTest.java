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
import java.util.Set;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.WeightFunctionInt;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.internal.util.SubSets;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class ChinesePostmanTest extends TestBase {

	@Test
	public void testRandGraphs() {
		final long seed = 0xf022c4bc9a0866e6L;
		ChinesePostman algo = new ChinesePostmanImpl();
		testRandGraphs(algo, false, seed);
	}

	@Test
	public void testRandGraphsWithSelfEdges() {
		final long seed = 0x0d8252e452d6abdfL;
		ChinesePostman algo = new ChinesePostmanImpl();
		testRandGraphs(algo, true, seed);
	}

	private static void testRandGraphs(ChinesePostman algo, boolean selfEdges, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(6, 10).repeat(16);
		tester.addPhase().withArgs(8, 32).repeat(16);
		tester.addPhase().withArgs(16, 32).repeat(16);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g =
					GraphsTestUtils.randConnectedGraph(n, m, false, selfEdges, true, seedGen.nextSeed());
			WeightFunctionInt<Integer> w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			testGraph(g, w, algo);
		});
	}

	private static <V, E> void testGraph(Graph<V, E> g, WeightFunctionInt<E> w, ChinesePostman algo) {
		Path<V, E> chinesePostmanTour = algo.computeShortestEdgeVisitorCircle(g, w);

		/* Asserts all edges are traversed by the tour */
		Set<E> tourEdges = new ObjectOpenHashSet<>();
		for (E e : chinesePostmanTour.edges())
			tourEdges.add(e);
		assertEquals(g.edges(), tourEdges);

		double chinesePostmanTourWeight = w.weightSum(chinesePostmanTour.edges());
		final int m = g.edges().size();
		if (m >= 32)
			return;

		Stream<Graph<V, Integer>> graphs = SubSets.stream(g.edges()).map(duplicateEdges -> {
			GraphBuilder<V, Integer> b = GraphBuilder.undirected();
			b.addVertices(g.vertices());
			WeightsInt<Integer> bWeights = b.addEdgesWeights("weights", int.class);
			for (E e : g.edges()) {
				Integer newEdge = Integer.valueOf(b.edges().size());
				b.addEdge(g.edgeSource(e), g.edgeTarget(e), newEdge);
				bWeights.set(newEdge, w.weightInt(e));
			}

			for (E origEdge : duplicateEdges) {
				V u = g.edgeSource(origEdge);
				V v = g.edgeTarget(origEdge);
				Integer duplicatedEdge = Integer.valueOf(b.edges().size());
				b.addEdge(v, u, duplicatedEdge);
				bWeights.set(duplicatedEdge, w.weightInt(origEdge));
			}
			return b.build();
		});
		double bestWeight = graphs
				.filter(eulerianGraph -> eulerianGraph
						.vertices()
						.stream()
						.allMatch(v -> nonSelfEdgesDegree(eulerianGraph, v) % 2 == 0))
				.mapToDouble(eulerianGraph -> {
					Path<V, Integer> eulerianTour = EulerianTourAlgo.newInstance().computeEulerianTour(eulerianGraph);
					WeightsInt<Integer> bWeights = eulerianGraph.edgesWeights("weights");
					return bWeights.weightSum(eulerianTour.edges());
				})
				.min()
				.orElse(Double.POSITIVE_INFINITY);

		assertEquals(bestWeight, chinesePostmanTourWeight, 1e-6);
	}

	private static <V, E> int nonSelfEdgesDegree(Graph<V, E> g, V v) {
		int nonSelfEdgesCount = 0;
		for (EdgeIter<V, E> eit = g.outEdges(v).iterator(); eit.hasNext();) {
			eit.next();
			if (!eit.target().equals(v))
				nonSelfEdgesCount++;
		}
		return nonSelfEdgesCount;
	}

}
