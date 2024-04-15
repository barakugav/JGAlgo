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
import java.util.Arrays;
import com.jgalgo.graph.EdgeSet;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Abstract class for computing a maximum flow in a graph without using a residual network.
 *
 * @author Barak Ugav
 */
public abstract class MaximumFlowAbstractWithoutResidualNet extends MaximumFlowAbstract {

	/**
	 * Default constructor.
	 */
	public MaximumFlowAbstractWithoutResidualNet() {}

	/**
	 * A worker of a maximum flow computation on a single graph.
	 *
	 * @author Barak Ugav
	 */
	protected static class Worker {
		final IndexGraph g;
		final int source;
		final int sink;
		final int n;
		final IWeightFunction capacity;
		final boolean directed;

		protected Worker(IndexGraph g, IWeightFunction capacity, int source, int sink) {
			Assertions.flowSourceSinkNotTheSame(source, sink);
			Assertions.flowPositiveCapacities(g, capacity);
			this.g = g;
			this.source = source;
			this.sink = sink;
			this.n = g.vertices().size();
			this.capacity = capacity;
			directed = g.isDirected();
		}

		void initCapacities(int[] capacities) {
			if (WeightFunction.isCardinality(capacity)) {
				Arrays.fill(capacities, 1);
			} else {
				IWeightFunctionInt capacity = (IWeightFunctionInt) this.capacity;
				for (int e : range(g.edges().size()))
					capacities[e] = capacity.weightInt(e);
			}
		}

		void initCapacities(double[] capacities) {
			if (WeightFunction.isCardinality(capacity)) {
				Arrays.fill(capacities, 1);
			} else {
				for (int e : range(g.edges().size()))
					capacities[e] = capacity.weight(e);
			}
		}

		IFlow constructResult(double[] capacity, double[] residualCapacity) {
			double[] flow = new double[g.edges().size()];
			for (int e : range(g.edges().size()))
				flow[e] = capacity[e] - residualCapacity[e];
			return newFlow(g, flow);
		}

		IFlow constructResult(int[] capacity, int[] residualCapacity) {
			double[] flow = new double[g.edges().size()];
			for (int e : range(g.edges().size()))
				flow[e] = capacity[e] - residualCapacity[e];
			return newFlow(g, flow);
		}

	}

	@Override
	protected IFlow computeMaximumFlow(IndexGraph gOrig, IWeightFunction capacityOrig, IntCollection sources,
			IntCollection sinks) {
		if (sources.size() == 1 && sinks.size() == 1)
			return computeMaximumFlow(gOrig, capacityOrig, sources.iterator().nextInt(), sinks.iterator().nextInt());
		Assertions.flowSourcesSinksNotTheSame(sources, sinks);

		IndexGraphBuilder builder = IndexGraphBuilder.newInstance(gOrig.isDirected());
		builder.ensureVertexCapacity(gOrig.vertices().size() + 2);
		builder.ensureEdgeCapacity(gOrig.edges().size() + sources.size() + sinks.size());

		builder.addVertices(gOrig.vertices());
		builder.addEdges(EdgeSet.allOf(gOrig));
		final int originalEdgesThreshold = builder.edges().size();

		final int source = builder.addVertexInt();
		final int sink = builder.addVertexInt();
		Object capacities;
		if (WeightFunction.isInteger(capacityOrig)) {
			IWeightFunctionInt capacityOrigInt = (IWeightFunctionInt) capacityOrig;
			int[] capacities0 = new int[sources.size() + sinks.size()];
			int capIdx = 0;
			for (int s : sources) {
				builder.addEdge(source, s);
				capacities0[capIdx++] = Flows.vertexMaxSupply(gOrig, capacityOrigInt, s);
			}
			for (int t : sinks) {
				builder.addEdge(t, sink);
				capacities0[capIdx++] = Flows.vertexMaxDemand(gOrig, capacityOrigInt, t);
			}
			capacities = capacities0;
		} else {
			double[] capacities0 = new double[sources.size() + sinks.size()];
			int capIdx = 0;
			for (int s : sources) {
				builder.addEdge(source, s);
				capacities0[capIdx++] = Flows.vertexMaxSupply(gOrig, capacityOrig, s);
			}
			for (int t : sinks) {
				builder.addEdge(t, sink);
				capacities0[capIdx++] = Flows.vertexMaxDemand(gOrig, capacityOrig, t);
			}
			capacities = capacities0;
		}
		IndexGraph g = builder.build();

		IWeightFunction capacity;
		if (WeightFunction.isInteger(capacityOrig)) {
			IWeightFunctionInt capacityOrigInt = (IWeightFunctionInt) capacityOrig;
			final int[] caps = (int[]) capacities;
			IWeightFunctionInt capacityInt = edge -> edge < originalEdgesThreshold ? capacityOrigInt.weightInt(edge)
					: caps[edge - originalEdgesThreshold];
			capacity = capacityInt;

		} else {
			final double[] caps = (double[]) capacities;
			capacity = edge -> edge < originalEdgesThreshold ? capacityOrig.weight(edge)
					: caps[edge - originalEdgesThreshold];
		}

		IFlow flow0 = computeMaximumFlow(g, capacity, source, sink);
		double[] flow = new double[gOrig.edges().size()];
		for (int e : range(gOrig.edges().size()))
			flow[e] = flow0.getFlow(e);
		return newFlow(gOrig, flow);
	}

}
