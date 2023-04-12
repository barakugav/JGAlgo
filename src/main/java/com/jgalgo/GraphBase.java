package com.jgalgo;

import java.util.Collection;
import java.util.NoSuchElementException;
import java.util.Objects;

import it.unimi.dsi.fastutil.ints.IntSet;

abstract class GraphBase implements Graph {

	final IDStrategy.Continues verticesIDStrategy;
	final IDStrategy edgesIDStrategy;
	private final GraphCapabilities capabilities;

	GraphBase(IDStrategy.Continues verticesIDStrategy, IDStrategy edgesIDStrategy, GraphCapabilities capabilities) {
		this.verticesIDStrategy = Objects.requireNonNull(verticesIDStrategy);
		this.edgesIDStrategy = Objects.requireNonNull(edgesIDStrategy);
		this.capabilities = Objects.requireNonNull(capabilities);
	}

	@Override
	public final IntSet vertices() {
		return verticesIDStrategy.idSet();
	}

	@Override
	public final IntSet edges() {
		return edgesIDStrategy.idSet();
	}

	@Override
	public EdgeIter getEdges(int u, int v) {
		return new EdgeIter() {
			EdgeIter it = edgesOut(u);
			int e = -1;

			@Override
			public boolean hasNext() {
				if (e != -1)
					return true;
				while (it.hasNext()) {
					int eNext = it.nextInt();
					if (it.v() == v) {
						e = eNext;
						return true;
					}
				}
				return false;
			}

			@Override
			public int nextInt() {
				if (!hasNext())
					throw new NoSuchElementException();
				int ret = e;
				e = -1;
				return ret;
			}

			@Override
			public int u() {
				return u;
			}

			@Override
			public int v() {
				return v;
			}
		};
	}

	@Override
	public void clear() {
		clearEdges();
		verticesIDStrategy.clear();
	}

	@Override
	public void clearEdges() {
		edgesIDStrategy.clear();
	}

	@Override
	public Weights.Factory addVerticesWeight(Object key) {
		return new WeightsFactoryVertices(key);
	}

	@Override
	public Weights.Factory addEdgesWeight(Object key) {
		return new WeightsFactoryEdges(key);
	}

