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

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

interface WeightsImpl<E> extends Weights<E> {

	DataContainer<E> container();

	WeightsImpl<E> copy(IDStrategyImpl indexIdStrat, IndexGraphMap indexMap);

	@SuppressWarnings("unchecked")
	default WeightsImpl<E> unmodifiable() {
		if (this instanceof UnmodifiableWeights<?>)
			return this;
		if (this instanceof Weights.Byte)
			return (WeightsImpl<E>) new UnmodifiableWeights.Byte((Weights.Byte) this);
		if (this instanceof Weights.Short)
			return (WeightsImpl<E>) new UnmodifiableWeights.Short((Weights.Short) this);
		if (this instanceof Weights.Int)
			return (WeightsImpl<E>) new UnmodifiableWeights.Int((Weights.Int) this);
		if (this instanceof Weights.Long)
			return (WeightsImpl<E>) new UnmodifiableWeights.Long((Weights.Long) this);
		if (this instanceof Weights.Float)
			return (WeightsImpl<E>) new UnmodifiableWeights.Float((Weights.Float) this);
		if (this instanceof Weights.Double)
			return (WeightsImpl<E>) new UnmodifiableWeights.Double((Weights.Double) this);
		if (this instanceof Weights.Bool)
			return (WeightsImpl<E>) new UnmodifiableWeights.Bool((Weights.Bool) this);
		if (this instanceof Weights.Char)
			return (WeightsImpl<E>) new UnmodifiableWeights.Char((Weights.Char) this);
		return new UnmodifiableWeights.Obj<>(this);
	}

	static Weights<?> wrapContainerDirected(DataContainer<?> container) {
		if (container instanceof DataContainer.Obj<?>) {
			return new WeightsImpl.Direct.Obj<>((DataContainer.Obj<?>) container);
		} else if (container instanceof DataContainer.Byte) {
			return new WeightsImpl.Direct.Byte((DataContainer.Byte) container);
		} else if (container instanceof DataContainer.Short) {
			return new WeightsImpl.Direct.Short((DataContainer.Short) container);
		} else if (container instanceof DataContainer.Int) {
			return new WeightsImpl.Direct.Int((DataContainer.Int) container);
		} else if (container instanceof DataContainer.Long) {
			return new WeightsImpl.Direct.Long((DataContainer.Long) container);
		} else if (container instanceof DataContainer.Float) {
			return new WeightsImpl.Direct.Float((DataContainer.Float) container);
		} else if (container instanceof DataContainer.Double) {
			return new WeightsImpl.Direct.Double((DataContainer.Double) container);
		} else if (container instanceof DataContainer.Bool) {
			return new WeightsImpl.Direct.Bool((DataContainer.Bool) container);
		} else if (container instanceof DataContainer.Char) {
			return new WeightsImpl.Direct.Char((DataContainer.Char) container);
		} else {
			throw new IllegalArgumentException(container.getClass().toString());
		}
	}

