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

import static com.jgalgo.internal.util.Range.range;
import java.util.Objects;
import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;

class Matchings {

	private Matchings() {}

	static class IndexMatching implements IMatching {

		private final IndexGraph g;
		private IntSet edges;
		private final int[] matched;
		private IntSet matchedVertices;
		private IntSet unmatchedVertices;

		IndexMatching(IndexGraph g, int[] matched) {
			assert matched.length == g.vertices().size();
			this.g = Objects.requireNonNull(g);
			this.matched = Objects.requireNonNull(matched);
		}

		@Override
		public boolean isVertexMatched(int vertex) {
			return getMatchedEdge(vertex) >= 0;
		}

		@Override
		public IntSet matchedVertices() {
			if (matchedVertices == null) {
				int matchedCount = 0;
				for (int v : range(matched.length))
					if (matched[v] >= 0)
						matchedCount++;
				int[] matchedVertices0 = new int[matchedCount];
				for (int i = 0, v = 0; v < matched.length; v++)
					if (matched[v] >= 0)
						matchedVertices0[i++] = v;
				matchedVertices = ImmutableIntArraySet
						.newInstance(matchedVertices0, v -> 0 <= v && v < matched.length && matched[v] >= 0);
			}
			return matchedVertices;
		}

		@Override
		public IntSet unmatchedVertices() {
			if (unmatchedVertices == null) {
				int unmatchedCount = 0;
				for (int v : range(matched.length))
					if (matched[v] < 0)
						unmatchedCount++;
				int[] unmatchedVertices0 = new int[unmatchedCount];
				for (int i = 0, v = 0; v < matched.length; v++)
					if (matched[v] < 0)
						unmatchedVertices0[i++] = v;
				unmatchedVertices = ImmutableIntArraySet
						.newInstance(unmatchedVertices0, v -> 0 <= v && v < matched.length && matched[v] < 0);
			}
			return unmatchedVertices;
		}

		@Override
		public int getMatchedEdge(int vertex) {
			return matched[vertex];
		}

		@Override
		public boolean containsEdge(int edge) {
			return matched[g.edgeSource(edge)] == edge;
		}

		@Override
		public IntSet edges() {
			computeEdgesCollection();
			return edges;
		}

		private void computeEdgesCollection() {
			if (edges != null)
				return;
			int edgesCount = 0;
			for (int v : range(g.vertices().size())) {
				int e = matched[v];
				if (e >= 0 && v == g.edgeSource(e)) {
					assert g.edgeSource(e) != g.edgeTarget(e);
					edgesCount++;
				}
			}
			int[] edges0 = new int[edgesCount];
			for (int i = 0, n = g.vertices().size(), v = 0; v < n; v++) {
				int e = matched[v];
				if (e >= 0 && v == g.edgeSource(e)) {
					assert g.edgeSource(e) != g.edgeTarget(e);
					edges0[i++] = e;
				}
			}
			edges = ImmutableIntArraySet
					.newInstance(edges0, e -> 0 <= e && e < g.edges().size() && matched[g.edgeSource(e)] == e);
		}

		@Override
		public String toString() {
			return edges().toString();
		}

		@Override
		public boolean isPerfect() {
			for (int e : matched)
				if (e < 0)
					return false;
			return true;
		}

	}

	static class SuperImpl implements MatchingAlgo {

		private final MatchingAlgo cardinalityGeneralAlgo;
		private final MatchingAlgo cardinalityBipartiteAlgo;
		private final MatchingAlgo weightedGeneralAlgo;
		private final MatchingAlgo weightedBipartiteAlgo;

		SuperImpl(MatchingAlgo cardinalityGeneralAlgo, MatchingAlgo cardinalityBipartiteAlgo,
				MatchingAlgo weightedGeneralAlgo, MatchingAlgo weightedBipartiteAlgo) {
			this.cardinalityGeneralAlgo = Objects.requireNonNull(cardinalityGeneralAlgo);
			this.cardinalityBipartiteAlgo = Objects.requireNonNull(cardinalityBipartiteAlgo);
			this.weightedGeneralAlgo = Objects.requireNonNull(weightedGeneralAlgo);
			this.weightedBipartiteAlgo = Objects.requireNonNull(weightedBipartiteAlgo);
		}

		@Override
		public <V, E> Matching<V, E> computeMaximumMatching(Graph<V, E> g, WeightFunction<E> w) {
			boolean cardinality = WeightFunction.isCardinality(w);
			boolean bipartite = isBipartite(g);
			if (cardinality) {
				if (bipartite) {
					return cardinalityBipartiteAlgo.computeMaximumMatching(g, w);
				} else {
					return cardinalityGeneralAlgo.computeMaximumMatching(g, w);
				}
			} else {
				if (bipartite) {
					return weightedBipartiteAlgo.computeMaximumMatching(g, w);
				} else {
					return weightedGeneralAlgo.computeMaximumMatching(g, w);
				}
			}
		}

