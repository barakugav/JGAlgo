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
 * An undirected graph implementation using linked lists to store edge lists.
 *
 * <p>
 * The edges of each vertex will be stored as a linked list. This implementation is efficient in use cases where
 * multiple vertices/edges removals are performed, but it should not be the default choice for an undirected graph.
 *
 * @see    GraphLinkedPtrDirected
 * @see    GraphArrayUndirected
 * @author Barak Ugav
 */
class GraphLinkedPtrUndirected extends GraphLinkedPtrAbstract {

	private Edge[] edges;
	private int[] edgesNum;
	private final DataContainer.Obj<Edge> edgesContainer;
	private final DataContainer.Int edgesNumContainer;

	private static final Edge[] EmptyEdgeArr = new Edge[0];

	private static final GraphBaseMutable.Capabilities Capabilities =
			GraphBaseMutable.Capabilities.of(false, true, true);

	/**
	 * Create a new graph with no vertices and edges, with expected number of vertices and edges.
	 *
	 * @param expectedVerticesNum the expected number of vertices that will be in the graph
	 * @param expectedEdgesNum    the expected number of edges that will be in the graph
	 */
	GraphLinkedPtrUndirected(int expectedVerticesNum, int expectedEdgesNum) {
		super(Capabilities, expectedVerticesNum, expectedEdgesNum);
		edgesContainer = new DataContainer.Obj<>(vertices, null, EmptyEdgeArr, newArr -> edges = newArr);
		edgesNumContainer = new DataContainer.Int(vertices, 0, newArr -> edgesNum = newArr);
		addInternalVerticesContainer(edgesContainer);
		addInternalVerticesContainer(edgesNumContainer);

	}

	GraphLinkedPtrUndirected(IndexGraph g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		super(Capabilities, g, copyVerticesWeights, copyEdgesWeights);
		edgesContainer = new DataContainer.Obj<>(vertices, null, EmptyEdgeArr, newArr -> edges = newArr);
		edgesNumContainer = new DataContainer.Int(vertices, 0, newArr -> edgesNum = newArr);
		addInternalVerticesContainer(edgesContainer);
		addInternalVerticesContainer(edgesNumContainer);

		final int m = g.edges().size();
		for (int e = 0; e < m; e++)
			addEdgeToLists(getEdge(e));
	}

	GraphLinkedPtrUndirected(IndexGraphBuilderImpl.Undirected builder) {
		super(Capabilities, builder);

		edgesContainer = new DataContainer.Obj<>(vertices, null, EmptyEdgeArr, newArr -> edges = newArr);
		edgesNumContainer = new DataContainer.Int(vertices, 0, newArr -> edgesNum = newArr);
		addInternalVerticesContainer(edgesContainer);
		addInternalVerticesContainer(edgesNumContainer);

		final int m = builder.edges().size();
		for (int e = 0; e < m; e++)
			addEdgeToLists(getEdge(e));
	}

	@Override
	void removeVertexLast(int vertex) {
		edges[vertex] = null;
		super.removeVertexLast(vertex);
	}

	@Override
	void vertexSwapAndRemove(int removedIdx, int swappedIdx) {
		assert edgesNum[removedIdx] == 0;

		for (Edge p = edges[swappedIdx], next; p != null; p = next) {
			next = next(p, swappedIdx);
			if (edgeSource(p.id) == swappedIdx)
				replaceEdgeSource(p.id, removedIdx);
			if (edgeTarget(p.id) == swappedIdx)
				replaceEdgeTarget(p.id, removedIdx);
		}

		swapAndClear(edges, removedIdx, swappedIdx, null);
		swapAndClear(edgesNum, removedIdx, swappedIdx, 0);

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
		int source = edgeSource(e.id), target = edgeTarget(e.id);
		Edge next;
		if ((next = edges[source]) != null) {
			setNext(e, source, next);
			setPrev(next, source, e);
		}
		edges[source] = e;
		edgesNum[source]++;
		if (source != target) {
			if ((next = edges[target]) != null) {
				setNext(e, target, next);
				setPrev(next, target, e);
			}
			edges[target] = e;
			edgesNum[target]++;
		}
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
		int u = edgeSource(e.id), v = edgeTarget(e.id);
		removeEdgePointers(e, u);
		if (u != v)
			removeEdgePointers(e, v);
		super.removeEdgeLast(edge);
	}

