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

import java.util.Iterator;
import com.jgalgo.graph.Graphs.GraphCapabilitiesBuilder;
import com.jgalgo.internal.util.Utils;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntCollection;

class GraphHashmapDirected extends GraphHashmapAbstract {

	private Int2IntMap[] edgesOut;
	private Int2IntMap[] edgesIn;
	private final DataContainer.Obj<Int2IntMap> edgesOutContainer;
	private final DataContainer.Obj<Int2IntMap> edgesInContainer;

	GraphHashmapDirected(int expectedVerticesNum, int expectedEdgesNum) {
		super(expectedVerticesNum, expectedEdgesNum);
		edgesOutContainer = new DataContainer.Obj<>(verticesIdStrat, Utils.EMPTY_INT2INT_MAP_DEFVAL_NEG1,
				EMPTY_MAP_ARRAY, newArr -> edgesOut = newArr);
		edgesInContainer = new DataContainer.Obj<>(verticesIdStrat, Utils.EMPTY_INT2INT_MAP_DEFVAL_NEG1,
				EMPTY_MAP_ARRAY, newArr -> edgesIn = newArr);

		addInternalVerticesContainer(edgesOutContainer);
		addInternalVerticesContainer(edgesInContainer);
	}

	GraphHashmapDirected(IndexGraph g) {
		super(g);
		final int n = g.vertices().size();

		if (g instanceof GraphHashmapDirected) {
			GraphHashmapDirected g0 = (GraphHashmapDirected) g;

			edgesOutContainer =
					g0.edgesOutContainer.copy(verticesIdStrat, EMPTY_MAP_ARRAY, newArr -> edgesOut = newArr);
			edgesInContainer = g0.edgesInContainer.copy(verticesIdStrat, EMPTY_MAP_ARRAY, newArr -> edgesIn = newArr);

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
			edgesOutContainer = new DataContainer.Obj<>(verticesIdStrat, Utils.EMPTY_INT2INT_MAP_DEFVAL_NEG1,
					EMPTY_MAP_ARRAY, newArr -> edgesOut = newArr);
			edgesInContainer = new DataContainer.Obj<>(verticesIdStrat, Utils.EMPTY_INT2INT_MAP_DEFVAL_NEG1,
					EMPTY_MAP_ARRAY, newArr -> edgesIn = newArr);

			addInternalVerticesContainer(edgesOutContainer);
			addInternalVerticesContainer(edgesInContainer);

			for (int v = 0; v < n; v++) {
				for (EdgeIter eit = g.outEdges(v).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int oldVal = edgesOut[v].put(eit.target(), e);
					if (oldVal != -1)
						throw new IllegalStateException("Parallel edge (" + v + "," + eit.target()
								+ ") already exists. Parallel edges are not allowed.");
				}
				for (EdgeIter eit = g.inEdges(v).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int oldVal = edgesIn[v].put(eit.source(), e);
					if (oldVal != -1)
						throw new IllegalStateException("Parallel edge (" + eit.source() + "," + v
								+ ") already exists. Parallel edges are not allowed.");
				}
			}
		}
	}

	@Override
	void removeVertexImpl(int vertex) {
		super.removeVertexImpl(vertex);
		edgesOut[vertex].clear();
		edgesIn[vertex].clear();
		// Reuse allocated edges arrays for v
		// edgesOutContainer.clear(v);
		// edgesInContainer.clear(v);
	}

