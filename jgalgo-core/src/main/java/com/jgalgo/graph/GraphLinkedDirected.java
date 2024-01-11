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

/**
 * A directed graph implementation using linked lists to store edge lists.
 *
 * <p>
 * The edges of each vertex will be stored as a linked list. This implementation is efficient in use cases where
 * multiple vertices/edges removals are performed, but it should not be the default choice for a directed graph.
 *
 * @see    GraphLinkedUndirected
 * @see    GraphArrayDirected
 * @author Barak Ugav
 */
class GraphLinkedDirected extends GraphLinkedAbstract implements GraphDefaultsDirected {

	private int[] edgesOutHead;
	private int[] edgesOutNum;
	private int[] edgesInHead;
	private int[] edgesInNum;

	private int[] edgeNextOut;
	private int[] edgeNextIn;
	private int[] edgePrevOut;
	private int[] edgePrevIn;

	private final DataContainer.Int edgesHeadOutContainer;
	private final DataContainer.Int edgesHeadInContainer;
	private final DataContainer.Int edgesOutNumContainer;
	private final DataContainer.Int edgesInNumContainer;

	private final DataContainer.Int edgeNextOutContainer;
	private final DataContainer.Int edgeNextInContainer;
	private final DataContainer.Int edgePrevOutContainer;
	private final DataContainer.Int edgePrevInContainer;

	private static final GraphBaseMutable.Capabilities CapabilitiesNoSelfEdges =
			GraphBaseMutable.Capabilities.of(true, false, true);
	private static final GraphBaseMutable.Capabilities CapabilitiesWithSelfEdges =
			GraphBaseMutable.Capabilities.of(true, true, true);

	private static GraphBaseMutable.Capabilities capabilities(boolean selfEdges) {
		return selfEdges ? CapabilitiesWithSelfEdges : CapabilitiesNoSelfEdges;
	}

	GraphLinkedDirected(boolean selfEdges, int expectedVerticesNum, int expectedEdgesNum) {
		super(capabilities(selfEdges), expectedVerticesNum, expectedEdgesNum);

		edgesHeadOutContainer = newVerticesIntContainer(-1, newArr -> edgesOutHead = newArr);
		edgesHeadInContainer = newVerticesIntContainer(-1, newArr -> edgesInHead = newArr);
		edgesOutNumContainer = newVerticesIntContainer(0, newArr -> edgesOutNum = newArr);
		edgesInNumContainer = newVerticesIntContainer(0, newArr -> edgesInNum = newArr);

		edgeNextOutContainer = newEdgesIntContainer(-1, newArr -> edgeNextOut = newArr);
		edgeNextInContainer = newEdgesIntContainer(-1, newArr -> edgeNextIn = newArr);
		edgePrevOutContainer = newEdgesIntContainer(-1, newArr -> edgePrevOut = newArr);
		edgePrevInContainer = newEdgesIntContainer(-1, newArr -> edgePrevIn = newArr);
	}

	GraphLinkedDirected(boolean selfEdges, IndexGraph g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		super(capabilities(selfEdges), g, copyVerticesWeights, copyEdgesWeights);

		edgesHeadOutContainer = newVerticesIntContainer(-1, newArr -> edgesOutHead = newArr);
		edgesHeadInContainer = newVerticesIntContainer(-1, newArr -> edgesInHead = newArr);
		edgesOutNumContainer = newVerticesIntContainer(0, newArr -> edgesOutNum = newArr);
		edgesInNumContainer = newVerticesIntContainer(0, newArr -> edgesInNum = newArr);

		edgeNextOutContainer = newEdgesIntContainer(-1, newArr -> edgeNextOut = newArr);
		edgeNextInContainer = newEdgesIntContainer(-1, newArr -> edgeNextIn = newArr);
		edgePrevOutContainer = newEdgesIntContainer(-1, newArr -> edgePrevOut = newArr);
		edgePrevInContainer = newEdgesIntContainer(-1, newArr -> edgePrevIn = newArr);

		final int m = g.edges().size();
		for (int e : range(m))
			addEdgeToLists(e);
	}

