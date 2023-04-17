package com.jgalgo;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;

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
			return new WorkerDouble((DiGraph) g, net, source, target).calcMaxFlow();
		}
	}

	private static class WorkerDouble extends MaxFlowPushRelabelAbstract.WorkerDouble {

		final VertexList list;

		WorkerDouble(DiGraph gOrig, FlowNetwork net, int source, int target) {
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
			list.clear();
			super.recomputeLabels();
			list.listIter = list.listHead != LinkedListDoubleArrayFixedSize.None ? list.vertices.iterator(list.listHead)
					: IntIterators.EMPTY_ITERATOR;
		}

		@Override
		void onVertexLabelReCompute(int u, int newLabel) {
			super.onVertexLabelReCompute(u, newLabel);
			if (list.listHead != LinkedListDoubleArrayFixedSize.None)
				list.vertices.connect(u, list.listHead);
			list.listHead = u;
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
			list.clear();
			super.recomputeLabels();
			list.listIter = list.listHead != LinkedListDoubleArrayFixedSize.None ? list.vertices.iterator(list.listHead)
					: IntIterators.EMPTY_ITERATOR;
		}

		@Override
		void onVertexLabelReCompute(int u, int newLabel) {
			super.onVertexLabelReCompute(u, newLabel);
			if (list.listHead != LinkedListDoubleArrayFixedSize.None)
				list.vertices.connect(u, list.listHead);
			list.listHead = u;
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

		final LinkedListDoubleArrayFixedSize vertices;
		int listHead = LinkedListDoubleArrayFixedSize.None;
		IntIterator listIter;

		VertexList(MaxFlowPushRelabelAbstract.Worker worker) {
			int n = worker.g.vertices().size();
			vertices = LinkedListDoubleArrayFixedSize.newInstance(n);
		}

		void clear() {
			vertices.clear();
			listHead = LinkedListDoubleArrayFixedSize.None;
			listIter = null;
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
