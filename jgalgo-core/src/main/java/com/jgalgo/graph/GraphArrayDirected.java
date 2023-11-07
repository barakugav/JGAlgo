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

	private static final IndexGraphBase.Capabilities Capabilities = IndexGraphBase.Capabilities.of(true, true, true);

	/**
	 * Create a new graph with no vertices and edges, with expected number of vertices and edges.
	 *
	 * @param expectedVerticesNum the expected number of vertices that will be in the graph
	 * @param expectedEdgesNum    the expected number of edges that will be in the graph
	 */
	GraphArrayDirected(int expectedVerticesNum, int expectedEdgesNum) {
		super(Capabilities, expectedVerticesNum, expectedEdgesNum);
		edgesOutContainer = new DataContainer.Obj<>(vertices, IntArrays.EMPTY_ARRAY, IntBigArrays.EMPTY_BIG_ARRAY,
				newArr -> edgesOut = newArr);
		edgesOutNumContainer = new DataContainer.Int(vertices, 0, newArr -> edgesOutNum = newArr);
		edgesInContainer = new DataContainer.Obj<>(vertices, IntArrays.EMPTY_ARRAY, IntBigArrays.EMPTY_BIG_ARRAY,
				newArr -> edgesIn = newArr);
		edgesInNumContainer = new DataContainer.Int(vertices, 0, newArr -> edgesInNum = newArr);

		addInternalVerticesContainer(edgesOutContainer);
		addInternalVerticesContainer(edgesOutNumContainer);
		addInternalVerticesContainer(edgesInContainer);
		addInternalVerticesContainer(edgesInNumContainer);
	}

	GraphArrayDirected(IndexGraph g, boolean copyWeights) {
		super(Capabilities, g, copyWeights);
		final int n = g.vertices().size();

		if (g instanceof GraphArrayDirected) {
			GraphArrayDirected g0 = (GraphArrayDirected) g;

			edgesOutContainer =
					g0.edgesOutContainer.copy(vertices, IntBigArrays.EMPTY_BIG_ARRAY, newArr -> edgesOut = newArr);
			edgesOutNumContainer = g0.edgesOutNumContainer.copy(vertices, newArr -> edgesOutNum = newArr);
			edgesInContainer =
					g0.edgesInContainer.copy(vertices, IntBigArrays.EMPTY_BIG_ARRAY, newArr -> edgesIn = newArr);
			edgesInNumContainer = g0.edgesInNumContainer.copy(vertices, newArr -> edgesInNum = newArr);

			addInternalVerticesContainer(edgesOutContainer);
			addInternalVerticesContainer(edgesOutNumContainer);
			addInternalVerticesContainer(edgesInContainer);
			addInternalVerticesContainer(edgesInNumContainer);

			for (int v = 0; v < n; v++) {
				edgesOut[v] = Arrays.copyOf(edgesOut[v], edgesOutNum[v]);
				edgesIn[v] = Arrays.copyOf(edgesIn[v], edgesInNum[v]);
			}
		} else {
			edgesOutContainer = new DataContainer.Obj<>(vertices, IntArrays.EMPTY_ARRAY, IntBigArrays.EMPTY_BIG_ARRAY,
					newArr -> edgesOut = newArr);
			edgesOutNumContainer = new DataContainer.Int(vertices, 0, newArr -> edgesOutNum = newArr);
			edgesInContainer = new DataContainer.Obj<>(vertices, IntArrays.EMPTY_ARRAY, IntBigArrays.EMPTY_BIG_ARRAY,
					newArr -> edgesIn = newArr);
			edgesInNumContainer = new DataContainer.Int(vertices, 0, newArr -> edgesInNum = newArr);

			addInternalVerticesContainer(edgesOutContainer);
			addInternalVerticesContainer(edgesOutNumContainer);
			addInternalVerticesContainer(edgesInContainer);
			addInternalVerticesContainer(edgesInNumContainer);

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

	GraphArrayDirected(IndexGraphBuilderImpl.Directed builder) {
		super(Capabilities, builder);
		edgesOutContainer = new DataContainer.Obj<>(vertices, IntArrays.EMPTY_ARRAY, IntBigArrays.EMPTY_BIG_ARRAY,
				newArr -> edgesOut = newArr);
		edgesOutNumContainer = new DataContainer.Int(vertices, 0, newArr -> edgesOutNum = newArr);
		edgesInContainer = new DataContainer.Obj<>(vertices, IntArrays.EMPTY_ARRAY, IntBigArrays.EMPTY_BIG_ARRAY,
				newArr -> edgesIn = newArr);
		edgesInNumContainer = new DataContainer.Int(vertices, 0, newArr -> edgesInNum = newArr);

		addInternalVerticesContainer(edgesOutContainer);
		addInternalVerticesContainer(edgesOutNumContainer);
		addInternalVerticesContainer(edgesInContainer);
		addInternalVerticesContainer(edgesInNumContainer);

		final int m = builder.edges().size();
		for (int e = 0; e < m; e++) {
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

		edgesOutContainer.swapAndClear(removedIdx, swappedIdx);
		edgesInContainer.swapAndClear(removedIdx, swappedIdx);
		edgesOutNumContainer.swapAndClear(removedIdx, swappedIdx);
		edgesInNumContainer.swapAndClear(removedIdx, swappedIdx);
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
		int u = edgeSource(edge), v = edgeTarget(edge);
		removeEdgeFromList(edgesOut, edgesOutNum, u, edge);
		removeEdgeFromList(edgesIn, edgesInNum, v, edge);
		super.removeEdgeLast(edge);
	}

	@Override
	void edgeSwapAndRemove(int removedIdx, int swappedIdx) {
		int ur = edgeSource(removedIdx), vr = edgeTarget(removedIdx);
		int us = edgeSource(swappedIdx), vs = edgeTarget(swappedIdx);
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
	public void reverseEdge(int edge) {
		int u = edgeSource(edge), v = edgeTarget(edge);
		if (u == v)
			return;
		removeEdgeFromList(edgesOut, edgesOutNum, u, edge);
		removeEdgeFromList(edgesIn, edgesInNum, v, edge);
		addEdgeToList(edgesOut, edgesOutNum, v, edge);
		addEdgeToList(edgesIn, edgesInNum, u, edge);
		super.reverseEdge0(edge);
	}

	@Override
	public void clearEdges() {
		edgesOutNumContainer.clear(edgesOutNum);
		edgesInNumContainer.clear(edgesInNum);
		super.clearEdges();
	}

	@Override
	public void clear() {
		super.clear();
		// Don't clear allocated edges arrays
		// edgesOut.clear();
		// edgesIn.clear();
	}

	private class EdgeSetOut extends IntGraphBase.EdgeSetOutDirected {
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

	private class EdgeSetIn extends IntGraphBase.EdgeSetInDirected {
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
			return edgeTarget(lastEdge);
		}
	}

	private class EdgeIterIn extends GraphArrayAbstract.EdgeIterIn {

		EdgeIterIn(int target, int[] edges, int count) {
			super(target, edges, count);
		}

		@Override
		public int sourceInt() {
			return edgeSource(lastEdge);
		}
	}

}
