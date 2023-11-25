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
 * @see    GraphLinkedPtrUndirected
 * @see    GraphArrayDirected
 * @author Barak Ugav
 */
class GraphLinkedPtrDirected extends GraphLinkedPtrAbstract {

	private Edge[] edgesOut;
	private Edge[] edgesIn;
	private int[] edgesOutNum;
	private int[] edgesInNum;
	private final DataContainer.Obj<Edge> edgesOutContainer;
	private final DataContainer.Obj<Edge> edgesInContainer;
	private final DataContainer.Int edgesOutNumContainer;
	private final DataContainer.Int edgesInNumContainer;

	private static final Edge[] EmptyEdgeArr = new Edge[0];

	private static final GraphBaseMutable.Capabilities CapabilitiesNoSelfEdges =
			GraphBaseMutable.Capabilities.of(true, false, true);
	private static final GraphBaseMutable.Capabilities CapabilitiesWithSelfEdges =
			GraphBaseMutable.Capabilities.of(true, true, true);

	private static GraphBaseMutable.Capabilities capabilities(boolean selfEdges) {
		return selfEdges ? CapabilitiesWithSelfEdges : CapabilitiesNoSelfEdges;
	}

	GraphLinkedPtrDirected(boolean selfEdges, int expectedVerticesNum, int expectedEdgesNum) {
		super(capabilities(selfEdges), expectedVerticesNum, expectedEdgesNum);

		edgesOutContainer = newVerticesContainer(null, EmptyEdgeArr, newArr -> edgesIn = newArr);
		edgesInContainer = newVerticesContainer(null, EmptyEdgeArr, newArr -> edgesOut = newArr);
		edgesOutNumContainer = newVerticesIntContainer(0, newArr -> edgesOutNum = newArr);
		edgesInNumContainer = newVerticesIntContainer(0, newArr -> edgesInNum = newArr);
	}

	GraphLinkedPtrDirected(boolean selfEdges, IndexGraph g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		super(capabilities(selfEdges), g, copyVerticesWeights, copyEdgesWeights);

		edgesOutContainer = newVerticesContainer(null, EmptyEdgeArr, newArr -> edgesIn = newArr);
		edgesInContainer = newVerticesContainer(null, EmptyEdgeArr, newArr -> edgesOut = newArr);
		edgesOutNumContainer = newVerticesIntContainer(0, newArr -> edgesOutNum = newArr);
		edgesInNumContainer = newVerticesIntContainer(0, newArr -> edgesInNum = newArr);

		final int m = g.edges().size();
		for (int e = 0; e < m; e++)
			addEdgeToLists(getEdge(e));
	}

	GraphLinkedPtrDirected(boolean selfEdges, IndexGraphBuilderImpl builder) {
		super(capabilities(selfEdges), builder);
		assert builder.isDirected();

		edgesOutContainer = newVerticesContainer(null, EmptyEdgeArr, newArr -> edgesIn = newArr);
		edgesInContainer = newVerticesContainer(null, EmptyEdgeArr, newArr -> edgesOut = newArr);
		edgesOutNumContainer = newVerticesIntContainer(0, newArr -> edgesOutNum = newArr);
		edgesInNumContainer = newVerticesIntContainer(0, newArr -> edgesInNum = newArr);

		final int m = builder.edges().size();
		for (int e = 0; e < m; e++)
			addEdgeToLists(getEdge(e));
	}

	@Override
	void removeVertexLast(int vertex) {
		edgesOut[vertex] = null;
		edgesIn[vertex] = null;
		super.removeVertexLast(vertex);
	}

	@Override
	void vertexSwapAndRemove(int removedIdx, int swappedIdx) {
		assert edgesOutNum[removedIdx] == 0 && edgesInNum[removedIdx] == 0;

		for (Edge p = edgesOut[swappedIdx]; p != null; p = p.nextOut)
			replaceEdgeSource(p.id, removedIdx);
		for (Edge p = edgesIn[swappedIdx]; p != null; p = p.nextIn)
			replaceEdgeTarget(p.id, removedIdx);

		swapAndClear(edgesOut, removedIdx, swappedIdx, null);
		swapAndClear(edgesIn, removedIdx, swappedIdx, null);
		swapAndClear(edgesOutNum, removedIdx, swappedIdx, 0);
		swapAndClear(edgesInNum, removedIdx, swappedIdx, 0);

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
		int u = source(e.id), v = target(e.id);
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
	Edge allocEdge(int id) {
		return new Edge(id);
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
		int source = source(e.id);
		if (prev == null) {
			edgesOut[source] = next;
		} else {
			prev.nextOut = next;
			e.prevOut = null;
		}
		if (next != null) {
			next.prevOut = prev;
			e.nextOut = null;
		}
		edgesOutNum[source]--;
	}

	private void removeEdgeInPointers(Edge e) {
		Edge next = e.nextIn, prev = e.prevIn;
		int target = target(e.id);
		if (prev == null) {
			edgesIn[target] = next;
		} else {
			prev.nextIn = next;
			e.prevIn = null;
		}
		if (next != null) {
			next.prevIn = prev;
			e.nextIn = null;
		}
		edgesInNum[target]--;
	}

	@Override
	public void reverseEdge(int edge) {
		checkEdge(edge);
		Edge n = getEdge(edge);
		int source = source(edge), target = target(edge);
		if (source == target)
			return;
		removeEdgeOutPointers(n);
		removeEdgeInPointers(n);
		reverseEdge0(edge);
		addEdgeToLists(n);
	}

	@Override
	public void clearEdges() {
		for (int m = edges().size(), e = 0; e < m; e++) {
			Edge p = getEdge(e);
			p.nextOut = p.prevOut = p.nextIn = p.prevIn = null;
		}
		edgesOutContainer.clear();
		edgesInContainer.clear();
		edgesOutNumContainer.clear();
		edgesInNumContainer.clear();
		super.clearEdges();
	}

	private class EdgeSetOut extends IndexGraphBase.EdgeSetOutDirected {
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

	private class EdgeSetIn extends IndexGraphBase.EdgeSetInDirected {
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

	private abstract class EdgeIterImpl extends GraphLinkedPtrAbstract.EdgeItr {
		EdgeIterImpl(Edge p) {
			super(p);
		}

		@Override
		public int sourceInt() {
			return GraphLinkedPtrDirected.this.source(last.id);
		}

		@Override
		public int targetInt() {
			return GraphLinkedPtrDirected.this.target(last.id);
		}
	}

	private class EdgeIterOut extends EdgeIterImpl {
		EdgeIterOut(Edge p) {
			super(p);
		}

		@Override
		Edge nextEdge(GraphLinkedPtrAbstract.Edge n) {
			return ((Edge) n).nextOut;
		}
	}

	private class EdgeIterIn extends EdgeIterImpl {
		EdgeIterIn(Edge p) {
			super(p);
		}

		@Override
		Edge nextEdge(GraphLinkedPtrAbstract.Edge n) {
			return ((Edge) n).nextIn;
		}
	}

	private static class Edge extends GraphLinkedPtrAbstract.Edge {

		private Edge nextOut;
		private Edge nextIn;
		private Edge prevOut;
		private Edge prevIn;

		Edge(int id) {
			super(id);
		}
	}

}
