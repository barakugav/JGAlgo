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

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.util.Random;
import java.util.function.Predicate;
import org.junit.jupiter.api.Test;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.IndexGraphFactory;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.Range;
import com.jgalgo.internal.util.TestBase;

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

		IndexGraphFactory gFactory = IndexGraphFactory.newUndirected();
		gFactory.addHint(GraphFactory.Hint.FastEdgeLookup);
		gFactory.addHint(GraphFactory.Hint.DenseGraph);
		IndexGraphBuilder gBuilder = gFactory.newBuilder();
		gBuilder.expectedVerticesNum(n);
		gBuilder.expectedEdgesNum(n * (n - 1) / 2);
		for (int v = 0; v < n; v++)
			gBuilder.addVertex();
		for (int u = 0; u < n; u++)
			for (int v = u + 1; v < n; v++)
				gBuilder.addEdge(u, v);
		IndexGraph g = gBuilder.build();
		IWeightFunction distances = e -> {
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			double xd = locations[u][x] - locations[v][x];
			double yd = locations[u][y] - locations[v][y];
			return Math.sqrt(xd * xd + yd * yd);
		};

		IPath appxMst = new TspMetricMSTAppx().computeShortestTour(g, distances);
		IPath appxMatch = new TspMetricMatchingAppx().computeShortestTour(g, distances);

		Predicate<IPath> isPathVisitAllVertices = path -> {
			Bitmap visited = Bitmap.fromOnes(n, path.vertices());
			return Range.of(n).intStream().allMatch(visited::get);
		};
		assertTrue(isPathVisitAllVertices.test(appxMst), "MST approximation result doesn't visit every vertex");
		assertTrue(isPathVisitAllVertices.test(appxMatch), "Matching approximation result doesn't visit every vertex");

		double mstAppxLen = distances.weightSum(appxMst.edges());
		double matchAppxLen = distances.weightSum(appxMatch.edges());

		assertTrue(mstAppxLen * 3 / 2 >= matchAppxLen && matchAppxLen * 2 > mstAppxLen,
				"Approximations factor doesn't match");

	}

}
