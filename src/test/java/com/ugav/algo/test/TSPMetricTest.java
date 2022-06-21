package com.ugav.algo.test;

import java.util.List;
import java.util.Random;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

import com.ugav.algo.TSPMetricMSTAppx;
import com.ugav.algo.TSPMetricMatchingAppx;

public class TSPMetricTest extends TestUtils {

	@Test
	public static boolean mstAppxAndMatchingAppxRandGraphs() {
		List<Phase> phases = List.of(phase(512, 4), phase(64, 16), phase(32, 32), phase(16, 64), phase(8, 128),
				phase(4, 256), phase(2, 512));
		return runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			return testMstAppxAndMatchingAppxRandGraph(n);
		});
	}

	@SuppressWarnings("boxing")
	private static boolean testMstAppxAndMatchingAppxRandGraph(int n) {
		Random rand = new Random(nextRandSeed());

		final int x = 0, y = 1;
		double[][] locations = new double[n][2];
		for (int u = 0; u < n; u++) {
			locations[u][x] = rand.nextDouble(1, 100);
			locations[u][y] = rand.nextDouble(1, 100);
		}

		double[][] distances = new double[n][n];
		for (int u = 0; u < n; u++) {
			for (int v = 0; v < n; v++) {
				double xd = locations[u][x] - locations[v][x];
				double yd = locations[u][y] - locations[v][y];
				distances[u][v] = distances[v][u] = Math.sqrt(xd * xd + yd * yd);
			}
		}

		int[] appxMst = new TSPMetricMSTAppx().calcTSP(distances);
		int[] appxMatch = new TSPMetricMatchingAppx().calcTSP(distances);

		Function<int[], Boolean> isPathVisitAllVertices = path -> {
			boolean[] visited = new boolean[n];
			for (int u : path)
				visited[u] = true;
			for (int u = 0; u < n; u++)
				if (!visited[u])
					return false;
			return true;
		};
		if (!isPathVisitAllVertices.apply(appxMst)) {
			printTestStr("MST approximation result doesn't visit every vertex\n");
			return false;
		}
		if (!isPathVisitAllVertices.apply(appxMatch)) {
			printTestStr("Matching approximation result doesn't visit every vertex\n");
			return false;
		}

		ToDoubleFunction<int[]> pathLength = path -> {
			double d = 0;
			for (int i = 0; i < path.length; i++) {
				int u = path[i], v = path[(i + 1) % path.length];
				d += distances[u][v];
			}
			return d;
		};
		double mstAppxLen = pathLength.applyAsDouble(appxMst);
		double matchAppxLen = pathLength.applyAsDouble(appxMatch);

		if (mstAppxLen * 3 / 2 < matchAppxLen || matchAppxLen * 2 < mstAppxLen) {
			printTestStr("Approximations factor doesn't match\n");
			return false;
		}
		return true;
	}

}