	@Override
	void vertexSwap(int v1, int v2) {
		assert v1 != v2;

		/* we handle self edges of v1,v2 and edges between v1 and v2 separately */
		int v1v1 = edgesOut[v1].remove(v1);
		int v1v2 = edgesOut[v1].remove(v2);
		int v2v1 = edgesOut[v2].remove(v1);
		int v2v2 = edgesOut[v2].remove(v2);
		if (v1v1 != -1) {
			int oldVal = edgesIn[v1].remove(v1);
			assert oldVal == v1v1;
		}
		if (v1v2 != -1) {
			int oldVal = edgesIn[v2].remove(v1);
			assert oldVal == v1v2;
		}
		if (v2v1 != -1) {
			int oldVal = edgesIn[v1].remove(v2);
			assert oldVal == v2v1;
		}
		if (v2v2 != -1) {
			int oldVal = edgesIn[v2].remove(v2);
			assert oldVal == v2v2;
		}

		for (Iterator<Int2IntMap.Entry> eit = Int2IntMaps.fastIterator(edgesOut[v1]); eit.hasNext();) {
			Int2IntMap.Entry entry = eit.next();
			int target = entry.getIntKey();
			int e = entry.getIntValue();

			replaceEdgeSource(e, v2);
			int oldVal1 = edgesIn[target].remove(v1);
			int oldVal2 = edgesIn[target].put(v2, e);
			assert oldVal1 == e;
			assert oldVal2 == -1;
		}
		for (Iterator<Int2IntMap.Entry> eit = Int2IntMaps.fastIterator(edgesIn[v1]); eit.hasNext();) {
			Int2IntMap.Entry entry = eit.next();
			int source = entry.getIntKey();
			int e = entry.getIntValue();

			replaceEdgeTarget(e, v2);
			int oldVal1 = edgesOut[source].remove(v1);
			int oldVal2 = edgesOut[source].put(v2, e);
			assert oldVal1 == e;
			assert oldVal2 == -1;
		}
		for (Iterator<Int2IntMap.Entry> eit = Int2IntMaps.fastIterator(edgesOut[v2]); eit.hasNext();) {
			Int2IntMap.Entry entry = eit.next();
			int target = entry.getIntKey();
			int e = entry.getIntValue();

			replaceEdgeSource(e, v1);
			int oldVal1 = edgesIn[target].remove(v2);
			int oldVal2 = edgesIn[target].put(v1, e);
			assert oldVal1 == e;
			assert oldVal2 == -1;
		}
		for (Iterator<Int2IntMap.Entry> eit = Int2IntMaps.fastIterator(edgesIn[v2]); eit.hasNext();) {
			Int2IntMap.Entry entry = eit.next();
			int source = entry.getIntKey();
			int e = entry.getIntValue();

			replaceEdgeTarget(e, v1);
			int oldVal1 = edgesOut[source].remove(v2);
			int oldVal2 = edgesOut[source].put(v1, e);
			assert oldVal1 == e;
			assert oldVal2 == -1;
		}

		if (v1v1 != -1) {
			EdgeEndpointsContainer.setEndpoints(edgeEndpoints(), v1v1, v2, v2);
			int oldVal1 = edgesOut[v1].put(v2, v1v1);
			int oldVal2 = edgesIn[v1].put(v2, v1v1);
			assert oldVal1 == -1;
			assert oldVal2 == -1;
		}
		if (v1v2 != -1) {
			EdgeEndpointsContainer.setEndpoints(edgeEndpoints(), v1v2, v2, v1);
			int oldVal1 = edgesOut[v1].put(v1, v1v2);
			int oldVal2 = edgesIn[v1].put(v1, v1v2);
			assert oldVal1 == -1;
			assert oldVal2 == -1;
		}
		if (v2v1 != -1) {
			EdgeEndpointsContainer.setEndpoints(edgeEndpoints(), v2v1, v1, v2);
			int oldVal1 = edgesOut[v2].put(v2, v2v1);
			int oldVal2 = edgesIn[v2].put(v2, v2v1);
			assert oldVal1 == -1;
			assert oldVal2 == -1;
		}
		if (v2v2 != -1) {
			EdgeEndpointsContainer.setEndpoints(edgeEndpoints(), v2v2, v1, v1);
			int oldVal1 = edgesOut[v2].put(v1, v2v2);
			int oldVal2 = edgesIn[v2].put(v1, v2v2);
			assert oldVal1 == -1;
			assert oldVal2 == -1;
		}

		edgesOutContainer.swap(edgesOut, v1, v2);
		edgesInContainer.swap(edgesIn, v1, v2);

		super.vertexSwap(v1, v2);
	}

	@Override
	public int getEdge(int source, int target) {
		checkVertex(source);
		checkVertex(target);
		return edgesOut[source].get(target);
	}

	@Override
	public EdgeSet outEdges(int source) {
		checkVertex(source);
		return new EdgeSetOut(source, edgesOut);
	}

	@Override
	public EdgeSet inEdges(int target) {
		checkVertex(target);
		return new EdgeSetIn(target, edgesIn);
	}

	@Override
	public int addEdge(int source, int target) {
		if (getEdge(source, target) != -1)
			throw new IllegalArgumentException(
					"Edge (" + source + "," + target + ") already exists. Parallel edges are not allowed.");
		int edge = super.addEdge(source, target);

		ensureEdgesMapMutable(edgesOut, source).put(target, edge);
		ensureEdgesMapMutable(edgesIn, target).put(source, edge);

		return edge;
	}

	@Override
	void removeEdgeImpl(int edge) {
		int source = edgeSource(edge), target = edgeTarget(edge);
		int oldVal1 = edgesOut[source].remove(target);
		int oldVal2 = edgesIn[target].remove(source);
		assert edge == oldVal1;
		assert edge == oldVal2;
		super.removeEdgeImpl(edge);
	}

	@Override
	void edgeSwap(int e1, int e2) {
		assert e1 != e2;
		int u1 = edgeSource(e1), v1 = edgeTarget(e1);
		int u2 = edgeSource(e2), v2 = edgeTarget(e2);
		assert edgesOut[u1] != Utils.EMPTY_INT2INT_MAP_DEFVAL_NEG1;
		assert edgesIn[v1] != Utils.EMPTY_INT2INT_MAP_DEFVAL_NEG1;
		assert edgesOut[u2] != Utils.EMPTY_INT2INT_MAP_DEFVAL_NEG1;
		assert edgesIn[v2] != Utils.EMPTY_INT2INT_MAP_DEFVAL_NEG1;
		int oldVal1 = edgesOut[u1].put(v1, e2);
		int oldVal2 = edgesIn[v1].put(u1, e2);
		int oldVal3 = edgesOut[u2].put(v2, e1);
		int oldVal4 = edgesIn[v2].put(u2, e1);
		assert oldVal1 == e1;
		assert oldVal2 == e1;
		assert oldVal3 == e2;
		assert oldVal4 == e2;
		super.edgeSwap(e1, e2);
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
					"Edge (" + target + "," + source + ") already exists. Parallel edges are not allowed.");

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

	@Override
	public GraphCapabilities getCapabilities() {
		return Capabilities;
	}

	private static final GraphCapabilities Capabilities =
			GraphCapabilitiesBuilder.newDirected().parallelEdges(false).selfEdges(true).build();

	class EdgeSetOut extends GraphBase.EdgeSetOutDirected {
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
		public EdgeIter iterator() {
			return new EdgeIterOut(source, edges);
		}
	}

	class EdgeSetIn extends GraphBase.EdgeSetInDirected {
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
		public EdgeIter iterator() {
			return new EdgeIterIn(target, edges);
		}
	}

}
