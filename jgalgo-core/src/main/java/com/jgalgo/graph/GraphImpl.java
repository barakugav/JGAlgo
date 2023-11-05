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

import java.util.AbstractSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import com.jgalgo.graph.GraphElementSet.IdAddRemoveListener;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectSets;
import it.unimi.dsi.fastutil.objects.ReferenceArrayList;

abstract class GraphImpl<V, E> extends GraphBase<V, E> {

	final IndexGraphImpl indexGraph;
	final IdIdxMapImpl<V> viMap;
	final IdIdxMapImpl<E> eiMap;
	private final Map<WeightsImpl.Index<?>, WeightsImpl.ObjMapped<V, ?>> verticesWeights = new IdentityHashMap<>();
	private final Map<WeightsImpl.Index<?>, WeightsImpl.ObjMapped<E, ?>> edgesWeights = new IdentityHashMap<>();

	GraphImpl(IndexGraph g, int expectedVerticesNum, int expectedEdgesNum) {
		assert g.vertices().isEmpty();
		assert g.edges().isEmpty();

		indexGraph = (IndexGraphImpl) g;
		viMap = IdIdxMapImpl.newInstance(indexGraph.vertices(), expectedVerticesNum, false);
		eiMap = IdIdxMapImpl.newInstance(indexGraph.edges(), expectedEdgesNum, true);
	}

	GraphImpl(IndexGraph indexGraph, IndexIdMap<V> viMap, IndexIdMap<E> eiMap) {
		this.indexGraph = (IndexGraphImpl) Objects.requireNonNull(indexGraph);
		this.viMap = IdIdxMapImpl.copyOf(viMap, this.indexGraph.vertices(), false);
		this.eiMap = IdIdxMapImpl.copyOf(eiMap, this.indexGraph.edges(), true);
	}

	/* copy constructor */
	GraphImpl(Graph<V, E> orig, IndexGraphFactory indexGraphFactory, boolean copyWeights) {
		this(indexGraphFactory.newCopyOf(orig.indexGraph(), copyWeights), orig.indexGraphVerticesMap(),
				orig.indexGraphEdgesMap());
	}

	@Override
	public IndexGraph indexGraph() {
		return indexGraph;
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
			throw new IllegalArgumentException("User chosen vertex ID must be non null");
		if (vertices().contains(vertex))
			throw new IllegalArgumentException("Graph already contain a vertex with the specified ID: " + vertex);
		/* The listener of new IDs will be called by the index graph implementation, and the user ID will be used */
		viMap.userChosenId = vertex;
		indexGraph.addVertex();
	}

	@Override
	public void removeVertex(V vertex) {
		int vIdx = viMap.idToIndex(vertex);
		indexGraph.removeVertex(vIdx);
	}

	@Override
	public EdgeSet<V, E> outEdges(V source) {
		return new EdgeSetMapped(indexGraph.outEdges(viMap.idToIndex(source)));
	}

	@Override
	public EdgeSet<V, E> inEdges(V target) {
		return new EdgeSetMapped(indexGraph.inEdges(viMap.idToIndex(target)));
	}

	@Override
	public E getEdge(V source, V target) {
		int uIdx = viMap.idToIndex(source);
		int vIdx = viMap.idToIndex(target);
		int eIdx = indexGraph.getEdge(uIdx, vIdx);
		return eIdx == -1 ? null : eiMap.indexToId(eIdx);
	}

	@Override
	public EdgeSet<V, E> getEdges(V source, V target) {
		int uIdx = viMap.idToIndex(source);
		int vIdx = viMap.idToIndex(target);
		IEdgeSet s = indexGraph.getEdges(uIdx, vIdx);
		return new EdgeSetMapped(s);
	}

	// @Override
	// public int addEdge(int source, int target) {
	// int uIdx = viMap.idToIndex(source);
	// int vIdx = viMap.idToIndex(target);
	// int eIdx = indexGraph.addEdge(uIdx, vIdx);
	// return eiMap.indexToIdInt(eIdx);
	// }

	@Override
	public void addEdge(V source, V target, E edge) {
		if (edge == null)
			throw new IllegalArgumentException("User chosen edge ID must be non null");
		if (edges().contains(edge))
			throw new IllegalArgumentException("Graph already contain a edge with the specified ID: " + edge);
		int uIdx = viMap.idToIndex(source);
		int vIdx = viMap.idToIndex(target);
		/* The listener of new IDs will be called by the index graph implementation, and the user ID will be used */
		eiMap.userChosenId = edge;
		indexGraph.addEdge(uIdx, vIdx);
	}

	@Override
	public void removeEdge(E edge) {
		int eIdx = eiMap.idToIndex(edge);
		indexGraph.removeEdge(eIdx);
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
	}

