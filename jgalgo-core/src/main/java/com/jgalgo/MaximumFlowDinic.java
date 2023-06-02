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
import it.unimi.dsi.fastutil.ints.IntArrayList;
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
class MaximumFlowDinic implements MaximumFlow {

	private Graph.Builder layerGraphBuilder = Graph.newBuilderDirected().setOption("impl", "GraphLinked");

	private static final Object FlowWeightKey = new Utils.Obj("flow");
	private static final Object CapacityWeightKey = new Utils.Obj("capacity");

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
	 * @param builder a builder that provide instances of graphs for the layers graph
	 */
	void setLayerGraphFactory(Graph.Builder builder) {
		layerGraphBuilder = Objects.requireNonNull(builder);
	}

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

		final Weights.Double flow;
		final Weights.Double capacity;

		Worker(Graph gOrig, FlowNetwork net, int source, int sink) {
			super(gOrig, net, source, sink);

			flow = g.addEdgesWeights(FlowWeightKey, double.class);
			capacity = g.addEdgesWeights(CapacityWeightKey, double.class);
			initCapacitiesAndFlows(flow, capacity);
		}

		double computeMaximumFlow() {
			Graph L = layerGraphBuilder.setDirected(true).useFixedEdgesIDs(true).expectedVerticesNum(n).build();
			for (int v = 0; v < n; v++)
				L.addVertex();
			Weights.Int edgeRefL = L.addEdgesWeights(EdgeRefWeightKey, int.class, Integer.valueOf(-1));
			IntPriorityQueue bfsQueue = new IntArrayFIFOQueue();
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
					for (EdgeIter eit = g.edgesOut(u).iterator(); eit.hasNext();) {
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

				searchBlockingFlow: for (;;) {
					IntStack path = new IntArrayList();
					searchAugPath: for (;;) {
						int u = path.isEmpty() ? source : L.edgeTarget(path.topInt());
						EdgeIter eit = L.edgesOut(u).iterator();
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
					for (int eL : pathList) {
						int e = edgeRefL.getInt(eL);
						f = Math.min(f, capacity.getDouble(e) - flow.getDouble(e));
					}

					// update flow of all edges on path
					for (int eL : pathList) {
						int e = edgeRefL.getInt(eL);
						int rev = twin.getInt(e);
						double newFlow = flow.getDouble(e) + f;
						double cap = capacity.getDouble(e);
						if (newFlow < cap) {
							flow.set(e, newFlow);
						} else {
							/* saturated, remove edge */
							flow.set(e, cap);
							L.removeEdge(eL);
						}
						flow.set(rev, flow.getDouble(rev) - f);
					}
				}
			}

			return constructResult(flow);
		}

	}

}
