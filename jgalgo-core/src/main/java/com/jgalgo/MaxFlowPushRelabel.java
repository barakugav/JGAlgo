package com.jgalgo;

import java.util.BitSet;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

/**
 * The push-relabel maximum flow algorithm with FIFO ordering.
 * <p>
 * The push-relabel algorithm maintain a "preflow" and gradually converts it
 * into a maximum flow by moving flow locally between neighboring nodes using
 * <i>push</i> operations under the guidance of an admissible network maintained
 * by <i>relabel</i> operations.
 * <p>
 * Different variants of the push relabel algorithm exists, mostly different in
 * the order the vertices with excess (more in-going than out-going flow) are
 * examined. This implementation order these vertices in a first-in-first-out
 * (FIFO) order, and achieve a running time of {@code O(n}<sup>3</sup>{@code )}
 * using linear space.
 * <p>
 * Heuristics are crucial for the practical running time of push-relabel
 * algorithm, and this implementation uses the 'global relabeling' and 'gap'
 * heuristics.
 * <p>
 * This algorithm can be implemented with better time theoretical bound using
 * {@link DynamicTree}, but in practice it has little to non advantages. See
 * {@link MaxFlowDinicDynamicTrees}.
 *
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/Push%E2%80%93relabel_maximum_flow_algorithm">Wikipedia</a>
 * @see MaxFlowPushRelabelToFront
 * @see MaxFlowPushRelabelHighestFirst
 * @see MaxFlowPushRelabelLowestFirst
 * @author Barak Ugav
 */
public class MaxFlowPushRelabel implements MaxFlow {

	/**
	 * Create a new maximum flow algorithm object.
	 */
	public MaxFlowPushRelabel() {
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not directed
	 */
	@Override
	public double computeMaximumFlow(Graph g, FlowNetwork net, int source, int sink) {
		if (!(g instanceof DiGraph))
			throw new IllegalArgumentException("only directed graphs are supported");
		if (net instanceof FlowNetwork.Int) {
			return new WorkerInt((DiGraph) g, (FlowNetwork.Int) net, source, sink).computeMaxFlow();
		} else {
			return new WorkerDouble((DiGraph) g, net, source, sink).computeMaxFlow();
		}
	}

	private static class WorkerDouble extends MaxFlowPushRelabelAbstract.WorkerDouble {

		final ActiveQueue active;

		WorkerDouble(DiGraph gOrig, FlowNetwork net, int source, int sink) {
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

	private static class WorkerInt extends MaxFlowPushRelabelAbstract.WorkerInt {

		final ActiveQueue active;

		WorkerInt(DiGraph gOrig, FlowNetwork.Int net, int source, int sink) {
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

		private final DiGraph g;
		final BitSet isActive;
		final IntPriorityQueue queue;

		ActiveQueue(MaxFlowPushRelabelAbstract.Worker worker) {
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