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
import static com.jgalgo.internal.util.Range.range;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectIterators;

interface WeightsImpl {

	static interface Index<T> extends IWeights<T>, WeightsImpl {

	}

	abstract static class IndexAbstract<T> implements WeightsImpl.Index<T> {

		final IntSet elements;
		private final boolean isEdges;

		IndexAbstract(IntSet elements, boolean isEdges) {
			this.elements = elements;
			this.isEdges = isEdges;
		}

		public int size() {
			return elements.size();
		}

		void checkIdx(int idx) {
			Assertions.Graphs.checkId(idx, elements.size(), isEdges);
		}

	}

	static interface IndexMutable<T> extends WeightsImpl.Index<T> {

		int capacity();

		void expand(int newCapacity);

		void clear(int idx);

		void clear();

		void swapAndClear(int removedIdx, int swappedIdx);

		static <D> WeightsImpl.IndexMutable<D> newInstance(IntSet elements, boolean isEdges, Class<? super D> type,
				D defVal) {
			IWeights<?> container;
			if (type == byte.class) {
				byte defVal0 = defVal != null ? ((java.lang.Byte) defVal).byteValue() : 0;
				container = new WeightsImplByte.IndexMutable(elements, isEdges, defVal0);

			} else if (type == short.class) {
				short defVal0 = defVal != null ? ((java.lang.Short) defVal).shortValue() : 0;
				container = new WeightsImplShort.IndexMutable(elements, isEdges, defVal0);

			} else if (type == int.class) {
				int defVal0 = defVal != null ? ((Integer) defVal).intValue() : 0;
				container = new WeightsImplInt.IndexMutable(elements, isEdges, defVal0);

			} else if (type == long.class) {
				long defVal0 = defVal != null ? ((java.lang.Long) defVal).longValue() : 0;
				container = new WeightsImplLong.IndexMutable(elements, isEdges, defVal0);

			} else if (type == float.class) {
				float defVal0 = defVal != null ? ((java.lang.Float) defVal).floatValue() : 0;
				container = new WeightsImplFloat.IndexMutable(elements, isEdges, defVal0);

			} else if (type == double.class) {
				double defVal0 = defVal != null ? ((java.lang.Double) defVal).doubleValue() : 0;
				container = new WeightsImplDouble.IndexMutable(elements, isEdges, defVal0);

			} else if (type == boolean.class) {
				boolean defVal0 = defVal != null ? ((Boolean) defVal).booleanValue() : false;
				container = new WeightsImplBool.IndexMutable(elements, isEdges, defVal0);

			} else if (type == char.class) {
				char defVal0 = defVal != null ? ((Character) defVal).charValue() : 0;
				container = new WeightsImplChar.IndexMutable(elements, isEdges, defVal0);

			} else {
				container = new WeightsImplObj.IndexMutable<>(elements, isEdges, defVal);
			}
			@SuppressWarnings("unchecked")
			WeightsImpl.IndexMutable<D> container0 = (WeightsImpl.IndexMutable<D>) container;
			return container0;
		}

