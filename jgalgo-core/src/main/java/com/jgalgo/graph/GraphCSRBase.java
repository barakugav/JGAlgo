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

import java.util.Map;
import java.util.Set;
import com.jgalgo.graph.EdgeEndpointsContainer.GraphWithEdgeEndpointsContainer;
import com.jgalgo.graph.Graphs.ImmutableGraph;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.JGAlgoUtils.Variant;

abstract class GraphCSRBase extends GraphBase implements GraphWithEdgeEndpointsContainer, ImmutableGraph {

	final GraphElementSet.FixedSize vertices;
	final GraphElementSet.FixedSize edges;

	final int[] edgesOutBegin;
	private final long[] endpoints;

	final Map<Object, WeightsImpl.IndexImmutable<?>> verticesUserWeights;
	final Map<Object, WeightsImpl.IndexImmutable<?>> edgesUserWeights;

	GraphCSRBase(Variant.Of2<IndexGraph, IndexGraphBuilderImpl> graphOrBuilder, BuilderProcessEdges processEdges,
			IndexGraphBuilder.ReIndexingMap edgesReIndexing, boolean copyWeights) {
		final int n = verticesNum(graphOrBuilder);
		final int m = edgesNum(graphOrBuilder);

		vertices = new GraphElementSet.FixedSize(n, false);
		edges = new GraphElementSet.FixedSize(m, true);

		edgesOutBegin = processEdges.edgesOutBegin;
		endpoints = new long[m];
		assert edgesOutBegin.length == n + 1;

		WeightsImpl.IndexImmutable.Builder verticesUserWeightsBuilder =
				new WeightsImpl.IndexImmutable.Builder(vertices);
		WeightsImpl.IndexImmutable.Builder edgesUserWeightsBuilder = new WeightsImpl.IndexImmutable.Builder(edges);
		if (copyWeights) {
			if (graphOrBuilder.contains(IndexGraph.class)) {
				IndexGraph g = graphOrBuilder.get(IndexGraph.class).get();
				for (Object weightKey : g.getVerticesWeightsKeys())
					verticesUserWeightsBuilder.copyAndAddWeights(weightKey, g.getVerticesWeights(weightKey));
				if (edgesReIndexing == null) {
					for (Object weightKey : g.getEdgesWeightsKeys())
						edgesUserWeightsBuilder.copyAndAddWeights(weightKey, g.getEdgesWeights(weightKey));
				} else {
					for (Object weightKey : g.getEdgesWeightsKeys())
						edgesUserWeightsBuilder.copyAndAddWeightsReindexed(weightKey, g.getEdgesWeights(weightKey),
								edgesReIndexing);
				}
			} else {
				IndexGraphBuilderImpl builder = graphOrBuilder.get(IndexGraphBuilderImpl.class).get();
				for (var entry : builder.verticesUserWeights.weights.entrySet())
					verticesUserWeightsBuilder.copyAndAddWeights(entry.getKey(), entry.getValue());
				if (edgesReIndexing == null) {
					for (var entry : builder.edgesUserWeights.weights.entrySet())
						edgesUserWeightsBuilder.copyAndAddWeights(entry.getKey(), entry.getValue());
				} else {
					for (var entry : builder.edgesUserWeights.weights.entrySet())
						edgesUserWeightsBuilder.copyAndAddWeightsReindexed(entry.getKey(), entry.getValue(),
								edgesReIndexing);
				}
			}
		}
		verticesUserWeights = verticesUserWeightsBuilder.build();
		edgesUserWeights = edgesUserWeightsBuilder.build();
	}

	GraphCSRBase(IndexGraph g, boolean copyWeights) {
		final int n = g.vertices().size();
		final int m = g.edges().size();

		vertices = new GraphElementSet.FixedSize(n, false);
		edges = new GraphElementSet.FixedSize(m, true);

		edgesOutBegin = new int[n + 1];
		endpoints = new long[m];

		for (int e = 0; e < m; e++)
			setEndpoints(e, g.edgeSource(e), g.edgeTarget(e));

		WeightsImpl.IndexImmutable.Builder verticesUserWeightsBuilder =
				new WeightsImpl.IndexImmutable.Builder(vertices);
		WeightsImpl.IndexImmutable.Builder edgesUserWeightsBuilder = new WeightsImpl.IndexImmutable.Builder(edges);
		if (copyWeights) {
			for (Object key : g.getVerticesWeightsKeys())
				verticesUserWeightsBuilder.copyAndAddWeights(key, g.getVerticesWeights(key));
			for (Object key : g.getEdgesWeightsKeys())
				edgesUserWeightsBuilder.copyAndAddWeights(key, g.getEdgesWeights(key));
		}
		verticesUserWeights = verticesUserWeightsBuilder.build();
		edgesUserWeights = edgesUserWeightsBuilder.build();
	}

	@Override
	public GraphElementSet vertices() {
		return vertices;
	}

