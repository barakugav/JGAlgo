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

class GraphCSRUnmappedUndirected extends GraphCSRUnmappedAbstract {

	GraphCSRUnmappedUndirected(IndexGraphBuilderImpl builder, BuilderProcessEdgesUndirected processEdges) {
		super(builder, processEdges);
	}

	GraphCSRUnmappedUndirected(IndexGraph g) {
		super(g);
		ArgumentCheck.onlyUndirected(g);
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
			GraphCapabilitiesBuilder.newUndirected().parallelEdges(true).selfEdges(true).build();

	private class EdgeSetOut extends GraphBase.EdgeSetOutUndirected {

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

	class EdgeSetIn extends GraphBase.EdgeSetInUndirected {

		EdgeSetIn(int target) {
			super(target);
		}

		@Override
		public int size() {
			return edgesOutBegin[target + 1] - edgesOutBegin[target];
		}

		@Override
		public boolean isEmpty() {
			return size() == 0;
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterIn(target, edgesOut, edgesOutBegin[target], edgesOutBegin[target + 1]);
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
		public int source() {
			return edgeEndpoint(lastEdge, target);
		}

		@Override
		public int target() {
			return target;
		}
	}

}
