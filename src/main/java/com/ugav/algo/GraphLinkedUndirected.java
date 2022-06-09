package com.ugav.algo;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class GraphLinkedUndirected<E> extends GraphLinkedAbstract<E> {

	private Node<E>[] edges;

	public GraphLinkedUndirected() {
		this(0);
	}

	@SuppressWarnings("unchecked")
	public GraphLinkedUndirected(int n) {
		super(n);
		edges = new Node[n != 0 ? n : 1];
		this.edgesView = new EdgesView();
	}

	@Override
	public int newVertex() {
		int v = super.newVertex();
		if (edges.length < n)
			edges = Arrays.copyOf(edges, edges.length * 2);
		return v;
	}

	@Override
	public EdgeIterator<E> edges(int u) {
		checkVertexIdentifier(u);
		return new EdgeVertexItr<>(edges[u]);
	}

	@Override
	public Edge<E> getEdge(int u, int v) {
		checkVertexIdentifier(u);
		checkVertexIdentifier(v);
		for (Node<E> n = edges[u]; n != null; n = n.next)
			if (n.v == v)
				return n;
		return null;
	}

	@Override
	public Edge<E> addEdge(int u, int v) {
		checkVertexIdentifier(u);
		checkVertexIdentifier(v);

		Node<E> e = new Node<>(u, v);
		addNode(e);
		if (u != v) {
			Node<E> e2 = new Node<>(v, u);
			e.twin = e2;
			e2.twin = e;
			addNode(e2);
		}

		m++;
		return e;
	}

	private void addNode(Node<E> e) {
		e.next = edges[e.u()];
		edges[e.u()] = e;
	}

	@Override
	public void removeEdge(Edge<E> e) {
		Edge<E> twin = e.twin();
		removeEdge0(e);
		if (twin != null)
			removeEdge0(twin);
		m--;
	}

	@Override
	public void removeEdgesOut(int u) {
		for (Node<E> p = edges[u]; p != null; p = p.next) {
			Edge<E> twin = p.twin();
			if (twin != null)
				removeEdge0(twin);
		}

		int count = 0;
		for (Node<E> p = edges[u], next; p != null; p = next) {
			next = p.next;
			p.next = null;
			count++;
		}
		edges[u] = null;
		m -= count;
	}

	void removeEdge0(Edge<E> e) {
		for (Node<E> prev = null, p = edges[e.u()]; p != null; p = (prev = p).next) {
			if (p == e) {
				if (prev == null)
					edges[e.u()] = p.next;
				else
					prev.next = p.next;
				p.next = null;
				return;
			}
		}
		throw new IllegalArgumentException("edge not in graph: " + e);
	}

	@Override
	public boolean isDirected() {
		return false;
	}

	private static class Node<E> extends NodeAbstact<E> {

		private Node<E> twin;
		private Node<E> next;

		private Node(int u, int v) {
			super(u, v);
		}

		boolean isApiEdge() {
			if (twin == null)
				return true;
			if (u != v)
				return u < v;
			return System.identityHashCode(this) <= System.identityHashCode(twin);
		}

		Node<E> getApiEdge() {
			return isApiEdge() ? this : twin;
		}

		@Override
		public E data() {
			return getApiEdge().data;
		}

		@Override
		public void setData(E data) {
			getApiEdge().data = data;
		}

		@Override
		public Edge<E> twin() {
			return twin;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof Edge))
				return false;

			Edge<?> e = (Edge<?>) o;
			return ((u == e.u() && v == e.v()) || (u == e.v() && v == e.u())) && Objects.equals(data(), e.data());
		}

		@Override
		public int hashCode() {
			return u ^ v ^ Objects.hashCode(data());
		}

	}

	private static class EdgeVertexItr<E> implements EdgeIterator<E> {

		Node<E> p;

		EdgeVertexItr(Node<E> p) {
			this.p = p;
		}

		@Override
		public boolean hasNext() {
			return p != null;
		}

		@Override
		public Edge<E> next() {
			if (!hasNext())
				throw new NoSuchElementException();
			Node<E> q = p;
			p = q.next;
			return q;
		}

		@Override
		public Edge<E> pickNext() {
			if (!hasNext())
				throw new NoSuchElementException();
			return p;
		}

	}

	private class EdgesItr implements Iterator<Edge<E>> {

		int idx;
		Node<E> p;

		EdgesItr() {
			idx = 0;
			p = edges[0];
		}

		@Override
		public boolean hasNext() {
			for (;;) {
				while (p != null) {
					// in undirected graph, show only one of the twin edges, choose one arbitrarily
					if (p.isApiEdge())
						return true;
					p = p.next;
				}

				if (++idx >= n)
					return false;
				p = edges[idx];
			}
		}

		@Override
		public Edge<E> next() {
			if (!hasNext())
				throw new NoSuchElementException();
			Node<E> q = p;
			p = q.next;
			return q;
		}

	}

	private class EdgesView extends EdgesViewAbstract {

		@Override
		public boolean contains(Object o) {
			if (!(o instanceof Edge<?>))
				return false;
			Edge<?> e = (Edge<?>) o;
			int u = e.u();
			if (u >= n || e.v() >= n)
				return false;
			for (Node<E> n = edges[u]; n != null; n = n.next)
				if (n == e)
					return true;
			return false;
		}

		@Override
		public Iterator<Edge<E>> iterator() {
			return new EdgesItr();
		}

		@Override
		public void clear() {
			Node<E>[] es = edges;
			for (int i = 0; i < es.length; i++) {
				for (Node<E> p = es[i], next; p != null; p = next) {
					next = p.next;
					p.next = null;
				}
				es[i] = null;
			}
			m = 0;
		}

	}

}
