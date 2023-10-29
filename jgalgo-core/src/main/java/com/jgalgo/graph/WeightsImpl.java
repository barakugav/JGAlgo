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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;

interface WeightsImpl {

	static interface Index<T> extends IWeights<T>, WeightsImpl {

	}

	static abstract class IndexAbstract<T> implements WeightsImpl.Index<T> {

		final GraphElementSet elements;

		IndexAbstract(GraphElementSet elements) {
			this.elements = elements;
		}

		public int size() {
			return elements.size();
		}

		void checkIdx(int idx) {
			elements.checkIdx(idx);
		}

	}

	static interface IndexMutable<T> extends WeightsImpl.Index<T> {

		int capacity();

		void expand(int newCapacity);

		void clear(int idx);

		void clear();

		void swap(int idx1, int idx2);

		static <D> WeightsImpl.IndexMutable<D> newInstance(GraphElementSet elements, Class<? super D> type, D defVal) {
			IWeights<?> container;
			if (type == byte.class) {
				byte defVal0 = defVal != null ? ((java.lang.Byte) defVal).byteValue() : 0;
				container = new WeightsImplByte.IndexMutable(elements, defVal0);

			} else if (type == short.class) {
				short defVal0 = defVal != null ? ((java.lang.Short) defVal).shortValue() : 0;
				container = new WeightsImplShort.IndexMutable(elements, defVal0);

			} else if (type == int.class) {
				int defVal0 = defVal != null ? ((Integer) defVal).intValue() : 0;
				container = new WeightsImplInt.IndexMutable(elements, defVal0);

			} else if (type == long.class) {
				long defVal0 = defVal != null ? ((java.lang.Long) defVal).longValue() : 0;
				container = new WeightsImplLong.IndexMutable(elements, defVal0);

			} else if (type == float.class) {
				float defVal0 = defVal != null ? ((java.lang.Float) defVal).floatValue() : 0;
				container = new WeightsImplFloat.IndexMutable(elements, defVal0);

			} else if (type == double.class) {
				double defVal0 = defVal != null ? ((java.lang.Double) defVal).doubleValue() : 0;
				container = new WeightsImplDouble.IndexMutable(elements, defVal0);

			} else if (type == boolean.class) {
				boolean defVal0 = defVal != null ? ((Boolean) defVal).booleanValue() : false;
				container = new WeightsImplBool.IndexMutable(elements, defVal0);

			} else if (type == char.class) {
				char defVal0 = defVal != null ? ((Character) defVal).charValue() : 0;
				container = new WeightsImplChar.IndexMutable(elements, defVal0);

			} else {
				container = new WeightsImplObj.IndexMutable<>(elements, defVal);
			}
			@SuppressWarnings("unchecked")
			WeightsImpl.IndexMutable<D> container0 = (WeightsImpl.IndexMutable<D>) container;
			return container0;
		}

		static WeightsImpl.IndexMutable<?> copyOf(IWeights<?> weights, GraphElementSet elements) {
			if (weights instanceof WeightsImpl.IntImmutableView<?>)
				weights = ((WeightsImpl.IntImmutableView<?>) weights).weights();
			if (weights instanceof WeightsImplByte.IndexImpl) {
				return new WeightsImplByte.IndexMutable((WeightsImplByte.IndexImpl) weights, elements);
			} else if (weights instanceof WeightsImplShort.IndexImpl) {
				return new WeightsImplShort.IndexMutable((WeightsImplShort.IndexImpl) weights, elements);
			} else if (weights instanceof WeightsImplInt.IndexImpl) {
				return new WeightsImplInt.IndexMutable((WeightsImplInt.IndexImpl) weights, elements);
			} else if (weights instanceof WeightsImplLong.IndexImpl) {
				return new WeightsImplLong.IndexMutable((WeightsImplLong.IndexImpl) weights, elements);
			} else if (weights instanceof WeightsImplFloat.IndexImpl) {
				return new WeightsImplFloat.IndexMutable((WeightsImplFloat.IndexImpl) weights, elements);
			} else if (weights instanceof WeightsImplDouble.IndexImpl) {
				return new WeightsImplDouble.IndexMutable((WeightsImplDouble.IndexImpl) weights, elements);
			} else if (weights instanceof WeightsImplBool.IndexImpl) {
				return new WeightsImplBool.IndexMutable((WeightsImplBool.IndexImpl) weights, elements);
			} else if (weights instanceof WeightsImplChar.IndexImpl) {
				return new WeightsImplChar.IndexMutable((WeightsImplChar.IndexImpl) weights, elements);
			} else if (weights instanceof WeightsImplObj.IndexImpl) {
				return new WeightsImplObj.IndexMutable<>((WeightsImplObj.IndexImpl<?>) weights, elements);
			} else {
				throw new IllegalArgumentException("unknown weights implementation: " + weights.getClass());
			}
		}

