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

import java.util.function.DoubleSupplier;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;

class PageRank {

	private int iterations = 100;
	private double tolerance = 0.0001;
	private double dampingFactor = 0.85;

	VertexScoring computeScores(Graph g, WeightFunction w) {
		if (g instanceof IndexGraph)
			return computeScores((IndexGraph) g, w);

		IndexGraph iGraph = g.indexGraph();
		IndexIdMap viMap = g.indexGraphVerticesMap();
		IndexIdMap eiMap = g.indexGraphEdgesMap();
		w = IndexIdMaps.idToIndexWeightFunc(w, eiMap);

		VertexScoring indexResult = computeScores(iGraph, w);
		return new VertexScoringImpl.ResultFromIndexResult(indexResult, viMap);
	}

	VertexScoring computeScores(IndexGraph g, WeightFunction w) {
		final int n = g.vertices().size();

		int[] outDegree = new int[n];
		double[] scores = new double[n];
		double[] transferredScores = new double[n];
		Predecessors ng = new Predecessors(g, w);

		for (int v = 0; v < n; v++) {
			outDegree[v] = g.outEdges(v).size();
			scores[v] = 1.0 / n;
		}

		DoubleSupplier randomFactor = () -> {
			double rFactor = 0;
			for (int v = 0; v < n; v++)
				rFactor += outDegree[v] > 0 ? (1 - dampingFactor) * scores[v] : scores[v];
			return rFactor / n;
		};

		if (w == null || w == WeightFunction.CardinalityWeightFunction) {
			for (int iters = 0; iters < iterations; iters++) {
				double rFactor = randomFactor.getAsDouble();

				for (int v = 0; v < n; v++)
					transferredScores[v] = scores[v] / outDegree[v];

				double maxChange = 0;
				for (int v = 0; v < n; v++) {
					double score = 0;
					for (int num = outDegree[v], eIdx = 0; eIdx < num; eIdx++)
						score += transferredScores[ng.predecessor(v, eIdx)];
					score = rFactor + dampingFactor * score;
					maxChange = Math.max(maxChange, Math.abs(scores[v] - score));
					scores[v] = score;
				}
				if (maxChange < tolerance)
					break;
			}

		} else {
			double[] weightSum = new double[n];
			for (int u = 0; u < n; u++)
				weightSum[u] = w.weightSum(g.outEdges(u));

			for (int iters = 0; iters < iterations; iters++) {
				double rFactor = randomFactor.getAsDouble();

				for (int v = 0; v < n; v++)
					transferredScores[v] = scores[v] / weightSum[v];

				double maxChange = 0;
				for (int v = 0; v < n; v++) {
					double score = 0;
					for (int num = outDegree[v], eIdx = 0; eIdx < num; eIdx++)
						score += transferredScores[ng.predecessor(v, eIdx)] * ng.edgeWeight(v, eIdx);
					score = rFactor + dampingFactor * score;
					maxChange = Math.max(maxChange, Math.abs(scores[v] - score));
					scores[v] = score;
				}
				if (maxChange < tolerance)
					break;
			}

		}

		return new VertexScoringImpl.ResultImpl(scores);
	}

	private static class Predecessors {

		private final int[] neighbors;
		private final int[] neighborsBegin;
		private final double[] weights;

		Predecessors(IndexGraph g, WeightFunction w) {
			final int n = g.vertices().size();
			final int m = g.edges().size();
			neighborsBegin = new int[n + 1];

			if (g.getCapabilities().directed()) {
				neighbors = new int[m];
			} else {
				int outDegreeSum = 0;
				for (int u = 0; u < n; u++)
					outDegreeSum += g.outEdges(u).size();
				neighbors = new int[outDegreeSum];
			}

			if (w == null || w == WeightFunction.CardinalityWeightFunction) {
				weights = null;
				for (int eIdx = 0, u = 0; u < n; u++) {
					neighborsBegin[u] = eIdx;
					for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
						eit.nextInt();
						neighbors[eIdx++] = eit.target();
					}
				}
			} else {
				weights = new double[neighbors.length];
				for (int eIdx = 0, u = 0; u < n; u++) {
					neighborsBegin[u] = eIdx;
					for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						neighbors[eIdx] = eit.target();
						weights[eIdx] = w.weight(e);
						eIdx++;
					}
				}
			}
			neighborsBegin[n] = neighbors.length;
		}

		int predecessor(int source, int edgeIdx) {
			return neighbors[neighborsBegin[source] + edgeIdx];
		}

		double edgeWeight(int source, int edgeIdx) {
			return weights[neighborsBegin[source] + edgeIdx];
		}

	}

}
