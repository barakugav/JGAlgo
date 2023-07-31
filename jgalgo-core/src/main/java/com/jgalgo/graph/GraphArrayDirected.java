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

import java.util.Arrays;
import com.jgalgo.graph.Graphs.GraphCapabilitiesBuilder;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntBigArrays;

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

	private int[][] edgesOut;
	private int[] edgesOutNum;
	private int[][] edgesIn;
	private int[] edgesInNum;
	private final DataContainer.Obj<int[]> edgesOutContainer;
	private final DataContainer.Int edgesOutNumContainer;
	private final DataContainer.Obj<int[]> edgesInContainer;
	private final DataContainer.Int edgesInNumContainer;

	/**
	 * Create a new graph with no vertices and edges, with expected number of vertices and edges.
	 *
	 * @param expectedVerticesNum the expected number of vertices that will be in the graph
	 * @param expectedEdgesNum    the expected number of edges that will be in the graph
	 */
	GraphArrayDirected(int expectedVerticesNum, int expectedEdgesNum) {
		super(expectedVerticesNum, expectedEdgesNum);
		edgesOutContainer = new DataContainer.Obj<>(verticesIdStrat, IntArrays.EMPTY_ARRAY,
				IntBigArrays.EMPTY_BIG_ARRAY, newArr -> edgesOut = newArr);
		edgesOutNumContainer = new DataContainer.Int(verticesIdStrat, 0, newArr -> edgesOutNum = newArr);
		edgesInContainer = new DataContainer.Obj<>(verticesIdStrat, IntArrays.EMPTY_ARRAY, IntBigArrays.EMPTY_BIG_ARRAY,
				newArr -> edgesIn = newArr);
		edgesInNumContainer = new DataContainer.Int(verticesIdStrat, 0, newArr -> edgesInNum = newArr);

		addInternalVerticesContainer(edgesOutContainer);
		addInternalVerticesContainer(edgesOutNumContainer);
		addInternalVerticesContainer(edgesInContainer);
		addInternalVerticesContainer(edgesInNumContainer);
	}

	GraphArrayDirected(IndexGraph g, boolean copyWeights) {
		super(g, copyWeights);
		final int n = g.vertices().size();

		if (g instanceof GraphArrayDirected) {
			GraphArrayDirected g0 = (GraphArrayDirected) g;

			edgesOutContainer = g0.edgesOutContainer.copy(verticesIdStrat, IntBigArrays.EMPTY_BIG_ARRAY,
					newArr -> edgesOut = newArr);
			edgesOutNumContainer = g0.edgesOutNumContainer.copy(verticesIdStrat, newArr -> edgesOutNum = newArr);
			edgesInContainer =
					g0.edgesInContainer.copy(verticesIdStrat, IntBigArrays.EMPTY_BIG_ARRAY, newArr -> edgesIn = newArr);
			edgesInNumContainer = g0.edgesInNumContainer.copy(verticesIdStrat, newArr -> edgesInNum = newArr);

			addInternalVerticesContainer(edgesOutContainer);
			addInternalVerticesContainer(edgesOutNumContainer);
			addInternalVerticesContainer(edgesInContainer);
			addInternalVerticesContainer(edgesInNumContainer);

			for (int v = 0; v < n; v++) {
				edgesOut[v] = Arrays.copyOf(edgesOut[v], edgesOutNum[v]);
				edgesIn[v] = Arrays.copyOf(edgesIn[v], edgesInNum[v]);
			}
		} else {
			edgesOutContainer = new DataContainer.Obj<>(verticesIdStrat, IntArrays.EMPTY_ARRAY,
					IntBigArrays.EMPTY_BIG_ARRAY, newArr -> edgesOut = newArr);
			edgesOutNumContainer = new DataContainer.Int(verticesIdStrat, 0, newArr -> edgesOutNum = newArr);
			edgesInContainer = new DataContainer.Obj<>(verticesIdStrat, IntArrays.EMPTY_ARRAY,
					IntBigArrays.EMPTY_BIG_ARRAY, newArr -> edgesIn = newArr);
			edgesInNumContainer = new DataContainer.Int(verticesIdStrat, 0, newArr -> edgesInNum = newArr);

			addInternalVerticesContainer(edgesOutContainer);
			addInternalVerticesContainer(edgesOutNumContainer);
			addInternalVerticesContainer(edgesInContainer);
			addInternalVerticesContainer(edgesInNumContainer);

			for (int v = 0; v < n; v++) {
				EdgeSet outEdges = g.outEdges(v);
				int outEdgesSize = edgesOutNum[v] = outEdges.size();
				if (outEdgesSize != 0) {
					int[] edges = edgesOut[v] = new int[outEdgesSize];
					int i = 0;
					for (int e : outEdges)
						edges[i++] = e;
				}
				EdgeSet inEdges = g.inEdges(v);
				int inEdgesSize = edgesInNum[v] = inEdges.size();
				if (inEdgesSize != 0) {
					int[] edges = edgesIn[v] = new int[inEdgesSize];
					int i = 0;
					for (int e : inEdges)
						edges[i++] = e;
				}
			}
		}
	}

	GraphArrayDirected(IndexGraphBuilderImpl.Directed builder) {
		super(builder);
		edgesOutContainer = new DataContainer.Obj<>(verticesIdStrat, IntArrays.EMPTY_ARRAY,
				IntBigArrays.EMPTY_BIG_ARRAY, newArr -> edgesOut = newArr);
		edgesOutNumContainer = new DataContainer.Int(verticesIdStrat, 0, newArr -> edgesOutNum = newArr);
		edgesInContainer = new DataContainer.Obj<>(verticesIdStrat, IntArrays.EMPTY_ARRAY, IntBigArrays.EMPTY_BIG_ARRAY,
				newArr -> edgesIn = newArr);
		edgesInNumContainer = new DataContainer.Int(verticesIdStrat, 0, newArr -> edgesInNum = newArr);

		addInternalVerticesContainer(edgesOutContainer);
		addInternalVerticesContainer(edgesOutNumContainer);
		addInternalVerticesContainer(edgesInContainer);
		addInternalVerticesContainer(edgesInNumContainer);

		final int m = builder.edges().size();
		for (int e = 0; e < m; e++) {
			int source = builder.endpoints[e * 2 + 0];
			int target = builder.endpoints[e * 2 + 1];
			addEdgeToList(edgesOut, edgesOutNum, source, e);
			addEdgeToList(edgesIn, edgesInNum, target, e);
		}
	}

	@Override
	void removeVertexImpl(int vertex) {
		super.removeVertexImpl(vertex);
		edgesOutNumContainer.clear(edgesOutNum, vertex);
		edgesInNumContainer.clear(edgesInNum, vertex);
		// Reuse allocated edges arrays for v
		// edgesOut.clear(v);
		// edgesIn.clear(v);
	}

	@Override
	void vertexSwap(int v1, int v2) {
		int[] es1Out = edgesOut[v1];
		int es1OutLen = edgesOutNum[v1];
		for (int i = 0; i < es1OutLen; i++)
			replaceEdgeSource(es1Out[i], v2);

		int[] es1In = edgesIn[v1];
		int es1InLen = edgesInNum[v1];
		for (int i = 0; i < es1InLen; i++)
			replaceEdgeTarget(es1In[i], v2);

		int[] es2Out = edgesOut[v2];
		int es2OutLen = edgesOutNum[v2];
		for (int i = 0; i < es2OutLen; i++)
			replaceEdgeSource(es2Out[i], v1);

		int[] es2In = edgesIn[v2];
		int es2InLen = edgesInNum[v2];
		for (int i = 0; i < es2InLen; i++)
			replaceEdgeTarget(es2In[i], v1);

		edgesOutContainer.swap(edgesOut, v1, v2);
		edgesOutNumContainer.swap(edgesOutNum, v1, v2);
		edgesInContainer.swap(edgesIn, v1, v2);
		edgesInNumContainer.swap(edgesInNum, v1, v2);

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
		addEdgeToList(edgesOut, edgesOutNum, source, e);
		addEdgeToList(edgesIn, edgesInNum, target, e);
		return e;
	}

	@Override
	void removeEdgeImpl(int edge) {
		int u = edgeSource(edge), v = edgeTarget(edge);
		removeEdgeFromList(edgesOut, edgesOutNum, u, edge);
		removeEdgeFromList(edgesIn, edgesInNum, v, edge);
		super.removeEdgeImpl(edge);
	}

	@Override
	void edgeSwap(int e1, int e2) {
		assert e1 != e2;
		int u1 = edgeSource(e1), v1 = edgeTarget(e1);
		int u2 = edgeSource(e2), v2 = edgeTarget(e2);
		int[] u1es = edgesOut[u1], v1es = edgesIn[v1];
		int[] u2es = edgesOut[u2], v2es = edgesIn[v2];
		int i1 = edgeIndexOf(u1es, edgesOutNum[u1], e1);
		int j1 = edgeIndexOf(v1es, edgesInNum[v1], e1);
		int i2 = edgeIndexOf(u2es, edgesOutNum[u2], e2);
		int j2 = edgeIndexOf(v2es, edgesInNum[v2], e2);
		u1es[i1] = e2;
		v1es[j1] = e2;
		u2es[i2] = e1;
		v2es[j2] = e1;
		super.edgeSwap(e1, e2);
	}

	@Override
	public void removeOutEdgesOf(int source) {
		checkVertex(source);
		while (edgesOutNum[source] > 0)
			removeEdge(edgesOut[source][0]);
	}

	@Override
	public void removeInEdgesOf(int target) {
		checkVertex(target);
		while (edgesInNum[target] > 0)
			removeEdge(edgesIn[target][0]);
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
		edgesOutNumContainer.clear(edgesOutNum);
		edgesInNumContainer.clear(edgesInNum);
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
			GraphCapabilitiesBuilder.newDirected().parallelEdges(true).selfEdges(true).build();

	private class EdgeSetOut extends GraphBase.EdgeSetOutDirected {
		EdgeSetOut(int source) {
			super(source);
		}

		@Override
		public int size() {
			return edgesOutNum[source];
		}

		@Override
		public boolean isEmpty() {
			return size() == 0;
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterOut(source, edgesOut[source], edgesOutNum[source]);
		}
	}

	private class EdgeSetIn extends GraphBase.EdgeSetInDirected {
		EdgeSetIn(int target) {
			super(target);
		}

		@Override
		public int size() {
			return edgesInNum[target];
		}

		@Override
		public boolean isEmpty() {
			return size() == 0;
		}

		@Override
		public EdgeIter iterator() {
			return new EdgeIterIn(target, edgesIn[target], edgesInNum[target]);
		}
	}

	private class EdgeIterOut extends GraphArrayAbstract.EdgeIterOut {

		EdgeIterOut(int source, int[] edges, int count) {
			super(source, edges, count);
		}

		@Override
		public int target() {
			return edgeTarget(lastEdge);
		}
	}

	private class EdgeIterIn extends GraphArrayAbstract.EdgeIterIn {

		EdgeIterIn(int target, int[] edges, int count) {
			super(target, edges, count);
		}

		@Override
		public int source() {
			return edgeSource(lastEdge);
		}
	}

}
