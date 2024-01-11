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

package com.jgalgo.alg;

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.ds.DynamicTree;
import com.jgalgo.internal.ds.DynamicTree.MinEdge;
import com.jgalgo.internal.ds.DynamicTreeExtension;
import com.jgalgo.internal.ds.LinkedListFixedSize;
import com.jgalgo.internal.ds.QueueFixSize;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * The push relabel algorithm for maximum flow using dynamic trees.
 *
 * <p>
 * The push-relabel algorithm maintain a "preflow" and gradually converts it into a maximum flow by moving flow locally
 * between neighboring vertices using <i>push</i> operations under the guidance of an admissible network maintained by
 * <i>relabel</i> operations.
 *
 * <p>
 * Conceptually, the dynamic trees are used to push flow along multiple edges simultaneously. The current flow of each
 * individual edges is not maintained explicitly, rather each path is stored as a dynamic tree, and the flow is stored
 * as a weight of the tree edges - to calculate the weight (flow) of an edge, one would have to traverse the tree from
 * the root to the edge and sum all weights on the path.
 *
 * <p>
 * Using the dynamic trees reduce the running time of the push-relabel algorithm to \(O(m n \log (n^2 / m))\) and linear
 * space. This implementation uses FIFO to order the vertices to be examined. Note that this implementation is usually
 * out preformed in practice by simpler variants of the push-relabel algorithm, such as
 * {@link MaximumFlowPushRelabelHighestFirst}.
 *
 * @see    MaximumFlowPushRelabelFifo
 * @author Barak Ugav
 */
class MaximumFlowPushRelabelDynamicTrees extends MaximumFlows.WithResidualGraph {

	/**
	 * Create a new maximum flow algorithm object.
	 */
	MaximumFlowPushRelabelDynamicTrees() {}

	@Override
	public IFlow computeMaximumFlow(IndexGraph g, IWeightFunction capacity, int source, int sink) {
		if (WeightFunction.isInteger(capacity)) {
			return new WorkerInt(g, (IWeightFunctionInt) capacity, source, sink).computeMaxFlow();
		} else {
			return new WorkerDouble(g, capacity, source, sink).computeMaxFlow();
		}
	}

	@Override
	public IFlow computeMaximumFlow(IndexGraph g, IWeightFunction capacity, IntCollection sources,
			IntCollection sinks) {
		throw new UnsupportedOperationException("multi source/sink not supported");
	}

	private abstract static class AbstractWorker extends MaximumFlows.WithResidualGraph.Worker {

		final DynamicTree dt;
		final DynamicTreeExtension.TreeSize dtTreeSize;
		final int maxTreeSize;

		final QueueFixSize<Vertex> active;
		final Vertex[] vertexData;

		/* Data structure maintaining the children of each vertex in the DT */
		final LinkedListFixedSize.Doubly children;
		final IntPriorityQueue toCut = new FIFOQueueIntNoReduce();

		AbstractWorker(IndexGraph gOrig, IWeightFunction capacity, int source, int sink) {
			super(gOrig, capacity, source, sink);

			double maxWeight = getMaxCapacity();
			dt = DynamicTree
					.builder()
					.setMaxWeight(maxWeight * 10)
					.setIntWeights(this instanceof WorkerInt)
					.addExtension(DynamicTreeExtension.TreeSize.class)
					.build();
			dtTreeSize = dt.getExtension(DynamicTreeExtension.TreeSize.class);
			maxTreeSize = Math.max(1, n * n / g.edges().size());

			active = new QueueFixSize<>(n);
			vertexData = new Vertex[n];
			for (int u : range(n))
				vertexData[u] = newVertex(u, dt.makeTree());

			// set source and sink as 'active' to prevent them from entering the active
			// queue
			vertexData[source].isActive = true;
			vertexData[sink].isActive = true;

			children = new LinkedListFixedSize.Doubly(n);

			/* Init all vertices iterators */
			for (int u : range(n))
				vertexData(u).edgeIter = g.outEdges(u).iterator();
		}