		@Override
		public <V, E> Matching<V, E> computeMinimumMatching(Graph<V, E> g, WeightFunction<E> w) {
			boolean cardinality = WeightFunction.isCardinality(w);
			boolean bipartite = isBipartite(g);
			if (cardinality) {
				if (bipartite) {
					return cardinalityBipartiteAlgo.computeMinimumMatching(g, w);
				} else {
					return cardinalityGeneralAlgo.computeMinimumMatching(g, w);
				}
			} else {
				if (bipartite) {
					return weightedBipartiteAlgo.computeMinimumMatching(g, w);
				} else {
					return weightedGeneralAlgo.computeMinimumMatching(g, w);
				}
			}
		}

		@Override
		public <V, E> Matching<V, E> computeMaximumPerfectMatching(Graph<V, E> g, WeightFunction<E> w) {
			boolean cardinality = WeightFunction.isCardinality(w);
			boolean bipartite = isBipartite(g);
			if (cardinality) {
				if (bipartite) {
					return cardinalityBipartiteAlgo.computeMaximumPerfectMatching(g, w);
				} else {
					return cardinalityGeneralAlgo.computeMaximumPerfectMatching(g, w);
				}
			} else {
				if (bipartite) {
					return weightedBipartiteAlgo.computeMaximumPerfectMatching(g, w);
				} else {
					return weightedGeneralAlgo.computeMaximumPerfectMatching(g, w);
				}
			}
		}

		@Override
		public <V, E> Matching<V, E> computeMinimumPerfectMatching(Graph<V, E> g, WeightFunction<E> w) {
			boolean cardinality = WeightFunction.isCardinality(w);
			boolean bipartite = isBipartite(g);
			if (cardinality) {
				if (bipartite) {
					return cardinalityBipartiteAlgo.computeMinimumPerfectMatching(g, w);
				} else {
					return cardinalityGeneralAlgo.computeMinimumPerfectMatching(g, w);
				}
			} else {
				if (bipartite) {
					return weightedBipartiteAlgo.computeMinimumPerfectMatching(g, w);
				} else {
					return weightedGeneralAlgo.computeMinimumPerfectMatching(g, w);
				}
			}
		}

		private static boolean isBipartite(Graph<?, ?> g) {
			return BipartiteGraphs.getExistingPartition(g).isPresent();
		}

	}

	static class ObjMatchingFromIndexMatching<V, E> implements Matching<V, E> {

		private final IMatching match;
		private final IndexIdMap<V> viMap;
		private final IndexIdMap<E> eiMap;

		ObjMatchingFromIndexMatching(Graph<V, E> g, IMatching match) {
			this.match = Objects.requireNonNull(match);
			this.viMap = g.indexGraphVerticesMap();
			this.eiMap = g.indexGraphEdgesMap();
		}

		@Override
		public boolean isVertexMatched(V vertex) {
			return match.isVertexMatched(viMap.idToIndex(vertex));
		}

		@Override
		public Set<V> matchedVertices() {
			return IndexIdMaps.indexToIdSet(match.matchedVertices(), viMap);
		}

		@Override
		public Set<V> unmatchedVertices() {
			return IndexIdMaps.indexToIdSet(match.unmatchedVertices(), viMap);
		}

		@Override
		public E getMatchedEdge(V vertex) {
			int e = match.getMatchedEdge(viMap.idToIndex(vertex));
			return eiMap.indexToIdIfExist(e);
		}

		@Override
		public boolean containsEdge(E edge) {
			return match.containsEdge(eiMap.idToIndex(edge));
		}

		@Override
		public Set<E> edges() {
			return IndexIdMaps.indexToIdSet(match.edges(), eiMap);
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

	static class IntMatchingFromIndexMatching implements IMatching {

		private final IMatching match;
		private final IndexIntIdMap viMap;
		private final IndexIntIdMap eiMap;

		IntMatchingFromIndexMatching(IntGraph g, IMatching match) {
			this.match = Objects.requireNonNull(match);
			this.viMap = g.indexGraphVerticesMap();
			this.eiMap = g.indexGraphEdgesMap();
		}

		@Override
		public boolean isVertexMatched(int vertex) {
			return match.isVertexMatched(viMap.idToIndex(vertex));
		}

		@Override
		public IntSet matchedVertices() {
			return IndexIdMaps.indexToIdSet(match.matchedVertices(), viMap);
		}

		@Override
		public IntSet unmatchedVertices() {
			return IndexIdMaps.indexToIdSet(match.unmatchedVertices(), viMap);
		}

		@Override
		public int getMatchedEdge(int vertex) {
			int e = match.getMatchedEdge(viMap.idToIndex(vertex));
			return eiMap.indexToIdIfExistInt(e);
		}

		@Override
		public boolean containsEdge(int edge) {
			return match.containsEdge(eiMap.idToIndex(edge));
		}

		@Override
		public IntSet edges() {
			return IndexIdMaps.indexToIdSet(match.edges(), eiMap);
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
