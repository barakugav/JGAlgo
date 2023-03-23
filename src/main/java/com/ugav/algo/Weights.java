package com.ugav.algo;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;

import it.unimi.dsi.fastutil.ints.IntIterator;

public abstract class Weights<E> {

	final boolean isEdges;

	Weights(boolean isEdges) {
		this.isEdges = isEdges;
	}

	public abstract E get(int key);

	public abstract void set(int key, E data);

	public abstract E defaultVal();

	public abstract void clear();

	abstract void keyAdd(int key);

	abstract void keyRemove(int key);

	abstract void keySwap(int k1, int k2);

	public static interface WeightIter<E> extends IntIterator {

		public E getWeight();

		public void setWeight(E weight);

		public static interface Int extends WeightIter<Integer> {

			public int getWeightInt();

			@Deprecated
			@Override
			default Integer getWeight() {
				return Integer.valueOf(getWeightInt());
			}

			public void setWeight(int weight);

			@Deprecated
			@Override
			default void setWeight(Integer weight) {
				setWeight(weight.intValue());
			}
		}

		public static interface Double extends WeightIter<java.lang.Double> {

			public double getWeightDouble();

			@Deprecated
			@Override
			default java.lang.Double getWeight() {
				return java.lang.Double.valueOf(getWeightDouble());
			}

			public void setWeight(double weight);

			@Deprecated
			@Override
			default void setWeight(java.lang.Double weight) {
				setWeight(weight.doubleValue());
			}
		}

		public static interface Bool extends WeightIter<Boolean> {

			public boolean getWeightBool();

			@Deprecated
			@Override
			default Boolean getWeight() {
				return Boolean.valueOf(getWeightBool());
			}

			public void setWeight(boolean weight);

			@Deprecated
			@Override
			default void setWeight(Boolean weight) {
				setWeight(weight.booleanValue());
			}
		}

	}

	public static class Obj<E> extends Weights<E> {

		private Object[] weights;
		private int size;
		private final E defaultVal;
		private static final Object[] EmptyWeights = new Object[0];
		private boolean isComparable;

		private Obj(boolean isEdges, int expectedSize, E defVal) {
			super(isEdges);
			weights = expectedSize > 0 ? new Object[expectedSize] : EmptyWeights;
			size = 0;
			defaultVal = defVal;
		}

		static <E> Weights.Obj<E> ofEdges(int expectedSize, E defVal) {
			return new Weights.Obj<>(true, expectedSize, defVal);
		}

		static <E> Weights.Obj<E> ofVertices(int expectedSize, E defVal) {
			return new Weights.Obj<>(false, expectedSize, defVal);
		}

		@SuppressWarnings("unchecked")
		@Override
		public E get(int key) {
			return key < size ? (E) weights[key] : defaultVal;
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
			size = 0;
		}

		private void checkKey(int key) {
			if (key >= size)
				throw new IndexOutOfBoundsException(key);
		}

		/**
		 * TODO
		 *
		 * Set this to true if your weight implement the Comparable interface to help
		 * {@link com.ugav.algo.Graph#equals(Object)} maintain an order in parallel
		 * edges.
		 *
		 * @param comparable
		 */
		public void setComparable(boolean comparable) {
			isComparable = comparable;
		}

