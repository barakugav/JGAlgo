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
import com.jgalgo.graph.Graphs.GraphCapabilitiesBuilder;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Utils;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntIntPair;

class GraphCSRDirectedReindexed extends GraphCSRBase {

	private final int[] edgesIn;
	private final int[] edgesInBegin;

	private GraphCSRDirectedReindexed(IndexGraphBuilderImpl builder, BuilderProcessEdgesDirected processEdges,
			IndexGraphBuilder.ReIndexingMap edgesReIndexing) {
		super(builder, processEdges, edgesReIndexing);
		final int n = builder.vertices().size();
		final int m = builder.edges().size();

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

		for (int eCsr = 0; eCsr < m; eCsr++) {
			int eOrig = edgesReIndexing.reIndexedToOrig(eCsr);
			endpoints[eCsr * 2 + 0] = builder.edgeSource(eOrig);
			endpoints[eCsr * 2 + 1] = builder.edgeTarget(eOrig);
		}
	}

	static IndexGraphBuilder.ReIndexedGraph newInstance(IndexGraphBuilderImpl builder) {
		GraphCSRBase.BuilderProcessEdgesDirected processEdges = new GraphCSRBase.BuilderProcessEdgesDirected(builder);

		final int m = builder.edges().size();
		int[] edgesCsrToOrig = processEdges.edgesOut;
		int[] edgesOrigToCsr = new int[m];
		for (int eCsr = 0; eCsr < m; eCsr++)
			edgesOrigToCsr[edgesCsrToOrig[eCsr]] = eCsr;

		IndexGraphBuilder.ReIndexingMap edgesReIndexing =
				new IndexGraphBuilderImpl.ReIndexingMapImpl(edgesOrigToCsr, edgesCsrToOrig);

		GraphCSRDirectedReindexed g = new GraphCSRDirectedReindexed(builder, processEdges, edgesReIndexing);
		return new IndexGraphBuilderImpl.ReIndexedGraphImpl(g, Optional.empty(), Optional.of(edgesReIndexing));
	}

	@Override
	public EdgeSet getEdges(int source, int target) {
		IntIntPair edgeRange =
				Utils.equalRange(edgesOutBegin[source], edgesOutBegin[source + 1], target, this::edgeTarget);
		return edgeRange == null ? Edges.EmptyEdgeSet
				: new EdgeSetSourceTarget(source, target, edgeRange.firstInt(), edgeRange.secondInt());
	}

	@Override
	public EdgeSet outEdges(int source) {
		return new EdgeSetOut(source);
	}

	@Override
	public EdgeSet inEdges(int target) {
		return new EdgeSetIn(target);
	}

	@Override
	public GraphCapabilities getCapabilities() {
		return Capabilities;
	}

	private static final GraphCapabilities Capabilities =
			GraphCapabilitiesBuilder.newDirected().parallelEdges(true).selfEdges(true).build();

	private class EdgeSetOut extends GraphBase.EdgeSetOutDirected {

		EdgeSetOut(int source) {
			super(source);
		}

		@Override
		public int size() {
			return edgesOutBegin[source + 1] - edgesOutBegin[source];
		}

		@Override
		public boolean isEmpty() {
			return size() == 0;
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterOut(source, edgesOutBegin[source], edgesOutBegin[source + 1]);
		}
	}

	private class EdgeSetIn extends GraphBase.EdgeSetInDirected {

		EdgeSetIn(int target) {
			super(target);
		}

		@Override
		public int size() {
			return edgesInBegin[target + 1] - edgesInBegin[target];
		}

		@Override
		public boolean isEmpty() {
			return size() == 0;
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterIn(target, edgesIn, edgesInBegin[target], edgesInBegin[target + 1]);
		}
	}

	private class EdgeSetSourceTarget extends AbstractIntSet implements EdgeSet {

		private final int source;
		private final int target;
		private final int begin;
		private final int end;

		EdgeSetSourceTarget(int source, int target, int start, int end) {
			this.source = source;
			this.target = target;
			this.begin = start;
			this.end = end;
		}

		@Override
		public boolean contains(int edge) {
			return source == edgeSource(edge) && target == edgeTarget(edge);
		}

		@Override
		public int size() {
			return end - begin;
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterSourceTarget(source, target, begin, end);
		}
	}

	private static abstract class EdgeIterOutAbstract implements EdgeIter {
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
		public int peekNext() {
			Assertions.Iters.hasNext(this);
			return nextEdge;
		}

		@Override
		public int source() {
			return source;
		}
	}

	private class EdgeIterOut extends EdgeIterOutAbstract {
		EdgeIterOut(int source, int beginEdge, int endEdge) {
			super(source, beginEdge, endEdge);
		}

		@Override
		public int target() {
			int lastEdge = nextEdge - 1; // undefined behavior if nextInt() wasn't called
			return edgeTarget(lastEdge);
		}
	}

	private class EdgeIterIn extends EdgeIterAbstract {
		private final int target;

		EdgeIterIn(int target, int[] edges, int beginIdx, int endIdx) {
			super(edges, beginIdx, endIdx);
			this.target = target;
		}

		@Override
		public int source() {
			return edgeSource(lastEdge);
		}

		@Override
		public int target() {
			return target;
		}
	}

	private static class EdgeIterSourceTarget extends EdgeIterOutAbstract {

		private final int target;

		EdgeIterSourceTarget(int source, int target, int beginEdge, int endEnd) {
			super(source, beginEdge, endEnd);
			this.target = target;
		}

		@Override
		public int target() {
			return target;
		}
	}

}
