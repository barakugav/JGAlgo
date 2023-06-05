/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jgalgo;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import it.unimi.dsi.fastutil.booleans.AbstractBooleanList;
import it.unimi.dsi.fastutil.booleans.BooleanCollection;
import it.unimi.dsi.fastutil.booleans.BooleanListIterator;
import it.unimi.dsi.fastutil.bytes.AbstractByteList;
import it.unimi.dsi.fastutil.bytes.ByteArrays;
import it.unimi.dsi.fastutil.bytes.ByteCollection;
import it.unimi.dsi.fastutil.bytes.ByteIterators;
import it.unimi.dsi.fastutil.bytes.ByteListIterator;
import it.unimi.dsi.fastutil.chars.AbstractCharList;
import it.unimi.dsi.fastutil.chars.CharArrays;
import it.unimi.dsi.fastutil.chars.CharCollection;
import it.unimi.dsi.fastutil.chars.CharIterators;
import it.unimi.dsi.fastutil.chars.CharListIterator;
import it.unimi.dsi.fastutil.doubles.AbstractDoubleList;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.doubles.DoubleCollection;
import it.unimi.dsi.fastutil.doubles.DoubleIterators;
import it.unimi.dsi.fastutil.doubles.DoubleListIterator;
import it.unimi.dsi.fastutil.floats.AbstractFloatList;
import it.unimi.dsi.fastutil.floats.FloatArrays;
import it.unimi.dsi.fastutil.floats.FloatCollection;
import it.unimi.dsi.fastutil.floats.FloatIterators;
import it.unimi.dsi.fastutil.floats.FloatListIterator;
import it.unimi.dsi.fastutil.ints.AbstractIntList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.longs.AbstractLongList;
import it.unimi.dsi.fastutil.longs.LongArrays;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongIterators;
import it.unimi.dsi.fastutil.longs.LongListIterator;
import it.unimi.dsi.fastutil.objects.AbstractObjectList;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.ObjectArrays;
import it.unimi.dsi.fastutil.objects.ObjectCollection;
import it.unimi.dsi.fastutil.objects.ObjectIterators;
import it.unimi.dsi.fastutil.objects.ObjectListIterator;
import it.unimi.dsi.fastutil.shorts.AbstractShortList;
import it.unimi.dsi.fastutil.shorts.ShortArrays;
import it.unimi.dsi.fastutil.shorts.ShortCollection;
import it.unimi.dsi.fastutil.shorts.ShortIterators;
import it.unimi.dsi.fastutil.shorts.ShortListIterator;

interface WeightsImpl<E> extends Weights<E> {

	@SuppressWarnings("unchecked")
	default WeightsImpl<E> unmodifiableView() {
		if (this instanceof Unmodifiable<?>)
			return this;
		if (this instanceof Weights.Byte)
			return (WeightsImpl<E>) new Unmodifiable.Byte((Weights.Byte) this);
		if (this instanceof Weights.Short)
			return (WeightsImpl<E>) new Unmodifiable.Short((Weights.Short) this);
		if (this instanceof Weights.Int)
			return (WeightsImpl<E>) new Unmodifiable.Int((Weights.Int) this);
		if (this instanceof Weights.Long)
			return (WeightsImpl<E>) new Unmodifiable.Long((Weights.Long) this);
		if (this instanceof Weights.Float)
			return (WeightsImpl<E>) new Unmodifiable.Float((Weights.Float) this);
		if (this instanceof Weights.Double)
			return (WeightsImpl<E>) new Unmodifiable.Double((Weights.Double) this);
		if (this instanceof Weights.Bool)
			return (WeightsImpl<E>) new Unmodifiable.Bool((Weights.Bool) this);
		if (this instanceof Weights.Char)
			return (WeightsImpl<E>) new Unmodifiable.Char((Weights.Char) this);
		return new Unmodifiable.Obj<>(this);
	}

	static interface Index<E> extends WeightsImpl<E> {

		int capacity();

		void expand(int newCapacity);

		void clear(int idx);

		void clear();

		void swap(int idx1, int idx2);

		Collection<E> values();

		Class<E> getTypeClass();

		WeightsImpl.Index<E> copy(IdStrategyImpl idStrat);

		static <D> WeightsImpl.Index<D> newInstance(IdStrategyImpl idStart, Class<? super D> type, D defVal) {
			@SuppressWarnings("rawtypes")
			WeightsImpl container;
			if (type == byte.class) {
				byte defVal0 = defVal != null ? ((java.lang.Byte) defVal).byteValue() : 0;
				container = new WeightsImpl.Index.Byte(idStart, defVal0);

			} else if (type == short.class) {
				short defVal0 = defVal != null ? ((java.lang.Short) defVal).shortValue() : 0;
				container = new WeightsImpl.Index.Short(idStart, defVal0);

			} else if (type == int.class) {
				int defVal0 = defVal != null ? ((Integer) defVal).intValue() : 0;
				container = new WeightsImpl.Index.Int(idStart, defVal0);

			} else if (type == long.class) {
				long defVal0 = defVal != null ? ((java.lang.Long) defVal).longValue() : 0;
				container = new WeightsImpl.Index.Long(idStart, defVal0);

			} else if (type == float.class) {
				float defVal0 = defVal != null ? ((java.lang.Float) defVal).floatValue() : 0;
				container = new WeightsImpl.Index.Float(idStart, defVal0);

			} else if (type == double.class) {
				double defVal0 = defVal != null ? ((java.lang.Double) defVal).doubleValue() : 0;
				container = new WeightsImpl.Index.Double(idStart, defVal0);

			} else if (type == boolean.class) {
				boolean defVal0 = defVal != null ? ((Boolean) defVal).booleanValue() : false;
				container = new WeightsImpl.Index.Bool(idStart, defVal0);

			} else if (type == char.class) {
				char defVal0 = defVal != null ? ((Character) defVal).charValue() : 0;
				container = new WeightsImpl.Index.Char(idStart, defVal0);

			} else {
				container = new WeightsImpl.Index.Obj<>(idStart, defVal, type);
			}
			@SuppressWarnings("unchecked")
			WeightsImpl.Index<D> container0 = (WeightsImpl.Index<D>) container;
			return container0;
		}

