package com.jgalgo;

import java.util.BitSet;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

/**
 * The Edmonds-Karp algorithm for maximum flow.
 * <p>
 * The most known implementation that solve the maximum flow problem. It does so
 * by finding augmenting paths from the source to the sink in the residual
 * network, and saturating at least one edge in each path. This is a
 * specification Ford–Fulkerson method, which chooses the shortest augmenting
 * path in each iteration. It runs in \(O(m^2 n)\) time and linear space.
 * <p>
 * Based on the paper 'Theoretical improvements in algorithmic efficiency for
 * network flow problems' by Jack Edmonds and Richard M Karp.
 *
 * @see <a href=
 *      "https://en.wikipedia.org/wiki/Edmonds%E2%80%93Karp_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
public class MaximumFlowEdmondsKarp implements MaximumFlow {

	private static final Object EdgeRefWeightKey = new Object();
	private static final Object EdgeRevWeightKey = new Object();
	private static final Object FlowWeightKey = new Object();
	private static final Object CapacityWeightKey = new Object();

	/**
	 * Create a new maximum flow algorithm object.
	 */
	public MaximumFlowEdmondsKarp() {
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not directed
	 */
	@Override
	public double computeMaximumFlow(Graph g, FlowNetwork net, int source, int sink) {
		if (!(g instanceof DiGraph))
			throw new IllegalArgumentException("only directed graphs are supported");
		if (net instanceof FlowNetwork.Int) {
			return new WorkerInt((DiGraph) g, (FlowNetwork.Int) net, source, sink).computeMaxFlow();
		} else {
			return new WorkerDouble((DiGraph) g, net, source, sink).computeMaxFlow();
		}
	}

	private static class WorkerDouble {

		final DiGraph gOring;
		final FlowNetwork net;
		final int source;
		final int sink;

		WorkerDouble(DiGraph gOring, FlowNetwork net, int source, int sink) {
			this.gOring = gOring;
			this.net = net;
			this.source = source;
			this.sink = sink;
		}

		double computeMaxFlow() {
			if (source == sink)
				throw new IllegalArgumentException("Source and sink can't be the same vertex");

			int n = gOring.vertices().size();
			DiGraph g = new GraphArrayDirected(n);
			Weights.Int edgeRef = g.addEdgesWeights(EdgeRefWeightKey, int.class, Integer.valueOf(-1));
			Weights.Int twin = g.addEdgesWeights(EdgeRevWeightKey, int.class, Integer.valueOf(-1));
			Weights.Double flow = g.addEdgesWeights(FlowWeightKey, double.class);
			Weights.Double capacity = g.addEdgesWeights(CapacityWeightKey, double.class);
			for (IntIterator it = gOring.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				int u = gOring.edgeSource(e), v = gOring.edgeTarget(e);
				if (u == v)
					continue;
				if (u == sink || v == source)
					continue;
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
				backtrack[sink] = -1;

				// perform BFS and find a path of non saturated edges from source to sink
				queue.enqueue(source);
				bfs: while (!queue.isEmpty()) {
					int u = queue.dequeueInt();
					for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
						int e = eit.nextInt();
						int v = eit.v();

						if (visited.get(v) || flow.getDouble(e) >= capacity.getDouble(e))
							continue;
						backtrack[v] = e;
						if (v == sink)
							break bfs;
						visited.set(v);
						queue.enqueue(v);
					}
				}

				// no path to sink
				if (backtrack[sink] == -1)
					break;

				// find out what is the maximum flow we can pass
				double f = Double.MAX_VALUE;
				for (int p = sink; p != source;) {
					int e = backtrack[p];
					f = Math.min(f, capacity.getDouble(e) - flow.getDouble(e));
					p = g.edgeSource(e);
				}

				// update flow of all edges on path
				for (int p = sink; p != source;) {
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
				if (u == gOring.edgeSource(orig))
					net.setFlow(orig, flow.getDouble(e));
			}
			double totalFlow = 0;
			for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
				int e = eit.nextInt();
				int orig = edgeRef.getInt(e);
				if (g.edgeSource(e) == gOring.edgeSource(orig))
					totalFlow += flow.getDouble(e);
			}
			for (EdgeIter eit = g.edgesIn(source); eit.hasNext();) {
				int e = eit.nextInt();
				int orig = edgeRef.getInt(e);
				if (g.edgeSource(e) == gOring.edgeSource(orig))
					totalFlow -= flow.getDouble(e);
			}
			return totalFlow;
		}
	}

	private static class WorkerInt {

		final DiGraph gOring;
		final FlowNetwork.Int net;
		final int source;
		final int sink;

		WorkerInt(DiGraph gOring, FlowNetwork.Int net, int source, int sink) {
			this.gOring = gOring;
			this.net = net;
			this.source = source;
			this.sink = sink;
		}

		double computeMaxFlow() {
			if (source == sink)
				throw new IllegalArgumentException("Source and sink can't be the same vertex");

			int n = gOring.vertices().size();
			DiGraph g = new GraphArrayDirected(n);
			Weights.Int edgeRef = g.addEdgesWeights(EdgeRefWeightKey, int.class, Integer.valueOf(-1));
			Weights.Int twin = g.addEdgesWeights(EdgeRevWeightKey, int.class, Integer.valueOf(-1));
			Weights.Int flow = g.addEdgesWeights(FlowWeightKey, int.class);
			Weights.Int capacity = g.addEdgesWeights(CapacityWeightKey, int.class);
			for (IntIterator it = gOring.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				int u = gOring.edgeSource(e), v = gOring.edgeTarget(e);
				if (u == v)
					continue;
				if (u == sink || v == source)
					continue;
				int e1 = g.addEdge(u, v);
				int e2 = g.addEdge(v, u);
				edgeRef.set(e1, e);
				edgeRef.set(e2, e);
				twin.set(e1, e2);
				twin.set(e2, e1);
				int cap = net.getCapacityInt(e);
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
				backtrack[sink] = -1;

				// perform BFS and find a path of non saturated edges from source to sink
				queue.enqueue(source);
				bfs: while (!queue.isEmpty()) {
					int u = queue.dequeueInt();
					for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
						int e = eit.nextInt();
						int v = eit.v();

						if (visited.get(v) || flow.getInt(e) >= capacity.getInt(e))
							continue;
						backtrack[v] = e;
						if (v == sink)
							break bfs;
						visited.set(v);
						queue.enqueue(v);
					}
				}

				// no path to sink
				if (backtrack[sink] == -1)
					break;

				// find out what is the maximum flow we can pass
				int f = Integer.MAX_VALUE;
				for (int p = sink; p != source;) {
					int e = backtrack[p];
					f = Math.min(f, capacity.getInt(e) - flow.getInt(e));
					p = g.edgeSource(e);
				}

				// update flow of all edges on path
				for (int p = sink; p != source;) {
					int e = backtrack[p], rev = twin.getInt(e);
					flow.set(e, Math.min(capacity.getInt(e), flow.getInt(e) + f));
					flow.set(rev, Math.max(0, flow.getInt(rev) - f));
					p = g.edgeSource(e);
				}
			}

			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				int u = g.edgeSource(e);
				int orig = edgeRef.getInt(e);
				if (u == gOring.edgeSource(orig))
					net.setFlow(orig, flow.getInt(e));
			}
			int totalFlow = 0;
			for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
				int e = eit.nextInt();
				int orig = edgeRef.getInt(e);
				if (g.edgeSource(e) == gOring.edgeSource(orig))
					totalFlow += flow.getInt(e);
			}
			for (EdgeIter eit = g.edgesIn(source); eit.hasNext();) {
				int e = eit.nextInt();
				int orig = edgeRef.getInt(e);
				if (g.edgeSource(e) == gOring.edgeSource(orig))
					totalFlow -= flow.getInt(e);
			}
			return totalFlow;
		}
	}

}
