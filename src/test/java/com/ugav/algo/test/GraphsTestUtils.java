package com.ugav.algo.test;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import com.ugav.algo.Graph;
import com.ugav.algo.Graph.DirectedType;
import com.ugav.algo.Graph.Edge;
import com.ugav.algo.GraphArray;
import com.ugav.algo.GraphBipartiteArray;
import com.ugav.algo.Pair;
import com.ugav.algo.UnionFind;
import com.ugav.algo.UnionFindImpl;

class GraphsTestUtils {

	private GraphsTestUtils() {
		throw new InternalError();
	}

	private static class RandomUnique {

		private final Random rand;
		private final int minWeight;
		private final int maxWeight;
		private final Set<Integer> usedWeights;

		RandomUnique(int minWeight, int maxWeight, long seed) {
			rand = new Random(seed);
			this.minWeight = minWeight;
			this.maxWeight = maxWeight;
			usedWeights = new HashSet<>();
		}

		int next() {
			int w;
			do {
				w = rand.nextInt(minWeight, maxWeight);
			} while (usedWeights.contains(w));

			usedWeights.add(w);

			return w;
		}
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

		<E> Graph<E> build() {
			Graph<E> g;
			DirectedType directedType = directed ? DirectedType.Directed : DirectedType.Undirected;
			if (!bipartite) {
				if (n < 0 || m < 0)
					throw new IllegalStateException();
				g = new GraphArray<>(directedType, n);
			} else {
				if (sn < 0 || tn < 0)
					throw new IllegalStateException();
				if ((sn == 0 || tn == 0) && m != 0)
					throw new IllegalStateException();
				n = sn + tn;
				g = new GraphBipartiteArray<>(directedType, sn, tn);
			}
			if (n == 0)
				return g;
			if (!cycles && m >= n)
				throw new IllegalStateException();
			if (!doubleEdges && m >= ((long) n) * n / 3)
				throw new IllegalArgumentException("too much edges for random sampling");

			Set<Pair<Integer, Integer>> existingEdges = new HashSet<>();
			UnionFind uf = UnionFindImpl.getInstance();
			@SuppressWarnings("unchecked")
			UnionFind.Elm<Void>[] ufs = new UnionFind.Elm[n];
			int componentsNum = n;
			Random rand = new Random(TestUtils.nextRandSeed());

			for (int i = 0; i < n; i++)
				ufs[i] = uf.make(null);

			while ((connected && componentsNum > 1) || g.edges().size() < m) {
				int u, v;

				if (!bipartite) {
					u = rand.nextInt(n);
					v = rand.nextInt(n);
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
					Pair<Integer, Integer> et = Pair.valueOf(ut, vt);
					if (!existingEdges.add(et))
						continue;
				}

				// keep track of number of connectivity components
				if (!cycles || connected) {
					UnionFind.Elm<Void> uElm = uf.find(ufs[u]);
					UnionFind.Elm<Void> vElm = uf.find(ufs[v]);

					// avoid cycles
					if (!cycles && uElm == vElm)
						continue;

					if (uElm != vElm)
						componentsNum--;
					uf.union(uElm, vElm);
				}

				g.addEdge(u, v);
			}

			return g;
		}

	}

	static <E> Graph<E> randTree(int n) {
		return new RandomGraphBuilder().n(n).m(n - 1).directed(false).doubleEdges(false).selfEdges(false).cycles(false)
				.connected(true).build();
	}

	static <E> Graph<E> randForest(int n, int m) {
		return new RandomGraphBuilder().n(n).m(m).directed(false).doubleEdges(false).selfEdges(false).cycles(false)
				.connected(false).build();
	}

	static void assignRandWeights(Graph<Double> g) {
		assignRandWeights(g, 1.0, 100.0);
	}

	static void assignRandWeights(Graph<Double> g, double minWeight, double maxWeight) {
		if (minWeight >= maxWeight)
			throw new IllegalArgumentException();

		Random rand = new Random(TestUtils.nextRandSeed());
		for (Edge<Double> e : g.edges())
			e.val(rand.nextDouble(minWeight, maxWeight));
	}

	static void assignRandWeightsInt(Graph<Integer> g) {
		int m = g.edges().size();
		int minWeight = 1;
		int maxWeight = m < 50 ? 100 : m * 2 + 2;
		assignRandWeightsInt(g, minWeight, maxWeight);
	}

	static void assignRandWeightsInt(Graph<Integer> g, int minWeight, int maxWeight) {
		if (minWeight >= maxWeight)
			throw new IllegalArgumentException();
		if (maxWeight - minWeight < g.edges().size() / 2)
			throw new IllegalArgumentException("weight range is too small for unique weights");

		RandomUnique rand = new RandomUnique(minWeight, maxWeight, TestUtils.nextRandSeed());
		for (Edge<Integer> e : g.edges())
			e.val(rand.next());
	}

	static <E> Graph<E> randGraph(int n, int m) {
		return randGraph(n, m, false);
	}

	static <E> Graph<E> randGraph(int n, int m, boolean selfEdges) {
		return new RandomGraphBuilder().n(n).m(m).directed(false).doubleEdges(false).selfEdges(selfEdges).cycles(true)
				.connected(false).build();
	}

	static Graph<Integer> createGraphFromAdjacencyMatrixWeightedInt(int[][] m, DirectedType directed) {
		int n = m.length;
		Graph<Integer> g = new GraphArray<>(directed, n);
		for (int u = 0; u < n; u++) {
			for (int v = directed == DirectedType.Directed ? 0 : u + 1; v < n; v++) {
				if (m[u][v] == 0)
					continue;
				g.addEdge(u, v).val(m[u][v]);
			}
		}
		return g;
	}

	static Graph<Integer> parseGraphFromAdjacencyMatrixWeightedInt(String s) {
		String[] lines = s.split("\r\n");
		int n = lines.length;
		int[][] m = new int[n][n];
		for (int u = 0; u < n; u++) {
			String[] esStr = lines[u].split(",");
			for (int v = u + 1; v < n; v++)
				Integer.parseInt(esStr[v].trim());
		}
		return createGraphFromAdjacencyMatrixWeightedInt(m, DirectedType.Undirected);
	}

	static Graph<Void> parseGraphFromAdjacencyMatrix01(String s) {
		String[] lines = s.split("\r\n");
		int n = lines.length;
		Graph<Void> g = new GraphArray<>(DirectedType.Undirected, n);
		for (int u = 0; u < n; u++) {
			String[] chars = lines[u].split(" ");
			for (int v = u + 1; v < n; v++)
				if (chars[v].equals("1"))
					g.addEdge(u, v);
		}
		return g;
	}

	static Graph<Void> parseGraphWeighted(String s) {
		String[] lines = s.split("\r\n");
		int n = lines.length;
		Graph<Void> g = new GraphArray<>(DirectedType.Undirected, n);
		for (int u = 0; u < n; u++) {
			String[] chars = lines[u].split(" ");
			for (int v = u + 1; v < n; v++)
				if (chars[v].equals("1"))
					g.addEdge(u, v);
		}
		return g;
	}

}
