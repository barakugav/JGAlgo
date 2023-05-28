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

package com.jgalgo;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;

abstract class IDStrategyImpl implements IDStrategy {

	private final List<IDSwapListener> idSwapListeners = new CopyOnWriteArrayList<>();
	private final List<IDAddRemoveListener> idAddRemoveListeners = new CopyOnWriteArrayList<>();

	IDStrategyImpl() {}

	static class Continues extends IDStrategyImpl implements IDStrategy.Continues {

		private int size;
		private final IntSet idSet;

		Continues(int initSize) {
			if (initSize < 0)
				throw new IllegalArgumentException("Initial size can not be negative: " + initSize);
			size = initSize;
			idSet = new AbstractIntSet() {

				@Override
				public int size() {
					return size;
				}

				@Override
				public boolean contains(int key) {
					return key >= 0 && key < size();
				}

				@Override
				public IntIterator iterator() {
					return new Utils.RangeIter(size());
				}
			};
		}

		@Override
		int newIdx() {
			int id = size++;
			notifyIDAdd(id);
			return id;
		}

		@Override
		void removeIdx(int idx) {
			assert idx == size - 1;
			assert size > 0;
			size--;
			notifyIDRemove(idx);
		}

		@Override
		int size() {
			return size;
		}

		@Override
		void clear() {
			size = 0;
		}

		@Override
		int idToIdx(int id) {
			if (!(0 <= id && id < size))
				throw new IndexOutOfBoundsException(id);
			return id;
		}

		@Override
		int idxToId(int idx) {
			checkIdx(idx);
			return idx;
		}

		@Override
		IntSet idSet() {
			return idSet;
		}

		@Override
		void ensureSize(int n) {}

		@Override
		void idxSwap(int idx1, int idx2) {
			int id1 = idxToId(idx1), id2 = idxToId(idx2);
			notifyIDSwap(id1, id2);
		}

		@Override
		IDStrategy.Continues copy() {
			return new IDStrategyImpl.Continues(size);
		}

		private void checkIdx(int idx) {
			if (!(0 <= idx && idx < size))
				throw new IndexOutOfBoundsException(idx);
		}
	}

	static class ContinuesEmpty extends IDStrategyImpl implements IDStrategy.Continues {

		@Override
		int newIdx() {
			throw new UnsupportedOperationException();
		}

		@Override
		void removeIdx(int idx) {
			throw new IndexOutOfBoundsException(idx);
		}

		@Override
		int size() {
			return 0;
		}

		@Override
		void clear() {
			throw new UnsupportedOperationException();
		}

		@Override
		int idToIdx(int id) {
			throw new IndexOutOfBoundsException(id);
		}

		@Override
		int idxToId(int idx) {
			throw new IndexOutOfBoundsException(idx);
		}

		@Override
		IntSet idSet() {
			return IntSets.emptySet();
		}

		@Override
		void idxSwap(int idx1, int idx2) {
			throw new IndexOutOfBoundsException(idx1);
		}

		@Override
		void ensureSize(int n) {}

		@Override
		IDStrategy copy() {
			return new IDStrategyImpl.ContinuesEmpty();
		}

	}

	private abstract static class FixedAbstract extends IDStrategyImpl implements IDStrategy.Fixed {

		private final Int2IntOpenHashMap idToIdx;
		private final IntSet idsView; // move to graph abstract implementation
		private final DataContainer.Int idxToId;

		FixedAbstract() {
			idToIdx = new Int2IntOpenHashMap();
			idsView = IntSets.unmodifiable(idToIdx.keySet());
			idxToId = new DataContainer.Int(this, 0);
		}

		FixedAbstract(FixedAbstract orig) {
			idToIdx = new Int2IntOpenHashMap(orig.idToIdx);
			idsView = IntSets.unmodifiable(idToIdx.keySet());
			idxToId = orig.idxToId.copy(this);
		}

