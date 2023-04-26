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
		WeightsT weights = WeightsImpl.newInstance(getVerticesIDStrategy(), type, defVal);
		addVerticesWeightsContainer(key, weights);
		return weights;
	}

	@Override
	public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type) {
		return addEdgesWeights(key, type, null);
	}

	@Override
	public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type, E defVal) {
		WeightsT weights = WeightsImpl.newInstance(getEdgesIDStrategy(), type, defVal);
		addEdgesWeightsContainer(key, weights);
		return weights;
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
