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

	private static final Object DataContainerKeyEdgesOut = new Utils.Obj("edgesOut");
	private static final Object DataContainerKeyEdgesOutNum = new Utils.Obj("edgesOutNum");
	private static final Object DataContainerKeyEdgesIn = new Utils.Obj("edgesIn");
	private static final Object DataContainerKeyEdgesInNum = new Utils.Obj("edgesInNum");

	/**
	 * Create a new graph with no vertices and edges.
	 */
	GraphArrayDirected() {
		this(0, 0);
	}

	/**
	 * Create a new graph with no vertices and edges, with expected number of vertices and edges.
	 *
	 * @param expectedVerticesNum the expected number of vertices that will be in the graph
	 * @param expectedEdgesNum    the expected number of edges that will be in the graph
	 */
	GraphArrayDirected(int expectedVerticesNum, int expectedEdgesNum) {
		super(expectedVerticesNum, expectedEdgesNum);
		edgesOut = new DataContainer.Obj<>(verticesIDStrat, IntArrays.EMPTY_ARRAY, int[].class);
		edgesOutNum = new DataContainer.Int(verticesIDStrat, 0);
		edgesIn = new DataContainer.Obj<>(verticesIDStrat, IntArrays.EMPTY_ARRAY, int[].class);
		edgesInNum = new DataContainer.Int(verticesIDStrat, 0);

		addInternalVerticesDataContainer(DataContainerKeyEdgesOut, edgesOut);
		addInternalVerticesDataContainer(DataContainerKeyEdgesOutNum, edgesOutNum);
		addInternalVerticesDataContainer(DataContainerKeyEdgesIn, edgesIn);
		addInternalVerticesDataContainer(DataContainerKeyEdgesInNum, edgesInNum);
	}

	GraphArrayDirected(GraphArrayDirected g) {
		super(g);
		final int n = g.vertices().size();

		edgesOut = g.edgesOut.copy(verticesIDStrat);
		edgesOutNum = g.edgesOutNum.copy(verticesIDStrat);
		edgesIn = g.edgesIn.copy(verticesIDStrat);
		edgesInNum = g.edgesInNum.copy(verticesIDStrat);
		addInternalVerticesDataContainer(DataContainerKeyEdgesOut, edgesOut);
		addInternalVerticesDataContainer(DataContainerKeyEdgesOutNum, edgesOutNum);
		addInternalVerticesDataContainer(DataContainerKeyEdgesIn, edgesIn);
		addInternalVerticesDataContainer(DataContainerKeyEdgesInNum, edgesInNum);

		for (int v = 0; v < n; v++) {
			edgesOut.set(v, Arrays.copyOf(edgesOut.get(v), edgesOutNum.getInt(v)));
			edgesIn.set(v, Arrays.copyOf(edgesIn.get(v), edgesInNum.getInt(v)));
		}
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
	public EdgeSet edgesOut(int source) {
		checkVertex(source);
		return new EdgeSetOut(source);
	}

	@Override
	public EdgeSet edgesIn(int target) {
		checkVertex(target);
		return new EdgeSetIn(target);
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

	@Override
	public GraphCapabilities getCapabilities() {
		return Capabilities;
	}

	private static final GraphCapabilities Capabilities = GraphCapabilitiesBuilder.newDirected().vertexAdd(true)
			.vertexRemove(true).edgeAdd(true).edgeRemove(true).parallelEdges(true).selfEdges(true).build();

	@Override
	public IndexGraph copy() {
		return new GraphArrayDirected(this);
	}

	private class EdgeSetOut extends GraphBase.EdgeSetOutDirected {
		EdgeSetOut(int source) {
			super(source);
		}

		@Override
		public int size() {
			return edgesOutNum.getInt(source);
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterOut(source, edgesOut.get(source), edgesOutNum.getInt(source));
		}
	}

	private class EdgeSetIn extends GraphBase.EdgeSetInDirected {
		EdgeSetIn(int target) {
			super(target);
		}

		@Override
		public int size() {
			return edgesInNum.getInt(target);
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterIn(target, edgesIn.get(target), edgesInNum.getInt(target));
		}
	}

	private class EdgeIterOut extends EdgeIt {

		private final int u;

		EdgeIterOut(int source, int[] edges, int count) {
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

	private class EdgeIterIn extends EdgeIt {

		private final int target;

		EdgeIterIn(int target, int[] edges, int count) {
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

}
