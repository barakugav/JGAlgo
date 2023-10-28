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
import it.unimi.dsi.fastutil.ints.IntSet;

abstract class BiConnectedComponentsAlgoAbstract implements BiConnectedComponentsAlgo {

	@Override
	public BiConnectedComponentsAlgo.Result findBiConnectedComponents(Graph g) {
		if (g instanceof IndexGraph)
			return findBiConnectedComponents((IndexGraph) g);

		IndexGraph iGraph = g.indexGraph();
		IndexIdMap viMap = g.indexGraphVerticesMap();
		IndexIdMap eiMap = g.indexGraphEdgesMap();

		BiConnectedComponentsAlgo.Result indexResult = findBiConnectedComponents(iGraph);
		return new ResultFromIndexResult(indexResult, viMap, eiMap);
	}

	abstract BiConnectedComponentsAlgo.Result findBiConnectedComponents(IndexGraph g);

	private static class ResultFromIndexResult implements BiConnectedComponentsAlgo.Result {

		private final BiConnectedComponentsAlgo.Result res;
		private final IndexIdMap viMap;
		private final IndexIdMap eiMap;

		ResultFromIndexResult(BiConnectedComponentsAlgo.Result res, IndexIdMap viMap, IndexIdMap eiMap) {
			this.res = Objects.requireNonNull(res);
			this.viMap = Objects.requireNonNull(viMap);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		@Override
		public IntSet getVertexBiCcs(int vertex) {
			return res.getVertexBiCcs(viMap.idToIndex(vertex));
		}

		@Override
		public int getNumberOfBiCcs() {
			return res.getNumberOfBiCcs();
		}

		@Override
		public IntSet getBiCcVertices(int biccIdx) {
			return IndexIdMaps.indexToIdSet(res.getBiCcVertices(biccIdx), viMap);
		}

		@Override
		public IntSet getBiCcEdges(int biccIdx) {
			return IndexIdMaps.indexToIdSet(res.getBiCcEdges(biccIdx), eiMap);
		}

		@Override
		public boolean isCutVertex(int vertex) {
			return res.isCutVertex(viMap.idToIndex(vertex));
		}

		@Override
		public IntSet getCutVertices() {
			return IndexIdMaps.indexToIdSet(res.getCutVertices(), viMap);
		}

		@Override
		public Graph getBlockGraph() {
			return res.getBlockGraph();
		}

	}

}
