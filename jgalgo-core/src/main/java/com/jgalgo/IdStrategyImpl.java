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

abstract class IdStrategyImpl implements IdStrategy {

	abstract int size();

	abstract IntSet idSet();

	@Override
	public String toString() {
		return idSet().toString();
	}

	static class Index extends IdStrategyImpl {

		private int size;
		private final IntSet idSet;
		private final List<IdSwapListener> idSwapListeners = new CopyOnWriteArrayList<>();
		private final List<IdAddRemoveListener> idAddRemoveListeners = new CopyOnWriteArrayList<>();

		Index(int initSize) {
			if (initSize < 0)
				throw new IllegalArgumentException("Initial size can not be negative: " + initSize);
			size = initSize;
			idSet = new IdSet();
		}

		int newIdx() {
			int id = size++;
			notifyIdAdd(id);
			return id;
		}

		void removeIdx(int idx) {
			assert idx == size - 1;
			assert size > 0;
			notifyIdRemove(idx);
			size--;
		}

		void clear() {
			notifyIdsClear();
			size = 0;
		}

		@Override
		int size() {
			return size;
		}

		@Override
		IntSet idSet() {
			return idSet;
		}

		int isSwapNeededBeforeRemove(int idx) {
			checkIdx(idx);
			return size - 1;
		}

		void idxSwap(int idx1, int idx2) {
			notifyIDSwap(idx1, idx2);
		}

		IdStrategyImpl.Index copy() {
			return new IdStrategyImpl.Index(size);
		}

		private void checkIdx(int idx) {
			if (!(0 <= idx && idx < size))
				throw new IndexOutOfBoundsException(idx);
		}

		void notifyIDSwap(int id1, int id2) {
			for (IdSwapListener listener : idSwapListeners)
				listener.idSwap(id1, id2);
		}

		void notifyIdAdd(int id) {
			for (IdAddRemoveListener listener : idAddRemoveListeners)
				listener.idAdd(id);
		}

		void notifyIdRemove(int id) {
			for (IdAddRemoveListener listener : idAddRemoveListeners)
				listener.idRemove(id);
		}

		void notifyIdsClear() {
			for (IdAddRemoveListener listener : idAddRemoveListeners)
				listener.idsClear();
		}

		@Override
		public void addIdSwapListener(IdSwapListener listener) {
			idSwapListeners.add(Objects.requireNonNull(listener));
		}

		@Override
		public void removeIdSwapListener(IdSwapListener listener) {
			idSwapListeners.remove(listener);
		}

		void addIdAddRemoveListener(IdAddRemoveListener listener) {
			idAddRemoveListeners.add(Objects.requireNonNull(listener));
		}

		void removeIdAddRemoveListener(IdAddRemoveListener listener) {
			idAddRemoveListeners.remove(listener);
		}

		private class IdSet extends AbstractIntSet {

			@Override
			public int size() {
				return size;
			}

			@Override
			public boolean contains(int key) {
				return key >= 0 && key < size;
			}

			@Override
			public IntIterator iterator() {
				return new Utils.RangeIter(size);
			}

			@Override
			public boolean equals(Object other) {
				if (this == other)
					return true;
				if (!(other instanceof IdSet))
					return super.equals(other);
				IdSet o = (IdSet) other;
				return size == o.size();
			}

			@Override
			public int hashCode() {
				return size * (size - 1) / 2;
			}
		}
	}

	static class Empty extends IdStrategyImpl {

		@Override
		int size() {
			return 0;
		}

		@Override
		IntSet idSet() {
			return IntSets.emptySet();
		}

		@Override
		public void addIdSwapListener(IdSwapListener listener) {
			Objects.requireNonNull(listener);
		}

		@Override
		public void removeIdSwapListener(IdSwapListener listener) {}
	}

	/**
	 * A listener that will be notified each time a strategy add or remove an id.
	 *
	 * @author Barak Ugav
	 */
	static interface IdAddRemoveListener {
		/**
		 * A callback that is called when {@code id} is added by the strategy.
		 *
		 * @param id the new id
		 */
		void idAdd(int id);

		/**
		 * A callback that is called when {@code id} is removed by the strategy.
		 *
		 * @param id the removed id
		 */
		void idRemove(int id);

		/**
		 * A callback that is called when all ids are removed from the strategy.
		 */
		void idsClear();
	}

}