	static Weights<?> wrapContainerMapped(Weights<?> weights, IndexGraphMap indexMap) {
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

	static abstract class Abstract<E> implements WeightsImpl<E> {

		private final DataContainer<E> container;

		private Abstract(DataContainer<E> container) {
			this.container = container;
		}

		@Override
		public DataContainer<E> container() {
			return container;
		}
	}

	static abstract class Direct<E> extends WeightsImpl.Abstract<E> {
		private Direct(DataContainer<E> data) {
			super(data);
		}

		static class Obj<E> extends Direct<E> {
			Obj(DataContainer.Obj<E> container) {
				super(container);
			}

			@Override
			public DataContainer.Obj<E> container() {
				return (DataContainer.Obj<E>) super.container();
			}

			@Override
			public E get(int id) {
				return container().get(id);
			}

			@Override
			public void set(int id, E val) {
				container().set(id, val);
			}

			@Override
			public E defaultWeight() {
				return container().defaultVal();
			}

			@Override
			public WeightsImpl.Direct.Obj<E> copy(IDStrategyImpl indexIdStrat, IndexGraphMap indexMap) {
				return new WeightsImpl.Direct.Obj<>(container().copy(indexIdStrat));
			}
		}

		static class Byte extends Direct<java.lang.Byte> implements Weights.Byte {
			Byte(DataContainer.Byte container) {
				super(container);
			}

			@Override
			public DataContainer.Byte container() {
				return (DataContainer.Byte) super.container();
			}

			@Override
			public byte getByte(int id) {
				return container().getByte(id);
			}

			@Override
			public void set(int key, byte weight) {
				container().set(key, weight);
			}

			@Override
			public byte defaultWeightByte() {
				return container().defaultValByte();
			}

			@Override
			public WeightsImpl.Direct.Byte copy(IDStrategyImpl indexIdStrat, IndexGraphMap indexMap) {
				return new WeightsImpl.Direct.Byte(container().copy(indexIdStrat));
			}
		}

		static class Short extends Direct<java.lang.Short> implements Weights.Short {
			Short(DataContainer.Short container) {
				super(container);
			}

			@Override
			public DataContainer.Short container() {
				return (DataContainer.Short) super.container();
			}

			@Override
			public short getShort(int id) {
				return container().getShort(id);
			}

			@Override
			public void set(int key, short weight) {
				container().set(key, weight);
			}

			@Override
			public short defaultWeightShort() {
				return container().defaultValShort();
			}

			@Override
			public WeightsImpl.Direct.Short copy(IDStrategyImpl indexIdStrat, IndexGraphMap indexMap) {
				return new WeightsImpl.Direct.Short(container().copy(indexIdStrat));
			}
		}

		static class Int extends Direct<Integer> implements Weights.Int {
			Int(DataContainer.Int container) {
				super(container);
			}

			@Override
			public DataContainer.Int container() {
				return (DataContainer.Int) super.container();
			}

			@Override
			public int getInt(int id) {
				return container().getInt(id);
			}

			@Override
			public void set(int id, int weight) {
				container().set(id, weight);
			}

			@Override
			public int defaultWeightInt() {
				return container().defaultValInt();
			}

			@Override
			public WeightsImpl.Direct.Int copy(IDStrategyImpl indexIdStrat, IndexGraphMap indexMap) {
				return new WeightsImpl.Direct.Int(container().copy(indexIdStrat));
			}
		}

		static class Long extends Direct<java.lang.Long> implements Weights.Long {
			Long(DataContainer.Long container) {
				super(container);
			}

			@Override
			public DataContainer.Long container() {
				return (DataContainer.Long) super.container();
			}

			@Override
			public long getLong(int id) {
				return container().getLong(id);
			}

			@Override
			public void set(int key, long weight) {
				container().set(key, weight);
			}

			@Override
			public long defaultWeightLong() {
				return container().defaultValLong();
			}

			@Override
			public WeightsImpl.Direct.Long copy(IDStrategyImpl indexIdStrat, IndexGraphMap indexMap) {
				return new WeightsImpl.Direct.Long(container().copy(indexIdStrat));
			}
		}

		static class Float extends Direct<java.lang.Float> implements Weights.Float {
			Float(DataContainer.Float container) {
				super(container);
			}

			@Override
			public DataContainer.Float container() {
				return (DataContainer.Float) super.container();
			}

			@Override
			public float getFloat(int id) {
				return container().getFloat(id);
			}

			@Override
			public void set(int key, float weight) {
				container().set(key, weight);
			}

			@Override
			public float defaultWeightFloat() {
				return container().defaultValFloat();
			}

			@Override
			public WeightsImpl.Direct.Float copy(IDStrategyImpl indexIdStrat, IndexGraphMap indexMap) {
				return new WeightsImpl.Direct.Float(container().copy(indexIdStrat));
			}
		}

		static class Double extends Direct<java.lang.Double> implements Weights.Double {
			Double(DataContainer.Double container) {
				super(container);
			}

			@Override
			public DataContainer.Double container() {
				return (DataContainer.Double) super.container();
			}

			@Override
			public double getDouble(int id) {
				return container().getDouble(id);
			}

			@Override
			public void set(int key, double weight) {
				container().set(key, weight);
			}

			@Override
			public double defaultWeightDouble() {
				return container().defaultValDouble();
			}

			@Override
			public WeightsImpl.Direct.Double copy(IDStrategyImpl indexIdStrat, IndexGraphMap indexMap) {
				return new WeightsImpl.Direct.Double(container().copy(indexIdStrat));
			}
		}

		static class Bool extends Direct<Boolean> implements Weights.Bool {
			Bool(DataContainer.Bool container) {
				super(container);
			}

			@Override
			public DataContainer.Bool container() {
				return (DataContainer.Bool) super.container();
			}

			@Override
			public boolean getBool(int id) {
				return container().getBool(id);
			}

			@Override
			public void set(int id, boolean weight) {
				container().set(id, weight);
			}

			@Override
			public boolean defaultWeightBool() {
				return container().defaultValBool();
			}

			@Override
			public WeightsImpl.Direct.Bool copy(IDStrategyImpl indexIdStrat, IndexGraphMap indexMap) {
				return new WeightsImpl.Direct.Bool(container().copy(indexIdStrat));
			}
		}

		static class Char extends Direct<Character> implements Weights.Char {
			Char(DataContainer.Char container) {
				super(container);
			}

			@Override
			public DataContainer.Char container() {
				return (DataContainer.Char) super.container();
			}

			@Override
			public char getChar(int id) {
				return container().getChar(id);
			}

			@Override
			public void set(int id, char weight) {
				container().set(id, weight);
			}

			@Override
			public char defaultWeightChar() {
				return container().defaultValChar();
			}

			@Override
			public WeightsImpl.Direct.Char copy(IDStrategyImpl indexIdStrat, IndexGraphMap indexMap) {
				return new WeightsImpl.Direct.Char(container().copy(indexIdStrat));
			}
		}
	}

	static abstract class Mapped<E> implements Weights<E> {

		private final Weights<E> weights;
		final IndexGraphMap indexMap;

		private Mapped(Weights<E> weights, IndexGraphMap indexMap) {
			this.weights = Objects.requireNonNull(weights);
			this.indexMap = indexMap;
		}

		Weights<E> weights() {
			return weights;
		}

		static class Obj<E> extends Mapped<E> {
			Obj(Weights<E> weights, IndexGraphMap indexMap) {
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
			Byte(Weights.Byte weights, IndexGraphMap indexMap) {
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
			Short(Weights.Short container, IndexGraphMap indexMap) {
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
			Int(Weights.Int container, IndexGraphMap indexMap) {
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
			Long(Weights.Long container, IndexGraphMap indexMap) {
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
			Float(Weights.Float container, IndexGraphMap indexMap) {
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
			Double(Weights.Double container, IndexGraphMap indexMap) {
				super(container, indexMap);
			}

			@Override
			public Weights.Double weights() {
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
			Bool(Weights.Bool container, IndexGraphMap indexMap) {
				super(container, indexMap);
			}

			@Override
			public Weights.Bool weights() {
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
			Char(Weights.Char container, IndexGraphMap indexMap) {
				super(container, indexMap);
			}

			@Override
			public Weights.Char weights() {
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

	static class Manager extends DataContainer.Manager {

		private final Map<Object, WeightsImpl<?>> weights = new Object2ObjectArrayMap<>();

		Manager(int initialCapacity) {
			super(initialCapacity);
		}

		Manager(Manager orig, IDStrategyImpl indexIdStrat, IndexGraphMap indexMap) {
			this(indexIdStrat.size());
			for (var entry : orig.weights.entrySet())
				addWeights(entry.getKey(), entry.getValue().copy(indexIdStrat, indexMap));
		}

		@SuppressWarnings("unchecked")
		<E, WeightsT extends Weights<E>> WeightsT getWeights(Object key) {
			return (WeightsT) weights.get(key);
		}

		@Override
		final void addContainer(Object key, DataContainer<?> container) {
			throw new UnsupportedOperationException();
		}

		void addWeights(Object key, Weights<?> weights) {
			super.addContainer(key, ((WeightsImpl<?>) weights).container());
			Object o = this.weights.put(key, (WeightsImpl<?>) weights);
			assert o == null;
		}

		void removeWeights(Object key) {
			Object o1 = weights.remove(key);
			if (o1 == null)
				return;
			Object o2 = containers.remove(key);
			assert o2 != null;
		}

		Set<Object> weightsKeys() {
			return Collections.unmodifiableSet(weights.keySet());
		}

	}

	static abstract class UnmodifiableWeights<E> implements WeightsImpl<E> {

		private final WeightsImpl<E> weights;

		UnmodifiableWeights(Weights<E> w) {
			this.weights = (WeightsImpl<E>) Objects.requireNonNull(w);
		}

		@Override
		public DataContainer<E> container() {
			return weights.container();
		}

		@Override
		public WeightsImpl<E> copy(IDStrategyImpl indexIdStrat, IndexGraphMap indexMap) {
			return weights.copy(indexIdStrat, indexMap);
		}

		Weights<E> weights() {
			return weights;
		}

		static class Obj<E> extends UnmodifiableWeights<E> {

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

		static class Byte extends UnmodifiableWeights<java.lang.Byte> implements Weights.Byte {
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

		static class Short extends UnmodifiableWeights<java.lang.Short> implements Weights.Short {
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

		static class Int extends UnmodifiableWeights<Integer> implements Weights.Int {
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

		static class Long extends UnmodifiableWeights<java.lang.Long> implements Weights.Long {
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

		static class Float extends UnmodifiableWeights<java.lang.Float> implements Weights.Float {
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

		static class Double extends UnmodifiableWeights<java.lang.Double> implements Weights.Double {
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

		static class Bool extends UnmodifiableWeights<Boolean> implements Weights.Bool {
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

		static class Char extends UnmodifiableWeights<Character> implements Weights.Char {
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

	static WeightFunction indexWeightFuncFromIdWeightFunc(WeightFunction w, IndexGraphMap map) {
		if (w == null || w == WeightFunction.CardinalityWeightFunction)
			return w;
		if (w instanceof WeightFunction.Int) {
			WeightFunction.Int wInt = (WeightFunction.Int) w;
			WeightFunction.Int r = idx -> wInt.weightInt(map.indexToId(idx)); // TODO.
			return r;
		}
		return idx -> w.weight(map.indexToId(idx)); // TODO
	}

}
