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
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Dasdan and Gupta algorithm for minimum mean cycle.
 * <p>
 * The algorithm runs in \(O(n m)\) time and uses \(O(n^2)\) space. Although this algorithm have a strong polynomial
 * bound, {@link MinimumMeanCycleHoward} is usually faster.
 * <p>
 * Based on 'Faster Maximum and Minimum Mean Cycle Algorithms for System Performance Analysis' by Ali Dasdan, Rajesh K.
 * Gupta (1997).
 *
 * @author Barak Ugav
 */
class MinimumMeanCycleDasdanGupta extends MinimumMeanCycleAbstract {

	private final ConnectedComponentsAlgo ccAlg = ConnectedComponentsAlgo.newBuilder().build();
	private static final double EPS = 0.00001;

	/* Although the paper suggest a value of 10, this seems only to slow us down */
	private static final int SourceChoosingUnfoldingDepth = 0;

	/**
	 * Create a new minimum mean cycle algorithm.
	 */
	MinimumMeanCycleDasdanGupta() {}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not directed
	 */
	@Override
	Path computeMinimumMeanCycle(IndexGraph g, WeightFunction w) {
		Assertions.Graphs.onlyDirected(g);
		w = WeightFunctions.localEdgeWeightFunction(g, w);

		Path cycle = computeMinimumMeanCycle0(g, w);

		/* The regular algorithm doesn't handle self loops (and specifically skip CC with a single vertex) */
		if (g.getCapabilities().selfEdges()) {
			int bestSelfLoop = -1;
			double bestSelfLoopWeight = Double.POSITIVE_INFINITY;
			for (int e : g.edges()) {
				if (g.edgeSource(e) != g.edgeTarget(e))
					continue;
				double ew = w.weight(e);
				if (ew < bestSelfLoopWeight) {
					bestSelfLoop = e;
					bestSelfLoopWeight = ew;
				}
			}
			if (bestSelfLoop != -1) {
				double bestCycleWeight = cycle != null ? cycle.weight(w) : Double.POSITIVE_INFINITY;
				if (bestCycleWeight > bestCycleWeight) {
					int loopVertex = g.edgeSource(bestSelfLoop);
					cycle = new PathImpl(g, loopVertex, loopVertex, IntList.of(bestSelfLoop));
				}
			}
		}

		return cycle;
	}

	private Path computeMinimumMeanCycle0(IndexGraph g, WeightFunction w) {
		final int n = g.vertices().size();

		/* find all SCC */
		ConnectedComponentsAlgo.Result cc = ccAlg.computeConnectivityComponents(g);
		final int ccNum = cc.getNumberOfCcs();
		SourceChooser sourceChooser = new SourceChooser(g, cc);

		/* init distances and policy */
		int maxCcSize = -1;
		for (int c = 0; c < ccNum; c++)
			maxCcSize = Math.max(maxCcSize, cc.getCcVertices(c).size());
		double[][] d = new double[maxCcSize + 1][n];
		int[][] policy = new int[maxCcSize + 1][n];
		for (int k = 0; k < maxCcSize + 1; k++) {
			Arrays.fill(d[k], 0, n, Double.POSITIVE_INFINITY);
			Arrays.fill(policy[k], 0, n, -1);
		}
		boolean[] visit1 = new boolean[n];
		boolean[] visit2 = new boolean[n];

		/* operate on each SCC separately */
		double bestCycleMeanWeight = Double.POSITIVE_INFINITY;
		int bestCycleMeanWeightVertex = -1;
		final IntList bestCycleLengths = new IntArrayList();
		IntList bestVertexCycleLengths = new IntArrayList();
		for (int ccIdx = 0; ccIdx < ccNum; ccIdx++) {
			final int ccSize = cc.getCcVertices(ccIdx).size();
			if (ccSize < 2)
				continue;

			int source = sourceChooser.chooseSource(ccIdx);
			boolean[] firstVisit = visit1;
			d[0][source] = 0;
			policy[0][source] = -1;
			firstVisit[source] = true;

			for (int k = 0; k < ccSize; k++) {
				boolean[] visit = k % 2 == 0 ? visit1 : visit2;
				boolean[] visitNext = k % 2 == 0 ? visit2 : visit1;
				for (int u : cc.getCcVertices(ccIdx)) {
					if (!visit[u])
						continue;
					visit[u] = false;
					for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						int v = eit.target();
						if (cc.getVertexCc(v) != ccIdx)
							continue;
						double newDistance = d[k][u] + w.weight(e);
						if (d[k + 1][v] > newDistance) {
							d[k + 1][v] = newDistance;
							policy[k + 1][v] = e;
							visitNext[v] = true;
						}
					}
				}
			}

			boolean[] lastVisit = ccSize % 2 == 0 ? visit1 : visit2;
			for (int u : cc.getCcVertices(ccIdx)) {
				if (!lastVisit[u])
					continue;
				double bestVertexCycleMeanWeight = Double.NEGATIVE_INFINITY;
				bestVertexCycleLengths.clear();
				for (int k = 0; k < ccSize; k++) {
					int len = ccSize - k;
					double cycleMeanWeight = (d[ccSize][u] - d[k][u]) / len;
					if (bestVertexCycleMeanWeight < cycleMeanWeight) {
						bestVertexCycleMeanWeight = cycleMeanWeight;
						bestVertexCycleLengths.clear();
						bestVertexCycleLengths.add(len);
					} else if (bestVertexCycleMeanWeight == cycleMeanWeight) {
						bestVertexCycleLengths.add(len);
					}
				}
				if (bestCycleMeanWeight > bestVertexCycleMeanWeight) {
					bestCycleMeanWeight = bestVertexCycleMeanWeight;
					bestCycleMeanWeightVertex = u;
					bestCycleLengths.clear();
					bestCycleLengths.addAll(bestVertexCycleLengths);
				}
			}
		}

