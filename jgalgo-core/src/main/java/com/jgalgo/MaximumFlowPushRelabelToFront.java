package com.jgalgo;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;

/**
 * The push-relabel maximum flow algorithm with relabel-to-front ordering.
 * <p>
 * The push-relabel algorithm maintain a "preflow" and gradually converts it
 * into a maximum flow by moving flow locally between neighboring nodes using
 * <i>push</i> operations under the guidance of an admissible network maintained
 * by <i>relabel</i> operations.
 * <p>
 * Different variants of the push relabel algorithm exists, mostly different in
 * the order the vertices with excess (more in-going than out-going flow) are
 * examined. This implementation order these vertices by maintaining the
 * vertices in a linked list, and moving a vertex to the front of the list each
 * time its relabel. Iterating the list actually traverse the vertices in a
 * topological order with respect to the admissible network. The algorithm runs
 * in {@code O(n}<sup>3</sup> {@code )} time and uses linear space.
 * <p>
 * Heuristics are crucial for the practical running time of push-relabel
 * algorithm, and this implementation uses the 'global relabeling' and 'gap'
 * heuristics.
 *
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/Push%E2%80%93relabel_maximum_flow_algorithm">Wikipedia</a>
 * @see MaximumFlowPushRelabel
 * @see MaximumFlowPushRelabelHighestFirst
 * @see MaximumFlowPushRelabelLowestFirst
 * @author Barak Ugav
 */
public class MaximumFlowPushRelabelToFront implements MaximumFlow {

	/**
	 * Create a new maximum flow algorithm object.
	 */
	public MaximumFlowPushRelabelToFront() {
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

	private static class WorkerDouble extends MaximumFlowPushRelabelAbstract.WorkerDouble {

		final VertexList list;

		WorkerDouble(DiGraph gOrig, FlowNetwork net, int source, int sink) {
			super(gOrig, net, source, sink);
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

	private static class WorkerInt extends MaximumFlowPushRelabelAbstract.WorkerInt {

		final VertexList list;

		WorkerInt(DiGraph gOrig, FlowNetwork.Int net, int source, int sink) {
			super(gOrig, net, source, sink);
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

		VertexList(MaximumFlowPushRelabelAbstract.Worker worker) {
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