		static WeightsImpl.IndexMutable<?> copyOf(Weights<Integer, ?> weights, IntSet elements, boolean isEdges) {
			if (weights instanceof WeightsImpl.IntImmutableView<?>)
				weights = ((WeightsImpl.IntImmutableView<?>) weights).weights();
			if (weights instanceof WeightsImplByte.IndexImpl) {
				return new WeightsImplByte.IndexMutable((WeightsImplByte.IndexImpl) weights, elements, isEdges);
			} else if (weights instanceof WeightsImplShort.IndexImpl) {
				return new WeightsImplShort.IndexMutable((WeightsImplShort.IndexImpl) weights, elements, isEdges);
			} else if (weights instanceof WeightsImplInt.IndexImpl) {
				return new WeightsImplInt.IndexMutable((WeightsImplInt.IndexImpl) weights, elements, isEdges);
			} else if (weights instanceof WeightsImplLong.IndexImpl) {
				return new WeightsImplLong.IndexMutable((WeightsImplLong.IndexImpl) weights, elements, isEdges);
			} else if (weights instanceof WeightsImplFloat.IndexImpl) {
				return new WeightsImplFloat.IndexMutable((WeightsImplFloat.IndexImpl) weights, elements, isEdges);
			} else if (weights instanceof WeightsImplDouble.IndexImpl) {
				return new WeightsImplDouble.IndexMutable((WeightsImplDouble.IndexImpl) weights, elements, isEdges);
			} else if (weights instanceof WeightsImplBool.IndexImpl) {
				return new WeightsImplBool.IndexMutable((WeightsImplBool.IndexImpl) weights, elements, isEdges);
			} else if (weights instanceof WeightsImplChar.IndexImpl) {
				return new WeightsImplChar.IndexMutable((WeightsImplChar.IndexImpl) weights, elements, isEdges);
			} else if (weights instanceof WeightsImplObj.IndexImpl) {
				return new WeightsImplObj.IndexMutable<>((WeightsImplObj.IndexImpl<?>) weights, elements, isEdges);
			}

			/* not an index weights or unknown implementation */
			assert range(elements.size()).equals(elements);
			if (weights instanceof WeightsByte) {
				@SuppressWarnings("unchecked")
				WeightsByte<Integer> weightsByte = (WeightsByte<Integer>) weights;
				WeightsImplByte.IndexMutable newWeights =
						new WeightsImplByte.IndexMutable(elements, isEdges, weightsByte.defaultWeight());
				for (int elm : range(elements.size()))
					newWeights.set(elm, weightsByte.get(Integer.valueOf(elm)));
				return newWeights;
			} else if (weights instanceof WeightsShort) {
				@SuppressWarnings("unchecked")
				WeightsShort<Integer> weightsShort = (WeightsShort<Integer>) weights;
				WeightsImplShort.IndexMutable newWeights =
						new WeightsImplShort.IndexMutable(elements, isEdges, weightsShort.defaultWeight());
				for (int elm : range(elements.size()))
					newWeights.set(elm, weightsShort.get(Integer.valueOf(elm)));
				return newWeights;
			} else if (weights instanceof WeightsInt) {
				@SuppressWarnings("unchecked")
				WeightsInt<Integer> weightsInt = (WeightsInt<Integer>) weights;
				WeightsImplInt.IndexMutable newWeights =
						new WeightsImplInt.IndexMutable(elements, isEdges, weightsInt.defaultWeight());
				for (int elm : range(elements.size()))
					newWeights.set(elm, weightsInt.get(Integer.valueOf(elm)));
				return newWeights;
			} else if (weights instanceof WeightsLong) {
				@SuppressWarnings("unchecked")
				WeightsLong<Integer> weightsLong = (WeightsLong<Integer>) weights;
				WeightsImplLong.IndexMutable newWeights =
						new WeightsImplLong.IndexMutable(elements, isEdges, weightsLong.defaultWeight());
				for (int elm : range(elements.size()))
					newWeights.set(elm, weightsLong.get(Integer.valueOf(elm)));
				return newWeights;
			} else if (weights instanceof WeightsFloat) {
				@SuppressWarnings("unchecked")
				WeightsFloat<Integer> weightsFloat = (WeightsFloat<Integer>) weights;
				WeightsImplFloat.IndexMutable newWeights =
						new WeightsImplFloat.IndexMutable(elements, isEdges, weightsFloat.defaultWeight());
				for (int elm : range(elements.size()))
					newWeights.set(elm, weightsFloat.get(Integer.valueOf(elm)));
				return newWeights;
			} else if (weights instanceof WeightsDouble) {
				@SuppressWarnings("unchecked")
				WeightsDouble<Integer> weightsDouble = (WeightsDouble<Integer>) weights;
				WeightsImplDouble.IndexMutable newWeights =
						new WeightsImplDouble.IndexMutable(elements, isEdges, weightsDouble.defaultWeight());
				for (int elm : range(elements.size()))
					newWeights.set(elm, weightsDouble.get(Integer.valueOf(elm)));
				return newWeights;
			} else if (weights instanceof WeightsBool) {
				@SuppressWarnings("unchecked")
				WeightsBool<Integer> weightsBool = (WeightsBool<Integer>) weights;
				WeightsImplBool.IndexMutable newWeights =
						new WeightsImplBool.IndexMutable(elements, isEdges, weightsBool.defaultWeight());
				for (int elm : range(elements.size()))
					newWeights.set(elm, weightsBool.get(Integer.valueOf(elm)));
				return newWeights;
			} else if (weights instanceof WeightsChar) {
				@SuppressWarnings("unchecked")
				WeightsChar<Integer> weightsChar = (WeightsChar<Integer>) weights;
				WeightsImplChar.IndexMutable newWeights =
						new WeightsImplChar.IndexMutable(elements, isEdges, weightsChar.defaultWeight());
				for (int elm : range(elements.size()))
					newWeights.set(elm, weightsChar.get(Integer.valueOf(elm)));
				return newWeights;
			} else {
				@SuppressWarnings("unchecked")
				WeightsObj<Integer, Object> weightsObj = (WeightsObj<Integer, Object>) weights;
				WeightsImplObj.IndexMutable<Object> newWeights =
						new WeightsImplObj.IndexMutable<>(elements, isEdges, weightsObj.defaultWeight());
				for (int elm : range(elements.size()))
					newWeights.set(elm, weightsObj.get(Integer.valueOf(elm)));
				return newWeights;
			}
		}

