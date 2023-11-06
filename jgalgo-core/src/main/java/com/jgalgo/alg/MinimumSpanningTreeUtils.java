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

	abstract static class AbstractUndirected implements MinimumSpanningTree {

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> MinimumSpanningTree.Result<V, E> computeMinimumSpanningTree(Graph<V, E> g, WeightFunction<E> w) {
			if (g instanceof IndexGraph) {
				IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
				return (MinimumSpanningTree.Result<V, E>) computeMinimumSpanningTree((IndexGraph) g, w0);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
				MinimumSpanningTree.IResult indexResult = computeMinimumSpanningTree(iGraph, iw);
				return resultFromIndexResult(g, indexResult);
			}
		}

		abstract MinimumSpanningTree.IResult computeMinimumSpanningTree(IndexGraph g, IWeightFunction w);

	}

	@SuppressWarnings("unchecked")
	abstract static class AbstractDirected implements MinimumDirectedSpanningTree {

		@Override
		public <V, E> MinimumSpanningTree.Result<V, E> computeMinimumDirectedSpanningTree(Graph<V, E> g,
				WeightFunction<E> w, V root) {
			if (g instanceof IndexGraph) {
				IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
				int root0 = ((Integer) root).intValue();
				return (MinimumSpanningTree.Result<V, E>) computeMinimumDirectedSpanningTree((IndexGraph) g, w0, root0);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
				int iRoot = viMap.idToIndex(root);
				MinimumSpanningTree.IResult indexResult = computeMinimumDirectedSpanningTree(iGraph, iw, iRoot);
				return resultFromIndexResult(g, indexResult);
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

		ObjResultFromIndexResult(Graph<V, E> g, MinimumSpanningTree.IResult indexRes) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.eiMap = g.indexGraphEdgesMap();
		}

		@Override
		public Collection<E> edges() {
			return IndexIdMaps.indexToIdCollection(indexRes.edges(), eiMap);
		}
	}

	static class IntResultFromIndexResult implements MinimumSpanningTree.IResult {

		private final MinimumSpanningTree.IResult indexRes;
		private final IndexIntIdMap eiMap;

		IntResultFromIndexResult(IntGraph g, MinimumSpanningTree.IResult indexRes) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.eiMap = g.indexGraphEdgesMap();
		}

		@Override
		public IntCollection edges() {
			return IndexIdMaps.indexToIdCollection(indexRes.edges(), eiMap);
		}
	}

	@SuppressWarnings("unchecked")
	private static <V, E> MinimumSpanningTree.Result<V, E> resultFromIndexResult(Graph<V, E> g,
			MinimumSpanningTree.IResult indexResult) {
		assert !(g instanceof IndexGraph);
		if (g instanceof IntGraph) {
			return (MinimumSpanningTree.Result<V, E>) new IntResultFromIndexResult((IntGraph) g, indexResult);
		} else {
			return new ObjResultFromIndexResult<>(g, indexResult);
		}
	}

}
