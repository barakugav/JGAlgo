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

	private Node[] edgesIn;
	private Node[] edgesOut;
	private final DataContainer.Obj<Node> edgesInContainer;
	private final DataContainer.Obj<Node> edgesOutContainer;

	private static final Node[] EmptyNodeArr = new Node[0];

	/**
	 * Create a new graph with no vertices and edges, with expected number of vertices and edges.
	 *
	 * @param expectedVerticesNum the expected number of vertices that will be in the graph
	 * @param expectedEdgesNum    the expected number of edges that will be in the graph
	 */
	GraphLinkedDirected(int expectedVerticesNum, int expectedEdgesNum) {
		super(expectedVerticesNum, expectedEdgesNum);

		edgesOutContainer = new DataContainer.Obj<>(verticesIdStrat, null, EmptyNodeArr, newArr -> edgesIn = newArr);
		edgesInContainer = new DataContainer.Obj<>(verticesIdStrat, null, EmptyNodeArr, newArr -> edgesOut = newArr);
		addInternalVerticesContainer(edgesOutContainer);
		addInternalVerticesContainer(edgesInContainer);
	}

	GraphLinkedDirected(IndexGraph g) {
		super(g);

		edgesOutContainer = new DataContainer.Obj<>(verticesIdStrat, null, EmptyNodeArr, newArr -> edgesIn = newArr);
		edgesInContainer = new DataContainer.Obj<>(verticesIdStrat, null, EmptyNodeArr, newArr -> edgesOut = newArr);
		addInternalVerticesContainer(edgesOutContainer);
		addInternalVerticesContainer(edgesInContainer);

		final int m = g.edges().size();
		for (int e = 0; e < m; e++)
			addEdgeToLists(getNode(e));
	}

	@Override
	void removeVertexImpl(int vertex) {
		super.removeVertexImpl(vertex);
		edgesOutContainer.clear(edgesOut, vertex);
		edgesInContainer.clear(edgesIn, vertex);
	}

	@Override
	void vertexSwap(int v1, int v2) {
		for (Node p = edgesOut[v1]; p != null; p = p.nextOut)
			p.source = v2;
		for (Node p = edgesIn[v1]; p != null; p = p.nextIn)
			p.target = v2;
		for (Node p = edgesOut[v2]; p != null; p = p.nextOut)
			p.source = v1;
		for (Node p = edgesIn[v2]; p != null; p = p.nextIn)
			p.target = v1;

		edgesOutContainer.swap(edgesOut, v1, v2);
		edgesInContainer.swap(edgesIn, v1, v2);

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
		int u = e.source, v = e.target;
		Node next;
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
		removeEdgeOutNode(node);
		removeEdgeInNode(node);
		super.removeEdgeImpl(edge);
	}

	@Override
	public void removeOutEdgesOf(int source) {
		checkVertex(source);
		for (Node p = edgesOut[source], next; p != null; p = next) {
			next = p.nextOut;
			p.nextOut = p.prevOut = null;
			removeEdgeInNode(p);
			edgeSwapBeforeRemove(p.id);
			super.removeEdgeImpl(p.id);
		}
		edgesOut[source] = null;
	}

	@Override
	public void removeInEdgesOf(int target) {
		checkVertex(target);
		for (Node p = edgesIn[target], next; p != null; p = next) {
			next = p.nextIn;
			p.nextIn = p.prevIn = null;
			removeEdgeOutNode(p);
			edgeSwapBeforeRemove(p.id);
			super.removeEdgeImpl(p.id);
		}
		edgesIn[target] = null;
	}

	private void removeEdgeOutNode(Node e) {
		Node next = e.nextOut, prev = e.prevOut;
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
	}

	private void removeEdgeInNode(Node e) {
		Node next = e.nextIn, prev = e.prevIn;
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
	}

	@Override
	public void reverseEdge(int edge) {
		Node n = getNode(edge);
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
		edgesOutContainer.clear(edgesOut);
		edgesInContainer.clear(edgesIn);
		super.clearEdges();
	}

	@Override
	public GraphCapabilities getCapabilities() {
		return Capabilities;
	}

	private static final GraphCapabilities Capabilities =
			GraphCapabilitiesBuilder.newDirected().parallelEdges(true).selfEdges(true).build();

	private class EdgeSetOut extends GraphBase.EdgeSetOutDirected {
		EdgeSetOut(int source) {
			super(source);
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterOut(edgesOut[source]);
		}
	}

	private class EdgeSetIn extends GraphBase.EdgeSetInDirected {
		EdgeSetIn(int target) {
			super(target);
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterIn(edgesIn[target]);
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
