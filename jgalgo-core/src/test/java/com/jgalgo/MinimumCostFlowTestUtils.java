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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.Weights;
import com.jgalgo.internal.util.RandomGraphBuilder;
import com.jgalgo.internal.util.TestUtils;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntIntPair;

class MinimumCostFlowTestUtils extends TestUtils {

	static void testRandDiGraphs(MinimumCostFlow algo, long seed) {
		final boolean directed = true;
		final SeedGenerator seedGen = new SeedGenerator(seed);
		Random rand = new Random(seedGen.nextSeed());
		List<Phase> phases = List.of(phase(32, 6, 6), phase(16, 16, 32), phase(16, 64, 128), phase(1, 512, 1324));
		runTestMultiple(phases, (testIter, args) -> {
			int n = args[0], m = args[1];
			Graph g = new RandomGraphBuilder(seedGen.nextSeed()).n(n).m(m).directed(directed).parallelEdges(false)
					.selfEdges(false).cycles(true).connected(false).build();

			Pair<FlowNetwork.Int, WeightFunction.Int> net = randNetwork(g, rand, true);
			IntIntPair sourceSink = MinimumCutSTTestUtils.chooseSourceSink(g, rand);

			testMinCostMaxFlow(g, net.first(), net.second(), sourceSink.firstInt(), sourceSink.secondInt(), algo);
		});
	}

	private static void testMinCostMaxFlow(Graph g, FlowNetwork.Int net, WeightFunction.Int cost, int source, int sink,
			MinimumCostFlow algo) {
		for (int e : g.edges())
			net.setFlow(e, 0);
		algo.computeMinCostMaxFlow(g, net, cost, source, sink);
		double totalFlow = net.getFlowSum(g, source);
		double totalCost = net.getCostSum(g.edges(), cost);

		MaximumFlowTestUtils.assertValidFlow(g, net, source, sink, totalFlow);

		IntIntPair expected = calcExpectedMinCostMaxFlow(g, net, cost, source, sink);
		double totalFlowExpected = expected.firstInt();
		double totalCostExpected = expected.secondInt();
		assertEquals(totalFlowExpected, totalFlow, 1E-3, "Unexpected max flow");
		// assertEquals(totalCostExpected, totalCost, 1E-3, "Unexpected flow cost");
		// the implementation from the internal doesn't deal well with negative costs, sometimes give sub-optimal
		// solution
		assertTrue(totalCostExpected >= totalCost - 1E-3, "Unexpected flow cost");
	}

	private static Pair<FlowNetwork.Int, WeightFunction.Int> randNetwork(Graph g, Random rand, boolean unitCapacity) {
		FlowNetwork.Int net = FlowNetwork.Int.createFromEdgeWeights(g);
		Weights.Int cost = Weights.createExternalEdgesWeights(g, int.class);
		for (int e : g.edges()) {
			net.setCapacity(e, rand.nextInt(1024));
			net.setFlow(e, 0);
			cost.set(e, rand.nextInt(1024) - 256);
		}
		return Pair.of(net, cost);
	}

	private static IntIntPair calcExpectedMinCostMaxFlow(Graph g, FlowNetwork.Int net, WeightFunction.Int cost0,
			int source, int sink) {
		IndexGraph iGraph = g.indexGraph();
		IndexIdMap viMap = g.indexGraphVerticesMap();
		IndexIdMap eiMap = g.indexGraphEdgesMap();
		int iSource = viMap.idToIndex(source);
		int iSink = viMap.idToIndex(sink);

		final int n = iGraph.vertices().size();
		int[][] capacity = new int[n][n];
		int[][] cost = new int[n][n];
		for (int e : iGraph.edges()) {
			int u = iGraph.edgeSource(e), v = iGraph.edgeTarget(e);
			capacity[u][v] = net.getCapacityInt(eiMap.indexToId(e));
			cost[u][v] = cost0.weightInt(eiMap.indexToId(e));
		}

		int[] ret = new MinCostMaxFlowImplFromTheInternet().getMaxFlow(capacity, cost, iSource, iSink);
		int totalFlow = ret[0];
		int totalCost = ret[1];
		return IntIntPair.of(totalFlow, totalCost);
	}

	private static class MinCostMaxFlowImplFromTheInternet {

		// Stores the found edges
		boolean found[];

		// Stores the number of nodes
		int N;

		// Stores the capacity
		// of each edge
		int cap[][];

		int flow[][];

		// Stores the cost per
		// unit flow of each edge
		int cost[][];

		// Stores the distance from each node
		// and picked edges for each node
		int dad[], dist[], pi[];

		static final int INF = Integer.MAX_VALUE / 2 - 1;

		// Function to check if it is possible to
		// have a flow from the src to sink
		boolean search(int src, int sink) {

			// Initialise found[] to false
			Arrays.fill(found, false);

			// Initialise the dist[] to INF
			Arrays.fill(dist, INF);

			// Distance from the source node
			dist[src] = 0;

			// Iterate until src reaches N
			while (src != N) {

				int best = N;
				found[src] = true;

				for (int k = 0; k < N; k++) {

					// If already found
					if (found[k])
						continue;

					// Evaluate while flow
					// is still in supply
					if (flow[k][src] != 0) {

						// Obtain the total value
						int val = dist[src] + pi[src] - pi[k] - cost[k][src];

						// If dist[k] is > minimum value
						if (dist[k] > val) {

							// Update
							dist[k] = val;
							dad[k] = src;
						}
					}

					if (flow[src][k] < cap[src][k]) {

						int val = dist[src] + pi[src] - pi[k] + cost[src][k];

						// If dist[k] is > minimum value
						if (dist[k] > val) {

							// Update
							dist[k] = val;
							dad[k] = src;
						}
					}

					if (dist[k] < dist[best])
						best = k;
				}

				// Update src to best for
				// next iteration
				src = best;
			}

			for (int k = 0; k < N; k++)
				pi[k] = Math.min(pi[k] + dist[k], INF);

			// Return the value obtained at sink
			return found[sink];
		}

		// Function to obtain the maximum Flow
		int[] getMaxFlow(int cap[][], int cost[][], int src, int sink) {

			this.cap = cap;
			this.cost = cost;

			N = cap.length;
			found = new boolean[N];
			flow = new int[N][N];
			dist = new int[N + 1];
			dad = new int[N];
			pi = new int[N];

			int totflow = 0, totcost = 0;

			// If a path exist from src to sink
			while (search(src, sink)) {

				// Set the default amount
				int amt = INF;
				for (int x = sink; x != src; x = dad[x])

					amt = Math.min(amt, flow[x][dad[x]] != 0 ? flow[x][dad[x]] : cap[dad[x]][x] - flow[dad[x]][x]);

				for (int x = sink; x != src; x = dad[x]) {

					if (flow[x][dad[x]] != 0) {
						flow[x][dad[x]] -= amt;
						totcost -= amt * cost[x][dad[x]];
					} else {
						flow[dad[x]][x] += amt;
						totcost += amt * cost[dad[x]][x];
					}
				}
				totflow += amt;
			}

			// Return pair total cost and sink
			return new int[] { totflow, totcost };
		}
	}

}
