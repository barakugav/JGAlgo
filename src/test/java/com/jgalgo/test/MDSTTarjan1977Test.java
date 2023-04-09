package com.jgalgo.test;

import java.util.List;
import java.util.function.Supplier;

import org.junit.jupiter.api.Test;

import com.jgalgo.DiGraph;
import com.jgalgo.EdgeIter;
import com.jgalgo.EdgeWeightFunc;
import com.jgalgo.Graph;
import com.jgalgo.GraphArrayDirected;
import com.jgalgo.MDST;
import com.jgalgo.MDSTTarjan1977;
import com.jgalgo.MST;
import com.jgalgo.Weights;
import com.jgalgo.test.GraphImplTestUtils.GraphImpl;
import com.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;

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
			Weights.Int edgeRef = dg.addEdgesWeight("edgeRef").defVal(-1).ofInts();
			for (int u = 0; u < n; u++) {
				for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
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
		final long seed = 0x9234356819f0ea1dL;
		MSTTestUtils.testRandGraph(() -> new MDSTUndirectedWrapper(new MDSTTarjan1977()), seed);
	}

	@Test
	public void testRandGraphDirected() {
		final long seed = 0xdb81d5dd5fe0d5b3L;
		testRandGraph(MDSTTarjan1977::new, seed);
	}

	private static void testRandGraph(Supplier<? extends MDST> builder, long seed) {
		testRandGraph(builder, GraphImplTestUtils.GRAPH_IMPL_DEFAULT, seed);
	}

	static void testRandGraph(Supplier<? extends MDST> builder, GraphImpl graphImpl, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(1, 0, 0), phase(256, 6, 5), phase(128, 16, 32), phase(64, 64, 128),
				phase(32, 128, 256), phase(8, 1024, 4096), phase(2, 4096, 16384));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];

			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(true).parallelEdges(false)
					.selfEdges(false).cycles(true).connected(false).graphImpl(graphImpl).build();
			EdgeWeightFunc.Int w = GraphsTestUtils.assignRandWeightsIntPos(g, seedGen.nextSeed());

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
