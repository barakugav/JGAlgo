package com.ugav.algo;

import java.util.Arrays;
import java.util.function.ObjDoubleConsumer;

import com.ugav.algo.DynamicTree.MinEdge;
import com.ugav.algo.Graph.EdgeIter;
import com.ugav.algo.Utils.QueueIntFixSize;
import com.ugav.algo.Utils.Stack;

public class MaxFlowDinic implements MaxFlow {

	/**
	 * Dinic's max flow algorithm using dynamic trees.
	 *
	 * O(m n log n)
	 */

	private final DebugPrintsManager debug;
	private static final double EPS = 0.0001;

	public MaxFlowDinic() {
		debug = new DebugPrintsManager(false);
	}

	@Override
	public double calcMaxFlow(Graph g0, FlowNetwork net, int source, int target) {
		if (!(g0 instanceof Graph.Directed))
			throw new IllegalArgumentException("only directed graphs are supported");
		if (source == target)
			throw new IllegalArgumentException("Source and target can't be the same vertices");
		debug.println("\t", getClass().getSimpleName());

		double maxCapacity = 100;
		for (int e = 0; e < g0.edges(); e++)
			maxCapacity = Math.max(maxCapacity, net.getCapacity(e));

		Graph.Directed g = referenceGraph((Graph.Directed) g0, net);
		EdgeData<Ref> edgeRef = g.getEdgeData("edgeRef");
		final int n = g.vertices();
		GraphLinkedDirected L = new GraphLinkedDirected(n);
		EdgeData<Ref> edgeRefL = L.newEdgeData("edgeRef");
		QueueIntFixSize bfsQueue = new QueueIntFixSize(n);
		int[] level = new int[n];
		DynamicTree<Integer, Integer> dt = new DynamicTreeSplay<>(maxCapacity * 3);
		@SuppressWarnings("unchecked")
		DynamicTree.Node<Integer, Integer>[] vToDt = new DynamicTree.Node[n];
		Stack<DynamicTree.Node<Integer, Integer>> cleanupStack = new Stack<>();

		for (;;) {
			debug.println("calculating residual network");
			L.clearEdges();

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
				for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.v();
					Ref eData = edgeRef.get(e);
					if (eData.flow >= net.getCapacity(eData.orig) || level[v] <= lvl)
						continue;
					edgeRefL.set(L.addEdge(u, v), eData);
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

			ObjDoubleConsumer<Ref> updateFlow = (e, weight) -> {
//				Ref e = g.edgeData().get(e0);
				double f = net.getCapacity(e.orig) - e.flow - weight;
//				if (e0.u() == e.orig.u())
//					debug.println("F(", e.orig, ") += ", Double.valueOf(f));
				Ref eRev = edgeRef.get(e.rev);
				e.flow += f;
				eRev.flow -= f;
				assert e.flow <= net.getCapacity(e.orig) + EPS;
				assert eRev.flow >= 0 - EPS;
			};

			calcBlockFlow: for (;;) {
				int v = dt.findRoot(vToDt[source]).getNodeData().intValue();
				if (v == target) {

					/* Augment */
					debug.println("Augment");
					MinEdge<Integer, Integer> min = dt.findMinEdge(vToDt[source]);
					dt.addWeight(vToDt[source], -min.weight());

					/* Delete all saturated edges */
					debug.println("Delete");
					do {
						int e = min.getData();
						assert vToDt[L.getEdgeSource(e)] == min.u();
						Ref ref = edgeRefL.get(e);
						L.removeEdge(e);

						updateFlow.accept(ref, 0);
						dt.cut(min.u());

						min = dt.findMinEdge(vToDt[source]);
					} while (min != null && Math.abs(min.weight()) < EPS);

				} else if (!L.edgesOut(v).hasNext()) {

					/* Retreat */
					debug.println("Retreat");
					if (v == source)
						break calcBlockFlow;
					for (EdgeIter eit = L.edgesIn(v); eit.hasNext();) {
						int e = eit.nextInt();
						int u = eit.u();
						if (vToDt[u].getParent() != vToDt[v])
							continue; /* If the edge is not in the DT, ignore */

						MinEdge<Integer, Integer> m = dt.findMinEdge(vToDt[u]);
						assert e == m.getData();
						Ref ref = edgeRefL.get(e);
						updateFlow.accept(ref, m.weight());

						dt.cut(m.u());
					}
					L.removeEdgesIn(v);

				} else {
					/* Advance */
					debug.println("Advance");
					EdgeIter eit = L.edgesOut(v);
					int e = eit.nextInt();
					Ref eRef = edgeRef.get(e);
					dt.link(vToDt[eit.u()], vToDt[eit.v()], net.getCapacity(eRef.orig) - eRef.flow, e);
				}
			}

			/* Cleanup all the edges that stayed in the DT */
			for (int u = 0; u < n; u++) {
				for (DynamicTree.Node<Integer, Integer> uDt = vToDt[u], pDt; (pDt = uDt.getParent()) != null; uDt = pDt)
					cleanupStack.push(uDt);
				while (!cleanupStack.isEmpty()) {
					DynamicTree.Node<Integer, Integer> uDt = cleanupStack.pop();
					assert uDt.getParent() == dt.findRoot(uDt);
					MinEdge<Integer, Integer> m = dt.findMinEdge(uDt);
					Ref ref = edgeRefL.get(m.getData());
					updateFlow.accept(ref, m.weight());
					dt.cut(m.u());
				}
			}
		}

		/* Construct result */
		for (int e = 0; e < g.edges(); e++) {
			Ref data = edgeRef.get(e);
			if (g.getEdgeSource(e) == g0.getEdgeSource(data.orig))
				net.setFlow(data.orig, data.flow);
		}
		double totalFlow = 0;
		for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
			int e = eit.nextInt();
			Ref data = edgeRef.get(e);
			if (g.getEdgeSource(e) == g0.getEdgeSource(data.orig))
				totalFlow += data.flow;
		}
		return totalFlow;
	}

	private static Graph.Directed referenceGraph(Graph.Directed g0, FlowNetwork net) {
		Graph.Directed g = new GraphArrayDirected(g0.vertices());
		EdgeData<Ref> edgeRef = g.newEdgeData("edgeRef");
		for (int e = 0; e < g0.edges(); e++) {
			int u = g.getEdgeSource(e), v = g.getEdgeTarget(e);
			Ref ref = new Ref(e, 0), refRev = new Ref(e, net.getCapacity(e));
			int e1 = g.addEdge(u, v);
			int e2 = g.addEdge(v, u);
			edgeRef.set(e1, ref);
			edgeRef.set(e2, refRev);
			refRev.rev = e1;
			ref.rev = e2;
		}
		return g;
	}

	private static class Ref {

		final int orig;
		int rev;
		double flow;

		Ref(int e, double flow) {
			orig = e;
			rev = -1;
			this.flow = flow;
		}

		@Override
		public int hashCode() {
			return orig;
		}

		@Override
		public boolean equals(Object other) {
			if (other == this)
				return true;
			if (!(other instanceof Ref))
				return false;

			Ref o = (Ref) other;
			return orig == o.orig;
		}

		@Override
		public String toString() {
			return "R(" + orig + ")";
		}

	}

}