		static abstract class Abstract<E> implements WeightsImpl.Index<E> {

			final IdStrategyImpl idStrat;

			Abstract(IdStrategyImpl idStrat) {
				this.idStrat = Objects.requireNonNull(idStrat);
			}

			int size() {
				return idStrat.size();
			}

			@Override
			public int hashCode() {
				return values().hashCode();
			}

			@Override
			public String toString() {
				return values().toString();
			}

			void checkIdx(int idx) {
				if (!(0 <= idx && idx < idStrat.size()))
					throw new IndexOutOfBoundsException(idx);
			}
		}

		static class Obj<E> extends WeightsImpl.Index.Abstract<E> {

			private Object[] weights;
			private final E defaultWeight;
			private final ObjectCollection<E> values;
			private final Class<E> type;

			Obj(IdStrategyImpl idStrat, E defVal, Class<E> type) {
				super(idStrat);

				defaultWeight = defVal;
				weights = ObjectArrays.EMPTY_ARRAY;
				Arrays.fill(weights, defaultWeight);

				this.type = Objects.requireNonNull(type);
				values = new AbstractObjectList<>() {
					@Override
					public int size() {
						return WeightsImpl.Index.Obj.super.size();
					}

					@SuppressWarnings({ "unchecked", "rawtypes" })
					@Override
					public ObjectListIterator<E> iterator() {
						return (ObjectListIterator) ObjectIterators.wrap(weights, 0, size());
					}

					@SuppressWarnings("unchecked")
					@Override
					public E get(int index) {
						checkIdx(index);
						return (E) weights[index];
					}
				};
			}

			@Override
			@SuppressWarnings("unchecked")
			public E get(int idx) {
				checkIdx(idx);
				return (E) weights[idx];
			}

			@Override
			public void set(int idx, E weight) {
				checkIdx(idx);
				weights[idx] = weight;
			}

			@Override
			public E defaultWeight() {
				return defaultWeight;
			}

			@Override
			public int capacity() {
				return weights.length;
			}

			@Override
			public void expand(int newCapacity) {
				int oldCapacity = weights.length;
				assert oldCapacity < newCapacity;
				weights = Arrays.copyOf(weights, newCapacity);
				Arrays.fill(weights, oldCapacity, newCapacity, defaultWeight);
			}

			@Override
			public void swap(int idx1, int idx2) {
				checkIdx(idx1);
				checkIdx(idx2);
				ObjectArrays.swap(weights, idx1, idx2);
			}

			@Override
			public void clear(int idx) {
				// checkIdx(idx);
				weights[idx] = defaultWeight;
			}

			@Override
			public void clear() {
				Arrays.fill(weights, 0, size(), defaultWeight);
			}

			@Override
			public Collection<E> values() {
				return values;
			}

			@Override
			public Class<E> getTypeClass() {
				return type;
			}

			@Override
			public WeightsImpl.Index.Obj<E> copy(IdStrategyImpl idStrat) {
				if (idStrat.size() != this.idStrat.size())
					throw new IllegalArgumentException();
				WeightsImpl.Index.Obj<E> copy = new WeightsImpl.Index.Obj<>(idStrat, defaultWeight, type);
				copy.weights = Arrays.copyOf(weights, idStrat.size());
				return copy;
			}

			@Override
			public boolean equals(Object other) {
				if (other == this)
					return true;
				if (!(other instanceof WeightsImpl.Index.Obj<?>))
					return false;
				WeightsImpl.Index.Obj<?> o = (WeightsImpl.Index.Obj<?>) other;
				return Arrays.equals(weights, 0, size(), o.weights, 0, o.size());
			}
		}

		static class Byte extends WeightsImpl.Index.Abstract<java.lang.Byte> implements Weights.Byte {

			private byte[] weights;
			private final byte defaultWeight;
			private final ByteCollection values;

			Byte(IdStrategyImpl idStrat, byte defVal) {
				super(idStrat);

				weights = ByteArrays.EMPTY_ARRAY;
				defaultWeight = defVal;
				values = new AbstractByteList() {

					@Override
					public int size() {
						return WeightsImpl.Index.Byte.super.size();
					}

					@Override
					public ByteListIterator iterator() {
						return ByteIterators.wrap(weights, 0, size());
					}

					@Override
					public byte getByte(int index) {
						checkIdx(index);
						return weights[index];
					}
				};
			}

			@Override
			public byte getByte(int idx) {
				checkIdx(idx);
				return weights[idx];
			}

			@Override
			public void set(int idx, byte weight) {
				checkIdx(idx);
				weights[idx] = weight;
			}

			@Override
			public byte defaultWeightByte() {
				return defaultWeight;
			}

			@Override
			public int capacity() {
				return weights.length;
			}

			@Override
			public void expand(int newCapacity) {
				int oldCapacity = weights.length;
				assert oldCapacity < newCapacity;
				weights = Arrays.copyOf(weights, newCapacity);
				Arrays.fill(weights, oldCapacity, newCapacity, defaultWeight);
			}

			@Override
			public void swap(int idx1, int idx2) {
				checkIdx(idx1);
				checkIdx(idx2);
				ByteArrays.swap(weights, idx1, idx2);
			}

			@Override
			public void clear(int idx) {
				// checkIdx(idx);
				weights[idx] = defaultWeight;
			}

			@Override
			public void clear() {
				Arrays.fill(weights, 0, size(), defaultWeight);
			}

			@Override
			public ByteCollection values() {
				return values;
			}

