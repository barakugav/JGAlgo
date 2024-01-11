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

import static com.jgalgo.internal.util.Range.range;
import java.util.Collection;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import com.jgalgo.graph.Graphs.ImmutableGraph;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.JGAlgoUtils.Variant2;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;

abstract class GraphCsrBase extends IndexGraphBase implements ImmutableGraph {

	final int[] edgesOutBegin;

	final Map<String, WeightsImpl.IndexImmutable<?>> verticesUserWeights;
	final Map<String, WeightsImpl.IndexImmutable<?>> edgesUserWeights;

	private boolean containsSelfEdges;
	private boolean containsSelfEdgesValid;
	private boolean containsParallelEdges;
	private boolean containsParallelEdgesValid;

	static final Int2IntMap EmptyEdgeMap;

	static {
		Int2IntMap emptyEdgeMap = new Int2IntOpenHashMap(0);
		emptyEdgeMap.defaultReturnValue(-1);
		// emptyEdgeMap = Int2IntMaps.unmodifiable(emptyEdgeMap);
		EmptyEdgeMap = emptyEdgeMap;
	}

	GraphCsrBase(boolean directed, Variant2<IndexGraph, IndexGraphBuilderImpl> graphOrBuilder,
			BuilderProcessEdges processEdges, Optional<IndexGraphBuilder.ReIndexingMap> edgesReIndexing,
			boolean copyVerticesWeights, boolean copyEdgesWeights) {
		super(graphOrBuilder, false);
		final int n = verticesNum(graphOrBuilder);

		final boolean inputIsGraph = graphOrBuilder.contains(IndexGraph.class);
		final Optional<IndexGraph> inputGraph = graphOrBuilder.getOptional(IndexGraph.class);
		final Optional<GraphCsrBase> inputCsrGraph = inputGraph.isPresent() && inputGraph.get() instanceof GraphCsrBase
				? Optional.of((GraphCsrBase) inputGraph.get())
				: Optional.empty();

		if (inputCsrGraph.isPresent()) {
			assert processEdges == null;
			edgeEndpoints = inputCsrGraph.get().edgeEndpoints;
			edgesOutBegin = inputCsrGraph.get().edgesOutBegin;
		} else {
			edgeEndpoints = new long[edgesNum(graphOrBuilder)];
			edgesOutBegin = processEdges.edgesOutBegin;
		}
		assert edgesOutBegin.length == n + 1;

		if (copyVerticesWeights && inputCsrGraph.isPresent()) {
			verticesUserWeights = inputCsrGraph.get().verticesUserWeights;

		} else if (copyVerticesWeights) {
			Set<String> verticesWeightsKeys =
					graphOrBuilder.map(IndexGraph::getVerticesWeightsKeys, b -> b.verticesUserWeights.weightsKeys());
			Function<String, IWeights<?>> getWeights = graphOrBuilder
					.map(g -> key -> (IWeights<?>) g.getVerticesWeights(key), b -> b.verticesUserWeights::getWeights);

			WeightsImpl.IndexImmutable.Builder verticesUserWeightsBuilder =
					new WeightsImpl.IndexImmutable.Builder(vertices, false);
			for (String key : verticesWeightsKeys)
				verticesUserWeightsBuilder.copyAndAddWeights(key, getWeights.apply(key));
			verticesUserWeights = verticesUserWeightsBuilder.build();

		} else {
			verticesUserWeights = Map.of();
		}

		if (copyEdgesWeights && inputCsrGraph.isPresent()) {
			assert edgesReIndexing.isEmpty();
			edgesUserWeights = inputCsrGraph.get().edgesUserWeights;

		} else if (copyEdgesWeights) {
			Set<String> edgesWeightsKeys =
					graphOrBuilder.map(IndexGraph::getEdgesWeightsKeys, b -> b.edgesUserWeights.weightsKeys());
			Function<String, IWeights<?>> getWeights = graphOrBuilder
					.map(g -> key -> (IWeights<?>) g.getEdgesWeights(key), b -> b.edgesUserWeights::getWeights);

			WeightsImpl.IndexImmutable.Builder edgesUserWeightsBuilder =
					new WeightsImpl.IndexImmutable.Builder(edges, true);
			if (edgesReIndexing.isEmpty()) {
				for (String key : edgesWeightsKeys)
					edgesUserWeightsBuilder.copyAndAddWeights(key, getWeights.apply(key));
			} else {
				for (String key : edgesWeightsKeys)
					edgesUserWeightsBuilder
							.copyAndAddWeightsReindexed(key, getWeights.apply(key), edgesReIndexing.get());
			}
			edgesUserWeights = edgesUserWeightsBuilder.build();

		} else {
			edgesUserWeights = Map.of();
		}

		if (inputIsGraph) {
			IndexGraph g = graphOrBuilder.get(IndexGraph.class);
			updateContainsSelfAndParallelEdgesFromCopiedGraph(g);
		}
	}

