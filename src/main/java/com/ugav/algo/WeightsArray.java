package com.ugav.algo;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Objects;

class WeightsArray {

	private WeightsArray() {
	}

	private static abstract class Abstract<E> extends WeightsAbstract<E> {
		int size;

		Abstract(boolean isEdges) {
			super(isEdges);
		}

		void checkKey(int key) {
			if (key >= size)
				throw new IndexOutOfBoundsException(key);
		}
	}

	static class Obj<E> extends Abstract<E> implements Weights<E> {

		private Object[] weights;
		private final E defaultVal;
		private static final Object[] EmptyWeights = new Object[0];

		private Obj(boolean isEdges, int expectedSize, E defVal) {
			super(isEdges);
			weights = expectedSize > 0 ? new Object[expectedSize] : EmptyWeights;
			defaultVal = defVal;
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
			size = 0;
		}

		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof WeightsArray.Obj<?>))
				return false;
			WeightsArray.Obj<?> o = (WeightsArray.Obj<?>) other;

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

	static class Int extends Abstract<Integer> implements Weights.Int, EdgeWeightFunc.Int {

		private int[] weights;
		private final int defaultVal;
		private static final int[] EmptyWeights = new int[0];

		private Int(boolean isEdges, int expectedSize, int defVal) {
			super(isEdges);
			weights = expectedSize > 0 ? new int[expectedSize] : EmptyWeights;
			defaultVal = defVal;
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
		public void clear() {
			size = 0;
		}

		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof WeightsArray.Int))
				return false;
			WeightsArray.Int o = (WeightsArray.Int) other;

			// TODO equals with keys collection input

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

	static class Double extends Abstract<java.lang.Double> implements Weights.Double, EdgeWeightFunc {

		private double[] weights;
		private final double defaultVal;
		private static final double[] EmptyWeights = new double[0];

		private Double(boolean isEdges, int expectedSize, double defVal) {
			super(isEdges);
			weights = expectedSize > 0 ? new double[expectedSize] : EmptyWeights;
			defaultVal = defVal;
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
		public void clear() {
			size = 0;
		}

		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof WeightsArray.Double))
				return false;
			WeightsArray.Double o = (WeightsArray.Double) other;

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

	static class Bool extends Abstract<Boolean> implements Weights.Bool {

		private final BitSet weights;
		private final boolean defaultVal;

		private Bool(boolean isEdges, int expectedSize, boolean defVal) {
			// We don't do anything with expectedSize, but we keep it for forward
			// compatibility
			super(isEdges);
			weights = new BitSet();
			defaultVal = defVal;
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
		public void clear() {
			size = 0;
		}

		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof WeightsArray.Bool))
				return false;
			WeightsArray.Bool o = (WeightsArray.Bool) other;

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
