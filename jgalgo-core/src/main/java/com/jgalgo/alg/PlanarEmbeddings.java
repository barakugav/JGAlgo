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
package com.jgalgo.alg;

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
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntList;

class PlanarEmbeddings {

	private PlanarEmbeddings() {}

	static class Impl implements IPlanarEmbedding {

		private final IndexGraph g;
		private final IWeightsLong allEdgeIdx;

		private int[] allEdgesCw;
		private int[] allEdgesOffsets;
		private int[] outEdgesCw;
		private int[] outEdgesOffsets;
		private int[] inEdgesCw;
		private int[] inEdgesOffsets;
		private int[] edgeToOutIdx;
		private int[] edgeToInIdx;

		Impl(IndexGraph g, IWeightsLong allEdgeIdx) {
			this.g = Objects.requireNonNull(g);
			this.allEdgeIdx = Objects.requireNonNull(allEdgeIdx);
		}

		@Override
		public IEdgeIter allEdgesCw(int vertex) {
			computeAllEdgesCwArr();
			return new EdgeIterDefaultSourceTarget(vertex, -1, true, allEdgesCw, allEdgesOffsets);
		}

		@Override
		public IEdgeIter allEdgesCw(int vertex, int precedingEdge) {
			computeAllEdgesCwArr();
			int precedingEdgeIndex = precedingEdgeToAllIdx(vertex, precedingEdge);
			return new EdgeIterDefaultSourceTarget(vertex, precedingEdgeIndex, true, allEdgesCw, allEdgesOffsets);
		}

		@Override
		public IEdgeIter allEdgesCcw(int vertex) {
			computeAllEdgesCwArr();
			return new EdgeIterDefaultSourceTarget(vertex, -1, false, allEdgesCw, allEdgesOffsets);
		}

		@Override
		public IEdgeIter allEdgesCcw(int vertex, int precedingEdge) {
			computeAllEdgesCwArr();
			int precedingEdgeIndex = precedingEdgeToAllIdx(vertex, precedingEdge);
			return new EdgeIterDefaultSourceTarget(vertex, precedingEdgeIndex, false, allEdgesCw, allEdgesOffsets);
		}

		@Override
		public IEdgeIter outEdgesCw(int source) {
			if (g.isDirected()) {
				computeOutEdgesCwArr();
				return new EdgeIterDefaultSourceTarget(source, -1, true, outEdgesCw, outEdgesOffsets);
			} else {
				return new VertexUndirectedOutIter(source, -1, true);
			}
		}

		@Override
		public IEdgeIter outEdgesCw(int source, int precedingEdge) {
			if (g.isDirected()) {
				if (source != g.edgeSource(precedingEdge))
					throw new IllegalArgumentException(
							"Preceding edge is not an outgoing edge of the given source vertex");
				computeOutEdgesCwArr();
				computeEdgeToOutIdx();
				int precedingEdgeIndex = edgeToOutIdx[precedingEdge];
				return new EdgeIterDefaultSourceTarget(source, precedingEdgeIndex, true, outEdgesCw, outEdgesOffsets);
			} else {
				int precedingEdgeIndex = precedingEdgeToAllIdx(source, precedingEdge);
				return new VertexUndirectedOutIter(source, precedingEdgeIndex, true);
			}
		}

		@Override
		public IEdgeIter outEdgesCcw(int source) {
			if (g.isDirected()) {
				computeOutEdgesCwArr();
				return new EdgeIterDefaultSourceTarget(source, -1, false, outEdgesCw, outEdgesOffsets);
			} else {
				return new VertexUndirectedOutIter(source, -1, false);
			}
		}

		@Override
		public IEdgeIter outEdgesCcw(int source, int precedingEdge) {
			if (g.isDirected()) {
				if (source != g.edgeSource(precedingEdge))
					throw new IllegalArgumentException(
							"Preceding edge is not an outgoing edge of the given source vertex");
				computeOutEdgesCwArr();
				computeEdgeToOutIdx();
				int precedingEdgeIndex = edgeToOutIdx[precedingEdge];
				return new EdgeIterDefaultSourceTarget(source, precedingEdgeIndex, false, outEdgesCw, outEdgesOffsets);
			} else {
				int precedingEdgeIndex = precedingEdgeToAllIdx(source, precedingEdge);
				return new VertexUndirectedOutIter(source, precedingEdgeIndex, false);
			}
		}

