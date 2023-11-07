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

import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntCollection;

class GraphHashmapUndirected extends GraphHashmapAbstract {

	private Int2IntMap[] edges;
	private final DataContainer.Obj<Int2IntMap> edgesContainer;

	private static final IndexGraphBase.Capabilities Capabilities = IndexGraphBase.Capabilities.of(false, true, false);

	/**
	 * Create a new graph with no vertices and edges, with expected number of vertices and edges.
	 *
	 * @param expectedVerticesNum the expected number of vertices that will be in the graph
	 * @param expectedEdgesNum    the expected number of edges that will be in the graph
	 */
	GraphHashmapUndirected(int expectedVerticesNum, int expectedEdgesNum) {
		super(Capabilities, expectedVerticesNum, expectedEdgesNum);
		edgesContainer = new DataContainer.Obj<>(vertices, JGAlgoUtils.EMPTY_INT2INT_MAP_DEFVAL_NEG_ONE,
				EMPTY_MAP_ARRAY, newArr -> edges = newArr);

		addInternalVerticesContainer(edgesContainer);
	}

	GraphHashmapUndirected(IndexGraph g, boolean copyWeights) {
		super(Capabilities, g, copyWeights);
		final int n = g.vertices().size();

		if (g instanceof GraphHashmapUndirected) {
			GraphHashmapUndirected g0 = (GraphHashmapUndirected) g;
			edgesContainer = g0.edgesContainer.copy(vertices, EMPTY_MAP_ARRAY, newArr -> edges = newArr);
			addInternalVerticesContainer(edgesContainer);

			for (int v = 0; v < n; v++)
				if (!edges[v].isEmpty())
					edges[v] = new Int2IntOpenHashMap(edges[v]);
		} else {
			edgesContainer = new DataContainer.Obj<>(vertices, JGAlgoUtils.EMPTY_INT2INT_MAP_DEFVAL_NEG_ONE,
					EMPTY_MAP_ARRAY, newArr -> edges = newArr);
			addInternalVerticesContainer(edgesContainer);

			for (int m = g.edges().size(), e = 0; e < m; e++) {
				int u = g.edgeSource(e);
				int v = g.edgeTarget(e);
				int oldVal = ensureEdgesMapMutable(edges, u).put(v, e);
				if (oldVal != -1)
					throw new IllegalStateException("Parallel edge (idx=" + u + ",idx=" + v
							+ ") already exists. Parallel edges are not allowed.");
				if (u != v) {
					int oldVal2 = ensureEdgesMapMutable(edges, v).put(u, e);
					assert oldVal2 == -1;
				}
			}
		}
	}

