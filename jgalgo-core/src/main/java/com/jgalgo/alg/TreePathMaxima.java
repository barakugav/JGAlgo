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

import java.util.Collection;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Tree Path Maxima (TPM) algorithm.
 *
 * <p>
 * Given a tree \(T\) and a sequence of vertices pairs \((u_1,v_1),(u_2,v_2),\ldots\) called <i>queries</i>, the tree
 * path maxima problem is to find for each pair \((u_i,v_i)\) the heaviest edge on the path between \(u_i\) and \(v_i\)
 * in \(T\).
 *
 * <p>
 * TPM can be used to validate if a spanning tree is minimum spanning tree (MST) or not, by checking for each edge
 * \((u,v)\) that is not in the tree that it is heavier than the heaviest edge in the path from \(u\) to \(v\) in the
 * tree. If a TPM on \(n\) vertices and \(m\) queries can be answer in \(O(n + m)\) time than an MST can be validated in
 * linear time.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * @author Barak Ugav
 */
public interface TreePathMaxima {

	/**
	 * Compute the heaviest edge in multiple tree paths.
	 *
	 * <p>
	 * The {@code queries} container contains pairs of vertices, each corresponding to a simple path in the given
	 * {@code tree}. For each of these paths, the heaviest edge in the path will be computed.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, a {@link TreePathMaxima.IResult} object is returned. In that case, its
	 * better to pass a {@link IWeightFunction} as {@code w}, and {@link TreePathMaxima.IQueries} as {@code queries} to
	 * avoid boxing/unboxing.
	 *
	 * @param  <V>     the vertices type
	 * @param  <E>     the edges type
	 * @param  tree    a tree
	 * @param  w       an edge weight function
	 * @param  queries a sequence of queries as pairs of vertices, each corresponding to a unique simple path in the
	 *                     tree.
	 * @return         a result object, with a corresponding result edge for each query
	 */
	<V, E> TreePathMaxima.Result<V, E> computeHeaviestEdgeInTreePaths(Graph<V, E> tree, WeightFunction<E> w,
			TreePathMaxima.Queries<V, E> queries);

	/**
	 * Queries container for {@link TreePathMaxima} computations.
	 *
	 * <p>
	 * Queries are added one by one to this container, and than the Queries object is passed to a {@link TreePathMaxima}
	 * algorithm using {@link TreePathMaxima#computeHeaviestEdgeInTreePaths(Graph, WeightFunction, Queries)}.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @author     Barak Ugav
	 */
	@SuppressWarnings("unused")
	static interface Queries<V, E> {

		/**
		 * Create an empty queries container.
		 *
		 * @param  <V> the vertices type
		 * @param  <E> the edges type
		 * @return     a new queries container
		 */
		static <V, E> TreePathMaxima.Queries<V, E> newInstance() {
			return new TreePathMaximaUtils.ObjQueriesImpl<>();
		}

		/**
		 * Add a query for the heaviest edge in a tree between two vertices.
		 *
		 * @param u the first vertex
		 * @param v the second vertex
		 */
		void addQuery(V u, V v);

		/**
		 * Get a query source by index.
		 *
		 * <p>
		 * A query is composed of two vertices, the source and the target. This method return the source vertex of a
		 * query. Use {@link #getQueryTarget(int)} to get the target vertex.
		 *
		 * @param  idx                       index of the query. Must be in range {@code [0, size())}
		 * @return                           the first vertex of the query
		 * @throws IndexOutOfBoundsException if {@code idx < 0} or {@code idx >= size()}
		 */
		V getQuerySource(int idx);

		/**
		 * Get a query target by index.
		 *
		 * <p>
		 * A query is composed of two vertices, the target and the source. This method return the target vertex of a
		 * query. Use {@link #getQueryTarget(int)} to get the source vertex.
		 *
		 * @param  idx                       index of the query. Must be in range {@code [0, size())}
		 * @return                           the second vertex of the query
		 * @throws IndexOutOfBoundsException if {@code idx < 0} or {@code idx >= size()}
		 */
		V getQueryTarget(int idx);

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
	 * Queries container for {@link TreePathMaxima} computations for {@link IntGraph}.
	 *
	 * @author Barak Ugav
	 */
	static interface IQueries extends TreePathMaxima.Queries<Integer, Integer> {

		/**
		 * Create an empty queries container.
		 *
		 * @return a new queries container
		 */
		static TreePathMaxima.IQueries newInstance() {
			return new TreePathMaximaUtils.IntQueriesImpl();
		}

		/**
		 * Add a query for the heaviest edge in a tree between two vertices.
		 *
		 * @param u the first vertex
		 * @param v the second vertex
		 */
		void addQuery(int u, int v);

		@Deprecated
		@Override
		default void addQuery(Integer u, Integer v) {
			addQuery(u.intValue(), v.intValue());
		}

		/**
		 * Get a query source by index.
		 *
		 * <p>
		 * A query is composed of two vertices, the source and the target. This method return the source vertex of a
		 * query. Use {@link #getQueryTargetInt(int)} to get the target vertex.
		 *
		 * @param  idx                       index of the query. Must be in range {@code [0, size())}
		 * @return                           the first vertex of the query
		 * @throws IndexOutOfBoundsException if {@code idx < 0} or {@code idx >= size()}
		 */
		int getQuerySourceInt(int idx);

		@Deprecated
		@Override
		default Integer getQuerySource(int idx) {
			return Integer.valueOf(getQuerySourceInt(idx));
		}

