package com.jgalgo;

public interface GraphBuilder {

	GraphBuilder setVerticesNum(int n);

	GraphBuilder setEdgesIDStrategy(Class<? extends IDStrategy> edgesIDStrategy);

	UGraph buildUndirected();

	DiGraph buildDirected();

	static GraphBuilder newInstance() {
		return new GraphBuilderImpl.Array();
	}

	static GraphBuilder newInstance(String impl) {
		switch (impl) {
			case "com.jgalgo.Array":
				return new GraphBuilderImpl.Array();
			case "com.jgalgo.Linked":
				return new GraphBuilderImpl.Linked();
			case "com.jgalgo.Table":
				return new GraphBuilderImpl.Table();
			default:
				throw new IllegalArgumentException("Unexpected value: " + impl);
		}
	}

}
