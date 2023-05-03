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

import it.unimi.dsi.fastutil.booleans.BooleanArrays;
import it.unimi.dsi.fastutil.doubles.DoubleArrays;
import it.unimi.dsi.fastutil.doubles.DoubleBigArrays;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntBigArrays;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Dasdan and Gupta algorithm for minimum mean cycle.
 * <p>
 * The algorithm runs in \(O(n m)\) time and uses \(O(n^2)\) space. Although this algorithm have a strong polynomial
 * bound, {@link MinimumMeanCycleHoward} is usually faster.
 * <p>
 * Based on 'Efficient algorithms for optimum cycle mean and optimum cost to time ratio problems' by Ali Dasdan, Sandy
 * S. Irani, Rajesh K. Gupta (1999).
 *
 * @author Barak Ugav
 */
public class MinimumMeanCycleDasdanGupta implements MinimumMeanCycle {

	private final ConnectivityAlgorithm ccAlg = ConnectivityAlgorithm.newBuilder().build();
	private IntList[] ccVertices = MemoryReuse.EmptyIntListArr;
	private double[][] d = DoubleBigArrays.EMPTY_BIG_ARRAY;
	private int[][] policy = IntBigArrays.EMPTY_BIG_ARRAY;
	private boolean[] visit1 = BooleanArrays.EMPTY_ARRAY;
	private boolean[] visit2 = BooleanArrays.EMPTY_ARRAY;
	private double[] pathWeights = DoubleArrays.EMPTY_ARRAY;
	private int[] path = IntArrays.EMPTY_ARRAY;
	private static final double EPS = 0.00001;

	/**
	 * Create a new minimum mean cycle algorithm.
	 */
	public MinimumMeanCycleDasdanGupta() {}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not directed
	 */
	@Override
	public Path computeMinimumMeanCycle(Graph g, EdgeWeightFunc w) {
		if (!g.getCapabilities().directed())
			throw new IllegalArgumentException("only directed graphs are supported");
		int n = g.vertices().size();

		/* find all SCC */
		ConnectivityAlgorithm.Result cc = ccAlg.computeConnectivityComponents(g);
		int ccNum = cc.getNumberOfCC();
		IntList[] ccVertices = this.ccVertices = MemoryReuse.ensureLength(this.ccVertices, ccNum);
		for (int c = 0; c < ccNum; c++) {
			ccVertices[c] = MemoryReuse.ensureAllocated(ccVertices[c], IntArrayList::new);
			assert ccVertices[c].isEmpty();
		}
		for (int u = 0; u < n; u++)
			ccVertices[cc.getVertexCc(u)].add(u);

		/* init distances and policy */
		int maxCcSize = -1;
		for (int c = 0; c < ccNum; c++)
			maxCcSize = Math.max(maxCcSize, ccVertices[c].size());
		double[][] d = this.d = MemoryReuse.ensureLength(this.d, maxCcSize + 1, n);
		int[][] policy = this.policy = MemoryReuse.ensureLength(this.policy, maxCcSize + 1, n);
		for (int k = 0; k < maxCcSize + 1; k++) {
			Arrays.fill(d[k], 0, n, Double.POSITIVE_INFINITY);
			Arrays.fill(policy[k], 0, n, -1);
		}
		boolean[] visit1 = this.visit1 = MemoryReuse.ensureLength(this.visit1, n);
		boolean[] visit2 = this.visit2 = MemoryReuse.ensureLength(this.visit2, n);
		Arrays.fill(visit1, 0, n, false);
		Arrays.fill(visit2, 0, n, false);

		/* operate on each SCC separately */
		double bestCycleMeanWeight = Double.POSITIVE_INFINITY;
		int bestCycleMeanWeightVertex = -1;
		int bestCycleLength = -1;
		for (int ccIdx = 0; ccIdx < ccNum; ccIdx++) {
			final int ccSize = ccVertices[ccIdx].size();
			if (ccSize < 2)
				continue;

			int source = ccVertices[ccIdx].iterator().nextInt();
			boolean[] firstVisit = visit1;
			d[0][source] = 0;
			policy[0][source] = -1;
			firstVisit[source] = true;

			for (int k = 0; k < ccSize; k++) {
				boolean[] visit = k % 2 == 0 ? visit1 : visit2;
				boolean[] visitNext = k % 2 == 0 ? visit2 : visit1;
				for (IntIterator uit = ccVertices[ccIdx].iterator(); uit.hasNext();) {
					int u = uit.nextInt();
					if (!visit[u])
						continue;
					visit[u] = false;
					for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
						int e = eit.nextInt();
						int v = eit.v();
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
			for (IntIterator uit = ccVertices[ccIdx].iterator(); uit.hasNext();) {
				int u = uit.nextInt();
				if (!lastVisit[u])
					continue;
				double bestVertexCycleMeanWeight = Double.NEGATIVE_INFINITY;
				int bestVertexCycleLength = -1;
				for (int k = 0; k < ccSize; k++) {
					double cycleMeanWeight = (d[ccSize][u] - d[k][u]) / (ccSize - k);
					if (bestVertexCycleMeanWeight < cycleMeanWeight) {
						bestVertexCycleMeanWeight = cycleMeanWeight;
						bestVertexCycleLength = ccSize - k;
					}
				}
				if (bestCycleMeanWeight > bestVertexCycleMeanWeight) {
					bestCycleMeanWeight = bestVertexCycleMeanWeight;
					bestCycleMeanWeightVertex = u;
					bestCycleLength = bestVertexCycleLength;
				}
			}
		}

		Path cycle = null;
		if (bestCycleMeanWeightVertex != -1) {
			final int ccIdx = cc.getVertexCc(bestCycleMeanWeightVertex);
			final int ccSize = ccVertices[ccIdx].size();
			int[] path = this.path = MemoryReuse.ensureLength(this.path, ccSize);
			for (int k = ccSize, v = bestCycleMeanWeightVertex; k > 0; k--) {
				int e = path[k - 1] = policy[k][v];
				v = g.edgeSource(e);
			}
			double[] pathWeights = this.pathWeights = MemoryReuse.ensureLength(this.pathWeights, ccSize + 1);
			pathWeights[0] = 0;
			for (int k = 1; k < ccSize + 1; k++)
				pathWeights[k] = pathWeights[k - 1] + w.weight(path[k - 1]);

			int len = bestCycleLength;
			for (int k = 0; k <= ccSize - bestCycleLength; k++) {
				if (g.edgeSource(path[k]) != g.edgeTarget(path[k + len - 1]))
					continue;
				if (Math.abs((pathWeights[k + len] - pathWeights[k]) - bestCycleMeanWeight * len) < EPS) {
					IntList cycleList = new IntArrayList(path, k, len);
					int cycleVertex = g.edgeSource(cycleList.getInt(0));
					cycle = new Path(g, cycleVertex, cycleVertex, cycleList);
					break;
				}
			}
			assert cycle != null;
		}

		for (int c = 0; c < ccNum; c++)
			ccVertices[c].clear();
		return cycle;
	}

}
