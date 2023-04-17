package com.jgalgo;

import java.util.BitSet;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

public class MaxFlowPushRelabelHighestFirst implements MaxFlow {

	public MaxFlowPushRelabelHighestFirst() {
	}

	@Override
	public double calcMaxFlow(Graph g, FlowNetwork net, int source, int target) {
		if (!(g instanceof DiGraph))
			throw new IllegalArgumentException("only directed graphs are supported");
		if (net instanceof FlowNetworkInt) {
			return new WorkerInt((DiGraph) g, (FlowNetworkInt) net, source, target).calcMaxFlow();
		} else {
			return new WorkerDouble((DiGraph) g, net, source, target).calcMaxFlow();
		}
	}

	private class WorkerDouble extends MaxFlowPushRelabelAbstract.WorkerDouble {

		final ActiveQueue active;

		WorkerDouble(DiGraph gOrig, FlowNetwork net, int source, int target) {
			super(gOrig, net, source, target);
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
			active.init();
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

	private class WorkerInt extends MaxFlowPushRelabelAbstract.WorkerInt {

		final ActiveQueue active;

		WorkerInt(DiGraph gOrig, FlowNetworkInt net, int source, int target) {
			super(gOrig, net, source, target);
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
			active.init();
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
		private final BitSet isActive;

		private final int[] label;
		private final int source;
		private final int target;

		private final IntPriorityQueue[] queues;
		// Upper bound for the highest label, a.k.a an upper bound for the highest entry
		// in 'queues' which is not empty
		private int level;

		ActiveQueue(MaxFlowPushRelabelAbstract.Worker worker) {
			g = worker.g;
			n = g.vertices().size();
			isActive = new BitSet(n);

			label = worker.label;
			source = worker.source;
			target = worker.target;

			queues = new IntPriorityQueue[n];
			for (int k = 1; k < n; k++)
				queues[k] = new IntArrayFIFOQueue();

			// set source and target as 'active' to prevent them from entering the active
			// queue
			isActive.set(source);
			isActive.set(target);
		}

		void init() {
			for (int k = 1; k < n; k++)
				queues[k].clear();

			level = n - 1;
			int n = g.vertices().size();
			for (int u = 0; u < n; u++)
				if (isActive.get(u) && u != source && u != target) {
					int l = label[u];
					if (l < n)
						queues[l].enqueue(u);
				}
		}

		boolean isEmpty() {
			for (; level > 0; level--)
				if (!queues[level].isEmpty())
					return false;
			return true;
		}

		int dequeue() {
			for (; level > 0; level--)
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
				if (level < l)
					level = l;
			}
		}

		void afterDischarge(int u) {
			isActive.clear(u);

			// relabel way have been performed
			int l = label[u];
			assert l >= level;
			if (l < n)
				level = l;
			assert level < n;
		}

	}
}
