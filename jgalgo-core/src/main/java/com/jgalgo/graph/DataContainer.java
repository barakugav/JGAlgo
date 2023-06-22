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
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import it.unimi.dsi.fastutil.ints.AbstractIntList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.longs.AbstractLongList;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.longs.LongListIterator;
import it.unimi.dsi.fastutil.objects.AbstractObjectList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;

abstract class DataContainer<E> {

	final IdStrategy idStrat;

	DataContainer(IdStrategy idStrat) {
		this.idStrat = Objects.requireNonNull(idStrat);
	}

	int size() {
		return idStrat.size();
	}

	@Override
	public int hashCode() {
		return values().hashCode();
	}

	@Override
	public String toString() {
		return values().toString();
	}

	void checkIdx(int idx) {
		if (!(0 <= idx && idx < idStrat.size()))
			throw new IndexOutOfBoundsException(idx);
	}

	abstract void expand(int newCapacity);

	abstract Collection<E> values();

	private static abstract class Abstract<E> extends DataContainer<E> {

		Abstract(IdStrategy idStrat) {
			super(idStrat);
		}

		abstract int capacity();

	}

	static class Obj<E> extends DataContainer.Abstract<E> {

		private E[] weights;
		private final E defaultWeight;
		private final ObjectCollection<E> values;
		private final Consumer<E[]> onArrayAlloc;

		Obj(IdStrategy idStrat, E defVal, E[] emptyArr, Consumer<E[]> onArrayAlloc) {
			super(idStrat);

			defaultWeight = defVal;
			this.onArrayAlloc = Objects.requireNonNull(onArrayAlloc);
			weights = emptyArr;
			Arrays.fill(weights, defaultWeight);
			onArrayAlloc.accept(weights);

			values = new AbstractObjectList<>() {
				@Override
				public int size() {
					return DataContainer.Obj.super.size();
				}

				@Override
				public ObjectListIterator<E> iterator() {
					return ObjectIterators.wrap(weights, 0, size());
				}

				@Override
				public E get(int index) {
					checkIdx(index);
					return weights[index];
				}
			};
		}

		public E get(int idx) {
			checkIdx(idx);
			return weights[idx];
		}

