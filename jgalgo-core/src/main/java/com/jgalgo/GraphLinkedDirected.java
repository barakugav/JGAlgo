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

	private static final Object DataContainerKeyEdgesOut = new Utils.Obj("edgesOut");
	private static final Object DataContainerKeyEdgesIn = new Utils.Obj("edgesIn");

	/**
	 * Create a new graph with no vertices and edges.
	 */
	GraphLinkedDirected() {
		this(0, 0);
	}

	/**
	 * Create a new graph with no vertices and edges, with expected number of vertices and edges.
	 *
	 * @param expectedVerticesNum the expected number of vertices that will be in the graph
	 * @param expectedEdgesNum    the expected number of edges that will be in the graph
	 */
	GraphLinkedDirected(int expectedVerticesNum, int expectedEdgesNum) {
		super(expectedVerticesNum, expectedEdgesNum);

		edgesOut = new DataContainer.Obj<>(verticesIDStrat, null, Node.class);
		edgesIn = new DataContainer.Obj<>(verticesIDStrat, null, Node.class);
		addInternalVerticesDataContainer(DataContainerKeyEdgesOut, edgesOut);
		addInternalVerticesDataContainer(DataContainerKeyEdgesIn, edgesIn);
	}

	GraphLinkedDirected(GraphLinkedDirected g) {
		super(g);

		edgesOut = new DataContainer.Obj<>(verticesIDStrat, null, Node.class);
		edgesIn = new DataContainer.Obj<>(verticesIDStrat, null, Node.class);
		addInternalVerticesDataContainer(DataContainerKeyEdgesOut, edgesOut);
		addInternalVerticesDataContainer(DataContainerKeyEdgesIn, edgesIn);

		final int m = g.edges().size();
		for (int e = 0; e < m; e++)
			addEdgeToLists((Node) getNode(e));
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
			p.target = v2;
		for (Node p = edgesOut.get(v2); p != null; p = p.nextOut)
			p.source = v1;
		for (Node p = edgesIn.get(v2); p != null; p = p.nextIn)
			p.target = v1;

		edgesOut.swap(v1, v2);
		edgesIn.swap(v1, v2);

		super.vertexSwap(v1, v2);
	}

	@Override
	public EdgeSet edgesOut(int source) {
		checkVertex(source);
		return new EdgeSetOut(source);
	}

	@Override
	public EdgeSet edgesIn(int target) {
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
		int u = e.source, v = e.target;
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
			edgesIn.set(e.target, next);
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
		if (n.source == n.target)
			return;
		removeEdgeOutNode(n);
		removeEdgeInNode(n);
		int w = n.source;
		n.source = n.target;
		n.target = w;
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

	@Override
	public GraphCapabilities getCapabilities() {
		return Capabilities;
	}

	private static final GraphCapabilities Capabilities = GraphCapabilitiesBuilder.newDirected().vertexAdd(true)
			.vertexRemove(true).edgeAdd(true).edgeRemove(true).parallelEdges(true).selfEdges(true).build();

	@Override
	public Graph copy() {
		return new GraphLinkedDirected(this);
	}

	private class EdgeSetOut extends GraphBase.EdgeSetOutDirected {
		EdgeSetOut(int source) {
			super(source);
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterOut(edgesOut.get(source));
		}
	}

	private class EdgeSetIn extends GraphBase.EdgeSetInDirected {
		EdgeSetIn(int target) {
			super(target);
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterIn(edgesIn.get(target));
		}
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
			return last.target;
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

}
