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

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

class GraphHashmapMultiDirected extends GraphHashmapMultiAbstract implements GraphDefaultsDirected {

	private Int2IntMap[] edgesOut;
	private Int2IntMap[] edgesIn;
	private int[] edgesOutNum;
	private int[] edgesInNum;
	private final DataContainer.Obj<Int2IntMap> edgesOutContainer;
	private final DataContainer.Obj<Int2IntMap> edgesInContainer;
	private final DataContainer.Int edgesOutNumContainer;
	private final DataContainer.Int edgesInNumContainer;

	private static final GraphBaseMutable.Capabilities CapabilitiesNoSelfEdges =
			GraphBaseMutable.Capabilities.of(true, false, true);
	private static final GraphBaseMutable.Capabilities CapabilitiesWithSelfEdges =
			GraphBaseMutable.Capabilities.of(true, true, true);

	private static GraphBaseMutable.Capabilities capabilities(boolean selfEdges) {
		return selfEdges ? CapabilitiesWithSelfEdges : CapabilitiesNoSelfEdges;
	}

	GraphHashmapMultiDirected(boolean selfEdges, int expectedVerticesNum, int expectedEdgesNum) {
		super(capabilities(selfEdges), expectedVerticesNum, expectedEdgesNum);
		edgesOutContainer = newVerticesContainer(EmptyEdgeMap, EMPTY_MAP_ARRAY, newArr -> edgesOut = newArr);
		edgesInContainer = newVerticesContainer(EmptyEdgeMap, EMPTY_MAP_ARRAY, newArr -> edgesIn = newArr);
		edgesOutNumContainer = newVerticesIntContainer(0, newArr -> edgesOutNum = newArr);
		edgesInNumContainer = newVerticesIntContainer(0, newArr -> edgesInNum = newArr);
	}

	GraphHashmapMultiDirected(boolean selfEdges, IndexGraph g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		super(capabilities(selfEdges), g, copyVerticesWeights, copyEdgesWeights);
		final int n = g.vertices().size();

		if (g instanceof GraphHashmapMultiDirected) {
			GraphHashmapMultiDirected g0 = (GraphHashmapMultiDirected) g;

			edgesOutContainer =
					copyVerticesContainer(g0.edgesOutContainer, EMPTY_MAP_ARRAY, newArr -> edgesOut = newArr);
			edgesInContainer = copyVerticesContainer(g0.edgesInContainer, EMPTY_MAP_ARRAY, newArr -> edgesIn = newArr);
			edgesOutNumContainer = copyVerticesContainer(g0.edgesOutNumContainer, newArr -> edgesOutNum = newArr);
			edgesInNumContainer = copyVerticesContainer(g0.edgesInNumContainer, newArr -> edgesInNum = newArr);

			for (int v = 0; v < n; v++) {
				if (!g0.edgesOut[v].isEmpty()) {
					/* Int2IntOpenHashMap refuse to shrink below the initial size, so we use expected=0 here */
					edgesOut[v] = new Int2IntOpenHashMap(0);
					edgesOut[v].defaultReturnValue(-1);
					edgesOut[v].putAll(g0.edgesOut[v]);
				}
				if (!g0.edgesIn[v].isEmpty()) {
					/* Int2IntOpenHashMap refuse to shrink below the initial size, so we use expected=0 here */
					edgesIn[v] = new Int2IntOpenHashMap(0);
					edgesIn[v].defaultReturnValue(-1);
					edgesIn[v].putAll(g0.edgesIn[v]);
				}
			}
		} else {
			edgesOutContainer = newVerticesContainer(EmptyEdgeMap, EMPTY_MAP_ARRAY, newArr -> edgesOut = newArr);
			edgesInContainer = newVerticesContainer(EmptyEdgeMap, EMPTY_MAP_ARRAY, newArr -> edgesIn = newArr);
			edgesOutNumContainer = newVerticesIntContainer(0, newArr -> edgesOutNum = newArr);
			edgesInNumContainer = newVerticesIntContainer(0, newArr -> edgesInNum = newArr);

			for (int m = g.edges().size(), e = 0; e < m; e++) {
				int source = g.edgeSource(e), target = g.edgeTarget(e);
				addEdgeToMaps(e, source, target);
			}
		}
	}

