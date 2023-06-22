/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jgalgo;

import com.jgalgo.graph.IndexGraph;
import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * The push-relabel maximum flow algorithm with relabel-to-front ordering.
 * <p>
 * The push-relabel algorithm maintain a "preflow" and gradually converts it into a maximum flow by moving flow locally
 * between neighboring nodes using <i>push</i> operations under the guidance of an admissible network maintained by
 * <i>relabel</i> operations.
 * <p>
 * Different variants of the push relabel algorithm exists, mostly different in the order the vertices with excess (more
 * in-going than out-going flow) are examined. This implementation order these vertices by maintaining the vertices in a
 * linked list, and moving a vertex to the front of the list each time its relabel. Iterating the list actually traverse
 * the vertices in a topological order with respect to the admissible network. The algorithm runs in \(O(n^3)\) time and
 * uses linear space.
 * <p>
 * Heuristics are crucial for the practical running time of push-relabel algorithm, and this implementation uses the
 * 'global relabeling' and 'gap' heuristics.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Push%E2%80%93relabel_maximum_flow_algorithm">Wikipedia</a>
 * @see    MaximumFlowPushRelabelFifo
 * @see    MaximumFlowPushRelabelHighestFirst
 * @see    MaximumFlowPushRelabelLowestFirst
 * @author Barak Ugav
 */
class MaximumFlowPushRelabelToFront extends MaximumFlowPushRelabelAbstract {

	/**
	 * Create a new maximum flow algorithm object.
	 */
	MaximumFlowPushRelabelToFront() {}

	@Override
	WorkerDouble newWorkerDouble(IndexGraph gOrig, FlowNetwork net, int source, int sink) {
		return new WorkerDouble(gOrig, net, source, sink);
	}

	@Override
	WorkerInt newWorkerInt(IndexGraph gOrig, FlowNetwork.Int net, int source, int sink) {
		return new WorkerInt(gOrig, net, source, sink);
	}

	private static class WorkerDouble extends MaximumFlowPushRelabelAbstract.WorkerDouble {

		final VertexList list;

		WorkerDouble(IndexGraph gOrig, FlowNetwork net, int source, int sink) {
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
			list.listIter = list.listHead != LinkedListFixedSize.None ? list.vertices.iterator(list.listHead)
					: Utils.IterPeekable.Int.Empty;
		}

		@Override
		void onVertexLabelReCompute(int u, int newLabel) {
			super.onVertexLabelReCompute(u, newLabel);
			if (list.listHead != LinkedListFixedSize.None)
				list.vertices.connect(u, list.listHead);
			list.listHead = u;
		}

		@Override
		boolean hasMoreVerticesToDischarge() {
			for (; list.listIter.hasNext(); list.listIter.nextInt())
				if (hasExcess(list.listIter.peekNext()))
					return true;
			return false;
		}

		@Override
		int nextVertexToDischarge() {
			int v;
			while (list.listIter.hasNext())
				if (hasExcess(v = list.listIter.nextInt()))
					return v;
			throw new IllegalStateException();
		}
	}

	private static class WorkerInt extends MaximumFlowPushRelabelAbstract.WorkerInt {

		final VertexList list;

		WorkerInt(IndexGraph gOrig, FlowNetwork.Int net, int source, int sink) {
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
			list.listIter = list.listHead != LinkedListFixedSize.None ? list.vertices.iterator(list.listHead)
					: Utils.IterPeekable.Int.Empty;
		}

		@Override
		void onVertexLabelReCompute(int u, int newLabel) {
			super.onVertexLabelReCompute(u, newLabel);
			if (list.listHead != LinkedListFixedSize.None)
				list.vertices.connect(u, list.listHead);
			list.listHead = u;
		}

		@Override
		boolean hasMoreVerticesToDischarge() {
			for (; list.listIter.hasNext(); list.listIter.nextInt())
				if (hasExcess(list.listIter.peekNext()))
					return true;
			return false;
		}

		@Override
		int nextVertexToDischarge() {
			int v;
			while (list.listIter.hasNext())
				if (hasExcess(v = list.listIter.nextInt()))
					return v;
			throw new IllegalStateException();
		}
	}

	private static class VertexList {

		final LinkedListFixedSize.Doubly vertices;
		int listHead = LinkedListFixedSize.None;
		Utils.IterPeekable.Int listIter;

		VertexList(MaximumFlowPushRelabelAbstract.Worker worker) {
			int n = worker.g.vertices().size();
			vertices = new LinkedListFixedSize.Doubly(n);
		}

		void clear() {
			vertices.clear();
			listHead = LinkedListFixedSize.None;
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
			if (listHead == LinkedListFixedSize.None)
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
