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
 * An undirected graph implementation using arrays to store edge lists.
 *
 * <p>
 * The edges of each vertex will be stored as an array of ints. This implementation is the most efficient for most use
 * cases and should be used as the first choice for an undirected graph implementation.
 *
 * <p>
 * If the use case require multiple vertices/edges removals, {@link GraphLinkedUndirected} could be more efficient.
 *
 * @see    GraphArrayDirected
 * @author Barak Ugav
 */
class GraphArrayUndirected extends GraphArrayAbstract {

	private int[][] edges;
	private int[] edgesNum;
	private final DataContainer.Obj<int[]> edgesContainer;
	private final DataContainer.Int edgesNumContainer;

	private static final GraphBaseMutable.Capabilities CapabilitiesNoSelfEdges =
			GraphBaseMutable.Capabilities.of(false, false, true);
	private static final GraphBaseMutable.Capabilities CapabilitiesWithSelfEdges =
			GraphBaseMutable.Capabilities.of(false, true, true);

	private static GraphBaseMutable.Capabilities capabilities(boolean selfEdges) {
		return selfEdges ? CapabilitiesWithSelfEdges : CapabilitiesNoSelfEdges;
	}

	GraphArrayUndirected(boolean selfEdges, int expectedVerticesNum, int expectedEdgesNum) {
		super(capabilities(selfEdges), expectedVerticesNum, expectedEdgesNum);
		edgesContainer =
				newVerticesContainer(IntArrays.EMPTY_ARRAY, IntBigArrays.EMPTY_BIG_ARRAY, newArr -> edges = newArr);
		edgesNumContainer = newVerticesIntContainer(0, newArr -> edgesNum = newArr);
	}

	GraphArrayUndirected(boolean selfEdges, IndexGraph g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		super(capabilities(selfEdges), g, copyVerticesWeights, copyEdgesWeights);
		final int n = g.vertices().size();

		if (g instanceof GraphArrayUndirected) {
			GraphArrayUndirected g0 = (GraphArrayUndirected) g;
			edgesContainer =
					copyVerticesContainer(g0.edgesContainer, IntBigArrays.EMPTY_BIG_ARRAY, newArr -> edges = newArr);
			edgesNumContainer = copyVerticesContainer(g0.edgesNumContainer, newArr -> edgesNum = newArr);

			for (int v = 0; v < n; v++)
				edges[v] = Arrays.copyOf(edges[v], edgesNum[v]);
		} else {
			edgesContainer =
					newVerticesContainer(IntArrays.EMPTY_ARRAY, IntBigArrays.EMPTY_BIG_ARRAY, newArr -> edges = newArr);
			edgesNumContainer = newVerticesIntContainer(0, newArr -> edgesNum = newArr);

			for (int v = 0; v < n; v++) {
				IEdgeSet edgeSet = g.outEdges(v);
				int edgeSetSize = edgesNum[v] = edgeSet.size();
				if (edgeSetSize != 0) {
					int[] edgesArr = edges[v] = new int[edgeSetSize];
					int i = 0;
					for (int e : edgeSet)
						edgesArr[i++] = e;
				}
			}
		}
	}

	GraphArrayUndirected(boolean selfEdges, IndexGraphBuilderImpl builder) {
		super(capabilities(selfEdges), builder);
		assert !builder.isDirected();

		edgesContainer =
				newVerticesContainer(IntArrays.EMPTY_ARRAY, IntBigArrays.EMPTY_BIG_ARRAY, newArr -> edges = newArr);
		edgesNumContainer = newVerticesIntContainer(0, newArr -> edgesNum = newArr);

		for (int m = builder.edges().size(), e = 0; e < m; e++) {
			int source = builder.edgeSource(e);
			int target = builder.edgeTarget(e);

			addEdgeToList(edges, edgesNum, source, e);
			if (source != target)
				addEdgeToList(edges, edgesNum, target, e);
		}
	}

	@Override
	void removeVertexLast(int vertex) {
		assert edgesNum[vertex] == 0;
		// Reuse allocated edges arrays for v
		// edges.clear(v);
		super.removeVertexLast(vertex);
	}