		@Override
		public IEdgeIter inEdgesCw(int target) {
			if (g.isDirected()) {
				computeInEdgesCwArr();
				return new EdgeIterDefaultSourceTarget(target, -1, true, inEdgesCw, inEdgesOffsets);
			} else {
				return new VertexUndirectedInIter(target, -1, true);
			}
		}

		@Override
		public IEdgeIter inEdgesCw(int target, int precedingEdge) {
			if (g.isDirected()) {
				if (target != g.edgeTarget(precedingEdge))
					throw new IllegalArgumentException(
							"Preceding edge is not an ingoing edge of the given target vertex");
				computeInEdgesCwArr();
				computeEdgeToInIdx();
				int precedingEdgeIndex = edgeToInIdx[precedingEdge];
				return new EdgeIterDefaultSourceTarget(target, precedingEdgeIndex, true, inEdgesCw, inEdgesOffsets);
			} else {
				int precedingEdgeIndex = precedingEdgeToAllIdx(target, precedingEdge);
				return new VertexUndirectedInIter(target, precedingEdgeIndex, true);
			}
		}

		@Override
		public IEdgeIter inEdgesCcw(int target) {
			if (g.isDirected()) {
				computeInEdgesCwArr();
				return new EdgeIterDefaultSourceTarget(target, -1, false, inEdgesCw, inEdgesOffsets);
			} else {
				return new VertexUndirectedInIter(target, -1, false);
			}
		}

		@Override
		public IEdgeIter inEdgesCcw(int target, int precedingEdge) {
			if (g.isDirected()) {
				if (target != g.edgeTarget(precedingEdge))
					throw new IllegalArgumentException(
							"Preceding edge is not an ingoing edge of the given target vertex");
				computeInEdgesCwArr();
				computeEdgeToInIdx();
				int precedingEdgeIndex = edgeToInIdx[precedingEdge];
				return new EdgeIterDefaultSourceTarget(target, precedingEdgeIndex, false, inEdgesCw, inEdgesOffsets);
			} else {
				int precedingEdgeIndex = precedingEdgeToAllIdx(target, precedingEdge);
				return new VertexUndirectedInIter(target, precedingEdgeIndex, false);
			}
		}

