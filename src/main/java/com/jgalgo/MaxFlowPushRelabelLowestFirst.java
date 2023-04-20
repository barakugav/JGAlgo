package com.jgalgo;

import java.util.BitSet;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

/**
 * The push-relabel maximum flow algorithm with lowest-first ordering.
 * <p>
 * The push-relabel algorithm maintain a "preflow" and gradually converts it
 * into a maximum flow by moving flow locally between neighboring nodes using
 * <i>push</i> operations under the guidance of an admissible network maintained
 * by <i>relabel</i> operations.
 * <p>
 * Different variants of the push relabel algorithm exists, mostly different in
 * the order the vertices with excess (more in-going than out-going flow) are
 * examined. This implementation order these vertices by lowest-first order,
 * namely it examine vertices with low 'label' first, and achieve a running
 * time of {@code O(n}<sup>2</sup> {@code m)} using linear space.
 * <p>
 * Heuristics are crucial for the practical running time of push-relabel
 * algorithm, and this implementation uses the 'global relabeling' and 'gap'
 * heuristics.
 *
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/Push%E2%80%93relabel_maximum_flow_algorithm">Wikipedia</a>
 * @see MaxFlowPushRelabel
 * @see MaxFlowPushRelabelToFront
 * @see MaxFlowPushRelabelHighestFirst
 * @author Barak Ugav
 */
public class MaxFlowPushRelabelLowestFirst implements MaxFlow {

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
		void recomputeLabels() {
			super.recomputeLabels();
			active.afterLabelRecompute();
		}

		@Override
		boolean hasMoreVerticesToDischarge() {
			return !active.isEmpty();
		}

		@Override
		int nextVertexToDischarge() {
			return active.dequeue();
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
		void recomputeLabels() {
			super.recomputeLabels();
			active.afterLabelRecompute();
		}

		@Override
		boolean hasMoreVerticesToDischarge() {
			return !active.isEmpty();
		}

		@Override
		int nextVertexToDischarge() {
			return active.dequeue();
		}
	}

	private static class ActiveQueue {

		private final DiGraph g;
		private final int n;

		private final int[] label;
		private final int source;
		private final int sink;

		private final BitSet isActive;
		private final IntPriorityQueue[] queues;
		// Lower bound for the lowest label, a.k.a a lower bound for the lowest entry in
		// 'queues' which is not empty
		private int level;

		ActiveQueue(MaxFlowPushRelabelAbstract.Worker worker) {
			g = worker.g;
			n = g.vertices().size();
			isActive = new BitSet(n);

			label = worker.label;
			source = worker.source;
			sink = worker.sink;

			queues = new IntPriorityQueue[n];
			for (int k = 1; k < n; k++)
				queues[k] = new IntArrayFIFOQueue();

			// set source and sink as 'active' to prevent them from entering the active
			// queue
			isActive.set(source);
			isActive.set(sink);
		}

		void afterLabelRecompute() {
			for (int k = 1; k < n; k++)
				queues[k].clear();

			level = 1;
			int n = g.vertices().size();
			for (int u = 0; u < n; u++)
				if (isActive.get(u) && u != source && u != sink) {
					int l = label[u];
					if (l < n)
						queues[l].enqueue(u);
				}
		}

		boolean isEmpty() {
			for (; level < n; level++)
				if (!queues[level].isEmpty())
					return false;
			return true;
		}

		int dequeue() {
			for (; level < n; level++)
				if (!queues[level].isEmpty())
					return queues[level].dequeueInt();
			throw new IllegalStateException();
		}

		void afterPush(int e) {
			int v = g.edgeTarget(e);
			if (!isActive.get(v)) {
				isActive.set(v);
				int l = label[v];
				queues[l].enqueue(v);
				if (level > l)
					level = l;
			}
		}

		void afterDischarge(int u) {
			isActive.clear(u);
		}

	}
}