			@Override
			public Class<java.lang.Byte> getTypeClass() {
				return byte.class;
			}

			@Override
			public WeightsImpl.Index.Byte copy(IdStrategyImpl idStrat) {
				if (idStrat.size() != this.idStrat.size())
					throw new IllegalArgumentException();
				WeightsImpl.Index.Byte copy = new WeightsImpl.Index.Byte(idStrat, defaultWeight);
				copy.weights = Arrays.copyOf(weights, idStrat.size());
				return copy;
			}

			@Override
			public boolean equals(Object other) {
				if (this == other)
					return true;
				if (!(other instanceof WeightsImpl.Index.Byte))
					return false;
				WeightsImpl.Index.Byte o = (WeightsImpl.Index.Byte) other;
				return Arrays.equals(weights, 0, size(), o.weights, 0, o.size());
			}
		}

		static class Short extends WeightsImpl.Index.Abstract<java.lang.Short> implements Weights.Short {

			private short[] weights;
			private final short defaultWeight;
			private final ShortCollection values;

			Short(IdStrategyImpl idStrat, short defVal) {
				super(idStrat);

				weights = ShortArrays.EMPTY_ARRAY;
				defaultWeight = defVal;
				values = new AbstractShortList() {

					@Override
					public int size() {
						return WeightsImpl.Index.Short.super.size();
					}

					@Override
					public ShortListIterator iterator() {
						return ShortIterators.wrap(weights, 0, size());
					}

					@Override
					public short getShort(int index) {
						checkIdx(index);
						return weights[index];
					}
				};
			}

			@Override
			public short getShort(int idx) {
				checkIdx(idx);
				return weights[idx];
			}

			@Override
			public void set(int idx, short weight) {
				checkIdx(idx);
				weights[idx] = weight;
			}

			@Override
			public short defaultWeightShort() {
				return defaultWeight;
			}

			@Override
			public int capacity() {
				return weights.length;
			}

			@Override
			public void expand(int newCapacity) {
				int oldCapacity = weights.length;
				assert oldCapacity < newCapacity;
				weights = Arrays.copyOf(weights, newCapacity);
				Arrays.fill(weights, oldCapacity, newCapacity, defaultWeight);
			}

			@Override
			public void swap(int idx1, int idx2) {
				checkIdx(idx1);
				checkIdx(idx2);
				ShortArrays.swap(weights, idx1, idx2);
			}

			@Override
			public void clear(int idx) {
				// checkIdx(idx);
				weights[idx] = defaultWeight;
			}

			@Override
			public void clear() {
				Arrays.fill(weights, 0, size(), defaultWeight);
			}

			@Override
			public ShortCollection values() {
				return values;
			}

			@Override
			public Class<java.lang.Short> getTypeClass() {
				return short.class;
			}

			@Override
			public WeightsImpl.Index.Short copy(IdStrategyImpl idStrat) {
				if (idStrat.size() != this.idStrat.size())
					throw new IllegalArgumentException();
				WeightsImpl.Index.Short copy = new WeightsImpl.Index.Short(idStrat, defaultWeight);
				copy.weights = Arrays.copyOf(weights, idStrat.size());
				return copy;
			}

			@Override
			public boolean equals(Object other) {
				if (this == other)
					return true;
				if (!(other instanceof WeightsImpl.Index.Short))
					return false;
				WeightsImpl.Index.Short o = (WeightsImpl.Index.Short) other;
				return Arrays.equals(weights, 0, size(), o.weights, 0, o.size());
			}
		}

		static class Int extends WeightsImpl.Index.Abstract<Integer> implements Weights.Int {

			private int[] weights;
			private final int defaultWeight;
			private final IntCollection values;

			Int(IdStrategyImpl idStrat, int defVal) {
				super(idStrat);

				weights = IntArrays.EMPTY_ARRAY;
				defaultWeight = defVal;
				values = new AbstractIntList() {

					@Override
					public int size() {
						return WeightsImpl.Index.Int.super.size();
					}

					@Override
					public IntListIterator iterator() {
						return IntIterators.wrap(weights, 0, size());
					}

					@Override
					public int getInt(int index) {
						checkIdx(index);
						return weights[index];
					}
				};
			}

			@Override
			public int getInt(int idx) {
				checkIdx(idx);
				return weights[idx];
			}

			@Override
			public void set(int idx, int weight) {
				checkIdx(idx);
				weights[idx] = weight;
			}

			@Override
			public int defaultWeightInt() {
				return defaultWeight;
			}

			@Override
			public int capacity() {
				return weights.length;
			}

			@Override
			public void expand(int newCapacity) {
				int oldCapacity = weights.length;
				assert oldCapacity < newCapacity;
				weights = Arrays.copyOf(weights, newCapacity);
				Arrays.fill(weights, oldCapacity, newCapacity, defaultWeight);
			}

			@Override
			public void swap(int idx1, int idx2) {
				checkIdx(idx1);
				checkIdx(idx2);
				IntArrays.swap(weights, idx1, idx2);
			}

			@Override
			public void clear(int idx) {
				// checkIdx(idx);
				weights[idx] = defaultWeight;
			}

			@Override
			public void clear() {
				Arrays.fill(weights, 0, size(), defaultWeight);
			}

			@Override
			public IntCollection values() {
				return values;
			}

			@Override
			public Class<Integer> getTypeClass() {
				return int.class;
			}

			@Override
			public WeightsImpl.Index.Int copy(IdStrategyImpl idStrat) {
				if (idStrat.size() != this.idStrat.size())
					throw new IllegalArgumentException();
				WeightsImpl.Index.Int copy = new WeightsImpl.Index.Int(idStrat, defaultWeight);
				copy.weights = Arrays.copyOf(weights, idStrat.size());
				return copy;
			}

