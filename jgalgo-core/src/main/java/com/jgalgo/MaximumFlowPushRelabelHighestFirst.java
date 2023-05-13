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

/**
 * The push-relabel maximum flow algorithm with highest-first ordering.
 * <p>
 * The push-relabel algorithm maintain a "preflow" and gradually converts it into a maximum flow by moving flow locally
 * between neighboring nodes using <i>push</i> operations under the guidance of an admissible network maintained by
 * <i>relabel</i> operations.
 * <p>
 * Different variants of the push relabel algorithm exists, mostly different in the order the vertices with excess (more
 * in-going than out-going flow) are examined. This implementation order these vertices by highest-first order, namely
 * it examine vertices with higher 'label' first, and achieve a running time of \(O(n^2 \sqrt{m})\) using linear space.
 * <p>
 * Heuristics are crucial for the practical running time of push-relabel algorithm, and this implementation uses the
 * 'global relabeling' and 'gap' heuristics.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Push%E2%80%93relabel_maximum_flow_algorithm">Wikipedia</a>
 * @see    MaximumFlowPushRelabel
 * @see    MaximumFlowPushRelabelToFront
 * @see    MaximumFlowPushRelabelLowestFirst
 * @author Barak Ugav
 */
public class MaximumFlowPushRelabelHighestFirst extends MaximumFlowPushRelabelAbstract {

	/**
	 * Create a new maximum flow algorithm object.
	 */
	public MaximumFlowPushRelabelHighestFirst() {}

	@Override
	WorkerDouble newWorkerDouble(Graph gOrig, FlowNetwork net, int source, int sink) {
		return new WorkerDouble(gOrig, net, source, sink);
	}

	@Override
	WorkerInt newWorkerInt(Graph gOrig, FlowNetwork.Int net, int source, int sink) {
		return new WorkerInt(gOrig, net, source, sink);
	}

	private static class WorkerDouble extends MaximumFlowPushRelabelAbstract.WorkerDouble {

		WorkerDouble(Graph gOrig, FlowNetwork net, int source, int sink) {
			super(gOrig, net, source, sink);
		}

		@Override
		boolean hasMoreVerticesToDischarge() {
			for (; maxLayerActive > 0; maxLayerActive--)
				if (layersHeadActive[maxLayerActive] != LinkedListFixedSize.None)
					return true;
			return false;
		}

		@Override
		int nextVertexToDischarge() {
			for (; maxLayerActive > 0; maxLayerActive--)
				if (layersHeadActive[maxLayerActive] != LinkedListFixedSize.None)
					return layersHeadActive[maxLayerActive];
			throw new IllegalStateException();
		}
	}

	private static class WorkerInt extends MaximumFlowPushRelabelAbstract.WorkerInt {

		WorkerInt(Graph gOrig, FlowNetwork.Int net, int source, int sink) {
			super(gOrig, net, source, sink);
		}

		@Override
		boolean hasMoreVerticesToDischarge() {
			for (; maxLayerActive > 0; maxLayerActive--)
				if (layersHeadActive[maxLayerActive] != LinkedListFixedSize.None)
					return true;
			return false;
		}

		@Override
		int nextVertexToDischarge() {
			for (; maxLayerActive > 0; maxLayerActive--)
				if (layersHeadActive[maxLayerActive] != LinkedListFixedSize.None)
					return layersHeadActive[maxLayerActive];
			throw new IllegalStateException();
		}
	}

}
