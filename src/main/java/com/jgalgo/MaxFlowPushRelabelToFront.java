package com.jgalgo;

import com.jgalgo.Utils.IntDoubleConsumer;
import com.jgalgo.Utils.IterPickable;

import it.unimi.dsi.fastutil.ints.IntIterator;

public class MaxFlowPushRelabelToFront implements MaxFlow {

	/**
	 * O(n^3)
	 */

	private static final double EPS = 0.0001;
	private static final Object EdgeRefWeightKey = new Object();
	private static final Object EdgeRevWeightKey = new Object();
	private static final Object FlowWeightKey = new Object();
	private static final Object CapacityWeightKey = new Object();

	@Override
	public double calcMaxFlow(Graph g, FlowNetwork net, int source, int target) {
		if (!(g instanceof DiGraph))
			throw new IllegalArgumentException("only directed graphs are supported");
		return calcMaxFlow0((DiGraph) g, net, source, target);
	}

	private static double calcMaxFlow0(DiGraph g0, FlowNetwork net, int source, int target) {
		if (source == target)
			throw new IllegalArgumentException("Source and target can't be the same vertices");

		int n = g0.vertices().size();
		DiGraph g = new GraphArrayDirected(n);
		Weights.Int edgeRef = g.addEdgesWeights(EdgeRefWeightKey, int.class, Integer.valueOf(-1));
		Weights.Int twin = g.addEdgesWeights(EdgeRevWeightKey, int.class, Integer.valueOf(-1));
		Weights.Double flow = g.addEdgesWeights(FlowWeightKey, double.class);
		Weights.Double capacity = g.addEdgesWeights(CapacityWeightKey, double.class);
		for (IntIterator it = g0.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g0.edgeSource(e), v = g0.edgeTarget(e);
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

		IterPickable.Int[] edges = new IterPickable.Int[n];
		double[] excess = new double[n];
		int[] d = new int[n];

		LinkedListDoubleArrayFixedSize list = LinkedListDoubleArrayFixedSize.newInstance(n);
		int listHead = -1;
		for (int u = 0, prev = -1; u < n; u++) {
			if (u == source || u == target)
				continue;
			if (prev == -1) {
				listHead = u;
			} else {
				list.setNext(prev, u);
				list.setPrev(u, prev);
			}
			prev = u;
		}

		IntDoubleConsumer pushFlow = (e, f) -> {
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

		/* Push as much as possible from the source vertex */
		for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
			int e = eit.nextInt();
			if (eit.v() == source)
				continue;
			double f = capacity.getDouble(e) - flow.getDouble(e);
			if (f > 0)
				pushFlow.accept(e, f);
		}

		/* Init all vertices distances */
		SSSP.Result initD = new SSSPCardinality().calcDistances(g, target);
		for (int u = 0; u < n; u++)
			if (u != source && u != target)
				d[u] = (int) initD.distance(target);
		d[source] = n;
		d[target] = 0;

		/* Init all vertices iterators */
		for (int u = 0; u < n; u++)
			edges[u] = new IterPickable.Int(g.edgesOut(u));

		for (IntIterator uit = list.iterator(listHead); uit.hasNext();) {
			int u = uit.nextInt();
			IterPickable.Int it = edges[u];

			// discharge
			while (excess[u] > EPS) {
				if (!it.hasNext()) {
					// Finished iterating over all vertex edges

					// relabel
					d[u]++;

					// reset iterator
					it = edges[u] = new IterPickable.Int(g.edgesOut(u));
					assert it.hasNext();

					// move to front
					if (u != listHead) {
						list.disconnect(u);
						list.connect(u, listHead);
						listHead = u;
						uit = list.iterator(listHead);
					}
				}

				int e = it.pickNext();
				double eAccess = capacity.getDouble(e) - flow.getDouble(e);
				if (eAccess > EPS && d[u] == d[g.edgeTarget(e)] + 1) {
					// e is admissible, push
					double f = Math.min(excess[u], eAccess);
					pushFlow.accept(e, f);
				} else {
					it.nextInt();
				}
			}
		}

		/* Construct result */
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g.edgeSource(e);
			int orig = edgeRef.getInt(e);
			if (u == g0.edgeSource(orig))
				net.setFlow(orig, flow.getDouble(e));
		}
		double totalFlow = 0;
		for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
			int e = eit.nextInt();
			if (g.edgeSource(e) == g0.edgeSource(edgeRef.getInt(e)))
				totalFlow += flow.getDouble(e);
		}
		for (EdgeIter eit = g.edgesIn(source); eit.hasNext();) {
			int e = eit.nextInt();
			if (g.edgeSource(e) == g0.edgeSource(edgeRef.getInt(e)))
				totalFlow -= flow.getDouble(e);
		}
		return totalFlow;
	}

}
