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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntListIterator;
import it.unimi.dsi.fastutil.ints.IntLists;

/**
 * Hopcroft-Tarjan algorithm for bi-connected components.
 * <p>
 * The algorithm performs a DFS and look for edges from vertices with greater depth to vertices with lower depth,
 * indicating for to separates paths between them: one using the DFS tree, and the other using the edge. The algorithm
 * runs in linear time and space.
 * <p>
 * Based on 'Algorithm 447: efficient algorithms for graph manipulation' by Hopcroft, J. and Tarjan, R. 1973.
 *
 * @author Barak Ugav
 */
class BiConnectedComponentsAlgoHopcroftTarjan extends BiConnectedComponentsAlgoAbstract {

	@Override
	BiConnectedComponentsAlgo.Result computeBiConnectivityComponents(IndexGraph g) {
		ArgumentCheck.onlyUndirected(g);

		final int n = g.vertices().size();
		int[] depths = new int[n];
		int[] edgePath = new int[n];
		int[] lowpoint = new int[n];
		EdgeIter[] edgeIters = new EdgeIter[n];
		// TODO DFS stack class

		IntArrayList edgeStack = new IntArrayList();

		Arrays.fill(depths, -1);

		var biccVerticesFromBiccEdgesState = new Object() {
			int[] visited = new int[n];
			int nextVisitIdx = 1;
		};
		Function<IntCollection, IntList> biccVerticesFromBiccEdges = biccsEdges -> {
			IntList biccVertices = new IntArrayList();
			int[] visited = biccVerticesFromBiccEdgesState.visited;
			final int visitIdx = biccVerticesFromBiccEdgesState.nextVisitIdx++;
			for (int e : biccsEdges) {
				for (int w : new int[] { g.edgeSource(e), g.edgeTarget(e) }) {
					if (visited[w] != visitIdx) {
						visited[w] = visitIdx;
						biccVertices.add(w);
					}
				}
			}
			return biccVertices;
		};

		// BitSet separatingVertex = new BitSet(n);
		List<IntList> biccsVertices = new ArrayList<>();
		// List<IntList> biccsEdges = new ArrayList<>();

		for (int root = 0; root < n; root++) {
			if (depths[root] != -1)
				continue;
			int depth = 0;
			depths[root] = depth;
			lowpoint[depth] = depth;
			edgeIters[depth] = g.edgesOut(root).iterator();

			int rootChildrenCount = 0;

			dfs: for (int u = root;;) {
				final int parent = depth == 0 ? -1 : g.edgeEndpoint(edgePath[depth - 1], u);

				for (EdgeIter eit = edgeIters[depth]; eit.hasNext();) {
					int e = eit.nextInt();

					int v = eit.target();
					final int vDepth = depths[v];
					if (vDepth == -1) { /* unvisited */
						edgeStack.push(e);
						edgePath[depth] = e;
						depth++;
						depths[v] = depth;
						lowpoint[depth] = depth;
						edgeIters[depth] = g.edgesOut(v).iterator();
						u = v;
						continue dfs;

					} else if (v != parent && vDepth < depth) {
						edgeStack.push(e);
						lowpoint[depth] = Math.min(lowpoint[depth], vDepth);
					}
				}

				if (depth > 0) {
					depth--;
					if (lowpoint[depth + 1] >= depth) {
						// separatingVertex.set(parent);

						IntList biccEdges = new IntArrayList();
						for (int lastEdge = edgePath[depth]; !edgeStack.isEmpty();) {
							int e = edgeStack.popInt();
							biccEdges.add(e);
							if (e == lastEdge)
								break;
						}
						assert !biccEdges.isEmpty();
						IntList biccVertices = biccVerticesFromBiccEdges.apply(biccEdges);
						biccsVertices.add(biccVertices);
						// biccsEdges.add(biccEdges);
					} else {
						lowpoint[depth] = Math.min(lowpoint[depth], lowpoint[depth + 1]);
					}

					if (depth == 0)
						rootChildrenCount++;
					u = parent;

				} else {
					assert u == root;
					// if (rootChildrenCount > 1)
					// separatingVertex.set(root);

					if (rootChildrenCount == 0) {
						biccsVertices.add(IntList.of(root));
						// biccsEdges.add(IntList.of());

					} else if (!edgeStack.isEmpty()) {
						IntList biccEdges = new IntArrayList(edgeStack);
						IntList biccVertices = biccVerticesFromBiccEdges.apply(biccEdges);
						edgeStack.clear();
						biccsVertices.add(biccVertices);
						// biccsEdges.add(biccEdges);

					}
					break;
				}
			}
		}

		return new Res(g, biccsVertices.toArray(IntList[]::new));
	}

