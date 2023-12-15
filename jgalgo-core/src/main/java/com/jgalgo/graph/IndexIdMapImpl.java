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

import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterators;

class IndexIdMapImpl<K> implements IndexIdMap<K> {

	private final IntSet indicesSet;
	private final Set<K> idsSet = new IdSet();
	private final Object2IntOpenHashMap<K> idToIndex;
	private Object[] indexToId;
	private final boolean isEdges;
	private final boolean immutable;

	private IndexIdMapImpl(IntSet indicesSet, boolean isEdges, int expectedSize) {
		assert indicesSet.isEmpty();
		this.indicesSet = indicesSet;
		idToIndex = new Object2IntOpenHashMap<>(expectedSize);
		idToIndex.defaultReturnValue(-1);
		indexToId = expectedSize == 0 ? ObjectArrays.DEFAULT_EMPTY_ARRAY : new Object[expectedSize];
		this.isEdges = isEdges;
		immutable = false;
	}

	private IndexIdMapImpl(IndexIdMap<K> orig, IndexGraphBuilder.ReIndexingMap reIndexing, IntSet indicesSet,
			boolean isEdges, boolean immutable) {
		this.indicesSet = indicesSet;
		int elementsSize = this.indicesSet.size();
		if (orig instanceof IndexIdMapImpl && reIndexing == null) {
			IndexIdMapImpl<K> orig0 = (IndexIdMapImpl<K>) orig;
			idToIndex = new Object2IntOpenHashMap<>(orig0.idToIndex);
			idToIndex.defaultReturnValue(-1);
			indexToId = Arrays.copyOf(orig0.indexToId, elementsSize);

		} else {
			idToIndex = new Object2IntOpenHashMap<>(elementsSize);
			idToIndex.defaultReturnValue(-1);
			if (this.indicesSet.isEmpty()) {
				indexToId = ObjectArrays.DEFAULT_EMPTY_ARRAY;
			} else {
				indexToId = new Object[elementsSize];
				if (reIndexing == null) {
					for (int idx : this.indicesSet) {
						K id = orig.indexToId(idx);
						if (id == null)
							throw new NullPointerException("null id");
						indexToId[idx] = id;

						int oldIdx = idToIndex.put(id, idx);
						if (oldIdx != -1)
							throw new IllegalArgumentException("duplicate id: " + id);
					}

				} else {
					for (int idx : this.indicesSet) {
						K id = orig.indexToId(reIndexing.reIndexedToOrig(idx));
						if (id == null)
							throw new NullPointerException("null id");
						indexToId[idx] = id;

						int oldIdx = idToIndex.put(id, idx);
						if (oldIdx != -1)
							throw new IllegalArgumentException("duplicate id: " + id);
					}
				}
			}
		}
		this.isEdges = isEdges;
		this.immutable = immutable;
	}

	static <K> IndexIdMapImpl<K> newEmpty(IntSet indicesSet, boolean isEdges, int expectedSize) {
		return new IndexIdMapImpl<>(indicesSet, isEdges, expectedSize);
	}

	static <K> IndexIdMapImpl<K> newCopyOf(IndexIdMap<K> orig, IndexGraphBuilder.ReIndexingMap reIndexing,
			IntSet indicesSet, boolean isEdges, boolean immutable) {
		return new IndexIdMapImpl<>(orig, reIndexing, indicesSet, isEdges, immutable);
	}

	void addId(K id, int idx) {
		boolean added = addIdIfNotDuplicate(id, idx);
		if (!added) {
			if (isEdges) {
				throw new IllegalArgumentException("Graph already contain such an edge: " + id);
			} else {
				throw new IllegalArgumentException("Graph already contain such a vertex: " + id);
			}
		}
	}

	boolean addIdIfNotDuplicate(K id, int idx) {
		assert idx == idToIndex.size();
		int oldIdx = idToIndex.putIfAbsent(id, idx);
		if (oldIdx != -1)
			return false;

		if (idx == indexToId.length)
			indexToId = Arrays.copyOf(indexToId, Math.max(2, 2 * indexToId.length));
		indexToId[idx] = id;
		return true;
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

	void initListeners(IndexGraph g) {
		IndexRemoveListener listener = new IndexRemoveListener() {

			@Override
			public void swapAndRemove(int removedIdx, int swappedIdx) {
				IndexIdMapImpl.this.swapAndRemove(removedIdx, swappedIdx);
			}

			@Override
			public void removeLast(int removedIdx) {
				IndexIdMapImpl.this.removeLast(removedIdx);
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
			Objects.requireNonNull(id);
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
		return idToIndex.getInt(Objects.requireNonNull(id));
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

	void ensureCapacity(int capacity) {
		idToIndex.ensureCapacity(capacity);
		if (capacity > indexToId.length)
			indexToId = Arrays.copyOf(indexToId, Math.max(2, Math.max(2 * indexToId.length, capacity)));
	}

}