			@Override
			public boolean equals(Object other) {
				if (this == other)
					return true;
				if (!(other instanceof WeightsImpl.Index.Int))
					return false;
				WeightsImpl.Index.Int o = (WeightsImpl.Index.Int) other;
				return Arrays.equals(weights, 0, size(), o.weights, 0, o.size());
			}
		}

		static class Long extends WeightsImpl.Index.Abstract<java.lang.Long> implements Weights.Long {

			private long[] weights;
			private final long defaultWeight;
			private final LongCollection values = new AbstractLongList() {

				@Override
				public int size() {
					return WeightsImpl.Index.Long.super.size();
				}

				@Override
				public LongListIterator iterator() {
					return LongIterators.wrap(weights, 0, size());
				}

				@Override
				public long getLong(int index) {
					checkIdx(index);
					return weights[index];
				}
			};

			Long(IdStrategyImpl idStrat, long defVal) {
				super(idStrat);

				weights = LongArrays.EMPTY_ARRAY;
				defaultWeight = defVal;
			}

			Long(WeightsImpl.Index.Long orig, IdStrategyImpl idStrat) {
				super(idStrat);
				if (idStrat.size() != this.idStrat.size())
					throw new IllegalArgumentException();

				weights = Arrays.copyOf(orig.weights, idStrat.size());
				defaultWeight = orig.defaultWeight;
			}

			@Override
			public long getLong(int idx) {
				checkIdx(idx);
				return weights[idx];
			}

			@Override
			public void set(int idx, long weight) {
				checkIdx(idx);
				weights[idx] = weight;
			}

			@Override
			public long defaultWeightLong() {
				return defaultWeight;
			}

			@Override
			public int capacity() {
				return weights.length;
			}

			@Override
			public void expand(int newCapacity) {
				int oldCapacity = weights.length;
				assert oldCapacity < newCapacity;
				weights = Arrays.copyOf(weights, newCapacity);
				Arrays.fill(weights, oldCapacity, newCapacity, defaultWeight);
			}

			@Override
			public void swap(int idx1, int idx2) {
				checkIdx(idx1);
				checkIdx(idx2);
				LongArrays.swap(weights, idx1, idx2);
			}

			@Override
			public void clear(int idx) {
				// checkIdx(idx);
				weights[idx] = defaultWeight;
			}

			@Override
			public void clear() {
				Arrays.fill(weights, 0, size(), defaultWeight);
			}

			@Override
			public LongCollection values() {
				return values;
			}

			@Override
			public Class<java.lang.Long> getTypeClass() {
				return long.class;
			}

			@Override
			public WeightsImpl.Index.Long copy(IdStrategyImpl idStrat) {
				return new WeightsImpl.Index.Long(this, idStrat);
			}

			@Override
			public boolean equals(Object other) {
				if (this == other)
					return true;
				if (!(other instanceof WeightsImpl.Index.Long))
					return false;
				WeightsImpl.Index.Long o = (WeightsImpl.Index.Long) other;
				return Arrays.equals(weights, 0, size(), o.weights, 0, o.size());
			}
		}

		static class Float extends WeightsImpl.Index.Abstract<java.lang.Float> implements Weights.Float {

			private float[] weights;
			private final float defaultWeight;
			private final FloatCollection values;

			Float(IdStrategyImpl idStrat, float defVal) {
				super(idStrat);

				weights = FloatArrays.EMPTY_ARRAY;
				defaultWeight = defVal;
				values = new AbstractFloatList() {

					@Override
					public int size() {
						return WeightsImpl.Index.Float.super.size();
					}

					@Override
					public FloatListIterator iterator() {
						return FloatIterators.wrap(weights, 0, size());
					}

					@Override
					public float getFloat(int index) {
						checkIdx(index);
						return weights[index];
					}
				};
			}

			@Override
			public float getFloat(int idx) {
				checkIdx(idx);
				return weights[idx];
			}

			@Override
			public void set(int idx, float weight) {
				checkIdx(idx);
				weights[idx] = weight;
			}

			@Override
			public float defaultWeightFloat() {
				return defaultWeight;
			}

			@Override
			public int capacity() {
				return weights.length;
			}

			@Override
			public void expand(int newCapacity) {
				int oldCapacity = weights.length;
				assert oldCapacity < newCapacity;
				weights = Arrays.copyOf(weights, newCapacity);
				Arrays.fill(weights, oldCapacity, newCapacity, defaultWeight);
			}

			@Override
			public void swap(int idx1, int idx2) {
				checkIdx(idx1);
				checkIdx(idx2);
				FloatArrays.swap(weights, idx1, idx2);
			}

			@Override
			public void clear(int idx) {
				// checkIdx(idx);
				weights[idx] = defaultWeight;
			}

			@Override
			public void clear() {
				Arrays.fill(weights, 0, size(), defaultWeight);
			}

			@Override
			public FloatCollection values() {
				return values;
			}

			@Override
			public Class<java.lang.Float> getTypeClass() {
				return float.class;
			}

			@Override
			public WeightsImpl.Index.Float copy(IdStrategyImpl idStrat) {
				if (idStrat.size() != this.idStrat.size())
					throw new IllegalArgumentException();
				WeightsImpl.Index.Float copy = new WeightsImpl.Index.Float(idStrat, defaultWeight);
				copy.weights = Arrays.copyOf(weights, idStrat.size());
				return copy;
			}

			@Override
			public boolean equals(Object other) {
				if (this == other)
					return true;
				if (!(other instanceof WeightsImpl.Index.Float))
					return false;
				WeightsImpl.Index.Float o = (WeightsImpl.Index.Float) other;
				return Arrays.equals(weights, 0, size(), o.weights, 0, o.size());
			}
		}

		static class Double extends WeightsImpl.Index.Abstract<java.lang.Double> implements Weights.Double {

			private double[] weights;
			private final double defaultWeight;
			private final DoubleCollection values;

