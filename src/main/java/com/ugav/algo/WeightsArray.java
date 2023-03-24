package com.ugav.algo;

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
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.longs.AbstractLongCollection;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.objects.AbstractObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

class WeightsArray {

	private WeightsArray() {
	}

	private static abstract class Abstract<E> extends WeightsAbstract<E> {
		int size;
		private final IntSet keysSet;

		Abstract(boolean isEdges) {
			super(isEdges);
			keysSet = new AbstractIntSet() {

				@Override
				public int size() {
					return size;
				}

				@Override
				public boolean contains(int key) {
					return key >= 0 && key < size();
				}

				@Override
				public IntIterator iterator() {
					return new IntIterator() {
						int u = 0;

						@Override
						public boolean hasNext() {
							return u < size();
						}

						@Override
						public int nextInt() {
							if (!hasNext())
								throw new NoSuchElementException();
							return u++;
						}
					};
				}
			};
		}

		@Override
		IntSet keysSet() {
			return keysSet;
		}

		@Override
		public void clear() {
			size = 0;
		}

		void checkKey(int key) {
			if (key >= size)
				throw new IndexOutOfBoundsException(key);
		}
	}

	static class Obj<E> extends Abstract<E> implements Weights<E> {

		private Object[] weights;
		private final E defaultVal;
		private final ObjectCollection<E> values;
		private static final Object[] EmptyWeights = new Object[0];

