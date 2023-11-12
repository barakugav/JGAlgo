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

abstract class GraphBaseMutable extends IndexGraphBase {

	final GraphElementSet.Default vertices;
	final GraphElementSet.Default edges;
	private final DataContainer.Manager verticesInternalContainers;
	private final DataContainer.Manager edgesInternalContainers;
	private final WeightsImpl.IndexMutable.Manager verticesUserWeights;
	private final WeightsImpl.IndexMutable.Manager edgesUserWeights;

	GraphBaseMutable(IndexGraphBase.Capabilities capabilities, int expectedVerticesNum, int expectedEdgesNum) {
		super(capabilities);
		vertices = new GraphElementSet.Default(0, false);
		edges = new GraphElementSet.Default(0, true);
		verticesInternalContainers = new DataContainer.Manager(expectedVerticesNum);
		edgesInternalContainers = new DataContainer.Manager(expectedEdgesNum);
		verticesUserWeights = new WeightsImpl.IndexMutable.Manager(expectedVerticesNum);
		edgesUserWeights = new WeightsImpl.IndexMutable.Manager(expectedEdgesNum);
	}

	GraphBaseMutable(IndexGraphBase.Capabilities capabilities, IndexGraph g, boolean copyWeights) {
		super(capabilities);
		if (isDirected()) {
			Assertions.Graphs.onlyDirected(g);
		} else {
			Assertions.Graphs.onlyUndirected(g);
		}
		if (!isAllowSelfEdges())
			Assertions.Graphs.noSelfEdges(g, "self edges are not supported");
		if (!isAllowParallelEdges())
			Assertions.Graphs.noParallelEdges(g, "parallel edges are not supported");

		vertices = new GraphElementSet.Default(g.vertices().size(), false);
		edges = new GraphElementSet.Default(g.edges().size(), true);

		verticesUserWeights = new WeightsImpl.IndexMutable.Manager(vertices.size());
		edgesUserWeights = new WeightsImpl.IndexMutable.Manager(edges.size());
		if (copyWeights) {
			for (String key : g.getVerticesWeightsKeys())
				verticesUserWeights.addWeights(key,
						WeightsImpl.IndexMutable.copyOf(g.getVerticesIWeights(key), vertices, false));
			for (String key : g.getEdgesWeightsKeys())
				edgesUserWeights.addWeights(key, WeightsImpl.IndexMutable.copyOf(g.getEdgesIWeights(key), edges, true));
		}

		/* internal data containers should be copied manually */
		// verticesInternalContainers = g.verticesInternalContainers.copy(vertices);
		// edgesInternalContainers = g.edgesInternalContainers.copy(edges);
		verticesInternalContainers = new DataContainer.Manager(vertices.size());
		edgesInternalContainers = new DataContainer.Manager(edges.size());
	}

	GraphBaseMutable(IndexGraphBase.Capabilities capabilities, IndexGraphBuilderImpl builder) {
		super(capabilities);
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
		// vertex = vertexSwapBeforeRemove(vertex);
		// removeVertexImpl(vertex);

		if (vertex == vertices.size - 1) {
			removeVertexLast(vertex);
		} else {
			vertexSwapAndRemove(vertex, vertices.size - 1);
		}
	}

	void removeVertexLast(int vertex) {
		verticesUserWeights.clearElement(vertex);
		vertices.removeIdx(vertex);
	}

	void vertexSwapAndRemove(int removedIdx, int swappedIdx) {
		// internal weights are handled manually
		// verticesInternalContainers.swapElements(removedIdx, swappedIdx);
		verticesUserWeights.swapAndClear(removedIdx, swappedIdx);
		vertices.swapAndRemove(removedIdx, swappedIdx);
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
		if (edge == edges.size - 1) {
			removeEdgeLast(edge);
		} else {
			edgeSwapAndRemove(edge, edges.size - 1);
		}
	}

	void removeEdgeLast(int edge) {
		edgesUserWeights.clearElement(edge);
		edges.removeIdx(edge);
	}

