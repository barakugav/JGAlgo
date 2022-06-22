package com.ugav.algo;

public abstract class GraphAbstract<E> implements Graph<E> {

	@Override
	public void addEdge(Edge<E> e) {
		edges().add(e);
	}

	@Override
	public void removeEdge(Edge<E> e) {
		edges().remove(e);
	}

	@Override
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (!(other instanceof Graph<?>))
			return false;
		Graph<?> o = (Graph<?>) other;

		return (this instanceof GraphDirected<?>) == (o instanceof GraphDirected<?>) && vertices() == o.vertices()
				&& edges().equals(o.edges());
	}

	@Override
	public int hashCode() {
		int h = vertices();
		for (Edge<E> e : edges())
			h = h * 31 + e.hashCode();
		return h;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append('{');
		int n = vertices();

		boolean firstVertex = true;
		for (int u = 0; u < n; u++) {
			if (firstVertex)
				firstVertex = false;
			else
				s.append(", ");
			s.append("<v" + u + ">->[");

			boolean firstEdge = true;
			for (Edge<E> e : Utils.iterable(edges(u))) {
				if (firstEdge)
					firstEdge = false;
				else
					s.append(", ");
				s.append(e);
			}
			s.append("]");
		}
		s.append('}');
		return s.toString();
	}

	public static abstract class EdgeAbstract<E> implements Edge<E> {

		final int u;
		final int v;
		E data;

		EdgeAbstract(int u, int v) {
			this(u, v, null);
		}

		EdgeAbstract(int u, int v, E data) {
			this.u = u;
			this.v = v;
			this.data = data;
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
		public E data() {
			return data;
		}

		@Override
		public void setData(E data) {
			this.data = data;
		}

		@Override
		public String toString() {
			E data = data();
			return "(" + u() + ", " + v() + ")" + (data != null ? "[" + data + "]" : "");
		}

	}

}
