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

import java.util.Arrays;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

/**
 * Dinic's algorithm for maximum flow.
 *
 * <p>
 * The algorithm finds a maximum flow by repetitively finding a blocking flow in the residual network. It runs in \(O(m
 * n^2)\) time and use linear space.
 *
 * <p>
 * Based on the paper 'Algorithm for solution of a problem of maximum flow in a network with power estimation' by Y. A.
 * Dinitz (Dinic).
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Dinic%27s_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
public class MaximumFlowDinic extends MaximumFlowAbstractWithResidualNet {

	/**
	 * Create a new maximum flow algorithm object.
	 *
	 * <p>
	 * Please prefer using {@link MaximumFlow#newInstance()} to get a default implementation for the {@link MaximumFlow}
	 * interface.
	 */
	public MaximumFlowDinic() {}

	@Override
	protected IFlow computeMaximumFlow(IndexGraph g, IWeightFunction capacity, int source, int sink) {
		return new Worker(g, capacity, source, sink).computeMaximumFlow();
	}

	@Override
	protected IFlow computeMaximumFlow(IndexGraph g, IWeightFunction capacity, IntCollection sources,
			IntCollection sinks) {
		return new Worker(g, capacity, sources, sinks).computeMaximumFlow();
	}

	private class Worker extends MaximumFlowAbstractWithResidualNet.Worker {

		final double[] flow;
		final double[] capacity;

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
			Assertions.onlyDirected(g);
			Bitmap residual = new Bitmap(g.edges().size());

			IntPriorityQueue bfsQueue = new FIFOQueueIntNoReduce();
			int[] level = new int[n];
			IntArrayList path = new IntArrayList();
			IEdgeIter[] edgeIters = new IEdgeIter[n];

			for (;;) {
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
						residual.set(e);
						if (level[v] != unvisited)
							continue;
						level[v] = lvl + 1;
						bfsQueue.enqueue(v);
					}
				}
				if (level[sink] == unvisited)
					break; /* All paths to sink are saturated */

				searchBlockingFlow: for (;;) {
					path.clear();
					searchAugPath: for (;;) {
						int u = path.isEmpty() ? source : g.edgeTarget(path.topInt());
						IEdgeIter eit = edgeIters[u];
						if (eit == null)
							eit = edgeIters[u] = g.outEdges(u).iterator();
						for (; eit.hasNext(); eit.nextInt())
							if (residual.get(eit.peekNextInt()))
								break;

						if (!eit.hasNext()) {
							if (path.isEmpty()) {
								// no path from source to sink
								break searchBlockingFlow;
							} else {
								// retreat
								int e = path.popInt();
								residual.clear(e);
								continue searchAugPath;
							}
						}

						int e = eit.peekNextInt();
						path.push(e);
						if (g.edgeTarget(e) == sink) {
							// augment
							break searchAugPath;
						} else {
							// advance
						}
					}

					// augment the path we found
					assert path.size() > 0;

					// find out what is the maximum flow we can pass
					double f = path.intStream().mapToDouble(e -> capacity[e] - flow[e]).min().orElse(Double.MAX_VALUE);

					// update flow of all edges on path
					for (int e : path) {
						int t = twin[e];
						double newFlow = flow[e] + f;
						double cap = capacity[e];
						if (newFlow < cap) {
							flow[e] = newFlow;
						} else {
							/* saturated, remove edge */
							flow[e] = cap;
							residual.clear(e);
						}
						flow[t] -= f;
					}
				}
				residual.clear();
				Arrays.fill(edgeIters, null);
			}

			return constructResult(flow);
		}

	}

}
