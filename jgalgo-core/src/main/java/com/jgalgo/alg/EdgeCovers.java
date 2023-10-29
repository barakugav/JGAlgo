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

class EdgeCovers {

	static abstract class AbstractImpl implements EdgeCover {

		@Override
		public EdgeCover.Result computeMinimumEdgeCover(IntGraph g, IWeightFunction w) {
			if (g instanceof IndexGraph)
				return computeMinimumEdgeCover((IndexGraph) g, w);

			IndexGraph iGraph = g.indexGraph();
			IndexIntIdMap eiMap = g.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);

			EdgeCover.Result indexResult = computeMinimumEdgeCover(iGraph, iw);
			return new ResultFromIndexResult(indexResult, eiMap);
		}

		abstract EdgeCover.Result computeMinimumEdgeCover(IndexGraph g, IWeightFunction w);

	}

	static class ResultImpl implements EdgeCover.Result {

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
	private static class ResultFromIndexResult implements EdgeCover.Result {

		private final EdgeCover.Result res;
		private final IndexIntIdMap eiMap;

		ResultFromIndexResult(EdgeCover.Result res, IndexIntIdMap eiMap) {
			this.res = Objects.requireNonNull(res);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		@Override
		public IntSet edges() {
			return IndexIdMaps.indexToIdSet(res.edges(), eiMap);
		}

		@Override
		public boolean isInCover(int edge) {
			return res.isInCover(eiMap.idToIndex(edge));
		}

		@Override
		public String toString() {
			return edges().toString();
		}
	}

}
