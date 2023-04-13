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
		return new Worker((DiGraph) g, net, source, target).calcMaxFlow();
	}

	private class Worker extends MaxFlowPushRelabelAbstract.Worker {

		final BitSet isActive;
		final IntPriorityQueue active;

		Worker(DiGraph gOrig, FlowNetwork net, int source, int target) {
			super(gOrig, net, source, target);
			int n = g.vertices().size();
			isActive = new BitSet(n);
			active = new IntArrayFIFOQueue();

			// set source and target as 'active' to prevent them from entering the active
			// queue
			isActive.set(source);
			isActive.set(target);
		}

		@Override
		void push(int e, double f) {
			super.push(e, f);
			int v = g.edgeTarget(e);
			if (!isActive.get(v)) {
				isActive.set(v);
				active.enqueue(v);
			}
		};

		@Override
		void discharge(int u) {
			super.discharge(u);
			isActive.clear(u);
		}

		double calcMaxFlow() {
			initLabels();
			pushAsMuchFromSource();
			while (!active.isEmpty()) {
				int u = active.dequeueInt();
				assert u != source && u != target;
				discharge(u);
			}
			return constructResult();
		}

	}
}
