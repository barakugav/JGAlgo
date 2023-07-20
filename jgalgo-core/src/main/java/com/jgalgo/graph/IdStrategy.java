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

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;

abstract class IdStrategy {

	int size;
	private final IntSet indices;

	IdStrategy(int initSize) {
		if (initSize < 0)
			throw new IllegalArgumentException("Initial size can not be negative: " + initSize);
		size = initSize;
		indices = new IndicesSet();
	}

	int size() {
		return size;
	}

	IntSet indices() {
		return indices;
	}

	abstract void addIdSwapListener(IndexSwapListener listener);

	abstract void removeIdSwapListener(IndexSwapListener listener);

	abstract void addIdAddRemoveListener(IdAddRemoveListener listener);

	abstract void removeIdAddRemoveListener(IdAddRemoveListener listener);

	@Override
	public String toString() {
		return indices().toString();
	}

	private class IndicesSet extends AbstractIntSet {

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
			return JGAlgoUtils.rangeIter(size);
		}

		@Override
		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (!(other instanceof IndicesSet))
				return super.equals(other);
			IndicesSet o = (IndicesSet) other;
			return size == o.size();
		}

		@Override
		public int hashCode() {
			return size * (size + 1) / 2;
		}
	}

	static class FixedSize extends IdStrategy {

		FixedSize(int initSize) {
			super(initSize);
		}

		@Override
		void addIdSwapListener(IndexSwapListener listener) {
			Objects.requireNonNull(listener);
		}

		@Override
		void removeIdSwapListener(IndexSwapListener listener) {}

		@Override
		void addIdAddRemoveListener(IdAddRemoveListener listener) {
			Objects.requireNonNull(listener);
		}

		@Override
		void removeIdAddRemoveListener(IdAddRemoveListener listener) {}
	}

	static class Default extends IdStrategy.FixedSize {

		private final List<IndexSwapListener> idSwapListeners = new CopyOnWriteArrayList<>();
		private final List<IdAddRemoveListener> idAddRemoveListeners = new CopyOnWriteArrayList<>();

		Default(int initSize) {
			super(initSize);
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

		int isSwapNeededBeforeRemove(int idx) {
			Assertions.Graphs.checkId(idx, size);
			return size - 1;
		}

		void idxSwap(int idx1, int idx2) {
			Assertions.Graphs.checkId(idx1, size);
			Assertions.Graphs.checkId(idx2, size);
			notifyIDSwap(idx1, idx2);
		}

		IdStrategy.Default copy() {
			return new IdStrategy.Default(size);
		}

		void notifyIDSwap(int id1, int id2) {
			for (IndexSwapListener listener : idSwapListeners)
				listener.swap(id1, id2);
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
		void addIdSwapListener(IndexSwapListener listener) {
			idSwapListeners.add(Objects.requireNonNull(listener));
		}

		@Override
		void removeIdSwapListener(IndexSwapListener listener) {
			idSwapListeners.remove(listener);
		}

		@Override
		void addIdAddRemoveListener(IdAddRemoveListener listener) {
			idAddRemoveListeners.add(Objects.requireNonNull(listener));
		}

		@Override
		void removeIdAddRemoveListener(IdAddRemoveListener listener) {
			idAddRemoveListeners.remove(listener);
		}
	}

	static final IdStrategy Empty = new IdStrategy.FixedSize(0);

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
