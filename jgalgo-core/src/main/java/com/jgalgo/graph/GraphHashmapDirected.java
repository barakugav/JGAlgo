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
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntCollection;

class GraphHashmapDirected extends GraphHashmapAbstract {

	private Int2IntMap[] edgesOut;
	private Int2IntMap[] edgesIn;
	private final DataContainer.Obj<Int2IntMap> edgesOutContainer;
	private final DataContainer.Obj<Int2IntMap> edgesInContainer;

	private static final IndexGraphBase.Capabilities Capabilities = IndexGraphBase.Capabilities.of(true, true, false);

	GraphHashmapDirected(int expectedVerticesNum, int expectedEdgesNum) {
		super(Capabilities, expectedVerticesNum, expectedEdgesNum);
		edgesOutContainer = new DataContainer.Obj<>(vertices, JGAlgoUtils.EMPTY_INT2INT_MAP_DEFVAL_NEG_ONE,
				EMPTY_MAP_ARRAY, newArr -> edgesOut = newArr);
		edgesInContainer = new DataContainer.Obj<>(vertices, JGAlgoUtils.EMPTY_INT2INT_MAP_DEFVAL_NEG_ONE,
				EMPTY_MAP_ARRAY, newArr -> edgesIn = newArr);

		addInternalVerticesContainer(edgesOutContainer);
		addInternalVerticesContainer(edgesInContainer);
	}

	GraphHashmapDirected(IndexGraph g, boolean copyVerticesWeights, boolean copyEdgesWeights) {
		super(Capabilities, g, copyVerticesWeights, copyEdgesWeights);
		final int n = g.vertices().size();

		if (g instanceof GraphHashmapDirected) {
			GraphHashmapDirected g0 = (GraphHashmapDirected) g;

			edgesOutContainer = g0.edgesOutContainer.copy(vertices, EMPTY_MAP_ARRAY, newArr -> edgesOut = newArr);
			edgesInContainer = g0.edgesInContainer.copy(vertices, EMPTY_MAP_ARRAY, newArr -> edgesIn = newArr);
			addInternalVerticesContainer(edgesOutContainer);
			addInternalVerticesContainer(edgesInContainer);

			for (int v = 0; v < n; v++) {
				if (!edgesOut[v].isEmpty()) {
					edgesOut[v] = new Int2IntOpenHashMap(edgesOut[v]);
					edgesOut[v].defaultReturnValue(-1);
				}
				if (!edgesIn[v].isEmpty()) {
					edgesIn[v] = new Int2IntOpenHashMap(edgesIn[v]);
					edgesIn[v].defaultReturnValue(-1);
				}
			}
		} else {
			edgesOutContainer = new DataContainer.Obj<>(vertices, JGAlgoUtils.EMPTY_INT2INT_MAP_DEFVAL_NEG_ONE,
					EMPTY_MAP_ARRAY, newArr -> edgesOut = newArr);
			edgesInContainer = new DataContainer.Obj<>(vertices, JGAlgoUtils.EMPTY_INT2INT_MAP_DEFVAL_NEG_ONE,
					EMPTY_MAP_ARRAY, newArr -> edgesIn = newArr);
			addInternalVerticesContainer(edgesOutContainer);
			addInternalVerticesContainer(edgesInContainer);

			for (int v = 0; v < n; v++) {
				for (IEdgeIter eit = g.outEdges(v).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int oldVal = ensureEdgesMapMutable(edgesOut, v).put(eit.targetInt(), e);
					if (oldVal != -1)
						throw new IllegalStateException("Parallel edge (idx=" + v + ",idx=" + eit.targetInt()
								+ ") already exists. Parallel edges are not allowed.");
				}
				for (IEdgeIter eit = g.inEdges(v).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int oldVal = ensureEdgesMapMutable(edgesIn, v).put(eit.sourceInt(), e);
					if (oldVal != -1)
						throw new IllegalStateException("Parallel edge (idx=" + eit.sourceInt() + ",idx=" + v
								+ ") already exists. Parallel edges are not allowed.");
				}
			}
		}
	}

