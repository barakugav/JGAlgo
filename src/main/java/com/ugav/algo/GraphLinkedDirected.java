package com.ugav.algo;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

public class GraphLinkedDirected<E> extends GraphLinkedAbstract<E> implements Graph.Directed<E> {

	private Node<E>[] edgesIn;
	private Node<E>[] edgesOut;

	public GraphLinkedDirected() {
		this(0);
	}

	@SuppressWarnings("unchecked")
	public GraphLinkedDirected(int n) {
		super(n);
		edgesIn = new Node[n != 0 ? n : 1];
		edgesOut = new Node[n != 0 ? n : 1];
		edgesView = new EdgesView();
	}

	@Override
	public int newVertex() {
		int v = super.newVertex();
		if (edgesIn.length < n) {
			edgesIn = Arrays.copyOf(edgesIn, edgesIn.length * 2);
			edgesOut = Arrays.copyOf(edgesOut, edgesOut.length * 2);
		}
		return v;
	}

	@Override
	public EdgeIterator<E> edges(int u) {
		return edgesOut(u);
	}

	@Override
	public EdgeIterator<E> edgesOut(int u) {
		checkVertexIdentifier(u);
		return new EdgeVertexItrOut<>(edgesOut[u]);
	}

	@Override
	public EdgeIterator<E> edgesIn(int v) {
		checkVertexIdentifier(v);
		return new EdgeVertexItrIn<>(edgesIn[v]);
	}

	@Override
	public Edge<E> getEdge(int u, int v) {
		checkVertexIdentifier(u);
		checkVertexIdentifier(v);
		for (Node<E> n = edgesOut[u]; n != null; n = n.nextOut)
			if (n.v == v)
				return n;
		return null;
	}

	@Override
	public Edge<E> addEdge(int u, int v) {
		checkVertexIdentifier(u);
		checkVertexIdentifier(v);

		Node<E> e = new Node<>(u, v), next;
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

		m++;
		return e;
	}

	@Override
	public void addEdge(Edge<E> e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeEdge(Edge<E> e) {
		removeEdgeOut((Node<E>) e);
		removeEdgeIn((Node<E>) e);
		m--;
	}

	@Override
	public void removeEdgesOut(int u) {
		int count = 0;
		for (Node<E> p = edgesOut[u], next; p != null; p = next) {
			next = p.nextOut;
			p.nextOut = p.prevOut = null;
			removeEdgeIn(p);
			count++;
		}
		edgesOut[u] = null;
		m -= count;
	}

	@Override
	public void removeEdgesIn(int v) {
		int count = 0;
		for (Node<E> p = edgesIn[v], next; p != null; p = next) {
			next = p.nextIn;
			p.nextIn = p.prevIn = null;
			removeEdgeOut(p);
			count++;
		}
		edgesIn[v] = null;
		m -= count;
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

	private static class Node<E> extends EdgeAbstract<E> {

		private Node<E> nextOut;
		private Node<E> nextIn;
		private Node<E> prevOut;
		private Node<E> prevIn;

		private Node(int u, int v) {
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
			return u == e.u() && v == e.v() && Objects.equals(data(), e.data());
		}

		@Override
		public int hashCode() {
			return u ^ ~v ^ Objects.hashCode(data());
		}

	}

	private static abstract class EdgeVertexItr<E> implements EdgeIterator<E> {

		Node<E> p;

		EdgeVertexItr(Node<E> p) {
			this.p = p;
		}

		@Override
		public boolean hasNext() {
			return p != null;
		}

	}

	private static class EdgeVertexItrOut<E> extends EdgeVertexItr<E> {

		EdgeVertexItrOut(Node<E> p) {
			super(p);
		}

		@Override
		public Edge<E> next() {
			if (!hasNext())
				throw new NoSuchElementException();
			Node<E> q = p;
			p = q.nextOut;
			return q;
		}

		@Override
		public Edge<E> pickNext() {
			if (!hasNext())
				throw new NoSuchElementException();
			return p;
		}

	}

	private static class EdgeVertexItrIn<E> extends EdgeVertexItr<E> {

		EdgeVertexItrIn(Node<E> p) {
			super(p);
		}

		@Override
		public Edge<E> next() {
			if (!hasNext())
				throw new NoSuchElementException();
			Node<E> q = p;
			p = q.nextIn;
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
			p = edgesOut[0];
		}

		@Override
		public boolean hasNext() {
			for (;;) {
				if (p != null)
					return true;
				if (++idx >= n)
					return false;
				p = edgesOut[idx];
			}
		}

		@Override
		public Edge<E> next() {
			if (!hasNext())
				throw new NoSuchElementException();
			Node<E> q = p;
			p = q.nextOut;
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
			for (Node<E> n = edgesOut[u]; n != null; n = n.nextOut)
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
			Node<E>[] es = edgesOut;
			for (int i = 0; i < es.length; i++) {
				for (Node<E> p = es[i], next; p != null; p = next) {
					next = p.nextOut;
					p.nextOut = p.prevOut = p.nextIn = p.prevIn = null;
				}
				es[i] = null;
			}
			Arrays.fill(edgesIn, null);
			m = 0;
		}

	}

}
