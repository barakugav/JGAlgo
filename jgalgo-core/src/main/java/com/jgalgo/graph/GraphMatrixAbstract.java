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

	GraphMatrixAbstract(IndexGraphBase.Capabilities capabilities, int expectedVerticesNum, int expectedEdgesNum) {
		super(capabilities, expectedVerticesNum, expectedEdgesNum);

		edges = new DataContainer.Obj<>(vertices, null, EmptyEdgesArr, JGAlgoUtils.consumerNoOp());
		addInternalVerticesContainer(edges);

		edgeEndpointsContainer =
				new DataContainer.Long(super.edges, EdgeEndpointsContainer.DefVal, newArr -> edgeEndpoints = newArr);
		addInternalEdgesContainer(edgeEndpointsContainer);
	}

	GraphMatrixAbstract(IndexGraphBase.Capabilities capabilities, IndexGraph g, boolean copyWeights) {
		super(capabilities, g, copyWeights);
		final int n = g.vertices().size();

		if (g instanceof GraphMatrixAbstract) {
			GraphMatrixAbstract g0 = (GraphMatrixAbstract) g;
			edges = g0.edges.copy(vertices, EmptyEdgesArr, JGAlgoUtils.consumerNoOp());
			addInternalVerticesContainer(edges);
			for (int u = 0; u < n; u++) {
				DataContainer.Int vEdges = edges.get(u).copy(vertices, JGAlgoUtils.consumerNoOp());
				addInternalVerticesContainer(vEdges);
				edges.set(u, vEdges);
			}

			edgeEndpointsContainer = g0.edgeEndpointsContainer.copy(super.edges, newArr -> edgeEndpoints = newArr);
			addInternalEdgesContainer(edgeEndpointsContainer);
		} else {

			edges = new DataContainer.Obj<>(vertices, null, EmptyEdgesArr, JGAlgoUtils.consumerNoOp());
			addInternalVerticesContainer(edges);
			for (int u = 0; u < n; u++) {
				DataContainer.Int vEdges = new DataContainer.Int(vertices, EdgeNone, JGAlgoUtils.consumerNoOp());
				addInternalVerticesContainer(vEdges);
				edges.set(u, vEdges);

				for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.targetInt();
					int existingEdge = vEdges.get(v);
					if (existingEdge != EdgeNone && existingEdge != e)
						throw new IllegalArgumentException("parallel edges are not supported");
					vEdges.set(v, e);
				}
			}

			final int m = edges.size();
			edgeEndpointsContainer = new DataContainer.Long(super.edges, EdgeEndpointsContainer.DefVal,
					newArr -> edgeEndpoints = newArr);
			addInternalEdgesContainer(edgeEndpointsContainer);
			for (int e = 0; e < m; e++)
				setEndpoints(e, g.edgeSource(e), g.edgeTarget(e));
		}
	}

	GraphMatrixAbstract(IndexGraphBase.Capabilities capabilities, IndexGraphBuilderImpl builder) {
		super(capabilities, builder);

		edges = new DataContainer.Obj<>(vertices, null, EmptyEdgesArr, JGAlgoUtils.consumerNoOp());
		addInternalVerticesContainer(edges);
		for (int n = builder.vertices().size(), u = 0; u < n; u++) {
			DataContainer.Int vEdges = new DataContainer.Int(vertices, EdgeNone, JGAlgoUtils.consumerNoOp());
			addInternalVerticesContainer(vEdges);
			edges.set(u, vEdges);
		}

		assert builder instanceof IndexGraphBuilderImpl.Directed || builder instanceof IndexGraphBuilderImpl.Undirected;
		boolean directed = builder instanceof IndexGraphBuilderImpl.Directed;
		if (directed) {
			for (int m = builder.edges().size(), e = 0; e < m; e++) {
				int source = builder.edgeSource(e), target = builder.edgeTarget(e);
				DataContainer.Int uEdges = edges.get(source);
				int existingEdge = uEdges.get(target);
				if (existingEdge != EdgeNone)
					throw new IllegalArgumentException("parallel edges are not supported");
				uEdges.set(target, e);
			}

		} else {
			for (int m = builder.edges().size(), e = 0; e < m; e++) {
				int source = builder.edgeSource(e), target = builder.edgeTarget(e);
				DataContainer.Int uEdges = edges.get(source);
				DataContainer.Int vEdges = edges.get(target);
				int existingEdge1 = uEdges.get(target);
				int existingEdge2 = vEdges.get(source);
				if (existingEdge1 != EdgeNone || existingEdge2 != EdgeNone)
					throw new IllegalArgumentException("parallel edges are not supported");
				uEdges.set(target, e);
				uEdges.set(source, e);
			}
		}

		edgeEndpointsContainer =
				new DataContainer.Long(super.edges, EdgeEndpointsContainer.DefVal, newArr -> edgeEndpoints = newArr);
		addInternalEdgesContainer(edgeEndpointsContainer);
		for (int m = builder.edges().size(), e = 0; e < m; e++)
			setEndpoints(e, builder.edgeSource(e), builder.edgeTarget(e));
	}

	@Override
	public int addVertex() {
		int v = super.addVertex();
		DataContainer.Int vEdges = edges.get(v);
		if (vEdges == null) {
			vEdges = new DataContainer.Int(vertices, EdgeNone, JGAlgoUtils.consumerNoOp());
			addInternalVerticesContainer(vEdges);
			edges.set(v, vEdges);
		}
		return v;
	}

	@Override
	void removeVertexLast(int vertex) {
		DataContainer.Int edgesV = edges.get(vertex);
		super.removeVertexLast(vertex);
		edgesV.clear();
		// Don't deallocate v array
		// edges.clear(v);

		final int n = vertices().size();
		for (int u = 0; u < n; u++)
			edges.get(u).clear(vertex);
	}

	@Override
	void vertexSwapAndRemove(int removedIdx, int swappedIdx) {
		for (int n = vertices().size(), u = 0; u < n; u++)
			edges.get(u).swapAndClear(removedIdx, swappedIdx);
		edges.swapAndClear(removedIdx, swappedIdx);
		super.vertexSwapAndRemove(removedIdx, swappedIdx);
	}

	@Override
	public int getEdge(int source, int target) {
		return edges.get(source).get(target);
	}

	@Override
	public IEdgeSet getEdges(int source, int target) {
		int edge = edges.get(source).get(target);
		return new Graphs.EdgeSetSourceTargetSingleton(this, source, target, edge);
	}

	@Override
	public int addEdge(int source, int target) {
		if (edges.get(source).get(target) != EdgeNone)
			throw new IllegalArgumentException("parallel edges are not supported");
		int e = super.addEdge(source, target);
		setEndpoints(e, source, target);
		return e;
	}

	@Override
	void removeEdgeLast(int edge) {
		edgeEndpointsContainer.clear(edgeEndpoints, edge);
		super.removeEdgeLast(edge);
	}

	@Override
	void edgeSwapAndRemove(int removedIdx, int swappedIdx) {
		edgeEndpointsContainer.swapAndClear(removedIdx, swappedIdx);
		super.edgeSwapAndRemove(removedIdx, swappedIdx);
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

	class EdgeIterOut implements IEdgeIter {

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
			int e = sourceEdges.get(lastTarget = target);
			advanceUntilNext(target + 1);
			return e;
		}

		@Override
		public int peekNextInt() {
			Assertions.Iters.hasNext(this);
			return sourceEdges.get(target);
		}

		void advanceUntilNext(int next) {
			int n = vertices().size();
			for (; next < n; next++) {
				if (sourceEdges.get(next) != EdgeNone) {
					target = next;
					return;
				}
			}
			target = -1;
		}

		@Override
		public int sourceInt() {
			return source;
		}

		@Override
		public int targetInt() {
			return lastTarget;
		}

		@Override
		public void remove() {
			removeEdge(sourceEdges.get(targetInt()));
		}
	}

	class EdgeIterInUndirected implements IEdgeIter {

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
			int e = targetEdges.get(lastSource = source);
			advanceUntilNext(source + 1);
			return e;
		}

		@Override
		public int peekNextInt() {
			Assertions.Iters.hasNext(this);
			return targetEdges.get(source);
		}

		private void advanceUntilNext(int next) {
			int n = vertices().size();
			for (; next < n; next++) {
				if (targetEdges.get(next) != EdgeNone) {
					source = next;
					return;
				}
			}
			source = -1;
		}

		@Override
		public int sourceInt() {
			return lastSource;
		}

		@Override
		public int targetInt() {
			return target;
		}

		@Override
		public void remove() {
			removeEdge(targetEdges.get(sourceInt()));
		}
	}

	class EdgeIterInDirected implements IEdgeIter {

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
			int e = edges.get(lastSource = source).get(target);
			advanceUntilNext(source + 1);
			return e;
		}

		@Override
		public int peekNextInt() {
			Assertions.Iters.hasNext(this);
			return edges.get(source).get(target);
		}

		private void advanceUntilNext(int next) {
			int n = vertices().size();
			for (; next < n; next++) {
				if (edges.get(next).get(target) != EdgeNone) {
					source = next;
					return;
				}
			}
			source = -1;
		}

		@Override
		public int sourceInt() {
			return lastSource;
		}

		@Override
		public int targetInt() {
			return target;
		}

		@Override
		public void remove() {
			removeEdge(edges.get(sourceInt()).get(targetInt()));
		}
	}

}
