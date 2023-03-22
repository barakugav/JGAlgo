package com.ugav.algo;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;

import com.ugav.algo.SSSP.SSSPResultsImpl;
import com.ugav.algo.Utils.QueueIntFixSize;
import com.ugav.algo.Utils.StackIntFixSize;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntComparator;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

public class Graphs {

	private Graphs() {
	}

	@FunctionalInterface
	public static interface BFSOperator {

		/**
		 * Perform some operation on a vertex during a BFS traversy
		 *
		 * @param v a vertex
		 * @param e the edge on which the BFS algorithm reached the vertex. might be
		 *          null for the source vertices
		 * @return true if the BFS should continue
		 */
		public boolean handleVertex(int v, int e);

	}

	/**
	 * Perform a BFS traversy on a graph
	 *
	 * @param g      a graph
	 * @param source s source vertex
	 * @param op     user operation to operate on the reachable vertices
	 */
	public static void runBFS(Graph g, int source, BFSOperator op) {
		runBFS(g, new int[] { source }, op);
	}

	public static void runBFS(Graph g, int[] sources, BFSOperator op) {
		int n = g.verticesNum();
		boolean[] visited = new boolean[n];

		QueueIntFixSize queue = new QueueIntFixSize(n);

		for (int source : sources) {
			visited[source] = true;
			queue.push(source);
			if (!op.handleVertex(source, -1))
				return;
		}

		while (!queue.isEmpty()) {
			int u = queue.pop();

			for (EdgeIter eit = g.edges(u); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.v();
				if (visited[v])
					continue;
				visited[v] = true;
				queue.push(v);

				if (!op.handleVertex(v, e))
					return;
			}
		}
	}

	@FunctionalInterface
	public static interface DFSOperator {

		/**
		 * Perform some operation on a vertex during a DFS traversy
		 *
		 * @param v              a vertex
		 * @param pathFromSource a list of the edges from the source to the current
		 *                       vertex
		 * @return true if the DFS should continue
		 */
		public boolean handleVertex(int v, IntList pathFromSource);

	}

	/**
	 * Perform a DFS traversy on a graph
	 *
	 * @param g      a graph
	 * @param source s source vertex
	 * @param op     user operation to operate on the reachable vertices
	 */
	public static void runDFS(Graph g, int source, DFSOperator op) {
		int n = g.verticesNum();
		boolean[] visited = new boolean[n];
		EdgeIter[] edges = new EdgeIter[n];
		IntList edgesFromSource = new IntArrayList();

		edges[0] = g.edges(source);
		visited[source] = true;
		if (!op.handleVertex(source, edgesFromSource))
			return;

		for (int depth = 0;;) {
			EdgeIter eit = edges[depth];
			if (eit.hasNext()) {
				int e = eit.nextInt();
				int v = eit.v();
				if (visited[v])
					continue;
				visited[v] = true;
				edgesFromSource.add(e);
				edges[++depth] = g.edges(v);

				if (!op.handleVertex(v, edgesFromSource))
					return;
			} else {
				edges[depth] = null;
				if (depth-- == 0)
					break;
				edgesFromSource.removeInt(edgesFromSource.size() - 1);
			}
		}
	}

	/**
	 * Find a valid path from u to v
	 *
	 * This function uses BFS, which will result in the shortest path in the number
	 * of edges
	 *
	 * @param g a graph
	 * @param u source vertex
	 * @param v target vertex
	 * @return list of edges that represent a valid path from u to v, null if path
	 *         not found
	 */
	public static IntList findPath(Graph g, int u, int v) {
		IntArrayList path = new IntArrayList();
		if (u == v)
			return path;
		boolean reverse = true;
		if (g instanceof UGraph) {
			int t = u;
			u = v;
			v = t;
			reverse = false;
		}
		int n = g.verticesNum();
		int[] backtrack = new int[n];
		Arrays.fill(backtrack, -1);

		int target = v;
		runBFS(g, u, (p, e) -> {
			backtrack[p] = e;
			return p != target;
		});

		if (backtrack[v] == -1)
			return null;

		for (int p = v; p != u;) {
			int e = backtrack[p];
			path.add(e);
			p = g.edgeEndpoint(e, p);
		}

		if (reverse)
			IntArrays.reverse(path.elements(), 0, path.size());
		return path;
	}

	public static boolean isTree(Graph g) {
		return isTree(g, 0);
	}

	public static boolean isTree(Graph g, int root) {
		return isForst(g, new int[] { root });
	}

	public static boolean isForst(Graph g) {
		int n = g.verticesNum();
		int[] roots = new int[n];
		for (int u = 0; u < n; u++)
			roots[u] = u;
		return isForst(g, roots, true);
	}

