package com.jgalgo;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;

import it.unimi.dsi.fastutil.booleans.AbstractBooleanCollection;
import it.unimi.dsi.fastutil.booleans.BooleanCollection;
import it.unimi.dsi.fastutil.booleans.BooleanIterator;
import it.unimi.dsi.fastutil.bytes.AbstractByteCollection;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import it.unimi.dsi.fastutil.bytes.ByteCollection;
import it.unimi.dsi.fastutil.bytes.ByteIterator;
import it.unimi.dsi.fastutil.chars.AbstractCharCollection;
import it.unimi.dsi.fastutil.chars.CharArrays;
import it.unimi.dsi.fastutil.chars.CharCollection;
import it.unimi.dsi.fastutil.chars.CharIterator;
import it.unimi.dsi.fastutil.doubles.AbstractDoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.floats.AbstractFloatCollection;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.floats.FloatCollection;
import it.unimi.dsi.fastutil.floats.FloatIterator;
import it.unimi.dsi.fastutil.ints.AbstractIntCollection;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.longs.AbstractLongCollection;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.AbstractObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.fastutil.shorts.AbstractShortCollection;
import it.unimi.dsi.fastutil.shorts.ShortArrays;
import it.unimi.dsi.fastutil.shorts.ShortCollection;
import it.unimi.dsi.fastutil.shorts.ShortIterator;

abstract class DataContainer<E> {
	int size;

	void clear() {
		size = 0;
	}

	abstract void add(int idx);

	abstract void remove(int idx);

	abstract void ensureCapacity(int size);

	abstract void swap(int i1, int i2);

	abstract Collection<E> values();

	void checkIdx(int idx) {
		// TODO add some messege of ID strategy choice
		if (idx >= size)
			throw new IndexOutOfBoundsException(idx);
	}

	static class Obj<E> extends DataContainer<E> {

		private Object[] weights;
		private final E defaultVal;
		private final ObjectCollection<E> values;

