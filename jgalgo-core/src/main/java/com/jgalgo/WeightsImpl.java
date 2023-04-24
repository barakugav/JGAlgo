package com.jgalgo;

abstract class WeightsImpl<E> implements Weights<E> {

	final DataContainer<E> container;

	private WeightsImpl(DataContainer<E> container) {
		this.container = container;
	}

	DataContainer<E> container() {
		return container;
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
		}
	}

}
