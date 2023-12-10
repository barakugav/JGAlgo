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

import static com.jgalgo.internal.util.Range.range;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;

class IntGraphBuilderImpl implements IntGraphBuilder {

	final IndexGraphBuilder ibuilder;
	private final Int2IntOpenHashMap vIdToIndex;
	private final IntArrayList vIndexToId;
	private final IntSet vertices;
	private final Int2IntOpenHashMap eIdToIndex;
	private final IntArrayList eIndexToId;
	private final IntSet edges;
	private boolean userProvideVerticesIds;
	private boolean userProvideEdgesIds;
	final IndexIdMapImpl viMap;
	final IndexIdMapImpl eiMap;
	private final Map<WeightsImpl.Index<?>, WeightsImpl.IntMapped<?>> verticesWeights = new IdentityHashMap<>();
	private final Map<WeightsImpl.Index<?>, WeightsImpl.IntMapped<?>> edgesWeights = new IdentityHashMap<>();

	IntGraphBuilderImpl(IndexGraphBuilder ibuilder) {
		assert ibuilder.vertices().isEmpty();
		assert ibuilder.edges().isEmpty();
		this.ibuilder = ibuilder;
		vIdToIndex = new Int2IntOpenHashMap();
		vIdToIndex.defaultReturnValue(-1);
		vIndexToId = new IntArrayList();
		vertices = IntSets.unmodifiable(vIdToIndex.keySet());
		eIdToIndex = new Int2IntOpenHashMap();
		eIdToIndex.defaultReturnValue(-1);
		eIndexToId = new IntArrayList();
		edges = IntSets.unmodifiable(eIdToIndex.keySet());
		viMap = new IndexIdMapImpl(vIdToIndex, vIndexToId, false);
		eiMap = new IndexIdMapImpl(eIdToIndex, eIndexToId, true);
	}

	IntGraphBuilderImpl(IntGraph g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		final int n = g.vertices().size();
		final int m = g.edges().size();
		this.ibuilder = IndexGraphBuilder.fromGraph(g.indexGraph(), copyVerticesWeights, copyEdgesWeights);
		vIdToIndex = new Int2IntOpenHashMap(n);
		vIdToIndex.defaultReturnValue(-1);
		vIndexToId = new IntArrayList(n);
		vertices = IntSets.unmodifiable(vIdToIndex.keySet());
		eIdToIndex = new Int2IntOpenHashMap(m);
		eIdToIndex.defaultReturnValue(-1);
		eIndexToId = new IntArrayList(m);
		edges = IntSets.unmodifiable(eIdToIndex.keySet());
		viMap = new IndexIdMapImpl(vIdToIndex, vIndexToId, false);
		eiMap = new IndexIdMapImpl(eIdToIndex, eIndexToId, true);

		IndexIntIdMap gViMap = g.indexGraphVerticesMap();
		IndexIntIdMap gEiMap = g.indexGraphEdgesMap();
		for (int vIdx = 0; vIdx < n; vIdx++) {
			int v = gViMap.indexToIdInt(vIdx);
			vIndexToId.add(v);
			vIdToIndex.put(v, vIdx);
		}
		for (int eIdx = 0; eIdx < m; eIdx++) {
			int e = gEiMap.indexToIdInt(eIdx);
			eIndexToId.add(e);
			eIdToIndex.put(e, eIdx);
		}
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
			throw new IllegalArgumentException("Can't mix addVertex() and addVertex(id), "
					+ "if IDs are provided for some of the vertices, they must be provided for all");
		int vIndex = ibuilder.addVertex();
		int vId = vIndex + 1; // +1 because we want to avoid 0 in fastutil open hash maps
		assert vIndex == vIndexToId.size();
		vIndexToId.add(vId);
		int oldVal = vIdToIndex.put(vId, vIndex);
		assert oldVal == -1;
		return vId;
	}

	@Override
	public void addVertex(int vertex) {
		if (!canAddVertexWithId())
			throw new IllegalArgumentException("Can't mix addVertex() and addVertex(id), "
					+ "if IDs are provided for some of the vertices, they must be provided for all");
		if (vertex < 0)
			throw new IllegalArgumentException("Vertex must be non negative");

		int vIndex = ibuilder.vertices().size();
		int oldVal = vIdToIndex.putIfAbsent(vertex, vIndex);
		if (oldVal != -1)
			throw new IllegalArgumentException("duplicate vertex: " + vertex);

		int vIndex2 = ibuilder.addVertex();
		assert vIndex == vIndex2;
		assert vIndex == vIndexToId.size();
		vIndexToId.add(vertex);

		userProvideVerticesIds = true;
	}

	@Override
	public void addVertices(Collection<? extends Integer> vertices) {
		if (!canAddVertexWithId())
			throw new IllegalArgumentException("Can't mix addVertex() and addVertex(id), "
					+ "if IDs are provided for some of the vertices, they must be provided for all");
		if (vertices.isEmpty())
			return;
		if (!(vertices instanceof IntCollection))
			for (Integer vertex : vertices)
				if (vertex == null)
					throw new NullPointerException("Vertex must be non null");
		for (int vertex : vertices)
			if (vertex < 0)
				throw new IllegalArgumentException("Vertex must be non negative");

		final int verticesNumBefore = ibuilder.vertices().size();
		int nextIdx = verticesNumBefore;
		int duplicateVertex = -1;
		for (int vertex : vertices) {
			int oldVal = vIdToIndex.putIfAbsent(vertex, nextIdx);
			if (oldVal != -1) {
				duplicateVertex = vertex;
				break;
			}
			vIndexToId.add(vertex);
			nextIdx++;
		}
		if (duplicateVertex >= 0) {
			for (; nextIdx-- > verticesNumBefore;) {
				int idx = vIdToIndex.remove(vIndexToId.getInt(nextIdx));
				assert idx == nextIdx;
			}
			vIndexToId.size(verticesNumBefore);
			throw new IllegalArgumentException("Duplicate vertex: " + duplicateVertex);
		}
		ibuilder.addVertices(range(verticesNumBefore, nextIdx));

		userProvideVerticesIds = true;
	}