			Double(IdStrategyImpl idStrat, double defVal) {
				super(idStrat);

				weights = DoubleArrays.EMPTY_ARRAY;
				defaultWeight = defVal;
				values = new AbstractDoubleList() {

					@Override
					public int size() {
						return WeightsImpl.Index.Double.super.size();
					}

					@Override
					public DoubleListIterator iterator() {
						return DoubleIterators.wrap(weights, 0, size());
					}

					@Override
					public double getDouble(int index) {
						checkIdx(index);
						return weights[index];
					}
				};
			}

			@Override
			public double getDouble(int idx) {
				checkIdx(idx);
				return weights[idx];
			}

			@Override
			public void set(int idx, double weight) {
				checkIdx(idx);
				weights[idx] = weight;
			}

			@Override
			public double defaultWeightDouble() {
				return defaultWeight;
			}

			@Override
			public int capacity() {
				return weights.length;
			}

			@Override
			public void expand(int newCapacity) {
				int oldCapacity = weights.length;
				assert oldCapacity < newCapacity;
				weights = Arrays.copyOf(weights, newCapacity);
				Arrays.fill(weights, oldCapacity, newCapacity, defaultWeight);
			}

			@Override
			public void swap(int idx1, int idx2) {
				checkIdx(idx1);
				checkIdx(idx2);
				DoubleArrays.swap(weights, idx1, idx2);
			}

			@Override
			public void clear(int idx) {
				// checkIdx(idx);
				weights[idx] = defaultWeight;
			}

			@Override
			public void clear() {
				Arrays.fill(weights, 0, size(), defaultWeight);
			}

			@Override
			public DoubleCollection values() {
				return values;
			}

			@Override
			public Class<java.lang.Double> getTypeClass() {
				return double.class;
			}

			@Override
			public WeightsImpl.Index.Double copy(IdStrategyImpl idStrat) {
				if (idStrat.size() != this.idStrat.size())
					throw new IllegalArgumentException();
				WeightsImpl.Index.Double copy = new WeightsImpl.Index.Double(idStrat, defaultWeight);
				copy.weights = Arrays.copyOf(weights, idStrat.size());
				return copy;
			}

			@Override
			public boolean equals(Object other) {
				if (this == other)
					return true;
				if (!(other instanceof WeightsImpl.Index.Double))
					return false;
				WeightsImpl.Index.Double o = (WeightsImpl.Index.Double) other;
				return Arrays.equals(weights, 0, size(), o.weights, 0, o.size());
			}
		}

		static class Bool extends WeightsImpl.Index.Abstract<Boolean> implements Weights.Bool {

			private final BitSet weights;
			private int capacity;
			private final boolean defaultWeight;
			private final BooleanCollection values = new AbstractBooleanList() {

				@Override
				public int size() {
					return WeightsImpl.Index.Bool.super.size();
				}

				@Override
				public BooleanListIterator iterator() {
					return new BooleanListIterator() {
						int idx = 0;

						@Override
						public boolean hasNext() {
							return idx < size();
						}

						@Override
						public boolean nextBoolean() {
							if (!hasNext())
								throw new NoSuchElementException();
							return weights.get(idx++);
						}

						@Override
						public boolean previousBoolean() {
							if (!hasPrevious())
								throw new NoSuchElementException();
							return weights.get(--idx);
						}

						@Override
						public boolean hasPrevious() {
							return idx > 0;
						}

						@Override
						public int nextIndex() {
							return idx;
						}

						@Override
						public int previousIndex() {
							return idx - 1;
						}
					};
				}

				@Override
				public boolean getBoolean(int index) {
					checkIdx(index);
					return weights.get(index);
				}
			};

			Bool(WeightsImpl.Index.Bool orig, IdStrategyImpl idStrat) {
				super(idStrat);
				defaultWeight = orig.defaultWeight;
				weights = (BitSet) orig.weights.clone();
			}

			Bool(IdStrategyImpl idStrat, boolean defVal) {
				super(idStrat);

				defaultWeight = defVal;
				weights = new BitSet();
			}

			@Override
			public boolean getBool(int idx) {
				checkIdx(idx);
				return weights.get(idx);
			}

			@Override
			public void set(int idx, boolean weight) {
				checkIdx(idx);
				weights.set(idx, weight);
			}

			@Override
			public boolean defaultWeightBool() {
				return defaultWeight;
			}

			@Override
			public int capacity() {
				return capacity;
			}

			@Override
			public void expand(int newCapacity) {
				int oldCapacity = capacity;
				assert oldCapacity < newCapacity;
				capacity = newCapacity;
				if (defaultWeight)
					weights.set(oldCapacity, newCapacity);
			}

			@Override
			public void swap(int idx1, int idx2) {
				checkIdx(idx1);
				checkIdx(idx2);
				boolean temp = weights.get(idx1);
				weights.set(idx1, weights.get(idx2));
				weights.set(idx2, temp);
			}

			@Override
			public void clear(int idx) {
				// checkIdx(idx);
				weights.set(idx, defaultWeight);
			}

			@Override
			public void clear() {
				weights.set(0, capacity, defaultWeight);
			}

			@Override
			public BooleanCollection values() {
				return values;
			}

			@Override
			public Class<Boolean> getTypeClass() {
				return boolean.class;
			}

			@Override
			public WeightsImpl.Index.Bool copy(IdStrategyImpl idStrat) {
				if (idStrat.size() != this.idStrat.size())
					throw new IllegalArgumentException();
				return new WeightsImpl.Index.Bool(this, idStrat);
			}

			@Override
			public boolean equals(Object other) {
				if (this == other)
					return true;
				if (!(other instanceof WeightsImpl.Index.Bool))
					return false;
				WeightsImpl.Index.Bool o = (WeightsImpl.Index.Bool) other;
				return size() == o.size() && weights.equals(o.weights);
			}
		}

		static class Char extends WeightsImpl.Index.Abstract<Character> implements Weights.Char {

