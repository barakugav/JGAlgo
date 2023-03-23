package com.ugav.algo;

import java.util.Arrays;
import java.util.function.ObjDoubleConsumer;

import com.ugav.algo.DynamicTree.MinEdge;
import com.ugav.algo.Graph.EdgeRenameListener;
import com.ugav.algo.Utils.QueueIntFixSize;
import com.ugav.algo.Utils.Stack;

import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

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

	private static final Object EdgeRefWeightKey = new Object();

	@Override
	public double calcMaxFlow(Graph g0, FlowNetwork net, int source, int target) {
		if (!(g0 instanceof DiGraph))
			throw new IllegalArgumentException("only directed graphs are supported");
		if (source == target)
			throw new IllegalArgumentException("Source and target can't be the same vertices");
		debug.println("\t", getClass().getSimpleName());

		double maxCapacity = 100;
		for (int e = 0; e < g0.edgesNum(); e++)
			maxCapacity = Math.max(maxCapacity, net.getCapacity(e));

		DiGraph g = referenceGraph((DiGraph) g0, net);
		Weights<Ref> edgeRef = g.edgesWeight(EdgeRefWeightKey);
		final int n = g.verticesNum();
		DiGraph L = new GraphLinkedDirected(n);
		Weights<Ref> edgeRefL = EdgesWeights.ofObjs(L, EdgeRefWeightKey);
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
			EdgeUniqueIDManager idManager = new EdgeUniqueIDManager(L);

			ObjDoubleConsumer<Ref> updateFlow = (e, weight) -> {
				double f = net.getCapacity(e.orig) - e.flow - weight;
//				if (e0.u() == e.orig.u())
//					debug.println("F(", e.orig, ") += ", Double.valueOf(f));
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
					MinEdge<Integer, Integer> min = dt.findMinEdge(vToDt[source]);
					dt.addWeight(vToDt[source], -min.weight());

					/* Delete all saturated edges */
					debug.println("Delete");
					do {
						int e = idManager.idToEdge(min.getData().intValue());
						assert vToDt[L.edgeSource(e)] == min.u();
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
						assert e == idManager.idToEdge(m.getData().intValue());
						Ref ref = edgeRefL.get(e);
						updateFlow.accept(ref, m.weight());

						dt.cut(m.u());
					}
					L.removeEdgesAllIn(v);

				} else {
					/* Advance */
					debug.println("Advance");
					EdgeIter eit = L.edgesOut(v);
					int e = eit.nextInt();
					Ref eRef = edgeRefL.get(e);
					dt.link(vToDt[eit.u()], vToDt[eit.v()], net.getCapacity(eRef.orig) - eRef.flow,
							Integer.valueOf(idManager.edgeToId(e)));
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
					Ref ref = edgeRefL.get(idManager.idToEdge(m.getData().intValue()));
					updateFlow.accept(ref, m.weight());
					dt.cut(m.u());
				}
			}

			idManager.clear();
		}

		/* Construct result */
		for (int e = 0; e < g.edgesNum(); e++) {
			Ref data = edgeRef.get(e);
			if (g.edgeSource(e) == g0.edgeSource(data.orig))
				net.setFlow(data.orig, data.flow);
		}
		double totalFlow = 0;
		for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
			int e = eit.nextInt();
			Ref data = edgeRef.get(e);
			if (g.edgeSource(e) == g0.edgeSource(data.orig))
				totalFlow += data.flow;
		}
		return totalFlow;
	}

	private static DiGraph referenceGraph(DiGraph g0, FlowNetwork net) {
		DiGraph g = new GraphArrayDirected(g0.verticesNum());
		Weights<Ref> edgeRef = EdgesWeights.ofObjs(g, EdgeRefWeightKey);
		for (int e = 0; e < g0.edgesNum(); e++) {
			int u = g0.edgeSource(e), v = g0.edgeTarget(e);
			Ref ref = new Ref(e, 0), refRev = new Ref(e, net.getCapacity(e));
			int e1 = g.addEdge(u, v);
			int e2 = g.addEdge(v, u);
			edgeRef.set(e1, ref);
			edgeRef.set(e2, refRev);
			refRev.rev = ref;
			ref.rev = refRev;
		}
		return g;
	}

	private static class Ref {

		final int orig;
		Ref rev;
		double flow;

		Ref(int e, double flow) {
			orig = e;
			rev = null;
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

	/*
	 * edges change id when we remove edges from the graph. TODO refactor this class
	 */
	private static class EdgeUniqueIDManager {
		private final Graph g;
		private final Int2IntMap id2e = new Int2IntOpenHashMap();
		private final Int2IntMap e2id = new Int2IntOpenHashMap();
		private int idCounter = 1;
		private final EdgeRenameListener listener;

		EdgeUniqueIDManager(Graph g) {
			this.g = g;
			for (int e = 0; e < g.edgesNum(); e++) {
				int id = idCounter++;
				id2e.put(id, e);
				e2id.put(e, id);
			}
			g.addEdgeRenameListener(listener = (e1, e2) -> {
				int id1 = e2id.get(e1);
				int id2 = e2id.get(e2);
				e2id.put(e1, id2);
				e2id.put(e2, id1);
				id2e.put(id1, e2);
				id2e.put(id2, e1);
			});
		}

		void clear() {
			g.removeEdgeRenameListener(listener);
			id2e.clear();
			e2id.clear();
		}

		int edgeToId(int e) {
			return e2id.get(e);
		}

		int idToEdge(int id) {
			return id2e.get(id);
		}
	}

}
