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

import com.jgalgo.graph.Graphs.GraphCapabilitiesBuilder;

/**
 * An undirected graph implementation using linked lists to store edge lists.
 * <p>
 * The edges of each vertex will be stored as a linked list. This implementation is efficient in use cases where
 * multiple vertices/edges removals are performed, but it should not be the default choice for an undirected graph.
 *
 * @see    GraphLinkedDirected
 * @see    GraphArrayUndirected
 * @author Barak Ugav
 */
class GraphLinkedUndirected extends GraphLinkedAbstract {

	private Node[] edges;
	private final DataContainer.Obj<Node> edgesContainer;

	private static final Node[] EmptyNodeArr = new Node[0];

	/**
	 * Create a new graph with no vertices and edges, with expected number of vertices and edges.
	 *
	 * @param expectedVerticesNum the expected number of vertices that will be in the graph
	 * @param expectedEdgesNum    the expected number of edges that will be in the graph
	 */
	GraphLinkedUndirected(int expectedVerticesNum, int expectedEdgesNum) {
		super(expectedVerticesNum, expectedEdgesNum);
		edgesContainer = new DataContainer.Obj<>(vertices, null, EmptyNodeArr, newArr -> edges = newArr);
		addInternalVerticesContainer(edgesContainer);
	}

	GraphLinkedUndirected(IndexGraph g, boolean copyWeights) {
		super(g, copyWeights);

		edgesContainer = new DataContainer.Obj<>(vertices, null, EmptyNodeArr, newArr -> edges = newArr);
		addInternalVerticesContainer(edgesContainer);

		final int m = g.edges().size();
		for (int e = 0; e < m; e++)
			addEdgeToLists(getNode(e));
	}

	@Override
	void removeVertexImpl(int vertex) {
		super.removeVertexImpl(vertex);
		edgesContainer.clear(edges, vertex);
	}

	@Override
	void vertexSwap(int v1, int v2) {
		assert v1 != v2;

		final int tempV = -2;
		for (Node p = edges[v1], next; p != null; p = next) {
			next = p.next(v1);
			if (p.source == v1)
				p.source = tempV;
			if (p.target == v1)
				p.target = tempV;
		}
		for (Node p = edges[v2], next; p != null; p = next) {
			next = p.next(v2);
			if (p.source == v2)
				p.source = v1;
			if (p.target == v2)
				p.target = v1;
		}
		for (Node p = edges[v1], next; p != null; p = next) {
			next = p.next(tempV);
			if (p.source == tempV)
				p.source = v2;
			if (p.target == tempV)
				p.target = v2;
		}

		edgesContainer.swap(edges, v1, v2);

		super.vertexSwap(v1, v2);
	}

	@Override
	public EdgeSet outEdges(int source) {
		checkVertex(source);
		return new EdgeSetOut(source);
	}

	@Override
	public EdgeSet inEdges(int target) {
		checkVertex(target);
		return new EdgeSetIn(target);
	}

	@Override
	public int addEdge(int source, int target) {
		Node e = (Node) addEdgeNode(source, target);
		addEdgeToLists(e);
		return e.id;
	}

	private void addEdgeToLists(Node e) {
		int source = e.source, target = e.target;
		Node next;
		if ((next = edges[source]) != null) {
			e.nextSet(source, next);
			next.prevSet(source, e);
		}
		edges[source] = e;
		if (source != target) {
			if ((next = edges[target]) != null) {
				e.nextSet(target, next);
				next.prevSet(target, e);
			}
			edges[target] = e;
		}
	}

	@Override
	Node allocNode(int id, int source, int target) {
		return new Node(id, source, target);
	}

	@Override
	Node getNode(int edge) {
		return (Node) super.getNode(edge);
	}

	@Override
	void removeEdgeImpl(int edge) {
		Node node = getNode(edge);
		removeEdgeNodePointers(node, node.source);
		if (node.source != node.target)
			removeEdgeNodePointers(node, node.target);
		super.removeEdgeImpl(edge);
	}

	private void removeEdgeNodePointers(Node e, int w) {
		Node next = e.next(w), prev = e.prev(w);
		if (prev == null) {
			edges[w] = next;
		} else {
			prev.nextSet(w, next);
			e.prevSet(w, null);
		}
		if (next != null) {
			next.prevSet(w, prev);
			e.nextSet(w, null);
		}
	}

	@Override
	public void removeEdgesOf(int source) {
		checkVertex(source);
		for (Node p = edges[source], next; p != null; p = next) {
			// update u list
			next = p.next(source);
			p.nextSet(source, null);
			p.prevSet(source, null);

			// update v list
			if (p.source != p.target) {
				int target = p.getEndpoint(source);
				removeEdgeNodePointers(p, target);
			}

			edgeSwapBeforeRemove(p.id);
			super.removeEdgeImpl(p.id);
		}
		edges[source] = null;
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
		for (GraphLinkedAbstract.Node p0 : nodes()) {
			Node p = (Node) p0;
			p.nextu = p.nextv = p.prevu = p.prevv = null;
		}
		edgesContainer.clear(edges);
		super.clearEdges();
	}

	@Override
	public GraphCapabilities getCapabilities() {
		return Capabilities;
	}

	private static final GraphCapabilities Capabilities =
			GraphCapabilitiesBuilder.newUndirected().parallelEdges(true).selfEdges(true).build();

	private static class Node extends GraphLinkedAbstract.Node {

		private Node nextu, nextv;
		private Node prevu, prevv;

		Node(int id, int source, int target) {
			super(id, source, target);
		}

		Node next(int w) {
			assert w == source || w == target;
			return w == source ? nextu : nextv;
		}

		void nextSet(int w, Node n) {
			if (w == source) {
				nextu = n;
			} else {
				assert w == target;
				nextv = n;
			}
		}

		Node prev(int w) {
			if (w == source) {
				return prevu;
			} else {
				assert w == target;
				return prevv;
			}
		}

		void prevSet(int w, Node n) {
			if (w == source) {
				prevu = n;
			} else {
				assert w == target;
				prevv = n;
			}
		}

		int getEndpoint(int w) {
			if (w == source) {
				return target;
			} else {
				assert w == target;
				return source;
			}
		}
	}

	private class EdgeSetOut extends GraphBase.EdgeSetOutUndirected {
		EdgeSetOut(int source) {
			super(source);
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterOut(source, edges[source]);
		}
	}

	private class EdgeSetIn extends GraphBase.EdgeSetInUndirected {
		EdgeSetIn(int target) {
			super(target);
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterIn(target, edges[target]);
		}
	}

	private class EdgeIterOut extends GraphLinkedAbstract.EdgeItr {

		private final int source;

		EdgeIterOut(int source, Node p) {
			super(p);
			this.source = source;
		}

		@Override
		Node nextNode(GraphLinkedAbstract.Node n) {
			return ((Node) n).next(source);
		}

		@Override
		public int source() {
			return source;
		}

		@Override
		public int target() {
			int u0 = last.source, v0 = last.target;
			return source == u0 ? v0 : u0;
		}
	}

	private class EdgeIterIn extends GraphLinkedAbstract.EdgeItr {

		private final int target;

		EdgeIterIn(int target, Node p) {
			super(p);
			this.target = target;
		}

		@Override
		Node nextNode(GraphLinkedAbstract.Node n) {
			return ((Node) n).next(target);
		}

		@Override
		public int source() {
			int u0 = last.source, v0 = last.target;
			return target == u0 ? v0 : u0;
		}

		@Override
		public int target() {
			return target;
		}
	}
}
