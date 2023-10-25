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

import java.util.Objects;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntCollections;

class MinimumSpanningTreeUtils {

	static abstract class AbstractUndirected implements MinimumSpanningTree {

		@Override
		public MinimumSpanningTree.Result computeMinimumSpanningTree(Graph g, WeightFunction w) {
			if (g instanceof IndexGraph)
				return computeMinimumSpanningTree((IndexGraph) g, w);

			IndexGraph iGraph = g.indexGraph();
			IndexIdMap eiMap = g.indexGraphEdgesMap();
			w = IndexIdMaps.idToIndexWeightFunc(w, eiMap);

			MinimumSpanningTree.Result indexResult = computeMinimumSpanningTree(iGraph, w);
			return new ResultFromIndexResult(indexResult, eiMap);
		}

		abstract MinimumSpanningTree.Result computeMinimumSpanningTree(IndexGraph g, WeightFunction w);

	}

	static abstract class AbstractDirected implements MinimumDirectedSpanningTree {

		@Override
		public MinimumSpanningTree.Result computeMinimumDirectedSpanningTree(Graph g, WeightFunction w, int root) {
			if (g instanceof IndexGraph)
				return computeMinimumDirectedSpanningTree((IndexGraph) g, w, root);

			IndexGraph iGraph = g.indexGraph();
			IndexIdMap viMap = g.indexGraphVerticesMap();
			IndexIdMap eiMap = g.indexGraphEdgesMap();
			w = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			int iRoot = viMap.idToIndex(root);

			MinimumSpanningTree.Result indexResult = computeMinimumDirectedSpanningTree(iGraph, w, iRoot);
			return new ResultFromIndexResult(indexResult, eiMap);
		}

		abstract MinimumSpanningTree.Result computeMinimumDirectedSpanningTree(IndexGraph g, WeightFunction w,
				int root);

	}

	static class ResultImpl implements MinimumSpanningTree.Result {

		private final IntCollection edges;
		static final MinimumSpanningTree.Result Empty = new ResultImpl(IntArrays.EMPTY_ARRAY);

		ResultImpl(IntCollection edges) {
			this.edges = IntCollections.unmodifiable(Objects.requireNonNull(edges));
		}

		ResultImpl(int[] edges) {
			this.edges = ImmutableIntArraySet.withNaiveContains(edges);
		}

		@Override
		public IntCollection edges() {
			return edges;
		}

		@Override
		public String toString() {
			return edges().toString();
		}

	}

	private static class ResultFromIndexResult implements MinimumSpanningTree.Result {

		private final MinimumSpanningTree.Result res;
		private final IndexIdMap eiMap;

		ResultFromIndexResult(MinimumSpanningTree.Result res, IndexIdMap eiMap) {
			this.res = Objects.requireNonNull(res);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		@Override
		public IntCollection edges() {
			return IndexIdMaps.indexToIdCollection(res.edges(), eiMap);
		}

	}

}