	void edgeSwapAndRemove(int removedIdx, int swappedIdx) {
		// internal weights are handled manually
		// edgesInternalContainers.swapElements(removedIdx, swappedIdx);
		edgesUserWeights.swapAndClear(removedIdx, swappedIdx);
		edges.swapAndRemove(removedIdx, swappedIdx);
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
	public <T, WeightsT extends IWeights<T>> WeightsT getVerticesIWeights(String key) {
		return verticesUserWeights.getWeights(key);
	}

	@Override
	public Set<String> getVerticesWeightsKeys() {
		return verticesUserWeights.weightsKeys();
	}

	@Override
	public void removeVerticesWeights(String key) {
		verticesUserWeights.removeWeights(key);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T, WeightsT extends IWeights<T>> WeightsT getEdgesIWeights(String key) {
		return (WeightsT) edgesUserWeights.getWeights(key);
	}

	@Override
	public Set<String> getEdgesWeightsKeys() {
		return edgesUserWeights.weightsKeys();
	}

	@Override
	public void removeEdgesWeights(String key) {
		edgesUserWeights.removeWeights(key);
	}

	void addInternalVerticesContainer(DataContainer container) {
		verticesInternalContainers.addContainer(container);
	}

	void addInternalEdgesContainer(DataContainer container) {
		edgesInternalContainers.addContainer(container);
	}

	@Override
	public <T, WeightsT extends Weights<Integer, T>> WeightsT addVerticesWeights(String key, Class<? super T> type,
			T defVal) {
		WeightsImpl.IndexMutable<T> weights = WeightsImpl.IndexMutable.newInstance(vertices, false, type, defVal);
		verticesUserWeights.addWeights(key, weights);
		@SuppressWarnings("unchecked")
		WeightsT weights0 = (WeightsT) weights;
		return weights0;
	}

	@Override
	public <T, WeightsT extends Weights<Integer, T>> WeightsT addEdgesWeights(String key, Class<? super T> type,
			T defVal) {
		WeightsImpl.IndexMutable<T> weights = WeightsImpl.IndexMutable.newInstance(edges, true, type, defVal);
		edgesUserWeights.addWeights(key, weights);
		@SuppressWarnings("unchecked")
		WeightsT weights0 = (WeightsT) weights;
		return weights0;
	}

	@Override
	public void addVertexRemoveListener(IndexRemoveListener listener) {
		vertices.addRemoveListener(listener);
	}

	@Override
	public void removeVertexRemoveListener(IndexRemoveListener listener) {
		vertices.removeRemoveListener(listener);
	}

	@Override
	public void addEdgeRemoveListener(IndexRemoveListener listener) {
		edges.addRemoveListener(listener);
	}

	@Override
	public void removeEdgeRemoveListener(IndexRemoveListener listener) {
		edges.removeRemoveListener(listener);
	}

	void checkVertex(int vertex) {
		Assertions.Graphs.checkVertex(vertex, vertices.size);
	}

	void checkEdge(int edge) {
		Assertions.Graphs.checkEdge(edge, edges.size);
	}

	static void clear(int[] dataContainer, int idx, int defaultVal) {
		dataContainer[idx] = defaultVal;
	}

	static void clear(long[] dataContainer, int idx, long defaultVal) {
		dataContainer[idx] = defaultVal;
	}

	void swapAndClear(int[] dataContainer, int removedIdx, int swappedIdx, int defaultVal) {
		dataContainer[removedIdx] = dataContainer[swappedIdx];
		dataContainer[swappedIdx] = defaultVal;
	}

	void swapAndClear(long[] dataContainer, int removedIdx, int swappedIdx, long defaultVal) {
		dataContainer[removedIdx] = dataContainer[swappedIdx];
		dataContainer[swappedIdx] = defaultVal;
	}

	<T> void swapAndClear(T[] dataContainer, int removedIdx, int swappedIdx, T defaultVal) {
		dataContainer[removedIdx] = dataContainer[swappedIdx];
		dataContainer[swappedIdx] = defaultVal;
	}
}
