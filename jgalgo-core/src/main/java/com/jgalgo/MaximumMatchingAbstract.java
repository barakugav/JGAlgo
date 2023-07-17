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

import java.util.Objects;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import it.unimi.dsi.fastutil.ints.IntCollection;

abstract class MaximumMatchingAbstract implements MaximumMatching {

	@Override
	public Matching computeMaximumCardinalityMatching(Graph g) {
		if (g instanceof IndexGraph)
			return computeMaximumCardinalityMatching((IndexGraph) g);

		IndexGraph iGraph = g.indexGraph();
		IndexIdMap viMap = g.indexGraphVerticesMap();
		IndexIdMap eiMap = g.indexGraphEdgesMap();

		Matching indexMatch = computeMaximumCardinalityMatching(iGraph);
		return new MatchingFromIndexMatching(indexMatch, viMap, eiMap);
	}

	@Override
	public Matching computeMaximumWeightedMatching(Graph g, WeightFunction w) {
		if (g instanceof IndexGraph)
			return computeMaximumWeightedMatching((IndexGraph) g, w);

		IndexGraph iGraph = g.indexGraph();
		IndexIdMap viMap = g.indexGraphVerticesMap();
		IndexIdMap eiMap = g.indexGraphEdgesMap();
		w = IndexIdMaps.idToIndexWeightFunc(w, eiMap);

		Matching indexMatch = computeMaximumWeightedMatching(iGraph, w);
		return new MatchingFromIndexMatching(indexMatch, viMap, eiMap);
	}

	@Override
	public Matching computeMaximumWeightedPerfectMatching(Graph g, WeightFunction w) {
		if (g instanceof IndexGraph)
			return computeMaximumWeightedPerfectMatching((IndexGraph) g, w);

		IndexGraph iGraph = g.indexGraph();
		IndexIdMap viMap = g.indexGraphVerticesMap();
		IndexIdMap eiMap = g.indexGraphEdgesMap();
		w = IndexIdMaps.idToIndexWeightFunc(w, eiMap);

		Matching indexMatch = computeMaximumWeightedPerfectMatching(iGraph, w);
		return new MatchingFromIndexMatching(indexMatch, viMap, eiMap);
	}

	abstract Matching computeMaximumCardinalityMatching(IndexGraph g);

	abstract Matching computeMaximumWeightedMatching(IndexGraph g, WeightFunction w);

	abstract Matching computeMaximumWeightedPerfectMatching(IndexGraph g, WeightFunction w);

	private static class MatchingFromIndexMatching implements Matching {

		private final Matching match;
		private final IndexIdMap viMap;
		private final IndexIdMap eiMap;

		MatchingFromIndexMatching(Matching match, IndexIdMap viMap, IndexIdMap eiMap) {
			this.match = Objects.requireNonNull(match);
			this.viMap = Objects.requireNonNull(viMap);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		@Override
		public boolean isVertexMatched(int vertex) {
			return match.isVertexMatched(viMap.idToIndex(vertex));
		}

		@Override
		public IntCollection matchedVertices() {
			return IndexIdMaps.indexToIdCollection(match.matchedVertices(), viMap);
		}

		@Override
		public IntCollection unmatchedVertices() {
			return IndexIdMaps.indexToIdCollection(match.unmatchedVertices(), viMap);
		}

		@Override
		public int getMatchedEdge(int vertex) {
			return match.getMatchedEdge(viMap.idToIndex(vertex));
		}

		@Override
		public boolean containsEdge(int edge) {
			return match.containsEdge(eiMap.idToIndex(edge));
		}

		@Override
		public IntCollection edges() {
			return IndexIdMaps.indexToIdCollection(match.edges(), eiMap);
		}

		@Override
		public double weight(WeightFunction w) {
			return match.weight(IndexIdMaps.idToIndexWeightFunc(w, eiMap));
		}

		@Override
		public String toString() {
			return edges().toString();
		}

		@Override
		public boolean isPerfect() {
			return match.isPerfect();
		}

	}

}