	abstract <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, WeightsT weights);

	abstract <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, WeightsT weights);

	@Override
	public IDStrategy.Continues getVerticesIDStrategy() {
		return verticesIDStrategy;
	}

	@Override
	public IDStrategy getEdgesIDStrategy() {
		return edgesIDStrategy;
	}

	@Override
	public GraphCapabilities getCapabilities() {
		return capabilities;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append('{');
		int n = vertices().size();
		Collection<Weights<?>> weights = getEdgesWeights();

		boolean firstVertex = true;
		for (int u = 0; u < n; u++) {
			if (firstVertex) {
				firstVertex = false;
			} else {
				s.append(", ");
			}
			s.append('v').append(u).append(": [");

			boolean firstEdge = true;
			for (EdgeIter eit = edgesOut(u); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.v();
				if (firstEdge)
					firstEdge = false;
				else
					s.append(", ");
				s.append(e).append('(').append(u).append(", ").append(v).append(')');
				if (!weights.isEmpty()) {
					s.append('[');
					boolean firstData = true;
					for (Weights<?> weight : weights) {
						if (firstData) {
							firstData = false;
						} else {
							s.append(", ");
						}
						s.append(weight.get(e));
					}
					s.append(']');
				}
			}
			s.append(']');
		}
		s.append('}');
		return s.toString();
	}

	private abstract class WeightsFactoryBase implements Weights.Factory {
		final Object key;
		private Object defVal;

		WeightsFactoryBase(Object key) {
			this.key = key;
		}

		@Override
		public Object getDefVal() {
			return defVal;
		}

		@Override
		public Weights.Factory defVal(Object defVal) {
			this.defVal = defVal;
			return this;
		}

		@Override
		public <E> Weights<E> ofObjs() {
			@SuppressWarnings("unchecked")
			E defVal = (E) getDefVal();
			return addWeights(new DataContainer.Obj<>(expectedSize(), defVal));
		}

		@Override
		public Weights.Int ofInts() {
			Integer defVal0 = (Integer) getDefVal();
			int defVal = defVal0 != null ? defVal0.intValue() : 0;
			return addWeights(new DataContainer.Int(expectedSize(), defVal));
		}

		@Override
		public Weights.Long ofLongs() {
			Long defVal0 = (Long) getDefVal();
			long defVal = defVal0 != null ? defVal0.longValue() : 0;
			return addWeights(new DataContainer.Long(expectedSize(), defVal));
		}

		@Override
		public Weights.Double ofDoubles() {
			Double defVal0 = (Double) getDefVal();
			double defVal = defVal0 != null ? defVal0.doubleValue() : 0;
			return addWeights(new DataContainer.Double(expectedSize(), defVal));
		}

		@Override
		public Weights.Bool ofBools() {
			Boolean defVal0 = (Boolean) getDefVal();
			boolean defVal = defVal0 != null ? defVal0.booleanValue() : false;
			return addWeights(new DataContainer.Bool(expectedSize(), defVal));
		}

		abstract int expectedSize();

		abstract <E, WeightsT extends Weights<E>> WeightsT addWeights(DataContainer<E> container);

		void addAllIndices(DataContainer<?> container) {
			int n = expectedSize();
			container.ensureCapacity(n);
			for (int idx = 0; idx < n; idx++)
				container.add(idx);
		}

		@SuppressWarnings("unchecked")
		<E, WeightsT extends Weights<E>> WeightsT wrapContainer(DataContainer<E> container0, IDStrategy idStrat) {
			boolean isContinues = idStrat instanceof IDStrategy.Continues;
			if (container0 instanceof DataContainer.Obj<?>) {
				DataContainer.Obj<E> container = (DataContainer.Obj<E>) container0;
				if (isContinues) {
					return (WeightsT) new WeightsImpl.Direct.Obj<>(container);
				} else {
					return (WeightsT) new WeightsImpl.Mapped.Obj<>(container, idStrat);
				}
			} else if (container0 instanceof DataContainer.Int) {
				DataContainer.Int container = (DataContainer.Int) container0;
				if (isContinues) {
					return (WeightsT) new WeightsImpl.Direct.Int(container);
				} else {
					return (WeightsT) new WeightsImpl.Mapped.Int(container, idStrat);
				}
			} else if (container0 instanceof DataContainer.Long) {
				DataContainer.Long container = (DataContainer.Long) container0;
				if (isContinues) {
					return (WeightsT) new WeightsImpl.Direct.Long(container);
				} else {
					return (WeightsT) new WeightsImpl.Mapped.Long(container, idStrat);
				}
			} else if (container0 instanceof DataContainer.Double) {
				DataContainer.Double container = (DataContainer.Double) container0;
				if (isContinues) {
					return (WeightsT) new WeightsImpl.Direct.Double(container);
				} else {
					return (WeightsT) new WeightsImpl.Mapped.Double(container, idStrat);
				}
			} else if (container0 instanceof DataContainer.Bool) {
				DataContainer.Bool container = (DataContainer.Bool) container0;
				if (isContinues) {
					return (WeightsT) new WeightsImpl.Direct.Bool(container);
				} else {
					return (WeightsT) new WeightsImpl.Mapped.Bool(container, idStrat);
				}
			} else {
				throw new IllegalArgumentException(container0.getClass().toString());
			}
		}
	}

	private class WeightsFactoryVertices extends WeightsFactoryBase {
		WeightsFactoryVertices(Object key) {
			super(key);
		}

		@Override
		int expectedSize() {
			return vertices().size();
		}

		@Override
		<E, WeightsT extends Weights<E>> WeightsT addWeights(DataContainer<E> container) {
			addAllIndices(container);
			WeightsT weights = wrapContainer(container, getVerticesIDStrategy());
			return addVerticesWeights(key, weights);
		}
	}

	private class WeightsFactoryEdges extends WeightsFactoryBase {
		WeightsFactoryEdges(Object key) {
			super(key);
		}

		@Override
		int expectedSize() {
			return edges().size();
		}

		@Override
		<E, WeightsT extends Weights<E>> WeightsT addWeights(DataContainer<E> container) {
			addAllIndices(container);
			WeightsT weights = wrapContainer(container, getEdgesIDStrategy());
			return addEdgesWeights(key, weights);
		}
	}

}