		static class Manager {

			final Map<String, WeightsImpl.IndexMutable<?>> weights = new Object2ObjectOpenHashMap<>();
			private int weightsCapacity;

			Manager(int initCapacity) {
				weightsCapacity = initCapacity;
			}

			Manager(Manager orig, GraphElementSet elements) {
				this(elements.size());
				for (var entry : orig.weights.entrySet())
					weights.put(entry.getKey(), WeightsImpl.IndexMutable.copyOf(entry.getValue(), elements));
			}

			void addWeights(String key, WeightsImpl.IndexMutable<?> weight) {
				WeightsImpl.IndexMutable<?> oldContainer = weights.put(key, weight);
				if (oldContainer != null)
					throw new IllegalArgumentException("Two weights types with the same key: " + key);
				if (weightsCapacity > weight.capacity())
					weight.expand(weightsCapacity);
			}

			void removeWeights(String key) {
				weights.remove(key);
			}

			@SuppressWarnings("unchecked")
			<T, WeightsT extends IWeights<T>> WeightsT getWeights(String key) {
				return (WeightsT) weights.get(key);
			}

			Set<String> weightsKeys() {
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

	static interface IndexImmutable<T> extends IWeights<T>, WeightsImpl {

		static WeightsImpl.IndexImmutable<?> copyOf(IWeights<?> weights, GraphElementSet.FixedSize elements) {
			if (weights instanceof WeightsImpl.IntImmutableView<?>)
				weights = ((WeightsImpl.IntImmutableView<?>) weights).weights();
			if (weights instanceof WeightsImplByte.IndexImpl) {
				return new WeightsImplByte.IndexImmutable((WeightsImplByte.IndexImpl) weights, elements);
			} else if (weights instanceof WeightsImplShort.IndexImpl) {
				return new WeightsImplShort.IndexImmutable((WeightsImplShort.IndexImpl) weights, elements);
			} else if (weights instanceof WeightsImplInt.IndexImpl) {
				return new WeightsImplInt.IndexImmutable((WeightsImplInt.IndexImpl) weights, elements);
			} else if (weights instanceof WeightsImplLong.IndexImpl) {
				return new WeightsImplLong.IndexImmutable((WeightsImplLong.IndexImpl) weights, elements);
			} else if (weights instanceof WeightsImplFloat.IndexImpl) {
				return new WeightsImplFloat.IndexImmutable((WeightsImplFloat.IndexImpl) weights, elements);
			} else if (weights instanceof WeightsImplDouble.IndexImpl) {
				return new WeightsImplDouble.IndexImmutable((WeightsImplDouble.IndexImpl) weights, elements);
			} else if (weights instanceof WeightsImplBool.IndexImpl) {
				return new WeightsImplBool.IndexImmutable((WeightsImplBool.IndexImpl) weights, elements);
			} else if (weights instanceof WeightsImplChar.IndexImpl) {
				return new WeightsImplChar.IndexImmutable((WeightsImplChar.IndexImpl) weights, elements);
			} else if (weights instanceof WeightsImplObj.IndexImpl) {
				return new WeightsImplObj.IndexImmutable<>((WeightsImplObj.IndexImpl<?>) weights, elements);
			} else {
				throw new IllegalArgumentException("unknown weights implementation: " + weights.getClass());
			}
		}

		static WeightsImpl.IndexImmutable<?> copyOfReindexed(IWeights<?> weights, GraphElementSet.FixedSize elements,
				IndexGraphBuilder.ReIndexingMap reIndexMap) {
			if (weights instanceof WeightsImpl.IntImmutableView<?>)
				weights = ((WeightsImpl.IntImmutableView<?>) weights).weights();
			if (weights instanceof WeightsImplByte.IndexImpl) {
				return new WeightsImplByte.IndexImmutable((WeightsImplByte.IndexImpl) weights, elements, reIndexMap);
			} else if (weights instanceof WeightsImplShort.IndexImpl) {
				return new WeightsImplShort.IndexImmutable((WeightsImplShort.IndexImpl) weights, elements, reIndexMap);
			} else if (weights instanceof WeightsImplInt.IndexImpl) {
				return new WeightsImplInt.IndexImmutable((WeightsImplInt.IndexImpl) weights, elements, reIndexMap);
			} else if (weights instanceof WeightsImplLong.IndexImpl) {
				return new WeightsImplLong.IndexImmutable((WeightsImplLong.IndexImpl) weights, elements, reIndexMap);
			} else if (weights instanceof WeightsImplFloat.IndexImpl) {
				return new WeightsImplFloat.IndexImmutable((WeightsImplFloat.IndexImpl) weights, elements, reIndexMap);
			} else if (weights instanceof WeightsImplDouble.IndexImpl) {
				return new WeightsImplDouble.IndexImmutable((WeightsImplDouble.IndexImpl) weights, elements,
						reIndexMap);
			} else if (weights instanceof WeightsImplBool.IndexImpl) {
				return new WeightsImplBool.IndexImmutable((WeightsImplBool.IndexImpl) weights, elements, reIndexMap);
			} else if (weights instanceof WeightsImplChar.IndexImpl) {
				return new WeightsImplChar.IndexImmutable((WeightsImplChar.IndexImpl) weights, elements, reIndexMap);
			} else if (weights instanceof WeightsImplObj.IndexImpl) {
				return new WeightsImplObj.IndexImmutable<>((WeightsImplObj.IndexImpl<?>) weights, elements, reIndexMap);
			} else {
				throw new IllegalArgumentException("unknown weights implementation: " + weights.getClass());
			}
		}

		static class Builder {

			private final GraphElementSet.FixedSize elements;
			private final Map<String, WeightsImpl.IndexImmutable<?>> weights;

			Builder(GraphElementSet.FixedSize elements) {
				this.elements = Objects.requireNonNull(elements);
				weights = new Object2ObjectOpenHashMap<>();
			}

			void copyAndAddWeights(String key, IWeights<?> weights) {
				Object oldWeights = this.weights.put(key, WeightsImpl.IndexImmutable.copyOf(weights, elements));
				if (oldWeights != null)
					throw new IllegalArgumentException("duplicate key: " + key);
			}

			void copyAndAddWeightsReindexed(String key, IWeights<?> weights,
					IndexGraphBuilder.ReIndexingMap reIndexMap) {
				Object oldWeights = this.weights.put(key,
						WeightsImpl.IndexImmutable.copyOfReindexed(weights, elements, reIndexMap));
				if (oldWeights != null)
					throw new IllegalArgumentException("duplicate key: " + key);
			}

			Map<String, WeightsImpl.IndexImmutable<?>> build() {
				return Map.copyOf(weights);
			}
		}

	}

	static abstract class IntMapped<T> implements IWeights<T>, WeightsImpl {

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

	static abstract class IntImmutableView<T> extends ImmutableView<Integer, T> implements IWeights<T> {

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

	static void checkSameSize(GraphElementSet i1, GraphElementSet i2) {
		if (i1.size() != i2.size())
			throw new IllegalArgumentException("Elements sets size mismatch: " + i1.size() + " != " + i2.size());
	}

	static boolean isEqual(IntSet elementsSet, IWeights<?> w1, IWeights<?> w2) {
		if (w1 == w2)
			return true;
		if (w1 instanceof IWeightsObj<?> && w2 instanceof IWeightsObj<?>) {
			for (int elm : elementsSet)
				if (!Objects.equals(((IWeightsObj<?>) w1).get(elm), ((IWeightsObj<?>) w2).get(elm)))
					return false;
			return true;
		} else if (w1 instanceof IWeightsByte && w2 instanceof IWeightsByte) {
			for (int elm : elementsSet)
				if (((IWeightsByte) w1).get(elm) != ((IWeightsByte) w2).get(elm))
					return false;
			return true;
		} else if (w1 instanceof IWeightsShort && w2 instanceof IWeightsShort) {
			for (int elm : elementsSet)
				if (((IWeightsShort) w1).get(elm) != ((IWeightsShort) w2).get(elm))
					return false;
			return true;
		} else if (w1 instanceof IWeightsInt && w2 instanceof IWeightsInt) {
			for (int elm : elementsSet)
				if (((IWeightsInt) w1).get(elm) != ((IWeightsInt) w2).get(elm))
					return false;
			return true;
		} else if (w1 instanceof IWeightsLong && w2 instanceof IWeightsLong) {
			for (int elm : elementsSet)
				if (((IWeightsLong) w1).get(elm) != ((IWeightsLong) w2).get(elm))
					return false;
			return true;
		} else if (w1 instanceof IWeightsFloat && w2 instanceof IWeightsFloat) {
			for (int elm : elementsSet)
				if (((IWeightsFloat) w1).get(elm) != ((IWeightsFloat) w2).get(elm))
					return false;
			return true;
		} else if (w1 instanceof IWeightsDouble && w2 instanceof IWeightsDouble) {
			for (int elm : elementsSet)
				if (((IWeightsDouble) w1).get(elm) != ((IWeightsDouble) w2).get(elm))
					return false;
			return true;
		} else if (w1 instanceof IWeightsBool && w2 instanceof IWeightsBool) {
			for (int elm : elementsSet)
				if (((IWeightsBool) w1).get(elm) != ((IWeightsBool) w2).get(elm))
					return false;
			return true;
		} else if (w1 instanceof IWeightsChar && w2 instanceof IWeightsChar) {
			for (int elm : elementsSet)
				if (((IWeightsChar) w1).get(elm) != ((IWeightsChar) w2).get(elm))
					return false;
			return true;
		} else {
			return false;
		}
	}

	static int hashCode(IntSet elementsSet, IWeights<?> w) {
		int h = 0;
		if (w instanceof IWeightsObj<?>) {
			for (int elm : elementsSet)
				h += Objects.hashCode(((IWeightsObj<?>) w).get(elm));
		} else if (w instanceof IWeightsByte) {
			for (int elm : elementsSet)
				h += ((IWeightsByte) w).get(elm);
		} else if (w instanceof IWeightsShort) {
			for (int elm : elementsSet)
				h += ((IWeightsShort) w).get(elm);
		} else if (w instanceof IWeightsInt) {
			for (int elm : elementsSet)
				h += ((IWeightsInt) w).get(elm);
		} else if (w instanceof IWeightsLong) {
			for (int elm : elementsSet) {
				long x = ((IWeightsLong) w).get(elm);
				h += (int) (x ^ (x >>> 32));
			}
		} else if (w instanceof IWeightsFloat) {
			for (int elm : elementsSet)
				h += java.lang.Float.floatToRawIntBits(((IWeightsFloat) w).get(elm));
		} else if (w instanceof IWeightsDouble) {
			for (int elm : elementsSet) {
				long x = java.lang.Double.doubleToRawLongBits(((IWeightsDouble) w).get(elm));
				h += (int) (x ^ (x >>> 32));
			}
		} else if (w instanceof IWeightsBool) {
			for (int elm : elementsSet)
				h += ((IWeightsBool) w).get(elm) ? 1231 : 1237;
		} else if (w instanceof IWeightsChar) {
			for (int elm : elementsSet)
				h += ((IWeightsChar) w).get(elm);
		} else {
			throw new IllegalArgumentException("Unsupported weights type: " + w.getClass());
		}
		return h;
	}

	static abstract class ObjMapped<K, T> implements Weights<K, T>, WeightsImpl {

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

	static abstract class ObjImmutableView<K, T> extends ImmutableView<K, T> implements Weights<K, T> {

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