	GraphLinkedDirected(boolean selfEdges, IndexGraphBuilderImpl builder) {
		super(capabilities(selfEdges), builder);
		assert builder.isDirected();

		edgesHeadOutContainer = newVerticesIntContainer(-1, newArr -> edgesOutHead = newArr);
		edgesHeadInContainer = newVerticesIntContainer(-1, newArr -> edgesInHead = newArr);
		edgesOutNumContainer = newVerticesIntContainer(0, newArr -> edgesOutNum = newArr);
		edgesInNumContainer = newVerticesIntContainer(0, newArr -> edgesInNum = newArr);

		edgeNextOutContainer = newEdgesIntContainer(-1, newArr -> edgeNextOut = newArr);
		edgeNextInContainer = newEdgesIntContainer(-1, newArr -> edgeNextIn = newArr);
		edgePrevOutContainer = newEdgesIntContainer(-1, newArr -> edgePrevOut = newArr);
		edgePrevInContainer = newEdgesIntContainer(-1, newArr -> edgePrevIn = newArr);

		final int m = builder.edges.size();
		for (int e : range(m))
			addEdgeToLists(e);
	}

	@Override
	void removeVertexLast(int vertex) {
		edgesOutHead[vertex] = edgesInHead[vertex] = -1;
		super.removeVertexLast(vertex);
	}

	@Override
	void vertexSwapAndRemove(int removedIdx, int swappedIdx) {
		assert edgesOutNum[removedIdx] == 0 && edgesInNum[removedIdx] == 0;

		for (int p = edgesOutHead[swappedIdx]; p >= 0; p = edgeNextOut[p])
			replaceEdgeSource(p, removedIdx);
		for (int p = edgesInHead[swappedIdx]; p >= 0; p = edgeNextIn[p])
			replaceEdgeTarget(p, removedIdx);

		swapAndClear(edgesOutHead, removedIdx, swappedIdx, -1);
		swapAndClear(edgesInHead, removedIdx, swappedIdx, -1);
		swapAndClear(edgesOutNum, removedIdx, swappedIdx, 0);
		swapAndClear(edgesInNum, removedIdx, swappedIdx, 0);

		super.vertexSwapAndRemove(removedIdx, swappedIdx);
	}