		@Override
		int newIdx() {
			int idx = idToIdx.size();
			int id = nextID();
			assert id >= 0;
			idToIdx.put(id, idx);
			if (idx == idxToId.capacity())
				idxToId.expand(Math.max(2, 2 * idxToId.capacity()));
			idxToId.set(idx, id);
			notifyIDAdd(id);
			return idx;
		}

		abstract int nextID();

		@Override
		void removeIdx(int idx) {
			final int id = idxToId.getInt(idx);
			idxToId.clear(idx);
			idToIdx.remove(id);
			notifyIDRemove(id);
		}

		@Override
		int size() {
			return idToIdx.size();
		}

		@Override
		void clear() {
			idToIdx.clear();
			idxToId.clear();
		}

		@Override
		int idToIdx(int id) {
			if (!idToIdx.containsKey(id))
				throw new IndexOutOfBoundsException(id);
			return idToIdx.get(id);
		}

		@Override
		int idxToId(int idx) {
			return idxToId.getInt(idx);
		}

		@Override
		IntSet idSet() {
			return idsView;
		}

		@Override
		void ensureSize(int n) {
			idToIdx.ensureCapacity(n);
		}

		@Override
		void idxSwap(int idx1, int idx2) {
			int id1 = idxToId.getInt(idx1);
			int id2 = idxToId.getInt(idx2);
			idxToId.set(idx1, id2);
			idxToId.set(idx2, id1);
			int oldIdx1 = idToIdx.put(id1, idx2);
			int oldIdx2 = idToIdx.put(id2, idx1);
			assert idx1 == oldIdx1;
			assert idx2 == oldIdx2;

			// The user IDs were not changed, no need to call notifyIDSwap
		}
	}

	static class Fixed extends FixedAbstract {

		private int counter;

		Fixed() {
			counter = 0;
		}

		Fixed(IDStrategyImpl.Fixed orig) {
			super(orig);
			this.counter = orig.counter;
		}

		@Override
		int nextID() {
			return counter++;
		}

		@Override
		IDStrategy.Fixed copy() {
			return new IDStrategyImpl.Fixed(this);
		}
	}

	static class Rand extends FixedAbstract {

		private final Random rand = new Random();

		Rand() {}

		Rand(Rand orig) {
			super(orig);
		}

		@Override
		int nextID() {
			for (;;) {
				int id = rand.nextInt();
				if (id >= 0 && !idSet().contains(id))
					return id;
			}
		}

		@Override
		IDStrategyImpl.Rand copy() {
			return new IDStrategyImpl.Rand(this);
		}
	}

	abstract int newIdx();

	abstract void removeIdx(int idx);

	abstract int size();

	abstract void clear();

	abstract int idToIdx(int id);

	abstract int idxToId(int idx);

	abstract IntSet idSet();

	@Override
	public String toString() {
		return idSet().toString();
	}

	int isSwapNeededBeforeRemove(int idx) {
		int size = idSet().size();
		if (!(0 <= idx && idx < size))
			throw new IndexOutOfBoundsException(idx);
		return size - 1;
	}

	abstract void idxSwap(int idx1, int idx2);

	void notifyIDSwap(int id1, int id2) {
		for (IDSwapListener listener : idSwapListeners)
			listener.idSwap(id1, id2);
	}

	void notifyIDAdd(int id) {
		for (IDAddRemoveListener listener : idAddRemoveListeners)
			listener.idAdd(id);
	}

	void notifyIDRemove(int id) {
		for (IDAddRemoveListener listener : idAddRemoveListeners)
			listener.idRemove(id);
	}

	abstract void ensureSize(int n);

	abstract IDStrategy copy();

	@Override
	public void addIDSwapListener(IDSwapListener listener) {
		idSwapListeners.add(Objects.requireNonNull(listener));
	}

	@Override
	public void removeIDSwapListener(IDSwapListener listener) {
		idSwapListeners.remove(listener);
	}

	@Override
	public void addIDAddRemoveListener(IDAddRemoveListener listener) {
		idAddRemoveListeners.add(Objects.requireNonNull(listener));
	}

	@Override
	public void removeIDAddRemoveListener(IDAddRemoveListener listener) {
		idAddRemoveListeners.remove(listener);
	}

}
