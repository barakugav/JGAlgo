package com.ugav.jgalgo;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.ugav.jgalgo.GraphImplTestUtils.GraphImpl;

import it.unimi.dsi.fastutil.ints.IntIterator;

class GraphsTestUtils extends TestUtils {

	private GraphsTestUtils() {
	}

	static class RandomGraphBuilder {

		private int n;
		private int sn;
		private int tn;
		private int m;
		private boolean bipartite;
		private boolean directed;
		private boolean doubleEdges;
		private boolean selfEdges;
		private boolean cycles;
		private boolean connected;
		private GraphImpl impl = GraphImplTestUtils.GRAPH_IMPL_DEFAULT;

		RandomGraphBuilder() {
			n = sn = tn = m = 0;
			bipartite = false;
			doubleEdges = false;
			selfEdges = false;
			cycles = false;
			connected = false;
		}

		RandomGraphBuilder n(int n) {
			this.n = n;
			return this;
		}

		RandomGraphBuilder sn(int sn) {
			this.sn = sn;
			return this;
		}

		RandomGraphBuilder tn(int tn) {
			this.tn = tn;
			return this;
		}

		RandomGraphBuilder m(int m) {
			this.m = m;
			return this;
		}

		RandomGraphBuilder bipartite(boolean bipartite) {
			this.bipartite = bipartite;
			return this;
		}

		RandomGraphBuilder directed(boolean directed) {
			this.directed = directed;
			return this;
		}

		RandomGraphBuilder doubleEdges(boolean doubleEdges) {
			this.doubleEdges = doubleEdges;
			return this;
		}

		RandomGraphBuilder selfEdges(boolean selfEdges) {
			this.selfEdges = selfEdges;
			return this;
		}

		RandomGraphBuilder cycles(boolean cycles) {
			this.cycles = cycles;
			return this;
		}

		RandomGraphBuilder connected(boolean connected) {
			this.connected = connected;
			return this;
		}

		RandomGraphBuilder graphImpl(GraphImpl impl) {
			this.impl = impl;
			return this;
		}

		Graph build() {
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
			if (!doubleEdges) {
				long limit;
				if (bipartite)
					limit = n <= 16 ? sn * tn : ((long) sn) * tn * 2 / 3;
				else
					limit = n <= 16 ? (n - 1) * n / 2 : ((long) n) * n / 3;
				if (m > limit)
					throw new IllegalArgumentException("too much edges for random sampling");
			}

			Set<Pair<Integer, Integer>> existingEdges = new HashSet<>();
			UnionFind uf = new UnionFindArray(n);
			int componentsNum = n;
			Random rand = new Random(nextRandSeed());
			boolean[] reachableFromRoot = new boolean[n];
			reachableFromRoot[0] = true;
			int reachableFromRootCount = 1;
			int[] queue = new int[n];

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
				if (!doubleEdges) {
					int ut = u, vt = v;
					if (!directed && ut > vt) {
						int temp = ut;
						ut = vt;
						vt = temp;
					}
					if (!existingEdges.add(Pair.of(Integer.valueOf(ut), Integer.valueOf(vt))))
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
						if (reachableFromRoot[u] && !reachableFromRoot[v]) {
							reachableFromRoot[v] = true;
							reachableFromRootCount++;

							int queueBegin = 0, queueEnd = 0;
							queue[queueEnd++] = v;
							while (queueBegin != queueEnd) {
								int p = queue[queueBegin++];

								for (EdgeIter eit = g.edges(p); eit.hasNext();) {
									eit.nextInt();
									int pv = eit.v();
									if (reachableFromRoot[pv])
										continue;
									reachableFromRoot[pv] = true;
									reachableFromRootCount++;
									queue[queueEnd++] = pv;
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

	static UGraph randTree(int n) {
		return (UGraph) new RandomGraphBuilder().n(n).m(n - 1).directed(false).selfEdges(false).cycles(false)
				.connected(true).build();
	}

	static UGraph randForest(int n, int m) {
		return (UGraph) new RandomGraphBuilder().n(n).m(m).directed(false).selfEdges(false).cycles(false)
				.connected(false).build();
	}

	static void assignRandWeights(Graph g) {
		assignRandWeights(g, 1.0, 100.0);
	}

	static void assignRandWeights(Graph g, double minWeight, double maxWeight) {
		if (minWeight >= maxWeight)
			throw new IllegalArgumentException();

		Random rand = new Random(nextRandSeed());
		Weights.Double weight = g.addEdgesWeight("weight").ofDoubles();
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			weight.set(e, nextDouble(rand, minWeight, maxWeight));
		}
	}

	static void assignRandWeightsIntPos(Graph g) {
		int m = g.edges().size();
		int minWeight = 1;
		int maxWeight = m < 50 ? 100 : m * 2 + 2;
		assignRandWeightsInt(g, minWeight, maxWeight);
	}

	static void assignRandWeightsIntNeg(Graph g) {
		int m = g.edges().size();
		int maxWeight = m < 50 ? 100 : m * 2 + 2;
		assignRandWeightsInt(g, -maxWeight / 8, maxWeight);
	}

	static void assignRandWeightsInt(Graph g, int minWeight, int maxWeight) {
		if (minWeight >= maxWeight)
			throw new IllegalArgumentException();
		if (maxWeight - minWeight < g.edges().size() / 2)
			throw new IllegalArgumentException("weight range is too small for unique weights");

		RandomIntUnique rand = new RandomIntUnique(minWeight, maxWeight, nextRandSeed());
		Weights.Int weight = g.addEdgesWeight("weight").ofInts();
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			weight.set(e, rand.next());
		}
	}

	static Graph randGraph(int n, int m) {
		return randGraph(n, m, GraphImplTestUtils.GRAPH_IMPL_DEFAULT);
	}

	static UGraph randGraph(int n, int m, GraphImpl graphImpl) {
		return (UGraph) new RandomGraphBuilder().n(n).m(m).directed(false).doubleEdges(false).selfEdges(false)
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
