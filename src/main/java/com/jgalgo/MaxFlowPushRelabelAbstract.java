package com.jgalgo;

import com.jgalgo.MaxFlow.FlowNetwork;
import com.jgalgo.MaxFlow.FlowNetworkInt;
import com.jgalgo.Utils.IterPickable;

import it.unimi.dsi.fastutil.ints.IntIterator;

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
			for (int u = 0; u < n; u++)
				edgeIters[u] = new IterPickable.Int(g.edgesOut(u));
		}

		void initLabels() {
			int n = g.vertices().size();
			SSSP.Result initD = new SSSPCardinality().calcDistances(g, target);
			for (int u = 0; u < n; u++)
				if (u != source && u != target)
					label[u] = (int) initD.distance(u);
			label[source] = n;
			label[target] = 0;
		}

		abstract void pushAsMuchFromSource();

		// abstract void push(int e, double f);

		void relabel(int v, int newLabel) {
			label[v] = newLabel;
		}

		// Return true if vertex was relabeled, false if discharged
		abstract boolean dischargeOrRelabel(int u);

		abstract double constructResult();

	}

	static class WorkerDouble extends Worker {
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

				int u = g.edgeSource(e);
				int orig = edgeRef.getInt(e);
				if (u == gOrig.edgeSource(orig)) {
					capacity.set(e, net.getCapacity(orig));
				} else {
					capacity.set(e, 0);
				}
			}

			excess = new double[n];
		}

		@Override
		void pushAsMuchFromSource() {
			for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
				int e = eit.nextInt();
				double f = capacity.getDouble(e) - flow.getDouble(e);
				if (f > 0)
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
			IterPickable.Int it = edgeIters[u];
			while (excess[u] > EPS) {
				if (!it.hasNext()) {
					// Finished iterating over all vertex edges

					// relabel
					relabel(u, label[u] + 1);

					// reset iterator
					it = edgeIters[u] = new IterPickable.Int(g.edgesOut(u));
					assert it.hasNext();
					return true;
				}

				int e = it.pickNext();
				double eAccess = capacity.getDouble(e) - flow.getDouble(e);
				if (eAccess > EPS && label[u] == label[g.edgeTarget(e)] + 1) {
					// e is admissible, push
					push(e, Math.min(excess[u], eAccess));
				} else {
					it.nextInt();
				}
			}
			return false;
		}

		@Override
		double constructResult() {
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				int u = g.edgeSource(e);
				int orig = edgeRef.getInt(e);
				if (u == gOrig.edgeSource(orig))
					net.setFlow(orig, flow.getDouble(e));
			}
			double totalFlow = 0;
			for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
				int e = eit.nextInt();
				if (g.edgeSource(e) == gOrig.edgeSource(edgeRef.getInt(e)))
					totalFlow += flow.getDouble(e);
			}
			for (EdgeIter eit = g.edgesIn(source); eit.hasNext();) {
				int e = eit.nextInt();
				if (g.edgeSource(e) == gOrig.edgeSource(edgeRef.getInt(e)))
					totalFlow -= flow.getDouble(e);
			}
			return totalFlow;
		}

	}

	static class WorkerInt extends Worker {
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

				int u = g.edgeSource(e);
				int orig = edgeRef.getInt(e);
				if (u == gOrig.edgeSource(orig)) {
					capacity.set(e, net.getCapacityInt(orig));
				} else {
					capacity.set(e, 0);
				}
			}

			excess = new int[n];
		}

		@Override
		void pushAsMuchFromSource() {
			for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
				int e = eit.nextInt();
				int f = capacity.getInt(e) - flow.getInt(e);
				if (f > 0)
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
			IterPickable.Int it = edgeIters[u];
			while (excess[u] > 0) {
				if (!it.hasNext()) {
					// Finished iterating over all vertex edges

					// relabel
					relabel(u, label[u] + 1);

					// reset iterator
					it = edgeIters[u] = new IterPickable.Int(g.edgesOut(u));
					assert it.hasNext();
					return true;
				}

				int e = it.pickNext();
				int eAccess = capacity.getInt(e) - flow.getInt(e);
				if (eAccess > 0 && label[u] == label[g.edgeTarget(e)] + 1) {
					// e is admissible, push
					push(e, Math.min(excess[u], eAccess));
				} else {
					it.nextInt();
				}
			}
			return false;
		}

		@Override
		double constructResult() {
			FlowNetworkInt net = (FlowNetworkInt) this.net;
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				int u = g.edgeSource(e);
				int orig = edgeRef.getInt(e);
				if (u == gOrig.edgeSource(orig))
					net.setFlow(orig, flow.getInt(e));
			}
			int totalFlow = 0;
			for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
				int e = eit.nextInt();
				if (g.edgeSource(e) == gOrig.edgeSource(edgeRef.getInt(e)))
					totalFlow += flow.getInt(e);
			}
			for (EdgeIter eit = g.edgesIn(source); eit.hasNext();) {
				int e = eit.nextInt();
				if (g.edgeSource(e) == gOrig.edgeSource(edgeRef.getInt(e)))
					totalFlow -= flow.getInt(e);
			}
			return totalFlow;
		}

	}

}
