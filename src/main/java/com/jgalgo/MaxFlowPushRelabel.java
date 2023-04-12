package com.jgalgo;

import java.util.Arrays;
import java.util.BitSet;

import com.jgalgo.Utils.IntDoubleConsumer;
import com.jgalgo.Utils.IterPickable;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

public class MaxFlowPushRelabel implements MaxFlow {

	/**
	 * Naive push/relabel implementation.
	 *
	 * O(n^3)
	 */

	private final DebugPrintsManager debug;
	private static final double EPS = 0.0001;
	private static final Object EdgeRefWeightKey = new Object();
	private static final Object EdgeRevWeightKey = new Object();
	private static final Object FlowWeightKey = new Object();
	private static final Object CapacityWeightKey = new Object();

	public MaxFlowPushRelabel() {
		debug = new DebugPrintsManager(false);
	}

	@Override
	public double calcMaxFlow(Graph g, FlowNetwork net, int source, int target) {
		if (!(g instanceof DiGraph))
			throw new IllegalArgumentException("only directed graphs are supported");
		return calcMaxFlow0((DiGraph) g, net, source, target);
	}

	private double calcMaxFlow0(DiGraph g0, FlowNetwork net, int source, int target) {
		if (source == target)
			throw new IllegalArgumentException("Source and target can't be the same vertices");
		debug.println("\t", getClass().getSimpleName());

		DiGraph g = new GraphArrayDirected(g0.vertices().size());
		Weights.Int edgeRef = g.addEdgesWeight(EdgeRefWeightKey).defVal(-1).ofInts();
		Weights.Int edgeRev = g.addEdgesWeight(EdgeRevWeightKey).defVal(-1).ofInts();
		Weights.Double flow = g.addEdgesWeight(FlowWeightKey).ofDoubles();
		Weights.Double capacity = g.addEdgesWeight(CapacityWeightKey).ofDoubles();
		for (IntIterator it = g0.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g0.edgeSource(e), v = g0.edgeTarget(e);
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

		int n = g.vertices().size();

		IterPickable.Int[] edges = new IterPickable.Int[n];
		double[] excess = new double[n];
		BitSet isActive = new BitSet(n);
		IntPriorityQueue active = new IntArrayFIFOQueue();
		int[] d = new int[n];

		IntDoubleConsumer pushFlow = (e, f) -> {
			assert f > 0;

			int rev = edgeRev.getInt(e);
			flow.set(e, flow.getDouble(e) + f);
			flow.set(rev, flow.getDouble(rev) - f);
			assert flow.getDouble(e) <= capacity.getDouble(e) + EPS;
			assert flow.getDouble(rev) <= capacity.getDouble(rev) + EPS;

			int u = g.edgeSource(e), v = g.edgeTarget(e);
			excess[u] -= f;
			excess[v] += f;
			if (!isActive.get(v)) {
				isActive.set(v);
				active.enqueue(v);
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
			int u = active.dequeueInt();
			if (u == source || u == target)
				continue;
			IterPickable.Int it = edges[u];

			while (excess[u] > EPS && it.hasNext()) {
				int e = it.pickNext();
				double eAccess = capacity.getDouble(e) - flow.getDouble(e);
				if (eAccess > EPS && d[u] == d[g.edgeTarget(e)] + 1) {
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
			if (excess[u] > EPS) {
				active.enqueue(u);
			} else {
				isActive.clear(u);
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
			totalFlow += flow.getDouble(e);
		}
		return totalFlow;
	}

}
