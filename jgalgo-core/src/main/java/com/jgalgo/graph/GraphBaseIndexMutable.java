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

abstract class GraphBaseIndexMutable extends IndexGraphBase {

	final GraphElementSet.Default vertices;
	final GraphElementSet.Default edges;
	private final DataContainer.Manager verticesInternalContainers;
	private final DataContainer.Manager edgesInternalContainers;
	private final WeightsImpl.IndexMutable.Manager verticesUserWeights;
	private final WeightsImpl.IndexMutable.Manager edgesUserWeights;

	GraphBaseIndexMutable(int expectedVerticesNum, int expectedEdgesNum) {
		vertices = new GraphElementSet.Default(0, false);
		edges = new GraphElementSet.Default(0, true);
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

		vertices = new GraphElementSet.Default(g.vertices().size(), false);
		edges = new GraphElementSet.Default(g.edges().size(), true);

		verticesUserWeights = new WeightsImpl.IndexMutable.Manager(vertices.size());
		edgesUserWeights = new WeightsImpl.IndexMutable.Manager(edges.size());
		if (copyWeights) {
			for (Object key : g.getVerticesWeightsKeys())
				verticesUserWeights.addWeights(key,
						WeightsImpl.IndexMutable.copyOf(g.getVerticesWeights(key), vertices));
			for (Object key : g.getEdgesWeightsKeys())
				edgesUserWeights.addWeights(key, WeightsImpl.IndexMutable.copyOf(g.getEdgesWeights(key), edges));
		}

		/* internal data containers should be copied manually */
		// verticesInternalContainers = g.verticesInternalContainers.copy(vertices);
		// edgesInternalContainers = g.edgesInternalContainers.copy(edges);
		verticesInternalContainers = new DataContainer.Manager(vertices.size());
		edgesInternalContainers = new DataContainer.Manager(edges.size());
	}

	GraphBaseIndexMutable(IndexGraphBuilderImpl builder) {
		vertices = builder.vertices.copy();
		edges = builder.edges.copy();
		verticesUserWeights = new WeightsImpl.IndexMutable.Manager(builder.verticesUserWeights, vertices);
		edgesUserWeights = new WeightsImpl.IndexMutable.Manager(builder.edgesUserWeights, edges);
		verticesInternalContainers = new DataContainer.Manager(vertices.size());
		edgesInternalContainers = new DataContainer.Manager(edges.size());
	}

	@Override
	public final GraphElementSet vertices() {
		return vertices;
	}

	@Override
	public final GraphElementSet edges() {
		return edges;
	}

	@Override
	public int addVertex() {
		int u = vertices.newIdx();
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
		vertices.removeIdx(vertex);
	}

	private int vertexSwapBeforeRemove(int v) {
		int vn = vertices.isSwapNeededBeforeRemove(v);
		if (v != vn) {
			vertexSwap(v, vn);
			v = vn;
		}
		return v;
	}

	void vertexSwap(int v1, int v2) {
		vertices.idxSwap(v1, v2);
		// internal weights are handled manually
		// verticesInternalContainers.swapElements(v1, v2);
		verticesUserWeights.swapElements(v1, v2);
	}

	@Override
	public int addEdge(int source, int target) {
		checkVertex(source);
		checkVertex(target);
		int e = edges.newIdx();
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
		edges.removeIdx(edge);
	}

	int edgeSwapBeforeRemove(int e) {
		int en = edges.isSwapNeededBeforeRemove(e);
		if (e != en) {
			edgeSwap(e, en);
			e = en;
		}
		return e;
	}

	void edgeSwap(int e1, int e2) {
		edges.idxSwap(e1, e2);
		// internal weights are handled manually
		// edgesInternalContainers.swapElements(e1, e2);
		edgesUserWeights.swapElements(e1, e2);
	}

	@Override
	public void clear() {
		clearEdges();
		vertices.clear();
		// internal weights are handled manually
		// verticesInternalContainers.clearContainers();
		verticesUserWeights.clearContainers();
	}

	@Override
	public void clearEdges() {
		edges.clear();
		// internal weights are handled manually
		// edgesInternalContainers.clearContainers();
		edgesUserWeights.clearContainers();
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

	void addInternalVerticesContainer(DataContainer container) {
		verticesInternalContainers.addContainer(container);
	}

	void addInternalEdgesContainer(DataContainer container) {
		edgesInternalContainers.addContainer(container);
	}

	@Override
	public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type, V defVal) {
		WeightsImpl.IndexMutable<V> weights = WeightsImpl.IndexMutable.newInstance(vertices, type, defVal);
		verticesUserWeights.addWeights(key, weights);
		@SuppressWarnings("unchecked")
		WeightsT weights0 = (WeightsT) weights;
		return weights0;
	}

	@Override
	public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type, E defVal) {
		WeightsImpl.IndexMutable<E> weights = WeightsImpl.IndexMutable.newInstance(edges, type, defVal);
		edgesUserWeights.addWeights(key, weights);
		@SuppressWarnings("unchecked")
		WeightsT weights0 = (WeightsT) weights;
		return weights0;
	}

	void checkVertex(int vertex) {
		Assertions.Graphs.checkVertex(vertex, vertices.size);
	}

	void checkEdge(int edge) {
		Assertions.Graphs.checkEdge(edge, edges.size);
	}

}
