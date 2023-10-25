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

interface WeightsImpl<E> extends Weights<E> {

	static interface Index<E> extends WeightsImpl<E> {

	}

	static abstract class IndexAbstract<E> implements WeightsImpl.Index<E> {

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

	static interface IndexMutable<E> extends WeightsImpl.Index<E> {

		int capacity();

		void expand(int newCapacity);

		void clear(int idx);

		void clear();

		void swap(int idx1, int idx2);

		static <D> WeightsImpl.IndexMutable<D> newInstance(GraphElementSet elements, Class<? super D> type, D defVal) {
			Weights<?> container;
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

		static WeightsImpl.IndexMutable<?> copyOf(Weights<?> weights, GraphElementSet elements) {
			if (weights instanceof WeightsImpl.ImmutableView<?>)
				weights = ((WeightsImpl.ImmutableView<?>) weights).weights;
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
			<E, WeightsT extends Weights<E>> WeightsT getWeights(String key) {
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

	static interface IndexImmutable<E> extends WeightsImpl<E> {

		static WeightsImpl.IndexImmutable<?> copyOf(Weights<?> weights, GraphElementSet.FixedSize elements) {
			if (weights instanceof WeightsImpl.ImmutableView<?>)
				weights = ((WeightsImpl.ImmutableView<?>) weights).weights;
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

		static WeightsImpl.IndexImmutable<?> copyOfReindexed(Weights<?> weights, GraphElementSet.FixedSize elements,
				IndexGraphBuilder.ReIndexingMap reIndexMap) {
			if (weights instanceof WeightsImpl.ImmutableView<?>)
				weights = ((WeightsImpl.ImmutableView<?>) weights).weights;
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

			void copyAndAddWeights(String key, Weights<?> weights) {
				Object oldWeights = this.weights.put(key, WeightsImpl.IndexImmutable.copyOf(weights, elements));
				if (oldWeights != null)
					throw new IllegalArgumentException("duplicate key: " + key);
			}

			void copyAndAddWeightsReindexed(String key, Weights<?> weights,
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

	static abstract class Mapped<E> implements WeightsImpl<E> {

		final WeightsImpl.IndexAbstract<E> weights;
		final IndexIdMap indexMap;

		Mapped(WeightsImpl.Index<E> weights, IndexIdMap indexMap) {
			this.weights = (WeightsImpl.IndexAbstract<E>) Objects.requireNonNull(weights);
			this.indexMap = indexMap;
		}

		WeightsImpl.IndexAbstract<E> weights() {
			return weights;
		}

		@SuppressWarnings({ "unchecked", "rawtypes" })
		static WeightsImpl.Mapped<?> newInstance(WeightsImpl.Index<?> weights, IndexIdMap indexMap) {
			if (weights instanceof WeightsImplByte.IndexImpl) {
				return new WeightsImplByte.Mapped((WeightsImplByte.IndexImpl) weights, indexMap);
			} else if (weights instanceof WeightsImplShort.IndexImpl) {
				return new WeightsImplShort.Mapped((WeightsImplShort.IndexImpl) weights, indexMap);
			} else if (weights instanceof WeightsImplInt.IndexImpl) {
				return new WeightsImplInt.Mapped((WeightsImplInt.IndexImpl) weights, indexMap);
			} else if (weights instanceof WeightsImplLong.IndexImpl) {
				return new WeightsImplLong.Mapped((WeightsImplLong.IndexImpl) weights, indexMap);
			} else if (weights instanceof WeightsImplFloat.IndexImpl) {
				return new WeightsImplFloat.Mapped((WeightsImplFloat.IndexImpl) weights, indexMap);
			} else if (weights instanceof WeightsImplDouble.IndexImpl) {
				return new WeightsImplDouble.Mapped((WeightsImplDouble.IndexImpl) weights, indexMap);
			} else if (weights instanceof WeightsImplBool.IndexImpl) {
				return new WeightsImplBool.Mapped((WeightsImplBool.IndexImpl) weights, indexMap);
			} else if (weights instanceof WeightsImplChar.IndexImpl) {
				return new WeightsImplChar.Mapped((WeightsImplChar.IndexImpl) weights, indexMap);
			} else {
				return new WeightsImplObj.Mapped<>((WeightsImplObj.IndexImpl) weights, indexMap);
			}
		}

	}

	static interface Immutable<E> extends WeightsImpl<E> {

	}

	static abstract class ImmutableView<E> implements Immutable<E> {

		final Weights<E> weights;

		ImmutableView(Weights<E> weights) {
			this.weights = Objects.requireNonNull(weights);
		}

		Weights<E> weights() {
			return weights;
		}

		@SuppressWarnings("unchecked")
		static <E> Weights<E> newInstance(Weights<E> weights) {
			if (weights instanceof Immutable<?>)
				return weights;
			if (weights instanceof WeightsByte)
				return (Weights<E>) new WeightsImplByte.ImmutableView((WeightsByte) weights);
			if (weights instanceof WeightsShort)
				return (Weights<E>) new WeightsImplShort.ImmutableView((WeightsShort) weights);
			if (weights instanceof WeightsInt)
				return (Weights<E>) new WeightsImplInt.ImmutableView((WeightsInt) weights);
			if (weights instanceof WeightsLong)
				return (Weights<E>) new WeightsImplLong.ImmutableView((WeightsLong) weights);
			if (weights instanceof WeightsFloat)
				return (Weights<E>) new WeightsImplFloat.ImmutableView((WeightsFloat) weights);
			if (weights instanceof WeightsDouble)
				return (Weights<E>) new WeightsImplDouble.ImmutableView((WeightsDouble) weights);
			if (weights instanceof WeightsBool)
				return (Weights<E>) new WeightsImplBool.ImmutableView((WeightsBool) weights);
			if (weights instanceof WeightsChar)
				return (Weights<E>) new WeightsImplChar.ImmutableView((WeightsChar) weights);
			if (weights instanceof WeightsObj)
				return new WeightsImplObj.ImmutableView<>((WeightsObj<E>) weights);
			throw new IllegalArgumentException("Unsupported weights type: " + weights.getClass());
		}
	}

	static void checkSameSize(GraphElementSet i1, GraphElementSet i2) {
		if (i1.size() != i2.size())
			throw new IllegalArgumentException("Elements sets size mismatch: " + i1.size() + " != " + i2.size());
	}

	static boolean isEqual(IntSet elementsSet, Weights<?> w1, Weights<?> w2) {
		if (w1 == w2)
			return true;
		if (w1 instanceof WeightsObj<?> && w2 instanceof WeightsObj<?>) {
			for (int elm : elementsSet)
				if (!Objects.equals(((WeightsObj<?>) w1).get(elm), ((WeightsObj<?>) w2).get(elm)))
					return false;
			return true;
		} else if (w1 instanceof WeightsByte && w2 instanceof WeightsByte) {
			for (int elm : elementsSet)
				if (((WeightsByte) w1).get(elm) != ((WeightsByte) w2).get(elm))
					return false;
			return true;
		} else if (w1 instanceof WeightsShort && w2 instanceof WeightsShort) {
			for (int elm : elementsSet)
				if (((WeightsShort) w1).get(elm) != ((WeightsShort) w2).get(elm))
					return false;
			return true;
		} else if (w1 instanceof WeightsInt && w2 instanceof WeightsInt) {
			for (int elm : elementsSet)
				if (((WeightsInt) w1).get(elm) != ((WeightsInt) w2).get(elm))
					return false;
			return true;
		} else if (w1 instanceof WeightsLong && w2 instanceof WeightsLong) {
			for (int elm : elementsSet)
				if (((WeightsLong) w1).get(elm) != ((WeightsLong) w2).get(elm))
					return false;
			return true;
		} else if (w1 instanceof WeightsFloat && w2 instanceof WeightsFloat) {
			for (int elm : elementsSet)
				if (((WeightsFloat) w1).get(elm) != ((WeightsFloat) w2).get(elm))
					return false;
			return true;
		} else if (w1 instanceof WeightsDouble && w2 instanceof WeightsDouble) {
			for (int elm : elementsSet)
				if (((WeightsDouble) w1).get(elm) != ((WeightsDouble) w2).get(elm))
					return false;
			return true;
		} else if (w1 instanceof WeightsBool && w2 instanceof WeightsBool) {
			for (int elm : elementsSet)
				if (((WeightsBool) w1).get(elm) != ((WeightsBool) w2).get(elm))
					return false;
			return true;
		} else if (w1 instanceof WeightsChar && w2 instanceof WeightsChar) {
			for (int elm : elementsSet)
				if (((WeightsChar) w1).get(elm) != ((WeightsChar) w2).get(elm))
					return false;
			return true;
		} else {
			return false;
		}
	}

	static int hashCode(IntSet elementsSet, Weights<?> w) {
		int h = 0;
		if (w instanceof WeightsObj<?>) {
			for (int elm : elementsSet)
				h += Objects.hashCode(((WeightsObj<?>) w).get(elm));
		} else if (w instanceof WeightsByte) {
			for (int elm : elementsSet)
				h += ((WeightsByte) w).get(elm);
		} else if (w instanceof WeightsShort) {
			for (int elm : elementsSet)
				h += ((WeightsShort) w).get(elm);
		} else if (w instanceof WeightsInt) {
			for (int elm : elementsSet)
				h += ((WeightsInt) w).get(elm);
		} else if (w instanceof WeightsLong) {
			for (int elm : elementsSet) {
				long x = ((WeightsLong) w).get(elm);
				h += (int) (x ^ (x >>> 32));
			}
		} else if (w instanceof WeightsFloat) {
			for (int elm : elementsSet)
				h += java.lang.Float.floatToRawIntBits(((WeightsFloat) w).get(elm));
		} else if (w instanceof WeightsDouble) {
			for (int elm : elementsSet) {
				long x = java.lang.Double.doubleToRawLongBits(((WeightsDouble) w).get(elm));
				h += (int) (x ^ (x >>> 32));
			}
		} else if (w instanceof WeightsBool) {
			for (int elm : elementsSet)
				h += ((WeightsBool) w).get(elm) ? 1231 : 1237;
		} else if (w instanceof WeightsChar) {
			for (int elm : elementsSet)
				h += ((WeightsChar) w).get(elm);
		} else {
			throw new IllegalArgumentException("Unsupported weights type: " + w.getClass());
		}
		return h;
	}

}
