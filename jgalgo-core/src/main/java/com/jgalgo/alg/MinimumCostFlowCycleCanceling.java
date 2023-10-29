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

import java.util.BitSet;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeights;
import com.jgalgo.graph.IWeightsDouble;
import com.jgalgo.internal.util.Assertions;

/**
 * Compute the minimum-cost (max) flow in a flow network using cycle canceling.
 * <p>
 * Firstly, a maximum flow is computed using {@link MaximumFlow}. Then, the residual graph is constructed from the max
 * flow (containing only non-saturated edges), and negative cycles (with respect to the cost function) are eliminated
 * from it repeatedly until no negative cycles remain.
 * <p>
 * The algorithm runs in \(O(CC) \cdot O(mCU)\), where \(O(CC)\) is the running time of the algorithm used to find a
 * cycle in the residual graph, \(C\) is the maximum (absolute) edge cost and \(U\) is the maximum edge capacity.
 * <p>
 * Based on 'A Primal Method for Minimal Cost Flows with Applications to the Assignment and Transportation Problems' by
 * M Klein (1966).
 *
 * @author Barak Ugav
 */
class MinimumCostFlowCycleCanceling extends MinimumCostFlows.AbstractImplBasedSourceSink {

	private final MaximumFlow maxFlowAlg = MaximumFlow.newInstance();
	private final MinimumMeanCycle minMeanCycleAlg = MinimumMeanCycle.newInstance();
	private static final double EPS = 0.0001;

	@Override
	void computeMinCostMaxFlow(IndexGraph gOrig, FlowNetwork net, IWeightFunction cost, int source, int sink) {
		Assertions.Graphs.onlyDirected(gOrig);
		Assertions.Flows.sourceSinkNotTheSame(source, sink);

		/* Compute maximum flow */
		maxFlowAlg.computeMaximumFlow(gOrig, net, source, sink);

		/* Construct the residual graph from the maximum flow */
		FlowNetworks.ResidualGraph.Builder builder = new FlowNetworks.ResidualGraph.Builder(gOrig);
		builder.addAllOriginalEdges();
		FlowNetworks.ResidualGraph resGraph = builder.build();
		IndexGraph g = resGraph.g;
		double[] capacity = new double[g.edges().size()];
		double[] flow = new double[g.edges().size()];

		/* eliminate negative cycles in the residual network repeatedly */
		initResidualCapacitiesAndFlows(gOrig, net, resGraph, capacity, flow);
		eliminateAllNegativeCycles(gOrig, resGraph, capacity, flow, cost);

		for (int m = g.edges().size(), e = 0; e < m; e++)
			if (resGraph.isOriginalEdge(e))
				net.setFlow(resGraph.edgeRef[e], flow[e]);

		MinimumCostFlows.saturateNegativeCostSelfEdges(gOrig, net, cost);
	}

	private static void initResidualCapacitiesAndFlows(IndexGraph gOrig, FlowNetwork net,
			FlowNetworks.ResidualGraph resGraph, double[] capacity, double[] flow) {
		IndexGraph g = resGraph.g;
		int[] edgeRef = resGraph.edgeRef;

		if (gOrig.isDirected()) {
			for (int m = g.edges().size(), e = 0; e < m; e++) {
				int eRef = edgeRef[e];
				double eFlow = net.getFlow(eRef);
				if (resGraph.isOriginalEdge(e)) {
					capacity[e] = net.getCapacity(eRef);
					flow[e] = eFlow;
				} else {
					capacity[e] = 0;
					flow[e] = -eFlow;
				}
				assert flow[e] <= capacity[e] + 1e-9;
			}
		} else {
			for (int m = g.edges().size(), e = 0; e < m; e++) {
				int eRef = edgeRef[e];
				double eFlow = net.getFlow(eRef);
				capacity[e] = net.getCapacity(eRef);
				flow[e] = resGraph.isOriginalEdge(e) ? eFlow : -eFlow;
				assert flow[e] <= capacity[e] + 1e-9;
			}
		}
	}

	private void eliminateAllNegativeCycles(IndexGraph gOrig, FlowNetworks.ResidualGraph resGraph, double[] capacity,
			double[] flow, IWeightFunction cost) {
		IndexGraph g = resGraph.g;
		int[] edgeRef = resGraph.edgeRef;
		int[] twin = resGraph.twin;

		/*
		 * Removing edges from the (residual) graph is expensive. We simply assign a high cost to edges we considered
		 * 'removed', namely saturated edges.
		 */

		BitSet saturated = new BitSet(g.edges().size());
		final double saturatedCost = hugeCost(gOrig, cost);

		/* Init costs for edges */
		/* cost(e) for original edges */
		/* -cost(e) for backward (twin) original edges */
		/* saturatedCost for 'removed' (saturated) edges */
		IWeightsDouble cost0 = IWeights.createExternalEdgesWeights(g, double.class);
		for (int m = g.edges().size(), e = 0; e < m; e++) {
			boolean isSaturated = capacity[e] - flow[e] < EPS;
			if (isSaturated) {
				saturated.set(e);
				cost0.set(e, saturatedCost);
			} else {
				double eCost = cost.weight(edgeRef[e]);
				cost0.set(e, resGraph.isOriginalEdge(e) ? eCost : -eCost);
			}
		}

		/* Repeatedly find the cycle with the minimum mean cost and push flow through it */
		for (;;) {
			Path minCycle = minMeanCycleAlg.computeMinimumMeanCycle(g, cost0);
			if (minCycle == null || cost0.weightSum(minCycle.edges()) >= -EPS)
				break; /* no cycle or cycle that will increase the total cost */

			/* find the maximum amount of flow we can push through the cycle */
			double f = Double.POSITIVE_INFINITY;
			for (int e : minCycle.edges())
				f = Math.min(f, capacity[e] - flow[e]);
			assert f > 0;

			/* Push flow along the cycle, lowering the overall cost */
			for (int e : minCycle.edges()) {
				int eTwin = twin[e];
				flow[e] += f;
				flow[eTwin] -= f;

				/* Add/remove edges to the non saturated residual net */
				/* We use high cost edge instead of actually removing an edge */
				assert !saturated.get(e);
				boolean isSaturated = capacity[e] - flow[e] < EPS;
				if (isSaturated) {
					saturated.set(e);
					cost0.set(e, saturatedCost);
				}
				if (saturated.get(eTwin)) {
					saturated.clear(eTwin);
					double eTwinCost = cost.weight(edgeRef[eTwin]);
					cost0.set(eTwin, resGraph.isOriginalEdge(eTwin) ? eTwinCost : -eTwinCost);
				}
			}
		}
	}

}