		static class Manager {

			private String[] weightsKey;
			private WeightsImpl.IndexMutable<?>[] weights;
			private final Object2IntMap<String> keyToIdx;
			private int weightsCapacity;
			private final boolean isEdges;

			private static final String[] EMPTY_WEIGHTS_KEY_ARR = new String[0];
			private static final WeightsImpl.IndexMutable<?>[] EMPTY_WEIGHTS_ARR = new WeightsImpl.IndexMutable<?>[0];

			Manager(int initCapacity, boolean isEdges) {
				weightsKey = EMPTY_WEIGHTS_KEY_ARR;
				weights = EMPTY_WEIGHTS_ARR;
				keyToIdx = new Object2IntOpenHashMap<>();
				keyToIdx.defaultReturnValue(-1);
				weightsCapacity = initCapacity;
				this.isEdges = isEdges;
			}

			Manager(Manager orig, IntSet elements) {
				weightsCapacity = elements.size();
				int numberOfWeights = orig.keyToIdx.size();
				isEdges = orig.isEdges;
				if (numberOfWeights == 0) {
					weightsKey = EMPTY_WEIGHTS_KEY_ARR;
					weights = EMPTY_WEIGHTS_ARR;
					keyToIdx = new Object2IntOpenHashMap<>();

				} else {
					weightsKey = Arrays.copyOf(orig.weightsKey, numberOfWeights);
					weights = new WeightsImpl.IndexMutable[numberOfWeights];
					for (int i = 0; i < numberOfWeights; i++)
						weights[i] = WeightsImpl.IndexMutable.copyOf(orig.weights[i], elements, isEdges);
					keyToIdx = new Object2IntOpenHashMap<>(orig.keyToIdx);
				}
				keyToIdx.defaultReturnValue(-1);
			}

			void addWeights(String key, WeightsImpl.IndexMutable<?> weights) {
				Objects.requireNonNull(key);
				int idx = keyToIdx.size();
				int oldIdx = keyToIdx.putIfAbsent(key, idx);
				if (oldIdx != -1)
					throw new IllegalArgumentException("Two weights types with the same key: " + key);

				if (idx == this.weights.length) {
					weightsKey = Arrays.copyOf(weightsKey, Math.max(2, 2 * weightsKey.length));
					this.weights = Arrays.copyOf(this.weights, Math.max(2, 2 * this.weights.length));
				}
				weightsKey[idx] = key;
				this.weights[idx] = weights;

				if (weightsCapacity > weights.capacity())
					weights.expand(weightsCapacity);
			}

			void removeWeights(String key) {
				int lastIdx = keyToIdx.size() - 1;
				int idx = keyToIdx.removeInt(key);
				if (idx == -1)
					throw new IllegalArgumentException("no weights with key: " + key);

				if (idx != lastIdx) {
					weights[idx] = weights[lastIdx];
					keyToIdx.put(weightsKey[lastIdx], idx);
				}
				weightsKey[lastIdx] = null;
				weights[lastIdx] = null;
			}

			@SuppressWarnings("unchecked")
			<T, WeightsT extends IWeights<T>> WeightsT getWeights(String key) {
				int idx = keyToIdx.getInt(key);
				return idx == -1 ? null : (WeightsT) weights[idx];
			}

			Set<String> weightsKeys() {
				return Collections.unmodifiableSet(keyToIdx.keySet());
			}

			void ensureCapacity(int capacity) {
				if (capacity <= weightsCapacity)
					return;
				int newCapacity = Math.max(Math.max(2, 2 * weightsCapacity), capacity);
				for (WeightsImpl.IndexMutable<?> container : weights())
					container.expand(newCapacity);
				weightsCapacity = newCapacity;
			}

			void swapAndClear(int removedIdx, int swappedIdx) {
				for (WeightsImpl.IndexMutable<?> container : weights())
					container.swapAndClear(removedIdx, swappedIdx);
			}

			void clearElement(int idx) {
				for (WeightsImpl.IndexMutable<?> container : weights())
					container.clear(idx);
			}

			void clearContainers() {
				for (WeightsImpl.IndexMutable<?> container : weights())
					container.clear();
				Arrays.fill(weightsKey, 0, keyToIdx.size(), null);
				Arrays.fill(weights, 0, keyToIdx.size(), null);
				keyToIdx.clear();
				weightsCapacity = 0;
			}

			private Iterable<WeightsImpl.IndexMutable<?>> weights() {
				return () -> ObjectIterators.wrap(weights, 0, keyToIdx.size());
			}
		}

	}

