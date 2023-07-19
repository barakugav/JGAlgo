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

class GraphHashmapUndirected extends GraphHashmapAbstract {

	private Int2IntMap[] edges;
	private final DataContainer.Obj<Int2IntMap> edgesContainer;

	/**
	 * Create a new graph with no vertices and edges, with expected number of vertices and edges.
	 *
	 * @param expectedVerticesNum the expected number of vertices that will be in the graph
	 * @param expectedEdgesNum    the expected number of edges that will be in the graph
	 */
	GraphHashmapUndirected(int expectedVerticesNum, int expectedEdgesNum) {
		super(expectedVerticesNum, expectedEdgesNum);
		edgesContainer = new DataContainer.Obj<>(verticesIdStrat, Utils.EMPTY_INT2INT_MAP_DEFVAL_NEG1, EMPTY_MAP_ARRAY,
				newArr -> edges = newArr);

		addInternalVerticesContainer(edgesContainer);
	}

	GraphHashmapUndirected(IndexGraph g) {
		super(g);
		final int n = g.vertices().size();

		if (g instanceof GraphHashmapUndirected) {
			GraphHashmapUndirected g0 = (GraphHashmapUndirected) g;
			edgesContainer = g0.edgesContainer.copy(verticesIdStrat, EMPTY_MAP_ARRAY, newArr -> edges = newArr);
			addInternalVerticesContainer(edgesContainer);

			for (int v = 0; v < n; v++)
				if (!edges[v].isEmpty())
					edges[v] = new Int2IntOpenHashMap(edges[v]);
		} else {
			edgesContainer = new DataContainer.Obj<>(verticesIdStrat, Utils.EMPTY_INT2INT_MAP_DEFVAL_NEG1,
					EMPTY_MAP_ARRAY, newArr -> edges = newArr);

			addInternalVerticesContainer(edgesContainer);

			for (int u = 0; u < n; u++) {
				for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.target();
					int oldVal = edges[u].put(v, e);
					if (oldVal != -1)
						throw new IllegalStateException("Parallel edge (" + u + "," + eit.target()
								+ ") already exists. Parallel edges are not allowed.");
					if (u != v) {
						int oldVal2 = edges[v].put(u, e);
						assert oldVal2 == -1;
					}
				}
			}
		}
	}

	@Override
	void removeVertexImpl(int vertex) {
		super.removeVertexImpl(vertex);
		assert edges[vertex].isEmpty();
		// Reuse allocated edges array for v
		// edges.clear(v);
	}

	@Override
	void vertexSwap(int v1, int v2) {
		assert v1 != v2;

		/* we handle self edges of v1,v2 and edges between v1 and v2 separately */
		int v1v1 = edges[v1].remove(v1);
		int v1v2 = edges[v1].remove(v2);
		int v2v1 = edges[v2].remove(v1);
		assert v1v2 == v2v1;
		int v2v2 = edges[v2].remove(v2);

		for (Iterator<Int2IntMap.Entry> eit = Int2IntMaps.fastIterator(edges[v1]); eit.hasNext();) {
			Int2IntMap.Entry entry = eit.next();
			int target = entry.getIntKey();
			int e = entry.getIntValue();

			replaceEdgeEndpoint(e, v1, v2);
			int oldVal1 = edges[target].remove(v1);
			int oldVal2 = edges[target].put(v2, e);
			assert oldVal1 == e;
			assert oldVal2 == -1;
		}
		for (Iterator<Int2IntMap.Entry> eit = Int2IntMaps.fastIterator(edges[v2]); eit.hasNext();) {
			Int2IntMap.Entry entry = eit.next();
			int target = entry.getIntKey();
			int e = entry.getIntValue();

			replaceEdgeEndpoint(e, v2, v1);
			int oldVal1 = edges[target].remove(v2);
			int oldVal2 = edges[target].put(v1, e);
			assert oldVal1 == e;
			assert oldVal2 == -1;
		}

		if (v1v1 != -1) {
			EdgeEndpointsContainer.setEndpoints(edgeEndpoints(), v1v1, v2, v2);
			int oldVal = edges[v1].put(v2, v1v1);
			assert oldVal == -1;
		}
		if (v1v2 != -1) {
			EdgeEndpointsContainer.reverseEdge(edgeEndpoints(), v1v2);
			int oldVal1 = edges[v1].put(v1, v1v2);
			int oldVal2 = edges[v2].put(v2, v1v2);
			assert oldVal1 == -1;
			assert oldVal2 == -1;
		}
		if (v2v2 != -1) {
			EdgeEndpointsContainer.setEndpoints(edgeEndpoints(), v2v2, v1, v1);
			int oldVal = edges[v2].put(v1, v2v2);
			assert oldVal == -1;
		}

		edgesContainer.swap(edges, v1, v2);

		super.vertexSwap(v1, v2);
	}

	@Override
	public int getEdge(int source, int target) {
		checkVertex(source);
		checkVertex(target);
		return edges[source].get(target);
	}

	@Override
	public EdgeSet outEdges(int source) {
		checkVertex(source);
		return new EdgeSetOut(source, edges);
	}

	@Override
	public EdgeSet inEdges(int target) {
		checkVertex(target);
		return new EdgeSetIn(target, edges);
	}

	@Override
	public int addEdge(int source, int target) {
		if (getEdge(source, target) != -1)
			throw new IllegalArgumentException(
					"Edge (" + source + "," + target + ") already exists. Parallel edges are not allowed.");
		int edge = super.addEdge(source, target);

		ensureEdgesMapMutable(edges, source).put(target, edge);
		if (source != target)
			ensureEdgesMapMutable(edges, target).put(source, edge);

		return edge;
	}

	@Override
	void removeEdgeImpl(int edge) {
		int source = edgeSource(edge), target = edgeTarget(edge);
		int oldVal1 = edges[source].remove(target);
		assert edge == oldVal1;
		if (source != target) {
			int oldVal2 = edges[target].remove(source);
			assert edge == oldVal2;
		}
		super.removeEdgeImpl(edge);
	}

	@Override
	void edgeSwap(int e1, int e2) {
		assert e1 != e2;

		int u1 = edgeSource(e1), v1 = edgeTarget(e1);
		assert edges[u1] != Utils.EMPTY_INT2INT_MAP_DEFVAL_NEG1;
		assert edges[v1] != Utils.EMPTY_INT2INT_MAP_DEFVAL_NEG1;
		int oldVal1 = edges[u1].put(v1, e2);
		assert oldVal1 == e1;
		if (u1 != v1) {
			int oldVal2 = edges[v1].put(u1, e2);
			assert oldVal2 == e1;
		}

		int u2 = edgeSource(e2), v2 = edgeTarget(e2);
		assert edges[u2] != Utils.EMPTY_INT2INT_MAP_DEFVAL_NEG1;
		assert edges[v2] != Utils.EMPTY_INT2INT_MAP_DEFVAL_NEG1;
		int oldVal3 = edges[u2].put(v2, e1);
		assert oldVal3 == e2;
		if (u2 != v2) {
			int oldVal4 = edges[v2].put(u2, e1);
			assert oldVal4 == e2;
		}

		super.edgeSwap(e1, e2);
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

	@Override
	public GraphCapabilities getCapabilities() {
		return Capabilities;
	}

	private static final GraphCapabilities Capabilities =
			GraphCapabilitiesBuilder.newUndirected().parallelEdges(false).selfEdges(true).build();

	class EdgeSetOut extends GraphBase.EdgeSetOutUndirected {
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

	class EdgeSetIn extends GraphBase.EdgeSetInUndirected {
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
