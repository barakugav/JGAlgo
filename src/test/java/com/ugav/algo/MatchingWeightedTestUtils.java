package com.ugav.algo;

import java.util.List;
import java.util.function.Supplier;

import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Graph.WeightFunctionInt;
import com.ugav.algo.GraphImplTestUtils.GraphImpl;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

@SuppressWarnings("boxing")
class MatchingWeightedTestUtils extends TestUtils {

	private MatchingWeightedTestUtils() {
	}

	static void randGraphsBipartiteWeighted(Supplier<? extends MatchingWeighted> builder) {
		randGraphsBipartiteWeighted(builder, GraphImplTestUtils.GRAPH_IMPL_DEFAULT);
	}

	static void randGraphsBipartiteWeighted(Supplier<? extends MatchingWeighted> builder, GraphImpl graphImpl) {
		List<Phase> phases = List.of(phase(256, 8, 8, 8), phase(128, 16, 16, 64), phase(12, 128, 128, 128),
				phase(8, 128, 128, 512), phase(2, 1024, 1024, 1024), phase(1, 1024, 1024, 5461));
		runTestMultiple(phases, (testIter, args) -> {
			int sn = args[0];
			int tn = args[1];
			int m = args[2];

			GraphBipartite g = MatchingBipartiteTestUtils.randGraphBipartite(sn, tn, m, graphImpl);
			GraphsTestUtils.assignRandWeightsIntNeg(g);
			WeightFunctionInt w = g.edgesWeight("weight");

			MatchingWeighted algo = builder.get();
			MatchingWeighted validationAlgo = algo instanceof MatchingWeightedBipartiteSSSP
					? new MatchingWeightedBipartiteHungarianMethod()
					: new MatchingWeightedBipartiteSSSP();
			testGraphWeighted(algo, g, w, validationAlgo);
		});
	}

	static void randBipartiteGraphsWeightedPerfect(Supplier<? extends MatchingWeighted> builder) {
		List<Phase> phases = List.of(phase(256, 8, 8, 8), phase(128, 16, 16, 64), phase(12, 128, 128, 128),
				phase(8, 128, 128, 512), phase(4, 1024, 1024, 1024));
		runTestMultiple(phases, (testIter, args) -> {
			int sn = args[0];
			int tn = args[1];
			int m = args[2];

			GraphBipartite g = MatchingBipartiteTestUtils.randGraphBipartite(sn, tn, m,
					GraphImplTestUtils.GRAPH_IMPL_DEFAULT);
			int maxWeight = m < 50 ? 100 : m * 2 + 2;
			GraphsTestUtils.assignRandWeightsInt(g, -maxWeight, maxWeight / 4);
			WeightFunctionInt w = g.edgesWeight("weight");

			MatchingWeighted algo = builder.get();
			Matching validationUnweightedAlgo = new MatchingBipartiteHopcroftKarp1973();
			MatchingWeighted validationWeightedAlgo = algo instanceof MatchingWeightedBipartiteHungarianMethodTest
					? new MatchingWeightedGabow2017()
					: new MatchingWeightedBipartiteHungarianMethod();
			testGraphWeightedPerfect(algo, g, w, validationUnweightedAlgo, validationWeightedAlgo);
		});
	}

	static void randGraphsWeighted(Supplier<? extends MatchingWeighted> builder) {
		List<Phase> phases = List.of(phase(256, 8, 8, 8), phase(128, 16, 16, 64), phase(12, 128, 128, 128),
				phase(8, 128, 128, 512), phase(4, 1024, 1024, 1024), phase(2, 1024, 1024, 8192));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];

			Graph g = GraphsTestUtils.randGraph(n, m);
			GraphsTestUtils.assignRandWeightsIntNeg(g);
			WeightFunctionInt w = g.edgesWeight("weight");

			MatchingWeighted algo = builder.get();
			// have nothing other than MatchingWeightedGabow2017, at least shuffle graph
			MatchingWeighted validationAlgo = new MatchingWeightedShuffled(new MatchingWeightedGabow2017());

