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

abstract class DataContainer {

	final GraphElementSet elements;

	DataContainer(GraphElementSet elements) {
		this.elements = Objects.requireNonNull(elements);
	}

	int size() {
		return elements.size();
	}

	abstract void expand(int newCapacity);

	abstract int capacity();

	static class Obj<T> extends DataContainer {

		T[] data;
		private final T defaultVal;
		private final Consumer<T[]> onArrayAlloc;

		Obj(GraphElementSet elements, T defVal, T[] emptyArr, Consumer<T[]> onArrayAlloc) {
			super(elements);

			defaultVal = defVal;
			this.onArrayAlloc = Objects.requireNonNull(onArrayAlloc);
			data = emptyArr;
			Arrays.fill(data, defaultVal);
			onArrayAlloc.accept(data);
		}

		@Override
		public int capacity() {
			return data.length;
		}

		@Override
		public void expand(int newCapacity) {
			int oldCapacity = data.length;
			assert oldCapacity < newCapacity;
			data = Arrays.copyOf(data, newCapacity);
			Arrays.fill(data, oldCapacity, newCapacity, defaultVal);
			onArrayAlloc.accept(data);
		}

		void clear() {
			Arrays.fill(data, 0, size(), defaultVal);
		}

		public DataContainer.Obj<T> copy(GraphElementSet elements, T[] emptyArr, Consumer<T[]> onArrayAlloc) {
			assert elements.size() == this.elements.size();
			DataContainer.Obj<T> copy = new DataContainer.Obj<>(elements, defaultVal, emptyArr, onArrayAlloc);
			copy.data = Arrays.copyOf(data, elements.size());
			onArrayAlloc.accept(copy.data);
			return copy;
		}
	}

	static class Int extends DataContainer {

		int[] data;
		private final int defaultVal;
		private final Consumer<int[]> onArrayAlloc;

		Int(GraphElementSet elements, int defVal, Consumer<int[]> onArrayAlloc) {
			super(elements);

			data = IntArrays.EMPTY_ARRAY;
			defaultVal = defVal;
			this.onArrayAlloc = Objects.requireNonNull(onArrayAlloc);
			onArrayAlloc.accept(data);
		}

		@Override
		public int capacity() {
			return data.length;
		}

		@Override
		public void expand(int newCapacity) {
			int oldCapacity = data.length;
			assert oldCapacity < newCapacity;
			data = Arrays.copyOf(data, newCapacity);
			Arrays.fill(data, oldCapacity, newCapacity, defaultVal);
			onArrayAlloc.accept(data);
		}

		void clear() {
			Arrays.fill(data, 0, size(), defaultVal);
		}

		DataContainer.Int copy(GraphElementSet elements, Consumer<int[]> onArrayAlloc) {
			assert elements.size() == this.elements.size();
			DataContainer.Int copy = new DataContainer.Int(elements, defaultVal, onArrayAlloc);
			copy.data = Arrays.copyOf(data, elements.size());
			onArrayAlloc.accept(copy.data);
			return copy;
		}
	}

	static class Long extends DataContainer {

		private long[] data;
		private final long defaultVal;
		private final Consumer<long[]> onArrayAlloc;

		Long(GraphElementSet elements, long defVal, Consumer<long[]> onArrayAlloc) {
			super(elements);

			data = LongArrays.EMPTY_ARRAY;
			defaultVal = defVal;
			this.onArrayAlloc = Objects.requireNonNull(onArrayAlloc);
			onArrayAlloc.accept(data);
		}

		@Override
		int capacity() {
			return data.length;
		}

		@Override
		void expand(int newCapacity) {
			int oldCapacity = data.length;
			assert oldCapacity < newCapacity;
			data = Arrays.copyOf(data, newCapacity);
			Arrays.fill(data, oldCapacity, newCapacity, defaultVal);
			onArrayAlloc.accept(data);
		}

		void clear() {
			Arrays.fill(data, 0, size(), defaultVal);
		}

		DataContainer.Long copy(GraphElementSet elements, Consumer<long[]> onArrayAlloc) {
			assert elements.size() == this.elements.size();
			DataContainer.Long copy = new DataContainer.Long(elements, defaultVal, onArrayAlloc);
			copy.data = Arrays.copyOf(data, elements.size());
			onArrayAlloc.accept(copy.data);
			return copy;
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
