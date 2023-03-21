package com.ugav.algo;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

abstract class GraphLinkedAbstract extends GraphAbstract {

	private Node[] edges;
	private static final Node[] EmptyNodeArr = new Node[0];

	GraphLinkedAbstract(int n) {
		super(n);
		edges = EmptyNodeArr;
	}

	@Override
	public int edgeSource(int e) {
		checkEdgeIdx(e);
		return edges[e].u;
	}

	@Override
	public int edgeTarget(int e) {
		checkEdgeIdx(e);
		return edges[e].v;
	}

	Node getNode(int e) {
		return edges[e];
	}

	Node addEdgeNode(int u, int v) {
		int e = super.addEdge(u, v);
		Node n = allocNode(e, u, v);
		if (e >= edges.length)
			edges = Arrays.copyOf(edges, Math.max(2, edges.length * 2));
		edges[e] = n;
		return n;
	}

	Node allocNode(int id, int u, int v) {
		return new Node(id, u, v);
	}

	@Override
	public void removeEdge(int e) {
		checkEdgeIdx(e);
		int lastEdge = edgesNum() - 1;
		if (e != lastEdge) {
			edgeSwap(e, lastEdge);
			e = lastEdge;
		}
		edges[e] = null;
		super.removeEdge(e);
	}

	@Override
	void edgeSwap(int e1, int e2) {
		Node n1 = edges[e1], n2 = edges[e2];
		edges[n1.id = e2] = n1;
		edges[n2.id = e1] = n2;
		super.edgeSwap(e1, e2);
	}

	Iterator<Node> nodes() {
		return new Utils.ArrayView<>(edges, edgesNum()).iterator();
	}

	Node removeEdgeNode(int e) {
		checkEdgeIdx(e);
		int lastEdge = edgesNum() - 1;
		if (e != lastEdge) {
			edgeSwap(e, lastEdge);
			e = lastEdge;
		}
		Node n = edges[e];
		edges[e] = null;
		super.removeEdge(e);
		return n;
	}

	@Override
	public void clearEdges() {
		Arrays.fill(edges, 0, edgesNum(), null);
		super.clearEdges();
	}

	abstract class EdgeItr implements Graph.EdgeIter {

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

	}

	static class Node {

		int id;
		int u, v;

		Node(int id, int u, int v) {
			this.id = id;
			this.u = u;
			this.v = v;
		}

	}

}