	@Override
	public GraphElementSet edges() {
		return edges;
	}

	@Override
	public int addVertex() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void removeVertex(int vertex) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int addEdge(int source, int target) {
		throw new UnsupportedOperationException();
	}

	@Override
	public long[] edgeEndpoints() {
		return endpoints;
	}

	@Override
	public void removeEdge(int edge) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void reverseEdge(int edge) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clear() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void clearEdges() {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <V, WeightsT extends Weights<V>> WeightsT getVerticesWeights(Object key) {
		return (WeightsT) verticesUserWeights.get(key);
	}

	@Override
	public Set<Object> getVerticesWeightsKeys() {
		return verticesUserWeights.keySet();
	}

	@Override
	public void removeVerticesWeights(Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E, WeightsT extends Weights<E>> WeightsT getEdgesWeights(Object key) {
		return (WeightsT) edgesUserWeights.get(key);
	}

	@Override
	public Set<Object> getEdgesWeightsKeys() {
		return edgesUserWeights.keySet();
	}

	@Override
	public void removeEdgesWeights(Object key) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type, V defVal) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type, E defVal) {
		throw new UnsupportedOperationException();
	}

	@Override
	public IndexGraph immutableCopy() {
		return this;
	}

	static abstract class EdgeIterAbstract implements EdgeIter {

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
			Assertions.Iters.hasNext(this);
			return lastEdge = edges[idx++];
		}

