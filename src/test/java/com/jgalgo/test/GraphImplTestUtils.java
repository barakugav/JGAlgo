package com.jgalgo.test;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.function.ToIntFunction;

import org.junit.jupiter.api.Assertions;

import com.jgalgo.DiGraph;
import com.jgalgo.EdgeIter;
import com.jgalgo.Graph;
import com.jgalgo.GraphArrayDirected;
import com.jgalgo.GraphArrayUndirected;
import com.jgalgo.GraphCapabilities;
import com.jgalgo.MDSTTarjan1977;
import com.jgalgo.MSTKruskal1956;
import com.jgalgo.MatchingGabow1976;
import com.jgalgo.MatchingWeightedBipartiteHungarianMethod;
import com.jgalgo.MaxFlowEdmondsKarp;
import com.jgalgo.Weights;
import com.jgalgo.test.GraphsTestUtils.RandomGraphBuilder;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
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
				IntIterator endpointsIt = endpoints.iterator();
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
						Assertions.assertEquals(u, eit.u());
						Assertions.assertEquals(g.edgeEndpoint(e, u), eit.v());
						uEdges.add(e);
					}
					Assertions.assertEquals(edgesOut.get(u), uEdges);
				}
				for (IntIterator it = g.vertices().iterator(); it.hasNext();) {
					int v = it.nextInt();
					IntSet vEdges = new IntOpenHashSet();
					for (EdgeIter eit = ((DiGraph) g).edgesIn(v); eit.hasNext();) {
						int e = eit.nextInt();
						Assertions.assertEquals(v, eit.v());
						Assertions.assertEquals(g.edgeEndpoint(e, v), eit.u());
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

	static void testUndirectedMST(GraphImpl graphImpl, long seed) {
		MSTTestUtils.testRandGraph(MSTKruskal1956::new, graphImpl, seed);
	}

	static void testDirectedMDST(GraphImpl graphImpl, long seed) {
		MDSTTarjan1977Test.testRandGraph(MDSTTarjan1977::new, graphImpl, seed);
	}

	static void testDirectedMaxFlow(GraphImpl graphImpl, long seed) {
		MaxFlowTestUtils.testRandGraphs(MaxFlowEdmondsKarp::new, graphImpl, seed);
	}

	static void testUndirectedBipartiteMatching(GraphImpl graphImpl, long seed) {
		MatchingBipartiteTestUtils.randBipartiteGraphs(MatchingGabow1976::new, graphImpl, seed);
	}

	static void testUndirectedBipartiteMatchingWeighted(GraphImpl graphImpl, long seed) {
		MatchingWeightedTestUtils.randGraphsBipartiteWeighted(MatchingWeightedBipartiteHungarianMethod::new, graphImpl,
				seed);
	}

	static void testUndirectedRandOps(GraphImpl graphImpl, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
		List<Phase> phases = List.of(phase(1024, 6, 6), phase(128, 16, 16), phase(128, 16, 32), phase(64, 64, 64),
				phase(64, 64, 128), phase(8, 512, 512), phase(4, 512, 1324), phase(1, 1025, 2016),
				phase(1, 3246, 5612));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			for (boolean directed : new boolean[] { true, false }) {
				Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(false)
						.selfEdges(false).cycles(true).connected(false).graphImpl(graphImpl).build();
				final int opsNum = 128;
				testRandOps(g, opsNum, seedGen.nextSeed());
			}
		});
	}

	private static class RandWeighted<E> {
		private final List<E> elms = new ArrayList<>();
		private final IntList weights = new IntArrayList();
		private int totalWeight;

		void add(E elm, int weight) {
			if (weight <= 0)
				throw new IllegalArgumentException();
			elms.add(elm);
			weights.add(weight);
			totalWeight += weight;
		}

		E get(Random rand) {
			final int v = rand.nextInt(totalWeight);
			int s = 0;
			for (int i = 0; i < elms.size(); i++) {
				s += weights.getInt(i);
				if (v <= s)
					return elms.get(i);
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

		void addVertex() {
			if (debugPrints)
				System.out.println("newVertex()");
			n++;
		}

		void addEdge(int u, int v, int data) {
			if (debugPrints)
				System.out.println("addEdge(" + u + ", " + v + ", " + data + ")");
			edges.add(new Edge(u, v, data));
		}

		Edge getEdge(int u, int v) {
			if (directed) {
				for (int i = 0; i < edges.size(); i++) {
					Edge e = edges.get(i);
					if (e.u == u && e.v == v)
						return e;
				}
			} else {
				for (int i = 0; i < edges.size(); i++) {
					Edge e = edges.get(i);
					if ((e.u == u && e.v == v) || (e.v == u && e.u == v))
						return e;
				}
			}
			return null;
		}

		void removeEdge(Edge edge) {
			if (debugPrints)
				System.out.println("removeEdge(" + edge.u + ", " + edge.v + ")");
			boolean removed = edges.remove(edge);
			Assertions.assertTrue(removed);
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

		void reverseEdge(Edge edge) {
			if (debugPrints)
				System.out.println("reverse(" + edge.u + ", " + edge.v + ")");
			int temp = edge.u;
			edge.u = edge.v;
			edge.v = temp;
		}

		Edge getRandEdge(Random rand) {
			return edges.get(rand.nextInt(edges.size()));
		}

		void clearEdges() {
			if (debugPrints)
				System.out.println("clearEdges()");
			edges.clear();
		}

		void checkEdgesEqual(Graph g) {
			Assertions.assertEquals(edgesNum(), g.edges().size());
			Weights.Int edgeData = g.edgesWeight(dataKey);

			List<IntList> actual = new ArrayList<>();
			List<IntList> expected = new ArrayList<>();

			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				if (!directed && u > v) {
					int temp = u;
					u = v;
					v = temp;
				}
				int data = edgeData.getInt(e);
				actual.add(IntList.of(u, v, data));
			}

			for (Edge edge : edges) {
				int u = edge.u, v = edge.v;
				if (!directed && u > v) {
					int temp = u;
					u = v;
					v = temp;
				}
				int data = edge.data;
				expected.add(IntList.of(u, v, data));
			}

			Comparator<IntList> cmp = (e1, e2) -> {
				int u1 = e1.getInt(0), v1 = e1.getInt(1), d1 = e1.getInt(2);
				int u2 = e2.getInt(0), v2 = e2.getInt(1), d2 = e2.getInt(2);
				int c;
				if ((c = Integer.compare(u1, u2)) != 0)
					return c;
				if ((c = Integer.compare(v1, v2)) != 0)
					return c;
				if ((c = Integer.compare(d1, d2)) != 0)
					return c;
				return 0;
			};
			actual.sort(cmp);
			expected.sort(cmp);

			if (!expected.equals(actual))
				System.out.println();
			Assertions.assertEquals(expected, actual);
		}

		private static class Edge {
			int u, v;
			final int data;

			Edge(int u, int v, int data) {
				this.u = u;
				this.v = v;
				this.data = data;
			}

			@Override
			public String toString() {
				return "(" + u + ", " + v + ", " + data + ")";
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

	private static class UniqueGenerator {
		private final Random rand;
		private final IntSet used;

		UniqueGenerator(long seed) {
			rand = new Random(seed);
			used = new IntOpenHashSet();
		}

		int next() {
			for (;;) {
				int x = rand.nextInt();
				if (!used.contains(x)) {
					used.add(x);
					return x;
				}
			}
		}
	}

	private static void testRandOps(Graph g, int opsNum, long seed) {
		final SeedGenerator seedGen = new SeedGenerator(seed);
//		System.out.println("\n\n*****");
		GraphCapabilities capabilities = g.getCapabilities();
		Random rand = new Random(seedGen.nextSeed());
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
		Weights.Int edgeData = g.addEdgesWeight(dataKey).ofInts();
		UniqueGenerator dataGen = new UniqueGenerator(seedGen.nextSeed());

		GraphTracker tracker = new GraphTracker(g.vertices().size(), g instanceof DiGraph, dataKey);
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			final int data = dataGen.next();
			edgeData.set(e, data);
			tracker.addEdge(u, v, data);
		}

		ToIntFunction<GraphTracker.Edge> getEdge = edge -> {
			int e = -1;
			for (EdgeIter eit = g.getEdges(edge.u, edge.v); eit.hasNext();) {
				int e0 = eit.nextInt();
				if (edge.data == edgeData.getInt(e0)) {
					e = e0;
					break;
				}
			}
			Assertions.assertNotEquals(e, -1);
			return e;
		};

		opLoop: for (; opsNum > 0;) {
			final GraphOp op = opRand.get(rand);
			switch (op) {
			case AddEdge: {
				int u, v, retry = 20;
				for (;; retry--) {
					if (retry <= 0)
						continue opLoop;

					u = rand.nextInt(tracker.verticesNum());
					v = rand.nextInt(tracker.verticesNum());
					if (!capabilities.selfEdges() && u == v)
						continue;
					if (!capabilities.parallelEdges() && tracker.getEdge(u, v) != null)
						continue;
					break;
				}

				final int data = dataGen.next();
				int e = g.addEdge(u, v);
				edgeData.set(e, data);
				tracker.addEdge(u, v, data);
				break;
			}
			case RemoveEdge: {
				if (tracker.edgesNum() == 0)
					continue;
				GraphTracker.Edge edge = tracker.getRandEdge(rand);
				int e = getEdge.applyAsInt(edge);

				g.removeEdge(e);
				tracker.removeEdge(edge);
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
				if (edge.u != edge.v && g.getEdge(edge.v, edge.u) != -1 && !capabilities.parallelEdges())
					continue;
				int e = getEdge.applyAsInt(edge);

				((DiGraph) g).reverseEdge(e);
				tracker.reverseEdge(edge);
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
			tracker.checkEdgesEqual(g);

			opsNum--;
		}
	}

}
