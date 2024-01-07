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
import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;

class SteinerTrees {

	private SteinerTrees() {}

	static class IndexResult implements SteinerTreeAlgo.IResult {

		private final IntSet edges;
		static final SteinerTreeAlgo.IResult Empty = new IndexResult(IntArrays.EMPTY_ARRAY);

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

	static class ObjResultFromIndexResult<V, E> implements SteinerTreeAlgo.Result<V, E> {

		private final SteinerTreeAlgo.IResult indexRes;
		private final IndexIdMap<E> eiMap;

		ObjResultFromIndexResult(Graph<V, E> g, SteinerTreeAlgo.IResult res) {
			this.indexRes = Objects.requireNonNull(res);
			this.eiMap = g.indexGraphEdgesMap();
		}

		@Override
		public Set<E> edges() {
			return IndexIdMaps.indexToIdSet(indexRes.edges(), eiMap);
		}
	}

	static class IntResultFromIndexResult implements SteinerTreeAlgo.IResult {

		private final SteinerTreeAlgo.IResult indexRes;
		private final IndexIntIdMap eiMap;

		IntResultFromIndexResult(IntGraph g, SteinerTreeAlgo.IResult res) {
			this.indexRes = Objects.requireNonNull(res);
			this.eiMap = g.indexGraphEdgesMap();
		}

		@Override
		public IntSet edges() {
			return IndexIdMaps.indexToIdSet(indexRes.edges(), eiMap);
		}
	}

}
