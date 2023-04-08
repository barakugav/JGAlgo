package com.jgalgo;

import java.util.Arrays;
import java.util.Collections;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntStack;

public class Graphs {

	private Graphs() {
	}

	/**
	 * Find a valid path from u to v.
	 *
	 * <p>
	 * This function uses BFS, which will result in the shortest path in the number
	 * of edges.
	 *
	 * @param g a graph
	 * @param u source vertex
	 * @param v target vertex
	 * @return list of edges that represent a valid path from u to v, null if path
	 *         not found
	 */
	public static Path findPath(Graph g, final int u, final int v) {
		if (u == v)
			return new Path(g, u, v, IntLists.emptyList());
		boolean reverse = true;
		int u0 = u, v0 = v;
		if (g instanceof UGraph) {
			u0 = v;
			v0 = u;
			reverse = false;
		}
		int n = g.vertices().size();
		int[] backtrack = new int[n];
		Arrays.fill(backtrack, -1);

		IntArrayList path = new IntArrayList();
		for (BFSIter it = new BFSIter(g, u0); it.hasNext();) {
			int p = it.nextInt();
			backtrack[p] = it.inEdge();
			if (p == v0)
				break;
		}

		if (backtrack[v0] == -1)
			return null;

		for (int p = v0; p != u0;) {
			int e = backtrack[p];
			path.add(e);
			p = g.edgeEndpoint(e, p);
		}

		if (reverse)
			IntArrays.reverse(path.elements(), 0, path.size());
		return new Path(g, u, v, path);
	}

	public static boolean isTree(UGraph g) {
		IntIterator vIter = g.vertices().iterator();
		return !vIter.hasNext() ? true : isTree(g, vIter.nextInt());
	}

	public static boolean isTree(Graph g, int root) {
		return isForest(g, new int[] { root });
	}

	public static boolean isForest(Graph g) {
		int n = g.vertices().size();
		int[] roots = new int[n];
		for (int u = 0; u < n; u++)
			roots[u] = u;
		return isForest(g, roots, true);
	}

	public static boolean isForest(Graph g, int[] roots) {
		return isForest(g, roots, false);
	}

	private static boolean isForest(Graph g, int[] roots, boolean allowVisitedRoot) {
		int n = g.vertices().size();
		if (n == 0)
			return true;
		boolean directed = g instanceof DiGraph;

		boolean[] visited = new boolean[n];
		int[] parent = new int[n];
		Arrays.fill(parent, -1);

		IntStack stack = new IntArrayList();
		int visitedCount = 0;

		for (int i = 0; i < roots.length; i++) {
			int root = roots[i];
			if (visited[root]) {
				if (allowVisitedRoot)
					continue;
				return false;
			}

			stack.push(root);
			visited[root] = true;

			while (!stack.isEmpty()) {
				int u = stack.popInt();
				visitedCount++;

				for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
					eit.nextInt();
					int v = eit.v();
					if (!directed && v == parent[u])
						continue;
					if (visited[v])
						return false;
					visited[v] = true;
					stack.push(v);
					parent[v] = u;
				}
			}
		}

