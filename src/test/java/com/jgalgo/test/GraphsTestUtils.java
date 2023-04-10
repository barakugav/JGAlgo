package com.jgalgo.test;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.jgalgo.EdgeIter;
import com.jgalgo.Graph;
import com.jgalgo.GraphArrayUndirected;
import com.jgalgo.UGraph;
import com.jgalgo.UnionFind;
import com.jgalgo.UnionFindArray;
import com.jgalgo.Weights;
import com.jgalgo.test.GraphImplTestUtils.GraphImpl;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

public class GraphsTestUtils extends TestUtils {

	private GraphsTestUtils() {
	}

	public static class RandomGraphBuilder {

		private final SeedGenerator seedGen;
		private int n;
		private int sn;
		private int tn;
		private int m;
		private boolean bipartite;
		private boolean directed;
		private boolean parallelEdges;
		private boolean selfEdges;
		private boolean cycles;
		private boolean connected;
		private GraphImpl impl = GraphImplTestUtils.GRAPH_IMPL_DEFAULT;

		public RandomGraphBuilder(long seed) {
			seedGen = new SeedGenerator(seed);
			n = sn = tn = m = 0;
			bipartite = false;
			parallelEdges = false;
			selfEdges = false;
			cycles = false;
			connected = false;
		}

		public RandomGraphBuilder n(int n) {
			this.n = n;
			return this;
		}

		public RandomGraphBuilder sn(int sn) {
			this.sn = sn;
			return this;
		}

		public RandomGraphBuilder tn(int tn) {
			this.tn = tn;
			return this;
		}

		public RandomGraphBuilder m(int m) {
			this.m = m;
			return this;
		}

		public RandomGraphBuilder bipartite(boolean bipartite) {
			this.bipartite = bipartite;
			return this;
		}

		public RandomGraphBuilder directed(boolean directed) {
			this.directed = directed;
			return this;
		}

		public RandomGraphBuilder parallelEdges(boolean parallelEdges) {
			this.parallelEdges = parallelEdges;
			return this;
		}

		public RandomGraphBuilder selfEdges(boolean selfEdges) {
			this.selfEdges = selfEdges;
			return this;
		}

		public RandomGraphBuilder cycles(boolean cycles) {
			this.cycles = cycles;
			return this;
		}

		public RandomGraphBuilder connected(boolean connected) {
			this.connected = connected;
			return this;
		}

		public RandomGraphBuilder graphImpl(GraphImpl impl) {
			this.impl = impl;
			return this;
		}

		public Graph build() {
			final Graph g;
			if (!bipartite) {
				if (n < 0 || m < 0)
					throw new IllegalStateException();
				g = impl.newGraph(directed, n);
			} else {
				if (sn < 0 || tn < 0)
					throw new IllegalStateException();
				if ((sn == 0 || tn == 0) && m != 0)
					throw new IllegalStateException();
				n = sn + tn;
				g = impl.newGraph(directed, n);
				Weights.Bool partition = g.addVerticesWeight(Weights.DefaultBipartiteWeightKey).ofBools();
				for (int u = 0; u < sn; u++)
					partition.set(u, true);
				for (int u = 0; u < tn; u++)
					partition.set(sn + u, false);
			}
			if (n == 0)
				return g;
			if (!directed && !cycles && m >= n)
				throw new IllegalArgumentException();
			if (!cycles && selfEdges)
				throw new IllegalArgumentException();
			if (!parallelEdges) {
				long limit;
				if (bipartite)
					limit = n <= 16 ? sn * tn : ((long) sn) * tn * 2 / 3;
				else
					limit = n <= 16 ? (n - 1) * n / 2 : ((long) n) * n / 3;
				if (m > limit)
					throw new IllegalArgumentException("too much edges for random sampling");
			}

			Set<IntList> existingEdges = new HashSet<>();
			UnionFind uf = new UnionFindArray(n);
			int componentsNum = n;
			Random rand = new Random(seedGen.nextSeed());
			BitSet reachableFromRoot = new BitSet(n);
			reachableFromRoot.set(0);
			int reachableFromRootCount = 1;
			IntPriorityQueue queue = new IntArrayFIFOQueue();

			while (true) {
				boolean done = true;
				if (g.edges().size() < m)
					done = false;
				if (connected) {
					if (!directed && componentsNum > 1)
						done = false;
					else if (directed && reachableFromRootCount < n)
						done = false;
				}
				if (done)
					break;

				int u, v;

				if (!bipartite) {
					u = rand.nextInt(n);
					v = rand.nextInt(n);
					if (directed && !cycles && u > v) {
						int temp = u;
						u = v;
						v = temp;
					}
				} else {
					u = rand.nextInt(sn);
					v = sn + rand.nextInt(tn);
				}

				// avoid self edges
				if (!selfEdges && u == v)
					continue;

				// avoid double edges
				if (!parallelEdges) {
					int ut = u, vt = v;
					if (!directed && ut > vt) {
						int temp = ut;
						ut = vt;
						vt = temp;
					}
					if (!existingEdges.add(IntList.of(ut, vt)))
						continue;
				}

				// keep track of number of connectivity components
				if (!cycles || connected) {
					if (!directed) {
						int uComp = uf.find(u);
						int vComp = uf.find(v);

						// avoid cycles
						if (!cycles && uComp == vComp)
							continue;

						if (uComp != vComp)
							componentsNum--;
						uf.union(uComp, vComp);
					} else if (connected) {
						if (reachableFromRoot.get(u) && !reachableFromRoot.get(v)) {
							reachableFromRoot.set(v);
							reachableFromRootCount++;

							queue.enqueue(v);
							while (!queue.isEmpty()) {
								int p = queue.dequeueInt();

								for (EdgeIter eit = g.edgesOut(p); eit.hasNext();) {
									eit.nextInt();
									int pv = eit.v();
									if (reachableFromRoot.get(pv))
										continue;
									reachableFromRoot.set(pv);
									reachableFromRootCount++;
									queue.enqueue(pv);
								}
							}

						}
					}
				}

				g.addEdge(u, v);
			}

			return g;
		}

	}

