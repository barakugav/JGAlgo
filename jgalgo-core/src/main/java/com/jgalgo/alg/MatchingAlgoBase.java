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
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.Assertions;

interface MatchingAlgoBase extends MatchingAlgo {

	@SuppressWarnings("unchecked")
	@Override
	default <V, E> Matching<V, E> computeMaximumMatching(Graph<V, E> g, WeightFunction<E> w) {
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
	default <V, E> Matching<V, E> computeMinimumMatching(Graph<V, E> g, WeightFunction<E> w) {
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
	default <V, E> Matching<V, E> computeMaximumPerfectMatching(Graph<V, E> g, WeightFunction<E> w) {
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
	default <V, E> Matching<V, E> computeMinimumPerfectMatching(Graph<V, E> g, WeightFunction<E> w) {
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

	IMatching computeMaximumWeightedMatching(IndexGraph g, IWeightFunction w);

	IMatching computeMinimumWeightedMatching(IndexGraph g, IWeightFunction w);

	IMatching computeMaximumWeightedPerfectMatching(IndexGraph g, IWeightFunction w);

	IMatching computeMinimumWeightedPerfectMatching(IndexGraph g, IWeightFunction w);

	@SuppressWarnings("unchecked")
	private static <V, E> Matching<V, E> matchingFromIndexMatching(Graph<V, E> g, IMatching indexResult) {
		assert !(g instanceof IndexGraph);
		if (g instanceof IntGraph) {
			return (Matching<V, E>) new Matchings.IntMatchingFromIndexMatching((IntGraph) g, indexResult);
		} else {
			return new Matchings.ObjMatchingFromIndexMatching<>(g, indexResult);
		}
	}

	static interface Cardinality extends MatchingAlgoBase {

		abstract IMatching computeMaximumCardinalityMatching(IndexGraph g);

		@Override
		default IMatching computeMaximumWeightedMatching(IndexGraph g, IWeightFunction w) {
			Assertions.onlyCardinality(w);
			return computeMaximumCardinalityMatching(g);
		}

		@Override
		default IMatching computeMinimumWeightedMatching(IndexGraph g, IWeightFunction w) {
			Assertions.onlyCardinality(w);
			int[] matched = new int[g.vertices().size()];
			Arrays.fill(matched, -1);
			return new Matchings.IndexMatching(g, matched);
		}

		@Override
		default IMatching computeMaximumWeightedPerfectMatching(IndexGraph g, IWeightFunction w) {
			Assertions.onlyCardinality(w);
			return computeMaximumCardinalityMatching(g);
		}

		@Override
		default IMatching computeMinimumWeightedPerfectMatching(IndexGraph g, IWeightFunction w) {
			Assertions.onlyCardinality(w);
			return computeMaximumCardinalityMatching(g);
		}
	}

	static interface MaximumBased extends MatchingAlgoBase {

		@Override
		default IMatching computeMinimumWeightedMatching(IndexGraph g, IWeightFunction w) {
			return computeMaximumWeightedMatching(g, WeightFunctions.negate(w));
		}

		@Override
		default IMatching computeMinimumWeightedPerfectMatching(IndexGraph g, IWeightFunction w) {
			if (WeightFunction.isCardinality(w)) {
				/* minimum and maximum weighted perfect matching are equivalent for unweighed graphs */
				return computeMaximumWeightedPerfectMatching(g, null);
			} else {
				return computeMaximumWeightedPerfectMatching(g, WeightFunctions.negate(w));
			}
		}
	}

	static interface MinimumBased extends MatchingAlgoBase {

		@Override
		default IMatching computeMaximumWeightedMatching(IndexGraph g, IWeightFunction w) {
			return computeMinimumWeightedMatching(g, WeightFunctions.negate(w));
		}

		@Override
		default IMatching computeMaximumWeightedPerfectMatching(IndexGraph g, IWeightFunction w) {
			if (WeightFunction.isCardinality(w)) {
				/* minimum and maximum weighted perfect matching are equivalent for unweighed graphs */
				return computeMinimumWeightedPerfectMatching(g, null);
			} else {
				return computeMinimumWeightedPerfectMatching(g, WeightFunctions.negate(w));
			}
		}
	}

}
