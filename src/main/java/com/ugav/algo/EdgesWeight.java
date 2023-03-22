package com.ugav.algo;

import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;

public abstract class EdgesWeight<E> {

	EdgesWeight() {
	}

	public abstract E get(int e);

	public abstract void set(int e, E data);

	public abstract E defaultVal();

	public abstract void setDefaultVal(E e);

	public abstract DataIter<E> iterator();

	public abstract void clear();

	abstract void edgeAdd(int e);

	abstract void edgeRemove(int e);

	abstract void edgeSwap(int e1, int e2);

	public static interface DataIter<E> {

		public boolean hasNext();

		public int nextEdge();

		public E getData();

		public void setData(E val);

		public static interface Int extends DataIter<Integer> {

			public int getDataInt();

			@Deprecated
			@Override
			default Integer getData() {
				return Integer.valueOf(getDataInt());
			}

			public void setData(int val);

			@Deprecated
			@Override
			default void setData(Integer val) {
				setData(val.intValue());
			}

		}

		public static interface Double extends DataIter<java.lang.Double> {

			public double getDataDouble();

			@Deprecated
			@Override
			default java.lang.Double getData() {
				return java.lang.Double.valueOf(getDataDouble());
			}

			public void setData(double val);

			@Deprecated
			@Override
			default void setData(java.lang.Double val) {
				setData(val.doubleValue());
			}

		}

	}

	public static class Obj<E> extends EdgesWeight<E> {

		private Object[] data;
		private int size;
		private E defaultVal = null;
		private static final Object[] EmptyData = new Object[0];
		private boolean isComparable;

		public Obj() {
			this(0);
		}

		public Obj(int expectedSize) {
			data = expectedSize > 0 ? new Object[expectedSize] : EmptyData;
			size = 0;
		}

		@SuppressWarnings("unchecked")
		@Override
		public E get(int e) {
			checkEdgeIdx(e);
			return (E) data[e];
		}

		@Override
		public void set(int e, E val) {
			checkEdgeIdx(e);
			data[e] = val;
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
		public DataIter<E> iterator() {
			return new DataItr();
		}

		@Override
		void edgeAdd(int e) {
			assert e == size : "only continues edges IDs are supported";
			if (size >= data.length)
				data = Arrays.copyOf(data, Math.max(2, data.length * 2));
			data[e] = defaultVal;
			size++;
		}

		@Override
		void edgeRemove(int e) {
			assert e == size - 1 : "only continues edges IDs are supported";
			data[e] = null;
			size--;
		}

		@Override
		void edgeSwap(int e1, int e2) {
			checkEdgeIdx(e1);
			checkEdgeIdx(e2);
			Object temp = data[e1];
			data[e1] = data[e2];
			data[e2] = temp;
		}

		@Override
		public void clear() {
			Arrays.fill(data, 0, size, null);
			size = 0;
		}

		private void checkEdgeIdx(int e) {
			if (e >= size)
				throw new IndexOutOfBoundsException(e);
		}

		/**
		 * TODO
		 *
		 * Set this to true if your data implement the Comparable interface to help
		 * Graph.equals maintain an order in parallel edges
		 *
		 * @param comparable
		 */
		public void setComparable(boolean comparable) {
			isComparable = comparable;
		}

		boolean isComparable() {
			return isComparable;
		}

		private class DataItr extends DataIterAbstract implements DataIter<E> {

			DataItr() {
				super(size);
			}

			@SuppressWarnings("unchecked")
			@Override
			public E getData() {
				return (E) data[idx];
			}

			@Override
			public void setData(E val) {
				data[idx] = val;
			}
		}

		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof EdgesWeight.Obj<?>))
				return false;
			EdgesWeight.Obj<?> o = (EdgesWeight.Obj<?>) other;

