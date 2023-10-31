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
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;

class KShortestPathsSTTestUtils extends TestUtils {

	static void randGraphs(KShortestPathsST algo, boolean directed, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(4, 8, 5).repeat(128);
		tester.addPhase().withArgs(16, 32, 5).repeat(128);
		tester.addPhase().withArgs(19, 39, 5).repeat(128);
		tester.addPhase().withArgs(23, 52, 5).repeat(128);
		tester.addPhase().withArgs(64, 256, 13).repeat(64);
		tester.addPhase().withArgs(512, 4096, 21).repeat(8);
		tester.addPhase().withArgs(4096, 16384, 23).repeat(1);
		tester.run((n, m, k) -> {
			IntGraph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(true)
					.selfEdges(true).cycles(true).connected(false).build();
			IWeightFunctionInt w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());
			int[] vs = g.vertices().toIntArray();
			int source = vs[rand.nextInt(vs.length)];
			int target = vs[rand.nextInt(vs.length)];

			validateKShortestPath(g, w, source, target, k, algo);
		});
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private static void validateKShortestPath(IntGraph g, IWeightFunctionInt w, int source, int target, int k,
			KShortestPathsST algo) {
		List<IPath> pathsActual = (List) algo.computeKShortestPaths(g, w, source, target, k);
		for (IPath p : pathsActual) {
			assertEquals(source, p.sourceInt());
			assertEquals(target, p.targetInt());
			assertTrue(p.isSimple());
			assertTrue(IPath.isPath(g, source, target, p.edges()));
			assertTrue(p.vertices().intStream().distinct().count() == p.vertices().size());
		}

		if ((g.isDirected() && g.edges().size() < 55) || (!g.isDirected() && g.edges().size() < 40)) {
			Iterator<IPath> simplePathsIter = SimplePathsFinder.newInstance().findAllSimplePaths(g, source, target);
			List<IPath> pathsExpected = StreamSupport
					.stream(Spliterators.spliteratorUnknownSize(simplePathsIter, Spliterator.ORDERED), false)
					.map(p -> ObjectDoublePair.of(p, w.weightSum(p.edges())))
					.sorted((p1, p2) -> Double.compare(p1.secondDouble(), p2.secondDouble())).limit(k)
					.map(ObjectDoublePair::first).collect(Collectors.toList());

			assertEquals(pathsExpected.size(), pathsActual.size());
			for (int i = 0; i < pathsExpected.size(); i++)
				assertEquals(w.weightSum(pathsExpected.get(i).edges()), w.weightSum(pathsActual.get(i).edges()));
		}
	}

}
