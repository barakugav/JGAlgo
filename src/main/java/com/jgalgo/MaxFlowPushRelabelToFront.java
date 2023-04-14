package com.jgalgo;

import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIterator;

public class MaxFlowPushRelabelToFront implements MaxFlow {

	/**
	 * O(n^3)
	 */

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

	private static class Worker extends MaxFlowPushRelabelAbstract.WorkerDouble {

		final VertexList list;

		Worker(DiGraph gOrig, FlowNetwork net, int source, int target) {
			super(gOrig, net, source, target);
			list = new VertexList(this);
		}

		@Override
		void relabel(int v, int newLabel) {
			super.relabel(v, newLabel);
			list.afterRelabel(v);
		}

		@Override
		void recomputeLabels() {
			super.recomputeLabels();
			list.init();
		}

		@Override
		boolean hasMoreVerticesToDischarge() {
			return list.listIter.hasNext();
		}

		@Override
		int nextVertexToDischarge() {
			return list.listIter.nextInt();
		}
	}

	private static class WorkerInt extends MaxFlowPushRelabelAbstract.WorkerInt {

		final VertexList list;

		WorkerInt(DiGraph gOrig, FlowNetworkInt net, int source, int target) {
			super(gOrig, net, source, target);
			list = new VertexList(this);
		}

		@Override
		void relabel(int v, int newLabel) {
			super.relabel(v, newLabel);
			list.afterRelabel(v);
		}

		@Override
		void recomputeLabels() {
			super.recomputeLabels();
			list.init();
		}

		@Override
		boolean hasMoreVerticesToDischarge() {
			return list.listIter.hasNext();
		}

		@Override
		int nextVertexToDischarge() {
			return list.listIter.nextInt();
		}
	}

	private static class VertexList {

		private final MaxFlowPushRelabelAbstract.Worker worker;
		final LinkedListDoubleArrayFixedSize vertices;
		int listHead = LinkedListDoubleArrayFixedSize.None;
		IntIterator listIter;

		VertexList(MaxFlowPushRelabelAbstract.Worker worker) {
			this.worker = worker;
			int n = worker.g.vertices().size();
			vertices = LinkedListDoubleArrayFixedSize.newInstance(n);
		}

		private void init() {
			vertices.clear();
			int[] vs = worker.g.vertices().toIntArray();
			IntArrays.parallelQuickSort(vs, (v1, v2) -> -Integer.compare(worker.label[v1], worker.label[v2]));
			int prev = LinkedListDoubleArrayFixedSize.None;
			for (int u : vs) {
				if (u == worker.source || u == worker.target)
					continue;
				if (prev == LinkedListDoubleArrayFixedSize.None) {
					listHead = u;
				} else {
					vertices.setNext(prev, u);
					vertices.setPrev(u, prev);
				}
				prev = u;
			}
			listIter = vertices.iterator(listHead);
		}

		void afterRelabel(int v) {
			// move to front
			if (v != listHead) {
				vertices.disconnect(v);
				vertices.connect(v, listHead);
				listHead = v;
			}
			listIter = vertices.iterator(listHead);
		}

		@Override
		public String toString() {
			if (listHead == LinkedListDoubleArrayFixedSize.None)
				return "[]";
			StringBuilder s = new StringBuilder().append('[');
			for (IntIterator it = vertices.iterator(listHead);;) {
				assert it.hasNext();
				int v = it.nextInt();
				s.append(v);
				if (!it.hasNext())
					return s.append(']').toString();
				s.append(',').append(' ');
			}
		}

	}
}
