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
import it.unimi.dsi.fastutil.ints.IntCollection;

final class GraphHashmapUndirected extends GraphHashmapAbstract implements GraphDefaultsUndirected {

	private Int2IntMap[] edges;
	private final DataContainer.Obj<Int2IntMap> edgesContainer;

	private static final GraphBaseMutable.Capabilities CapabilitiesNoSelfEdges =
			GraphBaseMutable.Capabilities.of(false, false, false);
	private static final GraphBaseMutable.Capabilities CapabilitiesWithSelfEdges =
			GraphBaseMutable.Capabilities.of(false, true, false);

	private static GraphBaseMutable.Capabilities capabilities(boolean selfEdges) {
		return selfEdges ? CapabilitiesWithSelfEdges : CapabilitiesNoSelfEdges;
	}

	GraphHashmapUndirected(boolean selfEdges, int expectedVerticesNum, int expectedEdgesNum) {
		super(capabilities(selfEdges), expectedVerticesNum, expectedEdgesNum);
		edgesContainer = newVerticesContainer(EmptyEdgeMap, EMPTY_MAP_ARRAY, newArr -> edges = newArr);
	}

	GraphHashmapUndirected(boolean selfEdges, IndexGraph g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		super(capabilities(selfEdges), g, copyVerticesWeights, copyEdgesWeights);
		final int n = g.vertices().size();

		if (g instanceof GraphHashmapUndirected) {
			GraphHashmapUndirected g0 = (GraphHashmapUndirected) g;
			edgesContainer = copyVerticesContainer(g0.edgesContainer, EMPTY_MAP_ARRAY, newArr -> edges = newArr);

			for (int v : range(n)) {
				if (!g0.edges[v].isEmpty()) {
					/* Int2IntOpenHashMap refuse to shrink below the initial size, so we use expected=0 here */
					edges[v] = new Int2IntOpenHashMap(0);
					edges[v].defaultReturnValue(-1);
					edges[v].putAll(g0.edges[v]);
				}
			}
		} else {
			edgesContainer = newVerticesContainer(EmptyEdgeMap, EMPTY_MAP_ARRAY, newArr -> edges = newArr);

			for (int e : range(g.edges().size())) {
				int source = g.edgeSource(e), target = g.edgeTarget(e);
				int oldVal1 = ensureEdgesMapMutable(edges, source).put(target, e);
				int oldVal2 = ensureEdgesMapMutable(edges, target).put(source, e);
				if (oldVal1 >= 0)
					throw new IllegalArgumentException("Parallel edge (idx=" + source + ",idx=" + target
							+ ") already exists. Parallel edges are not allowed.");
				assert oldVal2 < 0 || (source == target && oldVal2 == e);
			}
		}
	}

