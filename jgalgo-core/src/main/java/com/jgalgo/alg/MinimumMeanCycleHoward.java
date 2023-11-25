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

import java.util.Arrays;
import java.util.function.IntPredicate;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

/**
 * Howard's algorithm for minimum mean cycle detection.
 *
 * <p>
 * The algorithm runs in \(O(m N)\) and uses linear space, where \(N\) is product of the out-degrees of all the vertices
 * in the graph. Although this bound is not polynomial, this algorithm perform well in practice. There are other bounds
 * on the time such as \(O(n m \alpha)\) where \(\alpha\) is the number of simple cycles in the graph, or \(O(n^2 m
 * (MaxW-MinW)/\epsilon)\) where \(MaxW,MinW\) are the maximum and minimum edge weight in the graph, and \(\epsilon\) is
 * the precision of the algorithm.
 *
 * <p>
 * Based on 'Efficient Algorithms for Optimal Cycle Mean and Optimum Cost to Time Ratio Problems' by Ali Dasdan, Sandy
 * S. Irani, Rajesh K. Gupta (1999).
 *
 * @author Barak Ugav
 */
class MinimumMeanCycleHoward extends MinimumMeanCycleAbstract {

	private final StronglyConnectedComponentsAlgo sccAlg = StronglyConnectedComponentsAlgo.newInstance();

	private static final double EPS = 0.0001;

	/**
	 * Create a new minimum mean cycle algorithm.
	 */
	MinimumMeanCycleHoward() {}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not directed
	 */
	@Override
	IPath computeMinimumMeanCycle(IndexGraph g, IWeightFunction w) {
		Assertions.Graphs.onlyDirected(g);
		final int n = g.vertices().size();
		w = WeightFunctions.localEdgeWeightFunction(g, w);

		/* find all SCC */
		IVertexPartition cc = (IVertexPartition) sccAlg.findStronglyConnectedComponents(g);
		final int ccNum = cc.numberOfBlocks();

		/* init distances and policy */
		double[] d = new double[n];
		Arrays.fill(d, 0, n, Double.POSITIVE_INFINITY);
		int[] policy = new int[n];
		Arrays.fill(policy, 0, n, -1);
		for (int c = 0; c < ccNum; c++) {
			for (int e : cc.blockEdges(c)) {
				double ew = w.weight(e);
				int u = g.edgeSource(e);
				if (ew < d[u]) {
					d[u] = ew;
					policy[u] = e;
				}
			}
		}

		IntPriorityQueue queue = new FIFOQueueIntNoReduce();

		double overallBestCycleMeanWeight = Double.POSITIVE_INFINITY;
		int overallBestCycleVertex = -1;
		int nextSearchIdx = 1;
		int[] visitIdx = new int[n];
		Arrays.fill(visitIdx, 0, n, 0);
		/* operate on each SCC separately */
		for (int ccIdx = 0; ccIdx < ccNum; ccIdx++) {
			if (cc.blockVertices(ccIdx).size() < 2)
				continue;
			/* run in iteration as long as we find improvements */
			sccLoop: for (;;) {
				double bestCycleMeanWeight = Double.POSITIVE_INFINITY;
				int bestCycleVertex = -1;

				final int iterationFirstSearchIdx = nextSearchIdx;
				IntPredicate visited = v -> visitIdx[v] >= iterationFirstSearchIdx;
				/* DFS root loop */
				for (final int root : cc.blockVertices(ccIdx)) {
					if (visited.test(root))
						continue;
					final int searchIdx = nextSearchIdx++;

					/* Run DFS from root */
					int cycleVertex;
					for (int v = root;;) {
						visitIdx[v] = searchIdx;
						v = g.edgeTarget(policy[v]);
						if (visited.test(v)) {
							cycleVertex = visitIdx[v] == searchIdx ? v : -1;
							break;
						}
					}

					/* cycle found */
					if (cycleVertex != -1) {

						/* find cycle mean weight */
						double cycleWeight = 0;
						int cycleLength = 0;
						for (int v = cycleVertex;;) {
							int e = policy[v];
							cycleWeight += w.weight(e);
							cycleLength++;

							v = g.edgeTarget(e);
							if (v == cycleVertex)
								break;
						}

						/* compare to best */
						cycleWeight /= cycleLength;
						if (bestCycleMeanWeight > cycleWeight) {
							bestCycleMeanWeight = cycleWeight;
							bestCycleVertex = cycleVertex;
						}
					}
				}
				assert bestCycleVertex != -1;
				if (overallBestCycleMeanWeight > bestCycleMeanWeight) {
					overallBestCycleMeanWeight = bestCycleMeanWeight;
					overallBestCycleVertex = bestCycleVertex;
				}

				/* run a reversed BFS from a vertex in the best cycle */
				final int searchIdx = nextSearchIdx++;
				visitIdx[bestCycleVertex] = searchIdx;
				assert queue.isEmpty();
				queue.enqueue(bestCycleVertex);
				while (!queue.isEmpty()) {
					int v = queue.dequeueInt();
					for (IEdgeIter eit = g.inEdges(v).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						int u = g.edgeSource(e);
						if (policy[u] != e || visited.test(u))
							continue;
						/* update distance */
						d[u] += w.weight(e) - bestCycleMeanWeight;

						/* enqueue in BFS */
						visitIdx[u] = searchIdx;
						queue.enqueue(u);
					}
				}

				/* check for improvements */
				boolean improved = false;
				for (int e : cc.blockEdges(ccIdx)) {
					int u = g.edgeSource(e);
					int v = g.edgeTarget(e);
					double newDistance = d[v] + w.weight(e) - bestCycleMeanWeight;
					double delta = d[u] - newDistance;
					if (delta > 0) {
						if (delta > EPS)
							improved = true;
						d[u] = newDistance;
						policy[u] = e;
					}
				}
				if (!improved)
					break sccLoop;
			}
		}

		/* handle self edges separately, as the algorithm skip SCC of size one */
		if (g.isAllowSelfEdges()) {
			int bestSelfEdge = 1;
			double bestSelfEdgeWeight = Double.POSITIVE_INFINITY;
			for (int e : Graphs.selfEdges(g)) {
				if (w.weight(e) < bestSelfEdgeWeight) {
					bestSelfEdge = e;
					bestSelfEdgeWeight = w.weight(e);
				}
			}
			if (bestSelfEdge != -1 && bestSelfEdgeWeight < overallBestCycleMeanWeight) {
				int cycleVertex = g.edgeSource(bestSelfEdge);
				return new PathImpl(g, cycleVertex, cycleVertex, IntList.of(bestSelfEdge));
			}
		}

		if (overallBestCycleVertex == -1)
			return null;

		IntList cycle = new IntArrayList();
		for (int cycleVertex = overallBestCycleVertex, v = cycleVertex;;) {
			int e = policy[v];
			cycle.add(e);
			v = g.edgeTarget(e);
			if (v == cycleVertex)
				return new PathImpl(g, cycleVertex, cycleVertex, cycle);

			if (cycle.size() > n) {
				/*
				 * In case we have multiple cycles with the same minimum mean weight, the original
				 * overallBestCycleVertex might point to tail that leads to minimum mean cycle. For example:
				 * v1->v2,v2->v3,v3->v2, if start at v1 and search for a cycle ending at v1 we will never find it. If
				 * the cycle length is greater than n, we will not reach our original cycle.
				 */
				cycle.clear();
				cycleVertex = v;
			}
		}
	}

}
