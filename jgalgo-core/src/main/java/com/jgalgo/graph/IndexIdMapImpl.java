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
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.AbstractObjectSet;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.objects.ObjectIterators;

class IndexIdMapImpl<K> implements IndexIdMap<K> {

	private final IntSet indicesSet;
	private final Set<K> idsSet = new IdSet();
	private final Object2IntOpenHashMap<K> idToIndex;
	private Object[] indexToId;
	private final boolean isVertices;
	private final boolean immutable;

	private IndexIdMapImpl(IntSet indicesSet, Object2IntOpenHashMap<K> idToIndex, Object[] indexToId,
			boolean isVertices, boolean immutable) {
		this.indicesSet = indicesSet;
		this.idToIndex = idToIndex;
		this.indexToId = indexToId;
		this.isVertices = isVertices;
		this.immutable = immutable;
	}

	static <K> IndexIdMapImpl<K> newEmpty(IntSet indicesSet, boolean isVertices, int expectedSize) {
		assert indicesSet.isEmpty();
		Object2IntOpenHashMap<K> idToIndex = new Object2IntOpenHashMap<>(expectedSize);
		idToIndex.defaultReturnValue(-1);
		Object[] indexToId = expectedSize == 0 ? ObjectArrays.DEFAULT_EMPTY_ARRAY : new Object[expectedSize];
		return new IndexIdMapImpl<>(indicesSet, idToIndex, indexToId, isVertices, false);
	}

	static <K> IndexIdMapImpl<K> newCopyOf(IndexIdMap<K> orig, Optional<IndexGraphBuilder.ReIndexingMap> reIndexing,
			IntSet indicesSet, boolean isVertices, boolean immutable) {
		final int elementsSize = indicesSet.size();
		final Object2IntOpenHashMap<K> idToIndex = new Object2IntOpenHashMap<>(elementsSize);
		idToIndex.defaultReturnValue(-1);
		final Object[] indexToId;

		if (elementsSize == 0) {
			indexToId = ObjectArrays.DEFAULT_EMPTY_ARRAY;

		} else if (orig instanceof IndexIdMapImpl) {
			IndexIdMapImpl<K> orig0 = (IndexIdMapImpl<K>) orig;

			if (reIndexing.isEmpty()) {
				idToIndex.putAll(orig0.idToIndex);
				indexToId = Arrays.copyOf(orig0.indexToId, elementsSize);

			} else {
				IndexGraphBuilder.ReIndexingMap reIndexing0 = reIndexing.get();
				Object[] indexToIdOrig = orig0.indexToId;
				indexToId = new Object[elementsSize];
				for (int origIdx : range(elementsSize)) {
					int idx = reIndexing0.map(origIdx);
					@SuppressWarnings("unchecked")
					K id = (K) indexToIdOrig[origIdx];
					indexToId[idx] = id;
					idToIndex.put(id, idx);
				}
			}

		} else {
			indexToId = new Object[elementsSize];
			if (reIndexing.isEmpty()) {
				for (int idx : range(elementsSize)) {
					K id = orig.indexToId(idx);
					indexToId[idx] = id;
					idToIndex.put(id, idx);
				}

			} else {
				IndexGraphBuilder.ReIndexingMap reIndexing0 = reIndexing.get();
				for (int origIdx : range(elementsSize)) {
					int idx = reIndexing0.map(origIdx);
					K id = orig.indexToId(origIdx);
					indexToId[idx] = id;
					idToIndex.put(id, idx);
				}
			}
		}

		if (idToIndex.size() < elementsSize)
			throw new IllegalArgumentException("IDs are not unique");
		if (Arrays.stream(indexToId).anyMatch(Objects::isNull))
			throw new NullPointerException("null id");
		return new IndexIdMapImpl<>(indicesSet, idToIndex, indexToId, isVertices, immutable);
	}

	/* This object should not be used again. Responsibility of the user (of this class). */
	IndexIdMapImpl<K> intoImmutable(Optional<IndexGraphBuilder.ReIndexingMap> reIndexing) {
		if (reIndexing.isPresent()) {
			IndexGraphBuilder.ReIndexingMap reIndexing0 = reIndexing.get();
			for (var entry : Object2IntMaps.fastIterable(idToIndex)) {
				int origIdx = entry.getIntValue();
				int newIdx = reIndexing0.map(origIdx);
				entry.setValue(newIdx);
				indexToId[newIdx] = entry.getKey();
			}
		}
		return new IndexIdMapImpl<>(indicesSet, idToIndex, indexToId, isVertices, true);
	}

	void addId(K id, int idx) {
		boolean added = addIdIfNotDuplicate(id, idx);
		if (!added)
			throw new IllegalArgumentException(
					"Graph already contain such " + (isVertices ? "vertex: " : "edge: ") + id);
	}

	boolean addIdIfNotDuplicate(K id, int idx) {
		assert idx == idToIndex.size();
		int oldIdx = idToIndex.putIfAbsent(id, idx);
		if (oldIdx >= 0)
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
		if (isVertices) {
			g.addVertexRemoveListener(listener);
		} else {
			g.addEdgeRemoveListener(listener);
		}
	}

	void idsClear() {
		Arrays.fill(indexToId, 0, idToIndex.size(), null);
		idToIndex.clear();
	}

	@SuppressWarnings("unchecked")
	@Override
	public K indexToId(int index) {
		Assertions.checkGraphId(index, indicesSet.size(), isVertices);
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
			if (isVertices) {
				throw NoSuchVertexException.ofVertex(id);
			} else {
				throw NoSuchEdgeException.ofEdge(id);
			}
		}
		return idx;
	}

	@Override
	public int idToIndexIfExist(K id) {
		return idToIndex.getInt(Objects.requireNonNull(id));
	}

	void renameId(K oldId, K newId) {
		if (immutable)
			throw new UnsupportedOperationException(
					"graph is immutable, cannot rename " + (isVertices ? "vertices" : "edges"));
		int idx = idToIndex.removeInt(oldId);
		if (idx < 0) {
			if (isVertices) {
				throw NoSuchVertexException.ofVertex(oldId);
			} else {
				throw NoSuchEdgeException.ofEdge(oldId);
			}
		}
		int oldIdx = idToIndex.putIfAbsent(newId, idx);
		if (oldIdx >= 0) {
			idToIndex.put(oldId, idx); /* roll back */
			throw new IllegalArgumentException(
					"Graph already contain such " + (isVertices ? "vertex: " : "edge: ") + newId);
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
