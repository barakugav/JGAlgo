package com.jgalgo;

import java.util.function.Consumer;
import java.util.function.ObjDoubleConsumer;

import com.jgalgo.DynamicTree.MinEdge;
import com.jgalgo.Utils.IterPickable;
import com.jgalgo.Utils.QueueFixSize;
import com.jgalgo.Utils.Stack;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

/**
 * The push relabel algorithm for maximum flow using dynamic trees.
 * <p>
 * The push-relabel algorithm maintain a "preflow" and gradually converts it
 * into a maximum flow by moving flow locally between neighboring nodes using
 * <i>push</i> operations under the guidance of an admissible network maintained
 * by <i>relabel</i> operations.
 * <p>
 * Conceptually, the dynamic trees are used to push flow along multiple edges
 * simultaneously. The current flow of each individual edges is not maintained
 * explicitly, rather each path is stored as a dynamic tree, and the flow is
 * stored as a weight of the tree edges - to calculate the weight (flow) of an
 * edge, one would have to traverse the tree from the root to the edge and sum
 * all weights on the path.
 * <p>
 * Using the dynamic trees reduce the running time of the push-relabel algorithm
 * to {@code m n log (n^2 / m)} and linear space. This implementation uses FIFO
 * to order the vertices to be examined. Note that this implementation is
 * usually out preformed in practice by simpler variants of the push-relabel
 * algorithm, such as {@link MaxFlowPushRelabelHighestFirst}.
 *
 * @see MaxFlowPushRelabel
 * @author Barak Ugav
 */
public class MaxFlowPushRelabelDynamicTrees implements MaxFlow {

	private static final Object EdgeRefWeightKey = new Object();

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

		private static final double EPS = 0.0001;
		private final DebugPrintsManager debug = new DebugPrintsManager(false);

		WorkerDouble(DiGraph gOring, FlowNetwork net, int source, int sink) {
			this.gOring = gOring;
			this.net = net;
			this.source = source;
			this.sink = sink;
		}

		double computeMaxFlow() {
			if (source == sink)
				throw new IllegalArgumentException("Source and sink can't be the same vertex");
			debug.println("\t", getClass().getSimpleName());

			double maxCapacity = 100;
			for (IntIterator it = gOring.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				maxCapacity = Math.max(maxCapacity, net.getCapacity(e));
			}

			DiGraph g = referenceGraph(gOring, net);
			Weights<Ref> edgeRef = g.edgesWeight(EdgeRefWeightKey);
			int n = g.vertices().size();

			final int maxTreeSize = Math.max(1, n * n / g.edges().size());

			QueueFixSize<Vertex> active = new QueueFixSize<>(n);
			DynamicTree dt = new DynamicTreeSplaySized(maxCapacity * 10);
			Vertex[] vertexData = new Vertex[n];
			for (int u = 0; u < n; u++) {
				vertexData[u] = new Vertex(u, dt.makeTree());
				vertexData[u].dtNode.setNodeData(vertexData[u]);
			}

			/* Data structure maintaining the children of each node in the DT */
			LinkedListDoubleArrayFixedSize children = LinkedListDoubleArrayFixedSize.newInstance(n);
			IntPriorityQueue toCut = new IntArrayFIFOQueue();

			/* Init all vertices distances */
			SSSP.Result initD = new SSSPCardinality().computeShortestPaths(g, sink);
			for (int u = 0; u < n; u++)
				if (u != source && u != sink)
					vertexData[u].d = (int) initD.distance(sink);
			vertexData[source].d = n;
			vertexData[sink].d = 0;

			/* Init all vertices iterators */
			for (int u = 0; u < n; u++)
				vertexData[u].edges = new IterPickable.Int(g.edgesOut(u));

			ObjDoubleConsumer<Ref> pushFlow = (e, f) -> {
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
				children.disconnect(U.v);

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
				if (U.v == source || U.v == sink)
					continue;
				assert U.dtNode.getParent() == null;
				IterPickable.Int it = U.edges;
				int uSize = dt.size(U.dtNode);

				while (U.excess > EPS && it.hasNext()) {
					int e = it.pickNext();
					Ref data = edgeRef.get(e);
					Vertex V = vertexData[g.edgeTarget(e)];
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
						dt.link(U.dtNode, V.dtNode, eAccess);
						U.edgeToParent = e;
						assert !children.hasNext(U.v) && !children.hasPrev(U.v);
						if (V.firstDtChild == -1) {
							V.firstDtChild = U.v;
						} else {
							children.insert(V.firstDtChild, U.v);
						}
						W = U;
					} else {
						/* Avoid big trees, no link, push manually and continue pushing in v's tree */
						double f = Math.min(U.excess, eAccess);
						pushFlow.accept(data, f);
						U.excess -= f;
						V.excess += f;
						if (V.v == source || V.v == sink)
							continue;
						assert V.excess > 0;
						W = V;
					}

					/* Continue as long as u has excess and it is not the root */
					while (W.excess > EPS && W.dtNode.getParent() != null) {
						/* Find the maximum flow that can be pushed */
						MinEdge minEdge = dt.findMinEdge(W.dtNode);
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
							Ref edgeData = edgeRef.get(minEdge.u().<Vertex>getNodeData().edgeToParent);
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
						for (IntIterator childIt = children.iterator(U.firstDtChild); childIt.hasNext();) {
							int child = childIt.nextInt();
							Vertex childData = vertexData[child];
							assert childData.dtNode.getParent() == U.dtNode;

							/* update flow */
							MinEdge m = dt.findMinEdge(childData.dtNode);
							Ref edgeData = edgeRef.get(m.u().<Vertex>getNodeData().edgeToParent);
							updateFlow.accept(edgeData, m.weight());

							/* cut child */
							toCut.enqueue(child);
						}
						while (!toCut.isEmpty())
							cut.accept(vertexData[toCut.dequeueInt()]);
					}
				}

				/* Update isActive and add to queue if active */
				if (U.isActive = (U.excess > EPS))
					active.push(U);
			}

