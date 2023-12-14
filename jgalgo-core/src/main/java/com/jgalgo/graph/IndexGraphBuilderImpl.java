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
import java.util.Arrays;
import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;

class IndexGraphBuilderImpl implements IndexGraphBuilder {

	private final boolean directed;
	final GraphElementSet.Mutable vertices;
	final GraphElementSet.Mutable edges;
	private int[] endpoints = IntArrays.EMPTY_ARRAY;
	private int[] edgesUserIds = IntArrays.EMPTY_ARRAY;
	private IntSet edgesSetView;
	private boolean userProvideEdgesIds;

	final WeightsImpl.IndexMutable.Manager verticesUserWeights;
	final WeightsImpl.IndexMutable.Manager edgesUserWeights;

	IndexGraphFactoryImpl.Impl mutableImpl;
	IndexGraphFactoryImpl.Impl immutableImpl;

	IndexGraphBuilderImpl(boolean directed) {
		this.directed = directed;
		vertices = GraphElementSet.Mutable.ofVertices(0);
		edges = GraphElementSet.Mutable.ofEdges(0);
		verticesUserWeights = new WeightsImpl.IndexMutable.Manager(0, false);
		edgesUserWeights = new WeightsImpl.IndexMutable.Manager(0, true);
		setDefaultImpls();
	}

	IndexGraphBuilderImpl(Graph<Integer, Integer> g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		this.directed = g.isDirected();
		final int n = g.vertices().size();
		final int m = g.edges().size();

		if (!(g instanceof IndexGraph)) {
			if (!range(n).equals(g.vertices()))
				throw new IllegalArgumentException("vertices must be 0,1,2,...,n-1 but was " + g.vertices());
			if (!range(m).equals(g.edges()))
				throw new IllegalArgumentException("edges must be 0,1,2,...,m-1 but was " + g.edges());
		}

		vertices = GraphElementSet.Mutable.ofVertices(n);
		edges = GraphElementSet.Mutable.ofEdges(m);

		endpoints = new int[m * 2];

		if (g instanceof IntGraph) {
			IntGraph g0 = (IntGraph) g;
			for (int e = 0; e < m; e++) {
				int u = g0.edgeSource(e);
				int v = g0.edgeTarget(e);
				setEdgeEndpoints(e, u, v);
			}
		} else {
			for (int e = 0; e < m; e++) {
				Integer e0 = Integer.valueOf(e);
				int u = g.edgeSource(e0).intValue();
				int v = g.edgeTarget(e0).intValue();
				setEdgeEndpoints(e, u, v);
			}
		}

		verticesUserWeights = new WeightsImpl.IndexMutable.Manager(vertices.size(), false);
		edgesUserWeights = new WeightsImpl.IndexMutable.Manager(edges.size(), true);
		if (copyVerticesWeights)
			for (String key : g.getVerticesWeightsKeys())
				verticesUserWeights.addWeights(key,
						WeightsImpl.IndexMutable.copyOf(g.getVerticesWeights(key), vertices, false));
		if (copyEdgesWeights)
			for (String key : g.getEdgesWeightsKeys())
				edgesUserWeights.addWeights(key, WeightsImpl.IndexMutable.copyOf(g.getEdgesWeights(key), edges, true));

		setDefaultImpls();
	}

	private void setDefaultImpls() {
		mutableImpl = new IndexGraphFactoryImpl(directed).mutableImpl();
		immutableImpl = new IndexGraphFactoryImpl(directed).immutableImpl();
	}

	void setMutableImpl(IndexGraphFactoryImpl.Impl mutableImpl) {
		this.mutableImpl = Objects.requireNonNull(mutableImpl);
	}

	void setImmutableImpl(IndexGraphFactoryImpl.Impl immutableImpl) {
		this.immutableImpl = Objects.requireNonNull(immutableImpl);
	}

	@Override
	public IntSet vertices() {
		return vertices;
	}

	@Override
	public IntSet edges() {
		if (edgesSetView == null) {
			if (edges.isEmpty())
				return edges;
			if (userProvideEdgesIds) {
				edgesSetView = new EdgesSetProvidedIdx();
			} else {
				edgesSetView = edges;
			}
		}
		return edgesSetView;
	}

	@Override
	public int addVertex() {
		int u = vertices.add();
		verticesUserWeights.ensureCapacity(u + 1);
		return u;
	}

	@Override
	public void addVertices(Collection<? extends Integer> vertices) {
		if (!GraphBaseMutable.isRange(this.vertices.size, vertices))
			throw new IllegalArgumentException("added vertices must be a consecutive range of integers starting from "
					+ this.vertices.size + " but was " + vertices);

		this.vertices.addAll(vertices.size());
		verticesUserWeights.ensureCapacity(this.vertices.size);
	}

