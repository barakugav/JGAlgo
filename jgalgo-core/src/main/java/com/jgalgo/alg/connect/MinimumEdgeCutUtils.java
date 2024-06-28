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
package com.jgalgo.alg.connect;

import static com.jgalgo.internal.util.Range.range;
import com.jgalgo.alg.common.IVertexBiPartition;
import com.jgalgo.alg.flow.IFlow;
import com.jgalgo.alg.flow.MaximumFlow;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

class MinimumEdgeCutUtils {

	private MinimumEdgeCutUtils() {}

	static IVertexBiPartition computeMinimumCutUsingMaxFlow(IndexGraph g, IWeightFunction w, int source, int sink,
			MaximumFlow maxFlowAlg) {
		IFlow flow = (IFlow) maxFlowAlg.computeMaximumFlow(g, w, Integer.valueOf(source), Integer.valueOf(sink));
		return minCutFromMaxFlow(g, IntLists.singleton(source), w, flow);
	}

	static IVertexBiPartition computeMinimumCutUsingMaxFlow(IndexGraph g, IWeightFunction w, IntCollection sources,
			IntCollection sinks, MaximumFlow maxFlowAlg) {
		IFlow flow = (IFlow) maxFlowAlg.computeMaximumFlow(g, w, sources, sinks);
		return minCutFromMaxFlow(g, sources, w, flow);
	}

	private static IVertexBiPartition minCutFromMaxFlow(IndexGraph g, IntCollection sources, IWeightFunction capacity,
			IFlow flow) {
		capacity = IWeightFunction.replaceNullWeightFunc(capacity);
		final int n = g.vertices().size();
		Bitmap visited = new Bitmap(n);
		IntPriorityQueue queue = new FIFOQueueIntNoReduce();

		final double eps =
				range(g.edges().size()).mapToDouble(capacity::weight).filter(c -> c > 0).min().orElse(0) * 1e-8;

		/* perform a BFS from source and use only non saturated edges */
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
					if (Math.abs(capacity.weight(e) - flow.getFlow(e)) < eps)
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
					if (flow.getFlow(e) < eps)
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
					double directedFlow = flow.getFlow(e) * (g.edgeSource(e) == u ? +1 : -1);
					if (Math.abs(capacity.weight(e) - directedFlow) < eps)
						continue; // saturated edge
					visited.set(v);
					queue.enqueue(v);
				}
			}
		}

		return IVertexBiPartition.fromBitmap(g, visited);
	}

	static MinimumEdgeCutSt buildFromMaxFlow(MaximumFlow maxFlowAlg) {
		return new MinimumEdgeCutStAbstract() {

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

}
