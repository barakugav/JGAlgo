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

	GraphLinkedAbstract(int n, GraphCapabilities capabilities) {
		super(n, capabilities);
		edges = new DataContainer.Obj<>(n, null, Node.class);
		addInternalEdgesDataContainer(edges);
	}

	@Override
	public int edgeEndpoint(int edge, int endpoint) {
		Node n = getNode(edge);
		if (endpoint == n.u) {
			return n.v;
		} else if (endpoint == n.v) {
			return n.u;
		} else {
			throw new IllegalArgumentException(
					"The given vertex (" + endpoint + ") is not an endpoint of the edge (" + n.u + ", " + n.v + ")");
		}
	}

	Node getNode(int e) {
		Node n = edges.get(e);
		assert n.id == e;
		return n;
	}

	@Override
	public void removeEdge(int e) {
		removeEdge0(e);
	}

	private void removeEdge0(int e) {
		e = edgeSwapBeforeRemove(e);
		edges.remove(e);
		super.removeEdge(e);
	}

	void removeEdge(Node node) {
		removeEdge0(node.id);
	}

	Node addEdgeNode(int u, int v) {
		int e = super.addEdge(u, v);
		edges.add(e);
		Node n = allocNode(e, u, v);
		edges.set(e, n);
		return n;
	}

	abstract Node allocNode(int id, int u, int v);

	@Override
	void edgeSwap(int e1, int e2) {
		Node n1 = getNode(e1), n2 = getNode(e2);
		n1.id = e2;
		n2.id = e1;
		edges.swap(e1, e2);
		super.edgeSwap(e1, e2);
	}

	@Override
	public int edgeSource(int e) {
		checkEdgeIdx(e);
		return getNode(e).u;
	}

	@Override
	public int edgeTarget(int e) {
		checkEdgeIdx(e);
		return getNode(e).v;
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
		int u, v;

		Node(int id, int u, int v) {
			this.id = id;
			this.u = u;
			this.v = v;
		}

	}

}
