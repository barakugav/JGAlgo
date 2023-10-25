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
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;

class VertexCoverUtils {

	static abstract class AbstractImpl implements VertexCover {

		@Override
		public VertexCover.Result computeMinimumVertexCover(Graph g, WeightFunction w) {
			if (g instanceof IndexGraph)
				return computeMinimumVertexCover((IndexGraph) g, w);

			IndexGraph iGraph = g.indexGraph();
			IndexIdMap viMap = g.indexGraphVerticesMap();
			w = IndexIdMaps.idToIndexWeightFunc(w, viMap);

			VertexCover.Result indexResult = computeMinimumVertexCover(iGraph, w);
			return new ResultFromIndexResult(indexResult, viMap);
		}

		abstract VertexCover.Result computeMinimumVertexCover(IndexGraph g, WeightFunction w);

	}

	static class ResultImpl implements VertexCover.Result {

		private final IndexGraph g;
		private final BitSet cover;
		private IntCollection vertices;

		ResultImpl(IndexGraph g, BitSet cover) {
			this.g = Objects.requireNonNull(g);
			this.cover = Objects.requireNonNull(cover);
		}

		@Override
		public IntCollection vertices() {
			if (this.vertices == null) {
				IntList vertices = new IntArrayList(cover.cardinality());
				for (int v : JGAlgoUtils.iterable(cover))
					vertices.add(v);
				this.vertices = IntLists.unmodifiable(vertices);
			}
			return this.vertices;
		}

		@Override
		public boolean isInCover(int vertex) {
			if (!g.vertices().contains(vertex))
				throw new IndexOutOfBoundsException(vertex);
			return cover.get(vertex);
		}

	}

	private static class ResultFromIndexResult implements VertexCover.Result {

		private final VertexCover.Result res;
		private final IndexIdMap viMap;

		ResultFromIndexResult(VertexCover.Result res, IndexIdMap viMap) {
			this.res = Objects.requireNonNull(res);
			this.viMap = Objects.requireNonNull(viMap);
		}

		@Override
		public IntCollection vertices() {
			return IndexIdMaps.indexToIdCollection(res.vertices(), viMap);
		}

		@Override
		public boolean isInCover(int vertex) {
			return res.isInCover(viMap.idToIndex(vertex));
		}

	}

}
