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
class GraphArrayDirected extends GraphArrayAbstract {

	private final DataContainer.Obj<int[]> edgesOut;
	private final DataContainer.Int edgesOutNum;
	private final DataContainer.Obj<int[]> edgesIn;
	private final DataContainer.Int edgesInNum;

	/**
	 * Create a new graph with no vertices and edges.
	 */
	GraphArrayDirected() {
		this(0);
	}

	/**
	 * Create a new graph with no edges and {@code n} vertices numbered {@code 0,1,2,..,n-1}.
	 *
	 * @param n the number of initial vertices number
	 */
	GraphArrayDirected(int n) {
		super(n, Capabilities);
		edgesOut = new DataContainer.Obj<>(verticesIDStrategy, IntArrays.EMPTY_ARRAY, int[].class);
		edgesOutNum = new DataContainer.Int(verticesIDStrategy, 0);
		edgesIn = new DataContainer.Obj<>(verticesIDStrategy, IntArrays.EMPTY_ARRAY, int[].class);
		edgesInNum = new DataContainer.Int(verticesIDStrategy, 0);

		addInternalVerticesDataContainer(edgesOut);
		addInternalVerticesDataContainer(edgesOutNum);
		addInternalVerticesDataContainer(edgesIn);
		addInternalVerticesDataContainer(edgesInNum);
	}

	@Override
	public void removeVertex(int vertex) {
		vertex = vertexSwapBeforeRemove(vertex);
		super.removeVertex(vertex);
		edgesOutNum.clear(vertex);
		edgesInNum.clear(vertex);
		// Reuse allocated edges arrays for v
		// edgesOut.clear(v);
		// edgesIn.clear(v);
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
	public EdgeIter edgesOut(int source) {
		checkVertex(source);
		return new EdgeOutIt(source, edgesOut.get(source), edgesOutNum.getInt(source));
	}

	@Override
	public EdgeIter edgesIn(int target) {
		checkVertex(target);
		return new EdgeInIt(target, edgesIn.get(target), edgesInNum.getInt(target));
	}

	@Override
	public int addEdge(int source, int target) {
		int e = super.addEdge(source, target);
		addEdgeToList(edgesOut, edgesOutNum, source, e);
		addEdgeToList(edgesIn, edgesInNum, target, e);
		return e;
	}

	@Override
	public void removeEdge(int edge) {
		edge = edgeSwapBeforeRemove(edge);
		int u = edgeSource(edge), v = edgeTarget(edge);
		removeEdgeFromList(edgesOut, edgesOutNum, u, edge);
		removeEdgeFromList(edgesIn, edgesInNum, v, edge);
		super.removeEdge(edge);
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
	public void removeEdgesOutOf(int source) {
		checkVertex(source);
		while (edgesOutNum.getInt(source) > 0)
			removeEdge(edgesOut.get(source)[0]);
	}

	@Override
	public void removeEdgesInOf(int target) {
		checkVertex(target);
		while (edgesInNum.getInt(target) > 0)
			removeEdge(edgesIn.get(target)[0]);
	}

	@Override
	public void reverseEdge(int edge) {
		int u = edgeSource(edge), v = edgeTarget(edge);
		if (u == v)
			return;
		removeEdgeFromList(edgesOut, edgesOutNum, u, edge);
		removeEdgeFromList(edgesIn, edgesInNum, v, edge);
		addEdgeToList(edgesOut, edgesOutNum, v, edge);
		addEdgeToList(edgesIn, edgesInNum, u, edge);
		super.reverseEdge0(edge);
	}

	@Override
	public int degreeOut(int source) {
		checkVertex(source);
		return edgesOutNum.getInt(source);
	}

	@Override
	public int degreeIn(int target) {
		checkVertex(target);
		return edgesInNum.getInt(target);
	}

	@Override
	public void clearEdges() {
		edgesOutNum.clear();
		edgesInNum.clear();
		super.clearEdges();
	}

	@Override
	public void clear() {
		super.clear();
		// Don't clear allocated edges arrays
		// edgesOut.clear();
		// edgesIn.clear();
	}

	private class EdgeOutIt extends EdgeIt {

		private final int u;

		EdgeOutIt(int source, int[] edges, int count) {
			super(edges, count);
			this.u = source;
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

		private final int target;

		EdgeInIt(int target, int[] edges, int count) {
			super(edges, count);
			this.target = target;
		}

		@Override
		public int source() {
			return edgeSource(lastEdge);
		}

		@Override
		public int target() {
			return target;
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