		@Override
		public int peekNext() {
			Assertions.Iters.hasNext(this);
			return edges[idx];
		}
	}

	static class BuilderProcessEdges {

		int[] edgesOut;
		int[] edgesOutBegin;

	}

	static class BuilderProcessEdgesUndirected extends BuilderProcessEdges {

		static BuilderProcessEdgesUndirected valueOf(IndexGraphBuilderImpl builder) {
			return new BuilderProcessEdgesUndirected(Variant.Of2.withB(builder));
		}

		static BuilderProcessEdgesUndirected valueOf(IndexGraph g) {
			return new BuilderProcessEdgesUndirected(Variant.Of2.withA(g));
		}

		private BuilderProcessEdgesUndirected(Variant.Of2<IndexGraph, IndexGraphBuilderImpl> graphOrBuilder) {
			final int n = verticesNum(graphOrBuilder);
			final int m = edgesNum(graphOrBuilder);

			/* Count how many out/in edges each vertex has */
			edgesOutBegin = new int[n + 1];
			if (graphOrBuilder.contains(IndexGraph.class)) {
				IndexGraph g = graphOrBuilder.get(IndexGraph.class).get();
				for (int e = 0; e < m; e++) {
					int u = g.edgeSource(e), v = g.edgeTarget(e);
					edgesOutBegin[u]++;
					if (u != v)
						edgesOutBegin[v]++;
				}
			} else {
				IndexGraphBuilderImpl builder = graphOrBuilder.get(IndexGraphBuilderImpl.class).get();
				for (int e = 0; e < m; e++) {
					int u = builder.edgeSource(e), v = builder.edgeTarget(e);
					edgesOutBegin[u]++;
					if (u != v)
						edgesOutBegin[v]++;
				}
			}
			if (n == 0)
				return;
			int outNumSum = 0;
			for (int v = 0; v < n; v++)
				outNumSum += edgesOutBegin[v];
			edgesOut = new int[outNumSum];

			/*
			 * Arrange all the out-edges in a single continues array, where the out- edges of u are
			 * edgesOutBegin[u]...edgesOutBegin[u+1]-1
			 */
			int nextOutNum = edgesOutBegin[0];
			edgesOutBegin[0] = 0;
			for (int v = 1; v < n; v++) {
				int nextOutNum0 = edgesOutBegin[v];
				edgesOutBegin[v] = edgesOutBegin[v - 1] + nextOutNum;
				nextOutNum = nextOutNum0;
			}
			edgesOutBegin[n] = outNumSum;
			if (graphOrBuilder.contains(IndexGraph.class)) {
				IndexGraph g = graphOrBuilder.get(IndexGraph.class).get();
				for (int e = 0; e < m; e++) {
					int u = g.edgeSource(e), v = g.edgeTarget(e);
					int uOutIdx = edgesOutBegin[u]++;
					edgesOut[uOutIdx] = e;
					if (u != v) {
						int vInIdx = edgesOutBegin[v]++;
						edgesOut[vInIdx] = e;
					}
				}
			} else {
				IndexGraphBuilderImpl builder = graphOrBuilder.get(IndexGraphBuilderImpl.class).get();
				for (int e = 0; e < m; e++) {
					int u = builder.edgeSource(e), v = builder.edgeTarget(e);
					int uOutIdx = edgesOutBegin[u]++;
					edgesOut[uOutIdx] = e;
					if (u != v) {
						int vInIdx = edgesOutBegin[v]++;
						edgesOut[vInIdx] = e;
					}
				}
			}
			assert edgesOutBegin[n - 1] == outNumSum;
			for (int v = n - 1; v > 0; v--)
				edgesOutBegin[v] = edgesOutBegin[v - 1];
			edgesOutBegin[0] = 0;
		}

	}

	static class BuilderProcessEdgesDirected extends BuilderProcessEdges {

		int[] edgesIn;
		int[] edgesInBegin;

		static BuilderProcessEdgesDirected valueOf(IndexGraphBuilderImpl builder) {
			return new BuilderProcessEdgesDirected(Variant.Of2.withB(builder));
		}

		static BuilderProcessEdgesDirected valueOf(IndexGraph g) {
			return new BuilderProcessEdgesDirected(Variant.Of2.withA(g));
		}

		BuilderProcessEdgesDirected(Variant.Of2<IndexGraph, IndexGraphBuilderImpl> graphOrBuilder) {
			final int n = verticesNum(graphOrBuilder);
			final int m = edgesNum(graphOrBuilder);

			edgesOut = new int[m];
			edgesOutBegin = new int[n + 1];
			edgesIn = new int[m];
			edgesInBegin = new int[n + 1];

			/* Count how many out/in edges each vertex has */
			if (graphOrBuilder.contains(IndexGraph.class)) {
				IndexGraph g = graphOrBuilder.get(IndexGraph.class).get();
				for (int e = 0; e < m; e++) {
					edgesOutBegin[g.edgeSource(e)]++;
					edgesInBegin[g.edgeTarget(e)]++;
				}
			} else {
				IndexGraphBuilderImpl builder = graphOrBuilder.get(IndexGraphBuilderImpl.class).get();
				for (int e = 0; e < m; e++) {
					edgesOutBegin[builder.edgeSource(e)]++;
					edgesInBegin[builder.edgeTarget(e)]++;
				}
			}
			if (edgesOutBegin[n] != 0 || edgesInBegin[n] != 0)
				throw new IndexOutOfBoundsException(n);
			if (n == 0)
				return;

			/*
			 * Arrange all the out-edges in a single continues array, where the out-edges of u are
			 * edgesOutBegin[u]...edgesOutBegin[u+1]-1 and similarly all in-edges edgesInBegin[v]...edgesInBegin[v+1]-1
			 */
			int nextOutNum = edgesOutBegin[0], nextInNum = edgesInBegin[0];
			edgesOutBegin[0] = edgesInBegin[0] = 0;
			for (int v = 1; v < n; v++) {
				int nextOutNum0 = edgesOutBegin[v];
				int nextInNum0 = edgesInBegin[v];
				edgesOutBegin[v] = edgesOutBegin[v - 1] + nextOutNum;
				edgesInBegin[v] = edgesInBegin[v - 1] + nextInNum;
				nextOutNum = nextOutNum0;
				nextInNum = nextInNum0;
			}
			edgesOutBegin[n] = edgesInBegin[n] = m;
			if (graphOrBuilder.contains(IndexGraph.class)) {
				IndexGraph g = graphOrBuilder.get(IndexGraph.class).get();
				for (int e = 0; e < m; e++) {
					int uOutIdx = edgesOutBegin[g.edgeSource(e)]++;
					int vInIdx = edgesInBegin[g.edgeTarget(e)]++;
					edgesOut[uOutIdx] = e;
					edgesIn[vInIdx] = e;
				}
			} else {
				IndexGraphBuilderImpl builder = graphOrBuilder.get(IndexGraphBuilderImpl.class).get();
				for (int e = 0; e < m; e++) {
					int uOutIdx = edgesOutBegin[builder.edgeSource(e)]++;
					int vInIdx = edgesInBegin[builder.edgeTarget(e)]++;
					edgesOut[uOutIdx] = e;
					edgesIn[vInIdx] = e;
				}
			}
			assert edgesOutBegin[n - 1] == m;
			assert edgesInBegin[n - 1] == m;
			for (int v = n - 1; v > 0; v--) {
				edgesOutBegin[v] = edgesOutBegin[v - 1];
				edgesInBegin[v] = edgesInBegin[v - 1];
			}
			edgesOutBegin[0] = edgesInBegin[0] = 0;
		}

	}

	static int verticesNum(Variant.Of2<IndexGraph, IndexGraphBuilderImpl> graphOrBuilder) {
		return graphOrBuilder.contains(IndexGraph.class) ? graphOrBuilder.get(IndexGraph.class).get().vertices().size()
				: graphOrBuilder.get(IndexGraphBuilderImpl.class).get().vertices().size();
	}

	static int edgesNum(Variant.Of2<IndexGraph, IndexGraphBuilderImpl> graphOrBuilder) {
		return graphOrBuilder.contains(IndexGraph.class) ? graphOrBuilder.get(IndexGraph.class).get().edges().size()
				: graphOrBuilder.get(IndexGraphBuilderImpl.class).get().edges().size();
	}

}
