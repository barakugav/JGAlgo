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

import static com.jgalgo.internal.util.Range.range;
import java.util.Objects;
import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Abstract class for computing matchings in graphs.
 *
 * <p>
 * The class implements the interface by solving the problem on the index graph and then maps the results back to the
 * original graph. The implementation for the index graph is abstract and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class MatchingAlgoAbstract implements MatchingAlgo {

	/**
	 * Default constructor.
	 */
	public MatchingAlgoAbstract() {}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> Matching<V, E> computeMaximumMatching(Graph<V, E> g, WeightFunction<E> w) {
		if (g instanceof IndexGraph) {
			return (Matching<V, E>) computeMaximumWeightedMatching((IndexGraph) g,
					WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w));

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			IMatching indexMatch = computeMaximumWeightedMatching(iGraph, iw);
			return matchingFromIndexMatching(g, indexMatch);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> Matching<V, E> computeMinimumMatching(Graph<V, E> g, WeightFunction<E> w) {
		if (g instanceof IndexGraph) {
			return (Matching<V, E>) computeMinimumWeightedMatching((IndexGraph) g,
					WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w));

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			IMatching indexMatch = computeMinimumWeightedMatching(iGraph, iw);
			return matchingFromIndexMatching(g, indexMatch);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> Matching<V, E> computeMaximumPerfectMatching(Graph<V, E> g, WeightFunction<E> w) {
		if (g instanceof IndexGraph) {
			return (Matching<V, E>) computeMaximumWeightedPerfectMatching((IndexGraph) g,
					WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w));

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			IMatching indexMatch = computeMaximumWeightedPerfectMatching(iGraph, iw);
			return matchingFromIndexMatching(g, indexMatch);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> Matching<V, E> computeMinimumPerfectMatching(Graph<V, E> g, WeightFunction<E> w) {
		if (g instanceof IndexGraph) {
			return (Matching<V, E>) computeMinimumWeightedPerfectMatching((IndexGraph) g,
					WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w));

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			IMatching indexMatch = computeMinimumWeightedPerfectMatching(iGraph, iw);
			return matchingFromIndexMatching(g, indexMatch);
		}
	}

	protected abstract IMatching computeMaximumWeightedMatching(IndexGraph g, IWeightFunction w);

	protected abstract IMatching computeMinimumWeightedMatching(IndexGraph g, IWeightFunction w);

	protected abstract IMatching computeMaximumWeightedPerfectMatching(IndexGraph g, IWeightFunction w);

	protected abstract IMatching computeMinimumWeightedPerfectMatching(IndexGraph g, IWeightFunction w);

	/**
	 * Result of a matching algorithm on an index graph.
	 *
	 * @author Barak Ugav
	 */
	protected static class IndexMatching implements IMatching {

		private final IndexGraph g;
		private IntSet edges;
		private final int[] matched;
		private IntSet matchedVertices;
		private IntSet unmatchedVertices;

		/**
		 * Construct a new matching over an index graph.
		 *
		 * @param g       the index graph
		 * @param matched an array representing the matching of vertices to edges, using an index to index mapping, or
		 *                    {@code -1} for an unmatched vertex. The provided array <b>is not copied</b> and should not
		 *                    be modified after the matching is created
		 */
		public IndexMatching(IndexGraph g, int[] matched) {
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

	@SuppressWarnings("unchecked")
	private static <V, E> Matching<V, E> matchingFromIndexMatching(Graph<V, E> g, IMatching indexResult) {
		assert !(g instanceof IndexGraph);
		if (g instanceof IntGraph) {
			return (Matching<V, E>) new IntMatchingFromIndexMatching((IntGraph) g, indexResult);
		} else {
			return new ObjMatchingFromIndexMatching<>(g, indexResult);
		}
	}

	private static class ObjMatchingFromIndexMatching<V, E> implements Matching<V, E> {

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

	private static class IntMatchingFromIndexMatching implements IMatching {

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

	static IWeightFunction negate(IWeightFunction w) {
		w = IWeightFunction.replaceNullWeightFunc(w);
		if (WeightFunction.isInteger(w)) {
			IWeightFunctionInt w0 = (IWeightFunctionInt) w;
			IWeightFunctionInt w1 = e -> -w0.weightInt(e);
			return w1;
		} else {
			IWeightFunction w0 = w;
			return e -> -w0.weight(e);
		}
	}

}
