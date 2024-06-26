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
package com.jgalgo.alg.tree;

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IntGraph;

/**
 * An algorithm for computing the lowest common ancestor (LCA) of two vertices in a tree, offline.
 *
 * <p>
 * Given a rooted tree, the lowest common ancestor (LCA) of two vertices {@code u} and {@code v} is the lowest vertex
 * (farthest to root) that has both {@code u} and {@code v} as descendants. The offline version of this problem is given
 * a tree and a set of pairs of vertices, find the LCA of each pair. There are also the
 * {@linkplain LowestCommonAncestorStatic static} and {@linkplain LowestCommonAncestorDynamic online} versions of this
 * problem.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * @see    LowestCommonAncestorStatic
 * @see    LowestCommonAncestorDynamic
 * @author Barak Ugav
 */
public interface LowestCommonAncestorOffline {

	/**
	 * Find the lowest common ancestors of the given queries.
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned object is {@link LowestCommonAncestorOffline.IResult}.
	 *
	 * @param  <V>     the vertices type
	 * @param  <E>     the edges type
	 * @param  tree    the tree
	 * @param  root    the root of the tree
	 * @param  queries the queries
	 * @return         the lowest common ancestors of the given queries
	 */
	<V, E> LowestCommonAncestorOffline.Result<V, E> findLowestCommonAncestors(Graph<V, E> tree, V root,
			LowestCommonAncestorOffline.Queries<V, E> queries);

	/**
	 * Queries container for {@link LowestCommonAncestorOffline} computations.
	 *
	 * <p>
	 * Queries are added one by one to this container, and than the Queries object is passed to a
	 * {@link LowestCommonAncestorOffline} algorithm using
	 * {@link LowestCommonAncestorOffline#findLowestCommonAncestors(Graph, Object, Queries)}.
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
		 * @param  g   the graph
		 * @return     a new queries container
		 */
		@SuppressWarnings("unchecked")
		static <V, E> LowestCommonAncestorOffline.Queries<V, E> newInstance(Graph<V, E> g) {
			if (g instanceof IndexGraph) {
				LowestCommonAncestorOffline.IQueries queries =
						new LowestCommonAncestorOfflineQueriesImpl.IntQueriesImpl();
				return (LowestCommonAncestorOffline.Queries<V, E>) queries;
			} else {
				return new LowestCommonAncestorOfflineQueriesImpl.ObjQueriesImpl<>(g);
			}
		}

		/**
		 * Add a query for the lowest common ancestor of {@code u} and {@code v}.
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
	 * Queries container for {@link LowestCommonAncestorOffline} computations for {@link IntGraph}.
	 *
	 * @author Barak Ugav
	 */
	static interface IQueries extends LowestCommonAncestorOffline.Queries<Integer, Integer> {

		/**
		 * Create an empty queries container.
		 *
		 * @param  g the graph
		 * @return   a new queries container
		 */
		static LowestCommonAncestorOffline.IQueries newInstance(IntGraph g) {
			return new LowestCommonAncestorOfflineQueriesImpl.IntQueriesImpl();
		}

		/**
		 * Add a query for the lowest common ancestor of {@code u} and {@code v}.
		 *
		 * @param u the first vertex
		 * @param v the second vertex
		 */
		void addQuery(int u, int v);

		/**
		 * {@inheritDoc}
		 *
		 * @deprecated Please use {@link #addQuery(int, int)} instead to avoid un/boxing.
		 */
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

		/**
		 * {@inheritDoc}
		 *
		 * @deprecated Please use {@link #getQuerySourceInt(int)} instead to avoid un/boxing.
		 */
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

		/**
		 * {@inheritDoc}
		 *
		 * @deprecated Please use {@link #getQueryTargetInt(int)} instead to avoid un/boxing.
		 */
		@Deprecated
		@Override
		default Integer getQueryTarget(int idx) {
			return Integer.valueOf(getQueryTargetInt(idx));
		}
	}

	/**
	 * Result of a {@link LowestCommonAncestorOffline} computation.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @author     Barak Ugav
	 */
	@SuppressWarnings("unused")
	static interface Result<V, E> {

		/**
		 * Get the lowest common ancestor of the given query.
		 *
		 * <p>
		 * This result object was obtained by calling
		 * {@link LowestCommonAncestorOffline#findLowestCommonAncestors(Graph, Object, Queries)}, which accept a set of
		 * multiple queries using the {@link LowestCommonAncestorOffline.IQueries} object. This method return the answer
		 * to a <b>single</b> queries among them, by its index.
		 *
		 * @param  queryIdx index of the query. Must be in range {@code [0, size())}
		 * @return          the lowest common ancestor of the given query
		 */
		V getLca(int queryIdx);

		/**
		 * Get the number of queries in this result.
		 *
		 * <p>
		 * This number is the same as the number of queries in the {@link LowestCommonAncestorOffline.IQueries} object
		 * passed to {@link LowestCommonAncestorOffline#findLowestCommonAncestors(Graph, Object, Queries)}.
		 *
		 * @return the number of queries in this result
		 */
		int size();
	}

	/**
	 * Result of a {@link LowestCommonAncestorOffline} computation for {@link IntGraph}.
	 *
	 * @author Barak Ugav
	 */
	static interface IResult extends LowestCommonAncestorOffline.Result<Integer, Integer> {

		/**
		 * Get the lowest common ancestor of the given query.
		 *
		 * <p>
		 * This result object was obtained by calling
		 * {@link LowestCommonAncestorOffline#findLowestCommonAncestors(Graph, Object, Queries)}, which accept a set of
		 * multiple queries using the {@link LowestCommonAncestorOffline.IQueries} object. This method return the answer
		 * to a <b>single</b> queries among them, by its index.
		 *
		 * @param  queryIdx index of the query. Must be in range {@code [0, size())}
		 * @return          the lowest common ancestor of the given query
		 */
		int getLcaInt(int queryIdx);

		/**
		 * {@inheritDoc}
		 *
		 * @deprecated Please use {@link #getLcaInt(int)} instead to avoid un/boxing.
		 */
		@Deprecated
		@Override
		default Integer getLca(int queryIdx) {
			return Integer.valueOf(getLcaInt(queryIdx));
		}
	}

	/**
	 * Create a new offline LCA algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link LowestCommonAncestorOffline} object.
	 *
	 * @return a default implementation of {@link LowestCommonAncestorOffline}
	 */
	static LowestCommonAncestorOffline newInstance() {
		return new LowestCommonAncestorOfflineUnionFind();
	}

}
