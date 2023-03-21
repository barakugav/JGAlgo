package com.ugav.algo;

import java.util.Collection;

import it.unimi.dsi.fastutil.ints.IntIterator;

// TODO rename to IGraph
public interface Graph {

	public int vertices(); // TODO rename to verticesNum

	public int edges(); // TODO rename to edgesNUm

	// TODO add edges iterator over all edges

	public EdgeIter edges(int u);

	// TODO specific for table
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

	public int newVertex();

	public int addEdge(int u, int v);

	public void removeEdge(int e);

	public void removeEdges(int u);

	public void addEdgeRenameListener(EdgeRenameListener listener);

	public void removeEdgeRenameListener(EdgeRenameListener listener);

	@FunctionalInterface
	public static interface EdgeRenameListener {
		public void edgeRename(int e1, int e2);
	}

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

	// TODO change 'edgeData' to edgeWeight
	// TODO add weights for vertices
	// TODO implement bipartite graphs with boolean weights on vertices

	public <E, T extends EdgeData<E>> T getEdgeData(Object key);

	public <E> EdgeData<E> newEdgeData(Object key);

	public EdgeData.Int newEdgeDataInt(Object key);

	public EdgeData.Double newEdgeDataDouble(Object key);

	public Collection<Object> getEdgeDataKeys();

	public static interface EdgeIter extends IntIterator {

		int u();

		int v();

	}

	// TODO rename to Graph
	public static interface Undirected extends Graph {

	}

	// TODO rename to DiGraph
	public static interface Directed extends Graph {

		@Deprecated
		@Override
		default EdgeIter edges(int u) {
			return edgesOut(u);
		}

		public EdgeIter edgesOut(int u);

		public EdgeIter edgesIn(int v);

		@Override
		default void removeEdges(int u) {
			removeEdgesOut(u);
		}

		public void removeEdgesOut(int u);

		public void removeEdgesIn(int v);

		@Override
		@Deprecated
		default int degree(int u) {
			return degreeOut(u);
		}

		default int degreeOut(int u) {
			int count = 0;
			for (EdgeIter it = edgesOut(u); it.hasNext();) {
				it.nextInt();
				count++;
			}
			return count;
		}

		default int degreeIn(int v) {
			int count = 0;
			for (EdgeIter it = edgesIn(v); it.hasNext();) {
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
