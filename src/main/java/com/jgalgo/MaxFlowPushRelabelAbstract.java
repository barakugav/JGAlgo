package com.jgalgo;

import java.util.Arrays;
import java.util.BitSet;

import com.jgalgo.MaxFlow.FlowNetwork;
import com.jgalgo.MaxFlow.FlowNetworkInt;
import com.jgalgo.Utils.IterPickable;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

class MaxFlowPushRelabelAbstract {
	private static final Object EdgeRefWeightKey = new Object();
	private static final Object EdgeRevWeightKey = new Object();
	private static final Object FlowWeightKey = new Object();
	private static final Object CapacityWeightKey = new Object();

	static abstract class Worker {
		final DiGraph g;
		final DiGraph gOrig;

		final FlowNetwork net;
		final int source;
		final int target;

		final Weights.Int edgeRef;
		final Weights.Int twin;

		final int[] label;
		final IterPickable.Int[] edgeIters;

		private final LabelsReComputer labelsReComputer;
		private int relabelsSinceLastLabelsRecompute;
		private final int labelsReComputeThreshold;

		Worker(DiGraph gOrig, FlowNetwork net, int source, int target) {
			if (source == target)
				throw new IllegalArgumentException("Source and target can't be the same vertices");
			this.gOrig = gOrig;
			this.net = net;
			this.source = source;
			this.target = target;

			int n = gOrig.vertices().size();
			g = new GraphArrayDirected(n);
			edgeRef = g.addEdgesWeights(EdgeRefWeightKey, int.class, Integer.valueOf(-1));
			twin = g.addEdgesWeights(EdgeRevWeightKey, int.class, Integer.valueOf(-1));
			for (IntIterator it = gOrig.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				int u = gOrig.edgeSource(e), v = gOrig.edgeTarget(e);
				if (u == v)
					continue;
				if (u == target || v == source)
					continue;
				int e1 = g.addEdge(u, v);
				int e2 = g.addEdge(v, u);
				edgeRef.set(e1, e);
				edgeRef.set(e2, e);
				twin.set(e1, e2);
				twin.set(e2, e1);
			}

			label = new int[n];
			edgeIters = new IterPickable.Int[n];

			labelsReComputer = new LabelsReComputer();
			labelsReComputeThreshold = n;
		}

		void init() {
			recomputeLabels();
		}

		void recomputeLabels() {
			labelsReComputer.recompute();
			relabelsSinceLastLabelsRecompute = 0;

			// reset edge iterators
			int n = g.vertices().size();
			for (int u = 0; u < n; u++)
				edgeIters[u] = new IterPickable.Int(g.edgesOut(u));
		}

		abstract void pushAsMuchFromSource();

		// abstract void push(int e, double f);

		void relabel(int v, int newLabel) {
			label[v] = newLabel;
		}

		// Return true if vertex was relabeled, false if discharged
		abstract boolean dischargeOrRelabel(int u);

		abstract double constructResult();

		abstract boolean hasMoreVerticesToDischarge();

		abstract int nextVertexToDischarge();

		double calcMaxFlow() {
			init();
			pushAsMuchFromSource();
			while (hasMoreVerticesToDischarge()) {
				int u = nextVertexToDischarge();
				boolean relabeled = dischargeOrRelabel(u);
				if (relabeled && ++relabelsSinceLastLabelsRecompute >= labelsReComputeThreshold)
					recomputeLabels();
			}
			return constructResult();
		}

		abstract boolean isResidual(int e);

		boolean isOriginalEdge(int e) {
			return g.edgeSource(e) == gOrig.edgeSource(edgeRef.getInt(e));
		}

		private class LabelsReComputer {

			final BitSet visited;
			final IntPriorityQueue queue;

			LabelsReComputer() {
				int n = g.vertices().size();
				visited = new BitSet(n);
				queue = new IntArrayFIFOQueue();
			}

			void recompute() {
				// perform backward BFS from target on edges with flow < capacity
				// perform another one from source to init unreachable vertices

				assert visited.isEmpty();
				assert queue.isEmpty();

				int n = g.vertices().size();
				Arrays.fill(label, 2 * n - 1);

				visited.set(target);
				label[target] = 0;
				visited.set(source);
				label[source] = n;
				for (int start : new int[] { target, source }) {
					queue.enqueue(start);

					while (!queue.isEmpty()) {
						int v = queue.dequeueInt();
						int vLabel = label[v];
						for (EdgeIter eit = g.edgesIn(v); eit.hasNext();) {
							int e = eit.nextInt();
							if (!isResidual(e))
								continue;
							int u = eit.u();
							if (visited.get(u))
								continue;
							label[u] = vLabel + 1;
							visited.set(u);
							queue.enqueue(u);
						}
					}
				}
				visited.clear();
			}
		}
	}

	static abstract class WorkerDouble extends Worker {
		final Weights.Double flow;
		final Weights.Double capacity;

		final double[] excess;

		private static final double EPS = 0.0001;

		WorkerDouble(DiGraph gOrig, FlowNetwork net, int source, int target) {
			super(gOrig, net, source, target);

			int n = gOrig.vertices().size();
			flow = g.addEdgesWeights(FlowWeightKey, double.class);
			capacity = g.addEdgesWeights(CapacityWeightKey, double.class);
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				flow.set(e, 0);
				capacity.set(e, isOriginalEdge(e) ? net.getCapacity(edgeRef.getInt(e)) : 0);
			}

			excess = new double[n];
		}

