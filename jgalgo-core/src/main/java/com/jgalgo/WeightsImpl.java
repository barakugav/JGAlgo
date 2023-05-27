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
import java.util.Set;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

abstract class WeightsImpl<E> implements Weights<E> {

	final DataContainer<E> container;

	private WeightsImpl(DataContainer<E> container) {
		this.container = container;
	}

	DataContainer<E> container() {
		return container;
	}

	abstract WeightsImpl<E> copy(IDStrategy idStrat);

	@SuppressWarnings("unchecked")
	static <E, WeightsT extends Weights<E>> WeightsT newInstance(IDStrategy idStrat, Class<? super E> type, E defVal) {
		DataContainer<E> container = DataContainer.newInstance(idStrat, type, defVal);
		boolean isContinues = idStrat instanceof IDStrategy.Continues;
		return (WeightsT) (isContinues ? wrapContainerDirected(container) : wrapContainerMapped(container, idStrat));
	}

	private static Weights<?> wrapContainerDirected(DataContainer<?> container) {
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

	private static Weights<?> wrapContainerMapped(DataContainer<?> container, IDStrategy idStrat) {
		if (container instanceof DataContainer.Obj<?>) {
			return new WeightsImpl.Mapped.Obj<>((DataContainer.Obj<?>) container, idStrat);
		} else if (container instanceof DataContainer.Byte) {
			return new WeightsImpl.Mapped.Byte((DataContainer.Byte) container, idStrat);
		} else if (container instanceof DataContainer.Short) {
			return new WeightsImpl.Mapped.Short((DataContainer.Short) container, idStrat);
		} else if (container instanceof DataContainer.Int) {
			return new WeightsImpl.Mapped.Int((DataContainer.Int) container, idStrat);
		} else if (container instanceof DataContainer.Long) {
			return new WeightsImpl.Mapped.Long((DataContainer.Long) container, idStrat);
		} else if (container instanceof DataContainer.Float) {
			return new WeightsImpl.Mapped.Float((DataContainer.Float) container, idStrat);
		} else if (container instanceof DataContainer.Double) {
			return new WeightsImpl.Mapped.Double((DataContainer.Double) container, idStrat);
		} else if (container instanceof DataContainer.Bool) {
			return new WeightsImpl.Mapped.Bool((DataContainer.Bool) container, idStrat);
		} else if (container instanceof DataContainer.Char) {
			return new WeightsImpl.Mapped.Char((DataContainer.Char) container, idStrat);
		} else {
			throw new IllegalArgumentException(container.getClass().toString());
		}
	}

	static abstract class Direct<E> extends WeightsImpl<E> {
		private Direct(DataContainer<E> data) {
			super(data);
		}

		static class Obj<E> extends Direct<E> {
			Obj(DataContainer.Obj<E> container) {
				super(container);
			}

			@Override
			DataContainer.Obj<E> container() {
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
			WeightsImpl.Direct.Obj<E> copy(IDStrategy idStrat) {
				return new WeightsImpl.Direct.Obj<>(container().copy(idStrat));
			}
		}

		static class Byte extends Direct<java.lang.Byte> implements Weights.Byte {
			Byte(DataContainer.Byte container) {
				super(container);
			}

			@Override
			DataContainer.Byte container() {
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
			WeightsImpl.Direct.Byte copy(IDStrategy idStrat) {
				return new WeightsImpl.Direct.Byte(container().copy(idStrat));
			}
		}

		static class Short extends Direct<java.lang.Short> implements Weights.Short {
			Short(DataContainer.Short container) {
				super(container);
			}

			@Override
			DataContainer.Short container() {
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
			WeightsImpl.Direct.Short copy(IDStrategy idStrat) {
				return new WeightsImpl.Direct.Short(container().copy(idStrat));
			}
		}

		static class Int extends Direct<Integer> implements Weights.Int {
			Int(DataContainer.Int container) {
				super(container);
			}

			@Override
			DataContainer.Int container() {
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
			WeightsImpl.Direct.Int copy(IDStrategy idStrat) {
				return new WeightsImpl.Direct.Int(container().copy(idStrat));
			}
		}

		static class Long extends Direct<java.lang.Long> implements Weights.Long {
			Long(DataContainer.Long container) {
				super(container);
			}

			@Override
			DataContainer.Long container() {
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
			WeightsImpl.Direct.Long copy(IDStrategy idStrat) {
				return new WeightsImpl.Direct.Long(container().copy(idStrat));
			}
		}

		static class Float extends Direct<java.lang.Float> implements Weights.Float {
			Float(DataContainer.Float container) {
				super(container);
			}

			@Override
			DataContainer.Float container() {
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
			WeightsImpl.Direct.Float copy(IDStrategy idStrat) {
				return new WeightsImpl.Direct.Float(container().copy(idStrat));
			}
		}

		static class Double extends Direct<java.lang.Double> implements Weights.Double {
			Double(DataContainer.Double container) {
				super(container);
			}

			@Override
			DataContainer.Double container() {
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
			WeightsImpl.Direct.Double copy(IDStrategy idStrat) {
				return new WeightsImpl.Direct.Double(container().copy(idStrat));
			}
		}

		static class Bool extends Direct<Boolean> implements Weights.Bool {
			Bool(DataContainer.Bool container) {
				super(container);
			}

			@Override
			DataContainer.Bool container() {
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
			WeightsImpl.Direct.Bool copy(IDStrategy idStrat) {
				return new WeightsImpl.Direct.Bool(container().copy(idStrat));
			}
		}

		static class Char extends Direct<Character> implements Weights.Char {
			Char(DataContainer.Char container) {
				super(container);
			}

			@Override
			DataContainer.Char container() {
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
			WeightsImpl.Direct.Char copy(IDStrategy idStrat) {
				return new WeightsImpl.Direct.Char(container().copy(idStrat));
			}
		}
	}

	static abstract class Mapped<E> extends WeightsImpl<E> {

		final IDStrategy idStrategy;

		private Mapped(DataContainer<E> data, IDStrategy idStrategy) {
			super(data);
			this.idStrategy = idStrategy;
		}

		static class Obj<E> extends Mapped<E> {
			Obj(DataContainer.Obj<E> container, IDStrategy idStrategy) {
				super(container, idStrategy);
			}

			@Override
			DataContainer.Obj<E> container() {
				return (DataContainer.Obj<E>) super.container();
			}

			@Override
			public E get(int id) {
				return container().get(idStrategy.idToIdx(id));
			}

			@Override
			public void set(int id, E val) {
				container().set(idStrategy.idToIdx(id), val);
			}

			@Override
			public E defaultWeight() {
				return container().defaultVal();
			}

			@Override
			WeightsImpl.Mapped.Obj<E> copy(IDStrategy idStrat) {
				return new WeightsImpl.Mapped.Obj<>(container().copy(idStrat), idStrat);
			}
		}

		static class Byte extends Mapped<java.lang.Byte> implements Weights.Byte {
			Byte(DataContainer.Byte container, IDStrategy idStrategy) {
				super(container, idStrategy);
			}

			@Override
			DataContainer.Byte container() {
				return (DataContainer.Byte) super.container();
			}

			@Override
			public byte getByte(int id) {
				return container().getByte(idStrategy.idToIdx(id));
			}

			@Override
			public void set(int id, byte weight) {
				container().set(idStrategy.idToIdx(id), weight);
			}

			@Override
			public byte defaultWeightByte() {
				return container().defaultValByte();
			}

			@Override
			WeightsImpl.Mapped.Byte copy(IDStrategy idStrat) {
				return new WeightsImpl.Mapped.Byte(container().copy(idStrat), idStrat);
			}
		}

		static class Short extends Mapped<java.lang.Short> implements Weights.Short {
			Short(DataContainer.Short container, IDStrategy idStrategy) {
				super(container, idStrategy);
			}

			@Override
			DataContainer.Short container() {
				return (DataContainer.Short) super.container();
			}

			@Override
			public short getShort(int id) {
				return container().getShort(idStrategy.idToIdx(id));
			}

			@Override
			public void set(int id, short weight) {
				container().set(idStrategy.idToIdx(id), weight);
			}

			@Override
			public short defaultWeightShort() {
				return container().defaultValShort();
			}

			@Override
			WeightsImpl.Mapped.Short copy(IDStrategy idStrat) {
				return new WeightsImpl.Mapped.Short(container().copy(idStrat), idStrat);
			}
		}

		static class Int extends Mapped<Integer> implements Weights.Int {
			Int(DataContainer.Int container, IDStrategy idStrategy) {
				super(container, idStrategy);
			}

			@Override
			DataContainer.Int container() {
				return (DataContainer.Int) super.container();
			}

			@Override
			public int getInt(int id) {
				return container().getInt(idStrategy.idToIdx(id));
			}

			@Override
			public void set(int id, int weight) {
				container().set(idStrategy.idToIdx(id), weight);
			}

			@Override
			public int defaultWeightInt() {
				return container().defaultValInt();
			}

			@Override
			WeightsImpl.Mapped.Int copy(IDStrategy idStrat) {
				return new WeightsImpl.Mapped.Int(container().copy(idStrat), idStrat);
			}
		}

		static class Long extends Mapped<java.lang.Long> implements Weights.Long {
			Long(DataContainer.Long container, IDStrategy idStrategy) {
				super(container, idStrategy);
			}

			@Override
			DataContainer.Long container() {
				return (DataContainer.Long) super.container();
			}

			@Override
			public long getLong(int id) {
				return container().getLong(idStrategy.idToIdx(id));
			}

			@Override
			public void set(int id, long weight) {
				container().set(idStrategy.idToIdx(id), weight);
			}

			@Override
			public long defaultWeightLong() {
				return container().defaultValLong();
			}

			@Override
			WeightsImpl.Mapped.Long copy(IDStrategy idStrat) {
				return new WeightsImpl.Mapped.Long(container().copy(idStrat), idStrat);
			}
		}

		static class Float extends Mapped<java.lang.Float> implements Weights.Float {
			Float(DataContainer.Float container, IDStrategy idStrategy) {
				super(container, idStrategy);
			}

			@Override
			DataContainer.Float container() {
				return (DataContainer.Float) super.container();
			}

			@Override
			public float getFloat(int id) {
				return container().getFloat(idStrategy.idToIdx(id));
			}

			@Override
			public void set(int id, float weight) {
				container().set(idStrategy.idToIdx(id), weight);
			}

			@Override
			public float defaultWeightFloat() {
				return container().defaultValFloat();
			}

			@Override
			WeightsImpl.Mapped.Float copy(IDStrategy idStrat) {
				return new WeightsImpl.Mapped.Float(container().copy(idStrat), idStrat);
			}
		}

		static class Double extends Mapped<java.lang.Double> implements Weights.Double {
			Double(DataContainer.Double container, IDStrategy idStrategy) {
				super(container, idStrategy);
			}

			@Override
			DataContainer.Double container() {
				return (DataContainer.Double) super.container();
			}

			@Override
			public double getDouble(int id) {
				return container().getDouble(idStrategy.idToIdx(id));
			}

			@Override
			public void set(int id, double weight) {
				container().set(idStrategy.idToIdx(id), weight);
			}

			@Override
			public double defaultWeightDouble() {
				return container().defaultValDouble();
			}

			@Override
			WeightsImpl.Mapped.Double copy(IDStrategy idStrat) {
				return new WeightsImpl.Mapped.Double(container().copy(idStrat), idStrat);
			}
		}

		static class Bool extends Mapped<Boolean> implements Weights.Bool {
			Bool(DataContainer.Bool container, IDStrategy idStrategy) {
				super(container, idStrategy);
			}

			@Override
			DataContainer.Bool container() {
				return (DataContainer.Bool) super.container();
			}

			@Override
			public boolean getBool(int id) {
				return container().getBool(idStrategy.idToIdx(id));
			}

			@Override
			public void set(int id, boolean weight) {
				container().set(idStrategy.idToIdx(id), weight);
			}

			@Override
			public boolean defaultWeightBool() {
				return container().defaultValBool();
			}

			@Override
			WeightsImpl.Mapped.Bool copy(IDStrategy idStrat) {
				return new WeightsImpl.Mapped.Bool(container().copy(idStrat), idStrat);
			}
		}

		static class Char extends Mapped<Character> implements Weights.Char {
			Char(DataContainer.Char container, IDStrategy idStrategy) {
				super(container, idStrategy);
			}

			@Override
			DataContainer.Char container() {
				return (DataContainer.Char) super.container();
			}

			@Override
			public char getChar(int id) {
				return container().getChar(idStrategy.idToIdx(id));
			}

			@Override
			public void set(int id, char weight) {
				container().set(idStrategy.idToIdx(id), weight);
			}

			@Override
			public char defaultWeightChar() {
				return container().defaultValChar();
			}

			@Override
			WeightsImpl.Mapped.Char copy(IDStrategy idStrat) {
				return new WeightsImpl.Mapped.Char(container().copy(idStrat), idStrat);
			}
		}
	}

	static class Manager extends DataContainer.Manager {

		private final Map<Object, WeightsImpl<?>> weights = new Object2ObjectArrayMap<>();

		Manager(int initialCapacity) {
			super(initialCapacity);
		}

		Manager(Manager orig, IDStrategy idStrat) {
			this(idStrat.size());
			for (var entry : orig.weights.entrySet())
				addWeights(entry.getKey(), entry.getValue().copy(idStrat));
		}

		@Override
		Manager copy(IDStrategy idStrat) {
			return new Manager(this, idStrat);
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
			super.addContainer(key, ((WeightsImpl<?>) weights).container);
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

}
