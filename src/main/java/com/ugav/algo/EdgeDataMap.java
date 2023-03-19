package com.ugav.algo;

import com.ugav.algo.Graph.WeightFunction;
import com.ugav.algo.Graph.WeightFunctionInt;

import it.unimi.dsi.fastutil.ints.Int2DoubleMap;
import it.unimi.dsi.fastutil.ints.Int2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterator;

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

		@Override
		public DataIter<E> iterator() {

			data.int2ObjectEntrySet();
			return new DataIter<>() {

				final ObjectIterator<Int2ObjectMap.Entry<E>> it = ((Int2ObjectMap.FastEntrySet<E>) data
						.int2ObjectEntrySet()).fastIterator();
				Int2ObjectMap.Entry<E> last;

				@Override
				public boolean hasNext() {
					return it.hasNext();
				}

				@Override
				public int nextEdge() {
					return (last = it.next()).getIntKey();
				}

				@Override
				public E getData() {
					return last.getValue();
				}

				@Override
				public void setData(E val) {
					last.setValue(val);
				}

			};
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

		@Override
		public DataIter.Int iterator() {
			return new DataIter.Int() {

				final ObjectIterator<Int2IntMap.Entry> it = ((Int2IntMap.FastEntrySet) data.int2IntEntrySet())
						.fastIterator();
				Int2IntMap.Entry last;

				@Override
				public boolean hasNext() {
					return it.hasNext();
				}

				@Override
				public int nextEdge() {
					return (last = it.next()).getIntKey();
				}

				@Override
				public int getDataInt() {
					return last.getIntValue();
				}

				@Override
				public void setData(int val) {
					last.setValue(val);
				}

			};
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

		@Override
		public DataIter.Double iterator() {
			return new DataIter.Double() {

				final ObjectIterator<Int2DoubleMap.Entry> it = ((Int2DoubleMap.FastEntrySet) data.int2DoubleEntrySet())
						.fastIterator();
				Int2DoubleMap.Entry last;

				@Override
				public boolean hasNext() {
					return it.hasNext();
				}

				@Override
				public int nextEdge() {
					return (last = it.next()).getIntKey();
				}

				@Override
				public double getDataDouble() {
					return last.getDoubleValue();
				}

				@Override
				public void setData(double val) {
					last.setValue(val);
				}

			};
		}
	}

}
