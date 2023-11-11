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

import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Range;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;

abstract class IndexGraphBuilderImpl implements IndexGraphBuilder {

	private int m;
	final GraphElementSet.Default vertices;
	final GraphElementSet.Default edges;
	private int[] endpoints = IntArrays.EMPTY_ARRAY;
	private int[] edgesUserIds = IntArrays.EMPTY_ARRAY;
	private IntSet edgesSetView;
	private boolean userProvideEdgesIds;

	final WeightsImpl.IndexMutable.Manager verticesUserWeights;
	final WeightsImpl.IndexMutable.Manager edgesUserWeights;

	IndexGraphFactoryImpl.Impl mutableImpl;
	IndexGraphFactoryImpl.Impl immutableImpl;

	private IndexGraphBuilderImpl() {
		vertices = new GraphElementSet.Default(0, false);
		edges = new GraphElementSet.Default(0, true);
		verticesUserWeights = new WeightsImpl.IndexMutable.Manager(0);
		edgesUserWeights = new WeightsImpl.IndexMutable.Manager(0);
		setDefaultImpls();
	}

	private IndexGraphBuilderImpl(IndexGraph g, boolean copyWeights) {
		final int n = g.vertices().size();
		m = g.edges().size();

		vertices = new GraphElementSet.Default(n, false);
		edges = new GraphElementSet.Default(m, true);

		endpoints = new int[m * 2];
		for (int e = 0; e < m; e++) {
			setEdgeSource(e, g.edgeSource(e));
			setEdgeTarget(e, g.edgeTarget(e));
		}

		verticesUserWeights = new WeightsImpl.IndexMutable.Manager(vertices.size());
		edgesUserWeights = new WeightsImpl.IndexMutable.Manager(edges.size());
		if (copyWeights) {
			for (String key : g.getVerticesWeightsKeys())
				verticesUserWeights.addWeights(key,
						WeightsImpl.IndexMutable.copyOf(g.getVerticesIWeights(key), vertices, false));
			for (String key : g.getEdgesWeightsKeys())
				edgesUserWeights.addWeights(key, WeightsImpl.IndexMutable.copyOf(g.getEdgesIWeights(key), edges, true));
		}

		setDefaultImpls();
	}

	static IndexGraphBuilderImpl newFrom(IndexGraph g, boolean copyWeights) {
		return g.isDirected() ? new IndexGraphBuilderImpl.Directed(g, copyWeights)
				: new IndexGraphBuilderImpl.Undirected(g, copyWeights);
	}

	private void setDefaultImpls() {
		boolean directed = this instanceof IndexGraphBuilderImpl.Directed;
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
			if (m == 0)
				return IntSets.emptySet();
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
		int u = vertices.newIdx();
		verticesUserWeights.ensureCapacity(u + 1);
		return u;
	}

	private boolean canAddEdgeWithoutId() {
		return m == 0 || !userProvideEdgesIds;
	}

	private boolean canAddEdgeWithId() {
		return m == 0 || userProvideEdgesIds;
	}

	@Override
	public int addEdge(int source, int target) {
		if (!canAddEdgeWithoutId())
			throw new IllegalArgumentException("Can't mix addEdge(u,v) and addEdge(u,v,id), "
					+ "if IDs are provided for some of the edges, they must be provided for all");
		if (!vertices().contains(source))
			throw new IllegalArgumentException("invalid vertex: " + source);
		if (!vertices().contains(target))
			throw new IllegalArgumentException("invalid vertex: " + target);
		int e = m++;
		int eFromEdgesSet = edges.newIdx();
		assert e == eFromEdgesSet;
		if (e * 2 == endpoints.length)
			endpoints = Arrays.copyOf(endpoints, Math.max(4, 2 * endpoints.length));
		setEdgeSource(e, source);
		setEdgeTarget(e, target);
		edgesUserWeights.ensureCapacity(e + 1);
		return e;
	}

	@Override
	public void addEdge(int source, int target, int edge) {
		if (edge < 0)
			throw new IllegalArgumentException("edge ID must be non negative integer");
		if (!canAddEdgeWithId())
			throw new IllegalArgumentException("Can't mix addEdge(u,v) and addEdge(u,v,id), "
					+ "if IDs are provided for some of the edges, they must be provided for all");
		if (!vertices().contains(source))
			throw new IllegalArgumentException("invalid vertex: " + source);
		if (!vertices().contains(target))
			throw new IllegalArgumentException("invalid vertex: " + target);
		int eIdx = m++;
		while (eIdx >= edges.size())
			edges.newIdx();
		if (eIdx * 2 >= endpoints.length)
			endpoints = Arrays.copyOf(endpoints, Math.max(4, Math.max(2 * endpoints.length, (eIdx + 1) * 2)));
		if (eIdx >= edgesUserIds.length)
			edgesUserIds = Arrays.copyOf(edgesUserIds, Math.max(2, Math.max(2 * edgesUserIds.length, eIdx + 1)));
		setEdgeSource(eIdx, source);
		setEdgeTarget(eIdx, target);
		edgesUserIds[eIdx] = edge;
		edgesUserWeights.ensureCapacity(edge + 1);
		userProvideEdgesIds = true;
	}

	@Override
	public void expectedVerticesNum(int verticesNum) {
		verticesUserWeights.ensureCapacity(verticesNum);
	}