	GraphHashmapMultiDirected(boolean selfEdges, IndexGraphBuilderImpl builder) {
		super(capabilities(selfEdges), builder);
		assert builder.isDirected();

		edgesOutContainer = newVerticesContainer(EmptyEdgeMap, EMPTY_MAP_ARRAY, newArr -> edgesOut = newArr);
		edgesInContainer = newVerticesContainer(EmptyEdgeMap, EMPTY_MAP_ARRAY, newArr -> edgesIn = newArr);
		edgesOutNumContainer = newVerticesIntContainer(0, newArr -> edgesOutNum = newArr);
		edgesInNumContainer = newVerticesIntContainer(0, newArr -> edgesInNum = newArr);

		for (int m = builder.edges.size(), e = 0; e < m; e++) {
			int source = builder.edgeSource(e), target = builder.edgeTarget(e);
			addEdgeToMaps(e, source, target);
		}
	}

	@Override
	void removeVertexLast(int vertex) {
		assert edgesOut[vertex].isEmpty() && edgesIn[vertex].isEmpty();
		// Reuse allocated edges arrays for v
		// edgesOutContainer.clear(v);
		// edgesInContainer.clear(v);
		super.removeVertexLast(vertex);
	}

	@Override
	void vertexSwapAndRemove(int removedIdx, int swappedIdx) {
		assert edgesOut[removedIdx].isEmpty() && edgesIn[removedIdx].isEmpty();

		/* we handle the self edges of the swapped vertex separately */
		int firstSelfEdge = -1;
		if (isAllowSelfEdges() && edgesOut[swappedIdx] != EmptyEdgeMap) {
			firstSelfEdge = edgesOut[swappedIdx].remove(swappedIdx);
			if (firstSelfEdge >= 0) {
				int oldVal = edgesIn[swappedIdx].remove(swappedIdx);
				assert oldVal == firstSelfEdge;
			}
		}

		for (var entry : Int2IntMaps.fastIterable(edgesOut[swappedIdx])) {
			int target = entry.getIntKey();
			int firstEdge = entry.getIntValue();
			for (int e = firstEdge; e >= 0; e = edgeNext[e])
				replaceEdgeSource(e, removedIdx);
			int oldVal1 = edgesIn[target].remove(swappedIdx);
			int oldVal2 = edgesIn[target].put(removedIdx, firstEdge);
			assert oldVal1 == firstEdge;
			assert oldVal2 == -1;
		}
		for (var entry : Int2IntMaps.fastIterable(edgesIn[swappedIdx])) {
			int source = entry.getIntKey();
			int firstEdge = entry.getIntValue();
			for (int e = firstEdge; e >= 0; e = edgeNext[e])
				replaceEdgeTarget(e, removedIdx);
			int oldVal1 = edgesOut[source].remove(swappedIdx);
			int oldVal2 = edgesOut[source].put(removedIdx, firstEdge);
			assert oldVal1 == firstEdge;
			assert oldVal2 == -1;
		}

		if (firstSelfEdge >= 0) {
			for (int e = firstSelfEdge; e >= 0; e = edgeNext[e])
				setEndpoints(e, removedIdx, removedIdx);
			int oldVal1 = edgesOut[swappedIdx].put(removedIdx, firstSelfEdge);
			int oldVal2 = edgesIn[swappedIdx].put(removedIdx, firstSelfEdge);
			assert oldVal1 == -1;
			assert oldVal2 == -1;
		}

		swapAndClear(edgesOut, removedIdx, swappedIdx, EmptyEdgeMap);
		swapAndClear(edgesOutNum, removedIdx, swappedIdx, 0);
		swapAndClear(edgesIn, removedIdx, swappedIdx, EmptyEdgeMap);
		swapAndClear(edgesInNum, removedIdx, swappedIdx, 0);
		super.vertexSwapAndRemove(removedIdx, swappedIdx);
	}

	@Override
	public IEdgeSet getEdges(int source, int target) {
		checkVertex(source);
		checkVertex(target);
		return new SourceTargetEdgeSet(source, target);
	}

	@Override
	public int getEdge(int source, int target) {
		checkVertex(source);
		checkVertex(target);
		return edgesOut[source].get(target);
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
		int edge = super.addEdge(source, target);
		addEdgeToMaps(edge, source, target);
		return edge;
	}