	@Override
	public void clearEdges() {
		indexGraph.clearEdges();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T, WeightsT extends Weights<V, T>> WeightsT getVerticesWeights(String key) {
		proneRemovedVerticesWeights();
		WeightsImpl.Index<T> indexWeights = indexGraph.getVerticesWeights(key);
		if (indexWeights == null)
			return null;
		return (WeightsT) verticesWeights.computeIfAbsent(indexWeights,
				iw -> WeightsImpl.ObjMapped.newInstance(iw, indexGraphVerticesMap()));
	}

	@Override
	public Set<String> getVerticesWeightsKeys() {
		proneRemovedVerticesWeights();
		return indexGraph.getVerticesWeightsKeys();
	}

	@Override
	public void removeVerticesWeights(String key) {
		indexGraph.removeVerticesWeights(key);
		proneRemovedVerticesWeights();
	}

	private void proneRemovedVerticesWeights() {
		List<IWeights<?>> indexWeights = new ReferenceArrayList<>();
		for (String key : indexGraph.getVerticesWeightsKeys())
			indexWeights.add(indexGraph.getVerticesIWeights(key));
		verticesWeights.keySet().removeIf(w -> !indexWeights.contains(w));
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T, WeightsT extends Weights<E, T>> WeightsT getEdgesWeights(String key) {
		proneRemovedEdgesWeights();
		WeightsImpl.Index<T> indexWeights = indexGraph.getEdgesWeights(key);
		if (indexWeights == null)
			return null;
		return (WeightsT) edgesWeights.computeIfAbsent(indexWeights,
				iw -> WeightsImpl.ObjMapped.newInstance(iw, indexGraphEdgesMap()));
	}

	@Override
	public <T, WeightsT extends Weights<V, T>> WeightsT addVerticesWeights(String key, Class<? super T> type,
			T defVal) {
		proneRemovedVerticesWeights();
		indexGraph.addVerticesWeights(key, type, defVal);
		return getVerticesWeights(key);
	}

	@Override
	public <T, WeightsT extends Weights<E, T>> WeightsT addEdgesWeights(String key, Class<? super T> type, T defVal) {
		proneRemovedEdgesWeights();
		indexGraph.addEdgesWeights(key, type, defVal);
		return getEdgesWeights(key);
	}

	@Override
	public Set<String> getEdgesWeightsKeys() {
		proneRemovedEdgesWeights();
		return indexGraph.getEdgesWeightsKeys();
	}

	@Override
	public void removeEdgesWeights(String key) {
		indexGraph.removeEdgesWeights(key);
		proneRemovedEdgesWeights();
	}

	private void proneRemovedEdgesWeights() {
		List<IWeights<?>> indexWeights = new ReferenceArrayList<>();
		for (String key : indexGraph.getEdgesWeightsKeys())
			indexWeights.add(indexGraph.getEdgesIWeights(key));
		edgesWeights.keySet().removeIf(w -> !indexWeights.contains(w));
	}

	class EdgeSetMapped extends AbstractSet<E> implements EdgeSet<V, E> {

		private final IEdgeSet set;

		EdgeSetMapped(IEdgeSet set) {
			this.set = Objects.requireNonNull(set);
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean remove(Object edge) {
			int eIdx = eiMap.idToIndex((E) edge);
			return set.remove(eIdx);
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean contains(Object edge) {
			int eIdx = eiMap.idToIndex((E) edge);
			return set.contains(eIdx);
		}

		@Override
		public int size() {
			return set.size();
		}

		@Override
		public void clear() {
			set.clear();
		}

		@Override
		public EdgeIter<V, E> iterator() {
			return new EdgeIterMapped(set.iterator());
		}
	}

	class EdgeIterMapped implements EdgeIter<V, E> {

		private final IEdgeIter it;

		EdgeIterMapped(IEdgeIter it) {
			this.it = it;
		}

		@Override
		public boolean hasNext() {
			return it.hasNext();
		}

		@Override
		public E next() {
			int eIdx = it.nextInt();
			return eiMap.indexToId(eIdx);
		}

		@Override
		public E peekNext() {
			int eIdx = it.peekNextInt();
			return eiMap.indexToId(eIdx);
		}

		@Override
		public void remove() {
			it.remove();
		}

		@Override
		public V target() {
			int vIdx = it.targetInt();
			return viMap.indexToId(vIdx);
		}

		@Override
		public V source() {
			int uIdx = it.sourceInt();
			return viMap.indexToId(uIdx);
		}
	}

	@Override
	public boolean isDirected() {
		return indexGraph.isDirected();
	}

	@Override
	public boolean isAllowSelfEdges() {
		return indexGraph.isAllowSelfEdges();
	}

	@Override
	public boolean isAllowParallelEdges() {
		return indexGraph.isAllowParallelEdges();
	}

	static class Directed<V, E> extends GraphImpl<V, E> {

		Directed(IndexGraph indexGraph, int expectedVerticesNum, int expectedEdgesNum) {
			super(indexGraph, expectedVerticesNum, expectedEdgesNum);
			Assertions.Graphs.onlyDirected(indexGraph);
		}

		Directed(IndexGraph indexGraph, IndexIdMap<V> viMap, IndexIdMap<E> eiMap) {
			super(indexGraph, viMap, eiMap);
			Assertions.Graphs.onlyDirected(indexGraph);
		}

		/* copy constructor */
		Directed(Graph<V, E> orig, IndexGraphFactory indexGraphFactory, boolean copyWeights) {
			super(orig, indexGraphFactory, copyWeights);
			Assertions.Graphs.onlyDirected(orig);
			Assertions.Graphs.onlyDirected(indexGraph);
		}

		@Override
		public void reverseEdge(E edge) {
			int eIdx = eiMap.idToIndex(edge);
			indexGraph.reverseEdge(eIdx);
		}
	}

	static class Undirected<V, E> extends GraphImpl<V, E> {

		Undirected(IndexGraph indexGraph, IndexIdMap<V> viMap, IndexIdMap<E> eiMap) {
			super(indexGraph, viMap, eiMap);
			Assertions.Graphs.onlyUndirected(indexGraph);
		}

		Undirected(IndexGraph indexGraph, int expectedVerticesNum, int expectedEdgesNum) {
			super(indexGraph, expectedVerticesNum, expectedEdgesNum);
			Assertions.Graphs.onlyUndirected(indexGraph);
		}

		/* copy constructor */
		Undirected(Graph<V, E> orig, IndexGraphFactory indexGraphFactory, boolean copyWeights) {
			super(orig, indexGraphFactory, copyWeights);
			Assertions.Graphs.onlyUndirected(orig);
			Assertions.Graphs.onlyUndirected(indexGraph);
		}

		@Override
		public void reverseEdge(E edge) {
			int eIdx = eiMap.idToIndex(edge);
			indexGraph.reverseEdge(eIdx);
		}
	}

	private static class IdIdxMapImpl<K> implements IndexIdMap<K> {

		private final Object2IntOpenHashMap<K> idToIndex;
		private final Set<K> idsView; // TODO move to graph abstract implementation
		private final WeightsImplObj.IndexImpl<K> indexToId;
		private K userChosenId = null;
		private final boolean isEdges;

		IdIdxMapImpl(GraphElementSet elements, int expectedSize, boolean isEdges) {
			idToIndex = new Object2IntOpenHashMap<>(expectedSize);
			idToIndex.defaultReturnValue(-1);
			idsView = ObjectSets.unmodifiable(idToIndex.keySet());
			indexToId = new WeightsImplObj.IndexMutable<>(elements, null);
			this.isEdges = isEdges;
			initListeners(elements);
		}

		IdIdxMapImpl(IndexIdMap<K> orig, GraphElementSet elements, boolean isEdges) {
			if (orig instanceof IdIdxMapImpl) {
				IdIdxMapImpl<K> orig0 = (IdIdxMapImpl<K>) orig;
				idToIndex = new Object2IntOpenHashMap<>(orig0.idToIndex);
				idToIndex.defaultReturnValue(-1);
				indexToId = new WeightsImplObj.IndexMutable<>(orig0.indexToId, elements);
			} else {
				idToIndex = new Object2IntOpenHashMap<>(elements.size());
				idToIndex.defaultReturnValue(-1);
				indexToId = new WeightsImplObj.IndexMutable<>(elements, null);
				if (elements.size() > 0) {
					((WeightsImplObj.IndexMutable<K>) indexToId).expand(elements.size());
					for (int idx : elements) {
						K id = orig.indexToId(idx);
						if (id == null)
							throw new IllegalArgumentException("null id");
						if (indexToId.get(idx) != null)
							throw new IllegalArgumentException("duplicate index: " + idx);
						indexToId.set(idx, id);

						int oldIdx = idToIndex.put(id, idx);
						if (oldIdx != -1)
							throw new IllegalArgumentException("duplicate id: " + id);
					}
				}
			}
			this.isEdges = isEdges;
			idsView = ObjectSets.unmodifiable(idToIndex.keySet());
			initListeners(elements);
		}

		static <K> IdIdxMapImpl<K> newInstance(GraphElementSet elements, int expectedSize, boolean isEdges) {
			return new IdIdxMapImpl<>(elements, expectedSize, isEdges);
		}

		static <K> IdIdxMapImpl<K> copyOf(IndexIdMap<K> orig, GraphElementSet elements, boolean isEdges) {
			return new IdIdxMapImpl<>(orig, elements, isEdges);
		}

		private void initListeners(GraphElementSet elements) {
			elements.addIdSwapListener((idx1, idx2) -> {
				K id1 = indexToId.get(idx1);
				K id2 = indexToId.get(idx2);
				indexToId.set(idx1, id2);
				indexToId.set(idx2, id1);
				int oldIdx1 = idToIndex.put(id1, idx2);
				int oldIdx2 = idToIndex.put(id2, idx1);
				assert idx1 == oldIdx1;
				assert idx2 == oldIdx2;
			});
			elements.addIdAddRemoveListener(new IdAddRemoveListener() {

				WeightsImplObj.IndexMutable<K> indexToId() {
					return (WeightsImplObj.IndexMutable<K>) indexToId;
				}

				@Override
				public void idRemove(int idx) {
					final K id = indexToId.get(idx);
					indexToId().clear(idx);
					idToIndex.removeInt(id);
				}

				@Override
				public void idAdd(int idx) {
					assert idx == idToIndex.size();
					if (userChosenId == null)
						throw new IllegalStateException(
								"can't add vertex/edge from index graph, use wrapper graph API");
					K id = userChosenId;
					int oldIdx = idToIndex.put(id, idx);
					assert oldIdx == -1;

					if (idx == indexToId().capacity())
						indexToId().expand(Math.max(2, 2 * indexToId().capacity()));
					indexToId.set(idx, id);

					userChosenId = null;
				}

				@Override
				public void idsClear() {
					idToIndex.clear();
					indexToId().clear();
				}
			});
		}

		@Override
		public K indexToId(int index) {
			return indexToId.get(index);
		}

		@Override
		public int idToIndex(K id) {
			int idx = idToIndex.getInt(id);
			if (idx < 0)
				throw new IndexOutOfBoundsException("No such " + (isEdges ? "edge" : "vertex") + ": " + id);
			return idx;
		}

		Set<K> idSet() {
			return idsView;
		}

	}

	static class Factory<V, E> implements GraphFactory<V, E> {
		private final IndexGraphFactoryImpl factory;

		Factory(boolean directed) {
			this.factory = new IndexGraphFactoryImpl(directed);
		}

		Factory(Graph<V, E> g) {
			this.factory = new IndexGraphFactoryImpl(g.indexGraph());
		}

		@Override
		public Graph<V, E> newGraph() {
			IndexGraph indexGraph = factory.newGraph();
			if (indexGraph.isDirected()) {
				return new GraphImpl.Directed<>(indexGraph, factory.expectedVerticesNum, factory.expectedEdgesNum);
			} else {
				return new GraphImpl.Undirected<>(indexGraph, factory.expectedVerticesNum, factory.expectedEdgesNum);
			}
		}

		@Override
		public Graph<V, E> newCopyOf(Graph<V, E> g, boolean copyWeights) {
			if (g.isDirected()) {
				return new GraphImpl.Directed<>(g, factory, copyWeights);
			} else {
				return new GraphImpl.Undirected<>(g, factory, copyWeights);
			}
		}

		@Override
		public GraphBuilder<V, E> newBuilder() {
			IndexGraphBuilder indexBuilder = factory.newBuilder();
			return factory.directed ? new GraphBuilderImpl.Directed<>()
					: new GraphBuilderImpl.Undirected<>(indexBuilder);
		}

		@Override
		public GraphFactory<V, E> setDirected(boolean directed) {
			factory.setDirected(directed);
			return this;
		}

		@Override
		public GraphFactory<V, E> allowSelfEdges(boolean selfEdges) {
			factory.allowSelfEdges(selfEdges);
			return this;
		}

		@Override
		public GraphFactory<V, E> allowParallelEdges(boolean parallelEdges) {
			factory.allowParallelEdges(parallelEdges);
			return this;
		}

		@Override
		public GraphFactory<V, E> expectedVerticesNum(int expectedVerticesNum) {
			factory.expectedVerticesNum(expectedVerticesNum);
			return this;
		}

		@Override
		public GraphFactory<V, E> expectedEdgesNum(int expectedEdgesNum) {
			factory.expectedEdgesNum(expectedEdgesNum);
			return this;
		}

		@Override
		public GraphFactory<V, E> addHint(GraphFactory.Hint hint) {
			factory.addHint(hint);
			return this;
		}

		@Override
		public GraphFactory<V, E> removeHint(GraphFactory.Hint hint) {
			factory.removeHint(hint);
			return this;
		}

		@Override
		public GraphFactory<V, E> setOption(String key, Object value) {
			factory.setOption(key, value);
			return this;
		}
	}

}