	private boolean canAddEdgeWithoutId() {
		return edges.isEmpty() || !userProvideEdgesIds;
	}

	private boolean canAddEdgeWithId() {
		return edges.isEmpty() || userProvideEdgesIds;
	}

	@Override
	public int addEdge(int source, int target) {
		if (!canAddEdgeWithoutId())
			throw new IllegalStateException("Can't mix addEdge(u,v) and addEdge(u,v,id), "
					+ "if IDs are provided for some of the edges, they must be provided for all");
		if (!vertices().contains(source))
			throw NoSuchVertexException.ofIndex(source);
		if (!vertices().contains(target))
			throw NoSuchVertexException.ofIndex(target);
		int e = edges.add();
		ensureEdgeCapacity(e + 1);
		setEdgeEndpoints(e, source, target);
		return e;
	}

	@Override
	public void addEdge(int source, int target, int edge) {
		if (!canAddEdgeWithId())
			throw new IllegalStateException("Can't mix addEdge(u,v) and addEdge(u,v,id), "
					+ "if IDs are provided for some of the edges, they must be provided for all");
		if (edge < 0)
			throw new IllegalArgumentException("edge ID must be non negative integer");
		if (!vertices().contains(source))
			throw NoSuchVertexException.ofIndex(source);
		if (!vertices().contains(target))
			throw NoSuchVertexException.ofIndex(target);
		userProvideEdgesIds = true;
		int eIdx = edges.add();
		ensureEdgeCapacity(eIdx + 1);
		setEdgeEndpoints(eIdx, source, target);
		edgesUserIds[eIdx] = edge;
	}

	@Override
	public void addEdges(EdgeSet<? extends Integer, ? extends Integer> edges) {
		if (!canAddEdgeWithId())
			throw new IllegalStateException("Can't mix addEdge(u,v) and addEdge(u,v,id), "
					+ "if IDs are provided for some of the edges, they must be provided for all");
		@SuppressWarnings("unchecked")
		EdgeSet<Integer, Integer> edges0 = (EdgeSet<Integer, Integer>) edges;
		if (edges instanceof IEdgeSet) {
			for (IEdgeIter eit = ((IEdgeSet) edges).iterator(); eit.hasNext();) {
				eit.nextInt();
				int source = eit.sourceInt();
				int target = eit.targetInt();
				if (!vertices().contains(source))
					throw NoSuchVertexException.ofIndex(source);
				if (!vertices().contains(target))
					throw NoSuchVertexException.ofIndex(target);
			}
		} else {
			for (EdgeIter<Integer, Integer> eit = edges0.iterator(); eit.hasNext();) {
				eit.next();
				int source = eit.source().intValue();
				int target = eit.target().intValue();
				if (!vertices().contains(source))
					throw NoSuchVertexException.ofIndex(source);
				if (!vertices().contains(target))
					throw NoSuchVertexException.ofIndex(target);
			}
		}

		final int addedNum = edges.size();
		userProvideEdgesIds = true;
		int eIdx = this.edges.size;
		this.edges.addAll(addedNum);
		ensureEdgeCapacity(this.edges.size);
		if (edges instanceof IEdgeSet) {
			for (IEdgeIter eit = ((IEdgeSet) edges).iterator(); eit.hasNext(); eIdx++) {
				int edge = eit.nextInt();
				int source = eit.sourceInt();
				int target = eit.targetInt();
				setEdgeEndpoints(eIdx, source, target);
				edgesUserIds[eIdx] = edge;
			}
		} else {
			for (EdgeIter<Integer, Integer> eit = edges0.iterator(); eit.hasNext(); eIdx++) {
				int edge = eit.next().intValue();
				int source = eit.source().intValue();
				int target = eit.target().intValue();
				setEdgeEndpoints(eIdx, source, target);
				edgesUserIds[eIdx] = edge;
			}
		}
	}

	@Override
	public IntSet addEdgesReassignIds(IEdgeSet edges) {
		if (!canAddEdgeWithoutId())
			throw new IllegalStateException("Can't mix addEdge(u,v) and addEdge(u,v,id), "
					+ "if IDs are provided for some of the edges, they must be provided for all");
		for (IEdgeIter eit = edges.iterator(); eit.hasNext();) {
			eit.nextInt();
			int source = eit.sourceInt();
			int target = eit.targetInt();
			if (!vertices().contains(source))
				throw NoSuchVertexException.ofIndex(source);
			if (!vertices().contains(target))
				throw NoSuchVertexException.ofIndex(target);
		}

		final int addedNum = edges.size();
		int eIdx = this.edges.size;
		this.edges.addAll(addedNum);
		ensureEdgeCapacity(this.edges.size);
		for (IEdgeIter eit = edges.iterator(); eit.hasNext(); eIdx++) {
			eit.nextInt(); /* ignore edge ID */
			int source = eit.sourceInt();
			int target = eit.targetInt();
			setEdgeEndpoints(eIdx, source, target);
		}
		return range(this.edges.size - addedNum, this.edges.size);
	}

