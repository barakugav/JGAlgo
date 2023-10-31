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

class VertexCoverUtils {

	static abstract class AbstractImpl implements VertexCover {

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> VertexCover.Result<V, E> computeMinimumVertexCover(Graph<V, E> g, WeightFunction<V> w) {
			if (g instanceof IndexGraph) {
				IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
				return (VertexCover.Result<V, E>) computeMinimumVertexCover((IndexGraph) g, w0);

			} else if (g instanceof IntGraph) {
				IndexGraph iGraph = g.indexGraph();
				IndexIntIdMap viMap = ((IntGraph) g).indexGraphVerticesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc((WeightFunction<Integer>) w, viMap);
				VertexCover.IResult indexResult = computeMinimumVertexCover(iGraph, iw);
				return (VertexCover.Result<V, E>) new IntResultFromIndexResult(indexResult, viMap);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, viMap);
				VertexCover.IResult indexResult = computeMinimumVertexCover(iGraph, iw);
				return new ObjResultFromIndexResult<>(indexResult, viMap);
			}
		}

		abstract VertexCover.IResult computeMinimumVertexCover(IndexGraph g, IWeightFunction w);

	}

	static class ResultImpl implements VertexCover.IResult {

		private final IndexGraph g;
		private final BitSet cover;
		private IntSet vertices;

		ResultImpl(IndexGraph g, BitSet cover) {
			this.g = Objects.requireNonNull(g);
			this.cover = Objects.requireNonNull(cover);
		}

		@Override
		public IntSet vertices() {
			if (vertices == null) {
				vertices = new ImmutableIntArraySet(JGAlgoUtils.toArray(cover)) {
					@Override
					public boolean contains(int v) {
						return 0 <= v && v < g.vertices().size() && cover.get(v);
					}
				};
			}
			return vertices;
		}

		@Override
		public boolean isInCover(int vertex) {
			if (!g.vertices().contains(vertex))
				throw new IndexOutOfBoundsException(vertex);
			return cover.get(vertex);
		}

		@Override
		public String toString() {
			return vertices().toString();
		}
	}

	private static class ObjResultFromIndexResult<V, E> implements VertexCover.Result<V, E> {

		private final VertexCover.IResult indexRes;
		private final IndexIdMap<V> viMap;

		ObjResultFromIndexResult(VertexCover.IResult indexRes, IndexIdMap<V> viMap) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.viMap = Objects.requireNonNull(viMap);
		}

		@Override
		public Set<V> vertices() {
			return IndexIdMaps.indexToIdSet(indexRes.vertices(), viMap);
		}

		@Override
		public boolean isInCover(V vertex) {
			return indexRes.isInCover(viMap.idToIndex(vertex));
		}

		@Override
		public String toString() {
			return vertices().toString();
		}
	}

	private static class IntResultFromIndexResult implements VertexCover.IResult {

		private final VertexCover.IResult indexRes;
		private final IndexIntIdMap viMap;

		IntResultFromIndexResult(VertexCover.IResult indexRes, IndexIntIdMap viMap) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.viMap = Objects.requireNonNull(viMap);
		}

		@Override
		public IntSet vertices() {
			return IndexIdMaps.indexToIdSet(indexRes.vertices(), viMap);
		}

		@Override
		public boolean isInCover(int vertex) {
			return indexRes.isInCover(viMap.idToIndex(vertex));
		}

		@Override
		public String toString() {
			return vertices().toString();
		}
	}

}
