package com.jgalgo;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;

import it.unimi.dsi.fastutil.booleans.AbstractBooleanCollection;
import it.unimi.dsi.fastutil.booleans.BooleanCollection;
import it.unimi.dsi.fastutil.booleans.BooleanIterator;
import it.unimi.dsi.fastutil.doubles.AbstractDoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleIterator;
import it.unimi.dsi.fastutil.ints.AbstractIntCollection;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.longs.AbstractLongCollection;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.AbstractObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

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
		private static final Object[] EmptyWeights = new Object[0];

		Obj(int expectedSize, E defVal) {
			weights = expectedSize > 0 ? new Object[expectedSize] : EmptyWeights;
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

		public void set(int idx, E weight) {
			checkIdx(idx);
			weights[idx] = weight;
		}

		public E defaultVal() {
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

	static class Int extends DataContainer<Integer> {

		private int[] weights;
		private final int defaultVal;
		private final IntCollection values;
		private static final int[] EmptyWeights = new int[0];

		Int(int expectedSize, int defVal) {
			weights = expectedSize > 0 ? new int[expectedSize] : EmptyWeights;
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

		public int getInt(int idx) {
			checkIdx(idx);
			return weights[idx];
		}

		public void set(int idx, int weight) {
			checkIdx(idx);
			weights[idx] = weight;
		}

		public int defaultValInt() {
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
		private static final long[] EmptyWeights = new long[0];

		Long(int expectedSize, long defVal) {
			weights = expectedSize > 0 ? new long[expectedSize] : EmptyWeights;
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

		public long getLong(int idx) {
			checkIdx(idx);
			return weights[idx];
		}

		public void set(int idx, long weight) {
			checkIdx(idx);
			weights[idx] = weight;
		}

		public long defaultValLong() {
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

	static class Double extends DataContainer<java.lang.Double> {

		private double[] weights;
		private final double defaultVal;
		private final DoubleCollection values;
		private static final double[] EmptyWeights = new double[0];

		Double(int expectedSize, double defVal) {
			weights = expectedSize > 0 ? new double[expectedSize] : EmptyWeights;
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

		public double getDouble(int idx) {
			checkIdx(idx);
			return weights[idx];
		}

		public void set(int idx, double weight) {
			checkIdx(idx);
			weights[idx] = weight;
		}

		public double defaultValDouble() {
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

		public boolean getBool(int idx) {
			checkIdx(idx);
			return weights.get(idx);
		}

		public void set(int idx, boolean weight) {
			checkIdx(idx);
			weights.set(idx, weight);
		}

		public boolean defaultValBool() {
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

}
