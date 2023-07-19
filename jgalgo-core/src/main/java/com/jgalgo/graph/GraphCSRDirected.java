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

import com.jgalgo.graph.Graphs.GraphCapabilitiesBuilder;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIntPair;

class GraphCSRDirected extends GraphCSRAbstractUnindexed {

	private final int[] edgesIn;
	private final int[] edgesInBegin;

	GraphCSRDirected(IndexGraphBuilderImpl builder, BuilderProcessEdgesDirected processEdges) {
		super(builder, processEdges);
		edgesIn = processEdges.edgesIn;
		edgesInBegin = processEdges.edgesInBegin;
	}

	GraphCSRDirected(IndexGraph g) {
		super(g);
		Assertions.Graphs.onlyDirected(g);
		final int n = g.vertices().size();
		final int m = g.edges().size();

		edgesIn = new int[m];
		edgesInBegin = new int[n + 1];

		for (int eIdx = 0, v = 0; v < n; v++) {
			edgesInBegin[v] = eIdx;
			for (int e : g.inEdges(v))
				edgesIn[eIdx++] = e;
		}
		edgesInBegin[n] = m;

		for (int u = 0; u < n; u++) {
			IntArrays.quickSort(edgesOut, edgesOutBegin[u], edgesOutBegin[u + 1], (e1, e2) -> {
				int c;
				if ((c = Integer.compare(edgeTarget(e1), edgeTarget(e2))) != 0)
					return c;
				if ((c = Integer.compare(e1, e2)) != 0)
					return c;
				return 0;
			});
		}
		for (int v = 0; v < n; v++) {
			IntArrays.quickSort(edgesIn, edgesInBegin[v], edgesInBegin[v + 1], (e1, e2) -> {
				int c;
				if ((c = Integer.compare(edgeSource(e1), edgeSource(e2))) != 0)
					return c;
				if ((c = Integer.compare(e1, e2)) != 0)
					return c;
				return 0;
			});
		}
	}

	@Override
	public EdgeSet getEdges(int source, int target) {
		IntIntPair edgeRange = JGAlgoUtils.equalRange(edgesOutBegin[source], edgesOutBegin[source + 1], target,
				eIdx -> edgeTarget(edgesOut[eIdx]));
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
			return new EdgeIterOut(source, edgesOut, edgesOutBegin[source], edgesOutBegin[source + 1]);
		}
	}

	private class EdgeSetSourceTarget extends GraphCSRAbstractUnindexed.EdgeSetSourceTarget {

		EdgeSetSourceTarget(int source, int target, int edgeIdxBegin, int edgeIdxEnd) {
			super(source, target, edgeIdxBegin, edgeIdxEnd);
		}

		@Override
		public boolean contains(int edge) {
			return edgeSource(edge) == source && edgeTarget(edge) == target;
		}
	}

	class EdgeSetIn extends GraphBase.EdgeSetInDirected {

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

	private class EdgeIterOut extends EdgeIterAbstract {
		private final int source;

		EdgeIterOut(int source, int[] edges, int beginIdx, int endIdx) {
			super(edges, beginIdx, endIdx);
			this.source = source;
		}

		@Override
		public int source() {
			return source;
		}

		@Override
		public int target() {
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

}