	private static class Res implements BiConnectedComponentsAlgo.Result {

		private final IndexGraph g;
		private final IntList[] biccsVertices;
		private IntList[] biccsEdges;
		private IntList[] vertexBiCcs;
		// private final BitSet separatingVerticesBitmap;
		// private IntList separatingVertices;

		Res(IndexGraph g, IntList[] biccsVertices) {
			this.g = Objects.requireNonNull(g);
			this.biccsVertices = Objects.requireNonNull(biccsVertices);
			for (int biccIdx = 0; biccIdx < biccsVertices.length; biccIdx++)
				this.biccsVertices[biccIdx] = IntLists.unmodifiable(this.biccsVertices[biccIdx]);
			// this.separatingVerticesBitmap = Objects.requireNonNull(separatingVerticesBitmap);
		}

		@Override
		public IntCollection getVertexBiCcs(int vertex) {
			if (vertexBiCcs == null) {
				final int n = g.vertices().size();
				vertexBiCcs = new IntList[n];
				for (int v = 0; v < n; v++)
					vertexBiCcs[v] = new IntArrayList();

				for (int biccIdx = 0; biccIdx < biccsVertices.length; biccIdx++)
					for (int v : biccsVertices[biccIdx])
						vertexBiCcs[v].add(biccIdx);

				for (int v = 0; v < n; v++)
					vertexBiCcs[v] = IntLists.unmodifiable(vertexBiCcs[v]);
			}
			return vertexBiCcs[vertex];
		}

		@Override
		public int getNumberOfBiCcs() {
			return biccsVertices.length;
		}

		@Override
		public IntCollection getBiCcVertices(int biccIdx) {
			return biccsVertices[biccIdx];
		}

		@Override
		public IntCollection getBiCcEdges(int biccIdx) {
			/* unfortunately, this implementation is not linear */
			if (biccsEdges == null) {
				biccsEdges = new IntList[getNumberOfBiCcs()];
				for (int idx = 0; idx < biccsVertices.length; idx++)
					biccsEdges[idx] = new IntArrayList();
				for (int e : g.edges()) {
					int u = g.edgeSource(e);
					int v = g.edgeTarget(e);
					/* Both getVertexBiCcs(u) and getVertexBiCcs(v) are sorted */
					/* Iterate over them in order and find BiConnected components containing both of u and v */
					for (IntIterator uBiccs = getVertexBiCcs(u).iterator(),
							vBiccs = getVertexBiCcs(v).iterator(); uBiccs.hasNext() && vBiccs.hasNext();) {
						int ub = uBiccs.nextInt();
						int vb = vBiccs.nextInt();
						if (ub == vb) {
							biccsEdges[ub].add(e);
						} else if (ub > vb) {
							/* roll back uBiccs */
							((IntListIterator) uBiccs).previousInt();
						} else { /* ub < vb */
							/* roll back vBiccs */
							((IntListIterator) vBiccs).previousInt();
						}
					}
				}
				for (int idx = 0; idx < biccsVertices.length; idx++)
					biccsEdges[idx] = IntLists.unmodifiable(biccsEdges[idx]);

			}
			return biccsEdges[biccIdx];
		}

		// @Override
		// public IntCollection separatingVertices() {
		// if (separatingVertices == null) {
		// separatingVertices = new IntArrayList(separatingVerticesBitmap.cardinality());
		// for (int v : Utils.iterable(separatingVerticesBitmap))
		// separatingVertices.add(v);
		// separatingVertices = IntLists.unmodifiable(separatingVertices);
		// }
		// return separatingVertices;
		// }

		// @Override
		// public boolean isSeparatingVertex(int vertex) {
		// if (!g.vertices().contains(vertex))
		// throw new IndexOutOfBoundsException(vertex);
		// return separatingVerticesBitmap.get(vertex);
		// }

		@Override
		public String toString() {
			StringBuilder s = new StringBuilder().append('[');
			final int biccNum = getNumberOfBiCcs();
			for (int biccIdx = 0; biccIdx < biccNum; biccIdx++) {
				if (biccIdx > 0)
					s.append(", ");
				s.append(getBiCcVertices(biccIdx).toString());
			}
			return s.append(']').toString();
		}

	}
}