		return visitedCount == n;
	}

	public static int[] calcTopologicalSortingDAG(DiGraph g) {
		int n = g.vertices().size();
		int[] inDegree = new int[n];
		IntPriorityQueue queue = new IntArrayFIFOQueue();
		int[] topolSort = new int[n];
		int topolSortSize = 0;

		// calc in degree of all vertices
		for (int v = 0; v < n; v++)
			inDegree[v] = g.degreeIn(v);

		// Find vertices with zero in degree and insert them to the queue
		for (int v = 0; v < n; v++)
			if (inDegree[v] == 0)
				queue.enqueue(v);

		// Poll vertices from the queue and "remove" each one from the tree and add new
		// zero in degree vertices to the queue
		while (!queue.isEmpty()) {
			int u = queue.dequeueInt();
			topolSort[topolSortSize++] = u;
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				eit.nextInt();
				int v = eit.v();
				if (--inDegree[v] == 0)
					queue.enqueue(v);
			}
		}

		if (topolSortSize != n)
			throw new IllegalArgumentException("G is not a directed acyclic graph (DAG)");

		return topolSort;
	}

	static int getFullyBranchingTreeDepth(Graph t, int root) {
		for (int parent = -1, u = root, depth = 0;; depth++) {
			int v = parent;
			for (EdgeIter eit = t.edgesOut(u); eit.hasNext();) {
				eit.nextInt();
				v = eit.v();
				if (v != parent)
					break;
			}
			if (v == parent)
				return depth;
			parent = u;
			u = v;
		}
	}

	static int[] calcDegree(UGraph g, IntCollection edges) {
		int[] degree = new int[g.vertices().size()];
		for (IntIterator eit = edges.iterator(); eit.hasNext();) {
			int e = eit.nextInt();
			degree[g.edgeSource(e)]++;
			degree[g.edgeTarget(e)]++;
		}
		return degree;
	}

	static String formatAdjacencyMatrix(Graph g) {
		return formatAdjacencyMatrix(g, e -> e == -1 ? "0" : "1");
	}

	static String formatAdjacencyMatrixWeighted(Graph g, EdgeWeightFunc w) {
		double minWeight = Double.MAX_VALUE;
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			double ew = w.weight(e);
			if (ew < minWeight)
				minWeight = ew;
		}

		int unlimitedPrecision = 64;
		int precision = minWeight >= 1 ? 2 : Math.min(unlimitedPrecision, (int) -Math.log10(minWeight));

		return precision == unlimitedPrecision
				? formatAdjacencyMatrix(g, e -> e == -1 ? "-" : Double.toString(w.weight(e)))
				: formatAdjacencyMatrix(g,
						e -> e == -1 ? "-" : String.format("%." + precision + "f", Double.valueOf(w.weight(e))));
	}

	static String formatAdjacencyMatrixWeightedInt(Graph g, EdgeWeightFunc.Int w) {
		return formatAdjacencyMatrix(g, e -> e == -1 ? "-" : Integer.toString(w.weightInt(e)));
	}

	static String formatAdjacencyMatrix(Graph g, Int2ObjectFunction<String> formatter) {
		int n = g.vertices().size();
		if (n == 0)
			return "[]";

		/* format all edges */
		String[][] strs = new String[n][n];
		for (int u = 0; u < n; u++) {
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				int e = eit.nextInt();
				strs[u][eit.v()] = formatter.apply(e);
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
		int vertexLabelCellSize = String.valueOf(n - 1).length() + 1;
		int cellSize = Math.max(maxStr + 1, vertexLabelCellSize);

		/* format header row */
		StringBuilder s = new StringBuilder();
		s.append(strMult(" ", vertexLabelCellSize));
		for (int v = 0; v < n; v++)
			s.append(String.format("% " + cellSize + "d", Integer.valueOf(v)));
		s.append('\n');

		/* format adjacency matrix */
		for (int u = 0; u < n; u++) {
			s.append(String.format("% " + vertexLabelCellSize + "d", Integer.valueOf(u)));
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

	static DiGraph referenceGraph(DiGraph g, Object refEdgeWeightKey) {
		DiGraph g0 = new GraphArrayDirected(g.vertices().size());
		Weights.Int edgeRef = g0.addEdgesWeight(refEdgeWeightKey).ofInts();
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int e0 = g0.addEdge(g.edgeSource(e), g.edgeTarget(e));
			edgeRef.set(e0, e);
		}
		return g0;
	}

	static UGraph referenceGraph(UGraph g, Object refEdgeWeightKey) {
		UGraph g0 = new GraphArrayUndirected(g.vertices().size());
		Weights.Int edgeRef = g0.addEdgesWeight(refEdgeWeightKey).ofInts();
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int e0 = g0.addEdge(g.edgeSource(e), g.edgeTarget(e));
			edgeRef.set(e0, e);
		}
		return g0;
	}

	@SuppressWarnings("unchecked")
	static UGraph subGraph(UGraph g, IntCollection edgeSet) {
		final Object edgeMappingKey = new Object();
		UGraph g1 = new GraphArrayUndirected(g.vertices().size());

		Weights.Int sub2Edge = g1.addEdgesWeight(edgeMappingKey).ofInts();
		for (IntIterator it = edgeSet.iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			int s = g1.addEdge(u, v);
			sub2Edge.set(s, e);
		}
		for (Object key : g.getEdgesWeightsKeys()) {
			Weights<?> data0 = g.edgesWeight(key);

			if (data0 instanceof Weights.Int) {
				Weights.Int data = (Weights.Int) data0;
				int defVal = data.defaultValInt();
				Weights.Int datas = g1.addEdgesWeight(key).defVal(defVal).ofInts();
				for (IntIterator it = g1.edges().iterator(); it.hasNext();) {
					int s = it.nextInt();
					int w = data.getInt(sub2Edge.getInt(s));
					if (w != defVal)
						datas.set(s, w);
				}

			} else if (data0 instanceof Weights.Double) {
				Weights.Double data = (Weights.Double) data0;
				double defVal = data.defaultValDouble();
				Weights.Double datas = g1.addEdgesWeight(key).defVal(defVal).ofDoubles();
				for (IntIterator it = g1.edges().iterator(); it.hasNext();) {
					int s = it.nextInt();
					double w = data.getDouble(sub2Edge.getInt(s));
					if (w != defVal)
						datas.set(s, w);
				}

			} else {
				Object defVal = data0.defaultVal();
				@SuppressWarnings("rawtypes")
				Weights datas = g1.addEdgesWeight(key).defVal(defVal).ofObjs();
				for (IntIterator it = g1.edges().iterator(); it.hasNext();) {
					int s = it.nextInt();
					Object w = data0.get(sub2Edge.getInt(s));
					if (w != defVal)
						datas.set(s, w);
				}
			}
		}
		g1.removeEdgesWeights(edgeMappingKey);
		return g1;
	}

	static boolean containsSelfLoops(Graph g) {
		if (!g.getCapabilities().selfEdges())
			return false;
		int n = g.vertices().size();
		for (int u = 0; u < n; u++) {
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				eit.nextInt();
				if (u == eit.v())
					return true;
			}
		}
		return false;
	}

	static boolean containsParallelEdges(Graph g) {
		if (!g.getCapabilities().parallelEdges())
			return false;
		int n = g.vertices().size();
		int[] lastVisit = new int[n];
		for (int u = 0; u < n; u++) {
			final int visitIdx = u + 1;
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				eit.nextInt();
				int v = eit.v();
				if (lastVisit[v] == visitIdx)
					return true;
				lastVisit[v] = visitIdx;
			}
		}
		return false;
	}

}
