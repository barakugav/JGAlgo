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

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.BitSet;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;

public class TSPMetricTest extends TestBase {

	@Test
	public void testMstAppxAndMatchingAppxRandGraphs() {
		final long seed = 0x6c019c0fba54c10fL;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases =
				List.of(phase(512, 4), phase(64, 16), phase(32, 32), phase(8, 64), phase(4, 128), phase(3, 256));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			testMstAppxAndMatchingAppxRandGraph(n, seedGen.nextSeed());
		});
	}

	private static void testMstAppxAndMatchingAppxRandGraph(int n, long seed) {
		Random rand = new Random(seed);

		final int x = 0, y = 1;
		double[][] locations = new double[n][2];
		for (int u = 0; u < n; u++) {
			locations[u][x] = nextDouble(rand, 1, 100);
			locations[u][y] = nextDouble(rand, 1, 100);
		}

		Graph g = new GraphTableUndirected(n);
		Weights.Double distances = g.addEdgesWeights(g, double.class);
		for (int u = 0; u < n; u++) {
			for (int v = u + 1; v < n; v++) {
				double xd = locations[u][x] - locations[v][x];
				double yd = locations[u][y] - locations[v][y];
				int e = g.addEdge(u, v);
				distances.set(e, Math.sqrt(xd * xd + yd * yd));
			}
		}

		Path appxMst = new TSPMetricMSTAppx().computeShortestTour(g, distances);
		Path appxMatch = new TSPMetricMatchingAppx().computeShortestTour(g, distances);

		Predicate<Path> isPathVisitAllVertices = path -> {
			BitSet visited = new BitSet(n);
			for (int u : path.toVerticesList())
				visited.set(u);
			for (int u = 0; u < n; u++)
				if (!visited.get(u))
					return false;
			return true;
		};
		assertTrue(isPathVisitAllVertices.test(appxMst), "MST approximation result doesn't visit every vertex");
		assertTrue(isPathVisitAllVertices.test(appxMatch), "Matching approximation result doesn't visit every vertex");

		double mstAppxLen = appxMst.weight(distances);
		double matchAppxLen = appxMatch.weight(distances);

		assertTrue(mstAppxLen * 3 / 2 >= matchAppxLen && matchAppxLen * 2 > mstAppxLen,
				"Approximations factor doesn't match");

	}

}
