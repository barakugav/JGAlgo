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
import com.jgalgo.internal.data.LinkedListFixedSize;
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * The push-relabel maximum flow algorithm with lowest-first ordering.
 * <p>
 * The push-relabel algorithm maintain a "preflow" and gradually converts it into a maximum flow by moving flow locally
 * between neighboring nodes using <i>push</i> operations under the guidance of an admissible network maintained by
 * <i>relabel</i> operations.
 * <p>
 * Different variants of the push relabel algorithm exists, mostly different in the order the vertices with excess (more
 * in-going than out-going flow) are examined. This implementation order these vertices by lowest-first order, namely it
 * examine vertices with low 'label' first, and achieve a running time of \(O(n^2 m)\) using linear space.
 * <p>
 * Heuristics are crucial for the practical running time of push-relabel algorithm, and this implementation uses the
 * 'global relabeling' and 'gap' heuristics.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Push%E2%80%93relabel_maximum_flow_algorithm">Wikipedia</a>
 * @see    MaximumFlowPushRelabelFifo
 * @see    MaximumFlowPushRelabelToFront
 * @see    MaximumFlowPushRelabelHighestFirst
 * @author Barak Ugav
 */
class MaximumFlowPushRelabelLowestFirst extends MaximumFlowPushRelabelAbstract {

	/**
	 * Create a new maximum flow algorithm object.
	 */
	MaximumFlowPushRelabelLowestFirst() {}

	@Override
	WorkerDouble newWorkerDouble(IndexGraph gOrig, FlowNetwork net, int source, int sink) {
		return new WorkerDouble(gOrig, net, source, sink);
	}

	@Override
	WorkerInt newWorkerInt(IndexGraph gOrig, FlowNetwork.Int net, int source, int sink) {
		return new WorkerInt(gOrig, net, source, sink);
	}

	@Override
	WorkerDouble newWorkerDouble(IndexGraph gOrig, FlowNetwork net, IntCollection sources, IntCollection sinks) {
		return new WorkerDouble(gOrig, net, sources, sinks);
	}

	@Override
	WorkerInt newWorkerInt(IndexGraph gOrig, FlowNetwork.Int net, IntCollection sources, IntCollection sinks) {
		return new WorkerInt(gOrig, net, sources, sinks);
	}

	private static class WorkerDouble extends MaximumFlowPushRelabelAbstract.WorkerDouble {

		int minLayerActive;

		WorkerDouble(IndexGraph gOrig, FlowNetwork net, int source, int sink) {
			super(gOrig, net, source, sink);
		}

		WorkerDouble(IndexGraph gOrig, FlowNetwork net, IntCollection sources, IntCollection sinks) {
			super(gOrig, net, sources, sinks);
		}

		@Override
		void recomputeLabels() {
			minLayerActive = 0;
			super.recomputeLabels();
		}

		@Override
		void activate(int v) {
			super.activate(v);
			if (minLayerActive > label[v])
				minLayerActive = label[v];
		}

		@Override
		boolean hasMoreVerticesToDischarge() {
			for (; minLayerActive < n; minLayerActive++)
				if (layersHeadActive[minLayerActive] != LinkedListFixedSize.None)
					return true;
			return false;
		}

		@Override
		int nextVertexToDischarge() {
			for (; minLayerActive < n; minLayerActive++)
				if (layersHeadActive[minLayerActive] != LinkedListFixedSize.None)
					return layersHeadActive[minLayerActive];
			throw new IllegalStateException();
		}
	}

	private static class WorkerInt extends MaximumFlowPushRelabelAbstract.WorkerInt {

		int minLayerActive;

		WorkerInt(IndexGraph gOrig, FlowNetwork.Int net, int source, int sink) {
			super(gOrig, net, source, sink);
		}

		WorkerInt(IndexGraph gOrig, FlowNetwork.Int net, IntCollection sources, IntCollection sinks) {
			super(gOrig, net, sources, sinks);
		}

		@Override
		void recomputeLabels() {
			minLayerActive = 0;
			super.recomputeLabels();
		}

		@Override
		void activate(int v) {
			super.activate(v);
			if (minLayerActive > label[v])
				minLayerActive = label[v];
		}

		@Override
		boolean hasMoreVerticesToDischarge() {
			for (; minLayerActive < n; minLayerActive++)
				if (layersHeadActive[minLayerActive] != LinkedListFixedSize.None)
					return true;
			return false;
		}

		@Override
		int nextVertexToDischarge() {
			for (; minLayerActive < n; minLayerActive++)
				if (layersHeadActive[minLayerActive] != LinkedListFixedSize.None)
					return layersHeadActive[minLayerActive];
			throw new IllegalStateException();
		}
	}

}
