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

import com.jgalgo.graph.EdgeEndpointsContainer.GraphWithEdgeEndpointsContainer;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.JGAlgoUtils;

abstract class GraphMatrixAbstract extends GraphBaseIndexMutable implements GraphWithEdgeEndpointsContainer {

	final DataContainer.Obj<DataContainer.Int> edges;
	private long[] edgeEndpoints;
	private final DataContainer.Long edgeEndpointsContainer;

	static final int EdgeNone = -1;

	private static final DataContainer.Int[] EmptyEdgesArr = new DataContainer.Int[0];

	GraphMatrixAbstract(int expectedVerticesNum, int expectedEdgesNum) {
		super(expectedVerticesNum, expectedEdgesNum);

		edges = new DataContainer.Obj<>(verticesIdStrat, null, EmptyEdgesArr, JGAlgoUtils.consumerNoOp());
		addInternalVerticesContainer(edges);

		edgeEndpointsContainer =
				new DataContainer.Long(edgesIdStrat, EdgeEndpointsContainer.DefVal, newArr -> edgeEndpoints = newArr);
		addInternalEdgesContainer(edgeEndpointsContainer);
	}

	GraphMatrixAbstract(IndexGraph g, boolean copyWeights) {
		super(g, copyWeights);
		final int n = g.vertices().size();

		if (g instanceof GraphMatrixAbstract) {
			GraphMatrixAbstract g0 = (GraphMatrixAbstract) g;
			edges = g0.edges.copy(verticesIdStrat, EmptyEdgesArr, JGAlgoUtils.consumerNoOp());
			addInternalVerticesContainer(edges);
			for (int v = 0; v < n; v++) {
				DataContainer.Int vEdges = edges.get(v).copy(verticesIdStrat, JGAlgoUtils.consumerNoOp());
				addInternalVerticesContainer(vEdges);
				edges.set(v, vEdges);
			}

			edgeEndpointsContainer = g0.edgeEndpointsContainer.copy(edgesIdStrat, newArr -> edgeEndpoints = newArr);
			addInternalEdgesContainer(edgeEndpointsContainer);
		} else {

			edges = new DataContainer.Obj<>(verticesIdStrat, null, EmptyEdgesArr, JGAlgoUtils.consumerNoOp());
			addInternalVerticesContainer(edges);
			for (int v = 0; v < n; v++) {
				DataContainer.Int vEdges = new DataContainer.Int(verticesIdStrat, EdgeNone, JGAlgoUtils.consumerNoOp());
				addInternalVerticesContainer(vEdges);
				edges.set(v, vEdges);

				for (EdgeIter eit = g.outEdges(v).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					vEdges.set(eit.target(), e);
				}
			}

			final int m = edgesIdStrat.size();
			edgeEndpointsContainer = new DataContainer.Long(edgesIdStrat, EdgeEndpointsContainer.DefVal,
					newArr -> edgeEndpoints = newArr);
			addInternalEdgesContainer(edgeEndpointsContainer);
			for (int e = 0; e < m; e++)
				setEndpoints(e, g.edgeSource(e), g.edgeTarget(e));
		}
	}

	@Override
	public int addVertex() {
		int v = super.addVertex();
		DataContainer.Int vEdges = edges.get(v);
		if (vEdges == null) {
			vEdges = new DataContainer.Int(verticesIdStrat, EdgeNone, JGAlgoUtils.consumerNoOp());
			addInternalVerticesContainer(vEdges);
			edges.set(v, vEdges);
		}
		return v;
	}

	@Override
	void removeVertexImpl(int vertex) {
		DataContainer.Int edgesV = edges.get(vertex);
		super.removeVertexImpl(vertex);
		edgesV.clear();
		// Don't deallocate v array
		// edges.clear(v);

		final int n = vertices().size();
		for (int u = 0; u < n; u++)
			edges.get(u).clear(vertex);
	}

	@Override
	void vertexSwap(int v1, int v2) {
		final int n = vertices().size();
		edges.swap(v1, v2);
		for (int u = 0; u < n; u++)
			edges.get(u).swap(v1, v2);
		super.vertexSwap(v1, v2);
	}

	@Override
	public int getEdge(int source, int target) {
		return edges.get(source).getInt(target);
	}

	@Override
	public EdgeSet getEdges(int source, int target) {
		int edge = edges.get(source).getInt(target);
		if (edge == EdgeNone) {
			return Edges.EmptyEdgeSet;
		} else {
			return new Graphs.EdgeSetSourceTargetSingleton(this, source, target, edge);
		}
	}

