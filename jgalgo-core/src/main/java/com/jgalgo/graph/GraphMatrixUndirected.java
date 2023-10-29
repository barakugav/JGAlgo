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
 * A undirected graph implementation using a two dimensional matrix to store all edges.
 * <p>
 * If the graph contains \(n\) vertices, matrix of size {@code [n][n]} stores the edges of the graph. The implementation
 * does not support multiple edges with identical source and target.
 * <p>
 * This implementation is efficient for use cases where fast lookups of edge \((u,v)\) are required, as they can be
 * answered in \(O(1)\) time, but it should not be the default choice for an undirected graph.
 *
 * @see    GraphMatrixDirected
 * @author Barak Ugav
 */
class GraphMatrixUndirected extends GraphMatrixAbstract {

	private int[] edgesNum;
	private final DataContainer.Int edgesNumContainer;
	private static final IndexGraphBase.Capabilities Capabilities = IndexGraphBase.Capabilities.of(false, true, false);

	GraphMatrixUndirected() {
		this(0, 0);
	}

	/**
	 * Create a new graph with no vertices and edges, with expected number of vertices and edges.
	 *
	 * @param expectedVerticesNum the expected number of vertices that will be in the graph
	 * @param expectedEdgesNum    the expected number of edges that will be in the graph
	 */
	GraphMatrixUndirected(int expectedVerticesNum, int expectedEdgesNum) {
		super(Capabilities, expectedVerticesNum, expectedEdgesNum);
		edgesNumContainer = new DataContainer.Int(vertices, 0, newArr -> edgesNum = newArr);
		addInternalVerticesContainer(edgesNumContainer);
	}

	GraphMatrixUndirected(IndexGraph g, boolean copyWeights) {
		super(Capabilities, g, copyWeights);
		if (g instanceof GraphMatrixUndirected) {
			GraphMatrixUndirected g0 = (GraphMatrixUndirected) g;
			edgesNumContainer = g0.edgesNumContainer.copy(vertices, newArr -> edgesNum = newArr);
			addInternalEdgesContainer(edgesNumContainer);
		} else {
			edgesNumContainer = new DataContainer.Int(vertices, 0, newArr -> edgesNum = newArr);
			addInternalVerticesContainer(edgesNumContainer);
			assert !g.isDirected();
			for (int n = g.vertices().size(), u = 0; u < n; u++)
				edgesNum[u] = g.outEdges(u).size();
		}
	}

	@Override
	public int addEdge(int source, int target) {
		int e = super.addEdge(source, target);
		edges.get(source).set(target, e);
		edgesNum[source]++;
		if (source != target) {
			edges.get(target).set(source, e);
			edgesNum[target]++;
		}
		return e;
	}

	@Override
	void removeEdgeImpl(int edge) {
		int u = edgeSource(edge), v = edgeTarget(edge);
		edges.get(u).set(v, EdgeNone);
		edgesNum[u]--;
		if (u != v) {
			edges.get(v).set(u, EdgeNone);
			edgesNum[v]--;
		}
		super.removeEdgeImpl(edge);
	}

	@Override
	public void removeEdgesOf(int source) {
		for (IEdgeIter eit = outEdges(source).iterator(); eit.hasNext();) {
			eit.nextInt();
			eit.remove();
		}
	}

	@Override
	public void removeOutEdgesOf(int source) {
		removeEdgesOf(source);
	}

	@Override
	public void removeInEdgesOf(int target) {
		removeEdgesOf(target);
	}

	@Override
	public void reverseEdge(int edge) {
		// Do nothing
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
	public void clearEdges() {
		final int m = edges().size();
		for (int e = 0; e < m; e++) {
			int u = edgeSource(e), v = edgeTarget(e);
			edges.get(u).set(v, EdgeNone);
			edges.get(v).set(u, EdgeNone);
		}
		edgesNumContainer.clear(edgesNum);
		super.clearEdges();
	}

	@Override
	void edgeSwap(int e1, int e2) {
		int u1 = edgeSource(e1), v1 = edgeTarget(e1);
		int u2 = edgeSource(e2), v2 = edgeTarget(e2);
		edges.get(u1).set(v1, e2);
		edges.get(v1).set(u1, e2);
		edges.get(u2).set(v2, e1);
		edges.get(v2).set(u2, e1);
		super.edgeSwap(e1, e2);
	}

	@Override
	void vertexSwap(int v1, int v2) {
		final int tempV = -2;
		for (int e : outEdges(v1))
			replaceEdgeEndpoint(e, v1, tempV);
		for (int e : outEdges(v2))
			replaceEdgeEndpoint(e, v2, v1);
		for (int e : outEdges(v1))
			replaceEdgeEndpoint(e, tempV, v2);
		edgesNumContainer.swap(edgesNum, v1, v2);
		super.vertexSwap(v1, v2);
	}

	private class EdgeSetOut extends IntGraphBase.EdgeSetOutUndirected {
		EdgeSetOut(int source) {
			super(source);
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIterOut(source);
		}

		@Override
		public int size() {
			return edgesNum[source];
		}
	}

	private class EdgeSetIn extends IntGraphBase.EdgeSetInUndirected {
		EdgeSetIn(int target) {
			super(target);
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIterInUndirected(target);
		}

		@Override
		public int size() {
			return edgesNum[target];
		}
	}

}