		Obj(int expectedSize, E defVal) {
			weights = expectedSize > 0 ? new Object[expectedSize] : ObjectArrays.EMPTY_ARRAY;
			defaultVal = defVal;
			values = new AbstractObjectCollection<>() {

				@Override
				public int size() {
					return size;
				}

				@Override
				public ObjectIterator<E> iterator() {
					return new ObjectIterator<>() {
						int idx = 0;

						@Override
						public boolean hasNext() {
							return idx < size;
						}

						@SuppressWarnings("unchecked")
						@Override
						public E next() {
							if (!hasNext())
								throw new NoSuchElementException();
							return (E) weights[idx++];
						}
					};
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
			weights[idx] = null;
			size--;
		}

		@Override
		void ensureCapacity(int capacity) {
			if (capacity < weights.length)
				return;
			int newLen = Math.max(2, weights.length * 2);
			newLen = Math.max(newLen, capacity);
			weights = Arrays.copyOf(weights, newLen);
		}

		@Override
		void swap(int k1, int k2) {
			checkIdx(k1);
			checkIdx(k2);
			Object temp = weights[k1];
			weights[k1] = weights[k2];
			weights[k2] = temp;
		}

		@Override
		public void clear() {
			Arrays.fill(weights, 0, size, null);
			super.clear();
		}

		@Override
		Collection<E> values() {
			return values;
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

		@Override
		public int hashCode() {
			int h = 1;
			for (int i = 0; i < size; i++)
				h = 31 * h + Objects.hashCode(weights[i]);
			return h;
		}

		@Override
		public String toString() {
			int iMax = size - 1;
			if (iMax == -1)
				return "[]";

			StringBuilder b = new StringBuilder();
			b.append('[');
			for (int i = 0;; i++) {
				b.append(String.valueOf(weights[i]));
				if (i == iMax)
					return b.append(']').toString();
				b.append(", ");
			}
		}
	}

	static class Byte extends DataContainer<java.lang.Byte> {

		private byte[] weights;
		private final byte defaultVal;
		private final ByteCollection values;

		Byte(int expectedSize, byte defVal) {
			weights = expectedSize > 0 ? new byte[expectedSize] : ByteArrays.EMPTY_ARRAY;
			defaultVal = defVal;
			values = new AbstractByteCollection() {

				@Override
				public int size() {
					return size;
				}

				@Override
				public ByteIterator iterator() {
					return new ByteIterator() {
						int idx = 0;

						@Override
						public boolean hasNext() {
							return idx < size;
						}

						@Override
						public byte nextByte() {
							if (!hasNext())
								throw new NoSuchElementException();
							return weights[idx++];
						}
					};
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
		void ensureCapacity(int capacity) {
			if (capacity < weights.length)
				return;
			int newLen = Math.max(2, weights.length * 2);
			newLen = Math.max(newLen, capacity);
			weights = Arrays.copyOf(weights, newLen);
		}

		@Override
		void swap(int k1, int k2) {
			checkIdx(k1);
			checkIdx(k2);
			byte temp = weights[k1];
			weights[k1] = weights[k2];
			weights[k2] = temp;
		}

		@Override
		ByteCollection values() {
			return values;
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

		@Override
		public int hashCode() {
			int h = 1;
			for (int i = 0; i < size; i++)
				h = 31 * h + java.lang.Byte.hashCode(weights[i]);
			return h;
		}

		@Override
		public String toString() {
			int iMax = size - 1;
			if (iMax == -1)
				return "[]";

			StringBuilder b = new StringBuilder();
			b.append('[');
			for (int i = 0;; i++) {
				b.append(String.valueOf(weights[i]));
				if (i == iMax)
					return b.append(']').toString();
				b.append(", ");
			}
		}
	}

	static class Short extends DataContainer<java.lang.Short> {

		private short[] weights;
		private final short defaultVal;
		private final ShortCollection values;

		Short(int expectedSize, short defVal) {
			weights = expectedSize > 0 ? new short[expectedSize] : ShortArrays.EMPTY_ARRAY;
			defaultVal = defVal;
			values = new AbstractShortCollection() {

				@Override
				public int size() {
					return size;
				}

				@Override
				public ShortIterator iterator() {
					return new ShortIterator() {
						int idx = 0;

						@Override
						public boolean hasNext() {
							return idx < size;
						}

						@Override
						public short nextShort() {
							if (!hasNext())
								throw new NoSuchElementException();
							return weights[idx++];
						}
					};
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
		void ensureCapacity(int capacity) {
			if (capacity < weights.length)
				return;
			int newLen = Math.max(2, weights.length * 2);
			newLen = Math.max(newLen, capacity);
			weights = Arrays.copyOf(weights, newLen);
		}

		@Override
		void swap(int k1, int k2) {
			checkIdx(k1);
			checkIdx(k2);
			short temp = weights[k1];
			weights[k1] = weights[k2];
			weights[k2] = temp;
		}

		@Override
		ShortCollection values() {
			return values;
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

		@Override
		public int hashCode() {
			int h = 1;
			for (int i = 0; i < size; i++)
				h = 31 * h + java.lang.Short.hashCode(weights[i]);
			return h;
		}

		@Override
		public String toString() {
			int iMax = size - 1;
			if (iMax == -1)
				return "[]";

			StringBuilder b = new StringBuilder();
			b.append('[');
			for (int i = 0;; i++) {
				b.append(String.valueOf(weights[i]));
				if (i == iMax)
					return b.append(']').toString();
				b.append(", ");
			}
		}
	}

	static class Int extends DataContainer<Integer> {

		private int[] weights;
		private final int defaultVal;
		private final IntCollection values;

		Int(int expectedSize, int defVal) {
			weights = expectedSize > 0 ? new int[expectedSize] : IntArrays.EMPTY_ARRAY;
			defaultVal = defVal;
			values = new AbstractIntCollection() {

				@Override
				public int size() {
					return size;
				}

				@Override
				public IntIterator iterator() {
					return new IntIterator() {
						int idx = 0;

						@Override
						public boolean hasNext() {
							return idx < size;
						}

						@Override
						public int nextInt() {
							if (!hasNext())
								throw new NoSuchElementException();
							return weights[idx++];
						}
					};
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
		void ensureCapacity(int capacity) {
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
			int temp = weights[i1];
			weights[i1] = weights[i2];
			weights[i2] = temp;
		}

		@Override
		IntCollection values() {
			return values;
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

		@Override
		public int hashCode() {
			int h = 1;
			for (int i = 0; i < size; i++)
				h = 31 * h + weights[i];
			return h;
		}

		@Override
		public String toString() {
			int iMax = size - 1;
			if (iMax == -1)
				return "[]";

			StringBuilder b = new StringBuilder();
			b.append('[');
			for (int i = 0;; i++) {
				b.append(String.valueOf(weights[i]));
				if (i == iMax)
					return b.append(']').toString();
				b.append(", ");
			}
		}
	}

	static class Long extends DataContainer<java.lang.Long> {

		private long[] weights;
		private final long defaultVal;
		private final LongCollection values;

		Long(int expectedSize, long defVal) {
			weights = expectedSize > 0 ? new long[expectedSize] : LongArrays.EMPTY_ARRAY;
			defaultVal = defVal;
			values = new AbstractLongCollection() {

				@Override
				public int size() {
					return size;
				}

				@Override
				public LongIterator iterator() {
					return new LongIterator() {
						int idx = 0;

						@Override
						public boolean hasNext() {
							return idx < size;
						}

						@Override
						public long nextLong() {
							if (!hasNext())
								throw new NoSuchElementException();
							return weights[idx++];
						}
					};
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
		void ensureCapacity(int capacity) {
			if (capacity < weights.length)
				return;
			int newLen = Math.max(2, weights.length * 2);
			newLen = Math.max(newLen, capacity);
			weights = Arrays.copyOf(weights, newLen);
		}

		@Override
		void swap(int k1, int k2) {
			checkIdx(k1);
			checkIdx(k2);
			long temp = weights[k1];
			weights[k1] = weights[k2];
			weights[k2] = temp;
		}

		@Override
		LongCollection values() {
			return values;
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

		@Override
		public int hashCode() {
			int h = 1;
			for (int i = 0; i < size; i++)
				h = 31 * h + java.lang.Long.hashCode(weights[i]);
			return h;
		}

		@Override
		public String toString() {
			int iMax = size - 1;
			if (iMax == -1)
				return "[]";

			StringBuilder b = new StringBuilder();
			b.append('[');
			for (int i = 0;; i++) {
				b.append(String.valueOf(weights[i]));
				if (i == iMax)
					return b.append(']').toString();
				b.append(", ");
			}
		}
	}

	static class Float extends DataContainer<java.lang.Float> {

		private float[] weights;
		private final float defaultVal;
		private final FloatCollection values;

		Float(int expectedSize, float defVal) {
			weights = expectedSize > 0 ? new float[expectedSize] : FloatArrays.EMPTY_ARRAY;
			defaultVal = defVal;
			values = new AbstractFloatCollection() {

				@Override
				public int size() {
					return size;
				}

				@Override
				public FloatIterator iterator() {
					return new FloatIterator() {
						int idx = 0;

						@Override
						public boolean hasNext() {
							return idx < size;
						}

						@Override
						public float nextFloat() {
							if (!hasNext())
								throw new NoSuchElementException();
							return weights[idx++];
						}
					};
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
		void ensureCapacity(int capacity) {
			if (capacity < weights.length)
				return;
			int newLen = Math.max(2, weights.length * 2);
			newLen = Math.max(newLen, capacity);
			weights = Arrays.copyOf(weights, newLen);
		}

		@Override
		void swap(int k1, int k2) {
			checkIdx(k1);
			checkIdx(k2);
			float temp = weights[k1];
			weights[k1] = weights[k2];
			weights[k2] = temp;
		}

		@Override
		FloatCollection values() {
			return values;
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

		@Override
		public int hashCode() {
			int h = 1;
			for (int i = 0; i < size; i++)
				h = 31 * h + java.lang.Float.hashCode(weights[i]);
			return h;
		}

		@Override
		public String toString() {
			int iMax = size - 1;
			if (iMax == -1)
				return "[]";

			StringBuilder b = new StringBuilder();
			b.append('[');
			for (int i = 0;; i++) {
				b.append(String.valueOf(weights[i]));
				if (i == iMax)
					return b.append(']').toString();
				b.append(", ");
			}
		}
	}

	static class Double extends DataContainer<java.lang.Double> {

		private double[] weights;
		private final double defaultVal;
		private final DoubleCollection values;

		Double(int expectedSize, double defVal) {
			weights = expectedSize > 0 ? new double[expectedSize] : DoubleArrays.EMPTY_ARRAY;
			defaultVal = defVal;
			values = new AbstractDoubleCollection() {

				@Override
				public int size() {
					return size;
				}

				@Override
				public DoubleIterator iterator() {
					return new DoubleIterator() {
						int idx = 0;

						@Override
						public boolean hasNext() {
							return idx < size;
						}

						@Override
						public double nextDouble() {
							if (!hasNext())
								throw new NoSuchElementException();
							return weights[idx++];
						}
					};
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
		void ensureCapacity(int capacity) {
			if (capacity < weights.length)
				return;
			int newLen = Math.max(2, weights.length * 2);
			newLen = Math.max(newLen, capacity);
			weights = Arrays.copyOf(weights, newLen);
		}

		@Override
		void swap(int k1, int k2) {
			checkIdx(k1);
			checkIdx(k2);
			double temp = weights[k1];
			weights[k1] = weights[k2];
			weights[k2] = temp;
		}

		@Override
		DoubleCollection values() {
			return values;
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

		@Override
		public int hashCode() {
			int h = 1;
			for (int i = 0; i < size; i++)
				h = 31 * h + java.lang.Double.hashCode(weights[i]);
			return h;
		}

		@Override
		public String toString() {
			int iMax = size - 1;
			if (iMax == -1)
				return "[]";

			StringBuilder b = new StringBuilder();
			b.append('[');
			for (int i = 0;; i++) {
				b.append(String.valueOf(weights[i]));
				if (i == iMax)
					return b.append(']').toString();
				b.append(", ");
			}
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
			values = new AbstractBooleanCollection() {

				@Override
				public int size() {
					return size;
				}

				@Override
				public BooleanIterator iterator() {
					return new BooleanIterator() {
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
					};
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
		void ensureCapacity(int capacity) {
		}

		@Override
		void swap(int k1, int k2) {
			checkIdx(k1);
			checkIdx(k2);
			boolean temp = weights.get(k1);
			weights.set(k1, weights.get(k2));
			weights.set(k2, temp);
		}

		@Override
		BooleanCollection values() {
			return values;
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

		@Override
		public int hashCode() {
			return size * 1237 ^ weights.hashCode();
		}

		@Override
		public String toString() {
			int iMax = size - 1;
			if (iMax == -1)
				return "[]";

			StringBuilder b = new StringBuilder();
			b.append('[');
			for (int i = 0;; i++) {
				b.append(String.valueOf(weights.get(i)));
				if (i == iMax)
					return b.append(']').toString();
				b.append(", ");
			}
		}
	}

	static class Char extends DataContainer<Character> {

		private char[] weights;
		private final char defaultVal;
		private final CharCollection values;

		Char(int expectedSize, char defVal) {
			weights = expectedSize > 0 ? new char[expectedSize] : CharArrays.EMPTY_ARRAY;
			defaultVal = defVal;
			values = new AbstractCharCollection() {

				@Override
				public int size() {
					return size;
				}

				@Override
				public CharIterator iterator() {
					return new CharIterator() {
						int idx = 0;

						@Override
						public boolean hasNext() {
							return idx < size;
						}

						@Override
						public char nextChar() {
							if (!hasNext())
								throw new NoSuchElementException();
							return weights[idx++];
						}
					};
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
		void ensureCapacity(int capacity) {
			if (capacity < weights.length)
				return;
			int newLen = Math.max(2, weights.length * 2);
			newLen = Math.max(newLen, capacity);
			weights = Arrays.copyOf(weights, newLen);
		}

		@Override
		void swap(int k1, int k2) {
			checkIdx(k1);
			checkIdx(k2);
			char temp = weights[k1];
			weights[k1] = weights[k2];
			weights[k2] = temp;
		}

		@Override
		CharCollection values() {
			return values;
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

		@Override
		public int hashCode() {
			int h = 1;
			for (int i = 0; i < size; i++)
				h = 31 * h + Character.hashCode(weights[i]);
			return h;
		}

		@Override
		public String toString() {
			int iMax = size - 1;
			if (iMax == -1)
				return "[]";

			StringBuilder b = new StringBuilder();
			b.append('[');
			for (int i = 0;; i++) {
				b.append(String.valueOf(weights[i]));
				if (i == iMax)
					return b.append(']').toString();
				b.append(", ");
			}
		}
	}

}