		@Override
		void pushAsMuchFromSource() {
			for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
				int e = eit.nextInt();
				double f = capacity.getDouble(e) - flow.getDouble(e);
				if (f > 0 && label[source] > label[g.edgeTarget(e)])
					push(e, f);
			}
		}

		void push(int e, double f) {
			assert f > 0;

			int rev = twin.getInt(e);
			flow.set(e, flow.getDouble(e) + f);
			flow.set(rev, flow.getDouble(rev) - f);
			assert flow.getDouble(e) <= capacity.getDouble(e) + EPS;
			assert flow.getDouble(rev) <= capacity.getDouble(rev) + EPS;

			int u = g.edgeSource(e), v = g.edgeTarget(e);
			excess[u] -= f;
			excess[v] += f;
		};

		@Override
		boolean dischargeOrRelabel(int u) {
			if (excess[u] < EPS)
				return false;
			for (IterPickable.Int it = edgeIters[u]; it.hasNext();) {
				int e = it.pickNext();
				double eAccess = capacity.getDouble(e) - flow.getDouble(e);
				if (eAccess > EPS && label[u] == label[g.edgeTarget(e)] + 1) {
					// e is admissible, push
					if (excess[u] > eAccess) {
						// saturating push
						push(e, eAccess);
						// Due to floating points, need to check again we have something to push
						if (excess[u] < EPS)
							return false;
					} else {
						// non-saturating push
						push(e, excess[u]);
						return false;
					}
				} else {
					it.nextInt();
				}
			}

			// Finished iterating over all vertex edges.
			// Reset iterator and relabel
			edgeIters[u] = new IterPickable.Int(g.edgesOut(u));
			assert edgeIters[u].hasNext();
			relabel(u, label[u] + 1);
			return true;
		}

		@Override
		double constructResult() {
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				if (isOriginalEdge(e))
					net.setFlow(edgeRef.getInt(e), flow.getDouble(e));
			}
			double totalFlow = 0;
			for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
				int e = eit.nextInt();
				if (isOriginalEdge(e))
					totalFlow += flow.getDouble(e);
			}
			for (EdgeIter eit = g.edgesIn(source); eit.hasNext();) {
				int e = eit.nextInt();
				if (isOriginalEdge(e))
					totalFlow -= flow.getDouble(e);
			}
			return totalFlow;
		}

		@Override
		boolean isResidual(int e) {
			return capacity.getDouble(e) - flow.getDouble(e) > EPS;
		}
	}

	static abstract class WorkerInt extends Worker {
		final Weights.Int flow;
		final Weights.Int capacity;

		final int[] excess;

		WorkerInt(DiGraph gOrig, FlowNetworkInt net, int source, int target) {
			super(gOrig, net, source, target);

			int n = gOrig.vertices().size();
			flow = g.addEdgesWeights(FlowWeightKey, int.class);
			capacity = g.addEdgesWeights(CapacityWeightKey, int.class);
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				flow.set(e, 0);
				capacity.set(e, isOriginalEdge(e) ? net.getCapacityInt(edgeRef.getInt(e)) : 0);
			}

			excess = new int[n];
		}

		@Override
		void pushAsMuchFromSource() {
			for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
				int e = eit.nextInt();
				int f = capacity.getInt(e) - flow.getInt(e);
				if (f > 0 && label[source] > label[g.edgeTarget(e)])
					push(e, f);
			}
		}

		void push(int e, int f) {
			assert f > 0;

			int rev = twin.getInt(e);
			flow.set(e, flow.getInt(e) + f);
			flow.set(rev, flow.getInt(rev) - f);
			assert flow.getInt(e) <= capacity.getInt(e);
			assert flow.getInt(rev) <= capacity.getInt(rev);

			int u = g.edgeSource(e), v = g.edgeTarget(e);
			excess[u] -= f;
			excess[v] += f;
		};

		@Override
		boolean dischargeOrRelabel(int u) {
			if (excess[u] == 0)
				return false;
			for (IterPickable.Int it = edgeIters[u]; it.hasNext(); it.nextInt()) {
				int e = it.pickNext();
				int eAccess = capacity.getInt(e) - flow.getInt(e);
				if (eAccess > 0 && label[u] == label[g.edgeTarget(e)] + 1) {
					// e is admissible, push
					if (excess[u] > eAccess) {
						// saturating push
						push(e, eAccess);
					} else {
						// non-saturating push
						push(e, excess[u]);
						return false;
					}
				}
			}

			// Finished iterating over all vertex edges.
			// Reset iterator and relabel
			edgeIters[u] = new IterPickable.Int(g.edgesOut(u));
			assert edgeIters[u].hasNext();
			relabel(u, label[u] + 1);
			return true;
		}

		@Override
		double constructResult() {
			FlowNetworkInt net = (FlowNetworkInt) this.net;
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				if (isOriginalEdge(e))
					net.setFlow(edgeRef.getInt(e), flow.getInt(e));
			}
			int totalFlow = 0;
			for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
				int e = eit.nextInt();
				if (isOriginalEdge(e))
					totalFlow += flow.getInt(e);
			}
			for (EdgeIter eit = g.edgesIn(source); eit.hasNext();) {
				int e = eit.nextInt();
				if (isOriginalEdge(e))
					totalFlow -= flow.getInt(e);
			}
			return totalFlow;
		}

		@Override
		boolean isResidual(int e) {
			return capacity.getInt(e) - flow.getInt(e) > 0;
		}
	}

}
