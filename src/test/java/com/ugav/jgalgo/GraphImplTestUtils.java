package com.ugav.jgalgo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

import org.junit.jupiter.api.Assertions;

import com.ugav.jgalgo.GraphsTestUtils.RandomGraphBuilder;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;

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

	static IntSet intSetOf(int... elms) {
		IntSet set = new IntOpenHashSet();
		for (int e : elms)
			set.add(e);
		return IntSets.unmodifiable(set);
	}

	static void testVertexAdd(GraphImpl graphImpl) {
		for (boolean directed : new boolean[] { true, false }) {
			Graph g = graphImpl.newGraph(directed, 0);
			final int n = 100;
			IntSet verticesSet = new IntOpenHashSet();
			for (int i = 0; i < n; i++) {
				int v = g.addVertex();
				verticesSet.add(v);
			}
			Assertions.assertEquals(verticesSet, g.vertices());
			Assertions.assertEquals(IntSets.emptySet(), g.edges());
		}
	}

	static void testCreateWithNVertices(GraphImpl graphImpl) {
		for (boolean directed : new boolean[] { true, false }) {
			final int n = 100;
			Graph g = graphImpl.newGraph(directed, n);
			IntSet verticesSet = new IntOpenHashSet();
			for (int v = 0; v < n; v++)
				verticesSet.add(v);
			Assertions.assertEquals(verticesSet, g.vertices());
			Assertions.assertEquals(IntSets.emptySet(), g.edges());
		}
	}

	static void testAddEdge(GraphImpl graphImpl) {
		for (boolean directed : new boolean[] { true, false }) {
			final int n = 100;
			Graph g = graphImpl.newGraph(directed, n);

			Int2ObjectMap<int[]> edges = new Int2ObjectOpenHashMap<>();
			for (int u = 0; u < n; u++) {
				for (int v = u + 1; v < n; v++) {
					int e = g.addEdge(u, v);
					assertEndpoints(g, e, u, v);
					edges.put(e, new int[] { e, u, v });
				}
			}
			Assertions.assertEquals(edges.keySet(), g.edges());
			for (int[] edge : edges.values()) {
				int e = edge[0], u = edge[1], v = edge[2];
				assertEndpoints(g, e, u, v);
			}
		}
	}

	private static void assertEndpoints(Graph g, int e, int u, int v) {
		if (g.getCapabilities().directed()) {
			Assertions.assertEquals(u, g.edgeSource(e));
			Assertions.assertEquals(v, g.edgeTarget(e));
		} else {
			Assertions.assertEquals(intSetOf(u, v), intSetOf(g.edgeSource(e), g.edgeTarget(e)));
		}
		Assertions.assertEquals(u, g.edgeEndpoint(e, v));
		Assertions.assertEquals(v, g.edgeEndpoint(e, u));
	}

	static void testGetEdge(GraphImpl graphImpl) {
		for (boolean directed : new boolean[] { true, false }) {
			final int n = 100;
			Graph g = graphImpl.newGraph(directed, n);

			Object2IntMap<IntCollection> edges = new Object2IntOpenHashMap<>();
			for (int u = 0; u < n; u++) {
				for (int v = directed ? 0 : u; v < n; v++) {
					if (u == v && !g.getCapabilities().selfEdges())
						continue;
					int e = g.addEdge(u, v);
					assertEndpoints(g, e, u, v);
					if (directed) {
						edges.put(IntList.of(u, v), e);
					} else {
						edges.put(intSetOf(u, v), e);
					}
				}
			}
			for (Object2IntMap.Entry<IntCollection> edge : edges.object2IntEntrySet()) {
				IntCollection endpoints = edge.getKey();
				IntIterator endpointsIt = endpoints.intIterator();
				int u = endpointsIt.nextInt(), v = endpointsIt.hasNext() ? endpointsIt.nextInt() : u;
				int e = edge.getIntValue();
				Assertions.assertEquals(e, g.getEdge(u, v));
			}
		}
	}

	static void testGetEdges(GraphImpl graphImpl) {
		for (boolean directed : new boolean[] { true, false }) {
			final int n = 100;
			Graph g = graphImpl.newGraph(directed, n);

			Int2ObjectMap<IntSet> edgesOut = new Int2ObjectOpenHashMap<>();
			Int2ObjectMap<IntSet> edgesIn = new Int2ObjectOpenHashMap<>();
			for (int u = 0; u < n; u++) {
				for (int v = directed ? 0 : u; v < n; v++) {
					if (u == v && !g.getCapabilities().selfEdges())
						continue;
					int e = g.addEdge(u, v);
					if (directed) {
						edgesOut.computeIfAbsent(u, w -> new IntOpenHashSet()).add(e);
						edgesIn.computeIfAbsent(v, w -> new IntOpenHashSet()).add(e);
					} else {
						edgesOut.computeIfAbsent(u, w -> new IntOpenHashSet()).add(e);
						edgesOut.computeIfAbsent(v, w -> new IntOpenHashSet()).add(e);
					}
				}
			}
			for (IntIterator it = g.vertices().iterator(); it.hasNext();) {
				int u = it.nextInt();
				IntSet uEdges = new IntOpenHashSet();
				for (EdgeIter eit = g.edges(u); eit.hasNext();) {
					int e = eit.nextInt();
					uEdges.add(e);
				}
				Assertions.assertEquals(edgesOut.get(u), uEdges);
			}
			if (directed) {
				for (IntIterator it = g.vertices().iterator(); it.hasNext();) {
					int u = it.nextInt();
					IntSet uEdges = new IntOpenHashSet();
					for (EdgeIter eit = ((DiGraph) g).edgesOut(u); eit.hasNext();) {
						int e = eit.nextInt();
						uEdges.add(e);
					}
					Assertions.assertEquals(edgesOut.get(u), uEdges);
				}
				for (IntIterator it = g.vertices().iterator(); it.hasNext();) {
					int v = it.nextInt();
					IntSet vEdges = new IntOpenHashSet();
					for (EdgeIter eit = ((DiGraph) g).edgesIn(v); eit.hasNext();) {
						int e = eit.nextInt();
						vEdges.add(e);
					}
					Assertions.assertEquals(edgesIn.get(v), vEdges);
				}
			}
		}
	}

	static void testEdgeIter(GraphImpl graphImpl) {
		for (boolean directed : new boolean[] { true, false }) {
			final int n = 100;
			Graph g = graphImpl.newGraph(directed, n);

			Int2ObjectMap<IntCollection> edges = new Int2ObjectOpenHashMap<>();
			for (int u = 0; u < n; u++) {
				for (int v = directed ? 0 : u; v < n; v++) {
					if (u == v && !g.getCapabilities().selfEdges())
						continue;
					int e = g.addEdge(u, v);
					assertEndpoints(g, e, u, v);
					if (directed) {
						edges.put(e, IntList.of(u, v));
					} else {
						edges.put(e, intSetOf(u, v));
					}
				}
			}
			for (IntIterator it = g.vertices().iterator(); it.hasNext();) {
				int u = it.nextInt();
				for (EdgeIter eit = g.edges(u); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.v();
					if (directed) {
						Assertions.assertEquals(edges.get(e), IntList.of(eit.u(), eit.v()));
					} else {
						Assertions.assertEquals(edges.get(e), intSetOf(eit.u(), eit.v()));
					}
					Assertions.assertEquals(u, eit.u());
					Assertions.assertEquals(v, g.edgeEndpoint(e, u));
					if (directed) {
						Assertions.assertEquals(g.edgeSource(e), eit.u());
						Assertions.assertEquals(g.edgeTarget(e), eit.v());
					}
				}
			}
		}
	}

	static void testDgree(GraphImpl graphImpl) {
		for (boolean directed : new boolean[] { true, false }) {
			final int n = 100;
			Graph g = graphImpl.newGraph(directed, n);

			Int2IntMap degree = new Int2IntOpenHashMap();
			Int2IntMap degreeOut = new Int2IntOpenHashMap();
			Int2IntMap degreeIn = new Int2IntOpenHashMap();
			for (int u = 0; u < n; u++) {
				for (int v = directed ? 0 : u; v < n; v++) {
					if (u == v && !g.getCapabilities().selfEdges())
						continue;
					g.addEdge(u, v);

					degree.put(u, degree.get(u) + 1);
					if (u != v)
						degree.put(v, degree.get(v) + 1);

					degreeOut.put(u, degreeOut.get(u) + 1);
					degreeIn.put(v, degreeIn.get(v) + 1);
				}
			}
			for (IntIterator it = g.vertices().iterator(); it.hasNext();) {
				int u = it.nextInt();
				Assertions.assertEquals(degree.get(u), g.degree(u));
				if (directed) {
					Assertions.assertEquals(degreeOut.get(u), ((DiGraph) g).degreeOut(u));
					Assertions.assertEquals(degreeIn.get(u), ((DiGraph) g).degreeIn(u));
				}
			}
		}
	}

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
		void addVertex() {
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

		void removeEdgesAll(int u) {
			edges.removeIf(edge -> edge.u == u || edge.v == u);
		}

		void removeEdgesAllOut(int u) {
			edges.removeIf(edge -> edge.u == u);
		}

		void removeEdgesAllIn(int v) {
			edges.removeIf(edge -> edge.v == v);
		}

		Edge getRandEdge(Random rand) {
			return edges.get(rand.nextInt(edges.size()));
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
			int u, v;
			final Object data;

			Edge(int u, int v, Object data) {
				this.u = u;
				this.v = v;
				this.data = Objects.requireNonNull(data);
			}

			void reverse() {
				int temp = u;
				u = v;
				v = temp;
			}

			@Override
			public String toString() {
				return "(" + u + ", " + v + ")";
			}
		}
	}

	private static enum GraphOp {
		GetEdge, GetVertexEdges, GetVertexEdgesOut, GetVertexEdgesIn,

		EdgeSource, EdgeTarget,

		Degree, DegreeIn, DegreeOut,

		AddEdge, RemoveEdge, RemoveEdgeAll, RemoveEdgeAllIn, RemoveEdgeAllOut, ReverseEdge,

		ClearEdges,

		AddVertex, RemoveVertex,
	}

	private static void testRandOps(Graph g, int opsNum) {
//		System.out.println("\n\n*****");
		GraphCapabilities capabilities = g.getCapabilities();
		Random rand = new Random(nextRandSeed());
		RandWeighted<GraphOp> opRand = new RandWeighted<>();
		if (capabilities.edgeAdd())
			opRand.add(GraphOp.AddEdge, 20);
		if (capabilities.edgeRemove()) {
			opRand.add(GraphOp.RemoveEdge, 10);
			opRand.add(GraphOp.RemoveEdgeAll, 1);
			opRand.add(GraphOp.ClearEdges, 1);
		}
		if (capabilities.edgeRemove() && capabilities.directed()) {
			opRand.add(GraphOp.RemoveEdgeAllIn, 1);
			opRand.add(GraphOp.RemoveEdgeAllOut, 1);
		}
		if (capabilities.directed())
			opRand.add(GraphOp.ReverseEdge, 3);
		if (capabilities.vertexAdd())
			opRand.add(GraphOp.AddVertex, 4);
		if (capabilities.vertexRemove()) {
			if (!capabilities.edgeRemove())
				throw new IllegalArgumentException("vertex removal can't be supported while edge removal is not");
//			opRand.add(GraphOp.RemoveVertex, 4);
		}

		final Object dataKey = new Object();
		Weights<Object> edgeData = g.addEdgesWeight(dataKey).ofObjs();

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
				if (tracker.edgesNum() == 0)
					continue;
				GraphTracker.Edge edge = tracker.getRandEdge(rand);
				int e = g.getEdge(edge.u, edge.v);

				g.removeEdge(e);
				tracker.removeEdge(edge.u, edge.v);
				break;
			}
			case RemoveEdgeAll: {
				if (tracker.verticesNum() == 0)
					continue;
				int u = rand.nextInt(tracker.verticesNum());
				g.removeEdgesAll(u);
				tracker.removeEdgesAll(u);
				break;
			}
			case RemoveEdgeAllIn: {
				if (tracker.verticesNum() == 0)
					continue;
				int u = rand.nextInt(tracker.verticesNum());
				if (!(g instanceof DiGraph))
					System.out.println();
				((DiGraph) g).removeEdgesAllIn(u);
				tracker.removeEdgesAllIn(u);
				break;
			}
			case RemoveEdgeAllOut: {
				if (tracker.verticesNum() == 0)
					continue;
				int u = rand.nextInt(tracker.verticesNum());
				((DiGraph) g).removeEdgesAllOut(u);
				tracker.removeEdgesAllOut(u);
				break;
			}
			case ReverseEdge: {
				if (tracker.edgesNum() == 0)
					continue;
				GraphTracker.Edge edge = tracker.getRandEdge(rand);
				int e = g.getEdge(edge.u, edge.v);
				if (edge.u != edge.v && g.getEdge(edge.v, edge.u) != -1 && !capabilities.parallelEdges())
					continue;

				((DiGraph) g).reverseEdge(e);
				edge.reverse();
				break;
			}
			case ClearEdges:
				if (g.edges().size() == 0)
					continue;
				g.clearEdges();
				tracker.clearEdges();
				break;

			case AddVertex:
				g.addVertex();
				tracker.addVertex();
				break;

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