	static interface IndexImmutable<T> extends IWeights<T>, WeightsImpl {

		static WeightsImpl.IndexImmutable<?> copyOf(IWeights<?> weights, IntSet elements, boolean isEdges) {
			if (weights instanceof WeightsImpl.IntImmutableView<?>)
				weights = ((WeightsImpl.IntImmutableView<?>) weights).weights();
			if (weights instanceof WeightsImplByte.IndexImpl) {
				return new WeightsImplByte.IndexImmutable((WeightsImplByte.IndexImpl) weights, elements, isEdges);
			} else if (weights instanceof WeightsImplShort.IndexImpl) {
				return new WeightsImplShort.IndexImmutable((WeightsImplShort.IndexImpl) weights, elements, isEdges);
			} else if (weights instanceof WeightsImplInt.IndexImpl) {
				return new WeightsImplInt.IndexImmutable((WeightsImplInt.IndexImpl) weights, elements, isEdges);
			} else if (weights instanceof WeightsImplLong.IndexImpl) {
				return new WeightsImplLong.IndexImmutable((WeightsImplLong.IndexImpl) weights, elements, isEdges);
			} else if (weights instanceof WeightsImplFloat.IndexImpl) {
				return new WeightsImplFloat.IndexImmutable((WeightsImplFloat.IndexImpl) weights, elements, isEdges);
			} else if (weights instanceof WeightsImplDouble.IndexImpl) {
				return new WeightsImplDouble.IndexImmutable((WeightsImplDouble.IndexImpl) weights, elements, isEdges);
			} else if (weights instanceof WeightsImplBool.IndexImpl) {
				return new WeightsImplBool.IndexImmutable((WeightsImplBool.IndexImpl) weights, elements, isEdges);
			} else if (weights instanceof WeightsImplChar.IndexImpl) {
				return new WeightsImplChar.IndexImmutable((WeightsImplChar.IndexImpl) weights, elements, isEdges);
			} else if (weights instanceof WeightsImplObj.IndexImpl) {
				return new WeightsImplObj.IndexImmutable<>((WeightsImplObj.IndexImpl<?>) weights, elements, isEdges);
			} else {
				throw new IllegalArgumentException("unknown weights implementation: " + weights.getClass());
			}
		}

		static WeightsImpl.IndexImmutable<?> copyOfReindexed(IWeights<?> weights, IntSet elements, boolean isEdges,
				IndexGraphBuilder.ReIndexingMap reIndexMap) {
			if (weights instanceof WeightsImpl.IntImmutableView<?>)
				weights = ((WeightsImpl.IntImmutableView<?>) weights).weights();
			if (weights instanceof WeightsImplByte.IndexImpl) {
				return new WeightsImplByte.IndexImmutable((WeightsImplByte.IndexImpl) weights, elements, isEdges,
						reIndexMap);
			} else if (weights instanceof WeightsImplShort.IndexImpl) {
				return new WeightsImplShort.IndexImmutable((WeightsImplShort.IndexImpl) weights, elements, isEdges,
						reIndexMap);
			} else if (weights instanceof WeightsImplInt.IndexImpl) {
				return new WeightsImplInt.IndexImmutable((WeightsImplInt.IndexImpl) weights, elements, isEdges,
						reIndexMap);
			} else if (weights instanceof WeightsImplLong.IndexImpl) {
				return new WeightsImplLong.IndexImmutable((WeightsImplLong.IndexImpl) weights, elements, isEdges,
						reIndexMap);
			} else if (weights instanceof WeightsImplFloat.IndexImpl) {
				return new WeightsImplFloat.IndexImmutable((WeightsImplFloat.IndexImpl) weights, elements, isEdges,
						reIndexMap);
			} else if (weights instanceof WeightsImplDouble.IndexImpl) {
				return new WeightsImplDouble.IndexImmutable((WeightsImplDouble.IndexImpl) weights, elements, isEdges,
						reIndexMap);
			} else if (weights instanceof WeightsImplBool.IndexImpl) {
				return new WeightsImplBool.IndexImmutable((WeightsImplBool.IndexImpl) weights, elements, isEdges,
						reIndexMap);
			} else if (weights instanceof WeightsImplChar.IndexImpl) {
				return new WeightsImplChar.IndexImmutable((WeightsImplChar.IndexImpl) weights, elements, isEdges,
						reIndexMap);
			} else if (weights instanceof WeightsImplObj.IndexImpl) {
				return new WeightsImplObj.IndexImmutable<>((WeightsImplObj.IndexImpl<?>) weights, elements, isEdges,
						reIndexMap);
			} else {
				throw new IllegalArgumentException("unknown weights implementation: " + weights.getClass());
			}
		}