	private void addEdgeToMaps(int edge, int source, int target) {
		Int2IntMap outMap = ensureEdgesMapMutable(edgesOut, source);
		int firstEdge = outMap.putIfAbsent(target, edge);
		if (firstEdge >= 0) {
			int secondEdge = edgeNext[firstEdge];
			edgeNext[firstEdge] = edge;
			edgePrev[edge] = firstEdge;
			if (secondEdge >= 0) {
				edgeNext[edge] = secondEdge;
				edgePrev[secondEdge] = edge;
			}
		} else {
			ensureEdgesMapMutable(edgesIn, target).put(source, edge);
		}
		edgesOutNum[source]++;
		edgesInNum[target]++;
	}

	@Override
	void removeEdgeLast(int edge) {
		removeEdgeFromMaps(edge);
		edgeNext[edge] = edgePrev[edge] = -1;
		super.removeEdgeLast(edge);
	}

	private void removeEdgeFromMaps(int edge) {
		int source = source(edge), target = target(edge);
		int prevEdge = edgePrev[edge], nextEdge = edgeNext[edge];

		if (prevEdge >= 0) {
			edgeNext[prevEdge] = nextEdge;
			if (nextEdge >= 0)
				edgePrev[nextEdge] = prevEdge;

		} else if (nextEdge >= 0) {
			edgePrev[nextEdge] = -1;
			edgesOut[source].put(target, nextEdge);
			edgesIn[target].put(source, nextEdge);

		} else {
			edgesOut[source].remove(target);
			edgesIn[target].remove(source);
		}

		edgesOutNum[source]--;
		edgesInNum[target]--;
	}

	@Override
	void edgeSwapAndRemove(int removedIdx, int swappedIdx) {
		removeEdgeFromMaps(removedIdx);

		int us = source(swappedIdx), vs = target(swappedIdx);
		int prevEdge = edgePrev[swappedIdx], nextEdge = edgeNext[swappedIdx];

		edgePrev[removedIdx] = prevEdge;
		if (prevEdge >= 0) {
			edgeNext[prevEdge] = removedIdx;
		} else {
			edgesOut[us].put(vs, removedIdx);
			edgesIn[vs].put(us, removedIdx);
		}
		edgeNext[removedIdx] = nextEdge;
		if (nextEdge >= 0)
			edgePrev[nextEdge] = removedIdx;
		edgeNext[swappedIdx] = edgePrev[swappedIdx] = -1;

		super.edgeSwapAndRemove(removedIdx, swappedIdx);
	}

	@Override
	public void removeOutEdgesOf(int source) {
		checkVertex(source);
		while (!edgesOut[source].isEmpty())
			removeAllEdgesInList(edgesOut[source].values().iterator().nextInt());
	}

	@Override
	public void removeInEdgesOf(int target) {
		checkVertex(target);
		while (!edgesIn[target].isEmpty())
			removeAllEdgesInList(edgesIn[target].values().iterator().nextInt());
	}

	@Override
	public void moveEdge(int edge, int newSource, int newTarget) {
		checkEdge(edge);
		checkNewEdgeEndpoints(newSource, newTarget);
		removeEdgeFromMaps(edge);
		edgeNext[edge] = edgePrev[edge] = -1;
		addEdgeToMaps(edge, newSource, newTarget);
		setEndpoints(edge, newSource, newTarget);
	}

	@Override
	public void clearEdges() {
		final int n = vertices().size();
		for (int v = 0; v < n; v++) {
			if (edgesOut[v] != EmptyEdgeMap)
				edgesOut[v].clear();
			if (edgesIn[v] != EmptyEdgeMap)
				edgesIn[v].clear();
		}
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

	class EdgeSetOut extends IndexGraphBase.EdgeSetOutDirected {
		private final Int2IntMap edges;

		EdgeSetOut(int source) {
			super(source);
			this.edges = edgesOut[source];
		}

		@Override
		public int size() {
			return edgesOutNum[source];
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIterOut(source, edges);
		}
	}

	class EdgeSetIn extends IndexGraphBase.EdgeSetInDirected {
		private final Int2IntMap edges;

		EdgeSetIn(int target) {
			super(target);
			this.edges = edgesIn[target];
		}

		@Override
		public int size() {
			return edgesInNum[target];
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIterIn(target, edges);
		}
	}

	class SourceTargetEdgeSet extends GraphHashmapMultiAbstract.SourceTargetEdgeSet {

		SourceTargetEdgeSet(int source, int target) {
			super(source, target);
		}

		@Override
		Int2IntMap edgesOut(int source) {
			return edgesOut[source];
		}

		@Override
		public boolean contains(int edge) {
			return 0 <= edge && edge < edges().size() && source == source(edge) && target == target(edge);
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
