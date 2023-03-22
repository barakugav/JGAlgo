package com.ugav.algo;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;

import it.unimi.dsi.fastutil.ints.IntIterator;

public abstract class GraphWeights<E> {

	GraphWeights() {
	}

	public abstract E get(int key);

	public abstract void set(int key, E data);

	public abstract E defaultVal();

	public abstract void setDefaultVal(E defVal);

	public abstract WeightIter<E> iterator();

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

	}

	public static class Obj<E> extends GraphWeights<E> {

		private Object[] weights;
		private int size;
		private E defaultVal = null;
		private static final Object[] EmptyWeights = new Object[0];
		private boolean isComparable;

		public Obj() {
			this(0);
		}

		public Obj(int expectedSize) {
			weights = expectedSize > 0 ? new Object[expectedSize] : EmptyWeights;
			size = 0;
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
		public void setDefaultVal(E defVal) {
			defaultVal = defVal;
		}

		@Override
		public WeightIter<E> iterator() {
			return new WeightItr();
		}

		@Override
		void keyAdd(int key) {
			assert key == size : "only continues keys are supporte";
			if (size >= weights.length)
				weights = Arrays.copyOf(weights, Math.max(2, weights.length * 2));
			weights[key] = defaultVal;
			size++;
		}

		@Override
		void keyRemove(int key) {
			assert key == size - 1 : "only continues keys are supporte";
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

		private class WeightItr extends WeightIterAbstract implements WeightIter<E> {

			WeightItr() {
				super(size);
			}

			@SuppressWarnings("unchecked")
			@Override
			public E getWeight() {
				return (E) weights[idx];
			}

			@Override
			public void setWeight(E weight) {
				weights[idx] = weight;
			}
		}

		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof GraphWeights.Obj<?>))
				return false;
			GraphWeights.Obj<?> o = (GraphWeights.Obj<?>) other;

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

	public static class Int extends GraphWeights<Integer> implements EdgeWeightFunc.Int {

		private int[] weights;
		private int size;
		private int defaultVal = -1;
		private static final int[] EmptyWeights = new int[0];

		public Int() {
			this(0);
		}

		public Int(int expectedSize) {
			weights = expectedSize > 0 ? new int[expectedSize] : EmptyWeights;
			size = 0;
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

		public void setDefaultVal(int defVal) {
			defaultVal = defVal;
		}

		@Deprecated
		@Override
		public void setDefaultVal(Integer defVal) {
			defaultVal = defVal.intValue();
		}

		@Override
		public WeightIter.Int iterator() {
			return new WeightItr();
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
			assert key == size - 1 : "only continues keys are supporte";
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

		private class WeightItr extends WeightIterAbstract implements WeightIter.Int {

			WeightItr() {
				super(size);
			}

			@Override
			public int getWeightInt() {
				return weights[idx];
			}

			@Override
			public void setWeight(int weight) {
				weights[idx] = weight;
			}
		}

		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof GraphWeights.Int))
				return false;
			GraphWeights.Int o = (GraphWeights.Int) other;

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

	public static class Double extends GraphWeights<java.lang.Double> implements EdgeWeightFunc {

		private double[] weights;
		private int size;
		private double defaultVal = -1;
		private static final double[] EmptyWeights = new double[0];

		public Double() {
			this(0);
		}

		public Double(int expectedSize) {
			weights = expectedSize > 0 ? new double[expectedSize] : EmptyWeights;
			size = 0;
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

		public void setDefaultVal(double defVal) {
			defaultVal = defVal;
		}

		@Deprecated
		@Override
		public void setDefaultVal(java.lang.Double defVal) {
			defaultVal = defVal.doubleValue();
		}

		@Override
		public WeightIter.Double iterator() {
			return new WeightItr();
		}

		@Override
		void keyAdd(int key) {
			assert key == size : "only continues keys are supporte";
			if (size >= weights.length)
				weights = Arrays.copyOf(weights, Math.max(2, weights.length * 2));
			weights[key] = defaultVal;
			size++;
		}

		@Override
		void keyRemove(int key) {
			assert key == size - 1 : "only continues keys are supporte";
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

		private class WeightItr extends WeightIterAbstract implements WeightIter.Double {

			WeightItr() {
				super(size);
			}

			@Override
			public double getWeightDouble() {
				return weights[idx];
			}

			@Override
			public void setWeight(double weight) {
				weights[idx] = weight;
			}
		}

		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof GraphWeights.Double))
				return false;
			GraphWeights.Double o = (GraphWeights.Double) other;

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

	private static class WeightIterAbstract implements IntIterator {

		private final int maxIdx;
		int idx;

		WeightIterAbstract(int size) {
			this.maxIdx = size - 1;
			idx = -1;
		}

		@Override
		public boolean hasNext() {
			return idx < maxIdx;
		}

		@Override
		public int nextInt() {
			if (!hasNext())
				throw new NoSuchElementException();
			return ++idx;
		}
	}

}