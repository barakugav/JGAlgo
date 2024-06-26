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

import static com.jgalgo.internal.util.Range.range;
import com.jgalgo.internal.util.Assertions;

final class GraphCsrUndirected extends GraphCsrAbstractUnindexed {

	GraphCsrUndirected(IndexGraphBuilderImpl builder, ProcessedEdgesUndirected processEdges, boolean fastLookup) {
		super(false, builder, processEdges, fastLookup);
		assert !builder.isDirected();
	}

	GraphCsrUndirected(IndexGraph g, boolean copyVerticesWeights, boolean copyEdgesWeights, boolean fastLookup) {
		super(false, g, copyVerticesWeights, copyEdgesWeights, fastLookup);
		Assertions.onlyUndirected(g);
	}

	@Override
	public int getEdge(int source, int target) {
		checkVertex(source);
		if (fastLookup) {
			checkVertex(target);
			return super.fastGetEdge(source, target);
		}

		final int begin = edgesOutBegin[source], end = edgesOutBegin[source + 1];
		for (int i : range(begin, end)) {
			int e = edgesOut[i];
			if (target == edgeEndpoint(e, source))
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

	private class EdgeSetOut extends IndexGraphBase.EdgeSetOutUndirected {

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
			return new EdgeIterOut(source, edgesOut, begin, end);
		}
	}

	class EdgeSetIn extends IndexGraphBase.EdgeSetInUndirected {

		final int begin, end;

		EdgeSetIn(int target) {
			super(target);
			begin = edgesOutBegin[target];
			end = edgesOutBegin[target + 1];
		}

		@Override
		public int size() {
			return end - begin;
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIterIn(target, edgesOut, begin, end);
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
			return edgeEndpoint(lastEdge, source);
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
			return edgeEndpoint(lastEdge, target);
		}

		@Override
		public int targetInt() {
			return target;
		}
	}

}