			/* Cleanup all the edges that stayed in the DT */
			Stack<DynamicTree.Node> cleanupStack = new Stack<>();
			for (int u = 0; u < n; u++) {
				for (DynamicTree.Node uDt = vertexData[u].dtNode,
						pDt; (pDt = uDt.getParent()) != null; uDt = pDt)
					cleanupStack.push(uDt);
				while (!cleanupStack.isEmpty()) {
					DynamicTree.Node uDt = cleanupStack.pop();
					assert uDt.getParent().getParent() == null;
					MinEdge m = dt.findMinEdge(uDt);
					Ref edgeData = edgeRef.get(m.u().<Vertex>getNodeData().edgeToParent);
					updateFlow.accept(edgeData, m.weight());
					dt.cut(m.u());
				}
			}

			/* Construct result */
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				Ref data = edgeRef.get(e);
				if (g.edgeSource(e) == gOring.edgeSource(data.orig))
					net.setFlow(data.orig, data.flow);
			}
			double totalFlow = 0;
			for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
				int e = eit.nextInt();
				Ref data = edgeRef.get(e);
				if (g.edgeSource(e) == gOring.edgeSource(data.orig))
					totalFlow += data.flow;
			}
			for (EdgeIter eit = g.edgesIn(source); eit.hasNext();) {
				int e = eit.nextInt();
				Ref data = edgeRef.get(e);
				if (g.edgeSource(e) == gOring.edgeSource(data.orig))
					totalFlow -= data.flow;
			}
			return totalFlow;
		}

		private static DiGraph referenceGraph(DiGraph g0, FlowNetwork net) {
			DiGraph g = new GraphArrayDirected(g0.vertices().size());
			Weights<Ref> edgeRef = g.addEdgesWeights(EdgeRefWeightKey, Ref.class);
			for (IntIterator it = g0.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				int u = g0.edgeSource(e), v = g0.edgeTarget(e);
				Ref ref = new Ref(e, net.getCapacity(e), 0), refRev = new Ref(e, 0, 0);
				edgeRef.set(g.addEdge(u, v), ref);
				edgeRef.set(g.addEdge(v, u), refRev);
				refRev.rev = ref;
				ref.rev = refRev;
			}
			return g;
		}

		private static class Vertex {
			final int v;
			double excess;
			boolean isActive;
			int d;
			IterPickable.Int edges;

			final DynamicTree.Node dtNode;
			int firstDtChild;

			int edgeToParent = -1;

			Vertex(int v, DynamicTree.Node dtNode) {
				this.v = v;
				excess = 0;
				isActive = false;
				d = 0;
				edges = null;

				this.dtNode = dtNode;
				firstDtChild = -1;
			}
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
			public String toString() {
				return "R(" + orig + ")";
			}

		}
	}

	private static class WorkerInt {

		final DiGraph gOring;
		final FlowNetwork.Int net;
		final int source;
		final int sink;

		private final DebugPrintsManager debug = new DebugPrintsManager(false);

		WorkerInt(DiGraph gOring, FlowNetwork.Int net, int source, int sink) {
			this.gOring = gOring;
			this.net = net;
			this.source = source;
			this.sink = sink;
		}

		double computeMaxFlow() {
			if (source == sink)
				throw new IllegalArgumentException("Source and sink can't be the same vertex");
			debug.println("\t", getClass().getSimpleName());

			int maxCapacity = 100;
			for (IntIterator it = gOring.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				maxCapacity = Math.max(maxCapacity, net.getCapacityInt(e));
			}

			DiGraph g = referenceGraph(gOring, net);
			Weights<Ref> edgeRef = g.edgesWeight(EdgeRefWeightKey);
			int n = g.vertices().size();

			final int maxTreeSize = Math.max(1, n * n / g.edges().size());

			QueueFixSize<Vertex> active = new QueueFixSize<>(n);
			DynamicTree dt = new DynamicTreeSplaySizedInt(maxCapacity * 10);
			Vertex[] vertexData = new Vertex[n];
			for (int u = 0; u < n; u++) {
				vertexData[u] = new Vertex(u, dt.makeTree());
				vertexData[u].dtNode.setNodeData(vertexData[u]);
			}

			/* Data structure maintaining the children of each node in the DT */
			LinkedListDoubleArrayFixedSize children = LinkedListDoubleArrayFixedSize.newInstance(n);
			IntPriorityQueue toCut = new IntArrayFIFOQueue();

			/* Init all vertices distances */
			SSSP.Result initD = new SSSPCardinality().computeShortestPaths(g, sink);
			for (int u = 0; u < n; u++)
				if (u != source && u != sink)
					vertexData[u].d = (int) initD.distance(sink);
			vertexData[source].d = n;
			vertexData[sink].d = 0;

			/* Init all vertices iterators */
			for (int u = 0; u < n; u++)
				vertexData[u].edges = new IterPickable.Int(g.edgesOut(u));

			ObjDoubleConsumer<Ref> pushFlow = (e, f) -> {
				e.flow += f;
				e.rev.flow -= f;
				assert e.flow <= e.cap;
				assert e.rev.flow <= e.rev.cap;
			};
			ObjDoubleConsumer<Ref> updateFlow = (e, weight) -> {
				pushFlow.accept(e, e.cap - e.flow - weight);
			};

			Consumer<Vertex> cut = U -> {
				/* Remove vertex from parent children list */
				Vertex parent = U.dtNode.getParent().getNodeData();
				if (U.v == parent.firstDtChild)
					parent.firstDtChild = children.next(U.v);
				children.disconnect(U.v);

				/* Remove link from DT */
				dt.cut(U.dtNode);
			};

			/* Push as much as possible from the source vertex */
			for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.v();
				Ref data = edgeRef.get(e);
				int f = data.cap - data.flow;
				if (f == 0)
					continue;
				assert f > 0;

				int u = eit.u();

				data.flow += f;
				data.rev.flow -= f;
				assert data.flow <= data.cap;
				assert data.rev.flow <= data.rev.cap;

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
				if (U.v == source || U.v == sink)
					continue;
				assert U.dtNode.getParent() == null;
				IterPickable.Int it = U.edges;
				int uSize = dt.size(U.dtNode);

				while (U.excess > 0 && it.hasNext()) {
					int e = it.pickNext();
					Ref data = edgeRef.get(e);
					Vertex V = vertexData[g.edgeTarget(e)];
					int eAccess = data.cap - data.flow;

					if (!(eAccess > 0 && U.d == V.d + 1)) {
						/* edge is not admissible, just advance */
						it.nextInt();
						continue;
					}

					Vertex W;
					int vSize = dt.size(V.dtNode);
					if (uSize + vSize <= maxTreeSize) {
						/* Link u to a node with admissible edge and start pushing */
						dt.link(U.dtNode, V.dtNode, eAccess);
						U.edgeToParent = e;
						assert !children.hasNext(U.v) && !children.hasPrev(U.v);
						if (V.firstDtChild == -1) {
							V.firstDtChild = U.v;
						} else {
							children.insert(V.firstDtChild, U.v);
						}
						W = U;
					} else {
						/* Avoid big trees, no link, push manually and continue pushing in v's tree */
						int f = Math.min(U.excess, eAccess);
						pushFlow.accept(data, f);
						U.excess -= f;
						V.excess += f;
						if (V.v == source || V.v == sink)
							continue;
						assert V.excess > 0;
						W = V;
					}

					/* Continue as long as u has excess and it is not the root */
					while (W.excess > 0 && W.dtNode.getParent() != null) {
						/* Find the maximum flow that can be pushed */
						MinEdge minEdge = dt.findMinEdge(W.dtNode);
						int f = Math.min(W.excess, (int) minEdge.weight());

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
							if (minEdge.weight() > 0)
								break;
							Ref edgeData = edgeRef.get(minEdge.u().<Vertex>getNodeData().edgeToParent);
							updateFlow.accept(edgeData, minEdge.weight());
							cut.accept(minEdge.u().getNodeData());
						}
					}

					if (W.excess > 0 && !W.isActive) {
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
						for (IntIterator childIt = children.iterator(U.firstDtChild); childIt.hasNext();) {
							int child = childIt.nextInt();
							Vertex childData = vertexData[child];
							assert childData.dtNode.getParent() == U.dtNode;

							/* update flow */
							MinEdge m = dt.findMinEdge(childData.dtNode);
							Ref edgeData = edgeRef.get(m.u().<Vertex>getNodeData().edgeToParent);
							updateFlow.accept(edgeData, m.weight());

							/* cut child */
							toCut.enqueue(child);
						}
						while (!toCut.isEmpty())
							cut.accept(vertexData[toCut.dequeueInt()]);
					}
				}

				/* Update isActive and add to queue if active */
				if (U.isActive = (U.excess > 0))
					active.push(U);
			}

			/* Cleanup all the edges that stayed in the DT */
			Stack<DynamicTree.Node> cleanupStack = new Stack<>();
			for (int u = 0; u < n; u++) {
				for (DynamicTree.Node uDt = vertexData[u].dtNode,
						pDt; (pDt = uDt.getParent()) != null; uDt = pDt)
					cleanupStack.push(uDt);
				while (!cleanupStack.isEmpty()) {
					DynamicTree.Node uDt = cleanupStack.pop();
					assert uDt.getParent().getParent() == null;
					MinEdge m = dt.findMinEdge(uDt);
					Ref edgeData = edgeRef.get(m.u().<Vertex>getNodeData().edgeToParent);
					updateFlow.accept(edgeData, m.weight());
					dt.cut(m.u());
				}
			}

			/* Construct result */
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				Ref data = edgeRef.get(e);
				if (g.edgeSource(e) == gOring.edgeSource(data.orig))
					net.setFlow(data.orig, data.flow);
			}
			int totalFlow = 0;
			for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
				int e = eit.nextInt();
				Ref data = edgeRef.get(e);
				if (g.edgeSource(e) == gOring.edgeSource(data.orig))
					totalFlow += data.flow;
			}
			for (EdgeIter eit = g.edgesIn(source); eit.hasNext();) {
				int e = eit.nextInt();
				Ref data = edgeRef.get(e);
				if (g.edgeSource(e) == gOring.edgeSource(data.orig))
					totalFlow -= data.flow;
			}
			return totalFlow;
		}

		private static DiGraph referenceGraph(DiGraph g0, FlowNetwork.Int net) {
			DiGraph g = new GraphArrayDirected(g0.vertices().size());
			Weights<Ref> edgeRef = g.addEdgesWeights(EdgeRefWeightKey, Ref.class);
			for (IntIterator it = g0.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				int u = g0.edgeSource(e), v = g0.edgeTarget(e);
				Ref ref = new Ref(e, net.getCapacityInt(e), 0), refRev = new Ref(e, 0, 0);
				edgeRef.set(g.addEdge(u, v), ref);
				edgeRef.set(g.addEdge(v, u), refRev);
				refRev.rev = ref;
				ref.rev = refRev;
			}
			return g;
		}

		private static class Vertex {
			final int v;
			int excess;
			boolean isActive;
			int d;
			IterPickable.Int edges;

			final DynamicTree.Node dtNode;
			int firstDtChild;
			int edgeToParent = -1;

			Vertex(int v, DynamicTree.Node dtNode) {
				this.v = v;
				excess = 0;
				isActive = false;
				d = 0;
				edges = null;

				this.dtNode = dtNode;
				firstDtChild = -1;
			}
		}

		private static class Ref {

			final int orig;
			Ref rev;
			final int cap;
			int flow;

			Ref(int e, int cap, int flow) {
				orig = e;
				rev = null;
				this.cap = cap;
				this.flow = flow;
			}

			@Override
			public String toString() {
				return "R(" + orig + ")";
			}

		}
	}

}
