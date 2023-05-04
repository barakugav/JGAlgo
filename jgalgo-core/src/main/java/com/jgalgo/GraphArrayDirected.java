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

import it.unimi.dsi.fastutil.ints.IntArrays;

/**
 * A directed graph implementation using arrays to store edge lists.
 * <p>
 * The edges of each vertex will be stored as an array of ints. This implementation is the most efficient for most use
 * cases and should be used as the first choice for a directed graph implementation.
 * <p>
 * If the use case require multiple vertices/edges removals, {@link GraphLinkedDirected} could be more efficient.
 *
 * @see    GraphArrayUndirected
 * @author Barak Ugav
 */
public class GraphArrayDirected extends GraphArrayAbstract {

	private final DataContainer.Obj<int[]> edgesOut;
	private final DataContainer.Int edgesOutNum;
	private final DataContainer.Obj<int[]> edgesIn;
	private final DataContainer.Int edgesInNum;

	/**
	 * Create a new graph with no vertices and edges.
	 */
	public GraphArrayDirected() {
		this(0);
	}

	/**
	 * Create a new graph with no edges and {@code n} vertices numbered {@code 0,1,2,..,n-1}.
	 *
	 * @param n the number of initial vertices number
	 */
	public GraphArrayDirected(int n) {
		super(n, Capabilities);
		edgesOut = new DataContainer.Obj<>(n, IntArrays.EMPTY_ARRAY, int[].class);
		edgesOutNum = new DataContainer.Int(n, 0);
		edgesIn = new DataContainer.Obj<>(n, IntArrays.EMPTY_ARRAY, int[].class);
		edgesInNum = new DataContainer.Int(n, 0);

		addInternalVerticesDataContainer(edgesOut);
		addInternalVerticesDataContainer(edgesOutNum);
		addInternalVerticesDataContainer(edgesIn);
		addInternalVerticesDataContainer(edgesInNum);
	}

	@Override
	public int addVertex() {
		int v = super.addVertex();
		edgesOut.add(v);
		edgesOutNum.add(v);
		edgesIn.add(v);
		edgesInNum.add(v);
		return v;
	}

	@Override
	public void removeVertex(int v) {
		v = vertexSwapBeforeRemove(v);
		super.removeVertex(v);
		edgesOut.remove(v);
		edgesOutNum.remove(v);
		edgesIn.remove(v);
		edgesInNum.remove(v);
	}

	@Override
	void vertexSwap(int v1, int v2) {
		int[] es1Out = edgesOut.get(v1);
		int es1OutLen = edgesOutNum.getInt(v1);
		for (int i = 0; i < es1OutLen; i++)
			replaceEdgeSource(es1Out[i], v2);

		int[] es1In = edgesIn.get(v1);
		int es1InLen = edgesInNum.getInt(v1);
		for (int i = 0; i < es1InLen; i++)
			replaceEdgeTarget(es1In[i], v2);

		int[] es2Out = edgesOut.get(v2);
		int es2OutLen = edgesOutNum.getInt(v2);
		for (int i = 0; i < es2OutLen; i++)
			replaceEdgeSource(es2Out[i], v1);

		int[] es2In = edgesIn.get(v2);
		int es2InLen = edgesInNum.getInt(v2);
		for (int i = 0; i < es2InLen; i++)
			replaceEdgeTarget(es2In[i], v1);

		edgesOut.swap(v1, v2);
		edgesOutNum.swap(v1, v2);
		edgesIn.swap(v1, v2);
		edgesInNum.swap(v1, v2);

		super.vertexSwap(v1, v2);
	}

	@Override
	public EdgeIter edgesOut(int u) {
		checkVertexIdx(u);
		return new EdgeOutIt(u, edgesOut.get(u), edgesOutNum.getInt(u));
	}

	@Override
	public EdgeIter edgesIn(int v) {
		checkVertexIdx(v);
		return new EdgeInIt(v, edgesIn.get(v), edgesInNum.getInt(v));
	}

