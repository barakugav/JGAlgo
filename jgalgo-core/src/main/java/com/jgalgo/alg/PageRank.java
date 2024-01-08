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
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;

class PageRank {

	// TODO this file should be public API

	private int iterations = 100;
	private double tolerance = 1e-4;
	private double dampingFactor = 0.85;

	@SuppressWarnings("unchecked")
	<V, E> VertexScoring<V, E> computeScores(Graph<V, E> g, WeightFunction<E> w) {
		if (g instanceof IndexGraph) {
			IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
			return (VertexScoring<V, E>) computeScores((IndexGraph) g, w0);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			IVertexScoring indexResult = computeScores(iGraph, iw);
			return VertexScoringImpl.resultFromIndexResult(g, indexResult);
		}
	}

	private IVertexScoring computeScores(IndexGraph g, IWeightFunction w) {
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

		if (WeightFunction.isCardinality(w)) {
			for (int iters = 0; iters < iterations; iters++) {
				double rFactor = randomFactor.getAsDouble();

				for (int v = 0; v < n; v++)
					if (outDegree[v] != 0)
						transferredScores[v] = scores[v] / outDegree[v];

				double maxChange = 0;
				for (int v = 0; v < n; v++) {
					double score = 0;
					for (Predecessors.Iter it = ng.predecessors(v); it.hasNext(); it.advance())
						score += transferredScores[it.vertex()];
					score = rFactor + dampingFactor * score;
					maxChange = Math.max(maxChange, Math.abs(scores[v] - score));
					scores[v] = score;
				}
				if (maxChange < tolerance)
					break;
			}

		} else {
			double[] weightSum = new double[n];
			for (int v = 0; v < n; v++)
				weightSum[v] = w.weightSum(g.outEdges(v));

			for (int iters = 0; iters < iterations; iters++) {
				double rFactor = randomFactor.getAsDouble();

				for (int v = 0; v < n; v++)
					if (!g.outEdges(v).isEmpty())
						transferredScores[v] = scores[v] / weightSum[v];

				double maxChange = 0;
				for (int v = 0; v < n; v++) {
					double score = 0;
					for (Predecessors.Iter it = ng.predecessors(v); it.hasNext(); it.advance())
						score += transferredScores[it.vertex()] * it.weight();
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

		private final int[] predecessors;
		private final int[] predecessorsBegin;
		private final double[] weights;

		Predecessors(IndexGraph g, IWeightFunction w) {
			final int n = g.vertices().size();
			final int m = g.edges().size();
			predecessorsBegin = new int[n + 1];

			if (g.isDirected()) {
				predecessors = new int[m];
			} else {
				int outDegreeSum = 0;
				for (int u = 0; u < n; u++)
					outDegreeSum += g.outEdges(u).size();
				predecessors = new int[outDegreeSum];
			}

			if (WeightFunction.isCardinality(w)) {
				weights = null;
				for (int eIdx = 0, v = 0; v < n; v++) {
					predecessorsBegin[v] = eIdx;
					for (IEdgeIter eit = g.inEdges(v).iterator(); eit.hasNext();) {
						eit.nextInt();
						predecessors[eIdx++] = eit.sourceInt();
					}
				}
			} else {
				weights = new double[predecessors.length];
				for (int eIdx = 0, v = 0; v < n; v++) {
					predecessorsBegin[v] = eIdx;
					for (IEdgeIter eit = g.inEdges(v).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						predecessors[eIdx] = eit.sourceInt();
						weights[eIdx] = w.weight(e);
						eIdx++;
					}
				}
			}
			predecessorsBegin[n] = predecessors.length;
		}

		Predecessors.Iter predecessors(int target) {
			return new Predecessors.Iter(target);
		}

		class Iter {
			private int index;
			private final int end;

			Iter(int target) {
				index = predecessorsBegin[target];
				end = predecessorsBegin[target + 1];
			}

			boolean hasNext() {
				return index < end;
			}

			void advance() {
				index++;
			}

			int vertex() {
				return predecessors[index];
			}

			double weight() {
				return weights[index];
			}
		}

	}

}
