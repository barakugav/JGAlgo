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
package com.jgalgo.graph;

import java.util.Optional;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;

class GraphBuilderImpl {

	private static abstract class Abstract implements GraphBuilder {

		final IndexGraphBuilder ibuilder;
		private final Int2IntMap vIdToIndex;
		private final IntList vIndexToId;
		private final IntSet vertices;
		private final Int2IntMap eIdToIndex;
		private final IntList eIndexToId;
		private final IntSet edges;
		private boolean userProvideVerticesIds;
		private boolean userProvideEdgesIds;
		final IndexIdMap viMap;
		final IndexIdMap eiMap;

		Abstract(IndexGraphBuilder ibuilder) {
			this.ibuilder = ibuilder;
			vIdToIndex = new Int2IntOpenHashMap();
			vIdToIndex.defaultReturnValue(-1);
			vIndexToId = new IntArrayList();
			vertices = IntSets.unmodifiable(vIdToIndex.keySet());
			eIdToIndex = new Int2IntOpenHashMap();
			eIdToIndex.defaultReturnValue(-1);
			eIndexToId = new IntArrayList();
			edges = IntSets.unmodifiable(eIdToIndex.keySet());
			viMap = new IndexIdMapImpl(vIdToIndex, vIndexToId);
			eiMap = new IndexIdMapImpl(eIdToIndex, eIndexToId);
		}

		@Override
		public IntSet vertices() {
			return vertices;
		}

		@Override
		public IntSet edges() {
			return edges;
		}

		private boolean canAddVertexWithoutId() {
			return vIdToIndex.isEmpty() || !userProvideVerticesIds;
		}

		private boolean canAddVertexWithId() {
			return vIdToIndex.isEmpty() || userProvideVerticesIds;
		}

		private boolean canAddEdgeWithoutId() {
			return eIdToIndex.isEmpty() || !userProvideEdgesIds;
		}

		private boolean canAddEdgeWithId() {
			return eIdToIndex.isEmpty() || userProvideEdgesIds;
		}

		@Override
		public int addVertex() {
			if (!canAddVertexWithoutId())
				throw new IllegalArgumentException(
						"Can't mix addVertex() and addVertex(id), if IDs are provided for some of the vertices, they must be provided for all");
			int vIndex = ibuilder.addVertex();
			int vId = vIndex + 1; // +1 because we want to avoid 0 in fastutil open hash maps
			assert vIndex == vIndexToId.size();
			vIndexToId.add(vId);
			int oldVal = vIdToIndex.put(vId, vIndex);
			assert oldVal == vIdToIndex.defaultReturnValue();
			return vId;
		}

		@Override
		public void addVertex(int vertex) {
			if (!canAddVertexWithId())
				throw new IllegalArgumentException(
						"Can't mix addVertex() and addVertex(id), if IDs are provided for some of the vertices, they must be provided for all");
			int vIndex = ibuilder.addVertex();
			int vId = vertex;
			assert vIndex == vIndexToId.size();
			vIndexToId.add(vId);
			int oldVal = vIdToIndex.put(vId, vIndex);
			if (oldVal != vIdToIndex.defaultReturnValue())
				throw new IllegalArgumentException("duplicate vertex: " + vId);
			userProvideVerticesIds = true;
		}

		@Override
		public int addEdge(int source, int target) {
			if (!canAddEdgeWithoutId())
				throw new IllegalArgumentException(
						"Can't mix addEdge(u,v) and addEdge(u,v,id), if IDs are provided for some of the edges, they must be provided for all");
			int sourceIdx = vIdToIndex.get(source);
			int targetIdx = vIdToIndex.get(target);
			if (targetIdx == vIdToIndex.defaultReturnValue())
				throw new IndexOutOfBoundsException(target);
			if (sourceIdx == vIdToIndex.defaultReturnValue())
				throw new IndexOutOfBoundsException(source);

			int eIndex = ibuilder.addEdge(sourceIdx, targetIdx);
			int eId = eIndex;
			assert eIndex == eIndexToId.size();
			eIndexToId.add(eId);
			int oldVal = eIdToIndex.put(eId, eIndex);
			assert oldVal == eIdToIndex.defaultReturnValue();
			return eId;
		}

		@Override
		public void addEdge(int source, int target, int edge) {
			if (!canAddEdgeWithId())
				throw new IllegalArgumentException(
						"Can't mix addEdge(u,v) and addEdge(u,v,id), if IDs are provided for some of the edges, they must be provided for all");
			int sourceIdx = vIdToIndex.get(source);
			int targetIdx = vIdToIndex.get(target);
			if (targetIdx == vIdToIndex.defaultReturnValue())
				throw new IndexOutOfBoundsException(target);
			if (sourceIdx == vIdToIndex.defaultReturnValue())
				throw new IndexOutOfBoundsException(source);

			int eIndex = ibuilder.addEdge(sourceIdx, targetIdx);
			int eId = edge;
			assert eIndex == eIndexToId.size();
			eIndexToId.add(eId);
			int oldVal = eIdToIndex.put(eId, eIndex);
			if (oldVal != eIdToIndex.defaultReturnValue())
				throw new IllegalArgumentException("duplicate edge: " + edge);
			userProvideEdgesIds = true;
		}

