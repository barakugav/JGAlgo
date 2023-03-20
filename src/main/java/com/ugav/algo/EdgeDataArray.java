package com.ugav.algo;

import java.util.Arrays;
import java.util.NoSuchElementException;

import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Graph.WeightFunctionInt;

public class EdgeDataArray {

	private EdgeDataArray() {
	}

	public static class Obj<E> implements EdgeData<E> {

		private Object[] data;
		private int size;
		// TODO default value get set
		private static final Object DefVal = null;
		private static final Object[] EmptyData = new Object[0];

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
			return (E) (e < size ? data[e] : DefVal);
		}

		@Override
		public void set(int e, E val) {
			if (e >= data.length)
				data = Arrays.copyOf(data, Math.max(2, data.length * 2));
			data[e] = val;
			size = Math.max(size, e + 1); // TODO
		}

		@Override
		public void clear() {
			Arrays.fill(data, 0, size, null);
			size = 0;
		}

		@Override
		public DataIter<E> iterator() {
			return new DataItr();
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
	}

	public static class Int implements EdgeData.Int, WeightFunctionInt {

		private int[] data;
		private int size;
		// TODO default value get set
		private static final int DefVal = -1;
		private static final int[] EmptyData = new int[0];

		public Int() {
			this(0);
		}

		public Int(int expectedSize) {
			data = expectedSize > 0 ? new int[expectedSize] : EmptyData;
			size = 0;
		}

		@Override
		public int getInt(int e) {
			return e < size ? data[e] : DefVal;
		}

		@Override
		public void set(int e, int val) {
			if (e >= data.length)
				data = Arrays.copyOf(data, Math.max(2, data.length * 2));
			data[e] = val;
			size = Math.max(size, e + 1); // TODO
		}

		@Override
		public void clear() {
			size = 0;
		}

		@Override
		public DataIter.Int iterator() {
			return new DataItr();
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
	}

	public static class Double implements EdgeData.Double, WeightFunction {

		private double[] data;
		private int size;
		// TODO default value get set
		private static final double DefVal = -1;
		private static final double[] EmptyData = new double[0];

		public Double() {
			this(0);
		}

		public Double(int expectedSize) {
			data = expectedSize > 0 ? new double[expectedSize] : EmptyData;
			size = 0;
		}

		@Override
		public double getDouble(int e) {
			return e < size ? data[e] : DefVal;
		}

		@Override
		public void set(int e, double val) {
			if (e >= data.length)
				data = Arrays.copyOf(data, Math.max(2, data.length * 2));
			data[e] = val;
			size = Math.max(size, e + 1); // TODO
		}

		@Override
		public void clear() {
			size = 0;
		}

		@Override
		public DataIter.Double iterator() {
			return new DataItr();
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
