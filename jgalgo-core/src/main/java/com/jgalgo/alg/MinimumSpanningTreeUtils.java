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
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntCollections;

class MinimumSpanningTreeUtils {

	static abstract class AbstractUndirected implements MinimumSpanningTree {

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> MinimumSpanningTree.Result<V, E> computeMinimumSpanningTree(Graph<V, E> g, WeightFunction<E> w) {
			if (g instanceof IndexGraph) {
				IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
				return (MinimumSpanningTree.Result<V, E>) computeMinimumSpanningTree((IndexGraph) g, w0);

			} else if (g instanceof IntGraph) {
				IndexGraph iGraph = g.indexGraph();
				IndexIntIdMap eiMap = ((IntGraph) g).indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc((WeightFunction<Integer>) w, eiMap);
				MinimumSpanningTree.IResult indexResult = computeMinimumSpanningTree(iGraph, iw);
				return (MinimumSpanningTree.Result<V, E>) new IntResultFromIndexResult(indexResult, eiMap);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
				MinimumSpanningTree.IResult indexResult = computeMinimumSpanningTree(iGraph, iw);
				return new ObjResultFromIndexResult<>(indexResult, eiMap);
			}
		}

		abstract MinimumSpanningTree.IResult computeMinimumSpanningTree(IndexGraph g, IWeightFunction w);

	}

	@SuppressWarnings("unchecked")
	static abstract class AbstractDirected implements MinimumDirectedSpanningTree {

		@Override
		public <V, E> MinimumSpanningTree.Result<V, E> computeMinimumDirectedSpanningTree(Graph<V, E> g,
				WeightFunction<E> w, V root) {
			if (g instanceof IndexGraph) {
				IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
				int root0 = ((Integer) root).intValue();
				return (MinimumSpanningTree.Result<V, E>) computeMinimumDirectedSpanningTree((IndexGraph) g, w0, root0);

			} else if (g instanceof IntGraph) {
				IndexGraph iGraph = g.indexGraph();
				IndexIntIdMap viMap = ((IntGraph) g).indexGraphVerticesMap();
				IndexIntIdMap eiMap = ((IntGraph) g).indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc((WeightFunction<Integer>) w, eiMap);
				int iRoot = viMap.idToIndex(((Integer) root).intValue());

				MinimumSpanningTree.IResult indexResult = computeMinimumDirectedSpanningTree(iGraph, iw, iRoot);
				return (MinimumSpanningTree.Result<V, E>) new IntResultFromIndexResult(indexResult, eiMap);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
				int iRoot = viMap.idToIndex(root);

				MinimumSpanningTree.IResult indexResult = computeMinimumDirectedSpanningTree(iGraph, iw, iRoot);
				return new ObjResultFromIndexResult<>(indexResult, eiMap);
			}
		}

		abstract MinimumSpanningTree.IResult computeMinimumDirectedSpanningTree(IndexGraph g, IWeightFunction w,
				int root);

	}

	static class ResultImpl implements MinimumSpanningTree.IResult {

		private final IntCollection edges;
		static final MinimumSpanningTree.IResult Empty = new ResultImpl(IntArrays.EMPTY_ARRAY);

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

	static class ObjResultFromIndexResult<V, E> implements MinimumSpanningTree.Result<V, E> {

		private final MinimumSpanningTree.IResult indexRes;
		private final IndexIdMap<E> eiMap;

		ObjResultFromIndexResult(MinimumSpanningTree.IResult indexRes, IndexIdMap<E> eiMap) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		@Override
		public Collection<E> edges() {
			return IndexIdMaps.indexToIdCollection(indexRes.edges(), eiMap);
		}
	}

	static class IntResultFromIndexResult implements MinimumSpanningTree.IResult {

		private final MinimumSpanningTree.IResult indexRes;
		private final IndexIntIdMap eiMap;

		IntResultFromIndexResult(MinimumSpanningTree.IResult indexRes, IndexIntIdMap eiMap) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		@Override
		public IntCollection edges() {
			return IndexIdMaps.indexToIdCollection(indexRes.edges(), eiMap);
		}
	}

}
