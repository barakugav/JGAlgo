package com.ugav.algo;

import java.util.Arrays;

import com.ugav.algo.Graph.EdgeIter;
import com.ugav.algo.Utils.QueueIntFixSize;

import it.unimi.dsi.fastutil.ints.Int2DoubleFunction;

public class MaxFlowEdmondsKarp implements MaxFlow {

	/*
	 * O(m n^2)
	 */

	public MaxFlowEdmondsKarp() {
	}

	@Override
	public double calcMaxFlow(Graph g0, FlowNetwork net, int source, int target) {
		if (!(g0 instanceof Graph.Directed))
			throw new IllegalArgumentException("only directed graphs are supported");
		if (source == target)
			throw new IllegalArgumentException("Source and target can't be the same vertices");

		Graph.Directed g = new GraphArrayDirected(g0.vertices());
		EdgeData.Int edgeRef = g.newEdgeDataInt("edgeRef");
		EdgeData.Int edgeRev = g.newEdgeDataInt("edgeRev");
		EdgeData.Double flow = g.newEdgeDataDouble("flow");
		for (int e = 0; e < g0.edges(); e++) {
			int u = g0.getEdgeSource(e), v = g0.getEdgeTarget(e);
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

		int n = g.vertices();
		int[] backtrack = new int[n]; // TODO

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
				p = g.getEdgeSource(e);
			}

			// update flow of all edges on path
			for (int p = target; p != source;) {
				int e = backtrack[p], rev = edgeRev.getInt(e);
				flow.set(e, Math.min(capacity.applyAsDouble(e), flow.getDouble(e) + f));
				flow.set(rev, Math.max(0, flow.getDouble(rev) - f));
				p = g.getEdgeSource(e);
			}

			Arrays.fill(visited, false);
		}

		for (int e = 0; e < g.edges(); e++) {
			int u = g.getEdgeSource(e);
			int orig = edgeRef.getInt(e);
			if (u == g0.getEdgeSource(orig))
				net.setFlow(orig, flow.getDouble(e));
		}
		double totalFlow = 0;
		for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
			int e = eit.nextInt();
			int orig = edgeRef.getInt(e);
			if (source == g0.getEdgeSource(orig))
				totalFlow += flow.getDouble(e);
		}
		return totalFlow;
	}

}
