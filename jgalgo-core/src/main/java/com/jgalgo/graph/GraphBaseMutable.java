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
import java.util.function.Consumer;
import com.jgalgo.internal.util.Assertions;

abstract class GraphBaseMutable extends IndexGraphBase {

	private final boolean isAllowSelfEdges;
	private final boolean isAllowParallelEdges;
	private final DataContainer.Manager verticesInternalContainers;
	private final DataContainer.Manager edgesInternalContainers;
	private final WeightsImpl.IndexMutable.Manager verticesUserWeights;
	private final WeightsImpl.IndexMutable.Manager edgesUserWeights;

	private final DataContainer.Long edgeEndpointsContainer;

	GraphBaseMutable(GraphBaseMutable.Capabilities capabilities, int expectedVerticesNum, int expectedEdgesNum) {
		super(capabilities.isDirected, 0, 0, true);
		this.isAllowSelfEdges = capabilities.isAllowSelfEdges;
		this.isAllowParallelEdges = capabilities.isAllowParallelEdges;
		verticesInternalContainers = new DataContainer.Manager(expectedVerticesNum);
		edgesInternalContainers = new DataContainer.Manager(expectedEdgesNum);
		verticesUserWeights = new WeightsImpl.IndexMutable.Manager(expectedVerticesNum);
		edgesUserWeights = new WeightsImpl.IndexMutable.Manager(expectedEdgesNum);

		edgeEndpointsContainer = newEdgesLongContainer(DefaultEndpoints, newArr -> edgeEndpoints = newArr);
	}

	GraphBaseMutable(GraphBaseMutable.Capabilities capabilities, IndexGraph g, boolean copyVerticesWeights,
			boolean copyEdgesWeights) {
		super(capabilities.isDirected, g.vertices().size(), g.edges().size(), true);
		this.isAllowSelfEdges = capabilities.isAllowSelfEdges;
		this.isAllowParallelEdges = capabilities.isAllowParallelEdges;
		if (isDirected()) {
			Assertions.Graphs.onlyDirected(g);
		} else {
			Assertions.Graphs.onlyUndirected(g);
		}

		verticesUserWeights = new WeightsImpl.IndexMutable.Manager(vertices.size());
		edgesUserWeights = new WeightsImpl.IndexMutable.Manager(edges.size());
		if (copyVerticesWeights) {
			for (String key : g.getVerticesWeightsKeys())
				verticesUserWeights.addWeights(key,
						WeightsImpl.IndexMutable.copyOf(g.getVerticesIWeights(key), vertices, false));
		}
		if (copyEdgesWeights) {
			for (String key : g.getEdgesWeightsKeys())
				edgesUserWeights.addWeights(key, WeightsImpl.IndexMutable.copyOf(g.getEdgesIWeights(key), edges, true));
		}

		/* internal data containers should be copied manually */
		// verticesInternalContainers = g.verticesInternalContainers.copy(vertices);
		// edgesInternalContainers = g.edgesInternalContainers.copy(edges);
		verticesInternalContainers = new DataContainer.Manager(vertices.size());
		edgesInternalContainers = new DataContainer.Manager(edges.size());

		if (g instanceof GraphBaseMutable) {
			GraphBaseMutable g0 = (GraphBaseMutable) g;
			edgeEndpointsContainer = copyEdgesContainer(g0.edgeEndpointsContainer, newArr -> edgeEndpoints = newArr);
		} else {

			final int m = edges.size();
			edgeEndpointsContainer = newEdgesLongContainer(DefaultEndpoints, newArr -> edgeEndpoints = newArr);
			for (int e = 0; e < m; e++)
				setEndpoints(e, g.edgeSource(e), g.edgeTarget(e));
		}

		if (!capabilities.isAllowSelfEdges) {
			if (g.isAllowSelfEdges()) {
				checkNoSelfEdges();
			} else {
				assert checkNoSelfEdges();
			}
		}
	}

