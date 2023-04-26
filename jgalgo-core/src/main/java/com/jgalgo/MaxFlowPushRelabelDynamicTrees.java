package com.jgalgo;

import java.util.BitSet;
import java.util.List;

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
	private static final Object EdgeRevWeightKey = new Object();
	private static final Object FlowWeightKey = new Object();
	private static final Object CapacityWeightKey = new Object();

	/**
	 * Create a new maximum flow algorithm object.
	 */
	public MaxFlowPushRelabelDynamicTrees() {
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

	private static abstract class AbstractWorker {

		final FlowNetwork net;
		final DiGraph gOrig;
		final int source;
		final int sink;

		final DiGraph g;
		final Weights.Int edgeRef;
		final Weights.Int twin;

		final DynamicTree dt;
		final DynamicTreeSplayExtension.TreeSize dtTreeSize;
		final int maxTreeSize;

		final QueueFixSize<Vertex> active;
		final Vertex[] vertexData;

		/* Data structure maintaining the children of each node in the DT */
		final LinkedListDoubleArrayFixedSize children;
		final IntPriorityQueue toCut = new IntArrayFIFOQueue();

		AbstractWorker(DiGraph gOrig, FlowNetwork net, int source, int sink) {
			if (source == sink)
				throw new IllegalArgumentException("Source and sink can't be the same vertex");
			this.gOrig = gOrig;
			this.net = net;
			this.source = source;
			this.sink = sink;

			g = new GraphArrayDirected(gOrig.vertices().size());
			edgeRef = g.addEdgesWeights(EdgeRefWeightKey, int.class, Integer.valueOf(-1));
			twin = g.addEdgesWeights(EdgeRevWeightKey, int.class, Integer.valueOf(-1));
			for (IntIterator it = gOrig.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				int u = gOrig.edgeSource(e), v = gOrig.edgeTarget(e);
				if (u == v || u == sink || v == source)
					continue;

				int e1 = g.addEdge(u, v);
				int e2 = g.addEdge(v, u);
				edgeRef.set(e1, e);
				edgeRef.set(e2, e);
				twin.set(e1, e2);
				twin.set(e2, e1);
			}

			int n = g.vertices().size();
			dtTreeSize = new DynamicTreeSplayExtension.TreeSize();
			dt = createDynamicTree(dtTreeSize);
			maxTreeSize = Math.max(1, n * n / g.edges().size());

			active = new QueueFixSize<>(n);
			vertexData = new Vertex[n];
			for (int u = 0; u < n; u++)
				vertexData[u] = newVertex(u, dt.makeTree());

			// set source and sink as 'active' to prevent them from entering the active
			// queue
			vertexData[source].isActive = true;
			vertexData[sink].isActive = true;

			children = LinkedListDoubleArrayFixedSize.newInstance(n);

			/* Init all vertices iterators */
			for (int u = 0; u < n; u++)
				vertexData(u).edgeIter = new IterPickable.Int(g.edgesOut(u));
		}

		abstract Vertex newVertex(int v, DynamicTree.Node dtNode);

		abstract DynamicTree createDynamicTree(DynamicTreeSplayExtension.TreeSize treeSizeExtension);

		void recomputeLabels() {
			// Global labels heuristic
			// perform backward BFS from sink on edges with flow < capacity (residual)
			// perform another one from source to init unreachable vertices

			BitSet visited = new BitSet();
			IntPriorityQueue queue = new IntArrayFIFOQueue();
			assert visited.isEmpty();
			assert queue.isEmpty();

			int n = g.vertices().size();
			for (int u = 0; u < n; u++)
				vertexData[u].label = n;

			visited.set(sink);
			vertexData[sink].label = 0;
			visited.set(source);
			vertexData[source].label = n;
			queue.enqueue(sink);
			while (!queue.isEmpty()) {
				int v = queue.dequeueInt();
				int vLabel = vertexData[v].label;
				for (EdgeIter eit = g.edgesIn(v); eit.hasNext();) {
					int e = eit.nextInt();
					if (!isResidual(e))
						continue;
					int u = eit.u();
					if (visited.get(u))
						continue;
					vertexData[u].label = vLabel + 1;
					// onVertexLabelReCompute(u, vertexData[u].label);
					visited.set(u);
					queue.enqueue(u);
				}
			}
			visited.clear();
		}

		double computeMaxFlow() {
			/* Init all vertices distances */
			recomputeLabels();

			/* Push as much as possible from the source vertex */
			pushAsMuchFromSource();

			while (!active.isEmpty()) {
				Vertex U = active.pop();
				assert U.v != source && U.v != sink;
				assert U.dtNode.getParent() == null;
				IterPickable.Int it = U.edgeIter;
				int uSize = dtTreeSize.getTreeSize(U.dtNode);

				while (U.hasExcess() && it.hasNext()) {
					int e = it.pickNext();
					Vertex V = vertexData(g.edgeTarget(e));

					if (!(isResidual(e) && U.label == V.label + 1)) {
						/* edge is not admissible, just advance */
						it.nextInt();
						continue;
					}

					Vertex W;
					int vSize = dtTreeSize.getTreeSize(V.dtNode);
					if (uSize + vSize <= maxTreeSize) {
						/* Link u to a node with admissible edge and start pushing */
						dt.link(U.dtNode, V.dtNode, getResidualCapacity(e));
						U.linkedEdge = e;
						assert !children.hasNext(U.v) && !children.hasPrev(U.v);
						if (V.firstDtChild != -1)
							children.connect(U.v, V.firstDtChild);
						V.firstDtChild = U.v;
						W = U;
					} else {
						/* Avoid big trees, no link, push manually and continue pushing in v's tree */
						pushAlongEdge(e);
						if (V.v == source || V.v == sink)
							continue;
						assert V.hasExcess();
						W = V;
					}

					/* Continue as long as w has excess and it is not the root */
					while (W.hasExcess() && W.dtNode.getParent() != null)
						pushAlongPath(W);

					if (W.hasExcess() && !W.isActive) {
						W.isActive = true;
						active.push(W);
					}
				}

				/* Finished iterating over all vertex edges */
				if (!it.hasNext()) {
					U.label++;
					U.edgeIter = new IterPickable.Int(g.edgesOut(U.v));
					cutAllChildren(U);
				}

				/* Update isActive and add to queue if active */
				if (U.isActive = U.hasExcess())
					active.push(U);
			}

			/* Cleanup all the edges that stayed in the DT */
			cleanAllDTEdges();

			return constructResult();
		}

		abstract void updateFlow(int e, double weight);

		abstract void pushAsMuchFromSource();

		abstract void pushAlongEdge(int e);

		abstract void pushAlongPath(Vertex W);

		void cut(Vertex U) {
			/* Remove vertex from parent children list */
			Vertex parent = U.dtNode.getParent().getNodeData();
			if (U.v == parent.firstDtChild)
				parent.firstDtChild = children.next(U.v);
			children.disconnect(U.v);

			/* Remove link from DT */
			dt.cut(U.dtNode);
		}

		void cutAllChildren(Vertex U) {
			/* cut all vertices pointing into u */
			assert U.dtNode.getParent() == null;
			if (U.firstDtChild != -1) {
				for (IntIterator childIt = children.iterator(U.firstDtChild); childIt.hasNext();) {
					int child = childIt.nextInt();
					Vertex childData = vertexData(child);
					assert childData.dtNode.getParent() == U.dtNode;

					/* update flow */
					MinEdge m = dt.findMinEdge(childData.dtNode);
					int e = m.u().<Vertex>getNodeData().linkedEdge;
					updateFlow(e, m.weight());

					/* cut child */
					toCut.enqueue(child);
				}
				while (!toCut.isEmpty())
					cut(vertexData(toCut.dequeueInt()));
			}
		}

		void cleanAllDTEdges() {
			/* Cleanup all the edges that stayed in the DT */
			int n = g.vertices().size();
			Stack<DynamicTree.Node> cleanupStack = new Stack<>();
			for (int u = 0; u < n; u++) {
				for (DynamicTree.Node uDt = vertexData(u).dtNode,
						pDt; (pDt = uDt.getParent()) != null; uDt = pDt)
					cleanupStack.push(uDt);
				while (!cleanupStack.isEmpty()) {
					DynamicTree.Node uDt = cleanupStack.pop();
					assert uDt.getParent() == dt.findRoot(uDt);
					MinEdge m = dt.findMinEdge(uDt);
					int e = m.u().<Vertex>getNodeData().linkedEdge;
					updateFlow(e, m.weight());
					dt.cut(m.u());
				}
			}
		}

		abstract double constructResult();

		abstract double getResidualCapacity(int e);

		abstract boolean isResidual(int e);

		boolean isOriginalEdge(int e) {
			return g.edgeSource(e) == gOrig.edgeSource(edgeRef.getInt(e));
		}

		@SuppressWarnings("unchecked")
		<V extends Vertex> V vertexData(int v) {
			return (V) vertexData[v];
		}

		static abstract class Vertex {
			final int v;
			boolean isActive;
			int label;
			IterPickable.Int edgeIter;

			final DynamicTree.Node dtNode;
			int firstDtChild;
			int linkedEdge = -1;

			Vertex(int v, DynamicTree.Node dtNode) {
				this.v = v;
				this.dtNode = dtNode;
				firstDtChild = -1;
				dtNode.setNodeData(this);
			}

			abstract boolean hasExcess();
		}

	}

	private static class WorkerDouble extends AbstractWorker {

		final Weights.Double capacity;
		final Weights.Double flow;

		private static final double EPS = 0.0001;

		WorkerDouble(DiGraph gOrig, FlowNetwork net, int source, int sink) {
			super(gOrig, net, source, sink);

			flow = g.addEdgesWeights(FlowWeightKey, double.class);
			capacity = g.addEdgesWeights(CapacityWeightKey, double.class);
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				flow.set(e, 0);
				capacity.set(e, isOriginalEdge(e) ? net.getCapacity(edgeRef.getInt(e)) : 0);
			}
		}

		@Override
		Vertex newVertex(int v, DynamicTree.Node dtNode) {
			return new Vertex(v, dtNode);
		}

		@Override
		DynamicTree createDynamicTree(DynamicTreeSplayExtension.TreeSize treeSizeExtension) {
			double maxCapacity = 100;
			for (IntIterator it = gOrig.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				maxCapacity = Math.max(maxCapacity, net.getCapacity(e));
			}
			return new DynamicTreeSplayExtended(maxCapacity * 10, List.of(treeSizeExtension));
		}

		private void pushFlow(int e, double f) {
			int t = twin.getInt(e);
			flow.set(e, flow.getDouble(e) + f);
			flow.set(t, flow.getDouble(t) - f);
			assert flow.getDouble(e) <= capacity.getDouble(e) + EPS;
			assert flow.getDouble(t) <= capacity.getDouble(t) + EPS;
		}

		@Override
		void updateFlow(int e, double weight) {
			pushFlow(e, capacity.getDouble(e) - flow.getDouble(e) - weight);
		}

		@Override
		void pushAsMuchFromSource() {
			for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
				int e = eit.nextInt();
				double f = capacity.getDouble(e) - flow.getDouble(e);
				if (f > 0) {
					pushFlow(e, f);
					Vertex U = vertexData(eit.u()), V = vertexData(eit.v());
					U.excess -= f;
					V.excess += f;
					if (!V.isActive) {
						V.isActive = true;
						active.push(V);
					}
				}
			}
		}

		@Override
		void pushAlongEdge(int e) {
			Vertex U = vertexData(g.edgeSource(e));
			Vertex V = vertexData(g.edgeTarget(e));
			double eAccess = capacity.getDouble(e) - flow.getDouble(e);
			double f = Math.min(U.excess, eAccess);
			pushFlow(e, f);
			U.excess -= f;
			V.excess += f;
		}

		@Override
		void pushAlongPath(AbstractWorker.Vertex W0) {
			Vertex W = (Vertex) W0;
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
				int minEdgeId = minEdge.u().<Vertex>getNodeData().linkedEdge;
				updateFlow(minEdgeId, minEdge.weight());
				cut(minEdge.u().getNodeData());
			}
		}

		@Override
		double constructResult() {
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				if (isOriginalEdge(e))
					net.setFlow(edgeRef.getInt(e), flow.getDouble(e));
			}
			double totalFlow = 0;
			for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
				int e = eit.nextInt();
				if (isOriginalEdge(e))
					totalFlow += flow.getDouble(e);
			}
			for (EdgeIter eit = g.edgesIn(source); eit.hasNext();) {
				int e = eit.nextInt();
				if (isOriginalEdge(e))
					totalFlow -= flow.getDouble(e);
			}
			return totalFlow;
		}

		@Override
		double getResidualCapacity(int e) {
			return capacity.getDouble(e) - flow.getDouble(e);
		}

		@Override
		boolean isResidual(int e) {
			return getResidualCapacity(e) > EPS;
		}

		private static class Vertex extends AbstractWorker.Vertex {
			double excess = 0;

			Vertex(int v, DynamicTree.Node dtNode) {
				super(v, dtNode);
			}

			@Override
			boolean hasExcess() {
				return excess > EPS;
			}
		}

	}

	private static class WorkerInt extends AbstractWorker {

		final Weights.Int capacity;
		final Weights.Int flow;

		WorkerInt(DiGraph gOrig, FlowNetwork.Int net, int source, int sink) {
			super(gOrig, net, source, sink);

			flow = g.addEdgesWeights(FlowWeightKey, int.class);
			capacity = g.addEdgesWeights(CapacityWeightKey, int.class);
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				flow.set(e, 0);
				capacity.set(e, isOriginalEdge(e) ? net.getCapacityInt(edgeRef.getInt(e)) : 0);
			}
		}

		@Override
		Vertex newVertex(int v, DynamicTree.Node dtNode) {
			return new Vertex(v, dtNode);
		}

		@Override
		DynamicTree createDynamicTree(DynamicTreeSplayExtension.TreeSize treeSizeExtension) {
			FlowNetwork.Int net = (FlowNetwork.Int) this.net;
			int maxCapacity = 100;
			for (IntIterator it = gOrig.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				maxCapacity = Math.max(maxCapacity, net.getCapacityInt(e));
			}
			return new DynamicTreeSplayIntExtended(maxCapacity * 10, List.of(treeSizeExtension));
		}

		private void pushFlow(int e, int f) {
			int t = twin.getInt(e);
			flow.set(e, flow.getInt(e) + f);
			flow.set(t, flow.getInt(t) - f);
			assert flow.getInt(e) <= capacity.getInt(e);
			assert flow.getInt(t) <= capacity.getInt(t);
		}

		@Override
		void updateFlow(int e, double weight) {
			pushFlow(e, capacity.getInt(e) - flow.getInt(e) - (int) weight);
		}

		@Override
		void pushAsMuchFromSource() {
			for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
				int e = eit.nextInt();
				int f = capacity.getInt(e) - flow.getInt(e);
				if (f > 0) {
					pushFlow(e, f);
					Vertex U = vertexData(eit.u()), V = vertexData(eit.v());
					U.excess -= f;
					V.excess += f;
					if (!V.isActive) {
						V.isActive = true;
						active.push(V);
					}
				}
			}
		}

		@Override
		void pushAlongEdge(int e) {
			Vertex U = vertexData(g.edgeSource(e));
			Vertex V = vertexData(g.edgeTarget(e));
			int eAccess = capacity.getInt(e) - flow.getInt(e);
			int f = Math.min(U.excess, eAccess);
			pushFlow(e, f);
			U.excess -= f;
			V.excess += f;
		}

		@Override
		void pushAlongPath(AbstractWorker.Vertex W0) {
			Vertex W = (Vertex) W0;
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
				int minEdgeId = minEdge.u().<Vertex>getNodeData().linkedEdge;
				updateFlow(minEdgeId, minEdge.weight());
				cut(minEdge.u().getNodeData());
			}
		}

		@Override
		double constructResult() {
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				if (isOriginalEdge(e))
					((FlowNetwork.Int) net).setFlow(edgeRef.getInt(e), flow.getInt(e));
			}
			int totalFlow = 0;
			for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
				int e = eit.nextInt();
				if (isOriginalEdge(e))
					totalFlow += flow.getInt(e);
			}
			for (EdgeIter eit = g.edgesIn(source); eit.hasNext();) {
				int e = eit.nextInt();
				if (isOriginalEdge(e))
					totalFlow -= flow.getInt(e);
			}
			return totalFlow;
		}

		@Override
		double getResidualCapacity(int e) {
			return capacity.getInt(e) - flow.getInt(e);
		}

		@Override
		boolean isResidual(int e) {
			return getResidualCapacity(e) > 0;
		}

		private static class Vertex extends AbstractWorker.Vertex {
			int excess = 0;

			Vertex(int v, DynamicTree.Node dtNode) {
				super(v, dtNode);
			}

			@Override
			boolean hasExcess() {
				return excess > 0;
			}
		}
	}

}