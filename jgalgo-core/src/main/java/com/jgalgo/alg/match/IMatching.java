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
package com.jgalgo.alg.match;

import com.jgalgo.graph.IntGraph;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A matching in a {@link IntGraph}.
 *
 * <p>
 * This interface is a specific version of {@link Matching} for {@link IntGraph}. For the full documentation see
 * {@link Matching}.
 *
 * @see    MatchingAlgo
 * @see    <a href= "https://en.wikipedia.org/wiki/Matching_(graph_theory)">Wikipedia</a>
 * @author Barak Ugav
 */
public interface IMatching extends Matching<Integer, Integer> {

	/**
	 * Check whether a vertex is matched by the matching.
	 *
	 * <p>
	 * A vertex \(v\) is said to be <i>matched</i> if the matching contains an edge \((v,w)\) for some other vertex
	 * \(w\).
	 *
	 * @param  vertex a vertex
	 * @return        {@code true} if {@code vertex} has an adjacent edge in the matching, else {@code false}
	 */
	boolean isVertexMatched(int vertex);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #isVertexMatched(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default boolean isVertexMatched(Integer vertex) {
		return isVertexMatched(vertex.intValue());
	}

	/**
	 * Get the only matched edge adjacent to a given vertex.
	 *
	 * @param  vertex a vertex
	 * @return        the edge adjacent to {@code vertex} in the matching, or {@code -1} if {@code vertex} is not
	 *                matched
	 */
	int getMatchedEdge(int vertex);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #getMatchedEdge(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default Integer getMatchedEdge(Integer vertex) {
		int e = getMatchedEdge(vertex.intValue());
		return e < 0 ? null : Integer.valueOf(e);
	}

	@Override
	IntSet matchedVertices();

	@Override
	IntSet unmatchedVertices();

	/**
	 * Check whether an edge is part of the matching.
	 *
	 * <p>
	 * A matching \(M\) is a sub set of \(E\), the edge set of the graph. This method check whether a given edge is in
	 * \(M\).
	 *
	 * @param  edge an edge
	 * @return      {@code true} if the edge is part of the matching, else {@code false}
	 */
	boolean containsEdge(int edge);

	/**
	 * {@inheritDoc}
	 *
	 * @deprecated Please use {@link #containsEdge(int)} instead to avoid un/boxing.
	 */
	@Deprecated
	@Override
	default boolean containsEdge(Integer edge) {
		return containsEdge(edge.intValue());
	}

	@Override
	IntSet edges();

}
