package com.jgalgo;

import java.util.BitSet;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

class MinimumCutSTBuilderImpl {

	private MinimumCutSTBuilderImpl() {}

	static class Default implements MinimumCutST.Builder {
		@Override
		public MinimumCutST build() {
			return new MaximumFlowPushRelabel();
		}
	}

	static MinimumCutST buildFromMaxFlow(MaximumFlow maxFlowAlg) {
		return new MinimumCutST() {

			private final BitSet visited = new BitSet();
			private final IntPriorityQueue queue = new IntArrayFIFOQueue();

			@Override
			public IntList computeMinimumCut(Graph g, EdgeWeightFunc w, int source, int sink) {
				assert visited.isEmpty();
				assert queue.isEmpty();

				/* create a flow network with weights as capacities */
				FlowNetwork net = createFlowNetworkFromEdgeWeightFunc(g, w);

				/* compute max flow */
				maxFlowAlg.computeMaximumFlow(g, net, source, sink);

				/* perform a BFS from source and use only non saturated edges */
				IntList cut = new IntArrayList();
				final double eps = 0.00001;
				final boolean directed = g instanceof DiGraph;
				cut.add(source);
				visited.set(source);
				queue.enqueue(source);
				while (!queue.isEmpty()) {
					int u = queue.dequeueInt();

					for (EdgeIter it = g.edgesOut(u); it.hasNext();) {
						int e = it.nextInt();
						int v = it.v();
						if (visited.get(v))
							continue;
						if (Math.abs(net.getCapacity(e) - net.getFlow(e)) < eps)
							continue; // saturated edge
						cut.add(v);
						visited.set(v);
						queue.enqueue(v);
					}
					if (directed) {
						/*
						 * We don't have any guarantee that the graph has a twin edge for each edge, so we iterate over
						 * the in edges and search for edges with non zero flow which imply an existent of an out edge
						 * in the residual network
						 */
						for (EdgeIter it = g.edgesIn(u); it.hasNext();) {
							int e = it.nextInt();
							int v = it.u();
							if (visited.get(v))
								continue;
							if (net.getFlow(e) < eps)
								continue; // saturated edge
							cut.add(v);
							visited.set(v);
							queue.enqueue(v);
						}
					}
				}
				visited.clear();
				return cut;
			}

		};
	}

	static FlowNetwork createFlowNetworkFromEdgeWeightFunc(Graph g, EdgeWeightFunc w) {
		Weights.Double flow = Weights.createExternalEdgesWeights(g, double.class);
		FlowNetwork net = new FlowNetwork() {
			@Override
			public double getCapacity(int edge) {
				return w.weight(edge);
			}

			@Override
			public void setCapacity(int edge, double cap) {
				throw new UnsupportedOperationException();
			}

			@Override
			public double getFlow(int edge) {
				return flow.getDouble(edge);
			}

			@Override
			public void setFlow(int edge, double f) {
				flow.set(edge, f);
			}
		};
		return net;
	}

}
