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
 * A directed graph implementation using linked lists to store edge lists.
 *
 * <p>
 * The edges of each vertex will be stored as a linked list. This implementation is efficient in use cases where
 * multiple vertices/edges removals are performed, but it should not be the default choice for a directed graph.
 *
 * @see    GraphLinkedUndirected
 * @see    GraphArrayDirected
 * @author Barak Ugav
 */
class GraphLinkedDirected extends GraphLinkedAbstract {

	private Edge[] edgesOut;
	private Edge[] edgesIn;
	private int[] edgesOutNum;
	private int[] edgesInNum;
	private final DataContainer.Obj<Edge> edgesOutContainer;
	private final DataContainer.Obj<Edge> edgesInContainer;
	private final DataContainer.Int edgesOutNumContainer;
	private final DataContainer.Int edgesInNumContainer;

	private static final Edge[] EmptyEdgeArr = new Edge[0];

	private static final IndexGraphBase.Capabilities Capabilities = IndexGraphBase.Capabilities.of(true, true, true);

	/**
	 * Create a new graph with no vertices and edges, with expected number of vertices and edges.
	 *
	 * @param expectedVerticesNum the expected number of vertices that will be in the graph
	 * @param expectedEdgesNum    the expected number of edges that will be in the graph
	 */
	GraphLinkedDirected(int expectedVerticesNum, int expectedEdgesNum) {
		super(Capabilities, expectedVerticesNum, expectedEdgesNum);

		edgesOutContainer = new DataContainer.Obj<>(vertices, null, EmptyEdgeArr, newArr -> edgesIn = newArr);
		edgesInContainer = new DataContainer.Obj<>(vertices, null, EmptyEdgeArr, newArr -> edgesOut = newArr);
		edgesOutNumContainer = new DataContainer.Int(vertices, 0, newArr -> edgesOutNum = newArr);
		edgesInNumContainer = new DataContainer.Int(vertices, 0, newArr -> edgesInNum = newArr);
		addInternalVerticesContainer(edgesOutContainer);
		addInternalVerticesContainer(edgesInContainer);
		addInternalVerticesContainer(edgesOutNumContainer);
		addInternalVerticesContainer(edgesInNumContainer);
	}

	GraphLinkedDirected(IndexGraph g, boolean copyWeights) {
		super(Capabilities, g, copyWeights);

		edgesOutContainer = new DataContainer.Obj<>(vertices, null, EmptyEdgeArr, newArr -> edgesIn = newArr);
		edgesInContainer = new DataContainer.Obj<>(vertices, null, EmptyEdgeArr, newArr -> edgesOut = newArr);
		edgesOutNumContainer = new DataContainer.Int(vertices, 0, newArr -> edgesOutNum = newArr);
		edgesInNumContainer = new DataContainer.Int(vertices, 0, newArr -> edgesInNum = newArr);
		addInternalVerticesContainer(edgesOutContainer);
		addInternalVerticesContainer(edgesInContainer);
		addInternalVerticesContainer(edgesOutNumContainer);
		addInternalVerticesContainer(edgesInNumContainer);

		final int m = g.edges().size();
		for (int e = 0; e < m; e++)
			addEdgeToLists(getEdge(e));
	}

	GraphLinkedDirected(IndexGraphBuilderImpl.Directed builder) {
		super(Capabilities, builder);

		edgesOutContainer = new DataContainer.Obj<>(vertices, null, EmptyEdgeArr, newArr -> edgesIn = newArr);
		edgesInContainer = new DataContainer.Obj<>(vertices, null, EmptyEdgeArr, newArr -> edgesOut = newArr);
		edgesOutNumContainer = new DataContainer.Int(vertices, 0, newArr -> edgesOutNum = newArr);
		edgesInNumContainer = new DataContainer.Int(vertices, 0, newArr -> edgesInNum = newArr);
		addInternalVerticesContainer(edgesOutContainer);
		addInternalVerticesContainer(edgesInContainer);
		addInternalVerticesContainer(edgesOutNumContainer);
		addInternalVerticesContainer(edgesInNumContainer);

		final int m = builder.edges().size();
		for (int e = 0; e < m; e++)
			addEdgeToLists(getEdge(e));
	}

	@Override
	void removeVertexLast(int vertex) {
		edgesOutContainer.clear(edgesOut, vertex);
		edgesInContainer.clear(edgesIn, vertex);
		super.removeVertexLast(vertex);
	}

	@Override
	void vertexSwapAndRemove(int removedIdx, int swappedIdx) {
		assert edgesOutNum[removedIdx] == 0 && edgesInNum[removedIdx] == 0;

		for (Edge p = edgesOut[swappedIdx]; p != null; p = p.nextOut)
			p.source = removedIdx;
		for (Edge p = edgesIn[swappedIdx]; p != null; p = p.nextIn)
			p.target = removedIdx;

		edgesOutContainer.swapAndClear(removedIdx, swappedIdx);
		edgesInContainer.swapAndClear(removedIdx, swappedIdx);
		edgesOutNumContainer.swapAndClear(removedIdx, swappedIdx);
		edgesInNumContainer.swapAndClear(removedIdx, swappedIdx);

		super.vertexSwapAndRemove(removedIdx, swappedIdx);
	}

	@Override
	public IEdgeSet outEdges(int source) {
		checkVertex(source);
		return new EdgeSetOut(source);
	}

	@Override
	public IEdgeSet inEdges(int target) {
		checkVertex(target);
		return new EdgeSetIn(target);
	}

