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

import java.util.NoSuchElementException;
import com.jgalgo.EdgeEndpointsContainer.GraphWithEdgeEndpointsContainer;

abstract class GraphTableAbstract extends GraphBaseContinues implements GraphWithEdgeEndpointsContainer {

	final DataContainer.Obj<DataContainer.Int> edges;
	private final EdgeEndpointsContainer edgeEndpoints;

	static final int EdgeNone = -1;

	private static final Object DataContainerKeyEdges = new Utils.Obj("edges");
	private static final Object DataContainerKeyEdgeEndpoints = new Utils.Obj("edgeEndpoints");

	GraphTableAbstract(int n) {
		super(n);

		edges = new DataContainer.Obj<>(verticesIDStrategy, null, DataContainer.Int.class);
		addInternalVerticesDataContainer(DataContainerKeyEdges, edges);
		for (int u = 0; u < n; u++) {
			DataContainer.Int uEdges = new DataContainer.Int(verticesIDStrategy, EdgeNone);
			edges.set(u, uEdges);
			addInternalVerticesDataContainer(new Utils.Obj("perVertexEdges"), uEdges);
		}

		edgeEndpoints = new EdgeEndpointsContainer(edgesIDStrategy);
		addInternalEdgesDataContainer(DataContainerKeyEdgeEndpoints, edgeEndpoints);
	}

	GraphTableAbstract(GraphTableAbstract g) {
		super(g);

		final int n = g.vertices().size();
		edges = g.edges.copy(verticesIDStrategy);
		addInternalVerticesDataContainer(DataContainerKeyEdges, edges);
		for (int u = 0; u < n; u++) {
			DataContainer.Int uEdges = edges.get(u).copy(verticesIDStrategy);
			edges.set(u, uEdges);
			addInternalVerticesDataContainer(new Utils.Obj("perVertexEdges"), uEdges);
		}

		edgeEndpoints = g.edgeEndpoints.copy(edgesIDStrategy);
		addInternalEdgesDataContainer(DataContainerKeyEdgeEndpoints, edgeEndpoints);
	}

	@Override
	public int addVertex() {
		int v = super.addVertex();
		DataContainer.Int vEdges = edges.get(v);
		if (vEdges == null) {
			vEdges = new DataContainer.Int(verticesIDStrategy, EdgeNone);
			addInternalVerticesDataContainer(new Utils.Obj("perVertexEdges"), vEdges);
			edges.set(v, vEdges);
		}
		return v;
	}

	@Override
	public void removeVertex(int vertex) {
		vertex = vertexSwapBeforeRemove(vertex);
		DataContainer.Int edgesV = edges.get(vertex);
		super.removeVertex(vertex);
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
	public EdgeIter edgesOut(int source) {
		return new EdgeIterOut(source);
	}

	@Override
	public int getEdge(int source, int target) {
		return edges.get(source).getInt(target);
	}

	@Override
	public EdgeIter getEdges(int source, int target) {
		int e = edges.get(source).getInt(target);
		if (e == EdgeNone) {
			return EdgeIterImpl.Empty;
		} else {
			return new EdgeIterImpl() {

				boolean beforeNext = true;

				@Override
				public boolean hasNext() {
					return beforeNext;
				}

				@Override
				public int nextInt() {
					if (!hasNext())
						throw new NoSuchElementException();
					beforeNext = false;
					return e;
				}

				@Override
				public int peekNext() {
					if (!hasNext())
						throw new NoSuchElementException();
					return e;
				}

				@Override
				public int source() {
					return source;
				}

				@Override
				public int target() {
					return target;
				}

				@Override
				public void remove() {
					if (beforeNext)
						throw new IllegalStateException();
					int e = edges.get(source).getInt(target);
					if (e == EdgeNone)
						throw new IllegalStateException();
					removeEdge(e);
				}
			};
		}
	}

	@Override
	public int addEdge(int source, int target) {
		if (edges.get(source).getInt(target) != EdgeNone)
			throw new IllegalArgumentException("parallel edges are not supported");
		int e = super.addEdge(source, target);
		edgeEndpoints.setEndpoints(e, source, target);
		return e;
	}

	@Override
	public void removeEdge(int edge) {
		edge = edgeSwapBeforeRemove(edge);
		edgeEndpoints.clear(edge);
		super.removeEdge(edge);
	}

	@Override
	void edgeSwap(int e1, int e2) {
		edgeEndpoints.swap(e1, e2);
		super.edgeSwap(e1, e2);
	}

	void reverseEdge0(int edge) {
		edgeEndpoints.reverseEdge(edge);
	}

	@Override
	public EdgeEndpointsContainer edgeEndpoints() {
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
		edgeEndpoints.clear();
		super.clearEdges();
	}

	class EdgeIterOut implements EdgeIterImpl {

		private final int source;
		private int v;
		private int lastV = -1;
		private final DataContainer.Int uEdges;

		EdgeIterOut(int source) {
			checkVertex(source);
			this.source = source;
			uEdges = edges.get(source);

			advanceUntilNext(0);
		}

		@Override
		public boolean hasNext() {
			return v >= 0;
		}

		@Override
		public int nextInt() {
			if (!hasNext())
				throw new NoSuchElementException();
			int e = uEdges.getInt(lastV = v);
			advanceUntilNext(v + 1);
			return e;
		}

		@Override
		public int peekNext() {
			if (!hasNext())
				throw new NoSuchElementException();
			return uEdges.getInt(v);
		}

		void advanceUntilNext(int next) {
			int n = vertices().size();
			for (; next < n; next++) {
				if (uEdges.getInt(next) != EdgeNone) {
					v = next;
					return;
				}
			}
			v = -1;
		}

		@Override
		public int source() {
			return source;
		}

		@Override
		public int target() {
			return lastV;
		}

		@Override
		public void remove() {
			removeEdge(uEdges.getInt(target()));
		}
	}

	class EdgeIterIn implements EdgeIterImpl {

		private int source;
		private final int target;
		private int lastU = -1;

		EdgeIterIn(int target) {
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
			if (!hasNext())
				throw new NoSuchElementException();
			int e = edges.get(lastU = source).getInt(target);
			advanceUntilNext(source + 1);
			return e;
		}

		@Override
		public int peekNext() {
			if (!hasNext())
				throw new NoSuchElementException();
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
			return lastU;
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
