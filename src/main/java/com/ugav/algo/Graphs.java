package com.ugav.algo;

import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;
import java.util.function.Function;

import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Graph.WeightFunctionInt;

public class Graphs {

	private Graphs() {
		throw new InternalError();
	}

	public static <E> boolean isTree(Graph<E> graph) {
		int n = graph.vertices();

		boolean visited[] = new boolean[n];
		Arrays.fill(visited, false);

		int[] layer = new int[n];
		int[] nextLayer = new int[n];
		int layerSize = 0;
		int lextLayerSize = 0;

		layer[layerSize++] = 0;

		int[] edges = new int[n];
		int edgesCount;

		while (layerSize > 0) {
			for (; layerSize > 0; layerSize--) {
				int v = layer[layerSize - 1];
				edgesCount = graph.edges(v, edges, 0);

				for (int i = 0; i < edgesCount; i++) {
					int u = edges[i];
					if (visited[u])
						return false;
					visited[u] = true;
					nextLayer[lextLayerSize++] = u;
				}
			}

			layerSize = lextLayerSize;
			lextLayerSize = 0;
			int temp[] = layer;
			layer = nextLayer;
			nextLayer = temp;
		}

		for (int v = 0; v < n; v++)
			if (!visited[v])
				return false;
		return true;
	}

	public static <E> String formatAdjacencyMatrix(Graph<E> g) {
		return formatAdjacencyMatrix(g, e -> e == null ? "0" : "1");
	}

	public static <E> String formatAdjacencyMatrixWeighted(Graph<E> g, WeightFunction<E> w) {
		double minWeight = Double.MAX_VALUE;
		for (Iterator<Edge<E>> it = g.edges(); it.hasNext();) {
			double ew = w.weight(it.next());
			if (ew < minWeight)
				minWeight = ew;
		}

		int unlimitedPrecision = 64;
		int precision = minWeight > 1 ? 2 : Math.min(unlimitedPrecision, (int) -Math.log10(minWeight));

		return precision == unlimitedPrecision
				? formatAdjacencyMatrix(g, e -> e == null ? "" : Double.toString(w.weight(e)))
				: formatAdjacencyMatrix(g, e -> e == null ? "" : String.format("%." + precision + "f", w.weight(e)));
	}

	public static <E> String formatAdjacencyMatrixWeightedInt(Graph<E> g, WeightFunctionInt<E> w) {
		return formatAdjacencyMatrix(g, e -> e == null ? "" : Integer.toString(w.weightInt(e)));
	}

	public static <E> String formatAdjacencyMatrix(Graph<E> g, Function<Graph.Edge<E>, String> formatter) {
		int n = g.vertices();
		if (n == 0)
			return "[]";

		/* format all edges */
		String[][] strs = new String[n][n];
		for (int u = 0; u < n; u++) {
			for (Iterator<Edge<E>> it = g.edges(u); it.hasNext();) {
				Edge<E> e = it.next();
				strs[u][e.v()] = formatter.apply(e);
			}
		}

		/* calculate cell size */
		int maxStr = 0;
		for (int u = 0; u < n; u++) {
			for (int v = 0; v < n; v++) {
				if (strs[u][v] == null)
					strs[u][v] = formatter.apply(null);
				if (strs[u][v].length() > maxStr)
					maxStr = strs[u][v].length();
			}
		}
		int cellSize = maxStr + 1;
		int cellSizeFirstColumn = String.valueOf(n - 1).length();

		/* format header row */
		StringBuilder s = new StringBuilder();
		s.append(strMult(" ", cellSizeFirstColumn));
		for (int v = 0; v < n; v++)
			s.append(String.format("% " + cellSize + "d", v));
		s.append('\n');

		/* format adjacency matrix */
		for (int u = 0; u < n; u++) {
			s.append(String.format("% " + cellSizeFirstColumn + "d", u));
			for (int v = 0; v < n; v++) {
				if (strs[u][v].length() < cellSize)
					s.append(strMult(" ", cellSize - strs[u][v].length()));
				s.append(strs[u][v]);
			}
			s.append('\n');
		}

		return s.toString();
	}

	private static String strMult(String s, int n) {
		return String.join("", Collections.nCopies(n, s));
	}

	public static final WeightFunction<Double> WEIGHT_FUNC_DEFAULT = Edge::val;
	public static final WeightFunctionInt<Integer> WEIGHT_INT_FUNC_DEFAULT = Edge::val;

	static class EdgeWeightComparator<E> implements Comparator<Edge<E>> {

		private final Graph.WeightFunction<E> w;

		EdgeWeightComparator(Graph.WeightFunction<E> w) {
			this.w = Objects.requireNonNull(w);
		}

		@Override
		public int compare(Edge<E> e1, Edge<E> e2) {
			return Double.compare(w.weight(e1), w.weight(e2));
		}

	}

	static class EdgeWeightIntComparator<E> implements Comparator<Edge<E>> {

		private final Graph.WeightFunctionInt<E> w;

		EdgeWeightIntComparator(Graph.WeightFunctionInt<E> w) {
			this.w = Objects.requireNonNull(w);
		}

		@Override
		public int compare(Edge<E> e1, Edge<E> e2) {
			return Integer.compare(w.weightInt(e1), w.weightInt(e2));
		}

	}

}
