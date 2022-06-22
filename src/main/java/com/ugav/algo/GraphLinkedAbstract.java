package com.ugav.algo;

import java.util.AbstractCollection;
import java.util.Collection;

abstract class GraphLinkedAbstract<E> extends GraphAbstract<E> {

	int n;
	int m;
	Collection<Edge<E>> edgesView;

	GraphLinkedAbstract(int n) {
		if (n < 0)
			throw new IllegalArgumentException();
		this.n = n;
		m = 0;
	}

	@Override
	public int vertices() {
		return n;
	}

	@Override
	public int newVertex() {
		return n++;
	}

	@Override
	public Collection<Edge<E>> edges() {
		return edgesView;
	}

	@Override
	public void addEdge(Edge<E> e) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		edges().clear();
		n = 0;
	}

	void checkVertexIdentifier(int v) {
		if (!(0 <= v && v < n))
			throw new IllegalArgumentException("Illegal vertex identifier");
	}

	abstract class EdgesViewAbstract extends AbstractCollection<Edge<E>> {

		@Override
		public int size() {
			return m;
		}

		@Override
		public boolean add(Edge<E> e) {
			throw new UnsupportedOperationException();
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean remove(Object o) {
			removeEdge((Edge<E>) o);
			return true;
		}

	}

}
