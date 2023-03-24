package com.ugav.algo;

import java.util.function.Supplier;

public abstract class GraphBuilder {

	int verticesNum;
	Supplier<IDStrategy> verticesIDStrategy = DefaultVerticesIDStrategy;
	Supplier<IDStrategy> edgesIDStrategy = DefaultEdgesIDStrategy;
	private static final Supplier<IDStrategy> DefaultVerticesIDStrategy = () -> null;
	private static final Supplier<IDStrategy> DefaultEdgesIDStrategy = () -> null;

	private GraphBuilder() {
	}

	public abstract UGraph buildUndirected();

	public abstract DiGraph buildDirected();

	public GraphBuilder setVerticesNum(int n) {
		if (n < 0)
			throw new IllegalArgumentException();
		verticesNum = n;
		return this;
	}

	public GraphBuilder setVerticesIDStrategy(Supplier<IDStrategy> factory) {
		verticesIDStrategy = factory != null ? factory : DefaultVerticesIDStrategy;
		return this;
	}

	public GraphBuilder setEdgesIDStrategy(Supplier<IDStrategy> factory) {
		edgesIDStrategy = factory != null ? factory : DefaultEdgesIDStrategy;
		return this;
	}

	public static class Array extends GraphBuilder {
		private Array() {
		}

		public static GraphBuilder.Linked getInstance() {
			return new GraphBuilder.Linked();
		}

		@Override
		public UGraph buildUndirected() {
			return new GraphArrayUndirected(verticesNum, verticesIDStrategy.get(), edgesIDStrategy.get());
		}

		@Override
		public DiGraph buildDirected() {
			return new GraphArrayDirected(verticesNum, verticesIDStrategy.get(), edgesIDStrategy.get());
		}
	}

	public static class Linked extends GraphBuilder {
		private Linked() {
		}

		public static GraphBuilder.Linked getInstance() {
			return new GraphBuilder.Linked();
		}

		@Override
		public UGraph buildUndirected() {
			return new GraphLinkedUndirected(verticesNum, verticesIDStrategy.get(), edgesIDStrategy.get());
		}

		@Override
		public DiGraph buildDirected() {
			return new GraphLinkedDirected(verticesNum, verticesIDStrategy.get(), edgesIDStrategy.get());
		}
	}

	public static class Table extends GraphBuilder {
		private Table() {
		}

		public static GraphBuilder.Table getInstance() {
			return new GraphBuilder.Table();
		}

		@Override
		public UGraph buildUndirected() {
			if (verticesIDStrategy != DefaultVerticesIDStrategy)
				System.out.println("Table graphs do not support custom vertices IDs strategy, ignoring.");
			return new GraphTableUndirected(verticesNum, edgesIDStrategy.get());
		}

		@Override
		public DiGraph buildDirected() {
			if (verticesIDStrategy != DefaultVerticesIDStrategy)
				System.out.println("Table graphs do not support custom vertices IDs strategy, ignoring.");
			return new GraphTableDirected(verticesNum, edgesIDStrategy.get());
		}
	}

}