	GraphBaseMutable(GraphBaseMutable.Capabilities capabilities, IndexGraphBuilderImpl builder) {
		super(capabilities.isDirected, builder.vertices.copy(), builder.edges.copy());
		this.isAllowSelfEdges = capabilities.isAllowSelfEdges;
		this.isAllowParallelEdges = capabilities.isAllowParallelEdges;
		verticesUserWeights = new WeightsImpl.IndexMutable.Manager(builder.verticesUserWeights, vertices);
		edgesUserWeights = new WeightsImpl.IndexMutable.Manager(builder.edgesUserWeights, edges);
		verticesInternalContainers = new DataContainer.Manager(vertices.size());
		edgesInternalContainers = new DataContainer.Manager(edges.size());

		final int m = edges.size();
		edgeEndpointsContainer = newEdgesLongContainer(DefaultEndpoints, newArr -> edgeEndpoints = newArr);

		for (int e = 0; e < m; e++)
			setEndpoints(e, builder.edgeSource(e), builder.edgeTarget(e));

		if (!capabilities.isAllowSelfEdges)
			checkNoSelfEdges();
	}

	static class Capabilities {
		private Capabilities(boolean isDirected, boolean isAllowSelfEdges, boolean isAllowParallelEdges) {
			this.isDirected = isDirected;
			this.isAllowSelfEdges = isAllowSelfEdges;
			this.isAllowParallelEdges = isAllowParallelEdges;
		}

		static Capabilities of(boolean isDirected, boolean isAllowSelfEdges, boolean isAllowParallelEdges) {
			return new Capabilities(isDirected, isAllowSelfEdges, isAllowParallelEdges);
		}

		private final boolean isDirected;
		private final boolean isAllowSelfEdges;
		private final boolean isAllowParallelEdges;

	}

	@Override
	public final boolean isAllowSelfEdges() {
		return isAllowSelfEdges;
	}

	@Override
	public final boolean isAllowParallelEdges() {
		return isAllowParallelEdges;
	}

	private final GraphElementSet.Mutable vertices0() {
		return (GraphElementSet.Mutable) vertices;
	}

	private final GraphElementSet.Mutable edges0() {
		return (GraphElementSet.Mutable) edges;
	}

	@Override
	public int addVertex() {
		int u = vertices0().newIdx();
		assert u >= 0;
		verticesInternalContainers.ensureCapacity(u + 1);
		verticesUserWeights.ensureCapacity(u + 1);
		return u;
	}

	@Override
	public final void removeVertex(int vertex) {
		checkVertex(vertex);
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
		vertices0().removeIdx(vertex);
	}

	void vertexSwapAndRemove(int removedIdx, int swappedIdx) {
		// internal weights are handled manually
		// verticesInternalContainers.swapElements(removedIdx, swappedIdx);
		verticesUserWeights.swapAndClear(removedIdx, swappedIdx);
		vertices0().swapAndRemove(removedIdx, swappedIdx);
	}

	@Override
	public int addEdge(int source, int target) {
		checkVertex(source);
		checkVertex(target);
		checkNewEdgeEndpoints(source, target);

		int e = edges0().newIdx();

		assert e >= 0;
		edgesInternalContainers.ensureCapacity(e + 1);
		edgesUserWeights.ensureCapacity(e + 1);

		setEndpoints(e, source, target);
		return e;
	}

	void checkNewEdgeEndpoints(int source, int target) {
		if (!isAllowSelfEdges() && source == target)
			throw new IllegalArgumentException("Self edges are not allowed");
		if (!isAllowParallelEdges() && getEdge(source, target) != -1)
			throw new IllegalArgumentException(
					"Edge (idx=" + source + ",idx=" + target + ") already exists. Parallel edges are not allowed.");
	}

	@Override
	public final void removeEdge(int edge) {
		checkEdge(edge);
		if (edge == edges.size - 1) {
			removeEdgeLast(edge);
		} else {
			edgeSwapAndRemove(edge, edges.size - 1);
		}
	}

