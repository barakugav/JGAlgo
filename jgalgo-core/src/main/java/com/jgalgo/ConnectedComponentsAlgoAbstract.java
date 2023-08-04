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
package com.jgalgo;

import java.util.Objects;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import it.unimi.dsi.fastutil.ints.IntCollection;

abstract class ConnectedComponentsAlgoAbstract implements ConnectedComponentsAlgo {

	@Override
	public ConnectedComponentsAlgo.Result computeConnectivityComponents(Graph g) {
		if (g instanceof IndexGraph)
			return computeConnectivityComponents((IndexGraph) g);

		IndexGraph iGraph = g.indexGraph();
		IndexIdMap viMap = g.indexGraphVerticesMap();
		IndexIdMap eiMap = g.indexGraphEdgesMap();

		ConnectedComponentsAlgo.Result indexResult = computeConnectivityComponents(iGraph);
		return new ResultFromIndexResult(indexResult, viMap, eiMap);
	}

	@Override
	public ConnectedComponentsAlgo.Result computeWeaklyConnectivityComponents(Graph g) {
		if (g instanceof IndexGraph)
			return computeWeaklyConnectivityComponents((IndexGraph) g);

		IndexGraph iGraph = g.indexGraph();
		IndexIdMap viMap = g.indexGraphVerticesMap();
		IndexIdMap eiMap = g.indexGraphEdgesMap();

		ConnectedComponentsAlgo.Result indexResult = computeWeaklyConnectivityComponents(iGraph);
		return new ResultFromIndexResult(indexResult, viMap, eiMap);
	}

	abstract ConnectedComponentsAlgo.Result computeConnectivityComponents(IndexGraph g);

	abstract ConnectedComponentsAlgo.Result computeWeaklyConnectivityComponents(IndexGraph g);

	private static class ResultFromIndexResult implements ConnectedComponentsAlgo.Result {

		private final ConnectedComponentsAlgo.Result res;
		private final IndexIdMap viMap;
		private final IndexIdMap eiMap;

		ResultFromIndexResult(ConnectedComponentsAlgo.Result res, IndexIdMap viMap, IndexIdMap eiMap) {
			this.res = Objects.requireNonNull(res);
			this.viMap = Objects.requireNonNull(viMap);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		@Override
		public int getVertexCc(int vertex) {
			return res.getVertexCc(viMap.idToIndex(vertex));
		}

		@Override
		public int getNumberOfCcs() {
			return res.getNumberOfCcs();
		}

		@Override
		public IntCollection getCcVertices(int ccIdx) {
			return IndexIdMaps.indexToIdCollection(res.getCcVertices(ccIdx), viMap);
		}

		@Override
		public IntCollection getCcEdges(int ccIdx) {
			return IndexIdMaps.indexToIdCollection(res.getCcEdges(ccIdx), eiMap);
		}

	}

}
