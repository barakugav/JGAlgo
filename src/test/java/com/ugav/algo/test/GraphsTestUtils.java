package com.ugav.algo.test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import com.ugav.algo.Graph;
import com.ugav.algo.Graph.DirectedType;
import com.ugav.algo.Graph.Edge;
import com.ugav.algo.GraphArray;
import com.ugav.algo.Tuple;
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

		RandomUnique(int minWeight, int maxWeight) {
			rand = new Random();
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
		private int m;
		private boolean directed;
		private boolean doubleEdges;
		private boolean selfEdges;
		private boolean cycles;
		private boolean connected;

		RandomGraphBuilder() {
			n = 0;
			m = 0;
			doubleEdges = false;
			selfEdges = false;
			cycles = false;
			connected = false;
		}

		RandomGraphBuilder n(int n) {
			this.n = n;
			return this;
		}

		RandomGraphBuilder m(int m) {
			this.m = m;
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
			if (n < 0 || m < 0)
				throw new IllegalStateException();
			if ((!cycles && connected) && m != n - 1)
				throw new IllegalStateException();
			if (!doubleEdges && m >= n * n / 3)
				throw new IllegalArgumentException("too much edges for random sampling");

			Graph.Modifiable<E> g = new GraphArray<>(directed ? DirectedType.Directed : DirectedType.Undirected, n);
			Set<Tuple<Integer, Integer>> existingEdges = new HashSet<>();
			UnionFind uf = UnionFindImpl.getInstance();
			@SuppressWarnings("unchecked")
			UnionFind.Element<Void>[] ufs = new UnionFind.Element[n];
			int componentsNum = n;
			Random rand = new Random();

			for (int i = 0; i < n; i++)
				ufs[i] = uf.make(null);

			while ((connected && componentsNum > 1) || g.edgesNum() < m) {
				int u = rand.nextInt(n);
				int v = rand.nextInt(n);

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
					Tuple<Integer, Integer> et = new Tuple<>(ut, vt);
					if (!existingEdges.add(et))
						continue;
				}

				// keep track of number of connectivity components
				if (!cycles || connected) {
					UnionFind.Element<Void> uElm = uf.find(ufs[u]);
					UnionFind.Element<Void> vElm = uf.find(ufs[v]);

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

	static void assignRandWeights(Graph<Double> g) {
		assignRandWeights(g, 1.0, 100.0);
	}

	static void assignRandWeights(Graph<Double> g, double minWeight, double maxWeight) {
		if (minWeight >= maxWeight)
			throw new IllegalArgumentException();

		Random rand = new Random();
		for (Iterator<Edge<Double>> it = g.edges(); it.hasNext();)
			it.next().val(rand.nextDouble(minWeight, maxWeight));
	}

	static void assignRandWeightsInt(Graph<Integer> g) {
		int m = g.edgesNum();
		int minWeight = 1;
		int maxWeight = m < 50 ? 100 : m * 2 + 2;
		assignRandWeightsInt(g, minWeight, maxWeight);
	}

	static void assignRandWeightsInt(Graph<Integer> g, int minWeight, int maxWeight) {
		if (minWeight >= maxWeight)
			throw new IllegalArgumentException();
		if (maxWeight - minWeight < g.edgesNum() / 2)
			throw new IllegalArgumentException("weight range is too small for unique weights");

		RandomUnique rand = new RandomUnique(minWeight, maxWeight);
		for (Iterator<Edge<Integer>> it = g.edges(); it.hasNext();)
			it.next().val(rand.next());
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
		Graph.Modifiable<Integer> g = new GraphArray<>(directed, n);
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
		Graph.Modifiable<Void> g = new GraphArray<>(DirectedType.Undirected, n);
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
		Graph.Modifiable<Void> g = new GraphArray<>(DirectedType.Undirected, n);
		for (int u = 0; u < n; u++) {
			String[] chars = lines[u].split(" ");
			for (int v = u + 1; v < n; v++)
				if (chars[v].equals("1"))
					g.addEdge(u, v);
		}
		return g;
	}

}
