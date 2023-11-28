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

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;
import it.unimi.dsi.fastutil.objects.ObjectSets;

class GraphBuilderImpl<V, E> implements GraphBuilder<V, E> {

	final IndexGraphBuilder ibuilder;
	private final Object2IntOpenHashMap<V> vIdToIndex;
	private final ObjectArrayList<V> vIndexToId;
	private final Set<V> vertices;
	private final Object2IntOpenHashMap<E> eIdToIndex;
	private final ObjectArrayList<E> eIndexToId;
	private final Set<E> edges;
	final IndexIdMapImpl<V> viMap;
	final IndexIdMapImpl<E> eiMap;
	private final Map<WeightsImpl.Index<?>, WeightsImpl.ObjMapped<V, ?>> verticesWeights = new IdentityHashMap<>();
	private final Map<WeightsImpl.Index<?>, WeightsImpl.ObjMapped<E, ?>> edgesWeights = new IdentityHashMap<>();

	GraphBuilderImpl(IndexGraphBuilder ibuilder) {
		assert ibuilder.vertices().isEmpty();
		assert ibuilder.edges().isEmpty();
		this.ibuilder = ibuilder;
		vIdToIndex = new Object2IntOpenHashMap<>();
		vIdToIndex.defaultReturnValue(-1);
		vIndexToId = new ObjectArrayList<>();
		vertices = ObjectSets.unmodifiable(vIdToIndex.keySet());
		eIdToIndex = new Object2IntOpenHashMap<>();
		eIdToIndex.defaultReturnValue(-1);
		eIndexToId = new ObjectArrayList<>();
		edges = ObjectSets.unmodifiable(eIdToIndex.keySet());
		viMap = new IndexIdMapImpl<>(vIdToIndex, vIndexToId, false);
		eiMap = new IndexIdMapImpl<>(eIdToIndex, eIndexToId, true);
	}

	GraphBuilderImpl(Graph<V, E> g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		final int n = g.vertices().size();
		final int m = g.edges().size();
		this.ibuilder = IndexGraphBuilder.fromGraph(g.indexGraph(), copyVerticesWeights, copyEdgesWeights);
		vIdToIndex = new Object2IntOpenHashMap<>(n);
		vIdToIndex.defaultReturnValue(-1);
		vIndexToId = new ObjectArrayList<>(n);
		vertices = ObjectSets.unmodifiable(vIdToIndex.keySet());
		eIdToIndex = new Object2IntOpenHashMap<>(m);
		eIdToIndex.defaultReturnValue(-1);
		eIndexToId = new ObjectArrayList<>(m);
		edges = ObjectSets.unmodifiable(eIdToIndex.keySet());
		viMap = new IndexIdMapImpl<>(vIdToIndex, vIndexToId, false);
		eiMap = new IndexIdMapImpl<>(eIdToIndex, eIndexToId, true);

		IndexIdMap<V> gViMap = g.indexGraphVerticesMap();
		IndexIdMap<E> gEiMap = g.indexGraphEdgesMap();
		for (int vIdx = 0; vIdx < n; vIdx++) {
			V v = gViMap.indexToId(vIdx);
			vIndexToId.add(v);
			vIdToIndex.put(v, vIdx);
		}
		for (int eIdx = 0; eIdx < m; eIdx++) {
			E e = gEiMap.indexToId(eIdx);
			eIndexToId.add(e);
			eIdToIndex.put(e, eIdx);
		}
	}

	@Override
	public Set<V> vertices() {
		return vertices;
	}

	@Override
	public Set<E> edges() {
		return edges;
	}

	@Override
	public void addVertex(V vertex) {
		int vIndex = ibuilder.vertices().size();
		int oldVal = vIdToIndex.putIfAbsent(vertex, vIndex);
		if (oldVal != vIdToIndex.defaultReturnValue())
			throw new IllegalArgumentException("duplicate vertex: " + vertex);

		int vIndex2 = ibuilder.addVertex();
		assert vIndex == vIndex2;
		assert vIndex == vIndexToId.size();
		vIndexToId.add(vertex);
	}

	@Override
	public void addEdge(V source, V target, E edge) {
		int sourceIdx = vIdToIndex.getInt(source);
		int targetIdx = vIdToIndex.getInt(target);
		if (targetIdx == vIdToIndex.defaultReturnValue())
			throw NoSuchVertexException.ofVertex(target);
		if (sourceIdx == vIdToIndex.defaultReturnValue())
			throw NoSuchVertexException.ofVertex(source);

		int eIndex = ibuilder.edges().size();
		int oldVal = eIdToIndex.putIfAbsent(edge, eIndex);
		if (oldVal != eIdToIndex.defaultReturnValue())
			throw new IllegalArgumentException("duplicate edge: " + edge);

		int eIndex2 = ibuilder.addEdge(sourceIdx, targetIdx);
		assert eIndex == eIndex2;
		assert eIndex == eIndexToId.size();
		eIndexToId.add(edge);
	}

