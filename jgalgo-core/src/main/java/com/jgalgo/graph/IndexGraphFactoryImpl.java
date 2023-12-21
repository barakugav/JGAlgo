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
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;

class IndexGraphFactoryImpl implements IndexGraphFactory {

	private boolean directed;
	private boolean selfEdges;
	private boolean parallelEdges = true;
	int expectedVerticesNum;
	int expectedEdgesNum;
	private final EnumSet<GraphFactory.Hint> hints = EnumSet.noneOf(GraphFactory.Hint.class);
	private String impl;

	IndexGraphFactoryImpl(boolean directed) {
		this.directed = directed;
	}

	@Override
	public IndexGraph newGraph() {
		return mutableImpl().newGraph(expectedVerticesNum, expectedEdgesNum);
	}

	@Override
	public IndexGraph newCopyOf(Graph<Integer, Integer> g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		if (directed != g.isDirected())
			throw new IllegalArgumentException("graph is " + (g.isDirected() ? "directed" : "undirected")
					+ " while factory is " + (directed ? "directed" : "undirected"));
		return mutableImpl().newCopyOf((IndexGraph) g, copyVerticesWeights, copyEdgesWeights);
	}

	@Override
	public IndexGraphBuilder newBuilder() {
		IndexGraphBuilderImpl builder = new IndexGraphBuilderImpl(directed);
		builder.setMutableImpl(mutableImpl());
		builder.setImmutableImpl(immutableImpl());
		return builder;
	}

	@Override
	public IndexGraphBuilder newBuilderCopyOf(Graph<Integer, Integer> g, boolean copyVerticesWeights,
			boolean copyEdgesWeights) {
		IndexGraphBuilderImpl builder = new IndexGraphBuilderImpl(g, copyVerticesWeights, copyEdgesWeights);
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
		Boolean2ObjectFunction<Impl> arrayImplFactory = selfEdges -> {
			return directed ? new Impl() {

				@Override
				public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
					return new GraphArrayDirected(selfEdges, expectedVerticesNum, expectedEdgesNum);
				}

				@Override
				public IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights) {
					return new GraphArrayDirected(selfEdges, graph, copyVerticesWeights, copyEdgesWeights);
				}

				@Override
				public IndexGraph newFromBuilder(IndexGraphBuilderImpl builder) {
					return new GraphArrayDirected(selfEdges, builder);
				}
			} : new Impl() {

				@Override
				public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
					return new GraphArrayUndirected(selfEdges, expectedVerticesNum, expectedEdgesNum);
				}

