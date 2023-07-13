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
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;

abstract class IndexGraphBuilderImpl implements IndexGraphBuilder {

	private int m;
	final IdStrategy.Default verticesIdStrat;
	final IdStrategy.Default edgesIdStrat;
	int[] endpoints = IntArrays.EMPTY_ARRAY;
	private int[] edgesUserIds = IntArrays.EMPTY_ARRAY;
	private IntSet edgesSet;
	private boolean userProvideEdgesIds;

	final WeightsImpl.IndexMutable.Manager verticesUserWeights;
	final WeightsImpl.IndexMutable.Manager edgesUserWeights;

	private IndexGraphBuilderImpl() {
		verticesIdStrat = new IdStrategy.Default(0);
		edgesIdStrat = new IdStrategy.Default(0);
		verticesUserWeights = new WeightsImpl.IndexMutable.Manager(0);
		edgesUserWeights = new WeightsImpl.IndexMutable.Manager(0);
	}

	private IndexGraphBuilderImpl(IndexGraph g) {
		final int n = g.vertices().size();
		m = g.edges().size();

		verticesIdStrat = new IdStrategy.Default(n);
		edgesIdStrat = new IdStrategy.Default(m);

		endpoints = new int[m * 2];
		for (int e = 0; e < m; e++) {
			setEdgeSource(e, g.edgeSource(e));
			setEdgeTarget(e, g.edgeTarget(e));
		}

		verticesUserWeights = new WeightsImpl.IndexMutable.Manager(verticesIdStrat.size());
		for (Object key : g.getVerticesWeightsKeys())
			verticesUserWeights.addWeights(key,
					WeightsImpl.IndexMutable.copyOf(g.getVerticesWeights(key), verticesIdStrat));
		edgesUserWeights = new WeightsImpl.IndexMutable.Manager(edgesIdStrat.size());
		for (Object key : g.getEdgesWeightsKeys())
			edgesUserWeights.addWeights(key, WeightsImpl.IndexMutable.copyOf(g.getEdgesWeights(key), edgesIdStrat));
	}

	static IndexGraphBuilderImpl newFrom(IndexGraph g) {
		return g.getCapabilities().directed() ? new IndexGraphBuilderImpl.Directed(g)
				: new IndexGraphBuilderImpl.Undirected(g);
	}

	@Override
	public IntSet vertices() {
		return verticesIdStrat.indices();
	}

	@Override
	public IntSet edges() {
		if (edgesSet == null) {
			if (m == 0)
				return IntSets.emptySet();
			if (userProvideEdgesIds) {
				edgesSet = new EdgesSetProvidedIdx();
			} else {
				edgesSet = edgesIdStrat.indices();
			}
		}
		return edgesSet;
	}

	@Override
	public int addVertex() {
		int u = verticesIdStrat.newIdx();
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
			throw new IllegalArgumentException(
					"Can't mix addEdge(u,v) and addEdge(u,v,id), if IDs are provided for some of the edges, they must be provided for all");
		int e = m++;
		int eFromIdStrat = edgesIdStrat.newIdx();
		assert e == eFromIdStrat;
		if (e * 2 == endpoints.length)
			endpoints = Arrays.copyOf(endpoints, Math.max(2, 2 * endpoints.length));
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
			throw new IllegalArgumentException(
					"Can't mix addEdge(u,v) and addEdge(u,v,id), if IDs are provided for some of the edges, they must be provided for all");
		int eIdx = m++;
		while (eIdx >= edgesIdStrat.size())
			edgesIdStrat.newIdx();
		if (eIdx * 2 == endpoints.length)
			endpoints = Arrays.copyOf(endpoints, Math.max(4, 2 * endpoints.length));
		if (eIdx == edgesUserIds.length)
			edgesUserIds = Arrays.copyOf(edgesUserIds, Math.max(2, 2 * edgesUserIds.length));
		setEdgeSource(eIdx, source);
		setEdgeTarget(eIdx, target);
		edgesUserIds[eIdx] = edge;
		edgesUserWeights.ensureCapacity(edge + 1);
		userProvideEdgesIds = true;
	}

	@Override
	public void clear() {
		m = 0;
		verticesIdStrat.clear();
		edgesIdStrat.clear();
		edgesSet = null;
		userProvideEdgesIds = false;
		verticesUserWeights.clearContainers();
		edgesUserWeights.clearContainers();

	}

	int edgeSource(int e) {
		return endpoints[e * 2 + 0];
	}

	int edgeTarget(int e) {
		return endpoints[e * 2 + 1];
	}

	int edgeEndpoint(int e,int endpoint) {
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
		endpoints[e * 2 + 0] = source;
	}

	private void setEdgeTarget(int e, int target) {
		endpoints[e * 2 + 1] = target;
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
		for (int e = 0; e < m; e++)
			assert e == edgesUserIds[e];
	}

	@Override
	public <V, WeightsT extends Weights<V>> WeightsT getVerticesWeights(Object key) {
		return verticesUserWeights.getWeights(key);
	}

	@Override
	public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type, V defVal) {
		WeightsImpl.IndexMutable<V> weights = WeightsImpl.IndexMutable.newInstance(verticesIdStrat, type, defVal);
		verticesUserWeights.addWeights(key, weights);
		@SuppressWarnings("unchecked")
		WeightsT weights0 = (WeightsT) weights;
		return weights0;
	}

	@Override
	public Set<Object> getVerticesWeightsKeys() {
		return verticesUserWeights.weightsKeys();
	}

	@Override
	public <E, WeightsT extends Weights<E>> WeightsT getEdgesWeights(Object key) {
		return edgesUserWeights.getWeights(key);
	}

	@Override
	public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type, E defVal) {
		WeightsImpl.IndexMutable<E> weights = WeightsImpl.IndexMutable.newInstance(edgesIdStrat, type, defVal);
		edgesUserWeights.addWeights(key, weights);
		@SuppressWarnings("unchecked")
		WeightsT weights0 = (WeightsT) weights;
		return weights0;
	}

	@Override
	public Set<Object> getEdgesWeightsKeys() {
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

		Undirected(IndexGraph g) {
			super(g);
			Assertions.Graphs.onlyUndirected(g);
		}

		@Override
		public IndexGraph build() {
			validateUserProvidedIdsBeforeBuild();
			GraphCSRBase.BuilderProcessEdgesUndirected processEdges =
					new GraphCSRBase.BuilderProcessEdgesUndirected(this);
			return new GraphCSRUndirected(this, processEdges);
		}

		@Override
		public IndexGraph buildMutable() {
			validateUserProvidedIdsBeforeBuild();
			return new GraphArrayUndirected(this);
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

		Directed(IndexGraph g) {
			super(g);
			Assertions.Graphs.onlyDirected(g);
		}

		@Override
		public IndexGraph build() {
			validateUserProvidedIdsBeforeBuild();
			GraphCSRBase.BuilderProcessEdgesDirected processEdges = new GraphCSRBase.BuilderProcessEdgesDirected(this);
			return new GraphCSRDirected(this, processEdges);
		}

		@Override
		public IndexGraph buildMutable() {
			validateUserProvidedIdsBeforeBuild();
			return new GraphArrayDirected(this);
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
