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

class GraphHashmapMultiUndirected extends GraphHashmapMultiAbstract {

	private Int2ObjectMap<int[]>[] edges;
	private int[] edgesNum;
	private final DataContainer.Obj<Int2ObjectMap<int[]>> edgesContainer;
	private final DataContainer.Int edgesNumContainer;

	private static final GraphBaseMutable.Capabilities CapabilitiesNoSelfEdges =
			GraphBaseMutable.Capabilities.of(false, false, true);
	private static final GraphBaseMutable.Capabilities CapabilitiesWithSelfEdges =
			GraphBaseMutable.Capabilities.of(false, true, true);

	private static GraphBaseMutable.Capabilities capabilities(boolean selfEdges) {
		return selfEdges ? CapabilitiesWithSelfEdges : CapabilitiesNoSelfEdges;
	}

	GraphHashmapMultiUndirected(boolean selfEdges, int expectedVerticesNum, int expectedEdgesNum) {
		super(capabilities(selfEdges), expectedVerticesNum, expectedEdgesNum);
		edgesContainer = newVerticesContainer(EmptyEdgeMap, EMPTY_MAP_ARRAY, newArr -> edges = newArr);
		edgesNumContainer = newVerticesIntContainer(0, newArr -> edgesNum = newArr);
	}

	GraphHashmapMultiUndirected(boolean selfEdges, IndexGraph g, boolean copyVerticesWeights,
			boolean copyEdgesWeights) {
		super(capabilities(selfEdges), g, copyVerticesWeights, copyEdgesWeights);
		final int n = g.vertices().size();

		if (g instanceof GraphHashmapMultiUndirected) {
			GraphHashmapMultiUndirected g0 = (GraphHashmapMultiUndirected) g;
			edgesContainer = copyVerticesContainer(g0.edgesContainer, EMPTY_MAP_ARRAY, newArr -> edges = newArr);
			edgesNumContainer = copyVerticesContainer(g0.edgesNumContainer, newArr -> edgesNum = newArr);

			for (int u = 0; u < n; u++) {
				if (!g0.edges[u].isEmpty()) {
					edges[u] = new Int2ObjectOpenHashMap<>(g0.edges[u].size());
					edges[u].defaultReturnValue(EmptyEdgeArr);
				}
			}
			for (int u = 0; u < n; u++) {
				if (!g0.edges[u].isEmpty()) {
					for (var entry : Int2ObjectMaps.fastIterable(g0.edges[u])) {
						int v = entry.getIntKey();
						if (u > v)
							continue;
						int[] edgesArr = entry.getValue();
						int edgesNum = edgesArr[0];
						assert edgesNum > 0;
						edgesArr = Arrays.copyOf(edgesArr, 1 + edgesNum);
						edges[u].put(v, edgesArr);
						edges[v].put(u, edgesArr);
					}
				}
			}
		} else {
			edgesContainer = newVerticesContainer(EmptyEdgeMap, EMPTY_MAP_ARRAY, newArr -> edges = newArr);
			edgesNumContainer = newVerticesIntContainer(0, newArr -> edgesNum = newArr);

			for (int m = g.edges().size(), e = 0; e < m; e++) {
				int source = g.edgeSource(e), target = g.edgeTarget(e);
				addEdgeToMaps(e, source, target);
			}
		}
	}

	GraphHashmapMultiUndirected(boolean selfEdges, IndexGraphBuilderImpl builder) {
		super(capabilities(selfEdges), builder);
		assert !builder.isDirected();

		edgesContainer = newVerticesContainer(EmptyEdgeMap, EMPTY_MAP_ARRAY, newArr -> edges = newArr);
		edgesNumContainer = newVerticesIntContainer(0, newArr -> edgesNum = newArr);

		for (int m = builder.edges().size(), e = 0; e < m; e++) {
			int source = builder.edgeSource(e), target = builder.edgeTarget(e);
			addEdgeToMaps(e, source, target);
		}
	}

	@Override
	void removeVertexLast(int vertex) {
		assert edges[vertex].isEmpty();
		// Reuse allocated edges array for v
		// edges.clear(v);
		super.removeVertexLast(vertex);
	}

