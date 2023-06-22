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
import java.util.function.BiConsumer;
import com.jgalgo.graph.Graphs.GraphCapabilitiesBuilder;

public class GraphCSRRemappedDirected extends GraphCSRBase {
	public static class Builder extends GraphCSRBase.BuilderDirected implements GraphBuilderFixedRemapped {
		@Override
		public GraphBuilderFixedRemapped.BuilderResult build() {
			processEdges();
			var callbackData = new Object() {
				int[] edgesCsrToOrig;
				int[] edgesOrigToCsr;
			};
			GraphCSRRemappedDirected g = new GraphCSRRemappedDirected(this, (edgesCsrToOrig, edgesOrigToCsr) -> {
				callbackData.edgesCsrToOrig = edgesCsrToOrig;
				callbackData.edgesOrigToCsr = edgesOrigToCsr;
			});
			return new GraphBuilderFixedRemapped.BuilderResult(g, callbackData.edgesCsrToOrig,
					callbackData.edgesOrigToCsr);
		}
	}

	private final int[] edgesIn;
	private final int[] edgesInBegin;

	private GraphCSRRemappedDirected(Builder builder, BiConsumer<int[], int[]> mappingCallback) {
		super(builder);
		final int n = builder.n;
		final int m = builder.m;

		edgesIn = builder.edgesIn;
		edgesInBegin = builder.edgesInBegin;
		int[] edgesOut = builder.edgesOut;
		assert edgesOut.length == m;
		assert edgesIn.length == m;
		assert edgesInBegin.length == n + 1;

		int[] edgesCsrToOrig = edgesOut;
		int[] edgesOrigToCsr = new int[m];
		for (int eCsr = 0; eCsr < m; eCsr++)
			edgesOrigToCsr[edgesCsrToOrig[eCsr]] = eCsr;
		for (int eIdx = 0; eIdx < m; eIdx++) {
			int eOrig = edgesIn[eIdx];
			int eCsr = edgesOrigToCsr[eOrig];
			edgesIn[eIdx] = eCsr;
		}

		for (int eCsr = 0; eCsr < m; eCsr++) {
			int eOrig = edgesCsrToOrig[eCsr];
			endpoints[eCsr * 2 + 0] = builder.endpoints[eOrig * 2 + 0];
			endpoints[eCsr * 2 + 1] = builder.endpoints[eOrig * 2 + 1];
		}

		mappingCallback.accept(edgesCsrToOrig, edgesOrigToCsr);
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

	private class EdgeIterOut implements EdgeIter {
		private final int source;
		private int nextEdge;
		private final int endIdx;

		EdgeIterOut(int source, int beginEdge, int endEdge) {
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
			if (!hasNext())
				throw new NoSuchElementException();
			return nextEdge++;
		}

		@Override
		public int peekNext() {
			if (!hasNext())
				throw new NoSuchElementException();
			return nextEdge;
		}

		@Override
		public int source() {
			return source;
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

}
