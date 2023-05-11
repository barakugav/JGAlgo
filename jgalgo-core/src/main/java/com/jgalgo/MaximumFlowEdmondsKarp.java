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

import java.util.BitSet;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

/**
 * The Edmonds-Karp algorithm for maximum flow.
 * <p>
 * The most known implementation that solve the maximum flow problem. It does so by finding augmenting paths from the
 * source to the sink in the residual network, and saturating at least one edge in each path. This is a specification
 * Ford–Fulkerson method, which chooses the shortest augmenting path in each iteration. It runs in \(O(m^2 n)\) time and
 * linear space.
 * <p>
 * Based on the paper 'Theoretical improvements in algorithmic efficiency for network flow problems' by Jack Edmonds and
 * Richard M Karp.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Edmonds%E2%80%93Karp_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
public class MaximumFlowEdmondsKarp implements MaximumFlow {

	private final AllocatedMemory allocatedMemory = new AllocatedMemory();

	private static final Object FlowWeightKey = new Object();
	private static final Object CapacityWeightKey = new Object();

	/**
	 * Create a new maximum flow algorithm object.
	 */
	public MaximumFlowEdmondsKarp() {}

	@Override
	public double computeMaximumFlow(Graph g, FlowNetwork net, int source, int sink) {
		allocatedMemory.allocate(g.vertices().size());
		if (net instanceof FlowNetwork.Int) {
			return new WorkerInt(g, (FlowNetwork.Int) net, source, sink).computeMaxFlow();
		} else {
			return new WorkerDouble(g, net, source, sink).computeMaxFlow();
		}
	}

	private abstract class Worker extends MaximumFlowAbstract.Worker {

		Worker(Graph gOrig, FlowNetwork net, int source, int sink) {
			super(gOrig, net, source, sink);
		}

		void computeMaxFlow0() {
			int[] backtrack = allocatedMemory.backtrack;
			BitSet visited = allocatedMemory.visited;
			IntPriorityQueue queue = allocatedMemory.queue;

			for (;;) {
				queue.clear();
				visited.clear();
				visited.set(source);
				backtrack[sink] = -1;

				// perform BFS and find a path of non saturated edges from source to sink
				queue.enqueue(source);
				bfs: while (!queue.isEmpty()) {
					int u = queue.dequeueInt();
					for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
						int e = eit.nextInt();
						int v = eit.target();

						if (visited.get(v) || isSaturated(e))
							continue;
						backtrack[v] = e;
						if (v == sink)
							break bfs;
						visited.set(v);
						queue.enqueue(v);
					}
				}

				// no path to sink
				if (backtrack[sink] == -1)
					break;
				pushAlongPath(backtrack);
			}
		}

		abstract void pushAlongPath(int[] backtrack);

		abstract boolean isSaturated(int e);

	}

	private class WorkerDouble extends Worker {

		final Weights.Double flow;
		final Weights.Double capacity;

		private static final double EPS = 0.0001;

		WorkerDouble(Graph gOrig, FlowNetwork net, int source, int sink) {
			super(gOrig, net, source, sink);

			flow = g.addEdgesWeights(FlowWeightKey, double.class);
			capacity = g.addEdgesWeights(CapacityWeightKey, double.class);
			initCapacitiesAndFlows(flow, capacity);
		}

		double computeMaxFlow() {
			computeMaxFlow0();
			return constructResult(flow);
		}

		@Override
		void pushAlongPath(int[] backtrack) {
			// find out what is the maximum flow we can pass
			double f = Double.MAX_VALUE;
			for (int p = sink; p != source;) {
				int e = backtrack[p];
				f = Math.min(f, getResidualCapacity(e));
				p = g.edgeSource(e);
			}

			// update flow of all edges on path
			for (int p = sink; p != source;) {
				int e = backtrack[p], rev = twin.getInt(e);
				flow.set(e, flow.getDouble(e) + f);
				flow.set(rev, flow.getDouble(rev) - f);
				p = g.edgeSource(e);
			}
		}

		double getResidualCapacity(int e) {
			return capacity.getDouble(e) - flow.getDouble(e);
		}

		@Override
		boolean isSaturated(int e) {
			return getResidualCapacity(e) <= EPS;
		}
	}

	private class WorkerInt extends Worker {

		final Weights.Int flow;
		final Weights.Int capacity;

		WorkerInt(Graph gOrig, FlowNetwork.Int net, int source, int sink) {
			super(gOrig, net, source, sink);

			flow = g.addEdgesWeights(FlowWeightKey, int.class);
			capacity = g.addEdgesWeights(CapacityWeightKey, int.class);
			initCapacitiesAndFlows(flow, capacity);
		}

		int computeMaxFlow() {
			computeMaxFlow0();
			return constructResult(flow);
		}

		@Override
		void pushAlongPath(int[] backtrack) {
			// find out what is the maximum flow we can pass
			int f = Integer.MAX_VALUE;
			for (int p = sink; p != source;) {
				int e = backtrack[p];
				f = Math.min(f, getResidualCapacity(e));
				p = g.edgeSource(e);
			}

			// update flow of all edges on path
			for (int p = sink; p != source;) {
				int e = backtrack[p], rev = twin.getInt(e);
				flow.set(e, flow.getInt(e) + f);
				flow.set(rev, flow.getInt(rev) - f);
				p = g.edgeSource(e);
			}
		}

		int getResidualCapacity(int e) {
			return capacity.getInt(e) - flow.getInt(e);
		}

		@Override
		boolean isSaturated(int e) {
			return getResidualCapacity(e) <= 0;
		}
	}

	private static class AllocatedMemory {
		int[] backtrack = IntArrays.EMPTY_ARRAY;
		BitSet visited;
		IntPriorityQueue queue;

		void allocate(int n) {
			backtrack = MemoryReuse.ensureLength(backtrack, n);
			visited = MemoryReuse.ensureAllocated(visited, BitSet::new);
			queue = MemoryReuse.ensureAllocated(queue, IntArrayFIFOQueue::new);
		}
	}

}
