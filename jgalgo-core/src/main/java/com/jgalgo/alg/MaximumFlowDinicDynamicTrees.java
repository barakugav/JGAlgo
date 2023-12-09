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

import java.util.Arrays;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.ds.DynamicTree;
import com.jgalgo.internal.ds.DynamicTree.MinEdge;
import com.jgalgo.internal.util.DebugPrinter;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import com.jgalgo.internal.util.JGAlgoUtils.IntDoubleConsumer;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Dinic's algorithm for maximum flow using dynamic trees.
 *
 * <p>
 * Using dynamic trees, the algorithm of Dinic to maximum flow problem is implemented in time \(O(m n \log n)\) and
 * linear space. In practice, the (relative) complicated implementation of dynamic trees have little gain in the overall
 * performance, and its probably better to use some variant of the {@link MaximumFlowPushRelabelFifo}, which has worse
 * theoretically bounds, but runs faster in practice.
 *
 * @see    MaximumFlowDinic
 * @author Barak Ugav
 */
class MaximumFlowDinicDynamicTrees extends MaximumFlowAbstract.WithResidualGraph {

	private final DebugPrinter debug = new DebugPrinter(false);
	private static final double EPS = 0.0001;

	/**
	 * Create a new maximum flow algorithm object.
	 */
	MaximumFlowDinicDynamicTrees() {}

	@Override
	IFlow computeMaximumFlow(IndexGraph g, IWeightFunction capacity, int source, int sink) {
		return new Worker(g, capacity, source, sink).computeMaximumFlow();
	}

	@Override
	IFlow computeMaximumFlow(IndexGraph g, IWeightFunction capacity, IntCollection sources, IntCollection sinks) {
		return new Worker(g, capacity, sources, sinks).computeMaximumFlow();
	}

	private class Worker extends MaximumFlowAbstract.WithResidualGraph.Worker {

		final double[] capacity;
		final double[] flow;

		Worker(IndexGraph gOrig, IWeightFunction capacityOrig, int source, int sink) {
			super(gOrig, capacityOrig, source, sink);

			flow = new double[g.edges().size()];
			capacity = new double[g.edges().size()];
			initCapacitiesAndFlows(flow, capacity);
		}

		Worker(IndexGraph gOrig, IWeightFunction capacityOrig, IntCollection sources, IntCollection sinks) {
			super(gOrig, capacityOrig, sources, sinks);

			flow = new double[g.edges().size()];
			capacity = new double[g.edges().size()];
			initCapacitiesAndFlows(flow, capacity);
		}

