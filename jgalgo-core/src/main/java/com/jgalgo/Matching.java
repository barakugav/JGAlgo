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

import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * A matching in a graph.
 * <p>
 * Given a graph \(G=(V,E)\), a matching is a sub set of edges \(M\) such that any vertex in \(V\) has at most one
 * adjacent edge in \(M\). Its a common problem to compute the maximum (cardinality) matching, namely the matching with
 * the greatest number of edges. Another variant is to compute the maximum weighted matching with respect to some weight
 * function.
 *
 * @see    MaximumMatching
 * @see    MaximumMatchingWeighted
 * @see    <a href= "https://en.wikipedia.org/wiki/Matching_(graph_theory)">Wikipedia</a>
 * @author Barak Ugav
 */
public interface Matching {

	/**
	 * Check whether a vertex is matched by the matching.
	 * <p>
	 * A vertex \(v\) is said to be <i>matched</i> if the matching contains an edge \((v,w)\) for some other vertex
	 * \(w\).
	 *
	 * @param  vertex a vertex
	 * @return        {@code true} if {@code vertex} has an adjacent edge in the matching, else {@code false}
	 */
	boolean isVertexMatched(int vertex);

	/**
	 * Check whether an edge is part of the matching.
	 * <p>
	 * A matching \(M\) is a sub set of \(E\), the edge set of the graph. This method check whether a given edge is in
	 * \(M\).
	 *
	 * @param  edge an edge
	 * @return      {@code true} if the edge is part of the matching, else {@code false}
	 */
	boolean containsEdge(int edge);

	/**
	 * The collection of edges forming this matching.
	 *
	 * @return collection containing all the edges that are part of this matching
	 */
	IntCollection edges();

	/**
	 * Get the weight of the matching with respect to some weight function.
	 * <p>
	 * The weight of a matching is defined as the sum of its edges weights.
	 *
	 * @param  w an edge weight function
	 * @return   the sum of this matching edges weights
	 */
	double weight(EdgeWeightFunc w);

}
