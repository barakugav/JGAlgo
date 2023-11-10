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
package com.jgalgo.alg;

import java.util.Objects;
import java.util.Set;
import java.util.function.IntConsumer;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Linear cores computing algorithm.
 *
 * <p>
 * The algorithm compute the core number of each vertex by computing the 0-core, than the 1-core, 2-core ect. It does so
 * by removing all vertices with degree less than the current core number.
 *
 * <p>
 * The algorithm runs in linear time.
 *
 * <p>
 * Based on 'An O(m) Algorithm for Cores Decomposition of Networks' by Batagelj, V. and Zaversnik, M.
 *
 * @author Barak Ugav
 */
class CoresAlgoImpl implements CoresAlgo {

	CoresAlgo.IResult computeCores(IndexGraph g, EdgeDirection degreeType) {
		Objects.requireNonNull(degreeType);

		final int n = g.vertices().size();
		final boolean directed = g.isDirected();

		/* cache the degree of each vertex */
		int[] degree = new int[n];
		int maxDegree = 0;
		if (!directed || degreeType == EdgeDirection.Out) {
			for (int v = 0; v < n; v++) {
				degree[v] = g.outEdges(v).size();
				maxDegree = Math.max(maxDegree, degree[v]);
			}
		} else if (degreeType == EdgeDirection.In) {
			for (int v = 0; v < n; v++) {
				degree[v] = g.inEdges(v).size();
				maxDegree = Math.max(maxDegree, degree[v]);
			}
		} else {
			assert degreeType == EdgeDirection.All;
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
			if (!directed || degreeType == EdgeDirection.In) {
				for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					int v = eit.targetInt();
					if (degree[v] > uDegree)
						decreaseDegree.accept(v);
				}
			} else if (degreeType == EdgeDirection.Out) {
				for (IEdgeIter eit = g.inEdges(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					int v = eit.sourceInt();
					if (degree[v] > uDegree)
						decreaseDegree.accept(v);
				}
			} else {
				assert degreeType == EdgeDirection.All;
				for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					int v = eit.targetInt();
					if (degree[v] > uDegree)
						decreaseDegree.accept(v);
				}
				for (IEdgeIter eit = g.inEdges(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					int v = eit.sourceInt();
					if (degree[v] > uDegree)
						decreaseDegree.accept(v);
				}
			}
		}

		int[] core = degree;
		return new ResultImpl(core);
	}

	private static class ResultImpl implements CoresAlgo.IResult {

		private final int[] core;
		private final int maxCore;
		private int[] coreOffset;
		private int[] sortedVertices;
		private IntSet[] coreVertices;
		private IntSet[] coreShells;
		private IntSet[] coreCrusts;

		public ResultImpl(int[] core) {
			this.core = Objects.requireNonNull(core);
			int maxCore = -1;
			for (int v = 0; v < core.length; v++)
				maxCore = Math.max(maxCore, core[v]);
			this.maxCore = maxCore;
		}

		@Override
		public int vertexCoreNum(int v) {
			return core[v];
		}

		@Override
		public int maxCore() {
			return maxCore;
		}

		private void computeSortedVertices() {
			if (sortedVertices != null)
				return;
			final int n = this.core.length;
			final int coreNum = maxCore + 1;
			if (coreNum == 0)
				return;

			coreOffset = new int[coreNum + 1];
			for (int v = 0; v < n; v++)
				coreOffset[this.core[v]]++;
			for (int s = 0, c = 0; c < coreNum; c++) {
				int k = coreOffset[c];
				coreOffset[c] = s;
				s += k;
			}
			sortedVertices = new int[n];
			for (int v = 0; v < n; v++)
				sortedVertices[coreOffset[this.core[v]]++] = v;
			assert coreOffset[coreNum - 1] == n;
			for (int c = coreNum; c > 0; c--)
				coreOffset[c] = coreOffset[c - 1];
			coreOffset[0] = 0;
		}

		@Override
		public IntSet coreVertices(int core) {
			if (coreVertices == null) {
				computeSortedVertices();
				final int n = this.core.length;
				final int coreNum = maxCore + 1;
				coreVertices = new IntSet[coreNum];
				for (int c = 0; c < coreNum; c++) {
					final int c0 = c;
					coreVertices[c] = new ImmutableIntArraySet(sortedVertices, coreOffset[c], n) {
						@Override
						public boolean contains(int v) {
							return 0 <= v && v < n && ResultImpl.this.core[v] >= c0;
						}
					};
				}
			}
			return coreVertices[core];
		}