	GraphHashmapDirected(IndexGraphBuilderImpl.Directed builder) {
		super(Capabilities, builder);
		edgesOutContainer = new DataContainer.Obj<>(vertices, JGAlgoUtils.EMPTY_INT2INT_MAP_DEFVAL_NEG_ONE,
				EMPTY_MAP_ARRAY, newArr -> edgesOut = newArr);
		edgesInContainer = new DataContainer.Obj<>(vertices, JGAlgoUtils.EMPTY_INT2INT_MAP_DEFVAL_NEG_ONE,
				EMPTY_MAP_ARRAY, newArr -> edgesIn = newArr);

		addInternalVerticesContainer(edgesOutContainer);
		addInternalVerticesContainer(edgesInContainer);

		for (int m = builder.edges().size(), e = 0; e < m; e++) {
			int source = builder.edgeSource(e), target = builder.edgeTarget(e);
			int oldVal1 = ensureEdgesMapMutable(edgesOut, source).put(target, e);
			int oldVal2 = ensureEdgesMapMutable(edgesIn, target).put(source, e);
			if (oldVal1 != -1)
				throw new IllegalStateException("Parallel edge (idx=" + source + ",idx=" + target
						+ ") already exists. Parallel edges are not allowed.");
			assert oldVal2 == -1;
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
		int selfEdge = edgesOut[swappedIdx].remove(swappedIdx);
		if (selfEdge != -1) {
			int oldVal = edgesIn[swappedIdx].remove(swappedIdx);
			assert oldVal == selfEdge;
		}

		for (int e : edgesOut[swappedIdx].values()) {
			int target = edgeTarget(e);
			replaceEdgeSource(e, removedIdx);
			int oldVal1 = edgesIn[target].remove(swappedIdx);
			int oldVal2 = edgesIn[target].put(removedIdx, e);
			assert oldVal1 == e;
			assert oldVal2 == -1;
		}
		for (int e : edgesIn[swappedIdx].values()) {
			int source = edgeSource(e);
			replaceEdgeTarget(e, removedIdx);
			int oldVal1 = edgesOut[source].remove(swappedIdx);
			int oldVal2 = edgesOut[source].put(removedIdx, e);
			assert oldVal1 == e;
			assert oldVal2 == -1;
		}

		if (selfEdge != -1) {
			setEndpoints(selfEdge, removedIdx, removedIdx);
			int oldVal1 = edgesOut[swappedIdx].put(removedIdx, selfEdge);
			int oldVal2 = edgesIn[swappedIdx].put(removedIdx, selfEdge);
			assert oldVal1 == -1;
			assert oldVal2 == -1;
		}

		swapAndClear(edgesOut, removedIdx, swappedIdx, JGAlgoUtils.EMPTY_INT2INT_MAP_DEFVAL_NEG_ONE);
		swapAndClear(edgesIn, removedIdx, swappedIdx, JGAlgoUtils.EMPTY_INT2INT_MAP_DEFVAL_NEG_ONE);
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
		return new EdgeSetOut(source, edgesOut);
	}

	@Override
	public IEdgeSet inEdges(int target) {
		checkVertex(target);
		return new EdgeSetIn(target, edgesIn);
	}

	@Override
	public int addEdge(int source, int target) {
		if (getEdge(source, target) != -1)
			throw new IllegalArgumentException(
					"Edge (idx=" + source + ",idx=" + target + ") already exists. Parallel edges are not allowed.");
		int edge = super.addEdge(source, target);

		ensureEdgesMapMutable(edgesOut, source).put(target, edge);
		ensureEdgesMapMutable(edgesIn, target).put(source, edge);

		return edge;
	}

	@Override
	void removeEdgeLast(int edge) {
		int source = edgeSource(edge), target = edgeTarget(edge);
		int oldVal1 = edgesOut[source].remove(target);
		int oldVal2 = edgesIn[target].remove(source);
		assert edge == oldVal1;
		assert edge == oldVal2;
		super.removeEdgeLast(edge);
	}

	@Override
	void edgeSwapAndRemove(int removedIdx, int swappedIdx) {
		int ur = edgeSource(removedIdx), vr = edgeTarget(removedIdx);
		int us = edgeSource(swappedIdx), vs = edgeTarget(swappedIdx);
		assert edgesOut[ur] != JGAlgoUtils.EMPTY_INT2INT_MAP_DEFVAL_NEG_ONE;
		assert edgesIn[vr] != JGAlgoUtils.EMPTY_INT2INT_MAP_DEFVAL_NEG_ONE;
		assert edgesOut[us] != JGAlgoUtils.EMPTY_INT2INT_MAP_DEFVAL_NEG_ONE;
		assert edgesIn[vs] != JGAlgoUtils.EMPTY_INT2INT_MAP_DEFVAL_NEG_ONE;
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
	public void reverseEdge(int edge) {
		int source = edgeSource(edge), target = edgeTarget(edge);
		if (source == target)
			return;
		if (getEdge(target, source) != -1)
			throw new IllegalArgumentException(
					"Edge (idx=" + target + ",idx=" + source + ") already exists. Parallel edges are not allowed.");

		int oldVal1 = edgesOut[source].remove(target);
		int oldVal2 = edgesIn[target].remove(source);
		assert edge == oldVal1;
		assert edge == oldVal2;

		int oldVal4 = ensureEdgesMapMutable(edgesOut, target).put(source, edge);
		int oldVal3 = ensureEdgesMapMutable(edgesIn, source).put(target, edge);
		assert -1 == oldVal3;
		assert -1 == oldVal4;

		super.reverseEdge0(edge);
	}

	@Override
	public void clearEdges() {
		final int n = vertices().size();
		for (int v = 0; v < n; v++) {
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

	class EdgeSetIn extends IndexGraphBase.EdgeSetInDirected {
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
