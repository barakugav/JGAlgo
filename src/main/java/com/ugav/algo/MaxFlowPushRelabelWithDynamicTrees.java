package com.ugav.algo;

import java.util.function.Consumer;
import java.util.function.ObjDoubleConsumer;

import com.ugav.algo.DynamicTree.MinEdge;
import com.ugav.algo.Graph.EdgeIter;
import com.ugav.algo.Utils.IterPickable;
import com.ugav.algo.Utils.QueueFixSize;
import com.ugav.algo.Utils.QueueIntFixSize;
import com.ugav.algo.Utils.Stack;

public class MaxFlowPushRelabelWithDynamicTrees implements MaxFlow {

	/**
	 * Push/relabel implementation using dynamic trees.
	 *
	 * O(m n log (n^2 / m))
	 */

	private final DebugPrintsManager debug;
	private static final double EPS = 0.0001;

	public MaxFlowPushRelabelWithDynamicTrees() {
		debug = new DebugPrintsManager(false);
	}

	private static class Vertex {
		final int v;
		double excess;
		boolean isActive;
		int d;
		IterPickable.Int edges;

		final DynamicTree.Node<Vertex, Integer> dtNode;
		int firstDtChild;

		Vertex(int v, DynamicTree.Node<Vertex, Integer> dtNode) {
			this.v = v;
			excess = 0;
			isActive = false;
			d = 0;
			edges = null;

			this.dtNode = dtNode;
			firstDtChild = -1;
		}
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
		int n = g.vertices();

		final int maxTreeSize = Math.max(1, n * n / g.edges());

		QueueFixSize<Vertex> active = new QueueFixSize<>(n);
		DynamicTree<Vertex, Integer> dt = new DynamicTreeSplaySized<>(maxCapacity * 10);
		Vertex[] vertexData = new Vertex[n];
		for (int u = 0; u < n; u++) {
			vertexData[u] = new Vertex(u, dt.makeTree(null));
			vertexData[u].dtNode.setNodeData(vertexData[u]);
		}

		/* Data structure maintaining the children of each node in the DT */
		LinkedListDoubleArrayFixedSize children = new LinkedListDoubleArrayFixedSize(n);
		QueueIntFixSize toCut = new QueueIntFixSize(n);

		/* Init all vertices distances */
		vertexData[source].d = n;

		/* Init all vertices iterators */
		for (int u = 0; u < n; u++)
			vertexData[u].edges = new IterPickable.Int(g.edgesOut(u));

		ObjDoubleConsumer<Ref> pushFlow = (e, f) -> {
//			Ref e = e0.data();
//			if (e0.u() == e.orig.u())
//				debug.println("F(", e.orig, ") += ", Double.valueOf(f));
			e.flow += f;
			e.rev.flow -= f;
			assert e.flow <= e.cap + EPS;
			assert e.rev.flow <= e.rev.cap + EPS;
		};
		ObjDoubleConsumer<Ref> updateFlow = (e, weight) -> {
			pushFlow.accept(e, e.cap - e.flow - weight);
		};

		Consumer<Vertex> cut = U -> {
			/* Remove vertex from parent children list */
			Vertex parent = U.dtNode.getParent().getNodeData();
			if (U.v == parent.firstDtChild)
				parent.firstDtChild = children.next(U.v);
			children.remove(U.v);

			/* Remove link from DT */
			dt.cut(U.dtNode);
		};

		/* Push as much as possible from the source vertex */
		for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
			int e = eit.nextInt();
			int v = eit.v();
			Ref data = edgeRef.get(e);
			double f = data.cap - data.flow;
			if (f == 0)
				continue;
			assert f > 0;

			int u = eit.u();
//			int u = e.u(), v = e.v();
//			if (e.u() == data.orig.u())
//				debug.println("F(", data.orig, ") += ", Double.valueOf(f));

			data.flow += f;
			data.rev.flow -= f;
			assert data.flow <= data.cap + EPS;
			assert data.rev.flow <= data.rev.cap + EPS;

			Vertex U = vertexData[u], V = vertexData[v];
			U.excess -= f;
			V.excess += f;
			if (!V.isActive) {
				V.isActive = true;
				active.push(V);
			}
		}

