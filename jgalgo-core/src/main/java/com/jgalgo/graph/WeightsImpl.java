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

package com.jgalgo.graph;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import com.jgalgo.internal.util.Assertions;
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
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
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

	int size();

	@SuppressWarnings("unchecked")
	static <E> Weights<E> immutableView(Weights<E> weights) {
		if (weights instanceof Immutable<?>)
			return weights;
		if (weights instanceof Weights.Byte)
			return (WeightsImpl<E>) new ImmutableView.Byte((Weights.Byte) weights);
		if (weights instanceof Weights.Short)
			return (WeightsImpl<E>) new ImmutableView.Short((Weights.Short) weights);
		if (weights instanceof Weights.Int)
			return (WeightsImpl<E>) new ImmutableView.Int((Weights.Int) weights);
		if (weights instanceof Weights.Long)
			return (WeightsImpl<E>) new ImmutableView.Long((Weights.Long) weights);
		if (weights instanceof Weights.Float)
			return (WeightsImpl<E>) new ImmutableView.Float((Weights.Float) weights);
		if (weights instanceof Weights.Double)
			return (WeightsImpl<E>) new ImmutableView.Double((Weights.Double) weights);
		if (weights instanceof Weights.Bool)
			return (WeightsImpl<E>) new ImmutableView.Bool((Weights.Bool) weights);
		if (weights instanceof Weights.Char)
			return (WeightsImpl<E>) new ImmutableView.Char((Weights.Char) weights);
		return new ImmutableView.Obj<>(weights);
	}

	static interface Index<E> extends WeightsImpl<E> {

		Collection<E> values();

		Class<E> getTypeClass();

		static abstract class Abstract<E> implements WeightsImpl.Index<E> {

			final GraphElementSet elements;

			Abstract(GraphElementSet elements) {
				this.elements = Objects.requireNonNull(elements);
			}

			@Override
			public int size() {
				return elements.size();
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
				elements.checkIdx(idx);
			}
		}

		static abstract class Obj<E> extends WeightsImpl.Index.Abstract<E> {

			Object[] weights;
			final E defaultWeight;
			private final ObjectCollection<E> values;
			private final Class<E> type;

			Obj(GraphElementSet elements, E defVal, Class<E> type) {
				super(elements);

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

			Obj(WeightsImpl.Index.Obj<E> orig, GraphElementSet elements) {
				this(elements, orig.defaultWeight, orig.type);
				checkSameSize(elements, orig.elements);
				weights = Arrays.copyOf(orig.weights, elements.size());
			}

			Obj(WeightsImpl.Index.Obj<E> orig, GraphElementSet elements, IndexGraphBuilder.ReIndexingMap reIndexMap) {
				this(elements, orig.defaultWeight, orig.type);
				checkSameSize(elements, orig.elements);
				weights = Arrays.copyOf(weights, elements.size());
				final int s = elements.size();
				for (int i = 0; i < s; i++)
					weights[reIndexMap.origToReIndexed(i)] = orig.weights[i];
			}

			@Override
			@SuppressWarnings("unchecked")
			public E get(int idx) {
				checkIdx(idx);
				return (E) weights[idx];
			}

			@Override
			public E defaultWeight() {
				return defaultWeight;
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
			public boolean equals(Object other) {
				if (other == this)
					return true;
				if (!(other instanceof WeightsImpl.Index.Obj<?>))
					return false;
				WeightsImpl.Index.Obj<?> o = (WeightsImpl.Index.Obj<?>) other;
				return Arrays.equals(weights, 0, size(), o.weights, 0, o.size());
			}
		}

		static abstract class Byte extends WeightsImpl.Index.Abstract<java.lang.Byte> implements Weights.Byte {

			byte[] weights;
			final byte defaultWeight;
			private final ByteCollection values;

			Byte(GraphElementSet elements, byte defVal) {
				super(elements);

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

			Byte(WeightsImpl.Index.Byte orig, GraphElementSet elements) {
				this(elements, orig.defaultWeight);
				checkSameSize(elements, orig.elements);
				weights = Arrays.copyOf(orig.weights, elements.size());
			}

			Byte(WeightsImpl.Index.Byte orig, GraphElementSet elements, IndexGraphBuilder.ReIndexingMap reIndexMap) {
				this(elements, orig.defaultWeight);
				checkSameSize(elements, orig.elements);
				weights = Arrays.copyOf(weights, elements.size());
				final int s = elements.size();
				for (int i = 0; i < s; i++)
					weights[reIndexMap.origToReIndexed(i)] = orig.weights[i];
			}

			@Override
			public byte getByte(int idx) {
				checkIdx(idx);
				return weights[idx];
			}

			@Override
			public byte defaultWeightByte() {
				return defaultWeight;
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
			public boolean equals(Object other) {
				if (this == other)
					return true;
				if (!(other instanceof WeightsImpl.Index.Byte))
					return false;
				WeightsImpl.Index.Byte o = (WeightsImpl.Index.Byte) other;
				return Arrays.equals(weights, 0, size(), o.weights, 0, o.size());
			}
		}

		static abstract class Short extends WeightsImpl.Index.Abstract<java.lang.Short> implements Weights.Short {

			short[] weights;
			final short defaultWeight;
			private final ShortCollection values;

			Short(GraphElementSet elements, short defVal) {
				super(elements);

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

			Short(WeightsImpl.Index.Short orig, GraphElementSet elements) {
				this(elements, orig.defaultWeight);
				checkSameSize(elements, orig.elements);
				weights = Arrays.copyOf(orig.weights, elements.size());
			}

			Short(WeightsImpl.Index.Short orig, GraphElementSet elements, IndexGraphBuilder.ReIndexingMap reIndexMap) {
				this(elements, orig.defaultWeight);
				checkSameSize(elements, orig.elements);
				weights = Arrays.copyOf(weights, elements.size());
				final int s = elements.size();
				for (int i = 0; i < s; i++)
					weights[reIndexMap.origToReIndexed(i)] = orig.weights[i];
			}

			@Override
			public short getShort(int idx) {
				checkIdx(idx);
				return weights[idx];
			}

			@Override
			public short defaultWeightShort() {
				return defaultWeight;
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
			public boolean equals(Object other) {
				if (this == other)
					return true;
				if (!(other instanceof WeightsImpl.Index.Short))
					return false;
				WeightsImpl.Index.Short o = (WeightsImpl.Index.Short) other;
				return Arrays.equals(weights, 0, size(), o.weights, 0, o.size());
			}
		}

		static abstract class Int extends WeightsImpl.Index.Abstract<Integer> implements Weights.Int {

			int[] weights;
			final int defaultWeight;
			private final IntCollection values;

			Int(GraphElementSet elements, int defVal) {
				super(elements);

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

			Int(WeightsImpl.Index.Int orig, GraphElementSet elements) {
				this(elements, orig.defaultWeight);
				checkSameSize(elements, orig.elements);
				weights = Arrays.copyOf(orig.weights, elements.size());
			}

			Int(WeightsImpl.Index.Int orig, GraphElementSet elements, IndexGraphBuilder.ReIndexingMap reIndexMap) {
				this(elements, orig.defaultWeight);
				checkSameSize(elements, orig.elements);
				weights = Arrays.copyOf(weights, elements.size());
				final int s = elements.size();
				for (int i = 0; i < s; i++)
					weights[reIndexMap.origToReIndexed(i)] = orig.weights[i];
			}

			@Override
			public int getInt(int idx) {
				checkIdx(idx);
				return weights[idx];
			}

			@Override
			public int defaultWeightInt() {
				return defaultWeight;
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
			public boolean equals(Object other) {
				if (this == other)
					return true;
				if (!(other instanceof WeightsImpl.Index.Int))
					return false;
				WeightsImpl.Index.Int o = (WeightsImpl.Index.Int) other;
				return Arrays.equals(weights, 0, size(), o.weights, 0, o.size());
			}
		}

		static abstract class Long extends WeightsImpl.Index.Abstract<java.lang.Long> implements Weights.Long {

			long[] weights;
			final long defaultWeight;
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

			Long(GraphElementSet elements, long defVal) {
				super(elements);

				weights = LongArrays.EMPTY_ARRAY;
				defaultWeight = defVal;
			}

			Long(WeightsImpl.Index.Long orig, GraphElementSet elements) {
				this(elements, orig.defaultWeight);
				checkSameSize(elements, orig.elements);
				weights = Arrays.copyOf(orig.weights, elements.size());
			}

			Long(WeightsImpl.Index.Long orig, GraphElementSet elements, IndexGraphBuilder.ReIndexingMap reIndexMap) {
				this(elements, orig.defaultWeight);
				checkSameSize(elements, orig.elements);
				weights = Arrays.copyOf(weights, elements.size());
				final int s = elements.size();
				for (int i = 0; i < s; i++)
					weights[reIndexMap.origToReIndexed(i)] = orig.weights[i];
			}

			@Override
			public long getLong(int idx) {
				checkIdx(idx);
				return weights[idx];
			}

			@Override
			public long defaultWeightLong() {
				return defaultWeight;
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
			public boolean equals(Object other) {
				if (this == other)
					return true;
				if (!(other instanceof WeightsImpl.Index.Long))
					return false;
				WeightsImpl.Index.Long o = (WeightsImpl.Index.Long) other;
				return Arrays.equals(weights, 0, size(), o.weights, 0, o.size());
			}
		}

		static abstract class Float extends WeightsImpl.Index.Abstract<java.lang.Float> implements Weights.Float {

			float[] weights;
			final float defaultWeight;
			private final FloatCollection values;

			Float(GraphElementSet elements, float defVal) {
				super(elements);

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

			Float(WeightsImpl.Index.Float orig, GraphElementSet elements) {
				this(elements, orig.defaultWeight);
				checkSameSize(elements, orig.elements);
				weights = Arrays.copyOf(orig.weights, elements.size());
			}

			Float(WeightsImpl.Index.Float orig, GraphElementSet elements, IndexGraphBuilder.ReIndexingMap reIndexMap) {
				this(elements, orig.defaultWeight);
				checkSameSize(elements, orig.elements);
				weights = Arrays.copyOf(weights, elements.size());
				final int s = elements.size();
				for (int i = 0; i < s; i++)
					weights[reIndexMap.origToReIndexed(i)] = orig.weights[i];
			}

			@Override
			public float getFloat(int idx) {
				checkIdx(idx);
				return weights[idx];
			}

			@Override
			public float defaultWeightFloat() {
				return defaultWeight;
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
			public boolean equals(Object other) {
				if (this == other)
					return true;
				if (!(other instanceof WeightsImpl.Index.Float))
					return false;
				WeightsImpl.Index.Float o = (WeightsImpl.Index.Float) other;
				return Arrays.equals(weights, 0, size(), o.weights, 0, o.size());
			}
		}

		static abstract class Double extends WeightsImpl.Index.Abstract<java.lang.Double> implements Weights.Double {

			double[] weights;
			final double defaultWeight;
			private final DoubleCollection values;

			Double(GraphElementSet elements, double defVal) {
				super(elements);

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

			Double(WeightsImpl.Index.Double orig, GraphElementSet elements) {
				this(elements, orig.defaultWeight);
				checkSameSize(elements, orig.elements);
				weights = Arrays.copyOf(orig.weights, elements.size());
			}

			Double(WeightsImpl.Index.Double orig, GraphElementSet elements,
					IndexGraphBuilder.ReIndexingMap reIndexMap) {
				this(elements, orig.defaultWeight);
				checkSameSize(elements, orig.elements);
				weights = Arrays.copyOf(weights, elements.size());
				final int s = elements.size();
				for (int i = 0; i < s; i++)
					weights[reIndexMap.origToReIndexed(i)] = orig.weights[i];
			}

			@Override
			public double getDouble(int idx) {
				checkIdx(idx);
				return weights[idx];
			}

			@Override
			public double defaultWeightDouble() {
				return defaultWeight;
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
			public boolean equals(Object other) {
				if (this == other)
					return true;
				if (!(other instanceof WeightsImpl.Index.Double))
					return false;
				WeightsImpl.Index.Double o = (WeightsImpl.Index.Double) other;
				return Arrays.equals(weights, 0, size(), o.weights, 0, o.size());
			}
		}

		static abstract class Bool extends WeightsImpl.Index.Abstract<Boolean> implements Weights.Bool {

			final BitSet weights;
			int capacity;
			final boolean defaultWeight;
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
							Assertions.Iters.hasNext(this);
							return weights.get(idx++);
						}

						@Override
						public boolean previousBoolean() {
							Assertions.Iters.hasPrevious(this);
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

			Bool(GraphElementSet elements, boolean defVal) {
				super(elements);

				defaultWeight = defVal;
				weights = new BitSet();
			}

			Bool(WeightsImpl.Index.Bool orig, GraphElementSet elements) {
				super(elements);
				checkSameSize(elements, orig.elements);
				defaultWeight = orig.defaultWeight;
				if (defaultWeight) {
					weights = orig.weights.get(0, elements.size());
				} else {
					weights = orig.weights.get(0, orig.weights.previousSetBit(elements.size() - 1) + 1);
				}
			}

			Bool(WeightsImpl.Index.Bool orig, GraphElementSet elements, IndexGraphBuilder.ReIndexingMap reIndexMap) {
				this(elements, orig.defaultWeight);
				checkSameSize(elements, orig.elements);
				final int s = elements.size();
				for (int i = 0; i < s; i++)
					weights.set(reIndexMap.origToReIndexed(i), orig.weights.get(i));
			}

			@Override
			public boolean getBool(int idx) {
				checkIdx(idx);
				return weights.get(idx);
			}

			@Override
			public boolean defaultWeightBool() {
				return defaultWeight;
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
			public boolean equals(Object other) {
				if (this == other)
					return true;
				if (!(other instanceof WeightsImpl.Index.Bool))
					return false;
				WeightsImpl.Index.Bool o = (WeightsImpl.Index.Bool) other;
				final int s = size();
				if (s != o.size())
					return false;
				for (int i = 0; i < s; i++)
					if (weights.get(i) != o.weights.get(i))
						return false;
				return true;
			}
		}

		static abstract class Char extends WeightsImpl.Index.Abstract<Character> implements Weights.Char {

			char[] weights;
			final char defaultWeight;
			private final CharCollection values;

			Char(GraphElementSet elements, char defVal) {
				super(elements);

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

			Char(WeightsImpl.Index.Char orig, GraphElementSet elements) {
				this(elements, orig.defaultWeight);
				checkSameSize(elements, orig.elements);
				weights = Arrays.copyOf(orig.weights, elements.size());
			}

			Char(WeightsImpl.Index.Char orig, GraphElementSet elements, IndexGraphBuilder.ReIndexingMap reIndexMap) {
				this(elements, orig.defaultWeight);
				checkSameSize(elements, orig.elements);
				weights = Arrays.copyOf(weights, elements.size());
				final int s = elements.size();
				for (int i = 0; i < s; i++)
					weights[reIndexMap.origToReIndexed(i)] = orig.weights[i];
			}

			@Override
			public char getChar(int idx) {
				checkIdx(idx);
				return weights[idx];
			}

			@Override
			public char defaultWeightChar() {
				return defaultWeight;
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
			public boolean equals(Object other) {
				if (this == other)
					return true;
				if (!(other instanceof WeightsImpl.Index.Char))
					return false;
				WeightsImpl.Index.Char o = (WeightsImpl.Index.Char) other;
				return Arrays.equals(weights, 0, size(), o.weights, 0, o.size());
			}
		}

	}

	static interface IndexImmutable<E> extends WeightsImpl.Index<E> {

		static WeightsImpl.IndexImmutable<?> copyOf(Weights<?> weights, GraphElementSet.FixedSize elements) {
			if (weights instanceof WeightsImpl.ImmutableView<?>)
				weights = ((WeightsImpl.ImmutableView<?>) weights).weights;
			if (weights instanceof WeightsImpl.Index.Byte) {
				return new WeightsImpl.IndexImmutable.Byte((WeightsImpl.Index.Byte) weights, elements);
			} else if (weights instanceof WeightsImpl.Index.Short) {
				return new WeightsImpl.IndexImmutable.Short((WeightsImpl.Index.Short) weights, elements);
			} else if (weights instanceof WeightsImpl.Index.Int) {
				return new WeightsImpl.IndexImmutable.Int((WeightsImpl.Index.Int) weights, elements);
			} else if (weights instanceof WeightsImpl.Index.Long) {
				return new WeightsImpl.IndexImmutable.Long((WeightsImpl.Index.Long) weights, elements);
			} else if (weights instanceof WeightsImpl.Index.Float) {
				return new WeightsImpl.IndexImmutable.Float((WeightsImpl.Index.Float) weights, elements);
			} else if (weights instanceof WeightsImpl.Index.Double) {
				return new WeightsImpl.IndexImmutable.Double((WeightsImpl.Index.Double) weights, elements);
			} else if (weights instanceof WeightsImpl.Index.Bool) {
				return new WeightsImpl.IndexImmutable.Bool((WeightsImpl.Index.Bool) weights, elements);
			} else if (weights instanceof WeightsImpl.Index.Char) {
				return new WeightsImpl.IndexImmutable.Char((WeightsImpl.Index.Char) weights, elements);
			} else if (weights instanceof WeightsImpl.Index.Obj) {
				return new WeightsImpl.IndexImmutable.Obj<>((WeightsImpl.Index.Obj<?>) weights, elements);
			} else {
				throw new IllegalArgumentException("unknown weights implementation: " + weights.getClass());
			}
		}

		static WeightsImpl.IndexImmutable<?> copyOfReindexed(Weights<?> weights, GraphElementSet.FixedSize elements,
				IndexGraphBuilder.ReIndexingMap reIndexMap) {
			if (weights instanceof WeightsImpl.ImmutableView<?>)
				weights = ((WeightsImpl.ImmutableView<?>) weights).weights;
			if (weights instanceof WeightsImpl.Index.Byte) {
				return new WeightsImpl.IndexImmutable.Byte((WeightsImpl.Index.Byte) weights, elements, reIndexMap);
			} else if (weights instanceof WeightsImpl.Index.Short) {
				return new WeightsImpl.IndexImmutable.Short((WeightsImpl.Index.Short) weights, elements, reIndexMap);
			} else if (weights instanceof WeightsImpl.Index.Int) {
				return new WeightsImpl.IndexImmutable.Int((WeightsImpl.Index.Int) weights, elements, reIndexMap);
			} else if (weights instanceof WeightsImpl.Index.Long) {
				return new WeightsImpl.IndexImmutable.Long((WeightsImpl.Index.Long) weights, elements, reIndexMap);
			} else if (weights instanceof WeightsImpl.Index.Float) {
				return new WeightsImpl.IndexImmutable.Float((WeightsImpl.Index.Float) weights, elements, reIndexMap);
			} else if (weights instanceof WeightsImpl.Index.Double) {
				return new WeightsImpl.IndexImmutable.Double((WeightsImpl.Index.Double) weights, elements, reIndexMap);
			} else if (weights instanceof WeightsImpl.Index.Bool) {
				return new WeightsImpl.IndexImmutable.Bool((WeightsImpl.Index.Bool) weights, elements, reIndexMap);
			} else if (weights instanceof WeightsImpl.Index.Char) {
				return new WeightsImpl.IndexImmutable.Char((WeightsImpl.Index.Char) weights, elements, reIndexMap);
			} else if (weights instanceof WeightsImpl.Index.Obj) {
				return new WeightsImpl.IndexImmutable.Obj<>((WeightsImpl.Index.Obj<?>) weights, elements, reIndexMap);
			} else {
				throw new IllegalArgumentException("unknown weights implementation: " + weights.getClass());
			}
		}

		static class Obj<E> extends WeightsImpl.Index.Obj<E> implements WeightsImpl.IndexImmutable<E> {

			Obj(WeightsImpl.Index.Obj<E> orig, GraphElementSet.FixedSize elements) {
				super(orig, elements);
			}

			Obj(WeightsImpl.Index.Obj<E> orig, GraphElementSet.FixedSize elements,
					IndexGraphBuilder.ReIndexingMap reIndexMap) {
				super(orig, elements, reIndexMap);
			}

			@Override
			public void set(int idx, E weight) {
				throw new UnsupportedOperationException("immutable weights");
			}
		}

		static class Byte extends WeightsImpl.Index.Byte implements WeightsImpl.IndexImmutable<java.lang.Byte> {

			Byte(WeightsImpl.Index.Byte orig, GraphElementSet.FixedSize elements) {
				super(orig, elements);
			}

			Byte(WeightsImpl.Index.Byte orig, GraphElementSet.FixedSize elements,
					IndexGraphBuilder.ReIndexingMap reIndexMap) {
				super(orig, elements, reIndexMap);
			}

			@Override
			public void set(int idx, byte weight) {
				throw new UnsupportedOperationException("immutable weights");
			}
		}

		static class Short extends WeightsImpl.Index.Short implements WeightsImpl.IndexImmutable<java.lang.Short> {

			Short(WeightsImpl.Index.Short orig, GraphElementSet.FixedSize elements) {
				super(orig, elements);
			}

			Short(WeightsImpl.Index.Short orig, GraphElementSet.FixedSize elements,
					IndexGraphBuilder.ReIndexingMap reIndexMap) {
				super(orig, elements, reIndexMap);
			}

			@Override
			public void set(int idx, short weight) {
				throw new UnsupportedOperationException("immutable weights");
			}
		}

		static class Int extends WeightsImpl.Index.Int implements WeightsImpl.IndexImmutable<Integer> {

			Int(WeightsImpl.Index.Int orig, GraphElementSet.FixedSize elements) {
				super(orig, elements);
			}

			Int(WeightsImpl.Index.Int orig, GraphElementSet.FixedSize elements,
					IndexGraphBuilder.ReIndexingMap reIndexMap) {
				super(orig, elements, reIndexMap);
			}

			@Override
			public void set(int idx, int weight) {
				throw new UnsupportedOperationException("immutable weights");
			}
		}

		static class Long extends WeightsImpl.Index.Long implements WeightsImpl.IndexImmutable<java.lang.Long> {

			Long(WeightsImpl.Index.Long orig, GraphElementSet.FixedSize elements) {
				super(orig, elements);
			}

			Long(WeightsImpl.Index.Long orig, GraphElementSet.FixedSize elements,
					IndexGraphBuilder.ReIndexingMap reIndexMap) {
				super(orig, elements, reIndexMap);
			}

			@Override
			public void set(int idx, long weight) {
				throw new UnsupportedOperationException("immutable weights");
			}
		}

		static class Float extends WeightsImpl.Index.Float implements WeightsImpl.IndexImmutable<java.lang.Float> {

			Float(WeightsImpl.Index.Float orig, GraphElementSet.FixedSize elements) {
				super(orig, elements);
			}

			Float(WeightsImpl.Index.Float orig, GraphElementSet.FixedSize elements,
					IndexGraphBuilder.ReIndexingMap reIndexMap) {
				super(orig, elements, reIndexMap);
			}

			@Override
			public void set(int idx, float weight) {
				throw new UnsupportedOperationException("immutable weights");
			}
		}

		static class Double extends WeightsImpl.Index.Double implements WeightsImpl.IndexImmutable<java.lang.Double> {

			Double(WeightsImpl.Index.Double orig, GraphElementSet.FixedSize elements) {
				super(orig, elements);
			}

			Double(WeightsImpl.Index.Double orig, GraphElementSet.FixedSize elements,
					IndexGraphBuilder.ReIndexingMap reIndexMap) {
				super(orig, elements, reIndexMap);
			}

			@Override
			public void set(int idx, double weight) {
				throw new UnsupportedOperationException("immutable weights");
			}
		}

		static class Bool extends WeightsImpl.Index.Bool implements WeightsImpl.IndexImmutable<Boolean> {

			Bool(WeightsImpl.Index.Bool orig, GraphElementSet.FixedSize elements) {
				super(orig, elements);
			}

			Bool(WeightsImpl.Index.Bool orig, GraphElementSet.FixedSize elements,
					IndexGraphBuilder.ReIndexingMap reIndexMap) {
				super(orig, elements, reIndexMap);
			}

			@Override
			public void set(int idx, boolean weight) {
				throw new UnsupportedOperationException("immutable weights");
			}
		}

		static class Char extends WeightsImpl.Index.Char implements WeightsImpl.IndexImmutable<Character> {

			Char(WeightsImpl.Index.Char orig, GraphElementSet.FixedSize elements) {
				super(orig, elements);
			}

			Char(WeightsImpl.Index.Char orig, GraphElementSet.FixedSize elements,
					IndexGraphBuilder.ReIndexingMap reIndexMap) {
				super(orig, elements, reIndexMap);
			}

			@Override
			public void set(int idx, char weight) {
				throw new UnsupportedOperationException("immutable weights");
			}
		}

		static class Builder {

			private final GraphElementSet.FixedSize elements;
			private final Map<Object, WeightsImpl.IndexImmutable<?>> weights;

			Builder(GraphElementSet.FixedSize elements) {
				this.elements = Objects.requireNonNull(elements);
				weights = new Object2ObjectOpenHashMap<>();
			}

			void copyAndAddWeights(Object key, Weights<?> weights) {
				Object oldWeights = this.weights.put(key, WeightsImpl.IndexImmutable.copyOf(weights, elements));
				if (oldWeights != null)
					throw new IllegalArgumentException("duplicate key: " + key);
			}

			void copyAndAddWeightsReindexed(Object key, Weights<?> weights,
					IndexGraphBuilder.ReIndexingMap reIndexMap) {
				Object oldWeights = this.weights.put(key,
						WeightsImpl.IndexImmutable.copyOfReindexed(weights, elements, reIndexMap));
				if (oldWeights != null)
					throw new IllegalArgumentException("duplicate key: " + key);
			}

			Map<Object, WeightsImpl.IndexImmutable<?>> build() {
				return Map.copyOf(weights);
			}
		}
	}

	static interface IndexMutable<E> extends WeightsImpl.Index<E> {

		int capacity();

		void expand(int newCapacity);

		void clear(int idx);

		void clear();

		void swap(int idx1, int idx2);

		static <D> WeightsImpl.IndexMutable<D> newInstance(GraphElementSet elements, Class<? super D> type, D defVal) {
			@SuppressWarnings("rawtypes")
			WeightsImpl container;
			if (type == byte.class) {
				byte defVal0 = defVal != null ? ((java.lang.Byte) defVal).byteValue() : 0;
				container = new WeightsImpl.IndexMutable.Byte(elements, defVal0);

			} else if (type == short.class) {
				short defVal0 = defVal != null ? ((java.lang.Short) defVal).shortValue() : 0;
				container = new WeightsImpl.IndexMutable.Short(elements, defVal0);

			} else if (type == int.class) {
				int defVal0 = defVal != null ? ((Integer) defVal).intValue() : 0;
				container = new WeightsImpl.IndexMutable.Int(elements, defVal0);

			} else if (type == long.class) {
				long defVal0 = defVal != null ? ((java.lang.Long) defVal).longValue() : 0;
				container = new WeightsImpl.IndexMutable.Long(elements, defVal0);

			} else if (type == float.class) {
				float defVal0 = defVal != null ? ((java.lang.Float) defVal).floatValue() : 0;
				container = new WeightsImpl.IndexMutable.Float(elements, defVal0);

			} else if (type == double.class) {
				double defVal0 = defVal != null ? ((java.lang.Double) defVal).doubleValue() : 0;
				container = new WeightsImpl.IndexMutable.Double(elements, defVal0);

			} else if (type == boolean.class) {
				boolean defVal0 = defVal != null ? ((Boolean) defVal).booleanValue() : false;
				container = new WeightsImpl.IndexMutable.Bool(elements, defVal0);

			} else if (type == char.class) {
				char defVal0 = defVal != null ? ((Character) defVal).charValue() : 0;
				container = new WeightsImpl.IndexMutable.Char(elements, defVal0);

			} else {
				container = new WeightsImpl.IndexMutable.Obj<>(elements, defVal, type);
			}
			@SuppressWarnings("unchecked")
			WeightsImpl.IndexMutable<D> container0 = (WeightsImpl.IndexMutable<D>) container;
			return container0;
		}

		static WeightsImpl.IndexMutable<?> copyOf(Weights<?> weights, GraphElementSet elements) {
			if (weights instanceof WeightsImpl.ImmutableView<?>)
				weights = ((WeightsImpl.ImmutableView<?>) weights).weights;
			if (weights instanceof WeightsImpl.Index.Byte) {
				return new WeightsImpl.IndexMutable.Byte((WeightsImpl.Index.Byte) weights, elements);
			} else if (weights instanceof WeightsImpl.Index.Short) {
				return new WeightsImpl.IndexMutable.Short((WeightsImpl.Index.Short) weights, elements);
			} else if (weights instanceof WeightsImpl.Index.Int) {
				return new WeightsImpl.IndexMutable.Int((WeightsImpl.Index.Int) weights, elements);
			} else if (weights instanceof WeightsImpl.Index.Long) {
				return new WeightsImpl.IndexMutable.Long((WeightsImpl.Index.Long) weights, elements);
			} else if (weights instanceof WeightsImpl.Index.Float) {
				return new WeightsImpl.IndexMutable.Float((WeightsImpl.Index.Float) weights, elements);
			} else if (weights instanceof WeightsImpl.Index.Double) {
				return new WeightsImpl.IndexMutable.Double((WeightsImpl.Index.Double) weights, elements);
			} else if (weights instanceof WeightsImpl.Index.Bool) {
				return new WeightsImpl.IndexMutable.Bool((WeightsImpl.Index.Bool) weights, elements);
			} else if (weights instanceof WeightsImpl.Index.Char) {
				return new WeightsImpl.IndexMutable.Char((WeightsImpl.Index.Char) weights, elements);
			} else if (weights instanceof WeightsImpl.Index.Obj) {
				return new WeightsImpl.IndexMutable.Obj<>((WeightsImpl.Index.Obj<?>) weights, elements);
			} else {
				throw new IllegalArgumentException("unknown weights implementation: " + weights.getClass());
			}
		}

		static class Obj<E> extends WeightsImpl.Index.Obj<E> implements WeightsImpl.IndexMutable<E> {

			Obj(GraphElementSet elements, E defVal, Class<E> type) {
				super(elements, defVal, type);
			}

			Obj(WeightsImpl.Index.Obj<E> orig, GraphElementSet elements) {
				super(orig, elements);
			}

			@Override
			public void set(int idx, E weight) {
				checkIdx(idx);
				weights[idx] = weight;
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
		}

		static class Byte extends WeightsImpl.Index.Byte implements WeightsImpl.IndexMutable<java.lang.Byte> {

			Byte(GraphElementSet elements, byte defVal) {
				super(elements, defVal);
			}

			Byte(WeightsImpl.Index.Byte orig, GraphElementSet elements) {
				super(orig, elements);
			}

			@Override
			public void set(int idx, byte weight) {
				checkIdx(idx);
				weights[idx] = weight;
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
		}

		static class Short extends WeightsImpl.Index.Short implements WeightsImpl.IndexMutable<java.lang.Short> {

			Short(GraphElementSet elements, short defVal) {
				super(elements, defVal);
			}

			Short(WeightsImpl.Index.Short orig, GraphElementSet elements) {
				super(orig, elements);
			}

			@Override
			public void set(int idx, short weight) {
				checkIdx(idx);
				weights[idx] = weight;
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
		}

		static class Int extends WeightsImpl.Index.Int implements WeightsImpl.IndexMutable<Integer> {

			Int(GraphElementSet elements, int defVal) {
				super(elements, defVal);
			}

			Int(WeightsImpl.Index.Int orig, GraphElementSet elements) {
				super(orig, elements);
			}

			@Override
			public void set(int idx, int weight) {
				checkIdx(idx);
				weights[idx] = weight;
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
		}

		static class Long extends WeightsImpl.Index.Long implements WeightsImpl.IndexMutable<java.lang.Long> {

			Long(GraphElementSet elements, long defVal) {
				super(elements, defVal);
			}

			Long(WeightsImpl.Index.Long orig, GraphElementSet elements) {
				super(orig, elements);
			}

			@Override
			public void set(int idx, long weight) {
				checkIdx(idx);
				weights[idx] = weight;
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
		}

		static class Float extends WeightsImpl.Index.Float implements WeightsImpl.IndexMutable<java.lang.Float> {

			Float(GraphElementSet elements, float defVal) {
				super(elements, defVal);
			}

			Float(WeightsImpl.Index.Float orig, GraphElementSet elements) {
				super(orig, elements);
			}

			@Override
			public void set(int idx, float weight) {
				checkIdx(idx);
				weights[idx] = weight;
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
		}

		static class Double extends WeightsImpl.Index.Double implements WeightsImpl.IndexMutable<java.lang.Double> {

			Double(GraphElementSet elements, double defVal) {
				super(elements, defVal);
			}

			Double(WeightsImpl.Index.Double orig, GraphElementSet elements) {
				super(orig, elements);
			}

			@Override
			public void set(int idx, double weight) {
				checkIdx(idx);
				weights[idx] = weight;
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
		}

		static class Bool extends WeightsImpl.Index.Bool implements WeightsImpl.IndexMutable<Boolean> {

			Bool(GraphElementSet elements, boolean defVal) {
				super(elements, defVal);
			}

			Bool(WeightsImpl.Index.Bool orig, GraphElementSet elements) {
				super(orig, elements);
			}

			@Override
			public void set(int idx, boolean weight) {
				checkIdx(idx);
				weights.set(idx, weight);
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
		}

		static class Char extends WeightsImpl.Index.Char implements WeightsImpl.IndexMutable<Character> {

			Char(GraphElementSet elements, char defVal) {
				super(elements, defVal);
			}

			Char(WeightsImpl.Index.Char orig, GraphElementSet elements) {
				super(orig, elements);
			}

			@Override
			public void set(int idx, char weight) {
				checkIdx(idx);
				weights[idx] = weight;
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
		}

		static class Manager {

			final Map<Object, WeightsImpl.IndexMutable<?>> weights = new Object2ObjectOpenHashMap<>();
			private int weightsCapacity;

			Manager(int initCapacity) {
				weightsCapacity = initCapacity;
			}

			Manager(Manager orig, GraphElementSet elements) {
				this(elements.size());
				for (var entry : orig.weights.entrySet())
					weights.put(entry.getKey(), WeightsImpl.IndexMutable.copyOf(entry.getValue(), elements));
			}

			void addWeights(Object key, WeightsImpl.IndexMutable<?> weight) {
				WeightsImpl.IndexMutable<?> oldContainer = weights.put(key, weight);
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
				for (WeightsImpl.IndexMutable<?> container : weights.values())
					container.expand(newCapacity);
				weightsCapacity = newCapacity;
			}

			void swapElements(int idx1, int idx2) {
				for (WeightsImpl.IndexMutable<?> container : weights.values())
					container.swap(idx1, idx2);
			}

			void clearElement(int idx) {
				for (WeightsImpl.IndexMutable<?> container : weights.values())
					container.clear(idx);
			}

			void clearContainers() {
				for (WeightsImpl.IndexMutable<?> container : weights.values())
					container.clear();
			}
		}
	}

	static abstract class Mapped<E> implements WeightsImpl<E> {

		final WeightsImpl.Index.Abstract<E> weights;
		final IndexIdMap indexMap;

		private Mapped(WeightsImpl.Index<E> weights, IndexIdMap indexMap) {
			this.weights = (WeightsImpl.Index.Abstract<E>) Objects.requireNonNull(weights);
			this.indexMap = indexMap;
		}

		WeightsImpl.Index.Abstract<E> weights() {
			return weights;
		}

		@Override
		public int size() {
			return weights.size();
		}

		static WeightsImpl.Mapped<?> newInstance(WeightsImpl.Index<?> weights, IndexIdMap indexMap) {
			if (weights instanceof WeightsImpl.Index.Byte) {
				return new WeightsImpl.Mapped.Byte((WeightsImpl.Index.Byte) weights, indexMap);
			} else if (weights instanceof WeightsImpl.Index.Short) {
				return new WeightsImpl.Mapped.Short((WeightsImpl.Index.Short) weights, indexMap);
			} else if (weights instanceof WeightsImpl.Index.Int) {
				return new WeightsImpl.Mapped.Int((WeightsImpl.Index.Int) weights, indexMap);
			} else if (weights instanceof WeightsImpl.Index.Long) {
				return new WeightsImpl.Mapped.Long((WeightsImpl.Index.Long) weights, indexMap);
			} else if (weights instanceof WeightsImpl.Index.Float) {
				return new WeightsImpl.Mapped.Float((WeightsImpl.Index.Float) weights, indexMap);
			} else if (weights instanceof WeightsImpl.Index.Double) {
				return new WeightsImpl.Mapped.Double((WeightsImpl.Index.Double) weights, indexMap);
			} else if (weights instanceof WeightsImpl.Index.Bool) {
				return new WeightsImpl.Mapped.Bool((WeightsImpl.Index.Bool) weights, indexMap);
			} else if (weights instanceof WeightsImpl.Index.Char) {
				return new WeightsImpl.Mapped.Char((WeightsImpl.Index.Char) weights, indexMap);
			} else {
				return new WeightsImpl.Mapped.Obj<>(weights, indexMap);
			}
		}

		static class Obj<E> extends Mapped<E> {
			Obj(WeightsImpl.Index<E> weights, IndexIdMap indexMap) {
				super(weights, indexMap);
			}

			@Override
			public WeightsImpl.Index.Obj<E> weights() {
				return (WeightsImpl.Index.Obj<E>) super.weights();
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

			@Override
			public boolean equals(Object other) {
				if (other == this)
					return true;
				if (!(other instanceof WeightsImpl))
					return false;
				WeightsImpl<?> o = (WeightsImpl<?>) other;

				WeightsImpl.Index.Obj<E> w = weights();
				int size = w.size();
				if (size != ((WeightsImpl<?>) other).size())
					return false;
				try {
					for (int idx = 0; idx < size; idx++)
						if (!Objects.equals(w.get(idx), o.get(indexMap.indexToId(idx))))
							return false;
				} catch (IndexOutOfBoundsException e) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				int h = 0;
				WeightsImpl.Index.Obj<E> w = weights();
				int size = w.size();
				for (int idx = 0; idx < size; idx++)
					/* we must use addition, order shouldn't matter */
					h += Objects.hashCode(w.get(idx));
				return h;
			}

			@Override
			public String toString() {
				WeightsImpl.Index.Obj<E> w = weights();
				int size = w.size();
				if (size == 0)
					return "[]";
				StringBuilder s = new StringBuilder().append('[');
				for (int idx = 0;; idx++) {
					int id = indexMap.indexToId(idx);
					s.append(id).append('=').append(w.get(idx));
					if (idx == size - 1)
						break;
					s.append(", ");
				}
				return s.append(']').toString();
			}
		}

		static class Byte extends Mapped<java.lang.Byte> implements Weights.Byte {
			Byte(WeightsImpl.Index.Byte weights, IndexIdMap indexMap) {
				super(weights, indexMap);
			}

			@Override
			public WeightsImpl.Index.Byte weights() {
				return (WeightsImpl.Index.Byte) super.weights();
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

			@Override
			public boolean equals(Object other) {
				if (other == this)
					return true;
				if (!(other instanceof WeightsImpl && other instanceof Weights.Byte))
					return false;
				Weights.Byte o = (Weights.Byte) other;

				WeightsImpl.Index.Byte w = weights();
				int size = w.size();
				if (size != ((WeightsImpl<?>) other).size())
					return false;
				try {
					for (int idx = 0; idx < size; idx++)
						if (w.getByte(idx) != o.getByte(indexMap.indexToId(idx)))
							return false;
				} catch (IndexOutOfBoundsException e) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				int h = 0;
				WeightsImpl.Index.Byte w = weights();
				int size = w.size();
				for (int idx = 0; idx < size; idx++)
					/* we must use addition, order shouldn't matter */
					h += w.getByte(idx);
				return h;
			}

			@Override
			public String toString() {
				WeightsImpl.Index.Byte w = weights();
				int size = w.size();
				if (size == 0)
					return "[]";
				StringBuilder s = new StringBuilder().append('[');
				for (int idx = 0;; idx++) {
					int id = indexMap.indexToId(idx);
					s.append(id).append('=').append(w.getByte(idx));
					if (idx == size - 1)
						break;
					s.append(", ");
				}
				return s.append(']').toString();
			}
		}

		static class Short extends Mapped<java.lang.Short> implements Weights.Short {
			Short(WeightsImpl.Index.Short container, IndexIdMap indexMap) {
				super(container, indexMap);
			}

			@Override
			public WeightsImpl.Index.Short weights() {
				return (WeightsImpl.Index.Short) super.weights();
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

			@Override
			public boolean equals(Object other) {
				if (other == this)
					return true;
				if (!(other instanceof WeightsImpl && other instanceof Weights.Short))
					return false;
				Weights.Short o = (Weights.Short) other;

				WeightsImpl.Index.Short w = weights();
				int size = w.size();
				if (size != ((WeightsImpl<?>) other).size())
					return false;
				try {
					for (int idx = 0; idx < size; idx++)
						if (w.getShort(idx) != o.getShort(indexMap.indexToId(idx)))
							return false;
				} catch (IndexOutOfBoundsException e) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				int h = 0;
				WeightsImpl.Index.Short w = weights();
				int size = w.size();
				for (int idx = 0; idx < size; idx++)
					/* we must use addition, order shouldn't matter */
					h += w.getShort(idx);
				return h;
			}

			@Override
			public String toString() {
				WeightsImpl.Index.Short w = weights();
				int size = w.size();
				if (size == 0)
					return "[]";
				StringBuilder s = new StringBuilder().append('[');
				for (int idx = 0;; idx++) {
					int id = indexMap.indexToId(idx);
					s.append(id).append('=').append(w.getShort(idx));
					if (idx == size - 1)
						break;
					s.append(", ");
				}
				return s.append(']').toString();
			}
		}

		static class Int extends Mapped<Integer> implements Weights.Int {
			Int(WeightsImpl.Index.Int container, IndexIdMap indexMap) {
				super(container, indexMap);
			}

			@Override
			public WeightsImpl.Index.Int weights() {
				return (WeightsImpl.Index.Int) super.weights();
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

			@Override
			public boolean equals(Object other) {
				if (other == this)
					return true;
				if (!(other instanceof WeightsImpl && other instanceof Weights.Int))
					return false;
				Weights.Int o = (Weights.Int) other;

				WeightsImpl.Index.Int w = weights();
				int size = w.size();
				if (size != ((WeightsImpl<?>) other).size())
					return false;
				try {
					for (int idx = 0; idx < size; idx++)
						if (w.getInt(idx) != o.getInt(indexMap.indexToId(idx)))
							return false;
				} catch (IndexOutOfBoundsException e) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				int h = 0;
				WeightsImpl.Index.Int w = weights();
				int size = w.size();
				for (int idx = 0; idx < size; idx++)
					/* we must use addition, order shouldn't matter */
					h += w.getInt(idx);
				return h;
			}

			@Override
			public String toString() {
				WeightsImpl.Index.Int w = weights();
				int size = w.size();
				if (size == 0)
					return "[]";
				StringBuilder s = new StringBuilder().append('[');
				for (int idx = 0;; idx++) {
					int id = indexMap.indexToId(idx);
					s.append(id).append('=').append(w.getInt(idx));
					if (idx == size - 1)
						break;
					s.append(", ");
				}
				return s.append(']').toString();
			}
		}

		static class Long extends Mapped<java.lang.Long> implements Weights.Long {
			Long(WeightsImpl.Index.Long container, IndexIdMap indexMap) {
				super(container, indexMap);
			}

			@Override
			public WeightsImpl.Index.Long weights() {
				return (WeightsImpl.Index.Long) super.weights();
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

			@Override
			public boolean equals(Object other) {
				if (other == this)
					return true;
				if (!(other instanceof WeightsImpl && other instanceof Weights.Long))
					return false;
				Weights.Long o = (Weights.Long) other;

				WeightsImpl.Index.Long w = weights();
				int size = w.size();
				if (size != ((WeightsImpl<?>) other).size())
					return false;
				try {
					for (int idx = 0; idx < size; idx++)
						if (w.getLong(idx) != o.getLong(indexMap.indexToId(idx)))
							return false;
				} catch (IndexOutOfBoundsException e) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				int h = 0;
				WeightsImpl.Index.Long w = weights();
				int size = w.size();
				for (int idx = 0; idx < size; idx++)
					/* we must use addition, order shouldn't matter */
					h += java.lang.Long.hashCode(w.getLong(idx));
				return h;
			}

			@Override
			public String toString() {
				WeightsImpl.Index.Long w = weights();
				int size = w.size();
				if (size == 0)
					return "[]";
				StringBuilder s = new StringBuilder().append('[');
				for (int idx = 0;; idx++) {
					int id = indexMap.indexToId(idx);
					s.append(id).append('=').append(w.getLong(idx));
					if (idx == size - 1)
						break;
					s.append(", ");
				}
				return s.append(']').toString();
			}
		}

		static class Float extends Mapped<java.lang.Float> implements Weights.Float {
			Float(WeightsImpl.Index.Float container, IndexIdMap indexMap) {
				super(container, indexMap);
			}

			@Override
			public WeightsImpl.Index.Float weights() {
				return (WeightsImpl.Index.Float) super.weights();
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

			@Override
			public boolean equals(Object other) {
				if (other == this)
					return true;
				if (!(other instanceof WeightsImpl && other instanceof Weights.Float))
					return false;
				Weights.Float o = (Weights.Float) other;

				WeightsImpl.Index.Float w = weights();
				int size = w.size();
				if (size != ((WeightsImpl<?>) other).size())
					return false;
				try {
					for (int idx = 0; idx < size; idx++)
						if (w.getFloat(idx) != o.getFloat(indexMap.indexToId(idx)))
							return false;
				} catch (IndexOutOfBoundsException e) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				int h = 0;
				WeightsImpl.Index.Float w = weights();
				int size = w.size();
				for (int idx = 0; idx < size; idx++)
					/* we must use addition, order shouldn't matter */
					h += java.lang.Float.hashCode(w.getFloat(idx));
				return h;
			}

			@Override
			public String toString() {
				WeightsImpl.Index.Float w = weights();
				int size = w.size();
				if (size == 0)
					return "[]";
				StringBuilder s = new StringBuilder().append('[');
				for (int idx = 0;; idx++) {
					int id = indexMap.indexToId(idx);
					s.append(id).append('=').append(w.getFloat(idx));
					if (idx == size - 1)
						break;
					s.append(", ");
				}
				return s.append(']').toString();
			}
		}

		static class Double extends Mapped<java.lang.Double> implements Weights.Double {
			Double(WeightsImpl.Index.Double container, IndexIdMap indexMap) {
				super(container, indexMap);
			}

			@Override
			WeightsImpl.Index.Double weights() {
				return (WeightsImpl.Index.Double) super.weights();
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

			@Override
			public boolean equals(Object other) {
				if (other == this)
					return true;
				if (!(other instanceof WeightsImpl && other instanceof Weights.Double))
					return false;
				Weights.Double o = (Weights.Double) other;

				WeightsImpl.Index.Double w = weights();
				int size = w.size();
				if (size != ((WeightsImpl<?>) other).size())
					return false;
				try {
					for (int idx = 0; idx < size; idx++)
						if (w.getDouble(idx) != o.getDouble(indexMap.indexToId(idx)))
							return false;
				} catch (IndexOutOfBoundsException e) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				int h = 0;
				WeightsImpl.Index.Double w = weights();
				int size = w.size();
				for (int idx = 0; idx < size; idx++)
					/* we must use addition, order shouldn't matter */
					h += java.lang.Double.hashCode(w.getDouble(idx));
				return h;
			}

			@Override
			public String toString() {
				WeightsImpl.Index.Double w = weights();
				int size = w.size();
				if (size == 0)
					return "[]";
				StringBuilder s = new StringBuilder().append('[');
				for (int idx = 0;; idx++) {
					int id = indexMap.indexToId(idx);
					s.append(id).append('=').append(w.getDouble(idx));
					if (idx == size - 1)
						break;
					s.append(", ");
				}
				return s.append(']').toString();
			}
		}

		static class Bool extends Mapped<Boolean> implements Weights.Bool {
			Bool(WeightsImpl.Index.Bool container, IndexIdMap indexMap) {
				super(container, indexMap);
			}

			@Override
			WeightsImpl.Index.Bool weights() {
				return (WeightsImpl.Index.Bool) super.weights();
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

			@Override
			public boolean equals(Object other) {
				if (other == this)
					return true;
				if (!(other instanceof WeightsImpl && other instanceof Weights.Bool))
					return false;
				Weights.Bool o = (Weights.Bool) other;

				WeightsImpl.Index.Bool w = weights();
				int size = w.size();
				if (size != ((WeightsImpl<?>) other).size())
					return false;
				try {
					for (int idx = 0; idx < size; idx++)
						if (w.getBool(idx) != o.getBool(indexMap.indexToId(idx)))
							return false;
				} catch (IndexOutOfBoundsException e) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				int h = 0;
				WeightsImpl.Index.Bool w = weights();
				int size = w.size();
				for (int idx = 0; idx < size; idx++)
					/* we must use addition, order shouldn't matter */
					h += Boolean.hashCode(w.getBool(idx));
				return h;
			}

			@Override
			public String toString() {
				WeightsImpl.Index.Bool w = weights();
				int size = w.size();
				if (size == 0)
					return "[]";
				StringBuilder s = new StringBuilder().append('[');
				for (int idx = 0;; idx++) {
					int id = indexMap.indexToId(idx);
					s.append(id).append('=').append(w.getBool(idx));
					if (idx == size - 1)
						break;
					s.append(", ");
				}
				return s.append(']').toString();
			}
		}

		static class Char extends Mapped<Character> implements Weights.Char {
			Char(WeightsImpl.Index.Char container, IndexIdMap indexMap) {
				super(container, indexMap);
			}

			@Override
			WeightsImpl.Index.Char weights() {
				return (WeightsImpl.Index.Char) super.weights();
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

			@Override
			public boolean equals(Object other) {
				if (other == this)
					return true;
				if (!(other instanceof WeightsImpl && other instanceof Weights.Char))
					return false;
				Weights.Char o = (Weights.Char) other;

				WeightsImpl.Index.Char w = weights();
				int size = w.size();
				if (size != ((WeightsImpl<?>) other).size())
					return false;
				try {
					for (int idx = 0; idx < size; idx++)
						if (w.getChar(idx) != o.getChar(indexMap.indexToId(idx)))
							return false;
				} catch (IndexOutOfBoundsException e) {
					return false;
				}
				return true;
			}

			@Override
			public int hashCode() {
				int h = 0;
				WeightsImpl.Index.Char w = weights();
				int size = w.size();
				for (int idx = 0; idx < size; idx++)
					/* we must use addition, order shouldn't matter */
					h += w.getChar(idx);
				return h;
			}

			@Override
			public String toString() {
				WeightsImpl.Index.Char w = weights();
				int size = w.size();
				if (size == 0)
					return "[]";
				StringBuilder s = new StringBuilder().append('[');
				for (int idx = 0;; idx++) {
					int id = indexMap.indexToId(idx);
					s.append(id).append('=').append(w.getChar(idx));
					if (idx == size - 1)
						break;
					s.append(", ");
				}
				return s.append(']').toString();
			}
		}
	}

	/**
	 * Tag interface for graphs that can not be muted/changed/altered
	 *
	 * @author Barak Ugav
	 */
	static interface Immutable<E> extends WeightsImpl<E> {
	}

	static abstract class ImmutableView<E> implements Immutable<E> {

		final WeightsImpl<E> weights;

		ImmutableView(Weights<E> w) {
			this.weights = (WeightsImpl<E>) Objects.requireNonNull(w);
		}

		Weights<E> weights() {
			return weights;
		}

		@Override
		public int size() {
			return weights.size();
		}

		static class Obj<E> extends ImmutableView<E> {

			Obj(Weights<E> w) {
				super(w);
			}

			@Override
			public E get(int id) {
				return weights().get(id);
			}

			@Override
			public void set(int id, E weight) {
				throw new UnsupportedOperationException("immutable weights");
			}

			@Override
			public E defaultWeight() {
				return weights().defaultWeight();
			}
		}

		static class Byte extends ImmutableView<java.lang.Byte> implements Weights.Byte {
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
				throw new UnsupportedOperationException("immutable weights");
			}

			@Override
			public byte defaultWeightByte() {
				return weights().defaultWeightByte();
			}
		}

		static class Short extends ImmutableView<java.lang.Short> implements Weights.Short {
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
				throw new UnsupportedOperationException("immutable weights");
			}

			@Override
			public short defaultWeightShort() {
				return weights().defaultWeightShort();
			}
		}

		static class Int extends ImmutableView<Integer> implements Weights.Int {
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
				throw new UnsupportedOperationException("immutable weights");
			}

			@Override
			public int defaultWeightInt() {
				return weights().defaultWeightInt();
			}
		}

		static class Long extends ImmutableView<java.lang.Long> implements Weights.Long {
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
				throw new UnsupportedOperationException("immutable weights");
			}

			@Override
			public long defaultWeightLong() {
				return weights().defaultWeightLong();
			}
		}

		static class Float extends ImmutableView<java.lang.Float> implements Weights.Float {
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
				throw new UnsupportedOperationException("immutable weights");
			}

			@Override
			public float defaultWeightFloat() {
				return weights().defaultWeightFloat();
			}
		}

		static class Double extends ImmutableView<java.lang.Double> implements Weights.Double {
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
				throw new UnsupportedOperationException("immutable weights");
			}

			@Override
			public double defaultWeightDouble() {
				return weights().defaultWeightDouble();
			}
		}

		static class Bool extends ImmutableView<Boolean> implements Weights.Bool {
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
				throw new UnsupportedOperationException("immutable weights");
			}

			@Override
			public boolean defaultWeightBool() {
				return weights().defaultWeightBool();
			}
		}

		static class Char extends ImmutableView<Character> implements Weights.Char {
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
				throw new UnsupportedOperationException("immutable weights");
			}

			@Override
			public char defaultWeightChar() {
				return weights().defaultWeightChar();
			}
		}
	}

	private static void checkSameSize(GraphElementSet i1, GraphElementSet i2) {
		if (i1.size() != i2.size())
			throw new IllegalArgumentException("Elements sets size mismatch: " + i1.size() + " != " + i2.size());
	}

}
