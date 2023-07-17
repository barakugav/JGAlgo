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
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntCollections;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;

class MatchingImpl implements Matching {

	private final IndexGraph g;
	private IntCollection edges;
	private int[] matched;
	private IntCollection matchedVertices;
	private IntCollection unmatchedVertices;

	MatchingImpl(IndexGraph g, IntCollection edges) {
		this.g = Objects.requireNonNull(g);
		this.edges = IntCollections.unmodifiable(Objects.requireNonNull(edges));
	}

	MatchingImpl(IndexGraph g, int[] matched) {
		assert matched.length == g.vertices().size();
		this.g = Objects.requireNonNull(g);
		this.matched = Objects.requireNonNull(matched);
	}

	@Override
	public boolean isVertexMatched(int vertex) {
		return getMatchedEdge(vertex) != -1;
	}

	@Override
	public IntCollection matchedVertices() {
		if (matchedVertices == null) {
			computeMatchedArray();
			IntList matchedVertices0 = new IntArrayList();
			for (int v = 0; v < matched.length; v++)
				if (matched[v] != -1)
					matchedVertices0.add(v);
			matchedVertices = matchedVertices0;
		}
		return matchedVertices;
	}

	@Override
	public IntCollection unmatchedVertices() {
		if (unmatchedVertices == null) {
			computeMatchedArray();
			IntList unmatchedVertices0 = new IntArrayList();
			for (int v = 0; v < matched.length; v++)
				if (matched[v] == -1)
					unmatchedVertices0.add(v);
			unmatchedVertices = unmatchedVertices0;
		}
		return unmatchedVertices;
	}

	@Override
	public int getMatchedEdge(int vertex) {
		computeMatchedArray();
		return matched[vertex];
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
	public double weight(WeightFunction w) {
		computeEdgesCollection();
		return GraphsUtils.weightSum(edges, w);
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
			if (v == g.edgeSource(e))
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

	@Override
	public boolean isPerfect() {
		computeMatchedArray();
		for (int e : matched)
			if (e == -1)
				return false;
		return true;
	}

}
