package com.ugav.jgalgo;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.NoSuchElementException;

import com.ugav.jgalgo.Utils.StackIntFixSize;

import it.unimi.dsi.fastutil.ints.Int2ObjectFunction;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongPriorityQueue;

public class Graphs {

	private Graphs() {
	}

	public static class BFSIter implements IntIterator {

		private final Graph g;
		private final boolean[] visited;
		private final LongPriorityQueue queue;
		private int inEdge;
		private int layer;
		private int firstVInLayer;

		public BFSIter(Graph g, int source) {
			this(g, new int[] { source });
		}

		public BFSIter(Graph g, int[] sources) {
			if (sources.length == 0)
				throw new IllegalArgumentException();
			this.g = g;
			int n = g.vertices().size();
			visited = new boolean[n];
			queue = new LongArrayFIFOQueue(n * 2);
			inEdge = -1;
			layer = -1;

			for (int source : sources) {
				visited[source] = true;
				queue.enqueue(toQueueEntry(source, -1));
			}
			firstVInLayer = sources[0];
		}

		@Override
		public boolean hasNext() {
			return !queue.isEmpty();
		}

		@Override
		public int nextInt() {
			if (!hasNext())
				throw new NoSuchElementException();
			long entry = queue.dequeueLong();
			final int u = queueEntryToV(entry);
			inEdge = queueEntryToE(entry);
			if (u == firstVInLayer) {
				layer++;
				firstVInLayer = -1;
			}

			for (EdgeIter eit = g.edges(u); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.v();
				if (visited[v])
					continue;
				visited[v] = true;
				queue.enqueue(toQueueEntry(v, e));
				if (firstVInLayer == -1)
					firstVInLayer = v;
			}

			return u;
		}

		public int inEdge() {
			return inEdge;
		}

		public int layer() {
			return layer;
		}

		private static long toQueueEntry(int v, int e) {
			return ((v & 0xffffffffL) << 32) | ((e & 0xffffffffL) << 0);
		}

		private static int queueEntryToV(long entry) {
			return (int) ((entry >> 32) & 0xffffffff);
		}

		private static int queueEntryToE(long entry) {
			return (int) ((entry >> 0) & 0xffffffff);
		}
	}

	public static class DFSIter implements IntIterator {

		private final Graph g;
		private final boolean[] visited;
		private final List<EdgeIter> edgeIters;
		private final IntList edgePath;
		private boolean isValid;

		public DFSIter(Graph g, int source) {
			int n = g.vertices().size();
			this.g = g;
			visited = new boolean[n];
			edgeIters = new ArrayList<>();
			edgePath = new IntArrayList();

			visited[source] = true;
			edgeIters.add(g.edges(source));
			isValid = true;
		}

		@Override
		public boolean hasNext() {
			if (isValid)
				return true;
			if (edgeIters.isEmpty())
				return false;
			for (;;) {
				for (EdgeIter eit = edgeIters.get(edgeIters.size() - 1); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.v();
					if (visited[v])
						continue;
					visited[v] = true;
					edgeIters.add(g.edges(v));
					edgePath.add(e);
					return isValid = true;
				}
				edgeIters.remove(edgeIters.size() - 1);
				if (edgeIters.isEmpty()) {
					assert edgePath.isEmpty();
					return false;
				}
				edgePath.removeInt(edgePath.size() - 1);
			}
		}

		@Override
		public int nextInt() {
			if (!hasNext())
				throw new NoSuchElementException();
			isValid = false;
			return edgeIters.get(edgeIters.size() - 1).u();
		}

		public IntList edgePath() {
			return edgePath;
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
		int n = g.vertices().size();
		int[] backtrack = new int[n];
		Arrays.fill(backtrack, -1);

		for (BFSIter it = new BFSIter(g, u); it.hasNext();) {
			int p = it.nextInt();
			backtrack[p] = it.inEdge();
			if (p == v)
				break;
		}

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
		int n = g.vertices().size();
		int[] roots = new int[n];
		for (int u = 0; u < n; u++)
			roots[u] = u;
		return isForst(g, roots, true);
	}

	public static boolean isForst(Graph g, int[] roots) {
		return isForst(g, roots, false);
	}

	private static boolean isForst(Graph g, int[] roots, boolean allowVisitedRoot) {
		int n = g.vertices().size();
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
		int n = g.vertices().size();
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
		int n = g.vertices().size();

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
		int n = g.vertices().size();
		int[] inDegree = new int[n];
		IntPriorityQueue queue = new IntArrayFIFOQueue();
		int[] topolSort = new int[n];
		int topolSortSize = 0;

		// Cache in degree of all vertices
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

	public static SSSP.Result calcDistancesDAG(DiGraph g, EdgeWeightFunc w, int source) {
		SSSPResultImpl res = new SSSPResultImpl(g);
		res.distances[source] = 0;

		int[] topolSort = calcTopologicalSortingDAG(g);
		boolean sourceSeen = false;
		for (int u : topolSort) {
			if (!sourceSeen) {
				if (u != source)
					continue;
				sourceSeen = true;
			}
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.v();
				double d = res.distances[u] + w.weight(e);
				if (d < res.distances[v]) {
					res.distances[v] = d;
					res.backtrack[v] = e;
				}
			}
		}

		return res;
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
		int[] degree = new int[g.vertices().size()];
		for (IntIterator eit = edges.iterator(); eit.hasNext();) {
			int e = eit.nextInt();
			degree[g.edgeSource(e)]++;
			degree[g.edgeTarget(e)]++;
		}
		return degree;
	}

	public static IntList calcEulerianTour(UGraph g) {
		int n = g.vertices().size();

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

		IntArrayList tour = new IntArrayList(g.edges().size());
		IntSet usedEdges = new IntOpenHashSet();
		EdgeIter[] iters = new EdgeIter[n];
		for (int u = 0; u < n; u++)
			iters[u] = g.edges(u);

		StackIntFixSize queue = new StackIntFixSize(g.edges().size());

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

	public static String formatAdjacencyMatrixWeightedInt(Graph g, EdgeWeightFunc.Int w) {
		return formatAdjacencyMatrix(g, e -> e == -1 ? "-" : Integer.toString(w.weightInt(e)));
	}

	public static String formatAdjacencyMatrix(Graph g, Int2ObjectFunction<String> formatter) {
		int n = g.vertices().size();
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

	public static DiGraph referenceGraph(DiGraph g, Object refEdgeWeightKey) {
		DiGraph g0 = new GraphArrayDirected(g.vertices().size());
		Weights.Int edgeRef = g0.addEdgesWeight(refEdgeWeightKey).ofInts();
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int e0 = g0.addEdge(g.edgeSource(e), g.edgeTarget(e));
			edgeRef.set(e0, e);
		}
		return g0;
	}

	public static UGraph referenceGraph(UGraph g, Object refEdgeWeightKey) {
		UGraph g0 = new GraphArrayUndirected(g.vertices().size());
		Weights.Int edgeRef = g0.addEdgesWeight(refEdgeWeightKey).ofInts();
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int e0 = g0.addEdge(g.edgeSource(e), g.edgeTarget(e));
			edgeRef.set(e0, e);
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

			if (data0 instanceof Weights.Int data) {
				int defVal = data.defaultValInt();
				Weights.Int datas = g1.addEdgesWeight(key).defVal(defVal).ofInts();
				for (IntIterator it = g1.edges().iterator(); it.hasNext();) {
					int s = it.nextInt();
					int w = data.getInt(sub2Edge.getInt(s));
					if (w != defVal)
						datas.set(s, w);
				}

			} else if (data0 instanceof Weights.Double data) {
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

}