		if (bestCycleMeanWeightVertex == -1)
			return null;
		final int ccIdx = cc.getVertexCc(bestCycleMeanWeightVertex);
		final int ccSize = cc.getCcVertices(ccIdx).size();
		int[] path = new int[ccSize];
		for (int k = ccSize, v = bestCycleMeanWeightVertex; k > 0; k--) {
			int e = path[k - 1] = policy[k][v];
			v = g.edgeSource(e);
		}
		double[] pathWeights = new double[ccSize + 1];
		pathWeights[0] = 0;
		for (int k = 1; k < ccSize + 1; k++)
			pathWeights[k] = pathWeights[k - 1] + w.weight(path[k - 1]);

		for (int len : bestCycleLengths) {
			for (int k = 0; k <= ccSize - len; k++) {
				if (g.edgeSource(path[k]) != g.edgeTarget(path[k + len - 1]))
					continue;
				if (Math.abs((pathWeights[k + len] - pathWeights[k]) - bestCycleMeanWeight * len) < EPS) {
					IntList cycleList = new IntArrayList(path, k, len);
					int cycleVertex = g.edgeSource(cycleList.getInt(0));
					return new PathImpl(g, cycleVertex, cycleVertex, cycleList);
				}
			}
		}
		throw new IllegalStateException("shouldn't happend! implementation bug");
	}

	@SuppressWarnings("unused")
	private static class SourceChooser {

		final IndexGraph g;
		final ConnectedComponentsAlgo.Result cc;
		final int[] interCcDegree;
		final int[] f;
		final int[] l;

		SourceChooser(IndexGraph g, ConnectedComponentsAlgo.Result cc) {
			this.g = g;
			this.cc = cc;

			if (SourceChoosingUnfoldingDepth > 0) {
				final int n = g.vertices().size();
				interCcDegree = new int[n];
				f = new int[n];
				l = new int[n];
				for (int u = 0; u < n; u++) {
					final int uCc = cc.getVertexCc(u);
					for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
						eit.nextInt();
						int v = eit.target();
						if (uCc == cc.getVertexCc(v))
							interCcDegree[u]++;
					}
				}
			} else {
				interCcDegree = null;
				f = null;
				l = null;
			}
		}

		int chooseSource(int ccIdx) {
			int source = cc.getCcVertices(ccIdx).iterator().nextInt();
			if (SourceChoosingUnfoldingDepth > 0) {
				/* choose a source by unfolding the graph N layers from each possible source */
				for (int v : cc.getCcVertices(ccIdx))
					f[v] = l[v] = 0;
				for (int i = 0; i < SourceChoosingUnfoldingDepth; i++) {
					for (int u : cc.getCcVertices(ccIdx)) {
						final int uCc = cc.getVertexCc(u);
						l[u] = interCcDegree[u];
						for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
							eit.nextInt();
							int v = eit.target();
							if (uCc == cc.getVertexCc(v))
								l[u] += f[v];
						}
					}
					for (int u : cc.getCcVertices(ccIdx))
						f[u] = l[u];
				}
				for (int u : cc.getCcVertices(ccIdx))
					if (l[source] > l[u])
						source = u;
			}
			return source;
		}
	}

}
