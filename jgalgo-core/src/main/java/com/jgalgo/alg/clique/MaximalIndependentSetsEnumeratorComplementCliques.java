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
package com.jgalgo.alg.clique;

import static com.jgalgo.internal.util.Range.range;
import java.util.Iterator;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.internal.util.Bitmap;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Compute the maximal independent sets of a graph by computing the maximal cliques of the complement graph.
 *
 * @author Barak Ugav
 */
class MaximalIndependentSetsEnumeratorComplementCliques extends MaximalIndependentSetsEnumerators.AbstractImpl {

	private final MaximalCliquesEnumerator cliquesAlgo = MaximalCliquesEnumerator.newInstance();

	@Override
	Iterator<IntSet> maximalIndependentSetsIter(IndexGraph g) {
		final int n = g.vertices().size();
		Bitmap edges = new Bitmap(n * n);
		for (int e : range(g.edges().size())) {
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			if (u > v) {
				int tmp = u;
				u = v;
				v = tmp;
			}
			edges.set(u * n + v);
		}

		IndexGraphBuilder b = IndexGraphBuilder.newInstance(false);
		b.addVertices(range(n));
		for (int u : range(n))
			for (int v : range(u + 1, n))
				if (!edges.get(u * n + v))
					b.addEdge(u, v);
		IndexGraph complement = b.build();

		@SuppressWarnings({ "unchecked", "rawtypes" })
		Iterator<IntSet> resIter = (Iterator) cliquesAlgo.maximalCliquesIter(complement);
		return resIter;
	}

}
