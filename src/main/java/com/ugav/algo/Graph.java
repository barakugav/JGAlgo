package com.ugav.algo;

import java.util.Collection;

import it.unimi.dsi.fastutil.ints.IntIterator;

public interface Graph {

	public int vertices(); // TODO rename to verticesNum

	public int edges(); // TODO rename to edgesNUm

	// TODO add edges iterator over all edges

	public EdgeIter edges(int u);

	// TODO specific for table
	public int getEdge(int u, int v);

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

	public <E, T extends EdgeData<E>> T getEdgeData(String key);

	public <E> EdgeData<E> newEdgeData(String key);

	public EdgeData.Int newEdgeDataInt(String key);

	public EdgeData.Double newEdgeDataDouble(String key);

	public Collection<String> getEdgeDataKeys();

	public void setEdgeDataBuilder(EdgeData.Builder builder);

	public static interface EdgeIter extends IntIterator {

		int u();

		int v();

	}

	public static interface Undirected extends Graph {

		@Override
		default int getEdge(int u, int v) {
			for (IntIterator it = edges(u); it.hasNext();) {
				int e = it.nextInt();
				if (getEdgeEndpoint(e, u) == v)
					return e;
			}
			return -1;
		}

	}

	public static interface Directed extends Graph {

		@Deprecated
		@Override
		default EdgeIter edges(int u) {
			return edgesOut(u);
		}

		public EdgeIter edgesOut(int u);

		public EdgeIter edgesIn(int v);

		@Override
		default int getEdge(int u, int v) {
			for (IntIterator it = edgesOut(u); it.hasNext();) {
				int e = it.nextInt();
				if (getEdgeTarget(e) == v)
					return e;
			}
			return -1;
		}

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

	public static interface Removeable extends Graph {

		/*
		 * Graph that support edges removal. These graphs do not guaranteer that the
		 * edges identifiers will be in range [0, edges()), and expose the edges
		 * identifiers by the edgesIDs().
		 */

		public void removeEdge(int e);

		public IntIterator edgesIDs();

		public static interface Undirected extends Removeable, Graph.Undirected {

			default void removeEdges(int u) {
				for (EdgeIter it = edges(u); it.hasNext();) {
					it.nextInt();
					it.remove();
				}
			}
		}

		public static interface Directed extends Removeable, Graph.Directed {

			default void removeEdgesOut(int u) {
				for (EdgeIter it = edgesOut(u); it.hasNext();) {
					it.nextInt();
					it.remove();
				}
			}

			default void removeEdgesIn(int v) {
				for (EdgeIter it = edgesIn(v); it.hasNext();) {
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
