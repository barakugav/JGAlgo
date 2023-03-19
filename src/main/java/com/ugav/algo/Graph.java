package com.ugav.algo;

import it.unimi.dsi.fastutil.ints.IntIterator;

public interface Graph<E> {

	public int vertices();

	public int edges();

	public EdgeIter<E> edges(int u);

	default int getEdge(int u, int v) {
		for (IntIterator it = edges(u); it.hasNext();) {
			int e = it.nextInt();
			if (getEdgeTarget(e) == v)
				return e;
		}
		return -1;
	}

	default int degree(int u) {
		int count = 0;
		for (EdgeIter<E> it = edges(u); it.hasNext();) {
			it.nextInt();
			count++;
		}
		return count;
	}

	public int newVertex();

	public int addEdge(int u, int v);

	public void clear();

	public void clearEdges();

	public int getEdgeSource(int edge);

	public int getEdgeTarget(int edge);

	default int getEdgeEndpoint(int edge, int endpoint) {
		int u = getEdgeSource(edge);
		int v = getEdgeTarget(edge);
		if (endpoint == u)
			return v;
		else if (endpoint == v)
			return u;
		else
			throw new IllegalArgumentException();
	}

	public EdgeData<E> edgeData();

	public void setEdgesData(EdgeData<E> data);

	public static interface EdgeIter<E> extends IntIterator {

		int u();

		int v();

		E data();

		void setData(E val);

	}

	public static interface Undirected<E> extends Graph<E> {

	}

	public static interface Directed<E> extends Graph<E> {

		@Deprecated
		@Override
		default EdgeIter<E> edges(int u) {
			return edgesOut(u);
		}

		public EdgeIter<E> edgesOut(int u);

		public EdgeIter<E> edgesIn(int v);

		@Override
		@Deprecated
		default int degree(int u) {
			return degreeOut(u);
		}

		default int degreeOut(int u) {
			int count = 0;
			for (EdgeIter<E> it = edgesOut(u); it.hasNext();) {
				it.nextInt();
				count++;
			}
			return count;
		}

		default int degreeIn(int v) {
			int count = 0;
			for (EdgeIter<E> it = edgesIn(v); it.hasNext();) {
				it.nextInt();
				count++;
			}
			return count;
		}

		@Override
		@Deprecated
		default int getEdgeEndpoint(int edge, int endpoint) {
			return Graph.super.getEdgeEndpoint(edge, endpoint);
		}

	}

	public static interface Removeable<E> extends Graph<E> {

		/*
		 * Graph that support edges removal. These graphs do not guaranteer that the
		 * edges identifiers will be in range [0, edges()), and expose the edges
		 * identifiers by the edgesIDs().
		 */

		public void removeEdge(int e);

		public IntIterator edgesIDs();

		public static interface Undirected<E> extends Removeable<E>, Graph.Directed<E> {

			default void removeEdges(int u) {
				for (EdgeIter<E> it = edgesOut(u); it.hasNext();) {
					it.nextInt();
					it.remove();
				}
			}
		}

		public static interface Directed<E> extends Removeable<E>, Graph.Directed<E> {

			default void removeEdgesOut(int u) {
				for (EdgeIter<E> it = edgesOut(u); it.hasNext();) {
					it.nextInt();
					it.remove();
				}
			}

			default void removeEdgesIn(int v) {
				for (EdgeIter<E> it = edgesIn(v); it.hasNext();) {
					it.nextInt();
					it.remove();
				}
			}
		}

	}

	@FunctionalInterface
	public static interface WeightFunction {

		public double weight(int e);

	}

	@FunctionalInterface
	public static interface WeightFunctionInt extends WeightFunction {

		@Override
		default double weight(int e) {
			return weightInt(e);
		}

		public int weightInt(int e);

	}

}
