/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jgalgo.graph;

import java.util.EnumSet;
import java.util.List;

class IndexGraphFactoryImpl implements IndexGraphFactory {

	boolean directed;
	private boolean selfEdges;
	private boolean parallelEdges;
	int expectedVerticesNum;
	int expectedEdgesNum;
	private final EnumSet<GraphFactory.Hint> hints = EnumSet.noneOf(GraphFactory.Hint.class);
	private String impl;

	IndexGraphFactoryImpl(boolean directed) {
		this.directed = directed;
	}

	IndexGraphFactoryImpl(IndexGraph g) {
		this.directed = g.isDirected();
		this.selfEdges = g.isAllowSelfEdges();
		this.parallelEdges = g.isAllowParallelEdges();
		impl = Graphs.getIndexGraphImpl(g);
	}

	@Override
	public IndexGraph newGraph() {
		return mutableImpl().newGraph(expectedVerticesNum, expectedEdgesNum);
	}

	@Override
	public IndexGraph newCopyOf(Graph<Integer, Integer> g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		setDirected(g.isDirected());
		return mutableImpl().newCopyOf((IndexGraph) g, copyVerticesWeights, copyEdgesWeights);
	}

	@Override
	public IndexGraphBuilder newBuilder() {
		IndexGraphBuilderImpl builder =
				directed ? new IndexGraphBuilderImpl.Directed() : new IndexGraphBuilderImpl.Undirected();
		builder.setMutableImpl(mutableImpl());
		builder.setImmutableImpl(immutableImpl());
		return builder;
	}

	static interface Impl {

		IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum);

		IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights);

		IndexGraph newFromBuilder(IndexGraphBuilderImpl builder);

	}

	Impl mutableImpl() {
		Impl arrayImpl = directed ? new Impl() {

			@Override
			public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
				return new GraphArrayDirected(expectedVerticesNum, expectedEdgesNum);
			}

			@Override
			public IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights) {
				return new GraphArrayDirected(graph, copyVerticesWeights, copyEdgesWeights);
			}

			@Override
			public IndexGraph newFromBuilder(IndexGraphBuilderImpl builder) {
				return new GraphArrayDirected((IndexGraphBuilderImpl.Directed) builder);
			}
		} : new Impl() {

			@Override
			public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
				return new GraphArrayUndirected(expectedVerticesNum, expectedEdgesNum);
			}

			@Override
			public IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights) {
				return new GraphArrayUndirected(graph, copyVerticesWeights, copyEdgesWeights);
			}

			@Override
			public IndexGraph newFromBuilder(IndexGraphBuilderImpl builder) {
				return new GraphArrayUndirected((IndexGraphBuilderImpl.Undirected) builder);
			}
		};
		Impl linkedImpl = directed ? new Impl() {

			@Override
			public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
				return new GraphLinkedDirected(expectedVerticesNum, expectedEdgesNum);
			}

			@Override
			public IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights) {
				return new GraphLinkedDirected(graph, copyVerticesWeights, copyEdgesWeights);
			}

			@Override
			public IndexGraph newFromBuilder(IndexGraphBuilderImpl builder) {
				return new GraphLinkedDirected((IndexGraphBuilderImpl.Directed) builder);
			}
		} : new Impl() {

			@Override
			public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
				return new GraphLinkedUndirected(expectedVerticesNum, expectedEdgesNum);
			}

			@Override
			public IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights) {
				return new GraphLinkedUndirected(graph, copyVerticesWeights, copyEdgesWeights);
			}

			@Override
			public IndexGraph newFromBuilder(IndexGraphBuilderImpl builder) {
				return new GraphLinkedUndirected((IndexGraphBuilderImpl.Undirected) builder);
			}
		};
		Impl hashtableImpl = directed ? new Impl() {

			@Override
			public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
				return new GraphHashmapDirected(expectedVerticesNum, expectedEdgesNum);
			}

			@Override
			public IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights) {
				return new GraphHashmapDirected(graph, copyVerticesWeights, copyEdgesWeights);
			}

			@Override
			public IndexGraph newFromBuilder(IndexGraphBuilderImpl builder) {
				return new GraphHashmapDirected((IndexGraphBuilderImpl.Directed) builder);
			}
		} : new Impl() {

			@Override
			public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
				return new GraphHashmapUndirected(expectedVerticesNum, expectedEdgesNum);
			}

			@Override
			public IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights) {
				return new GraphHashmapUndirected(graph, copyVerticesWeights, copyEdgesWeights);
			}

			@Override
			public IndexGraph newFromBuilder(IndexGraphBuilderImpl builder) {
				return new GraphHashmapUndirected((IndexGraphBuilderImpl.Undirected) builder);
			}
		};
		Impl matrixImpl = directed ? new Impl() {

			@Override
			public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
				return new GraphMatrixDirected(expectedVerticesNum, expectedEdgesNum);
			}

			@Override
			public IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights) {
				return new GraphMatrixDirected(graph, copyVerticesWeights, copyEdgesWeights);
			}

			@Override
			public IndexGraph newFromBuilder(IndexGraphBuilderImpl builder) {
				return new GraphMatrixDirected((IndexGraphBuilderImpl.Directed) builder);
			}
		} : new Impl() {

			@Override
			public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
				return new GraphMatrixUndirected(expectedVerticesNum, expectedEdgesNum);
			}

			@Override
			public IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights) {
				return new GraphMatrixUndirected(graph, copyVerticesWeights, copyEdgesWeights);
			}

			@Override
			public IndexGraph newFromBuilder(IndexGraphBuilderImpl builder) {
				return new GraphMatrixUndirected((IndexGraphBuilderImpl.Undirected) builder);
			}
		};

		if (impl != null) {
			switch (impl) {
				case "array":
					return arrayImpl;
				case "linked-list":
					return linkedImpl;
				case "hashtable":
					return hashtableImpl;
				case "matrix":
					return matrixImpl;
				default:
					throw new IllegalArgumentException("unknown 'impl' value: " + impl);
			}
		} else {
			if (hints.contains(GraphFactory.Hint.FastEdgeLookup) && !parallelEdges)
				return hashtableImpl;

			if (hints.containsAll(List.of(GraphFactory.Hint.FastEdgeLookup, GraphFactory.Hint.DenseGraph)) && !selfEdges
					&& !parallelEdges)
				return matrixImpl;

			if (hints.contains(GraphFactory.Hint.FastEdgeRemoval) && !selfEdges)
				return linkedImpl;

			return arrayImpl;
		}
	}

	private static interface ImplImmutable extends Impl {
		@Override
		default IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
			throw new UnsupportedOperationException();
		}
	}

	Impl immutableImpl() {
		Impl csrImpl = directed ? new ImplImmutable() {
			@Override
			public IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights) {
				return new GraphCSRDirected(graph, copyVerticesWeights, copyEdgesWeights);
			}

			@Override
			public IndexGraph newFromBuilder(IndexGraphBuilderImpl builder) {
				IndexGraphBuilderImpl.Directed builder0 = (IndexGraphBuilderImpl.Directed) builder;
				GraphCSRBase.BuilderProcessEdgesDirected processEdges =
						GraphCSRBase.BuilderProcessEdgesDirected.valueOf(builder0);
				return new GraphCSRDirected(builder0, processEdges);
			}
		} : new ImplImmutable() {
			@Override
			public IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights) {
				return new GraphCSRUndirected(graph, copyVerticesWeights, copyEdgesWeights);
			}

			@Override
			public IndexGraph newFromBuilder(IndexGraphBuilderImpl builder) {
				IndexGraphBuilderImpl.Undirected builder0 = (IndexGraphBuilderImpl.Undirected) builder;
				GraphCSRBase.BuilderProcessEdgesUndirected processEdges =
						GraphCSRBase.BuilderProcessEdgesUndirected.valueOf(builder0);
				return new GraphCSRUndirected(builder0, processEdges);
			}
		};
		return csrImpl;
	}

	@Override
	public IndexGraphFactory setDirected(boolean directed) {
		this.directed = directed;
		return this;
	}

	@Override
	public IndexGraphFactory allowSelfEdges(boolean selfEdges) {
		this.selfEdges = selfEdges;
		return this;
	}

	@Override
	public IndexGraphFactory allowParallelEdges(boolean parallelEdges) {
		this.parallelEdges = parallelEdges;
		return this;
	}

	@Override
	public IndexGraphFactory expectedVerticesNum(int expectedVerticesNum) {
		if (expectedVerticesNum < 0)
			throw new IllegalArgumentException("invalid expected size: " + expectedVerticesNum);
		this.expectedVerticesNum = expectedVerticesNum;
		return this;
	}

	@Override
	public IndexGraphFactory expectedEdgesNum(int expectedEdgesNum) {
		if (expectedEdgesNum < 0)
			throw new IllegalArgumentException("invalid expected size: " + expectedEdgesNum);
		this.expectedEdgesNum = expectedEdgesNum;
		return this;
	}

	@Override
	public IndexGraphFactory addHint(GraphFactory.Hint hint) {
		hints.add(hint);
		return this;
	}

	@Override
	public IndexGraphFactory removeHint(GraphFactory.Hint hint) {
		hints.remove(hint);
		return this;
	}

	@Override
	public IndexGraphFactory setOption(String key, Object value) {
		switch (key) {
			case "impl":
				impl = (String) value;
				break;
			default:
				IndexGraphFactory.super.setOption(key, value);
		}
		return this;
	}

}