	@Override
	void vertexSwapAndRemove(int removedIdx, int swappedIdx) {
		assert edgesNum[removedIdx] == 0;

		int[] edges = this.edges[swappedIdx];
		for (int num = edgesNum[swappedIdx], i = 0; i < num; i++)
			replaceEdgeEndpoint(edges[i], swappedIdx, removedIdx);

		swapAndClear(this.edges, removedIdx, swappedIdx, IntArrays.EMPTY_ARRAY);
		swapAndClear(edgesNum, removedIdx, swappedIdx, 0);
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
		addEdgeToList(edges, edgesNum, source, e);
		if (source != target)
			addEdgeToList(edges, edgesNum, target, e);
		return e;
	}

	@Override
	void removeEdgeLast(int edge) {
		int u = source(edge), v = target(edge);
		removeEdgeFromList(edges, edgesNum, u, edge);
		if (u != v)
			removeEdgeFromList(edges, edgesNum, v, edge);
		super.removeEdgeLast(edge);
	}

	@Override
	void edgeSwapAndRemove(int removedIdx, int swappedIdx) {
		int ur = source(removedIdx), vr = target(removedIdx);
		int[] urEdges = edges[ur];
		int urIdx = edgeIndexOf(urEdges, edgesNum[ur], removedIdx);
		urEdges[urIdx] = urEdges[--edgesNum[ur]];
		if (ur != vr) {
			int[] vrEdges = edges[vr];
			int vrIdx = edgeIndexOf(vrEdges, edgesNum[vr], removedIdx);
			vrEdges[vrIdx] = vrEdges[--edgesNum[vr]];
		}

		int us = source(swappedIdx), vs = target(swappedIdx);
		int[] usEdges = edges[us];
		int usIdx = edgeIndexOf(usEdges, edgesNum[us], swappedIdx);
		usEdges[usIdx] = removedIdx;
		if (us != vs) {
			int[] vsEdges = edges[vs];
			int vsIdx = edgeIndexOf(vsEdges, edgesNum[vs], swappedIdx);
			vsEdges[vsIdx] = removedIdx;
		}

		super.edgeSwapAndRemove(removedIdx, swappedIdx);
	}

	@Override
	public void removeEdgesOf(int source) {
		checkVertex(source);
		while (edgesNum[source] > 0)
			removeEdge(edges[source][0]);
	}

	@Override
	public void removeOutEdgesOf(int source) {
		removeEdgesOf(source);
	}

	@Override
	public void removeInEdgesOf(int target) {
		removeEdgesOf(target);
	}

	@Override
	public void moveEdge(int edge, int newSource, int newTarget) {
		checkEdge(edge);
		checkNewEdgeEndpoints(newSource, newTarget);
		int oldSource = source(edge), oldTarget = target(edge);
		removeEdgeFromList(edges, edgesNum, oldSource, edge);
		if (oldSource != oldTarget)
			removeEdgeFromList(edges, edgesNum, oldTarget, edge);
		addEdgeToList(edges, edgesNum, newSource, edge);
		if (newSource != newTarget)
			addEdgeToList(edges, edgesNum, newTarget, edge);
		setEndpoints(edge, newSource, newTarget);
	}

	@Override
	public void clearEdges() {
		edgesNumContainer.clear();
		super.clearEdges();
	}

	@Override
	public void clear() {
		super.clear();
		// Don't clear allocated edges arrays
		// edges.clear();
	}

	private class EdgeSetOut extends IndexGraphBase.EdgeSetOutUndirected {
		EdgeSetOut(int source) {
			super(source);
		}

		@Override
		public int size() {
			return edgesNum[source];
		}

		@Override
		public boolean isEmpty() {
			return size() == 0;
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIterOut(source, edges[source], edgesNum[source]);
		}
	}

	private class EdgeSetIn extends IndexGraphBase.EdgeSetInUndirected {
		EdgeSetIn(int target) {
			super(target);
		}

		@Override
		public int size() {
			return edgesNum[target];
		}

		@Override
		public boolean isEmpty() {
			return size() == 0;
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIterIn(target, edges[target], edgesNum[target]);
		}
	}

	private class EdgeIterOut extends GraphArrayAbstract.EdgeIterOut {

		EdgeIterOut(int source, int[] edges, int count) {
			super(source, edges, count);
		}

		@Override
		public int targetInt() {
			return edgeEndpoint(lastEdge, source);
		}
	}

	private class EdgeIterIn extends GraphArrayAbstract.EdgeIterIn {

		EdgeIterIn(int target, int[] edges, int count) {
			super(target, edges, count);
		}

		@Override
		public int sourceInt() {
			return edgeEndpoint(lastEdge, target);
		}
	}

}