	@Override
	public int addEdge(int u, int v) {
		int e = super.addEdge(u, v);
		addEdgeToList(edgesOut, edgesOutNum, u, e);
		addEdgeToList(edgesIn, edgesInNum, v, e);
		return e;
	}

	@Override
	public void removeEdge(int e) {
		e = edgeSwapBeforeRemove(e);
		int u = edgeSource(e), v = edgeTarget(e);
		removeEdgeFromList(edgesOut, edgesOutNum, u, e);
		removeEdgeFromList(edgesIn, edgesInNum, v, e);
		super.removeEdge(e);
	}

	@Override
	void edgeSwap(int e1, int e2) {
		assert e1 != e2;
		int u1 = edgeSource(e1), v1 = edgeTarget(e1);
		int u2 = edgeSource(e2), v2 = edgeTarget(e2);
		int[] u1es = edgesOut.get(u1), v1es = edgesIn.get(v1);
		int[] u2es = edgesOut.get(u2), v2es = edgesIn.get(v2);
		int i1 = edgeIndexOf(u1es, edgesOutNum.getInt(u1), e1);
		int j1 = edgeIndexOf(v1es, edgesInNum.getInt(v1), e1);
		int i2 = edgeIndexOf(u2es, edgesOutNum.getInt(u2), e2);
		int j2 = edgeIndexOf(v2es, edgesInNum.getInt(v2), e2);
		u1es[i1] = e2;
		v1es[j1] = e2;
		u2es[i2] = e1;
		v2es[j2] = e1;
		super.edgeSwap(e1, e2);
	}

	@Override
	public void removeEdgesOutOf(int u) {
		checkVertexIdx(u);
		while (edgesOutNum.getInt(u) > 0)
			removeEdge(edgesOut.get(u)[0]);
	}

	@Override
	public void removeEdgesInOf(int v) {
		checkVertexIdx(v);
		while (edgesInNum.getInt(v) > 0)
			removeEdge(edgesIn.get(v)[0]);
	}

	@Override
	public void reverseEdge(int e) {
		int u = edgeSource(e), v = edgeTarget(e);
		if (u == v)
			return;
		removeEdgeFromList(edgesOut, edgesOutNum, u, e);
		removeEdgeFromList(edgesIn, edgesInNum, v, e);
		addEdgeToList(edgesOut, edgesOutNum, v, e);
		addEdgeToList(edgesIn, edgesInNum, u, e);
		super.reverseEdge0(e);
	}

	@Override
	public int degreeOut(int u) {
		checkVertexIdx(u);
		return edgesOutNum.getInt(u);
	}

	@Override
	public int degreeIn(int v) {
		checkVertexIdx(v);
		return edgesInNum.getInt(v);
	}

	@Override
	public void clearEdges() {
		int n = vertices().size();
		for (int u = 0; u < n; u++) {
			// TODO do some sort of 'addKey' instead of set, no need
			edgesOut.set(u, IntArrays.EMPTY_ARRAY);
			edgesIn.set(u, IntArrays.EMPTY_ARRAY);
			edgesOutNum.set(u, 0);
			edgesInNum.set(u, 0);
		}
		super.clearEdges();
	}

	@Override
	public void clear() {
		super.clear();
		edgesOut.clear();
		edgesOutNum.clear();
		edgesIn.clear();
		edgesInNum.clear();
	}

	private class EdgeOutIt extends EdgeIt {

		private final int u;

		EdgeOutIt(int u, int[] edges, int count) {
			super(edges, count);
			this.u = u;
		}

		@Override
		public int source() {
			return u;
		}

		@Override
		public int target() {
			return edgeTarget(lastEdge);
		}

	}

	private class EdgeInIt extends EdgeIt {

		private final int v;

		EdgeInIt(int v, int[] edges, int count) {
			super(edges, count);
			this.v = v;
		}

		@Override
		public int source() {
			return edgeSource(lastEdge);
		}

		@Override
		public int target() {
			return v;
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
			return true;
		}
	};

}
