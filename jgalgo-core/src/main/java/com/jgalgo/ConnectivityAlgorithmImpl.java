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

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntStack;

class ConnectivityAlgorithmImpl implements ConnectivityAlgorithm {

	private final AllocatedMemory allocatedMemory = new AllocatedMemory();

	@Override
	public ConnectivityAlgorithm.Result computeConnectivityComponents(Graph g) {
		return g.getCapabilities().directed() ? computeSCCDirected(g) : computeSCCUndirected(g);
	}

	private ConnectivityAlgorithm.Result computeSCCDirected(Graph g) {
		allocatedMemory.allocatedDirected(g);
		IntStack s = allocatedMemory.stack1;
		IntStack p = allocatedMemory.stack2;
		assert s.isEmpty();
		assert p.isEmpty();
		int[] dfsPath = allocatedMemory.dfsPath;
		int[] c = allocatedMemory.c;
		EdgeIter[] edges = allocatedMemory.edges;

		// implementation of Tarjan's strongly connected components algorithm
		// https://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm

		int n = g.vertices().size();
		int[] comp = new int[n];
		Arrays.fill(comp, -1);
		int compNum = 0;

		Arrays.fill(c, 0);
		int cNext = 1;

		for (int root = 0; root < n; root++) {
			if (comp[root] != -1)
				continue;
			dfsPath[0] = root;
			edges[0] = g.edgesOut(root);
			c[root] = cNext++;
			s.push(root);
			p.push(root);

			dfs: for (int depth = 0;;) {
				for (EdgeIter eit = edges[depth]; eit.hasNext();) {
					eit.nextInt();
					int v = eit.v();
					if (c[v] == 0) {
						c[v] = cNext++;
						s.push(v);
						p.push(v);

						dfsPath[++depth] = v;
						edges[depth] = g.edgesOut(v);
						continue dfs;
					} else if (comp[v] == -1)
						while (c[p.topInt()] > c[v])
							p.popInt();
				}
				int u = dfsPath[depth];
				if (p.topInt() == u) {
					int v;
					do {
						v = s.popInt();
						comp[v] = compNum;
					} while (v != u);
					compNum++;
					p.popInt();
				}

				edges[depth] = null;
				if (depth-- == 0)
					break;
			}
		}
		return new Result(compNum, comp);
	}

	private ConnectivityAlgorithm.Result computeSCCUndirected(Graph g) {
		allocatedMemory.allocatedUndirected(g);
		IntStack stack = allocatedMemory.stack1;
		assert stack.isEmpty();

		int n = g.vertices().size();
		int[] comp = new int[n];
		Arrays.fill(comp, -1);
		int compNum = 0;

		for (int root = 0; root < n; root++) {
			if (comp[root] != -1)
				continue;

			final int compIdx = compNum++;
			stack.push(root);
			comp[root] = compIdx;

			while (!stack.isEmpty()) {
				int u = stack.popInt();

				for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
					eit.nextInt();
					int v = eit.v();
					if (comp[v] != -1) {
						assert comp[v] == compIdx;
						continue;
					}
					comp[v] = compIdx;
					stack.push(v);
				}
			}
		}
		return new Result(compNum, comp);
	}

	private static class Result implements ConnectivityAlgorithm.Result {
		private final int ccNum;
		private final int[] vertexToCC;

		private Result(int ccNum, int[] vertexToCC) {
			this.ccNum = ccNum;
			this.vertexToCC = vertexToCC;
		}

		@Override
		public int getVertexCc(int v) {
			return vertexToCC[v];
		}

		@Override
		public int getNumberOfCC() {
			return ccNum;
		}

		@Override
		public String toString() {
			return Arrays.toString(vertexToCC);
		}
	}

	private static class AllocatedMemory {
		private IntStack stack1;
		private IntStack stack2;
		private int[] dfsPath = IntArrays.EMPTY_ARRAY;
		private int[] c = IntArrays.EMPTY_ARRAY;
		private EdgeIter[] edges = MemoryReuse.EmptyEdgeIterArr;

		void allocatedDirected(Graph g) {
			int n = g.vertices().size();
			stack1 = MemoryReuse.ensureAllocated(stack1, IntArrayList::new);
			stack2 = MemoryReuse.ensureAllocated(stack2, IntArrayList::new);
			dfsPath = MemoryReuse.ensureLength(dfsPath, n);
			c = MemoryReuse.ensureLength(c, n);
			edges = MemoryReuse.ensureLength(edges, n);

		}

		void allocatedUndirected(Graph g) {
			stack1 = MemoryReuse.ensureAllocated(stack1, IntArrayList::new);
		}
	}

	static class Builder implements ConnectivityAlgorithm.Builder {
		@Override
		public ConnectivityAlgorithm build() {
			return new ConnectivityAlgorithmImpl();
		}
	}

}
