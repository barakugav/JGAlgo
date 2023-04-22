package com.jgalgo;

/**
 * A graph builder.
 * <p>
 * A graph builder can be used as a factory for multiple graphs with the same
 * implementation, to specified the initial number of vertices or to modify the
 * edge IDs strategy. In this following example, a builder is used to build
 * multiple directed graphs based on linked edge list implementation with fixed
 * edge IDs and {@code 15} initial vertices:
 *
 * <pre> {@code
 * GraphBuilder builder = new GraphBuilder.Linked();
 * builder.setVerticesNum(15);
 * builder.setEdgesIDStrategy(IDStrategy.Fixed.class);
 *
 * DiGraph g1 = builder.buildDirected();
 * DiGraph g2 = builder.buildDirected();
 * DiGraph g3 = builder.buildDirected();
 * ...
 * }</pre>
 *
 * @author Barak Ugav
 */
public interface GraphBuilder {

	/**
	 * Set the number of initial vertices in the graph.
	 * <p>
	 * The default value is zero.
	 *
	 * @param n number of initial vertices in the graph
	 * @return this builder
	 */
	GraphBuilder setVerticesNum(int n);

	/**
	 * Set the edges ID strategy of this builder.
	 * <p>
	 * The default strategy used by this builder is {@link IDStrategy.Continues},
	 * namely the edges IDs will always be {@code [0,1,2,...,edgesNum-1]}. This
	 * default strategy may perform some IDs rename to maintain its invariant during
	 * the lifetime of the graph. A different strategy such as
	 * {@link IDStrategy.Fixed} may be used to ensure no IDs rename are performed.
	 *
	 * @param edgesIDStrategy type of edge ID strategy to use, or {@code null} for
	 *                        default strategy
	 * @return this builder
	 * @throws IllegalArgumentException if the strategy type is not supported
	 * @see IDStrategy
	 */
	GraphBuilder setEdgesIDStrategy(Class<? extends IDStrategy> edgesIDStrategy);

	/**
	 * Build an undirected graph with the options of this builder.
	 *
	 * @return a new undirected graph with the options of this builder.
	 */
	UGraph buildUndirected();

	/**
	 * Build a directed graph with the options of this builder.
	 *
	 * @return a new directed graph with the options of this builder.
	 */
	DiGraph buildDirected();

	/**
	 * Get a new builder instance.
	 *
	 * @return a new builder with default options.
	 */
	static GraphBuilder newInstance() {
		return new GraphBuilder.Array();
	}

	/**
	 * Graph builder based on array graph implementations such as
	 * {@link GraphArrayDirected} and {@link GraphArrayUndirected}.
	 *
	 * @author Barak Ugav
	 */
	static class Array extends GraphBuilderImpl.Abstract {
		/**
		 * Create a new builder with default options.
		 */
		public Array() {
		}

		@Override
		public UGraph buildUndirected() {
			return (UGraph) wrapWithCustomIDStrategies(new GraphArrayUndirected(verticesNum));
		}

		@Override
		public DiGraph buildDirected() {
			return (DiGraph) wrapWithCustomIDStrategies(new GraphArrayDirected(verticesNum));
		}
	}

	/**
	 * Graph builder based on array graph implementations such as
	 * {@link GraphLinkedDirected} and {@link GraphLinkedUndirected}.
	 *
	 * @author Barak Ugav
	 */
	static class Linked extends GraphBuilderImpl.Abstract {
		/**
		 * Create a new builder with default options.
		 */
		public Linked() {
		}

		@Override
		public UGraph buildUndirected() {
			return (UGraph) wrapWithCustomIDStrategies(new GraphLinkedUndirected(verticesNum));
		}

		@Override
		public DiGraph buildDirected() {
			return (DiGraph) wrapWithCustomIDStrategies(new GraphLinkedDirected(verticesNum));
		}
	}

	/**
	 * Graph builder based on array graph implementations such as
	 * {@link GraphTableDirected} and {@link GraphTableUndirected}.
	 *
	 * @author Barak Ugav
	 */
	static class Table extends GraphBuilderImpl.Abstract {
		/**
		 * Create a new builder with default options.
		 */
		public Table() {
		}

		@Override
		public UGraph buildUndirected() {
			return (UGraph) wrapWithCustomIDStrategies(new GraphTableUndirected(verticesNum));
		}

		@Override
		public DiGraph buildDirected() {
			return (DiGraph) wrapWithCustomIDStrategies(new GraphTableDirected(verticesNum));
		}
	}

}
