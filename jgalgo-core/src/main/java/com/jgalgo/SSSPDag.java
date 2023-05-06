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

import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * Linear Single Source Shortest Path (SSSP) algorithm for directed acyclic graphs (DAG).
 * <p>
 * The algorithm first compute a topological sorting of the vertices in linear time, and then traverse the vertices in
 * that order and determine the distance for each one of them.
 *
 * @see    TopologicalOrderAlgorithm
 * @author Barak Ugav
 */
public class SSSPDag implements SSSP {

	private final TopologicalOrderAlgorithm topoAlg = TopologicalOrderAlgorithm.newBuilder().build();

	/**
	 * Construct a new SSSP algorithm object.
	 */
	public SSSPDag() {}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if graph is not directed or contains cycles
	 */
	@Override
	public SSSPDag.Result computeShortestPaths(Graph g, EdgeWeightFunc w, int source) {
		ArgumentCheck.onlyDirected(g);
		SSSPResultImpl res = new SSSPResultImpl(g, source);
		res.distances[source] = 0;

		boolean sourceSeen = false;
		for (IntIterator uit = topoAlg.computeTopologicalSorting(g).verticesIterator(); uit.hasNext();) {
			int u = uit.nextInt();
			if (!sourceSeen) {
				if (u != source)
					continue;
				sourceSeen = true;
			}
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.target();
				double d = res.distances[u] + w.weight(e);
				if (d < res.distances[v]) {
					res.distances[v] = d;
					res.backtrack[v] = e;
				}
			}
		}

		return res;
	}

}
