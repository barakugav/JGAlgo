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
import com.jgalgo.graph.Graphs.ImmutableGraph;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntCollection;

final class GraphImpl<V, E> extends AbstractGraphImpl<V, E> {

	final IndexIdMapImpl<V> viMap;
	final IndexIdMapImpl<E> eiMap;
	private final Map<WeightsImpl.Index<?>, WeightsImpl.ObjMapped<V, ?>> verticesWeights = new IdentityHashMap<>();
	private final Map<WeightsImpl.Index<?>, WeightsImpl.ObjMapped<E, ?>> edgesWeights = new IdentityHashMap<>();
	private final IdBuilder<V> vertexBuilder;
	private final IdBuilder<E> edgeBuilder;

	GraphImpl(GraphFactoryImpl<V, E> factory) {
		super(factory.indexFactory.newGraph());
		viMap = IndexIdMapImpl.newEmpty(indexGraph.vertices(), true, factory.indexFactory.expectedVerticesNum);
		eiMap = IndexIdMapImpl.newEmpty(indexGraph.edges(), false, factory.indexFactory.expectedEdgesNum);
		viMap.initListeners(indexGraph);
		eiMap.initListeners(indexGraph);
		vertexBuilder = factory.vertexFactory != null ? factory.vertexFactory.get() : null;
		edgeBuilder = factory.edgeFactory != null ? factory.edgeFactory.get() : null;
	}

	GraphImpl(GraphFactoryImpl<V, E> factory, IndexGraph indexGraph, IndexIdMap<V> viMap, IndexIdMap<E> eiMap,
			Optional<IndexGraphBuilder.ReIndexingMap> vReIndexing,
			Optional<IndexGraphBuilder.ReIndexingMap> eReIndexing) {
		super(indexGraph);
		boolean immutable = this.indexGraph instanceof ImmutableGraph;
		this.viMap = IndexIdMapImpl.newCopyOf(viMap, vReIndexing, this.indexGraph.vertices(), true, immutable);
		this.eiMap = IndexIdMapImpl.newCopyOf(eiMap, eReIndexing, this.indexGraph.edges(), false, immutable);
		if (!immutable) {
			this.viMap.initListeners(this.indexGraph);
			this.eiMap.initListeners(this.indexGraph);
		}
		vertexBuilder = factory.vertexFactory != null ? factory.vertexFactory.get() : null;
		edgeBuilder = factory.edgeFactory != null ? factory.edgeFactory.get() : null;
	}

	/* If builder.isNewGraphShouldStealInterior(), the graph steal the vertices and edges maps of the builder */
	GraphImpl(GraphBuilderImpl<V, E> builder, IndexGraph indexGraph,
			Optional<IndexGraphBuilder.ReIndexingMap> vReIndexing,
			Optional<IndexGraphBuilder.ReIndexingMap> eReIndexing) {
		super(indexGraph);
		boolean immutable = this.indexGraph instanceof ImmutableGraph;

		if (builder.isNewGraphShouldStealInterior()) {
			viMap = builder.stealViMap().intoImmutable(vReIndexing);
			eiMap = builder.stealEiMap().intoImmutable(eReIndexing);

		} else {
			viMap = IndexIdMapImpl.newCopyOf(builder.viMap, vReIndexing, this.indexGraph.vertices(), true, immutable);
			eiMap = IndexIdMapImpl.newCopyOf(builder.eiMap, eReIndexing, this.indexGraph.edges(), false, immutable);
		}
		if (!immutable) {
			viMap.initListeners(this.indexGraph);
			eiMap.initListeners(this.indexGraph);
		}

		GraphFactoryImpl<V, E> factory = builder.factory;
		vertexBuilder = factory.vertexFactory != null ? factory.vertexFactory.get() : null;
		edgeBuilder = factory.edgeFactory != null ? factory.edgeFactory.get() : null;
	}

	@Override
	public IndexIdMap<V> indexGraphVerticesMap() {
		return viMap;
	}

	@Override
	public IndexIdMap<E> indexGraphEdgesMap() {
		return eiMap;
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
		int vIdx = indexGraph.vertices().size();
		viMap.addId(vertex, vIdx);
		int vIdx2 = indexGraph.addVertexInt();
		assert vIdx == vIdx2;
	}

	@Override
	public void addVertices(Collection<? extends V> vertices) {
		for (V vertex : vertices)
			if (vertex == null)
				throw new NullPointerException("Vertex must be non null");

		final int verticesNumBefore = indexGraph.vertices().size();
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
		indexGraph.addVertices(range(verticesNumBefore, nextIdx));
	}

	@Override
	public void removeVertex(V vertex) {
		int vIdx = viMap.idToIndex(vertex);
		indexGraph.removeVertex(vIdx);
	}

	@Override
	public void removeVertices(Collection<? extends V> vertices) {
		@SuppressWarnings("unchecked")
		IntCollection vIdxs = IndexIdMaps.idToIndexCollection((Collection<V>) vertices, viMap);
		indexGraph.removeVertices(vIdxs);
	}

	@Override
	public void renameVertex(V vertex, V newId) {
		if (newId == null)
			throw new NullPointerException("Vertex must be non null");
		viMap.renameId(vertex, newId);
	}

	@Override
	public EdgeSet<V, E> outEdges(V source) {
		int uIdx = viMap.idToIndex(source);
		IEdgeSet indexSet = indexGraph.outEdges(uIdx);
		return IndexIdMaps.indexToIdEdgeSet(indexSet, this);
	}

