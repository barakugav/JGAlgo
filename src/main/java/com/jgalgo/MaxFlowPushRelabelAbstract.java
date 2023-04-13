package com.jgalgo;

import com.jgalgo.MaxFlow.FlowNetwork;
import com.jgalgo.Utils.IterPickable;

import it.unimi.dsi.fastutil.ints.IntIterator;

class MaxFlowPushRelabelAbstract {

	private static final double EPS = 0.0001;
	private static final Object EdgeRefWeightKey = new Object();
	private static final Object EdgeRevWeightKey = new Object();
	private static final Object FlowWeightKey = new Object();
	private static final Object CapacityWeightKey = new Object();

	static class Worker {
		final DiGraph g;
		final DiGraph gOrig;

		final FlowNetwork net;
		final int source;
		final int target;

		final Weights.Int edgeRef;
		final Weights.Int twin;
		final Weights.Double flow;
		final Weights.Double capacity;

		final int[] label;
		final double[] excess;
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
			flow = g.addEdgesWeights(FlowWeightKey, double.class);
			capacity = g.addEdgesWeights(CapacityWeightKey, double.class);
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
				flow.set(e1, 0);
				flow.set(e2, 0);
				capacity.set(e1, net.getCapacity(e));
				capacity.set(e2, 0);
			}

			label = new int[n];
			excess = new double[n];
			edgeIters = new IterPickable.Int[n];
			for (int u = 0; u < n; u++)
				edgeIters[u] = new IterPickable.Int(g.edgesOut(u));
		}

		void initLabels() {
			int n = g.vertices().size();
			SSSP.Result initD = new SSSPCardinality().calcDistances(g, target);
			for (int u = 0; u < n; u++)
				if (u != source && u != target)
					label[u] = (int) initD.distance(target);
			label[source] = n;
			label[target] = 0;
		}

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

		void relabel(int v, int newLabel) {
			label[v] = newLabel;
		}

		void discharge(int u) {
			IterPickable.Int it = edgeIters[u];
			while (excess[u] > EPS) {
				if (!it.hasNext()) {
					// Finished iterating over all vertex edges

					// relabel
					relabel(u, label[u] + 1);

					// reset iterator
					it = edgeIters[u] = new IterPickable.Int(g.edgesOut(u));
					assert it.hasNext();
				}

				int e = it.pickNext();
				double eAccess = capacity.getDouble(e) - flow.getDouble(e);
				if (eAccess > EPS && label[u] == label[g.edgeTarget(e)] + 1) {
					// e is admissible, push
					double f = Math.min(excess[u], eAccess);
					push(e, f);
				} else {
					it.nextInt();
				}
			}
		}

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

}
