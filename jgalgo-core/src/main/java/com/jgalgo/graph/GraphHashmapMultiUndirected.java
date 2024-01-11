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
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

class GraphHashmapMultiUndirected extends GraphHashmapMultiAbstract implements GraphDefaultsUndirected {

	private Int2IntMap[] edgesMap;
	private int[] edgesNum;
	private final DataContainer.Obj<Int2IntMap> edgesContainer;
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
		edgesContainer = newVerticesContainer(EmptyEdgeMap, EMPTY_MAP_ARRAY, newArr -> edgesMap = newArr);
		edgesNumContainer = newVerticesIntContainer(0, newArr -> edgesNum = newArr);
	}

	GraphHashmapMultiUndirected(boolean selfEdges, IndexGraph g, boolean copyVerticesWeights,
			boolean copyEdgesWeights) {
		super(capabilities(selfEdges), g, copyVerticesWeights, copyEdgesWeights);
		final int n = g.vertices().size();

		if (g instanceof GraphHashmapMultiUndirected) {
			GraphHashmapMultiUndirected g0 = (GraphHashmapMultiUndirected) g;
			edgesContainer = copyVerticesContainer(g0.edgesContainer, EMPTY_MAP_ARRAY, newArr -> edgesMap = newArr);
			edgesNumContainer = copyVerticesContainer(g0.edgesNumContainer, newArr -> edgesNum = newArr);

			for (int v : range(n)) {
				if (!g0.edgesMap[v].isEmpty()) {
					/* Int2IntOpenHashMap refuse to shrink below the initial size, so we use expected=0 here */
					edgesMap[v] = new Int2IntOpenHashMap(0);
					edgesMap[v].defaultReturnValue(-1);
					edgesMap[v].putAll(g0.edgesMap[v]);
				}
			}
		} else {
			edgesContainer = newVerticesContainer(EmptyEdgeMap, EMPTY_MAP_ARRAY, newArr -> edgesMap = newArr);
			edgesNumContainer = newVerticesIntContainer(0, newArr -> edgesNum = newArr);

			for (int e : range(g.edges().size())) {
				int source = g.edgeSource(e), target = g.edgeTarget(e);
				addEdgeToMaps(e, source, target);
			}
		}
	}

	GraphHashmapMultiUndirected(boolean selfEdges, IndexGraphBuilderImpl builder) {
		super(capabilities(selfEdges), builder);
		assert !builder.isDirected();

		edgesContainer = newVerticesContainer(EmptyEdgeMap, EMPTY_MAP_ARRAY, newArr -> edgesMap = newArr);
		edgesNumContainer = newVerticesIntContainer(0, newArr -> edgesNum = newArr);

		for (int e : range(builder.edges.size())) {
			int source = builder.edgeSource(e), target = builder.edgeTarget(e);
			addEdgeToMaps(e, source, target);
		}
	}

	@Override
	void removeVertexLast(int vertex) {
		assert edgesMap[vertex].isEmpty();
		// Reuse allocated edges array for v
		// edges.clear(v);
		super.removeVertexLast(vertex);
	}

	@Override
	void vertexSwapAndRemove(int removedIdx, int swappedIdx) {
		assert edgesMap[removedIdx].isEmpty();

		/* we handle the self edges of the swapped vertex separately */
		int firstSelfEdge = -1;
		if (isAllowSelfEdges() && edgesMap[swappedIdx] != EmptyEdgeMap)
			firstSelfEdge = edgesMap[swappedIdx].remove(swappedIdx);

		for (var entry : Int2IntMaps.fastIterable(edgesMap[swappedIdx])) {
			int target = entry.getIntKey();
			int firstEdge = entry.getIntValue();
			for (int e = firstEdge; e >= 0; e = edgeNext[e])
				replaceEdgeEndpoint(e, swappedIdx, removedIdx);
			int oldVal1 = edgesMap[target].remove(swappedIdx);
			int oldVal2 = edgesMap[target].put(removedIdx, firstEdge);
			assert oldVal1 == firstEdge;
			assert oldVal2 == -1;
		}

		if (firstSelfEdge >= 0) {
			for (int e = firstSelfEdge; e >= 0; e = edgeNext[e])
				setEndpoints(e, removedIdx, removedIdx);
			int oldVal = edgesMap[swappedIdx].put(removedIdx, firstSelfEdge);
			assert oldVal == -1;
		}

		swapAndClear(edgesMap, removedIdx, swappedIdx, EmptyEdgeMap);
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
		return edgesMap[source].get(target);
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
		Int2IntMap outMap = ensureEdgesMapMutable(edgesMap, source);
		int firstEdge = outMap.putIfAbsent(target, edge);
		if (firstEdge >= 0) {
			int secondEdge = edgeNext[firstEdge];
			edgeNext[firstEdge] = edge;
			edgePrev[edge] = firstEdge;
			if (secondEdge >= 0) {
				edgePrev[secondEdge] = edge;
				edgeNext[edge] = secondEdge;
			}
		} else {
			ensureEdgesMapMutable(edgesMap, target).put(source, edge);
		}

		this.edgesNum[source]++;
		if (source != target)
			this.edgesNum[target]++;
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
			edgesMap[source].put(target, nextEdge);
			edgesMap[target].put(source, nextEdge);

		} else {
			edgesMap[source].remove(target);
			edgesMap[target].remove(source);
		}

		this.edgesNum[source]--;
		if (source != target)
			this.edgesNum[target]--;
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
			edgesMap[us].put(vs, removedIdx);
			edgesMap[vs].put(us, removedIdx);
		}
		edgeNext[removedIdx] = nextEdge;
		if (nextEdge >= 0)
			edgePrev[nextEdge] = removedIdx;
		edgeNext[swappedIdx] = edgePrev[swappedIdx] = -1;

		super.edgeSwapAndRemove(removedIdx, swappedIdx);
	}

	@Override
	public void removeEdgesOf(int source) {
		checkVertex(source);
		while (!edgesMap[source].isEmpty())
			removeAllEdgesInList(edgesMap[source].values().iterator().nextInt());
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
		for (int v : range(n))
			if (edgesMap[v] != EmptyEdgeMap)
				edgesMap[v].clear();
		edgesNumContainer.clear();
		super.clearEdges();
	}

	@Override
	public void clear() {
		super.clear();
		// Don't clear allocated edges arrays
		// edges.clear();
	}

	class EdgeSetOut extends IndexGraphBase.EdgeSetOutUndirected {
		private final Int2IntMap edges;

		EdgeSetOut(int source) {
			super(source);
			this.edges = GraphHashmapMultiUndirected.this.edgesMap[source];
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
		private final Int2IntMap edges;

		EdgeSetIn(int target) {
			super(target);
			this.edges = GraphHashmapMultiUndirected.this.edgesMap[target];
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
		Int2IntMap edgesMap(int source) {
			return edgesMap[source];
		}

		@Override
		public boolean contains(int edge) {
			if (!(0 <= edge && edge < edges().size()))
				return false;
			int u = source(edge), v = target(edge);
			return (source == u && target == v) || (source == v && target == u);
		}
	}

	@Override
	void markVertex(int vertex) {
		edgesNum[vertex] = -edgesNum[vertex] - 1;
	}

	@Override
	void unmarkVertex(int vertex) {
		edgesNum[vertex] = -edgesNum[vertex] - 1;
	}

	@Override
	boolean isMarkedVertex(int vertex) {
		return edgesNum[vertex] < 0;
	}
}
