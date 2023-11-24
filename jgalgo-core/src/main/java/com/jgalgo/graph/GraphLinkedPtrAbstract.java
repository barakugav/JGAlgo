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

abstract class GraphLinkedPtrAbstract extends GraphBaseMutable {

	private Edge[] edges;
	private final DataContainer.Obj<Edge> edgesContainer;
	private static final Edge[] EmptyEdgeArr = new Edge[0];

	GraphLinkedPtrAbstract(GraphBaseMutable.Capabilities capabilities, int expectedVerticesNum, int expectedEdgesNum) {
		super(capabilities, expectedVerticesNum, expectedEdgesNum);
		edgesContainer = new DataContainer.Obj<>(super.edges, null, EmptyEdgeArr, newArr -> edges = newArr);
		addInternalEdgesContainer(edgesContainer);
	}

	GraphLinkedPtrAbstract(GraphBaseMutable.Capabilities capabilities, IndexGraph g, boolean copyVerticesWeights,
			boolean copyEdgesWeights) {
		super(capabilities, g, copyVerticesWeights, copyEdgesWeights);
		edgesContainer = new DataContainer.Obj<>(super.edges, null, EmptyEdgeArr, newArr -> edges = newArr);
		addInternalEdgesContainer(edgesContainer);
		final int m = g.edges().size();
		for (int e = 0; e < m; e++) {
			edges[e] = allocEdge(e);
			setEndpoints(e, g.edgeSource(e), g.edgeTarget(e));
		}
	}

	GraphLinkedPtrAbstract(GraphBaseMutable.Capabilities capabilities, IndexGraphBuilderImpl builder) {
		super(capabilities, builder);
		edgesContainer = new DataContainer.Obj<>(super.edges, null, EmptyEdgeArr, newArr -> edges = newArr);
		addInternalEdgesContainer(edgesContainer);

		for (int m = super.edges.size(), e = 0; e < m; e++) {
			edges[e] = allocEdge(e);
			setEndpoints(e, builder.edgeSource(e), builder.edgeTarget(e));
		}
	}

	Edge getEdge(int edge) {
		Edge n = edges[edge];
		assert n.id == edge;
		return n;
	}

	@Override
	void removeEdgeLast(int edge) {
		edges[edge] = null;
		super.removeEdgeLast(edge);
	}

	@Override
	void edgeSwapAndRemove(int removedIdx, int swappedIdx) {
		getEdge(swappedIdx).id = removedIdx;
		swapAndClear(edges, removedIdx, swappedIdx, null);
		super.edgeSwapAndRemove(removedIdx, swappedIdx);
	}

	Edge addEdgeObj(int source, int target) {
		int e = super.addEdge(source, target);
		Edge n = allocEdge(e);
		edges[e] = n;
		return n;
	}

	abstract Edge allocEdge(int id);

	@Override
	public void clearEdges() {
		edgesContainer.clear();
		super.clearEdges();
	}

	abstract class EdgeItr implements IEdgeIter {

		private Edge next;
		Edge last;

		EdgeItr(Edge p) {
			this.next = p;
		}

		abstract Edge nextEdge(Edge n);

		@Override
		public boolean hasNext() {
			return next != null;
		}

		@Override
		public int nextInt() {
			Assertions.Iters.hasNext(this);
			next = nextEdge(last = next);
			return last.id;
		}

		@Override
		public int peekNextInt() {
			Assertions.Iters.hasNext(this);
			return next.id;
		}

		@Override
		public void remove() {
			if (last == null)
				throw new IllegalStateException();
			removeEdge(last.id);
			last = null;
		}
	}

	abstract static class Edge {

		int id;

		Edge(int id) {
			this.id = id;
		}

	}

}
