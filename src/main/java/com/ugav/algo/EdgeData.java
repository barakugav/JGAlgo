package com.ugav.algo;

import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Graph.WeightFunctionInt;

public interface EdgeData<E> {

	E get(int e);

	void set(int e, E data);

	DataIter<E> iterator();

	void clear();

	public static interface Removable<E> extends EdgeData<E> {
		void removeEdge(int e); //TODO
	}

	public static interface DataIter<E> {

		boolean hasNext();

		int nextEdge();

		E getData();

		void setData(E val);

		public static interface Int extends DataIter<Integer> {

			int getDataInt();

			@Deprecated
			@Override
			default Integer getData() {
				return Integer.valueOf(getDataInt());
			}

			void setData(int val);

			@Deprecated
			@Override
			default void setData(Integer val) {
				setData(val.intValue());
			}

		}

		public static interface Double extends DataIter<java.lang.Double> {

			double getDataDouble();

			@Deprecated
			@Override
			default java.lang.Double getData() {
				return java.lang.Double.valueOf(getDataDouble());
			}

			void setData(double val);

			@Deprecated
			@Override
			default void setData(java.lang.Double val) {
				setData(val.doubleValue());
			}

		}

	}

	public static interface Int extends EdgeData<Integer>, WeightFunctionInt {

		int getInt(int e);

		@Override
		default Integer get(int e) {
			return Integer.valueOf(getInt(e));
		}

		void set(int e, int data);

		@Override
		default void set(int e, Integer data) {
			set(e, data.intValue());
		}

		@Override
		DataIter.Int iterator();

		@Override
		default int weightInt(int e) {
			return getInt(e);
		}
	}

	public static interface Double extends EdgeData<java.lang.Double>, WeightFunction {

		double getDouble(int e);

		@Override
		default java.lang.Double get(int e) {
			return java.lang.Double.valueOf(getDouble(e));
		}

		void set(int e, double data);

		@Override
		default void set(int e, java.lang.Double data) {
			set(e, data.doubleValue());
		}

		@Override
		DataIter.Double iterator();

		@Override
		default double weight(int e) {
			return getDouble(e);
		}
	}

}