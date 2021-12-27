package com.ugav.algo.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import com.ugav.algo.Graph;
import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Graph.WeightFunctionInt;
import com.ugav.algo.Graphs;
import com.ugav.algo.MST;
import com.ugav.algo.MSTKruskal1956;

class MSTTestUtils {

	private MSTTestUtils() {
		throw new InternalError();
	}

	static boolean testRandGraph(MST algo) {
		int[][] phases = new int[][] { { 64, 8, 16 }, { 32, 16, 32 }, { 16, 32, 64 }, { 8, 64, 128 }, { 4, 128, 256 },
				{ 1, 1024, 4096 } };
		for (int phase = 0; phase < phases.length; phase++) {
			int repeat = phases[phase][0];
			int n = phases[phase][1];
			int m = phases[phase][2];

			for (int r = 0; r < repeat; r++)
				if (!testRandGraph(algo, n, m))
					return false;
		}
		return true;
	}

	static boolean testRandGraph(MST algo, int n, int m) {
		long seed = Utils.randSeed();
		Graph<Integer> g = GraphsTestUtils.randGraph(n, m, seed);
		GraphsTestUtils.assignRandWeightsInt(g, seed);

		WeightFunctionInt<Integer> w = Graphs.WEIGHT_INT_FUNC_DEFAULT;
		return testMST(g, w, algo);
	}

	static <E> boolean testMST(Graph<E> g, WeightFunction<E> w, MST algo) {
		RuntimeException e = null;
		try {
			if (testMST0(g, w, algo))
				return true;
		} catch (RuntimeException e1) {
			e = e1;
		}
		TestUtils.printTestStr(Graphs.formatAdjacencyMatrixWeighted(g, w) + "\n");
		if (e != null)
			throw e;
		return false;
	}

	static <E> boolean testMST0(Graph<E> g, WeightFunction<E> w, MST algo) {
		Collection<Edge<E>> mst = algo.calcMST(g, w);
		return verifyMST(g, w, mst);
	}

	private static class MSTEdgeComparator<E> implements Comparator<Edge<E>> {

		private final WeightFunction<E> w;

		MSTEdgeComparator(WeightFunction<E> w) {
			this.w = w;
		}

		@Override
		public int compare(Edge<E> e1, Edge<E> e2) {
			int u1 = e1.u(), v1 = e1.v(), u2 = e2.u(), v2 = e2.v();
			if (v1 > u1) {
				int temp = u1;
				u1 = v1;
				v1 = temp;
			}
			if (v2 > u2) {
				int temp = u2;
				u2 = v2;
				v2 = temp;
			}
			if (u1 != u2)
				return Integer.compare(u1, u2);
			if (v1 != v2)
				return Integer.compare(v1, v2);
			return Double.compare(w.weight(e1), w.weight(e2));
		}

	}

	private static <E> boolean verifyMST(Graph<E> g, WeightFunction<E> w, Collection<Edge<E>> mst) {
		/*
		 * It's hard to verify MST, we use Kruskal algorithm to verify the others, and
		 * assume its implementation is correct
		 */
		Collection<Edge<E>> expected = MSTKruskal1956.getInstance().calcMST(g, w);

		Comparator<Edge<E>> c = new MSTEdgeComparator<>(w);
		Set<Edge<E>> actualSet = new TreeSet<>(c);
		actualSet.addAll(mst);

		boolean equal = true;
		if (expected.size() != actualSet.size()) {
			TestUtils.printTestStr(
					"Expected MST with " + expected.size() + " edges, actual has " + actualSet.size() + "\n");
			equal = false;
		} else {
			for (Edge<E> e : expected) {
				if (!actualSet.contains(e)) {
					TestUtils.printTestStr("MST doesn't contains edge: " + e + "\n");
					equal = false;
				}
			}
		}
		if (!equal) {
			TestUtils.printTestStr("Expected: " + formatEdges(expected, w) + "\n");
			TestUtils.printTestStr("Actual: " + formatEdges(actualSet, w) + "\n");
		}
		return equal;
	}

	private static <E> String formatEdges(Collection<Edge<E>> edges, WeightFunction<E> w) {
		Comparator<Edge<E>> c = new MSTEdgeComparator<>(w);
		List<Edge<E>> l = new ArrayList<>(edges);
		l.sort(c);
		return l.toString();
	}

}