	GraphCsrBase(boolean directed, IndexGraph g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		super(Variant2.ofA(g), false);
		if (directed) {
			Assertions.onlyDirected(g);
		} else {
			Assertions.onlyUndirected(g);
		}
		final int n = g.vertices().size();
		final int m = g.edges().size();

		if (g instanceof GraphCsrBase) {
			GraphCsrBase gCsr = (GraphCsrBase) g;
			edgesOutBegin = gCsr.edgesOutBegin;
			edgeEndpoints = gCsr.edgeEndpoints;

			verticesUserWeights = copyVerticesWeights ? gCsr.verticesUserWeights : Map.of();
			edgesUserWeights = copyEdgesWeights ? gCsr.edgesUserWeights : Map.of();

		} else {
			edgesOutBegin = new int[n + 1];
			edgeEndpoints = new long[m];
			for (int e : range(m))
				setEndpoints(e, g.edgeSource(e), g.edgeTarget(e));

			WeightsImpl.IndexImmutable.Builder verticesUserWeightsBuilder =
					new WeightsImpl.IndexImmutable.Builder(vertices, false);
			WeightsImpl.IndexImmutable.Builder edgesUserWeightsBuilder =
					new WeightsImpl.IndexImmutable.Builder(edges, true);
			if (copyVerticesWeights)
				for (String key : g.getVerticesWeightsKeys())
					verticesUserWeightsBuilder.copyAndAddWeights(key, (IWeights<?>) g.getVerticesWeights(key));
			if (copyEdgesWeights)
				for (String key : g.getEdgesWeightsKeys())
					edgesUserWeightsBuilder.copyAndAddWeights(key, (IWeights<?>) g.getEdgesWeights(key));
			verticesUserWeights = verticesUserWeightsBuilder.build();
			edgesUserWeights = edgesUserWeightsBuilder.build();
		}

		updateContainsSelfAndParallelEdgesFromCopiedGraph(g);
	}

	private void updateContainsSelfAndParallelEdgesFromCopiedGraph(IndexGraph g) {
		if (g instanceof GraphCsrBase) {
			/* avoid forcing the copied CSR graph to compute if it has self/parallel edges */
			GraphCsrBase gCsr = (GraphCsrBase) g;
			containsSelfEdges = gCsr.containsSelfEdges;
			containsSelfEdgesValid = gCsr.containsSelfEdgesValid;
			containsParallelEdges = gCsr.containsParallelEdges;
			containsParallelEdgesValid = gCsr.containsParallelEdgesValid;

		} else {
			if (!g.isAllowSelfEdges()) {
				containsSelfEdges = false;
				containsSelfEdgesValid = true;
			}
			if (!g.isAllowParallelEdges()) {
				containsParallelEdges = false;
				containsParallelEdgesValid = true;
			}
		}
	}

