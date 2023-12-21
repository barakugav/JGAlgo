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

import java.util.Optional;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.JGAlgoUtils.Variant2;

class GraphCsrDirectedReindexed extends GraphCsrBase {

	private final int[] edgesIn;
	private final int[] edgesInBegin;

	private GraphCsrDirectedReindexed(Variant2<IndexGraph, IndexGraphBuilderImpl.Artifacts> graphOrBuilder,
			BuilderProcessEdgesDirected processEdges, IndexGraphBuilder.ReIndexingMap edgesReIndexing,
			boolean copyVerticesWeights, boolean copyEdgesWeights) {
		super(true, graphOrBuilder, processEdges, edgesReIndexing, copyVerticesWeights, copyEdgesWeights);
		final int n = verticesNum(graphOrBuilder);
		final int m = edgesNum(graphOrBuilder);

		edgesIn = processEdges.edgesIn;
		edgesInBegin = processEdges.edgesInBegin;
		int[] edgesOut = processEdges.edgesOut;
		assert edgesOut.length == m;
		assert edgesIn.length == m;
		assert edgesInBegin.length == n + 1;

		for (int eIdx = 0; eIdx < m; eIdx++) {
			int eOrig = edgesIn[eIdx];
			int eCsr = edgesReIndexing.origToReIndexed(eOrig);
			edgesIn[eIdx] = eCsr;
		}

		edgeEndpoints = new long[m];
		if (graphOrBuilder.contains(IndexGraph.class)) {
			IndexGraph g = graphOrBuilder.get(IndexGraph.class);
			assert g.isDirected();

			for (int eCsr = 0; eCsr < m; eCsr++) {
				int eOrig = edgesReIndexing.reIndexedToOrig(eCsr);
				setEndpoints(eCsr, g.edgeSource(eOrig), g.edgeTarget(eOrig));
			}
		} else {
			IndexGraphBuilderImpl.Artifacts builder = graphOrBuilder.get(IndexGraphBuilderImpl.Artifacts.class);
			assert builder.isDirected;

			for (int eCsr = 0; eCsr < m; eCsr++) {
				int eOrig = edgesReIndexing.reIndexedToOrig(eCsr);
				setEndpoints(eCsr, builder.edgeSource(eOrig), builder.edgeTarget(eOrig));
			}
		}
	}

	static IndexGraphBuilder.ReIndexedGraph newInstance(IndexGraphBuilderImpl.Artifacts builder) {
		return newInstance(Variant2.ofB(builder), true, true);
	}

	static IndexGraphBuilder.ReIndexedGraph newInstance(IndexGraph g, boolean copyVerticesWeights,
			boolean copyEdgesWeights) {
		return newInstance(Variant2.ofA(g), copyVerticesWeights, copyEdgesWeights);
	}

	private static IndexGraphBuilder.ReIndexedGraph newInstance(
			Variant2<IndexGraph, IndexGraphBuilderImpl.Artifacts> graphOrBuilder, boolean copyVerticesWeights,
			boolean copyEdgesWeights) {
		GraphCsrBase.BuilderProcessEdgesDirected processEdges =
				new GraphCsrBase.BuilderProcessEdgesDirected(graphOrBuilder);

		final int m = edgesNum(graphOrBuilder);
		int[] edgesCsrToOrig = processEdges.edgesOut;
		int[] edgesOrigToCsr = new int[m];
		for (int eCsr = 0; eCsr < m; eCsr++)
			edgesOrigToCsr[edgesCsrToOrig[eCsr]] = eCsr;

		IndexGraphBuilder.ReIndexingMap edgesReIndexing =
				new IndexGraphBuilderImpl.ReIndexingMapImpl(edgesOrigToCsr, edgesCsrToOrig);

		GraphCsrDirectedReindexed g = new GraphCsrDirectedReindexed(graphOrBuilder, processEdges, edgesReIndexing,
				copyVerticesWeights, copyEdgesWeights);
		return new IndexGraphBuilderImpl.ReIndexedGraphImpl(g, Optional.empty(), Optional.of(edgesReIndexing));
	}

	@Override
	public int getEdge(int source, int target) {
		checkVertex(source);
		final int begin = edgesOutBegin[source], end = edgesOutBegin[source + 1];
		for (int e = begin; e < end; e++) {
			if (target == target(e))
				return e;
		}
		checkVertex(target);
		return -1;
	}

	@Override
	public IEdgeSet outEdges(int source) {
		checkVertex(source);
		return new EdgeSetOut(source);
	}

	@Override
	public IEdgeSet inEdges(int target) {
		checkVertex(target);
		return new EdgeSetIn(target);
	}

	private class EdgeSetOut extends IndexGraphBase.EdgeSetOutDirected {

		final int begin, end;

		EdgeSetOut(int source) {
			super(source);
			begin = edgesOutBegin[source];
			end = edgesOutBegin[source + 1];
		}

		@Override
		public int size() {
			return end - begin;
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIterOut(source, begin, end);
		}
	}

	private class EdgeSetIn extends IndexGraphBase.EdgeSetInDirected {

		final int begin, end;

		EdgeSetIn(int target) {
			super(target);
			begin = edgesInBegin[target];
			end = edgesInBegin[target + 1];
		}

		@Override
		public int size() {
			return end - begin;
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIterIn(target, edgesIn, begin, end);
		}
	}

	private abstract static class EdgeIterOutAbstract implements IEdgeIter {
		private final int source;
		int nextEdge;
		private final int endIdx;

		EdgeIterOutAbstract(int source, int beginEdge, int endEdge) {
			this.source = source;
			this.nextEdge = beginEdge;
			this.endIdx = endEdge;
		}

		@Override
		public boolean hasNext() {
			return nextEdge < endIdx;
		}

		@Override
		public int nextInt() {
			Assertions.Iters.hasNext(this);
			return nextEdge++;
		}

		@Override
		public int peekNextInt() {
			Assertions.Iters.hasNext(this);
			return nextEdge;
		}

		@Override
		public int sourceInt() {
			return source;
		}
	}

	private class EdgeIterOut extends EdgeIterOutAbstract {
		EdgeIterOut(int source, int beginEdge, int endEdge) {
			super(source, beginEdge, endEdge);
		}

		@Override
		public int targetInt() {
			int lastEdge = nextEdge - 1; // undefined behavior if nextInt() wasn't called
			return GraphCsrDirectedReindexed.this.target(lastEdge);
		}
	}

	private class EdgeIterIn extends EdgeIterAbstract {
		private final int target;

		EdgeIterIn(int target, int[] edges, int beginIdx, int endIdx) {
			super(edges, beginIdx, endIdx);
			this.target = target;
		}

		@Override
		public int sourceInt() {
			return GraphCsrDirectedReindexed.this.source(lastEdge);
		}

		@Override
		public int targetInt() {
			return target;
		}
	}

}
