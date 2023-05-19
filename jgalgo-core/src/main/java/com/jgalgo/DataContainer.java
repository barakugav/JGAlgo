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

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;
import it.unimi.dsi.fastutil.booleans.AbstractBooleanList;
import it.unimi.dsi.fastutil.booleans.BooleanCollection;
import it.unimi.dsi.fastutil.booleans.BooleanListIterator;
import it.unimi.dsi.fastutil.bytes.AbstractByteList;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import it.unimi.dsi.fastutil.bytes.ByteCollection;
import it.unimi.dsi.fastutil.bytes.ByteIterators;
import it.unimi.dsi.fastutil.bytes.ByteListIterator;
import it.unimi.dsi.fastutil.chars.AbstractCharList;
import it.unimi.dsi.fastutil.chars.CharArrays;
import it.unimi.dsi.fastutil.chars.CharCollection;
import it.unimi.dsi.fastutil.chars.CharIterators;
import it.unimi.dsi.fastutil.chars.CharListIterator;
import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleIterators;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import it.unimi.dsi.fastutil.floats.AbstractFloatList;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.floats.FloatCollection;
import it.unimi.dsi.fastutil.floats.FloatIterators;
import it.unimi.dsi.fastutil.floats.FloatListIterator;
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
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import it.unimi.dsi.fastutil.shorts.AbstractShortList;
import it.unimi.dsi.fastutil.shorts.ShortArrays;
import it.unimi.dsi.fastutil.shorts.ShortCollection;
import it.unimi.dsi.fastutil.shorts.ShortIterators;
import it.unimi.dsi.fastutil.shorts.ShortListIterator;

abstract class DataContainer<E> {
	int size;

	void clear() {
		size = 0;
	}

	abstract void add(int idx);

	abstract void addUpTo(int endIdx);

	abstract void remove(int idx);

	abstract void swap(int i1, int i2);

	abstract Collection<E> values();

	abstract Class<E> getTypeClass();

	@Override
	public int hashCode() {
		return values().hashCode();
	}

	@Override
	public String toString() {
		return values().toString();
	}

	void checkIdx(int idx) {
		// TODO add some messege of ID strategy choice
		if (idx >= size)
			throw new IndexOutOfBoundsException(idx);
	}

	static <D> DataContainer<D> newInstance(Class<? super D> type, D defVal, int size) {
		@SuppressWarnings("rawtypes")
		DataContainer container;
		if (type == byte.class) {
			byte defVal0 = defVal != null ? ((java.lang.Byte) defVal).byteValue() : 0;
			container = new DataContainer.Byte(size, defVal0);

		} else if (type == short.class) {
			short defVal0 = defVal != null ? ((java.lang.Short) defVal).shortValue() : 0;
			container = new DataContainer.Short(size, defVal0);

		} else if (type == int.class) {
			int defVal0 = defVal != null ? ((Integer) defVal).intValue() : 0;
			container = new DataContainer.Int(size, defVal0);

		} else if (type == long.class) {
			long defVal0 = defVal != null ? ((java.lang.Long) defVal).longValue() : 0;
			container = new DataContainer.Long(size, defVal0);

		} else if (type == float.class) {
			float defVal0 = defVal != null ? ((java.lang.Float) defVal).floatValue() : 0;
			container = new DataContainer.Float(size, defVal0);

		} else if (type == double.class) {
			double defVal0 = defVal != null ? ((java.lang.Double) defVal).doubleValue() : 0;
			container = new DataContainer.Double(size, defVal0);

		} else if (type == boolean.class) {
			boolean defVal0 = defVal != null ? ((Boolean) defVal).booleanValue() : false;
			container = new DataContainer.Bool(size, defVal0);

		} else if (type == char.class) {
			char defVal0 = defVal != null ? ((Character) defVal).charValue() : 0;
			container = new DataContainer.Char(size, defVal0);

		} else {
			container = new DataContainer.Obj<>(size, defVal, type);
		}
		@SuppressWarnings("unchecked")
		DataContainer<D> container0 = container;
		return container0;
	}

	static class Obj<E> extends DataContainer<E> {

		private Object[] weights;
		private final E defaultVal;
		private final ObjectCollection<E> values;
		private final Class<E> type;

