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
package com.jgalgo.alg.flow;

import static com.jgalgo.internal.util.Range.range;
import java.util.Objects;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IWeights;
import com.jgalgo.graph.IWeightsDouble;
import com.jgalgo.graph.IWeightsInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Fastutil;

class MinimumCostFlows {

	private MinimumCostFlows() {}

	abstract static class AbstractImpl extends MinimumCostFlowAbstract {

		/**
		 * Default constructor.
		 */
		public AbstractImpl() {}

		@Override
		protected IFlow computeMinCostMaxFlow(IndexGraph g, IWeightFunction capacity, IWeightFunction cost,
				IWeightFunction lowerBound, int source, int sink) {
			Objects.requireNonNull(lowerBound);
			return computeMinCostMaxFlow(g, capacity, cost, lowerBound, Fastutil.list(source), Fastutil.list(sink));
		}

		@Override
		protected IFlow computeMinCostFlow(IndexGraph g, IWeightFunction capacityOrig, IWeightFunction cost,
				IWeightFunction lowerBound, IWeightFunction supply) {
			Assertions.onlyDirected(g);
			Assertions.flowCheckLowerBound(g, capacityOrig, lowerBound);
			Assertions.flowCheckSupply(g, supply);
			capacityOrig = IWeightFunction.replaceNullWeightFunc(capacityOrig);
			cost = IWeightFunction.replaceNullWeightFunc(cost);

			final boolean integerFlow = WeightFunction.isInteger(capacityOrig) && WeightFunction.isInteger(lowerBound);

			/*
			 * To solve the minimum cost flow for a given supply and edges lower bounds, we perform a reduction to the
			 * problem with given supply without any edges flow lower bounds. For each edge with lower bound we subtract
			 * the lower bound from the capacity of the edge, and add/remove supply to the edge endpoints.
			 */

			/* Create a network by subtracting the lower bound from each edge capacity */
			IWeightFunction capacity;
			if (integerFlow) {
				IWeightFunctionInt capacityOrigInt = (IWeightFunctionInt) capacityOrig;
				IWeightFunctionInt lowerBoundInt = (IWeightFunctionInt) lowerBound;
				IWeightFunctionInt capacityInt =
						edge -> capacityOrigInt.weightInt(edge) - lowerBoundInt.weightInt(edge);
				capacity = capacityInt;
			} else {
				IWeightFunction capacityOrig0 = capacityOrig;
				IWeightFunction lowerBound0 = lowerBound;
				capacity = edge -> capacityOrig0.weight(edge) - lowerBound0.weight(edge);
			}

			/* For each edge with lower bound we add/remove supply from the end endpoints */
			IWeightFunction supply2 = computeSupply(g, capacityOrig, lowerBound, supply);

			/* Solve the reduction problem with only supply without edges lower bounds */
			IFlow flow0 = computeMinCostFlow(g, capacity, cost, supply2);
			double[] flow = new double[g.edges().size()];
			for (int e : range(g.edges().size()))
				flow[e] = flow0.getFlow(e) + lowerBound.weight(e);
			return newFlow(g, flow);
		}

		static double hugeCost(IndexGraph g, IWeightFunction cost) {
			if (WeightFunction.isInteger(cost))
				return hugeCostLong(g, (IWeightFunctionInt) cost);

			double costSum = 0;
			for (int e : range(g.edges().size()))
				costSum += Math.abs(cost.weight(e));
			return 1 + costSum;
		}

		static int hugeCost(IndexGraph g, IWeightFunctionInt cost) {
			long costSum = hugeCostLong(g, cost);
			int costSumInt = (int) costSum;
			if (costSum != costSumInt)
				throw new AssertionError("integer overflow, huge cost can't fit in 32bit int");
			return costSumInt;
		}

		private static long hugeCostLong(IndexGraph g, IWeightFunctionInt cost) {
			long costSum = 0;
			for (int e : range(g.edges().size()))
				costSum += Math.abs(cost.weightInt(e));
			return costSum + 1;
		}

		static IWeightFunction computeSupply(IndexGraph g, IWeightFunction capacity, IWeightFunction lowerBound,
				IWeightFunction supply) {
			boolean isInt = WeightFunction.isInteger(capacity);
			if (lowerBound != null)
				isInt = isInt && WeightFunction.isInteger(lowerBound);
			if (supply != null)
				isInt = isInt && WeightFunction.isInteger(lowerBound);
			if (isInt)
				return computeSupply(g, (IWeightFunctionInt) capacity, (IWeightFunctionInt) lowerBound,
						(IWeightFunctionInt) supply);

			IWeightsDouble supply2 = IWeights.createExternalVerticesWeights(g, double.class);
			if (supply != null) {
				for (int v : range(g.vertices().size()))
					supply2.set(v, supply.weight(v));
			}
			if (lowerBound != null) {
				for (int e : range(g.edges().size())) {
					double l = lowerBound.weight(e);
					if (l == 0)
						continue;
					int u = g.edgeSource(e), v = g.edgeTarget(e);
					supply2.set(u, supply2.get(u) - l);
					supply2.set(v, supply2.get(v) + l);
				}
			}
			return supply2;
		}

		static IWeightFunctionInt computeSupply(IndexGraph g, IWeightFunctionInt capacity,
				IWeightFunctionInt lowerBound, IWeightFunctionInt supply) {
			IWeightsInt supply2 = IWeights.createExternalVerticesWeights(g, int.class);
			if (supply != null) {
				for (int v : range(g.vertices().size()))
					supply2.set(v, supply.weightInt(v));
			}
			if (lowerBound != null) {
				for (int e : range(g.edges().size())) {
					int l = lowerBound.weightInt(e);
					if (l == 0)
						continue;
					int u = g.edgeSource(e), v = g.edgeTarget(e);
					supply2.set(u, supply2.get(u) - l);
					supply2.set(v, supply2.get(v) + l);
				}
			}
			return supply2;
		}

	}

	static void saturateNegativeCostSelfEdges(IndexGraph g, IWeightFunction capacity, IWeightFunction cost,
			double[] flow) {
		if (!g.isAllowSelfEdges())
			return;
		if (WeightFunction.isInteger(capacity)) {
			IWeightFunctionInt capacityInt = (IWeightFunctionInt) capacity;
			if (WeightFunction.isInteger(cost)) {
				IWeightFunctionInt costInt = (IWeightFunctionInt) cost;
				for (int e : Graphs.selfEdges(g))
					if (costInt.weightInt(e) < 0)
						flow[e] = capacityInt.weightInt(e);
			} else {
				for (int e : Graphs.selfEdges(g))
					if (cost.weight(e) < 0)
						flow[e] = capacityInt.weightInt(e);
			}
		} else {
			if (WeightFunction.isInteger(cost)) {
				IWeightFunctionInt costInt = (IWeightFunctionInt) cost;
				for (int e : Graphs.selfEdges(g))
					if (costInt.weightInt(e) < 0)
						flow[e] = capacity.weight(e);
			} else {
				for (int e : Graphs.selfEdges(g))
					if (cost.weight(e) < 0)
						flow[e] = capacity.weight(e);
			}
		}
	}

}