				@Override
				public IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights) {
					return new GraphArrayUndirected(selfEdges, graph, copyVerticesWeights, copyEdgesWeights);
				}

				@Override
				public IndexGraph newFromBuilder(IndexGraphBuilderImpl builder) {
					return new GraphArrayUndirected(selfEdges, builder);
				}
			};
		};
		Impl arrayImpl = arrayImplFactory.get(false);
		Impl arrayImplWithSelfEdges = arrayImplFactory.get(true);

		Boolean2ObjectFunction<Impl> linkedImplFactory = selfEdges -> {
			return directed ? new Impl() {

				@Override
				public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
					return new GraphLinkedDirected(selfEdges, expectedVerticesNum, expectedEdgesNum);
				}

				@Override
				public IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights) {
					return new GraphLinkedDirected(selfEdges, graph, copyVerticesWeights, copyEdgesWeights);
				}

				@Override
				public IndexGraph newFromBuilder(IndexGraphBuilderImpl builder) {
					return new GraphLinkedDirected(selfEdges, builder);
				}
			} : new Impl() {

				@Override
				public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
					return new GraphLinkedUndirected(selfEdges, expectedVerticesNum, expectedEdgesNum);
				}

				@Override
				public IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights) {
					return new GraphLinkedUndirected(selfEdges, graph, copyVerticesWeights, copyEdgesWeights);
				}

				@Override
				public IndexGraph newFromBuilder(IndexGraphBuilderImpl builder) {
					return new GraphLinkedUndirected(selfEdges, builder);
				}
			};
		};
		Impl linkedImpl = linkedImplFactory.get(false);
		Impl linkedImplWithSelfEdges = linkedImplFactory.get(true);
		Boolean2ObjectFunction<Impl> linkedPtrImplFactory = selfEdges -> {
			return directed ? new Impl() {

				@Override
				public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
					return new GraphLinkedPtrDirected(selfEdges, expectedVerticesNum, expectedEdgesNum);
				}

				@Override
				public IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights) {
					return new GraphLinkedPtrDirected(selfEdges, graph, copyVerticesWeights, copyEdgesWeights);
				}

				@Override
				public IndexGraph newFromBuilder(IndexGraphBuilderImpl builder) {
					return new GraphLinkedPtrDirected(selfEdges, builder);
				}
			} : new Impl() {

				@Override
				public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
					return new GraphLinkedPtrUndirected(selfEdges, expectedVerticesNum, expectedEdgesNum);
				}

				@Override
				public IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights) {
					return new GraphLinkedPtrUndirected(selfEdges, graph, copyVerticesWeights, copyEdgesWeights);
				}

				@Override
				public IndexGraph newFromBuilder(IndexGraphBuilderImpl builder) {
					return new GraphLinkedPtrUndirected(selfEdges, builder);
				}
			};
		};
		Impl linkedPtrImpl = linkedPtrImplFactory.get(false);
		Impl linkedPtrImplWithSelfEdges = linkedPtrImplFactory.get(true);
		Boolean2ObjectFunction<Impl> hashtableImplFactory = selfEdges -> {
			return directed ? new Impl() {

				@Override
				public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
					return new GraphHashmapDirected(selfEdges, expectedVerticesNum, expectedEdgesNum);
				}

				@Override
				public IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights) {
					return new GraphHashmapDirected(selfEdges, graph, copyVerticesWeights, copyEdgesWeights);
				}

				@Override
				public IndexGraph newFromBuilder(IndexGraphBuilderImpl builder) {
					return new GraphHashmapDirected(selfEdges, builder);
				}
			} : new Impl() {

				@Override
				public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
					return new GraphHashmapUndirected(selfEdges, expectedVerticesNum, expectedEdgesNum);
				}

				@Override
				public IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights) {
					return new GraphHashmapUndirected(selfEdges, graph, copyVerticesWeights, copyEdgesWeights);
				}

				@Override
				public IndexGraph newFromBuilder(IndexGraphBuilderImpl builder) {
					return new GraphHashmapUndirected(selfEdges, builder);
				}
			};
		};
		Impl hashtableImpl = hashtableImplFactory.get(false);
		Impl hashtableImplWithSelfEdges = hashtableImplFactory.get(true);
		Boolean2ObjectFunction<Impl> hashtableMultiImplFactory = selfEdges -> {
			return directed ? new Impl() {

				@Override
				public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
					return new GraphHashmapMultiDirected(selfEdges, expectedVerticesNum, expectedEdgesNum);
				}

				@Override
				public IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights) {
					return new GraphHashmapMultiDirected(selfEdges, graph, copyVerticesWeights, copyEdgesWeights);
				}

				@Override
				public IndexGraph newFromBuilder(IndexGraphBuilderImpl builder) {
					return new GraphHashmapMultiDirected(selfEdges, builder);
				}
			} : new Impl() {

				@Override
				public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
					return new GraphHashmapMultiUndirected(selfEdges, expectedVerticesNum, expectedEdgesNum);
				}

				@Override
				public IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights) {
					return new GraphHashmapMultiUndirected(selfEdges, graph, copyVerticesWeights, copyEdgesWeights);
				}

				@Override
				public IndexGraph newFromBuilder(IndexGraphBuilderImpl builder) {
					return new GraphHashmapMultiUndirected(selfEdges, builder);
				}
			};
		};
		Impl hashtableMultiImpl = hashtableMultiImplFactory.get(false);
		Impl hashtableMultiImplWithSelfEdges = hashtableMultiImplFactory.get(true);
		Boolean2ObjectFunction<Impl> matrixImplFactory = selfEdges -> {
			return directed ? new Impl() {

				@Override
				public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
					return new GraphMatrixDirected(selfEdges, expectedVerticesNum, expectedEdgesNum);
				}

				@Override
				public IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights) {
					return new GraphMatrixDirected(selfEdges, graph, copyVerticesWeights, copyEdgesWeights);
				}

				@Override
				public IndexGraph newFromBuilder(IndexGraphBuilderImpl builder) {
					return new GraphMatrixDirected(selfEdges, builder);
				}
			} : new Impl() {

				@Override
				public IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum) {
					return new GraphMatrixUndirected(selfEdges, expectedVerticesNum, expectedEdgesNum);
				}

				@Override
				public IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights) {
					return new GraphMatrixUndirected(selfEdges, graph, copyVerticesWeights, copyEdgesWeights);
				}

				@Override
				public IndexGraph newFromBuilder(IndexGraphBuilderImpl builder) {
					return new GraphMatrixUndirected(selfEdges, builder);
				}
			};
		};
		Impl matrixImpl = matrixImplFactory.get(false);
		Impl matrixImplWithSelfEdges = matrixImplFactory.get(true);

		if (impl != null) {
			switch (impl) {
				case "array":
					return arrayImpl;
				case "array-selfedges":
					return arrayImplWithSelfEdges;
				case "linked-list":
					return linkedImpl;
				case "linked-list-selfedges":
					return linkedImplWithSelfEdges;
				case "linked-list-ptr":
					return linkedPtrImpl;
				case "linked-list-ptr-selfedges":
					return linkedPtrImplWithSelfEdges;
				case "hashtable":
					return hashtableImpl;
				case "hashtable-selfedges":
					return hashtableImplWithSelfEdges;
				case "hashtable-multi":
					return hashtableMultiImpl;
				case "hashtable-multi-selfedges":
					return hashtableMultiImplWithSelfEdges;
				case "matrix":
					return matrixImpl;
				case "matrix-selfedges":
					return matrixImplWithSelfEdges;
				default:
					throw new IllegalArgumentException("unknown 'impl' value: " + impl);
			}
		} else {
			boolean dense = hints.contains(GraphFactory.Hint.DenseGraph);
			boolean lookup = hints.contains(GraphFactory.Hint.FastEdgeLookup);
			boolean remove = hints.contains(GraphFactory.Hint.FastEdgeRemoval);

			if (parallelEdges) {
				if (lookup)
					return selfEdges ? hashtableMultiImplWithSelfEdges : hashtableMultiImpl;
				if (remove)
					return selfEdges ? linkedImplWithSelfEdges : linkedImpl;
				return selfEdges ? arrayImplWithSelfEdges : arrayImpl;

			} else {
				if (dense)
					return selfEdges ? matrixImplWithSelfEdges : matrixImpl;
				return selfEdges ? hashtableImplWithSelfEdges : hashtableImpl;
			}
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
				return new GraphCsrDirected(graph, copyVerticesWeights, copyEdgesWeights);
			}

			@Override
			public IndexGraph newFromBuilder(IndexGraphBuilderImpl builder) {
				GraphCsrBase.BuilderProcessEdgesDirected processEdges =
						GraphCsrBase.BuilderProcessEdgesDirected.valueOf(builder);
				return new GraphCsrDirected(builder, processEdges);
			}
		} : new ImplImmutable() {
			@Override
			public IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights) {
				return new GraphCsrUndirected(graph, copyVerticesWeights, copyEdgesWeights);
			}

			@Override
			public IndexGraph newFromBuilder(IndexGraphBuilderImpl builder) {
				GraphCsrBase.BuilderProcessEdgesUndirected processEdges =
						GraphCsrBase.BuilderProcessEdgesUndirected.valueOf(builder);
				return new GraphCsrUndirected(builder, processEdges);
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