	@Override
	public final boolean isAllowSelfEdges() {
		if (!containsSelfEdgesValid) {
			containsSelfEdges = false;
			for (int e : range(edges().size())) {
				if (source(e) == target(e)) {
					containsSelfEdges = true;
					break;
				}
			}
			containsSelfEdgesValid = true;
		}
		return containsSelfEdges;
	}

	final boolean containsParallelEdges() {
		if (!containsParallelEdgesValid) {
			containsParallelEdges = containsParallelEdgesImpl();
			containsParallelEdgesValid = true;
		}
		return containsParallelEdges;
	}

	boolean containsParallelEdgesImpl() {
		final int n = vertices().size();
		Bitmap neighborsBitmap = new Bitmap(n);
		IntList neighbors = new IntArrayList();
		for (int u : range(n)) {
			for (IEdgeIter eit = outEdges(u).iterator(); eit.hasNext();) {
				eit.nextInt();
				int v = eit.targetInt();
				if (neighborsBitmap.get(v))
					return true;
				neighborsBitmap.set(v);
				neighbors.add(v);
			}
			neighborsBitmap.clearAllUnsafe(neighbors);
			neighbors.clear();
		}
		return false;
	}

	@Override
	public final boolean isAllowParallelEdges() {
		return containsParallelEdges();
	}

	@Override
	public int addVertexInt() {
		throw new UnsupportedOperationException("graph is immutable, can't add vertex");
	}

	@Override
	public void addVertices(Collection<? extends Integer> vertices) {
		throw new UnsupportedOperationException("graph is immutable, can't add vertices");
	}

	@Override
	public void removeVertex(int vertex) {
		throw new UnsupportedOperationException("graph is immutable, can't remove vertex");
	}

	@Override
	public void removeVertices(Collection<? extends Integer> vertices) {
		throw new UnsupportedOperationException("graph is immutable, can't remove ");
	}

	@Override
	public int addEdge(int source, int target) {
		throw new UnsupportedOperationException("graph is immutable, can't add edge");
	}

	@Override
	public void addEdges(EdgeSet<? extends Integer, ? extends Integer> edges) {
		throw new UnsupportedOperationException("graph is immutable, cannot add edges");
	}

	@Override
	public IntSet addEdgesReassignIds(IEdgeSet edges) {
		throw new UnsupportedOperationException("graph is immutable, cannot add edges");
	}

	@Override
	public void removeEdge(int edge) {
		throw new UnsupportedOperationException("graph is immutable, can't remove edge");
	}

	@Override
	public void removeEdges(Collection<? extends Integer> edges) {
		throw new UnsupportedOperationException("graph is immutable, can't remove edges");
	}

	@Override
	public void removeEdgesOf(int vertex) {
		throw new UnsupportedOperationException("graph is immutable, can't remove edges");
	}

	@Override
	public void removeOutEdgesOf(int source) {
		throw new UnsupportedOperationException("graph is immutable, can't remove edges");
	}

	@Override
	public void removeInEdgesOf(int target) {
		throw new UnsupportedOperationException("graph is immutable, can't remove edges");
	}

	@Override
	public void moveEdge(int edge, int newSource, int newTarget) {
		throw new UnsupportedOperationException("graph is immutable, can't move edge");
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException("graph is immutable, can't clear");
	}

	@Override
	public void clearEdges() {
		throw new UnsupportedOperationException("graph is immutable, can't clear edges");
	}

	@Override
	public void ensureVertexCapacity(int vertexCapacity) {}

	@Override
	public void ensureEdgeCapacity(int edgeCapacity) {}

	@Override
	@SuppressWarnings("unchecked")
	public <T, WeightsT extends Weights<Integer, T>> WeightsT getVerticesWeights(String key) {
		return (WeightsT) verticesUserWeights.get(key);
	}

	@Override
	public Set<String> getVerticesWeightsKeys() {
		return verticesUserWeights.keySet();
	}

