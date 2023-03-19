package com.ugav.algo;

import java.util.Objects;
import java.util.function.BiFunction;

import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntComparator;

abstract class GraphAbstract<E> implements Graph<E> {

	@Override
	public boolean equals(Object other) {
		if (other == this)
			return true;
		if (!(other instanceof Graph<?>))
			return false;
		Graph<?> o = (Graph<?>) other;

		if ((this instanceof Graph.Directed<?>) != (o instanceof Graph.Directed<?>))
			return false;
		if (vertices() != o.vertices() || edges() != o.edges())
			return false;
		int m = edges();
		int[] es1 = new int[m];
		int[] es2 = new int[m];
		for (int e = 0; e < m; e++)
			es1[e] = es2[e] = e;

		final boolean directed = this instanceof Graph.Directed<?>;
		BiFunction<Graph<?>, Graph<?>, IntComparator> cmpFactory = (g1, g2) -> {

			EdgeData<?> eData1 = g1.edgeData(), eData2 = g2.edgeData();
			IntComparator dataCmp = (e1, e2) -> {
				Object d1 = eData1.get(e1);
				Object d2 = eData2.get(e2);
				if (Objects.equals(d1, d2))
					return 0;
				int i1 = System.identityHashCode(d1);
				int i2 = System.identityHashCode(d2);
				return Integer.compare(i1, i2);
			};

			if (directed) {
				return (e1, e2) -> {
					int c;
					int u1 = g1.getEdgeSource(e1), u2 = g2.getEdgeSource(e2);
					if ((c = Integer.compare(u1, u2)) != 0)
						return c;
					int v1 = g1.getEdgeTarget(e1), v2 = g2.getEdgeTarget(e2);
					if ((c = Integer.compare(v1, v2)) != 0)
						return c;
					return dataCmp.compare(e1, e2);
				};
			} else {
				return (e1, e2) -> {
					int c;
					int u1 = g1.getEdgeSource(e1), u2 = g2.getEdgeSource(e2);
					int v1 = g1.getEdgeTarget(e1), v2 = g2.getEdgeTarget(e2);
					if (u1 > v1) {
						int temp = u1;
						u1 = v1;
						v1 = temp;
					}
					if (u2 > v2) {
						int temp = u2;
						u2 = v2;
						v2 = temp;
					}
					if ((c = Integer.compare(u1, u2)) != 0)
						return c;
					if ((c = Integer.compare(v1, v2)) != 0)
						return c;
					return dataCmp.compare(e1, e2);
				};
			}
		};

		IntArrays.parallelQuickSort(es1, cmpFactory.apply(this, this));
		IntArrays.parallelQuickSort(es2, cmpFactory.apply(o, o));

		IntComparator cmp = cmpFactory.apply(this, o);
		for (int i = 0; i < m; i++)
			if (cmp.compare(es1[i], es2[i]) != 0)
				return false;
		return true;
	}

	@Override
	public int hashCode() {
		int h = 1, n = vertices();
		EdgeData<E> edgeData = edgeData();
		if (this instanceof Graph.Directed<?> g) {
			for (int u = 0; u < n; u++) {
				int uh = 1;
				for (EdgeIter<?> eit = g.edgesOut(u); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.v();
					int eh = v * 31 + Objects.hashCode(edgeData.get(e));
					uh = uh * 31 + eh;
				}
				h = h * 31 + uh;
			}
		} else {
			for (int u = 0; u < n; u++) {
				int uh = 1;
				for (EdgeIter<?> eit = edges(u); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.v();
					if (u > v)
						continue;
					int eh = v * 31 + Objects.hashCode(edgeData.get(e));
					uh = uh * 31 + eh;
				}
				h = h * 31 + uh;
			}

		}
		return h;
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append('{');
		int n = vertices();
		EdgeData<E> edgeData = edgeData();

		boolean firstVertex = true;
		for (int u = 0; u < n; u++) {
			if (firstVertex)
				firstVertex = false;
			else
				s.append(", ");
			s.append("<v" + u + ">->[");

			boolean firstEdge = true;
			for (EdgeIter<?> eit = edges(u); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.v();
				if (firstEdge)
					firstEdge = false;
				else
					s.append(", ");
				Object d = edgeData.get(e);
				s.append("(" + u + ", " + v + ")" + (d != null ? "[" + d + "]" : ""));
			}
			s.append("]");
		}
		s.append('}');
		return s.toString();
	}

}