		public void set(int idx, E weight) {
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

		void swap(E[] weights, int idx1, int idx2) {
			ObjectArrays.swap(weights, idx1, idx2);
		}

		void clear(int idx) {
			weights[idx] = defaultWeight;
		}

		void clear(E[] weights, int idx) {
			weights[idx] = defaultWeight;
		}

		void clear() {
			Arrays.fill(weights, 0, size(), defaultWeight);
		}

		public void clear(E[] weights) {
			Arrays.fill(weights, 0, size(), defaultWeight);
		}

		@Override
		public Collection<E> values() {
			return values;
		}

		public DataContainer.Obj<E> copy(IdStrategy idStrat, E[] emptyArr, Consumer<E[]> onArrayAlloc) {
			if (idStrat.size() != this.idStrat.size())
				throw new IllegalArgumentException();
			DataContainer.Obj<E> copy = new DataContainer.Obj<>(idStrat, defaultWeight, emptyArr, onArrayAlloc);
			copy.weights = Arrays.copyOf(weights, idStrat.size());
			onArrayAlloc.accept(copy.weights);
			return copy;
		}

		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof DataContainer.Obj<?>))
				return false;
			DataContainer.Obj<?> o = (DataContainer.Obj<?>) other;
			return Arrays.equals(weights, 0, size(), o.weights, 0, o.size());
		}
	}

	static class Int extends DataContainer.Abstract<Integer> {

		private int[] weights;
		private final int defaultWeight;
		private final IntCollection values;
		private final Consumer<int[]> onArrayAlloc;

		Int(IdStrategy idStrat, int defVal, Consumer<int[]> onArrayAlloc) {
			super(idStrat);

			weights = IntArrays.EMPTY_ARRAY;
			defaultWeight = defVal;
			this.onArrayAlloc = Objects.requireNonNull(onArrayAlloc);
			onArrayAlloc.accept(weights);
			values = new AbstractIntList() {

				@Override
				public int size() {
					return DataContainer.Int.super.size();
				}

				@Override
				public IntListIterator iterator() {
					return IntIterators.wrap(weights, 0, size());
				}

				@Override
				public int getInt(int index) {
					checkIdx(index);
					return weights[index];
				}
			};
		}

		public int getInt(int idx) {
			checkIdx(idx);
			return weights[idx];
		}

		public void set(int idx, int weight) {
			checkIdx(idx);
			weights[idx] = weight;
		}

		public int defaultWeightInt() {
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

		@Override
		IntCollection values() {
			return values;
		}

		DataContainer.Int copy(IdStrategy idStrat, Consumer<int[]> onArrayAlloc) {
			if (idStrat.size() != this.idStrat.size())
				throw new IllegalArgumentException();
			DataContainer.Int copy = new DataContainer.Int(idStrat, defaultWeight, onArrayAlloc);
			copy.weights = Arrays.copyOf(weights, idStrat.size());
			onArrayAlloc.accept(copy.weights);
			return copy;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (!(other instanceof DataContainer.Int))
				return false;
			DataContainer.Int o = (DataContainer.Int) other;
			return Arrays.equals(weights, 0, size(), o.weights, 0, o.size());
		}
	}

	static class Long extends DataContainer.Abstract<java.lang.Long> {

		private long[] weights;
		private final long defaultWeight;
		private final LongCollection values = new AbstractLongList() {

			@Override
			public int size() {
				return DataContainer.Long.super.size();
			}

			@Override
			public LongListIterator iterator() {
				return LongIterators.wrap(weights, 0, size());
			}

			@Override
			public long getLong(int index) {
				checkIdx(index);
				return weights[index];
			}
		};
		private final Consumer<long[]> onArrayAlloc;

		Long(IdStrategy idStrat, long defVal, Consumer<long[]> onArrayAlloc) {
			super(idStrat);

			weights = LongArrays.EMPTY_ARRAY;
			defaultWeight = defVal;
			this.onArrayAlloc = Objects.requireNonNull(onArrayAlloc);
			onArrayAlloc.accept(weights);
		}

		Long(DataContainer.Long orig, IdStrategy idStrat, Consumer<long[]> onArrayAlloc) {
			super(idStrat);
			if (idStrat.size() != this.idStrat.size())
				throw new IllegalArgumentException();

			weights = Arrays.copyOf(orig.weights, idStrat.size());
			defaultWeight = orig.defaultWeight;
			this.onArrayAlloc = Objects.requireNonNull(onArrayAlloc);
			onArrayAlloc.accept(weights);
		}

		long getLong(int idx) {
			checkIdx(idx);
			return weights[idx];
		}

		void set(int idx, long weight) {
			checkIdx(idx);
			weights[idx] = weight;
		}

		long defaultWeightLong() {
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

		@Override
		LongCollection values() {
			return values;
		}

		DataContainer.Long copy(IdStrategy idStrat, Consumer<long[]> onArrayAlloc) {
			return new DataContainer.Long(this, idStrat, onArrayAlloc);
		}

		@Override
		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (!(other instanceof DataContainer.Long))
				return false;
			DataContainer.Long o = (DataContainer.Long) other;
			return Arrays.equals(weights, 0, size(), o.weights, 0, o.size());
		}
	}

	static class Manager {

		final List<DataContainer<?>> containers = new ObjectArrayList<>();
		private int containersCapacity;

		Manager(int initCapacity) {
			containersCapacity = initCapacity;
		}

		void addContainer(DataContainer<?> container) {
			containers.add(container);
			if (containersCapacity > ((DataContainer.Abstract<?>) container).capacity())
				container.expand(containersCapacity);
		}

		void ensureCapacity(int capacity) {
			if (capacity <= containersCapacity)
				return;
			int newCapacity = Math.max(Math.max(2, 2 * containersCapacity), capacity);
			for (DataContainer<?> container : containers)
				container.expand(newCapacity);
			containersCapacity = newCapacity;
		}

		// void swapElements(int idx1, int idx2) {
		// for (DataContainer<?> container : containers.values())
		// container.swap(idx1, idx2);
		// }

		// void clearElement(int idx) {
		// for (DataContainer<?> container : containers.values())
		// container.clear(idx);
		// }

		// void clearContainers() {
		// for (DataContainer<?> container : containers.values())
		// container.clear();
		// }
	}

}
