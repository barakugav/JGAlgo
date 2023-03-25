package com.ugav.jgalgo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.junit.jupiter.api.Assertions;

import com.ugav.jgalgo.GraphsTestUtils.RandomGraphBuilder;

import it.unimi.dsi.fastutil.ints.IntIterator;

class GraphImplTestUtils extends TestUtils {

	@FunctionalInterface
	static interface GraphImpl {
		Graph newGraph(boolean directed, int vertices);
	}

	static final GraphImpl GRAPH_IMPL_DEFAULT = new GraphImpl() {

		@Override
		public Graph newGraph(boolean directed, int vertices) {
			if (directed) {
				return new GraphArrayDirected(vertices);
			} else {
				return new GraphArrayUndirected(vertices);
			}
		}
	};

	static void testUndirectedMST(GraphImpl graphImpl) {
		MSTTestUtils.testRandGraph(MSTKruskal1956::new, graphImpl);
	}

	static void testDirectedMDST(GraphImpl graphImpl) {
		MDSTTarjan1977Test.testRandGraph(MDSTTarjan1977::new, graphImpl);
	}

	static void testDirectedMaxFlow(GraphImpl graphImpl) {
		MaxFlowTestUtils.testRandGraphs(MaxFlowEdmondsKarp::new, graphImpl);
	}

	static void testUndirectedBipartiteMatching(GraphImpl graphImpl) {
		MatchingBipartiteTestUtils.randBipartiteGraphs(MatchingGabow1976::new, graphImpl);
	}

	static void testUndirectedBipartiteMatchingWeighted(GraphImpl graphImpl) {
		MatchingWeightedTestUtils.randGraphsBipartiteWeighted(MatchingWeightedBipartiteHungarianMethod::new, graphImpl);
	}

