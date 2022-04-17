package com.ugav.algo;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class GraphLinked<E> extends GraphAbstract<E> {

	private int n;
	private int m;
	private final boolean directed;
	private Node<E>[] edges;
	private final Collection<Edge<E>> edgesView;

	public GraphLinked(DirectedType directed) {
		this(directed, 0);
	}

	@SuppressWarnings("unchecked")
	public GraphLinked(DirectedType directed, int n) {
		if (n < 0)
			throw new IllegalArgumentException();
		this.directed = directed == DirectedType.Directed;
		this.n = n;
		m = 0;
		edges = new Node[n != 0 ? n : 1];
		edgesView = new EdgesView();
	}

	private class EdgesView extends AbstractCollection<Edge<E>> {

		@Override
		public int size() {
			return m;
		}

		@Override
		public boolean contains(Object o) {
			if (!(o instanceof Edge<?>))
				return false;
			Edge<?> e = (Edge<?>) o;
			int u = e.u();
			if (u >= n)
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
		public boolean add(Edge<E> e) {
			addEdge(e);
			return true;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean remove(Object o) {
			removeEdge((Edge<E>) o);
			return true;
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

	@Override
	public int vertices() {
		return n;
	}

	@Override
	public Collection<Edge<E>> edges() {
		return edgesView;
	}

	@Override
	public Iterator<Edge<E>> edges(int u) {
		if (u >= n)
			throw new IllegalArgumentException();
		return new EdgeVertexItr<>(edges[u]);
	}

	@Override
	public Edge<E> getEdge(int u, int v) {
		if (u >= n)
			throw new IllegalArgumentException();
		for (Node<E> n = this.edges[u]; n != null; n = n.next)
			if (n.v == v)
				return n;
		return null;
	}

	@Override
	public boolean isDirected() {
		return directed;
	}

	@Override
	public int newVertex() {
		int v = n++;
		if (edges.length < n)
			edges = Arrays.copyOf(edges, edges.length * 2);
		return v;
	}

	@Override
	public Edge<E> addEdge(int u, int v) {
		if (u >= n || v >= n)
			throw new IllegalArgumentException();
		Node<E> e;
		if (directed) {
			addNode(e = new NodeDirected<>(u, v));
		} else {
			NodeUndirected<E> e1 = new NodeUndirected<>(u, v);
			addNode(e = e1);
			if (u != v) {
				NodeUndirected<E> e2 = new NodeUndirected<>(v, u);
				e1.twin = e2;
				e2.twin = e1;
				addNode(e2);
			}
		}
		m++;
		return e;
	}

	private void addNode(Node<E> e) {
		e.next = edges[e.u()];
		edges[e.u()] = e;
	}

	@Override
	public void addEdge(Edge<E> e) {
		throw new UnsupportedOperationException();
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
		if (!isDirected()) {
			for (Node<E> p = edges[u]; p != null; p = p.next) {
				Edge<E> twin = p.twin();
				if (twin != null)
					removeEdge0(twin);
			}
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
	public void clear() {
		edges().clear();
		n = 0;
	}

	private abstract static class Node<E> extends EdgeAbstract<E> {

		final int u;
		final int v;
		private Node<E> next;

		private Node(int u, int v) {
			this.u = u;
			this.v = v;
		}

		@Override
		public int u() {
			return u;
		}

		@Override
		public int v() {
			return v;
		}

	}

	private static class NodeUndirected<E> extends Node<E> {

		private NodeUndirected<E> twin;

		private NodeUndirected(int u, int v) {
			super(u, v);
		}

		boolean isApiEdge() {
			return twin == null || System.identityHashCode(this) < System.identityHashCode(twin);
		}

		NodeUndirected<E> getApiEdge() {
			return isApiEdge() ? this : twin;
		}

		@Override
		public E val() {
			return getApiEdge().val;
		}

		@Override
		public void val(E v) {
			getApiEdge().val = v;
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
			return ((u == e.u() && v == e.v()) || (u == e.v() && v == e.u())) && Objects.equals(val(), e.val());
		}

		@Override
		public int hashCode() {
			return u ^ v ^ Objects.hashCode(val());
		}

	}

	private static class NodeDirected<E> extends Node<E> {

		private NodeDirected(int u, int v) {
			super(u, v);
		}

		@Override
		public Edge<E> twin() {
			return null;
		}

		@Override
		public boolean equals(Object o) {
			if (o == this)
				return true;
			if (!(o instanceof Edge))
				return false;

			Edge<?> e = (Edge<?>) o;
			return u == e.u() && v == e.v() && Objects.equals(val(), e.val());
		}

		@Override
		public int hashCode() {
			return u ^ ~v ^ Objects.hashCode(val());
		}

	}

	private static class EdgeVertexItr<E> implements Iterator<Edge<E>> {

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
			do {
				while (p != null) {
					// in undirected graph, show only one of the twin edges, choose one arbitrarily
					if (isDirected() || ((NodeUndirected<E>) p).isApiEdge())
						return true;
					p = p.next;
				}

				if (++idx >= n)
					return false;
				p = edges[idx];
			} while (true);
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

}
