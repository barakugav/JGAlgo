package com.ugav.algo;

import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

public interface Graph<E> {

	public int vertices();

	public int edges();

	public EdgeIter<E> edges(int u);

	default int getEdge(int u, int v) {
		for (IntIterator it = edges(u); it.hasNext();) {
			int e = it.nextInt();
			if (getEdgeTarget(e) == v)
				return e;
		}
		return -1;
	}

	public int degree(int u);

	public int newVertex();

	public int addEdge(int u, int v);

	public void clear();

	public void clearEdges();

	public int getEdgeSource(int edge);

	public int getEdgeTarget(int edge);

	default int getEdgeEndpoint(int edge, int endpoint) {
		int u = getEdgeSource(edge);
		int v = getEdgeTarget(edge);
		if (endpoint == u)
			return v;
		else if (endpoint == v)
			return u;
		else
			throw new IllegalArgumentException();
	}

	public EdgeData<E> edgeData();

	public void setEdgesData(EdgeData<E> data);

	public static interface EdgeIter<E> extends IntIterator {

		int u();

		int v();

		E data();

		void setData(E val);

	}

	public static interface Undirected<E> extends Graph<E> {

	}

	public static interface Directed<E> extends Graph<E> {

		@Deprecated
		@Override
		default EdgeIter<E> edges(int u) {
			return edgesOut(u);
		}

		public EdgeIter<E> edgesOut(int u);

		public EdgeIter<E> edgesIn(int v);

		@Override
		@Deprecated
		default int degree(int u) {
			return degreeOut(u);
		}

		public int degreeOut(int u);

		public int degreeIn(int v);

//		default void removeEdgesOut(int u) {
//			for (EdgeIter it = edgesOut(u); it.hasNext();) {
//				it.nextInt();
//				it.remove();
//			}
//		}
//
//		default void removeEdgesIn(int v) {
//			for (EdgeIter it = edgesIn(v); it.hasNext();) {
//				it.nextInt();
//				it.remove();
//			}
//		}

		@Override
		@Deprecated
		default int getEdgeEndpoint(int edge, int endpoint) {
			return Graph.super.getEdgeEndpoint(edge, endpoint);
		}

	}

	@FunctionalInterface
	public static interface WeightFunction {

		public double weight(int e);

	}

	@FunctionalInterface
	public static interface WeightFunctionInt extends WeightFunction {

		@Override
		default double weight(int e) {
			return weightInt(e);
		}

		public int weightInt(int e);

	}

	public static interface EdgeData<E> {

		public E get(int e);

		public void set(int e, E data);

		public void clear();

		public static class Obj<E> implements EdgeData<E> {

			private final ObjectArrayList<E> data;

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

		public static class Int implements EdgeData<Integer>, WeightFunctionInt {

			private final IntArrayList data;
			private static final int DefVal = -1;

			public Int() {
				this(0);
			}

			public Int(int expectedSize) {
				data = new IntArrayList(expectedSize);
			}

			public int getInt(int e) {
				return data.size() < e ? data.getInt(e) : DefVal;
			}

			@Override
			public Integer get(int e) {
				return Integer.valueOf(getInt(e));
			}

			public void set(int e, int data) {
				this.data.ensureCapacity(e + 1);
				while (this.data.size() <= e)
					this.data.add(DefVal);
				this.data.set(e, data);
			}

			@Override
			public void set(int e, Integer data) {
				set(e, data.intValue());
			}

			@Override
			public void clear() {
				data.clear();
			}

			@Override
			public int weightInt(int e) {
				return getInt(e);
			}
		}

		public static class Double implements EdgeData<java.lang.Double>, WeightFunction {

			private final DoubleArrayList data;
			private static final double DefVal = -1;

			public Double() {
				this(0);
			}

			public Double(int expectedSize) {
				data = new DoubleArrayList(expectedSize);
			}

			public double getDouble(int e) {
				return data.size() < e ? data.getDouble(e) : DefVal;
			}

			@Override
			public java.lang.Double get(int e) {
				return java.lang.Double.valueOf(getDouble(e));
			}

			public void set(int e, double data) {
				this.data.ensureCapacity(e + 1);
				while (this.data.size() <= e)
					this.data.add(DefVal);
				this.data.set(e, data);
			}

			@Override
			public void set(int e, java.lang.Double data) {
				set(e, data.doubleValue());
			}

			@Override
			public void clear() {
				data.clear();
			}

			@Override
			public double weight(int e) {
				return getDouble(e);
			}
		}

	}

}
