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
package com.jgalgo.alg.planar;

import java.util.Arrays;
import java.util.Objects;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightsLong;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.IntPair;
import it.unimi.dsi.fastutil.ints.IntList;

class PlanarEmbeddings {

	private PlanarEmbeddings() {}

	static class IndexEmbedding implements IPlanarEmbedding {

		private final IndexGraph g;
		private final IWeightsLong allEdgeIdx;
		private final boolean direction;

		private int[] allEdgesCw;
		private int[] allEdgesOffsets;
		private int[] outEdgesCw;
		private int[] outEdgesOffsets;
		private int[] inEdgesCw;
		private int[] inEdgesOffsets;
		private int[] edgeToOutIdx;
		private int[] edgeToInIdx;

		private IndexEmbedding reversedEmbedding;

		private IndexEmbedding(IndexGraph g, IWeightsLong allEdgeIdx, boolean direction) {
			this.g = Objects.requireNonNull(g);
			this.allEdgeIdx = Objects.requireNonNull(allEdgeIdx);
			this.direction = direction;
		}

		IndexEmbedding(IndexGraph g, IWeightsLong allEdgeIdx) {
			this(g, allEdgeIdx, true);
		}

		@Override
		public IEdgeIter allEdges(int vertex) {
			computeAllEdgesCwArr();
			return new EdgeIterDefaultSourceTarget(vertex, -1, direction, allEdgesCw, allEdgesOffsets);
		}

		@Override
		public IEdgeIter allEdges(int vertex, int precedingEdge) {
			computeAllEdgesCwArr();
			int precedingEdgeIndex = precedingEdgeToAllIdx(vertex, precedingEdge);
			return new EdgeIterDefaultSourceTarget(vertex, precedingEdgeIndex, direction, allEdgesCw,
					allEdgesOffsets);
		}

		@Override
		public IEdgeIter outEdges(int source) {
			if (g.isDirected()) {
				computeOutEdgesCwArr();
				return new EdgeIterDefaultSourceTarget(source, -1, direction, outEdgesCw, outEdgesOffsets);
			} else {
				return new VertexUndirectedOutIter(source, -1, direction);
			}
		}

		@Override
		public IEdgeIter outEdges(int source, int precedingEdge) {
			if (g.isDirected()) {
				if (source != g.edgeSource(precedingEdge))
					throw new IllegalArgumentException(
							"Preceding edge is not an outgoing edge of the given source vertex");
				computeOutEdgesCwArr();
				computeEdgeToOutIdx();
				int precedingEdgeIndex = edgeToOutIdx[precedingEdge];
				return new EdgeIterDefaultSourceTarget(source, precedingEdgeIndex, direction, outEdgesCw,
						outEdgesOffsets);
			} else {
				int precedingEdgeIndex = precedingEdgeToAllIdx(source, precedingEdge);
				return new VertexUndirectedOutIter(source, precedingEdgeIndex, direction);
			}
		}

		@Override
		public IEdgeIter inEdges(int target) {
			if (g.isDirected()) {
				computeInEdgesCwArr();
				return new EdgeIterDefaultSourceTarget(target, -1, direction, inEdgesCw, inEdgesOffsets);
			} else {
				return new VertexUndirectedInIter(target, -1, direction);
			}
		}

		@Override
		public IEdgeIter inEdges(int target, int precedingEdge) {
			if (g.isDirected()) {
				if (target != g.edgeTarget(precedingEdge))
					throw new IllegalArgumentException(
							"Preceding edge is not an ingoing edge of the given target vertex");
				computeInEdgesCwArr();
				computeEdgeToInIdx();
				int precedingEdgeIndex = edgeToInIdx[precedingEdge];
				return new EdgeIterDefaultSourceTarget(target, precedingEdgeIndex, direction, inEdgesCw,
						inEdgesOffsets);
			} else {
				int precedingEdgeIndex = precedingEdgeToAllIdx(target, precedingEdge);
				return new VertexUndirectedInIter(target, precedingEdgeIndex, direction);
			}
		}

		private int precedingEdgeToAllIdx(int vertex, int precedingEdge) {
			long eOrderIdx = allEdgeIdx.get(precedingEdge);
			return isSource(vertex, precedingEdge) ? IntPair.first(eOrderIdx) : IntPair.second(eOrderIdx);
		}

		private boolean isSource(int edge, int vertex) {
			if (vertex == g.edgeSource(edge))
				return true;
			if (vertex == g.edgeTarget(edge))
				return false;
			throw new IllegalArgumentException("Preceding edge is not incident to the vertex");
		}

