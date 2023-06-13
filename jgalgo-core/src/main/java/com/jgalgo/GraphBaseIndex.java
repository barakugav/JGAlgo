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
import it.unimi.dsi.fastutil.ints.IntSet;

abstract class GraphBaseIndex extends GraphBase implements IndexGraphImpl {

	final IdStrategy.Index verticesIdStrat;
	final IdStrategy.Index edgesIdStrat;
	private final DataContainer.Manager verticesInternalContainers;
	private final DataContainer.Manager edgesInternalContainers;
	private final WeightsImpl.Index.Manager verticesUserWeights;
	private final WeightsImpl.Index.Manager edgesUserWeights;

	GraphBaseIndex(int expectedVerticesNum, int expectedEdgesNum) {
		verticesIdStrat = new IdStrategy.Index(0);
		edgesIdStrat = new IdStrategy.Index(0);
		verticesInternalContainers = new DataContainer.Manager(expectedVerticesNum);
		edgesInternalContainers = new DataContainer.Manager(expectedEdgesNum);
		verticesUserWeights = new WeightsImpl.Index.Manager(expectedVerticesNum);
		edgesUserWeights = new WeightsImpl.Index.Manager(expectedEdgesNum);
	}

	GraphBaseIndex(GraphBaseIndex g) {
		verticesIdStrat = g.verticesIdStrat.copy();
		edgesIdStrat = g.edgesIdStrat.copy();

		/* internal data containers should be copied manually */
		// verticesInternalContainers = g.verticesInternalContainers.copy(verticesIdStrategy);
		// edgesInternalContainers = g.edgesInternalContainers.copy(edgesIdStrategy);
		verticesInternalContainers = new DataContainer.Manager(verticesIdStrat.size());
		edgesInternalContainers = new DataContainer.Manager(edgesIdStrat.size());

		verticesUserWeights = new WeightsImpl.Index.Manager(g.verticesUserWeights, verticesIdStrat);
		edgesUserWeights = new WeightsImpl.Index.Manager(g.edgesUserWeights, edgesIdStrat);
	}

	@Override
	public final IntSet vertices() {
		return verticesIdStrat.idSet();
	}

	@Override
	public final IntSet edges() {
		return edgesIdStrat.idSet();
	}

	@Override
	public int addVertex() {
		int u = verticesIdStrat.newIdx();
		assert u >= 0;
		verticesInternalContainers.ensureCapacity(u + 1);
		verticesUserWeights.ensureCapacity(u + 1);
		return u;
	}

	@Override
	public final void removeVertex(int vertex) {
		removeEdgesOf(vertex);
		vertex = vertexSwapBeforeRemove(vertex);
		removeVertexImpl(vertex);
	}

	void removeVertexImpl(int vertex) {
		// internal weights are handled manually
		// verticesInternalContainers.clearElement(vertex);
		verticesUserWeights.clearElement(vertex);
		verticesIdStrat.removeIdx(vertex);
	}

	private int vertexSwapBeforeRemove(int v) {
		int vn = verticesIdStrat.isSwapNeededBeforeRemove(v);
		if (v != vn) {
			vertexSwap(v, vn);
			v = vn;
		}
		return v;
	}

	void vertexSwap(int v1, int v2) {
		verticesIdStrat.idxSwap(v1, v2);
		// internal weights are handled manually
		// verticesInternalContainers.swapElements(v1, v2);
		verticesUserWeights.swapElements(v1, v2);
	}

	@Override
	public int addEdge(int source, int target) {
		checkVertex(source);
		checkVertex(target);
		int e = edgesIdStrat.newIdx();
		assert e >= 0;
		edgesInternalContainers.ensureCapacity(e + 1);
		edgesUserWeights.ensureCapacity(e + 1);
		return e;
	}

	@Override
	public final void removeEdge(int edge) {
		edge = edgeSwapBeforeRemove(edge);
		removeEdgeImpl(edge);
	}

	void removeEdgeImpl(int edge) {
		// internal weights are handled manually
		// edgesInternalContainers.clearElement(edge);
		edgesUserWeights.clearElement(edge);
		edgesIdStrat.removeIdx(edge);
	}

	int edgeSwapBeforeRemove(int e) {
		int en = edgesIdStrat.isSwapNeededBeforeRemove(e);
		if (e != en) {
			edgeSwap(e, en);
			e = en;
		}
		return e;
	}

	void edgeSwap(int e1, int e2) {
		edgesIdStrat.idxSwap(e1, e2);
		// internal weights are handled manually
		// edgesInternalContainers.swapElements(e1, e2);
		edgesUserWeights.swapElements(e1, e2);
	}

	@Override
	public void clear() {
		clearEdges();
		verticesIdStrat.clear();
		// internal weights are handled manually
		// verticesInternalContainers.clearContainers();
		verticesUserWeights.clearContainers();
	}

	@Override
	public void clearEdges() {
		edgesIdStrat.clear();
		// internal weights are handled manually
		// edgesInternalContainers.clearContainers();
		edgesUserWeights.clearContainers();
	}

	@Override
	public IdStrategy getVerticesIdStrategy() {
		return verticesIdStrat;
	}

	@Override
	public IdStrategy getEdgesIdStrategy() {
		return edgesIdStrat;
	}

	@Override
	public <V, WeightsT extends Weights<V>> WeightsT getVerticesWeights(Object key) {
		return verticesUserWeights.getWeights(key);
	}

	@Override
	public Set<Object> getVerticesWeightsKeys() {
		return verticesUserWeights.weightsKeys();
	}

	@Override
	public void removeVerticesWeights(Object key) {
		verticesUserWeights.removeWeights(key);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E, WeightsT extends Weights<E>> WeightsT getEdgesWeights(Object key) {
		return (WeightsT) edgesUserWeights.getWeights(key);
	}

	@Override
	public Set<Object> getEdgesWeightsKeys() {
		return edgesUserWeights.weightsKeys();
	}

	@Override
	public void removeEdgesWeights(Object key) {
		edgesUserWeights.removeWeights(key);
	}

	void addInternalVerticesContainer(DataContainer<?> container) {
		verticesInternalContainers.addContainer(container);
	}

	void addInternalEdgesContainer(DataContainer<?> container) {
		edgesInternalContainers.addContainer(container);
	}

	@Override
	public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type, V defVal) {
		WeightsImpl.Index<V> weights = WeightsImpl.Index.newInstance(verticesIdStrat, type, defVal);
		verticesUserWeights.addWeights(key, weights);
		@SuppressWarnings("unchecked")
		WeightsT weights0 = (WeightsT) weights;
		return weights0;
	}

	@Override
	public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type, E defVal) {
		WeightsImpl.Index<E> weights = WeightsImpl.Index.newInstance(edgesIdStrat, type, defVal);
		edgesUserWeights.addWeights(key, weights);
		@SuppressWarnings("unchecked")
		WeightsT weights0 = (WeightsT) weights;
		return weights0;
	}

	@Override
	public IndexGraph indexGraph() {
		return this;
	}

	@Override
	public IndexIdMap indexGraphVerticesMap() {
		return GraphsUtils.IndexGraphMapIdentify;
	}

	@Override
	public IndexIdMap indexGraphEdgesMap() {
		return GraphsUtils.IndexGraphMapIdentify;
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
