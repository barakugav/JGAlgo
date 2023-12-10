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
import java.util.Arrays;
import java.util.Collection;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import com.jgalgo.graph.Graphs.ImmutableGraph;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterators;

class GraphImpl<V, E> extends GraphBase<V, E> {

	final IndexGraph indexGraph;
	final IdIdxMapImpl<V> viMap;
	final IdIdxMapImpl<E> eiMap;
	private final Map<WeightsImpl.Index<?>, WeightsImpl.ObjMapped<V, ?>> verticesWeights = new IdentityHashMap<>();
	private final Map<WeightsImpl.Index<?>, WeightsImpl.ObjMapped<E, ?>> edgesWeights = new IdentityHashMap<>();

	GraphImpl(IndexGraph g, int expectedVerticesNum, int expectedEdgesNum) {
		assert g.vertices().isEmpty();
		assert g.edges().isEmpty();

		indexGraph = g;
		viMap = IdIdxMapImpl.newInstance(indexGraph, expectedVerticesNum, false);
		eiMap = IdIdxMapImpl.newInstance(indexGraph, expectedEdgesNum, true);
	}

	GraphImpl(IndexGraph indexGraph, IndexIdMap<V> viMap, IndexIdMap<E> eiMap,
			IndexGraphBuilder.ReIndexingMap vReIndexing, IndexGraphBuilder.ReIndexingMap eReIndexing) {
		this.indexGraph = Objects.requireNonNull(indexGraph);
		this.viMap = IdIdxMapImpl.reindexedCopyOf(viMap, vReIndexing, this.indexGraph, false);
		this.eiMap = IdIdxMapImpl.reindexedCopyOf(eiMap, eReIndexing, this.indexGraph, true);
	}

	/* copy constructor */
	GraphImpl(Graph<V, E> orig, IndexGraphFactory indexGraphFactory, boolean copyVerticesWeights,
			boolean copyEdgesWeights) {
		this(indexGraphFactory.newCopyOf(orig.indexGraph(), copyVerticesWeights, copyEdgesWeights),
				orig.indexGraphVerticesMap(), orig.indexGraphEdgesMap(), null, null);
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
			throw new IllegalArgumentException("Vertex must be non null");
		int vIdx = indexGraph.vertices().size();
		viMap.addId(vertex, vIdx);
		int vIdx2 = indexGraph.addVertex();
		assert vIdx == vIdx2;
	}

	@Override
	public void removeVertex(V vertex) {
		int vIdx = viMap.idToIndex(vertex);
		indexGraph.removeVertex(vIdx);
	}

