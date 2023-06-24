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

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.List;
import java.util.function.ToDoubleFunction;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphsTestUtils;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.Weights;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class VertexCoverBarYehudaTest extends TestBase {

	@Test
	public void testRandGraphs() {
		final VertexCover algo = new VertexCoverBarYehuda();
		final double appxFactor = 2;

		final long seed = 0x3c94d9694bd37614L;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases =
				List.of(phase(256, 8, 16), phase(64, 64, 256), phase(16, 1024, 2048), phase(2, 8096, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = GraphsTestUtils.randGraph(n, m, seedGen.nextSeed());

			RandomIntUnique rand = new RandomIntUnique(0, 163454, seedGen.nextSeed());
			Weights.Int weight = g.addVerticesWeights("weight", int.class);
			for (int e : g.vertices())
				weight.set(e, rand.next());

			testVC(g, weight, algo, appxFactor);
		});
	}

	private static void testVC(Graph g, WeightFunction.Int w, VertexCover algo, double appxFactor) {
		VertexCover.Result vc = algo.computeMinimumVertexCover(g, w);

		for (int e : g.edges()) {
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			assertTrue(vc.isInCover(u) || vc.isInCover(v), "edge is not covered: " + e);
		}

		final int n = g.vertices().size();
		if (n < 16) {

			/* check all covers */
			IntSet bestCover = null;
			IntList vertices = new IntArrayList(g.vertices());
			IntSet cover = new IntOpenHashSet(n);
			ToDoubleFunction<IntSet> coverWeight = c -> GraphsUtils.weightSum(c, w);
			coverLoop: for (int bitmap = 0; bitmap < 1 << n; bitmap++) {
				for (int i = 0; i < n; i++)
					if ((bitmap & (1 << i)) != 0)
						cover.add(vertices.getInt(i));
				for (int e : g.edges())
					if (!cover.contains(g.edgeSource(e)) && !cover.contains(g.edgeTarget(e)))
						continue coverLoop; /* don't cover all edges */
				if (bestCover == null || coverWeight.applyAsDouble(bestCover) > coverWeight.applyAsDouble(cover))
					bestCover = new IntOpenHashSet(cover);
				cover.clear();
			}

			assertNotNull(bestCover);
			assertTrue(vc.weight(w) / appxFactor <= coverWeight.applyAsDouble(bestCover));
		}
	}

}
