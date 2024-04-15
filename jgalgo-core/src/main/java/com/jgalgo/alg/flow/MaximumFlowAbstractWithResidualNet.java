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
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntLists;

/**
 * Abstract class for computing a maximum flow in a graph with a residual network.
 *
 * @author Barak Ugav
 */
public abstract class MaximumFlowAbstractWithResidualNet extends MaximumFlowAbstract {

	/**
	 * Default constructor.
	 */
	public MaximumFlowAbstractWithResidualNet() {}

	/**
	 * A worker of a maximum flow computation on a single graph.
	 *
	 * @author Barak Ugav
	 */
	protected static class Worker {
		final IndexGraph gOrig;
		final int source;
		final int sink;
		final int n;
		final IWeightFunction capacityOrig;

		final IndexGraph g;
		final int[] edgeRef;
		final int[] twin;

		final boolean multiSourceMultiSink;
		final IntCollection sources;
		final IntCollection sinks;

		protected Worker(IndexGraph gOrig, IWeightFunction capacityOrig, int source, int sink) {
			Assertions.flowSourceSinkNotTheSame(source, sink);
			Assertions.flowPositiveCapacities(gOrig, capacityOrig);
			this.gOrig = gOrig;
			this.source = source;
			this.sink = sink;
			this.sources = IntLists.singleton(source);
			this.sinks = IntLists.singleton(sink);
			this.n = gOrig.vertices().size();
			this.capacityOrig = capacityOrig;
			multiSourceMultiSink = false;

			Flows.ResidualGraph.Builder builder = new Flows.ResidualGraph.Builder(gOrig);
			builder.addAllOriginalEdges();

			Flows.ResidualGraph residualGraph = builder.build();
			g = residualGraph.g;
			edgeRef = residualGraph.edgeRef;
			twin = residualGraph.twin;
		}

		Worker(IndexGraph gOrig, IWeightFunction capacityOrig, IntCollection sources, IntCollection sinks) {
			Assertions.flowSourcesSinksNotTheSame(sources, sinks);
			Assertions.flowPositiveCapacities(gOrig, capacityOrig);
			this.gOrig = gOrig;
			this.n = gOrig.vertices().size() + 2;
			this.sources = sources;
			this.sinks = sinks;
			this.capacityOrig = capacityOrig;
			multiSourceMultiSink = true;

			Flows.ResidualGraph.Builder builder = new Flows.ResidualGraph.Builder(gOrig);
			builder.addAllOriginalEdges();

			source = builder.addVertex();
			sink = builder.addVertex();
			for (int s : sources)
				builder.addEdge(source, s, -1);
			for (int t : sinks)
				builder.addEdge(t, sink, -1);

			Flows.ResidualGraph residualGraph = builder.build();
			g = residualGraph.g;
			edgeRef = residualGraph.edgeRef;
			twin = residualGraph.twin;
		}

		void initCapacitiesAndFlows(double[] flow, double[] capacity) {
			Arrays.fill(flow, 0);
			initCapacities(capacity);
		}

		void initCapacitiesAndFlows(int[] flow, int[] capacity) {
			Arrays.fill(flow, 0);
			initCapacities(capacity);
		}

		void initCapacities(double[] residualCapacity) {
			if (gOrig.isDirected()) {
				if (WeightFunction.isCardinality(capacityOrig)) {
					for (int e : range(g.edges().size()))
						residualCapacity[e] = isOriginalEdge(e) ? 1 : 0;
				} else {
					for (int e : range(g.edges().size()))
						residualCapacity[e] = isOriginalEdge(e) ? capacityOrig.weight(edgeRef[e]) : 0;
				}
			} else {
				if (WeightFunction.isCardinality(capacityOrig)) {
					for (int e : range(g.edges().size())) {
						int eRef = edgeRef[e];
						double cap = (eRef >= 0 && g.edgeTarget(e) != source && g.edgeSource(e) != sink) ? 1 : 0;
						residualCapacity[e] = cap;
					}
				} else {
					for (int e : range(g.edges().size())) {
						int eRef = edgeRef[e];
						double cap = (eRef >= 0 && g.edgeTarget(e) != source && g.edgeSource(e) != sink)
								? capacityOrig.weight(eRef)
								: 0;
						residualCapacity[e] = cap;
					}
				}
			}
			if (multiSourceMultiSink) {
				double capacitySum = 0;
				for (int e : range(gOrig.edges().size()))
					capacitySum += capacityOrig.weight(e);

				/* init edges from super-source to sources and from sinks to super-sink */
				for (int e : range(g.edges().size()))
					if (edgeRef[e] < 0)
						residualCapacity[e] = source == g.edgeSource(e) || sink == g.edgeTarget(e) ? capacitySum : 0;
			}
		}

		void initCapacities(int[] residualCapacity) {
			IWeightFunctionInt capacity = (IWeightFunctionInt) this.capacityOrig;
			if (gOrig.isDirected()) {
				if (WeightFunction.isCardinality(capacityOrig)) {
					for (int e : range(g.edges().size()))
						residualCapacity[e] = isOriginalEdge(e) ? 1 : 0;
				} else {
					for (int e : range(g.edges().size()))
						residualCapacity[e] = isOriginalEdge(e) ? capacity.weightInt(edgeRef[e]) : 0;
				}
			} else {
				if (WeightFunction.isCardinality(capacityOrig)) {
					for (int e : range(g.edges().size())) {
						int eRef = edgeRef[e];
						int cap = (eRef >= 0 && g.edgeTarget(e) != source && g.edgeSource(e) != sink) ? 1 : 0;
						residualCapacity[e] = cap;
					}
				} else {
					for (int e : range(g.edges().size())) {
						int eRef = edgeRef[e];
						int cap = (eRef >= 0 && g.edgeTarget(e) != source && g.edgeSource(e) != sink)
								? capacity.weightInt(eRef)
								: 0;
						residualCapacity[e] = cap;
					}
				}
			}
			if (multiSourceMultiSink) {
				int capacitySum = 0;
				for (int e : range(gOrig.edges().size())) {
					int cap = capacity.weightInt(e);
					int capacitySumNext = capacitySum + cap;
					if (((capacitySum ^ capacitySumNext) & (cap ^ capacitySumNext)) < 0) {
						// HD 2-12 Overflow iff both arguments have the opposite sign of the result
						capacitySum = Integer.MAX_VALUE;
						break;
					}
					capacitySum = capacitySumNext;
				}

				/* init edges from super-source to sources and from sinks to super-sink */
				for (int e : range(g.edges().size()))
					if (edgeRef[e] < 0)
						residualCapacity[e] = source == g.edgeSource(e) || sink == g.edgeTarget(e) ? capacitySum : 0;
			}
		}

		IFlow constructResult(double[] flow) {
			double[] flowRes = new double[gOrig.edges().size()];
			for (int e : range(g.edges().size())) {
				if (isOriginalEdge(e))
					/* The flow of e might be negative if the original graph is undirected, which is fine */
					flowRes[edgeRef[e]] = flow[e];
			}
			return newFlow(gOrig, flowRes);
		}

		IFlow constructResult(int[] flow) {
			double[] flowRes = new double[gOrig.edges().size()];
			for (int e : range(g.edges().size())) {
				if (isOriginalEdge(e))
					/* The flow of e might be negative if the original graph is undirected, which is fine */
					flowRes[edgeRef[e]] = flow[e];
			}
			return newFlow(gOrig, flowRes);
		}

		boolean isOriginalEdge(int e) {
			int eOrig = edgeRef[e];
			return eOrig >= 0 && g.edgeSource(e) == gOrig.edgeSource(eOrig);

		}
	}

}
