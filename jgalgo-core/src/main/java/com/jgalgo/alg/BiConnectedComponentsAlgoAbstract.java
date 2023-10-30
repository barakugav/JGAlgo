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
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import it.unimi.dsi.fastutil.ints.IntSet;

abstract class BiConnectedComponentsAlgoAbstract implements BiConnectedComponentsAlgo {

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> BiConnectedComponentsAlgo.Result<V, E> findBiConnectedComponents(Graph<V, E> g) {
		if (g instanceof IndexGraph) {
			return (BiConnectedComponentsAlgo.Result<V, E>) findBiConnectedComponents((IndexGraph) g);
		} else if (g instanceof IntGraph) {
			IntGraph g0 = (IntGraph) g;
			IndexGraph iGraph = g.indexGraph();
			IndexIntIdMap viMap = g0.indexGraphVerticesMap();
			IndexIntIdMap eiMap = g0.indexGraphEdgesMap();
			BiConnectedComponentsAlgo.IResult indexResult = findBiConnectedComponents(iGraph);
			return (BiConnectedComponentsAlgo.Result<V, E>) new IntResultFromIndexResult(indexResult, viMap, eiMap);
		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			BiConnectedComponentsAlgo.IResult indexResult = findBiConnectedComponents(iGraph);
			return new ObjResultFromIndexResult<>(indexResult, viMap, eiMap);
		}
	}

	abstract BiConnectedComponentsAlgo.IResult findBiConnectedComponents(IndexGraph g);

	private static class IntResultFromIndexResult implements BiConnectedComponentsAlgo.IResult {

		private final BiConnectedComponentsAlgo.IResult res;
		private final IndexIntIdMap viMap;
		private final IndexIntIdMap eiMap;

		IntResultFromIndexResult(BiConnectedComponentsAlgo.IResult res, IndexIntIdMap viMap, IndexIntIdMap eiMap) {
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
		public IntGraph getBlockGraph() {
			return res.getBlockGraph();
		}
	}

	private static class ObjResultFromIndexResult<V, E> implements BiConnectedComponentsAlgo.Result<V, E> {

		private final BiConnectedComponentsAlgo.IResult res;
		private final IndexIdMap<V> viMap;
		private final IndexIdMap<E> eiMap;

		ObjResultFromIndexResult(BiConnectedComponentsAlgo.IResult res, IndexIdMap<V> viMap, IndexIdMap<E> eiMap) {
			this.res = Objects.requireNonNull(res);
			this.viMap = Objects.requireNonNull(viMap);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		@Override
		public IntSet getVertexBiCcs(V vertex) {
			return res.getVertexBiCcs(viMap.idToIndex(vertex));
		}

		@Override
		public int getNumberOfBiCcs() {
			return res.getNumberOfBiCcs();
		}

		@Override
		public Set<V> getBiCcVertices(int biccIdx) {
			return IndexIdMaps.indexToIdSet(res.getBiCcVertices(biccIdx), viMap);
		}

		@Override
		public Set<E> getBiCcEdges(int biccIdx) {
			return IndexIdMaps.indexToIdSet(res.getBiCcEdges(biccIdx), eiMap);
		}

		@Override
		public boolean isCutVertex(V vertex) {
			return res.isCutVertex(viMap.idToIndex(vertex));
		}

		@Override
		public Set<V> getCutVertices() {
			return IndexIdMaps.indexToIdSet(res.getCutVertices(), viMap);
		}

		@Override
		public IntGraph getBlockGraph() {
			return res.getBlockGraph();
		}
	}

}
