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
import java.util.Optional;
import com.jgalgo.graph.IndexGraphBuilder.ReIndexedGraph;
import com.jgalgo.graph.IndexGraphBuilderImpl.ReIndexedGraphImpl;
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
	public IndexGraph newImmutableCopyOf(Graph<Integer, Integer> g, boolean copyVerticesWeights,
			boolean copyEdgesWeights) {
		if (directed != g.isDirected())
			throw new IllegalArgumentException("graph is " + (g.isDirected() ? "directed" : "undirected")
					+ " while factory is " + (directed ? "directed" : "undirected"));
		return immutableImpl().newCopyOf((IndexGraph) g, copyVerticesWeights, copyEdgesWeights);
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

	static interface MutableImpl {

		IndexGraph newGraph(int expectedVerticesNum, int expectedEdgesNum);

		IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights);

		IndexGraph newFromBuilder(IndexGraphBuilderImpl builder);

		default IndexGraphBuilder.ReIndexedGraph newFromBuilderWithReIndex(IndexGraphBuilderImpl builder,
				boolean reIndexVertices, boolean reIndexEdges) {
			return new ReIndexedGraphImpl(newFromBuilder(builder), Optional.empty(), Optional.empty());
		}

	}

	MutableImpl mutableImpl() {
		Boolean2ObjectFunction<MutableImpl> arrayImplFactory = selfEdges -> {
			return directed ? new MutableImpl() {

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
			} : new MutableImpl() {

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
		MutableImpl arrayImpl = arrayImplFactory.get(false);
		MutableImpl arrayImplWithSelfEdges = arrayImplFactory.get(true);

		Boolean2ObjectFunction<MutableImpl> linkedImplFactory = selfEdges -> {
			return directed ? new MutableImpl() {

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
			} : new MutableImpl() {

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
		MutableImpl linkedImpl = linkedImplFactory.get(false);
		MutableImpl linkedImplWithSelfEdges = linkedImplFactory.get(true);
		Boolean2ObjectFunction<MutableImpl> linkedPtrImplFactory = selfEdges -> {
			return directed ? new MutableImpl() {

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
			} : new MutableImpl() {

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
		MutableImpl linkedPtrImpl = linkedPtrImplFactory.get(false);
		MutableImpl linkedPtrImplWithSelfEdges = linkedPtrImplFactory.get(true);
		Boolean2ObjectFunction<MutableImpl> hashtableImplFactory = selfEdges -> {
			return directed ? new MutableImpl() {

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
			} : new MutableImpl() {

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
		MutableImpl hashtableImpl = hashtableImplFactory.get(false);
		MutableImpl hashtableImplWithSelfEdges = hashtableImplFactory.get(true);
		Boolean2ObjectFunction<MutableImpl> hashtableMultiImplFactory = selfEdges -> {
			return directed ? new MutableImpl() {

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
			} : new MutableImpl() {

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
		MutableImpl hashtableMultiImpl = hashtableMultiImplFactory.get(false);
		MutableImpl hashtableMultiImplWithSelfEdges = hashtableMultiImplFactory.get(true);
		Boolean2ObjectFunction<MutableImpl> matrixImplFactory = selfEdges -> {
			return directed ? new MutableImpl() {

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
			} : new MutableImpl() {

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
		MutableImpl matrixImpl = matrixImplFactory.get(false);
		MutableImpl matrixImplWithSelfEdges = matrixImplFactory.get(true);

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

	static interface ImmutableImpl {

		IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights);

		IndexGraph newFromBuilder(IndexGraphBuilderImpl builder);

		IndexGraphBuilder.ReIndexedGraph newCopyOfWithReIndex(IndexGraph graph, boolean reIndexVertices,
				boolean reIndexEdges, boolean copyVerticesWeights, boolean copyEdgesWeights);

		IndexGraphBuilder.ReIndexedGraph newFromBuilderWithReIndex(IndexGraphBuilderImpl builder,
				boolean reIndexVertices, boolean reIndexEdges);

	}

	ImmutableImpl immutableImpl() {
		Boolean2ObjectFunction<ImmutableImpl> csrImplFactory = fastLookup -> {
			return directed ? new ImmutableImpl() {
				@Override
				public IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights) {
					if (graph instanceof GraphCsrDirectedReindexed) {
						IndexGraphBuilder.ReIndexedGraph reIndexedGraph = GraphCsrDirectedReindexed.newInstance(graph,
								copyVerticesWeights, copyEdgesWeights, fastLookup);
						assert reIndexedGraph.verticesReIndexing().isEmpty()
								&& reIndexedGraph.edgesReIndexing().isEmpty();
						return reIndexedGraph.graph();
					} else {
						return new GraphCsrDirected(graph, copyVerticesWeights, copyEdgesWeights, fastLookup);
					}
				}

				@Override
				public IndexGraphBuilder.ReIndexedGraph newCopyOfWithReIndex(IndexGraph graph, boolean reIndexVertices,
						boolean reIndexEdges, boolean copyVerticesWeights, boolean copyEdgesWeights) {
					if (reIndexEdges) {
						return GraphCsrDirectedReindexed.newInstance(graph, copyVerticesWeights, copyEdgesWeights,
								fastLookup);
					} else {
						return new ReIndexedGraphImpl(newCopyOf(graph, copyVerticesWeights, copyEdgesWeights),
								Optional.empty(), Optional.empty());
					}
				}

				@Override
				public IndexGraph newFromBuilder(IndexGraphBuilderImpl builder) {
					GraphCsrBase.BuilderProcessEdgesDirected processEdges =
							GraphCsrBase.BuilderProcessEdgesDirected.valueOf(builder);
					return new GraphCsrDirected(builder, processEdges, fastLookup);
				}

				@Override
				public ReIndexedGraph newFromBuilderWithReIndex(IndexGraphBuilderImpl builder, boolean reIndexVertices,
						boolean reIndexEdges) {
					if (reIndexEdges) {
						return GraphCsrDirectedReindexed.newInstance(builder, fastLookup);
					} else {
						return new ReIndexedGraphImpl(newFromBuilder(builder), Optional.empty(), Optional.empty());
					}
				}
			} : new ImmutableImpl() {
				@Override
				public IndexGraph newCopyOf(IndexGraph graph, boolean copyVerticesWeights, boolean copyEdgesWeights) {
					return new GraphCsrUndirected(graph, copyVerticesWeights, copyEdgesWeights, fastLookup);
				}

				@Override
				public IndexGraphBuilder.ReIndexedGraph newCopyOfWithReIndex(IndexGraph graph, boolean reIndexVertices,
						boolean reIndexEdges, boolean copyVerticesWeights, boolean copyEdgesWeights) {
					/* no re-indexing for undirected graph */
					return new ReIndexedGraphImpl(newCopyOf(graph, copyVerticesWeights, copyEdgesWeights),
							Optional.empty(), Optional.empty());
				}

				@Override
				public IndexGraph newFromBuilder(IndexGraphBuilderImpl builder) {
					GraphCsrBase.BuilderProcessEdgesUndirected processEdges =
							GraphCsrBase.BuilderProcessEdgesUndirected.valueOf(builder);
					return new GraphCsrUndirected(builder, processEdges, fastLookup);
				}

				@Override
				public ReIndexedGraph newFromBuilderWithReIndex(IndexGraphBuilderImpl builder, boolean reIndexVertices,
						boolean reIndexEdges) {
					/* no re-indexing for undirected graph */
					return new ReIndexedGraphImpl(newFromBuilder(builder), Optional.empty(), Optional.empty());
				}
			};
		};
		ImmutableImpl csrImpl = csrImplFactory.get(false);
		ImmutableImpl csrImplWithFastLookup = csrImplFactory.get(true);
		if (hints.contains(GraphFactory.Hint.FastEdgeLookup)) {
			return csrImplWithFastLookup;
		} else {
			return csrImpl;
		}
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
