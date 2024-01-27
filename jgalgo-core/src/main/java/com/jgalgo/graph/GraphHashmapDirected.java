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
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntCollection;

class GraphHashmapDirected extends GraphHashmapAbstract implements GraphDefaultsDirected {

	private Int2IntMap[] edgesOut;
	private Int2IntMap[] edgesIn;
	private final DataContainer.Obj<Int2IntMap> edgesOutContainer;
	private final DataContainer.Obj<Int2IntMap> edgesInContainer;

	private static final GraphBaseMutable.Capabilities CapabilitiesNoSelfEdges =
			GraphBaseMutable.Capabilities.of(true, false, false);
	private static final GraphBaseMutable.Capabilities CapabilitiesWithSelfEdges =
			GraphBaseMutable.Capabilities.of(true, true, false);

	private static GraphBaseMutable.Capabilities capabilities(boolean selfEdges) {
		return selfEdges ? CapabilitiesWithSelfEdges : CapabilitiesNoSelfEdges;
	}

	GraphHashmapDirected(boolean selfEdges, int expectedVerticesNum, int expectedEdgesNum) {
		super(capabilities(selfEdges), expectedVerticesNum, expectedEdgesNum);
		edgesOutContainer = newVerticesContainer(EmptyEdgeMap, EMPTY_MAP_ARRAY, newArr -> edgesOut = newArr);
		edgesInContainer = newVerticesContainer(EmptyEdgeMap, EMPTY_MAP_ARRAY, newArr -> edgesIn = newArr);
	}

