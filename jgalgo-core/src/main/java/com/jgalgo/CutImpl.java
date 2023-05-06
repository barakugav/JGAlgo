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
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntLists;

class CutImpl implements Cut {

	private final Graph g;
	private BitSet cutBitmap;
	private IntCollection cutVertices;
	private IntCollection crossEdges;

	CutImpl(Graph g, IntCollection cutVertices) {
		this.g = Objects.requireNonNull(g);
		this.cutVertices = IntCollections.unmodifiable(Objects.requireNonNull(cutVertices));
	}

	CutImpl(Graph g, BitSet cutBitmap) {
		this.g = Objects.requireNonNull(g);
		this.cutBitmap = Objects.requireNonNull(cutBitmap);
	}

	@Override
	public boolean containsVertex(int v) {
		computeCutBitmap();
		return cutBitmap.get(v);
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
	public double weight(EdgeWeightFunc w) {
		computeCrossEdgesCollection();
		return GraphsUtils.edgesWeightSum(crossEdges.iterator(), w);
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
		for (IntIterator it = cutVertices.iterator(); it.hasNext();)
			cutBitmap.set(it.nextInt());
	}

	private void computeCrossEdgesCollection() {
		if (crossEdges != null)
			return;
		computeCutBitmap();
		IntArrayList crossEdges0 = new IntArrayList();
		for (IntIterator uit = Utils.bitSetIterator(cutBitmap); uit.hasNext();) {
			int u = uit.nextInt();
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.target();
				if (!cutBitmap.get(v))
					crossEdges0.add(e);
			}
		}
		crossEdges0.trim();
		crossEdges = IntLists.unmodifiable(crossEdges0);
	}

}