			testGraphWeighted(algo, g, w, validationAlgo);
		});
	}

	private static void testGraphWeighted(MatchingWeighted algo, Graph g, WeightFunctionInt w,
			MatchingWeighted validationAlgo) {
		IntCollection actual = algo.calcMaxMatching(g, w);
		MatchingUnweightedTestUtils.validateMatching(g, actual);
		double actualWeight = calcMatchingWeight(actual, w);

		IntCollection expected = validationAlgo.calcMaxMatching(g, w);
		double expectedWeight = calcMatchingWeight(expected, w);

		if (actualWeight > expectedWeight) {
			printTestStr("matching is better than validation algo found: ", actualWeight, " > ", expectedWeight, "\n");
			throw new IllegalStateException();
		}
		assertEq(expectedWeight, actualWeight, "unexpected match weight");
	}

	static void randGraphsWeightedPerfect(Supplier<? extends MatchingWeighted> builder) {
		List<Phase> phases = List.of(phase(256, 8, 8, 8), phase(128, 16, 16, 64), phase(12, 128, 128, 128),
				phase(8, 128, 128, 512), phase(4, 1024, 1024, 1024));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];

			Graph g = GraphsTestUtils.randGraph(n, m);
			int maxWeight = m < 50 ? 100 : m * 2 + 2;
			GraphsTestUtils.assignRandWeightsInt(g, -maxWeight, maxWeight / 4);
			WeightFunctionInt w = g.edgesWeight("weight");

			MatchingWeighted algo = builder.get();
			Matching validationUnweightedAlgo = new MatchingGabow1976();
			MatchingWeighted validationWeightedAlgo = new MatchingWeightedShuffled(new MatchingWeightedGabow2017());
			testGraphWeightedPerfect(algo, g, w, validationUnweightedAlgo, validationWeightedAlgo);
		});
	}

	private static void testGraphWeightedPerfect(MatchingWeighted algo, Graph g, WeightFunctionInt w,
			Matching validationUnweightedAlgo, MatchingWeighted validationWeightedAlgo) {
		IntCollection actual = algo.calcPerfectMaxMatching(g, w);
		MatchingUnweightedTestUtils.validateMatching(g, actual);
		int actualSize = actual.size();
		double actualWeight = calcMatchingWeight(actual, w);

		int expectedSize = validationUnweightedAlgo.calcMaxMatching(g).size();
		if (actualSize > expectedSize) {
			printTestStr("matching size is better than validation algo found: ", actualSize, " > ", expectedSize, "\n");
			throw new IllegalStateException();
		}
		assertEq(expectedSize, actualSize, "unexpected match size");

		double expectedWeight = calcMatchingWeight(validationWeightedAlgo.calcPerfectMaxMatching(g, w), w);
		if (actualWeight > expectedWeight) {
			printTestStr("matching weight is better than validation algo found: ", actualWeight, " > ", expectedWeight,
					"\n");
			throw new IllegalStateException();
		}
		assertEq(expectedWeight, actualWeight, "unexpected match weight");
	}

	private static double calcMatchingWeight(IntCollection matching, WeightFunction w) {
		double sum = 0;
		for (IntIterator it = matching.iterator(); it.hasNext();)
			sum += w.weight(it.nextInt());
		return sum;
	}

	private static class MatchingWeightedShuffled implements MatchingWeighted {

		private final MatchingWeighted algo;

		MatchingWeightedShuffled(MatchingWeighted algo) {
			this.algo = algo;
		}

		@Override
		public IntCollection calcMaxMatching(Graph g, WeightFunction w) {
			return calcMaxMatchingshuffled(g, w, false);
		}

		@Override
		public IntCollection calcPerfectMaxMatching(Graph g, WeightFunction w) {
			return calcMaxMatchingshuffled(g, w, true);
		}

		private IntCollection calcMaxMatchingshuffled(Graph g, WeightFunction w, boolean perfect) {
			if (g instanceof DiGraph)
				throw new IllegalArgumentException("only undirected graphs are supported");
			int n = g.verticesNum();
			int[] shuffle = randPermutation(n, nextRandSeed());

			Graph shuffledG;
			if (g instanceof GraphBipartite) {
				GraphBipartite gb = (GraphBipartite) g;
				GraphBipartite shuffledGb = new GraphBipartiteArrayUndirected();

				int[] shuffleInv = new int[n];
				for (int v = 0; v < n; v++)
					shuffleInv[shuffle[v]] = v;

				for (int v = 0; v < n; v++) {
					int newv;
					if (gb.isVertexInS(shuffleInv[v]))
						newv = gb.newVertexS();
					else
						newv = gb.newVertexT();
					if (newv != v)
						throw new IllegalStateException();
				}
				shuffledG = shuffledGb;
			} else {
				shuffledG = new GraphArrayUndirected(n);
			}

			EdgesWeight.Int edgeRef = shuffledG.newEdgeWeightInt("edgeRef");
			for (int e = 0; e < g.edgesNum(); e++) {
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				int e0 = shuffledG.addEdge(shuffle[u], shuffle[v]);
				edgeRef.set(e0, e);
			}

			WeightFunction shuffledW = e -> w.weight(edgeRef.getInt(e));

			IntCollection shuffledEdges = perfect ? algo.calcPerfectMaxMatching(shuffledG, shuffledW)
					: algo.calcMaxMatching(shuffledG, shuffledW);

			IntList unshuffledEdges = new IntArrayList(shuffledEdges.size());
			for (IntIterator it = shuffledEdges.iterator(); it.hasNext();) {
				int e = it.nextInt();
				unshuffledEdges.add(edgeRef.getInt(e));
			}
			return unshuffledEdges;
		}

	}
}
