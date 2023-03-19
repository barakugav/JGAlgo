package com.ugav.algo;

import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Graph.WeightFunctionInt;

public interface EdgeData<E> {

	public E get(int e);

	public void set(int e, E data);

	public void clear();

	public static interface Int extends EdgeData<Integer>, WeightFunctionInt {

		public int getInt(int e);

		@Override
		default Integer get(int e) {
			return Integer.valueOf(getInt(e));
		}

		public void set(int e, int data);

		@Override
		default void set(int e, Integer data) {
			set(e, data.intValue());
		}

		@Override
		default int weightInt(int e) {
			return getInt(e);
		}
	}

	public static interface Double extends EdgeData<java.lang.Double>, WeightFunction {

		public double getDouble(int e);

		@Override
		default java.lang.Double get(int e) {
			return java.lang.Double.valueOf(getDouble(e));
		}

		public void set(int e, double data);

		@Override
		default void set(int e, java.lang.Double data) {
			set(e, data.doubleValue());
		}

		@Override
		default double weight(int e) {
			return getDouble(e);
		}
	}

}