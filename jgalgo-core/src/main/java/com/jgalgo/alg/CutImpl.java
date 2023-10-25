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

import java.util.BitSet;
import java.util.Objects;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntSet;

class CutImpl implements Cut {

	private final IndexGraph g;
	private BitSet cutBitmap;
	private IntSet cutVertices;
	private IntSet crossEdges;

	CutImpl(IndexGraph g, BitSet cutBitmap) {
		this.g = Objects.requireNonNull(g);
		this.cutBitmap = Objects.requireNonNull(cutBitmap);
	}

	@Override
	public boolean containsVertex(int vertex) {
		computeCutBitmap();
		return cutBitmap.get(vertex);
	}

	@Override
	public IntSet vertices() {
		if (cutVertices == null) {
			cutVertices = new ImmutableIntArraySet(JGAlgoUtils.toArray(cutBitmap)) {
				@Override
				public boolean contains(int v) {
					return 0 <= v && v < g.vertices().size() && cutBitmap.get(v);
				}
			};
		}
		return cutVertices;
	}

	@Override
	public IntSet edges() {
		if (crossEdges == null) {
			computeCutBitmap();
			int crossEdgesNum = 0;
			for (int u : JGAlgoUtils.iterable(cutBitmap)) {
				for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					if (!cutBitmap.get(eit.target()))
						crossEdgesNum++;
				}
			}
			int[] crossEdges0 = new int[crossEdgesNum];
			crossEdgesNum = 0;
			for (int u : JGAlgoUtils.iterable(cutBitmap)) {
				for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					if (!cutBitmap.get(eit.target()))
						crossEdges0[crossEdgesNum++] = e;
				}
			}
			crossEdges = new ImmutableIntArraySet(crossEdges0) {
				@Override
				public boolean contains(int e) {
					return 0 <= e && e <= g.edges().size()
							&& (cutBitmap.get(g.edgeSource(e)) ^ !cutBitmap.get(g.edgeTarget(e)));
				}
			};
		}
		return crossEdges;
	}

	private void computeCutBitmap() {
		if (cutBitmap != null)
			return;
		cutBitmap = new BitSet(g.vertices().size());
		for (int v : cutVertices)
			cutBitmap.set(v);
	}

	@Override
	public String toString() {
		computeCutBitmap();
		return cutBitmap.toString();
	}

	static class CutFromIndexCut implements Cut {

		private final Cut cut;
		private final IndexIdMap viMap;
		private final IndexIdMap eiMap;

		CutFromIndexCut(Cut cut, IndexIdMap viMap, IndexIdMap eiMap) {
			this.cut = Objects.requireNonNull(cut);
			this.viMap = Objects.requireNonNull(viMap);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		@Override
		public boolean containsVertex(int vertex) {
			return cut.containsVertex(viMap.idToIndex(vertex));
		}

		@Override
		public IntSet vertices() {
			return IndexIdMaps.indexToIdSet(cut.vertices(), viMap);
		}

		@Override
		public IntSet edges() {
			return IndexIdMaps.indexToIdSet(cut.edges(), eiMap);
		}

	}

}
