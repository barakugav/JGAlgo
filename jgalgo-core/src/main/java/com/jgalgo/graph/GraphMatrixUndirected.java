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
 *
 * <p>
 * If the graph contains \(n\) vertices, matrix of size {@code [n][n]} stores the edges of the graph. The implementation
 * does not support multiple edges with identical source and target.
 *
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

	GraphMatrixUndirected(IndexGraph g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		super(Capabilities, g, copyVerticesWeights, copyEdgesWeights);
		assert !g.isDirected();
		if (g instanceof GraphMatrixUndirected) {
			GraphMatrixUndirected g0 = (GraphMatrixUndirected) g;
			edgesNumContainer = g0.edgesNumContainer.copy(vertices, newArr -> edgesNum = newArr);
			addInternalEdgesContainer(edgesNumContainer);
		} else {
			edgesNumContainer = new DataContainer.Int(vertices, 0, newArr -> edgesNum = newArr);
			addInternalVerticesContainer(edgesNumContainer);
			for (int n = g.vertices().size(), u = 0; u < n; u++)
				edgesNum[u] = g.outEdges(u).size();
		}
	}

	GraphMatrixUndirected(IndexGraphBuilderImpl.Undirected builder) {
		super(Capabilities, builder);
		edgesNumContainer = new DataContainer.Int(vertices, 0, newArr -> edgesNum = newArr);
		addInternalVerticesContainer(edgesNumContainer);

		for (int m = builder.edges().size(), e = 0; e < m; e++) {
			int u = builder.edgeSource(e), v = builder.edgeTarget(e);
			edgesNum[u]++;
			if (u != v)
				edgesNum[v]++;
		}
	}

	@Override
	void vertexSwapAndRemove(int removedIdx, int swappedIdx) {
		assert outEdges(removedIdx).isEmpty() && inEdges(removedIdx).isEmpty();
		for (int e : outEdges(swappedIdx))
			replaceEdgeEndpoint(e, swappedIdx, removedIdx);
		swapAndClear(edgesNum, removedIdx, swappedIdx, 0);
		super.vertexSwapAndRemove(removedIdx, swappedIdx);
	}

	@Override
	public int addEdge(int source, int target) {
		int e = super.addEdge(source, target);
		edges[source].data[target] = e;
		edgesNum[source]++;
		if (source != target) {
			edges[target].data[source] = e;
			edgesNum[target]++;
		}
		return e;
	}

	@Override
	void removeEdgeLast(int edge) {
		int u = edgeSource(edge), v = edgeTarget(edge);
		edges[u].data[v] = EdgeNone;
		edgesNum[u]--;
		if (u != v) {
			edges[v].data[u] = EdgeNone;
			edgesNum[v]--;
		}
		super.removeEdgeLast(edge);
	}

	@Override
	void edgeSwapAndRemove(int removedIdx, int swappedIdx) {
		int ur = edgeSource(removedIdx), vr = edgeTarget(removedIdx);
		int us = edgeSource(swappedIdx), vs = edgeTarget(swappedIdx);
		edges[ur].data[vr] = EdgeNone;
		edgesNum[ur]--;
		if (ur != vr) {
			edges[vr].data[ur] = EdgeNone;
			edgesNum[vr]--;
		}
		edges[us].data[vs] = removedIdx;
		edges[vs].data[us] = removedIdx;
		super.edgeSwapAndRemove(removedIdx, swappedIdx);
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
			edges[u].data[v] = EdgeNone;
			edges[v].data[u] = EdgeNone;
		}
		edgesNumContainer.clear();
		super.clearEdges();
	}

	private class EdgeSetOut extends IndexGraphBase.EdgeSetOutUndirected {
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

	private class EdgeSetIn extends IndexGraphBase.EdgeSetInUndirected {
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
