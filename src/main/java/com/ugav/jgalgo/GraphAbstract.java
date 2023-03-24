package com.ugav.jgalgo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

abstract class GraphAbstract implements Graph {

	private final IDStrategy verticesIDStrategy;
	private final IDStrategy edgesIDStrategy;

	private final List<Weights<?>> eWeightsInternal = new ArrayList<>();
	private final List<Weights<?>> vWeightsInternal = new ArrayList<>();
	private final Map<Object, Weights<?>> eWeights = new Object2ObjectArrayMap<>();
	private final Map<Object, Weights<?>> vWeights = new Object2ObjectArrayMap<>();

	GraphAbstract(IDStrategy verticesIDStrategy, IDStrategy edgesIDStrategy) {
		if (verticesIDStrategy == null)
			verticesIDStrategy = new IDStrategy.Continues();
		if (edgesIDStrategy == null)
			edgesIDStrategy = new IDStrategy.Continues();
		if (verticesIDStrategy == edgesIDStrategy)
			throw new IllegalArgumentException();

		this.verticesIDStrategy = verticesIDStrategy;
		this.edgesIDStrategy = edgesIDStrategy;
	}

	@Override
	public int addVertex() {
		int u = verticesIDStrategy.nextID(vertices().size());
		for (Weights<?> data : vWeightsInternal)
			((WeightsAbstract<?>) data).keyAdd(u);
		for (Weights<?> data : vWeights.values())
			((WeightsAbstract<?>) data).keyAdd(u);
		return u;
	}

	@Override
	public int addEdge(int u, int v) {
		checkVertexIdx(u);
		checkVertexIdx(v);
		int e = edgesIDStrategy.nextID(edges().size());
		for (Weights<?> data : eWeightsInternal)
			((WeightsAbstract<?>) data).keyAdd(e);
		for (Weights<?> data : eWeights.values())
			((WeightsAbstract<?>) data).keyAdd(e);
		return e;
	}

	@Override
	public void removeEdge(int e) {
		e = swapBeforeRemove(e);
		for (Weights<?> data : eWeightsInternal)
			((WeightsAbstract<?>) data).keyRemove(e);
		for (Weights<?> data : eWeights.values())
			((WeightsAbstract<?>) data).keyRemove(e);
	}

	int swapBeforeRemove(int e) {
		checkEdgeIdx(e);
		int en = edgesIDStrategy.swapBeforeRemove(edges().size(), e);
		boolean rename = e != en;
		if (rename) {
			edgeSwap(e, en);
			edgesIDStrategy.afterSwap(e, en);
		}
		return en;
	}

	void edgeSwap(int e1, int e2) {
		for (Weights<?> data : eWeightsInternal)
			((WeightsAbstract<?>) data).keySwap(e1, e2);
		for (Weights<?> data : eWeights.values())
			((WeightsAbstract<?>) data).keySwap(e1, e2);
	}

	@Override
	public void clear() {
		clearEdges();
		for (Weights<?> data : vWeightsInternal)
			((WeightsAbstract<?>) data).clear();
		for (Weights<?> data : vWeights.values())
			((WeightsAbstract<?>) data).clear();
	}

	@Override
	public void clearEdges() {
		for (Weights<?> data : eWeightsInternal)
			((WeightsAbstract<?>) data).clear();
		for (Weights<?> data : eWeights.values())
			data.clear();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V, WeightsT extends Weights<V>> WeightsT verticesWeight(Object key) {
		return (WeightsT) vWeights.get(key);
	}

	<V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, WeightsT weights) {
		Weights<?> oldWeights = vWeights.put(key, weights);
		if (oldWeights != null)
			throw new IllegalArgumentException("Two weights types with the same key: " + key);
		return weights;
	}

	@Override
	public Set<Object> getVerticesWeightKeys() {
		return Collections.unmodifiableSet(vWeights.keySet());
	}

	@Override
	public Collection<Weights<?>> getVerticesWeights() {
		return Collections.unmodifiableCollection(vWeights.values());
	}

	@Override
	public void removeVerticesWeights(Object key) {
		vWeights.remove(key);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E, WeightsT extends Weights<E>> WeightsT edgesWeight(Object key) {
		return (WeightsT) eWeights.get(key);
	}

	<E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, WeightsT weights) {
		Weights<?> oldWeights = eWeights.put(key, weights);
		if (oldWeights != null)
			throw new IllegalArgumentException("Two weights types with the same key: " + key);
		return weights;
	}

	@Override
	public Set<Object> getEdgesWeightsKeys() {
		return Collections.unmodifiableSet(eWeights.keySet());
	}

	@Override
	public Collection<Weights<?>> getEdgesWeights() {
		return Collections.unmodifiableCollection(eWeights.values());
	}

	@Override
	public void removeEdgesWeights(Object key) {
		eWeights.remove(key);
	}

	void addInternalVerticesWeight(Weights<?> weight) {
		addInternalVerticesWeight(weight, true);
	}

	void addInternalVerticesWeight(Weights<?> weight, boolean addKeys) {
		vWeightsInternal.add(weight);
		if (addKeys) {
			for (IntIterator it = vertices().iterator(); it.hasNext();) {
				int v = it.nextInt();
				((WeightsAbstract<?>) weight).keyAdd(v);
			}
		}
	}

	void addInternalEdgesWeight(Weights<?> weight) {
		// TODO boolean addKeys
		eWeightsInternal.add(weight);
		for (IntIterator it = edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			((WeightsAbstract<?>) weight).keyAdd(e);
		}
	}

	@Override
	public IDStrategy getVerticesIDStrategy() {
		return verticesIDStrategy;
	}

	@Override
	public IDStrategy getEdgesIDStrategy() {
		return edgesIDStrategy;
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

	void checkVertexIdx(int u) {
		if (!vertices().contains(u))
			throw new IndexOutOfBoundsException(u);
	}

	void checkEdgeIdx(int e) {
		if (!edges().contains(e))
			throw new IndexOutOfBoundsException(e);
	}

}
