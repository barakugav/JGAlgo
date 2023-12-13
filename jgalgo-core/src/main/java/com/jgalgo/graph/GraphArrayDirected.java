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

import java.util.Arrays;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntBigArrays;

/**
 * A directed graph implementation using arrays to store edge lists.
 *
 * <p>
 * The edges of each vertex will be stored as an array of ints. This implementation is the most efficient for most use
 * cases and should be used as the first choice for a directed graph implementation.
 *
 * <p>
 * If the use case require multiple vertices/edges removals, {@link GraphLinkedDirected} could be more efficient.
 *
 * @see    GraphArrayUndirected
 * @author Barak Ugav
 */
class GraphArrayDirected extends GraphArrayAbstract {

	private int[][] edgesOut;
	private int[] edgesOutNum;
	private int[][] edgesIn;
	private int[] edgesInNum;
	private final DataContainer.Obj<int[]> edgesOutContainer;
	private final DataContainer.Int edgesOutNumContainer;
	private final DataContainer.Obj<int[]> edgesInContainer;
	private final DataContainer.Int edgesInNumContainer;

	private static final GraphBaseMutable.Capabilities CapabilitiesNoSelfEdges =
			GraphBaseMutable.Capabilities.of(true, false, true);
	private static final GraphBaseMutable.Capabilities CapabilitiesWithSelfEdges =
			GraphBaseMutable.Capabilities.of(true, true, true);

	private static GraphBaseMutable.Capabilities capabilities(boolean selfEdges) {
		return selfEdges ? CapabilitiesWithSelfEdges : CapabilitiesNoSelfEdges;
	}

	GraphArrayDirected(boolean selfEdges, int expectedVerticesNum, int expectedEdgesNum) {
		super(capabilities(selfEdges), expectedVerticesNum, expectedEdgesNum);
		edgesOutContainer =
				newVerticesContainer(IntArrays.EMPTY_ARRAY, IntBigArrays.EMPTY_BIG_ARRAY, newArr -> edgesOut = newArr);
		edgesOutNumContainer = newVerticesIntContainer(0, newArr -> edgesOutNum = newArr);
		edgesInContainer =
				newVerticesContainer(IntArrays.EMPTY_ARRAY, IntBigArrays.EMPTY_BIG_ARRAY, newArr -> edgesIn = newArr);
		edgesInNumContainer = newVerticesIntContainer(0, newArr -> edgesInNum = newArr);
	}

	GraphArrayDirected(boolean selfEdges, IndexGraph g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		super(capabilities(selfEdges), g, copyVerticesWeights, copyEdgesWeights);
		final int n = g.vertices().size();

		if (g instanceof GraphArrayDirected) {
			GraphArrayDirected g0 = (GraphArrayDirected) g;

			edgesOutContainer = copyVerticesContainer(g0.edgesOutContainer, IntBigArrays.EMPTY_BIG_ARRAY,
					newArr -> edgesOut = newArr);
			edgesOutNumContainer = copyVerticesContainer(g0.edgesOutNumContainer, newArr -> edgesOutNum = newArr);
			edgesInContainer = copyVerticesContainer(g0.edgesInContainer, IntBigArrays.EMPTY_BIG_ARRAY,
					newArr -> edgesIn = newArr);
			edgesInNumContainer = copyVerticesContainer(g0.edgesInNumContainer, newArr -> edgesInNum = newArr);

			for (int v = 0; v < n; v++) {
				edgesOut[v] = Arrays.copyOf(edgesOut[v], edgesOutNum[v]);
				edgesIn[v] = Arrays.copyOf(edgesIn[v], edgesInNum[v]);
			}
		} else {
			edgesOutContainer = newVerticesContainer(IntArrays.EMPTY_ARRAY, IntBigArrays.EMPTY_BIG_ARRAY,
					newArr -> edgesOut = newArr);
			edgesOutNumContainer = newVerticesIntContainer(0, newArr -> edgesOutNum = newArr);
			edgesInContainer = newVerticesContainer(IntArrays.EMPTY_ARRAY, IntBigArrays.EMPTY_BIG_ARRAY,
					newArr -> edgesIn = newArr);
			edgesInNumContainer = newVerticesIntContainer(0, newArr -> edgesInNum = newArr);

			for (int v = 0; v < n; v++) {
				IEdgeSet outEdges = g.outEdges(v);
				int outEdgesSize = edgesOutNum[v] = outEdges.size();
				if (outEdgesSize != 0) {
					int[] edges = edgesOut[v] = new int[outEdgesSize];
					int i = 0;
					for (int e : outEdges)
						edges[i++] = e;
				}
				IEdgeSet inEdges = g.inEdges(v);
				int inEdgesSize = edgesInNum[v] = inEdges.size();
				if (inEdgesSize != 0) {
					int[] edges = edgesIn[v] = new int[inEdgesSize];
					int i = 0;
					for (int e : inEdges)
						edges[i++] = e;
				}
			}
		}
	}

