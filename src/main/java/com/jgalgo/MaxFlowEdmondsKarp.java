package com.jgalgo;

import java.util.Arrays;

import com.jgalgo.Utils.QueueIntFixSize;

import it.unimi.dsi.fastutil.ints.Int2DoubleFunction;
import it.unimi.dsi.fastutil.ints.IntIterator;

public class MaxFlowEdmondsKarp implements MaxFlow {

	/*
	 * O(m n^2)
	 */

	private static final Object EdgeRefWeightKey = new Object();
	private static final Object EdgeRevWeightKey = new Object();
	private static final Object FlowWeightKey = new Object();

	public MaxFlowEdmondsKarp() {
	}

	@Override
	public double calcMaxFlow(Graph g0, FlowNetwork net, int source, int target) {
		if (!(g0 instanceof DiGraph))
			throw new IllegalArgumentException("only directed graphs are supported");
		if (source == target)
			throw new IllegalArgumentException("Source and target can't be the same vertices");

		DiGraph g = new GraphArrayDirected(g0.vertices().size());

		Weights.Int edgeRef = g.addEdgesWeight(EdgeRefWeightKey).defVal(-1).ofInts();
		Weights.Int edgeRev = g.addEdgesWeight(EdgeRevWeightKey).defVal(-1).ofInts();
		Weights.Double flow = g.addEdgesWeight(FlowWeightKey).ofDoubles();
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
			flow.set(e2, net.getCapacity(e));
		}
		Int2DoubleFunction capacity = e -> net.getCapacity(edgeRef.getInt(e));

		int n = g.vertices().size();
		int[] backtrack = new int[n];

		boolean[] visited = new boolean[n];
		QueueIntFixSize queue = new QueueIntFixSize(n);

		for (;;) {
			queue.clear();
			visited[source] = true;
			backtrack[target] = -1;
			queue.push(source);

			// perform BFS and find a path of non saturated edges from source to target
			bfs: while (!queue.isEmpty()) {
				int u = queue.pop();
				for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.v();

					if (visited[v] || flow.getDouble(e) >= capacity.applyAsDouble(e))
						continue;
					backtrack[v] = e;
					if (v == target)
						break bfs;
					visited[v] = true;
					queue.push(v);
				}
			}

			// no path to target
			if (backtrack[target] == -1)
				break;

			// find out what is the maximum flow we can pass
			double f = Double.MAX_VALUE;
			for (int p = target; p != source;) {
				int e = backtrack[p];
				f = Math.min(f, capacity.applyAsDouble(e) - flow.getDouble(e));
				p = g.edgeSource(e);
			}

			// update flow of all edges on path
			for (int p = target; p != source;) {
				int e = backtrack[p], rev = edgeRev.getInt(e);
				flow.set(e, Math.min(capacity.applyAsDouble(e), flow.getDouble(e) + f));
				flow.set(rev, Math.max(0, flow.getDouble(rev) - f));
				p = g.edgeSource(e);
			}

			Arrays.fill(visited, false);
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
			if (source == g0.edgeSource(orig))
				totalFlow += flow.getDouble(e);
		}
		return totalFlow;
	}

}