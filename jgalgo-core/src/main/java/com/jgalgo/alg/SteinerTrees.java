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

import java.util.Collection;
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
import com.jgalgo.internal.util.ImmutableIntArraySet;
import com.jgalgo.internal.util.IntContainers;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;

class SteinerTrees {

	static abstract class AbstractImpl implements SteinerTreeAlgo {

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> SteinerTreeAlgo.Result<V, E> computeSteinerTree(Graph<V, E> g, WeightFunction<E> w,
				Collection<V> terminals) {
			if (g instanceof IndexGraph) {
				IntCollection terminals0 = IntContainers.toIntCollection((Collection<Integer>) terminals);
				IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
				return (SteinerTreeAlgo.Result<V, E>) computeSteinerTree((IndexGraph) g, w0, terminals0);

			} else if (g instanceof IntGraph) {
				IndexGraph iGraph = g.indexGraph();
				IndexIntIdMap viMap = ((IntGraph) g).indexGraphVerticesMap();
				IndexIntIdMap eiMap = ((IntGraph) g).indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc((WeightFunction<Integer>) w, eiMap);
				IntCollection iTerminals = IndexIdMaps.idToIndexCollection((Collection<Integer>) terminals, viMap);

				SteinerTreeAlgo.IResult indexResult = computeSteinerTree(iGraph, iw, iTerminals);
				return (SteinerTreeAlgo.Result<V, E>) new IntResultFromIndexResult(indexResult, eiMap);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
				IntCollection iTerminals = IndexIdMaps.idToIndexCollection(terminals, viMap);

				SteinerTreeAlgo.IResult indexResult = computeSteinerTree(iGraph, iw, iTerminals);
				return new ObjResultFromIndexResult<>(indexResult, eiMap);
			}
		}

		abstract SteinerTreeAlgo.IResult computeSteinerTree(IndexGraph g, IWeightFunction w, IntCollection terminals);

	}

	static class ResultImpl implements SteinerTreeAlgo.IResult {

		private final IntSet edges;
		static final SteinerTreeAlgo.IResult Empty = new ResultImpl(IntArrays.EMPTY_ARRAY);

		ResultImpl(IntSet edges) {
			this.edges = IntSets.unmodifiable(Objects.requireNonNull(edges));
		}

		ResultImpl(int[] edges) {
			this.edges = ImmutableIntArraySet.withNaiveContains(edges);
		}

		@Override
		public IntSet edges() {
			return edges;
		}

		@Override
		public String toString() {
			return edges().toString();
		}

	}

	private static class ObjResultFromIndexResult<V, E> implements SteinerTreeAlgo.Result<V, E> {

		private final SteinerTreeAlgo.IResult indexRes;
		private final IndexIdMap<E> eiMap;

		ObjResultFromIndexResult(SteinerTreeAlgo.IResult res, IndexIdMap<E> eiMap) {
			this.indexRes = Objects.requireNonNull(res);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		@Override
		public Set<E> edges() {
			return IndexIdMaps.indexToIdSet(indexRes.edges(), eiMap);
		}
	}

	private static class IntResultFromIndexResult implements SteinerTreeAlgo.IResult {

		private final SteinerTreeAlgo.IResult indexRes;
		private final IndexIntIdMap eiMap;

		IntResultFromIndexResult(SteinerTreeAlgo.IResult res, IndexIntIdMap eiMap) {
			this.indexRes = Objects.requireNonNull(res);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		@Override
		public IntSet edges() {
			return IndexIdMaps.indexToIdSet(indexRes.edges(), eiMap);
		}
	}

}
