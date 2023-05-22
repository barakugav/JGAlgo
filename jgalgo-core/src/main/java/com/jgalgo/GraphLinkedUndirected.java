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

import com.jgalgo.GraphsUtils.UndirectedGraphImpl;

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
class GraphLinkedUndirected extends GraphLinkedAbstract implements UndirectedGraphImpl {

	private final DataContainer.Obj<Node> edges;

	/**
	 * Create a new graph with no vertices and edges.
	 */
	GraphLinkedUndirected() {
		this(0);
	}

	/**
	 * Create a new graph with no edges and {@code n} vertices numbered {@code 0,1,2,..,n-1}.
	 *
	 * @param n the number of initial vertices number
	 */
	GraphLinkedUndirected(int n) {
		super(n, Capabilities);
		edges = new DataContainer.Obj<>(verticesIDStrategy, null, Node.class);
		addInternalVerticesDataContainer(edges);
	}

	@Override
	public void removeVertex(int vertex) {
		vertex = vertexSwapBeforeRemove(vertex);
		super.removeVertex(vertex);
		edges.clear(vertex);
	}

	@Override
	void vertexSwap(int v1, int v2) {
		final int tempV = -2;
		for (Node p = edges.get(v1), next; p != null; p = next) {
			next = p.next(v1);
			if (p.source == v1)
				p.source = tempV;
			if (p.v == v1)
				p.v = tempV;
		}
		for (Node p = edges.get(v2), next; p != null; p = next) {
			next = p.next(v2);
			if (p.source == v2)
				p.source = v1;
			if (p.v == v2)
				p.v = v1;
		}
		for (Node p = edges.get(v1), next; p != null; p = next) {
			next = p.next(tempV);
			if (p.source == tempV)
				p.source = v2;
			if (p.v == tempV)
				p.v = v2;
		}

		edges.swap(v1, v2);

		super.vertexSwap(v1, v2);
	}

	@Override
	public EdgeIter edgesOut(int source) {
		checkVertex(source);
		return new EdgeVertexItr(source, edges.get(source));
	}

	@Override
	public int addEdge(int source, int target) {
		if (source == target)
			throw new IllegalArgumentException("self edges are not supported");
		Node e = (Node) addEdgeNode(source, target), next;
		if ((next = edges.get(source)) != null) {
			e.nextSet(source, next);
			next.prevSet(source, e);
		}
		edges.set(source, e);
		if ((next = edges.get(target)) != null) {
			e.nextSet(target, next);
			next.prevSet(target, e);
		}
		edges.set(target, e);
		return e.id;
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
		removeEdge0(n, n.source);
		removeEdge0(n, n.v);
		super.removeEdge(node);
	}

	@Override
	public void removeEdgesOf(int source) {
		checkVertex(source);
		for (Node p = edges.get(source), next; p != null; p = next) {
			// update u list
			next = p.next(source);
			p.nextSet(source, null);
			p.prevSet(source, null);

			// update v list
			int v = p.getEndpoint(source);
			removeEdge0(p, v);

			super.removeEdge(p.id);
		}
		edges.set(source, null);
	}

	void removeEdge0(Node e, int w) {
		Node next = e.next(w), prev = e.prev(w);
		if (prev == null) {
			edges.set(w, next);
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
	public void clearEdges() {
		for (GraphLinkedAbstract.Node p0 : nodes()) {
			Node p = (Node) p0;
			p.nextu = p.nextv = p.prevu = p.prevv = null;
		}
		edges.clear();
		super.clearEdges();
	}

	private static class Node extends GraphLinkedAbstract.Node {

		private Node nextu, nextv;
		private Node prevu, prevv;

		Node(int id, int source, int target) {
			super(id, source, target);
		}

		Node next(int w) {
			assert w == source || w == v;
			return w == source ? nextu : nextv;
		}

		void nextSet(int w, Node n) {
			assert w == source || w == v;
			if (w == source)
				nextu = n;
			else
				nextv = n;
		}

		Node prev(int w) {
			assert w == source || w == v;
			return w == source ? prevu : prevv;
		}

		void prevSet(int w, Node n) {
			assert w == source || w == v;
			if (w == source)
				prevu = n;
			else
				prevv = n;
		}

		int getEndpoint(int w) {
			assert w == source || w == v;
			return w == source ? v : source;
		}

	}

	private class EdgeVertexItr extends GraphLinkedAbstract.EdgeItr {

		private final int source;

		EdgeVertexItr(int source, Node p) {
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
			int u0 = last.source, v0 = last.v;
			return source == u0 ? v0 : u0;
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
			return false;
		}

		@Override
		public boolean directed() {
			return false;
		}
	};

}