		private void computeAllEdgesCwArr() {
			if (allEdgesCw != null)
				return;

			final int n = g.vertices().size();
			allEdgesOffsets = new int[n + 1];

			if (!g.isDirected()) {
				for (int v = 0; v < n; v++)
					allEdgesOffsets[v + 1] = allEdgesOffsets[v] + g.outEdges(v).size();

			} else if (!g.isAllowSelfEdges()) {
				for (int v = 0; v < n; v++)
					allEdgesOffsets[v + 1] = allEdgesOffsets[v] + g.outEdges(v).size() + g.inEdges(v).size();

			} else { /* directed with (maybe) self edges */
				for (int m = g.edges().size(), e = 0; e < m; e++) {
					int u = g.edgeSource(e), v = g.edgeTarget(e);
					allEdgesOffsets[u]++;
					if (u != v)
						allEdgesOffsets[v]++;
				}
				for (int offset = 0, v = 0; v < n; v++) {
					int vEdgesNum = allEdgesOffsets[v];
					allEdgesOffsets[v] = offset;
					offset += vEdgesNum;
				}
			}

			allEdgesCw = new int[allEdgesOffsets[n]];
			Arrays.fill(allEdgesCw, -1);
			for (int m = g.edges().size(), e = 0; e < m; e++) {
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				int uEdgesNum = allEdgesOffsets[u + 1] - allEdgesOffsets[u];
				long eOrderIdx = allEdgeIdx.get(e);
				int eOutIdx = IntPair.first(eOrderIdx), eInIdx = IntPair.second(eOrderIdx);
				if (!(0 <= eOutIdx && eOutIdx < uEdgesNum))
					throw new IllegalStateException("Out index out of range");
				if (allEdgesCw[allEdgesOffsets[u] + eOutIdx] >= 0)
					throw new IllegalStateException("Two edges with the same out index");
				allEdgesCw[allEdgesOffsets[u] + eOutIdx] = e;

				if (u != v) {
					int vEdgesNum = allEdgesOffsets[v + 1] - allEdgesOffsets[v];
					if (!(0 <= eInIdx && eInIdx < vEdgesNum))
						throw new IllegalStateException("In index out of range");
					if (allEdgesCw[allEdgesOffsets[v] + eInIdx] >= 0)
						throw new IllegalStateException("Two edges with the same in index");
					allEdgesCw[allEdgesOffsets[v] + eInIdx] = e;

				} else {
					if (eOutIdx != eInIdx)
						throw new IllegalStateException("Self edge with different out and in indices");
				}
			}
			assert !IntList.of(allEdgesCw).contains(-1);
		}

		private void computeOutEdgesCwArr() {
			if (outEdgesCw != null)
				return;
			assert g.isDirected();

			final int n = g.vertices().size();
			outEdgesOffsets = new int[n + 1];
			for (int v = 0; v < n; v++)
				outEdgesOffsets[v + 1] = outEdgesOffsets[v] + g.outEdges(v).size();
			outEdgesCw = new int[outEdgesOffsets[n]];

			computeAllEdgesCwArr();
			for (int v = 0; v < n; v++) {
				for (int eIdx = allEdgesOffsets[v]; eIdx < allEdgesOffsets[v + 1]; eIdx++) {
					int e = allEdgesCw[eIdx];
					if (v == g.edgeSource(e))
						outEdgesCw[outEdgesOffsets[v]++] = e;
				}
			}
			for (int v = 1; v < n; v++)
				outEdgesOffsets[v] = outEdgesOffsets[v - 1];
			outEdgesOffsets[0] = 0;
		}

		private void computeInEdgesCwArr() {
			if (inEdgesCw != null)
				return;
			assert g.isDirected();

			final int n = g.vertices().size();
			inEdgesOffsets = new int[n + 1];
			for (int v = 0; v < n; v++)
				inEdgesOffsets[v + 1] = inEdgesOffsets[v] + g.inEdges(v).size();
			inEdgesCw = new int[inEdgesOffsets[n]];

			computeAllEdgesCwArr();
			for (int v = 0; v < n; v++) {
				for (int eIdx = allEdgesOffsets[v]; eIdx < allEdgesOffsets[v + 1]; eIdx++) {
					int e = allEdgesCw[eIdx];
					if (v == g.edgeSource(e))
						inEdgesCw[inEdgesOffsets[v]++] = e;
				}
			}
			for (int v = 1; v < n; v++)
				inEdgesOffsets[v] = inEdgesOffsets[v - 1];
			inEdgesOffsets[0] = 0;
		}

		private void computeEdgeToOutIdx() {
			if (edgeToOutIdx != null)
				return;
			computeOutEdgesCwArr();

			final int m = g.edges().size();
			edgeToOutIdx = new int[m];
			assert outEdgesCw.length == m;
			for (int i = 0; i < m; i++) {
				int e = outEdgesCw[i];
				assert edgeToOutIdx[e] == 0;
				edgeToOutIdx[e] = i;
			}
			assert Arrays.stream(edgeToOutIdx).filter(i -> i == 0).count() == 1;
		}

