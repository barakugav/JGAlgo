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

	final GraphFactoryImpl<V, E> factory;
	final IndexGraphBuilder iBuilder;
	IndexIdMapImpl<V> viMap;
	IndexIdMapImpl<E> eiMap;
	private final Map<WeightsImpl.Index<?>, WeightsImpl.ObjMapped<V, ?>> verticesWeights = new IdentityHashMap<>();
	private final Map<WeightsImpl.Index<?>, WeightsImpl.ObjMapped<E, ?>> edgesWeights = new IdentityHashMap<>();
	private IdBuilder<V> vertexBuilder;
	private IdBuilder<E> edgeBuilder;

	private boolean isNewGraphShouldStealInterior;
	private Graph<V, E> lastImmutableGraph;
	private Optional<IndexGraphBuilder.ReIndexingMap> lastImmutableVerticesReIndexingMap;
	private Optional<IndexGraphBuilder.ReIndexingMap> lastImmutableEdgesReIndexingMap;

	GraphBuilderImpl(GraphFactoryImpl<V, E> factory) {
		this.factory = factory;
		this.iBuilder = factory.indexFactory.newBuilder();
		viMap = IndexIdMapImpl.newEmpty(iBuilder.vertices(), true, 0);
		eiMap = IndexIdMapImpl.newEmpty(iBuilder.edges(), false, 0);
		resetVertexAndEdgeBuilders();
	}

	GraphBuilderImpl(GraphFactoryImpl<V, E> factory, Graph<V, E> g, boolean copyVerticesWeights,
			boolean copyEdgesWeights) {
		this.factory = factory;
		this.iBuilder = factory.indexFactory.newBuilderCopyOf(g.indexGraph(), copyVerticesWeights, copyEdgesWeights);
		viMap = IndexIdMapImpl.newCopyOf(g.indexGraphVerticesMap(), Optional.empty(), iBuilder.vertices(), true, false);
		eiMap = IndexIdMapImpl.newCopyOf(g.indexGraphEdgesMap(), Optional.empty(), iBuilder.edges(), false, false);
		resetVertexAndEdgeBuilders();
	}

	@Override
	public Set<V> vertices() {
		copyOnWriteFromLastGraph();
		return viMap.idSet();
	}

	@Override
	public Set<E> edges() {
		copyOnWriteFromLastGraph();
		return eiMap.idSet();
	}

	@Override
	public void addVertex(V vertex) {
		copyOnWriteFromLastGraph();
		if (vertex == null)
			throw new NullPointerException("Vertex must be non null");
		int vIdx = iBuilder.vertices().size();
		viMap.addId(vertex, vIdx);
		int vIdx2 = iBuilder.addVertexInt();
		assert vIdx == vIdx2;
	}

	@Override
	public void addVertices(Collection<? extends V> vertices) {
		copyOnWriteFromLastGraph();
		for (V vertex : vertices)
			if (vertex == null)
				throw new NullPointerException("Vertex must be non null");

		final int verticesNumBefore = iBuilder.vertices().size();
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
		iBuilder.addVertices(range(verticesNumBefore, nextIdx));
	}

	@Override
	public void addEdge(V source, V target, E edge) {
		copyOnWriteFromLastGraph();
		if (edge == null)
			throw new NullPointerException("Edge must be non null");
		int uIdx = viMap.idToIndex(source);
		int vIdx = viMap.idToIndex(target);
		int eIdx = iBuilder.edges().size();
		eiMap.addId(edge, eIdx);

		int eIdx2 = iBuilder.addEdge(uIdx, vIdx);
		assert eIdx == eIdx2;
	}

	@Override
	public void addEdges(EdgeSet<? extends V, ? extends E> edges) {
		copyOnWriteFromLastGraph();
		final int edgesNumBefore = iBuilder.edges().size();
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

			iBuilder.addEdgesReassignIds(new GraphImpl.AddEdgesIgnoreIdsIndexSet<>(edges, viMap));

		} catch (RuntimeException e) {
			for (; nextMapIdx-- > edgesNumBefore;)
				eiMap.rollBackRemove(nextMapIdx);
			throw e;
		}
	}

	@Override
	public void ensureVertexCapacity(int verticesNum) {
		copyOnWriteFromLastGraph();
		iBuilder.ensureVertexCapacity(verticesNum);
		viMap.ensureCapacity(verticesNum);
	}

	@Override
	public void ensureEdgeCapacity(int edgesNum) {
		copyOnWriteFromLastGraph();
		iBuilder.ensureEdgeCapacity(edgesNum);
		eiMap.ensureCapacity(edgesNum);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T, WeightsT extends Weights<V, T>> WeightsT verticesWeights(String key) {
		WeightsImpl.Index<T> indexWeights = iBuilder.verticesWeights(key);
		if (indexWeights == null)
			return null;
		return (WeightsT) verticesWeights
				.computeIfAbsent(indexWeights, iw -> WeightsImpl.ObjMapped.newInstance(iw, viMap));
	}

	@Override
	public <T, WeightsT extends Weights<V, T>> WeightsT addVerticesWeights(String key, Class<? super T> type,
			T defVal) {
		copyOnWriteFromLastGraph();
		iBuilder.addVerticesWeights(key, type, defVal);
		return verticesWeights(key);
	}

	@Override
	public Set<String> verticesWeightsKeys() {
		return iBuilder.verticesWeightsKeys();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T, WeightsT extends Weights<E, T>> WeightsT edgesWeights(String key) {
		WeightsImpl.Index<T> indexWeights = iBuilder.edgesWeights(key);
		if (indexWeights == null)
			return null;
		return (WeightsT) edgesWeights
				.computeIfAbsent(indexWeights, iw -> WeightsImpl.ObjMapped.newInstance(iw, eiMap));
	}

	@Override
	public <T, WeightsT extends Weights<E, T>> WeightsT addEdgesWeights(String key, Class<? super T> type, T defVal) {
		copyOnWriteFromLastGraph();
		iBuilder.addEdgesWeights(key, type, defVal);
		return edgesWeights(key);
	}

	@Override
	public Set<String> edgesWeightsKeys() {
		return iBuilder.edgesWeightsKeys();
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
		iBuilder.clear();
		if (lastImmutableGraph == null) {
			viMap.idsClear();
			eiMap.idsClear();
		} else {
			viMap = IndexIdMapImpl.newEmpty(iBuilder.vertices(), true, 0);
			eiMap = IndexIdMapImpl.newEmpty(iBuilder.edges(), false, 0);
			lastImmutableGraph = null;
			lastImmutableVerticesReIndexingMap = null;
			lastImmutableEdgesReIndexingMap = null;
		}
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
		return iBuilder.isDirected();
	}

	@Override
	public Graph<V, E> build() {
		if (lastImmutableGraph != null)
			return lastImmutableGraph;

		IndexGraphBuilder.ReIndexedGraph reIndexedGraph = iBuilder.reIndexAndBuild(true, true);
		IndexGraph iGraph = reIndexedGraph.graph;
		Optional<IndexGraphBuilder.ReIndexingMap> vReIndexing = reIndexedGraph.verticesReIndexing;
		Optional<IndexGraphBuilder.ReIndexingMap> eReIndexing = reIndexedGraph.edgesReIndexing;

		isNewGraphShouldStealInterior = iGraph.verticesWeightsKeys().isEmpty() && iGraph.edgesWeightsKeys().isEmpty();
		Graph<V, E> g = new GraphImpl<>(this, iGraph, vReIndexing, eReIndexing);

		if (isNewGraphShouldStealInterior) {
			lastImmutableGraph = g;
			lastImmutableVerticesReIndexingMap = vReIndexing;
			lastImmutableEdgesReIndexingMap = eReIndexing;
		}
		return g;
	}

	@Override
	public Graph<V, E> buildMutable() {
		if (lastImmutableGraph != null) {
			IndexGraph ig = lastImmutableGraph.indexGraph().copy();
			IndexIdMap<V> prevViMap = lastImmutableGraph.indexGraphVerticesMap();
			IndexIdMap<E> prevEiMap = lastImmutableGraph.indexGraphEdgesMap();
			return new GraphImpl<>(factory, ig, prevViMap, prevEiMap, Optional.empty(), Optional.empty());
		}

		IndexGraphBuilder.ReIndexedGraph reIndexedGraph = iBuilder.reIndexAndBuildMutable(true, true);
		IndexGraph iGraph = reIndexedGraph.graph;
		Optional<IndexGraphBuilder.ReIndexingMap> vReIndexing = reIndexedGraph.verticesReIndexing;
		Optional<IndexGraphBuilder.ReIndexingMap> eReIndexing = reIndexedGraph.edgesReIndexing;
		isNewGraphShouldStealInterior = false;
		return new GraphImpl<>(this, iGraph, vReIndexing, eReIndexing);
	}

	IndexIdMapImpl<V> stealViMap() {
		assert isNewGraphShouldStealInterior && viMap != null;
		IndexIdMapImpl<V> map = viMap;
		viMap = null;
		return map;
	}

	IndexIdMapImpl<E> stealEiMap() {
		assert isNewGraphShouldStealInterior && eiMap != null;
		IndexIdMapImpl<E> map = eiMap;
		eiMap = null;
		return map;
	}

	boolean isNewGraphShouldStealInterior() {
		return isNewGraphShouldStealInterior;
	}

	private void copyOnWriteFromLastGraph() {
		if (lastImmutableGraph == null)
			return;
		viMap = IndexIdMapImpl
				.newCopyOf(lastImmutableGraph.indexGraphVerticesMap(),
						lastImmutableVerticesReIndexingMap.map(IndexGraphBuilder.ReIndexingMap::inverse),
						iBuilder.vertices(), false, false);
		eiMap = IndexIdMapImpl
				.newCopyOf(lastImmutableGraph.indexGraphEdgesMap(),
						lastImmutableEdgesReIndexingMap.map(IndexGraphBuilder.ReIndexingMap::inverse), iBuilder.edges(),
						true, false);
		lastImmutableGraph = null;
		lastImmutableVerticesReIndexingMap = null;
		lastImmutableEdgesReIndexingMap = null;
	}

}