		Obj(int expectedSize, E defVal, Class<E> type) {
			weights = expectedSize > 0 ? new Object[expectedSize] : ObjectArrays.EMPTY_ARRAY;
			defaultVal = defVal;
			this.type = Objects.requireNonNull(type);
			values = new AbstractObjectList<>() {

				@Override
				public int size() {
					return size;
				}

				@SuppressWarnings({ "unchecked", "rawtypes" })
				@Override
				public ObjectListIterator<E> iterator() {
					return (ObjectListIterator) ObjectIterators.wrap(weights, 0, size);
				}

				@SuppressWarnings("unchecked")
				@Override
				public E get(int index) {
					checkIdx(index);
					return (E) weights[index];
				}
			};
		}

		@SuppressWarnings("unchecked")
		public E get(int idx) {
			checkIdx(idx);
			return (E) weights[idx];
		}

		void set(int idx, E weight) {
			checkIdx(idx);
			weights[idx] = weight;
		}

		E defaultVal() {
			return defaultVal;
		}

		private void add0(int idx) {
			assert idx == size : "only continues idxs are supported";
			ensureCapacity(size + 1);
			size++;
		}

		@Override
		void add(int idx) {
			add0(idx);
			weights[idx] = defaultVal;
		}

		void addWithoutSettingDefaultVal(int idx) {
			add0(idx);
			if (weights[idx] == null)
				weights[idx] = defaultVal;
		}

		@Override
		void remove(int idx) {
			assert idx == size - 1 : "only continues idxs are supported";
			weights[idx] = null;
			size--;
		}

		@Override
		void addUpTo(int endIdx) {
			if (endIdx < size)
				throw new IllegalArgumentException();
			ensureCapacity(endIdx);
			Arrays.fill(weights, size, endIdx, defaultVal);
			size = endIdx;
		}

		private void ensureCapacity(int capacity) {
			if (capacity < weights.length)
				return;
			int newLen = Math.max(2, weights.length * 2);
			newLen = Math.max(newLen, capacity);
			weights = Arrays.copyOf(weights, newLen);
		}

		@Override
		void swap(int i1, int i2) {
			checkIdx(i1);
			checkIdx(i2);
			ObjectArrays.swap(weights, i1, i2);
		}

		void clearWithoutDeallocation() {
			super.clear();
		}

		@Override
		void clear() {
			Arrays.fill(weights, 0, size, null);
			super.clear();
		}

		@Override
		Collection<E> values() {
			return values;
		}

