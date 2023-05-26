/*-
 * Copyright 2023 Barak Ugav
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.jgalgo;

import java.util.BitSet;
import com.jgalgo.DynamicTree.MinEdge;
import com.jgalgo.Utils.QueueFixSize;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * The push relabel algorithm for maximum flow using dynamic trees.
 * <p>
 * The push-relabel algorithm maintain a "preflow" and gradually converts it into a maximum flow by moving flow locally
 * between neighboring nodes using <i>push</i> operations under the guidance of an admissible network maintained by
 * <i>relabel</i> operations.
 * <p>
 * Conceptually, the dynamic trees are used to push flow along multiple edges simultaneously. The current flow of each
 * individual edges is not maintained explicitly, rather each path is stored as a dynamic tree, and the flow is stored
 * as a weight of the tree edges - to calculate the weight (flow) of an edge, one would have to traverse the tree from
 * the root to the edge and sum all weights on the path.
 * <p>
 * Using the dynamic trees reduce the running time of the push-relabel algorithm to \(O(m n \log (n^2 / m))\) and linear
 * space. This implementation uses FIFO to order the vertices to be examined. Note that this implementation is usually
 * out preformed in practice by simpler variants of the push-relabel algorithm, such as
 * {@link MaximumFlowPushRelabelHighestFirst}.
 *
 * @see    MaximumFlowPushRelabel
 * @author Barak Ugav
 */
public class MaximumFlowPushRelabelDynamicTrees implements MaximumFlow {

	private static final Object FlowWeightKey = new Utils.Obj("flow");
	private static final Object CapacityWeightKey = new Utils.Obj("capacity");

	/**
	 * Create a new maximum flow algorithm object.
	 */
	public MaximumFlowPushRelabelDynamicTrees() {}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not directed
	 */
	@Override
	public double computeMaximumFlow(Graph g, FlowNetwork net, int source, int sink) {
		if (net instanceof FlowNetwork.Int) {
			return new WorkerInt(g, (FlowNetwork.Int) net, source, sink).computeMaxFlow();
		} else {
			return new WorkerDouble(g, net, source, sink).computeMaxFlow();
		}
	}

	private static abstract class AbstractWorker extends MaximumFlowAbstract.Worker {

		final DynamicTree dt;
		final DynamicTreeExtension.TreeSize dtTreeSize;
		final int maxTreeSize;

		final QueueFixSize<Vertex> active;
		final Vertex[] vertexData;

		/* Data structure maintaining the children of each node in the DT */
		final LinkedListFixedSize.Doubly children;
		final IntPriorityQueue toCut = new IntArrayFIFOQueue();

		AbstractWorker(Graph gOrig, FlowNetwork net, int source, int sink) {
			super(gOrig, net, source, sink);

			double maxWeight = getMaxWeight();
			dt = DynamicTree.newBuilder().setMaxWeight(maxWeight * 10).setIntWeights(this instanceof WorkerInt)
					.addExtension(DynamicTreeExtension.TreeSize.class).build();
			dtTreeSize = dt.getExtension(DynamicTreeExtension.TreeSize.class);
			maxTreeSize = Math.max(1, n * n / g.edges().size());

			active = new QueueFixSize<>(n);
			vertexData = new Vertex[n];
			for (int u = 0; u < n; u++)
				vertexData[u] = newVertex(u, dt.makeTree());

			// set source and sink as 'active' to prevent them from entering the active
			// queue
			vertexData[source].isActive = true;
			vertexData[sink].isActive = true;

			children = new LinkedListFixedSize.Doubly(n);

			/* Init all vertices iterators */
			for (int u = 0; u < n; u++)
				vertexData(u).edgeIter = (EdgeIterImpl) g.edgesOut(u);
		}

		abstract Vertex newVertex(int v, DynamicTree.Node dtNode);

		abstract double getMaxWeight();