		static class Builder {

			private final IntSet elements;
			private final boolean isEdges;
			private final Map<String, WeightsImpl.IndexImmutable<?>> weights;

			Builder(IntSet elements, boolean isEdges) {
				this.elements = Objects.requireNonNull(elements);
				this.isEdges = isEdges;
				weights = new Object2ObjectOpenHashMap<>();
			}

			void copyAndAddWeights(String key, IWeights<?> weights) {
				Object oldWeights =
						this.weights.put(key, WeightsImpl.IndexImmutable.copyOf(weights, elements, isEdges));
				if (oldWeights != null)
					throw new IllegalArgumentException("duplicate key: " + key);
			}

			void copyAndAddWeightsReindexed(String key, IWeights<?> weights,
					IndexGraphBuilder.ReIndexingMap reIndexMap) {
				Object oldWeights = this.weights.put(key,
						WeightsImpl.IndexImmutable.copyOfReindexed(weights, elements, isEdges, reIndexMap));
				if (oldWeights != null)
					throw new IllegalArgumentException("duplicate key: " + key);
			}

			Map<String, WeightsImpl.IndexImmutable<?>> build() {
				return Map.copyOf(weights);
			}
		}

	}

	abstract static class IntMapped<T> implements IWeights<T>, WeightsImpl {

		final WeightsImpl.IndexAbstract<T> weights;
		final IndexIntIdMap indexMap;

		IntMapped(WeightsImpl.Index<T> weights, IndexIntIdMap indexMap) {
			this.weights = (WeightsImpl.IndexAbstract<T>) Objects.requireNonNull(weights);
			this.indexMap = indexMap;
		}

		WeightsImpl.IndexAbstract<T> weights() {
			return weights;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		static WeightsImpl.IntMapped<?> newInstance(WeightsImpl.Index<?> weights, IndexIntIdMap indexMap) {
			if (weights instanceof WeightsImplByte.IndexImpl) {
				return new WeightsImplByte.IntMapped((WeightsImplByte.IndexImpl) weights, indexMap);
			} else if (weights instanceof WeightsImplShort.IndexImpl) {
				return new WeightsImplShort.IntMapped((WeightsImplShort.IndexImpl) weights, indexMap);
			} else if (weights instanceof WeightsImplInt.IndexImpl) {
				return new WeightsImplInt.IntMapped((WeightsImplInt.IndexImpl) weights, indexMap);
			} else if (weights instanceof WeightsImplLong.IndexImpl) {
				return new WeightsImplLong.IntMapped((WeightsImplLong.IndexImpl) weights, indexMap);
			} else if (weights instanceof WeightsImplFloat.IndexImpl) {
				return new WeightsImplFloat.IntMapped((WeightsImplFloat.IndexImpl) weights, indexMap);
			} else if (weights instanceof WeightsImplDouble.IndexImpl) {
				return new WeightsImplDouble.IntMapped((WeightsImplDouble.IndexImpl) weights, indexMap);
			} else if (weights instanceof WeightsImplBool.IndexImpl) {
				return new WeightsImplBool.IntMapped((WeightsImplBool.IndexImpl) weights, indexMap);
			} else if (weights instanceof WeightsImplChar.IndexImpl) {
				return new WeightsImplChar.IntMapped((WeightsImplChar.IndexImpl) weights, indexMap);
			} else {
				return new WeightsImplObj.IntMapped<>((WeightsImplObj.IndexImpl) weights, indexMap);
			}
		}

	}

	static class ImmutableView<K, T> implements WeightsImpl {

		private final Weights<K, T> weights;

		ImmutableView(Weights<K, T> weights) {
			this.weights = Objects.requireNonNull(weights);
		}

		Weights<K, T> weights() {
			return weights;
		}
	}

	abstract static class IntImmutableView<T> extends ImmutableView<Integer, T> implements IWeights<T> {

		IntImmutableView(IWeights<T> weights) {
			super(weights);
		}

		@Override
		IWeights<T> weights() {
			return (IWeights<T>) super.weights();
		}

