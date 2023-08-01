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

import java.util.Objects;
import java.util.function.IntConsumer;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Linear core computing algorithm.
 * <p>
 * The algorithm compute the core number of each vertex by computing the 0-core, than the 1-core, 2-core ect. It does so
 * by removing all vertices with degree less than the current core number.
 * <p>
 * The algorithm runs in linear time.
 * <p>
 * Based on 'An O(m) Algorithm for Cores Decomposition of Networks' by Batagelj, V. and Zaversnik, M.
 *
 * @author Barak Ugav
 */
class CoreAlgoImpl implements CoreAlgo {

	CoreAlgo.Result computeCores(IndexGraph g, DegreeType degreeType) {
		Objects.requireNonNull(degreeType);

		final int n = g.vertices().size();
		final boolean directed = g.getCapabilities().directed();

		/* cache the degree of each vertex */
		int[] degree = new int[n];
		int maxDegree = 0;
		if (!directed || degreeType == DegreeType.OutDegree) {
			for (int v = 0; v < n; v++) {
				degree[v] = g.outEdges(v).size();
				maxDegree = Math.max(maxDegree, degree[v]);
			}
		} else if (degreeType == DegreeType.InDegree) {
			for (int v = 0; v < n; v++) {
				degree[v] = g.inEdges(v).size();
				maxDegree = Math.max(maxDegree, degree[v]);
			}
		} else {
			assert degreeType == DegreeType.OutAndInDegree;
			for (int v = 0; v < n; v++) {
				degree[v] = g.outEdges(v).size() + g.inEdges(v).size();
				maxDegree = Math.max(maxDegree, degree[v]);
			}
		}

		/* arrange the vertices in sorted bins per degree */
		/* vertices[bin[d], bin[d+1]) are the vertices with degree d */
		/* vertices[pos[v]] == v */
		int[] bin = new int[maxDegree + 1];
		int[] vertices = new int[n];
		int[] pos = new int[n];
		for (int v = 0; v < n; v++)
			bin[degree[v]]++;
		for (int start = 0, d = 0; d <= maxDegree; d++) {
			int verticesNum = bin[d];
			bin[d] = start;
			start += verticesNum;
		}
		for (int v = 0; v < n; v++) {
			pos[v] = bin[degree[v]];
			vertices[pos[v]] = v;
			bin[degree[v]]++;
		}
		for (int i = maxDegree; i > 0; i--)
			bin[i] = bin[i - 1];
		bin[0] = 0;

		IntConsumer decreaseDegree = v -> {
			int vDegree = degree[v];
			int vPos = pos[v];
			int vBinStart = bin[vDegree];
			if (vPos != vBinStart) {
				int wPos = vBinStart;
				int w = vertices[wPos];
				pos[v] = wPos;
				pos[w] = vPos;
				vertices[vPos] = w;
				vertices[wPos] = v;
			}
			bin[vDegree]++;
			degree[v]--;
		};

		/* iterate over the vertices in increase order of their degree */
		for (int p = 0; p < n; p++) {
			int u = vertices[p];
			assert pos[u] == p;
			int uDegree = degree[u];

			/* we 'remove' u from the graph, actually just decreasing the degree of its neighbors */
			if (!directed || degreeType == DegreeType.InDegree) {
				for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					int v = eit.target();
					if (degree[v] > uDegree)
						decreaseDegree.accept(v);
				}
			} else if (degreeType == DegreeType.OutDegree) {
				for (EdgeIter eit = g.inEdges(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					int v = eit.source();
					if (degree[v] > uDegree)
						decreaseDegree.accept(v);
				}
			} else {
				assert degreeType == DegreeType.OutAndInDegree;
				for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					int v = eit.target();
					if (degree[v] > uDegree)
						decreaseDegree.accept(v);
				}
				for (EdgeIter eit = g.inEdges(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					int v = eit.source();
					if (degree[v] > uDegree)
						decreaseDegree.accept(v);
				}
			}
		}

		int[] core = degree;
		return new ResultImpl(core);
	}

	private static class ResultImpl implements CoreAlgo.Result {

		private final int[] core;
		private final int maxCore;
		private IntCollection[] coreVertices;

		public ResultImpl(int[] core) {
			this.core = Objects.requireNonNull(core);
			int m = 0;
			for (int v = 0; v < core.length; v++)
				m = Math.max(m, core[v]);
			maxCore = m;
		}

		@Override
		public int vertexCoreNum(int v) {
			return core[v];
		}

		@Override
		public int maxCore() {
			return maxCore;
		}

		@Override
		public IntCollection coreVertices(int core) {
			if (coreVertices == null) {
				/*  */
				final int n = this.core.length;
				IntList[] vs = new IntList[maxCore + 1];
				for (int c = 0; c <= maxCore; c++)
					vs[c] = new IntArrayList();
				for (int v = 0; v < n; v++)
					vs[this.core[v]].add(v);

				coreVertices = new IntCollection[maxCore + 1];
				for (int c0 = maxCore; c0 >= 0; c0--) {
					final int c = c0;
					coreVertices[c] = new AbstractIntSet() {

						final int size = c == maxCore ? vs[maxCore].size() : coreVertices[c + 1].size() + vs[c].size();

						@Override
						public int size() {
							return size;
						}

						@Override
						public IntIterator iterator() {
							return new IntIterator() {

								int cIdx = c;
								IntIterator it = vs[cIdx].iterator();

								@Override
								public boolean hasNext() {
									for (;;) {
										if (it.hasNext())
											return true;
										if (cIdx == maxCore)
											return false;
										cIdx++;
										it = vs[cIdx].iterator();
									}
								}

								@Override
								public int nextInt() {
									Assertions.Iters.hasNext(this);
									return it.nextInt();
								}
							};
						}

						@Override
						public boolean contains(int key) {
							return key >= 0 && key < n && ResultImpl.this.core[key] >= c;
						}

					};
				}
			}
			return coreVertices[core];
		}

	}

	@Override
	public CoreAlgo.Result computeCores(Graph g, DegreeType degreeType) {
		if (g instanceof IndexGraph)
			return computeCores((IndexGraph) g, degreeType);

		IndexGraph iGraph = g.indexGraph();
		IndexIdMap viMap = g.indexGraphVerticesMap();
		CoreAlgo.Result iResult = computeCores(iGraph, degreeType);
		return new ResultFromIndexResult(iResult, viMap);
	}

	private static class ResultFromIndexResult implements CoreAlgo.Result {

		private final CoreAlgo.Result iResult;
		private final IndexIdMap viMap;

		public ResultFromIndexResult(CoreAlgo.Result iResult, IndexIdMap viMap) {
			this.iResult = Objects.requireNonNull(iResult);
			this.viMap = Objects.requireNonNull(viMap);
		}

		@Override
		public int vertexCoreNum(int v) {
			return iResult.vertexCoreNum(viMap.idToIndex(v));
		}

		@Override
		public int maxCore() {
			return iResult.maxCore();
		}

		@Override
		public IntCollection coreVertices(int core) {
			return IndexIdMaps.indexToIdCollection(iResult.coreVertices(core), viMap);
		}

	}

}
