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

import com.jgalgo.internal.util.Assertions;

class GraphCsrDirected extends GraphCsrAbstractUnindexed {

	private final int[] edgesIn;
	private final int[] edgesInBegin;

	GraphCsrDirected(IndexGraphBuilderImpl builder, BuilderProcessEdgesDirected processEdges) {
		super(true, builder, processEdges);
		edgesIn = processEdges.edgesIn;
		edgesInBegin = processEdges.edgesInBegin;
	}

	GraphCsrDirected(IndexGraph g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		super(true, g, copyVerticesWeights, copyEdgesWeights);
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
	}

	@Override
	public IEdgeSet outEdges(int source) {
		return new EdgeSetOut(source);
	}

	@Override
	public IEdgeSet inEdges(int target) {
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
		public boolean isEmpty() {
			return size() == 0;
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIterOut(source, edgesOut, begin, end);
		}
	}

	class EdgeSetIn extends IndexGraphBase.EdgeSetInDirected {

		final int begin, end;

		EdgeSetIn(int target) {
			super(target);
			begin = edgesInBegin[target];
			end = edgesInBegin[target + 1];
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
		public IEdgeIter iterator() {
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
		public int sourceInt() {
			return source;
		}

		@Override
		public int targetInt() {
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
		public int sourceInt() {
			return edgeSource(lastEdge);
		}

		@Override
		public int targetInt() {
			return target;
		}
	}

}
