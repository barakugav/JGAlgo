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

import static com.jgalgo.internal.util.Range.range;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.junit.jupiter.api.Test;
import com.jgalgo.alg.common.IPath;
import com.jgalgo.alg.common.Path;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctionInt;
import com.jgalgo.internal.util.Fastutil;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;

class KShortestPathsStTestUtils extends TestBase {

	static void randGraphs(KShortestPathsSt algo, boolean directed, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(2, 1, 200).repeat(10);
		tester.addPhase().withArgs(3, 3, 200).repeat(64);
		tester.addPhase().withArgs(4, 8, 5).repeat(128);
		tester.addPhase().withArgs(16, 32, 5).repeat(128);
		tester.addPhase().withArgs(19, 39, 5).repeat(128);
		tester.addPhase().withArgs(23, 52, 5).repeat(128);
		tester.addPhase().withArgs(64, 256, 13).repeat(64);
		tester.addPhase().withArgs(512, 4096, 21).repeat(8);
		tester.addPhase().withArgs(4096, 16384, 23).repeat(1);
		tester.run((n, m, k) -> randGraph(algo, directed, n, m, k, seedGen.nextSeed()));
	}

	static void randGraph(KShortestPathsSt algo, boolean directed, int n, int m, int k, long seed) {
		Random rand = new Random(seed);
		Graph<Integer, Integer> g = GraphsTestUtils.randGraph(n, m, directed, rand.nextLong());
		g = maybeIndexGraph(g, rand);
		WeightFunctionInt<Integer> w = null;
		if (rand.nextInt(10) != 0)
			w = GraphsTestUtils.assignRandWeightsIntPos(g, rand.nextLong());
		Integer source = Graphs.randVertex(g, rand);
		Integer target = Graphs.randVertex(g, rand);

		validateKShortestPath(g, w, source, target, k, algo);
	}

	private static <V, E> void validateKShortestPath(Graph<V, E> g, WeightFunctionInt<E> w, V source, V target, int k,
			KShortestPathsSt algo) {
		List<Path<V, E>> pathsActual = algo.computeKShortestPaths(g, w, source, target, k);
		for (Path<V, E> p : pathsActual) {
			assertEquals(source, p.source());
			assertEquals(target, p.target());
			assertTrue(p.isSimple());
			assertTrue(Path.isPath(g, source, target, p.edges()));
			assertTrue(p.vertices().stream().distinct().count() == p.vertices().size());
		}
		if (w == null)
			w = WeightFunction.cardinalityWeightFunction();
		WeightFunctionInt<E> w0 = w;

		if ((g.isDirected() && g.edges().size() < 55) || (!g.isDirected() && g.edges().size() < 40)) {
			Iterator<Path<V, E>> simplePathsIter =
					SimplePathsEnumerator.newInstance().simplePathsIter(g, source, target);
			List<Path<V, E>> pathsExpected = StreamSupport
					.stream(Spliterators.spliteratorUnknownSize(simplePathsIter, Spliterator.ORDERED), false)
					.map(p -> ObjectDoublePair.of(p, w0.weightSum(p.edges())))
					.sorted((p1, p2) -> Double.compare(p1.secondDouble(), p2.secondDouble()))
					.limit(k)
					.map(ObjectDoublePair::first)
					.collect(Collectors.toList());

			assertEquals(pathsExpected.size(), pathsActual.size());
			for (int i : range(pathsExpected.size()))
				assertEquals(w.weightSum(pathsExpected.get(i).edges()), w.weightSum(pathsActual.get(i).edges()));
		}
	}

	@SuppressWarnings("boxing")
	static void invalidArgsTest(KShortestPathsSt algo, boolean directed) {
		foreachBoolConfig((intGraph, indexGraph) -> {
			GraphFactory<Integer, Integer> factory =
					intGraph ? IntGraphFactory.newInstance(directed) : GraphFactory.newInstance(directed);
			Graph<Integer, Integer> g0 = factory.newGraph();
			Graph<Integer, Integer> g = indexGraph ? g0.indexGraph() : g0;
			g.addVertices(range(10));
			g.addEdge(0, 1, 0);
			/* invalid source/target */
			assertThrows(IllegalArgumentException.class, () -> algo.computeKShortestPaths(g, null, 0, 100, 1));
			assertThrows(IllegalArgumentException.class, () -> algo.computeKShortestPaths(g, null, 100, 1, 1));
			/* invalid k */
			assertThrows(IllegalArgumentException.class, () -> algo.computeKShortestPaths(g, null, 0, 1, -5));
			assertThrows(IllegalArgumentException.class, () -> algo.computeKShortestPaths(g, null, 0, 1, 0));
			/* negative weights */
			assertThrows(IllegalArgumentException.class, () -> algo.computeKShortestPaths(g, e -> -1, 0, 1, 1));
		});
	}

	@Test
	public void testDefaultImpl() {
		KShortestPathsSt algo = KShortestPathsSt.newInstance();
		foreachBoolConfig(directed -> {
			IntGraph g = IntGraphFactory.newInstance(directed).newGraph();
			g.addVertices(range(2));
			g.addEdge(0, 1, 17);
			@SuppressWarnings("boxing")
			List<Path<Integer, Integer>> paths = algo.computeKShortestPaths(g, null, 0, 1, 1);
			assertEquals(List.of(IPath.valueOf(g, 0, 1, Fastutil.list(17))), paths);
		});
	}

}
