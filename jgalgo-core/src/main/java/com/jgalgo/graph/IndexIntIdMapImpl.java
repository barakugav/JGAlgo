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
import java.util.Optional;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntSet;

class IndexIntIdMapImpl implements IndexIntIdMap {

	private final IntSet indicesSet;
	private final IntSet idsSet = new IdSet();
	private final Int2IntOpenHashMap idToIndex;
	private int[] indexToId;
	private final boolean isEdges;
	private final boolean immutable;

	private IndexIntIdMapImpl(IntSet indicesSet, boolean isEdges, int expectedSize) {
		assert indicesSet.isEmpty();
		this.indicesSet = indicesSet;
		idToIndex = new Int2IntOpenHashMap(expectedSize);
		idToIndex.defaultReturnValue(-1);
		indexToId = expectedSize == 0 ? IntArrays.DEFAULT_EMPTY_ARRAY : new int[expectedSize];
		this.isEdges = isEdges;
		immutable = false;
	}

	private IndexIntIdMapImpl(IndexIdMap<Integer> orig, Optional<IndexGraphBuilder.ReIndexingMap> reIndexing,
			IntSet indicesSet, boolean isEdges, boolean immutable) {
		this.indicesSet = indicesSet;
		int elementsSize = this.indicesSet.size();
		if (orig instanceof IndexIntIdMapImpl && reIndexing.isEmpty()) {
			IndexIntIdMapImpl orig0 = (IndexIntIdMapImpl) orig;
			idToIndex = new Int2IntOpenHashMap(orig0.idToIndex);
			idToIndex.defaultReturnValue(-1);
			indexToId = Arrays.copyOf(orig0.indexToId, elementsSize);

		} else {
			idToIndex = new Int2IntOpenHashMap(elementsSize);
			idToIndex.defaultReturnValue(-1);
			if (this.indicesSet.isEmpty()) {
				indexToId = IntArrays.DEFAULT_EMPTY_ARRAY;
			} else {
				indexToId = new int[elementsSize];
				if (reIndexing.isEmpty()) {
					for (int idx : range(elementsSize)) {
						int id = orig.indexToId(idx).intValue();
						if (id < 0)
							throw new IllegalArgumentException("negative id: " + id);
						indexToId[idx] = id;

						int oldIdx = idToIndex.put(id, idx);
						if (oldIdx != -1)
							throw new IllegalArgumentException("duplicate id: " + id);
					}

				} else {
					IndexGraphBuilder.ReIndexingMap reIndexing0 = reIndexing.get();
					for (int idx : range(elementsSize)) {
						int id = orig.indexToId(reIndexing0.reIndexedToOrig(idx)).intValue();
						if (id < 0)
							throw new IllegalArgumentException("negative id: " + id);
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

	static IndexIntIdMapImpl newEmpty(IntSet indicesSet, boolean isEdges, int expectedSize) {
		return new IndexIntIdMapImpl(indicesSet, isEdges, expectedSize);
	}

	static IndexIntIdMapImpl newCopyOf(IndexIdMap<Integer> orig, Optional<IndexGraphBuilder.ReIndexingMap> reIndexing,
			IntSet indicesSet, boolean isEdges, boolean immutable) {
		return new IndexIntIdMapImpl(orig, reIndexing, indicesSet, isEdges, immutable);
	}

	void addId(int id, int idx) {
		boolean added = addIdIfNotDuplicate(id, idx);
		if (!added) {
			if (isEdges) {
				throw new IllegalArgumentException("Graph already contain such an edge: " + id);
			} else {
				throw new IllegalArgumentException("Graph already contain such a vertex: " + id);
			}
		}
	}

	boolean addIdIfNotDuplicate(int id, int idx) {
		assert id >= 0;
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
		int id1 = indexToId[removedIdx];
		int id2 = indexToId[swappedIdx];
		indexToId[removedIdx] = id2;
		// indexToId[swappedIdx] = -1;
		int oldIdx1 = idToIndex.remove(id1);
		int oldIdx2 = idToIndex.put(id2, removedIdx);
		assert removedIdx == oldIdx1;
		assert swappedIdx == oldIdx2;
	}

	private void removeLast(int removedIdx) {
		int id = indexToId[removedIdx];
		// indexToId[removedIdx] = -1;
		idToIndex.remove(id);
	}

	void initListeners(IndexGraph g) {
		IndexRemoveListener listener = new IndexRemoveListener() {

			@Override
			public void swapAndRemove(int removedIdx, int swappedIdx) {
				IndexIntIdMapImpl.this.swapAndRemove(removedIdx, swappedIdx);
			}

			@Override
			public void removeLast(int removedIdx) {
				IndexIntIdMapImpl.this.removeLast(removedIdx);
			}
		};
		if (isEdges) {
			g.addEdgeRemoveListener(listener);
		} else {
			g.addVertexRemoveListener(listener);
		}
	}

	void idsClear() {
		// Arrays.fill(indexToId, 0, idToIndex.size(), -1);
		idToIndex.clear();
	}

	@Override
	public int indexToIdInt(int index) {
		Assertions.checkGraphId(index, indicesSet.size(), isEdges);
		return indexToId[index];
	}

	@Override
	public int indexToIdIfExistInt(int index) {
		if (!(0 <= index && index < indicesSet.size()))
			return -1;
		return indexToId[index];
	}

	@Override
	public int idToIndex(int id) {
		int idx = idToIndex.get(id);
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
	public int idToIndexIfExist(int id) {
		return idToIndex.get(id);
	}

	void renameId(int oldId, int newId) {
		if (immutable) {
			if (isEdges) {
				throw new UnsupportedOperationException("graph is immutable, cannot rename vertices");
			} else {
				throw new UnsupportedOperationException("graph is immutable, cannot rename edges");
			}
		}
		int idx = idToIndex.remove(oldId);
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

	IntSet idSet() {
		return idsSet;
	}

	private class IdSet extends AbstractIntSet {

		@Override
		public int size() {
			return indicesSet.size();
		}

		@Override
		public boolean contains(int key) {
			return idToIndex.containsKey(key);
		}

		@Override
		public boolean containsAll(IntCollection c) {
			return idToIndex.keySet().containsAll(c);
		}

		@Override
		public boolean containsAll(Collection<?> c) {
			return idToIndex.keySet().containsAll(c);
		}

		@Override
		public IntIterator iterator() {
			return IntIterators.wrap(indexToId, 0, indicesSet.size());
		}

		@Override
		public int[] toIntArray() {
			return Arrays.copyOf(indexToId, indicesSet.size());
		}

		@Override
		public int[] toArray(int[] a) {
			int size = indicesSet.size();
			if (a.length < size)
				a = java.util.Arrays.copyOf(a, size);
			System.arraycopy(indexToId, 0, a, 0, size);
			return a;
		}
	}

	void ensureCapacity(int capacity) {
		idToIndex.ensureCapacity(capacity);
		if (capacity > indexToId.length)
			indexToId = Arrays.copyOf(indexToId, Math.max(2, Math.max(2 * indexToId.length, capacity)));
	}

}
