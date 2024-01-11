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

import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * Linear Single Source Shortest Path (SSSP) algorithm for directed acyclic graphs (DAG).
 *
 * <p>
 * The algorithm first compute a topological sorting of the vertices in linear time, and then traverse the vertices in
 * that order and determine the distance for each one of them.
 *
 * @see    TopologicalOrderAlgo
 * @author Barak Ugav
 */
class ShortestPathSingleSourceDag implements ShortestPathSingleSourceBase {

	private final TopologicalOrderAlgo topoAlg = TopologicalOrderAlgo.newInstance();

	/**
	 * Construct a new SSSP algorithm.
	 */
	ShortestPathSingleSourceDag() {}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if graph is not directed or contains cycles
	 */
	@Override
	public ShortestPathSingleSourceDag.IResult computeShortestPaths(IndexGraph g, IWeightFunction w, int source) {
		Assertions.onlyDirected(g);
		w = IWeightFunction.replaceNullWeightFunc(w);
		return WeightFunction.isInteger(w) ? computeSsspInt(g, (IWeightFunctionInt) w, source)
				: computeSsspDouble(g, w, source);
	}

	private ShortestPathSingleSourceDag.IResult computeSsspDouble(IndexGraph g, IWeightFunction w, int source) {
		ShortestPathSingleSourceUtils.IndexResult res = new ShortestPathSingleSourceUtils.IndexResult(g, source);
		res.distances[source] = 0;

		TopologicalOrderAlgo.IResult topoOrder = (TopologicalOrderAlgo.IResult) topoAlg.computeTopologicalSorting(g);
		for (IntIterator uit = topoOrder.orderedVertices().listIterator(topoOrder.vertexOrderIndex(source)); uit
				.hasNext();) {
			int u = uit.nextInt();
			double uDistance = res.distances[u];
			if (uDistance == Double.POSITIVE_INFINITY)
				continue;
			for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.targetInt();
				double d = uDistance + w.weight(e);
				if (d < res.distances[v]) {
					res.distances[v] = d;
					res.backtrack[v] = e;
				}
			}
		}
		return res;
	}

	private ShortestPathSingleSourceDag.IResult computeSsspInt(IndexGraph g, IWeightFunctionInt w, int source) {
		ShortestPathSingleSourceUtils.IndexResult res = new ShortestPathSingleSourceUtils.IndexResult(g, source);
		res.distances[source] = 0;

		TopologicalOrderAlgo.IResult topoOrder = (TopologicalOrderAlgo.IResult) topoAlg.computeTopologicalSorting(g);
		for (IntIterator uit = topoOrder.orderedVertices().listIterator(topoOrder.vertexOrderIndex(source)); uit
				.hasNext();) {
			int u = uit.nextInt();
			if (res.distances[u] == Double.POSITIVE_INFINITY)
				continue;
			int uDistance = (int) res.distances[u];
			for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.targetInt();
				int d = uDistance + w.weightInt(e);
				if (d < (int) res.distances[v]) {
					res.distances[v] = d;
					res.backtrack[v] = e;
				}
			}
		}
		return res;
	}

}
