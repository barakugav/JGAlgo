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

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;

class CoresAlgos {

	private CoresAlgos() {}

	static class IndexResult implements CoresAlgo.IResult {

		private final int[] core;
		private final int maxCore;
		private int[] coreOffset;
		private int[] sortedVertices;
		private IntSet[] coreVertices;
		private IntSet[] coreShells;
		private IntSet[] coreCrusts;

		public IndexResult(int[] core) {
			this.core = Objects.requireNonNull(core);
			this.maxCore = Arrays.stream(core).max().orElse(-1);
		}

		@Override
		public int vertexCoreNum(int v) {
			Assertions.checkVertex(v, core.length);
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
			for (int v : range(n))
				coreOffset[this.core[v]]++;
			int s = 0;
			for (int c : range(coreNum)) {
				int k = coreOffset[c];
				coreOffset[c] = s;
				s += k;
			}
			sortedVertices = new int[n];
			for (int v : range(n))
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
				for (int c : range(coreNum)) {
					coreVertices[c] = new ImmutableIntArraySet(sortedVertices, coreOffset[c], n) {
						@Override
						public boolean contains(int v) {
							return 0 <= v && v < n && IndexResult.this.core[v] >= c;
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
				for (int c : range(coreNum)) {
					coreShells[c] = new ImmutableIntArraySet(sortedVertices, coreOffset[c], coreOffset[c + 1]) {
						@Override
						public boolean contains(int v) {
							return 0 <= v && v < n && IndexResult.this.core[v] == c;
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
				for (int c : range(coreNum)) {
					coreCrusts[c] = new ImmutableIntArraySet(sortedVertices, 0, coreOffset[c]) {
						@Override
						public boolean contains(int v) {
							return 0 <= v && v < n && IndexResult.this.core[v] < c;
						}
					};
				}
			}
			return coreCrusts[core];
		}
	}

	static class IntResultFromIndexResult implements CoresAlgo.IResult {

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

	static class ObjResultFromIndexResult<V, E> implements CoresAlgo.Result<V, E> {

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

}
