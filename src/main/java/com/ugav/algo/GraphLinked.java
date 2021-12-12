package com.ugav.algo;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public abstract class GraphLinked<E> implements Graph.Modifiable<E> {

	int n;
	int m;
	Node<E>[] edges;

	@SuppressWarnings("unchecked")
	protected GraphLinked(int n) {
		if (n < 0)
			throw new IllegalArgumentException();
		this.n = n;
		m = 0;
		edges = new Node[n != 0 ? n : 1];
	}

	@Override
	public int vertices() {
		return n;
	}

	@Override
	public int edgesNum() {
		return m;
	}

	@Override
	public Iterator<Edge<E>> edges() {
		return new EdgesItr();
	}

	@Override
	public Iterator<Edge<E>> edges(int u) {
		if (u >= n)
			throw new IllegalArgumentException();
		return new EdgeVertexItr<>(edges[u]);
	}

	@Override
	public int edges(int u, int[] edges, int begin) {
		if (u >= n)
			throw new IllegalArgumentException();
		int i = 0;
		for (Node<E> n = this.edges[u]; n != null; n = n.next)
			edges[begin + i++] = n.v;
		return i;
	}

	@Override
	public int newVertex() {
		int v = n++;
		if (edges.length < n)
			edges = Arrays.copyOf(edges, edges.length * 2);
		return v;
	}

	void removeEdge0(Edge<E> e) {
		for (Node<E> prev = null, p = edges[e.u()]; p != null; p = (prev = p).next) {
			if (p == e) {
				if (prev == null)
					edges[e.u()] = p.next;
				else
					prev.next = p.next;
				p.next = null;
				break;
			}
		}
	}

	Node<E> addNode(int u, int v) {
		Node<E> e = newNode(u, v);
		e.next = edges[u];
		edges[u] = e;
		return e;
	}

	protected abstract Node<E> newNode(int u, int v);

	public static class Directed<E> extends GraphLinked<E> {

		protected Directed(int n) {
			super(n);
		}

		@Override
		public boolean isDirected() {
			return true;
		}

		@Override
		public void removeEdge(Edge<E> e) {
			removeEdge0(e);
			m--;
		}

		@Override
		public Edge<E> addEdge(int u, int v) {
			if (u >= n || v >= n)
				throw new IllegalArgumentException();
			Edge<E> e = addNode(u, v);
			m++;
			return e;
		}

		@Override
		protected Node<E> newNode(int u, int v) {
			return new NodeDirected<>(u, v);
		}

	}

	public static class Undirected<E> extends GraphLinked<E> {

		protected Undirected(int n) {
			super(n);
		}

		@Override
		public boolean isDirected() {
			return false;
		}

		@Override
		public void removeEdge(Edge<E> e0) {
			NodeUndirected<E> e = (NodeUndirected<E>) e0;
			removeEdge0(e);
			if (e.twin != null)
				removeEdge0(e.twin);
			m--;
		}

		@Override
		public Edge<E> addEdge(int u, int v) {
			if (u >= n || v >= n)
				throw new IllegalArgumentException();
			NodeUndirected<E> e1 = (NodeUndirected<E>) addNode(u, v);
			if (u != v) {
				NodeUndirected<E> e2 = (NodeUndirected<E>) addNode(v, u);
				e1.twin = e2;
				e2.twin = e1;
			}
			m++;
			return e1;
		}

		@Override
		protected Node<E> newNode(int u, int v) {
			return new NodeUndirected<>(u, v);
		}

	}

	protected static class Node<E> implements Graph.Edge<E> {

		protected final int u;
		protected final int v;
		protected Node<E> next;
		E value;

		protected Node(int u, int v) {
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

		@Override
		public E val() {
			return value;
		}

		@Override
		public void val(E v) {
			value = v;
		}

		@Override
		public String toString() {
			return "(" + u + ", " + v + ")[" + val() + "]";
		}

	}

	protected static class NodeUndirected<E> extends Node<E> {

		NodeUndirected<E> twin;

		protected NodeUndirected(int u, int v) {
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
			return getApiEdge().value;
		}

		@Override
		public void val(E v) {
			getApiEdge().value = v;
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

	protected static class NodeDirected<E> extends Node<E> {

		protected NodeDirected(int u, int v) {
			super(u, v);
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

	public static Builder builder() {
		return new Builder();
	}

	public static class Builder {

		private int n;
		private boolean directed;

		public Builder() {
			n = 0;
			directed = false;
		}

		public Builder setVertexNum(int n) {
			this.n = n;
			return this;
		}

		public Builder setDirected(boolean directed) {
			this.directed = directed;
			return this;
		}

		public <E> GraphLinked<E> build() {
			return directed ? new Directed<>(n) : new Undirected<>(n);
		}

	}

}