		while (!active.isEmpty()) {
			Vertex U = active.pop();
			if (U.v == source || U.v == target)
				continue;
			assert U.dtNode.getParent() == null;
			IterPickable.Int it = U.edges;
			int uSize = dt.size(U.dtNode);

			while (U.excess > EPS && it.hasNext()) {
				int e = it.pickNext();
				Ref data = edgeRef.get(e);
				Vertex V = vertexData[g.getEdgeTarget(e)];
				double eAccess = data.cap - data.flow;

				if (!(eAccess > EPS && U.d == V.d + 1)) {
					/* edge is not admissible, just advance */
					it.nextInt();
					continue;
				}

				Vertex W;
				int vSize = dt.size(V.dtNode);
				if (uSize + vSize <= maxTreeSize) {
					/* Link u to a node with admissible edge and start pushing */
					dt.link(U.dtNode, V.dtNode, eAccess, e);
					assert !children.hasNext(U.v) && !children.hasPrev(U.v);
					if (V.firstDtChild == -1) {
						V.firstDtChild = U.v;
					} else {
						children.add(V.firstDtChild, U.v);
					}
					W = U;
				} else {
					/* Avoid big trees, no link, push manually and continue pushing in v's tree */
					double f = Math.min(U.excess, eAccess);
					pushFlow.accept(data, f);
					U.excess -= f;
					V.excess += f;
					if (V.v == source || V.v == target)
						continue;
					assert V.excess > 0;
					W = V;
				}

				/* Continue as long as u has excess and it is not the root */
				while (W.excess > EPS && W.dtNode.getParent() != null) {
					/* Find the maximum flow that can be pushed */
					MinEdge<Vertex, Integer> minEdge = dt.findMinEdge(W.dtNode);
					double f = Math.min(W.excess, minEdge.weight());

					/* Push from u up to u's tree root */
					dt.addWeight(W.dtNode, -f);

					/* Update u's excess and u's tree root excess */
					Vertex wRoot = dt.findRoot(W.dtNode).getNodeData();
					W.excess -= f;
					wRoot.excess += f;
					if (!wRoot.isActive) {
						wRoot.isActive = true;
						active.push(wRoot);
					}

					/* Cut all saturated edges from u to u's tree root */
					for (; W.dtNode.getParent() != null;) {
						minEdge = dt.findMinEdge(W.dtNode);
						if (minEdge.weight() > EPS)
							break;
						Ref edgeData = edgeRef.get(minEdge.getData());
						updateFlow.accept(edgeData, minEdge.weight());
						cut.accept(minEdge.u().getNodeData());
					}
				}

				if (W.excess > EPS && !W.isActive) {
					W.isActive = true;
					active.push(W);
				}
			}

			/* Finished iterating over all vertex edges, relabel and reset iterator */
			if (!it.hasNext()) {
				U.d++;
				debug.println("R(", Integer.valueOf(U.v), ") <- ", Integer.valueOf(U.d));
				U.edges = new IterPickable.Int(g.edgesOut(U.v));

				/* cut all vertices pointing into u */
				assert U.dtNode.getParent() == null;
				if (U.firstDtChild != -1) {
					for (IteratorInt childIt = children.iterator(U.firstDtChild); childIt.hasNext();) {
						// TODO use fastutil iter
						int child = childIt.next();
						Vertex childData = vertexData[child];
						assert childData.dtNode.getParent() == U.dtNode;

						/* update flow */
						MinEdge<Vertex, Integer> m = dt.findMinEdge(childData.dtNode);
						Ref edgeData = edgeRef.get(m.getData());
						updateFlow.accept(edgeData, m.weight());

						/* cut child */
						toCut.push(child);
					}
					while (!toCut.isEmpty())
						cut.accept(vertexData[toCut.pop()]);
				}
			}

			/* Update isActive and add to queue if active */
			if (U.isActive = (U.excess > EPS))
				active.push(U);
		}

		/* Cleanup all the edges that stayed in the DT */
		Stack<DynamicTree.Node<Vertex, Integer>> cleanupStack = new Stack<>();
		for (int u = 0; u < n; u++) {
			for (DynamicTree.Node<Vertex, Integer> uDt = vertexData[u].dtNode,
					pDt; (pDt = uDt.getParent()) != null; uDt = pDt)
				cleanupStack.push(uDt);
			while (!cleanupStack.isEmpty()) {
				DynamicTree.Node<Vertex, Integer> uDt = cleanupStack.pop();
				assert uDt.getParent().getParent() == null;
				MinEdge<Vertex, Integer> m = dt.findMinEdge(uDt);
				Ref edgeData = edgeRef.get(m.getData());
				updateFlow.accept(edgeData, m.weight());
				dt.cut(m.u());
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
			totalFlow += data.flow;
		}
		return totalFlow;
	}

	private static Graph.Directed referenceGraph(Graph.Directed g0, FlowNetwork net) {
		Graph.Directed g = new GraphArrayDirected(g0.vertices());
		EdgeData<Ref> edgeRef = g.newEdgeData("edgeRef");
		for (int e = 0; e < g0.edges(); e++) {
			int u = g0.getEdgeSource(e), v = g0.getEdgeTarget(e);
			Ref ref = new Ref(e, net.getCapacity(e), 0), refRev = new Ref(e, 0, 0);
			edgeRef.set(g.addEdge(u, v), ref);
			edgeRef.set(g.addEdge(v, u), refRev);
			refRev.rev = ref;
			ref.rev = refRev;
		}
		return g;
	}

	private static class Ref {

		final int orig;
		Ref rev;
		final double cap;
		double flow;

		Ref(int e, double cap, double flow) {
			orig = e;
			rev = null;
			this.cap = cap;
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
