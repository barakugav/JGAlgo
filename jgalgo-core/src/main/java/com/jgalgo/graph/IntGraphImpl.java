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
import com.jgalgo.graph.Graphs.ImmutableGraph;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSet;

class IntGraphImpl extends AbstractGraphImpl<Integer, Integer> implements IntGraph {

	final IndexIntIdMapImpl viMap;
	final IndexIntIdMapImpl eiMap;
	private final Map<WeightsImpl.Index<?>, WeightsImpl.IntMapped<?>> verticesWeights = new IdentityHashMap<>();
	private final Map<WeightsImpl.Index<?>, WeightsImpl.IntMapped<?>> edgesWeights = new IdentityHashMap<>();
	private final IdBuilderInt vertexBuilder;
	private final IdBuilderInt edgeBuilder;

	IntGraphImpl(IntGraphFactoryImpl factory) {
		super(factory.indexFactory.newGraph());
		viMap = IndexIntIdMapImpl.newEmpty(indexGraph.vertices(), true, factory.indexFactory.expectedVerticesNum);
		eiMap = IndexIntIdMapImpl.newEmpty(indexGraph.edges(), false, factory.indexFactory.expectedEdgesNum);
		viMap.initListeners(indexGraph);
		eiMap.initListeners(indexGraph);
		vertexBuilder = factory.vertexFactory != null ? factory.vertexFactory.get() : null;
		edgeBuilder = factory.edgeFactory != null ? factory.edgeFactory.get() : null;
	}

	IntGraphImpl(IntGraphFactoryImpl factory, IndexGraph indexGraph, IndexIdMap<Integer> viMap,
			IndexIdMap<Integer> eiMap, Optional<IndexGraphBuilder.ReIndexingMap> vReIndexing,
			Optional<IndexGraphBuilder.ReIndexingMap> eReIndexing) {
		super(indexGraph);
		boolean immutable = this.indexGraph instanceof ImmutableGraph;
		this.viMap = IndexIntIdMapImpl.newCopyOf(viMap, vReIndexing, this.indexGraph.vertices(), true, immutable);
		this.eiMap = IndexIntIdMapImpl.newCopyOf(eiMap, eReIndexing, this.indexGraph.edges(), false, immutable);
		if (!immutable) {
			this.viMap.initListeners(this.indexGraph);
			this.eiMap.initListeners(this.indexGraph);
		}
		vertexBuilder = factory.vertexFactory != null ? factory.vertexFactory.get() : null;
		edgeBuilder = factory.edgeFactory != null ? factory.edgeFactory.get() : null;
	}

	/* If builder.isNewGraphShouldStealInterior(), the graph steal the vertices and edges maps of the builder */
	IntGraphImpl(IntGraphBuilderImpl builder, IndexGraph indexGraph,
			Optional<IndexGraphBuilder.ReIndexingMap> vReIndexing,
			Optional<IndexGraphBuilder.ReIndexingMap> eReIndexing) {
		super(indexGraph);
		boolean immutable = this.indexGraph instanceof ImmutableGraph;

		if (builder.isNewGraphShouldStealInterior()) {
			viMap = builder.stealViMap().intoImmutable(vReIndexing);
			eiMap = builder.stealEiMap().intoImmutable(eReIndexing);
		} else {
			viMap = IndexIntIdMapImpl
					.newCopyOf(builder.viMap, vReIndexing, this.indexGraph.vertices(), true, immutable);
			eiMap = IndexIntIdMapImpl.newCopyOf(builder.eiMap, eReIndexing, this.indexGraph.edges(), false, immutable);
		}
		if (!immutable) {
			viMap.initListeners(this.indexGraph);
			eiMap.initListeners(this.indexGraph);
		}

		IntGraphFactoryImpl factory = builder.factory;
		vertexBuilder = factory.vertexFactory != null ? factory.vertexFactory.get() : null;
		edgeBuilder = factory.edgeFactory != null ? factory.edgeFactory.get() : null;
	}

	@Override
	public IndexIntIdMap indexGraphVerticesMap() {
		return viMap;
	}

