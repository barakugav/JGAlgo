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
		edges.get(source).set(target, e);
		edgesOutNum[source]++;
		edgesInNum[target]++;
		return e;
	}

	@Override
	void removeEdgeImpl(int edge) {
		int u = edgeSource(edge), v = edgeTarget(edge);
		edges.get(u).set(v, EdgeNone);
		edgesOutNum[u]--;
		edgesInNum[v]--;
		super.removeEdgeImpl(edge);
	}

	@Override
	void edgeSwap(int e1, int e2) {
		int u1 = edgeSource(e1), v1 = edgeTarget(e1);
		int u2 = edgeSource(e2), v2 = edgeTarget(e2);
		edges.get(u1).set(v1, e2);
		edges.get(u2).set(v2, e1);
		super.edgeSwap(e1, e2);
	}

	@Override
	public void clearEdges() {
		final int m = edges().size();
		for (int e = 0; e < m; e++) {
			int u = edgeSource(e), v = edgeTarget(e);
			edges.get(u).set(v, EdgeNone);
		}
		edgesOutNumContainer.clear(edgesOutNum);
		edgesInNumContainer.clear(edgesInNum);
		super.clearEdges();
	}

	@Override
	public void reverseEdge(int edge) {
		int u = edgeSource(edge), v = edgeTarget(edge);
		if (edges.get(v).get(u) != EdgeNone && u != v)
			throw new IllegalArgumentException("parallel edges are not supported");
		edges.get(u).set(v, EdgeNone);
		edges.get(v).set(u, edge);
		edgesOutNum[u]--;
		edgesInNum[v]--;
		edgesOutNum[v]++;
		edgesInNum[u]++;
		super.reverseEdge0(edge);
	}

	@Override
	void vertexSwap(int v1, int v2) {
		for (int e : outEdges(v1))
			replaceEdgeSource(e, v2);
		for (int e : outEdges(v2))
			replaceEdgeSource(e, v1);
		for (int e : inEdges(v1))
			replaceEdgeTarget(e, v2);
		for (int e : inEdges(v2))
			replaceEdgeTarget(e, v1);
		edgesOutNumContainer.swap(edgesOutNum, v1, v2);
		edgesInNumContainer.swap(edgesInNum, v1, v2);
		super.vertexSwap(v1, v2);
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
