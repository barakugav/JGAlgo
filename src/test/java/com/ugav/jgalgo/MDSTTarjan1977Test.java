package com.ugav.jgalgo;

import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.ugav.jgalgo.GraphImplTestUtils.GraphImpl;
import com.ugav.jgalgo.GraphsTestUtils.RandomGraphBuilder;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;

public class MDSTTarjan1977Test extends TestUtils {

	private static class MDSTUndirectedWrapper implements MST {

		private final MDST algo;

		MDSTUndirectedWrapper(MDST algo) {
			this.algo = algo;
		}

		@Override
		public IntCollection calcMST(Graph g, EdgeWeightFunc w) {
			if (g instanceof DiGraph)
				return algo.calcMST(g, w);
			int n = g.vertices().size();
			Graph dg = new GraphArrayDirected(n);
			Weights.Int edgeRef = EdgesWeights.ofInts(dg, "edgeRef", -1);
			for (int u = 0; u < n; u++) {
				for (EdgeIter eit = g.edges(u); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.v();
					edgeRef.set(dg.addEdge(u, v), e);
					edgeRef.set(dg.addEdge(v, u), e);
				}
			}
			IntCollection mst0 = algo.calcMST(dg, e -> w.weight(edgeRef.getInt(e)));
			IntCollection mst = new IntArrayList(mst0.size());
			for (IntIterator it = mst0.iterator(); it.hasNext();)
				mst.add(edgeRef.getInt(it.nextInt()));
			return mst;
		}
	}

	@Test
	public void testRandGraphUndirected() {
		MSTTestUtils.testRandGraph(() -> new MDSTUndirectedWrapper(new MDSTTarjan1977()));
	}

	@Test
	public void testRandGraphDirected() {
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

			Graph g = new RandomGraphBuilder().n(n).m(m).directed(true).doubleEdges(false).selfEdges(false).cycles(true)
					.connected(false).graphImpl(graphImpl).build();
			GraphsTestUtils.assignRandWeightsIntPos(g);
			EdgeWeightFunc w = g.edgesWeight("weight");

			MDST algo = builder.get();
			testRandGraph(algo, g, w);
		});
	}

	private static void testRandGraph(MDST algo, Graph g, EdgeWeightFunc w) {
		@SuppressWarnings("unused")
		IntCollection mst = algo.calcMST(g, w, 0);
		// TODO verify the result
	}

}