		IFlow computeMaximumFlow() {
			debug.println("\t", getClass().getSimpleName());

			double capacitySum = 100;
			if (WeightFunction.isCardinality(capacityOrig)) {
				capacitySum += gOrig.edges().size();
			} else {
				for (int m = gOrig.edges().size(), e = 0; e < m; e++)
					capacitySum += capacityOrig.weight(e);
			}
			capacitySum *= 16;

			IntGraphFactory factory = IntGraphFactory.directed().setOption("impl", "linked-list");
			IntGraph L = factory.expectedVerticesNum(n).expectedEdgesNum(/* >= */ n).newGraph();
			for (int n = g.vertices().size(), v = 0; v < n; v++)
				L.addVertex(v);

			IntPriorityQueue bfsQueue = new FIFOQueueIntNoReduce();
			int[] level = new int[n];
			DynamicTree dt = DynamicTree.builder().setMaxWeight(capacitySum > 0 ? capacitySum : 1e100).build();
			DynamicTree.Vertex[] vToDt = new DynamicTree.Vertex[n];
			Stack<DynamicTree.Vertex> cleanupStack = new ObjectArrayList<>();

			int[] edgeToParent = new int[n];
			Arrays.fill(edgeToParent, -1);

			for (;;) {
				debug.println("calculating residual network");
				L.clearEdges();

				/* Calc the sub graph non saturated edges from source to sink using BFS */
				final int unvisited = Integer.MAX_VALUE;
				Arrays.fill(level, unvisited);
				bfsQueue.clear();
				level[source] = 0;
				bfsQueue.enqueue(source);
				bfs: while (!bfsQueue.isEmpty()) {
					int u = bfsQueue.dequeueInt();
					if (u == sink)
						break bfs;
					int lvl = level[u];
					for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						int v = eit.targetInt();
						if (flow[e] >= capacity[e] || level[v] <= lvl)
							continue;
						L.addEdge(u, v, e);
						if (level[v] != unvisited)
							continue;
						level[v] = lvl + 1;
						bfsQueue.enqueue(v);
					}
				}
				if (level[sink] == unvisited)
					break; /* All paths to sink are saturated */
				debug.println("sink level: " + level[sink]);

				dt.clear();
				for (int u = 0; u < n; u++)
					(vToDt[u] = dt.makeTree()).setData(Integer.valueOf(u));

				IntDoubleConsumer updateFlow = (e, weight) -> {
					double currentFlow = flow[e];
					double f = capacity[e] - currentFlow - weight;
					int t = twin[e];
					flow[e] = currentFlow + f;
					flow[t] -= f;
				};

				calcBlockFlow: for (;;) {
					int v = dt.findRoot(vToDt[source]).<Integer>getData().intValue();
					if (v == sink) {

						/* Augment */
						debug.println("Augment");
						MinEdge min = dt.findMinEdge(vToDt[source]);
						dt.addWeight(vToDt[source], -min.weight());

						/* Delete all saturated edges */
						debug.println("Delete");
						do {
							int e = edgeToParent[min.source().<Integer>getData().intValue()];
							assert vToDt[L.edgeSource(e)] == min.source();
							L.removeEdge(e);

							updateFlow.accept(e, 0);
							dt.cut(min.source());

							min = dt.findMinEdge(vToDt[source]);
						} while (min != null && Math.abs(min.weight()) < EPS);

					} else if (L.outEdges(v).isEmpty()) {

						/* Retreat */
						debug.println("Retreat");
						if (v == source)
							break calcBlockFlow;
						for (IEdgeIter eit = L.inEdges(v).iterator(); eit.hasNext();) {
							int e = eit.nextInt();
							int u = g.edgeSource(e);
							if (edgeToParent[u] != e)
								continue; /* If the edge is not in the DT, ignore */
							assert vToDt[u].getParent() == vToDt[v];

							MinEdge m = dt.findMinEdge(vToDt[u]);
							assert e == edgeToParent[m.source().<Integer>getData().intValue()];
							edgeToParent[u] = -1;
							updateFlow.accept(e, m.weight());

							dt.cut(m.source());
						}
						L.removeInEdgesOf(v);

					} else {
						/* Advance */
						debug.println("Advance");
						IEdgeIter eit = L.outEdges(v).iterator();
						int e = eit.nextInt();
						int eSource = g.edgeSource(e);
						int eTarget = g.edgeTarget(e);
						dt.link(vToDt[eSource], vToDt[eTarget], capacity[e] - flow[e]);
						edgeToParent[eSource] = e;
					}
				}

				/* Cleanup all the edges that stayed in the DT */
				for (int u = 0; u < n; u++) {
					for (DynamicTree.Vertex uDt = vToDt[u], pDt; (pDt = uDt.getParent()) != null; uDt = pDt)
						cleanupStack.push(uDt);
					while (!cleanupStack.isEmpty()) {
						DynamicTree.Vertex uDt = cleanupStack.pop();
						assert uDt.getParent() == dt.findRoot(uDt);
						MinEdge m = dt.findMinEdge(uDt);
						int eSource = m.source().<Integer>getData().intValue();
						int e = edgeToParent[eSource];
						edgeToParent[eSource] = -1;
						updateFlow.accept(e, m.weight());
						dt.cut(m.source());
					}
				}
			}

			return constructResult(flow);
		}

	}

}
