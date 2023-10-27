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

/**
 * An algorithm for computing the lowest common ancestor (LCA) of two vertices in a tree, offline.
 * <p>
 * Given a rooted tree, the lowest common ancestor (LCA) of two vertices {@code u} and {@code v} is the lowest vertex
 * (farthest to root) that has both {@code u} and {@code v} as descendants. The offline version of this problem is given
 * a tree and a set of pairs of vertices, find the LCA of each pair. There are also the
 * {@linkplain LowestCommonAncestorStatic static} and {@linkplain LowestCommonAncestorDynamic online} versions of this
 * problem.
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * @see    LowestCommonAncestorStatic
 * @see    LowestCommonAncestorDynamic
 * @author Barak Ugav
 */
public interface LowestCommonAncestorOffline {

	/**
	 * Find the lowest common ancestors of the given queries.
	 *
	 * @param  tree    the tree
	 * @param  root    the root of the tree
	 * @param  queries the queries
	 * @return         the lowest common ancestors of the given queries
	 */
	LowestCommonAncestorOffline.Result findLCAs(Graph tree, int root, LowestCommonAncestorOffline.Queries queries);

	/**
	 * Queries container for {@link LowestCommonAncestorOffline} computations.
	 * <p>
	 * Queries are added one by one to this container, and than the Queries object is passed to a
	 * {@link LowestCommonAncestorOffline} algorithm using
	 * {@link LowestCommonAncestorOffline#findLCAs(Graph, int, Queries)}.
	 *
	 * @author Barak Ugav
	 */
	static interface Queries {

		/**
		 * Create an empty queries container.
		 *
		 * @return a new queries container
		 */
		static LowestCommonAncestorOffline.Queries newInstance() {
			return new LowestCommonAncestorOfflineUtils.QueriesImpl();
		}

		/**
		 * Add a query for the lowest common ancestor of {@code u} and {@code v}.
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
	 * Result of a {@link LowestCommonAncestorOffline} computation.
	 *
	 * @author Barak Ugav
	 */
	static interface Result {

		/**
		 * Get the lowest common ancestor of the given query.
		 * <p>
		 * This result object was obtained by calling {@link LowestCommonAncestorOffline#findLCAs(Graph, int, Queries)},
		 * which accept a set of multiple queries using the {@link LowestCommonAncestorOffline.Queries} object. This
		 * method return the answer to a <b>single</b> queries among them, by its index.
		 *
		 * @param  queryIdx index of the query. Must be in range {@code [0, size())}
		 * @return          the lowest common ancestor of the given query
		 */
		int getLca(int queryIdx);

		/**
		 * Get the number of queries in this result.
		 * <p>
		 * This number is the same as the number of queries in the {@link LowestCommonAncestorOffline.Queries} object
		 * passed to {@link LowestCommonAncestorOffline#findLCAs(Graph, int, Queries)}.
		 *
		 * @return the number of queries in this result
		 */
		int size();
	}

	/**
	 * Create a new tree path maxima algorithm object.
	 * <p>
	 * This is the recommended way to instantiate a new {@link LowestCommonAncestorOffline} object. The
	 * {@link LowestCommonAncestorOffline.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link LowestCommonAncestorOffline}
	 */
	static LowestCommonAncestorOffline newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new tree path maxima algorithm builder.
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link LowestCommonAncestorOffline} objects
	 */
	static LowestCommonAncestorOffline.Builder newBuilder() {
		return LowestCommonAncestorOfflineUnionFind::new;
	}

	/**
	 * A builder for {@link LowestCommonAncestorOffline} objects.
	 *
	 * @see    LowestCommonAncestorOffline#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for tree path maxima computation.
		 *
		 * @return a new tree path maxima algorithm
		 */
		LowestCommonAncestorOffline build();

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
		default LowestCommonAncestorOffline.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