			return Arrays.equals(data, 0, size, o.data, 0, o.size, (d1, d2) -> Objects.equals(d1, d2) ? 0 : 1);
		}

		@Override
		public int hashCode() {
			int h = 1;
			for (int i = 0; i < size; i++)
				h = 31 * h + Objects.hashCode(data[i]);
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
				b.append(String.valueOf(data[i]));
				if (i == iMax)
					return b.append(']').toString();
				b.append(", ");
			}
		}

	}

	public static class Int extends EdgesWeight<Integer> implements EdgeWeightFunc.Int {

		private int[] data;
		private int size;
		private int defaultVal = -1;
		private static final int[] EmptyData = new int[0];

		public Int() {
			this(0);
		}

		public Int(int expectedSize) {
			data = expectedSize > 0 ? new int[expectedSize] : EmptyData;
			size = 0;
		}

		public int getInt(int e) {
			checkEdgeIdx(e);
			return data[e];
		}

		@Deprecated
		@Override
		public Integer get(int e) {
			return Integer.valueOf(getInt(e));
		}

		public void set(int e, int val) {
			checkEdgeIdx(e);
			data[e] = val;
		}

		@Deprecated
		@Override
		public void set(int e, Integer data) {
			set(e, data.intValue());
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
		public DataIter.Int iterator() {
			return new DataItr();
		}

		@Override
		void edgeAdd(int e) {
			assert e == size : "only continues edges IDs are supported";
			if (size >= data.length)
				data = Arrays.copyOf(data, Math.max(2, data.length * 2));
			data[e] = defaultVal;
			size++;
		}

		@Override
		void edgeRemove(int e) {
			assert e == size - 1 : "only continues edges IDs are supported";
			size--;
		}

		@Override
		void edgeSwap(int e1, int e2) {
			checkEdgeIdx(e1);
			checkEdgeIdx(e2);
			int temp = data[e1];
			data[e1] = data[e2];
			data[e2] = temp;
		}

		@Override
		public void clear() {
			size = 0;
		}

		private void checkEdgeIdx(int e) {
			if (e >= size)
				throw new IndexOutOfBoundsException(e);
		}

		@Override
		public int weightInt(int e) {
			return getInt(e);
		}

		private class DataItr extends DataIterAbstract implements DataIter.Int {

			DataItr() {
				super(size);
			}

			@Override
			public int getDataInt() {
				return data[idx];
			}

			@Override
			public void setData(int val) {
				data[idx] = val;
			}
		}

		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof EdgesWeight.Int))
				return false;
			EdgesWeight.Int o = (EdgesWeight.Int) other;

			return Arrays.equals(data, 0, size, o.data, 0, o.size);
		}

		@Override
		public int hashCode() {
			int h = 1;
			for (int i = 0; i < size; i++)
				h = 31 * h + data[i];
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
				b.append(String.valueOf(data[i]));
				if (i == iMax)
					return b.append(']').toString();
				b.append(", ");
			}
		}

	}

	public static class Double extends EdgesWeight<java.lang.Double> implements EdgeWeightFunc {

		private double[] data;
		private int size;
		private double defaultVal = -1;
		private static final double[] EmptyData = new double[0];

		public Double() {
			this(0);
		}

		public Double(int expectedSize) {
			data = expectedSize > 0 ? new double[expectedSize] : EmptyData;
			size = 0;
		}

		public double getDouble(int e) {
			checkEdgeIdx(e);
			return data[e];
		}

		@Deprecated
		@Override
		public java.lang.Double get(int e) {
			return java.lang.Double.valueOf(getDouble(e));
		}

		public void set(int e, double val) {
			checkEdgeIdx(e);
			data[e] = val;
		}

		@Deprecated
		@Override
		public void set(int e, java.lang.Double data) {
			set(e, data.doubleValue());
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
		public DataIter.Double iterator() {
			return new DataItr();
		}

		@Override
		void edgeAdd(int e) {
			assert e == size : "only continues edges IDs are supported";
			if (size >= data.length)
				data = Arrays.copyOf(data, Math.max(2, data.length * 2));
			data[e] = defaultVal;
			size++;
		}

		@Override
		void edgeRemove(int e) {
			assert e == size - 1 : "only continues edges IDs are supported";
			size--;
		}

		@Override
		void edgeSwap(int e1, int e2) {
			checkEdgeIdx(e1);
			checkEdgeIdx(e2);
			double temp = data[e1];
			data[e1] = data[e2];
			data[e2] = temp;
		}

		@Override
		public void clear() {
			size = 0;
		}

		private void checkEdgeIdx(int e) {
			if (e >= size)
				throw new IndexOutOfBoundsException(e);
		}

		@Override
		public double weight(int e) {
			return getDouble(e);
		}

		private class DataItr extends DataIterAbstract implements DataIter.Double {

			DataItr() {
				super(size);
			}

			@Override
			public double getDataDouble() {
				return data[idx];
			}

			@Override
			public void setData(double val) {
				data[idx] = val;
			}
		}

		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof EdgesWeight.Double))
				return false;
			EdgesWeight.Double o = (EdgesWeight.Double) other;

			return Arrays.equals(data, 0, size, o.data, 0, o.size);
		}

		@Override
		public int hashCode() {
			int h = 1;
			for (int i = 0; i < size; i++)
				h = 31 * h + java.lang.Double.hashCode(data[i]);
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
				b.append(String.valueOf(data[i]));
				if (i == iMax)
					return b.append(']').toString();
				b.append(", ");
			}
		}

	}

	private static class DataIterAbstract {

		private final int maxIdx;
		int idx;

		DataIterAbstract(int size) {
			this.maxIdx = size - 1;
			idx = -1;
		}

		public boolean hasNext() {
			return idx < maxIdx;
		}

		public int nextEdge() {
			if (!hasNext())
				throw new NoSuchElementException();
			return ++idx;
		}
	}
}