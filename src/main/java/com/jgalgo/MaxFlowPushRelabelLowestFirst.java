package com.jgalgo;

import java.util.BitSet;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

public class MaxFlowPushRelabelLowestFirst implements MaxFlow {

	public MaxFlowPushRelabelLowestFirst() {
	}

	@Override
	public double calcMaxFlow(Graph g, FlowNetwork net, int source, int target) {
		if (!(g instanceof DiGraph))
			throw new IllegalArgumentException("only directed graphs are supported");
		if (net instanceof FlowNetworkInt) {
			return new WorkerInt((DiGraph) g, (FlowNetworkInt) net, source, target).calcMaxFlow();
		} else {
			return new Worker((DiGraph) g, net, source, target).calcMaxFlow();
		}
	}

	private class Worker extends MaxFlowPushRelabelAbstract.WorkerDouble {

		final ActiveQueue active;

		Worker(DiGraph gOrig, FlowNetwork net, int source, int target) {
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

		private final int[] label;
		private final int source;
		private final int target;

		private final BitSet isActive;
		private final IntPriorityQueue[] queues;
		// Lower bound for the lowest label, a.k.a a lower bound for the lowest entry in
		// 'queues' which is not empty
		private int level;

		ActiveQueue(MaxFlowPushRelabelAbstract.Worker worker) {
			g = worker.g;
			int n = g.vertices().size();
			isActive = new BitSet(n);

			label = worker.label;
			source = worker.source;
			target = worker.target;

			queues = new IntPriorityQueue[2 * n];
			for (int k = 1; k < queues.length; k++)
				queues[k] = new IntArrayFIFOQueue();

			// set source and target as 'active' to prevent them from entering the active
			// queue
			isActive.set(source);
			isActive.set(target);
		}

		void afterLabelRecompute() {
			for (int k = 1; k < queues.length; k++)
				queues[k].clear();

			level = 1;
			int n = g.vertices().size();
			for (int u = 0; u < n; u++)
				if (isActive.get(u) && u != source && u != target)
					queues[label[u]].enqueue(u);
		}

		boolean isEmpty() {
			for (; level < queues.length; level++)
				if (!queues[level].isEmpty())
					return false;
			return true;
		}

		int dequeue() {
			for (; level < queues.length; level++)
				if (!queues[level].isEmpty())
					return queues[level].dequeueInt();
			throw new IllegalStateException();
		}

		void afterPush(int e) {
			int v = g.edgeTarget(e);
			if (!isActive.get(v)) {
				isActive.set(v);
				queues[label[v]].enqueue(v);
				if (level > label[v])
					level = label[v];
			}
		}

		void afterDischarge(int u) {
			isActive.clear(u);
		}

	}
}