	@Override
	public EdgeSet<V, E> inEdges(V target) {
		int vIdx = viMap.idToIndex(target);
		IEdgeSet indexSet = indexGraph.inEdges(vIdx);
		return IndexIdMaps.indexToIdEdgeSet(indexSet, this);
	}

	@Override
	public E getEdge(V source, V target) {
		int uIdx = viMap.idToIndex(source);
		int vIdx = viMap.idToIndex(target);
		int eIdx = indexGraph.getEdge(uIdx, vIdx);
		return eiMap.indexToIdIfExist(eIdx);
	}

	@Override
	public EdgeSet<V, E> getEdges(V source, V target) {
		int uIdx = viMap.idToIndex(source);
		int vIdx = viMap.idToIndex(target);
		IEdgeSet indexSet = indexGraph.getEdges(uIdx, vIdx);
		return IndexIdMaps.indexToIdEdgeSet(indexSet, this);
	}

	@Override
	public void addEdge(V source, V target, E edge) {
		if (edge == null)
			throw new NullPointerException("Edge must be non null");

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
	public void addEdges(EdgeSet<? extends V, ? extends E> edges) {
		final int edgesNumBefore = indexGraph.edges().size();
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

			indexGraph.addEdgesReassignIds(new AddEdgesIgnoreIdsIndexSet<>(edges, viMap));

		} catch (RuntimeException e) {
			for (; nextMapIdx-- > edgesNumBefore;)
				eiMap.rollBackRemove(nextMapIdx);
			throw e;
		}
	}

	static class AddEdgesIgnoreIdsIndexSet<V> extends AbstractIntSet implements IEdgeSet {

		private final EdgeSet<? extends V, ?> idSet;
		private final IndexIdMap<V> viMap;

		AddEdgesIgnoreIdsIndexSet(EdgeSet<? extends V, ?> idSet, IndexIdMap<V> viMap) {
			this.idSet = idSet;
			this.viMap = viMap;
		}

		@Override
		public int size() {
			return idSet.size();
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIters.IBase() {
				private final EdgeIter<? extends V, ?> idIter = idSet.iterator();

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
					return viMap.idToIndex(idIter.source());
				}

				@Override
				public int targetInt() {
					return viMap.idToIndex(idIter.target());
				}
			};
		}
	}

	@Override
	public void removeEdge(E edge) {
		int eIdx = eiMap.idToIndex(edge);
		indexGraph.removeEdge(eIdx);
	}

	@Override
	public void removeEdges(Collection<? extends E> edges) {
		@SuppressWarnings("unchecked")
		IntCollection eIdxs = IndexIdMaps.idToIndexCollection((Collection<E>) edges, eiMap);
		indexGraph.removeEdges(eIdxs);
	}

	@Override
	public void removeEdgesOf(V source) {
		int uIdx = viMap.idToIndex(source);
		indexGraph.removeEdgesOf(uIdx);
	}

	@Override
	public void removeOutEdgesOf(V source) {
		indexGraph.removeOutEdgesOf(viMap.idToIndex(source));
	}

	@Override
	public void removeInEdgesOf(V target) {
		indexGraph.removeInEdgesOf(viMap.idToIndex(target));
	}

	@Override
	public void renameEdge(E edge, E newId) {
		if (newId == null)
			throw new NullPointerException("Edge must be non null");
		eiMap.renameId(edge, newId);
	}

	@Override
	public void moveEdge(E edge, V newSource, V newTarget) {
		indexGraph.moveEdge(eiMap.idToIndex(edge), viMap.idToIndex(newSource), viMap.idToIndex(newTarget));
	}

	@Override
	public V edgeSource(E edge) {
		int eIdx = eiMap.idToIndex(edge);
		int uIdx = indexGraph.edgeSource(eIdx);
		return viMap.indexToId(uIdx);
	}

	@Override
	public V edgeTarget(E edge) {
		int eIdx = eiMap.idToIndex(edge);
		int vIdx = indexGraph.edgeTarget(eIdx);
		return viMap.indexToId(vIdx);
	}

	@Override
	public V edgeEndpoint(E edge, V endpoint) {
		int eIdx = eiMap.idToIndex(edge);
		int endpointIdx = viMap.idToIndex(endpoint);
		int resIdx = indexGraph.edgeEndpoint(eIdx, endpointIdx);
		return viMap.indexToId(resIdx);
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
	public IdBuilder<V> vertexBuilder() {
		return vertexBuilder;
	}

	@Override
	public IdBuilder<E> edgeBuilder() {
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
	public <T, WeightsT extends Weights<V, T>> WeightsT verticesWeights(String key) {
		WeightsImpl.Index<T> indexWeights = indexGraph.verticesWeights(key);
		if (indexWeights == null)
			return null;
		return (WeightsT) verticesWeights
				.computeIfAbsent(indexWeights, iw -> WeightsImpl.ObjMapped.newInstance(iw, indexGraphVerticesMap()));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T, WeightsT extends Weights<E, T>> WeightsT edgesWeights(String key) {
		WeightsImpl.Index<T> indexWeights = indexGraph.edgesWeights(key);
		if (indexWeights == null)
			return null;
		return (WeightsT) edgesWeights
				.computeIfAbsent(indexWeights, iw -> WeightsImpl.ObjMapped.newInstance(iw, indexGraphEdgesMap()));
	}

}
