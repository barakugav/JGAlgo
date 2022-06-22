package com.ugav.algo;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

public class GraphTableDirected<E> extends GraphTableAbstract<E> implements GraphDirected<E> {

	private final Collection<Edge<E>> edgesView;

	public GraphTableDirected(int n) {
		super(n);
		edgesView = new EdgesView();
	}

	@Override
	public Collection<Edge<E>> edges() {
		return edgesView;
	}

	@Override
	public EdgeIterator<E> edgesOut(int u) {
		return new EdgesOutItrVertex(u);
	}

	@Override
	public EdgeIterator<E> edgesIn(int v) {
		return new EdgesInItrVertex(v);
	}

	@Override
	public Edge<E> addEdge(int u, int v) {
		EdgeDirectedImpl<E> e = new EdgeDirectedImpl<>(u, v);
		addEdge(e);
		return e;
	}

	@Override
	public void addEdge(Edge<E> e) {
		if (edges[e.u()][e.v()] != null)
			throw new IllegalArgumentException("Duplicate edges are not supported");
		if (e.twin() != null)
			throw new IllegalArgumentException("twin edges are only supported in undirected graphs");
		edges[e.u()][e.v()] = e;
		m++;
	}

	private class EdgesView extends GraphTableAbstract<E>.EdgesView {

		@Override
		public Iterator<Edge<E>> iterator() {
			return new EdgesAllItr();
		}

	}

	private static class EdgeDirectedImpl<E> extends EdgeAbstract<E> {

		private EdgeDirectedImpl(int u, int v) {
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

}
