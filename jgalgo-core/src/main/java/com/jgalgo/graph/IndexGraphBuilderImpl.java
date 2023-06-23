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
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;

abstract class IndexGraphBuilderImpl implements IndexGraphBuilder {

	private int n, m;
	int[] endpoints = IntArrays.EMPTY_ARRAY;
	private int[] edgesUserIds = IntArrays.EMPTY_ARRAY;
	private final IntSet verticesSet = new VerticesSet();
	private IntSet edgesSet;
	private boolean userProvideEdgesIds;

	@Override
	public IntSet vertices() {
		return verticesSet;
	}

	@Override
	public IntSet edges() {
		if (edgesSet == null) {
			if (m == 0)
				return IntSets.emptySet();
			if (userProvideEdgesIds) {
				edgesSet = new EdgesSetProvidedIdx();
			} else {
				edgesSet = new EdgesSetNonProvidedIdx();
			}
		}
		return edgesSet;
	}

	@Override
	public int addVertex() {
		return n++;
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
		if (e * 2 == endpoints.length)
			endpoints = Arrays.copyOf(endpoints, Math.max(2, 2 * endpoints.length));
		endpoints[e * 2 + 0] = source;
		endpoints[e * 2 + 1] = target;
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
		if (eIdx * 2 == endpoints.length)
			endpoints = Arrays.copyOf(endpoints, Math.max(4, 2 * endpoints.length));
		if (eIdx == edgesUserIds.length)
			edgesUserIds = Arrays.copyOf(edgesUserIds, Math.max(2, 2 * edgesUserIds.length));
		endpoints[eIdx * 2 + 0] = source;
		endpoints[eIdx * 2 + 1] = target;
		edgesUserIds[eIdx] = edge;
		userProvideEdgesIds = true;
	}

	@Override
	public void clear() {
		n = m = 0;
		edgesSet = null;
		userProvideEdgesIds = false;
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
			int u = endpoints[startIdx * 2 + 0];
			int v = endpoints[startIdx * 2 + 1];
			edgesUserIds[startIdx] = -1;
			for (;;) {
				int nextE = edgesUserIds[e];
				if (nextE == -1) {
					/* we completed a cycle */
					edgesUserIds[e] = e;
					endpoints[e * 2 + 0] = u;
					endpoints[e * 2 + 1] = v;
					break;
				} else if (nextE == e)
					throw new IllegalArgumentException("duplicate edge id: " + e);
				if (nextE >= m)
					throw new IllegalArgumentException(
							"Edges IDs should be 0,1,2,...,m-1. id >= m: " + nextE + " >= " + m);
				int nextU = endpoints[e * 2 + 0];
				int nextV = endpoints[e * 2 + 1];
				endpoints[e * 2 + 0] = u;
				endpoints[e * 2 + 1] = v;
				edgesUserIds[e] = e;
				u = nextU;
				v = nextV;
				e = nextE;
			}
		}
		for (int e = 0; e < m; e++)
			assert e == edgesUserIds[e];
	}

	private class VerticesSet extends AbstractIntSet {

		@Override
		public int size() {
			return n;
		}

		@Override
		public boolean contains(int v) {
			return v >= 0 && v < n;
		}

		@Override
		public IntIterator iterator() {
			return new Utils.RangeIter(n);
		}

		@Override
		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (!(other instanceof VerticesSet))
				return super.equals(other);
			VerticesSet o = (VerticesSet) other;
			return n == o.size();
		}

		@Override
		public int hashCode() {
			return n * (n + 1) / 2;
		}
	}

	private abstract class EdgesSetAbstract extends AbstractIntSet {
		@Override
		public int size() {
			return m;
		}

	}

	private class EdgesSetProvidedIdx extends EdgesSetAbstract {

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
					if (!hasNext())
						throw new NoSuchElementException();
					return edgesUserIds[idx++];
				}

			};
		}
	}

	private class EdgesSetNonProvidedIdx extends EdgesSetAbstract {

		@Override
		public boolean contains(int e) {
			return m >= 0 && e < m;
		}

		@Override
		public IntIterator iterator() {
			return new Utils.RangeIter(m);
		}

		@Override
		public boolean equals(Object other) {
			if (this == other)
				return true;
			if (!(other instanceof EdgesSetNonProvidedIdx))
				return super.equals(other);
			EdgesSetNonProvidedIdx o = (EdgesSetNonProvidedIdx) other;
			return m == o.size();
		}

		@Override
		public int hashCode() {
			return m * (m + 1) / 2;
		}
	}

	static class Undirected extends IndexGraphBuilderImpl {

		@Override
		public IndexGraph build() {
			validateUserProvidedIdsBeforeBuild();
			GraphCSRBase.BuilderProcessEdgesUndirected processEdges =
					new GraphCSRBase.BuilderProcessEdgesUndirected(this);
			return new GraphCSRUnmappedUndirected(this, processEdges);
		}

		@Override
		public IndexGraph buildMutable() {
			validateUserProvidedIdsBeforeBuild();
			final int n = vertices().size();
			final int m = edges().size();
			IndexGraph g = IndexGraphFactory.newUndirected().expectedVerticesNum(n).expectedEdgesNum(m).newGraph();
			for (int v = 0; v < m; v++)
				g.addVertex();
			for (int e = 0; e < m; e++)
				g.addEdge(endpoints[e * 2 + 0], endpoints[e * 2 + 1]);
			return g;
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

		@Override
		public IndexGraph build() {
			validateUserProvidedIdsBeforeBuild();
			GraphCSRBase.BuilderProcessEdgesDirected processEdges = new GraphCSRBase.BuilderProcessEdgesDirected(this);
			return new GraphCSRUnmappedDirected(this, processEdges);
		}

		@Override
		public IndexGraph buildMutable() {
			validateUserProvidedIdsBeforeBuild();
			final int n = vertices().size();
			final int m = edges().size();
			IndexGraph g = IndexGraphFactory.newDirected().expectedVerticesNum(n).expectedEdgesNum(m).newGraph();
			for (int v = 0; v < m; v++)
				g.addVertex();
			for (int e = 0; e < m; e++)
				g.addEdge(endpoints[e * 2 + 0], endpoints[e * 2 + 1]);
			return g;
		}

		@Override
		public IndexGraphBuilder.ReIndexedGraph reIndexAndBuild(boolean reIndexVertices, boolean reIndexEdges) {
			if (reIndexEdges) {
				validateUserProvidedIdsBeforeBuild();
				return GraphCSRRemappedDirected.newInstance(this);
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