	@Override
	public void ensureVertexCapacity(int verticesNum) {
		verticesUserWeights.ensureCapacity(verticesNum);
	}

	@Override
	public void ensureEdgeCapacity(int edgesNum) {
		edgesUserWeights.ensureCapacity(edgesNum);
		if (edgesNum * 2 > endpoints.length)
			endpoints = Arrays.copyOf(endpoints, Math.max(4, Math.max(2 * endpoints.length, edgesNum * 2)));
		if (edges.size > 0 && userProvideEdgesIds && edgesNum > edgesUserIds.length)
			edgesUserIds = Arrays.copyOf(edgesUserIds, Math.max(2, Math.max(2 * edgesUserIds.length, edgesNum)));
	}

	@Override
	public void clear() {
		vertices.clear();
		edges.clear();
		edgesSetView = null;
		userProvideEdgesIds = false;
		verticesUserWeights.clearContainers();
		edgesUserWeights.clearContainers();
	}

	@Override
	public boolean isDirected() {
		return directed;
	}

	private void setEdgeEndpoints(int e, int source, int target) {
		endpoints[edgeSourceIndex(e)] = source;
		endpoints[edgeTargetIndex(e)] = target;
	}

	private static int edgeSourceIndex(int e) {
		return e * 2 + 0;
	}

	private static int edgeTargetIndex(int e) {
		return e * 2 + 1;
	}

	void validateUserProvidedIdsBeforeBuild() {
		if (!userProvideEdgesIds)
			return;

		/* Rearrange edges such that edgesUserIds[e]==e */
		final int m = edges.size;
		for (int startIdx = 0; startIdx < m; startIdx++) {
			if (startIdx == edgesUserIds[startIdx])
				continue;
			int e = edgesUserIds[startIdx];
			if (e >= m)
				throw new IllegalArgumentException("Edges IDs should be 0,1,2,...,m-1. id >= m: " + e + " >= " + m);
			int u = endpoints[edgeSourceIndex(startIdx)];
			int v = endpoints[edgeTargetIndex(startIdx)];
			edgesUserIds[startIdx] = -1;
			for (;;) {
				int nextE = edgesUserIds[e];
				if (nextE == -1) {
					/* we completed a cycle */
					edgesUserIds[e] = e;
					setEdgeEndpoints(e, u, v);
					break;
				} else if (nextE == e)
					throw new IllegalArgumentException("duplicate edge id: " + e);
				if (nextE >= m)
					throw new IllegalArgumentException(
							"Edges IDs should be 0,1,2,...,m-1. id >= m: " + nextE + " >= " + m);
				int nextU = endpoints[edgeSourceIndex(e)];
				int nextV = endpoints[edgeTargetIndex(e)];
				setEdgeEndpoints(e, u, v);
				edgesUserIds[e] = e;
				u = nextU;
				v = nextV;
				e = nextE;
			}
		}
		assert range(m).allMatch(e -> e == edgesUserIds[e]);
	}

	@Override
	public IndexGraph build() {
		validateUserProvidedIdsBeforeBuild();
		return immutableImpl.newFromBuilder(new IndexGraphBuilderImpl.Artifacts(this));
	}

	@Override
	public IndexGraph buildMutable() {
		validateUserProvidedIdsBeforeBuild();
		return mutableImpl.newFromBuilder(new IndexGraphBuilderImpl.Artifacts(this));
	}

	@Override
	public IndexGraphBuilder.ReIndexedGraph reIndexAndBuild(boolean reIndexVertices, boolean reIndexEdges) {
		if (directed && reIndexEdges) {
			validateUserProvidedIdsBeforeBuild();
			return GraphCsrDirectedReindexed.newInstance(new IndexGraphBuilderImpl.Artifacts(this));
		} else {
			return new ReIndexedGraphImpl(build(), Optional.empty(), Optional.empty());
		}
	}

	@Override
	public IndexGraphBuilder.ReIndexedGraph reIndexAndBuildMutable(boolean reIndexVertices, boolean reIndexEdges) {
		return new ReIndexedGraphImpl(buildMutable(), Optional.empty(), Optional.empty());
	}

