package com.ugav.algo;

import java.util.Arrays;

import com.ugav.algo.Graph.EdgeIter;
import com.ugav.algo.Utils.IntDoubleConsumer;
import com.ugav.algo.Utils.IterPickable;
import com.ugav.algo.Utils.QueueIntFixSize;

public class MaxFlowPushRelabel implements MaxFlow {

	/**
	 * Naive push/relabel implementation.
	 *
	 * O(n^3)
	 */

	private final DebugPrintsManager debug;
	private static final double EPS = 0.0001;

	public MaxFlowPushRelabel() {
		debug = new DebugPrintsManager(false);
	}

	@Override
	public double calcMaxFlow(Graph g0, FlowNetwork net, int source, int target) {
		if (!(g0 instanceof Graph.Directed))
			throw new IllegalArgumentException("only directed graphs are supported");
		if (source == target)
			throw new IllegalArgumentException("Source and target can't be the same vertices");
		debug.println("\t", getClass().getSimpleName());

		Graph.Directed g = new GraphArrayDirected(g0.vertices());
		EdgeData.Int edgeRef = g.newEdgeDataInt("edgeRef");
		EdgeData.Int edgeRev = g.newEdgeDataInt("edgeRev");
		EdgeData.Double flow = g.newEdgeDataDouble("flow");
		EdgeData.Double capacity = g.newEdgeDataDouble("capacity");
		for (int e = 0; e < g0.edges(); e++) {
			int u = g.getEdgeSource(e), v = g.getEdgeTarget(e);
			int e1 = g.addEdge(u, v);
			int e2 = g.addEdge(v, u);
			edgeRef.set(e1, e);
			edgeRef.set(e2, e);
			edgeRev.set(e1, e2);
			edgeRev.set(e2, e1);
			flow.set(e1, 0);
			flow.set(e2, 0);
			capacity.set(e1, net.getCapacity(e));
			capacity.set(e2, 0);
		}

		int n = g.vertices();

		IterPickable.Int[] edges = new IterPickable.Int[n];
		double[] excess = new double[n];
		boolean[] isActive = new boolean[n];
		QueueIntFixSize active = new QueueIntFixSize(n);
		int[] d = new int[n];

		IntDoubleConsumer pushFlow = (e0, f) -> {
			assert f > 0;

			int e = edgeRef.getInt(e0);
			int u = g.getEdgeSource(e0), v = g.getEdgeTarget(e0);
			if (u == g0.getEdgeSource(e))
				; // debug.println("F(", e.orig, ") += ", Double.valueOf(f));

			int rev = edgeRev.getInt(e);
			flow.set(e, flow.getDouble(e) + f);
			flow.set(rev, flow.getDouble(rev) - f);
			assert flow.getDouble(e) <= capacity.getDouble(e) + EPS;
			assert flow.getDouble(rev) <= capacity.getDouble(rev) + EPS;

			excess[u] -= f;
			excess[v] += f;
			if (!isActive[v]) {
				isActive[v] = true;
				active.push(v);
			}
		};

		/* Push as much as possible from the source vertex */
		for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
			int e = eit.nextInt();
			double f = capacity.getDouble(e) - flow.getDouble(e);
			if (f != 0)
				pushFlow.accept(e, f);
		}

		/* Init all vertices distances */
		Arrays.fill(d, 0);
		d[source] = n;

		/* Init all vertices iterators */
		for (int u = 0; u < n; u++)
			edges[u] = new IterPickable.Int(g.edgesOut(u));

		while (!active.isEmpty()) {
			int u = active.pop();
			if (u == source || u == target)
				continue;
			IterPickable.Int it = edges[u];

			while (excess[u] > EPS && it.hasNext()) {
				int e = it.pickNext();
				double eAccess = capacity.getDouble(e) - flow.getDouble(e);
				if (eAccess > EPS && d[u] == d[g.getEdgeTarget(e)] + 1) {
					double f = Math.min(excess[u], eAccess);
					pushFlow.accept(e, f);
				} else {
					it.nextInt();
				}
			}

			/* Finished iterating over all vertex edges, relabel and reset iterator */
			if (!it.hasNext()) {
				d[u]++;
				debug.println("R(", Integer.valueOf(u), ") <- ", Integer.valueOf(d[u]));
				edges[u] = new IterPickable.Int(g.edgesOut(u));
			}

			/* Update isActive and add to queue if active */
			if (isActive[u] = excess[u] > EPS)
				active.push(u);
		}

		/* Construct result */
		for (int e = 0; e < g.edges(); e++) {
			int u = g.getEdgeSource(e);
			int orig = edgeRef.getInt(e);
			if (u == g0.getEdgeSource(orig))
				net.setFlow(orig, flow.getDouble(e));
		}
		double totalFlow = 0;
		for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
			int e = eit.nextInt();
			totalFlow += flow.getDouble(e);
		}
		return totalFlow;
	}

}
