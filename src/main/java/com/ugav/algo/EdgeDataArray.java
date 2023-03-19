package com.ugav.algo;

import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Graph.WeightFunctionInt;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public class EdgeDataArray {

	private EdgeDataArray() {
	}

	public static class Obj<E> implements EdgeData<E> {

		private final ObjectArrayList<E> data;
		// TODO replace with primitive array
		// TODO default value get set

		public Obj() {
			this(0);
		}

		public Obj(int expectedSize) {
			data = new ObjectArrayList<>(expectedSize);
		}

		private E defVal() {
			return null;
		}

		@Override
		public E get(int e) {
			return data.size() < e ? data.get(e) : defVal();
		}

		@Override
		public void set(int e, E data) {
			this.data.ensureCapacity(e + 1);
			while (this.data.size() <= e)
				this.data.add(defVal());
			this.data.set(e, data);
		}

		@Override
		public void clear() {
			data.clear();
		}
	}

	public static class Int implements EdgeData.Int, WeightFunctionInt {

		private final IntArrayList data;
		// TODO replace with primitive array
		// TODO default value get set
		private static final int DefVal = -1;

		public Int() {
			this(0);
		}

		public Int(int expectedSize) {
			data = new IntArrayList(expectedSize);
		}

		@Override
		public int getInt(int e) {
			return data.size() < e ? data.getInt(e) : DefVal;
		}

		@Override
		public void set(int e, int data) {
			this.data.ensureCapacity(e + 1);
			while (this.data.size() <= e)
				this.data.add(DefVal);
			this.data.set(e, data);
		}

		@Override
		public void clear() {
			data.clear();
		}

	}

	public static class Double implements EdgeData.Double, WeightFunction {

		private final DoubleArrayList data;
		// TODO replace with primitive array
		// TODO default value get set
		private static final double DefVal = -1;

		public Double() {
			this(0);
		}

		public Double(int expectedSize) {
			data = new DoubleArrayList(expectedSize);
		}

		@Override
		public double getDouble(int e) {
			return data.size() < e ? data.getDouble(e) : DefVal;
		}

		@Override
		public void set(int e, double data) {
			this.data.ensureCapacity(e + 1);
			while (this.data.size() <= e)
				this.data.add(DefVal);
			this.data.set(e, data);
		}

		@Override
		public void clear() {
			data.clear();
		}
	}

}
