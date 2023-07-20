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
import java.util.Objects;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntStack;

/**
 * Dinic's algorithm for maximum flow.
 * <p>
 * The algorithm finds a maximum flow by repetitively finding a blocking flow in the residual network. It runs in \(O(m
 * n^2)\) time and use linear space.
 * <p>
 * Based on the paper 'Algorithm for solution of a problem of maximum flow in a network with power estimation' by Y. A.
 * Dinitz (Dinic).
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Dinic%27s_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
class MaximumFlowDinic extends MaximumFlowAbstract {

	private GraphFactory layerGraphFactory = GraphFactory.newDirected().setOption("impl", "GraphLinked");

	/**
	 * Create a new maximum flow algorithm object.
	 */
	MaximumFlowDinic() {}

	/**
	 * Set the graph implementation used by this algorithm for the layers graph.
	 * <p>
	 * Multiple {@code remove} operations are performed on the layers graph, therefore its non trivial that an array
	 * graph implementation should be used, as linked graph implementation perform {@code remove} operations more
	 * efficiently.
	 *
	 * @param factory a factory that provide instances of graphs for the layers graph
	 */
	void setLayerGraphFactory(GraphFactory factory) {
		layerGraphFactory = Objects.requireNonNull(factory);
	}

	@Override
	double computeMaximumFlow(IndexGraph g, FlowNetwork net, int source, int sink) {
		return new Worker(g, net, source, sink).computeMaximumFlow();
	}

	@Override
	double computeMaximumFlow(IndexGraph g, FlowNetwork net, IntCollection sources, IntCollection sinks) {
		return new Worker(g, net, sources, sinks).computeMaximumFlow();
	}

	private class Worker extends MaximumFlowAbstract.Worker {

		final double[] flow;
		final double[] capacity;

		Worker(IndexGraph gOrig, FlowNetwork net, int source, int sink) {
			super(gOrig, net, source, sink);

			flow = new double[g.edges().size()];
			capacity = new double[g.edges().size()];
			initCapacitiesAndFlows(flow, capacity);
		}

		Worker(IndexGraph gOrig, FlowNetwork net, IntCollection sources, IntCollection sinks) {
			super(gOrig, net, sources, sinks);

			flow = new double[g.edges().size()];
			capacity = new double[g.edges().size()];
			initCapacitiesAndFlows(flow, capacity);
		}

		double computeMaximumFlow() {
			Graph L =
					layerGraphFactory.setDirected(true).expectedVerticesNum(/* >= */ n).expectedEdgesNum(n).newGraph();
			for (int n = g.vertices().size(), v = 0; v < n; v++)
				L.addVertex(v);

			IntPriorityQueue bfsQueue = new FIFOQueueIntNoReduce();
			int[] level = new int[n];

			for (;;) {
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

				searchBlockingFlow: for (;;) {
					IntStack path = new IntArrayList();
					searchAugPath: for (;;) {
						int u = path.isEmpty() ? source : L.edgeTarget(path.topInt());
						EdgeIter eit = L.outEdges(u).iterator();
						if (!eit.hasNext()) {
							if (path.isEmpty()) {
								// no path from source to sink
								break searchBlockingFlow;
							} else {
								// retreat
								int e = path.popInt();
								L.removeEdge(e);
								continue searchAugPath;
							}
						}

						int e = eit.nextInt();
						path.push(e);
						if (eit.target() == sink) {
							// augment
							break searchAugPath;
						} else {
							// advance
						}
					}

					// augment the path we found
					IntList pathList = (IntList) path;
					assert pathList.size() > 0;

					// find out what is the maximum flow we can pass
					double f = Double.MAX_VALUE;
					for (int e : pathList)
						f = Math.min(f, capacity[e] - flow[e]);

					// update flow of all edges on path
					for (int e : pathList) {
						int t = twin[e];
						double newFlow = flow[e] + f;
						double cap = capacity[e];
						if (newFlow < cap) {
							flow[e] = newFlow;
						} else {
							/* saturated, remove edge */
							flow[e] = cap;
							L.removeEdge(e);
						}
						flow[t] -= f;
					}
				}
			}

			return constructResult(flow);
		}

	}

}
