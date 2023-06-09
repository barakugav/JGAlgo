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

import com.jgalgo.GraphsUtils.GraphCapabilitiesBuilder;

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

	private final WeightsImpl.Index.Obj<Node> edges;

	private static final Object WeightsKeyEdges = new Utils.Obj("edges");

	/**
	 * Create a new graph with no vertices and edges.
	 */
	GraphLinkedUndirected() {
		this(0, 0);
	}

	/**
	 * Create a new graph with no vertices and edges, with expected number of vertices and edges.
	 *
	 * @param expectedVerticesNum the expected number of vertices that will be in the graph
	 * @param expectedEdgesNum    the expected number of edges that will be in the graph
	 */
	GraphLinkedUndirected(int expectedVerticesNum, int expectedEdgesNum) {
		super(expectedVerticesNum, expectedEdgesNum);
		edges = new WeightsImpl.Index.Obj<>(verticesIdStrat, null, Node.class);
		addInternalVerticesWeights(WeightsKeyEdges, edges);
	}

	GraphLinkedUndirected(GraphLinkedUndirected g) {
		super(g);

		edges = new WeightsImpl.Index.Obj<>(verticesIdStrat, null, Node.class);
		addInternalVerticesWeights(WeightsKeyEdges, edges);

		final int m = g.edges().size();
		for (int e = 0; e < m; e++)
			addEdgeToLists(getNode(e));
	}

	@Override
	void removeVertexImpl(int vertex) {
		super.removeVertexImpl(vertex);
		edges.clear(vertex);
	}

	@Override
	void vertexSwap(int v1, int v2) {
		final int tempV = -2;
		for (Node p = edges.get(v1), next; p != null; p = next) {
			next = p.next(v1);
			if (p.source == v1)
				p.source = tempV;
			if (p.target == v1)
				p.target = tempV;
		}
		for (Node p = edges.get(v2), next; p != null; p = next) {
			next = p.next(v2);
			if (p.source == v2)
				p.source = v1;
			if (p.target == v2)
				p.target = v1;
		}
		for (Node p = edges.get(v1), next; p != null; p = next) {
			next = p.next(tempV);
			if (p.source == tempV)
				p.source = v2;
			if (p.target == tempV)
				p.target = v2;
		}

		edges.swap(v1, v2);

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
		if (source == target)
			throw new IllegalArgumentException("self edges are not supported");
		Node e = (Node) addEdgeNode(source, target);
		addEdgeToLists(e);
		return e.id;
	}

	private void addEdgeToLists(Node e) {
		int source = e.source, target = e.target;
		Node next;
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
		removeEdgeNodePointers(node, node.target);
		super.removeEdgeImpl(edge);
	}

	void removeEdgeNodePointers(Node e, int w) {
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
	public void removeEdgesOf(int source) {
		checkVertex(source);
		for (Node p = edges.get(source), next; p != null; p = next) {
			// update u list
			next = p.next(source);
			p.nextSet(source, null);
			p.prevSet(source, null);

			// update v list
			int v = p.getEndpoint(source);
			removeEdgeNodePointers(p, v);
			edgeSwapBeforeRemove(p.id);
			super.removeEdgeImpl(p.id);
		}
		edges.set(source, null);
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
		edges.clear();
		super.clearEdges();
	}

	@Override
	public GraphCapabilities getCapabilities() {
		return Capabilities;
	}

	private static final GraphCapabilities Capabilities =
			GraphCapabilitiesBuilder.newUndirected().parallelEdges(true).selfEdges(false).build();

	@Override
	public IndexGraph copy() {
		return new GraphLinkedUndirected(this);
	}

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
			assert w == source || w == target;
			if (w == source)
				nextu = n;
			else
				nextv = n;
		}

		Node prev(int w) {
			assert w == source || w == target;
			return w == source ? prevu : prevv;
		}

		void prevSet(int w, Node n) {
			assert w == source || w == target;
			if (w == source)
				prevu = n;
			else
				prevv = n;
		}

		int getEndpoint(int w) {
			assert w == source || w == target;
			return w == source ? target : source;
		}
	}

	private class EdgeSetOut extends GraphBase.EdgeSetOutUndirected {
		EdgeSetOut(int source) {
			super(source);
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterOut(source, edges.get(source));
		}
	}

	private class EdgeSetIn extends GraphBase.EdgeSetInUndirected {
		EdgeSetIn(int target) {
			super(target);
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterIn(target, edges.get(target));
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
