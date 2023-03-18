package com.ugav.algo;

import java.util.AbstractCollection;
import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

abstract class GraphTableAbstractOld<E> extends GraphAbstractOld<E> {

	final int n;
	int m;
	final Edge<E>[][] edges;

	@SuppressWarnings("unchecked")
	GraphTableAbstractOld(int n) {
		if (n < 0)
			throw new IllegalArgumentException();
		this.n = n;
		edges = new Edge[n][n];
	}

	@Override
	public int vertices() {
		return n;
	}

	@Override
	public Iterator<Edge<E>> edges(int u) {
		return new EdgesOutItrVertex(u);
	}

	@Override
	public Edge<E> getEdge(int u, int v) {
		return edges[u][v];
	}

	@Override
	public int newVertex() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	class EdgesOutItrVertex implements Iterator<Edge<E>> {

		final int u;
		int v;

		EdgesOutItrVertex(int u) {
			if (!(0 <= u && u < n))
				throw new IllegalArgumentException("Illegal vertex: " + u);
			this.u = u;

			v = 0;
			advanceUntilNext();
		}

		@Override
		public boolean hasNext() {
			return v != -1;
		}

		@Override
		public Edge<E> next() {
			if (!hasNext())
				throw new NoSuchElementException();
			Edge<E> ret = edges[u][v];
			v++;
			advanceUntilNext();
			return ret;
		}

		void advanceUntilNext() {
			for (int next = v; next < n; next++) {
				if (edges[u][next] != null) {
					v = next;
					return;
				}
			}
			v = -1;
		}

	}

	class EdgesInItrVertex implements Iterator<Edge<E>> {

		private int u;
		private final int v;

		EdgesInItrVertex(int v) {
			if (!(0 <= v && v < n))
				throw new IllegalArgumentException("Illegal vertex: " + v);
			this.v = v;

			u = 0;
			advanceUntilNext();
		}

		@Override
		public boolean hasNext() {
			return u != -1;
		}

		@Override
		public Edge<E> next() {
			if (!hasNext())
				throw new NoSuchElementException();
			Edge<E> ret = edges[u][v];
			u++;
			advanceUntilNext();
			return ret;
		}

		private void advanceUntilNext() {
			for (int next = u; next < n; next++) {
				if (edges[next][v] != null) {
					u = next;
					return;
				}
			}
			u = -1;
		}

	}

	class EdgesAllItr implements Iterator<Edge<E>> {

		private int u;
		private Iterator<Edge<E>> uIter;

		EdgesAllItr() {
			u = 0;
			uIter = null;
			advanceUntilNext();
		}

		@Override
		public boolean hasNext() {
			return u != -1;
		}

		@Override
		public Edge<E> next() {
			if (!hasNext())
				throw new NoSuchElementException();
			Edge<E> ret = uIter.next();
			advanceUntilNext();
			return ret;
		}

		private void advanceUntilNext() {
			if (uIter != null && uIter.hasNext())
				return;
			if (uIter != null)
				u++;
			for (; u < n; u++) {
				uIter = vertexEdgeIter(u);
				if (uIter.hasNext())
					return;
			}
			u = -1;
			uIter = null;
		}

		Iterator<Edge<E>> vertexEdgeIter(int u) {
			return new EdgesOutItrVertex(u);
		}

	}

	abstract class EdgesView extends AbstractCollection<Edge<E>> {

		@Override
		public int size() {
			return m;
		}

		@Override
		public boolean contains(Object o) {
			if (!(o instanceof Edge<?>))
				return false;
			Edge<?> e = (Edge<?>) o;
			if (e.u() >= n || e.v() >= n)
				return false;
			return edges[e.u()][e.v()] == e;
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
			for (int u = 0; u < n; u++)
				Arrays.fill(edges[u], null);
			m = 0;
		}
	}

}