		void recomputeLabels() {
			// Global labels heuristic
			// perform backward BFS from sink on edges with flow < capacity (residual)
			// perform another one from source to init unreachable vertices

			BitSet visited = new BitSet(n);
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
					int u = eit.source();
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
				EdgeIterImpl it = U.edgeIter;
				int uSize = dtTreeSize.getTreeSize(U.dtNode);

				while (U.hasExcess() && it.hasNext()) {
					int e = it.peekNext();
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
						// Avoid big trees, no link, push manually and continue pushing in v's tree
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
					U.edgeIter = (EdgeIterImpl) g.edgesOut(U.v);
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
					int e = m.source().<Vertex>getNodeData().linkedEdge;
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
			Stack<DynamicTree.Node> cleanupStack = new ObjectArrayList<>();
			for (int u = 0; u < n; u++) {
				for (DynamicTree.Node uDt = vertexData(u).dtNode, pDt; (pDt = uDt.getParent()) != null; uDt = pDt)
					cleanupStack.push(uDt);
				while (!cleanupStack.isEmpty()) {
					DynamicTree.Node uDt = cleanupStack.pop();
					assert uDt.getParent() == dt.findRoot(uDt);
					MinEdge m = dt.findMinEdge(uDt);
					int e = m.source().<Vertex>getNodeData().linkedEdge;
					updateFlow(e, m.weight());
					dt.cut(m.source());
				}
			}
		}

		abstract double constructResult();

		abstract double getResidualCapacity(int e);

		abstract boolean isResidual(int e);

		@SuppressWarnings("unchecked")
		<V extends Vertex> V vertexData(int v) {
			return (V) vertexData[v];
		}

		static abstract class Vertex {
			final int v;
			boolean isActive;
			int label;
			EdgeIterImpl edgeIter;

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

		WorkerDouble(Graph gOrig, FlowNetwork net, int source, int sink) {
			super(gOrig, net, source, sink);

			flow = g.addEdgesWeights(FlowWeightKey, double.class);
			capacity = g.addEdgesWeights(CapacityWeightKey, double.class);
			initCapacitiesAndFlows(flow, capacity);
		}

		@Override
		Vertex newVertex(int v, DynamicTree.Node dtNode) {
			return new Vertex(v, dtNode);
		}

		@Override
		double getMaxWeight() {
			double maxCapacity = 100;
			for (IntIterator it = gOrig.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				maxCapacity = Math.max(maxCapacity, net.getCapacity(e));
			}
			return maxCapacity;
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
					Vertex U = vertexData(eit.source()), V = vertexData(eit.target());
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
				int minEdgeId = minEdge.source().<Vertex>getNodeData().linkedEdge;
				updateFlow(minEdgeId, minEdge.weight());
				cut(minEdge.source().getNodeData());
			}
		}

		@Override
		double constructResult() {
			return constructResult(flow);
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

		WorkerInt(Graph gOrig, FlowNetwork.Int net, int source, int sink) {
			super(gOrig, net, source, sink);

			flow = g.addEdgesWeights(FlowWeightKey, int.class);
			capacity = g.addEdgesWeights(CapacityWeightKey, int.class);
			initCapacitiesAndFlows(flow, capacity);
		}

		@Override
		Vertex newVertex(int v, DynamicTree.Node dtNode) {
			return new Vertex(v, dtNode);
		}

		@Override
		double getMaxWeight() {
			FlowNetwork.Int net = (FlowNetwork.Int) this.net;
			int maxCapacity = 100;
			for (IntIterator it = gOrig.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				maxCapacity = Math.max(maxCapacity, net.getCapacityInt(e));
			}
			return maxCapacity;
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
					Vertex U = vertexData(eit.source()), V = vertexData(eit.target());
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
				int minEdgeId = minEdge.source().<Vertex>getNodeData().linkedEdge;
				updateFlow(minEdgeId, minEdge.weight());
				cut(minEdge.source().getNodeData());
			}
		}

		@Override
		double constructResult() {
			return constructResult(flow);
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
