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

import java.util.Set;

abstract class GraphBaseContinues extends GraphBase {

	private final DataContainer.Manager verticesInternalData;
	private final DataContainer.Manager edgesInternalData;
	private final WeightsImpl.Manager verticesUserData;
	private final WeightsImpl.Manager edgesUserData;

	GraphBaseContinues(int n) {
		super(new IDStrategy.Continues(n), new IDStrategy.Continues(0));
		verticesInternalData = new DataContainer.Manager(n);
		edgesInternalData = new DataContainer.Manager(0);
		verticesUserData = new WeightsImpl.Manager(n);
		edgesUserData = new WeightsImpl.Manager(0);
	}

	GraphBaseContinues(GraphBaseContinues g) {
		super(g.verticesIDStrategy.copy(), g.edgesIDStrategy.copy());

		/* internal data containers should be copied manually */
		// verticesInternalData = g.verticesInternalData.copy(verticesIDStrategy);
		// edgesInternalData = g.edgesInternalData.copy(edgesIDStrategy);
		verticesInternalData = new DataContainer.Manager(verticesIDStrategy.size());
		edgesInternalData = new DataContainer.Manager(edgesIDStrategy.size());

		verticesUserData = g.verticesUserData.copy(verticesIDStrategy);
		edgesUserData = g.edgesUserData.copy(edgesIDStrategy);
	}

	@Override
	public int addVertex() {
		int u = verticesIDStrategy.newIdx();
		assert u >= 0;
		verticesInternalData.ensureCapacity(u + 1);
		verticesUserData.ensureCapacity(u + 1);
		return u;
	}

	@Override
	public void removeVertex(int vertex) {
		removeEdgesOf(vertex);
		vertex = vertexSwapBeforeRemove(vertex);
		// internal weights are handled manually
		// verticesWeightsInternal.clearElement(vertex);
		verticesUserData.clearElement(vertex);
		verticesIDStrategy.removeIdx(vertex);
	}

