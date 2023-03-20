package com.ugav.algo;

import java.util.NoSuchElementException;

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

		@Override
		public DataIter<E> iterator() {
			return new DataItr();
		}

		private class DataItr extends DataIterAbstract implements DataIter<E> {

			DataItr() {
				super(data.size());
			}

			@Override
			public E getData() {
				return data.get(idx);
			}

			@Override
			public void setData(E val) {
				data.set(idx, val);
			}
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

		@Override
		public DataIter.Int iterator() {
			return new DataItr();
		}

		private class DataItr extends DataIterAbstract implements DataIter.Int {

			DataItr() {
				super(data.size());
			}

			@Override
			public int getDataInt() {
				return data.getInt(idx);
			}

			@Override
			public void setData(int val) {
				data.set(idx, val);
			}
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

		@Override
		public DataIter.Double iterator() {
			return new DataItr();
		}

		private class DataItr extends DataIterAbstract implements DataIter.Double {

			DataItr() {
				super(data.size());
			}

			@Override
			public double getDataDouble() {
				return data.getDouble(idx);
			}

			@Override
			public void setData(double val) {
				data.set(idx, val);
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

	public static class Builder implements EdgeData.Builder {

		private Builder() {
		}

		private static final Builder Instace = new Builder();

		public static EdgeData.Builder getInstance() {
			return Instace;
		}

		@Override
		public <E> EdgeData<E> ofObjs() {
			return new EdgeDataArray.Obj<>();
		}

		@Override
		public EdgeData.Int ofInts() {
			return new EdgeDataArray.Int();
		}

		@Override
		public EdgeData.Double ofDoubles() {
			return new EdgeDataArray.Double();
		}

	}

//	private static class IdxIterator implements IntIterator {
//		private final int maxIdx;
//		int idx;
//
//		IdxIterator(int size) {
//			this.maxIdx = size - 1;
//			idx = -1;
//		}
//
//		@Override
//		public boolean hasNext() {
//			return idx < maxIdx;
//		}
//
//		@Override
//		public int nextInt() {
//			if (!hasNext())
//				throw new NoSuchElementException();
//			return ++idx;
//		}
//	}

}
