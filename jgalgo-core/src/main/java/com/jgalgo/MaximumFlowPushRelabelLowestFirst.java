package com.jgalgo;

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
 * @see    MaximumFlowPushRelabel
 * @see    MaximumFlowPushRelabelToFront
 * @see    MaximumFlowPushRelabelHighestFirst
 * @author Barak Ugav
 */
public class MaximumFlowPushRelabelLowestFirst extends MaximumFlowPushRelabelAbstract {

	/**
	 * Create a new maximum flow algorithm object.
	 */
	public MaximumFlowPushRelabelLowestFirst() {}

	@Override
	WorkerDouble newWorkerDouble(DiGraph gOrig, FlowNetwork net, int source, int sink) {
		return new WorkerDouble(gOrig, net, source, sink);
	}

	@Override
	WorkerInt newWorkerInt(DiGraph gOrig, FlowNetwork.Int net, int source, int sink) {
		return new WorkerInt(gOrig, net, source, sink);
	}

	private static class WorkerDouble extends MaximumFlowPushRelabelAbstract.WorkerDouble {

		int minLayerActive;

		WorkerDouble(DiGraph gOrig, FlowNetwork net, int source, int sink) {
			super(gOrig, net, source, sink);
		}

		@Override
		void recomputeLabels() {
			minLayerActive = 0;
			super.recomputeLabels();
		}

		@Override
		void push(int e, double f) {
			int v = g.edgeTarget(e);
			if (v != sink && !hasExcess(v))
				if (minLayerActive > label[v])
					minLayerActive = label[v];
			super.push(e, f);
		};

		@Override
		boolean hasMoreVerticesToDischarge() {
			for (; minLayerActive < n; minLayerActive++)
				if (layersHeadActive[minLayerActive] != LinkedListDoubleArrayFixedSize.None)
					return true;
			return false;
		}

		@Override
		int nextVertexToDischarge() {
			for (; minLayerActive < n; minLayerActive++)
				if (layersHeadActive[minLayerActive] != LinkedListDoubleArrayFixedSize.None)
					return layersHeadActive[minLayerActive];
			throw new IllegalStateException();
		}
	}

	private static class WorkerInt extends MaximumFlowPushRelabelAbstract.WorkerInt {

		int minLayerActive;

		WorkerInt(DiGraph gOrig, FlowNetwork.Int net, int source, int sink) {
			super(gOrig, net, source, sink);
		}

		@Override
		void recomputeLabels() {
			minLayerActive = 0;
			super.recomputeLabels();
		}

		@Override
		void push(int e, int f) {
			int v = g.edgeTarget(e);
			if (v != sink && !hasExcess(v))
				if (minLayerActive > label[v])
					minLayerActive = label[v];
			super.push(e, f);
		};

		@Override
		boolean hasMoreVerticesToDischarge() {
			for (; minLayerActive < n; minLayerActive++)
				if (layersHeadActive[minLayerActive] != LinkedListDoubleArrayFixedSize.None)
					return true;
			return false;
		}

		@Override
		int nextVertexToDischarge() {
			for (; minLayerActive < n; minLayerActive++)
				if (layersHeadActive[minLayerActive] != LinkedListDoubleArrayFixedSize.None)
					return layersHeadActive[minLayerActive];
			throw new IllegalStateException();
		}
	}

}
