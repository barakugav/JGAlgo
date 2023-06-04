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

abstract class GraphBaseIndex extends GraphBase implements IndexGraph {

	final IdStrategyImpl.Index verticesIdStrat;
	final IdStrategyImpl.Index edgesIdStrat;
	private final WeightsImpl.Index.Manager verticesInternalWeights;
	private final WeightsImpl.Index.Manager edgesInternalWeights;
	private final WeightsImpl.Index.Manager verticesUserWeights;
	private final WeightsImpl.Index.Manager edgesUserWeights;

	GraphBaseIndex(int expectedVerticesNum, int expectedEdgesNum) {
		verticesIdStrat = new IdStrategyImpl.Index(0);
		edgesIdStrat = new IdStrategyImpl.Index(0);
		verticesInternalWeights = new WeightsImpl.Index.Manager(expectedVerticesNum);
		edgesInternalWeights = new WeightsImpl.Index.Manager(expectedEdgesNum);
		verticesUserWeights = new WeightsImpl.Index.Manager(expectedVerticesNum);
		edgesUserWeights = new WeightsImpl.Index.Manager(expectedEdgesNum);
	}

	GraphBaseIndex(GraphBaseIndex g) {
		verticesIdStrat = g.verticesIdStrat.copy();
		edgesIdStrat = g.edgesIdStrat.copy();

		/* internal data containers should be copied manually */
		// verticesInternalWeights = g.verticesInternalWeights.copy(verticesIdStrategy);
		// edgesInternalWeights = g.edgesInternalWeights.copy(edgesIdStrategy);
		verticesInternalWeights = new WeightsImpl.Index.Manager(verticesIdStrat.size());
		edgesInternalWeights = new WeightsImpl.Index.Manager(edgesIdStrat.size());

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
		verticesInternalWeights.ensureCapacity(u + 1);
		verticesUserWeights.ensureCapacity(u + 1);
		return u;
	}

	@Override
	public void removeVertex(int vertex) {
		removeEdgesOf(vertex);
		vertex = vertexSwapBeforeRemove(vertex);
		// internal weights are handled manually
		// verticesWeightsInternal.clearElement(vertex);
		verticesUserWeights.clearElement(vertex);
		verticesIdStrat.removeIdx(vertex);
	}

	int vertexSwapBeforeRemove(int v) {
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
		// verticesWeightsInternal.swapElements(v1, v2);
		verticesUserWeights.swapElements(v1, v2);
	}

	@Override
	public int addEdge(int source, int target) {
		checkVertex(source);
		checkVertex(target);
		int e = edgesIdStrat.newIdx();
		assert e >= 0;
		edgesInternalWeights.ensureCapacity(e + 1);
		edgesUserWeights.ensureCapacity(e + 1);
		return e;
	}

	@Override
	public void removeEdge(int edge) {
		edge = edgeSwapBeforeRemove(edge);
		// internal weights are handled manually
		// edgesWeightsInternal.clearElement(edge);
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
		// edgesWeightsInternal.swapElements(e1, e2);
		edgesUserWeights.swapElements(e1, e2);
	}

	@Override
	public void clear() {
		clearEdges();
		verticesIdStrat.clear();
		// internal weights are handled manually
		// verticesWeightsInternal.clearContainers();
		verticesUserWeights.clearContainers();
	}

	@Override
	public void clearEdges() {
		edgesIdStrat.clear();
		// internal weights are handled manually
		// edgesWeightsInternal.clearContainers();
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
	public Set<Object> getVerticesWeightKeys() {
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

	void addInternalVerticesWeights(Object key, WeightsImpl.Index<?> container) {
		verticesInternalWeights.addWeights(key, container);
	}

	void addInternalEdgesWeights(Object key, WeightsImpl.Index<?> container) {
		edgesInternalWeights.addWeights(key, container);
	}

	@Override
	public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type, V defVal) {
		IdStrategyImpl idStrat = (IdStrategyImpl) getVerticesIdStrategy();
		WeightsImpl.Index<V> weights = WeightsImpl.Index.newInstance(idStrat, type, defVal);
		verticesUserWeights.addWeights(key, weights);
		@SuppressWarnings("unchecked")
		WeightsT weights0 = (WeightsT) weights;
		return weights0;
	}

	@Override
	public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type, E defVal) {
		IdStrategyImpl idStrat = (IdStrategyImpl) getEdgesIdStrategy();
		WeightsImpl.Index<E> weights = WeightsImpl.Index.newInstance(idStrat, type, defVal);
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
