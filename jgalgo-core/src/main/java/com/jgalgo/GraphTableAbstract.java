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

	GraphTableAbstract(int n, GraphCapabilities capabilities) {
		super(n, capabilities);

		edges = new DataContainer.Obj<>(verticesIDStrategy, null, DataContainer.Int.class);
		addInternalVerticesDataContainer(edges);
		for (int u = 0; u < n; u++) {
			DataContainer.Int uEdges = new DataContainer.Int(verticesIDStrategy, EdgeNone);
			edges.set(u, uEdges);
			addInternalVerticesDataContainer(uEdges);
		}

		edgeEndpoints = new EdgeEndpointsContainer(edgesIDStrategy);
		addInternalEdgesDataContainer(edgeEndpoints);
	}

	@Override
	public int addVertex() {
		int v = super.addVertex();
		DataContainer.Int vEdges = edges.get(v);
		if (vEdges == null) {
			vEdges = new DataContainer.Int(verticesIDStrategy, EdgeNone);
			addInternalVerticesDataContainer(vEdges);
			edges.set(v, vEdges);
		}
		return v;
	}

	@Override
	public void removeVertex(int v) {
		v = vertexSwapBeforeRemove(v);
		DataContainer.Int edgesV = edges.get(v);
		super.removeVertex(v);
		edgesV.clear();
		// Don't deallocate v array
		// edges.clear(v);

		final int n = vertices().size();
		for (int u = 0; u < n; u++)
			edges.get(u).clear(v);
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
	public EdgeIter edgesOut(int u) {
		return new EdgeIterOut(u);
	}

	@Override
	public int getEdge(int u, int v) {
		return edges.get(u).getInt(v);
	}

	@Override
	public EdgeIter getEdges(int u, int v) {
		int e = edges.get(u).getInt(v);
		if (e == EdgeNone) {
			return EdgeIterImpl.Empty;
		} else {
			return new EdgeIterImpl() {

				boolean first = true;

				@Override
				public boolean hasNext() {
					return first;
				}

				@Override
				public int nextInt() {
					if (!hasNext())
						throw new NoSuchElementException();
					first = false;
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
					return u;
				}

				@Override
				public int target() {
					return v;
				}
			};
		}
	}

	@Override
	public int addEdge(int u, int v) {
		if (edges.get(u).getInt(v) != EdgeNone)
			throw new IllegalArgumentException("parallel edges are not supported");
		int e = super.addEdge(u, v);
		edgeEndpoints.setEndpoints(e, u, v);
		return e;
	}

	@Override
	public void removeEdge(int e) {
		e = edgeSwapBeforeRemove(e);
		edgeEndpoints.clear(e);
		super.removeEdge(e);
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

		private final int u;
		private int v;
		private int lastV = -1;
		private final DataContainer.Int uEdges;

		EdgeIterOut(int u) {
			checkVertexIdx(u);
			this.u = u;
			uEdges = edges.get(u);

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
			return u;
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

		private int u;
		private final int v;
		private int lastU = -1;

		EdgeIterIn(int v) {
			checkVertexIdx(v);
			this.v = v;

			advanceUntilNext(0);
		}

		@Override
		public boolean hasNext() {
			return u != -1;
		}

		@Override
		public int nextInt() {
			if (!hasNext())
				throw new NoSuchElementException();
			int e = edges.get(lastU = u).getInt(v);
			advanceUntilNext(u + 1);
			return e;
		}

		@Override
		public int peekNext() {
			if (!hasNext())
				throw new NoSuchElementException();
			return edges.get(u).getInt(v);
		}

		private void advanceUntilNext(int next) {
			int n = vertices().size();
			for (; next < n; next++) {
				if (edges.get(next).getInt(v) != EdgeNone) {
					u = next;
					return;
				}
			}
			u = -1;
		}

		@Override
		public int source() {
			return lastU;
		}

		@Override
		public int target() {
			return v;
		}

		@Override
		public void remove() {
			removeEdge(edges.get(source()).getInt(target()));
		}
	}

}
