package com.ugav.algo;

public interface Weights<E> {

	public E get(int key);

	public void set(int key, E data);

	public E defaultVal();

	public void clear();

	public static interface Int extends Weights<Integer>, EdgeWeightFunc.Int {

		public int getInt(int key);

		@Deprecated
		@Override
		default Integer get(int key) {
			return Integer.valueOf(getInt(key));
		}

		public void set(int key, int weight);

		@Deprecated
		@Override
		default void set(int key, Integer data) {
			set(key, data.intValue());
		}

		public int defaultValInt();

		@Deprecated
		@Override
		default Integer defaultVal() {
			return Integer.valueOf(defaultValInt());
		}

		@Override
		default int weightInt(int key) {
			return getInt(key);
		}
	}

	public static interface Double extends Weights<java.lang.Double>, EdgeWeightFunc {

		public double getDouble(int key);

		@Deprecated
		@Override
		default java.lang.Double get(int key) {
			return java.lang.Double.valueOf(getDouble(key));
		}

		public void set(int key, double weight);

		@Deprecated
		@Override
		default void set(int key, java.lang.Double data) {
			set(key, data.doubleValue());
		}

		public double defaultValDouble();

		@Deprecated
		@Override
		default java.lang.Double defaultVal() {
			return java.lang.Double.valueOf(defaultValDouble());
		}

		@Override
		default double weight(int key) {
			return getDouble(key);
		}
	}

	public static interface Bool extends Weights<Boolean> {

		public boolean getBool(int key);

		@Deprecated
		@Override
		default Boolean get(int key) {
			return Boolean.valueOf(getBool(key));
		}

		public void set(int key, boolean weight);

		@Deprecated
		@Override
		default void set(int key, Boolean data) {
			set(key, data.booleanValue());
		}

		public boolean defaultValBool();

		@Deprecated
		@Override
		default Boolean defaultVal() {
			return Boolean.valueOf(defaultValBool());
		}
	}

}