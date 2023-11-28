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
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

class GraphHashmapMultiDirected extends GraphHashmapMultiAbstract {

	private Int2ObjectMap<int[]>[] edgesOut;
	private Int2ObjectMap<int[]>[] edgesIn;
	private int[] edgesOutNum;
	private int[] edgesInNum;
	private final DataContainer.Obj<Int2ObjectMap<int[]>> edgesOutContainer;
	private final DataContainer.Obj<Int2ObjectMap<int[]>> edgesInContainer;
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
				if (!g0.edgesIn[v].isEmpty()) {
					edgesIn[v] = new Int2ObjectOpenHashMap<>(g0.edgesIn[v].size());
					edgesIn[v].defaultReturnValue(EmptyEdgeArr);
				}
			}
			for (int u = 0; u < n; u++) {
				if (!g0.edgesOut[u].isEmpty()) {
					edgesOut[u] = new Int2ObjectOpenHashMap<>(g0.edgesOut[u].size());
					edgesOut[u].defaultReturnValue(EmptyEdgeArr);
					for (var entry : Int2ObjectMaps.fastIterable(g0.edgesOut[u])) {
						int v = entry.getIntKey();
						int[] edgesArr = entry.getValue();
						int edgesNum = edgesArr[0];
						assert edgesNum > 0;
						edgesArr = Arrays.copyOf(edgesArr, 1 + edgesNum);

						assert edgesIn[v] != EmptyEdgeMap;
						edgesOut[u].put(v, edgesArr);
						edgesIn[v].put(u, edgesArr);
					}
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

		for (int m = builder.edges().size(), e = 0; e < m; e++) {
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
		int[] selfEdges = EmptyEdgeArr;
		if (isAllowSelfEdges() && edgesOut[swappedIdx] != EmptyEdgeMap) {
			selfEdges = edgesOut[swappedIdx].remove(swappedIdx);
			if (selfEdges != EmptyEdgeArr) {
				int[] oldVal = edgesIn[swappedIdx].remove(swappedIdx);
				assert oldVal == selfEdges;
			}
		}

		for (var entry : Int2ObjectMaps.fastIterable(edgesOut[swappedIdx])) {
			int target = entry.getIntKey();
			int[] edgesArr = entry.getValue();
			for (int edgesNum = edgesArr[0], i = 1; i <= edgesNum; i++)
				replaceEdgeSource(edgesArr[i], removedIdx);
			int[] oldVal1 = edgesIn[target].remove(swappedIdx);
			int[] oldVal2 = edgesIn[target].put(removedIdx, edgesArr);
			assert oldVal1 == edgesArr;
			assert oldVal2 == EmptyEdgeArr;
		}
		for (var entry : Int2ObjectMaps.fastIterable(edgesIn[swappedIdx])) {
			int source = entry.getIntKey();
			int[] edgesArr = entry.getValue();
			for (int edgesNum = edgesArr[0], i = 1; i <= edgesNum; i++)
				replaceEdgeTarget(edgesArr[i], removedIdx);
			int[] oldVal1 = edgesOut[source].remove(swappedIdx);
			int[] oldVal2 = edgesOut[source].put(removedIdx, edgesArr);
			assert oldVal1 == edgesArr;
			assert oldVal2 == EmptyEdgeArr;
		}

		if (selfEdges != EmptyEdgeArr) {
			for (int edgesNum = selfEdges[0], i = 1; i <= edgesNum; i++)
				setEndpoints(selfEdges[i], removedIdx, removedIdx);
			int[] oldVal1 = edgesOut[swappedIdx].put(removedIdx, selfEdges);
			int[] oldVal2 = edgesIn[swappedIdx].put(removedIdx, selfEdges);
			assert oldVal1 == EmptyEdgeArr;
			assert oldVal2 == EmptyEdgeArr;
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
		int[] edgesArr = edgesOut[source].get(target);
		int edgesNum = edgesArr[0];
		return edgesNum == 0 ? -1 : edgesArr[1];
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
		Int2ObjectMap<int[]> outMap = ensureEdgesMapMutable(edgesOut, source);
		int edgesMapSizeBefore = outMap.size();
		int[] edgesArr = outMap.computeIfAbsent(target, v0 -> new int[2]);
		boolean isNewEdgesArr = edgesMapSizeBefore != outMap.size();
		int edgesNum = ++edgesArr[0];

		if (isNewEdgesArr) {
			ensureEdgesMapMutable(edgesIn, target).put(source, edgesArr);

		} else if (edgesNum == edgesArr.length) {
			edgesArr = Arrays.copyOf(edgesArr, 1 + Math.max(2, edgesNum * 2));

			outMap.put(target, edgesArr);
			edgesIn[target].put(source, edgesArr);
		}
		edgesArr[edgesNum] = edge;

		edgesOutNum[source]++;
		edgesInNum[target]++;
	}

	@Override
	void removeEdgeLast(int edge) {
		removeEdgeFromMaps(edge);
		super.removeEdgeLast(edge);
	}

	private void removeEdgeFromMaps(int edge) {
		int source = source(edge), target = target(edge);

		Int2ObjectMap<int[]> outMap = edgesOut[source];
		int[] edgesArr = outMap.get(target);
		int edgesNum = edgesArr[0];
		if (edgesNum == 1) {
			assert edge == edgesArr[1];
			edgesArr[0] = 0;
			outMap.remove(target);
			edgesIn[target].remove(source);

		} else {
			int edgeIdx = edgeIndexInArr(edgesArr, edge);
			edgesArr[edgeIdx] = edgesArr[edgesNum];
			edgesArr[0] = --edgesNum;
			if (edgesNum <= (edgesArr.length - 1) / 4) {
				edgesArr = Arrays.copyOf(edgesArr, 1 + Math.max(2, edgesNum * 2));
				outMap.put(target, edgesArr);
				edgesIn[target].put(source, edgesArr);
			}
		}

		edgesOutNum[source]--;
		edgesInNum[target]--;
	}

	@Override
	void edgeSwapAndRemove(int removedIdx, int swappedIdx) {
		removeEdgeFromMaps(removedIdx);

		int us = source(swappedIdx), vs = target(swappedIdx);
		int[] edgesArr = edgesOut[us].get(vs);
		edgesArr[edgeIndexInArr(edgesArr, swappedIdx)] = removedIdx;

		super.edgeSwapAndRemove(removedIdx, swappedIdx);
	}

	@Override
	public void removeOutEdgesOf(int source) {
		checkVertex(source);
		while (!edgesOut[source].isEmpty())
			removeEdge(edgesOut[source].values().iterator().next()[1]);
	}

	@Override
	public void removeInEdgesOf(int target) {
		checkVertex(target);
		while (!edgesIn[target].isEmpty())
			removeEdge(edgesIn[target].values().iterator().next()[1]);
	}

	@Override
	public void moveEdge(int edge, int newSource, int newTarget) {
		checkEdge(edge);
		checkNewEdgeEndpoints(newSource, newTarget);
		removeEdgeFromMaps(edge);
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
		private final Int2ObjectMap<int[]> edges;

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
		private final Int2ObjectMap<int[]> edges;

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
		Int2ObjectMap<int[]> edgesOut(int source) {
			return edgesOut[source];
		}

		@Override
		public boolean contains(int edge) {
			return 0 <= edge && edge < edges().size() && source == source(edge) && target == target(edge);
		}
	}

}
