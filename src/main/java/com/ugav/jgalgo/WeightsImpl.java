package com.ugav.jgalgo;

abstract class WeightsImpl<E> implements Weights<E> {

	final DataContainer<E> container;

	private WeightsImpl(DataContainer<E> container) {
		this.container = container;
	}

	DataContainer<E> container() {
		return container;
	}

	@Override
	public void clear() {
		container.clear();
	}

	static abstract class Direct<E> extends WeightsImpl<E> {
		private Direct(DataContainer<E> data) {
			super(data);
		}

		static class Obj<E> extends Direct<E> {
			Obj(int expectedSize, E defVal) {
				super(new DataContainer.Obj<>(expectedSize, defVal));
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
			public E defaultVal() {
				return container().defaultVal();
			}
		}

		static class Int extends Direct<Integer> implements Weights.Int {
			Int(int expectedSize, int defVal) {
				super(new DataContainer.Int(expectedSize, defVal));
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
			public int defaultValInt() {
				return container().defaultValInt();
			}
		}

		static class Long extends Direct<java.lang.Long> implements Weights.Long {
			Long(int expectedSize, long defVal) {
				super(new DataContainer.Long(expectedSize, defVal));
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
			public long defaultValLong() {
				return container().defaultValLong();
			}
		}

		static class Double extends Direct<java.lang.Double> implements Weights.Double {
			Double(int expectedSize, double defVal) {
				super(new DataContainer.Double(expectedSize, defVal));
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
			public double defaultValDouble() {
				return container().defaultValDouble();
			}
		}

		static class Bool extends Direct<Boolean> implements Weights.Bool {
			Bool(int expectedSize, boolean defVal) {
				super(new DataContainer.Bool(expectedSize, defVal));
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
			public boolean defaultValBool() {
				return container().defaultValBool();
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
			Obj(int expectedSize, E defVal, IDStrategy idStrategy) {
				super(new DataContainer.Obj<>(expectedSize, defVal), idStrategy);
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
			public E defaultVal() {
				return container().defaultVal();
			}

		}

		static class Int extends Mapped<Integer> implements Weights.Int {
			Int(int expectedSize, int defVal, IDStrategy idStrategy) {
				super(new DataContainer.Int(expectedSize, defVal), idStrategy);
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
			public int defaultValInt() {
				return container().defaultValInt();
			}
		}

		static class Long extends Mapped<java.lang.Long> implements Weights.Long {
			Long(int expectedSize, long defVal, IDStrategy idStrategy) {
				super(new DataContainer.Long(expectedSize, defVal), idStrategy);
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
			public long defaultValLong() {
				return container().defaultValLong();
			}
		}

		static class Double extends Mapped<java.lang.Double> implements Weights.Double {
			Double(int expectedSize, double defVal, IDStrategy idStrategy) {
				super(new DataContainer.Double(expectedSize, defVal), idStrategy);
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
			public double defaultValDouble() {
				return container().defaultValDouble();
			}
		}

		static class Bool extends Mapped<Boolean> implements Weights.Bool {
			Bool(int expectedSize, boolean defVal, IDStrategy idStrategy) {
				super(new DataContainer.Bool(expectedSize, defVal), idStrategy);
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
			public boolean defaultValBool() {
				return container().defaultValBool();
			}
		}
	}

}
