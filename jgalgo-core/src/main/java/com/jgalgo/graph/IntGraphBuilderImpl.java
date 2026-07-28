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

	final IntGraphFactoryImpl factory;
	final IndexGraphBuilder iBuilder;
	IndexIntIdMapImpl viMap;
	IndexIntIdMapImpl eiMap;
	private final Map<WeightsImpl.Index<?>, WeightsImpl.IntMapped<?>> verticesWeights = new IdentityHashMap<>();
	private final Map<WeightsImpl.Index<?>, WeightsImpl.IntMapped<?>> edgesWeights = new IdentityHashMap<>();
	private IdBuilderInt vertexBuilder;
	private IdBuilderInt edgeBuilder;

	private boolean isNewGraphShouldStealInterior;
	private IntGraph lastImmutableGraph;
	private Optional<IndexGraphBuilder.ReIndexingMap> lastImmutableVerticesReIndexingMap;
	private Optional<IndexGraphBuilder.ReIndexingMap> lastImmutableEdgesReIndexingMap;

	IntGraphBuilderImpl(IntGraphFactoryImpl factory) {
		this.factory = factory;
		this.iBuilder = factory.indexFactory.newBuilder();
		viMap = IndexIntIdMapImpl.newEmpty(iBuilder.vertices(), true, 0);
		eiMap = IndexIntIdMapImpl.newEmpty(iBuilder.edges(), false, 0);
		resetVertexAndEdgeBuilders();
	}

	IntGraphBuilderImpl(IntGraphFactoryImpl factory, Graph<Integer, Integer> g, boolean copyVerticesWeights,
			boolean copyEdgesWeights) {
		this.factory = factory;
		this.iBuilder = factory.indexFactory.newBuilderCopyOf(g.indexGraph(), copyVerticesWeights, copyEdgesWeights);
		viMap = IndexIntIdMapImpl
				.newCopyOf(g.indexGraphVerticesMap(), Optional.empty(), iBuilder.vertices(), true, false);
		eiMap = IndexIntIdMapImpl.newCopyOf(g.indexGraphEdgesMap(), Optional.empty(), iBuilder.edges(), false, false);
		resetVertexAndEdgeBuilders();
	}

	@Override
	public IntSet vertices() {
		copyOnWriteFromLastGraph();
		return viMap.idSet();
	}

	@Override
	public IntSet edges() {
		copyOnWriteFromLastGraph();
		return eiMap.idSet();
	}

	@Override
	public void addVertex(int vertex) {
		copyOnWriteFromLastGraph();
		if (vertex < 0)
			throw new IllegalArgumentException("Vertex must be non negative");

		int vIdx = iBuilder.vertices().size();
		viMap.addId(vertex, vIdx);
		int vIdx2 = iBuilder.addVertexInt();
		assert vIdx == vIdx2;
	}

	@Override
	public void addVertices(Collection<? extends Integer> vertices) {
		copyOnWriteFromLastGraph();
		if (vertices.isEmpty())
			return;
		if (!(vertices instanceof IntCollection))
			for (Integer vertex : vertices)
				if (vertex == null)
					throw new NullPointerException("Vertex must be non null");
		for (int vertex : vertices)
			if (vertex < 0)
				throw new IllegalArgumentException("Vertex must be non negative");

		final int verticesNumBefore = iBuilder.vertices().size();
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
		iBuilder.addVertices(range(verticesNumBefore, nextIdx));
	}

	@Override
	public void addEdge(int source, int target, int edge) {
		copyOnWriteFromLastGraph();
		if (edge < 0)
			throw new IllegalArgumentException("Edge must be non negative");

		int uIdx = viMap.idToIndex(source);
		int vIdx = viMap.idToIndex(target);
		int eIdx = iBuilder.edges().size();
		eiMap.addId(edge, eIdx);
		int eIdx2 = iBuilder.addEdge(uIdx, vIdx);
		assert eIdx == eIdx2;
	}

	@Override
	public void addEdges(EdgeSet<? extends Integer, ? extends Integer> edges) {
		copyOnWriteFromLastGraph();
		final int edgesNumBefore = iBuilder.edges().size();
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
			iBuilder.addEdgesReassignIds(new IntGraphImpl.AddEdgesIgnoreIdsIndexSet(edges0, viMap));

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
	public <T, WeightsT extends Weights<Integer, T>> WeightsT verticesWeights(String key) {
		WeightsImpl.Index<T> indexWeights = iBuilder.verticesWeights(key);
		if (indexWeights == null)
			return null;
		return (WeightsT) verticesWeights
				.computeIfAbsent(indexWeights, iw -> WeightsImpl.IntMapped.newInstance(iw, viMap));
	}

	@Override
	public <T, WeightsT extends Weights<Integer, T>> WeightsT addVerticesWeights(String key, Class<? super T> type,
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
	public <T, WeightsT extends Weights<Integer, T>> WeightsT edgesWeights(String key) {
		WeightsImpl.Index<T> indexWeights = iBuilder.edgesWeights(key);
		if (indexWeights == null)
			return null;
		return (WeightsT) edgesWeights
				.computeIfAbsent(indexWeights, iw -> WeightsImpl.IntMapped.newInstance(iw, eiMap));
	}

	@Override
	public <T, WeightsT extends Weights<Integer, T>> WeightsT addEdgesWeights(String key, Class<? super T> type,
			T defVal) {
		copyOnWriteFromLastGraph();
		iBuilder.addEdgesWeights(key, type, defVal);
		return edgesWeights(key);
	}

	@Override
	public Set<String> edgesWeightsKeys() {
		return iBuilder.edgesWeightsKeys();
	}

	@Override
	public IdBuilderInt vertexBuilder() {
		return vertexBuilder;
	}

	@Override
	public IdBuilderInt edgeBuilder() {
		return edgeBuilder;
	}

	@Override
	public void clear() {
		iBuilder.clear();
		if (lastImmutableGraph == null) {
			viMap.idsClear();
			eiMap.idsClear();
		} else {
			viMap = IndexIntIdMapImpl.newEmpty(iBuilder.vertices(), true, 0);
			eiMap = IndexIntIdMapImpl.newEmpty(iBuilder.edges(), false, 0);
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
	public IntGraph build() {
		if (lastImmutableGraph != null)
			return lastImmutableGraph;

		IndexGraphBuilder.ReIndexedGraph reIndexedGraph = iBuilder.reIndexAndBuild(true, true);
		IndexGraph iGraph = reIndexedGraph.graph;
		Optional<IndexGraphBuilder.ReIndexingMap> vReIndexing = reIndexedGraph.verticesReIndexing;
		Optional<IndexGraphBuilder.ReIndexingMap> eReIndexing = reIndexedGraph.edgesReIndexing;

		isNewGraphShouldStealInterior = iGraph.verticesWeightsKeys().isEmpty() && iGraph.edgesWeightsKeys().isEmpty();
		IntGraph g = new IntGraphImpl(this, iGraph, vReIndexing, eReIndexing);

		if (isNewGraphShouldStealInterior) {
			lastImmutableGraph = g;
			lastImmutableVerticesReIndexingMap = vReIndexing;
			lastImmutableEdgesReIndexingMap = eReIndexing;
		}
		return g;
	}

	@Override
	public IntGraph buildMutable() {
		if (lastImmutableGraph != null) {
			IndexGraph ig = lastImmutableGraph.indexGraph().copy();
			IndexIntIdMap prevViMap = lastImmutableGraph.indexGraphVerticesMap();
			IndexIntIdMap prevEiMap = lastImmutableGraph.indexGraphEdgesMap();
			return new IntGraphImpl(factory, ig, prevViMap, prevEiMap, Optional.empty(), Optional.empty());
		}

		IndexGraphBuilder.ReIndexedGraph reIndexedGraph = iBuilder.reIndexAndBuildMutable(true, true);
		IndexGraph iGraph = reIndexedGraph.graph;
		Optional<IndexGraphBuilder.ReIndexingMap> vReIndexing = reIndexedGraph.verticesReIndexing;
		Optional<IndexGraphBuilder.ReIndexingMap> eReIndexing = reIndexedGraph.edgesReIndexing;
		isNewGraphShouldStealInterior = false;
		return new IntGraphImpl(factory, iGraph, viMap, eiMap, vReIndexing, eReIndexing);
	}

	IndexIntIdMapImpl stealViMap() {
		assert isNewGraphShouldStealInterior && viMap != null;
		IndexIntIdMapImpl map = viMap;
		viMap = null;
		return map;
	}

	IndexIntIdMapImpl stealEiMap() {
		assert isNewGraphShouldStealInterior && eiMap != null;
		IndexIntIdMapImpl map = eiMap;
		eiMap = null;
		return map;
	}

	boolean isNewGraphShouldStealInterior() {
		return isNewGraphShouldStealInterior;
	}

	private void copyOnWriteFromLastGraph() {
		if (lastImmutableGraph == null)
			return;
		viMap = IndexIntIdMapImpl
				.newCopyOf(lastImmutableGraph.indexGraphVerticesMap(),
						lastImmutableVerticesReIndexingMap.map(IndexGraphBuilder.ReIndexingMap::inverse),
						iBuilder.vertices(), false, false);
		eiMap = IndexIntIdMapImpl
				.newCopyOf(lastImmutableGraph.indexGraphEdgesMap(),
						lastImmutableEdgesReIndexingMap.map(IndexGraphBuilder.ReIndexingMap::inverse), iBuilder.edges(),
						true, false);
		lastImmutableGraph = null;
		lastImmutableVerticesReIndexingMap = null;
		lastImmutableEdgesReIndexingMap = null;
	}

}