		private void computeEdgeToInIdx() {
			if (edgeToInIdx != null)
				return;
			computeInEdgesCwArr();

			final int m = g.edges().size();
			edgeToInIdx = new int[m];
			assert inEdgesCw.length == m;
			for (int i = 0; i < m; i++) {
				int e = inEdgesCw[i];
				assert edgeToInIdx[e] == 0;
				edgeToInIdx[e] = i;
			}
			assert Arrays.stream(edgeToInIdx).filter(i -> i == 0).count() == 1;
		}

		private abstract static class IterBase implements IEdgeIter {

			private int count;
			private int cursor;
			int lastEdge = -1;
			private final int vBegin, vEnd;
			private final int advanceStep;
			private final int[] edgesCw;

			IterBase(int vertex, int precedingEdgeIndex, boolean direction, int[] edgesCw, int[] edgesOffsets) {
				if (direction) {
					vBegin = edgesOffsets[vertex];
					vEnd = edgesOffsets[vertex + 1];
				} else {
					vBegin = edgesOffsets[vertex + 1] - 1;
					vEnd = edgesOffsets[vertex] - 1;
				}
				this.edgesCw = edgesCw;
				advanceStep = direction ? 1 : -1;
				count = edgesOffsets[vertex + 1] - edgesOffsets[vertex];
				if (precedingEdgeIndex < 0) {
					cursor = vBegin;
				} else {
					cursor = vBegin + advanceStep * precedingEdgeIndex;
					advance();
				}
			}

			private void advance() {
				cursor += advanceStep;
				if (cursor == vEnd)
					cursor = vBegin;
			}

			@Override
			public boolean hasNext() {
				return count > 0;
			}

			@Override
			public int nextInt() {
				Assertions.hasNext(this);
				count--;
				int e = edgesCw[cursor];
				advance();
				return lastEdge = e;
			}

			@Override
			public int peekNextInt() {
				Assertions.hasNext(this);
				return edgesCw[cursor];
			}
		}

		// TODO make class static
		private class EdgeIterDefaultSourceTarget extends IterBase {

			EdgeIterDefaultSourceTarget(int vertex, int precedingEdgeIndex, boolean direction, int[] edgesCw,
					int[] edgesOffsets) {
				super(vertex, precedingEdgeIndex, direction, edgesCw, edgesOffsets);
			}

			@Override
			public int sourceInt() {
				return g.edgeSource(lastEdge);
			}

			@Override
			public int targetInt() {
				return g.edgeTarget(lastEdge);
			}
		}

		// TODO make class static
		private class VertexUndirectedOutIter extends IterBase {

			private final int source;

			VertexUndirectedOutIter(int source, int precedingEdgeIndex, boolean direction) {
				super(source, precedingEdgeIndex, direction, outEdgesCw, outEdgesOffsets);
				this.source = source;
			}

			@Override
			public int sourceInt() {
				return source;
			}

			@Override
			public int targetInt() {
				return g.edgeEndpoint(lastEdge, source);
			}
		}

		// TODO make class static
		private class VertexUndirectedInIter extends IterBase {

			private final int target;

			VertexUndirectedInIter(int target, int precedingEdgeIndex, boolean direction) {
				super(target, precedingEdgeIndex, direction, inEdgesCw, inEdgesOffsets);
				this.target = target;
			}

			@Override
			public int sourceInt() {
				return g.edgeEndpoint(lastEdge, target);
			}

			@Override
			public int targetInt() {
				return target;
			}
		}

		@Override
		public IPlanarEmbedding reversed() {
			if (reversedEmbedding == null) {
				reversedEmbedding = new IndexEmbedding(g, allEdgeIdx, !direction);
				reversedEmbedding.reversedEmbedding = this;
			}
			return reversedEmbedding;
		}

		@Override
		public IntGraph graph() {
			return g;
		}
	}

	private static class ObjEmbeddingFromIndexEmbedding<V, E> implements PlanarEmbedding<V, E> {

		private final IPlanarEmbedding indexEmbedding;
		private final Graph<V, E> g;
		private final IndexIdMap<V> viMap;
		private final IndexIdMap<E> eiMap;
		private ObjEmbeddingFromIndexEmbedding<V, E> reversed;

		ObjEmbeddingFromIndexEmbedding(Graph<V, E> g, IPlanarEmbedding indexEmbedding) {
			this.indexEmbedding = Objects.requireNonNull(indexEmbedding);
			this.g = Objects.requireNonNull(g);
			this.viMap = g.indexGraphVerticesMap();
			this.eiMap = g.indexGraphEdgesMap();
		}

