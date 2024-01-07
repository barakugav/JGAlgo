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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import com.jgalgo.internal.util.Assertions;
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
		return 0 <= key && key < size;
	}

	@Override
	public IntIterator iterator() {
		return range(size).iterator();
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
		return size * (size - 1) / 2;
	}

	void checkIdx(int idx) {
		Assertions.checkGraphId(idx, size, isEdges);
	}

	static class Immutable extends GraphElementSet {

		private Immutable(int size, boolean isEdges) {
			super(size, isEdges);
		}

		static GraphElementSet.Immutable ofEdges(int size) {
			return new GraphElementSet.Immutable(size, true);
		}

		static GraphElementSet.Immutable ofVertices(int size) {
			return new GraphElementSet.Immutable(size, false);
		}

	}

	static class Mutable extends GraphElementSet {

		private final List<IndexRemoveListener> removeListeners = new CopyOnWriteArrayList<>();

		private Mutable(int initSize, boolean isEdges) {
			super(initSize, isEdges);
		}

		static GraphElementSet.Mutable ofEdges(int initSize) {
			return new GraphElementSet.Mutable(initSize, true);
		}

		static GraphElementSet.Mutable ofVertices(int initSize) {
			return new GraphElementSet.Mutable(initSize, false);
		}

		int add() {
			int id = size++;
			return id;
		}

		void addAll(int count) {
			assert count >= 0;
			size += count;
		}

		void removeIdx(int idx) {
			assert idx == size - 1;
			assert size > 0;
			for (IndexRemoveListener listener : removeListeners)
				listener.removeLast(idx);
			size--;
		}

		/* identical to removeIdx without notifying listeners */
		void rollBackAdd(int idx) {
			assert idx == size - 1;
			assert size > 0;
			size--;
		}

		@Override
		public void clear() {
			size = 0;
		}

		void swapAndRemove(int removedIdx, int swappedIdx) {
			checkIdx(removedIdx);
			checkIdx(swappedIdx);
			assert swappedIdx == size - 1;
			assert size > 0;
			for (IndexRemoveListener listener : removeListeners)
				listener.swapAndRemove(removedIdx, swappedIdx);
			size--;
		}

		void addRemoveListener(IndexRemoveListener listener) {
			removeListeners.add(Objects.requireNonNull(listener));
		}

		void removeRemoveListener(IndexRemoveListener listener) {
			removeListeners.remove(listener);
		}
	}

}