	public static boolean isForst(Graph g, int[] roots) {
		return isForst(g, roots, false);
	}

	private static boolean isForst(Graph g, int[] roots, boolean allowVisitedRoot) {
		int n = g.verticesNum();
		if (n == 0)
			return true;
		boolean directed = g instanceof DiGraph;

		boolean[] visited = new boolean[n];
		int[] parent = new int[n];
		Arrays.fill(parent, -1);

		StackIntFixSize stack = new StackIntFixSize(n);
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
				int u = stack.pop();
				visitedCount++;

				for (EdgeIter eit = g.edges(u); eit.hasNext();) {
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

	/**
	 * Find all connectivity components in the graph
	 *
	 * The connectivity components (CC) are groups of vertices where it's possible
	 * to reach each one from one another.
	 *
	 * This function support undirected graphs only
	 *
	 * @param g an undirected graph
	 * @return (CC number, [vertex]->[CC])
	 * @throws IllegalArgumentException if the graph is directed
	 */
	public static Pair<Integer, int[]> findConnectivityComponents(UGraph g) {
		int n = g.verticesNum();
		StackIntFixSize stack = new StackIntFixSize(n);

		int[] comp = new int[n];
		Arrays.fill(comp, -1);
		int compNum = 0;

		for (int r = 0; r < n; r++) {
			if (comp[r] != -1)
				continue;

			stack.push(r);
			comp[r] = compNum;

			while (!stack.isEmpty()) {
				int u = stack.pop();

				for (EdgeIter eit = g.edges(u); eit.hasNext();) {
					eit.nextInt();
					int v = eit.v();
					if (comp[v] != -1)
						continue;
					comp[v] = compNum;
					stack.push(v);
				}
			}
			compNum++;
		}
		return Pair.of(Integer.valueOf(compNum), comp);
	}

	/**
	 * Find all strong connectivity components
	 *
	 * The connectivity components (CC) are groups of vertices where it's possible
	 * to reach each one from one another.
	 *
	 * This function is specifically for directed graphs.
	 *
	 * @param g a directed graph
	 * @return (CC number, [vertex]->[CC])
	 */
	public static Pair<Integer, int[]> findStrongConnectivityComponents(DiGraph g) {
		int n = g.verticesNum();

		int[] comp = new int[n];
		Arrays.fill(comp, -1);
		int compNum = 0;

		int[] dfsPath = new int[n];
		EdgeIter[] edges = new EdgeIter[n];

		int[] c = new int[n];
		int[] s = new int[n];
		int[] p = new int[n];
		int cNext = 1, sSize = 0, pSize = 0;

		for (int r = 0; r < n; r++) {
			if (comp[r] != -1)
				continue;
			dfsPath[0] = r;
			edges[0] = g.edgesOut(r);
			c[r] = cNext++;
			s[sSize++] = p[pSize++] = r;

			dfs: for (int depth = 0;;) {
				for (EdgeIter eit = edges[depth]; eit.hasNext();) {
					eit.nextInt();
					int v = eit.v();
					if (c[v] == 0) {
						c[v] = cNext++;
						s[sSize++] = p[pSize++] = v;

						dfsPath[++depth] = v;
						edges[depth] = g.edgesOut(v);
						continue dfs;
					} else if (comp[v] == -1)
						while (c[p[pSize - 1]] > c[v])
							pSize--;
				}
				int u = dfsPath[depth];
				if (p[pSize - 1] == u) {
					int v;
					do {
						v = s[--sSize];
						comp[v] = compNum;
					} while (v != u);
					compNum++;
					pSize--;
				}

				edges[depth] = null;
				if (depth-- == 0)
					break;
			}
		}
		return Pair.of(Integer.valueOf(compNum), comp);
	}

	public static int[] calcTopologicalSortingDAG(DiGraph g) {
		int n = g.verticesNum();
		int[] inDegree = new int[n];
		QueueIntFixSize queue = new QueueIntFixSize(n);
		int[] topolSort = new int[n];
		int topolSortSize = 0;

		// Cache in degree of all vertices
		for (int v = 0; v < n; v++)
			inDegree[v] = g.degreeIn(v);

		// Find vertices with zero in degree and insert them to the queue
		for (int v = 0; v < n; v++)
			if (inDegree[v] == 0)
				queue.push(v);

		// Poll vertices from the queue and "remove" each one from the tree and add new
		// zero in degree vertices to the queue
		while (!queue.isEmpty()) {
			int u = queue.pop();
			topolSort[topolSortSize++] = u;
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				eit.nextInt();
				int v = eit.v();
				if (--inDegree[v] == 0)
					queue.push(v);
			}
		}

		if (topolSortSize != n)
			throw new IllegalArgumentException("G is not a directed acyclic graph (DAG)");

		return topolSort;
	}

	public static SSSP.Result calcDistancesDAG(DiGraph g, EdgeWeightFunc w, int source) {
		int n = g.verticesNum();
		int[] backtrack = new int[n];
		Arrays.fill(backtrack, -1);
		double[] distances = new double[n];
		Arrays.fill(distances, Double.POSITIVE_INFINITY);
		distances[source] = 0;

		int[] topolSort = calcTopologicalSortingDAG(g);
		boolean sourceSeen = false;
		for (int i = 0; i < n; i++) {
			int u = topolSort[i];
			if (!sourceSeen) {
				if (u != source)
					continue;
				sourceSeen = true;
			}
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.v();
				double d = distances[u] + w.weight(e);
				if (d < distances[v]) {
					distances[v] = d;
					backtrack[v] = e;
				}
			}
		}

		return new SSSPResultsImpl(g, distances, backtrack);
	}

