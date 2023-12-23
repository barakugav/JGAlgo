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

class GraphBuilderImpl<V, E> implements GraphBuilder<V, E> {

	private final GraphFactoryImpl<V, E> factory;
	final IndexGraphBuilder ibuilder;
	final IndexIdMapImpl<V> viMap;
	final IndexIdMapImpl<E> eiMap;
	private final Map<WeightsImpl.Index<?>, WeightsImpl.ObjMapped<V, ?>> verticesWeights = new IdentityHashMap<>();
	private final Map<WeightsImpl.Index<?>, WeightsImpl.ObjMapped<E, ?>> edgesWeights = new IdentityHashMap<>();
	private IdBuilder<V> vertexBuilder;
	private IdBuilder<E> edgeBuilder;

	GraphBuilderImpl(GraphFactoryImpl<V, E> factory) {
		this.factory = factory;
		this.ibuilder = factory.indexFactory.newBuilder();
		viMap = IndexIdMapImpl.newEmpty(ibuilder.vertices(), false, 0);
		eiMap = IndexIdMapImpl.newEmpty(ibuilder.edges(), true, 0);
		resetVertexAndEdgeBuilders();
	}

	GraphBuilderImpl(GraphFactoryImpl<V, E> factory, Graph<V, E> g, boolean copyVerticesWeights,
			boolean copyEdgesWeights) {
		this.factory = factory;
		this.ibuilder = factory.indexFactory.newBuilderCopyOf(g.indexGraph(), copyVerticesWeights, copyEdgesWeights);
		viMap = IndexIdMapImpl.newCopyOf(g.indexGraphVerticesMap(), Optional.empty(), ibuilder.vertices(), false,
				false);
		eiMap = IndexIdMapImpl.newCopyOf(g.indexGraphEdgesMap(), Optional.empty(), ibuilder.edges(), true, false);
		resetVertexAndEdgeBuilders();
	}

	@Override
	public Set<V> vertices() {
		return viMap.idSet();
	}

	@Override
	public Set<E> edges() {
		return eiMap.idSet();
	}

	@Override
	public void addVertex(V vertex) {
		if (vertex == null)
			throw new NullPointerException("Vertex must be non null");
		int vIdx = ibuilder.vertices().size();
		viMap.addId(vertex, vIdx);
		int vIdx2 = ibuilder.addVertexInt();
		assert vIdx == vIdx2;
	}

	@Override
	public void addVertices(Collection<? extends V> vertices) {
		for (V vertex : vertices)
			if (vertex == null)
				throw new NullPointerException("Vertex must be non null");

		final int verticesNumBefore = ibuilder.vertices().size();
		ensureVertexCapacity(verticesNumBefore + vertices.size());
		int nextIdx = verticesNumBefore;
		for (V vertex : vertices) {
			boolean added = viMap.addIdIfNotDuplicate(vertex, nextIdx);
			if (!added) {
				for (; nextIdx-- > verticesNumBefore;)
					viMap.rollBackRemove(nextIdx);
				throw new IllegalArgumentException("Duplicate vertex: " + vertex);
			}
			nextIdx++;
		}
		ibuilder.addVertices(range(verticesNumBefore, nextIdx));
	}

	@Override
	public void addEdge(V source, V target, E edge) {
		if (edge == null)
			throw new NullPointerException("Edge must be non null");
		int uIdx = viMap.idToIndex(source);
		int vIdx = viMap.idToIndex(target);
		int eIdx = ibuilder.edges().size();
		eiMap.addId(edge, eIdx);

		int eIdx2 = ibuilder.addEdge(uIdx, vIdx);
		assert eIdx == eIdx2;
	}

	@Override
	public void addEdges(EdgeSet<? extends V, ? extends E> edges) {
		final int edgesNumBefore = ibuilder.edges().size();
		ensureEdgeCapacity(edgesNumBefore + edges.size());
		int nextMapIdx = edgesNumBefore;
		try {
			for (E edge : edges) {
				if (edge == null)
					throw new NullPointerException("Edge must be non null");
				boolean added = eiMap.addIdIfNotDuplicate(edge, nextMapIdx);
				if (!added)
					throw new IllegalArgumentException("Duplicate edge: " + edge);
				nextMapIdx++;
			}

			ibuilder.addEdgesReassignIds(new GraphImpl.AddEdgesIgnoreIdsIndexSet<>(edges, viMap));

		} catch (RuntimeException e) {
			for (; nextMapIdx-- > edgesNumBefore;)
				eiMap.rollBackRemove(nextMapIdx);
			throw e;
		}
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
	public IdBuilder<V> vertexBuilder() {
		return vertexBuilder;
	}

	@Override
	public IdBuilder<E> edgeBuilder() {
		return edgeBuilder;
	}

	@Override
	public void clear() {
		ibuilder.clear();
		viMap.idsClear();
		eiMap.idsClear();
		verticesWeights.clear();
		edgesWeights.clear();
		resetVertexAndEdgeBuilders();
	}

	private void resetVertexAndEdgeBuilders() {
		vertexBuilder = factory.vertexFactory != null ? factory.vertexFactory.get() : null;
		edgeBuilder = factory.edgeFactory != null ? factory.edgeFactory.get() : null;
	}

	@Override
	public boolean isDirected() {
		return ibuilder.isDirected();
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
		return new GraphImpl<>(factory, iGraph, viMap, eiMap, vReIndexing, eReIndexing);
	}

}
