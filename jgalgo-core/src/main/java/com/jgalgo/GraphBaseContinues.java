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
	private final WeightsImpl.Index.Manager verticesInternalWeights;
	private final WeightsImpl.Index.Manager edgesInternalWeights;
	private final WeightsImpl.Index.Manager verticesUserWeights;
	private final WeightsImpl.Index.Manager edgesUserWeights;

	GraphBaseContinues(int expectedVerticesNum, int expectedEdgesNum) {
		verticesIDStrat = new IDStrategyImpl.Continues(0);
		edgesIDStrat = new IDStrategyImpl.Continues(0);
		verticesInternalWeights = new WeightsImpl.Index.Manager(expectedVerticesNum);
		edgesInternalWeights = new WeightsImpl.Index.Manager(expectedEdgesNum);
		verticesUserWeights = new WeightsImpl.Index.Manager(expectedVerticesNum);
		edgesUserWeights = new WeightsImpl.Index.Manager(expectedEdgesNum);
	}

	GraphBaseContinues(GraphBaseContinues g) {
		verticesIDStrat = g.verticesIDStrat.copy();
		edgesIDStrat = g.edgesIDStrat.copy();

		/* internal data containers should be copied manually */
		// verticesInternalWeights = g.verticesInternalWeights.copy(verticesIDStrategy);
		// edgesInternalWeights = g.edgesInternalWeights.copy(edgesIDStrategy);
		verticesInternalWeights = new WeightsImpl.Index.Manager(verticesIDStrat.size());
		edgesInternalWeights = new WeightsImpl.Index.Manager(edgesIDStrat.size());

		verticesUserWeights = new WeightsImpl.Index.Manager(g.verticesUserWeights, verticesIDStrat);
		edgesUserWeights = new WeightsImpl.Index.Manager(g.edgesUserWeights, edgesIDStrat);
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
		verticesUserWeights.swapElements(v1, v2);
	}

	@Override
	public int addEdge(int source, int target) {
		checkVertex(source);
		checkVertex(target);
		int e = edgesIDStrat.newIdx();
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
		edgesUserWeights.swapElements(e1, e2);
	}

	@Override
	public void clear() {
		clearEdges();
		verticesIDStrat.clear();
		// internal weights are handled manually
		// verticesWeightsInternal.clearContainers();
		verticesUserWeights.clearContainers();
	}

	@Override
	public void clearEdges() {
		edgesIDStrat.clear();
		// internal weights are handled manually
		// edgesWeightsInternal.clearContainers();
		edgesUserWeights.clearContainers();
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
		IDStrategyImpl idStrat = (IDStrategyImpl) getVerticesIDStrategy();
		WeightsImpl.Index<V> weights = WeightsImpl.Index.newInstance(idStrat, type, defVal);
		verticesUserWeights.addWeights(key, weights);
		@SuppressWarnings("unchecked")
		WeightsT weights0 = (WeightsT) weights;
		return weights0;
	}

	@Override
	public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type, E defVal) {
		IDStrategyImpl idStrat = (IDStrategyImpl) getEdgesIDStrategy();
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
