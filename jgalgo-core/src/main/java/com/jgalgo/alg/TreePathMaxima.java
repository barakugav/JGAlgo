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

package com.jgalgo.alg;

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import it.unimi.dsi.fastutil.ints.IntCollection;

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
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
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
	 * @return         a result object, with a corresponding result edge for each query
	 */
	TreePathMaxima.Result computeHeaviestEdgeInTreePaths(Graph tree, WeightFunction w, TreePathMaxima.Queries queries);

	/**
	 * Queries container for {@link TreePathMaxima} computations.
	 * <p>
	 * Queries are added one by one to this container, and than the Queries object is passed to a {@link TreePathMaxima}
	 * algorithm using {@link TreePathMaxima#computeHeaviestEdgeInTreePaths(Graph, WeightFunction, Queries)}.
	 *
	 * @author Barak Ugav
	 */
	static interface Queries {

		/**
		 * Create an empty queries container.
		 *
		 * @return a new queries container
		 */
		static TreePathMaxima.Queries newInstance() {
			return new TreePathMaximaUtils.QueriesImpl();
		}

		/**
		 * Add a query for the heaviest edge in a tree between two vertices.
		 *
		 * @param u the first vertex
		 * @param v the second vertex
		 */
		void addQuery(int u, int v);

		/**
		 * Get a query source by index.
		 * <p>
		 * A query is composed of two vertices, the source and the target. This method return the source vertex of a
		 * query. Use {@link #getQueryTarget(int)} to get the target vertex.
		 *
		 * @param  idx                       index of the query. Must be in range {@code [0, size())}
		 * @return                           the first vertex of the query
		 * @throws IndexOutOfBoundsException if {@code idx < 0} or {@code idx >= size()}
		 */
		int getQuerySource(int idx);

		/**
		 * Get a query target by index.
		 * <p>
		 * A query is composed of two vertices, the target and the source. This method return the target vertex of a
		 * query. Use {@link #getQueryTarget(int)} to get the source vertex.
		 *
		 * @param  idx                       index of the query. Must be in range {@code [0, size())}
		 * @return                           the second vertex of the query
		 * @throws IndexOutOfBoundsException if {@code idx < 0} or {@code idx >= size()}
		 */
		int getQueryTarget(int idx);

		/**
		 * Get the number of queries in this container.
		 *
		 * @return the number of queries in this container
		 */
		int size();

		/**
		 * Clear the container from all existing queries.
		 */
		void clear();

	}

	/**
	 * A result object for {@link TreePathMaxima} algorithm.
	 *
	 * @author Barak Ugav
	 */
	static interface Result {

		/**
		 * Get the heaviest edge found for a single query.
		 * <p>
		 * This result object was obtained by calling
		 * {@link TreePathMaxima#computeHeaviestEdgeInTreePaths(Graph, WeightFunction, Queries)}, which accept a set of
		 * multiple queries using the {@link TreePathMaxima.Queries} object. This method return the answer to a
		 * <b>single</b> queries among them, by its index.
		 *
		 * @param  queryIdx the index of the query \((u, v)\) in the {@link TreePathMaxima.Queries} object passed to
		 *                      {@link TreePathMaxima#computeHeaviestEdgeInTreePaths(Graph, WeightFunction, Queries)}
		 * @return          the edge identifier of the heaviest on the path from \(u\) to \(v\) (the query vertices) in
		 *                  the tree passed to the algorithm, or {@code -1} if no such path exists
		 */
		int getHeaviestEdge(int queryIdx);

		/**
		 * Get the number queries results this result object hold.
		 * <p>
		 * This number always much the size of the {@link TreePathMaxima.Queries} container passed to the
		 * {@link TreePathMaxima} algorithm.
		 *
		 * @return the number queries results this result object hold
		 */
		int size();
	}

	/**
	 * Create a new tree path maxima algorithm object.
	 * <p>
	 * This is the recommended way to instantiate a new {@link TreePathMaxima} object. The
	 * {@link TreePathMaxima.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link TreePathMaxima}
	 */
	static TreePathMaxima newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new tree path maxima algorithm builder.
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link TreePathMaxima} objects
	 */
	static TreePathMaxima.Builder newBuilder() {
		return new TreePathMaxima.Builder() {

			boolean bitsLookupTablesEnable;

			@Override
			public TreePathMaxima build() {
				TreePathMaximaHagerup tpm = new TreePathMaximaHagerup();
				tpm.setBitsLookupTablesEnable(bitsLookupTablesEnable);
				return tpm;
			}

			@Override
			public TreePathMaxima.Builder setOption(String key, Object value) {
				switch (key) {
					case "bits-lookup-tables-enable":
						bitsLookupTablesEnable = ((Boolean) value).booleanValue();
						break;
					default:
						throw new IllegalArgumentException("unknown option key: " + key);
				}
				return this;
			}

		};
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

		/**
		 * <b>[TL;DR Don't call me!]</b> Set an option.
		 * <p>
		 * The builder might support different options to customize its implementation. These options never change the
		 * behavior of the algorithm, only its internal implementation. The possible options are not exposed as 'public'
		 * because they are not part of the API and may change in the future.
		 * <p>
		 * These options are mainly for debug and benchmark purposes.
		 *
		 * @param  key   the option key
		 * @param  value the option value
		 * @return       this builder
		 */
		default TreePathMaxima.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

	/**
	 * Verify that the given edges actually form an MST of a graph.
	 * <p>
	 * The verification is done by computing for each original edge \((u, v)\) in the graph the heaviest edge on the
	 * path from \(u\) to \(v\) in the given spanning tree. If all of the edges which are not in the MST have a greater
	 * weight than the maximum one in the path of the MST, the MST is valid.
	 *
	 * @param  g                        an undirected graph
	 * @param  w                        an edge weight function
	 * @param  mstEdges                 collection of edges that form an MST
	 * @param  tpmAlgo                  tree path maximum algorithm, used for verification
	 * @return                          {@code true} if the collection of edges form an MST of {@code g}, else
	 *                                  {@code false}
	 * @throws IllegalArgumentException if {@code g} is a directed graph
	 */
	public static boolean verifyMST(Graph g, WeightFunction w, IntCollection mstEdges, TreePathMaxima tpmAlgo) {
		if (g instanceof IndexGraph)
			return TreePathMaximaUtils.verifyMST((IndexGraph) g, w, mstEdges, tpmAlgo);
		IndexGraph iGraph = g.indexGraph();
		IndexIdMap eiMap = g.indexGraphEdgesMap();
		w = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
		mstEdges = IndexIdMaps.idToIndexCollection(mstEdges, eiMap);
		return TreePathMaximaUtils.verifyMST(iGraph, w, mstEdges, tpmAlgo);
	}

}