	static void testUndirectedRandOps(GraphImpl graphImpl) {
		List<Phase> phases = List.of(phase(1024, 6, 6), phase(128, 16, 16), phase(128, 16, 32), phase(64, 64, 64),
				phase(64, 64, 128), phase(8, 512, 512), phase(4, 512, 1324), phase(1, 1025, 2016),
				phase(1, 3246, 5612));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder().n(n).m(m).directed(false).doubleEdges(false).selfEdges(false)
					.cycles(true).connected(false).graphImpl(graphImpl).build();
			final int opsNum = 128;
			testRandOps(g, opsNum);
		});
	}

	private static class RandWeighted<E> {
		private final List<Pair<E, Integer>> elms = new ArrayList<>();
		private int totalWeight;

		void add(E elm, int weight) {
			if (weight <= 0)
				throw new IllegalArgumentException();
			elms.add(Pair.of(elm, Integer.valueOf(weight)));
			totalWeight += weight;
		}

		E get(Random rand) {
			final int v = rand.nextInt(totalWeight);
			int s = 0;
			for (Pair<E, Integer> elm : elms) {
				s += elm.e2.intValue();
				if (v <= s)
					return elm.e1;
			}
			throw new IllegalStateException();
		}
	}

	private static class GraphTracker {
		private int n;
		private final List<Edge> edges = new ArrayList<>();
		private final boolean directed;
		private final Object dataKey;
		private final boolean debugPrints = false;

		GraphTracker(int n, boolean directed, Object dataKey) {
			this.n = n;
			this.directed = directed;
			this.dataKey = dataKey;
		}

		int verticesNum() {
			return n;
		}

		int edgesNum() {
			return edges.size();
		}

		@SuppressWarnings("unused")
		void newVertex() {
			if (debugPrints)
				System.out.println("newVertex()");
			n++;
		}

		void addEdge(int u, int v, Object data) {
			if (debugPrints)
				System.out.println("addEdge(" + u + ", " + v + ")");
			if (indexOfEdge(u, v) >= 0)
				throw new IllegalArgumentException("parallel edges are not allowed");
			edges.add(new Edge(u, v, data));
		}

		void removeEdge(int u, int v) {
			if (debugPrints)
				System.out.println("removeEdge(" + u + ", " + v + ")");
			int index = indexOfEdge(u, v);
			if (index < 0)
				throw new IllegalArgumentException("no edge (" + u + ", " + v + ")");
			edges.remove(index);
		}

		private int indexOfEdge(int u, int v) {
			if (directed) {
				for (int i = 0; i < edges.size(); i++) {
					Edge e = edges.get(i);
					if (e.u == u && e.v == v)
						return i;
				}
			} else {
				for (int i = 0; i < edges.size(); i++) {
					Edge e = edges.get(i);
					if ((e.u == u && e.v == v) || (e.v == u && e.u == v))
						return i;
				}
			}
			return -1;
		}

		void clearEdges() {
			if (debugPrints)
				System.out.println("clearEdges()");
			edges.clear();
		}

		boolean checkEdgesEqual(Graph g) {
			if (g.edges().size() != edgesNum())
				return false;
			Weights<Object> edgeData = g.edgesWeight(dataKey);
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				int index = indexOfEdge(u, v);
				if (index < 0)
					return false;
				Edge e0 = edges.get(index);
				Object expected = e0.data;
				Object actual = edgeData.get(e);
				if (!expected.equals(actual))
					return false;
			}
			return true;
		}

		private static class Edge {
			final int u, v;
			final Object data;

			Edge(int u, int v, Object data) {
				this.u = u;
				this.v = v;
				this.data = Objects.requireNonNull(data);
			}

			@Override
			public String toString() {
				return "(" + u + ", " + v + ")";
			}
		}
	}

	private static enum GraphOp {
		AddEdge, RemoveEdge, ClearEdges, AddVertex
	}

	private static void testRandOps(Graph g, int opsNum) {
//		System.out.println("\n\n*****");
		Random rand = new Random(nextRandSeed());
		RandWeighted<GraphOp> opRand = new RandWeighted<>();
		opRand.add(GraphOp.AddEdge, 20);
		opRand.add(GraphOp.RemoveEdge, 10);
		opRand.add(GraphOp.ClearEdges, 1);
		opRand.add(GraphOp.AddVertex, 4);

		final Object dataKey = new Object();
		Weights<Object> edgeData = EdgesWeights.ofObjs(g, dataKey);

		GraphTracker tracker = new GraphTracker(g.vertices().size(), g instanceof DiGraph, dataKey);
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			Object data = new Object();
			edgeData.set(e, data);
			tracker.addEdge(u, v, data);
		}

		for (; opsNum > 0;) {
			final GraphOp op = opRand.get(rand);
			switch (op) {
			case AddEdge: {
				int u, v, retry = 20;
				do {
					u = rand.nextInt(tracker.verticesNum());
					v = rand.nextInt(tracker.verticesNum());
					int e = g.getEdge(u, v);
					if (u != v && e == -1)
						break;
				} while (retry-- > 0);
				if (retry <= 0)
					continue;

				Object data = new Object();
				int e = g.addEdge(u, v);
				edgeData.set(e, data);
				tracker.addEdge(u, v, data);
				break;
			}
			case RemoveEdge: {
				int u, v, e, retry = 20;
				do {
					u = rand.nextInt(tracker.verticesNum());
					v = rand.nextInt(tracker.verticesNum());
					e = g.getEdge(u, v);
				} while (e == -1 && retry-- > 0);
				if (retry <= 0)
					continue;

				g.removeEdge(e);
				tracker.removeEdge(u, v);
				break;
			}
			case ClearEdges:
				if (g.edges().size() == 0)
					continue;
				g.clearEdges();
				tracker.clearEdges();
				break;

			case AddVertex:
//				g.newVertex();
//				tracker.newVertex();
//				break;
				continue; // not supported by all graph implementations

			default:
				throw new IllegalArgumentException("Unexpected value: " + op);
			}

			Assertions.assertTrue(g.vertices().size() == tracker.verticesNum());
			Assertions.assertTrue(g.edges().size() == tracker.edgesNum());
			Assertions.assertTrue(tracker.checkEdgesEqual(g));

			opsNum--;
		}
	}

}
