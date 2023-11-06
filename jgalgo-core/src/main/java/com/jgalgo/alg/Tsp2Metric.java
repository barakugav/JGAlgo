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
 * Metric Traveling Salesman Problem (TSP) algorithm.
 *
 * <p>
 * Given a list of points, the traveling salesman problem asking what is the shortest tour to visit all points. The
 * metric version of TSP is a special case in which the distances satisfy the triangle inequality and the distances are
 * symmetric.
 *
 * <p>
 * The problem itself is NP-hard, but various approximation algorithms exists.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Travelling_salesman_problem">Wikipedia</a>
 * @author Barak Ugav
 */
public interface Tsp2Metric {

	/**
	 * Compute the shortest tour that visit all vertices.
	 *
	 * <p>
	 * Note that this problem is NP-hard and therefore the result is only the best approximation the implementation
	 * could find. If {@code g} is an {@link IntGraph}, a {@link IPath} object is returned. In that case, its better to
	 * pass a {@link IWeightFunction} as {@code w} to avoid boxing/unboxing.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   a graph containing all the vertices the tour must visit, using its edges
	 * @param  w   an edge weight function. In the metric world every three vertices \(u,v,w\) should satisfy \(w((u,v))
	 *                 + w((v,w)) \leq w((u,w))$
	 * @return     a result object containing the list of the \(n\) vertices ordered by the calculated path
	 */
	<V, E> Path<V, E> computeShortestTour(Graph<V, E> g, WeightFunction<E> w);

}
