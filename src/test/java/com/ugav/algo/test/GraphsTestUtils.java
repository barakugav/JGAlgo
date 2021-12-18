package com.ugav.algo.test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import com.ugav.algo.Graph;
import com.ugav.algo.Graph.DirectedType;
import com.ugav.algo.Graph.Edge;
import com.ugav.algo.GraphArray;

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

	static <E> Graph<E> randTree(int n) {
		Graph.Modifiable<E> g = new GraphArray<>(DirectedType.Undirected, n);
		Random rand = new Random();
		for (int i = 0; i < n - 1; i++) {
			int u = rand.nextInt(i + 1);
			int v = i + 1;
			g.addEdge(u, v);
		}
		return g;
	}

	static void assignRandWeights(Graph<Integer> g, int minWeight, int maxWeight) {
		if (minWeight >= maxWeight)
			throw new IllegalArgumentException();
		if (maxWeight - minWeight < g.edgesNum() / 2)
			throw new IllegalArgumentException("weight range is too small for unique weights");

		RandomUnique rand = new RandomUnique(minWeight, maxWeight);
		for (Iterator<Edge<Integer>> it = g.edges(); it.hasNext();)
			it.next().val(rand.next());
	}

	static Graph<Integer> randTreeWeighted(int n) {
		if (n < 1)
			throw new IllegalArgumentException();
		int m = n - 1;
		int minWeight = 1;
		int maxWeight = m < 50 ? 100 : m * 2 + 2;
		return randTreeWeighted(n, minWeight, maxWeight);
	}

	static Graph<Integer> randTreeWeighted(int n, int minWeight, int maxWeight) {
		Graph<Integer> t = randTree(n);
		assignRandWeights(t, minWeight, maxWeight);
		return t;
	}

	static <E> Graph<E> randGraph(int n, int m) {
		return randGraph(n, m, false);
	}

	static <E> Graph<E> randGraph(int n, int m, boolean selfEdges) {
		Graph.Modifiable<E> g = new GraphArray<>(DirectedType.Undirected, n);
		if (m >= n * n / 3)
			throw new IllegalArgumentException("too much edges for random sampling");

		int[] edges = new int[n];

		Random rand = new Random();
		mainLoop: for (int i = 0; i < m;) {
			int u = rand.nextInt(n);
			int v = rand.nextInt(n);
			if (!selfEdges && u == v)
				continue;

			int edgeCount = g.getEdgesArrVs(u, edges, 0);
			for (int j = 0; j < edgeCount; j++)
				if (edges[j] == v)
					continue mainLoop;
			g.addEdge(u, v);
			i++;
		}
		return g;
	}

	static Graph<Integer> randGraphWeightedInt(int n, int m) {
		int minWeight = 1;
		int maxWeight = m < 50 ? 100 : m * 2 + 2;
		return randGraphWeightedInt(n, m, minWeight, maxWeight);
	}

	static Graph<Integer> randGraphWeightedInt(int n, int m, int minWeight, int maxWeight) {
		Graph<Integer> g = randGraph(n, m);
		assignRandWeights(g, minWeight, maxWeight);
		return g;
	}

	static Graph<Double> randGraphWeighted(int n, int m) {
		return randGraphWeighted(n, m, 0.1, 100);
	}

	static Graph<Double> randGraphWeighted(int n, int m, double minWeight, double maxWeight) {
		Graph<Double> g = randGraph(n, m);
		Random rand = new Random();

		for (Iterator<Edge<Double>> it = g.edges(); it.hasNext();) {
			Edge<Double> e = it.next();
			e.val(rand.nextDouble(minWeight, maxWeight));
		}
		return g;
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
