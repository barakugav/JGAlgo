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
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

class MinimumCutSTUtils {

	private MinimumCutSTUtils() {}

	static abstract class AbstractImpl implements MinimumCutST {

		@Override
		public IVertexBiPartition computeMinimumCut(IntGraph g, IWeightFunction w, int source, int sink) {
			if (g instanceof IndexGraph)
				return computeMinimumCut((IndexGraph) g, w, source, sink);

			IndexGraph iGraph = g.indexGraph();
			IndexIntIdMap viMap = g.indexGraphVerticesMap();
			IndexIntIdMap eiMap = g.indexGraphEdgesMap();

			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			int iSource = viMap.idToIndex(source);
			int iSink = viMap.idToIndex(sink);

			IVertexBiPartition indexCut = computeMinimumCut(iGraph, iw, iSource, iSink);
			return new VertexBiPartitions.IntBiPartitionFromIndexBiPartition(g, indexCut);
		}

		@Override
		public IVertexBiPartition computeMinimumCut(IntGraph g, IWeightFunction w, IntCollection sources,
				IntCollection sinks) {
			if (g instanceof IndexGraph)
				return computeMinimumCut((IndexGraph) g, w, sources, sinks);

			IndexGraph iGraph = g.indexGraph();
			IndexIntIdMap viMap = g.indexGraphVerticesMap();
			IndexIntIdMap eiMap = g.indexGraphEdgesMap();

			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			IntCollection iSources = IndexIdMaps.idToIndexCollection(sources, viMap);
			IntCollection iSinks = IndexIdMaps.idToIndexCollection(sinks, viMap);

			IVertexBiPartition indexCut = computeMinimumCut(iGraph, iw, iSources, iSinks);
			return new VertexBiPartitions.IntBiPartitionFromIndexBiPartition(g, indexCut);
		}

		abstract IVertexBiPartition computeMinimumCut(IndexGraph g, IWeightFunction w, int sources, int sinks);

		abstract IVertexBiPartition computeMinimumCut(IndexGraph g, IWeightFunction w, IntCollection sources,
				IntCollection sinks);

	}

	static IVertexBiPartition computeMinimumCutUsingMaxFlow(IndexGraph g, IWeightFunction w, int source, int sink,
			MaximumFlow maxFlowAlg) {
		/* create a flow network with weights as capacities */
		IFlowNetwork net = createFlowNetworkFromEdgeWeightFunc(g, w);

		/* compute max flow */
		maxFlowAlg.computeMaximumFlow(g, net, source, sink);

		return minCutFromMaxFlow(g, IntLists.singleton(source), net);
	}

	static IVertexBiPartition computeMinimumCutUsingMaxFlow(IndexGraph g, IWeightFunction w, IntCollection sources,
			IntCollection sinks, MaximumFlow maxFlowAlg) {
		/* create a flow network with weights as capacities */
		IFlowNetwork net = createFlowNetworkFromEdgeWeightFunc(g, w);

		/* compute max flow */
		maxFlowAlg.computeMaximumFlow(g, net, sources, sinks);

		return minCutFromMaxFlow(g, sources, net);
	}

	private static IVertexBiPartition minCutFromMaxFlow(IndexGraph g, IntCollection sources, IFlowNetwork net) {
		final int n = g.vertices().size();
		BitSet visited = new BitSet(n);
		IntPriorityQueue queue = new FIFOQueueIntNoReduce();

		/* perform a BFS from source and use only non saturated edges */
		final double eps = 0.00001;
		for (int source : sources) {
			visited.set(source);
			queue.enqueue(source);
		}

		if (g.isDirected()) {
			while (!queue.isEmpty()) {
				int u = queue.dequeueInt();

				for (IEdgeIter it = g.outEdges(u).iterator(); it.hasNext();) {
					int e = it.nextInt();
					int v = it.targetInt();
					if (visited.get(v))
						continue;
					if (Math.abs(net.getCapacity(e) - net.getFlow(e)) < eps)
						continue; // saturated edge
					visited.set(v);
					queue.enqueue(v);
				}
				/*
				 * We don't have any guarantee that the graph has a twin edge for each edge, so we iterate over the
				 * in-edges and search for edges with non zero flow which imply an existent of an out edge in the
				 * residual network
				 */
				for (IEdgeIter it = g.inEdges(u).iterator(); it.hasNext();) {
					int e = it.nextInt();
					int v = it.sourceInt();
					if (visited.get(v))
						continue;
					if (net.getFlow(e) < eps)
						continue; // saturated edge
					visited.set(v);
					queue.enqueue(v);
				}
			}
		} else {
			while (!queue.isEmpty()) {
				int u = queue.dequeueInt();

				for (IEdgeIter it = g.outEdges(u).iterator(); it.hasNext();) {
					int e = it.nextInt();
					int v = it.targetInt();
					if (visited.get(v))
						continue;
					double directedFlow = net.getFlow(e) * (g.edgeSource(e) == u ? +1 : -1);
					if (Math.abs(net.getCapacity(e) - directedFlow) < eps)
						continue; // saturated edge
					visited.set(v);
					queue.enqueue(v);
				}
			}
		}

		return new VertexBiPartitions.FromBitSet(g, visited);
	}

	static MinimumCutST buildFromMaxFlow(MaximumFlow maxFlowAlg) {
		return new AbstractImpl() {

			@Override
			public IVertexBiPartition computeMinimumCut(IndexGraph g, IWeightFunction w, int source, int sink) {
				return computeMinimumCutUsingMaxFlow(g, w, source, sink, maxFlowAlg);
			}

			@Override
			public IVertexBiPartition computeMinimumCut(IndexGraph g, IWeightFunction w, IntCollection sources,
					IntCollection sinks) {
				return computeMinimumCutUsingMaxFlow(g, w, sources, sinks, maxFlowAlg);
			}

		};
	}

	static IFlowNetwork createFlowNetworkFromEdgeWeightFunc(IndexGraph g, IWeightFunction w) {
		Assertions.Graphs.onlyPositiveEdgesWeights(g, w);
		double[] flow = new double[g.edges().size()];
		IFlowNetwork net = new IFlowNetwork() {
			@Override
			public double getCapacity(int edge) {
				return w.weight(edge);
			}

			@Override
			public void setCapacity(int edge, double cap) {
				throw new UnsupportedOperationException("capacities are immutable");
			}

			@Override
			public double getFlow(int edge) {
				return flow[edge];
			}

			@Override
			public void setFlow(int edge, double f) {
				flow[edge] = f;
			}
		};
		return net;
	}

	static MinimumCutGlobal globalMinCutFromStMinCut(MinimumCutST stMinCut) {
		return new MinimumCutGlobalAbstract() {
			@Override
			IVertexBiPartition computeMinimumCut(IndexGraph g, IWeightFunction w) {
				final int n = g.vertices().size();
				if (n < 2)
					throw new IllegalArgumentException("no valid cut in graphs with less than two vertices");
				w = WeightFunctions.localEdgeWeightFunction(g, w);

				IVertexBiPartition bestCut = null;
				double bestCutWeight = Double.MAX_VALUE;
				final int source = 0;
				for (int sink = 1; sink < n; sink++) {
					IVertexBiPartition cut = stMinCut.computeMinimumCut(g, w, source, sink);
					double cutWeight = w.weightSum(cut.crossEdges());
					if (bestCutWeight > cutWeight) {
						bestCutWeight = cutWeight;
						bestCut = cut;
					}
				}
				assert bestCut != null;
				return bestCut;
			}
		};
	}

}
