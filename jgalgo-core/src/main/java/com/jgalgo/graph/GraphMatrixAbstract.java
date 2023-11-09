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

import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.JGAlgoUtils;

abstract class GraphMatrixAbstract extends GraphBaseWithEdgeEndpointsContainer {

	final DataContainer.Obj<DataContainer.Int> edges;

	static final int EdgeNone = -1;

	private static final DataContainer.Int[] EmptyEdgesArr = new DataContainer.Int[0];

	GraphMatrixAbstract(IndexGraphBase.Capabilities capabilities, int expectedVerticesNum, int expectedEdgesNum) {
		super(capabilities, expectedVerticesNum, expectedEdgesNum);

		edges = new DataContainer.Obj<>(vertices, null, EmptyEdgesArr, JGAlgoUtils.consumerNoOp());
		addInternalVerticesContainer(edges);
	}

	GraphMatrixAbstract(IndexGraphBase.Capabilities capabilities, IndexGraph g, boolean copyWeights) {
		super(capabilities, g, copyWeights);
		final int n = g.vertices().size();

		if (g instanceof GraphMatrixAbstract) {
			GraphMatrixAbstract g0 = (GraphMatrixAbstract) g;
			edges = g0.edges.copy(vertices, EmptyEdgesArr, JGAlgoUtils.consumerNoOp());
			addInternalVerticesContainer(edges);
			for (int u = 0; u < n; u++) {
				DataContainer.Int vEdges = edges.data[u].copy(vertices, JGAlgoUtils.consumerNoOp());
				addInternalVerticesContainer(vEdges);
				edges.data[u] = vEdges;
			}
		} else {

			edges = new DataContainer.Obj<>(vertices, null, EmptyEdgesArr, JGAlgoUtils.consumerNoOp());
			addInternalVerticesContainer(edges);
			for (int u = 0; u < n; u++) {
				DataContainer.Int vEdges = new DataContainer.Int(vertices, EdgeNone, JGAlgoUtils.consumerNoOp());
				addInternalVerticesContainer(vEdges);
				edges.data[u] = vEdges;

				for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.targetInt();
					int existingEdge = vEdges.data[v];
					if (existingEdge != EdgeNone && existingEdge != e)
						throw new IllegalArgumentException("parallel edges are not supported");
					vEdges.data[v] = e;
				}
			}
		}
	}

	GraphMatrixAbstract(IndexGraphBase.Capabilities capabilities, IndexGraphBuilderImpl builder) {
		super(capabilities, builder);

		edges = new DataContainer.Obj<>(vertices, null, EmptyEdgesArr, JGAlgoUtils.consumerNoOp());
		addInternalVerticesContainer(edges);
		for (int n = builder.vertices().size(), u = 0; u < n; u++) {
			DataContainer.Int vEdges = new DataContainer.Int(vertices, EdgeNone, JGAlgoUtils.consumerNoOp());
			addInternalVerticesContainer(vEdges);
			edges.data[u] = vEdges;
		}

		assert builder instanceof IndexGraphBuilderImpl.Directed || builder instanceof IndexGraphBuilderImpl.Undirected;
		boolean directed = builder instanceof IndexGraphBuilderImpl.Directed;
		if (directed) {
			for (int m = builder.edges().size(), e = 0; e < m; e++) {
				int source = builder.edgeSource(e), target = builder.edgeTarget(e);
				DataContainer.Int uEdges = edges.data[source];
				int existingEdge = uEdges.data[target];
				if (existingEdge != EdgeNone)
					throw new IllegalArgumentException("parallel edges are not supported");
				uEdges.data[target] = e;
			}

		} else {
			for (int m = builder.edges().size(), e = 0; e < m; e++) {
				int source = builder.edgeSource(e), target = builder.edgeTarget(e);
				DataContainer.Int uEdges = edges.data[source];
				DataContainer.Int vEdges = edges.data[target];
				int existingEdge1 = uEdges.data[target];
				int existingEdge2 = vEdges.data[source];
				if (existingEdge1 != EdgeNone || existingEdge2 != EdgeNone)
					throw new IllegalArgumentException("parallel edges are not supported");
				uEdges.data[target] = e;
				uEdges.data[source] = e;
			}
		}
	}

	@Override
	public int addVertex() {
		int v = super.addVertex();
		DataContainer.Int vEdges = edges.data[v];
		if (vEdges == null) {
			vEdges = new DataContainer.Int(vertices, EdgeNone, JGAlgoUtils.consumerNoOp());
			addInternalVerticesContainer(vEdges);
			edges.data[v] = vEdges;
		}
		return v;
	}

	@Override
	void removeVertexLast(int vertex) {
		DataContainer.Int edgesV = edges.data[vertex];
		super.removeVertexLast(vertex);
		edgesV.clear();
		// Don't deallocate v array
		// edges.clear(v);

		final int n = vertices().size();
		for (int u = 0; u < n; u++)
			clear(edges.data[u].data, vertex, EdgeNone);
	}

	@Override
	void vertexSwapAndRemove(int removedIdx, int swappedIdx) {
		for (int n = vertices().size(), u = 0; u < n; u++)
			swapAndClear(edges.data[u].data, removedIdx, swappedIdx, EdgeNone);
		swapAndClear(edges.data, removedIdx, swappedIdx, null);
		super.vertexSwapAndRemove(removedIdx, swappedIdx);
	}

	@Override
	public int getEdge(int source, int target) {
		return edges.data[source].data[target];
	}

	@Override
	public IEdgeSet getEdges(int source, int target) {
		int edge = edges.data[source].data[target];
		return new Graphs.EdgeSetSourceTargetSingleton(this, source, target, edge);
	}

	@Override
	public int addEdge(int source, int target) {
		if (edges.data[source].data[target] != EdgeNone)
			throw new IllegalArgumentException("parallel edges are not supported");
		int e = super.addEdge(source, target);
		setEndpoints(e, source, target);
		return e;
	}

	@Override
	public void clear() {
		clearEdges();
		final int n = vertices().size();
		for (int u = 0; u < n; u++)
			edges.data[u].clear();
		// Don't deallocate edges containers
		// edges.clear();
		super.clear();
	}

	class EdgeIterOut implements IEdgeIter {

		private final int source;
		private int target;
		private int lastTarget = -1;
		private final DataContainer.Int sourceEdges;

		EdgeIterOut(int source) {
			checkVertex(source);
			this.source = source;
			sourceEdges = edges.data[source];

			advanceUntilNext(0);
		}

		@Override
		public boolean hasNext() {
			return target >= 0;
		}

		@Override
		public int nextInt() {
			Assertions.Iters.hasNext(this);
			int e = sourceEdges.data[lastTarget = target];
			advanceUntilNext(target + 1);
			return e;
		}

		@Override
		public int peekNextInt() {
			Assertions.Iters.hasNext(this);
			return sourceEdges.data[target];
		}

		void advanceUntilNext(int next) {
			int n = vertices().size();
			for (; next < n; next++) {
				if (sourceEdges.data[next] != EdgeNone) {
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
			removeEdge(sourceEdges.data[targetInt()]);
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
			targetEdges = edges.data[target];

			advanceUntilNext(0);
		}

		@Override
		public boolean hasNext() {
			return source != -1;
		}

		@Override
		public int nextInt() {
			Assertions.Iters.hasNext(this);
			int e = targetEdges.data[lastSource = source];
			advanceUntilNext(source + 1);
			return e;
		}

		@Override
		public int peekNextInt() {
			Assertions.Iters.hasNext(this);
			return targetEdges.data[source];
		}

		private void advanceUntilNext(int next) {
			int n = vertices().size();
			for (; next < n; next++) {
				if (targetEdges.data[next] != EdgeNone) {
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
			removeEdge(targetEdges.data[sourceInt()]);
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
			int e = edges.data[lastSource = source].data[target];
			advanceUntilNext(source + 1);
			return e;
		}

		@Override
		public int peekNextInt() {
			Assertions.Iters.hasNext(this);
			return edges.data[source].data[target];
		}

		private void advanceUntilNext(int next) {
			int n = vertices().size();
			for (; next < n; next++) {
				if (edges.data[next].data[target] != EdgeNone) {
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
			removeEdge(edges.data[sourceInt()].data[targetInt()]);
		}
	}

}
