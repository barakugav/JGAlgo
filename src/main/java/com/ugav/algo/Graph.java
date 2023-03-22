package com.ugav.algo;

import java.util.Collection;
import java.util.Set;

import it.unimi.dsi.fastutil.ints.IntIterator;

public interface Graph {

	public int verticesNum();

	public int edgesNum();

	public EdgeIter edges(int u);

	default int getEdge(int u, int v) {
		for (EdgeIter it = edges(u); it.hasNext();) {
			int e = it.nextInt();
			if (it.v() == v)
				return e;
		}
		return -1;
	}

	default int degree(int u) {
		int count = 0;
		for (EdgeIter it = edges(u); it.hasNext();) {
			it.nextInt();
			count++;
		}
		return count;
	}

	public int addVertex();

	public int addEdge(int u, int v);

	public void removeEdge(int edge);

	default void removeEdgesAll(int u) {
		for (EdgeIter eit = edges(u); eit.hasNext();) {
			eit.nextInt();
			eit.remove();
		}
	}

	public void addEdgeRenameListener(EdgeRenameListener listener);

	public void removeEdgeRenameListener(EdgeRenameListener listener);

	@FunctionalInterface
	public static interface EdgeRenameListener {
		public void edgeRename(int e1, int e2);
	}

	public void clear();

	public void clearEdges();

	public int edgeSource(int edge);

	public int edgeTarget(int edge);

	default int edgeEndpoint(int edge, int endpoint) {
		int u = edgeSource(edge);
		int v = edgeTarget(edge);
		if (endpoint == u) {
			return v;
		} else if (endpoint == v) {
			return u;
		} else {
			throw new IllegalArgumentException();
		}
	}

	// TODO add weights for vertices
	// TODO remove vertex
	// TODO documentation
	// TODO implement bipartite graphs with boolean weights on vertices

	public <E, T extends EdgesWeight<E>> T edgesWeight(Object key);

	public <E> EdgesWeight<E> newEdgeWeight(Object key);

	public EdgesWeight.Int newEdgeWeightInt(Object key);

	public EdgesWeight.Double newEdgeWeightDouble(Object key);

	public Set<Object> getEdgeWeightKeys();

	public Collection<EdgesWeight<?>> getEdgeWeights();

	public static interface EdgeIter extends IntIterator {

		int u();

		int v();

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
