package com.jgalgo;

/**
 * A directed graph implementation using a two dimensional table to store all edges.
 * <p>
 * The graph is initialized with a fixed number of vertices, \(n\), and does not support addition or removals of
 * vertices. A fixed sized table of size {@code [n][n]} stores the edges of the graph. The implementation does not
 * support multiple edges with identical source and target.
 * <p>
 * This implementation is efficient for use cases where fast lookups of edge \((u,v)\) are required, as they can be
 * answered in \(O(1)\) time, but it should not be the default choice for a directed graph.
 *
 * @see    GraphTableUndirected
 * @author Barak Ugav
 */
public class GraphTableDirected extends GraphTableAbstract implements DiGraph {

	/**
	 * Create a new graph with no edges and {@code n} vertices numbered {@code 0,1,2,..,n-1}.
	 * <p>
	 * Vertices can not be added or removed after the graph was created.
	 *
	 * @param n the number of initial vertices number
	 */
	public GraphTableDirected(int n) {
		super(n, Capabilities);
	}

	@Override
	public EdgeIter edgesOut(int u) {
		return new EdgeIterOut(u);
	}

	@Override
	public EdgeIter edgesIn(int v) {
		return new EdgeIterIn(v);
	}

	@Override
	public int addEdge(int u, int v) {
		int e = super.addEdge(u, v);
		edges[u][v] = e;
		return e;
	}

	@Override
	public void removeEdge(int e) {
		e = edgeSwapBeforeRemove(e);
		int u = edgeSource(e), v = edgeTarget(e);
		edges[u][v] = EdgeNone;
		super.removeEdge(e);
	}

	@Override
	void edgeSwap(int e1, int e2) {
		int u1 = edgeSource(e1), v1 = edgeTarget(e1);
		int u2 = edgeSource(e2), v2 = edgeTarget(e2);
		edges[u1][v1] = e2;
		edges[u2][v2] = e1;
		super.edgeSwap(e1, e2);
	}

	@Override
	public void reverseEdge(int e) {
		int u = edgeSource(e), v = edgeTarget(e);
		if (u == v)
			return;
		if (edges[v][u] != EdgeNone)
			throw new IllegalArgumentException("parallel edges are not supported");
		edges[v][u] = e;
		edges[u][v] = EdgeNone;
		super.reverseEdge(e);
	}

	private static final GraphCapabilities Capabilities = new GraphCapabilities() {
		@Override
		public boolean vertexAdd() {
			return false;
		}

		@Override
		public boolean vertexRemove() {
			return false;
		}

		@Override
		public boolean edgeAdd() {
			return true;
		}

		@Override
		public boolean edgeRemove() {
			return true;
		}

		@Override
		public boolean parallelEdges() {
			return false;
		}

		@Override
		public boolean selfEdges() {
			return true;
		}

		@Override
		public boolean directed() {
			return true;
		}
	};

}
