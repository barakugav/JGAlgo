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
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntSet;

class EdgeCovers {

	static abstract class AbstractImpl implements EdgeCover {

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> EdgeCover.Result<V, E> computeMinimumEdgeCover(Graph<V, E> g, WeightFunction<E> w) {
			if (g instanceof IndexGraph) {
				IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
				return (EdgeCover.Result<V, E>) computeMinimumEdgeCover((IndexGraph) g, w0);

			} else if (g instanceof IntGraph) {
				IndexGraph iGraph = g.indexGraph();
				IndexIntIdMap eiMap = ((IntGraph) g).indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc((WeightFunction<Integer>) w, eiMap);
				EdgeCover.IResult indexResult = computeMinimumEdgeCover(iGraph, iw);
				return (EdgeCover.Result<V, E>) new IntResultFromIndexResult(indexResult, eiMap);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
				EdgeCover.IResult indexResult = computeMinimumEdgeCover(iGraph, iw);
				return new ObjResultFromIndexResult<>(indexResult, eiMap);
			}
		}

		abstract EdgeCover.IResult computeMinimumEdgeCover(IndexGraph g, IWeightFunction w);

	}

	static class ResultImpl implements EdgeCover.IResult {

		private final IndexGraph g;
		private final BitSet cover;
		private IntSet edges;

		ResultImpl(IndexGraph g, BitSet cover) {
			this.g = Objects.requireNonNull(g);
			this.cover = Objects.requireNonNull(cover);
		}

		@Override
		public IntSet edges() {
			if (edges == null) {
				edges = new ImmutableIntArraySet(JGAlgoUtils.toArray(cover)) {
					@Override
					public boolean contains(int e) {
						return 0 <= e && e < g.edges().size() && cover.get(e);
					}
				};
			}
			return edges;
		}

		@Override
		public boolean isInCover(int edge) {
			if (!g.edges().contains(edge))
				throw new IndexOutOfBoundsException(edge);
			return cover.get(edge);
		}

		@Override
		public String toString() {
			return edges().toString();
		}
	}
	private static class IntResultFromIndexResult implements EdgeCover.IResult {

		private final EdgeCover.IResult indexRes;
		private final IndexIntIdMap eiMap;

		IntResultFromIndexResult(EdgeCover.IResult indexRes, IndexIntIdMap eiMap) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		@Override
		public IntSet edges() {
			return IndexIdMaps.indexToIdSet(indexRes.edges(), eiMap);
		}

		@Override
		public boolean isInCover(int edge) {
			return indexRes.isInCover(eiMap.idToIndex(edge));
		}

		@Override
		public String toString() {
			return edges().toString();
		}
	}
	private static class ObjResultFromIndexResult<V, E> implements EdgeCover.Result<V, E> {

		private final EdgeCover.IResult indexRes;
		private final IndexIdMap<E> eiMap;

		ObjResultFromIndexResult(EdgeCover.IResult indexRes, IndexIdMap<E> eiMap) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		@Override
		public Set<E> edges() {
			return IndexIdMaps.indexToIdSet(indexRes.edges(), eiMap);
		}

		@Override
		public boolean isInCover(E edge) {
			return indexRes.isInCover(eiMap.idToIndex(edge));
		}

		@Override
		public String toString() {
			return edges().toString();
		}
	}

}
