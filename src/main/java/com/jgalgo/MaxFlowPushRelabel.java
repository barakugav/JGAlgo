package com.jgalgo;

import java.util.BitSet;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

public class MaxFlowPushRelabel implements MaxFlow {

	/**
	 * Push-relabel implementation with FIFO ordering.
	 *
	 * O(n^3)
	 */

	public MaxFlowPushRelabel() {
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
		boolean hasMoreVerticesToDischarge() {
			return !active.queue.isEmpty();
		}

		@Override
		int nextVertexToDischarge() {
			return active.queue.dequeueInt();
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

			// set source and target as 'active' to prevent them from entering the active
			// queue
			isActive.set(worker.source);
			isActive.set(worker.target);
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
