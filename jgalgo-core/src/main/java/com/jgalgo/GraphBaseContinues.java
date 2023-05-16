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
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;

abstract class GraphBaseContinues extends GraphBase {

	private final Map<Object, Weights<?>> eWeights = new Object2ObjectArrayMap<>();
	private final Map<Object, Weights<?>> vWeights = new Object2ObjectArrayMap<>();

	GraphBaseContinues(int n, GraphCapabilities capabilities) {
		super(new IDStrategy.Continues(n), new IDStrategy.Continues(0), capabilities);
	}

	@Override
	public int addVertex() {
		int u = verticesIDStrategy.newIdx();
		assert u >= 0;
		for (Weights<?> weight : vWeights.values())
			((WeightsImpl<?>) weight).container.add(u);
		return u;
	}

	@Override
	public void removeVertex(int v) {
		removeEdgesOf(v);
		v = vertexSwapBeforeRemove(v);
		verticesIDStrategy.removeIdx(v);
		for (Weights<?> weight : vWeights.values())
			((WeightsImpl<?>) weight).container.remove(v);
	}

	/**
	 * Remove multiple vertices.
	 * <p>
	 * After removing a vertex, the vertices ID strategy may rename other vertices identifiers to maintain its
	 * invariants, see {@link #getVerticesIDStrategy()}. Theses renames can be subscribed using
	 * {@link IDStrategy#addIDSwapListener}.
	 * <p>
	 * This function may be useful in case a user want to remove a collection of vertices, and does not want to update
	 * IDs within the collection due to IDs renames.
	 *
	 * @param  vs                        a collection of vertices to remove
	 * @throws IndexOutOfBoundsException if one of the edges is not a valid edge identifier
	 * @throws IllegalArgumentException  if the vertices collection to remove contains duplications
	 */
	// @Override public
	void removeVertices(IntCollection vs) {
		int[] vsArr = vs.toIntArray();
		IntArrays.parallelQuickSort(vsArr);
		for (int i = 0; i < vsArr.length - 1; i++) {
			int v1 = vsArr[i], v2 = vsArr[i + 1];
			if (v1 == v2)
				throw new IllegalArgumentException("vertex identifier duplication: " + v1);
		}
		/*
		 * When we remove a vertex, a rename may be performed, swapping the removed vertex id with numberOfVertices-1.
		 * By removing them in decreasing order, the smaller IDs remain valid.
		 */
		for (int i = vsArr.length - 1; i >= 0; i--)
			removeVertex(vsArr[i]);
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
		for (Weights<?> weight : vWeights.values())
			((WeightsImpl<?>) weight).container.swap(v1, v2);
	}

	@Override
	public int addEdge(int u, int v) {
		checkVertexIdx(u);
		checkVertexIdx(v);
		int e = edgesIDStrategy.newIdx();
		assert e >= 0;
		for (Weights<?> weight : eWeights.values())
			((WeightsImpl<?>) weight).container.add(e);
		return e;
	}

	@Override
	public void removeEdge(int e) {
		e = edgeSwapBeforeRemove(e);
		edgesIDStrategy.removeIdx(e);
		for (Weights<?> weight : eWeights.values())
			((WeightsImpl<?>) weight).container.remove(e);
	}

	/**
	 * Remove multiple edges.
	 * <p>
	 * After removing an edge, the edges ID strategy may rename other edges identifiers to maintain its invariants, see
	 * {@link #getEdgesIDStrategy()}. Theses renames can be subscribed using {@link IDStrategy#addIDSwapListener}.
	 * <p>
	 * This function may be useful in case a user want to remove a collection of edges, and does not want to update IDs
	 * within the collection due to IDs renames.
	 *
	 * @param  edges                     a collection of edges to remove
	 * @throws IndexOutOfBoundsException if one of the edges is not a valid edge identifier
	 * @throws IllegalArgumentException  if the edges collection to remove contains duplications
	 */
	// @Override public
	void removeEdges(IntCollection edges) {
		int[] edgesArr = edges.toIntArray();
		IntArrays.parallelQuickSort(edgesArr);
		for (int i = 0; i < edgesArr.length - 1; i++) {
			int e1 = edgesArr[i], e2 = edgesArr[i + 1];
			if (e1 == e2)
				throw new IllegalArgumentException("edge identifier duplication: " + e1);
		}
		/*
		 * When we remove an edge, a rename may be performed, swapping the removed edge id with numberOfEdges-1. By
		 * removing them in decreasing order, the smaller IDs remain valid.
		 */
		for (int i = edgesArr.length - 1; i >= 0; i--)
			removeEdge(edgesArr[i]);
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
		for (Weights<?> weight : eWeights.values())
			((WeightsImpl<?>) weight).container.swap(e1, e2);
	}

	@Override
	public void clear() {
		super.clear();
		for (Weights<?> weight : vWeights.values())
			((WeightsImpl<?>) weight).container.clear();
	}

	@Override
	public void clearEdges() {
		super.clearEdges();
		for (Weights<?> weight : eWeights.values())
			((WeightsImpl<?>) weight).container.clear();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V, WeightsT extends Weights<V>> WeightsT getVerticesWeights(Object key) {
		return (WeightsT) vWeights.get(key);
	}

	@Override
	public Set<Object> getVerticesWeightKeys() {
		return Collections.unmodifiableSet(vWeights.keySet());
	}

	@Override
	public void removeVerticesWeights(Object key) {
		vWeights.remove(key);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E, WeightsT extends Weights<E>> WeightsT getEdgesWeights(Object key) {
		return (WeightsT) eWeights.get(key);
	}

	@Override
	public IDStrategy.Continues getEdgesIDStrategy() {
		return (IDStrategy.Continues) super.getEdgesIDStrategy();
	}

	@Override
	void addVerticesWeightsContainer(Object key, Weights<?> weights) {
		Weights<?> oldWeights = vWeights.put(key, weights);
		if (oldWeights != null)
			throw new IllegalArgumentException("Two weights types with the same key: " + key);
	}

	@Override
	void addEdgesWeightsContainer(Object key, Weights<?> weights) {
		Weights<?> oldWeights = eWeights.put(key, weights);
		if (oldWeights != null)
			throw new IllegalArgumentException("Two weights types with the same key: " + key);
	}

	@Override
	public Set<Object> getEdgesWeightsKeys() {
		return Collections.unmodifiableSet(eWeights.keySet());
	}

	@Override
	public void removeEdgesWeights(Object key) {
		eWeights.remove(key);
	}

	void addInternalVerticesDataContainer(DataContainer<?> container) {
		int n = vertices().size();
		container.ensureCapacity(n);
		for (int u = 0; u < n; u++)
			container.add(u);
	}

	void addInternalEdgesDataContainer(DataContainer<?> container) {
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
