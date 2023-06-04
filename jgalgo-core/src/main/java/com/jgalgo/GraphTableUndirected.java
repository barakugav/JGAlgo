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

import com.jgalgo.GraphsUtils.GraphCapabilitiesBuilder;

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
class GraphTableUndirected extends GraphTableAbstract {

	GraphTableUndirected() {
		this(0, 0);
	}

	/**
	 * Create a new graph with no vertices and edges, with expected number of vertices and edges.
	 *
	 * @param expectedVerticesNum the expected number of vertices that will be in the graph
	 * @param expectedEdgesNum    the expected number of edges that will be in the graph
	 */
	GraphTableUndirected(int expectedVerticesNum, int expectedEdgesNum) {
		super(expectedVerticesNum, expectedEdgesNum);
	}

	GraphTableUndirected(GraphTableUndirected g) {
		super(g);
	}

	@Override
	public int addEdge(int source, int target) {
		int e = super.addEdge(source, target);
		edges.get(source).set(target, e);
		edges.get(target).set(source, e);
		return e;
	}

	@Override
	void removeEdgeImpl(int edge) {
		int u = edgeSource(edge), v = edgeTarget(edge);
		edges.get(u).set(v, EdgeNone);
		edges.get(v).set(u, EdgeNone);
		super.removeEdgeImpl(edge);
	}

	@Override
	public void removeEdgesOf(int source) {
		for (EdgeIter eit = edgesOut(source).iterator(); eit.hasNext();) {
			eit.nextInt();
			eit.remove();
		}
	}

	@Override
	public void removeEdgesOutOf(int source) {
		removeEdgesOf(source);
	}

	@Override
	public void removeEdgesInOf(int target) {
		removeEdgesOf(target);
	}

	@Override
	public void reverseEdge(int edge) {
		// Do nothing
	}

	@Override
	public EdgeSet edgesOut(int source) {
		return new EdgeSetOut(source);
	}

	@Override
	public EdgeSet edgesIn(int target) {
		return new EdgeSetIn(target);
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
		for (int e : edgesOut(v1))
			replaceEdgeEndpoint(e, v1, tempV);
		for (int e : edgesOut(v2))
			replaceEdgeEndpoint(e, v2, v1);
		for (int e : edgesOut(v1))
			replaceEdgeEndpoint(e, tempV, v2);
		super.vertexSwap(v1, v2);
	}

	@Override
	public GraphCapabilities getCapabilities() {
		return Capabilities;
	}

	private static final GraphCapabilities Capabilities = GraphCapabilitiesBuilder.newUndirected().vertexAdd(true)
			.vertexRemove(true).edgeAdd(true).edgeRemove(true).parallelEdges(false).selfEdges(false).build();

	@Override
	public IndexGraph copy() {
		return new GraphTableUndirected(this);
	}

	private class EdgeSetOut extends GraphBase.EdgeSetOutUndirected {
		EdgeSetOut(int source) {
			super(source);
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterOut(source);
		}
	}

	private class EdgeSetIn extends GraphBase.EdgeSetInUndirected {
		EdgeSetIn(int target) {
			super(target);
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterInUndirected(target);
		}
	}

}
