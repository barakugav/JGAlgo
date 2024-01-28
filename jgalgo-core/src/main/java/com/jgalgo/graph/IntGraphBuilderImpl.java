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
	final IndexIntIdMapImpl viMap;
	final IndexIntIdMapImpl eiMap;
	private final Map<WeightsImpl.Index<?>, WeightsImpl.IntMapped<?>> verticesWeights = new IdentityHashMap<>();
	private final Map<WeightsImpl.Index<?>, WeightsImpl.IntMapped<?>> edgesWeights = new IdentityHashMap<>();
	private IdBuilderInt vertexBuilder;
	private IdBuilderInt edgeBuilder;

	IntGraphBuilderImpl(IntGraphFactoryImpl factory) {
		this.factory = factory;
		this.ibuilder = factory.indexFactory.newBuilder();
		viMap = IndexIntIdMapImpl.newEmpty(ibuilder.vertices(), false, 0);
		eiMap = IndexIntIdMapImpl.newEmpty(ibuilder.edges(), true, 0);
		resetVertexAndEdgeBuilders();
	}

	IntGraphBuilderImpl(IntGraphFactoryImpl factory, Graph<Integer, Integer> g, boolean copyVerticesWeights,
			boolean copyEdgesWeights) {
		this.factory = factory;
		this.ibuilder = factory.indexFactory.newBuilderCopyOf(g.indexGraph(), copyVerticesWeights, copyEdgesWeights);
		viMap = IndexIntIdMapImpl
				.newCopyOf(g.indexGraphVerticesMap(), Optional.empty(), ibuilder.vertices(), false, false);
		eiMap = IndexIntIdMapImpl.newCopyOf(g.indexGraphEdgesMap(), Optional.empty(), ibuilder.edges(), true, false);
		resetVertexAndEdgeBuilders();
	}

	@Override
	public IntSet vertices() {
		return viMap.idSet();
	}

	@Override
	public IntSet edges() {
		return eiMap.idSet();
	}

	@Override
	public void addVertex(int vertex) {
		if (vertex < 0)
			throw new IllegalArgumentException("Vertex must be non negative");

		int vIdx = ibuilder.vertices().size();
		viMap.addId(vertex, vIdx);
		int vIdx2 = ibuilder.addVertexInt();
		assert vIdx == vIdx2;
	}

	@Override
	public void addVertices(Collection<? extends Integer> vertices) {
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
	}

	@Override
	public void addEdge(int source, int target, int edge) {
		if (edge < 0)
			throw new IllegalArgumentException("Edge must be non negative");

		int uIdx = viMap.idToIndex(source);
		int vIdx = viMap.idToIndex(target);
		int eIdx = ibuilder.edges().size();
		eiMap.addId(edge, eIdx);
		int eIdx2 = ibuilder.addEdge(uIdx, vIdx);
		assert eIdx == eIdx2;
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
	public <T, WeightsT extends Weights<Integer, T>> WeightsT verticesWeights(String key) {
		WeightsImpl.Index<T> indexWeights = ibuilder.verticesWeights(key);
		if (indexWeights == null)
			return null;
		return (WeightsT) verticesWeights
				.computeIfAbsent(indexWeights, iw -> WeightsImpl.IntMapped.newInstance(iw, viMap));
	}

	@Override
	public <T, WeightsT extends Weights<Integer, T>> WeightsT addVerticesWeights(String key, Class<? super T> type,
			T defVal) {
		ibuilder.addVerticesWeights(key, type, defVal);
		return verticesWeights(key);
	}

	@Override
	public Set<String> verticesWeightsKeys() {
		return ibuilder.verticesWeightsKeys();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T, WeightsT extends Weights<Integer, T>> WeightsT edgesWeights(String key) {
		WeightsImpl.Index<T> indexWeights = ibuilder.edgesWeights(key);
		if (indexWeights == null)
			return null;
		return (WeightsT) edgesWeights
				.computeIfAbsent(indexWeights, iw -> WeightsImpl.IntMapped.newInstance(iw, eiMap));
	}

	@Override
	public <T, WeightsT extends Weights<Integer, T>> WeightsT addEdgesWeights(String key, Class<? super T> type,
			T defVal) {
		ibuilder.addEdgesWeights(key, type, defVal);
		return edgesWeights(key);
	}

	@Override
	public Set<String> edgesWeightsKeys() {
		return ibuilder.edgesWeightsKeys();
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
		return new IntGraphImpl(factory, iGraph, viMap, eiMap, vReIndexing, eReIndexing);
	}

}