		private Obj(boolean isEdges, int expectedSize, E defVal) {
			super(isEdges);
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

		static <E> Weights<E> ofEdges(int expectedSize, E defVal) {
			return new WeightsArray.Obj<>(true, expectedSize, defVal);
		}

		static <E> Weights<E> ofVertices(int expectedSize, E defVal) {
			return new WeightsArray.Obj<>(false, expectedSize, defVal);
		}

		@SuppressWarnings("unchecked")
		@Override
		public E get(int key) {
			checkKey(key);
			return (E) weights[key];
		}

		@Override
		public void set(int key, E weight) {
			checkKey(key);
			weights[key] = weight;
		}

		@Override
		public E defaultVal() {
			return defaultVal;
		}

		@Override
		void keyAdd(int key) {
			assert key == size : "only continues keys are supported";
			if (size >= weights.length)
				weights = Arrays.copyOf(weights, Math.max(2, weights.length * 2));
			weights[key] = defaultVal;
			size++;
		}

		@Override
		void keyRemove(int key) {
			assert key == size - 1 : "only continues keys are supported";
			weights[key] = null;
			size--;
		}

		@Override
		void keySwap(int k1, int k2) {
			checkKey(k1);
			checkKey(k2);
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
			return other == this || (other instanceof WeightsArray.Obj<?> o
					&& Arrays.equals(weights, 0, size, o.weights, 0, o.size));
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

	static class Int extends Abstract<Integer> implements Weights.Int {

		private int[] weights;
		private final int defaultVal;
		private final IntCollection values;
		private static final int[] EmptyWeights = new int[0];

		private Int(boolean isEdges, int expectedSize, int defVal) {
			super(isEdges);
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

		static Weights.Int ofEdges(int expectedSize, int defVal) {
			return new WeightsArray.Int(true, expectedSize, defVal);
		}

		static Weights.Int ofVertices(int expectedSize, int defVal) {
			return new WeightsArray.Int(false, expectedSize, defVal);
		}

		@Override
		public int getInt(int key) {
			checkKey(key);
			return weights[key];
		}

		@Override
		public void set(int key, int weight) {
			checkKey(key);
			weights[key] = weight;
		}

		@Override
		public int defaultValInt() {
			return defaultVal;
		}

		@Override
		void keyAdd(int key) {
			assert key == size : "only continues keys are supported";
			if (size >= weights.length)
				weights = Arrays.copyOf(weights, Math.max(2, weights.length * 2));
			weights[key] = defaultVal;
			size++;
		}

		@Override
		void keyRemove(int key) {
			assert key == size - 1 : "only continues keys are supported";
			size--;
		}

		@Override
		void keySwap(int k1, int k2) {
			checkKey(k1);
			checkKey(k2);
			int temp = weights[k1];
			weights[k1] = weights[k2];
			weights[k2] = temp;
		}

		@Override
		IntCollection values() {
			return values;
		}

		@Override
		public boolean equals(Object other) {
			// TODO equals with keys collection input
			return other == this
					|| (other instanceof WeightsArray.Int o && Arrays.equals(weights, 0, size, o.weights, 0, o.size));
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

	static class Long extends Abstract<java.lang.Long> implements Weights.Long {

		private long[] weights;
		private final long defaultVal;
		private final LongCollection values;
		private static final long[] EmptyWeights = new long[0];

		private Long(boolean isEdges, int expectedSize, long defVal) {
			super(isEdges);
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

		static Weights.Long ofEdges(int expectedSize, long defVal) {
			return new WeightsArray.Long(true, expectedSize, defVal);
		}

		static Weights.Long ofVertices(int expectedSize, long defVal) {
			return new WeightsArray.Long(false, expectedSize, defVal);
		}

		@Override
		public long getLong(int key) {
			checkKey(key);
			return weights[key];
		}

		@Override
		public void set(int key, long weight) {
			checkKey(key);
			weights[key] = weight;
		}

		@Override
		public long defaultValLong() {
			return defaultVal;
		}

		@Override
		void keyAdd(int key) {
			assert key == size : "only continues keys are supported";
			if (size >= weights.length)
				weights = Arrays.copyOf(weights, Math.max(2, weights.length * 2));
			weights[key] = defaultVal;
			size++;
		}

		@Override
		void keyRemove(int key) {
			assert key == size - 1 : "only continues keys are supported";
			size--;
		}

		@Override
		void keySwap(int k1, int k2) {
			checkKey(k1);
			checkKey(k2);
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
			return other == this
					|| (other instanceof WeightsArray.Long o && Arrays.equals(weights, 0, size, o.weights, 0, o.size));
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

	static class Double extends Abstract<java.lang.Double> implements Weights.Double {

		private double[] weights;
		private final double defaultVal;
		private final DoubleCollection values;
		private static final double[] EmptyWeights = new double[0];

		private Double(boolean isEdges, int expectedSize, double defVal) {
			super(isEdges);
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

		static Weights.Double ofEdges(int expectedSize, double defVal) {
			return new WeightsArray.Double(true, expectedSize, defVal);
		}

		static Weights.Double ofVertices(int expectedSize, double defVal) {
			return new WeightsArray.Double(false, expectedSize, defVal);
		}

		@Override
		public double getDouble(int key) {
			checkKey(key);
			return weights[key];
		}

		@Override
		public void set(int key, double weight) {
			checkKey(key);
			weights[key] = weight;
		}

		@Override
		public double defaultValDouble() {
			return defaultVal;
		}

		@Override
		void keyAdd(int key) {
			assert key == size : "only continues keys are supported";
			if (size >= weights.length)
				weights = Arrays.copyOf(weights, Math.max(2, weights.length * 2));
			weights[key] = defaultVal;
			size++;
		}

		@Override
		void keyRemove(int key) {
			assert key == size - 1 : "only continues keys are supported";
			size--;
		}

		@Override
		void keySwap(int k1, int k2) {
			checkKey(k1);
			checkKey(k2);
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
			return other == this || (other instanceof WeightsArray.Double o
					&& Arrays.equals(weights, 0, size, o.weights, 0, o.size));
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

	static class Bool extends Abstract<Boolean> implements Weights.Bool {

		private final BitSet weights;
		private final boolean defaultVal;
		private final BooleanCollection values;

		private Bool(boolean isEdges, int expectedSize, boolean defVal) {
			// We don't do anything with expectedSize, but we keep it for forward
			// compatibility
			super(isEdges);
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

		static Weights.Bool ofEdges(int expectedSize, boolean defVal) {
			return new WeightsArray.Bool(true, expectedSize, defVal);
		}

		static Weights.Bool ofVertices(int expectedSize, boolean defVal) {
			return new WeightsArray.Bool(false, expectedSize, defVal);
		}

		@Override
		public boolean getBool(int key) {
			checkKey(key);
			return weights.get(key);
		}

		@Override
		public void set(int key, boolean weight) {
			checkKey(key);
			weights.set(key, weight);
		}

		@Override
		public boolean defaultValBool() {
			return defaultVal;
		}

		@Override
		void keyAdd(int key) {
			assert key == size : "only continues keys are supported";
			weights.set(key, defaultVal);
			size++;
		}

		@Override
		void keyRemove(int key) {
			assert key == size - 1 : "only continues keys are supported";
			size--;
		}

		@Override
		void keySwap(int k1, int k2) {
			checkKey(k1);
			checkKey(k2);
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
			return other == this
					|| (other instanceof WeightsArray.Bool o && size == o.size && weights.equals(o.weights));
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
