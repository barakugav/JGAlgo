package com.jgalgo;

import java.util.BitSet;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

public class MaxFlowEdmondsKarp implements MaxFlow {

	/*
	 * O(m^2 n)
	 */

	private static final Object EdgeRefWeightKey = new Object();
	private static final Object EdgeRevWeightKey = new Object();
	private static final Object FlowWeightKey = new Object();
	private static final Object CapacityWeightKey = new Object();

	public MaxFlowEdmondsKarp() {
	}

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
			double cap = net.getCapacity(e);
			capacity.set(e1, cap);
			capacity.set(e2, cap);
			flow.set(e1, 0);
			flow.set(e2, cap);
		}

		int[] backtrack = new int[n];
		BitSet visited = new BitSet(n);
		IntPriorityQueue queue = new IntArrayFIFOQueue();

		for (;;) {
			queue.clear();
			visited.clear();
			visited.set(source);
			backtrack[target] = -1;

			// perform BFS and find a path of non saturated edges from source to target
			queue.enqueue(source);
			bfs: while (!queue.isEmpty()) {
				int u = queue.dequeueInt();
				for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.v();

					if (visited.get(v) || flow.getDouble(e) >= capacity.getDouble(e))
						continue;
					backtrack[v] = e;
					if (v == target)
						break bfs;
					visited.set(v);
					queue.enqueue(v);
				}
			}

			// no path to target
			if (backtrack[target] == -1)
				break;

			// find out what is the maximum flow we can pass
			double f = Double.MAX_VALUE;
			for (int p = target; p != source;) {
				int e = backtrack[p];
				f = Math.min(f, capacity.getDouble(e) - flow.getDouble(e));
				p = g.edgeSource(e);
			}

			// update flow of all edges on path
			for (int p = target; p != source;) {
				int e = backtrack[p], rev = twin.getInt(e);
				flow.set(e, Math.min(capacity.getDouble(e), flow.getDouble(e) + f));
				flow.set(rev, Math.max(0, flow.getDouble(rev) - f));
				p = g.edgeSource(e);
			}
		}

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
			int orig = edgeRef.getInt(e);
			if (g.edgeSource(e) == g0.edgeSource(orig))
				totalFlow += flow.getDouble(e);
		}
		for (EdgeIter eit = g.edgesIn(source); eit.hasNext();) {
			int e = eit.nextInt();
			int orig = edgeRef.getInt(e);
			if (g.edgeSource(e) == g0.edgeSource(orig))
				totalFlow -= flow.getDouble(e);
		}
		return totalFlow;
	}

}
