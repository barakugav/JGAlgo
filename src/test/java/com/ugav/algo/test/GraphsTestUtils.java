package com.ugav.algo.test;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;
import java.util.Set;

import com.ugav.algo.Graph;
import com.ugav.algo.Graph.Edge;
import com.ugav.algo.GraphArray;

class GraphsTestUtils {

	private GraphsTestUtils() {
		throw new InternalError();
	}

	static <E> Graph<E> randTree(int n) {
//		Graph.Modifiable<E> g = GraphLinked.builder().setVertexNum(n).build();
		Graph.Modifiable<E> g = new GraphArray<>(false, n);
		Random rand = new Random();
		for (int i = 0; i < n - 1; i++) {
			int u = rand.nextInt(i + 1);
			int v = i + 1;
			g.addEdge(u, v);
		}
		return g;
	}

	static <E> Graph<E> randGraph(int n, int m) {
		return randGraph(n, m, false);
	}

	static <E> Graph<E> randGraph(int n, int m, boolean selfEdges) {
//		Graph.Modifiable<E> g = GraphLinked.builder().setDirected(false).setVertexNum(n).build();
		Graph.Modifiable<E> g = new GraphArray<>(false, n);
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
		if (minWeight >= maxWeight)
			throw new IllegalArgumentException();
		if (maxWeight - minWeight < m * 2)
			throw new IllegalArgumentException("weight range is too small for unique weights");

		Graph<Integer> g = randGraph(n, m);
		Random rand = new Random();
		Set<Integer> weights = new HashSet<>(m);

		for (Iterator<Edge<Integer>> it = g.edges(); it.hasNext();) {
			/* random unique weight */
			int w;
			do {
				w = rand.nextInt(minWeight, maxWeight);
			} while (weights.contains(w));

			Edge<Integer> e = it.next();
			e.val(w);
			weights.add(w);
		}
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

	static Graph<Integer> createGraphFromAdjacencyMatrixWeightedInt(int[][] m, boolean directed) {
		int n = m.length;
//		Graph.Modifiable<Integer> g = GraphLinked.builder().setDirected(directed).setVertexNum(n).build();
		Graph.Modifiable<Integer> g = new GraphArray<>(directed, n);
		for (int u = 0; u < n; u++) {
			for (int v = directed ? 0 : u + 1; v < n; v++) {
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
		return createGraphFromAdjacencyMatrixWeightedInt(m, false);
	}

	static Graph<Void> parseGraphFromAdjacencyMatrix01(String s) {
		String[] lines = s.split("\r\n");
		int n = lines.length;
//		Graph.Modifiable<Void> g = GraphLinked.builder().setDirected(false).setVertexNum(n).build();
		Graph.Modifiable<Void> g = new GraphArray<>(false, n);
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
//		Graph.Modifiable<Void> g = GraphLinked.builder().setDirected(false).setVertexNum(n).build();
		Graph.Modifiable<Void> g = new GraphArray<>(false, n);
		for (int u = 0; u < n; u++) {
			String[] chars = lines[u].split(" ");
			for (int v = u + 1; v < n; v++)
				if (chars[v].equals("1"))
					g.addEdge(u, v);
		}
		return g;
	}

}