	@Override
	void vertexSwapAndRemove(int removedIdx, int swappedIdx) {
		assert edges[removedIdx].isEmpty();

		/* we handle the self edges of the swapped vertex separately */
		int[] selfEdges = EmptyEdgeArr;
		if (isAllowSelfEdges() && edges[swappedIdx] != EmptyEdgeMap)
			selfEdges = edges[swappedIdx].remove(swappedIdx);

		for (var entry : Int2ObjectMaps.fastIterable(edges[swappedIdx])) {
			int target = entry.getIntKey();
			int[] edgesArr = entry.getValue();
			for (int edgesNum = edgesArr[0], i = 1; i <= edgesNum; i++)
				replaceEdgeEndpoint(edgesArr[i], swappedIdx, removedIdx);
			int[] oldVal1 = edges[target].remove(swappedIdx);
			int[] oldVal2 = edges[target].put(removedIdx, edgesArr);
			assert oldVal1 == edgesArr;
			assert oldVal2 == EmptyEdgeArr;
		}

		if (selfEdges != EmptyEdgeArr) {
			for (int edgesNum = selfEdges[0], i = 1; i <= edgesNum; i++)
				setEndpoints(selfEdges[i], removedIdx, removedIdx);
			int[] oldVal = edges[swappedIdx].put(removedIdx, selfEdges);
			assert oldVal == EmptyEdgeArr;
		}

		swapAndClear(edges, removedIdx, swappedIdx, EmptyEdgeMap);
		swapAndClear(edgesNum, removedIdx, swappedIdx, 0);
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
		int[] edgesArr = edges[source].get(target);
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
		Int2ObjectMap<int[]> outMap = ensureEdgesMapMutable(edges, source);
		int edgesMapSizeBefore = outMap.size();
		int[] edgesArr = outMap.computeIfAbsent(target, v0 -> new int[2]);
		boolean isNewEdgesArr = edgesMapSizeBefore != outMap.size();
		int edgesNum = ++edgesArr[0];

		if (isNewEdgesArr) {
			ensureEdgesMapMutable(edges, target).put(source, edgesArr);

		} else if (edgesNum == edgesArr.length) {
			edgesArr = Arrays.copyOf(edgesArr, 1 + Math.max(2, edgesNum * 2));

			outMap.put(target, edgesArr);
			edges[target].put(source, edgesArr);
		}
		edgesArr[edgesNum] = edge;

		this.edgesNum[source]++;
		if (source != target)
			this.edgesNum[target]++;
	}

	@Override
	void removeEdgeLast(int edge) {
		removeEdgeFromMaps(edge);
		super.removeEdgeLast(edge);
	}

	private void removeEdgeFromMaps(int edge) {
		int source = source(edge), target = target(edge);

		Int2ObjectMap<int[]> outMap = edges[source];
		int[] edgesArr = outMap.get(target);
		int edgesNum = edgesArr[0];
		if (edgesNum == 1) {
			assert edge == edgesArr[1];
			edgesArr[0] = 0;
			outMap.remove(target);
			edges[target].remove(source);

		} else {
			int edgeIdx = edgeIndexInArr(edgesArr, edge);
			edgesArr[edgeIdx] = edgesArr[edgesNum];
			edgesArr[0] = --edgesNum;
			if (edgesNum <= (edgesArr.length - 1) / 4) {
				edgesArr = Arrays.copyOf(edgesArr, 1 + Math.max(2, edgesNum * 2));
				outMap.put(target, edgesArr);
				edges[target].put(source, edgesArr);
			}
		}

		this.edgesNum[source]--;
		if (source != target)
			this.edgesNum[target]--;
	}

	@Override
	void edgeSwapAndRemove(int removedIdx, int swappedIdx) {
		removeEdgeFromMaps(removedIdx);

		int us = source(swappedIdx), vs = target(swappedIdx);
		int[] edgesArr = edges[us].get(vs);
		edgesArr[edgeIndexInArr(edgesArr, swappedIdx)] = removedIdx;

		super.edgeSwapAndRemove(removedIdx, swappedIdx);
	}

	@Override
	public void removeEdgesOf(int source) {
		checkVertex(source);
		while (!edges[source].isEmpty())
			removeEdge(edges[source].values().iterator().next()[1]);
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
		removeEdgeFromMaps(edge);
		addEdgeToMaps(edge, newSource, newTarget);
		setEndpoints(edge, newSource, newTarget);
	}

	@Override
	public void clearEdges() {
		final int n = vertices().size();
		for (int v = 0; v < n; v++)
			if (edges[v] != EmptyEdgeMap)
				edges[v].clear();
		super.clearEdges();
	}

	@Override
	public void clear() {
		super.clear();
		// Don't clear allocated edges arrays
		// edges.clear();
	}

	class EdgeSetOut extends IndexGraphBase.EdgeSetOutUndirected {
		private final Int2ObjectMap<int[]> edges;

		EdgeSetOut(int source) {
			super(source);
			this.edges = GraphHashmapMultiUndirected.this.edges[source];
		}

		@Override
		public int size() {
			return edgesNum[source];
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIterOut(source, edges);
		}
	}

	class EdgeSetIn extends IndexGraphBase.EdgeSetInUndirected {
		private final Int2ObjectMap<int[]> edges;

		EdgeSetIn(int target) {
			super(target);
			this.edges = GraphHashmapMultiUndirected.this.edges[target];
		}

		@Override
		public int size() {
			return edgesNum[target];
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
			return edges[source];
		}

		@Override
		public boolean contains(int edge) {
			if (!(0 <= edge && edge < edges().size()))
				return false;
			int u = source(edge), v = target(edge);
			return (source == u && target == v) || (source == v && target == u);
		}
	}

}
