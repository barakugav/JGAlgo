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
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSet;

class IntGraphBuilderImpl implements IntGraphBuilder {

	private final IntGraphFactoryImpl factory;
	final IndexGraphBuilder ibuilder;
	private boolean userProvideVerticesIds;
	private boolean userProvideEdgesIds;
	final IndexIntIdMapImpl viMap;
	final IndexIntIdMapImpl eiMap;
	private final Map<WeightsImpl.Index<?>, WeightsImpl.IntMapped<?>> verticesWeights = new IdentityHashMap<>();
	private final Map<WeightsImpl.Index<?>, WeightsImpl.IntMapped<?>> edgesWeights = new IdentityHashMap<>();

	IntGraphBuilderImpl(IntGraphFactoryImpl factory) {
		this.factory = factory;
		this.ibuilder = factory.indexFactory.newBuilder();
		viMap = IndexIntIdMapImpl.newEmpty(ibuilder.vertices(), false, 0);
		eiMap = IndexIntIdMapImpl.newEmpty(ibuilder.edges(), true, 0);
	}

	IntGraphBuilderImpl(IntGraphFactoryImpl factory, Graph<Integer, Integer> g, boolean copyVerticesWeights,
			boolean copyEdgesWeights) {
		this.factory = factory;
		this.ibuilder = factory.indexFactory.newBuilderCopyOf(g.indexGraph(), copyVerticesWeights, copyEdgesWeights);
		viMap = IndexIntIdMapImpl.newCopyOf(g.indexGraphVerticesMap(), null, ibuilder.vertices(), false, false);
		eiMap = IndexIntIdMapImpl.newCopyOf(g.indexGraphEdgesMap(), null, ibuilder.edges(), true, false);
	}

	@Override
	public IntSet vertices() {
		return viMap.idSet();
	}

	@Override
	public IntSet edges() {
		return eiMap.idSet();
	}

	private boolean canAddVertexWithoutId() {
		return vertices().isEmpty() || !userProvideVerticesIds;
	}

	private boolean canAddVertexWithId() {
		return vertices().isEmpty() || userProvideVerticesIds;
	}

	private boolean canAddEdgeWithoutId() {
		return edges().isEmpty() || !userProvideEdgesIds;
	}

	private boolean canAddEdgeWithId() {
		return edges().isEmpty() || userProvideEdgesIds;
	}

	@Override
	public int addVertex() {
		if (!canAddVertexWithoutId())
			throw new IllegalArgumentException("Can't mix addVertex() and addVertex(id), "
					+ "if IDs are provided for some of the vertices, they must be provided for all");
		int vIndex = ibuilder.addVertex();
		int vId = vIndex + 1; // +1 because we want to avoid 0 in fastutil open hash maps
		viMap.addId(vId, vIndex);
		return vId;
	}

	@Override
	public void addVertex(int vertex) {
		if (!canAddVertexWithId())
			throw new IllegalArgumentException("Can't mix addVertex() and addVertex(id), "
					+ "if IDs are provided for some of the vertices, they must be provided for all");
		if (vertex < 0)
			throw new IllegalArgumentException("Vertex must be non negative");

		int vIdx = ibuilder.vertices().size();
		viMap.addId(vertex, vIdx);
		int vIdx2 = ibuilder.addVertex();
		assert vIdx == vIdx2;

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
		ensureVertexCapacity(verticesNumBefore + vertices.size());
		int nextIdx = verticesNumBefore;
		for (int vertex : vertices) {
			boolean added = viMap.addIdIfNotDuplicate(vertex, nextIdx);
			if (!added) {
				for (; nextIdx-- > verticesNumBefore;)
					viMap.rollBackRemove(nextIdx);
				throw new IllegalArgumentException("Duplicate vertex: " + vertex);
			}
			nextIdx++;
		}
		ibuilder.addVertices(range(verticesNumBefore, nextIdx));

		userProvideVerticesIds = true;
	}

	@Override
	public int addEdge(int source, int target) {
		if (!canAddEdgeWithoutId())
			throw new IllegalStateException("Can't mix addEdge(u,v) and addEdge(u,v,id), "
					+ "if IDs are provided for some of the edges, they must be provided for all");

		int uIdx = viMap.idToIndex(source);
		int vIdx = viMap.idToIndex(target);
		int eIdx = ibuilder.addEdge(uIdx, vIdx);
		int id = eIdx + 1; // avoid null key in open hash maps
		eiMap.addId(id, eIdx);
		return id;
	}

	@Override
	public void addEdge(int source, int target, int edge) {
		if (!canAddEdgeWithId())
			throw new IllegalStateException("Can't mix addEdge(u,v) and addEdge(u,v,id), "
					+ "if IDs are provided for some of the edges, they must be provided for all");
		if (edge < 0)
			throw new IllegalArgumentException("Edge must be non negative");

		int uIdx = viMap.idToIndex(source);
		int vIdx = viMap.idToIndex(target);
		int eIdx = ibuilder.edges().size();
		eiMap.addId(edge, eIdx);
		int eIdx2 = ibuilder.addEdge(uIdx, vIdx);
		assert eIdx == eIdx2;

		userProvideEdgesIds = true;
	}

	@Override
	public void addEdges(EdgeSet<? extends Integer, ? extends Integer> edges) {
		final int edgesNumBefore = ibuilder.edges().size();
		ensureEdgeCapacity(edgesNumBefore + edges.size());
		int nextMapIdx = edgesNumBefore;
		try {
			for (int edge : edges) {
				if (edge < 0)
					throw new IllegalArgumentException("Edge must be non negative");
				boolean added = eiMap.addIdIfNotDuplicate(edge, nextMapIdx);
				if (!added)
					throw new IllegalArgumentException("Duplicate edge: " + edge);
				nextMapIdx++;
			}

			@SuppressWarnings("unchecked")
			EdgeSet<Integer, Integer> edges0 = (EdgeSet<Integer, Integer>) edges;
			ibuilder.addEdgesReassignIds(new IntGraphImpl.AddEdgesIgnoreIdsIndexSet(edges0, viMap));

		} catch (RuntimeException e) {
			for (; nextMapIdx-- > edgesNumBefore;)
				eiMap.rollBackRemove(nextMapIdx);
			throw e;
		}

		userProvideEdgesIds = true;
	}

	@Override
	public void ensureVertexCapacity(int verticesNum) {
		ibuilder.ensureVertexCapacity(verticesNum);
		viMap.ensureCapacity(verticesNum);
	}

	@Override
	public void ensureEdgeCapacity(int edgesNum) {
		ibuilder.ensureEdgeCapacity(edgesNum);
		eiMap.ensureCapacity(edgesNum);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T, WeightsT extends Weights<Integer, T>> WeightsT getVerticesWeights(String key) {
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
	public <T, WeightsT extends Weights<Integer, T>> WeightsT getEdgesWeights(String key) {
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
		viMap.idsClear();
		eiMap.idsClear();
		verticesWeights.clear();
		edgesWeights.clear();
		userProvideVerticesIds = false;
		userProvideEdgesIds = false;
	}

	@Override
	public boolean isDirected() {
		return ibuilder.isDirected();
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
		return new IntGraphImpl(factory, iGraph, viMap, eiMap, vReIndexing.orElse(null), eReIndexing.orElse(null));
	}

}
