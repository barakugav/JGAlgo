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

package com.jgalgo.graph;

import java.util.Set;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntSet;

abstract class GraphBaseIndexMutable extends GraphBase implements IndexGraphImpl {

	final IdStrategy.Default verticesIdStrat;
	final IdStrategy.Default edgesIdStrat;
	private final DataContainer.Manager verticesInternalContainers;
	private final DataContainer.Manager edgesInternalContainers;
	private final WeightsImpl.IndexMutable.Manager verticesUserWeights;
	private final WeightsImpl.IndexMutable.Manager edgesUserWeights;

	GraphBaseIndexMutable(int expectedVerticesNum, int expectedEdgesNum) {
		verticesIdStrat = new IdStrategy.Default(0);
		edgesIdStrat = new IdStrategy.Default(0);
		verticesInternalContainers = new DataContainer.Manager(expectedVerticesNum);
		edgesInternalContainers = new DataContainer.Manager(expectedEdgesNum);
		verticesUserWeights = new WeightsImpl.IndexMutable.Manager(expectedVerticesNum);
		edgesUserWeights = new WeightsImpl.IndexMutable.Manager(expectedEdgesNum);
	}

	GraphBaseIndexMutable(IndexGraph g, boolean copyWeights) {
		if (getCapabilities().directed()) {
			Assertions.Graphs.onlyDirected(g);
		} else {
			Assertions.Graphs.onlyUndirected(g);
		}
		if (!getCapabilities().selfEdges())
			Assertions.Graphs.noSelfEdges(g, "self edges are not supported");
		if (!getCapabilities().parallelEdges())
			Assertions.Graphs.noParallelEdges(g, "parallel edges are not supported");

		verticesIdStrat = new IdStrategy.Default(g.vertices().size());
		edgesIdStrat = new IdStrategy.Default(g.edges().size());

		verticesUserWeights = new WeightsImpl.IndexMutable.Manager(verticesIdStrat.size());
		edgesUserWeights = new WeightsImpl.IndexMutable.Manager(edgesIdStrat.size());
		if (copyWeights) {
			for (Object key : g.getVerticesWeightsKeys())
				verticesUserWeights.addWeights(key,
						WeightsImpl.IndexMutable.copyOf(g.getVerticesWeights(key), verticesIdStrat));
			for (Object key : g.getEdgesWeightsKeys())
				edgesUserWeights.addWeights(key, WeightsImpl.IndexMutable.copyOf(g.getEdgesWeights(key), edgesIdStrat));
		}

		/* internal data containers should be copied manually */
		// verticesInternalContainers = g.verticesInternalContainers.copy(verticesIdStrategy);
		// edgesInternalContainers = g.edgesInternalContainers.copy(edgesIdStrategy);
		verticesInternalContainers = new DataContainer.Manager(verticesIdStrat.size());
		edgesInternalContainers = new DataContainer.Manager(edgesIdStrat.size());
	}

	GraphBaseIndexMutable(IndexGraphBuilderImpl builder) {
		verticesIdStrat = builder.verticesIdStrat.copy();
		edgesIdStrat = builder.edgesIdStrat.copy();
		verticesUserWeights = new WeightsImpl.IndexMutable.Manager(builder.verticesUserWeights, verticesIdStrat);
		edgesUserWeights = new WeightsImpl.IndexMutable.Manager(builder.edgesUserWeights, edgesIdStrat);
		verticesInternalContainers = new DataContainer.Manager(verticesIdStrat.size());
		edgesInternalContainers = new DataContainer.Manager(edgesIdStrat.size());
	}

	@Override
	public final IntSet vertices() {
		return verticesIdStrat.indices();
	}

	@Override
	public final IntSet edges() {
		return edgesIdStrat.indices();
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
		WeightsImpl.IndexMutable<V> weights = WeightsImpl.IndexMutable.newInstance(verticesIdStrat, type, defVal);
		verticesUserWeights.addWeights(key, weights);
		@SuppressWarnings("unchecked")
		WeightsT weights0 = (WeightsT) weights;
		return weights0;
	}

	@Override
	public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type, E defVal) {
		WeightsImpl.IndexMutable<E> weights = WeightsImpl.IndexMutable.newInstance(edgesIdStrat, type, defVal);
		edgesUserWeights.addWeights(key, weights);
		@SuppressWarnings("unchecked")
		WeightsT weights0 = (WeightsT) weights;
		return weights0;
	}

	void checkVertex(int vertex) {
		Assertions.Graphs.checkVertex(vertex, verticesIdStrat.size);
	}

	void checkEdge(int edge) {
		Assertions.Graphs.checkEdge(edge, edgesIdStrat.size);
	}

}
