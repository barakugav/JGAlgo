package com.ugav.algo;

import java.util.Collection;
import java.util.Iterator;
import java.util.Objects;

public class GraphTableUndirectedOld<E> extends GraphTableAbstractOld<E> implements Graph.Undirected<E> {

	private final Collection<Edge<E>> edgesView;

	public GraphTableUndirectedOld(int n) {
		super(n);
		edgesView = new EdgesView();
	}

	@Override
	public Collection<Edge<E>> edges() {
		return edgesView;
	}

	@Override
	public Edge<E> addEdge(int u, int v) {
		EdgeUndirectedImpl<E> e = new EdgeUndirectedImpl<>(u, v);
		if (u != v) {
			EdgeUndirectedImpl<E> eTwin = new EdgeUndirectedImpl<>(v, u);
			e.twin = eTwin;
			eTwin.twin = e;
		}
		addEdge(e);
		return e;
	}

	@Override
	public void addEdge(Edge<E> e) {
		if (edges[e.u()][e.v()] != null)
			throw new IllegalArgumentException("Duplicate edges are not supported");
		edges[e.u()][e.v()] = e;
		Edge<E> twin = e.twin();
		if (twin != null)
			edges[e.v()][e.u()] = twin;
		m++;
	}

	private static <E> boolean isApiEdge(Edge<E> e) {
		Edge<E> twin = e.twin();
		if (twin == null)
			return true;
		if (e.u() != e.v())
			return e.u() < e.v();
		return System.identityHashCode(e) <= System.identityHashCode(twin);
	}

	private class EdgesView extends GraphTableAbstractOld<E>.EdgesView {

		@Override
		public Iterator<Edge<E>> iterator() {
			return new EdgesAllItrApiOnly();
		}

	}

	private class EdgesAllItrApiOnly extends GraphTableAbstractOld<E>.EdgesAllItr {

		@Override
		Iterator<Edge<E>> vertexEdgeIter(int u) {
			return new EdgesIterVertexApiOnly(u);
		}
	}

	private class EdgesIterVertexApiOnly extends EdgesOutItrVertex {

		EdgesIterVertexApiOnly(int u) {
			super(u);
		}

		@Override
		void advanceUntilNext() {
			do {
				super.advanceUntilNext();
			} while (v != -1 && !isApiEdge(edges[u][v]));
		}

	}

	private static class EdgeUndirectedImpl<E> extends EdgeAbstract<E> {

		EdgeUndirectedImpl<E> twin;

		private EdgeUndirectedImpl(int u, int v) {
			super(u, v);
		}

		EdgeUndirectedImpl<E> getApiEdge() {
			return isApiEdge(this) ? this : twin;
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

}
