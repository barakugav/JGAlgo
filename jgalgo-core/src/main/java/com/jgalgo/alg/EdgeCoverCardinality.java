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
import com.jgalgo.graph.IEdgeSet;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.internal.util.Assertions;

class EdgeCoverCardinality extends EdgeCovers.AbstractImpl {

	private final MatchingAlgo matchingAlgo = MatchingAlgo.newBuilder().setCardinality(true).build();

	@Override
	EdgeCover.Result computeMinimumEdgeCover(IndexGraph g, IWeightFunction w) {
		Assertions.Graphs.onlyCardinality(w);
		final int m = g.edges().size();

		Matching matching = matchingAlgo.computeMaximumCardinalityMatching(g);
		BitSet cover = new BitSet(m);

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

		/* add all the matched edges */
		for (int e = 0; e < m; e++)
			if (matching.containsEdge(e))
				cover.set(e);

		return new EdgeCovers.ResultImpl(g, cover);
	}

}
