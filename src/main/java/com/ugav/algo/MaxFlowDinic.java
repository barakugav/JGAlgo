package com.ugav.algo;

import java.util.Arrays;
import java.util.function.ObjDoubleConsumer;

import com.ugav.algo.DynamicTree.MinEdge;
import com.ugav.algo.Graph.DirectedType;
import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Utils.QueueIntFixSize;
import com.ugav.algo.Utils.Stack;

public class MaxFlowDinic implements MaxFlow {

	private final DebugPrintsManager debug;
	private static final double EPS = 0.0001;

	public MaxFlowDinic() {
		debug = new DebugPrintsManager(false);
	}

	@Override
	public <E> double calcMaxFlow(Graph<E> g0, FlowNetwork<E> net, int source, int target) {
		if (!g0.isDirected())
			throw new IllegalArgumentException("only directed graphs are supported");
		if (source == target)
			throw new IllegalArgumentException("Source and target can't be the same vertices");
		debug.println("\t", getClass().getSimpleName());

		double maxCapacity = 100;
		for (Edge<E> e : g0.edges())
			maxCapacity = Math.max(maxCapacity, net.getCapacity(e));

		Graph<Ref<E>> g = referenceGraph(g0, net);
		final int n = g.vertices();
		GraphLinkedDirected<Ref<E>> L = new GraphLinkedDirected<>(n);
		QueueIntFixSize bfsQueue = new QueueIntFixSize(n);
		int[] level = new int[n];
		DynamicTree<Integer, Edge<Ref<E>>> dt = new DynamicTreeSplay<>(maxCapacity * 3);
		@SuppressWarnings("unchecked")
		DynamicTree.Node<Integer, Edge<Ref<E>>>[] vToDt = new DynamicTree.Node[n];
		Stack<DynamicTree.Node<Integer, Edge<Ref<E>>>> cleanupStack = new Stack<>();

		for (;;) {
			debug.println("calculating residual network");
			L.edges().clear();

			/* Calc the sub graph non saturated edges from source to target using BFS */
			final int unvisited = Integer.MAX_VALUE;
			Arrays.fill(level, unvisited);
			bfsQueue.clear();
			level[source] = 0;
			bfsQueue.push(source);
			bfs: while (!bfsQueue.isEmpty()) {
				int u = bfsQueue.pop();
				if (u == target)
					break bfs;
				int lvl = level[u];
				for (Edge<Ref<E>> e : Utils.iterable(g.edges(u))) {
					int v = e.v();
					if (e.val().flow >= net.getCapacity(e.val().orig) || level[v] <= lvl)
						continue;
					L.addEdge(u, v).val(e.val());
					if (level[v] != unvisited)
						continue;
					level[v] = lvl + 1;
					bfsQueue.push(v);
				}
			}
			if (level[target] == unvisited)
				break; /* All paths to target are saturated */
			debug.println("taget level: ", Integer.valueOf(level[target]));

			dt.clear();
			for (int u = 0; u < n; u++)
				vToDt[u] = dt.makeTree(Integer.valueOf(u));

			ObjDoubleConsumer<Edge<Ref<E>>> updateFlow = (e0, weight) -> {
				Ref<E> e = e0.val();
				double f = net.getCapacity(e.orig) - e.flow - weight;
				if (e0.u() == e.orig.u())
					debug.println("F(", e.orig, ") += ", Double.valueOf(f));
				e.flow += f;
				e.rev.flow -= f;
				assert e.flow <= net.getCapacity(e.orig) + EPS;
				assert e.rev.flow >= 0 - EPS;
			};

			calcBlockFlow: for (;;) {
				int v = dt.findRoot(vToDt[source]).getNodeData().intValue();
				if (v == target) {

					/* Augment */
					debug.println("Augment");
					MinEdge<Integer, Edge<Ref<E>>> min = dt.findMinEdge(vToDt[source]);
					dt.addWeight(vToDt[source], -min.weight());

					/* Delete all saturated edges */
					debug.println("Delete");
					do {
						Edge<Ref<E>> e = min.val();
						assert vToDt[e.u()] == min.u();
						L.removeEdge(e);

						updateFlow.accept(e, 0);
						dt.cut(min.u());

						min = dt.findMinEdge(vToDt[source]);
					} while (min != null && Math.abs(min.weight()) < EPS);

				} else if (!L.edgesOut(v).hasNext()) {

					/* Retreat */
					debug.println("Retreat");
					if (v == source)
						break calcBlockFlow;
					for (Edge<Ref<E>> e : Utils.iterable(L.edgesIn(v))) {
						if (vToDt[e.u()].getParent() != vToDt[v])
							continue; /* If the edge is not in the DT, ignore */

						MinEdge<Integer, Edge<Ref<E>>> m = dt.findMinEdge(vToDt[e.u()]);
						assert e == m.val();
						updateFlow.accept(e, m.weight());

						dt.cut(m.u());
					}
					L.removeEdgesIn(v);

				} else {
					/* Advance */
					debug.println("Advance");
					Edge<Ref<E>> e = L.edgesOut(v).next();
					dt.link(vToDt[e.u()], vToDt[e.v()], net.getCapacity(e.val().orig) - e.val().flow, e);
				}
			}

			/* Cleanup all the edges that stayed in the DT */
			for (int u = 0; u < n; u++) {
				for (DynamicTree.Node<Integer, Edge<Ref<E>>> uDt = vToDt[u],
						pDt; (pDt = uDt.getParent()) != null; uDt = pDt)
					cleanupStack.push(uDt);
				while (!cleanupStack.isEmpty()) {
					DynamicTree.Node<Integer, Edge<Ref<E>>> uDt = cleanupStack.pop();
					assert uDt.getParent() == dt.findRoot(uDt);
					MinEdge<Integer, Edge<Ref<E>>> m = dt.findMinEdge(uDt);
					updateFlow.accept(m.val(), m.weight());
					dt.cut(m.u());
				}
			}
		}

		/* Construct result */
		for (Edge<Ref<E>> e : g.edges())
			if (e.u() == e.val().orig.u())
				net.setFlow(e.val().orig, e.val().flow);
		double totalFlow = 0;
		for (Edge<Ref<E>> e : Utils.iterable(g.edges(source)))
			if (e.u() == e.val().orig.u())
				totalFlow += e.val().flow;
		return totalFlow;
	}

	private static <E> Graph<Ref<E>> referenceGraph(Graph<E> g0, FlowNetwork<E> net) {
		Graph<Ref<E>> g = new GraphArray<>(DirectedType.Directed, g0.vertices());
		for (Edge<E> e : g0.edges()) {
			Ref<E> ref = new Ref<>(e, 0), refRev = new Ref<>(e, net.getCapacity(e));
			g.addEdge(e.u(), e.v()).val(ref);
			g.addEdge(e.v(), e.u()).val(refRev);
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
