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
	WorkerDouble newWorkerDouble(DiGraph gOrig, FlowNetwork net, int source, int sink) {
		return new WorkerDouble(gOrig, net, source, sink);
	}

	@Override
	WorkerInt newWorkerInt(DiGraph gOrig, FlowNetwork.Int net, int source, int sink) {
		return new WorkerInt(gOrig, net, source, sink);
	}

	private static class WorkerDouble extends MaximumFlowPushRelabelAbstract.WorkerDouble {

		WorkerDouble(DiGraph gOrig, FlowNetwork net, int source, int sink) {
			super(gOrig, net, source, sink);
		}

		@Override
		boolean hasMoreVerticesToDischarge() {
			for (; maxLayerActive > 0; maxLayerActive--)
				if (layersHeadActive[maxLayerActive] != LinkedListDoubleArrayFixedSize.None)
					return true;
			return false;
		}

		@Override
		int nextVertexToDischarge() {
			for (; maxLayerActive > 0; maxLayerActive--)
				if (layersHeadActive[maxLayerActive] != LinkedListDoubleArrayFixedSize.None)
					return layersHeadActive[maxLayerActive];
			throw new IllegalStateException();
		}
	}

	private static class WorkerInt extends MaximumFlowPushRelabelAbstract.WorkerInt {

		WorkerInt(DiGraph gOrig, FlowNetwork.Int net, int source, int sink) {
			super(gOrig, net, source, sink);
		}

		@Override
		boolean hasMoreVerticesToDischarge() {
			for (; maxLayerActive > 0; maxLayerActive--)
				if (layersHeadActive[maxLayerActive] != LinkedListDoubleArrayFixedSize.None)
					return true;
			return false;
		}

		@Override
		int nextVertexToDischarge() {
			for (; maxLayerActive > 0; maxLayerActive--)
				if (layersHeadActive[maxLayerActive] != LinkedListDoubleArrayFixedSize.None)
					return layersHeadActive[maxLayerActive];
			throw new IllegalStateException();
		}
	}

}
