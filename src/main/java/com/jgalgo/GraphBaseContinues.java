package com.jgalgo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

abstract class GraphBaseContinues extends GraphBase {

	private final List<DataContainer<?>> eWeightsInternal = new ArrayList<>();
	private final List<DataContainer<?>> vWeightsInternal = new ArrayList<>();
	private final Map<Object, Weights<?>> eWeights = new Object2ObjectArrayMap<>();
	private final Map<Object, Weights<?>> vWeights = new Object2ObjectArrayMap<>();

	GraphBaseContinues(int n, GraphCapabilities capabilities) {
		super(new IDStrategy.Continues(n), new IDStrategy.Continues(0), capabilities);
	}

	@Override
	public int addVertex() {
		int u = verticesIDStrategy.newIdx();
		assert u >= 0;
		for (DataContainer<?> container : vWeightsInternal)
			container.add(u);
		for (Weights<?> weight : vWeights.values())
			((WeightsImpl<?>) weight).container.add(u);
		return u;
	}

	@Override
	public void removeVertex(int v) {
		removeEdgesAll(v);
		v = vertexSwapBeforeRemove(v);
		verticesIDStrategy.removeIdx(v);
		for (DataContainer<?> container : vWeightsInternal)
			container.remove(v);
		for (Weights<?> weight : vWeights.values())
			((WeightsImpl<?>) weight).container.remove(v);
	}

	int vertexSwapBeforeRemove(int v) {
		int vn = verticesIDStrategy.isSwapNeededBeforeRemove(v);
		if (v != vn) {
			vertexSwap(v, vn);
			v = vn;
		}
		return v;
	}

	void vertexSwap(int v1, int v2) {
		verticesIDStrategy.idxSwap(v1, v2);
		for (DataContainer<?> container : vWeightsInternal)
			container.swap(v1, v2);
		for (Weights<?> weight : vWeights.values())
			((WeightsImpl<?>) weight).container.swap(v1, v2);
	}

	@Override
	public int addEdge(int u, int v) {
		checkVertexIdx(u);
		checkVertexIdx(v);
		int e = edgesIDStrategy.newIdx();
		assert e >= 0;
		for (DataContainer<?> container : eWeightsInternal)
			container.add(e);
		for (Weights<?> weight : eWeights.values())
			((WeightsImpl<?>) weight).container.add(e);
		return e;
	}

	@Override
	public void removeEdge(int e) {
		e = edgeSwapBeforeRemove(e);
		edgesIDStrategy.removeIdx(e);
		for (DataContainer<?> container : eWeightsInternal)
			container.remove(e);
		for (Weights<?> weight : eWeights.values())
			((WeightsImpl<?>) weight).container.remove(e);
	}

	int edgeSwapBeforeRemove(int e) {
		int en = edgesIDStrategy.isSwapNeededBeforeRemove(e);
		if (e != en) {
			edgeSwap(e, en);
			e = en;
		}
		return e;
	}

	void edgeSwap(int e1, int e2) {
		edgesIDStrategy.idxSwap(e1, e2);
		for (DataContainer<?> container : eWeightsInternal)
			container.swap(e1, e2);
		for (Weights<?> weight : eWeights.values())
			((WeightsImpl<?>) weight).container.swap(e1, e2);
	}

	@Override
	public void clear() {
		super.clear();
		for (DataContainer<?> container : vWeightsInternal)
			container.clear();
		for (Weights<?> weight : vWeights.values())
			((WeightsImpl<?>) weight).clear();
	}

	@Override
	public void clearEdges() {
		super.clearEdges();
		for (DataContainer<?> container : eWeightsInternal)
			container.clear();
		for (Weights<?> weight : eWeights.values())
			weight.clear();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V, WeightsT extends Weights<V>> WeightsT verticesWeight(Object key) {
		return (WeightsT) vWeights.get(key);
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

	@Override
	public IDStrategy.Continues getEdgesIDStrategy() {
		return (IDStrategy.Continues) super.getEdgesIDStrategy();
	}

	@Override
	<V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, WeightsT weights) {
		Weights<?> oldWeights = vWeights.put(key, weights);
		if (oldWeights != null)
			throw new IllegalArgumentException("Two weights types with the same key: " + key);
		return weights;
	}

	@Override
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

	void addInternalVerticesDataContainer(DataContainer<?> container) {
		vWeightsInternal.add(container);
		int n = vertices().size();
		container.ensureCapacity(n);
		for (int u = 0; u < n; u++)
			container.add(u);
	}

	void addInternalEdgesDataContainer(DataContainer<?> container) {
		eWeightsInternal.add(container);
		int m = edges().size();
		container.ensureCapacity(m);
		for (int e = 0; e < m; e++)
			container.add(e);
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
