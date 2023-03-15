package com.ugav.algo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Graph.WeightFunctionInt;
import com.ugav.algo.GraphImplTestUtils.GraphImpl;
import com.ugav.algo.GraphsTestUtils.RandomGraphBuilder;

public class MDSTTarjan1977Test extends TestUtils {

	private static class MDSTUndirectedWrapper implements MST {

		private final MDST algo;

		MDSTUndirectedWrapper(MDST algo) {
			this.algo = algo;
		}

		@Override
		public <E> Collection<Edge<E>> calcMST(Graph<E> g, WeightFunction<E> w) {
			if (g instanceof Graph.Directed<?>)
				return algo.calcMST(g, w);
			int n = g.vertices();
			Graph<Edge<E>> dg = new GraphArrayDirected<>(n);
			for (int u = 0; u < n; u++) {
				for (Edge<E> e : Utils.iterable(g.edges(u))) {
					dg.addEdge(e.u(), e.v()).setData(e);
					dg.addEdge(e.v(), e.u()).setData(e);
				}
			}
			Collection<Edge<Edge<E>>> mst0 = algo.calcMST(dg, e -> w.weight(e.data()));
			Collection<Edge<E>> mst = new ArrayList<>(mst0.size());
			for (Edge<Edge<E>> e : mst0)
				mst.add(e.data());
			return mst;
		}

	}

	@Test
	public static void randGraphUndirected() {
		MSTTestUtils.testRandGraph(() -> new MDSTUndirectedWrapper(new MDSTTarjan1977()));
	}

	@Test
	public static void randGraphDirected() {
		testRandGraph(MDSTTarjan1977::new);
	}

	private static void testRandGraph(Supplier<? extends MDST> builder) {
		testRandGraph(builder, GraphImplTestUtils.GRAPH_IMPL_DEFAULT);
	}

	static void testRandGraph(Supplier<? extends MDST> builder, GraphImpl graphImpl) {
		List<Phase> phases = List.of(phase(1, 0, 0), phase(256, 6, 5), phase(128, 16, 32), phase(64, 64, 128),
				phase(32, 128, 256), phase(8, 1024, 4096), phase(2, 4096, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];

			Graph<Integer> g = new RandomGraphBuilder().n(n).m(m).directed(true).doubleEdges(false).selfEdges(false)
					.cycles(true).connected(false).graphImpl(graphImpl).build();
			GraphsTestUtils.assignRandWeightsIntPos(g);

			MDST algo = builder.get();
			testRandGraph(algo, g);
		});
	}

	private static void testRandGraph(MDST algo, Graph<Integer> g) {
		WeightFunctionInt<Integer> w = Graphs.WEIGHT_INT_FUNC_DEFAULT;
		@SuppressWarnings("unused")
		Collection<Edge<Integer>> mst = algo.calcMST(g, w, 0);
		// TODO verify the result
	}

}
