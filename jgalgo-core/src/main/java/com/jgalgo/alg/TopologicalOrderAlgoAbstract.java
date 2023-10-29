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
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IndexIdMaps;
import it.unimi.dsi.fastutil.ints.IntList;

abstract class TopologicalOrderAlgoAbstract implements TopologicalOrderAlgo {

	@Override
	public TopologicalOrderAlgo.Result computeTopologicalSorting(IntGraph g) {
		if (g instanceof IndexGraph)
			return computeTopologicalSorting((IndexGraph) g);

		IndexGraph iGraph = g.indexGraph();
		IndexIntIdMap viMap = g.indexGraphVerticesMap();

		TopologicalOrderAlgo.Result indexResult = computeTopologicalSorting(iGraph);
		return new ResultFromIndexResult(indexResult, viMap);
	}

	abstract TopologicalOrderAlgo.Result computeTopologicalSorting(IndexGraph g);

	private static class ResultFromIndexResult implements TopologicalOrderAlgo.Result {

		private final TopologicalOrderAlgo.Result res;
		private final IndexIntIdMap viMap;

		ResultFromIndexResult(TopologicalOrderAlgo.Result res, IndexIntIdMap viMap) {
			this.res = Objects.requireNonNull(res);
			this.viMap = Objects.requireNonNull(viMap);
		}

		@Override
		public IntList orderedVertices() {
			return IndexIdMaps.indexToIdList(res.orderedVertices(), viMap);
		}

		@Override
		public int vertexOrderIndex(int vertex) {
			return res.vertexOrderIndex(viMap.idToIndex(vertex));
		}

	}

}
