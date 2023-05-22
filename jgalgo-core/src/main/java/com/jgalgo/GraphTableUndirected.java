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
import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * A undirected graph implementation using a two dimensional table to store all edges.
 * <p>
 * If the graph contains \(n\) vertices, table of size {@code [n][n]} stores the edges of the graph. The implementation
 * does not support multiple edges with identical source and target.
 * <p>
 * This implementation is efficient for use cases where fast lookups of edge \((u,v)\) are required, as they can be
 * answered in \(O(1)\) time, but it should not be the default choice for an undirected graph.
 *
 * @see    GraphTableDirected
 * @author Barak Ugav
 */
class GraphTableUndirected extends GraphTableAbstract implements UndirectedGraphImpl {

	/**
	 * Create a new graph with no edges and {@code n} vertices numbered {@code 0,1,2,..,n-1}.
	 *
	 * @param n the number of initial vertices number
	 */
	GraphTableUndirected(int n) {
		super(n, Capabilities);
	}

	@Override
	public int addEdge(int source, int target) {
		int e = super.addEdge(source, target);
		edges.get(source).set(target, e);
		edges.get(target).set(source, e);
		return e;
	}

	@Override
	public void removeEdge(int edge) {
		edge = edgeSwapBeforeRemove(edge);
		int u = edgeSource(edge), v = edgeTarget(edge);
		edges.get(u).set(v, EdgeNone);
		edges.get(v).set(u, EdgeNone);
		super.removeEdge(edge);
	}

	@Override
	public void clearEdges() {
		final int m = edges().size();
		for (int e = 0; e < m; e++) {
			int u = edgeSource(e), v = edgeTarget(e);
			edges.get(u).set(v, EdgeNone);
			edges.get(v).set(u, EdgeNone);
		}
		super.clearEdges();
	}

	@Override
	void edgeSwap(int e1, int e2) {
		int u1 = edgeSource(e1), v1 = edgeTarget(e1);
		int u2 = edgeSource(e2), v2 = edgeTarget(e2);
		edges.get(u1).set(v1, e2);
		edges.get(v1).set(u1, e2);
		edges.get(u2).set(v2, e1);
		edges.get(v2).set(u2, e1);
		super.edgeSwap(e1, e2);
	}

	@Override
	void vertexSwap(int v1, int v2) {
		final int tempV = -2;
		for (IntIterator eit1 = edgesOut(v1); eit1.hasNext();)
			replaceEdgeEndpoint(eit1.nextInt(), v1, tempV);
		for (IntIterator eit1 = edgesOut(v2); eit1.hasNext();)
			replaceEdgeEndpoint(eit1.nextInt(), v2, v1);
		for (IntIterator eit1 = edgesOut(v1); eit1.hasNext();)
			replaceEdgeEndpoint(eit1.nextInt(), tempV, v2);
		super.vertexSwap(v1, v2);
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
			return false;
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
