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
import java.util.NoSuchElementException;
import com.jgalgo.EdgeEndpointsContainer.GraphWithEdgeEndpointsContainer;

abstract class GraphArrayAbstract extends GraphBaseContinues implements GraphWithEdgeEndpointsContainer {

	private final EdgeEndpointsContainer edgeEndpoints;

	public GraphArrayAbstract(int n, GraphCapabilities capabilities) {
		super(n, capabilities);
		edgeEndpoints = new EdgeEndpointsContainer(0);
		addInternalEdgesDataContainer(edgeEndpoints);
	}

	@Override
	public int addEdge(int u, int v) {
		int e = super.addEdge(u, v);
		edgeEndpoints.add(e);
		edgeEndpoints.setEndpoints(e, u, v);
		return e;
	}

	@Override
	public void removeEdge(int e) {
		e = edgeSwapBeforeRemove(e);
		edgeEndpoints.remove(e);
		super.removeEdge(e);
	}

	@Override
	void edgeSwap(int e1, int e2) {
		edgeEndpoints.swap(e1, e2);
		super.edgeSwap(e1, e2);
	}

	static void addEdgeToList(DataContainer.Obj<int[]> edges, DataContainer.Int edgesNum, int w, int e) {
		int[] es = edges.get(w);
		int num = edgesNum.getInt(w);
		if (es.length <= num) {
			es = Arrays.copyOf(es, Math.max(es.length * 2, 2));
			edges.set(w, es);
		}
		es[num] = e;
		edgesNum.set(w, num + 1);
	}

	static int edgeIndexOf(int[] edges, int edgesNum, int e) {
		for (int i = 0; i < edgesNum; i++)
			if (edges[i] == e)
				return i;
		return -1;
	}

	static void removeEdgeFromList(DataContainer.Obj<int[]> edges, DataContainer.Int edgesNum, int w, int e) {
		int[] es = edges.get(w);
		int num = edgesNum.getInt(w);
		int i = edgeIndexOf(es, num, e);
		es[i] = es[num - 1];
		edgesNum.set(w, num - 1);
	}

	@Override
	public EdgeEndpointsContainer edgeEndpoints() {
		return edgeEndpoints;
	}

	void reverseEdge0(int edge) {
		edgeEndpoints.reverseEdge(edge);
	}

	@Override
	public void clearEdges() {
		edgeEndpoints.clear();
		super.clearEdges();
	}

	abstract class EdgeIt implements EdgeIterImpl {

		private final int[] edges;
		private int count;
		private int idx;
		int lastEdge = -1;

		EdgeIt(int[] edges, int count) {
			this.edges = edges;
			this.count = count;
		}

		@Override
		public boolean hasNext() {
			return idx < count;
		}

		@Override
		public int nextInt() {
			if (!hasNext())
				throw new NoSuchElementException();
			return lastEdge = edges[idx++];
		}

		@Override
		public int peekNext() {
			if (!hasNext())
				throw new NoSuchElementException();
			return edges[idx];
		}

		@Override
		public void remove() {
			if (lastEdge == -1)
				throw new IllegalStateException();
			removeEdge(lastEdge);
			/**
			 * The edge will be removed from entry idx-1 in edges[], go back and decrease the edges count.
			 */
			count--;
			idx--;
			lastEdge = -1;
		}

	}

}
