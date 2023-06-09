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
import java.util.Objects;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.ints.IntStack;

class ConnectedComponentsAlgoImpl extends ConnectedComponentsAlgoAbstract {

	@Override
	ConnectedComponentsAlgo.Result computeConnectivityComponents(IndexGraph g) {
		return g.getCapabilities().directed() ? computeSCCDirected(g) : computeSCCUndirected(g);
	}

	private static ConnectedComponentsAlgo.Result computeSCCDirected(IndexGraph g) {
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
			edges[0] = g.outEdges(root).iterator();
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
						edges[depth] = g.outEdges(v).iterator();
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
		return new Result(g, compNum, comp);
	}

	private static ConnectedComponentsAlgo.Result computeSCCUndirected(IndexGraph g) {
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

				for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
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
		return new Result(g, compNum, comp);
	}

	private static class Result implements ConnectedComponentsAlgo.Result {
		private final IndexGraph g;
		private final int ccNum;
		private final int[] vertexToCc;
		private IntList[] ccVertices;
		private IntList[] ccEdges;

		private Result(IndexGraph g, int ccNum, int[] vertexToCc) {
			this.g = Objects.requireNonNull(g);
			this.ccNum = ccNum;
			this.vertexToCc = Objects.requireNonNull(vertexToCc);
		}

		@Override
		public int getVertexCc(int vertex) {
			return vertexToCc[vertex];
		}

		@Override
		public int getNumberOfCcs() {
			return ccNum;
		}

		@Override
		public String toString() {
			return Arrays.toString(vertexToCc);
		}

		@Override
		public IntCollection getCcVertices(int ccIdx) {
			if (ccVertices == null) {
				ccVertices = new IntList[ccNum];
				for (int c = 0; c < ccNum; c++)
					ccVertices[c] = new IntArrayList();
				final int n = vertexToCc.length;
				for (int u = 0; u < n; u++)
					ccVertices[vertexToCc[u]].add(u);
				for (int c = 0; c < ccNum; c++)
					ccVertices[c] = IntLists.unmodifiable(ccVertices[c]);
			}
			return ccVertices[ccIdx];
		}

		@Override
		public IntCollection getCcEdges(int ccIdx) {
			if (ccEdges == null) {
				ccEdges = new IntList[ccNum];
				for (int c = 0; c < ccNum; c++)
					ccEdges[c] = new IntArrayList();
				for (int e : g.edges()) {
					int cc1 = vertexToCc[g.edgeSource(e)];
					int cc2 = vertexToCc[g.edgeTarget(e)];
					if (cc1 == cc2)
						ccEdges[cc1].add(e);
				}
				for (int c = 0; c < ccNum; c++)
					ccEdges[c] = IntLists.unmodifiable(ccEdges[c]);
			}
			return ccEdges[ccIdx];
		}

	}

}
