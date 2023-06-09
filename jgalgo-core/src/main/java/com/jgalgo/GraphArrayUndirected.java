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

package com.jgalgo;

import java.util.Arrays;
import com.jgalgo.GraphsUtils.GraphCapabilitiesBuilder;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntBigArrays;

/**
 * An undirected graph implementation using arrays to store edge lists.
 * <p>
 * The edges of each vertex will be stored as an array of ints. This implementation is the most efficient for most use
 * cases and should be used as the first choice for an undirected graph implementation.
 * <p>
 * If the use case require multiple vertices/edges removals, {@link GraphLinkedUndirected} could be more efficient.
 *
 * @see    GraphArrayDirected
 * @author Barak Ugav
 */
class GraphArrayUndirected extends GraphArrayAbstract {

	private int[][] edges;
	private int[] edgesNum;
	private final DataContainer.Obj<int[]> edgesContainer;
	private final DataContainer.Int edgesNumContainer;

	/**
	 * Create a new graph with no vertices and edges.
	 */
	GraphArrayUndirected() {
		this(0, 0);
	}

	/**
	 * Create a new graph with no vertices and edges, with expected number of vertices and edges.
	 *
	 * @param expectedVerticesNum the expected number of vertices that will be in the graph
	 * @param expectedEdgesNum    the expected number of edges that will be in the graph
	 */
	GraphArrayUndirected(int expectedVerticesNum, int expectedEdgesNum) {
		super(expectedVerticesNum, expectedEdgesNum);
		edgesContainer = new DataContainer.Obj<>(verticesIdStrat, IntArrays.EMPTY_ARRAY, IntBigArrays.EMPTY_BIG_ARRAY,
				newArr -> edges = newArr);
		edgesNumContainer = new DataContainer.Int(verticesIdStrat, 0, newArr -> edgesNum = newArr);

		addInternalVerticesContainer(edgesContainer);
		addInternalVerticesContainer(edgesNumContainer);
	}

	GraphArrayUndirected(GraphArrayUndirected g) {
		super(g);
		final int n = g.vertices().size();

		edgesContainer = g.edgesContainer.copy(verticesIdStrat, IntBigArrays.EMPTY_BIG_ARRAY, newArr -> edges = newArr);
		edgesNumContainer = g.edgesNumContainer.copy(verticesIdStrat, newArr -> edgesNum = newArr);
		addInternalVerticesContainer(edgesContainer);
		addInternalVerticesContainer(edgesNumContainer);

		for (int v = 0; v < n; v++)
			edges[v] = Arrays.copyOf(edges[v], edgesNum[v]);
	}

	@Override
	void removeVertexImpl(int vertex) {
		super.removeVertexImpl(vertex);
		edgesNumContainer.clear(edgesNum, vertex);
		// Reuse allocated edges array for v
		// edges.clear(v);
	}

	@Override
	void vertexSwap(int v1, int v2) {
		int[] es1 = edges[v1];
		int es1Len = edgesNum[v1];
		int[] es2 = edges[v2];
		int es2Len = edgesNum[v2];

		final int tempV = -2;
		for (int i = 0; i < es1Len; i++)
			replaceEdgeEndpoint(es1[i], v1, tempV);
		for (int i = 0; i < es2Len; i++)
			replaceEdgeEndpoint(es2[i], v2, v1);
		for (int i = 0; i < es1Len; i++)
			replaceEdgeEndpoint(es1[i], tempV, v2);

		edgesContainer.swap(edges, v1, v2);
		edgesNumContainer.swap(edgesNum, v1, v2);

		super.vertexSwap(v1, v2);
	}

	@Override
	public EdgeSet outEdges(int source) {
		checkVertex(source);
		return new EdgeSetOut(source);
	}

	@Override
	public EdgeSet inEdges(int target) {
		checkVertex(target);
		return new EdgeSetIn(target);
	}

	@Override
	public int addEdge(int source, int target) {
		int e = super.addEdge(source, target);
		addEdgeToList(edges, edgesNum, source, e);
		if (source != target)
			addEdgeToList(edges, edgesNum, target, e);
		return e;
	}

	@Override
	void removeEdgeImpl(int edge) {
		int u = edgeSource(edge), v = edgeTarget(edge);
		removeEdgeFromList(edges, edgesNum, u, edge);
		if (u != v)
			removeEdgeFromList(edges, edgesNum, v, edge);
		super.removeEdgeImpl(edge);
	}

	@Override
	void edgeSwap(int e1, int e2) {
		assert e1 != e2;

		int u1 = edgeSource(e1), v1 = edgeTarget(e1);
		int[] u1es = edges[u1];
		int i1 = edgeIndexOf(u1es, edgesNum[u1], e1);
		u1es[i1] = e2;
		if (u1 != v1) {
			int[] v1es = edges[v1];
			int j1 = edgeIndexOf(v1es, edgesNum[v1], e1);
			v1es[j1] = e2;
		}

		int u2 = edgeSource(e2), v2 = edgeTarget(e2);
		int[] u2es = edges[u2];
		int i2 = edgeIndexOf(u2es, edgesNum[u2], e2);
		u2es[i2] = e1;
		if (u2 != v2) {
			int[] v2es = edges[v2];
			int j2 = edgeIndexOf(v2es, edgesNum[v2], e2);
			v2es[j2] = e1;
		}

		super.edgeSwap(e1, e2);
	}

	@Override
	public void removeEdgesOf(int source) {
		checkVertex(source);
		while (edgesNum[source] > 0)
			removeEdge(edges[source][0]);
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
		edgesNumContainer.clear(edgesNum);
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
			GraphCapabilitiesBuilder.newUndirected().parallelEdges(true).selfEdges(true).build();

	@Override
	public IndexGraph copy() {
		return new GraphArrayUndirected(this);
	}

	private class EdgeSetOut extends GraphBase.EdgeSetOutUndirected {
		EdgeSetOut(int source) {
			super(source);
		}

		@Override
		public int size() {
			return edgesNum[source];
		}

		@Override
		public boolean isEmpty() {
			return size() == 0;
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterOut(source, edges[source], edgesNum[source]);
		}
	}

	private class EdgeSetIn extends GraphBase.EdgeSetInUndirected {
		EdgeSetIn(int target) {
			super(target);
		}

		@Override
		public int size() {
			return edgesNum[target];
		}

		@Override
		public boolean isEmpty() {
			return size() == 0;
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterIn(target, edges[target], edgesNum[target]);
		}
	}

	private class EdgeIterOut extends GraphArrayAbstract.EdgeIt {

		private final int source;

		EdgeIterOut(int source, int[] edges, int count) {
			super(edges, count);
			this.source = source;
		}

		@Override
		public int source() {
			return source;
		}

		@Override
		public int target() {
			int u0 = edgeSource(lastEdge);
			int v0 = edgeTarget(lastEdge);
			return source == u0 ? v0 : u0;
		}
	}

	private class EdgeIterIn extends GraphArrayAbstract.EdgeIt {

		private final int target;

		EdgeIterIn(int target, int[] edges, int count) {
			super(edges, count);
			this.target = target;
		}

		@Override
		public int source() {
			return edgeEndpoint(lastEdge, target);
		}

		@Override
		public int target() {
			return target;
		}
	}

}
