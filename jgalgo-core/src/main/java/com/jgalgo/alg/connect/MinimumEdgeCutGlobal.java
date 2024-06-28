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
package com.jgalgo.alg.connect;

import com.jgalgo.alg.common.IVertexBiPartition;
import com.jgalgo.alg.common.VertexBiPartition;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;

/**
 * Global Minimum Edge-Cut algorithm without terminal vertices.
 *
 * <p>
 * Given a graph \(G=(V,E)\), an edge cut is a partition of \(V\) into two sets \(C, \bar{C} = V \setminus C\). Given a
 * weight function, the weight of an edge-cut \((C,\bar{C})\) is the weight sum of all edges \((u,v)\) such that \(u\)
 * is in \(C\) and \(v\) is in \(\bar{C}\). There are two variants of the problem to find a minimum weight edge-cut: (1)
 * With terminal vertices, and (2) without terminal vertices. In the variant with terminal vertices, we are given two
 * special vertices {@code source (S)} and {@code sink (T)} and we need to find the minimum edge-cut \((C,\bar{C})\)
 * such that the {@code source} is in \(C\) and the {@code sink} is in \(\bar{C}\). In the variant without terminal
 * vertices (also called 'global edge-cut') we need to find the minimal cut among all possible cuts, and \(C,\bar{C}\)
 * simply must not be empty.
 *
 * <p>
 * Algorithms implementing this interface compute the global minimum edge-cut without terminal vertices.
 *
 * <p>
 * The cardinality (unweighted) global minimum edge-cut is equal to the edge connectivity of a graph.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * @see    <a href="https://en.wikipedia.org/wiki/Minimum_cut">Wikipedia</a>
 * @see    MinimumEdgeCutSt
 * @author Barak Ugav
 */
public interface MinimumEdgeCutGlobal {

	/**
	 * Compute the global minimum edge-cut in a graph.
	 *
	 * <p>
	 * Given a graph \(G=(V,E)\), an edge-cut is a partition of \(V\) into twos sets \(C, \bar{C} = V \setminus C\). The
	 * return value of this function is a partition into these two sets.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, a {@link IVertexBiPartition} object will be returned. In that case, its
	 * better to pass a {@link IWeightFunction} as {@code w} to avoid boxing/unboxing.
	 *
	 * @param  <V>                      the vertices type
	 * @param  <E>                      the edges type
	 * @param  g                        a graph
	 * @param  w                        an edge weight function
	 * @return                          the cut that was computed
	 * @throws IllegalArgumentException if the graph has less than two vertices
	 */
	<V, E> VertexBiPartition<V, E> computeMinimumCut(Graph<V, E> g, WeightFunction<E> w);

	/**
	 * Create a new minimum global edge-cut algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link MinimumEdgeCutGlobal} object.
	 *
	 * @return a default implementation of {@link MinimumEdgeCutGlobal}
	 */
	static MinimumEdgeCutGlobal newInstance() {
		return new MinimumEdgeCutGlobalStoerWagner();
	}
}