	@Override
	public int getEdge(int source, int target) {
		checkVertex(source);
		for (int e = edgesOutHead[source]; e >= 0; e = edgeNextOut[e])
			if (target == target(e))
				return e;
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

	@Override
	public int addEdge(int source, int target) {
		int e = super.addEdge(source, target);
		addEdgeToLists(e);
		return e;
	}

	private void addEdgeToLists(int e) {
		int u = source(e), v = target(e), next;

		if ((next = edgesOutHead[u]) >= 0) {
			edgePrevOut[next] = e;
			edgeNextOut[e] = next;
		}
		edgesOutHead[u] = e;
		edgesOutNum[u]++;

		if ((next = edgesInHead[v]) >= 0) {
			edgePrevIn[next] = e;
			edgeNextIn[e] = next;
		}
		edgesInHead[v] = e;
		edgesInNum[v]++;
	}

	@Override
	void removeEdgeLast(int edge) {
		removeEdgeOutPointers(edge);
		removeEdgeInPointers(edge);
		super.removeEdgeLast(edge);
	}

	@Override
	void edgeSwapAndRemove(int removedIdx, int swappedIdx) {
		removeEdgeOutPointers(removedIdx);
		removeEdgeInPointers(removedIdx);

		edgeSwapAndRemove2(removedIdx, swappedIdx);
	}

	private void edgeSwapAndRemove2(int removedIdx, int swappedIdx) {
		int prev, next;
		if ((prev = edgePrevOut[swappedIdx]) >= 0) {
			edgeNextOut[prev] = removedIdx;
		} else {
			edgesOutHead[source(swappedIdx)] = removedIdx;
		}
		if ((next = edgeNextOut[swappedIdx]) >= 0)
			edgePrevOut[next] = removedIdx;

		if ((prev = edgePrevIn[swappedIdx]) >= 0) {
			edgeNextIn[prev] = removedIdx;
		} else {
			edgesInHead[target(swappedIdx)] = removedIdx;
		}
		if ((next = edgeNextIn[swappedIdx]) >= 0)
			edgePrevIn[next] = removedIdx;

		swapAndClear(edgeNextIn, removedIdx, swappedIdx, -1);
		swapAndClear(edgeNextOut, removedIdx, swappedIdx, -1);
		swapAndClear(edgePrevIn, removedIdx, swappedIdx, -1);
		swapAndClear(edgePrevOut, removedIdx, swappedIdx, -1);

		super.edgeSwapAndRemove(removedIdx, swappedIdx);
	}

	@Override
	public void removeOutEdgesOf(int source) {
		checkVertex(source);
		for (int p = edgesOutHead[source], next; p >= 0; p = next) {
			next = edgeNextOut[p];
			edgeNextOut[p] = edgePrevOut[p] = -1;
			removeEdgeInPointers(p);

			int lastEdge = edges().size() - 1;
			if (p == lastEdge) {
				super.removeEdgeLast(p);
			} else {
				int swappedEdge = lastEdge;
				edgeSwapAndRemove2(p, swappedEdge);
				if (next == swappedEdge)
					next = p;
			}
		}
		edgesOutHead[source] = -1;
		edgesOutNum[source] = 0;
	}

	@Override
	public void removeInEdgesOf(int target) {
		checkVertex(target);
		for (int p = edgesInHead[target], next; p >= 0; p = next) {
			next = edgeNextIn[p];
			edgeNextIn[p] = edgePrevIn[p] = -1;
			removeEdgeOutPointers(p);

			int lastEdge = edges().size() - 1;
			if (p == lastEdge) {
				super.removeEdgeLast(p);
			} else {
				int swappedEdge = lastEdge;
				edgeSwapAndRemove2(p, swappedEdge);
				if (next == swappedEdge)
					next = p;
			}
		}
		edgesInHead[target] = -1;
		edgesInNum[target] = 0;
	}

	private void removeEdgeOutPointers(int e) {
		int u = source(e);
		int next = edgeNextOut[e], prev = edgePrevOut[e];
		if (prev == -1) {
			edgesOutHead[u] = next;
		} else {
			edgeNextOut[prev] = next;
			edgePrevOut[e] = -1;
		}
		if (next >= 0) {
			edgePrevOut[next] = prev;
			edgeNextOut[e] = -1;
		}
		edgesOutNum[u]--;
	}

	private void removeEdgeInPointers(int e) {
		int v = target(e);
		int next = edgeNextIn[e], prev = edgePrevIn[e];
		if (prev == -1) {
			edgesInHead[v] = next;
		} else {
			edgeNextIn[prev] = next;
			edgePrevIn[e] = -1;
		}
		if (next >= 0) {
			edgePrevIn[next] = prev;
			edgeNextIn[e] = -1;
		}
		edgesInNum[v]--;
	}

	@Override
	public void moveEdge(int edge, int newSource, int newTarget) {
		checkEdge(edge);
		checkNewEdgeEndpoints(newSource, newTarget);
		removeEdgeOutPointers(edge);
		removeEdgeInPointers(edge);
		setEndpoints(edge, newSource, newTarget);
		addEdgeToLists(edge);
	}

	@Override
	public void clearEdges() {
		edgeNextOutContainer.clear();
		edgeNextInContainer.clear();
		edgePrevOutContainer.clear();
		edgePrevInContainer.clear();

		edgesHeadOutContainer.clear();
		edgesHeadInContainer.clear();
		edgesOutNumContainer.clear();
		edgesInNumContainer.clear();

		super.clearEdges();
	}

	private class EdgeSetOut extends IndexGraphBase.EdgeSetOutDirected {
		EdgeSetOut(int source) {
			super(source);
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIterOut(edgesOutHead[source]);
		}

		@Override
		public int size() {
			return edgesOutNum[source];
		}
	}

	private class EdgeSetIn extends IndexGraphBase.EdgeSetInDirected {
		EdgeSetIn(int target) {
			super(target);
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIterIn(edgesInHead[target]);
		}

		@Override
		public int size() {
			return edgesInNum[target];
		}
	}

	private abstract class EdgeIterImpl extends GraphLinkedAbstract.EdgeItr {
		EdgeIterImpl(int p) {
			super(p);
		}

		@Override
		public int sourceInt() {
			return GraphLinkedDirected.this.source(last);
		}

		@Override
		public int targetInt() {
			return GraphLinkedDirected.this.target(last);
		}
	}

	private class EdgeIterOut extends EdgeIterImpl {
		EdgeIterOut(int p) {
			super(p);
		}

		@Override
		int nextEdge(int n) {
			return edgeNextOut[n];
		}
	}

	private class EdgeIterIn extends EdgeIterImpl {
		EdgeIterIn(int p) {
			super(p);
		}

		@Override
		int nextEdge(int n) {
			return edgeNextIn[n];
		}
	}

	@Override
	void markVertex(int vertex) {
		edgesOutNum[vertex] = -edgesOutNum[vertex] - 1;
	}

	@Override
	void unmarkVertex(int vertex) {
		edgesOutNum[vertex] = -edgesOutNum[vertex] - 1;
	}

	@Override
	boolean isMarkedVertex(int vertex) {
		return edgesOutNum[vertex] < 0;
	}
}
