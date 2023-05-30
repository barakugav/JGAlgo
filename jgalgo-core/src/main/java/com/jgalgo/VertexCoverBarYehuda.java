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
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;

/**
 * Bar Yehuda's vertex cover algorithm.
 * <p>
 * Runs in linear time.
 * <p>
 * Based on 'A linear-time approximation algorithm for the weighted vertex cover problem' by R Bar-Yehuda.
 *
 * @author Barak Ugav
 */
class VertexCoverBarYehuda implements VertexCover {

	@Override
	public Result computeMinimumVertexCover(Graph g, WeightFunction w) {
		final int n = g.vertices().size();
		double[] sw = new double[n];
		for (int v = 0; v < n; v++)
			sw[v] = w.weight(v);

		BitSet cover = new BitSet(n);

		for (int e : g.edges()) {
			int u, v;
			if (cover.get(u = g.edgeSource(e)) || cover.get(v = g.edgeTarget(e)))
				continue;
			if (sw[u] <= sw[v]) {
				sw[v] -= sw[u];
				// sw[u] -= sw[u]; don't bother
				cover.set(u);
			} else {
				sw[u] -= sw[v];
				// sw[v] -= sw[v]; don't bother
				cover.set(v);
			}
		}

		return new Result(g, cover);
	}

	private static class Result implements VertexCover.Result {

		private final Graph g;
		private final BitSet cover;
		private IntCollection vertices;

		Result(Graph g, BitSet cover) {
			this.g = Objects.requireNonNull(g);
			this.cover = Objects.requireNonNull(cover);
		}

		@Override
		public IntCollection vertices() {
			if (this.vertices == null) {
				IntList vertices = new IntArrayList(cover.cardinality());
				for (int v : Utils.iterable(cover))
					vertices.add(v);
				this.vertices = IntLists.unmodifiable(vertices);
			}
			return this.vertices;
		}

		@Override
		public boolean isInCover(int vertex) {
			if (!g.vertices().contains(vertex))
				throw new IndexOutOfBoundsException(vertex);
			return cover.get(vertex);
		}

		@Override
		public double weight(WeightFunction w) {
			return GraphsUtils.weightSum(vertices(), w);
		}

	}

}