		@SuppressWarnings("unchecked")
		static <T> IWeights<T> newInstance(IWeights<T> weights) {
			if (weights instanceof WeightsImpl.ImmutableView)
				return weights;
			if (weights instanceof IWeightsByte)
				return (IWeights<T>) new WeightsImplByte.IntImmutableView((IWeightsByte) weights);
			if (weights instanceof IWeightsShort)
				return (IWeights<T>) new WeightsImplShort.IntImmutableView((IWeightsShort) weights);
			if (weights instanceof IWeightsInt)
				return (IWeights<T>) new WeightsImplInt.IntImmutableView((IWeightsInt) weights);
			if (weights instanceof IWeightsLong)
				return (IWeights<T>) new WeightsImplLong.IntImmutableView((IWeightsLong) weights);
			if (weights instanceof IWeightsFloat)
				return (IWeights<T>) new WeightsImplFloat.IntImmutableView((IWeightsFloat) weights);
			if (weights instanceof IWeightsDouble)
				return (IWeights<T>) new WeightsImplDouble.IntImmutableView((IWeightsDouble) weights);
			if (weights instanceof IWeightsBool)
				return (IWeights<T>) new WeightsImplBool.IntImmutableView((IWeightsBool) weights);
			if (weights instanceof IWeightsChar)
				return (IWeights<T>) new WeightsImplChar.IntImmutableView((IWeightsChar) weights);
			if (weights instanceof IWeightsObj)
				return new WeightsImplObj.IntImmutableView<>((IWeightsObj<T>) weights);
			throw new IllegalArgumentException("Unsupported weights type: " + weights.getClass());
		}
	}

	static void checkSameSize(IntSet i1, IntSet i2) {
		if (i1.size() != i2.size())
			throw new IllegalArgumentException("Elements sets size mismatch: " + i1.size() + " != " + i2.size());
	}

	@SuppressWarnings("unchecked")
	static <K> boolean isEqual(Set<K> elementsSet, Weights<K, ?> w1, Weights<K, ?> w2) {
		if (w1 == w2)
			return true;
		if (w1 instanceof IWeights && w2 instanceof IWeights) {
			IntCollection elements = IntAdapters.asIntCollection((Collection<Integer>) elementsSet);
			if (w1 instanceof IWeightsObj<?> && w2 instanceof IWeightsObj<?>) {
				for (int elm : elements)
					if (!Objects.equals(((IWeightsObj<?>) w1).get(elm), ((IWeightsObj<?>) w2).get(elm)))
						return false;
				return true;
			} else if (w1 instanceof IWeightsByte && w2 instanceof IWeightsByte) {
				for (int elm : elements)
					if (((IWeightsByte) w1).get(elm) != ((IWeightsByte) w2).get(elm))
						return false;
				return true;
			} else if (w1 instanceof IWeightsShort && w2 instanceof IWeightsShort) {
				for (int elm : elements)
					if (((IWeightsShort) w1).get(elm) != ((IWeightsShort) w2).get(elm))
						return false;
				return true;
			} else if (w1 instanceof IWeightsInt && w2 instanceof IWeightsInt) {
				for (int elm : elements)
					if (((IWeightsInt) w1).get(elm) != ((IWeightsInt) w2).get(elm))
						return false;
				return true;
			} else if (w1 instanceof IWeightsLong && w2 instanceof IWeightsLong) {
				for (int elm : elements)
					if (((IWeightsLong) w1).get(elm) != ((IWeightsLong) w2).get(elm))
						return false;
				return true;
			} else if (w1 instanceof IWeightsFloat && w2 instanceof IWeightsFloat) {
				for (int elm : elements)
					if (((IWeightsFloat) w1).get(elm) != ((IWeightsFloat) w2).get(elm))
						return false;
				return true;
			} else if (w1 instanceof IWeightsDouble && w2 instanceof IWeightsDouble) {
				for (int elm : elements)
					if (((IWeightsDouble) w1).get(elm) != ((IWeightsDouble) w2).get(elm))
						return false;
				return true;
			} else if (w1 instanceof IWeightsBool && w2 instanceof IWeightsBool) {
				for (int elm : elements)
					if (((IWeightsBool) w1).get(elm) != ((IWeightsBool) w2).get(elm))
						return false;
				return true;
			} else if (w1 instanceof IWeightsChar && w2 instanceof IWeightsChar) {
				for (int elm : elements)
					if (((IWeightsChar) w1).get(elm) != ((IWeightsChar) w2).get(elm))
						return false;
				return true;
			}
		} else {
			if (w1 instanceof WeightsObj && w2 instanceof WeightsObj) {
				for (K elm : elementsSet)
					if (!Objects.equals(((WeightsObj<K, ?>) w1).get(elm), ((WeightsObj<K, ?>) w2).get(elm)))
						return false;
				return true;
			} else if (w1 instanceof WeightsByte && w2 instanceof WeightsByte) {
				for (K elm : elementsSet)
					if (((WeightsByte<K>) w1).get(elm) != ((WeightsByte<K>) w2).get(elm))
						return false;
				return true;
			} else if (w1 instanceof WeightsShort && w2 instanceof WeightsShort) {
				for (K elm : elementsSet)
					if (((WeightsShort<K>) w1).get(elm) != ((WeightsShort<K>) w2).get(elm))
						return false;
				return true;
			} else if (w1 instanceof WeightsInt && w2 instanceof WeightsInt) {
				for (K elm : elementsSet)
					if (((WeightsInt<K>) w1).get(elm) != ((WeightsInt<K>) w2).get(elm))
						return false;
				return true;
			} else if (w1 instanceof WeightsLong && w2 instanceof WeightsLong) {
				for (K elm : elementsSet)
					if (((WeightsLong<K>) w1).get(elm) != ((WeightsLong<K>) w2).get(elm))
						return false;
				return true;
			} else if (w1 instanceof WeightsFloat && w2 instanceof WeightsFloat) {
				for (K elm : elementsSet)
					if (((WeightsFloat<K>) w1).get(elm) != ((WeightsFloat<K>) w2).get(elm))
						return false;
				return true;
			} else if (w1 instanceof WeightsDouble && w2 instanceof WeightsDouble) {
				for (K elm : elementsSet)
					if (((WeightsDouble<K>) w1).get(elm) != ((WeightsDouble<K>) w2).get(elm))
						return false;
				return true;
			} else if (w1 instanceof WeightsBool && w2 instanceof WeightsBool) {
				for (K elm : elementsSet)
					if (((WeightsBool<K>) w1).get(elm) != ((WeightsBool<K>) w2).get(elm))
						return false;
				return true;
			} else if (w1 instanceof WeightsChar && w2 instanceof WeightsChar) {
				for (K elm : elementsSet)
					if (((WeightsChar<K>) w1).get(elm) != ((WeightsChar<K>) w2).get(elm))
						return false;
				return true;
			}
		}
		return false;
	}

