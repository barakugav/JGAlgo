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
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;

/**
 * Minimum spanning tree algorithm for directed graphs.
 * <p>
 * A spanning tree in directed graph is defined similarly to a spanning tree in undirected graph, but the 'spanning
 * tree' does not yield a strongly connected graph, but a weakly connected tree rooted at some vertex.
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * @author Barak Ugav
 */
public interface MinimumDirectedSpanningTree {

	/**
	 * Compute a minimum directed spanning tree (MDST) in a directed graph, rooted at the given vertex.
	 * <p>
	 * If {@code g} is an {@link IntGraph}, a {@link MinimumSpanningTree.IResult} object will be returned. In that case,
	 * its better to pass a {@link IWeightFunction} as {@code w} to avoid boxing/unboxing.
	 *
	 * @param  <V>                      the vertices type
	 * @param  <E>                      the edges type
	 * @param  g                        a directed graph
	 * @param  w                        an edge weight function
	 * @param  root                     vertex in the graph the spanning tree will be rooted from
	 * @return                          all edges composing the spanning tree
	 * @throws IllegalArgumentException if {@code g} is not directed
	 */
	public <V, E> MinimumSpanningTree.Result<V, E> computeMinimumDirectedSpanningTree(Graph<V, E> g,
			WeightFunction<E> w, V root);

	/**
	 * Create a new directed-MST algorithm object.
	 * <p>
	 * This is the recommended way to instantiate a new {@link MinimumDirectedSpanningTree} object. The
	 * {@link MinimumDirectedSpanningTree.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link MinimumDirectedSpanningTree}
	 */
	static MinimumDirectedSpanningTree newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new minimum directed spanning tree algorithm builder.
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link MinimumDirectedSpanningTree} objects
	 */
	static MinimumDirectedSpanningTree.Builder newBuilder() {
		return MinimumDirectedSpanningTreeTarjan::new;
	}

	/**
	 * A builder for {@link MinimumDirectedSpanningTree} objects.
	 *
	 * @see    MinimumDirectedSpanningTree#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for minimum directed spanning tree computation.
		 *
		 * @return a new minimum directed spanning tree algorithm
		 */
		MinimumDirectedSpanningTree build();

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
		default MinimumDirectedSpanningTree.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
