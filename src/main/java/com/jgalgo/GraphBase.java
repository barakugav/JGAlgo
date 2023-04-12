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
	public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type) {
		return addVerticesWeights(key, type, null);
	}

	@Override
	public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type, V defVal) {
		DataContainer<V> container = createContainer(type, defVal, vertices().size());
		WeightsT weights = wrapContainer(container, getVerticesIDStrategy());
		addVerticesWeightsContainer(key, weights);
		return weights;
	}

	@Override
	public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type) {
		return addEdgesWeights(key, type, null);
	}

	@Override
	public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type, E defVal) {
		DataContainer<E> container = createContainer(type, defVal, edges().size());
		WeightsT weights = wrapContainer(container, getEdgesIDStrategy());
		addEdgesWeightsContainer(key, weights);
		return weights;
	}

	private static <D> DataContainer<D> createContainer(Class<? super D> type, D defVal, int size) {
		@SuppressWarnings("rawtypes")
		DataContainer container;
		if (type == int.class) {
			int defVal0 = defVal != null ? ((Integer) defVal).intValue() : 0;
			container = new DataContainer.Int(size, defVal0);
		} else if (type == long.class) {
			long defVal0 = defVal != null ? ((Long) defVal).longValue() : 0;
			container = new DataContainer.Long(size, defVal0);
		} else if (type == double.class) {
			double defVal0 = defVal != null ? ((Double) defVal).doubleValue() : 0;
			container = new DataContainer.Double(size, defVal0);
		} else if (type == boolean.class) {
			boolean defVal0 = defVal != null ? ((Boolean) defVal).booleanValue() : false;
			container = new DataContainer.Bool(size, defVal0);
		} else {
			container = new DataContainer.Obj<>(size, defVal);
		}

		// TODO should be done in constructor
		container.ensureCapacity(size);
		for (int idx = 0; idx < size; idx++)
			container.add(idx);

		@SuppressWarnings("unchecked")
		DataContainer<D> container0 = container;
		return container0;
	}

	@SuppressWarnings("unchecked")
	private static <D, WeightsT extends Weights<D>> WeightsT wrapContainer(DataContainer<D> container0,
			IDStrategy idStrat) {
		boolean isContinues = idStrat instanceof IDStrategy.Continues;
		if (container0 instanceof DataContainer.Obj<?>) {
			DataContainer.Obj<D> container = (DataContainer.Obj<D>) container0;
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

	abstract void addVerticesWeightsContainer(Object key, Weights<?> weights);

	abstract void addEdgesWeightsContainer(Object key, Weights<?> weights);

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

}
