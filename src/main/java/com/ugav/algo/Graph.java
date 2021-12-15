package com.ugav.algo;

import java.util.Iterator;

public interface Graph<E> {

	public int vertices();

	public int edgesNum();

	public Iterator<Edge<E>> edges();

	public Iterator<Edge<E>> edges(int u);

	public int getEdgesArr(int u, Edge<E>[] edges, int begin);

	public int getEdgesArrVs(int u, int[] edges, int begin);

	public boolean isDirected();

	public interface Modifiable<E> extends Graph<E> {

		public int newVertex();

		public Edge<E> addEdge(int u, int v);

		public void removeEdge(Edge<E> e);

		public void clear();

	}

	public interface Flexible<E> extends Modifiable<E> {

		public void addEdge(Edge<E> e);

	}

	public static interface Edge<E> {

		public int u();

		public int v();

		public E val();

		public void val(E v);

		public Edge<E> twin();

	}

	@FunctionalInterface
	public static interface WeightFunction<E> {

		public double weight(Edge<E> e);

	}

	@FunctionalInterface
	public static interface WeightFunctionInt<E> extends WeightFunction<E> {

		default double weight(Edge<E> e) {
			return weightInt(e);
		}

		public int weightInt(Edge<E> e);

	}

}
