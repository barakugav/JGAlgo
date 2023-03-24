package com.ugav.jgalgo;

import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Assertions;

import com.ugav.jgalgo.GraphImplTestUtils.GraphImpl;

import it.unimi.dsi.fastutil.ints.IntAVLTreeSet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;

class MSTTestUtils extends TestUtils {

	private MSTTestUtils() {
	}

	static void testRandGraph(Supplier<? extends MST> builder) {
		testRandGraph(builder, GraphImplTestUtils.GRAPH_IMPL_DEFAULT);
	}

	static void testRandGraph(Supplier<? extends MST> builder, GraphImpl graphImpl) {
		List<Phase> phases = List.of(phase(1, 0, 0), phase(128, 16, 32), phase(64, 64, 128), phase(32, 128, 256),
				phase(8, 1024, 4096), phase(2, 4096, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];
			MST algo = builder.get();

			Graph g = GraphsTestUtils.randGraph(n, m, graphImpl);
			GraphsTestUtils.assignRandWeightsIntPos(g);
			EdgeWeightFunc.Int w = g.edgesWeight("weight");

			IntCollection mst = algo.calcMST(g, w);
			verifyMST(g, w, mst);
		});
	}

	private static class MSTEdgeComparator implements IntComparator {

		private final Graph g;
		private final EdgeWeightFunc w;

		MSTEdgeComparator(Graph g, EdgeWeightFunc w) {
			this.g = g;
			this.w = w;
		}

		@Override
		public int compare(int e1, int e2) {
			int u1 = g.edgeSource(e1), v1 = g.edgeTarget(e1);
			int u2 = g.edgeSource(e2), v2 = g.edgeTarget(e2);
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

	private static void verifyMST(Graph g, EdgeWeightFunc w, IntCollection mst) {
		/*
		 * It's hard to verify MST, we use Kruskal algorithm to verify the others, and
		 * assume its implementation is correct
		 */
		IntCollection expected = new MSTKruskal1956().calcMST(g, w);

		IntComparator c = new MSTEdgeComparator(g, w);
		IntSet actualSet = new IntAVLTreeSet(c);
		actualSet.addAll(mst);

		Assertions.assertEquals(mst.size(), actualSet.size(), "MST contains duplications");
		Assertions.assertEquals(expected.size(), actualSet.size(), "unexpected MST size");
		for (IntIterator it = expected.iterator(); it.hasNext();) {
			int e = it.nextInt();
			Assertions.assertTrue(actualSet.contains(e), "MST doesn't contains edge: " + e);
		}
	}

}
