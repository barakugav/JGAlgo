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

import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;

class Matchings {

	static class MatchingImpl implements IMatching {

		private final IndexGraph g;
		private IntSet edges;
		private final int[] matched;
		private IntSet matchedVertices;
		private IntSet unmatchedVertices;

		MatchingImpl(IndexGraph g, int[] matched) {
			assert matched.length == g.vertices().size();
			this.g = Objects.requireNonNull(g);
			this.matched = Objects.requireNonNull(matched);
		}

		static MatchingImpl emptyMatching(IndexGraph g) {
			int[] matched = new int[g.vertices().size()];
			Arrays.fill(matched, -1);
			return new MatchingImpl(g, matched);
		}

		@Override
		public boolean isVertexMatched(int vertex) {
			return getMatchedEdge(vertex) != -1;
		}

		@Override
		public IntSet matchedVertices() {
			if (matchedVertices == null) {
				int matchedCount = 0;
				for (int v = 0; v < matched.length; v++)
					if (matched[v] != -1)
						matchedCount++;
				int[] matchedVertices0 = new int[matchedCount];
				for (int i = 0, v = 0; v < matched.length; v++)
					if (matched[v] != -1)
						matchedVertices0[i++] = v;
				matchedVertices = new ImmutableIntArraySet(matchedVertices0) {
					@Override
					public boolean contains(int v) {
						return 0 <= v && v < matched.length && matched[v] != -1;
					}
				};
			}
			return matchedVertices;
		}

		@Override
		public IntSet unmatchedVertices() {
			if (unmatchedVertices == null) {
				int unmatchedCount = 0;
				for (int v = 0; v < matched.length; v++)
					if (matched[v] == -1)
						unmatchedCount++;
				int[] unmatchedVertices0 = new int[unmatchedCount];
				for (int i = 0, v = 0; v < matched.length; v++)
					if (matched[v] == -1)
						unmatchedVertices0[i++] = v;
				unmatchedVertices = new ImmutableIntArraySet(unmatchedVertices0) {
					@Override
					public boolean contains(int v) {
						return 0 <= v && v < matched.length && matched[v] == -1;
					}
				};
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
			for (int n = g.vertices().size(), v = 0; v < n; v++) {
				int e = matched[v];
				if (e != -1 && v == g.edgeSource(e)) {
					assert g.edgeSource(e) != g.edgeTarget(e);
					edgesCount++;
				}
			}
			int[] edges0 = new int[edgesCount];
			for (int i = 0, n = g.vertices().size(), v = 0; v < n; v++) {
				int e = matched[v];
				if (e != -1 && v == g.edgeSource(e)) {
					assert g.edgeSource(e) != g.edgeTarget(e);
					edges0[i++] = e;
				}
			}
			edges = new ImmutableIntArraySet(edges0) {

				@Override
				public boolean contains(int e) {
					return 0 <= e && e < g.edges().size() && matched[g.edgeSource(e)] == e;
				}
			};
		}

		@Override
		public String toString() {
			return edges().toString();
		}

		@Override
		public boolean isPerfect() {
			for (int e : matched)
				if (e == -1)
					return false;
			return true;
		}

	}

