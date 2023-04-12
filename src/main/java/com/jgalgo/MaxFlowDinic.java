package com.jgalgo;

import java.util.Arrays;
import java.util.function.ObjDoubleConsumer;

import com.jgalgo.DynamicTree.MinEdge;
import com.jgalgo.IDStrategy.Fixed;
import com.jgalgo.Utils.Stack;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

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
	public double calcMaxFlow(Graph g, FlowNetwork net, int source, int target) {
		if (!(g instanceof DiGraph))
			throw new IllegalArgumentException("only directed graphs are supported");
		return calcMaxFlow0((DiGraph) g, net, source, target);
	}

	private double calcMaxFlow0(DiGraph g0, FlowNetwork net, int source, int target) {
		if (source == target)
			throw new IllegalArgumentException("Source and target can't be the same vertices");
		debug.println("\t", getClass().getSimpleName());

		double maxCapacity = 100;
		for (IntIterator it = g0.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			maxCapacity = Math.max(maxCapacity, net.getCapacity(e));
		}

		DiGraph g = referenceGraph(g0, net);
		Weights<Ref> edgeRef = g.edgesWeight(EdgeRefWeightKey);
		final int n = g.vertices().size();
		DiGraph L = GraphBuilder.Linked.newInstance().setVerticesNum(n).setEdgesIDStrategy(Fixed.class).buildDirected();
		Weights<Ref> edgeRefL = L.addEdgesWeights(EdgeRefWeightKey, Ref.class);
		IntPriorityQueue bfsQueue = new IntArrayFIFOQueue();
		int[] level = new int[n];
		DynamicTree<Integer, Integer> dt = new DynamicTreeSplay<>(maxCapacity * 10);
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
			bfsQueue.enqueue(source);
			bfs: while (!bfsQueue.isEmpty()) {
				int u = bfsQueue.dequeueInt();
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
					bfsQueue.enqueue(v);
				}
			}
			if (level[target] == unvisited)
				break; /* All paths to target are saturated */
			debug.println("target level: " + level[target]);

			dt.clear();
			for (int u = 0; u < n; u++)
				vToDt[u] = dt.makeTree(Integer.valueOf(u));

			ObjDoubleConsumer<Ref> updateFlow = (e, weight) -> {
				double f = net.getCapacity(e.orig) - e.flow - weight;
				// if (e0.u() == e.orig.u())
				// debug.println("F(", e.orig, ") += ", Double.valueOf(f));
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
						int e = min.getData().intValue();
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
						assert e == m.getData().intValue();
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
					dt.link(vToDt[eit.u()], vToDt[eit.v()], net.getCapacity(eRef.orig) - eRef.flow, Integer.valueOf(e));
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
					Ref ref = edgeRefL.get(m.getData().intValue());
					updateFlow.accept(ref, m.weight());
					dt.cut(m.u());
				}
			}
		}

		/* Construct result */
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
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
		DiGraph g = new GraphArrayDirected(g0.vertices().size());
		Weights<Ref> edgeRef = g.addEdgesWeights(EdgeRefWeightKey, Ref.class);
		for (IntIterator it = g0.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
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
		public String toString() {
			return "R(" + orig + ")";
		}

	}

}
