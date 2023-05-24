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

import java.util.Collection;
import java.util.NoSuchElementException;

abstract class GraphLinkedAbstract extends GraphBaseContinues {

	private final DataContainer.Obj<Node> edges;

	private static final Object DataContainerKeyEdgeEndpoints = new Utils.Obj("edgeEndpoints");

	GraphLinkedAbstract(int n) {
		super(n);
		edges = new DataContainer.Obj<>(edgesIDStrategy, null, Node.class);
		addInternalEdgesDataContainer(DataContainerKeyEdgeEndpoints, edges);
	}

	GraphLinkedAbstract(GraphLinkedAbstract g) {
		super(g);
		edges = new DataContainer.Obj<>(edgesIDStrategy, null, Node.class);
		addInternalEdgesDataContainer(DataContainerKeyEdgeEndpoints, edges);
		final int m = g.edges().size();
		for (int e = 0; e < m; e++)
			edges.set(e, allocNode(e, g.edgeSource(e), g.edgeTarget(e)));
	}

	@Override
	public int edgeEndpoint(int edge, int endpoint) {
		Node n = getNode(edge);
		if (endpoint == n.source) {
			return n.target;
		} else if (endpoint == n.target) {
			return n.source;
		} else {
			throw new IllegalArgumentException("The given vertex (" + endpoint + ") is not an endpoint of the edge ("
					+ n.source + ", " + n.target + ")");
		}
	}

	Node getNode(int edge) {
		Node n = edges.get(edge);
		assert n.id == edge;
		return n;
	}

	@Override
	public void removeEdge(int edge) {
		removeEdge0(edge);
	}

	private void removeEdge0(int edge) {
		edge = edgeSwapBeforeRemove(edge);
		edges.clear(edge);
		super.removeEdge(edge);
	}

	void removeEdge(Node node) {
		removeEdge0(node.id);
	}

	Node addEdgeNode(int source, int target) {
		int e = super.addEdge(source, target);
		Node n = allocNode(e, source, target);
		edges.set(e, n);
		return n;
	}

	abstract Node allocNode(int id, int source, int target);

	@Override
	void edgeSwap(int e1, int e2) {
		Node n1 = getNode(e1), n2 = getNode(e2);
		n1.id = e2;
		n2.id = e1;
		edges.swap(e1, e2);
		super.edgeSwap(e1, e2);
	}

	@Override
	public int edgeSource(int edge) {
		checkEdge(edge);
		return getNode(edge).source;
	}

	@Override
	public int edgeTarget(int edge) {
		checkEdge(edge);
		return getNode(edge).target;
	}

	Collection<Node> nodes() {
		return edges.values();
	}

	@Override
	public void clearEdges() {
		edges.clear();
		super.clearEdges();
	}

	abstract class EdgeItr implements EdgeIterImpl {

		private Node next;
		Node last;

		EdgeItr(Node p) {
			this.next = p;
		}

		abstract Node nextNode(Node n);

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public int nextInt() {
			if (!hasNext())
				throw new NoSuchElementException();
			next = nextNode(last = next);
			return last.id;
		}

		@Override
		public int peekNext() {
			if (!hasNext())
				throw new NoSuchElementException();
			return next.id;
		}

		@Override
		public void remove() {
			if (last == null)
				throw new IllegalStateException();
			removeEdge(last);
			last = null;
		}
	}

	abstract static class Node {

		int id;
		int source, target;

		Node(int id, int source, int target) {
			this.id = id;
			this.source = source;
			this.target = target;
		}

	}

}