		boolean isComparable() {
			return isComparable;
		}

		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof Weights.Obj<?>))
				return false;
			Weights.Obj<?> o = (Weights.Obj<?>) other;

			return Arrays.equals(weights, 0, size, o.weights, 0, o.size, (d1, d2) -> Objects.equals(d1, d2) ? 0 : 1);
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

	public static class Int extends Weights<Integer> implements EdgeWeightFunc.Int {

		private int[] weights;
		private int size;
		private final int defaultVal;
		private static final int[] EmptyWeights = new int[0];

		private Int(boolean isEdges, int expectedSize, int defVal) {
			super(isEdges);
			weights = expectedSize > 0 ? new int[expectedSize] : EmptyWeights;
			size = 0;
			defaultVal = defVal;
		}

		static Weights.Int ofEdges(int expectedSize, int defVal) {
			return new Weights.Int(true, expectedSize, defVal);
		}

		static Weights.Int ofVertices(int expectedSize, int defVal) {
			return new Weights.Int(false, expectedSize, defVal);
		}

		public int getInt(int key) {
			checkKey(key);
			return weights[key];
		}

		@Deprecated
		@Override
		public Integer get(int key) {
			return Integer.valueOf(getInt(key));
		}

		public void set(int key, int weight) {
			checkKey(key);
			weights[key] = weight;
		}

		@Deprecated
		@Override
		public void set(int key, Integer data) {
			set(key, data.intValue());
		}

		public int defaultValInt() {
			return defaultVal;
		}

		@Deprecated
		@Override
		public Integer defaultVal() {
			return Integer.valueOf(defaultValInt());
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
		public void clear() {
			size = 0;
		}

		private void checkKey(int key) {
			if (key >= size)
				throw new IndexOutOfBoundsException(key);
		}

		@Override
		public int weightInt(int key) {
			return getInt(key);
		}

		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof Weights.Int))
				return false;
			Weights.Int o = (Weights.Int) other;

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

	public static class Double extends Weights<java.lang.Double> implements EdgeWeightFunc {

		private double[] weights;
		private int size;
		private final double defaultVal;
		private static final double[] EmptyWeights = new double[0];

		private Double(boolean isEdges, int expectedSize, double defVal) {
			super(isEdges);
			weights = expectedSize > 0 ? new double[expectedSize] : EmptyWeights;
			size = 0;
			defaultVal = defVal;
		}

		static Weights.Double ofEdges(int expectedSize, double defVal) {
			return new Weights.Double(true, expectedSize, defVal);
		}

		static Weights.Double ofVertices(int expectedSize, double defVal) {
			return new Weights.Double(false, expectedSize, defVal);
		}

		public double getDouble(int key) {
			checkKey(key);
			return weights[key];
		}

		@Deprecated
		@Override
		public java.lang.Double get(int key) {
			return java.lang.Double.valueOf(getDouble(key));
		}

		public void set(int key, double weight) {
			checkKey(key);
			weights[key] = weight;
		}

		@Deprecated
		@Override
		public void set(int key, java.lang.Double data) {
			set(key, data.doubleValue());
		}

		public double defaultValDouble() {
			return defaultVal;
		}

		@Deprecated
		@Override
		public java.lang.Double defaultVal() {
			return java.lang.Double.valueOf(defaultValDouble());
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
		public void clear() {
			size = 0;
		}

		private void checkKey(int key) {
			if (key >= size)
				throw new IndexOutOfBoundsException(key);
		}

		@Override
		public double weight(int key) {
			return getDouble(key);
		}

		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof Weights.Double))
				return false;
			Weights.Double o = (Weights.Double) other;

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

	public static class Bool extends Weights<Boolean> {

		private final BitSet weights;
		private final boolean defaultVal;
		private int size;

		private Bool(boolean isEdges, int expectedSize, boolean defVal) {
			// We don't do anything with expectedSize, but we keep it for forward
			// compatibility
			super(isEdges);
			weights = new BitSet();
			size = 0;
			defaultVal = defVal;
		}

		static Weights.Bool ofEdges(int expectedSize, boolean defVal) {
			return new Weights.Bool(true, expectedSize, defVal);
		}

		static Weights.Bool ofVertices(int expectedSize, boolean defVal) {
			return new Weights.Bool(false, expectedSize, defVal);
		}

		public boolean getBool(int key) {
			checkKey(key);
			return weights.get(key);
		}

		@Deprecated
		@Override
		public Boolean get(int key) {
			return Boolean.valueOf(getBool(key));
		}

		public void set(int key, boolean weight) {
			checkKey(key);
			weights.set(key, weight);
		}

		@Deprecated
		@Override
		public void set(int key, Boolean data) {
			set(key, data.booleanValue());
		}

		public boolean defaultValBool() {
			return defaultVal;
		}

		@Deprecated
		@Override
		public Boolean defaultVal() {
			return Boolean.valueOf(defaultValBool());
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
		public void clear() {
			size = 0;
		}

		private void checkKey(int key) {
			if (key >= size)
				throw new IndexOutOfBoundsException(key);
		}

		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof Weights.Bool))
				return false;
			Weights.Bool o = (Weights.Bool) other;

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