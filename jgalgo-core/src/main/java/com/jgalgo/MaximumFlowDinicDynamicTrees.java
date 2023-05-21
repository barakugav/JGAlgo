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
import com.jgalgo.DynamicTree.MinEdge;
import com.jgalgo.IDStrategy.Fixed;
import com.jgalgo.Utils.IntDoubleConsumer;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Dinic's algorithm for maximum flow using dynamic trees.
 * <p>
 * Using dynamic trees, the algorithm of Dinic to maximum flow problem is implemented in time \(O(m n \log n)\) and
 * linear space. In practice, the (relative) complicated implementation of dynamic trees have little gain in the overall
 * performance, and its probably better to use some variant of the {@link MaximumFlowPushRelabel}, which has worse
 * theoretically bounds, but runs faster in practice.
 *
 * @see    MaximumFlowDinic
 * @author Barak Ugav
 */
public class MaximumFlowDinicDynamicTrees implements MaximumFlow {

	private final DebugPrintsManager debug = new DebugPrintsManager(false);
	private static final double EPS = 0.0001;
	static final Object EdgeRefWeightKey = new Object();
	static final Object EdgeRevWeightKey = new Object();
	private static final Object FlowWeightKey = new Object();
	private static final Object CapacityWeightKey = new Object();

	/**
	 * Create a new maximum flow algorithm object.
	 */
	public MaximumFlowDinicDynamicTrees() {}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not directed
	 */
	@Override
	public double computeMaximumFlow(Graph g, FlowNetwork net, int source, int sink) {
		return new Worker(g, net, source, sink).computeMaximumFlow();
	}

	private class Worker extends MaximumFlowAbstract.Worker {

		final Weights.Double capacity;
		final Weights.Double flow;

		Worker(Graph gOrig, FlowNetwork net, int source, int sink) {
			super(gOrig, net, source, sink);

			flow = g.addEdgesWeights(FlowWeightKey, double.class);
			capacity = g.addEdgesWeights(CapacityWeightKey, double.class);
			initCapacitiesAndFlows(flow, capacity);
		}

		double computeMaximumFlow() {
			debug.println("\t", getClass().getSimpleName());

			double maxCapacity = 100;
			for (IntIterator it = gOrig.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				maxCapacity = Math.max(maxCapacity, net.getCapacity(e));
			}

			GraphBuilder builder = GraphBuilder.newDirected().setOption("impl", "GraphLinked");
			Graph L = builder.setEdgesIDStrategy(Fixed.class).build(n);
			Weights.Int edgeRefL = L.addEdgesWeights(EdgeRefWeightKey, int.class);
			IntPriorityQueue bfsQueue = new IntArrayFIFOQueue();
			int[] level = new int[n];
			DynamicTree dt = new DynamicTreeSplay(maxCapacity * 10);
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
					for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
						int e = eit.nextInt();
						int v = eit.target();
						if (flow.getDouble(e) >= capacity.getDouble(e) || level[v] <= lvl)
							continue;
						edgeRefL.set(L.addEdge(u, v), e);
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
					double currentFlow = flow.getDouble(e);
					double f = capacity.getDouble(e) - currentFlow - weight;
					int eTwin = twin.getInt(e);
					flow.set(e, currentFlow + f);
					flow.set(eTwin, flow.getDouble(eTwin) - f);
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
							int gEdge = edgeRefL.getInt(e);
							L.removeEdge(e);

							updateFlow.accept(gEdge, 0);
							dt.cut(min.source());

							min = dt.findMinEdge(vToDt[source]);
						} while (min != null && Math.abs(min.weight()) < EPS);

					} else if (!L.edgesOut(v).hasNext()) {

						/* Retreat */
						debug.println("Retreat");
						if (v == source)
							break calcBlockFlow;
						for (EdgeIter eit = L.edgesIn(v); eit.hasNext();) {
							int e = eit.nextInt();
							int u = eit.source();
							if (vToDt[u].getParent() != vToDt[v])
								continue; /* If the edge is not in the DT, ignore */

							MinEdge m = dt.findMinEdge(vToDt[u]);
							assert e == edgeToParent[m.source().<Integer>getNodeData().intValue()];
							int gEdge = edgeRefL.getInt(e);
							updateFlow.accept(gEdge, m.weight());

							dt.cut(m.source());
						}
						L.removeEdgesInOf(v);

					} else {
						/* Advance */
						debug.println("Advance");
						EdgeIter eit = L.edgesOut(v);
						int e = eit.nextInt();
						int gEdge = edgeRefL.getInt(e);
						dt.link(vToDt[eit.source()], vToDt[eit.target()],
								capacity.getDouble(gEdge) - flow.getDouble(gEdge));
						edgeToParent[eit.source()] = e;
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
						int gEdge = edgeRefL.getInt(edgeToParent[m.source().<Integer>getNodeData().intValue()]);
						updateFlow.accept(gEdge, m.weight());
						dt.cut(m.source());
					}
				}
			}

			return constructResult(flow);
		}

	}

}