	void removeEdgeLast(int edge) {
		edgesUserWeights.clearElement(edge);
		edges0().removeIdx(edge);
		clear(edgeEndpoints, edge, DefaultEndpoints);
	}

	void edgeSwapAndRemove(int removedIdx, int swappedIdx) {
		// internal weights are handled manually
		// edgesInternalContainers.swapElements(removedIdx, swappedIdx);
		edgesUserWeights.swapAndClear(removedIdx, swappedIdx);
		edges0().swapAndRemove(removedIdx, swappedIdx);
		swapAndClear(edgeEndpoints, removedIdx, swappedIdx, DefaultEndpoints);
	}

	void replaceEdgeSource(int edge, int newSource) {
		long endpoints = edgeEndpoints[edge];
		int target = endpoints2Target(endpoints);
		edgeEndpoints[edge] = sourceTarget2Endpoints(newSource, target);
	}

	void replaceEdgeTarget(int edge, int newTarget) {
		long endpoints = edgeEndpoints[edge];
		int source = endpoints2Source(endpoints);
		edgeEndpoints[edge] = sourceTarget2Endpoints(source, newTarget);
	}

	void replaceEdgeEndpoint(int edge, int oldEndpoint, int newEndpoint) {
		long endpoints = edgeEndpoints[edge];
		int source = endpoints2Source(endpoints);
		int target = endpoints2Target(endpoints);
		if (source == oldEndpoint)
			source = newEndpoint;
		if (target == oldEndpoint)
			target = newEndpoint;
		edgeEndpoints[edge] = sourceTarget2Endpoints(source, target);
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
		edgeEndpointsContainer.clear();
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

	<T> DataContainer.Obj<T> newVerticesContainer(T defVal, T[] emptyArr, Consumer<T[]> onArrayAlloc) {
		return addVerticesContainer(new DataContainer.Obj<>(vertices, defVal, emptyArr, onArrayAlloc));
	}

	DataContainer.Int newVerticesIntContainer(int defVal, Consumer<int[]> onArrayAlloc) {
		return addVerticesContainer(new DataContainer.Int(vertices, defVal, onArrayAlloc));
	}

	<T> DataContainer.Obj<T> newEdgesContainer(T defVal, T[] emptyArr, Consumer<T[]> onArrayAlloc) {
		return addEdgesContainer(new DataContainer.Obj<>(edges, defVal, emptyArr, onArrayAlloc));
	}

	DataContainer.Int newEdgesIntContainer(int defVal, Consumer<int[]> onArrayAlloc) {
		return addEdgesContainer(new DataContainer.Int(edges, defVal, onArrayAlloc));
	}

	DataContainer.Long newEdgesLongContainer(long defVal, Consumer<long[]> onArrayAlloc) {
		return addEdgesContainer(new DataContainer.Long(edges, defVal, onArrayAlloc));
	}

	DataContainer.Int copyVerticesContainer(DataContainer.Int container, Consumer<int[]> onArrayAlloc) {
		return addVerticesContainer(container.copy(vertices, onArrayAlloc));
	}

	<T> DataContainer.Obj<T> copyVerticesContainer(DataContainer.Obj<T> container, T[] emptyArr,
			Consumer<T[]> onArrayAlloc) {
		return addVerticesContainer(container.copy(vertices, emptyArr, onArrayAlloc));
	}

	DataContainer.Long copyEdgesContainer(DataContainer.Long container, Consumer<long[]> onArrayAlloc) {
		return addEdgesContainer(container.copy(edges, onArrayAlloc));
	}

	private <ContainerT extends DataContainer> ContainerT addVerticesContainer(ContainerT container) {
		verticesInternalContainers.addContainer(container);
		return container;
	}

	private <ContainerT extends DataContainer> ContainerT addEdgesContainer(ContainerT container) {
		edgesInternalContainers.addContainer(container);
		return container;
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

	private boolean checkNoSelfEdges() {
		for (int m = edges().size(), e = 0; e < m; e++)
			if (source(e) == target(e))
				throw new IllegalArgumentException("Self edges are not allowed");
		return true;
	}
}
