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

/**
 * An undirected graph implementation using linked lists to store edge lists.
 *
 * <p>
 * The edges of each vertex will be stored as a linked list. This implementation is efficient in use cases where
 * multiple vertices/edges removals are performed, but it should not be the default choice for an undirected graph.
 *
 * @see    GraphLinkedDirected
 * @see    GraphArrayUndirected
 * @author Barak Ugav
 */
class GraphLinkedUndirected extends GraphLinkedAbstract {

	private int[] edgesHead;
	private int[] edgesNum;

	private int[] nextu;
	private int[] prevu;
	private int[] nextv;
	private int[] prevv;

	private final DataContainer.Int edgesHeadContainer;
	private final DataContainer.Int edgesNumContainer;

	private final DataContainer.Int nextuContainer;
	private final DataContainer.Int prevuContainer;
	private final DataContainer.Int nextvContainer;
	private final DataContainer.Int prevvContainer;

	private static final GraphBaseMutable.Capabilities CapabilitiesNoSelfEdges =
			GraphBaseMutable.Capabilities.of(false, false, true);
	private static final GraphBaseMutable.Capabilities CapabilitiesWithSelfEdges =
			GraphBaseMutable.Capabilities.of(false, true, true);

	private static GraphBaseMutable.Capabilities capabilities(boolean selfEdges) {
		return selfEdges ? CapabilitiesWithSelfEdges : CapabilitiesNoSelfEdges;
	}

	GraphLinkedUndirected(boolean selfEdges, int expectedVerticesNum, int expectedEdgesNum) {
		super(capabilities(selfEdges), expectedVerticesNum, expectedEdgesNum);

		edgesHeadContainer = newVerticesIntContainer(-1, newArr -> edgesHead = newArr);
		edgesNumContainer = newVerticesIntContainer(0, newArr -> edgesNum = newArr);

		nextuContainer = newEdgesIntContainer(-1, newArr -> nextu = newArr);
		prevuContainer = newEdgesIntContainer(-1, newArr -> prevu = newArr);
		nextvContainer = newEdgesIntContainer(-1, newArr -> nextv = newArr);
		prevvContainer = newEdgesIntContainer(-1, newArr -> prevv = newArr);
	}

	GraphLinkedUndirected(boolean selfEdges, IndexGraph g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		super(capabilities(selfEdges), g, copyVerticesWeights, copyEdgesWeights);

		edgesHeadContainer = newVerticesIntContainer(-1, newArr -> edgesHead = newArr);
		edgesNumContainer = newVerticesIntContainer(0, newArr -> edgesNum = newArr);

		nextuContainer = newEdgesIntContainer(-1, newArr -> nextu = newArr);
		prevuContainer = newEdgesIntContainer(-1, newArr -> prevu = newArr);
		nextvContainer = newEdgesIntContainer(-1, newArr -> nextv = newArr);
		prevvContainer = newEdgesIntContainer(-1, newArr -> prevv = newArr);

		final int m = g.edges().size();
		for (int e = 0; e < m; e++)
			addEdgeToLists(e);
	}

	GraphLinkedUndirected(boolean selfEdges, IndexGraphBuilderImpl builder) {
		super(capabilities(selfEdges), builder);
		assert !builder.isDirected();

		edgesHeadContainer = newVerticesIntContainer(-1, newArr -> edgesHead = newArr);
		edgesNumContainer = newVerticesIntContainer(0, newArr -> edgesNum = newArr);

		nextuContainer = newEdgesIntContainer(-1, newArr -> nextu = newArr);
		prevuContainer = newEdgesIntContainer(-1, newArr -> prevu = newArr);
		nextvContainer = newEdgesIntContainer(-1, newArr -> nextv = newArr);
		prevvContainer = newEdgesIntContainer(-1, newArr -> prevv = newArr);

		final int m = builder.edges().size();
		for (int e = 0; e < m; e++)
			addEdgeToLists(e);
	}

	@Override
	void removeVertexLast(int vertex) {
		edgesHead[vertex] = -1;
		super.removeVertexLast(vertex);
	}

	@Override
	void vertexSwapAndRemove(int removedIdx, int swappedIdx) {
		assert edgesNum[removedIdx] == 0;

		for (int p = edgesHead[swappedIdx], next; p >= 0; p = next) {
			next = next(p, swappedIdx);
			if (source(p) == swappedIdx)
				replaceEdgeSource(p, removedIdx);
			if (target(p) == swappedIdx)
				replaceEdgeTarget(p, removedIdx);
		}

		swapAndClear(edgesHead, removedIdx, swappedIdx, -1);
		swapAndClear(edgesNum, removedIdx, swappedIdx, 0);

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
		addEdgeToLists(e);
		return e;
	}

	private void addEdgeToLists(int e) {
		int source = source(e), target = target(e);
		int next = edgesHead[source];
		if (next >= 0) {
			setNext(e, source, next);
			setPrev(next, source, e);
		}
		edgesHead[source] = e;
		edgesNum[source]++;
		if (source != target) {
			next = edgesHead[target];
			if (next >= 0) {
				setNext(e, target, next);
				setPrev(next, target, e);
			}
			edgesHead[target] = e;
			edgesNum[target]++;
		}
	}

	@Override
	void removeEdgeLast(int edge) {
		int u = source(edge), v = target(edge);
		removeEdgePointers(edge, u);
		if (u != v)
			removeEdgePointers(edge, v);
		super.removeEdgeLast(edge);
	}

