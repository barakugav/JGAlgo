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

package com.jgalgo;

/**
 * A directed graph implementation using linked lists to store edge lists.
 * <p>
 * The edges of each vertex will be stored as a linked list. This implementation is efficient in use cases where
 * multiple vertices/edges removals are performed, but it should not be the default choice for a directed graph.
 *
 * @see    GraphLinkedUndirected
 * @see    GraphArrayDirected
 * @author Barak Ugav
 */
class GraphLinkedDirected extends GraphLinkedAbstract {

	private final DataContainer.Obj<Node> edgesIn;
	private final DataContainer.Obj<Node> edgesOut;

	/**
	 * Create a new graph with no vertices and edges.
	 */
	GraphLinkedDirected() {
		this(0);
	}

	/**
	 * Create a new graph with no edges and {@code n} vertices numbered {@code 0,1,2,..,n-1}.
	 *
	 * @param n the number of initial vertices number
	 */
	GraphLinkedDirected(int n) {
		super(n, Capabilities);

		edgesIn = new DataContainer.Obj<>(verticesIDStrategy, null, Node.class);
		edgesOut = new DataContainer.Obj<>(verticesIDStrategy, null, Node.class);

		addInternalVerticesDataContainer(edgesIn);
		addInternalVerticesDataContainer(edgesOut);
	}

	@Override
	public void removeVertex(int vertex) {
		vertex = vertexSwapBeforeRemove(vertex);
		super.removeVertex(vertex);
		edgesOut.clear(vertex);
		edgesIn.clear(vertex);
	}

	@Override
	void vertexSwap(int v1, int v2) {
		for (Node p = edgesOut.get(v1); p != null; p = p.nextOut)
			p.source = v2;
		for (Node p = edgesIn.get(v1); p != null; p = p.nextIn)
			p.v = v2;
		for (Node p = edgesOut.get(v2); p != null; p = p.nextOut)
			p.source = v1;
		for (Node p = edgesIn.get(v2); p != null; p = p.nextIn)
			p.v = v1;

		edgesOut.swap(v1, v2);
		edgesIn.swap(v1, v2);

		super.vertexSwap(v1, v2);
	}

	@Override
	public EdgeIter edgesOut(int source) {
		checkVertex(source);
		return new EdgeIterOut(edgesOut.get(source));
	}

	@Override
	public EdgeIter edgesIn(int target) {
		checkVertex(target);
		return new EdgeIterIn(edgesIn.get(target));
	}

	@Override
	public int addEdge(int source, int target) {
		Node e = (Node) addEdgeNode(source, target);
		addEdgeToLists(e);
		return e.id;
	}

	private void addEdgeToLists(Node e) {
		int u = e.source, v = e.v;
		Node next;
		next = edgesOut.get(u);
		if (next != null) {
			next.prevOut = e;
			e.nextOut = next;
		}
		edgesOut.set(u, e);
		next = edgesIn.get(v);
		if (next != null) {
			next.prevIn = e;
			e.nextIn = next;
		}
		edgesIn.set(v, e);
	}

	@Override
	Node allocNode(int id, int source, int target) {
		return new Node(id, source, target);
	}

	@Override
	public void removeEdge(int edge) {
		removeEdge(getNode(edge));
	}

	@Override
	void removeEdge(GraphLinkedAbstract.Node node) {
		Node n = (Node) node;
		removeEdgeOutNode(n);
		removeEdgeInNode(n);
		super.removeEdge(node);
	}

	@Override
	public void removeEdgesOutOf(int source) {
		checkVertex(source);
		for (Node p = edgesOut.get(source), next; p != null; p = next) {
			next = p.nextOut;
			p.nextOut = p.prevOut = null;
			removeEdgeInNode(p);
			super.removeEdge(p.id);
		}
		edgesOut.set(source, null);
	}

	@Override
	public void removeEdgesInOf(int target) {
		checkVertex(target);
		for (Node p = edgesIn.get(target), next; p != null; p = next) {
			next = p.nextIn;
			p.nextIn = p.prevIn = null;
			removeEdgeOutNode(p);
			super.removeEdge(p.id);
		}
		edgesIn.set(target, null);
	}

	private void removeEdgeOutNode(Node e) {
		Node next = e.nextOut, prev = e.prevOut;
		if (prev == null) {
			edgesOut.set(e.source, next);
		} else {
			prev.nextOut = next;
			e.prevOut = null;
		}
		if (next != null) {
			next.prevOut = prev;
			e.nextOut = null;
		}
	}

	private void removeEdgeInNode(Node e) {
		Node next = e.nextIn, prev = e.prevIn;
		if (prev == null) {
			edgesIn.set(e.v, next);
		} else {
			prev.nextIn = next;
			e.prevIn = null;
		}
		if (next != null) {
			next.prevIn = prev;
			e.nextIn = null;
		}
	}

	@Override
	public void reverseEdge(int edge) {
		Node n = (Node) getNode(edge);
		if (n.source == n.v)
			return;
		removeEdgeOutNode(n);
		removeEdgeInNode(n);
		int w = n.source;
		n.source = n.v;
		n.v = w;
		addEdgeToLists(n);
	}

	@Override
	public void clearEdges() {
		for (GraphLinkedAbstract.Node p0 : nodes()) {
			Node p = (Node) p0;
			p.nextOut = p.prevOut = p.nextIn = p.prevIn = null;
		}
		edgesOut.clear();
		edgesIn.clear();
		super.clearEdges();
	}

	private abstract class EdgeIterImpl extends GraphLinkedAbstract.EdgeItr {

		EdgeIterImpl(Node p) {
			super(p);
		}

		@Override
		public int source() {
			return last.source;
		}

		@Override
		public int target() {
			return last.v;
		}

	}

	private class EdgeIterOut extends EdgeIterImpl {

		EdgeIterOut(Node p) {
			super(p);
		}

		@Override
		Node nextNode(GraphLinkedAbstract.Node n) {
			return ((Node) n).nextOut;
		}

	}

	private class EdgeIterIn extends EdgeIterImpl {

		EdgeIterIn(Node p) {
			super(p);
		}

		@Override
		Node nextNode(GraphLinkedAbstract.Node n) {
			return ((Node) n).nextIn;
		}

	}

	private static class Node extends GraphLinkedAbstract.Node {

		private Node nextOut;
		private Node nextIn;
		private Node prevOut;
		private Node prevIn;

		Node(int id, int source, int target) {
			super(id, source, target);
		}

	}

	private static final GraphCapabilities Capabilities = new GraphCapabilities() {
		@Override
		public boolean vertexAdd() {
			return true;
		}

		@Override
		public boolean vertexRemove() {
			return true;
		}

		@Override
		public boolean edgeAdd() {
			return true;
		}

		@Override
		public boolean edgeRemove() {
			return true;
		}

		@Override
		public boolean parallelEdges() {
			return true;
		}

		@Override
		public boolean selfEdges() {
			return true;
		}

		@Override
		public boolean directed() {
			return true;
		}
	};

}
