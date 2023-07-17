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

import it.unimi.dsi.fastutil.ints.AbstractIntSet;

abstract class GraphCSRAbstractUnindexed extends GraphCSRBase {

	final int[] edgesOut;

	GraphCSRAbstractUnindexed(IndexGraphBuilderImpl builder, BuilderProcessEdges processEdges) {
		super(builder, processEdges, null);
		edgesOut = processEdges.edgesOut;

		final int m = builder.edges().size();
		for (int e = 0; e < m; e++) {
			endpoints[e * 2 + 0] = builder.edgeSource(e);
			endpoints[e * 2 + 1] = builder.edgeTarget(e);
		}
	}

	GraphCSRAbstractUnindexed(IndexGraph g) {
		super(g);
		final int n = g.vertices().size();
		final int m = g.edges().size();

		int edgesOutArrLen;
		if (getCapabilities().directed()) {
			edgesOutArrLen = m;
		} else {
			edgesOutArrLen = 0;
			for (int u = 0; u < n; u++)
				edgesOutArrLen += g.outEdges(u).size();
		}
		edgesOut = new int[edgesOutArrLen];

		for (int eIdx = 0, u = 0; u < n; u++) {
			edgesOutBegin[u] = eIdx;
			for (int e : g.outEdges(u))
				edgesOut[eIdx++] = e;
		}
		edgesOutBegin[n] = edgesOutArrLen;

		for (int e : g.edges()) {
			endpoints[e * 2 + 0] = g.edgeSource(e);
			endpoints[e * 2 + 1] = g.edgeTarget(e);
		}
	}

	abstract class EdgeSetSourceTarget extends AbstractIntSet implements EdgeSet {

		final int source;
		final int target;
		private final int edgeIdxBegin;
		private final int edgeIdxEnd;

		EdgeSetSourceTarget(int source, int target, int edgeIdxBegin, int edgeIdxEnd) {
			this.source = source;
			this.target = target;
			this.edgeIdxBegin = edgeIdxBegin;
			this.edgeIdxEnd = edgeIdxEnd;
		}

		@Override
		public int size() {
			return edgeIdxEnd - edgeIdxBegin;
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterSourceTarget(source, target, edgesOut, edgeIdxBegin, edgeIdxEnd);
		}
	}

	private static class EdgeIterSourceTarget extends EdgeIterAbstract {
		private final int source;
		private final int target;

		EdgeIterSourceTarget(int source, int target, int[] edges, int beginIdx, int endIdx) {
			super(edges, beginIdx, endIdx);
			this.source = source;
			this.target = target;
		}

		@Override
		public int source() {
			return source;
		}

		@Override
		public int target() {
			return target;
		}
	}

}