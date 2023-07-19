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

import java.util.Arrays;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.data.DynamicTree;
import com.jgalgo.internal.data.DynamicTree.MinEdge;
import com.jgalgo.internal.util.DebugPrintsManager;
import com.jgalgo.internal.util.IntArrayFIFOQueue;
import com.jgalgo.internal.util.Utils.IntDoubleConsumer;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Dinic's algorithm for maximum flow using dynamic trees.
 * <p>
 * Using dynamic trees, the algorithm of Dinic to maximum flow problem is implemented in time \(O(m n \log n)\) and
 * linear space. In practice, the (relative) complicated implementation of dynamic trees have little gain in the overall
 * performance, and its probably better to use some variant of the {@link MaximumFlowPushRelabelFifo}, which has worse
 * theoretically bounds, but runs faster in practice.
 *
 * @see    MaximumFlowDinic
 * @author Barak Ugav
 */
class MaximumFlowDinicDynamicTrees extends MaximumFlowAbstract {

	private final DebugPrintsManager debug = new DebugPrintsManager(false);
	private static final double EPS = 0.0001;

	/**
	 * Create a new maximum flow algorithm object.
	 */
	MaximumFlowDinicDynamicTrees() {}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not directed
	 */
	@Override
	double computeMaximumFlow(IndexGraph g, FlowNetwork net, int source, int sink) {
		return new Worker(g, net, source, sink).computeMaximumFlow();
	}

	private class Worker extends MaximumFlowAbstract.Worker {

		final double[] capacity;
		final double[] flow;

		Worker(IndexGraph gOrig, FlowNetwork net, int source, int sink) {
			super(gOrig, net, source, sink);

			flow = new double[g.edges().size()];
			capacity = new double[g.edges().size()];
			initCapacitiesAndFlows(flow, capacity);
		}

		double computeMaximumFlow() {
			debug.println("\t", getClass().getSimpleName());

			double capacitySum = 100;
			for (int e : gOrig.edges())
				capacitySum += net.getCapacity(e);
			capacitySum *= 16;

			GraphFactory factory = GraphFactory.newDirected().setOption("impl", "GraphLinked");
			Graph L = factory.expectedVerticesNum(n).expectedEdgesNum(/* >= */ n).newGraph();
			for (int v : g.vertices())
				L.addVertex(v);

			IntPriorityQueue bfsQueue = new IntArrayFIFOQueue();
			int[] level = new int[n];
			DynamicTree dt = DynamicTree.newBuilder().setMaxWeight(capacitySum > 0 ? capacitySum : 1e100).build();
			DynamicTree.Node[] vToDt = new DynamicTree.Node[n];
			Stack<DynamicTree.Node> cleanupStack = new ObjectArrayList<>();

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
					for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						int v = eit.target();
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
					(vToDt[u] = dt.makeTree()).setNodeData(Integer.valueOf(u));

				IntDoubleConsumer updateFlow = (e, weight) -> {
					double currentFlow = flow[e];
					double f = capacity[e] - currentFlow - weight;
					int t = twin[e];
					flow[e] = currentFlow + f;
					flow[t] -= f;
				};

				calcBlockFlow: for (;;) {
					int v = dt.findRoot(vToDt[source]).<Integer>getNodeData().intValue();
					if (v == sink) {

						/* Augment */
						debug.println("Augment");
						MinEdge min = dt.findMinEdge(vToDt[source]);
						dt.addWeight(vToDt[source], -min.weight());

						/* Delete all saturated edges */
						debug.println("Delete");
						do {
							int e = edgeToParent[min.source().<Integer>getNodeData().intValue()];
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
						for (EdgeIter eit = L.inEdges(v).iterator(); eit.hasNext();) {
							int e = eit.nextInt();
							int u = g.edgeSource(e);
							if (edgeToParent[u] != e)
								continue; /* If the edge is not in the DT, ignore */
							assert vToDt[u].getParent() == vToDt[v];

							MinEdge m = dt.findMinEdge(vToDt[u]);
							assert e == edgeToParent[m.source().<Integer>getNodeData().intValue()];
							edgeToParent[u] = -1;
							updateFlow.accept(e, m.weight());

							dt.cut(m.source());
						}
						L.removeInEdgesOf(v);

					} else {
						/* Advance */
						debug.println("Advance");
						EdgeIter eit = L.outEdges(v).iterator();
						int e = eit.nextInt();
						int eSource = g.edgeSource(e);
						int eTarget = g.edgeTarget(e);
						dt.link(vToDt[eSource], vToDt[eTarget], capacity[e] - flow[e]);
						edgeToParent[eSource] = e;
					}
				}

				/* Cleanup all the edges that stayed in the DT */
				for (int u = 0; u < n; u++) {
					for (DynamicTree.Node uDt = vToDt[u], pDt; (pDt = uDt.getParent()) != null; uDt = pDt)
						cleanupStack.push(uDt);
					while (!cleanupStack.isEmpty()) {
						DynamicTree.Node uDt = cleanupStack.pop();
						assert uDt.getParent() == dt.findRoot(uDt);
						MinEdge m = dt.findMinEdge(uDt);
						int eSource = m.source().<Integer>getNodeData().intValue();
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
