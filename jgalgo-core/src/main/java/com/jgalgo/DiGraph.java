package com.jgalgo;

/**
 * A discrete directed graph with vertices and edges.
 * <p>
 * An extension to the {@link Graph} interface, where edges are directed, namely
 * an edge \(e(u, v)\) will appear in the iteration of {@code edgesOut(u)}
 * and {@code edgesIn(v)} and will not appear in the iteration of
 * {@code edgesOut(v)} and {@code edgesIn(u)}.
 * <p>
 * Use {@link GraphArrayDirected} as a default implementation of directed
 * graphs, its the most efficient for most use cases.
 *
 * @see UGraph
 * @see GraphArrayDirected
 * @author Barak Ugav
 */
public interface DiGraph extends Graph {

	/**
	 * Reverse an edge by switching its source and target.
	 *
	 * @param edge an existing edge in the graph
	 * @throws IndexOutOfBoundsException if {@code edge} is not a valid edge
	 *                                   identifier
	 */
	public void reverseEdge(int edge);

	/**
	 * Create a directed graph builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link DiGraph}
	 * object.
	 *
	 * @return a new builder that can build {@link DiGraph} objects
	 */
	static DiGraph.Builder newBuilder() {
		return new GraphBuilderImpl.ArrayDirected();
	}

	/**
	 * A builder for {@link DiGraph} objects.
	 *
	 * @see DiGraph#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder extends Graph.Builder {

		/**
		 * Create a new directed graph.
		 *
		 * @return a new directed graph with the builder options
		 */
		@Override
		DiGraph build();

		@Override
		DiGraph.Builder setVerticesNum(int n);

		@Override
		DiGraph.Builder setEdgesIDStrategy(Class<? extends IDStrategy> edgesIDStrategy);
	}

}