	@Override
	public int addEdge(int source, int target) {
		Edge e = (Edge) addEdgeObj(source, target);
		addEdgeToLists(e);
		return e.id;
	}

	private void addEdgeToLists(Edge e) {
		int u = e.source, v = e.target;
		Edge next;
		next = edgesOut[u];
		if (next != null) {
			next.prevOut = e;
			e.nextOut = next;
		}
		edgesOut[u] = e;
		next = edgesIn[v];
		if (next != null) {
			next.prevIn = e;
			e.nextIn = next;
		}
		edgesIn[v] = e;
		edgesOutNum[u]++;
		edgesInNum[v]++;
	}

	@Override
	Edge allocEdge(int id, int source, int target) {
		return new Edge(id, source, target);
	}

	@Override
	Edge getEdge(int edge) {
		return (Edge) super.getEdge(edge);
	}

	@Override
	void removeEdgeLast(int edge) {
		Edge e = getEdge(edge);
		removeEdgeOutPointers(e);
		removeEdgeInPointers(e);
		super.removeEdgeLast(edge);
	}

	@Override
	void edgeSwapAndRemove(int removedIdx, int swappedIdx) {
		Edge removed = getEdge(removedIdx);
		removeEdgeOutPointers(removed);
		removeEdgeInPointers(removed);
		super.edgeSwapAndRemove(removedIdx, swappedIdx);
	}

	@Override
	public void removeOutEdgesOf(int source) {
		checkVertex(source);
		for (Edge p = edgesOut[source], next; p != null; p = next) {
			next = p.nextOut;
			p.nextOut = p.prevOut = null;
			removeEdgeInPointers(p);
			if (p.id == edges().size() - 1) {
				super.removeEdgeLast(p.id);
			} else {
				super.edgeSwapAndRemove(p.id, edges().size() - 1);
			}
		}
		edgesOut[source] = null;
		edgesOutNum[source] = 0;
	}

	@Override
	public void removeInEdgesOf(int target) {
		checkVertex(target);
		for (Edge p = edgesIn[target], next; p != null; p = next) {
			next = p.nextIn;
			p.nextIn = p.prevIn = null;
			removeEdgeOutPointers(p);
			if (p.id == edges().size() - 1) {
				super.removeEdgeLast(p.id);
			} else {
				super.edgeSwapAndRemove(p.id, edges().size() - 1);
			}
		}
		edgesIn[target] = null;
		edgesInNum[target] = 0;
	}

	private void removeEdgeOutPointers(Edge e) {
		Edge next = e.nextOut, prev = e.prevOut;
		if (prev == null) {
			edgesOut[e.source] = next;
		} else {
			prev.nextOut = next;
			e.prevOut = null;
		}
		if (next != null) {
			next.prevOut = prev;
			e.nextOut = null;
		}
		edgesOutNum[e.source]--;
	}

	private void removeEdgeInPointers(Edge e) {
		Edge next = e.nextIn, prev = e.prevIn;
		if (prev == null) {
			edgesIn[e.target] = next;
		} else {
			prev.nextIn = next;
			e.prevIn = null;
		}
		if (next != null) {
			next.prevIn = prev;
			e.nextIn = null;
		}
		edgesInNum[e.target]--;
	}

	@Override
	public void reverseEdge(int edge) {
		Edge n = getEdge(edge);
		if (n.source == n.target)
			return;
		removeEdgeOutPointers(n);
		removeEdgeInPointers(n);
		int w = n.source;
		n.source = n.target;
		n.target = w;
		addEdgeToLists(n);
	}

	@Override
	public void clearEdges() {
		for (int m = edges().size(), e = 0; e < m; e++) {
			Edge p = getEdge(e);
			p.nextOut = p.prevOut = p.nextIn = p.prevIn = null;
		}
		edgesOutContainer.clear(edgesOut);
		edgesInContainer.clear(edgesIn);
		edgesOutNumContainer.clear(edgesOutNum);
		edgesInNumContainer.clear(edgesInNum);
		super.clearEdges();
	}

	private class EdgeSetOut extends IntGraphBase.EdgeSetOutDirected {
		EdgeSetOut(int source) {
			super(source);
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIterOut(edgesOut[source]);
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
			return new EdgeIterIn(edgesIn[target]);
		}

		@Override
		public int size() {
			return edgesInNum[target];
		}
	}

	private abstract class EdgeIterImpl extends GraphLinkedAbstract.EdgeItr {
		EdgeIterImpl(Edge p) {
			super(p);
		}

		@Override
		public int sourceInt() {
			return last.source;
		}

		@Override
		public int targetInt() {
			return last.target;
		}
	}

	private class EdgeIterOut extends EdgeIterImpl {
		EdgeIterOut(Edge p) {
			super(p);
		}

		@Override
		Edge nextEdge(GraphLinkedAbstract.Edge n) {
			return ((Edge) n).nextOut;
		}
	}

	private class EdgeIterIn extends EdgeIterImpl {
		EdgeIterIn(Edge p) {
			super(p);
		}

		@Override
		Edge nextEdge(GraphLinkedAbstract.Edge n) {
			return ((Edge) n).nextIn;
		}
	}

	private static class Edge extends GraphLinkedAbstract.Edge {

		private Edge nextOut;
		private Edge nextIn;
		private Edge prevOut;
		private Edge prevIn;

		Edge(int id, int source, int target) {
			super(id, source, target);
		}
	}

}
