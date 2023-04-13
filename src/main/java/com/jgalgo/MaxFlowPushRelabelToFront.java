package com.jgalgo;

import it.unimi.dsi.fastutil.ints.IntIterator;

public class MaxFlowPushRelabelToFront implements MaxFlow {

	/**
	 * O(n^3)
	 */

	@Override
	public double calcMaxFlow(Graph g, FlowNetwork net, int source, int target) {
		if (!(g instanceof DiGraph))
			throw new IllegalArgumentException("only directed graphs are supported");
		return new Worker((DiGraph) g, net, source, target).calcMaxFlow();
	}

	private static class Worker extends MaxFlowPushRelabelAbstract.Worker {

		final LinkedListDoubleArrayFixedSize list;
		int listHead = -1;
		IntIterator listIter;

		Worker(DiGraph gOrig, FlowNetwork net, int source, int target) {
			super(gOrig, net, source, target);
			int n = g.vertices().size();
			list = LinkedListDoubleArrayFixedSize.newInstance(n);
		}

		@Override
		void relabel(int v, int newLabel) {
			super.relabel(v, newLabel);

			// move to front
			if (v != listHead) {
				list.disconnect(v);
				list.connect(v, listHead);
				listHead = v;
				listIter = list.iterator(listHead);
			}
		}

		private void initList() {
			int n = g.vertices().size();
			for (int u = 0, prev = -1; u < n; u++) {
				if (u == source || u == target)
					continue;
				if (prev == -1) {
					listHead = u;
				} else {
					list.setNext(prev, u);
					list.setPrev(u, prev);
				}
				prev = u;
			}
		}

		double calcMaxFlow() {
			initLabels();
			initList();
			pushAsMuchFromSource();
			for (listIter = list.iterator(listHead); listIter.hasNext();) {
				int u = listIter.nextInt();
				discharge(u);
			}
			return constructResult();
		}
	}
}
