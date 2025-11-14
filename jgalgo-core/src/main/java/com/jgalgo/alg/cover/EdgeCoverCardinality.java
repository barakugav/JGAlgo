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
package com.jgalgo.alg.cover;

import com.jgalgo.alg.match.IMatching;
import com.jgalgo.alg.match.MatchingAlgo;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IEdgeSet;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A simply algorithm for computing a minimum edge cover using a maximum matching algorithm.
 *
 * <p>
 * The algorithm compute a maximum cardinality matching and adds the matching edges to the cover, and then adds more
 * edges greedily until the cover is complete. This algorithm achieves the optimal solution for both directed and
 * undirected graphs. For directed graph, the algorithm treat use the {@linkplain Graph#undirectedView() undirected
 * view} of the graph.
 *
 * <p>
 * The algorithm running time is dominated by the maximum matching algorithm. Other than that, the algorithm use linear
 * time and space.
 *
 * @author Barak Ugav
 */
public class EdgeCoverCardinality extends EdgeCoverAbstract {

	private final MatchingAlgo matchingAlgo = MatchingAlgo.builder().cardinality(true).build();

	/**
	 * Create a new edge cover algorithm object for unweighted graphs.
	 *
	 * <p>
	 * Please prefer using {@link EdgeCover#newInstance()} to get a default implementation for the {@link EdgeCover}
	 * interface.
	 */
	public EdgeCoverCardinality() {}

	@Override
	protected IntSet computeMinimumEdgeCover(IndexGraph g, IWeightFunction w) {
		Assertions.onlyCardinality(w);
		final int m = g.edges().size();

		IMatching matching =
				(IMatching) matchingAlgo.computeMaximumMatching(g.isDirected() ? g.undirectedView() : g, null);

		/* add all the matched edges */
		Bitmap cover = new Bitmap(m);
		for (int e : matching.edges())
			cover.set(e);

		/* add more edges greedily to complete the cover */
		if (g.isDirected()) {
			for (int v : matching.unmatchedVertices()) {
				IEdgeSet edges;
				if ((edges = g.outEdges(v)).isEmpty() && (edges = g.inEdges(v)).isEmpty())
					throw new IllegalArgumentException(
							"no edge cover exists, vertex with index " + v + " has no edges");
				cover.set(edges.iterator().nextInt());
			}

		} else {
			for (int v : matching.unmatchedVertices()) {
				IEdgeSet edges;
				if ((edges = g.outEdges(v)).isEmpty())
					throw new IllegalArgumentException(
							"no edge cover exists, vertex with index " + v + " has no edges");
				cover.set(edges.iterator().nextInt());
			}
		}

		return ImmutableIntArraySet.withBitmap(cover);
	}

}
