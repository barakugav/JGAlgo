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
import java.util.concurrent.CopyOnWriteArrayList;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;

abstract class IDStrategyImpl implements IDStrategy {

	private final List<IDSwapListener> idSwapListeners = new CopyOnWriteArrayList<>();
	private final List<IDAddRemoveListener> idAddRemoveListeners = new CopyOnWriteArrayList<>();

	IDStrategyImpl() {}

	static class Continues extends IDStrategyImpl {

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
			notifyIdAdd(id);
			return id;
		}

		@Override
		void removeIdx(int idx) {
			assert idx == size - 1;
			assert size > 0;
			notifyIdRemove(idx);
			size--;
		}

		@Override
		int size() {
			return size;
		}

		@Override
		void clear() {
			notifyIdsClear();
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
		void idxSwap(int idx1, int idx2) {
			int id1 = idxToId(idx1), id2 = idxToId(idx2);
			notifyIDSwap(id1, id2);
		}

		@Override
		IDStrategyImpl copy() {
			return new IDStrategyImpl.Continues(size);
		}

		private void checkIdx(int idx) {
			if (!(0 <= idx && idx < size))
				throw new IndexOutOfBoundsException(idx);
		}
	}

	static class ContinuesEmpty extends IDStrategyImpl {

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
		IDStrategyImpl copy() {
			return new IDStrategyImpl.ContinuesEmpty();
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

	void notifyIdAdd(int id) {
		for (IDAddRemoveListener listener : idAddRemoveListeners)
			listener.idAdd(id);
	}

	void notifyIdRemove(int id) {
		for (IDAddRemoveListener listener : idAddRemoveListeners)
			listener.idRemove(id);
	}

	void notifyIdsClear() {
		for (IDAddRemoveListener listener : idAddRemoveListeners)
			listener.idsClear();
	}

	abstract IDStrategyImpl copy();

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
