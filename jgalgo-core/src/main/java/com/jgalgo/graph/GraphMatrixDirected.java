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

/**
 * A directed graph implementation using a two dimensional matrix to store all edges.
 *
 * <p>
 * If the graph contains \(n\) vertices, matrix of size {@code [n][n]} stores the edges of the graph. The implementation
 * does not support multiple edges with identical source and target.
 *
 * <p>
 * This implementation is efficient for use cases where fast lookups of edge \((u,v)\) are required, as they can be
 * answered in \(O(1)\) time, but it should not be the default choice for a directed graph.
 *
 * @see    GraphMatrixUndirected
 * @author Barak Ugav
 */
class GraphMatrixDirected extends GraphMatrixAbstract {

	private int[] edgesOutNum;
	private int[] edgesInNum;
	private final DataContainer.Int edgesOutNumContainer;
	private final DataContainer.Int edgesInNumContainer;
	private static final IndexGraphBase.Capabilities Capabilities = IndexGraphBase.Capabilities.of(true, true, false);

	GraphMatrixDirected() {
		this(0, 0);
	}

	/**
	 * Create a new graph with no vertices and edges, with expected number of vertices and edges.
	 *
	 * @param expectedVerticesNum the expected number of vertices that will be in the graph
	 * @param expectedEdgesNum    the expected number of edges that will be in the graph
	 */
	GraphMatrixDirected(int expectedVerticesNum, int expectedEdgesNum) {
		super(Capabilities, expectedVerticesNum, expectedEdgesNum);
		edgesOutNumContainer = new DataContainer.Int(vertices, 0, newArr -> edgesOutNum = newArr);
		edgesInNumContainer = new DataContainer.Int(vertices, 0, newArr -> edgesInNum = newArr);
		addInternalVerticesContainer(edgesOutNumContainer);
		addInternalVerticesContainer(edgesInNumContainer);
	}

	GraphMatrixDirected(IndexGraph g, boolean copyWeights) {
		super(Capabilities, g, copyWeights);
		assert g.isDirected();
		if (g instanceof GraphMatrixDirected) {
			GraphMatrixDirected g0 = (GraphMatrixDirected) g;
			edgesOutNumContainer = g0.edgesOutNumContainer.copy(vertices, newArr -> edgesOutNum = newArr);
			edgesInNumContainer = g0.edgesInNumContainer.copy(vertices, newArr -> edgesInNum = newArr);
			addInternalEdgesContainer(edgesOutNumContainer);
			addInternalEdgesContainer(edgesInNumContainer);
		} else {
			edgesOutNumContainer = new DataContainer.Int(vertices, 0, newArr -> edgesOutNum = newArr);
			edgesInNumContainer = new DataContainer.Int(vertices, 0, newArr -> edgesInNum = newArr);
			addInternalVerticesContainer(edgesOutNumContainer);
			addInternalVerticesContainer(edgesInNumContainer);

			for (int n = g.vertices().size(), u = 0; u < n; u++) {
				edgesOutNum[u] = g.outEdges(u).size();
				edgesInNum[u] = g.inEdges(u).size();
			}
		}
	}

	GraphMatrixDirected(IndexGraphBuilderImpl.Directed builder) {
		super(Capabilities, builder);
		edgesOutNumContainer = new DataContainer.Int(vertices, 0, newArr -> edgesOutNum = newArr);
		edgesInNumContainer = new DataContainer.Int(vertices, 0, newArr -> edgesInNum = newArr);
		addInternalVerticesContainer(edgesOutNumContainer);
		addInternalVerticesContainer(edgesInNumContainer);

		for (int m = builder.edges().size(), e = 0; e < m; e++) {
			edgesOutNum[builder.edgeSource(e)]++;
			edgesInNum[builder.edgeTarget(e)]++;
		}
	}

	@Override
	void vertexSwapAndRemove(int removedIdx, int swappedIdx) {
		assert outEdges(removedIdx).isEmpty() && inEdges(removedIdx).isEmpty();
		for (int e : outEdges(swappedIdx))
			replaceEdgeSource(e, removedIdx);
		for (int e : inEdges(swappedIdx))
			replaceEdgeTarget(e, removedIdx);
		swapAndClear(edgesOutNum, removedIdx, swappedIdx, 0);
		swapAndClear(edgesInNum, removedIdx, swappedIdx, 0);
		super.vertexSwapAndRemove(removedIdx, swappedIdx);
	}

	@Override
	public IEdgeSet outEdges(int source) {
		return new EdgeSetOut(source);
	}

	@Override
	public IEdgeSet inEdges(int target) {
		return new EdgeSetIn(target);
	}

	@Override
	public int addEdge(int source, int target) {
		int e = super.addEdge(source, target);
		edges.data[source].data[target] = e;
		edgesOutNum[source]++;
		edgesInNum[target]++;
		return e;
	}

	@Override
	void removeEdgeLast(int edge) {
		int u = edgeSource(edge), v = edgeTarget(edge);
		edges.data[u].data[v] = EdgeNone;
		edgesOutNum[u]--;
		edgesInNum[v]--;
		super.removeEdgeLast(edge);
	}

	@Override
	void edgeSwapAndRemove(int removedIdx, int swappedIdx) {
		int ur = edgeSource(removedIdx), vr = edgeTarget(removedIdx);
		int us = edgeSource(swappedIdx), vs = edgeTarget(swappedIdx);
		edges.data[ur].data[vr] = EdgeNone;
		edges.data[us].data[vs] = removedIdx;
		edgesOutNum[ur]--;
		edgesInNum[vr]--;
		super.edgeSwapAndRemove(removedIdx, swappedIdx);
	}

	@Override
	public void clearEdges() {
		final int m = edges().size();
		for (int e = 0; e < m; e++) {
			int u = edgeSource(e), v = edgeTarget(e);
			edges.data[u].data[v] = EdgeNone;
		}
		edgesOutNumContainer.clear();
		edgesInNumContainer.clear();
		super.clearEdges();
	}

	@Override
	public void reverseEdge(int edge) {
		int u = edgeSource(edge), v = edgeTarget(edge);
		if (edges.data[v].data[u] != EdgeNone && u != v)
			throw new IllegalArgumentException("parallel edges are not supported");
		edges.data[u].data[v] = EdgeNone;
		edges.data[v].data[u] = edge;
		edgesOutNum[u]--;
		edgesInNum[v]--;
		edgesOutNum[v]++;
		edgesInNum[u]++;
		super.reverseEdge0(edge);
	}

	private class EdgeSetOut extends IntGraphBase.EdgeSetOutDirected {
		EdgeSetOut(int source) {
			super(source);
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIterOut(source);
		}

		@Override
		public int size() {
			return edgesOutNum[source];
		}
	}

	private class EdgeSetIn extends IntGraphBase.EdgeSetInDirected {
		EdgeSetIn(int target) {
			super(target);
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIterInDirected(target);
		}

		@Override
		public int size() {
			return edgesInNum[target];
		}
	}

}