	@SuppressWarnings("unchecked")
	static <K> int hashCode(Set<K> elementsSet, Weights<K, ?> w) {
		int h = 0;
		if (w instanceof IWeights) {
			IntCollection elements = IntAdapters.asIntCollection((Collection<Integer>) elementsSet);
			if (w instanceof IWeightsObj) {
				for (int elm : elements)
					h += Objects.hashCode(((IWeightsObj<?>) w).get(elm));
			} else if (w instanceof IWeightsByte) {
				for (int elm : elements)
					h += ((IWeightsByte) w).get(elm);
			} else if (w instanceof IWeightsShort) {
				for (int elm : elements)
					h += ((IWeightsShort) w).get(elm);
			} else if (w instanceof IWeightsInt) {
				for (int elm : elements)
					h += ((IWeightsInt) w).get(elm);
			} else if (w instanceof IWeightsLong) {
				for (int elm : elements) {
					long x = ((IWeightsLong) w).get(elm);
					h += (int) (x ^ (x >>> 32));
				}
			} else if (w instanceof IWeightsFloat) {
				for (int elm : elements)
					h += java.lang.Float.floatToRawIntBits(((IWeightsFloat) w).get(elm));
			} else if (w instanceof IWeightsDouble) {
				for (int elm : elements) {
					long x = java.lang.Double.doubleToRawLongBits(((IWeightsDouble) w).get(elm));
					h += (int) (x ^ (x >>> 32));
				}
			} else if (w instanceof IWeightsBool) {
				for (int elm : elements)
					h += ((IWeightsBool) w).get(elm) ? 1231 : 1237;
			} else if (w instanceof IWeightsChar) {
				for (int elm : elements)
					h += ((IWeightsChar) w).get(elm);
			} else {
				throw new IllegalArgumentException("Unsupported weights type: " + w.getClass());
			}
		} else {
			if (w instanceof WeightsObj) {
				for (K elm : elementsSet)
					h += Objects.hashCode(((WeightsObj<K, ?>) w).get(elm));
			} else if (w instanceof WeightsByte) {
				for (K elm : elementsSet)
					h += ((WeightsByte<K>) w).get(elm);
			} else if (w instanceof WeightsShort) {
				for (K elm : elementsSet)
					h += ((WeightsShort<K>) w).get(elm);
			} else if (w instanceof WeightsInt) {
				for (K elm : elementsSet)
					h += ((WeightsInt<K>) w).get(elm);
			} else if (w instanceof WeightsLong) {
				for (K elm : elementsSet) {
					long x = ((WeightsLong<K>) w).get(elm);
					h += (int) (x ^ (x >>> 32));
				}
			} else if (w instanceof WeightsFloat) {
				for (K elm : elementsSet)
					h += java.lang.Float.floatToRawIntBits(((WeightsFloat<K>) w).get(elm));
			} else if (w instanceof WeightsDouble) {
				for (K elm : elementsSet) {
					long x = java.lang.Double.doubleToRawLongBits(((WeightsDouble<K>) w).get(elm));
					h += (int) (x ^ (x >>> 32));
				}
			} else if (w instanceof WeightsBool) {
				for (K elm : elementsSet)
					h += ((WeightsBool<K>) w).get(elm) ? 1231 : 1237;
			} else if (w instanceof WeightsChar) {
				for (K elm : elementsSet)
					h += ((WeightsChar<K>) w).get(elm);
			} else {
				throw new IllegalArgumentException("Unsupported weights type: " + w.getClass());
			}
		}

		return h;
	}

