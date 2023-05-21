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

import it.unimi.dsi.fastutil.ints.IntIterator;

class MaximumFlowAbstract {

	private MaximumFlowAbstract() {}

	static class Worker {
		final Graph gOrig;
		final int source;
		final int sink;
		final int n;
		final FlowNetwork net;

		final Graph g;
		final Weights.Int edgeRef;
		final Weights.Int twin;

		static final Object EdgeRefWeightKey = new Object();
		static final Object EdgeRevWeightKey = new Object();

		Worker(Graph gOrig, FlowNetwork net, int source, int sink) {
			ArgumentCheck.sourceSinkNotTheSame(source, sink);
			positiveCapacitiesOrThrow(gOrig, net);
			this.gOrig = gOrig;
			this.source = source;
			this.sink = sink;
			this.n = gOrig.vertices().size();
			this.net = net;

			g = GraphBuilder.newDirected().build(n);
			edgeRef = g.addEdgesWeights(EdgeRefWeightKey, int.class, Integer.valueOf(-1));
			twin = g.addEdgesWeights(EdgeRevWeightKey, int.class, Integer.valueOf(-1));

			boolean directed = gOrig.getCapabilities().directed();
			for (IntIterator it = gOrig.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				int u = gOrig.edgeSource(e), v = gOrig.edgeTarget(e);
				if (u == v)
					continue;
				if (directed && (u == sink || v == source))
					continue;
				int e1 = g.addEdge(u, v);
				int e2 = g.addEdge(v, u);
				edgeRef.set(e1, e);
				edgeRef.set(e2, e);
				twin.set(e1, e2);
				twin.set(e2, e1);
			}
		}

		void initCapacitiesAndFlows(Weights.Double flow, Weights.Double capacity) {
			if (gOrig.getCapabilities().directed()) {
				for (IntIterator it = g.edges().iterator(); it.hasNext();) {
					int e = it.nextInt();
					capacity.set(e, isOriginalEdge(e) ? net.getCapacity(edgeRef.getInt(e)) : 0);
					flow.set(e, 0);
				}
			} else {
				for (IntIterator it = g.edges().iterator(); it.hasNext();) {
					int e = it.nextInt();
					double cap =
							(g.edgeTarget(e) != source && g.edgeSource(e) != sink) ? net.getCapacity(edgeRef.getInt(e))
									: 0;
					capacity.set(e, cap);
					flow.set(e, 0);
				}
			}
		}

		void initCapacitiesAndFlows(Weights.Int flow, Weights.Int capacity) {
			FlowNetwork.Int net = (FlowNetwork.Int) this.net;
			if (gOrig.getCapabilities().directed()) {
				for (IntIterator it = g.edges().iterator(); it.hasNext();) {
					int e = it.nextInt();
					capacity.set(e, isOriginalEdge(e) ? net.getCapacityInt(edgeRef.getInt(e)) : 0);
					flow.set(e, 0);
				}
			} else {
				for (IntIterator it = g.edges().iterator(); it.hasNext();) {
					int e = it.nextInt();
					int cap = (g.edgeTarget(e) != source && g.edgeSource(e) != sink)
							? net.getCapacityInt(edgeRef.getInt(e))
							: 0;
					capacity.set(e, cap);
					flow.set(e, 0);
				}
			}
		}

		double constructResult(Weights.Double flow) {
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				if (isOriginalEdge(e))
					/* The flow of e might be negative if the original graph is undirected, which is fine */
					net.setFlow(edgeRef.getInt(e), flow.getDouble(e));
			}

			double totalFlow = 0;
			if (gOrig.getCapabilities().directed()) {
				for (EdgeIter eit = gOrig.edgesOut(source); eit.hasNext();)
					totalFlow += net.getFlow(eit.nextInt());
				for (EdgeIter eit = gOrig.edgesIn(source); eit.hasNext();)
					totalFlow -= net.getFlow(eit.nextInt());
			} else {
				for (EdgeIter eit = g.edgesOut(source); eit.hasNext();)
					totalFlow += flow.getDouble(eit.nextInt());
			}
			return totalFlow;
		}

		int constructResult(Weights.Int flow) {
			FlowNetwork.Int net = (FlowNetwork.Int) this.net;
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				if (isOriginalEdge(e))
					/* The flow of e might be negative if the original graph is undirected, which is fine */
					net.setFlow(edgeRef.getInt(e), flow.getInt(e));
			}

			int totalFlow = 0;
			if (gOrig.getCapabilities().directed()) {
				for (EdgeIter eit = gOrig.edgesOut(source); eit.hasNext();)
					totalFlow += net.getFlowInt(eit.nextInt());
				for (EdgeIter eit = gOrig.edgesIn(source); eit.hasNext();)
					totalFlow -= net.getFlowInt(eit.nextInt());
			} else {
				for (EdgeIter eit = g.edgesOut(source); eit.hasNext();)
					totalFlow += flow.getInt(eit.nextInt());
			}
			return totalFlow;
		}

		boolean isOriginalEdge(int e) {
			return g.edgeSource(e) == gOrig.edgeSource(edgeRef.getInt(e));
		}

		private static void positiveCapacitiesOrThrow(Graph g, FlowNetwork net) {
			if (net instanceof FlowNetwork.Int) {
				FlowNetwork.Int netInt = (FlowNetwork.Int) net;
				for (IntIterator it = g.edges().iterator(); it.hasNext();) {
					int e = it.nextInt();
					int cap = netInt.getCapacityInt(e);
					if (cap < 0)
						throw new IllegalArgumentException("negative capacity of edge (" + e + "): " + cap);
				}
			} else {
				for (IntIterator it = g.edges().iterator(); it.hasNext();) {
					int e = it.nextInt();
					double cap = net.getCapacity(e);
					if (cap < 0)
						throw new IllegalArgumentException("negative capacity of edge (" + e + "): " + cap);
				}
			}
		}

	}
}