	@Override
	public int addEdge(int source, int target) {
		if (edges.get(source).getInt(target) != EdgeNone)
			throw new IllegalArgumentException("parallel edges are not supported");
		int e = super.addEdge(source, target);
		setEndpoints(e, source, target);
		return e;
	}

	@Override
	void removeEdgeImpl(int edge) {
		edgeEndpointsContainer.clear(edgeEndpoints, edge);
		super.removeEdgeImpl(edge);
	}

	@Override
	void edgeSwap(int e1, int e2) {
		edgeEndpointsContainer.swap(edgeEndpoints, e1, e2);
		super.edgeSwap(e1, e2);
	}

	void reverseEdge0(int edge) {
		EdgeEndpointsContainer.reverseEdge(edgeEndpoints, edge);
	}

	@Override
	public long[] edgeEndpoints() {
		return edgeEndpoints;
	}

	@Override
	public void clear() {
		clearEdges();
		final int n = vertices().size();
		for (int u = 0; u < n; u++)
			edges.get(u).clear();
		// Don't deallocate edges containers
		// edges.clear();
		super.clear();
	}

	@Override
	public void clearEdges() {
		edgeEndpointsContainer.clear(edgeEndpoints);
		super.clearEdges();
	}

	class EdgeIterOut implements EdgeIter {

		private final int source;
		private int target;
		private int lastTarget = -1;
		private final DataContainer.Int sourceEdges;

		EdgeIterOut(int source) {
			checkVertex(source);
			this.source = source;
			sourceEdges = edges.get(source);

			advanceUntilNext(0);
		}

		@Override
		public boolean hasNext() {
			return target >= 0;
		}

		@Override
		public int nextInt() {
			Assertions.Iters.hasNext(this);
			int e = sourceEdges.getInt(lastTarget = target);
			advanceUntilNext(target + 1);
			return e;
		}

		@Override
		public int peekNext() {
			Assertions.Iters.hasNext(this);
			return sourceEdges.getInt(target);
		}

		void advanceUntilNext(int next) {
			int n = vertices().size();
			for (; next < n; next++) {
				if (sourceEdges.getInt(next) != EdgeNone) {
					target = next;
					return;
				}
			}
			target = -1;
		}

		@Override
		public int source() {
			return source;
		}

		@Override
		public int target() {
			return lastTarget;
		}

		@Override
		public void remove() {
			removeEdge(sourceEdges.getInt(target()));
		}
	}

	class EdgeIterInUndirected implements EdgeIter {

		private int source;
		private final int target;
		private int lastSource = -1;
		private final DataContainer.Int targetEdges;

		EdgeIterInUndirected(int target) {
			checkVertex(target);
			this.target = target;
			targetEdges = edges.get(target);

			advanceUntilNext(0);
		}

		@Override
		public boolean hasNext() {
			return source != -1;
		}

		@Override
		public int nextInt() {
			Assertions.Iters.hasNext(this);
			int e = targetEdges.getInt(lastSource = source);
			advanceUntilNext(source + 1);
			return e;
		}

		@Override
		public int peekNext() {
			Assertions.Iters.hasNext(this);
			return targetEdges.getInt(source);
		}

		private void advanceUntilNext(int next) {
			int n = vertices().size();
			for (; next < n; next++) {
				if (targetEdges.getInt(next) != EdgeNone) {
					source = next;
					return;
				}
			}
			source = -1;
		}

		@Override
		public int source() {
			return lastSource;
		}

		@Override
		public int target() {
			return target;
		}

		@Override
		public void remove() {
			removeEdge(targetEdges.getInt(source()));
		}
	}

	class EdgeIterInDirected implements EdgeIter {

		private int source;
		private final int target;
		private int lastSource = -1;

		EdgeIterInDirected(int target) {
			checkVertex(target);
			this.target = target;

			advanceUntilNext(0);
		}

		@Override
		public boolean hasNext() {
			return source != -1;
		}

		@Override
		public int nextInt() {
			Assertions.Iters.hasNext(this);
			int e = edges.get(lastSource = source).getInt(target);
			advanceUntilNext(source + 1);
			return e;
		}

		@Override
		public int peekNext() {
			Assertions.Iters.hasNext(this);
			return edges.get(source).getInt(target);
		}

		private void advanceUntilNext(int next) {
			int n = vertices().size();
			for (; next < n; next++) {
				if (edges.get(next).getInt(target) != EdgeNone) {
					source = next;
					return;
				}
			}
			source = -1;
		}

		@Override
		public int source() {
			return lastSource;
		}

		@Override
		public int target() {
			return target;
		}

		@Override
		public void remove() {
			removeEdge(edges.get(source()).getInt(target()));
		}
	}

}
