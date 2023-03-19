package com.ugav.algo;

import java.util.Arrays;

public class GraphLinkedDirected<E> extends GraphLinkedAbstract<E> implements Graph.Removeable.Directed<E> {

	private Node<E>[] edgesIn;
	private Node<E>[] edgesOut;

	@SuppressWarnings("rawtypes")
	private static final Node[] EmptyNodeArr = new Node[0];

	public GraphLinkedDirected() {
		this(0);
	}

	@SuppressWarnings("unchecked")
	public GraphLinkedDirected(int n) {
		super(n);
		edgesIn = n != 0 ? new Node[n] : EmptyNodeArr;
		edgesOut = n != 0 ? new Node[n] : EmptyNodeArr;
	}

	@Override
	public int newVertex() {
		int v = super.newVertex();
		if (v >= edgesIn.length) {
			edgesIn = Arrays.copyOf(edgesIn, Math.max(edgesIn.length * 2, 2));
			edgesOut = Arrays.copyOf(edgesOut, Math.max(edgesOut.length * 2, 2));
		}
		return v;
	}

	@Override
	public EdgeIter<E> edgesOut(int u) {
		checkVertexIdx(u);
		return new EdgeVertexItrOut(edgesOut[u]);
	}

	@Override
	public EdgeIter<E> edgesIn(int v) {
		checkVertexIdx(v);
		return new EdgeVertexItrIn(edgesIn[v]);
	}

	@Override
	public int addEdge(int u, int v) {
		Node<E> e = (Node<E>) newEdgeNode(u, v), next;
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

		return e.id;
	}

	@Override
	Node<E> allocNode(int id, int u, int v) {
		return new Node<>(id, u, v);
	}

	@Override
	public void removeEdge(int id) {
		Node<E> e = (Node<E>) removeEdgeNode(id);
		removeEdgeOut(e);
		removeEdgeIn(e);
	}

	@Override
	public void removeEdgesOut(int u) {
		for (Node<E> p = edgesOut[u], next; p != null; p = next) {
			next = p.nextOut;
			p.nextOut = p.prevOut = null;
			removeEdgeIn(p);
			removeEdgeNode(p.id);
		}
		edgesOut[u] = null;
	}

	@Override
	public void removeEdgesIn(int v) {
		for (Node<E> p = edgesIn[v], next; p != null; p = next) {
			next = p.nextIn;
			p.nextIn = p.prevIn = null;
			removeEdgeOut(p);
			removeEdgeNode(p.id);
		}
		edgesIn[v] = null;
	}

	private void removeEdgeOut(Node<E> e) {
		Node<E> next = e.nextOut, prev = e.prevOut;
		if (prev == null) {
			edgesOut[e.u] = next;
		} else {
			prev.nextOut = next;
			e.prevOut = null;
		}
		if (next != null) {
			next.prevOut = prev;
			e.nextOut = null;
		}
	}

	private void removeEdgeIn(Node<E> e) {
		Node<E> next = e.nextIn, prev = e.prevIn;
		if (prev == null) {
			edgesIn[e.v] = next;
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
	public void clearEdges() {
		for (GraphLinkedAbstract.Node<E> p0 : Utils.iterable(nodes())) {
			Node<E> p = (Node<E>) p0;
			p.nextOut = p.prevOut = p.nextIn = p.prevIn = null;
		}
		Arrays.fill(edgesOut, null);
		Arrays.fill(edgesIn, null);
		super.clearEdges();
	}

	private abstract class EdgeVertexItr extends GraphLinkedAbstract<E>.EdgeItr {

		EdgeVertexItr(Node<E> p) {
			super(p);
		}

		@Override
		public int u() {
			return last.u;
		}

		@Override
		public int v() {
			return last.u;
		}

	}

	private class EdgeVertexItrOut extends EdgeVertexItr {

		EdgeVertexItrOut(Node<E> p) {
			super(p);
		}

		@Override
		Node<E> nextNode(GraphLinkedAbstract.Node<E> n) {
			return ((Node<E>) n).nextOut;
		}

	}

	private class EdgeVertexItrIn extends EdgeVertexItr {

		EdgeVertexItrIn(Node<E> p) {
			super(p);
		}

		@Override
		Node<E> nextNode(GraphLinkedAbstract.Node<E> n) {
			return ((Node<E>) n).nextIn;
		}

	}

	private static class Node<E> extends GraphLinkedAbstract.Node<E> {

		private Node<E> nextOut;
		private Node<E> nextIn;
		private Node<E> prevOut;
		private Node<E> prevIn;

		Node(int id, int u, int v) {
			super(id, u, v);
		}

	}

}