	@Override
	public IndexIntIdMap indexGraphEdgesMap() {
		return eiMap;
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
		int vIdx = indexGraph.vertices().size();
		viMap.addId(vertex, vIdx);
		try {
			int vIdx2 = indexGraph.addVertexInt();
			assert vIdx == vIdx2;
		} catch (RuntimeException e) {
			viMap.rollBackRemove(vIdx);
			throw e;
		}
	}

	@Override
	public void addVertices(Collection<? extends Integer> vertices) {
		if (!(vertices instanceof IntCollection))
			for (Integer vertex : vertices)
				if (vertex == null)
					throw new NullPointerException("Vertex must be non null");
		for (int vertex : vertices)
			if (vertex < 0)
				throw new IllegalArgumentException("Vertex must be non negative");

		final int verticesNumBefore = indexGraph.vertices().size();
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
		indexGraph.addVertices(range(verticesNumBefore, nextIdx));
	}

	@Override
	public void removeVertex(int vertex) {
		int vIdx = viMap.idToIndex(vertex);
		indexGraph.removeVertex(vIdx);
	}

	@Override
	public void removeVertices(Collection<? extends Integer> vertices) {
		@SuppressWarnings("unchecked")
		IntCollection vIdxs = IndexIdMaps.idToIndexCollection((Collection<Integer>) vertices, viMap);
		indexGraph.removeVertices(vIdxs);
	}

	@Override
	public void renameVertex(int vertex, int newId) {
		if (newId < 0)
			throw new IllegalArgumentException("Vertex must be non negative");
		viMap.renameId(vertex, newId);
	}

	@Override
	public IEdgeSet outEdges(int source) {
		int uIdx = viMap.idToIndex(source);
		IEdgeSet indexSet = indexGraph.outEdges(uIdx);
		return IndexIdMaps.indexToIdEdgeSet(indexSet, this);
	}

	@Override
	public IEdgeSet inEdges(int target) {
		int vIdx = viMap.idToIndex(target);
		IEdgeSet indexSet = indexGraph.inEdges(vIdx);
		return IndexIdMaps.indexToIdEdgeSet(indexSet, this);
	}

	@Override
	public int getEdge(int source, int target) {
		int uIdx = viMap.idToIndex(source);
		int vIdx = viMap.idToIndex(target);
		int eIdx = indexGraph.getEdge(uIdx, vIdx);
		return eiMap.indexToIdIfExistInt(eIdx);
	}

	@Override
	public IEdgeSet getEdges(int source, int target) {
		int uIdx = viMap.idToIndex(source);
		int vIdx = viMap.idToIndex(target);
		IEdgeSet indexSet = indexGraph.getEdges(uIdx, vIdx);
		return IndexIdMaps.indexToIdEdgeSet(indexSet, this);
	}

	@Override
	public void addEdge(int source, int target, int edge) {
		if (edge < 0)
			throw new IllegalArgumentException("Edge must be non negative");

		int uIdx = viMap.idToIndex(source);
		int vIdx = viMap.idToIndex(target);
		int eIdx = indexGraph.edges().size();
		eiMap.addId(edge, eIdx);

		try {
			int eIdx2 = indexGraph.addEdge(uIdx, vIdx);
			assert eIdx == eIdx2;
		} catch (RuntimeException e) {
			eiMap.rollBackRemove(eIdx);
			throw e;
		}
	}