			private char[] weights;
			private final char defaultWeight;
			private final CharCollection values;

			Char(IdStrategyImpl idStrat, char defVal) {
				super(idStrat);

				weights = CharArrays.EMPTY_ARRAY;
				defaultWeight = defVal;
				values = new AbstractCharList() {

					@Override
					public int size() {
						return WeightsImpl.Index.Char.super.size();
					}

					@Override
					public CharListIterator iterator() {
						return CharIterators.wrap(weights, 0, size());
					}

					@Override
					public char getChar(int index) {
						checkIdx(index);
						return weights[index];
					}
				};
			}

			@Override
			public char getChar(int idx) {
				checkIdx(idx);
				return weights[idx];
			}

			@Override
			public void set(int idx, char weight) {
				checkIdx(idx);
				weights[idx] = weight;
			}

			@Override
			public char defaultWeightChar() {
				return defaultWeight;
			}

			@Override
			public int capacity() {
				return weights.length;
			}

			@Override
			public void expand(int newCapacity) {
				int oldCapacity = weights.length;
				assert oldCapacity < newCapacity;
				weights = Arrays.copyOf(weights, newCapacity);
				Arrays.fill(weights, oldCapacity, newCapacity, defaultWeight);
			}

			@Override
			public void swap(int idx1, int idx2) {
				checkIdx(idx1);
				checkIdx(idx2);
				CharArrays.swap(weights, idx1, idx2);
			}

			@Override
			public void clear(int idx) {
				// checkIdx(idx);
				weights[idx] = defaultWeight;
			}

			@Override
			public void clear() {
				Arrays.fill(weights, 0, size(), defaultWeight);
			}

			@Override
			public CharCollection values() {
				return values;
			}

			@Override
			public Class<Character> getTypeClass() {
				return char.class;
			}

			@Override
			public WeightsImpl.Index.Char copy(IdStrategyImpl idStrat) {
				if (idStrat.size() != this.idStrat.size())
					throw new IllegalArgumentException();
				WeightsImpl.Index.Char copy = new WeightsImpl.Index.Char(idStrat, defaultWeight);
				copy.weights = Arrays.copyOf(weights, idStrat.size());
				return copy;
			}

			@Override
			public boolean equals(Object other) {
				if (this == other)
					return true;
				if (!(other instanceof WeightsImpl.Index.Char))
					return false;
				WeightsImpl.Index.Char o = (WeightsImpl.Index.Char) other;
				return Arrays.equals(weights, 0, size(), o.weights, 0, o.size());
			}
		}

		static class Manager {

			final Map<Object, WeightsImpl.Index<?>> weights = new Object2ObjectArrayMap<>();
			private int weightsCapacity;

			Manager(int initCapacity) {
				weightsCapacity = initCapacity;
			}

			Manager(Manager orig, IdStrategyImpl idStrat) {
				this(idStrat.size());
				for (var entry : orig.weights.entrySet())
					weights.put(entry.getKey(), entry.getValue().copy(idStrat));
			}

			void addWeights(Object key, WeightsImpl.Index<?> weight) {
				WeightsImpl.Index<?> oldContainer = weights.put(key, weight);
				if (oldContainer != null)
					throw new IllegalArgumentException("Two weights types with the same key: " + key);
				if (weightsCapacity > weight.capacity())
					weight.expand(weightsCapacity);
			}

			void removeWeights(Object key) {
				weights.remove(key);
			}

			@SuppressWarnings("unchecked")
			<E, WeightsT extends Weights<E>> WeightsT getWeights(Object key) {
				return (WeightsT) weights.get(key);
			}

			Set<Object> weightsKeys() {
				return Collections.unmodifiableSet(weights.keySet());
			}

			void ensureCapacity(int capacity) {
				if (capacity <= weightsCapacity)
					return;
				int newCapacity = Math.max(Math.max(2, 2 * weightsCapacity), capacity);
				for (WeightsImpl.Index<?> container : weights.values())
					container.expand(newCapacity);
				weightsCapacity = newCapacity;
			}

			void swapElements(int idx1, int idx2) {
				for (WeightsImpl.Index<?> container : weights.values())
					container.swap(idx1, idx2);
			}

			void clearElement(int idx) {
				for (WeightsImpl.Index<?> container : weights.values())
					container.clear(idx);
			}

			void clearContainers() {
				for (WeightsImpl.Index<?> container : weights.values())
					container.clear();
			}
		}
	}

	static abstract class Mapped<E> implements WeightsImpl<E> {

		private final WeightsImpl.Index<E> weights;
		final IndexIdMap indexMap;

		private Mapped(Weights<E> weights, IndexIdMap indexMap) {
			this.weights = (WeightsImpl.Index<E>) Objects.requireNonNull(weights);
			this.indexMap = indexMap;
		}

		Weights<E> weights() {
			return weights;
		}

		static WeightsImpl.Mapped<?> newInstance(WeightsImpl.Index<?> weights, IndexIdMap indexMap) {
			if (weights instanceof Weights.Byte) {
				return new WeightsImpl.Mapped.Byte((Weights.Byte) weights, indexMap);
			} else if (weights instanceof Weights.Short) {
				return new WeightsImpl.Mapped.Short((Weights.Short) weights, indexMap);
			} else if (weights instanceof Weights.Int) {
				return new WeightsImpl.Mapped.Int((Weights.Int) weights, indexMap);
			} else if (weights instanceof Weights.Long) {
				return new WeightsImpl.Mapped.Long((Weights.Long) weights, indexMap);
			} else if (weights instanceof Weights.Float) {
				return new WeightsImpl.Mapped.Float((Weights.Float) weights, indexMap);
			} else if (weights instanceof Weights.Double) {
				return new WeightsImpl.Mapped.Double((Weights.Double) weights, indexMap);
			} else if (weights instanceof Weights.Bool) {
				return new WeightsImpl.Mapped.Bool((Weights.Bool) weights, indexMap);
			} else if (weights instanceof Weights.Char) {
				return new WeightsImpl.Mapped.Char((Weights.Char) weights, indexMap);
			} else {
				return new WeightsImpl.Mapped.Obj<>(weights, indexMap);
			}
		}

