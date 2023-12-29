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

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.HashSet;
import java.util.Random;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphBuilder;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.Pair;

public class TspMetricTest extends TestBase {

	@Test
	public void testMstAppxAndMatchingAppxRandGraphs() {
		final long seed = 0x6c019c0fba54c10fL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(4).repeat(512);
		tester.addPhase().withArgs(15).repeat(64);
		tester.addPhase().withArgs(32).repeat(32);
		tester.addPhase().withArgs(59).repeat(8);
		tester.addPhase().withArgs(128).repeat(4);
		tester.addPhase().withArgs(256).repeat(3);
		tester.run(n -> {
			Pair<Graph<Integer, Integer>, WeightFunction<Integer>> p = generateMetricGraph(n, seedGen.nextSeed());
			Graph<Integer, Integer> g = p.first();
			WeightFunction<Integer> distances = p.second();
			testMstAppxAndMatchingAppxRandGraph(g, distances, seedGen.nextSeed());
		});
	}

	@SuppressWarnings("boxing")
	private static Pair<Graph<Integer, Integer>, WeightFunction<Integer>> generateMetricGraph(int n, long seed) {
		Random rand = new Random(seed);

		final int x = 0, y = 1;
		double[][] locations = new double[n][2];
		for (int u = 0; u < n; u++) {
			locations[u][x] = nextDouble(rand, 1, 100);
			locations[u][y] = nextDouble(rand, 1, 100);
		}

		GraphFactory<Integer, Integer> gFactory =
				rand.nextBoolean() ? IntGraphFactory.undirected() : GraphFactory.undirected();
		gFactory.addHint(GraphFactory.Hint.FastEdgeLookup);
		gFactory.addHint(GraphFactory.Hint.DenseGraph);
		GraphBuilder<Integer, Integer> gBuilder = gFactory.newBuilder();
		gBuilder.addVertices(range(n));
		gBuilder.ensureEdgeCapacity(n * (n - 1) / 2);
		for (int u = 0; u < n; u++)
			for (int v = u + 1; v < n; v++)
				gBuilder.addEdge(u, v, gBuilder.edges().size());
		Graph<Integer, Integer> g = rand.nextBoolean() ? gBuilder.build() : gBuilder.build().indexGraph();
		WeightFunction<Integer> distances = e -> {
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			double xd = locations[u][x] - locations[v][x];
			double yd = locations[u][y] - locations[v][y];
			return Math.sqrt(xd * xd + yd * yd);
		};
		return Pair.of(g, distances);
	}

	private static <V, E> void testMstAppxAndMatchingAppxRandGraph(Graph<V, E> g, WeightFunction<E> distances,
			long seed) {

		Path<V, E> appxMst = new TspMetricMSTAppx().computeShortestTour(g, distances);
		Path<V, E> appxMatch = new TspMetricMatchingAppx().computeShortestTour(g, distances);

		Predicate<Path<V, E>> isPathVisitAllVertices =
				path -> new HashSet<>(path.vertices()).size() == g.vertices().size();
		assertTrue(isPathVisitAllVertices.test(appxMst), "MST approximation result doesn't visit every vertex");
		assertTrue(isPathVisitAllVertices.test(appxMatch), "Matching approximation result doesn't visit every vertex");

		double mstAppxLen = distances.weightSum(appxMst.edges());
		double matchAppxLen = distances.weightSum(appxMatch.edges());

		assertTrue(mstAppxLen * 3 / 2 >= matchAppxLen && matchAppxLen * 2 > mstAppxLen,
				"Approximations factor doesn't match");
	}

	@Test
	public void emptyGraph() {
		Graph<Integer, Integer> g = IntGraph.newUndirected();
		assertNull(new TspMetricMSTAppx().computeShortestTour(g, e -> 1));
		assertNull(new TspMetricMatchingAppx().computeShortestTour(g, e -> 1));
	}

	@Test
	public void notConnectedGraphs() {
		IntGraph g = IntGraph.newUndirected();
		g.addVertex(0);
		g.addVertex(1);
		assertThrows(IllegalArgumentException.class, () -> new TspMetricMSTAppx().computeShortestTour(g, e -> 1));
		assertThrows(IllegalArgumentException.class, () -> new TspMetricMatchingAppx().computeShortestTour(g, e -> 1));
	}

}
