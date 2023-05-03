/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jgalgo;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIntPair;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Tree Path Maxima (TPM) algorithm.
 * <p>
 * Given a tree \(T\) and a sequence of vertices pairs \((u_1,v_1),(u_2,v_2),\ldots\) called <i>queries</i>, the tree
 * path maxima problem is to find for each pair \((u_i,v_i)\) the heaviest edge on the path between \(u_i\) and \(v_i\)
 * in \(T\).
 * <p>
 * TPM can be used to validate if a spanning tree is minimum spanning tree (MST) or not, by checking for each edge
 * \((u,v)\) that is not in the tree that it is heavier than the heaviest edge in the path from \(u\) to \(v\) in the
 * tree. If a TPM on \(n\) vertices and \(m\) queries can be answer in \(O(n + m)\) time than an MST can be validated in
 * linear time.
 *
 * @author Barak Ugav
 */
public interface TreePathMaxima {

	/**
	 * Compute the heaviest edge in multiple tree paths.
	 * <p>
	 * The {@code queries} container contains pairs of vertices, each corresponding to a simple path in the given
	 * {@code tree}. For each of these paths, the heaviest edge in the path will be computed.
	 *
	 * @param  tree    a tree
	 * @param  w       an edge weight function
	 * @param  queries a sequence of queries as pairs of vertices, each corresponding to a unique simple path in the
	 *                     tree.
	 * @return         array of edges in the same size as the queries container, where the edge in the {@code i}-th
	 *                 entry is the heaviest edge in the tree path between the two vertices of the {@code i}'th query.
	 */
	public int[] computeHeaviestEdgeInTreePaths(Graph tree, EdgeWeightFunc w, TreePathMaxima.Queries queries);

	/**
	 * Queries container for {@link TreePathMaxima} computations.
	 * <p>
	 * Queries are added one by one to this container, and than the Queries object is passed to the
	 * {@link TreePathMaxima} algorithm using
	 * {@link TreePathMaxima#computeHeaviestEdgeInTreePaths(Graph, EdgeWeightFunc, Queries)}.
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
		 * @param  idx                       index of the query. Must be in range {@code [0, size())}
		 * @return                           pair with the two vertices of the query
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
	 * Create a new tree path maxima algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link TreePathMaxima} object.
	 *
	 * @return a new builder that can build {@link TreePathMaxima} objects
	 */
	static TreePathMaxima.Builder newBuilder() {
		return TreePathMaximaHagerup::new;
	}

	/**
	 * A builder for {@link TreePathMaxima} objects.
	 *
	 * @see    TreePathMaxima#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for tree path maxima computation.
		 *
		 * @return a new tree path maxima algorithm
		 */
		TreePathMaxima build();
	}

	/**
	 * Verify that the given edges actually form an MST of a graph.
	 * <p>
	 * The verification is done by computing for each original edge in the graph the maximum edge in the given MST. If
	 * all of the edges which are not in the MST have a greater weight than the maximum one in the path of the MST, the
	 * MST is valid.
	 *
	 * @param  g                        an undirected graph
	 * @param  w                        an edge weight function
	 * @param  mstEdges                 collection of edges that form an MST
	 * @param  tpmAlgo                  tree path maximum algorithm, used for verification
	 * @return                          {@code true} if the collection of edges form an MST of {@code g}, else
	 *                                  {@code false}
	 * @throws IllegalArgumentException if {@code g} is a directed graph
	 */
	public static boolean verifyMST(Graph g, EdgeWeightFunc w, IntCollection mstEdges, TreePathMaxima tpmAlgo) {
		if (g.getCapabilities().directed())
			throw new IllegalArgumentException("directed graphs are not supported");
		int n = g.vertices().size();
		Graph mst = new GraphArrayUndirected(n);
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
		TreePathMaxima.Queries queries = new TreePathMaxima.Queries();
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