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
import it.unimi.dsi.fastutil.ints.IntCollections;
import it.unimi.dsi.fastutil.ints.IntLists;

class MatchingImpl implements Matching {

	private final Graph g;
	private IntCollection edges;
	private int[] matched;

	MatchingImpl(Graph g, IntCollection edges) {
		this.g = Objects.requireNonNull(g);
		this.edges = IntCollections.unmodifiable(Objects.requireNonNull(edges));
	}

	MatchingImpl(Graph g, int[] matched) {
		assert matched.length == g.vertices().size();
		this.g = Objects.requireNonNull(g);
		this.matched = Objects.requireNonNull(matched);
	}

	@Override
	public boolean isVertexMatched(int vertex) {
		computeMatchedArray();
		return matched[vertex] != -1;
	}

	@Override
	public boolean containsEdge(int edge) {
		computeMatchedArray();
		return matched[g.edgeSource(edge)] == edge;
	}

	@Override
	public IntCollection edges() {
		computeEdgesCollection();
		return edges;
	}

	@Override
	public double weight(EdgeWeightFunc w) {
		computeEdgesCollection();
		return GraphsUtils.edgesWeightSum(edges.iterator(), w);
	}

	private void computeEdgesCollection() {
		if (edges != null)
			return;
		IntArrayList edges0 = new IntArrayList();
		int n = g.vertices().size();
		for (int v = 0; v < n; v++) {
			int e = matched[v];
			if (e == -1)
				continue;
			if (v <= g.edgeEndpoint(e, v))
				edges0.add(e);
		}
		edges = IntLists.unmodifiable(edges0);
	}

	private void computeMatchedArray() {
		if (matched != null)
			return;
		matched = new int[g.vertices().size()];
		Arrays.fill(matched, -1);
		for (int e : edges) {
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			if (matched[u] != -1)
				throw new IllegalArgumentException("vertex is matched twice: " + u);
			matched[u] = e;
			if (matched[v] != -1)
				throw new IllegalArgumentException("vertex is matched twice: " + v);
			matched[v] = e;
		}
	}

	@Override
	public String toString() {
		return edges().toString();
	}

}
