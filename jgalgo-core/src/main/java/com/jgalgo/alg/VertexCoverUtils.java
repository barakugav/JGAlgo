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
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntSet;

class VertexCoverUtils {

	static abstract class AbstractImpl implements VertexCover {

		@Override
		public VertexCover.Result computeMinimumVertexCover(IntGraph g, IWeightFunction w) {
			if (g instanceof IndexGraph)
				return computeMinimumVertexCover((IndexGraph) g, w);

			IndexGraph iGraph = g.indexGraph();
			IndexIntIdMap viMap = g.indexGraphVerticesMap();
			w = IndexIdMaps.idToIndexWeightFunc(w, viMap);

			VertexCover.Result indexResult = computeMinimumVertexCover(iGraph, w);
			return new ResultFromIndexResult(indexResult, viMap);
		}

		abstract VertexCover.Result computeMinimumVertexCover(IndexGraph g, IWeightFunction w);

	}

	static class ResultImpl implements VertexCover.Result {

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

	private static class ResultFromIndexResult implements VertexCover.Result {

		private final VertexCover.Result res;
		private final IndexIntIdMap viMap;

		ResultFromIndexResult(VertexCover.Result res, IndexIntIdMap viMap) {
			this.res = Objects.requireNonNull(res);
			this.viMap = Objects.requireNonNull(viMap);
		}

		@Override
		public IntSet vertices() {
			return IndexIdMaps.indexToIdSet(res.vertices(), viMap);
		}

		@Override
		public boolean isInCover(int vertex) {
			return res.isInCover(viMap.idToIndex(vertex));
		}

		@Override
		public String toString() {
			return vertices().toString();
		}
	}

}