	// /**
	// * Remove multiple vertices.
	// * <p>
	// * After removing a vertex, the vertices ID strategy may rename other vertices identifiers to maintain its
	// * invariants, see {@link #getVerticesIDStrategy()}. Theses renames can be subscribed using
	// * {@link IDStrategy#addIDSwapListener}.
	// * <p>
	// * This function may be useful in case a user want to remove a collection of vertices, and does not want to update
	// * IDs within the collection due to IDs renames.
	// *
	// * @param vs a collection of vertices to remove
	// * @throws IndexOutOfBoundsException if one of the edges is not a valid edge identifier
	// * @throws IllegalArgumentException if the vertices collection to remove contains duplications
	// */
	// @Override public
	// void removeVertices(IntCollection vs) {
	// int[] vsArr = vs.toIntArray();
	// IntArrays.parallelQuickSort(vsArr);
	// for (int i = 0; i < vsArr.length - 1; i++) {
	// int v1 = vsArr[i], v2 = vsArr[i + 1];
	// if (v1 == v2)
	// throw new IllegalArgumentException("vertex identifier duplication: " + v1);
	// }
	// /*
	// * When we remove a vertex, a rename may be performed, swapping the removed vertex id with numberOfVertices-1.
	// * By removing them in decreasing order, the smaller IDs remain valid.
	// */
	// for (int i = vsArr.length - 1; i >= 0; i--)
	// removeVertex(vsArr[i]);
	// }

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
		// internal weights are handled manually
		// verticesWeightsInternal.swapElements(v1, v2);
		verticesUserData.swapElements(v1, v2);
	}

	@Override
	public int addEdge(int source, int target) {
		checkVertex(source);
		checkVertex(target);
		int e = edgesIDStrategy.newIdx();
		assert e >= 0;
		edgesInternalData.ensureCapacity(e + 1);
		edgesUserData.ensureCapacity(e + 1);
		return e;
	}

	@Override
	public void removeEdge(int edge) {
		edge = edgeSwapBeforeRemove(edge);
		// internal weights are handled manually
		// edgesWeightsInternal.clearElement(edge);
		edgesUserData.clearElement(edge);
		edgesIDStrategy.removeIdx(edge);
	}

	// /**
	// * Remove multiple edges.
	// * <p>
	// * After removing an edge, the edges ID strategy may rename other edges identifiers to maintain its invariants,
	// see
	// * {@link #getEdgesIDStrategy()}. Theses renames can be subscribed using {@link IDStrategy#addIDSwapListener}.
	// * <p>
	// * This function may be useful in case a user want to remove a collection of edges, and does not want to update
	// IDs
	// * within the collection due to IDs renames.
	// *
	// * @param edges a collection of edges to remove
	// * @throws IndexOutOfBoundsException if one of the edges is not a valid edge identifier
	// * @throws IllegalArgumentException if the edges collection to remove contains duplications
	// */
	// @Override public
	// void removeEdges(IntCollection edges) {
	// int[] edgesArr = edges.toIntArray();
	// IntArrays.parallelQuickSort(edgesArr);
	// for (int i = 0; i < edgesArr.length - 1; i++) {
	// int e1 = edgesArr[i], e2 = edgesArr[i + 1];
	// if (e1 == e2)
	// throw new IllegalArgumentException("edge identifier duplication: " + e1);
	// }
	// /*
	// * When we remove an edge, a rename may be performed, swapping the removed edge id with numberOfEdges-1. By
	// * removing them in decreasing order, the smaller IDs remain valid.
	// */
	// for (int i = edgesArr.length - 1; i >= 0; i--)
	// removeEdge(edgesArr[i]);
	// }

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
		// internal weights are handled manually
		// edgesWeightsInternal.swapElements(e1, e2);
		edgesUserData.swapElements(e1, e2);
	}

	@Override
	public void clear() {
		super.clear();
		// internal weights are handled manually
		// verticesWeightsInternal.clearContainers();
		verticesUserData.clearContainers();
	}

	@Override
	public void clearEdges() {
		super.clearEdges();
		// internal weights are handled manually
		// edgesWeightsInternal.clearContainers();
		edgesUserData.clearContainers();
	}

	@Override
	public IDStrategy.Continues getEdgesIDStrategy() {
		return (IDStrategy.Continues) super.getEdgesIDStrategy();
	}

	@Override
	public <V, WeightsT extends Weights<V>> WeightsT getVerticesWeights(Object key) {
		return verticesUserData.getWeights(key);
	}

	@Override
	public Set<Object> getVerticesWeightKeys() {
		return verticesUserData.weightsKeys();
	}

	@Override
	public void removeVerticesWeights(Object key) {
		verticesUserData.removeWeights(key);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E, WeightsT extends Weights<E>> WeightsT getEdgesWeights(Object key) {
		return (WeightsT) edgesUserData.getWeights(key);
	}

	@Override
	public Set<Object> getEdgesWeightsKeys() {
		return edgesUserData.weightsKeys();
	}

	@Override
	public void removeEdgesWeights(Object key) {
		edgesUserData.removeWeights(key);
	}

	void addInternalVerticesDataContainer(Object key, DataContainer<?> container) {
		verticesInternalData.addContainer(key, container);
	}

	void addInternalEdgesDataContainer(Object key, DataContainer<?> container) {
		edgesInternalData.addContainer(key, container);
	}

	@Override
	void addVerticesWeightsContainer(Object key, Weights<?> weights) {
		verticesUserData.addWeights(key, weights);
	}

	@Override
	void addEdgesWeightsContainer(Object key, Weights<?> weights) {
		edgesUserData.addWeights(key, weights);
	}

	void checkVertex(int vertex) {
		if (!vertices().contains(vertex))
			throw new IndexOutOfBoundsException(vertex);
	}

	void checkEdge(int edge) {
		if (!edges().contains(edge))
			throw new IndexOutOfBoundsException(edge);
	}

}