		@Override
		public EdgeIter<V, E> allEdges(V vertex) {
			return IndexIdMaps.indexToIdEdgeIter(g, indexEmbedding.allEdges(viMap.idToIndex(vertex)));
		}

		@Override
		public EdgeIter<V, E> allEdges(V vertex, E precedingEdge) {
			return IndexIdMaps
					.indexToIdEdgeIter(g,
							indexEmbedding.allEdges(viMap.idToIndex(vertex), eiMap.idToIndex(precedingEdge)));
		}

		@Override
		public EdgeIter<V, E> outEdges(V source) {
			return IndexIdMaps.indexToIdEdgeIter(g, indexEmbedding.outEdges(viMap.idToIndex(source)));
		}

		@Override
		public EdgeIter<V, E> outEdges(V source, E precedingEdge) {
			return IndexIdMaps
					.indexToIdEdgeIter(g,
							indexEmbedding.outEdges(viMap.idToIndex(source), eiMap.idToIndex(precedingEdge)));
		}

		@Override
		public EdgeIter<V, E> inEdges(V target) {
			return IndexIdMaps.indexToIdEdgeIter(g, indexEmbedding.inEdges(viMap.idToIndex(target)));
		}

		@Override
		public EdgeIter<V, E> inEdges(V target, E precedingEdge) {
			return IndexIdMaps
					.indexToIdEdgeIter(g,
							indexEmbedding.inEdges(viMap.idToIndex(target), eiMap.idToIndex(precedingEdge)));
		}

		@Override
		public PlanarEmbedding<V, E> reversed() {
			if (reversed == null) {
				reversed = new ObjEmbeddingFromIndexEmbedding<>(g, indexEmbedding.reversed());
				reversed.reversed = this;
			}
			return reversed;
		}

		@Override
		public Graph<V, E> graph() {
			return g;
		}
	}

	private static class IntEmbeddingFromIndexEmbedding implements IPlanarEmbedding {

		private final IPlanarEmbedding indexEmbedding;
		private final IntGraph g;
		private final IndexIntIdMap viMap;
		private final IndexIntIdMap eiMap;
		private IntEmbeddingFromIndexEmbedding reversed;

		IntEmbeddingFromIndexEmbedding(IntGraph g, IPlanarEmbedding indexEmbedding) {
			this.indexEmbedding = Objects.requireNonNull(indexEmbedding);
			this.g = Objects.requireNonNull(g);
			this.viMap = g.indexGraphVerticesMap();
			this.eiMap = g.indexGraphEdgesMap();
		}

		@Override
		public IEdgeIter allEdges(int vertex) {
			return IndexIdMaps.indexToIdEdgeIter(g, indexEmbedding.allEdges(viMap.idToIndex(vertex)));
		}

		@Override
		public IEdgeIter allEdges(int vertex, int precedingEdge) {
			return IndexIdMaps
					.indexToIdEdgeIter(g,
							indexEmbedding.allEdges(viMap.idToIndex(vertex), eiMap.idToIndex(precedingEdge)));
		}

		@Override
		public IEdgeIter outEdges(int source) {
			return IndexIdMaps.indexToIdEdgeIter(g, indexEmbedding.outEdges(viMap.idToIndex(source)));
		}

		@Override
		public IEdgeIter outEdges(int source, int precedingEdge) {
			return IndexIdMaps
					.indexToIdEdgeIter(g,
							indexEmbedding.outEdges(viMap.idToIndex(source), eiMap.idToIndex(precedingEdge)));
		}

		@Override
		public IEdgeIter inEdges(int target) {
			return IndexIdMaps.indexToIdEdgeIter(g, indexEmbedding.inEdges(viMap.idToIndex(target)));
		}

		@Override
		public IEdgeIter inEdges(int target, int precedingEdge) {
			return IndexIdMaps
					.indexToIdEdgeIter(g,
							indexEmbedding.inEdges(viMap.idToIndex(target), eiMap.idToIndex(precedingEdge)));
		}

		@Override
		public IPlanarEmbedding reversed() {
			if (reversed == null) {
				reversed = new IntEmbeddingFromIndexEmbedding(g, indexEmbedding.reversed());
				reversed.reversed = this;
			}
			return reversed;
		}

		@Override
		public IntGraph graph() {
			return g;
		}
	}

	@SuppressWarnings("unchecked")
	static <V, E> PlanarEmbedding<V, E> embeddingFromIndexEmbedding(Graph<V, E> g, IPlanarEmbedding indexEmbedding) {
		assert !(g instanceof IndexGraph);
		if (g instanceof IntGraph) {
			return (PlanarEmbedding<V, E>) new IntEmbeddingFromIndexEmbedding((IntGraph) g, indexEmbedding);
		} else {
			return new ObjEmbeddingFromIndexEmbedding<>(g, indexEmbedding);
		}
	}

}
