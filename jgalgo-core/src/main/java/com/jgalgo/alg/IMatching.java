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

import java.util.BitSet;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMaps;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A matching in a {@link IntGraph}.
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
	 * <p>
	 * A vertex \(v\) is said to be <i>matched</i> if the matching contains an edge \((v,w)\) for some other vertex
	 * \(w\).
	 *
	 * @param  vertex a vertex
	 * @return        {@code true} if {@code vertex} has an adjacent edge in the matching, else {@code false}
	 */
	boolean isVertexMatched(int vertex);

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

	@Deprecated
	@Override
	default Integer getMatchedEdge(Integer vertex) {
		return Integer.valueOf(getMatchedEdge(vertex.intValue()));
	}

	@Override
	IntSet matchedVertices();

	@Override
	IntSet unmatchedVertices();

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

	@Deprecated
	@Override
	default boolean containsEdge(Integer edge) {
		return containsEdge(edge.intValue());
	}

	@Override
	IntSet edges();

	/**
	 * Check whether the given collection of edges form a valid matching in the graph.
	 * <p>
	 * A matching \(M\) is a sub set of \(E\), the edge set of the graph, in which for each vertex of the graph, no more
	 * than one adjacent edge is in \(M\). This method check whether a given collection of edges form a valid matching.
	 *
	 * @param  g     a graph
	 * @param  edges a collection of edges
	 * @return       {@code true} if {@code edges} form a valid matching in {@code g}, else {@code false}
	 */
	static boolean isMatching(IntGraph g, IntCollection edges) {
		IndexGraph ig;
		if (g instanceof IndexGraph) {
			ig = (IndexGraph) g;
		} else {
			ig = g.indexGraph();
			edges = IndexIdMaps.idToIndexCollection(edges, g.indexGraphEdgesMap());
		}
		final int n = ig.vertices().size();

		if (edges.size() > n / 2)
			return false;

		if (edges.size() * 4 > n / 8) {
			/* matching is big, use BitSet */
			BitSet matched = new BitSet(n);
			for (int e : edges) {
				int u = ig.edgeSource(e), v = ig.edgeTarget(e);
				if (matched.get(u))
					return false;
				matched.set(u);
				if (matched.get(v))
					return false;
				matched.set(v);
			}

		} else {
			/* matching is small, use hashtable */
			IntSet matched = new IntOpenHashSet(edges.size() * 2);
			for (int e : edges) {
				int u = ig.edgeSource(e), v = ig.edgeTarget(e);
				if (!matched.add(u))
					return false;
				if (!matched.add(v))
					return false;
			}

		}
		return true;
	}

}
