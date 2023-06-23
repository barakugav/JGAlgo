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

import java.util.NoSuchElementException;
import java.util.Set;
import it.unimi.dsi.fastutil.ints.IntSet;

abstract class GraphCSRBase extends GraphBase implements IndexGraphImpl {

	final IdStrategy.FixedSize verticesIdStrat;
	final IdStrategy.FixedSize edgesIdStrat;

	final int[] edgesOutBegin;
	final int[] endpoints;

	final WeightsImpl.Index.Manager verticesUserWeights;
	final WeightsImpl.Index.Manager edgesUserWeights;

	GraphCSRBase(IndexGraphBuilderImpl builder, BuilderProcessEdges processEdges) {
		final int n = builder.vertices().size();
		final int m = builder.edges().size();

		verticesIdStrat = new IdStrategy.FixedSize(n);
		edgesIdStrat = new IdStrategy.FixedSize(m);

		edgesOutBegin = processEdges.edgesOutBegin;
		endpoints = new int[m * 2];
		assert edgesOutBegin.length == n + 1;

		verticesUserWeights = new WeightsImpl.Index.Manager(n);
		edgesUserWeights = new WeightsImpl.Index.Manager(m);
	}

	GraphCSRBase(IndexGraph g) {
		final int n = g.vertices().size();
		final int m = g.edges().size();

		verticesIdStrat = new IdStrategy.FixedSize(n);
		edgesIdStrat = new IdStrategy.FixedSize(m);

		edgesOutBegin = new int[n + 1];
		endpoints = new int[m * 2];

		for (int e : g.edges()) {
			endpoints[e * 2 + 0] = g.edgeSource(e);
			endpoints[e * 2 + 1] = g.edgeTarget(e);
		}

		verticesUserWeights = new WeightsImpl.Index.Manager(n);
		edgesUserWeights = new WeightsImpl.Index.Manager(m);
	}

	@Override
	public IntSet vertices() {
		return verticesIdStrat.indices();
	}

	@Override
	public IntSet edges() {
		return edgesIdStrat.indices();
	}

	@Override
	public int edgeSource(int edge) {
		return endpoints[edge * 2 + 0];
	}

	@Override
	public int edgeTarget(int edge) {
		return endpoints[edge * 2 + 1];
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
	public <V, WeightsT extends Weights<V>> WeightsT getVerticesWeights(Object key) {
		return verticesUserWeights.getWeights(key);
	}

	@Override
	public Set<Object> getVerticesWeightsKeys() {
		return verticesUserWeights.weightsKeys();
	}

	@Override
	public void removeVerticesWeights(Object key) {
		verticesUserWeights.removeWeights(key);
	}

	@Override
	@SuppressWarnings("unchecked")
	public <E, WeightsT extends Weights<E>> WeightsT getEdgesWeights(Object key) {
		return (WeightsT) edgesUserWeights.getWeights(key);
	}

	@Override
	public Set<Object> getEdgesWeightsKeys() {
		return edgesUserWeights.weightsKeys();
	}

	@Override
	public void removeEdgesWeights(Object key) {
		edgesUserWeights.removeWeights(key);
	}

	@Override
	public <V, WeightsT extends Weights<V>> WeightsT addVerticesWeights(Object key, Class<? super V> type, V defVal) {
		WeightsImpl.Index<V> weights = WeightsImpl.Index.newInstance(verticesIdStrat, type, defVal);
		verticesUserWeights.addWeights(key, weights);
		@SuppressWarnings("unchecked")
		WeightsT weights0 = (WeightsT) weights;
		return weights0;
	}

	@Override
	public <E, WeightsT extends Weights<E>> WeightsT addEdgesWeights(Object key, Class<? super E> type, E defVal) {
		WeightsImpl.Index<E> weights = WeightsImpl.Index.newInstance(edgesIdStrat, type, defVal);
		edgesUserWeights.addWeights(key, weights);
		@SuppressWarnings("unchecked")
		WeightsT weights0 = (WeightsT) weights;
		return weights0;
	}

	@Override
	public IdStrategy getVerticesIdStrategy() {
		return verticesIdStrat;
	}

	@Override
	public IdStrategy getEdgesIdStrategy() {
		return edgesIdStrat;
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
			if (!hasNext())
				throw new NoSuchElementException();
			return lastEdge = edges[idx++];
		}

		@Override
		public int peekNext() {
			if (!hasNext())
				throw new NoSuchElementException();
			return edges[idx];
		}
	}

	static class BuilderProcessEdges {

		int[] edgesOut;
		int[] edgesOutBegin;

	}

	static class BuilderProcessEdgesUndirected extends BuilderProcessEdges {

		BuilderProcessEdgesUndirected(IndexGraphBuilderImpl builder) {
			final int n = builder.vertices().size();
			final int m = builder.edges().size();

			/* Count how many out/in edges each vertex has */
			edgesOutBegin = new int[n + 1];
			for (int e = 0; e < m; e++) {
				int u = builder.endpoints[e * 2 + 0];
				int v = builder.endpoints[e * 2 + 1];
				if (!(0 <= u && u < n))
					throw new IndexOutOfBoundsException(u);
				if (!(0 <= v && v < n))
					throw new IndexOutOfBoundsException(v);
				edgesOutBegin[u]++;
				if (u != v)
					edgesOutBegin[v]++;
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
			for (int e = 0; e < m; e++) {
				int u = builder.endpoints[e * 2 + 0];
				int v = builder.endpoints[e * 2 + 1];
				int uOutIdx = edgesOutBegin[u]++;
				edgesOut[uOutIdx] = e;
				if (u != v) {
					int vInIdx = edgesOutBegin[v]++;
					edgesOut[vInIdx] = e;
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

		BuilderProcessEdgesDirected(IndexGraphBuilderImpl builder) {
			final int n = builder.vertices().size();
			final int m = builder.edges().size();

			edgesOut = new int[m];
			edgesOutBegin = new int[n + 1];
			edgesIn = new int[m];
			edgesInBegin = new int[n + 1];

			/* Count how many out/in edges each vertex has */
			for (int e = 0; e < m; e++) {
				int u = builder.endpoints[e * 2 + 0];
				int v = builder.endpoints[e * 2 + 1];
				if (!(0 <= u && u < n))
					throw new IndexOutOfBoundsException(u);
				if (!(0 <= v && v < n))
					throw new IndexOutOfBoundsException(v);
				edgesOutBegin[u]++;
				edgesInBegin[v]++;
			}
			if (n == 0)
				return;

			/*
			 * Arrange all the out-edges in a single continues array, where the out- edges of u are
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
			for (int e = 0; e < m; e++) {
				int uOutIdx = edgesOutBegin[builder.endpoints[e * 2 + 0]]++;
				int vInIdx = edgesInBegin[builder.endpoints[e * 2 + 1]]++;
				edgesOut[uOutIdx] = e;
				edgesIn[vInIdx] = e;
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

}