	GraphArrayDirected(boolean selfEdges, IndexGraphBuilderImpl.Artifacts builder) {
		super(capabilities(selfEdges), builder);
		assert builder.isDirected;

		edgesOutContainer =
				newVerticesContainer(IntArrays.EMPTY_ARRAY, IntBigArrays.EMPTY_BIG_ARRAY, newArr -> edgesOut = newArr);
		edgesOutNumContainer = newVerticesIntContainer(0, newArr -> edgesOutNum = newArr);
		edgesInContainer =
				newVerticesContainer(IntArrays.EMPTY_ARRAY, IntBigArrays.EMPTY_BIG_ARRAY, newArr -> edgesIn = newArr);
		edgesInNumContainer = newVerticesIntContainer(0, newArr -> edgesInNum = newArr);

		for (int m = builder.edges.size(), e = 0; e < m; e++) {
			addEdgeToList(edgesOut, edgesOutNum, builder.edgeSource(e), e);
			addEdgeToList(edgesIn, edgesInNum, builder.edgeTarget(e), e);
		}
	}

	@Override
	void removeVertexLast(int vertex) {
		assert edgesOutNum[vertex] == 0 && edgesInNum[vertex] == 0;
		// Reuse allocated edges arrays for v
		// edgesOut.clear(v);
		// edgesIn.clear(v);
		super.removeVertexLast(vertex);
	}

