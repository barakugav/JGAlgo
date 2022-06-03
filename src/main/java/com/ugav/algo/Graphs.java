package com.ugav.algo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.function.IntConsumer;

import com.ugav.algo.Graph.DirectedType;
import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Graph.WeightFunctionInt;
import com.ugav.algo.SSSP.SSSPResultsImpl;
import com.ugav.algo.Utils.QueueIntFixSize;

public class Graphs {

	private Graphs() {
		throw new InternalError();
	}

	@FunctionalInterface
	public static interface BFSOperator<E> {

		/**
		 * Perform some operation on a vertex during a BFS traversy
		 *
		 * @param v a vertex
		 * @param e the edge on which the BFS algorithm reached the vertex. might be
		 *          null for the source vertices
		 * @return true if the BFS should continue
		 */
		public boolean handleVertex(int v, Edge<E> e);

	}

	/**
	 * Perform a BFS traversy on a graph
	 *
	 * @param g      a graph
	 * @param source s source vertex
	 * @param op     user operation to operate on the reachable vertices
	 */
	public static <E> void runBFS(Graph<E> g, int source, BFSOperator<E> op) {
		runBFS(g, new int[] { source }, op);
	}

	public static <E> void runBFS(Graph<E> g, int[] sources, BFSOperator<E> op) {
		int n = g.vertices();
		boolean[] visited = new boolean[n];

		QueueIntFixSize queue = new QueueIntFixSize(n);

		for (int source : sources) {
			visited[source] = true;
			queue.push(source);
			if (!op.handleVertex(source, null))
				return;
		}

		while (!queue.isEmpty()) {
			int u = queue.pop();

			for (Edge<E> e : Utils.iterable(g.edges(u))) {
				int v = e.v();
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
	public static interface DFSOperator<E> {

		/**
		 * Perform some operation on a vertex during a DFS traversy
		 *
		 * @param v              a vertex
		 * @param pathFromSource a list of the edges from the source to the current
		 *                       vertex
		 * @return true if the DFS should continue
		 */
		public boolean handleVertex(int v, List<Edge<E>> pathFromSource);

	}

	/**
	 * Perform a DFS traversy on a graph
	 *
	 * @param g      a graph
	 * @param source s source vertex
	 * @param op     user operation to operate on the reachable vertices
	 */
	public static <E> void runDFS(Graph<E> g, int source, DFSOperator<E> op) {
		int n = g.vertices();
		boolean[] visited = new boolean[n];
		@SuppressWarnings("unchecked")
		Iterator<Edge<E>>[] edges = new Iterator[n];
		List<Edge<E>> edgesFromSource = new ArrayList<>();

		edges[0] = g.edges(source);
		visited[source] = true;
		if (!op.handleVertex(source, edgesFromSource))
			return;

		for (int depth = 0;;) {
			if (edges[depth].hasNext()) {
				Edge<E> e = edges[depth].next();
				int v = e.v();
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
				edgesFromSource.remove(edgesFromSource.size() - 1);
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
	public static <E> List<Edge<E>> findPath(Graph<E> g, int u, int v) {
		if (u == v)
			return Collections.emptyList();
		boolean reverse = true;
		if (!g.isDirected()) {
			int t = u;
			u = v;
			v = t;
			reverse = false;
		}
		int n = g.vertices();

		@SuppressWarnings("unchecked")
		Edge<E>[] backtrack = new Edge[n];

		int target = v;
		runBFS(g, u, (p, e) -> {
			backtrack[p] = e;
			return p != target;
		});

		if (backtrack[v] == null)
			return null;

		List<Edge<E>> path = new ArrayList<>();
		for (int p = v; p != u;) {
			Edge<E> e = backtrack[p];
			path.add(e);
			p = e.u();
		}
		if (reverse)
			Collections.reverse(path);
		return path;
	}

	public static <E> boolean isTree(Graph<E> g) {
		return isTree(g, 0);
	}

	public static <E> boolean isTree(Graph<E> g, int root) {
		return isForst(g, new int[] { root });
	}

	public static <E> boolean isForst(Graph<E> g) {
		int n = g.vertices();
		int[] roots = new int[n];
		for (int u = 0; u < n; u++)
			roots[u] = u;
		return isForst(g, roots, true);
	}

	public static <E> boolean isForst(Graph<E> g, int[] roots) {
		return isForst(g, roots, false);
	}

	private static <E> boolean isForst(Graph<E> g, int[] roots, boolean allowVisitedRoot) {
		int n = g.vertices();
		if (n == 0)
			return true;
		boolean directed = g.isDirected();

		boolean[] visited = new boolean[n];
		int[] parent = new int[n];
		Arrays.fill(parent, -1);

		int[] stack = new int[n];
		int visitedCount = 0;

		for (int i = 0; i < roots.length; i++) {
			int root = roots[i];
			if (visited[root]) {
				if (allowVisitedRoot)
					continue;
				return false;
			}

			stack[0] = root;
			int stackSize = 1;
			visited[root] = true;

			while (stackSize-- > 0) {
				int u = stack[stackSize];
				visitedCount++;

				for (Edge<E> e : Utils.iterable(g.edges(u))) {
					int v = e.v();
					if (!directed && v == parent[u])
						continue;
					if (visited[v])
						return false;
					visited[v] = true;
					stack[stackSize++] = v;
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
	public static <E> Pair<Integer, int[]> findConnectivityComponents(Graph<E> g) {
		if (g.isDirected())
			throw new IllegalArgumentException("only undirected graphs are supported");
		int n = g.vertices();
		int[] stack = new int[n];

		int[] comp = new int[n];
		Arrays.fill(comp, -1);
		int compNum = 0;

		for (int r = 0; r < n; r++) {
			if (comp[r] != -1)
				continue;

			int stackSize = 1;
			stack[0] = r;
			comp[r] = compNum;

			while (stackSize-- > 0) {
				int u = stack[stackSize];

				for (Edge<E> e : Utils.iterable(g.edges(u))) {
					int v = e.v();
					if (comp[v] != -1)
						continue;
					comp[v] = compNum;
					stack[stackSize++] = v;
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
	public static <E> Pair<Integer, int[]> findStrongConnectivityComponents(Graph<E> g) {
		if (!g.isDirected())
			return findConnectivityComponents(g);

		int n = g.vertices();

		int[] comp = new int[n];
		Arrays.fill(comp, -1);
		int compNum = 0;

		int[] dfsPath = new int[n];
		@SuppressWarnings("unchecked")
		Iterator<Edge<E>>[] edges = new Iterator[n];

		int[] c = new int[n];
		int[] s = new int[n];
		int[] p = new int[n];
		int cNext = 1, sSize = 0, pSize = 0;

		for (int r = 0; r < n; r++) {
			if (comp[r] != -1)
				continue;
			dfsPath[0] = r;
			edges[0] = g.edges(r);
			c[r] = cNext++;
			s[sSize++] = p[pSize++] = r;

			dfs: for (int depth = 0;;) {
				while (edges[depth].hasNext()) {
					Edge<E> e = edges[depth].next();
					int v = e.v();
					if (c[v] == 0) {
						c[v] = cNext++;
						s[sSize++] = p[pSize++] = v;

						dfsPath[++depth] = v;
						edges[depth] = g.edges(v);
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

	public static <E> int[] calcTopologicalSortingDAG(Graph<E> g) {
		if (!g.isDirected())
			throw new IllegalArgumentException();
		int n = g.vertices();
		int[] inDegree = new int[n];
		QueueIntFixSize queue = new QueueIntFixSize(n);
		int[] topolSort = new int[n];
		int topolSortSize = 0;

		// Calc in degree of all vertices
		for (Edge<E> e : g.edges())
			if (e.u() != e.v())
				inDegree[e.v()]++;

		// Find vertices with zero in degree and insert them to the queue
		for (int v = 0; v < n; v++)
			if (inDegree[v] == 0)
				queue.push(v);

		// Poll vertices from the queue and "remove" each one from the tree and add new
		// zero in degree vertices to the queue
		while (!queue.isEmpty()) {
			int u = queue.pop();
			topolSort[topolSortSize++] = u;
			for (Edge<E> e : Utils.iterable(g.edges(u))) {
				int v = e.v();
				if (--inDegree[v] == 0)
					queue.push(v);
			}
		}

		if (topolSortSize != n)
			throw new IllegalArgumentException("G is not a directed acyclic graph (DAG)");

		return topolSort;
	}

	public static <E> SSSP.Result<E> calcDistancesDAG(Graph<E> g, WeightFunction<E> w, int source) {
		int n = g.vertices();
		@SuppressWarnings("unchecked")
		Edge<E>[] backtrack = new Edge[n];
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
			for (Edge<E> e : Utils.iterable(g.edges(u))) {
				int v = e.v();
				double d = distances[u] + w.weight(e);
				if (d < distances[v]) {
					distances[v] = d;
					backtrack[v] = e;
				}
			}
		}

		return new SSSPResultsImpl<>(distances, backtrack);
	}

	public static <E> int getFullyBranchingTreeDepth(Graph<E> t, int root) {
		for (int parent = -1, u = root, depth = 0;; depth++) {
			int v = parent;
			for (Edge<E> e : Utils.iterable(t.edges(u))) {
				v = e.v();
				if (v != parent)
					break;
			}
			if (v == parent)
				return depth;
			parent = u;
			u = v;
		}
	}

	public static int[] calcDegree(Graph<?> g) {
		return calcDegree(g.edges(), g.vertices());
	}

	public static int[] calcDegree(Collection<? extends Edge<?>> edges, int n) {
		int[] degree = new int[n];
		for (Edge<?> e : edges) {
			degree[e.u()]++;
			degree[e.v()]++;
		}
		return degree;
	}

	public static <E> List<Edge<E>> calcEulerianTour(Graph<E> g) {
		if (g.isDirected())
			throw new IllegalArgumentException("not supported for directed graphs yet");
		int n = g.vertices();

		int[] degree = calcDegree(g);
		int start = -1, end = -1;
		for (int u = 0; u < n; u++) {
			if (degree[u] % 2 == 0)
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

		Function<Edge<E>, Edge<E>> edgeID = g.isDirected() ? Function.identity() : e -> {
			return System.identityHashCode(e) < System.identityHashCode(e.twin()) ? e : e.twin();
		};

		List<Edge<E>> tour = new ArrayList<>(g.edges().size());
		Set<Edge<E>> usedEdges = Collections.newSetFromMap(new IdentityHashMap<>());
		@SuppressWarnings("unchecked")
		Iterator<Edge<E>>[] iters = new Iterator[n];
		for (int u = 0; u < n; u++)
			iters[u] = g.edges(u);

		List<Edge<E>> queue = new ArrayList<>();
		IntConsumer findCycle = u -> {
			for (;;) {
				Edge<E> e;
				for (Iterator<Edge<E>> iter = iters[u];;) {
					if (!iter.hasNext())
						return;
					e = iter.next();
					if (!usedEdges.contains(edgeID.apply(e)))
						break;
				}
				usedEdges.add(edgeID.apply(e));
				queue.add(e);
				u = e.v();
			}
		};

		findCycle.accept(start);
		while (!queue.isEmpty()) {
			Edge<E> e = queue.get(queue.size() - 1);
			assert !iters[e.v()].hasNext();
			tour.add(e);
			queue.remove(queue.size() - 1);

			findCycle.accept(e.u());
		}
		Collections.reverse(tour);
		return tour;
	}

	public static <E> String formatAdjacencyMatrix(Graph<E> g) {
		return formatAdjacencyMatrix(g, e -> e == null ? "0" : "1");
	}

	public static <E> String formatAdjacencyMatrixWeighted(Graph<E> g, WeightFunction<E> w) {
		double minWeight = Double.MAX_VALUE;
		for (Edge<E> e : g.edges()) {
			double ew = w.weight(e);
			if (ew < minWeight)
				minWeight = ew;
		}

		int unlimitedPrecision = 64;
		int precision = minWeight >= 1 ? 2 : Math.min(unlimitedPrecision, (int) -Math.log10(minWeight));

		return precision == unlimitedPrecision
				? formatAdjacencyMatrix(g, e -> e == null ? "-" : Double.toString(w.weight(e)))
				: formatAdjacencyMatrix(g,
						e -> e == null ? "-" : String.format("%." + precision + "f", Double.valueOf(w.weight(e))));
	}

	public static <E> String formatAdjacencyMatrixWeightedInt(Graph<E> g, WeightFunctionInt<E> w) {
		return formatAdjacencyMatrix(g, e -> e == null ? "-" : Integer.toString(w.weightInt(e)));
	}

	public static <E> String formatAdjacencyMatrix(Graph<E> g, Function<Graph.Edge<E>, String> formatter) {
		int n = g.vertices();
		if (n == 0)
			return "[]";

		/* format all edges */
		String[][] strs = new String[n][n];
		for (int u = 0; u < n; u++)
			for (Edge<E> e : Utils.iterable(g.edges(u)))
				strs[u][e.v()] = formatter.apply(e);

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

	public static final WeightFunction<Double> WEIGHT_FUNC_DEFAULT = Edge::val;
	public static final WeightFunctionInt<Integer> WEIGHT_INT_FUNC_DEFAULT = Edge::val;

	public static class EdgeWeightComparator<E> implements Comparator<Edge<E>> {

		private final Graph.WeightFunction<E> w;

		EdgeWeightComparator(Graph.WeightFunction<E> w) {
			this.w = Objects.requireNonNull(w);
		}

		@Override
		public int compare(Edge<E> e1, Edge<E> e2) {
			return Utils.compare(w.weight(e1), w.weight(e2));
		}

	}

	public static class EdgeWeightIntComparator<E> implements Comparator<Edge<E>> {

		private final Graph.WeightFunctionInt<E> w;

		EdgeWeightIntComparator(Graph.WeightFunctionInt<E> w) {
			this.w = Objects.requireNonNull(w);
		}

		@Override
		public int compare(Edge<E> e1, Edge<E> e2) {
			return Integer.compare(w.weightInt(e1), w.weightInt(e2));
		}

	}

	public static <E> Graph<Edge<E>> referenceGraph(Graph<E> g) {
		Graph<Edge<E>> g0 = new GraphArray<>(g.isDirected() ? DirectedType.Directed : DirectedType.Undirected,
				g.vertices());
		for (Edge<E> e : g.edges())
			g0.addEdge(e.u(), e.v()).val(e);
		return g0;
	}

}