	@Override
	public void expectedEdgesNum(int edgesNum) {
		edgesUserWeights.ensureCapacity(edgesNum);
		if (edgesNum * 2 > endpoints.length)
			endpoints = Arrays.copyOf(endpoints, Math.max(4, Math.max(2 * endpoints.length, edgesNum * 2)));
		if (edgesNum > edgesUserIds.length)
			edgesUserIds = Arrays.copyOf(edgesUserIds, Math.max(2, Math.max(2 * edgesUserIds.length, edgesNum)));
	}

	@Override
	public void clear() {
		m = 0;
		vertices.clear();
		edges.clear();
		edgesSetView = null;
		userProvideEdgesIds = false;
		verticesUserWeights.clearContainers();
		edgesUserWeights.clearContainers();

	}

	int edgeSource(int e) {
		return endpoints[edgeSourceIndex(e)];
	}

	int edgeTarget(int e) {
		return endpoints[edgeTargetIndex(e)];
	}

	int edgeEndpoint(int e, int endpoint) {
		int u = edgeSource(e);
		int v = edgeTarget(e);
		if (u == endpoint) {
			return v;
		} else {
			assert v == endpoint;
			return u;
		}
	}

	private void setEdgeSource(int e, int source) {
		endpoints[edgeSourceIndex(e)] = source;
	}

	private void setEdgeTarget(int e, int target) {
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
		for (int startIdx = 0; startIdx < m; startIdx++) {
			if (startIdx == edgesUserIds[startIdx])
				continue;
			int e = edgesUserIds[startIdx];
			if (e >= m)
				throw new IllegalArgumentException("Edges IDs should be 0,1,2,...,m-1. id >= m: " + e + " >= " + m);
			int u = edgeSource(startIdx), v = edgeTarget(startIdx);
			edgesUserIds[startIdx] = -1;
			for (;;) {
				int nextE = edgesUserIds[e];
				if (nextE == -1) {
					/* we completed a cycle */
					edgesUserIds[e] = e;
					setEdgeSource(e, u);
					setEdgeTarget(e, v);
					break;
				} else if (nextE == e)
					throw new IllegalArgumentException("duplicate edge id: " + e);
				if (nextE >= m)
					throw new IllegalArgumentException(
							"Edges IDs should be 0,1,2,...,m-1. id >= m: " + nextE + " >= " + m);
				int nextU = edgeSource(e);
				int nextV = edgeTarget(e);
				setEdgeSource(e, u);
				setEdgeTarget(e, v);
				edgesUserIds[e] = e;
				u = nextU;
				v = nextV;
				e = nextE;
			}
		}
		assert Range.of(m).intStream().allMatch(e -> e == edgesUserIds[e]);
	}

	@Override
	public IndexGraph build() {
		validateUserProvidedIdsBeforeBuild();
		return immutableImpl.newFromBuilder(this);
	}

	@Override
	public IndexGraph buildMutable() {
		validateUserProvidedIdsBeforeBuild();
		return mutableImpl.newFromBuilder(this);
	}

	@Override
	public <T, WeightsT extends IWeights<T>> WeightsT getVerticesIWeights(String key) {
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
	public <T, WeightsT extends IWeights<T>> WeightsT getEdgesIWeights(String key) {
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
			return m;
		}

		@Override
		public IntIterator iterator() {
			return new IntIterator() {

				int idx = 0;

				@Override
				public boolean hasNext() {
					return idx < m;
				}

				@Override
				public int nextInt() {
					Assertions.Iters.hasNext(this);
					return edgesUserIds[idx++];
				}

			};
		}
	}

	static class Undirected extends IndexGraphBuilderImpl {

		Undirected() {}

		Undirected(IndexGraph g, boolean copyWeights) {
			super(g, copyWeights);
			Assertions.Graphs.onlyUndirected(g);
		}

		@Override
		public IndexGraphBuilder.ReIndexedGraph reIndexAndBuild(boolean reIndexVertices, boolean reIndexEdges) {
			return new ReIndexedGraphImpl(build(), Optional.empty(), Optional.empty());
		}

		@Override
		public IndexGraphBuilder.ReIndexedGraph reIndexAndBuildMutable(boolean reIndexVertices, boolean reIndexEdges) {
			return new ReIndexedGraphImpl(buildMutable(), Optional.empty(), Optional.empty());
		}

	}

	static class Directed extends IndexGraphBuilderImpl {

		Directed() {}

		Directed(IndexGraph g, boolean copyWeights) {
			super(g, copyWeights);
			Assertions.Graphs.onlyDirected(g);
		}

		@Override
		public IndexGraphBuilder.ReIndexedGraph reIndexAndBuild(boolean reIndexVertices, boolean reIndexEdges) {
			if (reIndexEdges) {
				validateUserProvidedIdsBeforeBuild();
				return GraphCSRDirectedReindexed.newInstance(this);
			} else {
				return new ReIndexedGraphImpl(build(), Optional.empty(), Optional.empty());
			}
		}

		@Override
		public IndexGraphBuilder.ReIndexedGraph reIndexAndBuildMutable(boolean reIndexVertices, boolean reIndexEdges) {
			return new ReIndexedGraphImpl(buildMutable(), Optional.empty(), Optional.empty());
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
