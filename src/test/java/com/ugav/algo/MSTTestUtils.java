package com.ugav.algo;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Supplier;


import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Graph.WeightFunctionInt;
import com.ugav.algo.GraphImplTestUtils.GraphImpl;

@SuppressWarnings("boxing")
class MSTTestUtils extends TestUtils {

	private MSTTestUtils() {
		throw new InternalError();
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

			Graph<Integer> g = GraphsTestUtils.randGraph(n, m, graphImpl);
			GraphsTestUtils.assignRandWeightsIntPos(g);

			WeightFunctionInt<Integer> w = Graphs.WEIGHT_INT_FUNC_DEFAULT;
			Collection<Edge<Integer>> mst = algo.calcMST(g, w);
			verifyMST(g, w, mst);
		});
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

	private static <E> void verifyMST(Graph<E> g, WeightFunction<E> w, Collection<Edge<E>> mst) {
		/*
		 * It's hard to verify MST, we use Kruskal algorithm to verify the others, and
		 * assume its implementation is correct
		 */
		Collection<Edge<E>> expected = new MSTKruskal1956().calcMST(g, w);

		Comparator<Edge<E>> c = new MSTEdgeComparator<>(w);
		Set<Edge<E>> actualSet = new TreeSet<>(c);
		actualSet.addAll(mst);

		assertEq(mst.size(), actualSet.size(), "MST contains duplications\n");
		assertEq(expected.size(), actualSet.size(), "unexpected MST size");
		for (Edge<E> e : expected)
			assertTrue(actualSet.contains(e), "MST doesn't contains edge: ", e, "\n");
	}

}