	GraphHashmapUndirected(boolean selfEdges, IndexGraphBuilderImpl builder) {
		super(capabilities(selfEdges), builder);
		assert !builder.isDirected();

		edgesContainer = newVerticesContainer(EmptyEdgeMap, EMPTY_MAP_ARRAY, newArr -> edges = newArr);

		for (int e : range(builder.edges.size())) {
			int source = builder.edgeSource(e), target = builder.edgeTarget(e);
			int oldVal1 = ensureEdgesMapMutable(edges, source).put(target, e);
			int oldVal2 = ensureEdgesMapMutable(edges, target).put(source, e);
			if (oldVal1 >= 0)
				throw new IllegalArgumentException("Parallel edge (idx=" + source + ",idx=" + target
						+ ") already exists. Parallel edges are not allowed.");
			assert oldVal2 < 0 || (source == target && oldVal2 == e);
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

		/* we handle the self edge of the swapped vertex separately */
		int selfEdge = isAllowSelfEdges() ? edges[swappedIdx].remove(swappedIdx) : -1;

		for (Int2IntMap.Entry entry : Int2IntMaps.fastIterable(edges[swappedIdx])) {
			int target = entry.getIntKey();
			int e = entry.getIntValue();
			replaceEdgeEndpoint(e, swappedIdx, removedIdx);
			int oldVal1 = edges[target].remove(swappedIdx);
			int oldVal2 = edges[target].put(removedIdx, e);
			assert oldVal1 == e;
			assert oldVal2 < 0;
		}

		if (selfEdge >= 0) {
			setEndpoints(selfEdge, removedIdx, removedIdx);
			int oldVal = edges[swappedIdx].put(removedIdx, selfEdge);
			assert oldVal < 0;
		}

		swapAndClear(edges, removedIdx, swappedIdx, EmptyEdgeMap);
		super.vertexSwapAndRemove(removedIdx, swappedIdx);
	}

	@Override
	public int getEdge(int source, int target) {
		checkVertex(source);
		checkVertex(target);
		return edges[source].get(target);
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
		ensureEdgesMapMutable(edges, source).put(target, edge);
		ensureEdgesMapMutable(edges, target).put(source, edge);
		return edge;
	}

	@Override
	void removeEdgeLast(int edge) {
		int source = source(edge), target = target(edge);
		int oldVal1 = edges[source].remove(target);
		int oldVal2 = edges[target].remove(source);
		assert edge == oldVal1;
		assert edge == oldVal2 || (source == target && oldVal2 < 0);
		super.removeEdgeLast(edge);
	}

	@Override
	void edgeSwapAndRemove(int removedIdx, int swappedIdx) {
		int ur = source(removedIdx), vr = target(removedIdx);
		assert edges[ur] != EmptyEdgeMap;
		assert edges[vr] != EmptyEdgeMap;
		int oldVal1 = edges[ur].remove(vr);
		int oldVal2 = edges[vr].remove(ur);
		assert oldVal1 == removedIdx;
		assert oldVal2 == removedIdx || (ur == vr && oldVal2 < 0);

		int us = source(swappedIdx), vs = target(swappedIdx);
		assert edges[us] != EmptyEdgeMap;
		assert edges[vs] != EmptyEdgeMap;
		int oldVal3 = edges[us].put(vs, removedIdx);
		int oldVal4 = edges[vs].put(us, removedIdx);
		assert oldVal3 == swappedIdx;
		assert oldVal4 == swappedIdx || (us == vs && oldVal4 == removedIdx);

		super.edgeSwapAndRemove(removedIdx, swappedIdx);
	}

	@Override
	public void removeEdgesOf(int source) {
		checkVertex(source);
		IntCollection sEdges = edges[source].values();
		while (!sEdges.isEmpty())
			removeEdge(sEdges.iterator().nextInt());
	}

	@Override
	public void moveEdge(int edge, int newSource, int newTarget) {
		checkEdge(edge);
		int oldSource = source(edge), oldTarget = target(edge);
		if ((oldSource == newSource && oldTarget == newTarget) || (oldSource == newTarget && oldTarget == newSource))
			return;
		checkNewEdgeEndpoints(newSource, newTarget);

		edges[oldSource].remove(oldTarget);
		edges[oldTarget].remove(oldSource);
		ensureEdgesMapMutable(edges, newSource).put(newTarget, edge);
		ensureEdgesMapMutable(edges, newTarget).put(newSource, edge);
		setEndpoints(edge, newSource, newTarget);
	}

	@Override
	public void clearEdges() {
		final int n = vertices().size();
		for (int v : range(n))
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
		private final Int2IntMap edges;

		EdgeSetOut(int source) {
			super(source);
			this.edges = GraphHashmapUndirected.this.edges[source];
		}

		@Override
		public int size() {
			return edges.size();
		}

		@Override
		public boolean isEmpty() {
			return edges.isEmpty();
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
			this.edges = GraphHashmapUndirected.this.edges[target];
		}

		@Override
		public int size() {
			return edges.size();
		}

		@Override
		public boolean isEmpty() {
			return edges.isEmpty();
		}

		@Override
		public IEdgeIter iterator() {
			return new EdgeIterIn(target, edges);
		}
	}

	@Override
	void markVertex(int vertex) {
		if (edges[vertex] == EmptyEdgeMap) {
			edges[vertex] = null;
		} else {
			edges[vertex].defaultReturnValue(-2);
		}
	}

	@Override
	void unmarkVertex(int vertex) {
		if (edges[vertex] == null) {
			edges[vertex] = EmptyEdgeMap;
		} else {
			edges[vertex].defaultReturnValue(-1);
		}
	}

	@Override
	boolean isMarkedVertex(int vertex) {
		return edges[vertex] == null || edges[vertex].defaultReturnValue() == -2;
	}
}