	public static UGraph randTree(int n, long seed) {
		return (UGraph) new RandomGraphBuilder(seed).n(n).m(n - 1).directed(false).selfEdges(false).cycles(false)
				.connected(true).build();
	}

	static UGraph randForest(int n, int m, long seed) {
		return (UGraph) new RandomGraphBuilder(seed).n(n).m(m).directed(false).selfEdges(false).cycles(false)
				.connected(false).build();
	}

	static Weights.Double assignRandWeights(Graph g, long seed) {
		return assignRandWeights(g, 1.0, 100.0, seed);
	}

	static Weights.Double assignRandWeights(Graph g, double minWeight, double maxWeight, long seed) {
		if (minWeight >= maxWeight)
			throw new IllegalArgumentException();

		Random rand = new Random(seed);
		Weights.Double weight = g.addEdgesWeight("weight").ofDoubles();
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			weight.set(e, nextDouble(rand, minWeight, maxWeight));
		}
		return weight;
	}

	public static Weights.Int assignRandWeightsIntPos(Graph g, long seed) {
		int m = g.edges().size();
		int minWeight = 1;
		int maxWeight = m < 50 ? 100 : m * 2 + 2;
		return assignRandWeightsInt(g, minWeight, maxWeight, seed);
	}

	public static Weights.Int assignRandWeightsIntNeg(Graph g, long seed) {
		int m = g.edges().size();
		int maxWeight = m < 50 ? 100 : m * 2 + 2;
		return assignRandWeightsInt(g, -maxWeight / 8, maxWeight, seed);
	}

	static Weights.Int assignRandWeightsInt(Graph g, int minWeight, int maxWeight, long seed) {
		if (minWeight >= maxWeight)
			throw new IllegalArgumentException();
		if (maxWeight - minWeight < g.edges().size() / 2)
			throw new IllegalArgumentException("weight range is too small for unique weights");

		RandomIntUnique rand = new RandomIntUnique(minWeight, maxWeight, seed);
		Weights.Int weight = g.addEdgesWeight("weight").ofInts();
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			weight.set(e, rand.next());
		}
		return weight;
	}

	public static Graph randGraph(int n, int m, long seed) {
		return randGraph(n, m, GraphImplTestUtils.GRAPH_IMPL_DEFAULT, seed);
	}

	static UGraph randGraph(int n, int m, GraphImpl graphImpl, long seed) {
		return (UGraph) new RandomGraphBuilder(seed).n(n).m(m).directed(false).parallelEdges(false).selfEdges(false)
				.cycles(true).connected(false).build();
	}

//	static Graph<Integer> createGraphFromAdjacencyMatrixWeightedInt(int[][] m, DirectedType directed) {
//		int n = m.length;
//		Graph<Integer> g = new GraphArray<>(directed, n);
//		for (int u = 0; u < n; u++) {
//			for (int v = directed == DirectedType.Directed ? 0 : u + 1; v < n; v++) {
//				if (m[u][v] == 0)
//					continue;
//				g.addEdge(u, v).setData(Integer.valueOf(m[u][v]));
//			}
//		}
//		return g;
//	}
//
//	static Graph<Integer> parseGraphFromAdjacencyMatrixWeightedInt(String s) {
//		String[] lines = s.split("\r\n");
//		int n = lines.length;
//		int[][] m = new int[n][n];
//		for (int u = 0; u < n; u++) {
//			String[] esStr = lines[u].split(",");
//			for (int v = u + 1; v < n; v++)
//				Integer.parseInt(esStr[v].trim());
//		}
//		return createGraphFromAdjacencyMatrixWeightedInt(m, DirectedType.Undirected);
//	}

	static Graph parseGraphFromAdjacencyMatrix01(String s) {
		String[] lines = s.split("\r\n");
		int n = lines.length;
		Graph g = new GraphArrayUndirected(n);
		for (int u = 0; u < n; u++) {
			String[] chars = lines[u].split(" ");
			for (int v = u + 1; v < n; v++)
				if (chars[v].equals("1"))
					g.addEdge(u, v);
		}
		return g;
	}

	static Graph parseGraphWeighted(String s) {
		String[] lines = s.split("\r\n");
		int n = lines.length;
		Graph g = new GraphArrayUndirected(n);
		for (int u = 0; u < n; u++) {
			String[] chars = lines[u].split(" ");
			for (int v = u + 1; v < n; v++)
				if (chars[v].equals("1"))
					g.addEdge(u, v);
		}
		return g;
	}

}
