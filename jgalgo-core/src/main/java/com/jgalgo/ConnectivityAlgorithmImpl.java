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
import it.unimi.dsi.fastutil.ints.IntStack;

class ConnectivityAlgorithmImpl implements ConnectivityAlgorithm {

	@Override
	public ConnectivityAlgorithm.Result computeConnectivityComponents(Graph g) {
		return g.getCapabilities().directed() ? computeSCCDirected(g) : computeSCCUndirected(g);
	}

	private static ConnectivityAlgorithm.Result computeSCCDirected(Graph g) {
		final int n = g.vertices().size();
		IntStack s = new IntArrayList();
		IntStack p = new IntArrayList();
		int[] dfsPath = new int[n];
		int[] c = new int[n];
		EdgeIter[] edges = new EdgeIter[n];
		// TODO DFS stack class

		// implementation of Tarjan's strongly connected components algorithm
		// https://en.wikipedia.org/wiki/Tarjan%27s_strongly_connected_components_algorithm

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
					int v = eit.target();
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

	private static ConnectivityAlgorithm.Result computeSCCUndirected(Graph g) {
		final int n = g.vertices().size();
		int[] comp = new int[n];
		Arrays.fill(comp, -1);
		int compNum = 0;

		IntStack stack = new IntArrayList();
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
					int v = eit.target();
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
		public int getVertexCc(int vertex) {
			return vertexToCC[vertex];
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

}
