package com.ugav.jgalgo;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;

abstract class GraphAbstract implements Graph {

	final IDStrategy verticesIDStrategy;
	final IDStrategy edgesIDStrategy;

	GraphAbstract(IDStrategy verticesIDStrategy, IDStrategy edgesIDStrategy) {
		this.verticesIDStrategy = Objects.requireNonNull(verticesIDStrategy);
		this.edgesIDStrategy = Objects.requireNonNull(edgesIDStrategy);
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
	public IDStrategy getVerticesIDStrategy() {
		return verticesIDStrategy;
	}

	@Override
	public IDStrategy getEdgesIDStrategy() {
		return edgesIDStrategy;
	}

	abstract <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, WeightsT weights);

	abstract <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, WeightsT weights);

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
				s.append("(" + u + ", " + v + ")");
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

}
