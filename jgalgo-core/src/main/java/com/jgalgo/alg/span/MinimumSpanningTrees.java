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

package com.jgalgo.alg.span;

import java.util.Objects;
import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;

class MinimumSpanningTrees {

	private MinimumSpanningTrees() {}

	static class IndexResult implements MinimumSpanningTree.IResult {

		private final IntSet edges;
		static final MinimumSpanningTree.IResult Empty = new IndexResult(IntArrays.EMPTY_ARRAY);

		IndexResult(IntSet edges) {
			this.edges = IntSets.unmodifiable(Objects.requireNonNull(edges));
		}

		IndexResult(int[] edges) {
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

	private static class ObjResultFromIndexResult<V, E> implements MinimumSpanningTree.Result<V, E> {

		private final MinimumSpanningTree.IResult indexRes;
		private final IndexIdMap<E> eiMap;

		ObjResultFromIndexResult(Graph<V, E> g, MinimumSpanningTree.IResult indexRes) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.eiMap = g.indexGraphEdgesMap();
		}

		@Override
		public Set<E> edges() {
			return IndexIdMaps.indexToIdSet(indexRes.edges(), eiMap);
		}
	}

	private static class IntResultFromIndexResult implements MinimumSpanningTree.IResult {

		private final MinimumSpanningTree.IResult indexRes;
		private final IndexIntIdMap eiMap;

		IntResultFromIndexResult(IntGraph g, MinimumSpanningTree.IResult indexRes) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.eiMap = g.indexGraphEdgesMap();
		}

		@Override
		public IntSet edges() {
			return IndexIdMaps.indexToIdSet(indexRes.edges(), eiMap);
		}
	}

	@SuppressWarnings("unchecked")
	static <V, E> MinimumSpanningTree.Result<V, E> resultFromIndexResult(Graph<V, E> g,
			MinimumSpanningTree.IResult indexResult) {
		assert !(g instanceof IndexGraph);
		if (g instanceof IntGraph) {
			return (MinimumSpanningTree.Result<V, E>) new IntResultFromIndexResult((IntGraph) g, indexResult);
		} else {
			return new ObjResultFromIndexResult<>(g, indexResult);
		}
	}

}