		@Override
		public IntSet coreShell(int core) {
			if (coreShells == null) {
				computeSortedVertices();
				final int n = this.core.length;
				final int coreNum = maxCore + 1;
				coreShells = new IntSet[coreNum];
				for (int c = 0; c < coreNum; c++) {
					final int c0 = c;
					coreShells[c] = new ImmutableIntArraySet(sortedVertices, coreOffset[c], coreOffset[c + 1]) {
						@Override
						public boolean contains(int v) {
							return 0 <= v && v < n && ResultImpl.this.core[v] >= c0;
						}
					};
				}
			}
			return coreShells[core];
		}

		@Override
		public IntSet coreCrust(int core) {
			if (coreCrusts == null) {
				computeSortedVertices();
				final int n = this.core.length;
				final int coreNum = maxCore + 1;
				coreCrusts = new IntSet[coreNum];
				for (int c = 0; c < coreNum; c++) {
					final int c0 = c;
					coreCrusts[c] = new ImmutableIntArraySet(sortedVertices, 0, coreOffset[c]) {
						@Override
						public boolean contains(int v) {
							return 0 <= v && v < n && ResultImpl.this.core[v] >= c0;
						}
					};
				}
			}
			return coreCrusts[core];
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> CoresAlgo.Result<V, E> computeCores(Graph<V, E> g, EdgeDirection degreeType) {
		if (g instanceof IndexGraph) {
			return (CoresAlgo.Result<V, E>) computeCores((IndexGraph) g, degreeType);

		} else {
			IndexGraph iGraph = g.indexGraph();
			CoresAlgo.IResult iResult = computeCores(iGraph, degreeType);
			return resultFromIndexResult(g, iResult);
		}
	}

	private static class IntResultFromIndexResult implements CoresAlgo.IResult {

		private final CoresAlgo.IResult iResult;
		private final IndexIntIdMap viMap;

		public IntResultFromIndexResult(IntGraph g, CoresAlgo.IResult iResult) {
			this.iResult = Objects.requireNonNull(iResult);
			this.viMap = g.indexGraphVerticesMap();
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
		public IntSet coreVertices(int core) {
			return IndexIdMaps.indexToIdSet(iResult.coreVertices(core), viMap);
		}

		@Override
		public IntSet coreShell(int core) {
			return IndexIdMaps.indexToIdSet(iResult.coreShell(core), viMap);
		}

		@Override
		public IntSet coreCrust(int core) {
			return IndexIdMaps.indexToIdSet(iResult.coreCrust(core), viMap);
		}
	}

	private static class ObjResultFromIndexResult<V, E> implements CoresAlgo.Result<V, E> {

		private final CoresAlgo.IResult iResult;
		private final IndexIdMap<V> viMap;

		public ObjResultFromIndexResult(Graph<V, E> g, CoresAlgo.IResult iResult) {
			this.iResult = Objects.requireNonNull(iResult);
			this.viMap = g.indexGraphVerticesMap();
		}

		@Override
		public int vertexCoreNum(V v) {
			return iResult.vertexCoreNum(viMap.idToIndex(v));
		}

		@Override
		public int maxCore() {
			return iResult.maxCore();
		}

		@Override
		public Set<V> coreVertices(int core) {
			return IndexIdMaps.indexToIdSet(iResult.coreVertices(core), viMap);
		}

		@Override
		public Set<V> coreShell(int core) {
			return IndexIdMaps.indexToIdSet(iResult.coreShell(core), viMap);
		}

		@Override
		public Set<V> coreCrust(int core) {
			return IndexIdMaps.indexToIdSet(iResult.coreCrust(core), viMap);
		}
	}

	@SuppressWarnings("unchecked")
	private static <V, E> CoresAlgo.Result<V, E> resultFromIndexResult(Graph<V, E> g, CoresAlgo.IResult indexResult) {
		assert !(g instanceof IndexGraph);
		if (g instanceof IntGraph) {
			return (CoresAlgo.Result<V, E>) new IntResultFromIndexResult((IntGraph) g, indexResult);
		} else {
			return new ObjResultFromIndexResult<>(g, indexResult);
		}
	}

}