		abstract Vertex newVertex(int v, DynamicTree.Vertex dtVertex);

		abstract double getMaxCapacity();

		void recomputeLabels() {
			// Global labels heuristic
			// perform backward BFS from sink on edges with flow < capacity (residual)
			// perform another one from source to init unreachable vertices

			Bitmap visited = new Bitmap(n);
			IntPriorityQueue queue = new FIFOQueueIntNoReduce();
			assert visited.isEmpty();
			assert queue.isEmpty();

			int n = g.vertices().size();
			for (int u : range(n))
				vertexData[u].label = n;

			visited.set(sink);
			vertexData[sink].label = 0;
			visited.set(source);
			vertexData[source].label = n;
			queue.enqueue(sink);
			while (!queue.isEmpty()) {
				int v = queue.dequeueInt();
				int vLabel = vertexData[v].label;
				for (IEdgeIter eit = g.inEdges(v).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					if (!isResidual(e))
						continue;
					int u = eit.sourceInt();
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

		IFlow computeMaxFlow() {
			/* Init all vertices distances */
			recomputeLabels();

			/* Push as much as possible from the source vertex */
			pushAsMuchFromSource();

			while (!active.isEmpty()) {
				Vertex U = active.pop();
				assert U.v != source && U.v != sink;
				assert U.dtVertex.getParent() == null;
				IEdgeIter it = U.edgeIter;
				int uSize = dtTreeSize.getTreeSize(U.dtVertex);

				while (hasExcess(U) && it.hasNext()) {
					int e = it.peekNextInt();
					Vertex V = vertexData(g.edgeTarget(e));

					if (!(isResidual(e) && U.label == V.label + 1)) {
						/* edge is not admissible, just advance */
						it.nextInt();
						continue;
					}

					Vertex W;
					int vSize = dtTreeSize.getTreeSize(V.dtVertex);
					if (uSize + vSize <= maxTreeSize) {
						/* Link u to a vertex with admissible edge and start pushing */
						dt.link(U.dtVertex, V.dtVertex, getResidualCapacity(e));
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
						assert hasExcess(V);
						W = V;
					}

					/* Continue as long as w has excess and it is not the root */
					while (hasExcess(W) && W.dtVertex.getParent() != null)
						pushAlongPath(W);

					if (hasExcess(W) && !W.isActive) {
						W.isActive = true;
						active.push(W);
					}
				}

				/* Finished iterating over all vertex edges */
				if (!it.hasNext()) {
					U.label++;
					U.edgeIter = g.outEdges(U.v).iterator();
					cutAllChildren(U);
				}

				/* Update isActive and add to queue if active */
				if (U.isActive = hasExcess(U))
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
			Vertex parent = U.dtVertex.getParent().getData();
			if (U.v == parent.firstDtChild)
				parent.firstDtChild = children.next(U.v);
			children.disconnect(U.v);

			/* Remove link from DT */
			dt.cut(U.dtVertex);
		}

		void cutAllChildren(Vertex U) {
			/* cut all vertices pointing into u */
			assert U.dtVertex.getParent() == null;
			if (U.firstDtChild != -1) {
				for (IntIterator childIt = children.iterator(U.firstDtChild); childIt.hasNext();) {
					int child = childIt.nextInt();
					Vertex childData = vertexData(child);
					assert childData.dtVertex.getParent() == U.dtVertex;

					/* update flow */
					MinEdge m = dt.findMinEdge(childData.dtVertex);
					int e = m.source().<Vertex>getData().linkedEdge;
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
			Stack<DynamicTree.Vertex> cleanupStack = new ObjectArrayList<>();
			for (int u : range(n)) {
				for (DynamicTree.Vertex uDt = vertexData(u).dtVertex, pDt; (pDt = uDt.getParent()) != null; uDt = pDt)
					cleanupStack.push(uDt);
				while (!cleanupStack.isEmpty()) {
					DynamicTree.Vertex uDt = cleanupStack.pop();
					assert uDt.getParent() == dt.findRoot(uDt);
					MinEdge m = dt.findMinEdge(uDt);
					int e = m.source().<Vertex>getData().linkedEdge;
					updateFlow(e, m.weight());
					dt.cut(m.source());
				}
			}
		}

		abstract IFlow constructResult();

		abstract double getResidualCapacity(int e);

		abstract boolean isResidual(int e);

		@SuppressWarnings("unchecked")
		<V extends Vertex> V vertexData(int v) {
			return (V) vertexData[v];
		}

		abstract static class Vertex {
			final int v;
			boolean isActive;
			int label;
			IEdgeIter edgeIter;

			final DynamicTree.Vertex dtVertex;
			int firstDtChild;
			int linkedEdge = -1;

			Vertex(int v, DynamicTree.Vertex dtVertex) {
				this.v = v;
				this.dtVertex = dtVertex;
				firstDtChild = -1;
				dtVertex.setData(this);
			}
		}

		abstract boolean hasExcess(Vertex v);

	}

	private static class WorkerDouble extends AbstractWorker {

		final double[] capacity;
		final double[] flow;

		private final double eps;

		WorkerDouble(IndexGraph gOrig, IWeightFunction capacityOrig, int source, int sink) {
			super(gOrig, capacityOrig, source, sink);

			flow = new double[g.edges().size()];
			capacity = new double[g.edges().size()];
			initCapacitiesAndFlows(flow, capacity);
			eps = Arrays.stream(capacity).filter(c -> c > 0).min().orElse(0) * 1e-8;
		}

		@Override
		Vertex newVertex(int v, DynamicTree.Vertex dtVertex) {
			return new Vertex(v, dtVertex);
		}

		@Override
		double getMaxCapacity() {
			double m = range(gOrig.edges().size()).mapToDouble(capacityOrig::weight).max().orElse(Double.MIN_VALUE);
			return Math.max(100, m);
		}

		private void pushFlow(int e, double f) {
			int t = twin[e];
			flow[e] += f;
			flow[t] -= f;
			assert flow[e] <= capacity[e] + eps;
			assert flow[t] <= capacity[t] + eps;
		}

		@Override
		void updateFlow(int e, double weight) {
			pushFlow(e, capacity[e] - flow[e] - weight);
		}

		@Override
		void pushAsMuchFromSource() {
			for (IEdgeIter eit = g.outEdges(source).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				double f = capacity[e] - flow[e];
				if (f > 0) {
					pushFlow(e, f);
					Vertex U = vertexData(eit.sourceInt()), V = vertexData(eit.targetInt());
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
			double eAccess = capacity[e] - flow[e];
			double f = Math.min(U.excess, eAccess);
			pushFlow(e, f);
			U.excess -= f;
			V.excess += f;
		}

		@Override
		void pushAlongPath(AbstractWorker.Vertex W0) {
			Vertex W = (Vertex) W0;
			/* Find the maximum flow that can be pushed */
			MinEdge minEdge = dt.findMinEdge(W.dtVertex);
			double f = Math.min(W.excess, minEdge.weight());

			/* Push from u up to u's tree root */
			dt.addWeight(W.dtVertex, -f);

			/* Update u's excess and u's tree root excess */
			Vertex wRoot = dt.findRoot(W.dtVertex).getData();
			W.excess -= f;
			wRoot.excess += f;
			if (!wRoot.isActive) {
				wRoot.isActive = true;
				active.push(wRoot);
			}

			/* Cut all saturated edges from u to u's tree root */
			while (W.dtVertex.getParent() != null) {
				minEdge = dt.findMinEdge(W.dtVertex);
				if (minEdge.weight() > eps)
					break;
				int minEdgeId = minEdge.source().<Vertex>getData().linkedEdge;
				updateFlow(minEdgeId, minEdge.weight());
				cut(minEdge.source().getData());
			}
		}

		@Override
		IFlow constructResult() {
			return constructResult(flow);
		}

		@Override
		double getResidualCapacity(int e) {
			return capacity[e] - flow[e];
		}

		@Override
		boolean isResidual(int e) {
			return getResidualCapacity(e) > eps;
		}

		private static class Vertex extends AbstractWorker.Vertex {
			double excess = 0;

			Vertex(int v, DynamicTree.Vertex dtVertex) {
				super(v, dtVertex);
			}
		}

		@Override
		boolean hasExcess(AbstractWorker.Vertex v) {
			return ((Vertex) v).excess > eps;
		}

	}

	private static class WorkerInt extends AbstractWorker {

		final int[] capacity;
		final int[] flow;

		WorkerInt(IndexGraph gOrig, IWeightFunctionInt capacityOrig, int source, int sink) {
			super(gOrig, capacityOrig, source, sink);

			flow = new int[g.edges().size()];
			capacity = new int[g.edges().size()];
			initCapacitiesAndFlows(flow, capacity);
		}

		@Override
		Vertex newVertex(int v, DynamicTree.Vertex dtVertex) {
			return new Vertex(v, dtVertex);
		}

		@Override
		double getMaxCapacity() {
			IWeightFunctionInt capacity = (IWeightFunctionInt) this.capacityOrig;
			int m = range(gOrig.edges().size()).map(capacity::weightInt).max().orElse(0);
			return Math.max(100, m);
		}

		private void pushFlow(int e, int f) {
			int t = twin[e];
			flow[e] += f;
			flow[t] -= f;
			assert flow[e] <= capacity[e];
			assert flow[t] <= capacity[t];
		}

		@Override
		void updateFlow(int e, double weight) {
			pushFlow(e, capacity[e] - flow[e] - (int) weight);
		}

		@Override
		void pushAsMuchFromSource() {
			for (IEdgeIter eit = g.outEdges(source).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int f = capacity[e] - flow[e];
				if (f > 0) {
					pushFlow(e, f);
					Vertex U = vertexData(eit.sourceInt()), V = vertexData(eit.targetInt());
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
			int eAccess = capacity[e] - flow[e];
			int f = Math.min(U.excess, eAccess);
			pushFlow(e, f);
			U.excess -= f;
			V.excess += f;
		}

		@Override
		void pushAlongPath(AbstractWorker.Vertex W0) {
			Vertex W = (Vertex) W0;
			/* Find the maximum flow that can be pushed */
			MinEdge minEdge = dt.findMinEdge(W.dtVertex);
			int f = Math.min(W.excess, (int) minEdge.weight());

			/* Push from u up to u's tree root */
			dt.addWeight(W.dtVertex, -f);

			/* Update u's excess and u's tree root excess */
			Vertex wRoot = dt.findRoot(W.dtVertex).getData();
			W.excess -= f;
			wRoot.excess += f;
			if (!wRoot.isActive) {
				wRoot.isActive = true;
				active.push(wRoot);
			}

			/* Cut all saturated edges from u to u's tree root */
			while (W.dtVertex.getParent() != null) {
				minEdge = dt.findMinEdge(W.dtVertex);
				if (minEdge.weight() > 0)
					break;
				int minEdgeId = minEdge.source().<Vertex>getData().linkedEdge;
				updateFlow(minEdgeId, minEdge.weight());
				cut(minEdge.source().getData());
			}
		}

		@Override
		IFlow constructResult() {
			return constructResult(flow);
		}

		@Override
		double getResidualCapacity(int e) {
			return capacity[e] - flow[e];
		}

		@Override
		boolean isResidual(int e) {
			return getResidualCapacity(e) > 0;
		}

		private static class Vertex extends AbstractWorker.Vertex {
			int excess = 0;

			Vertex(int v, DynamicTree.Vertex dtVertex) {
				super(v, dtVertex);
			}
		}

		@Override
		boolean hasExcess(AbstractWorker.Vertex v) {
			return ((Vertex) v).excess > 0;
		}
	}

}