	GraphHashmapUndirected(IndexGraphBuilderImpl.Undirected builder) {
		super(Capabilities, builder);
		edgesContainer = new DataContainer.Obj<>(vertices, JGAlgoUtils.EMPTY_INT2INT_MAP_DEFVAL_NEG_ONE,
				EMPTY_MAP_ARRAY, newArr -> edges = newArr);

		addInternalVerticesContainer(edgesContainer);

		for (int m = builder.edges().size(), e = 0; e < m; e++) {
			int source = builder.edgeSource(e), target = builder.edgeTarget(e);
			int oldVal1 = ensureEdgesMapMutable(edges, source).put(target, e);
			if (oldVal1 != -1)
				throw new IllegalStateException("Parallel edge (idx=" + source + ",idx=" + target
						+ ") already exists. Parallel edges are not allowed.");
			if (source != target) {
				int oldVal2 = ensureEdgesMapMutable(edges, target).put(source, e);
				assert oldVal2 == -1;
			}
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
		int selfEdge = edges[swappedIdx].remove(swappedIdx);

		for (Int2IntMap.Entry entry : JGAlgoUtils.iterable(Int2IntMaps.fastIterator(edges[swappedIdx]))) {
			int target = entry.getIntKey();
			int e = entry.getIntValue();
			replaceEdgeEndpoint(e, swappedIdx, removedIdx);
			int oldVal1 = edges[target].remove(swappedIdx);
			int oldVal2 = edges[target].put(removedIdx, e);
			assert oldVal1 == e;
			assert oldVal2 == -1;
		}

		if (selfEdge != -1) {
			setEndpoints(selfEdge, removedIdx, removedIdx);
			int oldVal = edges[swappedIdx].put(removedIdx, selfEdge);
			assert oldVal == -1;
		}

		edgesContainer.swapAndClear(removedIdx, swappedIdx);
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
		return new EdgeSetOut(source, edges);
	}

	@Override
	public IEdgeSet inEdges(int target) {
		checkVertex(target);
		return new EdgeSetIn(target, edges);
	}

	@Override
	public int addEdge(int source, int target) {
		if (getEdge(source, target) != -1)
			throw new IllegalArgumentException(
					"Edge (idx=" + source + ",idx=" + target + ") already exists. Parallel edges are not allowed.");
		int edge = super.addEdge(source, target);

		ensureEdgesMapMutable(edges, source).put(target, edge);
		if (source != target)
			ensureEdgesMapMutable(edges, target).put(source, edge);

		return edge;
	}

	@Override
	void removeEdgeLast(int edge) {
		int source = edgeSource(edge), target = edgeTarget(edge);
		int oldVal1 = edges[source].remove(target);
		assert edge == oldVal1;
		if (source != target) {
			int oldVal2 = edges[target].remove(source);
			assert edge == oldVal2;
		}
		super.removeEdgeLast(edge);
	}

	@Override
	void edgeSwapAndRemove(int removedIdx, int swappedIdx) {
		int ur = edgeSource(removedIdx), vr = edgeTarget(removedIdx);
		assert edges[ur] != JGAlgoUtils.EMPTY_INT2INT_MAP_DEFVAL_NEG_ONE;
		assert edges[vr] != JGAlgoUtils.EMPTY_INT2INT_MAP_DEFVAL_NEG_ONE;
		int oldVal1 = edges[ur].remove(vr);
		assert oldVal1 == removedIdx;
		if (ur != vr) {
			int oldVal2 = edges[vr].remove(ur);
			assert oldVal2 == removedIdx;
		}

		int us = edgeSource(swappedIdx), vs = edgeTarget(swappedIdx);
		assert edges[us] != JGAlgoUtils.EMPTY_INT2INT_MAP_DEFVAL_NEG_ONE;
		assert edges[vs] != JGAlgoUtils.EMPTY_INT2INT_MAP_DEFVAL_NEG_ONE;
		int oldVal3 = edges[us].put(vs, removedIdx);
		assert oldVal3 == swappedIdx;
		if (us != vs) {
			int oldVal4 = edges[vs].put(us, removedIdx);
			assert oldVal4 == swappedIdx;
		}

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
	public void removeOutEdgesOf(int source) {
		removeEdgesOf(source);
	}

	@Override
	public void removeInEdgesOf(int target) {
		removeEdgesOf(target);
	}

	@Override
	public void reverseEdge(int edge) {
		// Do nothing
	}

	@Override
	public void clearEdges() {
		final int n = vertices().size();
		for (int v = 0; v < n; v++)
			edges[v].clear();
		super.clearEdges();
	}

	@Override
	public void clear() {
		super.clear();
		// Don't clear allocated edges arrays
		// edges.clear();
	}

	class EdgeSetOut extends IntGraphBase.EdgeSetOutUndirected {
		private final Int2IntMap edges;

		EdgeSetOut(int source, Int2IntMap[] edges) {
			super(source);
			this.edges = edges[source];
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

	class EdgeSetIn extends IntGraphBase.EdgeSetInUndirected {
		private final Int2IntMap edges;

		EdgeSetIn(int target, Int2IntMap[] edges) {
			super(target);
			this.edges = edges[target];
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

}