	@Override
	void vertexSwapAndRemove(int removedIdx, int swappedIdx) {
		assert edgesOutNum[removedIdx] == 0 && edgesInNum[removedIdx] == 0;

		int[] outEdges = edgesOut[swappedIdx], inEdges = edgesIn[swappedIdx];
		for (int num = edgesOutNum[swappedIdx], i = 0; i < num; i++)
			replaceEdgeSource(outEdges[i], removedIdx);
		for (int num = edgesInNum[swappedIdx], i = 0; i < num; i++)
			replaceEdgeTarget(inEdges[i], removedIdx);

		swapAndClear(edgesOut, removedIdx, swappedIdx, IntArrays.EMPTY_ARRAY);
		swapAndClear(edgesIn, removedIdx, swappedIdx, IntArrays.EMPTY_ARRAY);
		swapAndClear(edgesOutNum, removedIdx, swappedIdx, 0);
		swapAndClear(edgesInNum, removedIdx, swappedIdx, 0);
		// Reuse allocated edges arrays for v
		// edgesOut.clear(v);
		// edgesIn.clear(v);
		super.vertexSwapAndRemove(removedIdx, swappedIdx);
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

	@Override
	public int addEdge(int source, int target) {
		int e = super.addEdge(source, target);
		addEdgeToList(edgesOut, edgesOutNum, source, e);
		addEdgeToList(edgesIn, edgesInNum, target, e);
		return e;
	}

	@Override
	void removeEdgeLast(int edge) {
		int u = source(edge), v = target(edge);
		removeEdgeFromList(edgesOut, edgesOutNum, u, edge);
		removeEdgeFromList(edgesIn, edgesInNum, v, edge);
		super.removeEdgeLast(edge);
	}

	@Override
	void edgeSwapAndRemove(int removedIdx, int swappedIdx) {
		int ur = source(removedIdx), vr = target(removedIdx);
		int us = source(swappedIdx), vs = target(swappedIdx);
		int[] urEdges = edgesOut[ur], vrEdges = edgesIn[vr];
		int[] usEdges = edgesOut[us], vsEdges = edgesIn[vs];
		int urIdx = edgeIndexOf(urEdges, edgesOutNum[ur], removedIdx);
		int vrIdx = edgeIndexOf(vrEdges, edgesInNum[vr], removedIdx);
		urEdges[urIdx] = urEdges[--edgesOutNum[ur]];
		vrEdges[vrIdx] = vrEdges[--edgesInNum[vr]];
		int usIdx = edgeIndexOf(usEdges, edgesOutNum[us], swappedIdx);
		int vsIdx = edgeIndexOf(vsEdges, edgesInNum[vs], swappedIdx);
		usEdges[usIdx] = removedIdx;
		vsEdges[vsIdx] = removedIdx;
		super.edgeSwapAndRemove(removedIdx, swappedIdx);
	}

	@Override
	public void removeOutEdgesOf(int source) {
		checkVertex(source);
		while (edgesOutNum[source] > 0)
			removeEdge(edgesOut[source][0]);
	}

	@Override
	public void removeInEdgesOf(int target) {
		checkVertex(target);
		while (edgesInNum[target] > 0)
			removeEdge(edgesIn[target][0]);
	}

	@Override
	public void moveEdge(int edge, int newSource, int newTarget) {
		checkEdge(edge);
		checkNewEdgeEndpoints(newSource, newTarget);
		int oldSource = source(edge), oldTarget = target(edge);
		removeEdgeFromList(edgesOut, edgesOutNum, oldSource, edge);
		removeEdgeFromList(edgesIn, edgesInNum, oldTarget, edge);
		addEdgeToList(edgesOut, edgesOutNum, newSource, edge);
		addEdgeToList(edgesIn, edgesInNum, newTarget, edge);
		setEndpoints(edge, newSource, newTarget);
	}

	@Override
	public void clearEdges() {
		edgesOutNumContainer.clear();
		edgesInNumContainer.clear();
		super.clearEdges();
	}

	@Override
	public void clear() {
		super.clear();
		// Don't clear allocated edges arrays
		// edgesOut.clear();
		// edgesIn.clear();
	}

	private class EdgeSetOut extends IndexGraphBase.EdgeSetOutDirected {
		EdgeSetOut(int source) {
			super(source);
		}

		@Override
		public int size() {
			return edgesOutNum[source];
		}

		@Override
		public boolean isEmpty() {
			return size() == 0;
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIterOut(source, edgesOut[source], edgesOutNum[source]);
		}
	}

	private class EdgeSetIn extends IndexGraphBase.EdgeSetInDirected {
		EdgeSetIn(int target) {
			super(target);
		}

		@Override
		public int size() {
			return edgesInNum[target];
		}

		@Override
		public boolean isEmpty() {
			return size() == 0;
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIterIn(target, edgesIn[target], edgesInNum[target]);
		}
	}

	private class EdgeIterOut extends GraphArrayAbstract.EdgeIterOut {

		EdgeIterOut(int source, int[] edges, int count) {
			super(source, edges, count);
		}

		@Override
		public int targetInt() {
			return GraphArrayDirected.this.target(lastEdge);
		}
	}

	private class EdgeIterIn extends GraphArrayAbstract.EdgeIterIn {

		EdgeIterIn(int target, int[] edges, int count) {
			super(target, edges, count);
		}

		@Override
		public int sourceInt() {
			return GraphArrayDirected.this.source(lastEdge);
		}
	}

}
