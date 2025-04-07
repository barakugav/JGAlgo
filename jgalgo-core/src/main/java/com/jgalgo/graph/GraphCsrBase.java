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
import com.jgalgo.internal.util.BitmapSet;
import com.jgalgo.internal.util.IntPair;
import com.jgalgo.internal.util.JGAlgoUtils.Variant2;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntSet;

abstract class GraphCsrBase extends IndexGraphBase implements ImmutableGraph {

	final int[] edgesOutBegin;

	final Map<String, WeightsImpl.IndexImmutable<?>> verticesWeights;
	final Map<String, WeightsImpl.IndexImmutable<?>> edgesWeights;

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
			ProcessedEdges processEdges, Optional<IndexGraphBuilder.ReIndexingMap> edgesReIndexing,
			boolean copyVerticesWeights, boolean copyEdgesWeights) {
		super(graphOrBuilder, false);
		final int n = verticesNum(graphOrBuilder);
		final int m = edgesNum(graphOrBuilder);

		final Optional<IndexGraphBuilderImpl> inputBuilder = graphOrBuilder.getOptional(IndexGraphBuilderImpl.class);
		final Optional<IndexGraph> inputGraph = graphOrBuilder.getOptional(IndexGraph.class);
		final Optional<GraphCsrBase> inputCsrGraph =
				inputGraph.filter(GraphCsrBase.class::isInstance).map(GraphCsrBase.class::cast);

		if (inputCsrGraph.isPresent()) {
			assert processEdges == null;
			edgeEndpoints = inputCsrGraph.get().edgeEndpoints;
			edgesOutBegin = inputCsrGraph.get().edgesOutBegin;
		} else {
			if (inputBuilder.isEmpty() || !inputBuilder.get().isNewGraphShouldStealInterior()) {
				edgeEndpoints = new long[edgesNum(graphOrBuilder)];
				if (edgesReIndexing.isEmpty()) {
					if (inputGraph.isPresent()) {
						IndexGraph g = inputGraph.get();
						for (int e : range(m))
							setEndpoints(e, g.edgeSource(e), g.edgeTarget(e));
					} else {
						IndexGraphBuilderImpl b = inputBuilder.get();
						for (int e : range(m))
							setEndpoints(e, b.edgeSource(e), b.edgeTarget(e));
					}
				} else {
					IndexGraphBuilder.ReIndexingMap eReIndexing = edgesReIndexing.get();
					if (inputGraph.isPresent()) {
						IndexGraph g = inputGraph.get();
						for (int eOrig : range(m))
							setEndpoints(eReIndexing.map(eOrig), g.edgeSource(eOrig), g.edgeTarget(eOrig));
					} else {
						IndexGraphBuilderImpl b = inputBuilder.get();
						for (int eOrig : range(m))
							setEndpoints(eReIndexing.map(eOrig), b.edgeSource(eOrig), b.edgeTarget(eOrig));
					}
				}
			} else {
				/* Steal the builder interior endpoints array */
				edgeEndpoints = inputBuilder.get().stealEndpointsArray();
				if (edgesReIndexing.isPresent()) {
					IndexGraphBuilder.ReIndexingMap eReIndexing = edgesReIndexing.get();
					/*
					 * We have an array of endpoints, and we want to re-index the array in place. We do so by iterating
					 * over the edges in some arbitrary way (0,1,2,...) and for iterated edge e1 that we didn't already
					 * handled, we move it to its new index e2, and than we move e2 to its new index, and so on, until
					 * we close the loop and come back to the first edge e1. We mark the edges that we handled by
					 * setting they source to be -src-1 which is always negative.
					 */
					assert range(m).allMatch(e -> IntPair.first(edgeEndpoints[e]) >= 0);
					for (int eStart : range(m)) {
						if (IntPair.first(edgeEndpoints[eStart]) < 0)
							continue;
						for (int e = eStart, u = IntPair.first(edgeEndpoints[e]),
								v = IntPair.second(edgeEndpoints[e]);;) {
							int eNext = eReIndexing.map(e);
							int uNext = IntPair.first(edgeEndpoints[eNext]);
							int vNext = IntPair.second(edgeEndpoints[eNext]);
							assert u >= 0;
							edgeEndpoints[eNext] = IntPair.of(-u - 1, v);
							if (eNext == eStart)
								break;
							e = eNext;
							u = uNext;
							v = vNext;
						}
					}
					assert range(m).allMatch(e -> IntPair.first(edgeEndpoints[e]) < 0);
					for (int e : range(m))
						edgeEndpoints[e] =
								IntPair.of(-IntPair.first(edgeEndpoints[e]) - 1, IntPair.second(edgeEndpoints[e]));
				}
			}
			edgesOutBegin = processEdges.edgesOutBegin;
		}
		assert edgesOutBegin.length == n + 1;

		if (copyVerticesWeights && inputCsrGraph.isPresent()) {
			verticesWeights = inputCsrGraph.get().verticesWeights;

		} else if (copyVerticesWeights) {
			Set<String> verticesWeightsKeys =
					graphOrBuilder.map(IndexGraph::verticesWeightsKeys, b -> b.verticesWeights.weightsKeys());
			Function<String, IWeights<?>> getWeights = graphOrBuilder
					.map(g -> key -> (IWeights<?>) g.verticesWeights(key), b -> b.verticesWeights::getWeights);

			WeightsImpl.IndexImmutable.Builder verticesWeightsBuilder =
					new WeightsImpl.IndexImmutable.Builder(vertices, true);
			for (String key : verticesWeightsKeys)
				verticesWeightsBuilder.copyAndAddWeights(key, getWeights.apply(key));
			verticesWeights = verticesWeightsBuilder.build();

		} else {
			verticesWeights = Map.of();
		}

		if (copyEdgesWeights && inputCsrGraph.isPresent()) {
			assert edgesReIndexing.isEmpty();
			edgesWeights = inputCsrGraph.get().edgesWeights;

		} else if (copyEdgesWeights) {
			Set<String> edgesWeightsKeys =
					graphOrBuilder.map(IndexGraph::edgesWeightsKeys, b -> b.edgesWeights.weightsKeys());
			Function<String, IWeights<?>> getWeights =
					graphOrBuilder.map(g -> key -> (IWeights<?>) g.edgesWeights(key), b -> b.edgesWeights::getWeights);

			WeightsImpl.IndexImmutable.Builder edgesWeightsBuilder =
					new WeightsImpl.IndexImmutable.Builder(edges, false);
			if (edgesReIndexing.isEmpty()) {
				for (String key : edgesWeightsKeys)
					edgesWeightsBuilder.copyAndAddWeights(key, getWeights.apply(key));
			} else {
				for (String key : edgesWeightsKeys)
					edgesWeightsBuilder.copyAndAddWeightsReindexed(key, getWeights.apply(key), edgesReIndexing.get());
			}
			edgesWeights = edgesWeightsBuilder.build();

		} else {
			edgesWeights = Map.of();
		}

		inputGraph.ifPresent(this::updateContainsSelfAndParallelEdgesFromCopiedGraph);
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

			verticesWeights = copyVerticesWeights ? gCsr.verticesWeights : Map.of();
			edgesWeights = copyEdgesWeights ? gCsr.edgesWeights : Map.of();

		} else {
			edgesOutBegin = new int[n + 1];
			edgeEndpoints = new long[m];
			for (int e : range(m))
				setEndpoints(e, g.edgeSource(e), g.edgeTarget(e));

			WeightsImpl.IndexImmutable.Builder verticesWeightsBuilder =
					new WeightsImpl.IndexImmutable.Builder(vertices, true);
			WeightsImpl.IndexImmutable.Builder edgesWeightsBuilder =
					new WeightsImpl.IndexImmutable.Builder(edges, false);
			if (copyVerticesWeights)
				for (String key : g.verticesWeightsKeys())
					verticesWeightsBuilder.copyAndAddWeights(key, (IWeights<?>) g.verticesWeights(key));
			if (copyEdgesWeights)
				for (String key : g.edgesWeightsKeys())
					edgesWeightsBuilder.copyAndAddWeights(key, (IWeights<?>) g.edgesWeights(key));
			verticesWeights = verticesWeightsBuilder.build();
			edgesWeights = edgesWeightsBuilder.build();
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
		BitmapSet neighbors = new BitmapSet(n);
		for (int u : range(n)) {
			for (IEdgeIter eit = outEdges(u).iterator(); eit.hasNext();) {
				eit.nextInt();
				int v = eit.targetInt();
				if (!neighbors.add(v))
					return true;
			}
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
	public <T, WeightsT extends Weights<Integer, T>> WeightsT verticesWeights(String key) {
		return (WeightsT) verticesWeights.get(key);
	}

	@Override
	public Set<String> verticesWeightsKeys() {
		return verticesWeights.keySet();
	}

	@Override
	public void removeVerticesWeights(String key) {
		throw new UnsupportedOperationException("graph is immutable, can't remove vertices weights");
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T, WeightsT extends Weights<Integer, T>> WeightsT edgesWeights(String key) {
		return (WeightsT) edgesWeights.get(key);
	}

	@Override
	public Set<String> edgesWeightsKeys() {
		return edgesWeights.keySet();
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
		if (verticesWeights.isEmpty())
			copyVerticesWeights = true;
		if (edgesWeights.isEmpty())
			copyEdgesWeights = true;
		if (copyVerticesWeights && copyEdgesWeights)
			return this;
		return super.immutableCopy(copyVerticesWeights, copyEdgesWeights);
	}

	abstract static class EdgeIterAbstract implements EdgeIters.IBase {

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

		@Override
		public int skip(int n) {
			if (n < 0)
				throw new IllegalArgumentException("Argument must be nonnegative: " + n);
			n = Math.min(n, endIdx - idx);
			idx += n;
			lastEdge = -1;
			return n;
		}
	}

	static class ProcessedEdges {

		final int[] edgesOut;
		final int[] edgesOutBegin;

		ProcessedEdges(int[] edgesOut, int[] edgesOutBegin) {
			this.edgesOut = edgesOut;
			this.edgesOutBegin = edgesOutBegin;
		}
	}

	static class ProcessedEdgesUndirected extends ProcessedEdges {

		private ProcessedEdgesUndirected(int[] edgesOut, int[] edgesOutBegin) {
			super(edgesOut, edgesOutBegin);
		}

		static ProcessedEdgesUndirected valueOf(IndexGraphBuilderImpl builder) {
			assert !builder.isDirected();
			final int n = builder.vertices().size();
			final int m = builder.edges().size();

			/* Count how many out-edges each vertex has */
			final int[] edgesOutBegin = new int[n + 1];
			int selfEdgesNum = 0;
			for (int e : range(m)) {
				int u = builder.edgeSource(e), v = builder.edgeTarget(e);
				edgesOutBegin[u]++;
				if (u != v) {
					edgesOutBegin[v]++;
				} else {
					selfEdgesNum++;
				}
			}
			final int outNumSum = m * 2 - selfEdgesNum;
			final int[] edgesOut = new int[outNumSum];

			/*
			 * Arrange all the out-edges in a single continues array, where the out-edges of u are
			 * edgesOutBegin[u]...edgesOutBegin[u+1]-1
			 */
			int outEdgesOffset = 0;
			for (int v : range(0, n)) {
				int vOutEdges = edgesOutBegin[v];
				edgesOutBegin[v] = outEdgesOffset;
				outEdgesOffset += vOutEdges;
			}
			assert outEdgesOffset == outNumSum;
			edgesOutBegin[n] = outEdgesOffset;
			for (int e : range(m)) {
				int u = builder.edgeSource(e), v = builder.edgeTarget(e);
				edgesOut[edgesOutBegin[u]++] = e;
				if (u != v)
					edgesOut[edgesOutBegin[v]++] = e;
			}
			for (int v = n - 1; v > 0; v--)
				edgesOutBegin[v] = edgesOutBegin[v - 1];
			edgesOutBegin[0] = 0;

			return new ProcessedEdgesUndirected(edgesOut, edgesOutBegin);
		}
	}

	static class ProcessedEdgesDirected extends ProcessedEdges {

		final int[] edgesIn;
		final int[] edgesInBegin;

		private ProcessedEdgesDirected(int[] edgesOut, int[] edgesOutBegin, int[] edgesIn, int[] edgesInBegin) {
			super(edgesOut, edgesOutBegin);
			this.edgesIn = edgesIn;
			this.edgesInBegin = edgesInBegin;
		}

		static ProcessedEdgesDirected valueOf(IndexGraphBuilderImpl builder) {
			return valueOf(Variant2.ofB(builder));
		}

		static ProcessedEdgesDirected valueOf(IndexGraph g) {
			return valueOf(Variant2.ofA(g));
		}

		static ProcessedEdgesDirected valueOf(Variant2<IndexGraph, IndexGraphBuilderImpl> graphOrBuilder) {
			assert graphOrBuilder.contains(IndexGraph.class) ? graphOrBuilder.get(IndexGraph.class).isDirected()
					: graphOrBuilder.get(IndexGraphBuilderImpl.class).isDirected();
			if (graphOrBuilder.contains(IndexGraph.class)
					&& graphOrBuilder.get(IndexGraph.class) instanceof GraphCsrDirectedReindexed)
				return null;

			final int n = verticesNum(graphOrBuilder);
			final int m = edgesNum(graphOrBuilder);

			final int[] edgesOut = new int[m];
			final int[] edgesOutBegin = new int[n + 1];
			final int[] edgesIn = new int[m];
			final int[] edgesInBegin = new int[n + 1];

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
			int outEdgesOffset = 0, inEdgesOffset = 0;
			for (int v : range(0, n)) {
				int vOutEdges = edgesOutBegin[v];
				edgesOutBegin[v] = outEdgesOffset;
				outEdgesOffset += vOutEdges;

				int vInEdges = edgesInBegin[v];
				edgesInBegin[v] = inEdgesOffset;
				inEdgesOffset += vInEdges;
			}
			assert outEdgesOffset == m && inEdgesOffset == m;
			edgesOutBegin[n] = edgesInBegin[n] = m;
			if (graphOrBuilder.contains(IndexGraph.class)) {
				IndexGraph g = graphOrBuilder.get(IndexGraph.class);
				/* firsts bucket sort by target */
				for (int e : range(m))
					edgesIn[edgesInBegin[g.edgeTarget(e)]++] = e;
				/* than bucket sort by source, stable sorting after the first sort */
				for (int e : edgesIn)
					edgesOut[edgesOutBegin[g.edgeSource(e)]++] = e;
			} else {
				IndexGraphBuilderImpl builder = graphOrBuilder.get(IndexGraphBuilderImpl.class);
				/* firsts bucket sort by target */
				for (int e : range(m))
					edgesIn[edgesInBegin[builder.edgeTarget(e)]++] = e;
				/* than bucket sort by source, stable sorting after the first sort */
				for (int e : edgesIn)
					edgesOut[edgesOutBegin[builder.edgeSource(e)]++] = e;
			}
			for (int v = n - 1; v > 0; v--) {
				edgesOutBegin[v] = edgesOutBegin[v - 1];
				edgesInBegin[v] = edgesInBegin[v - 1];
			}
			edgesOutBegin[0] = edgesInBegin[0] = 0;

			return new ProcessedEdgesDirected(edgesOut, edgesOutBegin, edgesIn, edgesInBegin);
		}

	}

}
