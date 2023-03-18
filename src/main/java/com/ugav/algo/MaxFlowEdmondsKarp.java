package com.ugav.algo;

import java.util.Arrays;


import com.ugav.algo.Utils.QueueIntFixSize;

public class MaxFlowEdmondsKarp implements MaxFlow {

	/*
	 * O(m n^2)
	 */

	public MaxFlowEdmondsKarp() {
	}

	@Override
	public <E> double calcMaxFlow(Graph<E> g0, FlowNetwork<E> net, int source, int target) {
		if (!(g0 instanceof Graph.Directed<?>))
			throw new IllegalArgumentException("only directed graphs are supported");
		if (source == target)
			throw new IllegalArgumentException("Source and target can't be the same vertices");

		Graph<Ref<E>> g = referenceGraph(g0, net);
		int n = g.vertices();
		@SuppressWarnings("unchecked")
		Edge<Ref<E>>[] backtrack = new Edge[n];

		boolean[] visited = new boolean[n];
		QueueIntFixSize queue = new QueueIntFixSize(n);

		for (;;) {
			queue.clear();
			visited[source] = true;
			queue.push(source);

			// perform BFS and find a path of non saturated edges from source to target
			bfs: while (!queue.isEmpty()) {
				int u = queue.pop();
				for (Edge<Ref<E>> e : Utils.iterable(g.edges(u))) {
					int v = e.v();
					if (e.data().flow >= net.getCapacity(e.data().orig) || visited[v])
						continue;
					backtrack[v] = e;
					if (v == target)
						break bfs;
					visited[v] = true;
					queue.push(v);
				}
			}

			// no path to target
			if (backtrack[target] == null)
				break;

			// find out what is the maximum flow we can pass
			double f = Double.MAX_VALUE;
			for (int p = target; p != source;) {
				Edge<Ref<E>> e = backtrack[p];
				f = Math.min(f, net.getCapacity(e.data().orig) - e.data().flow);
				p = e.u();
			}

			// update flow of all edges on path
			for (int p = target; p != source;) {
				Edge<Ref<E>> e = backtrack[p];
				e.data().flow = Math.min(net.getCapacity(e.data().orig), e.data().flow + f);
				e.data().rev.flow = Math.max(0, e.data().rev.flow - f);
				p = e.u();
			}

			backtrack[target] = null;
			Arrays.fill(visited, false);
		}

		for (Edge<Ref<E>> e : g.edges())
			if (e.u() == e.data().orig.u())
				net.setFlow(e.data().orig, e.data().flow);
		double totalFlow = 0;
		for (Edge<Ref<E>> e : Utils.iterable(g.edges(source)))
			if (e.u() == e.data().orig.u())
				totalFlow += e.data().flow;
		return totalFlow;
	}

	private static <E> Graph<Ref<E>> referenceGraph(Graph<E> g0, FlowNetwork<E> net) {
		Graph<Ref<E>> g = new GraphArrayDirectedOld<>(g0.vertices());
		for (Edge<E> e : g0.edges()) {
			Ref<E> ref = new Ref<>(e, 0), refRev = new Ref<>(e, net.getCapacity(e));
			g.addEdge(e.u(), e.v()).setData(ref);
			g.addEdge(e.v(), e.u()).setData(refRev);
			refRev.rev = ref;
			ref.rev = refRev;
		}
		return g;
	}

	private static class Ref<E> {

		final Edge<E> orig;
		Ref<E> rev;
		double flow;

		Ref(Edge<E> e, double flow) {
			orig = e;
			rev = null;
			this.flow = flow;
		}

		@Override
		public int hashCode() {
			return orig.hashCode();
		}

		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof Ref))
				return false;

			Ref<?> o = (Ref<?>) other;
			return orig.equals(o.orig);
		}

		@Override
		public String toString() {
			return "R(" + orig + ")";
		}

	}

}
