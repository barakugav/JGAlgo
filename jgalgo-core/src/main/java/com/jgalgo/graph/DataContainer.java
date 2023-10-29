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
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrays;

abstract class DataContainer {

	final GraphElementSet elements;

	DataContainer(GraphElementSet elements) {
		this.elements = Objects.requireNonNull(elements);
	}

	int size() {
		return elements.size();
	}

	void checkIdx(int idx) {
		if (!elements.contains(idx))
			throw new IndexOutOfBoundsException(idx);
	}

	abstract void expand(int newCapacity);

	abstract int capacity();

	static class Obj<T> extends DataContainer {

		private T[] weights;
		private final T defaultWeight;
		private final Consumer<T[]> onArrayAlloc;

		Obj(GraphElementSet elements, T defVal, T[] emptyArr, Consumer<T[]> onArrayAlloc) {
			super(elements);

			defaultWeight = defVal;
			this.onArrayAlloc = Objects.requireNonNull(onArrayAlloc);
			weights = emptyArr;
			Arrays.fill(weights, defaultWeight);
			onArrayAlloc.accept(weights);
		}

		public T get(int idx) {
			checkIdx(idx);
			return weights[idx];
		}

		public void set(int idx, T weight) {
			checkIdx(idx);
			weights[idx] = weight;
		}

		@Override
		public int capacity() {
			return weights.length;
		}

		@Override
		public void expand(int newCapacity) {
			int oldCapacity = weights.length;
			assert oldCapacity < newCapacity;
			weights = Arrays.copyOf(weights, newCapacity);
			Arrays.fill(weights, oldCapacity, newCapacity, defaultWeight);
			onArrayAlloc.accept(weights);
		}

		void swap(int idx1, int idx2) {
			checkIdx(idx1);
			checkIdx(idx2);
			ObjectArrays.swap(weights, idx1, idx2);
		}

		void swap(T[] weights, int idx1, int idx2) {
			ObjectArrays.swap(weights, idx1, idx2);
		}

		void clear(int idx) {
			weights[idx] = defaultWeight;
		}

		void clear(T[] weights, int idx) {
			weights[idx] = defaultWeight;
		}

		void clear() {
			Arrays.fill(weights, 0, size(), defaultWeight);
		}

		public void clear(T[] weights) {
			Arrays.fill(weights, 0, size(), defaultWeight);
		}

		public DataContainer.Obj<T> copy(GraphElementSet elements, T[] emptyArr, Consumer<T[]> onArrayAlloc) {
			if (elements.size() != this.elements.size())
				throw new IllegalArgumentException();
			DataContainer.Obj<T> copy = new DataContainer.Obj<>(elements, defaultWeight, emptyArr, onArrayAlloc);
			copy.weights = Arrays.copyOf(weights, elements.size());
			onArrayAlloc.accept(copy.weights);
			return copy;
		}
	}

	static class Int extends DataContainer {

		private int[] weights;
		private final int defaultWeight;
		private final Consumer<int[]> onArrayAlloc;

		Int(GraphElementSet elements, int defVal, Consumer<int[]> onArrayAlloc) {
			super(elements);

			weights = IntArrays.EMPTY_ARRAY;
			defaultWeight = defVal;
			this.onArrayAlloc = Objects.requireNonNull(onArrayAlloc);
			onArrayAlloc.accept(weights);
		}

		public int get(int idx) {
			checkIdx(idx);
			return weights[idx];
		}

		public void set(int idx, int weight) {
			checkIdx(idx);
			weights[idx] = weight;
		}

		public int defaultWeight() {
			return defaultWeight;
		}

		@Override
		public int capacity() {
			return weights.length;
		}

		@Override
		public void expand(int newCapacity) {
			int oldCapacity = weights.length;
			assert oldCapacity < newCapacity;
			weights = Arrays.copyOf(weights, newCapacity);
			Arrays.fill(weights, oldCapacity, newCapacity, defaultWeight);
			onArrayAlloc.accept(weights);
		}

		void swap(int idx1, int idx2) {
			checkIdx(idx1);
			checkIdx(idx2);
			IntArrays.swap(weights, idx1, idx2);
		}