		/**
		 * Get a query target by index.
		 *
		 * <p>
		 * A query is composed of two vertices, the target and the source. This method return the target vertex of a
		 * query. Use {@link #getQueryTargetInt(int)} to get the source vertex.
		 *
		 * @param  idx                       index of the query. Must be in range {@code [0, size())}
		 * @return                           the second vertex of the query
		 * @throws IndexOutOfBoundsException if {@code idx < 0} or {@code idx >= size()}
		 */
		int getQueryTargetInt(int idx);

		@Deprecated
		@Override
		default Integer getQueryTarget(int idx) {
			return Integer.valueOf(getQueryTargetInt(idx));
		}
	}

	/**
	 * A result object for {@link TreePathMaxima} algorithm.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @author     Barak Ugav
	 */
	@SuppressWarnings("unused")
	static interface Result<V, E> {

		/**
		 * Get the heaviest edge found for a single query.
		 *
		 * <p>
		 * This result object was obtained by calling
		 * {@link TreePathMaxima#computeHeaviestEdgeInTreePaths(Graph, WeightFunction, Queries)}, which accept a set of
		 * multiple queries using the {@link TreePathMaxima.IQueries} object. This method return the answer to a
		 * <b>single</b> queries among them, by its index.
		 *
		 * @param  queryIdx the index of the query \((u, v)\) in the {@link TreePathMaxima.IQueries} object passed to
		 *                      {@link TreePathMaxima#computeHeaviestEdgeInTreePaths(Graph, WeightFunction, Queries)}
		 * @return          the edge identifier of the heaviest on the path from \(u\) to \(v\) (the query vertices) in
		 *                  the tree passed to the algorithm, or {@code -1} if no such path exists
		 */
		E getHeaviestEdge(int queryIdx);

		/**
		 * Get the number queries results this result object hold.
		 *
		 * <p>
		 * This number always much the size of the {@link TreePathMaxima.IQueries} container passed to the
		 * {@link TreePathMaxima} algorithm.
		 *
		 * @return the number queries results this result object hold
		 */
		int size();
	}

	/**
	 * A result object for {@link TreePathMaxima} algorithm for {@link IntGraph}.
	 *
	 * @author Barak Ugav
	 */
	static interface IResult extends TreePathMaxima.Result<Integer, Integer> {

		/**
		 * Get the heaviest edge found for a single query.
		 *
		 * <p>
		 * This result object was obtained by calling
		 * {@link TreePathMaxima#computeHeaviestEdgeInTreePaths(Graph, WeightFunction, Queries)}, which accept a set of
		 * multiple queries using the {@link TreePathMaxima.IQueries} object. This method return the answer to a
		 * <b>single</b> queries among them, by its index.
		 *
		 * @param  queryIdx the index of the query \((u, v)\) in the {@link TreePathMaxima.IQueries} object passed to
		 *                      {@link TreePathMaxima#computeHeaviestEdgeInTreePaths(Graph, WeightFunction, Queries)}
		 * @return          the edge identifier of the heaviest on the path from \(u\) to \(v\) (the query vertices) in
		 *                  the tree passed to the algorithm, or {@code -1} if no such path exists
		 */
		int getHeaviestEdgeInt(int queryIdx);

		@Deprecated
		@Override
		default Integer getHeaviestEdge(int queryIdx) {
			return Integer.valueOf(getHeaviestEdgeInt(queryIdx));
		}
	}

	/**
	 * Create a new tree path maxima algorithm object.
	 *
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
	 *
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
						TreePathMaxima.Builder.super.setOption(key, value);
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
		 *
		 * <p>
		 * The builder might support different options to customize its implementation. These options never change the
		 * behavior of the algorithm, only its internal implementation. The possible options are not exposed as 'public'
		 * because they are not part of the API and may change in the future.
		 *
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
	 *
	 * <p>
	 * The verification is done by computing for each original edge \((u, v)\) in the graph the heaviest edge on the
	 * path from \(u\) to \(v\) in the given spanning tree. If all of the edges which are not in the MST have a greater
	 * weight than the maximum one in the path of the MST, the MST is valid.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, its better to pass a {@link IWeightFunction} as {@code w}, and
	 * {@link IntCollection} as {@code edges} to avoid boxing/unboxing.
	 *
	 * @param  <V>                      the vertices type
	 * @param  <E>                      the edges type
	 * @param  g                        an undirected graph
	 * @param  w                        an edge weight function
	 * @param  mstEdges                 collection of edges that form an MST
	 * @param  tpmAlgo                  tree path maximum algorithm, used for verification
	 * @return                          {@code true} if the collection of edges form an MST of {@code g}, else
	 *                                  {@code false}
	 * @throws IllegalArgumentException if {@code g} is a directed graph
	 */
	@SuppressWarnings("unchecked")
	public static <V, E> boolean verifyMST(Graph<V, E> g, WeightFunction<E> w, Collection<E> mstEdges,
			TreePathMaxima tpmAlgo) {
		if (g instanceof IndexGraph) {
			IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
			IntCollection mstEdges0 = IntAdapters.asIntCollection((Collection<Integer>) mstEdges);
			return TreePathMaximaUtils.verifyMST((IndexGraph) g, w0, mstEdges0, tpmAlgo);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			IntCollection iMstEdges = IndexIdMaps.idToIndexCollection(mstEdges, eiMap);
			return TreePathMaximaUtils.verifyMST(iGraph, iw, iMstEdges, tpmAlgo);
		}
	}

}
