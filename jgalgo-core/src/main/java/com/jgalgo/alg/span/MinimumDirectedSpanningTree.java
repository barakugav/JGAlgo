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

package com.jgalgo.alg.span;

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;

/**
 * Minimum spanning tree algorithm for directed graphs.
 *
 * <p>
 * A spanning tree in directed graph is defined similarly to a spanning tree in undirected graph, but the 'spanning
 * tree' does not yield a strongly connected graph, rather a tree in which all the vertices are reachable from the root.
 * Note that differing from the undirected {@link MinimumSpanningTree}, the root is given as part of the input, and the
 * result spanning tree will span only the vertices reachable from the root with a single tree, and not a forest.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * @author Barak Ugav
 */
public interface MinimumDirectedSpanningTree {

	/**
	 * Compute a minimum directed spanning tree (MDST) in a directed graph, rooted at the given vertex.
	 *
	 * <p>
	 * Note that the returned spanning tree is a single tree that span only the vertices reachable from the root, and
	 * not a forest that span the whole graph.
	 *
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
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link MinimumDirectedSpanningTree} object.
	 *
	 * @return a default implementation of {@link MinimumDirectedSpanningTree}
	 */
	static MinimumDirectedSpanningTree newInstance() {
		return new MinimumDirectedSpanningTreeTarjan();
	}

}