	static abstract class AbstractMatchingImpl implements MatchingAlgo {

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> Matching<V, E> computeMaximumCardinalityMatching(Graph<V, E> g) {
			if (g instanceof IndexGraph) {
				return (Matching<V, E>) computeMaximumCardinalityMatching((IndexGraph) g);

			} else if (g instanceof IntGraph) {
				IndexGraph iGraph = g.indexGraph();
				IndexIntIdMap viMap = ((IntGraph) g).indexGraphVerticesMap();
				IndexIntIdMap eiMap = ((IntGraph) g).indexGraphEdgesMap();
				IMatching indexMatch = computeMaximumCardinalityMatching(iGraph);
				return (Matching<V, E>) new IntMatchingFromIndexMatching(indexMatch, viMap, eiMap);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IMatching indexMatch = computeMaximumCardinalityMatching(iGraph);
				return new ObjMatchingFromIndexMatching<>(indexMatch, viMap, eiMap);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> Matching<V, E> computeMaximumWeightedMatching(Graph<V, E> g, WeightFunction<E> w) {
			if (g instanceof IndexGraph) {
				return (Matching<V, E>) computeMaximumWeightedMatching((IndexGraph) g,
						WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w));

			} else if (g instanceof IntGraph) {
				IndexGraph iGraph = g.indexGraph();
				IndexIntIdMap viMap = ((IntGraph) g).indexGraphVerticesMap();
				IndexIntIdMap eiMap = ((IntGraph) g).indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc((WeightFunction<Integer>) w, eiMap);
				IMatching indexMatch = computeMaximumWeightedMatching(iGraph, iw);
				return (Matching<V, E>) new IntMatchingFromIndexMatching(indexMatch, viMap, eiMap);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
				IMatching indexMatch = computeMaximumWeightedMatching(iGraph, iw);
				return new ObjMatchingFromIndexMatching<>(indexMatch, viMap, eiMap);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> Matching<V, E> computeMinimumWeightedMatching(Graph<V, E> g, WeightFunction<E> w) {
			if (g instanceof IndexGraph) {
				return (Matching<V, E>) computeMinimumWeightedMatching((IndexGraph) g,
						WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w));

			} else if (g instanceof IntGraph) {
				IndexGraph iGraph = g.indexGraph();
				IndexIntIdMap viMap = ((IntGraph) g).indexGraphVerticesMap();
				IndexIntIdMap eiMap = ((IntGraph) g).indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc((WeightFunction<Integer>) w, eiMap);
				IMatching indexMatch = computeMinimumWeightedMatching(iGraph, iw);
				return (Matching<V, E>) new IntMatchingFromIndexMatching(indexMatch, viMap, eiMap);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
				IMatching indexMatch = computeMinimumWeightedMatching(iGraph, iw);
				return new ObjMatchingFromIndexMatching<>(indexMatch, viMap, eiMap);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> Matching<V, E> computeMaximumWeightedPerfectMatching(Graph<V, E> g, WeightFunction<E> w) {
			if (g instanceof IndexGraph) {
				return (Matching<V, E>) computeMaximumWeightedPerfectMatching((IndexGraph) g,
						WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w));

			} else if (g instanceof IntGraph) {
				IndexGraph iGraph = g.indexGraph();
				IndexIntIdMap viMap = ((IntGraph) g).indexGraphVerticesMap();
				IndexIntIdMap eiMap = ((IntGraph) g).indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc((WeightFunction<Integer>) w, eiMap);
				IMatching indexMatch = computeMaximumWeightedPerfectMatching(iGraph, iw);
				return (Matching<V, E>) new IntMatchingFromIndexMatching(indexMatch, viMap, eiMap);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
				IMatching indexMatch = computeMaximumWeightedPerfectMatching(iGraph, iw);
				return new ObjMatchingFromIndexMatching<>(indexMatch, viMap, eiMap);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> Matching<V, E> computeMinimumWeightedPerfectMatching(Graph<V, E> g, WeightFunction<E> w) {
			if (g instanceof IndexGraph) {
				return (Matching<V, E>) computeMinimumWeightedPerfectMatching((IndexGraph) g,
						WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w));

			} else if (g instanceof IntGraph) {
				IndexGraph iGraph = g.indexGraph();
				IndexIntIdMap viMap = ((IntGraph) g).indexGraphVerticesMap();
				IndexIntIdMap eiMap = ((IntGraph) g).indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc((WeightFunction<Integer>) w, eiMap);
				IMatching indexMatch = computeMinimumWeightedPerfectMatching(iGraph, iw);
				return (Matching<V, E>) new IntMatchingFromIndexMatching(indexMatch, viMap, eiMap);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
				IMatching indexMatch = computeMinimumWeightedPerfectMatching(iGraph, iw);
				return new ObjMatchingFromIndexMatching<>(indexMatch, viMap, eiMap);
			}
		}

		abstract IMatching computeMaximumCardinalityMatching(IndexGraph g);

		abstract IMatching computeMaximumWeightedMatching(IndexGraph g, IWeightFunction w);

		abstract IMatching computeMinimumWeightedMatching(IndexGraph g, IWeightFunction w);

		abstract IMatching computeMaximumWeightedPerfectMatching(IndexGraph g, IWeightFunction w);

		abstract IMatching computeMinimumWeightedPerfectMatching(IndexGraph g, IWeightFunction w);

	}

	static abstract class AbstractCardinalityMatchingImpl extends AbstractMatchingImpl {

		@Override
		IMatching computeMaximumWeightedMatching(IndexGraph g, IWeightFunction w) {
			Assertions.Graphs.onlyCardinality(w);
			return computeMaximumCardinalityMatching(g);
		}

		@Override
		IMatching computeMinimumWeightedMatching(IndexGraph g, IWeightFunction w) {
			Assertions.Graphs.onlyCardinality(w);
			return Matchings.MatchingImpl.emptyMatching(g);
		}

		@Override
		IMatching computeMaximumWeightedPerfectMatching(IndexGraph g, IWeightFunction w) {
			Assertions.Graphs.onlyCardinality(w);
			return computeMaximumCardinalityMatching(g);
		}

		@Override
		IMatching computeMinimumWeightedPerfectMatching(IndexGraph g, IWeightFunction w) {
			Assertions.Graphs.onlyCardinality(w);
			return computeMaximumCardinalityMatching(g);
		}

	}

	static abstract class AbstractWeightedMatchingImpl extends AbstractMatchingImpl {

		@Override
		IMatching computeMaximumCardinalityMatching(IndexGraph g) {
			return computeMaximumWeightedMatching(g, IWeightFunction.CardinalityWeightFunction);
		}

	}

	static abstract class AbstractMaximumMatchingImpl extends AbstractWeightedMatchingImpl {

		@Override
		IMatching computeMinimumWeightedMatching(IndexGraph g, IWeightFunction w) {
			return computeMaximumWeightedMatching(g, e -> -w.weight(e));
		}

		@Override
		IMatching computeMinimumWeightedPerfectMatching(IndexGraph g, IWeightFunction w) {
			return computeMaximumWeightedPerfectMatching(g, e -> -w.weight(e));
		}

	}

	static abstract class AbstractMinimumMatchingImpl extends AbstractWeightedMatchingImpl {

		@Override
		IMatching computeMaximumWeightedMatching(IndexGraph g, IWeightFunction w) {
			return computeMinimumWeightedMatching(g, e -> -w.weight(e));
		}

		@Override
		IMatching computeMaximumWeightedPerfectMatching(IndexGraph g, IWeightFunction w) {
			return computeMinimumWeightedPerfectMatching(g, e -> -w.weight(e));
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
		public <V, E> Matching<V, E> computeMaximumCardinalityMatching(Graph<V, E> g) {
			boolean bipartite = isBipartite(g);
			if (bipartite) {
				return cardinalityBipartiteAlgo.computeMaximumCardinalityMatching(g);
			} else {
				return cardinalityGeneralAlgo.computeMaximumCardinalityMatching(g);
			}
		}

		@Override
		public <V, E> Matching<V, E> computeMaximumWeightedMatching(Graph<V, E> g, WeightFunction<E> w) {
			boolean cardinality = isCardinality(w);
			boolean bipartite = isBipartite(g);
			if (cardinality && bipartite)
				return cardinalityBipartiteAlgo.computeMaximumWeightedMatching(g, w);
			if (cardinality && !bipartite)
				return cardinalityGeneralAlgo.computeMaximumWeightedMatching(g, w);
			if (!cardinality && bipartite)
				return weightedBipartiteAlgo.computeMaximumWeightedMatching(g, w);
			if (!cardinality && !bipartite)
				return weightedGeneralAlgo.computeMaximumWeightedMatching(g, w);
			throw new AssertionError();
		}

		@Override
		public <V, E> Matching<V, E> computeMinimumWeightedMatching(Graph<V, E> g, WeightFunction<E> w) {
			boolean cardinality = isCardinality(w);
			boolean bipartite = isBipartite(g);
			if (cardinality && bipartite)
				return cardinalityBipartiteAlgo.computeMinimumWeightedMatching(g, w);
			if (cardinality && !bipartite)
				return cardinalityGeneralAlgo.computeMinimumWeightedMatching(g, w);
			if (!cardinality && bipartite)
				return weightedBipartiteAlgo.computeMinimumWeightedMatching(g, w);
			if (!cardinality && !bipartite)
				return weightedGeneralAlgo.computeMinimumWeightedMatching(g, w);
			throw new AssertionError();
		}

		@Override
		public <V, E> Matching<V, E> computeMaximumWeightedPerfectMatching(Graph<V, E> g, WeightFunction<E> w) {
			boolean cardinality = isCardinality(w);
			boolean bipartite = isBipartite(g);
			if (cardinality && bipartite)
				return cardinalityBipartiteAlgo.computeMaximumWeightedPerfectMatching(g, w);
			if (cardinality && !bipartite)
				return cardinalityGeneralAlgo.computeMaximumWeightedPerfectMatching(g, w);
			if (!cardinality && bipartite)
				return weightedBipartiteAlgo.computeMaximumWeightedPerfectMatching(g, w);
			if (!cardinality && !bipartite)
				return weightedGeneralAlgo.computeMaximumWeightedPerfectMatching(g, w);
			throw new AssertionError();
		}

		@Override
		public <V, E> Matching<V, E> computeMinimumWeightedPerfectMatching(Graph<V, E> g, WeightFunction<E> w) {
			boolean cardinality = isCardinality(w);
			boolean bipartite = isBipartite(g);
			if (cardinality && bipartite)
				return cardinalityBipartiteAlgo.computeMinimumWeightedPerfectMatching(g, w);
			if (cardinality && !bipartite)
				return cardinalityGeneralAlgo.computeMinimumWeightedPerfectMatching(g, w);
			if (!cardinality && bipartite)
				return weightedBipartiteAlgo.computeMinimumWeightedPerfectMatching(g, w);
			if (!cardinality && !bipartite)
				return weightedGeneralAlgo.computeMinimumWeightedPerfectMatching(g, w);
			throw new AssertionError();
		}

		private static boolean isCardinality(WeightFunction<?> w) {
			return w == null || w == WeightFunction.CardinalityWeightFunction
					|| w == IWeightFunction.CardinalityWeightFunction;
		}

		private static boolean isBipartite(Graph<?, ?> g) {
			return BipartiteGraphs.getExistingPartition(g).isPresent();
		}

	}

	private static class ObjMatchingFromIndexMatching<V, E> implements Matching<V, E> {

		private final IMatching match;
		private final IndexIdMap<V> viMap;
		private final IndexIdMap<E> eiMap;

		ObjMatchingFromIndexMatching(IMatching match, IndexIdMap<V> viMap, IndexIdMap<E> eiMap) {
			this.match = Objects.requireNonNull(match);
			this.viMap = Objects.requireNonNull(viMap);
			this.eiMap = Objects.requireNonNull(eiMap);
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
			return e == -1 ? null : eiMap.indexToId(e);
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

	private static class IntMatchingFromIndexMatching implements IMatching {

		private final IMatching match;
		private final IndexIntIdMap viMap;
		private final IndexIntIdMap eiMap;

		IntMatchingFromIndexMatching(IMatching match, IndexIntIdMap viMap, IndexIntIdMap eiMap) {
			this.match = Objects.requireNonNull(match);
			this.viMap = Objects.requireNonNull(viMap);
			this.eiMap = Objects.requireNonNull(eiMap);
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
			return e == -1 ? -1 : eiMap.indexToIdInt(e);
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