	@Override
	public void addEdges(EdgeSet<? extends Integer, ? extends Integer> edges) {
		final int edgesNumBefore = indexGraph.edges().size();
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
			indexGraph.addEdgesReassignIds(new AddEdgesIgnoreIdsIndexSet(edges0, viMap));

		} catch (RuntimeException e) {
			for (; nextMapIdx-- > edgesNumBefore;)
				eiMap.rollBackRemove(nextMapIdx);
			throw e;
		}
	}

	static class AddEdgesIgnoreIdsIndexSet extends AbstractIntSet implements IEdgeSet {

		private final EdgeSet<Integer, Integer> idSet;
		private final IndexIntIdMap viMap;

		AddEdgesIgnoreIdsIndexSet(EdgeSet<Integer, Integer> idSet, IndexIntIdMap viMap) {
			this.idSet = idSet;
			this.viMap = viMap;
		}

		@Override
		public int size() {
			return idSet.size();
		}

		@Override
		public IEdgeIter iterator() {
			return new IEdgeIter() {

				EdgeIter<Integer, Integer> idIter = idSet.iterator();

				@Override
				public boolean hasNext() {
					return idIter.hasNext();
				}

				@Override
				public int nextInt() {
					idIter.next();
					return -1; /* ignore edges IDs */
				}

				@Override
				public int peekNextInt() {
					idIter.peekNext();
					return -1; /* ignore edges IDs */
				}

				@Override
				public int sourceInt() {
					return viMap.idToIndex(idIter.source().intValue());
				}

				@Override
				public int targetInt() {
					return viMap.idToIndex(idIter.target().intValue());
				}
			};
		}
	}

	@Override
	public void removeEdge(int edge) {
		int eIdx = eiMap.idToIndex(edge);
		indexGraph.removeEdge(eIdx);
	}

	@Override
	public void removeEdges(Collection<? extends Integer> edges) {
		@SuppressWarnings("unchecked")
		IntCollection eIdxs = IndexIdMaps.idToIndexCollection((Collection<Integer>) edges, eiMap);
		indexGraph.removeEdges(eIdxs);
	}

	@Override
	public void removeEdgesOf(int source) {
		int uIdx = viMap.idToIndex(source);
		indexGraph.removeEdgesOf(uIdx);
	}

	@Override
	public void removeOutEdgesOf(int source) {
		indexGraph.removeOutEdgesOf(viMap.idToIndex(source));
	}

	@Override
	public void removeInEdgesOf(int target) {
		indexGraph.removeInEdgesOf(viMap.idToIndex(target));
	}

	@Override
	public void renameEdge(int edge, int newId) {
		if (newId < 0)
			throw new IllegalArgumentException("Edge must be non negative");
		eiMap.renameId(edge, newId);
	}

	@Override
	public void moveEdge(int edge, int newSource, int newTarget) {
		indexGraph.moveEdge(eiMap.idToIndex(edge), viMap.idToIndex(newSource), viMap.idToIndex(newTarget));
	}

	@Override
	public int edgeSource(int edge) {
		int eIdx = eiMap.idToIndex(edge);
		int uIdx = indexGraph.edgeSource(eIdx);
		return viMap.indexToIdInt(uIdx);
	}

	@Override
	public int edgeTarget(int edge) {
		int eIdx = eiMap.idToIndex(edge);
		int vIdx = indexGraph.edgeTarget(eIdx);
		return viMap.indexToIdInt(vIdx);
	}

	@Override
	public int edgeEndpoint(int edge, int endpoint) {
		int eIdx = eiMap.idToIndex(edge);
		int endpointIdx = viMap.idToIndex(endpoint);
		int resIdx = indexGraph.edgeEndpoint(eIdx, endpointIdx);
		return viMap.indexToIdInt(resIdx);
	}

	@Override
	public void clear() {
		indexGraph.clear();
		viMap.idsClear();
		eiMap.idsClear();
	}

	@Override
	public void clearEdges() {
		indexGraph.clearEdges();
		eiMap.idsClear();
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
	public void ensureVertexCapacity(int vertexCapacity) {
		indexGraph.ensureVertexCapacity(vertexCapacity);
		viMap.ensureCapacity(vertexCapacity);
	}

	@Override
	public void ensureEdgeCapacity(int edgeCapacity) {
		indexGraph.ensureEdgeCapacity(edgeCapacity);
		eiMap.ensureCapacity(edgeCapacity);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T, WeightsT extends Weights<Integer, T>> WeightsT verticesWeights(String key) {
		WeightsImpl.Index<T> indexWeights = indexGraph.verticesWeights(key);
		if (indexWeights == null)
			return null;
		return (WeightsT) verticesWeights
				.computeIfAbsent(indexWeights, iw -> WeightsImpl.IntMapped.newInstance(iw, indexGraphVerticesMap()));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T, WeightsT extends Weights<Integer, T>> WeightsT edgesWeights(String key) {
		WeightsImpl.Index<T> indexWeights = indexGraph.edgesWeights(key);
		if (indexWeights == null)
			return null;
		return (WeightsT) edgesWeights
				.computeIfAbsent(indexWeights, iw -> WeightsImpl.IntMapped.newInstance(iw, indexGraphEdgesMap()));
	}

}
