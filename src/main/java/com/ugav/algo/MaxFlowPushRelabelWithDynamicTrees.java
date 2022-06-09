package com.ugav.algo;

import java.util.function.Consumer;
import java.util.function.ObjDoubleConsumer;

import com.ugav.algo.DynamicTree.MinEdge;
import com.ugav.algo.Graph.DirectedType;
import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.EdgeIterator;
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

	private static class Vertex<E> {
		final int v;
		double excess;
		boolean isActive;
		int d;
		EdgeIterator<Ref<E>> edges;

		final DynamicTree.Node<Vertex<E>, Edge<Ref<E>>> dtNode;
		int firstDtChild;

		Vertex(int v, DynamicTree.Node<Vertex<E>, Edge<Ref<E>>> dtNode) {
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
		int n = g.vertices();

		final int maxTreeSize = Math.max(1, n * n / g.edges().size());

		QueueFixSize<Vertex<E>> active = new QueueFixSize<>(n);
		DynamicTree<Vertex<E>, Edge<Ref<E>>> dt = new DynamicTreeSplaySized<>(maxCapacity * 10);
		@SuppressWarnings("unchecked")
		Vertex<E>[] vertexData = new Vertex[n];
		for (int u = 0; u < n; u++) {
			vertexData[u] = new Vertex<>(u, dt.makeTree(null));
			vertexData[u].dtNode.setNodeData(vertexData[u]);
		}

		/* Data structure maintaining the children of each node in the DT */
		LinkedListDoubleArrayFixedSize children = new LinkedListDoubleArrayFixedSize(n);
		QueueIntFixSize toCut = new QueueIntFixSize(n);

		/* Init all vertices distances */
		vertexData[source].d = n;

		/* Init all vertices iterators */
		for (int u = 0; u < n; u++)
			vertexData[u].edges = g.edges(u);

		ObjDoubleConsumer<Edge<Ref<E>>> pushFlow = (e0, f) -> {
			Ref<E> e = e0.data();
			if (e0.u() == e.orig.u())
				debug.println("F(", e.orig, ") += ", Double.valueOf(f));
			e.flow += f;
			e.rev.flow -= f;
			assert e.flow <= e.cap + EPS;
			assert e.rev.flow <= e.rev.cap + EPS;
		};
		ObjDoubleConsumer<Edge<Ref<E>>> updateFlow = (e, weight) -> {
			pushFlow.accept(e, e.data().cap - e.data().flow - weight);
		};

		Consumer<Vertex<E>> cut = U -> {
			/* Remove vertex from parent children list */
			Vertex<E> parent = U.dtNode.getParent().getNodeData();
			if (U.v == parent.firstDtChild)
				parent.firstDtChild = children.next(U.v);
			children.remove(U.v);

			/* Remove link from DT */
			dt.cut(U.dtNode);
		};

		/* Push as much as possible from the source vertex */
		for (Edge<Ref<E>> e : Utils.iterable(g.edges(source))) {
			double f = e.data().cap - e.data().flow;
			if (f == 0)
				continue;
			assert f > 0;

			int u = e.u(), v = e.v();
			if (e.u() == e.data().orig.u())
				debug.println("F(", e.data().orig, ") += ", Double.valueOf(f));

			e.data().flow += f;
			e.data().rev.flow -= f;
			assert e.data().flow <= e.data().cap + EPS;
			assert e.data().rev.flow <= e.data().rev.cap + EPS;

			Vertex<E> U = vertexData[u], V = vertexData[v];
			U.excess -= f;
			V.excess += f;
			if (!V.isActive) {
				V.isActive = true;
				active.push(V);
			}
		}

		while (!active.isEmpty()) {
			Vertex<E> U = active.pop();
			if (U.v == source || U.v == target)
				continue;
			assert U.dtNode.getParent() == null;
			EdgeIterator<Ref<E>> it = U.edges;
			int uSize = dt.size(U.dtNode);

			while (U.excess > EPS && it.hasNext()) {
				Edge<Ref<E>> e = it.pickNext();
				Vertex<E> V = vertexData[e.v()];
				double eAccess = e.data().cap - e.data().flow;

				if (!(eAccess > EPS && U.d == V.d + 1)) {
					/* edge is not admissible, just advance */
					it.next();
					continue;
				}

				Vertex<E> W;
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
					pushFlow.accept(e, f);
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
					MinEdge<Vertex<E>, Edge<Ref<E>>> minEdge = dt.findMinEdge(W.dtNode);
					double f = Math.min(W.excess, minEdge.weight());

					/* Push from u up to u's tree root */
					dt.addWeight(W.dtNode, -f);

					/* Update u's excess and u's tree root excess */
					Vertex<E> wRoot = dt.findRoot(W.dtNode).getNodeData();
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
						updateFlow.accept(minEdge.getData(), minEdge.weight());
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
				U.edges = g.edges(U.v);

				/* cut all vertices pointing into u */
				assert U.dtNode.getParent() == null;
				if (U.firstDtChild != -1) {
					for (IteratorInt childIt = children.iterator(U.firstDtChild); childIt.hasNext();) {
						int child = childIt.next();
						Vertex<E> childData = vertexData[child];
						assert childData.dtNode.getParent() == U.dtNode;

						/* update flow */
						MinEdge<Vertex<E>, Edge<Ref<E>>> m = dt.findMinEdge(childData.dtNode);
						updateFlow.accept(m.getData(), m.weight());

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
		Stack<DynamicTree.Node<Vertex<E>, Edge<Ref<E>>>> cleanupStack = new Stack<>();
		for (int u = 0; u < n; u++) {
			for (DynamicTree.Node<Vertex<E>, Edge<Ref<E>>> uDt = vertexData[u].dtNode,
					pDt; (pDt = uDt.getParent()) != null; uDt = pDt)
				cleanupStack.push(uDt);
			while (!cleanupStack.isEmpty()) {
				DynamicTree.Node<Vertex<E>, Edge<Ref<E>>> uDt = cleanupStack.pop();
				assert uDt.getParent().getParent() == null;
				MinEdge<Vertex<E>, Edge<Ref<E>>> m = dt.findMinEdge(uDt);
				updateFlow.accept(m.getData(), m.weight());
				dt.cut(m.u());
			}
		}

		/* Construct result */
		for (Edge<Ref<E>> e : g.edges())
			if (e.u() == e.data().orig.u())
				net.setFlow(e.data().orig, e.data().flow);
		double totalFlow = 0;
		for (Edge<Ref<E>> e : Utils.iterable(g.edges(source)))
			totalFlow += e.data().flow;
		return totalFlow;
	}

	private static <E> Graph<Ref<E>> referenceGraph(Graph<E> g0, FlowNetwork<E> net) {
		Graph<Ref<E>> g = new GraphArray<>(DirectedType.Directed, g0.vertices());
		for (Edge<E> e : g0.edges()) {
			Ref<E> ref = new Ref<>(e, net.getCapacity(e), 0), refRev = new Ref<>(e, 0, 0);
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
		final double cap;
		double flow;

		Ref(Edge<E> e, double cap, double flow) {
			orig = e;
			rev = null;
			this.cap = cap;
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
