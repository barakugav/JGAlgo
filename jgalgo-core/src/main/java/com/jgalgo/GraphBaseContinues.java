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

abstract class GraphBaseContinues extends GraphBase implements IndexGraph {

	final IDStrategyImpl verticesIDStrat;
	final IDStrategyImpl edgesIDStrat;
	private final DataContainer.Manager verticesInternalData;
	private final DataContainer.Manager edgesInternalData;
	private final WeightsImpl.Manager verticesUserData;
	private final WeightsImpl.Manager edgesUserData;

	GraphBaseContinues(int expectedVerticesNum, int expectedEdgesNum) {
		verticesIDStrat = new IDStrategyImpl.Continues(0);
		edgesIDStrat = new IDStrategyImpl.Continues(0);
		verticesInternalData = new DataContainer.Manager(expectedVerticesNum);
		edgesInternalData = new DataContainer.Manager(expectedEdgesNum);
		verticesUserData = new WeightsImpl.Manager(expectedVerticesNum);
		edgesUserData = new WeightsImpl.Manager(expectedEdgesNum);
	}

	GraphBaseContinues(GraphBaseContinues g) {
		verticesIDStrat = g.verticesIDStrat.copy();
		edgesIDStrat = g.edgesIDStrat.copy();

		/* internal data containers should be copied manually */
		// verticesInternalData = g.verticesInternalData.copy(verticesIDStrategy);
		// edgesInternalData = g.edgesInternalData.copy(edgesIDStrategy);
		verticesInternalData = new DataContainer.Manager(verticesIDStrat.size());
		edgesInternalData = new DataContainer.Manager(edgesIDStrat.size());

		verticesUserData = new WeightsImpl.Manager(g.verticesUserData, verticesIDStrat, null);
		edgesUserData = new WeightsImpl.Manager(g.edgesUserData, edgesIDStrat, null);
	}

	@Override
	public final IntSet vertices() {
		return verticesIDStrat.idSet();
	}

	@Override
	public final IntSet edges() {
		return edgesIDStrat.idSet();
	}

	@Override
	public int addVertex() {
		int u = verticesIDStrat.newIdx();
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
		verticesIDStrat.removeIdx(vertex);
	}

	int vertexSwapBeforeRemove(int v) {
		int vn = verticesIDStrat.isSwapNeededBeforeRemove(v);
		if (v != vn) {
			vertexSwap(v, vn);
			v = vn;
		}
		return v;
	}

	void vertexSwap(int v1, int v2) {
		verticesIDStrat.idxSwap(v1, v2);
		// internal weights are handled manually
		// verticesWeightsInternal.swapElements(v1, v2);
		verticesUserData.swapElements(v1, v2);
	}

	@Override
	public int addEdge(int source, int target) {
		checkVertex(source);
		checkVertex(target);
		int e = edgesIDStrat.newIdx();
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
		edgesIDStrat.removeIdx(edge);
	}

	int edgeSwapBeforeRemove(int e) {
		int en = edgesIDStrat.isSwapNeededBeforeRemove(e);
		if (e != en) {
			edgeSwap(e, en);
			e = en;
		}
		return e;
	}

	void edgeSwap(int e1, int e2) {
		edgesIDStrat.idxSwap(e1, e2);
		// internal weights are handled manually
		// edgesWeightsInternal.swapElements(e1, e2);
		edgesUserData.swapElements(e1, e2);
	}

	@Override
	public void clear() {
		clearEdges();
		verticesIDStrat.clear();
		// internal weights are handled manually
		// verticesWeightsInternal.clearContainers();
		verticesUserData.clearContainers();
	}

	@Override
	public void clearEdges() {
		edgesIDStrat.clear();
		// internal weights are handled manually
		// edgesWeightsInternal.clearContainers();
		edgesUserData.clearContainers();
	}

	@Override
	public IDStrategy getVerticesIDStrategy() {
		return verticesIDStrat;
	}

	@Override
	public IDStrategy getEdgesIDStrategy() {
		return edgesIDStrat;
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
	public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type, V defVal) {
		IDStrategyImpl idStrat = (IDStrategyImpl) getVerticesIDStrategy();
		DataContainer<V> container = DataContainer.newInstance(idStrat, type, defVal);
		Weights<?> weights = WeightsImpl.wrapContainerDirected(container);
		verticesUserData.addWeights(key, weights);
		@SuppressWarnings("unchecked")
		WeightsT weights0 = (WeightsT) weights;
		return weights0;
	}

	@Override
	public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type, E defVal) {
		IDStrategyImpl idStrat = (IDStrategyImpl) getEdgesIDStrategy();
		DataContainer<E> container = DataContainer.newInstance(idStrat, type, defVal);
		Weights<?> weights = WeightsImpl.wrapContainerDirected(container);
		edgesUserData.addWeights(key, weights);
		@SuppressWarnings("unchecked")
		WeightsT weights0 = (WeightsT) weights;
		return weights0;
	}

	@Override
	public IndexGraph indexGraph() {
		return this;
	}

	@Override
	public IndexGraphMap indexGraphVerticesMap() {
		return GraphsUtils.IndexGraphMapIdentify;
	}

	@Override
	public IndexGraphMap indexGraphEdgesMap() {
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
