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
package com.jgalgo.alg.common;

import com.jgalgo.alg.cores.CoresAlgo;

/**
 * The direction type of an edge with respect to a vertex.
 *
 * <p>
 * This enum is usually used as a parameter to algorithm that compute some property of the vertices related to their
 * edges. For example, the {@link CoresAlgo} compute the cores of the graph with respect to the degree of the vertices:
 * the degree can be in-degree, out-degree or in-degree + out-degree (not a real addition, as self edges are only count
 * once, see {@link #All}), determining different types of cores.
 *
 * @author Barak Ugav
 */
public enum EdgeDirection {

	/**
	 * The 'Out' direction include all the edges with the vertex as the source, in case of a directed graph, or as the
	 * source or the target, in case of an undirected graph.
	 */
	Out,

	/**
	 * The 'In' direction include all the edges with the vertex as the target, in case of a directed graph, or as the
	 * source or the target, in case of an undirected graph.
	 */
	In,

	/**
	 * The 'All' direction include all the edges with the vertex as the source or the target. Note that self edges are
	 * count only once.
	 */
	All;

}
