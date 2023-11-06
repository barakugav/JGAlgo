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

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import it.unimi.dsi.fastutil.ints.IntSet;

class MaximalCliquesUtils {

	abstract static class AbstractImpl implements MaximalCliques {

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public <V, E> Iterator<Set<V>> iterateMaximalCliques(Graph<V, E> g) {
			if (g instanceof IndexGraph) {
				return (Iterator) iterateMaximalCliques((IndexGraph) g);

			} else {
				IndexGraph iGraph = g.indexGraph();
				Iterator<IntSet> indexResult = iterateMaximalCliques(iGraph);
				return resultFromIndexResult(g, indexResult);
			}
		}

		abstract Iterator<IntSet> iterateMaximalCliques(IndexGraph g);

	}

	private static class IntResultFromIndexResult implements Iterator<IntSet> {

		private final Iterator<IntSet> indexResult;
		private final IndexIntIdMap viMap;

		IntResultFromIndexResult(IntGraph g, Iterator<IntSet> indexResult) {
			this.indexResult = Objects.requireNonNull(indexResult);
			this.viMap = g.indexGraphVerticesMap();
		}

		@Override
		public boolean hasNext() {
			return indexResult.hasNext();
		}

		@Override
		public IntSet next() {
			return IndexIdMaps.indexToIdSet(indexResult.next(), viMap);
		}
	}

	private static class ObjResultFromIndexResult<V, E> implements Iterator<Set<V>> {

		private final Iterator<IntSet> indexResult;
		private final IndexIdMap<V> viMap;

		ObjResultFromIndexResult(Graph<V, E> g, Iterator<IntSet> indexResult) {
			this.indexResult = Objects.requireNonNull(indexResult);
			this.viMap = g.indexGraphVerticesMap();
		}

		@Override
		public boolean hasNext() {
			return indexResult.hasNext();
		}

		@Override
		public Set<V> next() {
			return IndexIdMaps.indexToIdSet(indexResult.next(), viMap);
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static <V, E> Iterator<Set<V>> resultFromIndexResult(Graph<V, E> g, Iterator<IntSet> indexResult) {
		assert !(g instanceof IndexGraph);
		if (g instanceof IntGraph) {
			return (Iterator) new IntResultFromIndexResult((IntGraph) g, indexResult);
		} else {
			return new ObjResultFromIndexResult<>(g, indexResult);
		}
	}

}