	@Override
	public <T, WeightsT extends Weights<Integer, T>> WeightsT getVerticesWeights(String key) {
		return verticesUserWeights.getWeights(key);
	}

	@Override
	public <T, WeightsT extends Weights<Integer, T>> WeightsT addVerticesWeights(String key, Class<? super T> type,
			T defVal) {
		WeightsImpl.IndexMutable<T> weights = WeightsImpl.IndexMutable.newInstance(vertices, false, type, defVal);
		verticesUserWeights.addWeights(key, weights);
		@SuppressWarnings("unchecked")
		WeightsT weights0 = (WeightsT) weights;
		return weights0;
	}

	@Override
	public Set<String> getVerticesWeightsKeys() {
		return verticesUserWeights.weightsKeys();
	}

	@Override
	public <T, WeightsT extends Weights<Integer, T>> WeightsT getEdgesWeights(String key) {
		return edgesUserWeights.getWeights(key);
	}

	@Override
	public <T, WeightsT extends Weights<Integer, T>> WeightsT addEdgesWeights(String key, Class<? super T> type,
			T defVal) {
		WeightsImpl.IndexMutable<T> weights = WeightsImpl.IndexMutable.newInstance(edges, true, type, defVal);
		edgesUserWeights.addWeights(key, weights);
		@SuppressWarnings("unchecked")
		WeightsT weights0 = (WeightsT) weights;
		return weights0;
	}

	@Override
	public Set<String> getEdgesWeightsKeys() {
		return edgesUserWeights.weightsKeys();
	}

	private class EdgesSetProvidedIdx extends AbstractIntSet {
		@Override
		public int size() {
			return edges.size;
		}

		@Override
		public IntIterator iterator() {
			return new IntIterator() {

				int idx = 0;

				@Override
				public boolean hasNext() {
					return idx < edges.size;
				}

				@Override
				public int nextInt() {
					Assertions.Iters.hasNext(this);
					return edgesUserIds[idx++];
				}

			};
		}
	}

	static class Artifacts {

		final boolean isDirected;
		final GraphElementSet.Mutable vertices;
		final GraphElementSet.Mutable edges;
		private final int[] endpoints;
		final WeightsImpl.IndexMutable.Manager verticesUserWeights;
		final WeightsImpl.IndexMutable.Manager edgesUserWeights;

		Artifacts(IndexGraphBuilderImpl builder) {
			isDirected = builder.directed;
			vertices = builder.vertices;
			edges = builder.edges;
			endpoints = builder.endpoints;
			verticesUserWeights = builder.verticesUserWeights;
			edgesUserWeights = builder.edgesUserWeights;
		}

		int edgeSource(int edge) {
			return endpoints[edgeSourceIndex(edge)];
		}

		int edgeTarget(int edge) {
			return endpoints[edgeTargetIndex(edge)];
		}
	}

	static class ReIndexedGraphImpl implements IndexGraphBuilder.ReIndexedGraph {

		private final IndexGraph graph;
		private final Optional<IndexGraphBuilder.ReIndexingMap> verticesReIndexing;
		private final Optional<IndexGraphBuilder.ReIndexingMap> edgesReIndexing;

		ReIndexedGraphImpl(IndexGraph graph, Optional<IndexGraphBuilder.ReIndexingMap> verticesReIndexing,
				Optional<IndexGraphBuilder.ReIndexingMap> edgesReIndexing) {
			this.graph = Objects.requireNonNull(graph);
			this.verticesReIndexing = Objects.requireNonNull(verticesReIndexing);
			this.edgesReIndexing = Objects.requireNonNull(edgesReIndexing);
		}

		@Override
		public IndexGraph graph() {
			return graph;
		}

		@Override
		public Optional<IndexGraphBuilder.ReIndexingMap> verticesReIndexing() {
			return verticesReIndexing;
		}

		@Override
		public Optional<IndexGraphBuilder.ReIndexingMap> edgesReIndexing() {
			return edgesReIndexing;
		}
	}

	static class ReIndexingMapImpl implements IndexGraphBuilder.ReIndexingMap {

		private final int[] origToReIndexed;
		private final int[] reIndexedToOrig;

		ReIndexingMapImpl(int[] origToReIndexed, int[] reIndexedToOrig) {
			this.origToReIndexed = Objects.requireNonNull(origToReIndexed);
			this.reIndexedToOrig = Objects.requireNonNull(reIndexedToOrig);
		}

		@Override
		public int origToReIndexed(int orig) {
			return origToReIndexed[orig];
		}

		@Override
		public int reIndexedToOrig(int reindexed) {
			return reIndexedToOrig[reindexed];
		}
	}

}
