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
		boolean dischargeOrRelabel(int u) {
			boolean relabeled = super.dischargeOrRelabel(u);
			active.afterDischargeOrRelabel(u, relabeled);
			return relabeled;
		}

		double calcMaxFlow() {
			initLabels();
			pushAsMuchFromSource();
			while (!active.queue.isEmpty()) {
				int u = active.queue.dequeueInt();
				assert u != source && u != target;
				dischargeOrRelabel(u);
			}
			return constructResult();
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
		boolean dischargeOrRelabel(int u) {
			boolean relabeled = super.dischargeOrRelabel(u);
			active.afterDischargeOrRelabel(u, relabeled);
			return relabeled;
		}

		double calcMaxFlow() {
			initLabels();
			pushAsMuchFromSource();
			while (!active.queue.isEmpty()) {
				int u = active.queue.dequeueInt();
				assert u != source && u != target;
				dischargeOrRelabel(u);
			}
			return constructResult();
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

		void afterDischargeOrRelabel(int u, boolean relabeled) {
			if (relabeled) {
				// vertex was relabeled and not discharged, still active
				queue.enqueue(u);
			} else {
				// discharged, not active anymore
				isActive.clear(u);
			}
		}

	}
}
