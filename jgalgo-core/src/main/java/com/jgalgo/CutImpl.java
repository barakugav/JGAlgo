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

import java.util.BitSet;
import java.util.Objects;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntCollections;
import it.unimi.dsi.fastutil.ints.IntLists;

class CutImpl implements Cut {

	private final IndexGraph g;
	private BitSet cutBitmap;
	private IntCollection cutVertices;
	private IntCollection crossEdges;

	CutImpl(IndexGraph g, IntCollection cutVertices) {
		this.g = Objects.requireNonNull(g);
		this.cutVertices = IntCollections.unmodifiable(Objects.requireNonNull(cutVertices));
	}

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
	public IntCollection vertices() {
		computeVerticesCollection();
		return cutVertices;
	}

	@Override
	public IntCollection edges() {
		computeCrossEdgesCollection();
		return cutVertices;
	}

	@Override
	public double weight(WeightFunction w) {
		computeCrossEdgesCollection();
		return GraphsUtils.weightSum(crossEdges, w);
	}

	private void computeVerticesCollection() {
		if (cutVertices != null)
			return;
		IntArrayList cutVertices0 = new IntArrayList();
		int n = g.vertices().size();
		for (int v = 0; v < n; v++)
			if (cutBitmap.get(v))
				cutVertices0.add(v);
		cutVertices0.trim();
		cutVertices = IntLists.unmodifiable(cutVertices0);
	}

	private void computeCutBitmap() {
		if (cutBitmap != null)
			return;
		cutBitmap = new BitSet(g.vertices().size());
		for (int v : cutVertices)
			cutBitmap.set(v);
	}

	private void computeCrossEdgesCollection() {
		if (crossEdges != null)
			return;
		computeCutBitmap();
		IntArrayList crossEdges0 = new IntArrayList();
		for (int u : Utils.iterable(cutBitmap)) {
			for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.target();
				if (!cutBitmap.get(v))
					crossEdges0.add(e);
			}
		}
		crossEdges0.trim();
		crossEdges = IntLists.unmodifiable(crossEdges0);
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
		public IntCollection vertices() {
			return new IndexIdMapUtils.CollectionFromIndexCollection(cut.vertices(), viMap);
		}

		@Override
		public IntCollection edges() {
			return new IndexIdMapUtils.CollectionFromIndexCollection(cut.edges(), eiMap);
		}

		@Override
		public double weight(WeightFunction w) {
			return cut.weight(WeightsImpl.indexWeightFuncFromIdWeightFunc(w, eiMap));
		}

	}

}