	@Override
	public void renameVertex(V vertex, V newId) {
		if (newId == null)
			throw new IllegalArgumentException("Vertex must be non null");
		viMap.renameId(vertex, newId);
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

	@Override
	public void addEdge(V source, V target, E edge) {
		if (edge == null)
			throw new IllegalArgumentException("Edge must be non null");

		int eIdx = indexGraph.edges().size();
		eiMap.addId(edge, eIdx);

		int uIdx = viMap.idToIndex(source);
		int vIdx = viMap.idToIndex(target);
		try {
			int eIdx2 = indexGraph.addEdge(uIdx, vIdx);
			assert eIdx == eIdx2;
		} catch (RuntimeException e) {
			eiMap.rollBackRemove(eIdx);
			throw e;
		}
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
	public void renameEdge(E edge, E newId) {
		if (newId == null)
			throw new IllegalArgumentException("Edge must be non null");
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
	@SuppressWarnings("unchecked")
	public <T, WeightsT extends Weights<V, T>> WeightsT getVerticesWeights(String key) {
		WeightsImpl.Index<T> indexWeights = indexGraph.getVerticesWeights(key);
		if (indexWeights == null)
			return null;
		return (WeightsT) verticesWeights.computeIfAbsent(indexWeights,
				iw -> WeightsImpl.ObjMapped.newInstance(iw, indexGraphVerticesMap()));
	}

	@Override
	public Set<String> getVerticesWeightsKeys() {
		return indexGraph.getVerticesWeightsKeys();
	}

	@Override
	public void removeVerticesWeights(String key) {
		indexGraph.removeVerticesWeights(key);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T, WeightsT extends Weights<E, T>> WeightsT getEdgesWeights(String key) {
		WeightsImpl.Index<T> indexWeights = indexGraph.getEdgesWeights(key);
		if (indexWeights == null)
			return null;
		return (WeightsT) edgesWeights.computeIfAbsent(indexWeights,
				iw -> WeightsImpl.ObjMapped.newInstance(iw, indexGraphEdgesMap()));
	}

	@Override
	public <T, WeightsT extends Weights<V, T>> WeightsT addVerticesWeights(String key, Class<? super T> type,
			T defVal) {
		indexGraph.addVerticesWeights(key, type, defVal);
		return getVerticesWeights(key);
	}

	@Override
	public <T, WeightsT extends Weights<E, T>> WeightsT addEdgesWeights(String key, Class<? super T> type, T defVal) {
		indexGraph.addEdgesWeights(key, type, defVal);
		return getEdgesWeights(key);
	}

	@Override
	public Set<String> getEdgesWeightsKeys() {
		return indexGraph.getEdgesWeightsKeys();
	}

	@Override
	public void removeEdgesWeights(String key) {
		indexGraph.removeEdgesWeights(key);
	}

	class EdgeSetMapped extends AbstractSet<E> implements EdgeSet<V, E> {

		private final IEdgeSet set;

		EdgeSetMapped(IEdgeSet set) {
			this.set = Objects.requireNonNull(set);
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean remove(Object edge) {
			return set.remove(eiMap.idToIndexIfExist((E) edge));
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean contains(Object edge) {
			return set.contains(eiMap.idToIndexIfExist((E) edge));
		}

		@Override
		public int size() {
			return set.size();
		}

		@Override
		public boolean isEmpty() {
			return set.isEmpty();
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

	private static class IdIdxMapImpl<K> implements IndexIdMap<K> {

		private final IntSet indicesSet;
		private final Set<K> idsSet = new IdSet();
		private final Object2IntOpenHashMap<K> idToIndex;
		private Object[] indexToId;
		private final boolean isEdges;
		private final boolean immutable;

		IdIdxMapImpl(IndexGraph g, int expectedSize, boolean isEdges) {
			this.indicesSet = isEdges ? g.edges() : g.vertices();
			idToIndex = new Object2IntOpenHashMap<>(expectedSize);
			idToIndex.defaultReturnValue(-1);
			indexToId = expectedSize == 0 ? ObjectArrays.DEFAULT_EMPTY_ARRAY : new Object[expectedSize];
			this.isEdges = isEdges;
			immutable = false;
			initListeners(g);
		}

		IdIdxMapImpl(IndexIdMap<K> orig, IndexGraphBuilder.ReIndexingMap reIndexing, IndexGraph g, boolean isEdges) {
			this.indicesSet = isEdges ? g.edges() : g.vertices();
			int elementsSize = indicesSet.size();
			if (orig instanceof IdIdxMapImpl && reIndexing == null) {
				IdIdxMapImpl<K> orig0 = (IdIdxMapImpl<K>) orig;
				idToIndex = new Object2IntOpenHashMap<>(orig0.idToIndex);
				idToIndex.defaultReturnValue(-1);
				indexToId = Arrays.copyOf(orig0.indexToId, elementsSize);

			} else {
				idToIndex = new Object2IntOpenHashMap<>(elementsSize);
				idToIndex.defaultReturnValue(-1);
				if (indicesSet.isEmpty()) {
					indexToId = ObjectArrays.DEFAULT_EMPTY_ARRAY;
				} else {
					indexToId = new Object[elementsSize];
					if (reIndexing == null) {
						for (int idx : indicesSet) {
							K id = orig.indexToId(idx);
							if (id == null)
								throw new IllegalArgumentException("null id");
							indexToId[idx] = id;

							int oldIdx = idToIndex.put(id, idx);
							if (oldIdx != -1)
								throw new IllegalArgumentException("duplicate id: " + id);
						}

					} else {
						for (int idx : indicesSet) {
							K id = orig.indexToId(reIndexing.reIndexedToOrig(idx));
							if (id == null)
								throw new IllegalArgumentException("null id");
							indexToId[idx] = id;

							int oldIdx = idToIndex.put(id, idx);
							if (oldIdx != -1)
								throw new IllegalArgumentException("duplicate id: " + id);
						}
					}
				}
			}
			this.isEdges = isEdges;
			immutable = g instanceof ImmutableGraph;
			initListeners(g);
		}

		static <K> IdIdxMapImpl<K> newInstance(IndexGraph g, int expectedSize, boolean isEdges) {
			return new IdIdxMapImpl<>(g, expectedSize, isEdges);
		}

		static <K> IdIdxMapImpl<K> reindexedCopyOf(IndexIdMap<K> orig, IndexGraphBuilder.ReIndexingMap reIndexing,
				IndexGraph g, boolean isEdges) {
			return new IdIdxMapImpl<>(orig, reIndexing, g, isEdges);
		}

		void addId(K id, int idx) {
			assert idx == idToIndex.size();
			int oldIdx = idToIndex.putIfAbsent(id, idx);
			if (oldIdx != -1) {
				if (isEdges) {
					throw new IllegalArgumentException("Graph already contain such an edge: " + id);
				} else {
					throw new IllegalArgumentException("Graph already contain such a vertex: " + id);
				}
			}

			if (idx == indexToId.length)
				indexToId = Arrays.copyOf(indexToId, Math.max(2, 2 * indexToId.length));
			indexToId[idx] = id;
		}

		void rollBackRemove(int index) {
			assert index == idToIndex.size() - 1;
			removeLast(index);
		}

		private void swapAndRemove(int removedIdx, int swappedIdx) {
			@SuppressWarnings("unchecked")
			K id1 = (K) indexToId[removedIdx];
			@SuppressWarnings("unchecked")
			K id2 = (K) indexToId[swappedIdx];
			indexToId[removedIdx] = id2;
			indexToId[swappedIdx] = null;
			int oldIdx1 = idToIndex.removeInt(id1);
			int oldIdx2 = idToIndex.put(id2, removedIdx);
			assert removedIdx == oldIdx1;
			assert swappedIdx == oldIdx2;
		}

		private void removeLast(int removedIdx) {
			Object id = indexToId[removedIdx];
			indexToId[removedIdx] = null;
			idToIndex.removeInt(id);
		}

		private void initListeners(IndexGraph g) {
			IndexRemoveListener listener = new IndexRemoveListener() {

				@Override
				public void swapAndRemove(int removedIdx, int swappedIdx) {
					IdIdxMapImpl.this.swapAndRemove(removedIdx, swappedIdx);
				}

				@Override
				public void removeLast(int removedIdx) {
					IdIdxMapImpl.this.removeLast(removedIdx);
				}
			};
			if (isEdges) {
				g.addEdgeRemoveListener(listener);
			} else {
				g.addVertexRemoveListener(listener);
			}
		}

		void idsClear() {
			Arrays.fill(indexToId, 0, idToIndex.size(), null);
			idToIndex.clear();
		}

		@SuppressWarnings("unchecked")
		@Override
		public K indexToId(int index) {
			Assertions.Graphs.checkId(index, indicesSet.size(), isEdges);
			return (K) indexToId[index];
		}

		@SuppressWarnings("unchecked")
		@Override
		public K indexToIdIfExist(int index) {
			if (!(0 <= index && index < indicesSet.size()))
				return null;
			return (K) indexToId[index];
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

		void renameId(K oldId, K newId) {
			if (immutable) {
				if (isEdges) {
					throw new UnsupportedOperationException("graph is immutable, cannot rename vertices");
				} else {
					throw new UnsupportedOperationException("graph is immutable, cannot rename edges");
				}
			}
			int idx = idToIndex.removeInt(oldId);
			if (idx < 0) {
				if (isEdges) {
					throw NoSuchEdgeException.ofEdge(oldId);
				} else {
					throw NoSuchVertexException.ofVertex(oldId);
				}
			}
			int oldIdx = idToIndex.putIfAbsent(newId, idx);
			if (oldIdx != -1) {
				idToIndex.put(oldId, idx); /* roll back */
				if (isEdges) {
					throw new IllegalArgumentException("Graph already contain such an edge: " + newId);
				} else {
					throw new IllegalArgumentException("Graph already contain such a vertex: " + newId);
				}
			}
			indexToId[idx] = newId;
		}

		Set<K> idSet() {
			return idsSet;
		}

		private class IdSet extends AbstractObjectSet<K> {

			@Override
			public int size() {
				return indicesSet.size();
			}

			@Override
			public boolean contains(Object o) {
				return idToIndex.containsKey(o);
			}

			@Override
			public boolean containsAll(Collection<?> c) {
				return idToIndex.keySet().containsAll(c);
			}

			@SuppressWarnings("unchecked")
			@Override
			public ObjectIterator<K> iterator() {
				return ObjectIterators.wrap((K[]) indexToId, 0, indicesSet.size());
			}

			@Override
			public Object[] toArray() {
				return Arrays.copyOf(indexToId, indicesSet.size());
			}

			@Override
			public <T> T[] toArray(T[] a) {
				int size = indicesSet.size();
				if (a.length < size)
					a = java.util.Arrays.copyOf(a, size);
				System.arraycopy(indexToId, 0, a, 0, size);
				if (a.length > size)
					a[size] = null;
				return a;
			}
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
			return new GraphImpl<>(indexGraph, factory.expectedVerticesNum, factory.expectedEdgesNum);
		}

		@Override
		public Graph<V, E> newCopyOf(Graph<V, E> g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
			return new GraphImpl<>(g, factory, copyVerticesWeights, copyEdgesWeights);
		}

		@Override
		public GraphBuilder<V, E> newBuilder() {
			return new GraphBuilderImpl<>(factory.newBuilder());
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
