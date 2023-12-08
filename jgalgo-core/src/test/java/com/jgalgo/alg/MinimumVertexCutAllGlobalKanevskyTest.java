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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightsDouble;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.internal.util.TestBase;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class MinimumVertexCutAllGlobalKanevskyTest extends TestBase {

	@Test
	public void randGraphs() {
		final long seed = 0x89baaec02cb39b1dL;
		testRandGraphs(new MinimumVertexCutAllGlobalKanevsky(), false, false, seed);
	}

	static void testRandGraphs(MinimumVertexCutAllGlobal algo, boolean directed, boolean weighted, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		PhasedTester tester = new PhasedTester();
		tester.addPhase().withArgs(4, 4).repeat(400);
		tester.addPhase().withArgs(8, 20).repeat(40);
		tester.addPhase().withArgs(16, 32).repeat(10);
		tester.addPhase().withArgs(64, 128).repeat(64);
		tester.addPhase().withArgs(128, 256).repeat(32);
		tester.run((n, m) -> {
			Graph<Integer, Integer> g = randGraph(n, m, directed, true, false, seedGen.nextSeed());
			g = maybeIndexGraph(g, rand);

			WeightFunction<Integer> w = null;
			if (weighted && rand.nextBoolean()) {
				WeightsInt<Integer> w0 = g.addVerticesWeights("weight", int.class);
				for (Integer v : g.vertices())
					w0.set(v, rand.nextInt(551));
				w = w0;

			} else if (weighted) {
				WeightsDouble<Integer> w0 = g.addVerticesWeights("weight", double.class);
				for (Integer v : g.vertices())
					w0.set(v, rand.nextDouble() * 5642);
				w = w0;
			}

			testMinCut(g, w, algo);
		});
	}

	private static <V, E> void testMinCut(Graph<V, E> g, WeightFunction<V> w, MinimumVertexCutAllGlobal alg) {
		List<Set<V>> minCuts = alg.allMinimumCuts(g, w);
		boolean isConnected = StronglyConnectedComponentsAlgo.newInstance().isStronglyConnected(g);
		assertEqualsBool(isConnected, !minCuts.isEmpty());
		if (minCuts.isEmpty())
			return;

		double minCutWeight = WeightFunction.weightSum(w, minCuts.get(0));
		for (Set<V> cut : minCuts)
			assertEquals(minCutWeight, WeightFunction.weightSum(w, cut), 1e-6);

		Set<Set<V>> actual = new HashSet<>(minCuts);
		assertEquals(minCuts.size(), actual.size(), "duplicate cuts: " + minCuts);

		if (g.vertices().size() <= 16) {
			Set<Set<V>> expected = new HashSet<>(computeExpectedMinCuts(g, w));
			assertEquals(expected, actual);
		}

	}

	private static <V, E> List<Set<V>> computeExpectedMinCuts(Graph<V, E> g, WeightFunction<V> w) {
		if (w == null)
			w = WeightFunction.cardinalityWeightFunction();

		final int n = g.vertices().size();
		List<V> vertices = new ArrayList<>(g.vertices());

		Set<V> cut = new ObjectOpenHashSet<>(n);
		List<ObjectDoublePair<Set<V>>> cuts = new ObjectArrayList<>();
		for (int bitmap = 0; bitmap < 1 << n; bitmap++) {
			cut.clear();
			for (int i = 0; i < n; i++)
				if ((bitmap & (1 << i)) != 0)
					cut.add(vertices.get(i));
			if (!MinimumVertexCutGlobal.isCut(g, cut))
				continue;
			double cutWeight = WeightFunction.weightSum(w, cut);
			cuts.add(ObjectDoublePair.of(new ObjectOpenHashSet<>(cut), cutWeight));
		}
		if (cuts.isEmpty())
			return List.of();
		cuts.sort((p1, p2) -> Double.compare(p1.secondDouble(), p2.secondDouble()));
		double minCutWeight = cuts.get(0).rightDouble();
		final double eps = 0.0001;
		return cuts.stream().filter(p -> p.secondDouble() <= minCutWeight + eps).map(ObjectDoublePair::first)
				.collect(Collectors.toList());
	}

	@Test
	public void testBuilderDefaultImpl() {
		MinimumVertexCutAllGlobal alg = MinimumVertexCutAllGlobal.newInstance();
		assertEquals(MinimumVertexCutAllGlobalKanevsky.class, alg.getClass());
	}

}
