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

abstract class GraphArrayAbstract extends GraphBaseContinues {

	private final DataContainer.Long edgeEndpoints;

	public GraphArrayAbstract(int n, GraphCapabilities capabilities) {
		super(n, capabilities);
		edgeEndpoints = new DataContainer.Long(0, sourceTarget2Endpoints(-1, -1));
		addInternalEdgesDataContainer(edgeEndpoints);
	}

	@Override
	public int addEdge(int u, int v) {
		int e = super.addEdge(u, v);
		edgeEndpoints.add(e);
		edgeEndpoints.set(e, sourceTarget2Endpoints(u, v));
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

	void reverseEdge0(int edge) {
		long endpoints = edgeEndpoints.getLong(edge);
		int u = endpoints2Source(endpoints);
		int v = endpoints2Target(endpoints);
		endpoints = sourceTarget2Endpoints(v, u);
		edgeEndpoints.set(edge, endpoints);
	}

	@Override
	public int edgeEndpoint(int edge, int endpoint) {
		long endpoints = edgeEndpoints.getLong(edge);
		int u = endpoints2Source(endpoints);
		int v = endpoints2Target(endpoints);
		if (endpoint == u) {
			return v;
		} else if (endpoint == v) {
			return u;
		} else {
			throw new IllegalArgumentException(
					"The given vertex (" + endpoint + ") is not an endpoint of the edge (" + u + ", " + v + ")");
		}
	}

	@Override
	public int edgeSource(int edge) {
		checkEdgeIdx(edge);
		return endpoints2Source(edgeEndpoints.getLong(edge));
	}

	@Override
	public int edgeTarget(int edge) {
		checkEdgeIdx(edge);
		return endpoints2Target(edgeEndpoints.getLong(edge));
	}

	void replaceEdgeSource(int edge, int newSource) {
		long endpoints = edgeEndpoints.getLong(edge);
		int target = endpoints2Target(endpoints);
		edgeEndpoints.set(edge, sourceTarget2Endpoints(newSource, target));
	}

	void replaceEdgeTarget(int edge, int newTarget) {
		long endpoints = edgeEndpoints.getLong(edge);
		int source = endpoints2Source(endpoints);
		edgeEndpoints.set(edge, sourceTarget2Endpoints(source, newTarget));
	}

	void replaceEdgeEndpoint(int edge, int oldEndpoint, int newEndpoint) {
		long endpoints = edgeEndpoints.getLong(edge);
		int source = endpoints2Source(endpoints);
		int target = endpoints2Target(endpoints);
		if (source == oldEndpoint)
			source = newEndpoint;
		if (target == oldEndpoint)
			target = newEndpoint;
		edgeEndpoints.set(edge, sourceTarget2Endpoints(source, target));
	}

	@Override
	public void clearEdges() {
		edgeEndpoints.clear();
		super.clearEdges();
	}

	private static long sourceTarget2Endpoints(int u, int v) {
		return ((u & 0xffffffffL) << 32) | ((v & 0xffffffffL) << 0);
	}

	private static int endpoints2Source(long endpoints) {
		return (int) ((endpoints >> 32) & 0xffffffffL);
	}

	private static int endpoints2Target(long endpoints) {
		return (int) ((endpoints >> 0) & 0xffffffffL);
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
