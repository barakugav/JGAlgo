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

import com.jgalgo.GraphsUtils.UndirectedGraphImpl;
import it.unimi.dsi.fastutil.ints.IntArrays;

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
class GraphArrayUndirected extends GraphArrayAbstract implements UndirectedGraphImpl {

	private final DataContainer.Obj<int[]> edges;
	private final DataContainer.Int edgesNum;

	/**
	 * Create a new graph with no vertices and edges.
	 */
	GraphArrayUndirected() {
		this(0);
	}

	/**
	 * Create a new graph with no edges and {@code n} vertices numbered {@code 0,1,2,..,n-1}.
	 *
	 * @param n the number of initial vertices number
	 */
	GraphArrayUndirected(int n) {
		super(n, Capabilities);
		edges = new DataContainer.Obj<>(verticesIDStrategy, IntArrays.EMPTY_ARRAY, int[].class);
		edgesNum = new DataContainer.Int(verticesIDStrategy, 0);

		addInternalVerticesDataContainer(edges);
		addInternalVerticesDataContainer(edgesNum);
	}

	@Override
	public void removeVertex(int vertex) {
		vertex = vertexSwapBeforeRemove(vertex);
		super.removeVertex(vertex);
		edgesNum.clear(vertex);
		// Reuse allocated edges array for v
		// edges.clear(v);
	}

	@Override
	void vertexSwap(int v1, int v2) {
		int[] es1 = edges.get(v1);
		int es1Len = edgesNum.getInt(v1);
		int[] es2 = edges.get(v2);
		int es2Len = edgesNum.getInt(v2);

		final int tempV = -2;
		for (int i = 0; i < es1Len; i++)
			replaceEdgeEndpoint(es1[i], v1, tempV);
		for (int i = 0; i < es2Len; i++)
			replaceEdgeEndpoint(es2[i], v2, v1);
		for (int i = 0; i < es1Len; i++)
			replaceEdgeEndpoint(es1[i], tempV, v2);

		edges.swap(v1, v2);
		edgesNum.swap(v1, v2);

		super.vertexSwap(v1, v2);
	}

	@Override
	public EdgeIter edgesOut(int source) {
		checkVertex(source);
		return new EdgeIt(source, edges.get(source), edgesNum.getInt(source));
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
	public void removeEdge(int edge) {
		edge = edgeSwapBeforeRemove(edge);
		int u = edgeSource(edge), v = edgeTarget(edge);
		removeEdgeFromList(edges, edgesNum, u, edge);
		if (u != v)
			removeEdgeFromList(edges, edgesNum, v, edge);
		super.removeEdge(edge);
	}

	@Override
	void edgeSwap(int e1, int e2) {
		assert e1 != e2;

		int u1 = edgeSource(e1), v1 = edgeTarget(e1);
		int[] u1es = edges.get(u1);
		int i1 = edgeIndexOf(u1es, edgesNum.getInt(u1), e1);
		u1es[i1] = e2;
		if (u1 != v1) {
			int[] v1es = edges.get(v1);
			int j1 = edgeIndexOf(v1es, edgesNum.getInt(v1), e1);
			v1es[j1] = e2;
		}

		int u2 = edgeSource(e2), v2 = edgeTarget(e2);
		int[] u2es = edges.get(u2);
		int i2 = edgeIndexOf(u2es, edgesNum.getInt(u2), e2);
		u2es[i2] = e1;
		if (u2 != v2) {
			int[] v2es = edges.get(v2);
			int j2 = edgeIndexOf(v2es, edgesNum.getInt(v2), e2);
			v2es[j2] = e1;
		}

		super.edgeSwap(e1, e2);
	}

	@Override
	public void removeEdgesOf(int source) {
		checkVertex(source);
		while (edgesNum.getInt(source) > 0)
			removeEdge(edges.get(source)[0]);
	}

	@Override
	public int degreeOut(int source) {
		checkVertex(source);
		return edgesNum.getInt(source);
	}

	@Override
	public void clearEdges() {
		edgesNum.clear();
		super.clearEdges();
	}

	@Override
	public void clear() {
		super.clear();
		// Don't clear allocated edges arrays
		// edges.clear();
	}

	private class EdgeIt extends GraphArrayAbstract.EdgeIt {

		private final int source;

		EdgeIt(int source, int[] edges, int count) {
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

	private static final GraphCapabilities Capabilities = new GraphCapabilities() {
		@Override
		public boolean vertexAdd() {
			return true;
		}

		@Override
		public boolean vertexRemove() {
			return true;
		}

		@Override
		public boolean edgeAdd() {
			return true;
		}

		@Override
		public boolean edgeRemove() {
			return true;
		}

		@Override
		public boolean parallelEdges() {
			return true;
		}

		@Override
		public boolean selfEdges() {
			return true;
		}

		@Override
		public boolean directed() {
			return false;
		}
	};

}
