package com.ugav.algo.test;

import java.util.Iterator;
import java.util.Random;

import com.ugav.algo.Graph;
import com.ugav.algo.Graph.Edge;
import com.ugav.algo.GraphLinked;

class GraphsTestUtils {

	static <E> Graph<E> randTree(int n) {
		Graph.Modifiable<E> g = GraphLinked.builder().setVertexNum(n).build();
		Random rand = new Random();
		for (int i = 0; i < n - 1; i++) {
			int u = rand.nextInt(i + 1);
			int v = i + 1;
			g.addEdge(u, v);
		}
		return g;
	}

	static <E> Graph<E> randGraph(int n, int m) {
		Graph.Modifiable<E> g = GraphLinked.builder().setDirected(false).setVertexNum(n).build();
		if (m >= n * n / 3)
			throw new IllegalArgumentException("too much edges for random sampling");

		int[] edges = new int[n];

		Random rand = new Random();
		mainLoop: for (int i = 0; i < m;) {
			int u = rand.nextInt(n);
			int v = rand.nextInt(n);

			int edgeCount = g.edges(u, edges, 0);
			for (int j = 0; j < edgeCount; j++)
				if (edges[j] == v)
					continue mainLoop;
			g.addEdge(u, v);
			i++;
		}
		return g;
	}

	static Graph<Integer> randGraphWeightedInt(int n, int m) {
		return randGraphWeightedInt(n, m, 1, 100);
	}

	static Graph<Integer> randGraphWeightedInt(int n, int m, int minWeight, int maxWeight) {
		Graph<Integer> g = randGraph(n, m);
		Random rand = new Random();

		for (Iterator<Edge<Integer>> it = g.edges(); it.hasNext();) {
			Edge<Integer> e = it.next();
			e.val(rand.nextInt(minWeight, maxWeight));
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

	static Graph<Integer> createGraphFromAdjacencyMatrixWeightedInt(int[][] m) {
		int n = m.length;
		Graph.Modifiable<Integer> g = GraphLinked.builder().setDirected(false).setVertexNum(n).build();
		for (int u = 0; u < n; u++) {
			for (int v = u + 1; v < n; v++) {
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
		return createGraphFromAdjacencyMatrixWeightedInt(m);
	}

	static Graph<Void> parseGraphFromAdjacencyMatrix01(String s) {
		String[] lines = s.split("\r\n");
		int n = lines.length;
		Graph.Modifiable<Void> g = GraphLinked.builder().setDirected(false).setVertexNum(n).build();
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
		Graph.Modifiable<Void> g = GraphLinked.builder().setDirected(false).setVertexNum(n).build();
		for (int u = 0; u < n; u++) {
			String[] chars = lines[u].split(" ");
			for (int v = u + 1; v < n; v++)
				if (chars[v].equals("1"))
					g.addEdge(u, v);
		}
		return g;
	}

}