		@Override
		public void clear() {
			ibuilder.clear();
			vIdToIndex.clear();
			vIndexToId.clear();
			eIdToIndex.clear();
			eIndexToId.clear();
			userProvideVerticesIds = false;
			userProvideEdgesIds = false;
		}

		private static class IndexIdMapImpl implements IndexIdMap {
			private final Int2IntMap idToIndex;
			private final IntList indexToId;

			IndexIdMapImpl(Int2IntMap idToIndex, IntList indexToId) {
				this.idToIndex = idToIndex;
				this.indexToId = indexToId;
			}

			@Override
			public int indexToId(int index) {
				return indexToId.getInt(index);
			}

			@Override
			public int idToIndex(int id) {
				return idToIndex.get(id);
			}
		}

		static IndexIdMap reIndexedIdMap(IndexIdMap iMapOrig, IndexGraphBuilder.ReIndexingMap indexingMap) {
			return new IndexIdMap() {

				@Override
				public int indexToId(int index) {
					return iMapOrig.indexToId(indexingMap.reIndexedToOrig(index));
				}

				@Override
				public int idToIndex(int id) {
					return indexingMap.origToReIndexed(iMapOrig.idToIndex(id));
				}
			};
		}
	}

	static class Undirected extends GraphBuilderImpl.Abstract {

		Undirected() {
			super(IndexGraphBuilder.newUndirected());
		}

		@Override
		public Graph build() {
			IndexGraphBuilder.ReIndexedGraph reIndexedGraph = ibuilder.reIndexAndBuild(true, true);
			IndexGraph iGraph = reIndexedGraph.graph();
			Optional<IndexGraphBuilder.ReIndexingMap> vReIndexing = reIndexedGraph.verticesReIndexing();
			Optional<IndexGraphBuilder.ReIndexingMap> eReIndexing = reIndexedGraph.edgesReIndexing();
			IndexIdMap viMap = vReIndexing.isEmpty() ? this.viMap : reIndexedIdMap(this.viMap, vReIndexing.get());
			IndexIdMap eiMap = eReIndexing.isEmpty() ? this.eiMap : reIndexedIdMap(this.eiMap, eReIndexing.get());
			return new GraphImpl.Undirected(iGraph, viMap, eiMap);
		}

		@Override
		public Graph buildMutable() {
			IndexGraphBuilder.ReIndexedGraph reIndexedGraph = ibuilder.reIndexAndBuildMutable(true, true);
			IndexGraph iGraph = reIndexedGraph.graph();
			Optional<IndexGraphBuilder.ReIndexingMap> vReIndexing = reIndexedGraph.verticesReIndexing();
			Optional<IndexGraphBuilder.ReIndexingMap> eReIndexing = reIndexedGraph.edgesReIndexing();
			IndexIdMap viMap = vReIndexing.isEmpty() ? this.viMap : reIndexedIdMap(this.viMap, vReIndexing.get());
			IndexIdMap eiMap = eReIndexing.isEmpty() ? this.eiMap : reIndexedIdMap(this.eiMap, eReIndexing.get());
			return new GraphImpl.Undirected(iGraph, viMap, eiMap);
		}
	}

	static class Directed extends GraphBuilderImpl.Abstract {

		Directed() {
			super(IndexGraphBuilder.newDirected());
		}

		@Override
		public Graph build() {
			IndexGraphBuilder.ReIndexedGraph reIndexedGraph = ibuilder.reIndexAndBuild(true, true);
			IndexGraph iGraph = reIndexedGraph.graph();
			Optional<IndexGraphBuilder.ReIndexingMap> vReIndexing = reIndexedGraph.verticesReIndexing();
			Optional<IndexGraphBuilder.ReIndexingMap> eReIndexing = reIndexedGraph.edgesReIndexing();
			IndexIdMap viMap = vReIndexing.isEmpty() ? this.viMap : reIndexedIdMap(this.viMap, vReIndexing.get());
			IndexIdMap eiMap = eReIndexing.isEmpty() ? this.eiMap : reIndexedIdMap(this.eiMap, eReIndexing.get());
			return new GraphImpl.Directed(iGraph, viMap, eiMap);
		}

		@Override
		public Graph buildMutable() {
			IndexGraphBuilder.ReIndexedGraph reIndexedGraph = ibuilder.reIndexAndBuildMutable(true, true);
			IndexGraph iGraph = reIndexedGraph.graph();
			Optional<IndexGraphBuilder.ReIndexingMap> vReIndexing = reIndexedGraph.verticesReIndexing();
			Optional<IndexGraphBuilder.ReIndexingMap> eReIndexing = reIndexedGraph.edgesReIndexing();
			IndexIdMap viMap = vReIndexing.isEmpty() ? this.viMap : reIndexedIdMap(this.viMap, vReIndexing.get());
			IndexIdMap eiMap = eReIndexing.isEmpty() ? this.eiMap : reIndexedIdMap(this.eiMap, eReIndexing.get());
			return new GraphImpl.Directed(iGraph, viMap, eiMap);
		}
	}

}
