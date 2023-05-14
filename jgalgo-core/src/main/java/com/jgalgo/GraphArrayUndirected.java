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
		edges = new DataContainer.Obj<>(n, IntArrays.EMPTY_ARRAY, int[].class);
		edgesNum = new DataContainer.Int(n, 0);

		addInternalVerticesDataContainer(edges);
		addInternalVerticesDataContainer(edgesNum);
	}

	@Override
	public int addVertex() {
		int v = super.addVertex();
		edges.add(v);
		edgesNum.add(v);
		return v;
	}

	@Override
	public void removeVertex(int v) {
		v = vertexSwapBeforeRemove(v);
		super.removeVertex(v);
		edges.remove(v);
		edgesNum.remove(v);
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
	public EdgeIter edgesOut(int u) {
		checkVertexIdx(u);
		return new EdgeIt(u, edges.get(u), edgesNum.getInt(u));
	}

	@Override
	public int addEdge(int u, int v) {
		int e = super.addEdge(u, v);
		addEdgeToList(edges, edgesNum, u, e);
		if (u != v)
			addEdgeToList(edges, edgesNum, v, e);
		return e;
	}

	@Override
	public void removeEdge(int e) {
		e = edgeSwapBeforeRemove(e);
		int u = edgeSource(e), v = edgeTarget(e);
		removeEdgeFromList(edges, edgesNum, u, e);
		if (u != v)
			removeEdgeFromList(edges, edgesNum, v, e);
		super.removeEdge(e);
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
	public void removeEdgesOf(int u) {
		checkVertexIdx(u);
		while (edgesNum.getInt(u) > 0)
			removeEdge(edges.get(u)[0]);
	}

	@Override
	public int degreeOut(int u) {
		checkVertexIdx(u);
		return edgesNum.getInt(u);
	}

	@Override
	public void clearEdges() {
		int n = vertices().size();
		for (int u = 0; u < n; u++) {
			edges.set(u, IntArrays.EMPTY_ARRAY);
			edgesNum.set(u, 0);
		}
		super.clearEdges();
	}

	@Override
	public void clear() {
		super.clear();
		edges.clear();
		edgesNum.clear();
	}

	private class EdgeIt extends GraphArrayAbstract.EdgeIt {

		private final int u;

		EdgeIt(int u, int[] edges, int count) {
			super(edges, count);
			this.u = u;
		}

		@Override
		public int source() {
			return u;
		}

		@Override
		public int target() {
			int u0 = edgeSource(lastEdge);
			int v0 = edgeTarget(lastEdge);
			return u == u0 ? v0 : u0;
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