	abstract static class ObjMapped<K, T> implements Weights<K, T>, WeightsImpl {

		final WeightsImpl.IndexAbstract<T> weights;
		final IndexIdMap<K> indexMap;

		ObjMapped(WeightsImpl.Index<T> weights, IndexIdMap<K> indexMap) {
			this.weights = (WeightsImpl.IndexAbstract<T>) Objects.requireNonNull(weights);
			this.indexMap = indexMap;
		}

		WeightsImpl.IndexAbstract<T> weights() {
			return weights;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		static <K> WeightsImpl.ObjMapped<K, ?> newInstance(WeightsImpl.Index<?> weights, IndexIdMap<K> indexMap) {
			if (weights instanceof WeightsImplByte.IndexImpl) {
				return new WeightsImplByte.ObjMapped((WeightsImplByte.IndexImpl) weights, indexMap);
			} else if (weights instanceof WeightsImplShort.IndexImpl) {
				return new WeightsImplShort.ObjMapped((WeightsImplShort.IndexImpl) weights, indexMap);
			} else if (weights instanceof WeightsImplInt.IndexImpl) {
				return new WeightsImplInt.ObjMapped((WeightsImplInt.IndexImpl) weights, indexMap);
			} else if (weights instanceof WeightsImplLong.IndexImpl) {
				return new WeightsImplLong.ObjMapped((WeightsImplLong.IndexImpl) weights, indexMap);
			} else if (weights instanceof WeightsImplFloat.IndexImpl) {
				return new WeightsImplFloat.ObjMapped((WeightsImplFloat.IndexImpl) weights, indexMap);
			} else if (weights instanceof WeightsImplDouble.IndexImpl) {
				return new WeightsImplDouble.ObjMapped((WeightsImplDouble.IndexImpl) weights, indexMap);
			} else if (weights instanceof WeightsImplBool.IndexImpl) {
				return new WeightsImplBool.ObjMapped((WeightsImplBool.IndexImpl) weights, indexMap);
			} else if (weights instanceof WeightsImplChar.IndexImpl) {
				return new WeightsImplChar.ObjMapped((WeightsImplChar.IndexImpl) weights, indexMap);
			} else {
				return new WeightsImplObj.ObjMapped<>((WeightsImplObj.IndexImpl) weights, indexMap);
			}
		}

	}

	abstract static class ObjImmutableView<K, T> extends ImmutableView<K, T> implements Weights<K, T> {

		ObjImmutableView(Weights<K, T> weights) {
			super(weights);
		}

		@SuppressWarnings("unchecked")
		static <K, T> Weights<K, T> newInstance(Weights<K, T> weights) {
			if (weights instanceof ImmutableView)
				return weights;
			if (weights instanceof WeightsByte)
				return (Weights<K, T>) new WeightsImplByte.ObjImmutableView<>((WeightsByte<K>) weights);
			if (weights instanceof WeightsShort)
				return (Weights<K, T>) new WeightsImplShort.ObjImmutableView<>((WeightsShort<K>) weights);
			if (weights instanceof WeightsInt)
				return (Weights<K, T>) new WeightsImplInt.ObjImmutableView<>((WeightsInt<K>) weights);
			if (weights instanceof WeightsLong)
				return (Weights<K, T>) new WeightsImplLong.ObjImmutableView<>((WeightsLong<K>) weights);
			if (weights instanceof WeightsFloat)
				return (Weights<K, T>) new WeightsImplFloat.ObjImmutableView<>((WeightsFloat<K>) weights);
			if (weights instanceof WeightsDouble)
				return (Weights<K, T>) new WeightsImplDouble.ObjImmutableView<>((WeightsDouble<K>) weights);
			if (weights instanceof WeightsBool)
				return (Weights<K, T>) new WeightsImplBool.ObjImmutableView<>((WeightsBool<K>) weights);
			if (weights instanceof WeightsChar)
				return (Weights<K, T>) new WeightsImplChar.ObjImmutableView<>((WeightsChar<K>) weights);
			if (weights instanceof WeightsObj)
				return new WeightsImplObj.ObjImmutableView<>((WeightsObj<K, T>) weights);
			throw new IllegalArgumentException("Unsupported weights type: " + weights.getClass());
		}
	}

}
