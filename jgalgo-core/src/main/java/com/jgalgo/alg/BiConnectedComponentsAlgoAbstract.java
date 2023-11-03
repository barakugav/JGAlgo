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
		if (g instanceof IndexGraph)
			return (BiConnectedComponentsAlgo.Result<V, E>) findBiConnectedComponents((IndexGraph) g);

		IndexGraph iGraph = g.indexGraph();
		BiConnectedComponentsAlgo.IResult indexResult = findBiConnectedComponents(iGraph);
		return resultFromIndexResult(g, indexResult);
	}

	abstract BiConnectedComponentsAlgo.IResult findBiConnectedComponents(IndexGraph g);

	private static class IntResultFromIndexResult implements BiConnectedComponentsAlgo.IResult {

		private final BiConnectedComponentsAlgo.IResult indexRes;
		private final IndexIntIdMap viMap;
		private final IndexIntIdMap eiMap;

		IntResultFromIndexResult(IntGraph g, BiConnectedComponentsAlgo.IResult indexRes) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.viMap = g.indexGraphVerticesMap();
			this.eiMap = g.indexGraphEdgesMap();
		}

		@Override
		public IntSet getVertexBiCcs(int vertex) {
			return indexRes.getVertexBiCcs(viMap.idToIndex(vertex));
		}

		@Override
		public int getNumberOfBiCcs() {
			return indexRes.getNumberOfBiCcs();
		}

		@Override
		public IntSet getBiCcVertices(int biccIdx) {
			return IndexIdMaps.indexToIdSet(indexRes.getBiCcVertices(biccIdx), viMap);
		}

		@Override
		public IntSet getBiCcEdges(int biccIdx) {
			return IndexIdMaps.indexToIdSet(indexRes.getBiCcEdges(biccIdx), eiMap);
		}

		@Override
		public boolean isCutVertex(int vertex) {
			return indexRes.isCutVertex(viMap.idToIndex(vertex));
		}

		@Override
		public IntSet getCutVertices() {
			return IndexIdMaps.indexToIdSet(indexRes.getCutVertices(), viMap);
		}

		@Override
		public IntGraph getBlockGraph() {
			return indexRes.getBlockGraph();
		}
	}

	private static class ObjResultFromIndexResult<V, E> implements BiConnectedComponentsAlgo.Result<V, E> {

		private final BiConnectedComponentsAlgo.IResult indexRes;
		private final IndexIdMap<V> viMap;
		private final IndexIdMap<E> eiMap;

		ObjResultFromIndexResult(Graph<V, E> g, BiConnectedComponentsAlgo.IResult indexRes) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.viMap = g.indexGraphVerticesMap();
			this.eiMap = g.indexGraphEdgesMap();
		}

		@Override
		public IntSet getVertexBiCcs(V vertex) {
			return indexRes.getVertexBiCcs(viMap.idToIndex(vertex));
		}

		@Override
		public int getNumberOfBiCcs() {
			return indexRes.getNumberOfBiCcs();
		}

		@Override
		public Set<V> getBiCcVertices(int biccIdx) {
			return IndexIdMaps.indexToIdSet(indexRes.getBiCcVertices(biccIdx), viMap);
		}

		@Override
		public Set<E> getBiCcEdges(int biccIdx) {
			return IndexIdMaps.indexToIdSet(indexRes.getBiCcEdges(biccIdx), eiMap);
		}

		@Override
		public boolean isCutVertex(V vertex) {
			return indexRes.isCutVertex(viMap.idToIndex(vertex));
		}

		@Override
		public Set<V> getCutVertices() {
			return IndexIdMaps.indexToIdSet(indexRes.getCutVertices(), viMap);
		}

		@Override
		public IntGraph getBlockGraph() {
			return indexRes.getBlockGraph();
		}
	}

	@SuppressWarnings("unchecked")
	private static <V, E> BiConnectedComponentsAlgo.Result<V, E> resultFromIndexResult(Graph<V, E> g,
			BiConnectedComponentsAlgo.IResult indexRes) {
		if (g instanceof IntGraph) {
			return (BiConnectedComponentsAlgo.Result<V, E>) new IntResultFromIndexResult((IntGraph) g, indexRes);
		} else {
			return new ObjResultFromIndexResult<>(g, indexRes);
		}
	}

}
