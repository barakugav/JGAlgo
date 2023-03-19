package com.ugav.algo;

import java.util.Arrays;

public class GraphLinkedUndirected<E> extends GraphLinkedAbstract<E> implements Graph.Removeable.Undirected<E> {

	private Node<E>[] edges;

	@SuppressWarnings("rawtypes")
	private static final Node[] EmptyNodeArr = new Node[0];

	public GraphLinkedUndirected() {
		this(0);
	}

	@SuppressWarnings("unchecked")
	public GraphLinkedUndirected(int n) {
		super(n);
		edges = n != 0 ? new Node[n] : EmptyNodeArr;
	}

	@Override
	public int newVertex() {
		int v = super.newVertex();
		if (v >= edges.length)
			edges = Arrays.copyOf(edges, Math.max(edges.length * 2, 2));
		return v;
	}

	@Override
	public EdgeIter<E> edges(int u) {
		checkVertexIdx(u);
		return new EdgeVertexItr(u, edges[u]);
	}

	@Override
	public int addEdge(int u, int v) {
		Node<E> e = (Node<E>) newEdgeNode(u, v);
		e.nextSet(u, edges[u]);
		edges[u] = e;
		e.nextSet(v, edges[v]);
		edges[v] = e;
		return e.id;
	}

	@Override
	Node<E> allocNode(int id, int u, int v) {
		return new Node<>(id, u, v);
	}

	@Override
	public void removeEdge(int id) {
		Node<E> e = (Node<E>) removeEdgeNode(id);
		removeEdge0(e, e.u);
		removeEdge0(e, e.v);
	}

	void removeEdge0(Node<E> e, int w) {
		// TODO add prev pointers
		for (Node<E> prev = null, p = edges[w]; p != null; p = (prev = p).next(w)) {
			if (p == e) {
				if (prev == null)
					edges[w] = p.next(w);
				else
					prev.nextSet(w, p.next(w));
				p.nextSet(w, null);
				return;
			}
		}
		throw new IllegalArgumentException("edge not in graph: " + e);
	}

	@Override
	public void clearEdges() {
		for (GraphLinkedAbstract.Node<E> p0 : Utils.iterable(nodes())) {
			Node<E> p = (Node<E>) p0;
			p.nextu = p.nextv = null;
		}
		Arrays.fill(edges, null);
		super.clearEdges();

	}

	private static class Node<E> extends GraphLinkedAbstract.Node<E> {

		private Node<E> nextu, nextv;

		Node(int id, int u, int v) {
			super(id, u, v);
		}

		Node<E> next(int w) {
			return w == u ? nextu : nextv;
		}

		void nextSet(int w, Node<E> n) {
			if (w == u)
				nextu = n;
			else
				nextv = n;
		}

	}

	private class EdgeVertexItr extends GraphLinkedAbstract<E>.EdgeItr {

		private final int u;

		EdgeVertexItr(int u, Node<E> p) {
			super(p);
			this.u = u;
		}

		@Override
		Node<E> nextNode(GraphLinkedAbstract.Node<E> n) {
			return ((Node<E>) n).next(u);
		}

		@Override
		public int u() {
			return u;
		}

		@Override
		public int v() {
			int u0 = last.u, v0 = last.v;
			return u == u0 ? v0 : u0;
		}

	}

}