		private int precedingEdgeToAllIdx(int vertex, int precedingEdge) {
			long eOrderIdx = allEdgeIdx.get(precedingEdge);
			return isSource(vertex, precedingEdge) ? JGAlgoUtils.long2low(eOrderIdx) : JGAlgoUtils.long2high(eOrderIdx);
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
				int eOutIdx = JGAlgoUtils.long2low(eOrderIdx), eInIdx = JGAlgoUtils.long2high(eOrderIdx);
				if (!(0 <= eOutIdx && eOutIdx < uEdgesNum))
					throw new IllegalStateException("Out index out of range");
				if (allEdgesCw[allEdgesOffsets[u] + eOutIdx] != -1)
					throw new IllegalStateException("Duplicate edge");
				allEdgesCw[allEdgesOffsets[u] + eOutIdx] = e;

				if (u != v) {
					int vEdgesNum = allEdgesOffsets[v + 1] - allEdgesOffsets[v];
					if (!(0 <= eInIdx && eInIdx < vEdgesNum))
						throw new IllegalStateException("In index out of range");
					if (allEdgesCw[allEdgesOffsets[v] + eInIdx] != -1)
						throw new IllegalStateException("Duplicate edge");
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

		private abstract class IterBase implements IEdgeIter {

			private int count;
			private int cursor;
			int lastEdge = -1;
			private final boolean cw;
			private final int vBegin, vEnd;
			private final int[] edgesCw;

			IterBase(int vertex, int precedingEdgeIndex, boolean cw, int[] edgesCw, int[] edgesOffsets) {
				if (this.cw = cw) {
					vBegin = edgesOffsets[vertex];
					vEnd = edgesOffsets[vertex + 1];
				} else {
					vBegin = edgesOffsets[vertex + 1] - 1;
					vEnd = edgesOffsets[vertex] - 1;
				}
				this.edgesCw = edgesCw;
				count = edgesOffsets[vertex] - edgesOffsets[vertex + 1];
				if (precedingEdgeIndex == -1) {
					cursor = vBegin;
				} else {
					cursor = vBegin + (cw ? 1 : -1) * precedingEdgeIndex;
					advance();
				}
			}

			private void advance() {
				cursor += cw ? 1 : -1;
				if (cursor == vEnd)
					cursor = vBegin;
			}

			@Override
			public boolean hasNext() {
				return count > 0;
			}

			@Override
			public int nextInt() {
				Assertions.Iters.hasNext(this);
				count--;
				int e = edgesCw[cursor];
				advance();
				return lastEdge = e;
			}

			@Override
			public int peekNextInt() {
				Assertions.Iters.hasNext(this);
				return edgesCw[cursor];
			}
		}

		private class EdgeIterDefaultSourceTarget extends IterBase {

			EdgeIterDefaultSourceTarget(int vertex, int precedingEdgeIndex, boolean cw, int[] edgesCw,
					int[] edgesOffsets) {
				super(vertex, precedingEdgeIndex, cw, edgesCw, edgesOffsets);
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

		private class VertexUndirectedOutIter extends IterBase {

			private final int source;

			VertexUndirectedOutIter(int source, int precedingEdgeIndex, boolean cw) {
				super(source, precedingEdgeIndex, cw, outEdgesCw, outEdgesOffsets);
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

		private class VertexUndirectedInIter extends IterBase {

			private final int target;

			VertexUndirectedInIter(int target, int precedingEdgeIndex, boolean cw) {
				super(target, precedingEdgeIndex, cw, inEdgesCw, inEdgesOffsets);
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

	}

	private static class ObjEmbeddingFromIndexEmbedding<V, E> implements PlanarEmbedding<V, E> {

		private final IPlanarEmbedding indexEmbedding;
		private final Graph<V, E> g;
		private final IndexIdMap<V> viMap;
		private final IndexIdMap<E> eiMap;

		ObjEmbeddingFromIndexEmbedding(Graph<V, E> g, IPlanarEmbedding indexEmbedding) {
			this.indexEmbedding = Objects.requireNonNull(indexEmbedding);
			this.g = Objects.requireNonNull(g);
			this.viMap = g.indexGraphVerticesMap();
			this.eiMap = g.indexGraphEdgesMap();
		}

		@Override
		public EdgeIter<V, E> allEdgesCw(V vertex) {
			return IndexIdMaps.indexToIdEdgeIter(g, indexEmbedding.allEdgesCw(viMap.idToIndex(vertex)));
		}

		@Override
		public EdgeIter<V, E> allEdgesCw(V vertex, E precedingEdge) {
			return IndexIdMaps.indexToIdEdgeIter(g,
					indexEmbedding.allEdgesCw(viMap.idToIndex(vertex), eiMap.idToIndex(precedingEdge)));
		}

		@Override
		public EdgeIter<V, E> allEdgesCcw(V vertex) {
			return IndexIdMaps.indexToIdEdgeIter(g, indexEmbedding.allEdgesCcw(viMap.idToIndex(vertex)));
		}

		@Override
		public EdgeIter<V, E> allEdgesCcw(V vertex, E precedingEdge) {
			return IndexIdMaps.indexToIdEdgeIter(g,
					indexEmbedding.allEdgesCcw(viMap.idToIndex(vertex), eiMap.idToIndex(precedingEdge)));
		}

		@Override
		public EdgeIter<V, E> outEdgesCw(V source) {
			return IndexIdMaps.indexToIdEdgeIter(g, indexEmbedding.outEdgesCw(viMap.idToIndex(source)));
		}

		@Override
		public EdgeIter<V, E> outEdgesCw(V source, E precedingEdge) {
			return IndexIdMaps.indexToIdEdgeIter(g,
					indexEmbedding.outEdgesCw(viMap.idToIndex(source), eiMap.idToIndex(precedingEdge)));
		}

		@Override
		public EdgeIter<V, E> outEdgesCcw(V source) {
			return IndexIdMaps.indexToIdEdgeIter(g, indexEmbedding.outEdgesCcw(viMap.idToIndex(source)));
		}

		@Override
		public EdgeIter<V, E> outEdgesCcw(V source, E precedingEdge) {
			return IndexIdMaps.indexToIdEdgeIter(g,
					indexEmbedding.outEdgesCcw(viMap.idToIndex(source), eiMap.idToIndex(precedingEdge)));
		}

		@Override
		public EdgeIter<V, E> inEdgesCw(V target) {
			return IndexIdMaps.indexToIdEdgeIter(g, indexEmbedding.inEdgesCw(viMap.idToIndex(target)));
		}

		@Override
		public EdgeIter<V, E> inEdgesCw(V target, E precedingEdge) {
			return IndexIdMaps.indexToIdEdgeIter(g,
					indexEmbedding.inEdgesCw(viMap.idToIndex(target), eiMap.idToIndex(precedingEdge)));
		}

		@Override
		public EdgeIter<V, E> inEdgesCcw(V target) {
			return IndexIdMaps.indexToIdEdgeIter(g, indexEmbedding.inEdgesCcw(viMap.idToIndex(target)));
		}

		@Override
		public EdgeIter<V, E> inEdgesCcw(V target, E precedingEdge) {
			return IndexIdMaps.indexToIdEdgeIter(g,
					indexEmbedding.inEdgesCcw(viMap.idToIndex(target), eiMap.idToIndex(precedingEdge)));
		}
	}

	private static class IntEmbeddingFromIndexEmbedding implements IPlanarEmbedding {

		private final IPlanarEmbedding indexEmbedding;
		private final IntGraph g;
		private final IndexIntIdMap viMap;
		private final IndexIntIdMap eiMap;

		IntEmbeddingFromIndexEmbedding(IntGraph g, IPlanarEmbedding indexEmbedding) {
			this.indexEmbedding = Objects.requireNonNull(indexEmbedding);
			this.g = Objects.requireNonNull(g);
			this.viMap = g.indexGraphVerticesMap();
			this.eiMap = g.indexGraphEdgesMap();
		}

		@Override
		public IEdgeIter allEdgesCw(int vertex) {
			return IndexIdMaps.indexToIdEdgeIter(g, indexEmbedding.allEdgesCw(viMap.idToIndex(vertex)));
		}

		@Override
		public IEdgeIter allEdgesCw(int vertex, int precedingEdge) {
			return IndexIdMaps.indexToIdEdgeIter(g,
					indexEmbedding.allEdgesCw(viMap.idToIndex(vertex), eiMap.idToIndex(precedingEdge)));
		}

		@Override
		public IEdgeIter allEdgesCcw(int vertex) {
			return IndexIdMaps.indexToIdEdgeIter(g, indexEmbedding.allEdgesCcw(viMap.idToIndex(vertex)));
		}

		@Override
		public IEdgeIter allEdgesCcw(int vertex, int precedingEdge) {
			return IndexIdMaps.indexToIdEdgeIter(g,
					indexEmbedding.allEdgesCcw(viMap.idToIndex(vertex), eiMap.idToIndex(precedingEdge)));
		}

		@Override
		public IEdgeIter outEdgesCw(int source) {
			return IndexIdMaps.indexToIdEdgeIter(g, indexEmbedding.outEdgesCw(viMap.idToIndex(source)));
		}

		@Override
		public IEdgeIter outEdgesCw(int source, int precedingEdge) {
			return IndexIdMaps.indexToIdEdgeIter(g,
					indexEmbedding.outEdgesCw(viMap.idToIndex(source), eiMap.idToIndex(precedingEdge)));
		}

		@Override
		public IEdgeIter outEdgesCcw(int source) {
			return IndexIdMaps.indexToIdEdgeIter(g, indexEmbedding.outEdgesCcw(viMap.idToIndex(source)));
		}

		@Override
		public IEdgeIter outEdgesCcw(int source, int precedingEdge) {
			return IndexIdMaps.indexToIdEdgeIter(g,
					indexEmbedding.outEdgesCcw(viMap.idToIndex(source), eiMap.idToIndex(precedingEdge)));
		}

		@Override
		public IEdgeIter inEdgesCw(int target) {
			return IndexIdMaps.indexToIdEdgeIter(g, indexEmbedding.inEdgesCw(viMap.idToIndex(target)));
		}

		@Override
		public IEdgeIter inEdgesCw(int target, int precedingEdge) {
			return IndexIdMaps.indexToIdEdgeIter(g,
					indexEmbedding.inEdgesCw(viMap.idToIndex(target), eiMap.idToIndex(precedingEdge)));
		}

		@Override
		public IEdgeIter inEdgesCcw(int target) {
			return IndexIdMaps.indexToIdEdgeIter(g, indexEmbedding.inEdgesCcw(viMap.idToIndex(target)));
		}

		@Override
		public IEdgeIter inEdgesCcw(int target, int precedingEdge) {
			return IndexIdMaps.indexToIdEdgeIter(g,
					indexEmbedding.inEdgesCcw(viMap.idToIndex(target), eiMap.idToIndex(precedingEdge)));
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
