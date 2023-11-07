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
import com.jgalgo.internal.util.Range;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntIterator;

abstract class GraphElementSet extends AbstractIntSet {

	int size;
	final boolean isEdges;

	GraphElementSet(int initSize, boolean isEdges) {
		if (initSize < 0)
			throw new IllegalArgumentException("Initial size can not be negative: " + initSize);
		size = initSize;
		this.isEdges = isEdges;
	}

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
		return Range.of(size).iterator();
	}

	@Override
	public boolean equals(Object other) {
		if (this == other)
			return true;
		if (!(other instanceof GraphElementSet))
			return super.equals(other);
		GraphElementSet o = (GraphElementSet) other;
		return size == o.size();
	}

	@Override
	public int hashCode() {
		return size * (size + 1) / 2;
	}

	void checkIdx(int idx) {
		Assertions.Graphs.checkId(idx, size, isEdges);
	}

	abstract void addRemoveListener(IndexRemoveListener listener);

	abstract void removeRemoveListener(IndexRemoveListener listener);

	static class FixedSize extends GraphElementSet {

		FixedSize(int initSize, boolean isEdges) {
			super(initSize, isEdges);
		}

		@Override
		void addRemoveListener(IndexRemoveListener listener) {
			Objects.requireNonNull(listener);
		}

		@Override
		void removeRemoveListener(IndexRemoveListener listener) {}

	}

	static class Default extends GraphElementSet.FixedSize {

		private final List<IndexRemoveListener> removeListeners = new CopyOnWriteArrayList<>();

		Default(int initSize, boolean isEdges) {
			super(initSize, isEdges);
		}

		int newIdx() {
			int id = size++;
			return id;
		}

		void removeIdx(int idx) {
			assert idx == size - 1;
			assert size > 0;
			for (IndexRemoveListener listener : removeListeners)
				listener.removeLast(idx);
			size--;
		}

		@Override
		public void clear() {
			size = 0;
		}

		// void idxSwap(int idx1, int idx2) {
		// checkIdx(idx1);
		// checkIdx(idx2);
		// notifyIDSwap(idx1, idx2);
		// }

		void swapAndRemove(int removedIdx, int swappedIdx) {
			checkIdx(removedIdx);
			checkIdx(swappedIdx);
			assert swappedIdx == size - 1;
			assert size > 0;
			for (IndexRemoveListener listener : removeListeners)
				listener.swapAndRemove(removedIdx, swappedIdx);
			size--;
		}

		GraphElementSet.Default copy() {
			return new GraphElementSet.Default(size, isEdges);
		}

		// void notifyIDSwap(int id1, int id2) {
		// for (IndexRemoveListener listener : idSwapListeners)
		// listener.swap(id1, id2);
		// }

		@Override
		void addRemoveListener(IndexRemoveListener listener) {
			removeListeners.add(Objects.requireNonNull(listener));
		}

		@Override
		void removeRemoveListener(IndexRemoveListener listener) {
			removeListeners.remove(listener);
		}
	}

	static final GraphElementSet EmptyVertices = new GraphElementSet.FixedSize(0, false);
	static final GraphElementSet EmptyEdges = new GraphElementSet.FixedSize(0, true);

}
