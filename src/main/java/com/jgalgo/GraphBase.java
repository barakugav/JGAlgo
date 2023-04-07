package com.jgalgo;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;

abstract class GraphBase implements Graph {

	final IDStrategy verticesIDStrategy;
	final IDStrategy edgesIDStrategy;
	private final GraphCapabilities capabilities;

	GraphBase(IDStrategy verticesIDStrategy, IDStrategy edgesIDStrategy, GraphCapabilities capabilities) {
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
	public IDStrategy getVerticesIDStrategy() {
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
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (!(other instanceof Graph))
			return false;
		Graph o = (Graph) other;

		if ((this instanceof DiGraph) != (o instanceof DiGraph))
			return false;
		if (vertices().size() != o.vertices().size() || edges().size() != o.edges().size())
			return false;

		boolean isDirected = (this instanceof DiGraph);
		if (isDirected) {
			for (IntIterator it = edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				int u1 = edgeSource(e), v1 = edgeTarget(e);
				int u2 = o.edgeSource(e), v2 = o.edgeTarget(e);
				if (!(u1 == u2 && v1 == v2))
					return false;
			}
		} else {
			for (IntIterator it = edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				int u1 = edgeSource(e), v1 = edgeTarget(e);
				int u2 = o.edgeSource(e), v2 = o.edgeTarget(e);
				if (!((u1 == u2 && v1 == v2) || (u1 == v2 && v1 == u2)))
					return false;
			}
		}

		Set<Object> vwKeys = getVerticesWeightKeys();
		if (!vwKeys.equals(o.getVerticesWeightKeys()))
			return false;
		for (Object weightKey : vwKeys)
			if (!verticesWeight(weightKey).equals(o.verticesWeight(weightKey)))
				return false;

		Set<Object> ewKeys = getEdgesWeightsKeys();
		if (!ewKeys.equals(o.getEdgesWeightsKeys()))
			return false;
		for (Object weightKey : ewKeys)
			if (!edgesWeight(weightKey).equals(o.edgesWeight(weightKey)))
				return false;

		return true;
	}

	@Override
	public int hashCode() {
		int vWeightsHash = 1 + vertices().size();
		for (Weights<?> vWeight : getVerticesWeights())
			vWeightsHash = vWeightsHash * 31 + vWeight.hashCode();

		int edgesHash = 1;
		if (this instanceof DiGraph) {
			for (IntIterator it = edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				edgesHash = edgesHash * 31 + edgeSource(e);
				edgesHash = edgesHash * 31 + edgeTarget(e);
			}
		} else {
			for (IntIterator it = edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				int u = edgeSource(e), v = edgeTarget(e);
				if (u > v) {
					int temp = u;
					u = v;
					v = temp;
				}
				edgesHash = edgesHash * 31 + u;
				edgesHash = edgesHash * 31 + v;
			}
		}

		int eWeightsHash = 1;
		for (Weights<?> eWeight : getEdgesWeights())
			eWeightsHash = eWeightsHash * 31 + eWeight.hashCode();

		int h = 1;
		h = h * 31 + vWeightsHash;
		h = h * 31 + edgesHash;
		h = h * 31 + eWeightsHash;
		return h;
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
			s.append("<v" + u + ">->[");

			boolean firstEdge = true;
			for (EdgeIter eit = edges(u); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.v();
				if (firstEdge)
					firstEdge = false;
				else
					s.append(", ");
				s.append("" + e + "(" + u + ", " + v + ")");
				if (!weights.isEmpty()) {
					s.append('[');
					boolean firstData = true;
					for (Weights<?> weight : weights) {
						if (firstData) {
							firstData = false;
						} else {
							s.append(", ");
						}
						s.append(String.valueOf(weight.get(e)));
					}
					s.append(']');
				}
			}
			s.append("]");
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