	@Override
	void edgeSwapAndRemove(int removedIdx, int swappedIdx) {
		int u = source(removedIdx), v = target(removedIdx);
		removeEdgePointers(removedIdx, u);
		if (u != v)
			removeEdgePointers(removedIdx, v);
		edgeSwapAndRemove2(removedIdx, swappedIdx);
	}

	private void edgeSwapAndRemove2(int removedIdx, int swappedIdx) {
		int u = source(swappedIdx), v = target(swappedIdx);
		int prev, next;
		if ((prev = prevu[swappedIdx]) >= 0) {
			setNext(prev, u, removedIdx);
		} else {
			edgesHead[u] = removedIdx;
		}
		if ((next = nextu[swappedIdx]) >= 0)
			setPrev(next, u, removedIdx);

		if (u != v) {
			if ((prev = prevv[swappedIdx]) >= 0) {
				setNext(prev, v, removedIdx);
			} else {
				edgesHead[v] = removedIdx;
			}
			if ((next = nextv[swappedIdx]) >= 0)
				setPrev(next, v, removedIdx);
		}

		swapAndClear(prevu, removedIdx, swappedIdx, -1);
		swapAndClear(nextu, removedIdx, swappedIdx, -1);
		swapAndClear(prevv, removedIdx, swappedIdx, -1);
		swapAndClear(nextv, removedIdx, swappedIdx, -1);

		super.edgeSwapAndRemove(removedIdx, swappedIdx);
	}

	private void removeEdgePointers(int e, int w) {
		int next = next(e, w), prev = prev(e, w);
		if (prev == -1) {
			edgesHead[w] = next;
		} else {
			setNext(prev, w, next);
			setPrev(e, w, -1);
		}
		if (next >= 0) {
			setPrev(next, w, prev);
			setNext(e, w, -1);
		}
		edgesNum[w]--;
	}

	@Override
	public void removeEdgesOf(int source) {
		checkVertex(source);
		for (int p = edgesHead[source], next; p >= 0; p = next) {
			// update u list
			next = next(p, source);
			setNext(p, source, -1);
			setPrev(p, source, -1);

			// update v list
			if (source(p) != target(p)) {
				int target = edgeEndpoint(p, source);
				removeEdgePointers(p, target);
			}

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
		edgesHead[source] = -1;
		edgesNum[source] = 0;
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
		removeEdgePointers(edge, oldSource);
		if (oldSource != oldTarget)
			removeEdgePointers(edge, oldTarget);
		setEndpoints(edge, newSource, newTarget);
		addEdgeToLists(edge);
	}

	@Override
	public void clearEdges() {
		edgesHeadContainer.clear();
		edgesNumContainer.clear();

		nextuContainer.clear();
		prevuContainer.clear();
		nextvContainer.clear();
		prevvContainer.clear();

		super.clearEdges();
	}

	private int next(int e, int w) {
		int source = source(e), target = target(e);
		if (w == source) {
			return nextu[e];
		} else {
			assert w == target;
			return nextv[e];
		}
	}

	private int setNext(int e, int w, int n) {
		int source = source(e), target = target(e);
		if (w == source) {
			nextu[e] = n;
		} else {
			assert w == target;
			nextv[e] = n;
		}
		return n;
	}

	private int prev(int e, int w) {
		int source = source(e), target = target(e);
		if (w == source) {
			return prevu[e];
		} else {
			assert w == target;
			return prevv[e];
		}
	}

	private int setPrev(int e, int w, int n) {
		int source = source(e), target = target(e);
		if (w == source) {
			prevu[e] = n;
		} else {
			assert w == target;
			prevv[e] = n;
		}
		return n;
	}

	private class EdgeSetOut extends IndexGraphBase.EdgeSetOutUndirected {
		EdgeSetOut(int source) {
			super(source);
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIterOut(source, edgesHead[source]);
		}

		@Override
		public int size() {
			return edgesNum[source];
		}
	}

	private class EdgeSetIn extends IndexGraphBase.EdgeSetInUndirected {
		EdgeSetIn(int target) {
			super(target);
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIterIn(target, edgesHead[target]);
		}

		@Override
		public int size() {
			return edgesNum[target];
		}
	}

	private class EdgeIterOut extends GraphLinkedAbstract.EdgeItr {

		private final int source;

		EdgeIterOut(int source, int p) {
			super(p);
			this.source = source;
		}

		@Override
		int nextEdge(int n) {
			return GraphLinkedUndirected.this.next(n, source);
		}

		@Override
		public int sourceInt() {
			return source;
		}

		@Override
		public int targetInt() {
			int u0 = GraphLinkedUndirected.this.source(last), v0 = GraphLinkedUndirected.this.target(last);
			return source == u0 ? v0 : u0;
		}
	}

	private class EdgeIterIn extends GraphLinkedAbstract.EdgeItr {

		private final int target;

		EdgeIterIn(int target, int p) {
			super(p);
			this.target = target;
		}

		@Override
		int nextEdge(int n) {
			return GraphLinkedUndirected.this.next(n, target);
		}

		@Override
		public int sourceInt() {
			int u0 = GraphLinkedUndirected.this.source(last), v0 = GraphLinkedUndirected.this.target(last);
			return target == u0 ? v0 : u0;
		}

		@Override
		public int targetInt() {
			return target;
		}
	}
}