		static class Obj<E> extends Mapped<E> {
			Obj(Weights<E> weights, IndexIdMap indexMap) {
				super(weights, indexMap);
			}

			@Override
			public E get(int id) {
				return weights().get(indexMap.idToIndex(id));
			}

			@Override
			public void set(int id, E val) {
				weights().set(indexMap.idToIndex(id), val);
			}

			@Override
			public E defaultWeight() {
				return weights().defaultWeight();
			}
		}

		static class Byte extends Mapped<java.lang.Byte> implements Weights.Byte {
			Byte(Weights.Byte weights, IndexIdMap indexMap) {
				super(weights, indexMap);
			}

			@Override
			public Weights.Byte weights() {
				return (Weights.Byte) super.weights();
			}

			@Override
			public byte getByte(int id) {
				return weights().getByte(indexMap.idToIndex(id));
			}

			@Override
			public void set(int id, byte weight) {
				weights().set(indexMap.idToIndex(id), weight);
			}

			@Override
			public byte defaultWeightByte() {
				return weights().defaultWeightByte();
			}
		}

		static class Short extends Mapped<java.lang.Short> implements Weights.Short {
			Short(Weights.Short container, IndexIdMap indexMap) {
				super(container, indexMap);
			}

			@Override
			public Weights.Short weights() {
				return (Weights.Short) super.weights();
			}

			@Override
			public short getShort(int id) {
				return weights().getShort(indexMap.idToIndex(id));
			}

			@Override
			public void set(int id, short weight) {
				weights().set(indexMap.idToIndex(id), weight);
			}

			@Override
			public short defaultWeightShort() {
				return weights().defaultWeightShort();
			}
		}

		static class Int extends Mapped<Integer> implements Weights.Int {
			Int(Weights.Int container, IndexIdMap indexMap) {
				super(container, indexMap);
			}

			@Override
			public Weights.Int weights() {
				return (Weights.Int) super.weights();
			}

			@Override
			public int getInt(int id) {
				return weights().getInt(indexMap.idToIndex(id));
			}

			@Override
			public void set(int id, int weight) {
				weights().set(indexMap.idToIndex(id), weight);
			}

			@Override
			public int defaultWeightInt() {
				return weights().defaultWeightInt();
			}
		}

		static class Long extends Mapped<java.lang.Long> implements Weights.Long {
			Long(Weights.Long container, IndexIdMap indexMap) {
				super(container, indexMap);
			}

			@Override
			public Weights.Long weights() {
				return (Weights.Long) super.weights();
			}

			@Override
			public long getLong(int id) {
				return weights().getLong(indexMap.idToIndex(id));
			}

			@Override
			public void set(int id, long weight) {
				weights().set(indexMap.idToIndex(id), weight);
			}

			@Override
			public long defaultWeightLong() {
				return weights().defaultWeightLong();
			}
		}

		static class Float extends Mapped<java.lang.Float> implements Weights.Float {
			Float(Weights.Float container, IndexIdMap indexMap) {
				super(container, indexMap);
			}

			@Override
			public Weights.Float weights() {
				return (Weights.Float) super.weights();
			}

			@Override
			public float getFloat(int id) {
				return weights().getFloat(indexMap.idToIndex(id));
			}

			@Override
			public void set(int id, float weight) {
				weights().set(indexMap.idToIndex(id), weight);
			}

			@Override
			public float defaultWeightFloat() {
				return weights().defaultWeightFloat();
			}
		}

		static class Double extends Mapped<java.lang.Double> implements Weights.Double {
			Double(Weights.Double container, IndexIdMap indexMap) {
				super(container, indexMap);
			}

			@Override
			Weights.Double weights() {
				return (Weights.Double) super.weights();
			}

			@Override
			public double getDouble(int id) {
				return weights().getDouble(indexMap.idToIndex(id));
			}

			@Override
			public void set(int id, double weight) {
				weights().set(indexMap.idToIndex(id), weight);
			}

			@Override
			public double defaultWeightDouble() {
				return weights().defaultWeightDouble();
			}
		}

		static class Bool extends Mapped<Boolean> implements Weights.Bool {
			Bool(Weights.Bool container, IndexIdMap indexMap) {
				super(container, indexMap);
			}

			@Override
			Weights.Bool weights() {
				return (Weights.Bool) super.weights();
			}

			@Override
			public boolean getBool(int id) {
				return weights().getBool(indexMap.idToIndex(id));
			}

			@Override
			public void set(int id, boolean weight) {
				weights().set(indexMap.idToIndex(id), weight);
			}

			@Override
			public boolean defaultWeightBool() {
				return weights().defaultWeightBool();
			}
		}

		static class Char extends Mapped<Character> implements Weights.Char {
			Char(Weights.Char container, IndexIdMap indexMap) {
				super(container, indexMap);
			}

			@Override
			Weights.Char weights() {
				return (Weights.Char) super.weights();
			}

			@Override
			public char getChar(int id) {
				return weights().getChar(indexMap.idToIndex(id));
			}

			@Override
			public void set(int id, char weight) {
				weights().set(indexMap.idToIndex(id), weight);
			}

			@Override
			public char defaultWeightChar() {
				return weights().defaultWeightChar();
			}
		}
	}

	static abstract class Unmodifiable<E> implements WeightsImpl<E> {

		private final WeightsImpl<E> weights;

		Unmodifiable(Weights<E> w) {
			this.weights = (WeightsImpl<E>) Objects.requireNonNull(w);
		}

		Weights<E> weights() {
			return weights;
		}