	@Override
	void edgeSwapAndRemove(int removedIdx, int swappedIdx) {
		Edge e = getEdge(removedIdx);
		int u = edgeSource(e.id), v = edgeTarget(e.id);
		removeEdgePointers(e, u);
		if (u != v)
			removeEdgePointers(e, v);
		super.edgeSwapAndRemove(removedIdx, swappedIdx);
	}

	private void removeEdgePointers(Edge e, int w) {
		Edge next = next(e, w), prev = prev(e, w);
		if (prev == null) {
			edges[w] = next;
		} else {
			setNext(prev, w, next);
			setPrev(e, w, null);
		}
		if (next != null) {
			setPrev(next, w, prev);
			setNext(e, w, null);
		}
		edgesNum[w]--;
	}

	@Override
	public void removeEdgesOf(int source) {
		checkVertex(source);
		for (Edge p = edges[source], next; p != null; p = next) {
			// update u list
			next = next(p, source);
			setNext(p, source, null);
			setPrev(p, source, null);

			// update v list
			if (edgeSource(p.id) != edgeTarget(p.id)) {
				int target = edgeEndpoint(p.id, source);
				removeEdgePointers(p, target);
			}

			if (p.id == edges().size() - 1) {
				super.removeEdgeLast(p.id);
			} else {
				super.edgeSwapAndRemove(p.id, edges().size() - 1);
			}
		}
		edges[source] = null;
		edgesNum[source] = 0;
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
	public void clearEdges() {
		for (int m = edges().size(), e = 0; e < m; e++) {
			Edge p = getEdge(e);
			p.nextu = p.nextv = p.prevu = p.prevv = null;
		}
		edgesContainer.clear();
		edgesNumContainer.clear();
		super.clearEdges();
	}

	private Edge next(Edge e, int w) {
		int source = edgeSource(e.id), target = edgeTarget(e.id);
		if (w == source) {
			return e.nextu;
		} else {
			assert w == target;
			return e.nextv;
		}
	}

	private Edge setNext(Edge e, int w, Edge n) {
		int source = edgeSource(e.id), target = edgeTarget(e.id);
		if (w == source) {
			e.nextu = n;
		} else {
			assert w == target;
			e.nextv = n;
		}
		return n;
	}

	private Edge prev(Edge e, int w) {
		int source = edgeSource(e.id), target = edgeTarget(e.id);
		if (w == source) {
			return e.prevu;
		} else {
			assert w == target;
			return e.prevv;
		}
	}

	private Edge setPrev(Edge e, int w, Edge n) {
		int source = edgeSource(e.id), target = edgeTarget(e.id);
		if (w == source) {
			e.prevu = n;
		} else {
			assert w == target;
			e.prevv = n;
		}
		return n;
	}

	private static class Edge extends GraphLinkedPtrAbstract.Edge {

		private Edge nextu, nextv;
		private Edge prevu, prevv;

		Edge(int id) {
			super(id);
		}
	}

	private class EdgeSetOut extends IndexGraphBase.EdgeSetOutUndirected {
		EdgeSetOut(int source) {
			super(source);
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIterOut(source, edges[source]);
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
			return new EdgeIterIn(target, edges[target]);
		}

		@Override
		public int size() {
			return edgesNum[target];
		}
	}

	private class EdgeIterOut extends GraphLinkedPtrAbstract.EdgeItr {

		private final int source;

		EdgeIterOut(int source, Edge p) {
			super(p);
			this.source = source;
		}

		@Override
		Edge nextEdge(GraphLinkedPtrAbstract.Edge n) {
			return GraphLinkedPtrUndirected.this.next((Edge) n, source);
		}

		@Override
		public int sourceInt() {
			return source;
		}

		@Override
		public int targetInt() {
			int u0 = edgeSource(last.id), v0 = edgeTarget(last.id);
			return source == u0 ? v0 : u0;
		}
	}

	private class EdgeIterIn extends GraphLinkedPtrAbstract.EdgeItr {

		private final int target;

		EdgeIterIn(int target, Edge p) {
			super(p);
			this.target = target;
		}

		@Override
		Edge nextEdge(GraphLinkedPtrAbstract.Edge n) {
			return GraphLinkedPtrUndirected.this.next((Edge) n, target);
		}

		@Override
		public int sourceInt() {
			int u0 = edgeSource(last.id), v0 = edgeTarget(last.id);
			return target == u0 ? v0 : u0;
		}

		@Override
		public int targetInt() {
			return target;
		}
	}
}