	@Override
	public void expectedVerticesNum(int verticesNum) {
		ibuilder.expectedVerticesNum(verticesNum);
		vIdToIndex.ensureCapacity(verticesNum);
		vIndexToId.ensureCapacity(verticesNum);
	}

	@Override
	public void expectedEdgesNum(int edgesNum) {
		ibuilder.expectedEdgesNum(edgesNum);
		eIdToIndex.ensureCapacity(edgesNum);
		eIndexToId.ensureCapacity(edgesNum);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T, WeightsT extends Weights<V, T>> WeightsT getVerticesWeights(String key) {
		WeightsImpl.Index<T> indexWeights = ibuilder.getVerticesWeights(key);
		if (indexWeights == null)
			return null;
		return (WeightsT) verticesWeights.computeIfAbsent(indexWeights,
				iw -> WeightsImpl.ObjMapped.newInstance(iw, viMap));
	}

	@Override
	public <T, WeightsT extends Weights<V, T>> WeightsT addVerticesWeights(String key, Class<? super T> type,
			T defVal) {
		ibuilder.addVerticesWeights(key, type, defVal);
		return getVerticesWeights(key);
	}

	@Override
	public Set<String> getVerticesWeightsKeys() {
		return ibuilder.getVerticesWeightsKeys();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T, WeightsT extends Weights<E, T>> WeightsT getEdgesWeights(String key) {
		WeightsImpl.Index<T> indexWeights = ibuilder.getEdgesWeights(key);
		if (indexWeights == null)
			return null;
		return (WeightsT) edgesWeights.computeIfAbsent(indexWeights,
				iw -> WeightsImpl.ObjMapped.newInstance(iw, eiMap));
	}

	@Override
	public <T, WeightsT extends Weights<E, T>> WeightsT addEdgesWeights(String key, Class<? super T> type, T defVal) {
		ibuilder.addEdgesWeights(key, type, defVal);
		return getEdgesWeights(key);
	}

	@Override
	public Set<String> getEdgesWeightsKeys() {
		return ibuilder.getEdgesWeightsKeys();
	}

	@Override
	public void clear() {
		ibuilder.clear();
		vIdToIndex.clear();
		vIndexToId.clear();
		eIdToIndex.clear();
		eIndexToId.clear();
		verticesWeights.clear();
		edgesWeights.clear();
	}

	@Override
	public boolean isDirected() {
		return ibuilder.isDirected();
	}

	private static class IndexIdMapImpl<K> implements IndexIdMap<K> {

		// TODO: remove this implementation, use GraphImpl.IndexIdMapImpl instead

		private final Object2IntMap<K> idToIndex;
		private final ObjectList<K> indexToId;
		private final boolean isEdges;

		IndexIdMapImpl(Object2IntMap<K> idToIndex, ObjectList<K> indexToId, boolean isEdges) {
			this.idToIndex = idToIndex;
			this.indexToId = indexToId;
			this.isEdges = isEdges;
		}

		@Override
		public K indexToId(int index) {
			if (!(0 <= index && index < indexToId.size())) {
				if (isEdges) {
					throw NoSuchEdgeException.ofIndex(index);
				} else {
					throw NoSuchVertexException.ofIndex(index);
				}
			}
			return indexToId.get(index);
		}

		@Override
		public K indexToIdIfExist(int index) {
			if (!(0 <= index && index < indexToId.size()))
				return null;
			return indexToId.get(index);
		}

		@Override
		public int idToIndex(K id) {
			int idx = idToIndex.getInt(id);
			if (idx < 0) {
				if (isEdges) {
					throw NoSuchEdgeException.ofEdge(id);
				} else {
					throw NoSuchVertexException.ofVertex(id);
				}
			}
			return idx;
		}

		@Override
		public int idToIndexIfExist(K id) {
			return idToIndex.getInt(id);
		}
	}

	@Override
	public Graph<V, E> build() {
		return buildFromReIndexed(ibuilder.reIndexAndBuild(true, true));
	}

	@Override
	public Graph<V, E> buildMutable() {
		return buildFromReIndexed(ibuilder.reIndexAndBuildMutable(true, true));
	}

	private Graph<V, E> buildFromReIndexed(IndexGraphBuilder.ReIndexedGraph reIndexedGraph) {
		IndexGraph iGraph = reIndexedGraph.graph();
		Optional<IndexGraphBuilder.ReIndexingMap> vReIndexing = reIndexedGraph.verticesReIndexing();
		Optional<IndexGraphBuilder.ReIndexingMap> eReIndexing = reIndexedGraph.edgesReIndexing();
		return new GraphImpl<>(iGraph, viMap, eiMap, vReIndexing.orElse(null), eReIndexing.orElse(null));
	}

}