	@Override
	public int addEdge(int source, int target) {
		if (!canAddEdgeWithoutId())
			throw new IllegalArgumentException("Can't mix addEdge(u,v) and addEdge(u,v,id), "
					+ "if IDs are provided for some of the edges, they must be provided for all");

		int sourceIdx = vIdToIndex.get(source);
		int targetIdx = vIdToIndex.get(target);
		if (targetIdx == -1)
			throw NoSuchVertexException.ofVertex(target);
		if (sourceIdx == -1)
			throw NoSuchVertexException.ofVertex(source);

		int eIndex = ibuilder.addEdge(sourceIdx, targetIdx);
		int eId = eIndex + 1; // avoid null key in open hash maps
		assert eIndex == eIndexToId.size();
		eIndexToId.add(eId);
		int oldVal = eIdToIndex.put(eId, eIndex);
		assert oldVal == eIdToIndex.defaultReturnValue();
		return eId;
	}

	@Override
	public void addEdge(int source, int target, int edge) {
		if (!canAddEdgeWithId())
			throw new IllegalArgumentException("Can't mix addEdge(u,v) and addEdge(u,v,id), "
					+ "if IDs are provided for some of the edges, they must be provided for all");
		if (edge < 0)
			throw new IllegalArgumentException("Edge must be non negative");

		int sourceIdx = vIdToIndex.get(source);
		int targetIdx = vIdToIndex.get(target);
		if (targetIdx == -1)
			throw NoSuchVertexException.ofVertex(target);
		if (sourceIdx == -1)
			throw NoSuchVertexException.ofVertex(source);

		int eIndex = ibuilder.edges().size();
		int oldVal = eIdToIndex.putIfAbsent(edge, eIndex);
		if (oldVal != eIdToIndex.defaultReturnValue())
			throw new IllegalArgumentException("duplicate edge: " + edge);

		int eIndex2 = ibuilder.addEdge(sourceIdx, targetIdx);
		assert eIndex == eIndex2;
		assert eIndex == eIndexToId.size();
		eIndexToId.add(edge);

		userProvideEdgesIds = true;
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
	public <T, WeightsT extends IWeights<T>> WeightsT getVerticesIWeights(String key) {
		WeightsImpl.Index<T> indexWeights = ibuilder.getVerticesWeights(key);
		if (indexWeights == null)
			return null;
		return (WeightsT) verticesWeights.computeIfAbsent(indexWeights,
				iw -> WeightsImpl.IntMapped.newInstance(iw, viMap));
	}

	@Override
	public <T, WeightsT extends Weights<Integer, T>> WeightsT addVerticesWeights(String key, Class<? super T> type,
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
	public <T, WeightsT extends IWeights<T>> WeightsT getEdgesIWeights(String key) {
		WeightsImpl.Index<T> indexWeights = ibuilder.getEdgesWeights(key);
		if (indexWeights == null)
			return null;
		return (WeightsT) edgesWeights.computeIfAbsent(indexWeights,
				iw -> WeightsImpl.IntMapped.newInstance(iw, eiMap));
	}

	@Override
	public <T, WeightsT extends Weights<Integer, T>> WeightsT addEdgesWeights(String key, Class<? super T> type,
			T defVal) {
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
		userProvideVerticesIds = false;
		userProvideEdgesIds = false;
	}

	@Override
	public boolean isDirected() {
		return ibuilder.isDirected();
	}

	private static class IndexIdMapImpl implements IndexIntIdMap {

		// TODO: remove this implementation, use GraphImpl.IndexIdMapImpl instead

		private final Int2IntMap idToIndex;
		private final IntList indexToId;
		private final boolean isEdges;

		IndexIdMapImpl(Int2IntMap idToIndex, IntList indexToId, boolean isEdges) {
			this.idToIndex = idToIndex;
			this.indexToId = indexToId;
			this.isEdges = isEdges;
		}

		@Override
		public int indexToIdInt(int index) {
			if (!(0 <= index && index < indexToId.size())) {
				if (isEdges) {
					throw NoSuchEdgeException.ofIndex(index);
				} else {
					throw NoSuchVertexException.ofIndex(index);
				}
			}
			return indexToId.getInt(index);
		}

		@Override
		public int indexToIdIfExistInt(int index) {
			if (!(0 <= index && index < indexToId.size()))
				return -1;
			return indexToId.getInt(index);
		}

		@Override
		public int idToIndex(int id) {
			int idx = idToIndex.get(id);
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
		public int idToIndexIfExist(int id) {
			return idToIndex.get(id);
		}
	}

	@Override
	public IntGraph build() {
		return buildFromReIndexed(ibuilder.reIndexAndBuild(true, true));
	}

	@Override
	public IntGraph buildMutable() {
		return buildFromReIndexed(ibuilder.reIndexAndBuildMutable(true, true));
	}

	private IntGraph buildFromReIndexed(IndexGraphBuilder.ReIndexedGraph reIndexedGraph) {
		IndexGraph iGraph = reIndexedGraph.graph();
		Optional<IndexGraphBuilder.ReIndexingMap> vReIndexing = reIndexedGraph.verticesReIndexing();
		Optional<IndexGraphBuilder.ReIndexingMap> eReIndexing = reIndexedGraph.edgesReIndexing();
		return new IntGraphImpl(iGraph, viMap, eiMap, vReIndexing.orElse(null), eReIndexing.orElse(null));
	}

}