	@Override
	public void removeVerticesWeights(String key) {
		throw new UnsupportedOperationException("graph is immutable, can't remove vertices weights");
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T, WeightsT extends Weights<Integer, T>> WeightsT getEdgesWeights(String key) {
		return (WeightsT) edgesUserWeights.get(key);
	}

	@Override
	public Set<String> getEdgesWeightsKeys() {
		return edgesUserWeights.keySet();
	}

	@Override
	public void removeEdgesWeights(String key) {
		throw new UnsupportedOperationException("graph is immutable, can't remove edges weights");
	}

	@Override
	public <T, WeightsT extends Weights<Integer, T>> WeightsT addVerticesWeights(String key, Class<? super T> type,
			T defVal) {
		throw new UnsupportedOperationException("graph is immutable, can't add vertices weights");
	}

	@Override
	public <T, WeightsT extends Weights<Integer, T>> WeightsT addEdgesWeights(String key, Class<? super T> type,
			T defVal) {
		throw new UnsupportedOperationException("graph is immutable, can't add edges weights");
	}

	@Override
	public void addVertexRemoveListener(IndexRemoveListener listener) {
		Objects.requireNonNull(listener);
	}

	@Override
	public void removeVertexRemoveListener(IndexRemoveListener listener) {}

	@Override
	public void addEdgeRemoveListener(IndexRemoveListener listener) {
		Objects.requireNonNull(listener);
	}

	@Override
	public void removeEdgeRemoveListener(IndexRemoveListener listener) {}

	@Override
	public IndexGraph immutableCopy(boolean copyVerticesWeights, boolean copyEdgesWeights) {
		if (verticesUserWeights.isEmpty())
			copyVerticesWeights = true;
		if (edgesUserWeights.isEmpty())
			copyEdgesWeights = true;
		if (copyVerticesWeights && copyEdgesWeights)
			return this;
		return super.immutableCopy(copyVerticesWeights, copyEdgesWeights);
	}

	abstract static class EdgeIterAbstract implements IEdgeIter {

		private final int[] edges;
		private int idx;
		private final int endIdx;
		int lastEdge = -1;

		EdgeIterAbstract(int[] edges, int beginIdx, int endIdx) {
			this.edges = edges;
			this.idx = beginIdx;
			this.endIdx = endIdx;
		}

		@Override
		public boolean hasNext() {
			return idx < endIdx;
		}

		@Override
		public int nextInt() {
			Assertions.hasNext(this);
			return lastEdge = edges[idx++];
		}

		@Override
		public int peekNextInt() {
			Assertions.hasNext(this);
			return edges[idx];
		}
	}

	static class BuilderProcessEdges {

		final int[] edgesOut;
		final int[] edgesOutBegin;

		BuilderProcessEdges(int[] edgesOut, int[] edgesOutBegin) {
			this.edgesOut = edgesOut;
			this.edgesOutBegin = edgesOutBegin;
		}

	}

	static class BuilderProcessEdgesUndirected extends BuilderProcessEdges {

		private BuilderProcessEdgesUndirected(int[] edgesOut, int[] edgesOutBegin) {
			super(edgesOut, edgesOutBegin);
		}

		static BuilderProcessEdgesUndirected valueOf(IndexGraphBuilderImpl builder) {
			// return valueOf(Variant2.ofB(builder));
			// }

			// static BuilderProcessEdgesUndirected valueOf(IndexGraph g) {
			// return valueOf(Variant2.ofA(g));
			// }

			// private static BuilderProcessEdgesUndirected valueOf(
			// Variant2<IndexGraph, IndexGraphBuilderImpl> graphOrBuilder) {
			final int n = builder.vertices().size();
			final int m = builder.edges().size();
			// final int n = verticesNum(graphOrBuilder);
			// final int m = edgesNum(graphOrBuilder);

			/* Count how many out/in edges each vertex has */
			final int[] edgesOutBegin = new int[n + 1];
			// if (graphOrBuilder.contains(IndexGraph.class)) {
			// IndexGraph g = graphOrBuilder.get(IndexGraph.class);
			// for (int e : range(m)) {
			// int u = g.edgeSource(e), v = g.edgeTarget(e);
			// edgesOutBegin[u]++;
			// if (u != v)
			// edgesOutBegin[v]++;
			// }
			// } else {
			// IndexGraphBuilderImpl builder = graphOrBuilder.get(IndexGraphBuilderImpl.class);
			for (int e : range(m)) {
				int u = builder.edgeSource(e), v = builder.edgeTarget(e);
				edgesOutBegin[u]++;
				if (u != v)
					edgesOutBegin[v]++;
			}
			// }
			int outNumSum = 0;
			for (int v : range(n))
				outNumSum += edgesOutBegin[v];
			final int[] edgesOut = new int[outNumSum];
			if (n == 0)
				return new BuilderProcessEdgesUndirected(edgesOut, edgesOutBegin);

			/*
			 * Arrange all the out-edges in a single continues array, where the out- edges of u are
			 * edgesOutBegin[u]...edgesOutBegin[u+1]-1
			 */
			int nextOutNum = edgesOutBegin[0];
			edgesOutBegin[0] = 0;
			for (int v : range(1, n)) {
				int nextOutNum0 = edgesOutBegin[v];
				edgesOutBegin[v] = edgesOutBegin[v - 1] + nextOutNum;
				nextOutNum = nextOutNum0;
			}
			edgesOutBegin[n] = outNumSum;
			// if (graphOrBuilder.contains(IndexGraph.class)) {
			// IndexGraph g = graphOrBuilder.get(IndexGraph.class);
			// for (int e : range(m)) {
			// int u = g.edgeSource(e), v = g.edgeTarget(e);
			// int uOutIdx = edgesOutBegin[u]++;
			// edgesOut[uOutIdx] = e;
			// if (u != v) {
			// int vInIdx = edgesOutBegin[v]++;
			// edgesOut[vInIdx] = e;
			// }
			// }
			// } else {
			// IndexGraphBuilderImpl builder = graphOrBuilder.get(IndexGraphBuilderImpl.class);
			for (int e : range(m)) {
				int u = builder.edgeSource(e), v = builder.edgeTarget(e);
				int uOutIdx = edgesOutBegin[u]++;
				edgesOut[uOutIdx] = e;
				if (u != v) {
					int vInIdx = edgesOutBegin[v]++;
					edgesOut[vInIdx] = e;
				}
			}
			// }
			assert edgesOutBegin[n - 1] == outNumSum;
			for (int v = n - 1; v > 0; v--)
				edgesOutBegin[v] = edgesOutBegin[v - 1];
			edgesOutBegin[0] = 0;

			return new BuilderProcessEdgesUndirected(edgesOut, edgesOutBegin);
		}

	}

	static class BuilderProcessEdgesDirected extends BuilderProcessEdges {

		final int[] edgesIn;
		final int[] edgesInBegin;

		private BuilderProcessEdgesDirected(int[] edgesOut, int[] edgesOutBegin, int[] edgesIn, int[] edgesInBegin) {
			super(edgesOut, edgesOutBegin);
			this.edgesIn = edgesIn;
			this.edgesInBegin = edgesInBegin;
		}

		static BuilderProcessEdgesDirected valueOf(IndexGraphBuilderImpl builder) {
			return valueOf(Variant2.ofB(builder));
		}

		static BuilderProcessEdgesDirected valueOf(IndexGraph g) {
			return valueOf(Variant2.ofA(g));
		}

		static BuilderProcessEdgesDirected valueOf(Variant2<IndexGraph, IndexGraphBuilderImpl> graphOrBuilder) {
			if (graphOrBuilder.contains(IndexGraph.class)
					&& graphOrBuilder.get(IndexGraph.class) instanceof GraphCsrDirectedReindexed)
				return null;

			final int n = verticesNum(graphOrBuilder);
			final int m = edgesNum(graphOrBuilder);

			final int[] edgesOut = new int[m];
			final int[] edgesOutBegin = new int[n + 1];
			final int[] edgesIn = new int[m];
			final int[] edgesInBegin = new int[n + 1];
			if (m == 0)
				return new BuilderProcessEdgesDirected(edgesOut, edgesOutBegin, edgesIn, edgesInBegin);

			/* Count how many out/in edges each vertex has */
			if (graphOrBuilder.contains(IndexGraph.class)) {
				IndexGraph g = graphOrBuilder.get(IndexGraph.class);
				for (int e : range(m)) {
					edgesOutBegin[g.edgeSource(e)]++;
					edgesInBegin[g.edgeTarget(e)]++;
				}
			} else {
				IndexGraphBuilderImpl builder = graphOrBuilder.get(IndexGraphBuilderImpl.class);
				for (int e : range(m)) {
					edgesOutBegin[builder.edgeSource(e)]++;
					edgesInBegin[builder.edgeTarget(e)]++;
				}
			}

			/*
			 * Arrange all the out-edges in a single continues array, where the out-edges of u are
			 * edgesOutBegin[u]...edgesOutBegin[u+1]-1 and similarly all in-edges edgesInBegin[v]...edgesInBegin[v+1]-1.
			 * In addition, sort the out-edges of each vertex by target. This is done in linear time by two (stable)
			 * bucket sorts, first by target and than by source.
			 */
			int nextOutNum = edgesOutBegin[0], nextInNum = edgesInBegin[0];
			edgesOutBegin[0] = edgesInBegin[0] = 0;
			for (int v : range(1, n)) {
				int nextOutNum0 = edgesOutBegin[v];
				int nextInNum0 = edgesInBegin[v];
				edgesOutBegin[v] = edgesOutBegin[v - 1] + nextOutNum;
				edgesInBegin[v] = edgesInBegin[v - 1] + nextInNum;
				nextOutNum = nextOutNum0;
				nextInNum = nextInNum0;
			}
			edgesOutBegin[n] = edgesInBegin[n] = m;
			if (graphOrBuilder.contains(IndexGraph.class)) {
				IndexGraph g = graphOrBuilder.get(IndexGraph.class);
				/* firsts bucket sort by target */
				for (int e : range(m)) {
					int vInIdx = edgesInBegin[g.edgeTarget(e)]++;
					edgesIn[vInIdx] = e;
				}
				/* than bucket sort by source, stable sorting after the first sort */
				for (int e : edgesIn) {
					int uOutIdx = edgesOutBegin[g.edgeSource(e)]++;
					edgesOut[uOutIdx] = e;
				}
			} else {
				IndexGraphBuilderImpl builder = graphOrBuilder.get(IndexGraphBuilderImpl.class);
				/* firsts bucket sort by target */
				for (int e : range(m)) {
					int vInIdx = edgesInBegin[builder.edgeTarget(e)]++;
					edgesIn[vInIdx] = e;
				}
				/* than bucket sort by source, stable sorting after the first sort */
				for (int e : edgesIn) {
					int uOutIdx = edgesOutBegin[builder.edgeSource(e)]++;
					edgesOut[uOutIdx] = e;
				}
			}
			assert edgesOutBegin[n - 1] == m;
			assert edgesInBegin[n - 1] == m;
			for (int v = n - 1; v > 0; v--) {
				edgesOutBegin[v] = edgesOutBegin[v - 1];
				edgesInBegin[v] = edgesInBegin[v - 1];
			}
			edgesOutBegin[0] = edgesInBegin[0] = 0;

			return new BuilderProcessEdgesDirected(edgesOut, edgesOutBegin, edgesIn, edgesInBegin);
		}

	}

}