		void swap(int[] weights, int idx1, int idx2) {
			IntArrays.swap(weights, idx1, idx2);
		}

		void clear(int idx) {
			weights[idx] = defaultWeight;
		}

		void clear(int[] weights, int idx) {
			weights[idx] = defaultWeight;
		}

		void clear() {
			Arrays.fill(weights, 0, size(), defaultWeight);
		}

		void clear(int[] weights) {
			Arrays.fill(weights, 0, size(), defaultWeight);
		}

		DataContainer.Int copy(GraphElementSet elements, Consumer<int[]> onArrayAlloc) {
			if (elements.size() != this.elements.size())
				throw new IllegalArgumentException();
			DataContainer.Int copy = new DataContainer.Int(elements, defaultWeight, onArrayAlloc);
			copy.weights = Arrays.copyOf(weights, elements.size());
			onArrayAlloc.accept(copy.weights);
			return copy;
		}
	}

	static class Long extends DataContainer {

		private long[] weights;
		private final long defaultWeight;
		private final Consumer<long[]> onArrayAlloc;

		Long(GraphElementSet elements, long defVal, Consumer<long[]> onArrayAlloc) {
			super(elements);

			weights = LongArrays.EMPTY_ARRAY;
			defaultWeight = defVal;
			this.onArrayAlloc = Objects.requireNonNull(onArrayAlloc);
			onArrayAlloc.accept(weights);
		}

		Long(DataContainer.Long orig, GraphElementSet elements, Consumer<long[]> onArrayAlloc) {
			super(elements);
			if (elements.size() != this.elements.size())
				throw new IllegalArgumentException();

			weights = Arrays.copyOf(orig.weights, elements.size());
			defaultWeight = orig.defaultWeight;
			this.onArrayAlloc = Objects.requireNonNull(onArrayAlloc);
			onArrayAlloc.accept(weights);
		}

		long get(int idx) {
			checkIdx(idx);
			return weights[idx];
		}

		void set(int idx, long weight) {
			checkIdx(idx);
			weights[idx] = weight;
		}

		long defaultWeight() {
			return defaultWeight;
		}

		@Override
		int capacity() {
			return weights.length;
		}

		@Override
		void expand(int newCapacity) {
			int oldCapacity = weights.length;
			assert oldCapacity < newCapacity;
			weights = Arrays.copyOf(weights, newCapacity);
			Arrays.fill(weights, oldCapacity, newCapacity, defaultWeight);
			onArrayAlloc.accept(weights);
		}

		// void swap(int idx1, int idx2) {
		// checkIdx(idx1);
		// checkIdx(idx2);
		// LongArrays.swap(weights, idx1, idx2);
		// }

		void swap(long[] weights, int idx1, int idx2) {
			checkIdx(idx1);
			checkIdx(idx2);
			LongArrays.swap(weights, idx1, idx2);
		}

		// void clear(int idx) {
		// weights[idx] = defaultWeight;
		// }

		void clear(long[] weights, int idx) {
			weights[idx] = defaultWeight;
		}

		// void clear() {
		// Arrays.fill(weights, 0, size(), defaultWeight);
		// }

		void clear(long[] weights) {
			Arrays.fill(weights, 0, size(), defaultWeight);
		}

		DataContainer.Long copy(GraphElementSet elements, Consumer<long[]> onArrayAlloc) {
			return new DataContainer.Long(this, elements, onArrayAlloc);
		}
	}

	static class Manager {

		final List<DataContainer> containers = new ObjectArrayList<>();
		private int containersCapacity;

		Manager(int initCapacity) {
			containersCapacity = initCapacity;
		}

		void addContainer(DataContainer container) {
			containers.add(container);
			if (containersCapacity > container.capacity())
				container.expand(containersCapacity);
		}

		void ensureCapacity(int capacity) {
			if (capacity <= containersCapacity)
				return;
			int newCapacity = Math.max(Math.max(2, 2 * containersCapacity), capacity);
			for (DataContainer container : containers)
				container.expand(newCapacity);
			containersCapacity = newCapacity;
		}
	}

}
