package com.ugav.algo;

import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Graph.WeightFunctionInt;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

public class EdgeDataMap {

	private EdgeDataMap() {
	}

	public static class Obj<E> implements EdgeData<E> {

		private final Int2ObjectMap<E> data;

		public Obj() {
			this(0);
		}

		public Obj(int expectedSize) {
			data = new Int2ObjectOpenHashMap<>(expectedSize);
		}

		@Override
		public E get(int e) {
			return data.get(e);
		}

		@Override
		public void set(int e, E data) {
			this.data.put(e, data);
		}

		@Override
		public void clear() {
			data.clear();
		}
	}

	public static class Int implements EdgeData.Int, WeightFunctionInt {

		private final Int2IntMap data;

		public Int() {
			this(0);
		}

		public Int(int expectedSize) {
			data = new Int2IntOpenHashMap(expectedSize);
		}

		@Override
		public int getInt(int e) {
			return data.get(e);
		}

		@Override
		public void set(int e, int data) {
			this.data.put(e, data);
		}

		@Override
		public void clear() {
			data.clear();
		}

	}

	public static class Double implements EdgeData.Double, WeightFunction {

		private final Int2DoubleMap data;

		public Double() {
			this(0);
		}

		public Double(int expectedSize) {
			data = new Int2DoubleOpenHashMap(expectedSize);
		}

		@Override
		public double getDouble(int e) {
			return data.get(e);
		}

		@Override
		public void set(int e, double data) {
			this.data.put(e, data);
		}

		@Override
		public void clear() {
			data.clear();
		}
	}

}
