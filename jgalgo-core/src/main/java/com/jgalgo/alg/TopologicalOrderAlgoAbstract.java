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

import java.util.List;
import java.util.Objects;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import it.unimi.dsi.fastutil.ints.IntList;

abstract class TopologicalOrderAlgoAbstract implements TopologicalOrderAlgo {

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> TopologicalOrderAlgo.Result<V, E> computeTopologicalSorting(Graph<V, E> g) {
		if (g instanceof IndexGraph) {
			return (TopologicalOrderAlgo.Result<V, E>) computeTopologicalSorting((IndexGraph) g);

		} else {
			IndexGraph iGraph = g.indexGraph();
			TopologicalOrderAlgo.IResult indexResult = computeTopologicalSorting(iGraph);
			return resultFromIndexResult(g, indexResult);
		}
	}

	abstract TopologicalOrderAlgo.IResult computeTopologicalSorting(IndexGraph g);

	private static class ObjResultFromIndexResult<V, E> implements TopologicalOrderAlgo.Result<V, E> {

		private final TopologicalOrderAlgo.IResult indexRes;
		private final IndexIdMap<V> viMap;

		ObjResultFromIndexResult(Graph<V, E> g, TopologicalOrderAlgo.IResult indexRes) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.viMap = g.indexGraphVerticesMap();
		}

		@Override
		public List<V> orderedVertices() {
			return IndexIdMaps.indexToIdList(indexRes.orderedVertices(), viMap);
		}

		@Override
		public int vertexOrderIndex(V vertex) {
			return indexRes.vertexOrderIndex(viMap.idToIndex(vertex));
		}
	}

	private static class IntResultFromIndexResult implements TopologicalOrderAlgo.IResult {

		private final TopologicalOrderAlgo.IResult indexRes;
		private final IndexIntIdMap viMap;

		IntResultFromIndexResult(IntGraph g, TopologicalOrderAlgo.IResult indexRes) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.viMap = g.indexGraphVerticesMap();
		}

		@Override
		public IntList orderedVertices() {
			return IndexIdMaps.indexToIdList(indexRes.orderedVertices(), viMap);
		}

		@Override
		public int vertexOrderIndex(int vertex) {
			return indexRes.vertexOrderIndex(viMap.idToIndex(vertex));
		}
	}

	@SuppressWarnings("unchecked")
	private static <V, E> TopologicalOrderAlgo.Result<V, E> resultFromIndexResult(Graph<V, E> g,
			TopologicalOrderAlgo.IResult indexResult) {
		assert !(g instanceof IndexGraph);
		if (g instanceof IntGraph) {
			return (TopologicalOrderAlgo.Result<V, E>) new IntResultFromIndexResult((IntGraph) g, indexResult);
		} else {
			return new ObjResultFromIndexResult<>(g, indexResult);
		}
	}

}
