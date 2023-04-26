package com.jgalgo;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Tree Path Maxima (TPM) algorithm.
 * <p>
 * Given a tree {@code T} and a sequence of vertices pairs
 * (u<sub>1</sub>,v<sub>1</sub>),(u<sub>2</sub>, v<sub>2</sub>),... called
 * <i>queries</i>, the tree path maxima problem is to find for each pair
 * (u<sub>i</sub>, v<sub>i</sub>) the heaviest edge on the path between
 * u<sub>i</sub> and v<sub>i</sub> in {@code T}.
 * <p>
 * TPM can be used to validate if a spanning tree is minimum spanning tree (MST)
 * or not, by checking for each edge {@code (u, v)} that is not in the tree that
 * it is heavier than the heaviest edge in the path from {@code u} to {@code v}
 * in the tree. If a TPM on {@code n} vertices and {@code m} queries can be
 * answer in {@code O(n + m)} time than an MST can be validated in linear time.
 *
 * @author Barak Ugav
 */
public interface TPM {

	/**
	 * Compute the heaviest edge in multiple tree paths.
	 * <p>
	 * The {@code queries} container contains pairs of vertices, each corresponding
	 * to a simple path in the given {@code tree}. For each of these paths, the
	 * heaviest edge in the path will be computed.
	 *
	 * @param tree    a tree
	 * @param w       an edge weight function
	 * @param queries a sequence of queries as pairs of vertices, each corresponding
	 *                to a unique simple path in the tree.
	 * @return array of edges in the same size as the queries container, where the
	 *         edge in the {@code i}'th entry is the heaviest edge in the tree path
	 *         between the two vertices of the {@code i}'th query.
	 */
	public int[] computeHeaviestEdgeInTreePaths(Graph tree, EdgeWeightFunc w, TPM.Queries queries);

	/**
	 * Queries container for {@link TPM} computations.
	 * <p>
	 * Queries are added one by one to this container, and than the Queries object
	 * is passed to the {@link TPM} algorithm using
	 * {@link TPM#computeHeaviestEdgeInTreePaths(Graph, EdgeWeightFunc, Queries)}.
	 *
	 * @author Barak Ugav
	 */
	static class Queries {
		private final IntList qs;

		/**
		 * Create an empty queries container.
		 */
		public Queries() {
			qs = new IntArrayList();
		}

		/**
		 * Add a query for the heaviest edge in a tree between two vertices.
		 *
		 * @param u the first vertex
		 * @param v the second vertex
		 */
		public void addQuery(int u, int v) {
			qs.add(u);
			qs.add(v);
		}

		/**
		 * Get a query by index.
		 *
		 * @param idx index of the query. Must be in range {@code [0, size())}
		 * @return pair with the two vertices of the query
		 * @throws IndexOutOfBoundsException if {@code idx < 0} or {@code idx >= size()}
		 */
		public IntIntPair getQuery(int idx) {
			return IntIntPair.of(qs.getInt(idx * 2), qs.getInt(idx * 2 + 1));
		}

		/**
		 * Get the number of queries in this container.
		 *
		 * @return the number of queries in this container
		 */
		public int size() {
			return qs.size() / 2;
		}

		/**
		 * Clear the container from all existing queries.
		 */
		public void clear() {
			qs.clear();
		}

	}

	/**
	 * Verify that the given edges actually form an MST of a graph.
	 * <p>
	 * The verification is done by computing for each original edge in the graph the
	 * maximum edge in the given MST. If all of the edges which are not in the MST
	 * have a greater weight than the maximum one in the path of the MST, the MST is
	 * valid.
	 *
	 * @param g        an undirected graph
	 * @param w        an edge weight function
	 * @param mstEdges collection of edges that form an MST
	 * @param tpmAlgo  tree path maximum algorithm, used for verification
	 * @return {@code true} if the collection of edges form an MST of {@code g},
	 *         else {@code false}
	 */
	public static boolean verifyMST(UGraph g, EdgeWeightFunc w, IntCollection mstEdges, TPM tpmAlgo) {
		int n = g.vertices().size();
		UGraph mst = new GraphArrayUndirected(n);
		Weights.Int edgeRef = mst.addEdgesWeights("edgeRef", int.class);
		for (IntIterator it = mstEdges.iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			int ne = mst.addEdge(u, v);
			edgeRef.set(ne, e);
		}
		if (!Trees.isTree(mst))
			return false;

		EdgeWeightFunc w0 = e -> w.weight(edgeRef.getInt(e));
		TPM.Queries queries = new TPM.Queries();
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			queries.addQuery(g.edgeSource(e), g.edgeTarget(e));
		}
		int[] tpmResults = tpmAlgo.computeHeaviestEdgeInTreePaths(mst, w0, queries);

		int i = 0;
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int mstEdge = tpmResults[i++];
			if (mstEdge == -1 || w.weight(e) < w0.weight(mstEdge))
				return false;
		}
		return true;
	}

}