	GraphHashmapDirected(boolean selfEdges, IndexGraph g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		super(capabilities(selfEdges), g, copyVerticesWeights, copyEdgesWeights);
		final int n = g.vertices().size();

		if (g instanceof GraphHashmapDirected) {
			GraphHashmapDirected g0 = (GraphHashmapDirected) g;

			edgesOutContainer =
					copyVerticesContainer(g0.edgesOutContainer, EMPTY_MAP_ARRAY, newArr -> edgesOut = newArr);
			edgesInContainer = copyVerticesContainer(g0.edgesInContainer, EMPTY_MAP_ARRAY, newArr -> edgesIn = newArr);

			for (int v : range(n)) {
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

			for (int v : range(n)) {
				for (IEdgeIter eit = g.outEdges(v).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int oldVal = ensureEdgesMapMutable(edgesOut, v).put(eit.targetInt(), e);
					if (oldVal >= 0)
						throw new IllegalArgumentException("Parallel edge (idx=" + v + ",idx=" + eit.targetInt()
								+ ") already exists. Parallel edges are not allowed.");
				}
				for (IEdgeIter eit = g.inEdges(v).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int oldVal = ensureEdgesMapMutable(edgesIn, v).put(eit.sourceInt(), e);
					if (oldVal >= 0)
						throw new IllegalArgumentException("Parallel edge (idx=" + eit.sourceInt() + ",idx=" + v
								+ ") already exists. Parallel edges are not allowed.");
				}
			}
		}
	}

	GraphHashmapDirected(boolean selfEdges, IndexGraphBuilderImpl builder) {
		super(capabilities(selfEdges), builder);
		assert builder.isDirected();

		edgesOutContainer = newVerticesContainer(EmptyEdgeMap, EMPTY_MAP_ARRAY, newArr -> edgesOut = newArr);
		edgesInContainer = newVerticesContainer(EmptyEdgeMap, EMPTY_MAP_ARRAY, newArr -> edgesIn = newArr);

		for (int e : range(builder.edges.size())) {
			int source = builder.edgeSource(e), target = builder.edgeTarget(e);
			int oldVal1 = ensureEdgesMapMutable(edgesOut, source).put(target, e);
			int oldVal2 = ensureEdgesMapMutable(edgesIn, target).put(source, e);
			if (oldVal1 >= 0)
				throw new IllegalArgumentException("Parallel edge (idx=" + source + ",idx=" + target
						+ ") already exists. Parallel edges are not allowed.");
			assert oldVal2 < 0;
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

		/* we handle the self edge of the swapped vertex separately */
		int selfEdge = -1;
		if (isAllowSelfEdges() && (selfEdge = edgesOut[swappedIdx].remove(swappedIdx)) >= 0) {
			int oldVal = edgesIn[swappedIdx].remove(swappedIdx);
			assert oldVal == selfEdge;
		}

		for (int e : edgesOut[swappedIdx].values()) {
			int target = target(e);
			replaceEdgeSource(e, removedIdx);
			int oldVal1 = edgesIn[target].remove(swappedIdx);
			int oldVal2 = edgesIn[target].put(removedIdx, e);
			assert oldVal1 == e;
			assert oldVal2 < 0;
		}
		for (int e : edgesIn[swappedIdx].values()) {
			int source = source(e);
			replaceEdgeTarget(e, removedIdx);
			int oldVal1 = edgesOut[source].remove(swappedIdx);
			int oldVal2 = edgesOut[source].put(removedIdx, e);
			assert oldVal1 == e;
			assert oldVal2 < 0;
		}

		if (selfEdge >= 0) {
			setEndpoints(selfEdge, removedIdx, removedIdx);
			int oldVal1 = edgesOut[swappedIdx].put(removedIdx, selfEdge);
			int oldVal2 = edgesIn[swappedIdx].put(removedIdx, selfEdge);
			assert oldVal1 < 0;
			assert oldVal2 < 0;
		}

		swapAndClear(edgesOut, removedIdx, swappedIdx, EmptyEdgeMap);
		swapAndClear(edgesIn, removedIdx, swappedIdx, EmptyEdgeMap);
		super.vertexSwapAndRemove(removedIdx, swappedIdx);
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
		ensureEdgesMapMutable(edgesOut, source).put(target, edge);
		ensureEdgesMapMutable(edgesIn, target).put(source, edge);
		return edge;
	}

	@Override
	void removeEdgeLast(int edge) {
		int source = source(edge), target = target(edge);
		int oldVal1 = edgesOut[source].remove(target);
		int oldVal2 = edgesIn[target].remove(source);
		assert edge == oldVal1;
		assert edge == oldVal2;
		super.removeEdgeLast(edge);
	}

	@Override
	void edgeSwapAndRemove(int removedIdx, int swappedIdx) {
		int ur = source(removedIdx), vr = target(removedIdx);
		int us = source(swappedIdx), vs = target(swappedIdx);
		assert edgesOut[ur] != EmptyEdgeMap;
		assert edgesIn[vr] != EmptyEdgeMap;
		assert edgesOut[us] != EmptyEdgeMap;
		assert edgesIn[vs] != EmptyEdgeMap;
		int oldVal1 = edgesOut[ur].remove(vr);
		int oldVal2 = edgesIn[vr].remove(ur);
		int oldVal3 = edgesOut[us].put(vs, removedIdx);
		int oldVal4 = edgesIn[vs].put(us, removedIdx);
		assert oldVal1 == removedIdx;
		assert oldVal2 == removedIdx;
		assert oldVal3 == swappedIdx;
		assert oldVal4 == swappedIdx;
		super.edgeSwapAndRemove(removedIdx, swappedIdx);
	}

	@Override
	public void removeOutEdgesOf(int source) {
		checkVertex(source);
		IntCollection edges = edgesOut[source].values();
		while (!edges.isEmpty())
			removeEdge(edges.iterator().nextInt());
	}

	@Override
	public void removeInEdgesOf(int target) {
		checkVertex(target);
		IntCollection edges = edgesIn[target].values();
		while (!edges.isEmpty())
			removeEdge(edges.iterator().nextInt());
	}

	@Override
	public void moveEdge(int edge, int newSource, int newTarget) {
		checkEdge(edge);
		int oldSource = source(edge), oldTarget = target(edge);
		if (oldSource == newSource && oldTarget == newTarget)
			return;
		checkNewEdgeEndpoints(newSource, newTarget);

		int oldVal1 = edgesOut[oldSource].remove(oldTarget);
		int oldVal2 = edgesIn[oldTarget].remove(oldSource);
		assert edge == oldVal1 && edge == oldVal2;

		int oldVal4 = ensureEdgesMapMutable(edgesOut, newSource).put(newTarget, edge);
		int oldVal3 = ensureEdgesMapMutable(edgesIn, newTarget).put(newSource, edge);
		assert oldVal3 < 0 && oldVal4 < 0;

		setEndpoints(edge, newSource, newTarget);
	}

	@Override
	public void clearEdges() {
		final int n = vertices().size();
		for (int v : range(n)) {
			edgesOut[v].clear();
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
		private final Int2IntMap edges;

		EdgeSetOut(int source) {
			super(source);
			this.edges = edgesOut[source];
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

	class EdgeSetIn extends IndexGraphBase.EdgeSetInDirected {
		private final Int2IntMap edges;

		EdgeSetIn(int target) {
			super(target);
			this.edges = edgesIn[target];
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
		if (edgesOut[vertex] == EmptyEdgeMap) {
			edgesOut[vertex] = null;
		} else {
			edgesOut[vertex].defaultReturnValue(-2);
		}
	}

	@Override
	void unmarkVertex(int vertex) {
		if (edgesOut[vertex] == null) {
			edgesOut[vertex] = EmptyEdgeMap;
		} else {
			edgesOut[vertex].defaultReturnValue(-1);
		}
	}

	@Override
	boolean isMarkedVertex(int vertex) {
		return edgesOut[vertex] == null || edgesOut[vertex].defaultReturnValue() == -2;
	}
}
