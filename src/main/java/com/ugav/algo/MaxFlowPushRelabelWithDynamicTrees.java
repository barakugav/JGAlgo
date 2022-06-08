package com.ugav.algo;

import java.util.function.Consumer;
import java.util.function.ObjDoubleConsumer;

import com.ugav.algo.DynamicTree.MinEdge;
import com.ugav.algo.Graph.DirectedType;
import com.ugav.algo.Graph.Edge;
import com.ugav.algo.Graph.EdgeIterator;
import com.ugav.algo.Utils.QueueIntFixSize;
import com.ugav.algo.Utils.Stack;

public class MaxFlowPushRelabelWithDynamicTrees implements MaxFlow {

	// TODO overview

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

		QueueIntFixSize active = new QueueIntFixSize(n);
		DynamicTree<Vertex<E>, Edge<Ref<E>>> dt = new DynamicTreeSplay<>(maxCapacity * 10);
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

		ObjDoubleConsumer<Edge<Ref<E>>> updateFlow = (e0, weight) -> {
			Ref<E> e = e0.val();
			double f = e.cap - e.flow - weight;
			if (e0.u() == e.orig.u())
				debug.println("F(", e.orig, ") += ", Double.valueOf(f));
			e.flow += f;
			e.rev.flow -= f;
			assert e.flow <= e.cap + EPS;
			assert e.rev.flow <= e.rev.cap + EPS;
		};

		Consumer<Vertex<E>> cut = uData -> {
			/* Remove vertex from parent children list */
			Vertex<E> parent = uData.dtNode.getParent().getNodeData();
			if (uData.v == parent.firstDtChild)
				parent.firstDtChild = children.next(uData.v);
			children.remove(uData.v);

			/* Remove link from DT */
			dt.cut(uData.dtNode);
		};

		/* Push as much as possible from the source vertex */
		for (Edge<Ref<E>> e : Utils.iterable(g.edges(source))) {
			double f = e.val().cap - e.val().flow;
			if (f == 0)
				continue;
			assert f > 0;

			int u = e.u(), v = e.v();
			if (e.u() == e.val().orig.u())
				debug.println("F(", e.val().orig, ") += ", Double.valueOf(f));

			e.val().flow += f;
			e.val().rev.flow -= f;
			assert e.val().flow <= e.val().cap + EPS;
			assert e.val().rev.flow <= e.val().rev.cap + EPS;

			Vertex<E> uData = vertexData[u], vData = vertexData[v];
			uData.excess -= f;
			vData.excess += f;
			if (!vData.isActive) {
				vData.isActive = true;
				active.push(v);
			}
		}

		while (!active.isEmpty()) {
			int u = active.pop();
			if (u == source || u == target)
				continue;
			Vertex<E> uData = vertexData[u];
			assert uData.dtNode == dt.findRoot(uData.dtNode);
			EdgeIterator<Ref<E>> it = uData.edges;

			while (uData.excess > EPS && it.hasNext()) {
				Edge<Ref<E>> e = it.pickNext();
				Vertex<E> vData = vertexData[e.v()];
				double eAccess = e.val().cap - e.val().flow;
				if (eAccess > EPS && uData.d == vData.d + 1) {

					/* Link u to a node with admissible edge and start pushing */
					dt.link(uData.dtNode, vData.dtNode, eAccess, e);
					assert !children.hasNext(u) && !children.hasPrev(u);
					if (vData.firstDtChild == -1) {
						vData.firstDtChild = u;
					} else {
						children.add(vData.firstDtChild, u);
					}

					do {
						/* Find the maximum flow that can be pushed */
						MinEdge<Vertex<E>, Edge<Ref<E>>> minEdge = dt.findMinEdge(uData.dtNode);
						double f = Math.min(uData.excess, minEdge.weight());

						/* Push from u up to u's tree root */
						dt.addWeight(uData.dtNode, -f);

						/* Update u's excess and u's tree root excess */
						Vertex<E> uRoot = dt.findRoot(uData.dtNode).getNodeData();
						uData.excess -= f;
						uRoot.excess += f;
						if (!uRoot.isActive) {
							uRoot.isActive = true;
							active.push(uRoot.v);
						}

						/* Cut all saturated edges from u to u's tree root */
						for (; uData.dtNode.getParent() != null;) {
							minEdge = dt.findMinEdge(uData.dtNode);
							if (minEdge.weight() > EPS)
								break;
							updateFlow.accept(minEdge.val(), minEdge.weight());
							cut.accept(minEdge.u().getNodeData());
						}

						/* Continue as long as u has excess and it is not the root */
					} while (uData.excess > EPS && uData.dtNode != dt.findRoot(uData.dtNode));

				} else {
					/* edge is not admissible, just advance */
					it.next();
				}
			}

			/* Finished iterating over all vertex edges, relabel and reset iterator */
			if (!it.hasNext()) {
				uData.d++;
				debug.println("R(", Integer.valueOf(u), ") <- ", Integer.valueOf(uData.d));
				uData.edges = g.edges(u);

				/* cut all vertices pointing into u */
				assert uData.dtNode.getParent() == null;
				if (uData.firstDtChild != -1) {
					for (IteratorInt childIt = children.iterator(uData.firstDtChild); childIt.hasNext();) {
						int child = childIt.next();
						Vertex<E> childData = vertexData[child];
						assert childData.dtNode.getParent() == uData.dtNode;

						/* update flow */
						MinEdge<Vertex<E>, Edge<Ref<E>>> m = dt.findMinEdge(childData.dtNode);
						updateFlow.accept(m.val(), m.weight());

						/* cut child */
						toCut.push(child);
					}
					while (!toCut.isEmpty())
						cut.accept(vertexData[toCut.pop()]);
				}
			}

			/* Update isActive and add to queue if active */
			if (uData.isActive = (uData.excess > EPS))
				active.push(u);
		}

		/* Cleanup all the edges that stayed in the DT */
		Stack<DynamicTree.Node<Vertex<E>, Edge<Ref<E>>>> cleanupStack = new Stack<>();
		for (int u = 0; u < n; u++) {
			for (DynamicTree.Node<Vertex<E>, Edge<Ref<E>>> uDt = vertexData[u].dtNode,
					pDt; (pDt = uDt.getParent()) != null; uDt = pDt)
				cleanupStack.push(uDt);
			while (!cleanupStack.isEmpty()) {
				DynamicTree.Node<Vertex<E>, Edge<Ref<E>>> uDt = cleanupStack.pop();
				assert uDt.getParent() == dt.findRoot(uDt);
				MinEdge<Vertex<E>, Edge<Ref<E>>> m = dt.findMinEdge(uDt);
				updateFlow.accept(m.val(), m.weight());
				dt.cut(m.u());
			}
		}

		/* Construct result */
		for (Edge<Ref<E>> e : g.edges())
			if (e.u() == e.val().orig.u())
				net.setFlow(e.val().orig, e.val().flow);
		double totalFlow = 0;
		for (Edge<Ref<E>> e : Utils.iterable(g.edges(source)))
			totalFlow += e.val().flow;
		return totalFlow;
	}

	private static <E> Graph<Ref<E>> referenceGraph(Graph<E> g0, FlowNetwork<E> net) {
		Graph<Ref<E>> g = new GraphArray<>(DirectedType.Directed, g0.vertices());
		for (Edge<E> e : g0.edges()) {
			Ref<E> ref = new Ref<>(e, net.getCapacity(e), 0), refRev = new Ref<>(e, 0, 0);
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