		static class Obj<E> extends Unmodifiable<E> {

			Obj(Weights<E> w) {
				super(w);
			}

			@Override
			public E get(int id) {
				return weights().get(id);
			}

			@Override
			public void set(int id, E weight) {
				throw new UnsupportedOperationException();
			}

			@Override
			public E defaultWeight() {
				return weights().defaultWeight();
			}
		}

		static class Byte extends Unmodifiable<java.lang.Byte> implements Weights.Byte {
			Byte(Weights.Byte w) {
				super(w);
			}

			@Override
			Weights.Byte weights() {
				return (Weights.Byte) super.weights();
			}

			@Override
			public byte getByte(int id) {
				return weights().getByte(id);
			}

			@Override
			public void set(int id, byte weight) {
				throw new UnsupportedOperationException();
			}

			@Override
			public byte defaultWeightByte() {
				return weights().defaultWeightByte();
			}
		}

		static class Short extends Unmodifiable<java.lang.Short> implements Weights.Short {
			Short(Weights.Short w) {
				super(w);
			}

			@Override
			Weights.Short weights() {
				return (Weights.Short) super.weights();
			}

			@Override
			public short getShort(int id) {
				return weights().getShort(id);
			}

			@Override
			public void set(int id, short weight) {
				throw new UnsupportedOperationException();
			}

			@Override
			public short defaultWeightShort() {
				return weights().defaultWeightShort();
			}
		}

		static class Int extends Unmodifiable<Integer> implements Weights.Int {
			Int(Weights.Int w) {
				super(w);
			}

			@Override
			Weights.Int weights() {
				return (Weights.Int) super.weights();
			}

			@Override
			public int getInt(int id) {
				return weights().getInt(id);
			}

			@Override
			public void set(int id, int weight) {
				throw new UnsupportedOperationException();
			}

			@Override
			public int defaultWeightInt() {
				return weights().defaultWeightInt();
			}
		}

		static class Long extends Unmodifiable<java.lang.Long> implements Weights.Long {
			Long(Weights.Long w) {
				super(w);
			}

			@Override
			Weights.Long weights() {
				return (Weights.Long) super.weights();
			}

			@Override
			public long getLong(int id) {
				return weights().getLong(id);
			}

			@Override
			public void set(int id, long weight) {
				throw new UnsupportedOperationException();
			}

			@Override
			public long defaultWeightLong() {
				return weights().defaultWeightLong();
			}
		}

		static class Float extends Unmodifiable<java.lang.Float> implements Weights.Float {
			Float(Weights.Float w) {
				super(w);
			}

			@Override
			Weights.Float weights() {
				return (Weights.Float) super.weights();
			}

			@Override
			public float getFloat(int id) {
				return weights().getFloat(id);
			}

			@Override
			public void set(int id, float weight) {
				throw new UnsupportedOperationException();
			}

			@Override
			public float defaultWeightFloat() {
				return weights().defaultWeightFloat();
			}
		}

		static class Double extends Unmodifiable<java.lang.Double> implements Weights.Double {
			Double(Weights.Double w) {
				super(w);
			}

			@Override
			Weights.Double weights() {
				return (Weights.Double) super.weights();
			}

			@Override
			public double getDouble(int id) {
				return weights().getDouble(id);
			}

			@Override
			public void set(int id, double weight) {
				throw new UnsupportedOperationException();
			}

			@Override
			public double defaultWeightDouble() {
				return weights().defaultWeightDouble();
			}
		}

		static class Bool extends Unmodifiable<Boolean> implements Weights.Bool {
			Bool(Weights.Bool w) {
				super(w);
			}

			@Override
			Weights.Bool weights() {
				return (Weights.Bool) super.weights();
			}

			@Override
			public boolean getBool(int id) {
				return weights().getBool(id);
			}

			@Override
			public void set(int id, boolean weight) {
				throw new UnsupportedOperationException();
			}

			@Override
			public boolean defaultWeightBool() {
				return weights().defaultWeightBool();
			}
		}

		static class Char extends Unmodifiable<Character> implements Weights.Char {
			Char(Weights.Char w) {
				super(w);
			}

			@Override
			Weights.Char weights() {
				return (Weights.Char) super.weights();
			}

			@Override
			public char getChar(int id) {
				return weights().getChar(id);
			}

			@Override
			public void set(int id, char weight) {
				throw new UnsupportedOperationException();
			}

			@Override
			public char defaultWeightChar() {
				return weights().defaultWeightChar();
			}
		}
	}

	static WeightFunction indexWeightFuncFromIdWeightFunc(WeightFunction w, IndexIdMap map) {
		if (w == null || w == WeightFunction.CardinalityWeightFunction) {
			return w;

		} else if (w instanceof WeightsImpl<?>) {
			/* The weight function is some implementation of a mapped weights object */
			/* Instead of re-mapping by wrapping the weight function, return the underlying index weights container */
			WeightsImpl<?> weights = (WeightsImpl<?>) w;
			final boolean unmodifiable = weights instanceof WeightsImpl.Unmodifiable<?>;
			if (unmodifiable)
				weights = ((WeightsImpl.Unmodifiable<?>) weights).weights;
			if (!(weights instanceof WeightsImpl.Mapped<?>))
				throw new IllegalArgumentException("weights of index graph used with non index graph");
			weights = ((WeightsImpl.Mapped<?>) weights).weights;
			if (unmodifiable)
				weights = weights.unmodifiableView();
			return (WeightFunction) weights;

		} else {
			/* Unknown weight function, return a mapped wrapper */
			if (w instanceof WeightFunction.Int) {
				WeightFunction.Int wInt = (WeightFunction.Int) w;
				WeightFunction.Int wIntMapped = idx -> wInt.weightInt(map.indexToId(idx));
				return wIntMapped;
			} else {
				return idx -> w.weight(map.indexToId(idx));
			}
		}
	}

}