	public static int getFullyBranchingTreeDepth(Graph t, int root) {
		for (int parent = -1, u = root, depth = 0;; depth++) {
			int v = parent;
			for (EdgeIter eit = t.edges(u); eit.hasNext();) {
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

	public static int[] calcDegree(UGraph g, IntCollection edges) {
		int[] degree = new int[g.verticesNum()];
		for (IntIterator eit = edges.iterator(); eit.hasNext();) {
			int e = eit.nextInt();
			degree[g.edgeSource(e)]++;
			degree[g.edgeTarget(e)]++;
		}
		return degree;
	}

	public static IntList calcEulerianTour(UGraph g) {
		int n = g.verticesNum();

		int start = -1, end = -1;
		for (int u = 0; u < n; u++) {
			if (g.degree(u) % 2 == 0)
				continue;
			if (start == -1)
				start = u;
			else if (end == -1)
				end = u;
			else
				throw new IllegalArgumentException(
						"More than two vertices have an odd degree (" + start + ", " + end + ", " + u + ")");
		}
		if (start != -1 && end == -1)
			throw new IllegalArgumentException(
					"Eulerian tour exists only if all vertices have even degree or only two vertices have odd degree");
		if (start == -1)
			start = 0;

		IntArrayList tour = new IntArrayList(g.edgesNum());
		IntSet usedEdges = new IntOpenHashSet();
		EdgeIter[] iters = new EdgeIter[n];
		for (int u = 0; u < n; u++)
			iters[u] = g.edges(u);

		StackIntFixSize queue = new StackIntFixSize(g.edgesNum());

		for (int u = start;;) {
			findCycle: for (;;) {
				int e, v;
				for (EdgeIter iter = iters[u];;) {
					if (!iter.hasNext())
						break findCycle;
					e = iter.nextInt();
					if (!usedEdges.contains(e)) {
						v = iter.v();
						break;
					}
				}
				usedEdges.add(e);
				queue.push(e);
				u = v;
			}

			if (queue.isEmpty())
				break;

			int e = queue.pop();
			tour.add(e);
			u = g.edgeEndpoint(e, u);
		}

		IntArrays.reverse(tour.elements(), 0, tour.size());
		return tour;
	}

	public static String formatAdjacencyMatrix(Graph g) {
		return formatAdjacencyMatrix(g, e -> e == -1 ? "0" : "1");
	}

	public static String formatAdjacencyMatrixWeighted(Graph g, EdgeWeightFunc w) {
		int m = g.edgesNum();
		double minWeight = Double.MAX_VALUE;
		for (int e = 0; e < m; e++) {
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

	public static String formatAdjacencyMatrixWeightedInt(Graph g, EdgeWeightFunc.Int w) {
		return formatAdjacencyMatrix(g, e -> e == -1 ? "-" : Integer.toString(w.weightInt(e)));
	}

	public static String formatAdjacencyMatrix(Graph g, Int2ObjectFunction<String> formatter) {
		int n = g.verticesNum();
		if (n == 0)
			return "[]";

		/* format all edges */
		String[][] strs = new String[n][n];
		for (int u = 0; u < n; u++) {
			for (EdgeIter eit = g.edges(u); eit.hasNext();) {
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

	public static class EdgeWeightComparator implements IntComparator {

		private final EdgeWeightFunc w;

		EdgeWeightComparator(EdgeWeightFunc w) {
			this.w = Objects.requireNonNull(w);
		}

		@Override
		public int compare(int e1, int e2) {
			return Utils.compare(w.weight(e1), w.weight(e2));
		}

	}

	public static class EdgeWeightIntComparator implements IntComparator {

		private final EdgeWeightFunc.Int w;

		EdgeWeightIntComparator(EdgeWeightFunc.Int w) {
			this.w = Objects.requireNonNull(w);
		}

		@Override
		public int compare(int e1, int e2) {
			return Integer.compare(w.weightInt(e1), w.weightInt(e2));
		}

	}

	public static DiGraph referenceGraph(DiGraph g, Object refEdgeWeightKey) {
		int m = g.edgesNum();
		DiGraph g0 = new GraphArrayDirected(g.verticesNum());
		GraphWeights.Int data = g0.newEdgeWeightInt(refEdgeWeightKey);
		for (int e = 0; e < m; e++) {
			int e0 = g0.addEdge(g.edgeSource(e), g.edgeTarget(e));
			data.set(e0, e);
		}
		return g0;
	}

	public static UGraph referenceGraph(UGraph g, Object refEdgeWeightKey) {
		int m = g.edgesNum();
		UGraph g0 = new GraphArrayUndirected(g.verticesNum());
		GraphWeights.Int data = g0.newEdgeWeightInt(refEdgeWeightKey);
		for (int e = 0; e < m; e++) {
			int e0 = g0.addEdge(g.edgeSource(e), g.edgeTarget(e));
			data.set(e0, e);
		}
		return g0;
	}

	public static interface PathIter {

		public boolean hasNext();

		public int nextEdge();

		public int u();

		public int v();

		static PathIter of(Graph g0, IntList edgeList) {
			if (g0 instanceof UGraph g) {
				return new PathIterUndirected(g, edgeList);
			} else if (g0 instanceof DiGraph g) {
				return new PathIterDirected(g, edgeList);
			} else {
				throw new IllegalArgumentException();
			}
		}

	}

	private static class PathIterUndirected implements PathIter {

		private final UGraph g;
		private final IntIterator it;
		private int e = -1, v = -1;

		PathIterUndirected(UGraph g, IntList path) {
			this.g = g;
			if (path.size() == 1) {
				v = g.edgeTarget(path.getInt(0));
			} else if (path.size() >= 2) {
				int e0 = path.getInt(0), e1 = path.getInt(1);
				int u0 = g.edgeSource(e0), v0 = g.edgeTarget(e0);
				int u1 = g.edgeSource(e1), v1 = g.edgeTarget(e1);
				if (v0 == u1 || v0 == v1) {
					v = u0;
				} else {
					v = v0;
					assert (u0 == u1 || u0 == v1) : "not a path";
				}
			}
			it = path.iterator();
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public int nextEdge() {
			e = it.nextInt();
			assert v == g.edgeSource(e) || v == g.edgeTarget(e);
			v = g.edgeEndpoint(e, v);
			return e;
		}

		@Override
		public int u() {
			return g.edgeEndpoint(e, v);
		}

		@Override
		public int v() {
			return v;
		}

	}

	private static class PathIterDirected implements PathIter {

		private final DiGraph g;
		private final IntIterator it;
		private int e = -1;

		PathIterDirected(DiGraph g, IntList path) {
			this.g = g;
			it = path.iterator();
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public int nextEdge() {
			e = it.nextInt();
			return e;
		}

		@Override
		public int u() {
			return g.edgeSource(e);
		}

		@Override
		public int v() {
			return g.edgeTarget(e);
		}

	}

	@SuppressWarnings("unchecked")
	static UGraph subGraph(UGraph g, IntCollection edgeSet) {
		UGraph g1 = new GraphArrayUndirected(g.verticesNum());

		int[] s2e = edgeSet.toIntArray();
		for (int s = 0; s < s2e.length; s++) {
			int e = s2e[s];
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			int s0 = g1.addEdge(u, v);
			assert s0 == s;
		}
		for (Object key : g.getEdgeWeightKeys()) {
			GraphWeights<?> data0 = g.edgesWeight(key);

			if (data0 instanceof GraphWeights.Int data) {
				GraphWeights.Int datas = g1.newEdgeWeightInt(key);
				for (int s = 0; s < s2e.length; s++)
					datas.set(s, data.getInt(s2e[s]));

			} else if (data0 instanceof GraphWeights.Double data) {
				GraphWeights.Double datas = g1.newEdgeWeightDouble(key);
				for (int s = 0; s < s2e.length; s++)
					datas.set(s, data.getDouble(s2e[s]));

			} else {
				@SuppressWarnings("rawtypes")
				GraphWeights datas = g1.newEdgeWeight(key);
				for (int s = 0; s < s2e.length; s++)
					datas.set(s, data0.get(s2e[s]));
			}
		}
		return g1;
	}

}
