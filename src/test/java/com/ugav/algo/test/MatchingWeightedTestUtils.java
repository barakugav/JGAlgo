package com.ugav.algo.test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;

import com.ugav.algo.Graph;
import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Graph.WeightFunctionInt;
import com.ugav.algo.GraphArrayUndirected;
import com.ugav.algo.GraphBipartite;
import com.ugav.algo.GraphBipartiteArrayUndirected;
import com.ugav.algo.GraphDirected;
import com.ugav.algo.Graphs;
import com.ugav.algo.Matching;
import com.ugav.algo.MatchingBipartiteHopcroftKarp1973;
import com.ugav.algo.MatchingGabow1976;
import com.ugav.algo.MatchingWeighted;
import com.ugav.algo.MatchingWeightedBipartiteHungarianMethod;
import com.ugav.algo.MatchingWeightedBipartiteSSSP;
import com.ugav.algo.MatchingWeightedGabow2017;
import com.ugav.algo.test.GraphImplTestUtils.GraphImpl;

@SuppressWarnings("boxing")
class MatchingWeightedTestUtils extends TestUtils {

	private MatchingWeightedTestUtils() {
		throw new InternalError();
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

			GraphBipartite<Integer> g = MatchingBipartiteTestUtils.randGraphBipartite(sn, tn, m, graphImpl);
			GraphsTestUtils.assignRandWeightsIntNeg(g);
			WeightFunctionInt<Integer> w = Graphs.WEIGHT_INT_FUNC_DEFAULT;

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

			GraphBipartite<Integer> g = MatchingBipartiteTestUtils.randGraphBipartite(sn, tn, m,
					GraphImplTestUtils.GRAPH_IMPL_DEFAULT);
			int maxWeight = m < 50 ? 100 : m * 2 + 2;
			GraphsTestUtils.assignRandWeightsInt(g, -maxWeight, maxWeight / 4);
			WeightFunctionInt<Integer> w = Graphs.WEIGHT_INT_FUNC_DEFAULT;

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

			Graph<Integer> g = GraphsTestUtils.randGraph(n, m);
			GraphsTestUtils.assignRandWeightsIntNeg(g);
			WeightFunctionInt<Integer> w = Graphs.WEIGHT_INT_FUNC_DEFAULT;

			MatchingWeighted algo = builder.get();
			// have nothing other than MatchingWeightedGabow2017, at least shuffle graph
			MatchingWeighted validationAlgo = new MatchingWeightedShuffled(new MatchingWeightedGabow2017());

			testGraphWeighted(algo, g, w, validationAlgo);
		});
	}

	private static <E> void testGraphWeighted(MatchingWeighted algo, Graph<E> g, WeightFunctionInt<E> w,
			MatchingWeighted validationAlgo) {
		Collection<Edge<E>> actual = algo.calcMaxMatching(g, w);
		MatchingUnweightedTestUtils.validateMatching(actual);
		double actualWeight = calcMatchingWeight(actual, w);

		Collection<Edge<E>> expected = validationAlgo.calcMaxMatching(g, w);
		double expectedWeight = calcMatchingWeight(expected, w);

		if (actualWeight > expectedWeight) {
			printTestStr("matching is better than validation algo found: ", actualWeight, " > ", expectedWeight, "\n");
			throw new InternalError();
		}
		assertEq(expectedWeight, actualWeight, "unexpected match weight");
	}

	static void randGraphsWeightedPerfect(Supplier<? extends MatchingWeighted> builder) {
		List<Phase> phases = List.of(phase(256, 8, 8, 8), phase(128, 16, 16, 64), phase(12, 128, 128, 128),
				phase(8, 128, 128, 512), phase(4, 1024, 1024, 1024));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0];
			int m = args[1];

			Graph<Integer> g = GraphsTestUtils.randGraph(n, m);
			int maxWeight = m < 50 ? 100 : m * 2 + 2;
			GraphsTestUtils.assignRandWeightsInt(g, -maxWeight, maxWeight / 4);
			WeightFunctionInt<Integer> w = Graphs.WEIGHT_INT_FUNC_DEFAULT;

			MatchingWeighted algo = builder.get();
			Matching validationUnweightedAlgo = new MatchingGabow1976();
			MatchingWeighted validationWeightedAlgo = new MatchingWeightedShuffled(new MatchingWeightedGabow2017());
			testGraphWeightedPerfect(algo, g, w, validationUnweightedAlgo, validationWeightedAlgo);
		});
	}

	private static <E> void testGraphWeightedPerfect(MatchingWeighted algo, Graph<E> g, WeightFunctionInt<E> w,
			Matching validationUnweightedAlgo, MatchingWeighted validationWeightedAlgo) {
		Collection<Edge<E>> actual = algo.calcPerfectMaxMatching(g, w);
		MatchingUnweightedTestUtils.validateMatching(actual);
		int actualSize = actual.size();
		double actualWeight = calcMatchingWeight(actual, w);

		int expectedSize = validationUnweightedAlgo.calcMaxMatching(g).size();
		if (actualSize > expectedSize) {
			printTestStr("matching size is better than validation algo found: ", actualSize, " > ", expectedSize, "\n");
			throw new InternalError();
		}
		assertEq(expectedSize, actualSize, "unexpected match size");

		double expectedWeight = calcMatchingWeight(validationWeightedAlgo.calcPerfectMaxMatching(g, w), w);
		if (actualWeight > expectedWeight) {
			printTestStr("matching weight is better than validation algo found: ", actualWeight, " > ", expectedWeight,
					"\n");
			throw new InternalError();
		}
		assertEq(expectedWeight, actualWeight, "unexpected match weight");
	}

	private static <E> double calcMatchingWeight(Collection<Edge<E>> matching, WeightFunction<E> w) {
		double sum = 0;
		for (Edge<E> e : matching)
			sum += w.weight(e);
		return sum;
	}

	private static class MatchingWeightedShuffled implements MatchingWeighted {

		private final MatchingWeighted algo;

		MatchingWeightedShuffled(MatchingWeighted algo) {
			this.algo = algo;
		}

		@Override
		public <E> Collection<Edge<E>> calcMaxMatching(Graph<E> g, WeightFunction<E> w) {
			return calcMaxMatchingshuffled(g, w, false);
		}

		@Override
		public <E> Collection<Edge<E>> calcPerfectMaxMatching(Graph<E> g, WeightFunction<E> w) {
			return calcMaxMatchingshuffled(g, w, true);
		}

		private <E> Collection<Edge<E>> calcMaxMatchingshuffled(Graph<E> g, WeightFunction<E> w, boolean perfect) {
			if (g instanceof GraphDirected<?>)
				throw new IllegalArgumentException("only undirected graphs are supported");
			int n = g.vertices();
			int[] shuffle = Utils.randPermutation(n, nextRandSeed());

			Graph<Edge<E>> shuffledG;
			if (g instanceof GraphBipartite) {
				GraphBipartite<E> gb = (GraphBipartite<E>) g;
				GraphBipartite<Edge<E>> shuffledGb = new GraphBipartiteArrayUndirected<>();

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
						throw new InternalError();
				}
				shuffledG = shuffledGb;
			} else {
				shuffledG = new GraphArrayUndirected<>(n);
			}

			g.edges().forEach(e -> shuffledG.addEdge(shuffle[e.u()], shuffle[e.v()]).setData(e));
			WeightFunction<Edge<E>> shuffledW = e -> w.weight(e.data());

			Collection<Edge<Edge<E>>> shuffledEdges = perfect ? algo.calcPerfectMaxMatching(shuffledG, shuffledW)
					: algo.calcMaxMatching(shuffledG, shuffledW);

			List<Edge<E>> unshuffledEdges = new ArrayList<>(shuffledEdges.size());
			for (Edge<Edge<E>> e : shuffledEdges)
				unshuffledEdges.add(e.data());
			return unshuffledEdges;
		}

	}

}