		@Override
		Class<E> getTypeClass() {
			return type;
		}

		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof DataContainer.Obj<?>))
				return false;
			DataContainer.Obj<?> o = (DataContainer.Obj<?>) other;
			return Arrays.equals(weights, 0, size, o.weights, 0, o.size);
		}
	}

	static class Byte extends DataContainer<java.lang.Byte> {

		private byte[] weights;
		private final byte defaultVal;
		private final ByteCollection values;

		Byte(int expectedSize, byte defVal) {
			weights = expectedSize > 0 ? new byte[expectedSize] : ByteArrays.EMPTY_ARRAY;
			defaultVal = defVal;
			values = new AbstractByteList() {

				@Override
				public int size() {
					return size;
				}

				@Override
				public ByteListIterator iterator() {
					return ByteIterators.wrap(weights, 0, size);
				}

				@Override
				public byte getByte(int index) {
					checkIdx(index);
					return weights[index];
				}
			};
		}

		byte getByte(int idx) {
			checkIdx(idx);
			return weights[idx];
		}

		void set(int idx, byte weight) {
			checkIdx(idx);
			weights[idx] = weight;
		}

		byte defaultValByte() {
			return defaultVal;
		}

		@Override
		public void add(int idx) {
			assert idx == size : "only continues idxs are supported";
			ensureCapacity(size + 1);
			weights[idx] = defaultVal;
			size++;
		}

		@Override
		void remove(int idx) {
			assert idx == size - 1 : "only continues idxs are supported";
			size--;
		}

		@Override
		void addUpTo(int endIdx) {
			if (endIdx < size)
				throw new IllegalArgumentException();
			ensureCapacity(endIdx);
			Arrays.fill(weights, size, endIdx, defaultVal);
			size = endIdx;
		}

		private void ensureCapacity(int capacity) {
			if (capacity < weights.length)
				return;
			int newLen = Math.max(2, weights.length * 2);
			newLen = Math.max(newLen, capacity);
			weights = Arrays.copyOf(weights, newLen);
		}

		@Override
		void swap(int i1, int i2) {
			checkIdx(i1);
			checkIdx(i2);
			ByteArrays.swap(weights, i1, i2);
		}

		@Override
		ByteCollection values() {
			return values;
		}

		@Override
		Class<java.lang.Byte> getTypeClass() {
			return byte.class;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (!(other instanceof DataContainer.Byte))
				return false;
			DataContainer.Byte o = (DataContainer.Byte) other;
			return Arrays.equals(weights, 0, size, o.weights, 0, o.size);
		}
	}

	static class Short extends DataContainer<java.lang.Short> {

		private short[] weights;
		private final short defaultVal;
		private final ShortCollection values;

		Short(int expectedSize, short defVal) {
			weights = expectedSize > 0 ? new short[expectedSize] : ShortArrays.EMPTY_ARRAY;
			defaultVal = defVal;
			values = new AbstractShortList() {

				@Override
				public int size() {
					return size;
				}

				@Override
				public ShortListIterator iterator() {
					return ShortIterators.wrap(weights, 0, size);
				}

				@Override
				public short getShort(int index) {
					checkIdx(index);
					return weights[index];
				}
			};
		}

		short getShort(int idx) {
			checkIdx(idx);
			return weights[idx];
		}

		void set(int idx, short weight) {
			checkIdx(idx);
			weights[idx] = weight;
		}

		short defaultValShort() {
			return defaultVal;
		}

		@Override
		public void add(int idx) {
			assert idx == size : "only continues idxs are supported";
			ensureCapacity(size + 1);
			weights[idx] = defaultVal;
			size++;
		}

		@Override
		void remove(int idx) {
			assert idx == size - 1 : "only continues idxs are supported";
			size--;
		}

		@Override
		void addUpTo(int endIdx) {
			if (endIdx < size)
				throw new IllegalArgumentException();
			ensureCapacity(endIdx);
			Arrays.fill(weights, size, endIdx, defaultVal);
			size = endIdx;
		}

		private void ensureCapacity(int capacity) {
			if (capacity < weights.length)
				return;
			int newLen = Math.max(2, weights.length * 2);
			newLen = Math.max(newLen, capacity);
			weights = Arrays.copyOf(weights, newLen);
		}

		@Override
		void swap(int i1, int i2) {
			checkIdx(i1);
			checkIdx(i2);
			ShortArrays.swap(weights, i1, i2);
		}

		@Override
		ShortCollection values() {
			return values;
		}

		@Override
		Class<java.lang.Short> getTypeClass() {
			return short.class;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (!(other instanceof DataContainer.Short))
				return false;
			DataContainer.Short o = (DataContainer.Short) other;
			return Arrays.equals(weights, 0, size, o.weights, 0, o.size);
		}
	}

	static class Int extends DataContainer<Integer> {

		private int[] weights;
		private final int defaultVal;
		private final IntCollection values;

		Int(int expectedSize, int defVal) {
			weights = expectedSize > 0 ? new int[expectedSize] : IntArrays.EMPTY_ARRAY;
			defaultVal = defVal;
			values = new AbstractIntList() {

				@Override
				public int size() {
					return size;
				}

				@Override
				public IntListIterator iterator() {
					return IntIterators.wrap(weights, 0, size);
				}

				@Override
				public int getInt(int index) {
					checkIdx(index);
					return weights[index];
				}
			};
		}

		int getInt(int idx) {
			checkIdx(idx);
			return weights[idx];
		}

		void set(int idx, int weight) {
			checkIdx(idx);
			weights[idx] = weight;
		}

		int defaultValInt() {
			return defaultVal;
		}

		@Override
		void add(int idx) {
			assert idx == size : "only continues idxs are supported";
			ensureCapacity(size + 1);
			weights[idx] = defaultVal;
			size++;
		}

		@Override
		void remove(int idx) {
			assert idx == size - 1 : "only continues idxs are supported";
			size--;
		}

		@Override
		void addUpTo(int endIdx) {
			if (endIdx < size)
				throw new IllegalArgumentException();
			ensureCapacity(endIdx);
			Arrays.fill(weights, size, endIdx, defaultVal);
			size = endIdx;
		}

		private void ensureCapacity(int capacity) {
			if (capacity < weights.length)
				return;
			int newLen = Math.max(2, weights.length * 2);
			newLen = Math.max(newLen, capacity);
			weights = Arrays.copyOf(weights, newLen);
		}

		@Override
		void swap(int i1, int i2) {
			checkIdx(i1);
			checkIdx(i2);
			IntArrays.swap(weights, i1, i2);
		}

		@Override
		IntCollection values() {
			return values;
		}

		@Override
		Class<Integer> getTypeClass() {
			return int.class;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (!(other instanceof DataContainer.Int))
				return false;
			DataContainer.Int o = (DataContainer.Int) other;
			return Arrays.equals(weights, 0, size, o.weights, 0, o.size);
		}
	}

	static class Long extends DataContainer<java.lang.Long> {

		private long[] weights;
		private final long defaultVal;
		private final LongCollection values;

		Long(int expectedSize, long defVal) {
			weights = expectedSize > 0 ? new long[expectedSize] : LongArrays.EMPTY_ARRAY;
			defaultVal = defVal;
			values = new AbstractLongList() {

				@Override
				public int size() {
					return size;
				}

				@Override
				public LongListIterator iterator() {
					return LongIterators.wrap(weights, 0, size);
				}

				@Override
				public long getLong(int index) {
					checkIdx(index);
					return weights[index];
				}
			};
		}

		long getLong(int idx) {
			checkIdx(idx);
			return weights[idx];
		}

		void set(int idx, long weight) {
			checkIdx(idx);
			weights[idx] = weight;
		}

		long defaultValLong() {
			return defaultVal;
		}

		@Override
		public void add(int idx) {
			assert idx == size : "only continues idxs are supported";
			ensureCapacity(size + 1);
			weights[idx] = defaultVal;
			size++;
		}

		@Override
		void remove(int idx) {
			assert idx == size - 1 : "only continues idxs are supported";
			size--;
		}

		@Override
		void addUpTo(int endIdx) {
			if (endIdx < size)
				throw new IllegalArgumentException();
			ensureCapacity(endIdx);
			Arrays.fill(weights, size, endIdx, defaultVal);
			size = endIdx;
		}

		private void ensureCapacity(int capacity) {
			if (capacity < weights.length)
				return;
			int newLen = Math.max(2, weights.length * 2);
			newLen = Math.max(newLen, capacity);
			weights = Arrays.copyOf(weights, newLen);
		}

		@Override
		void swap(int i1, int i2) {
			checkIdx(i1);
			checkIdx(i2);
			LongArrays.swap(weights, i1, i2);
		}

		@Override
		LongCollection values() {
			return values;
		}

		@Override
		Class<java.lang.Long> getTypeClass() {
			return long.class;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (!(other instanceof DataContainer.Long))
				return false;
			DataContainer.Long o = (DataContainer.Long) other;
			return Arrays.equals(weights, 0, size, o.weights, 0, o.size);
		}
	}

	static class Float extends DataContainer<java.lang.Float> {

		private float[] weights;
		private final float defaultVal;
		private final FloatCollection values;

		Float(int expectedSize, float defVal) {
			weights = expectedSize > 0 ? new float[expectedSize] : FloatArrays.EMPTY_ARRAY;
			defaultVal = defVal;
			values = new AbstractFloatList() {

				@Override
				public int size() {
					return size;
				}

				@Override
				public FloatListIterator iterator() {
					return FloatIterators.wrap(weights, 0, size);
				}

				@Override
				public float getFloat(int index) {
					checkIdx(index);
					return weights[index];
				}
			};
		}

		float getFloat(int idx) {
			checkIdx(idx);
			return weights[idx];
		}

		void set(int idx, float weight) {
			checkIdx(idx);
			weights[idx] = weight;
		}

		float defaultValFloat() {
			return defaultVal;
		}

		@Override
		void add(int idx) {
			assert idx == size : "only continues idxs are supported";
			ensureCapacity(size + 1);
			weights[idx] = defaultVal;
			size++;
		}

		@Override
		void remove(int idx) {
			assert idx == size - 1 : "only continues idxs are supported";
			size--;
		}

		@Override
		void addUpTo(int endIdx) {
			if (endIdx < size)
				throw new IllegalArgumentException();
			ensureCapacity(endIdx);
			Arrays.fill(weights, size, endIdx, defaultVal);
			size = endIdx;
		}

		private void ensureCapacity(int capacity) {
			if (capacity < weights.length)
				return;
			int newLen = Math.max(2, weights.length * 2);
			newLen = Math.max(newLen, capacity);
			weights = Arrays.copyOf(weights, newLen);
		}

		@Override
		void swap(int i1, int i2) {
			checkIdx(i1);
			checkIdx(i2);
			FloatArrays.swap(weights, i1, i2);
		}

		@Override
		FloatCollection values() {
			return values;
		}

		@Override
		Class<java.lang.Float> getTypeClass() {
			return float.class;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (!(other instanceof DataContainer.Float))
				return false;
			DataContainer.Float o = (DataContainer.Float) other;
			return Arrays.equals(weights, 0, size, o.weights, 0, o.size);
		}
	}

	static class Double extends DataContainer<java.lang.Double> {

		private double[] weights;
		private final double defaultVal;
		private final DoubleCollection values;

		Double(int expectedSize, double defVal) {
			weights = expectedSize > 0 ? new double[expectedSize] : DoubleArrays.EMPTY_ARRAY;
			defaultVal = defVal;
			values = new AbstractDoubleList() {

				@Override
				public int size() {
					return size;
				}

				@Override
				public DoubleListIterator iterator() {
					return DoubleIterators.wrap(weights, 0, size);
				}

				@Override
				public double getDouble(int index) {
					checkIdx(index);
					return weights[index];
				}
			};
		}

		double getDouble(int idx) {
			checkIdx(idx);
			return weights[idx];
		}

		void set(int idx, double weight) {
			checkIdx(idx);
			weights[idx] = weight;
		}

		double defaultValDouble() {
			return defaultVal;
		}

		@Override
		void add(int idx) {
			assert idx == size : "only continues idxs are supported";
			ensureCapacity(size + 1);
			weights[idx] = defaultVal;
			size++;
		}

		@Override
		void remove(int idx) {
			assert idx == size - 1 : "only continues idxs are supported";
			size--;
		}

		@Override
		void addUpTo(int endIdx) {
			if (endIdx < size)
				throw new IllegalArgumentException();
			ensureCapacity(endIdx);
			Arrays.fill(weights, size, endIdx, defaultVal);
			size = endIdx;
		}

		private void ensureCapacity(int capacity) {
			if (capacity < weights.length)
				return;
			int newLen = Math.max(2, weights.length * 2);
			newLen = Math.max(newLen, capacity);
			weights = Arrays.copyOf(weights, newLen);
		}

		@Override
		void swap(int i1, int i2) {
			checkIdx(i1);
			checkIdx(i2);
			DoubleArrays.swap(weights, i1, i2);
		}

		@Override
		DoubleCollection values() {
			return values;
		}

		@Override
		Class<java.lang.Double> getTypeClass() {
			return double.class;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (!(other instanceof DataContainer.Double))
				return false;
			DataContainer.Double o = (DataContainer.Double) other;
			return Arrays.equals(weights, 0, size, o.weights, 0, o.size);
		}
	}

	static class Bool extends DataContainer<Boolean> {

		private final BitSet weights;
		private final boolean defaultVal;
		private final BooleanCollection values;

		Bool(int expectedSize, boolean defVal) {
			// We don't do anything with expectedSize, but we keep it for forward
			// compatibility
			weights = new BitSet();
			defaultVal = defVal;
			values = new AbstractBooleanList() {

				@Override
				public int size() {
					return size;
				}

				@Override
				public BooleanListIterator iterator() {
					return new BooleanListIterator() {
						int idx = 0;

						@Override
						public boolean hasNext() {
							return idx < size;
						}

						@Override
						public boolean nextBoolean() {
							if (!hasNext())
								throw new NoSuchElementException();
							return weights.get(idx++);
						}

						@Override
						public boolean previousBoolean() {
							if (!hasPrevious())
								throw new NoSuchElementException();
							return weights.get(--idx);
						}

						@Override
						public boolean hasPrevious() {
							return idx > 0;
						}

						@Override
						public int nextIndex() {
							return idx;
						}

						@Override
						public int previousIndex() {
							return idx - 1;
						}
					};
				}

				@Override
				public boolean getBoolean(int index) {
					checkIdx(index);
					return weights.get(index);
				}
			};
		}

		boolean getBool(int idx) {
			checkIdx(idx);
			return weights.get(idx);
		}

		void set(int idx, boolean weight) {
			checkIdx(idx);
			weights.set(idx, weight);
		}

		boolean defaultValBool() {
			return defaultVal;
		}

		@Override
		void add(int idx) {
			assert idx == size : "only continues idxs are supported";
			weights.set(idx, defaultVal);
			size++;
		}

		@Override
		void remove(int idx) {
			assert idx == size - 1 : "only continues idxs are supported";
			size--;
		}

		@Override
		void addUpTo(int endIdx) {
			if (endIdx < size)
				throw new IllegalArgumentException();
			weights.set(size, endIdx, defaultVal);
			size = endIdx;
		}

		@Override
		void swap(int i1, int i2) {
			checkIdx(i1);
			checkIdx(i2);
			boolean temp = weights.get(i1);
			weights.set(i1, weights.get(i2));
			weights.set(i2, temp);
		}

		@Override
		BooleanCollection values() {
			return values;
		}

		@Override
		Class<Boolean> getTypeClass() {
			return boolean.class;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (!(other instanceof DataContainer.Bool))
				return false;
			DataContainer.Bool o = (DataContainer.Bool) other;
			return size == o.size && weights.equals(o.weights);
		}
	}

	static class Char extends DataContainer<Character> {

		private char[] weights;
		private final char defaultVal;
		private final CharCollection values;

		Char(int expectedSize, char defVal) {
			weights = expectedSize > 0 ? new char[expectedSize] : CharArrays.EMPTY_ARRAY;
			defaultVal = defVal;
			values = new AbstractCharList() {

				@Override
				public int size() {
					return size;
				}

				@Override
				public CharListIterator iterator() {
					return CharIterators.wrap(weights, 0, size);
				}

				@Override
				public char getChar(int index) {
					checkIdx(index);
					return weights[index];
				}
			};
		}

		char getChar(int idx) {
			checkIdx(idx);
			return weights[idx];
		}

		void set(int idx, char weight) {
			checkIdx(idx);
			weights[idx] = weight;
		}

		char defaultValChar() {
			return defaultVal;
		}

		@Override
		void add(int idx) {
			assert idx == size : "only continues idxs are supported";
			ensureCapacity(size + 1);
			weights[idx] = defaultVal;
			size++;
		}

		@Override
		void remove(int idx) {
			assert idx == size - 1 : "only continues idxs are supported";
			size--;
		}

		@Override
		void addUpTo(int endIdx) {
			if (endIdx < size)
				throw new IllegalArgumentException();
			ensureCapacity(endIdx);
			Arrays.fill(weights, size, endIdx, defaultVal);
			size = endIdx;
		}

		private void ensureCapacity(int capacity) {
			if (capacity < weights.length)
				return;
			int newLen = Math.max(2, weights.length * 2);
			newLen = Math.max(newLen, capacity);
			weights = Arrays.copyOf(weights, newLen);
		}

		@Override
		void swap(int i1, int i2) {
			checkIdx(i1);
			checkIdx(i2);
			CharArrays.swap(weights, i1, i2);
		}

		@Override
		CharCollection values() {
			return values;
		}

		@Override
		Class<Character> getTypeClass() {
			return char.class;
		}

		@Override
		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (!(other instanceof DataContainer.Char))
				return false;
			DataContainer.Char o = (DataContainer.Char) other;
			return Arrays.equals(weights, 0, size, o.weights, 0, o.size);
		}
	}

}
