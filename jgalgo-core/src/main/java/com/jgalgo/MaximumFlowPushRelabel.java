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
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

/**
 * The push-relabel maximum flow algorithm with FIFO ordering.
 * <p>
 * The push-relabel algorithm maintain a "preflow" and gradually converts it into a maximum flow by moving flow locally
 * between neighboring nodes using <i>push</i> operations under the guidance of an admissible network maintained by
 * <i>relabel</i> operations.
 * <p>
 * Different variants of the push relabel algorithm exists, mostly different in the order the vertices with excess (more
 * in-going than out-going flow) are examined. This implementation order these vertices in a first-in-first-out (FIFO)
 * order, and achieve a running time of \(O(n^3)\) using linear space.
 * <p>
 * Heuristics are crucial for the practical running time of push-relabel algorithm, and this implementation uses the
 * 'global relabeling' and 'gap' heuristics.
 * <p>
 * This algorithm can be implemented with better time theoretical bound using {@link DynamicTree}, but in practice it
 * has little to non advantages. See {@link MaximumFlowPushRelabelDynamicTrees}.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Push%E2%80%93relabel_maximum_flow_algorithm">Wikipedia</a>
 * @see    MaximumFlowPushRelabelToFront
 * @see    MaximumFlowPushRelabelHighestFirst
 * @see    MaximumFlowPushRelabelLowestFirst
 * @author Barak Ugav
 */
public class MaximumFlowPushRelabel extends MaximumFlowPushRelabelAbstract {

	/**
	 * Create a new maximum flow algorithm object.
	 */
	public MaximumFlowPushRelabel() {}

	@Override
	WorkerDouble newWorkerDouble(Graph gOrig, FlowNetwork net, int source, int sink) {
		return new WorkerDouble(gOrig, net, source, sink);
	}

	@Override
	WorkerInt newWorkerInt(Graph gOrig, FlowNetwork.Int net, int source, int sink) {
		return new WorkerInt(gOrig, net, source, sink);
	}

	private static class WorkerDouble extends MaximumFlowPushRelabelAbstract.WorkerDouble {

		final ActiveQueue active;

		WorkerDouble(Graph gOrig, FlowNetwork net, int source, int sink) {
			super(gOrig, net, source, sink);
			active = new ActiveQueue(this);
		}

		@Override
		void push(int e, double f) {
			super.push(e, f);
			active.afterPush(e);
		};

		@Override
		void discharge(int u) {
			super.discharge(u);
			active.afterDischarge(u);
		}

		@Override
		boolean hasMoreVerticesToDischarge() {
			return !active.queue.isEmpty();
		}

		@Override
		int nextVertexToDischarge() {
			return active.queue.dequeueInt();
		}
	}

	private static class WorkerInt extends MaximumFlowPushRelabelAbstract.WorkerInt {

		final ActiveQueue active;

		WorkerInt(Graph gOrig, FlowNetwork.Int net, int source, int sink) {
			super(gOrig, net, source, sink);
			active = new ActiveQueue(this);
		}

		@Override
		void push(int e, int f) {
			super.push(e, f);
			active.afterPush(e);
		};

		@Override
		void discharge(int u) {
			super.discharge(u);
			active.afterDischarge(u);
		}

		@Override
		boolean hasMoreVerticesToDischarge() {
			return !active.queue.isEmpty();
		}

		@Override
		int nextVertexToDischarge() {
			return active.queue.dequeueInt();
		}
	}

	private static class ActiveQueue {

		private final Graph g;
		final BitSet isActive;
		final IntPriorityQueue queue;

		ActiveQueue(MaximumFlowPushRelabelAbstract.Worker worker) {
			g = worker.g;
			int n = g.vertices().size();
			isActive = new BitSet(n);
			queue = new IntArrayFIFOQueue();

			// set source and sink as 'active' to prevent them from entering the active
			// queue
			isActive.set(worker.source);
			isActive.set(worker.sink);
		}

		void afterPush(int e) {
			int v = g.edgeTarget(e);
			if (!isActive.get(v)) {
				isActive.set(v);
				queue.enqueue(v);
			}
		}

		void afterDischarge(int u) {
			isActive.clear(u);
		}

	}
}
