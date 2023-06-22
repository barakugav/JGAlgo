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

import java.util.Arrays;
import java.util.BitSet;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunction;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

abstract class MaximumFlowPushRelabelAbstract extends MaximumFlowAbstract implements MinimumCutSTUtils.AbstractImpl {

	abstract WorkerDouble newWorkerDouble(IndexGraph gOrig, FlowNetwork net, int source, int sink);

	abstract WorkerInt newWorkerInt(IndexGraph gOrig, FlowNetwork.Int net, int source, int sink);

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not directed
	 */
	@Override
	double computeMaximumFlow(IndexGraph g, FlowNetwork net, int source, int sink) {
		if (net instanceof FlowNetwork.Int) {
			return newWorkerInt(g, (FlowNetwork.Int) net, source, sink).computeMaxFlow();
		} else {
			return newWorkerDouble(g, net, source, sink).computeMaxFlow();
		}
	}

	@Override
	public Cut computeMinimumCut(IndexGraph g, WeightFunction w, int source, int sink) {
		if (w instanceof WeightFunction.Int) {
			WeightFunction.Int wInt = (WeightFunction.Int) w;
			FlowNetwork.Int net = new FlowNetwork.Int() {

				@Override
				public int getCapacityInt(int edge) {
					return wInt.weightInt(edge);
				}

				@Override
				public void setCapacity(int edge, int capacity) {
					throw new UnsupportedOperationException("Unimplemented method 'setCapacity'");
				}

				@Override
				public int getFlowInt(int edge) {
					throw new UnsupportedOperationException("Unimplemented method 'getFlowInt'");
				}

				@Override
				public void setFlow(int edge, int flow) {
					throw new UnsupportedOperationException("Unimplemented method 'setFlow'");
				}

			};
			return newWorkerInt(g, net, source, sink).computeMinimumCut();
		} else {
			FlowNetwork net = new FlowNetwork() {

				@Override
				public double getCapacity(int edge) {
					return w.weight(edge);
				}

				@Override
				public void setCapacity(int edge, double capacity) {
					throw new UnsupportedOperationException("Unimplemented method 'setCapacity'");
				}

				@Override
				public double getFlow(int edge) {
					throw new UnsupportedOperationException("Unimplemented method 'getFlow'");
				}

				@Override
				public void setFlow(int edge, double flow) {
					throw new UnsupportedOperationException("Unimplemented method 'setFlow'");
				}

			};
			return newWorkerDouble(g, net, source, sink).computeMinimumCut();
		}
	}

	static abstract class Worker extends MaximumFlowAbstract.Worker {

		final int[] label;
		final EdgeIter[] edgeIters;

		private final BitSet relabelVisited;
		private final IntPriorityQueue relabelQueue;
		private int relabelsSinceLastLabelsRecompute;
		private final int labelsReComputeThreshold;

		final LinkedListFixedSize.Doubly layersActive;
		final int[] layersHeadActive;
		int maxLayerActive;

		private final LinkedListFixedSize.Doubly layersInactive;
		private final int[] layersHeadInactive;
		private int maxLayerInactive;

		Worker(IndexGraph gOrig, FlowNetwork net, int source, int sink) {
			super(gOrig, net, source, sink);

			label = new int[n];
			edgeIters = new EdgeIter[n];

			relabelVisited = new BitSet(n);
			relabelQueue = new IntArrayFIFOQueue();
			labelsReComputeThreshold = n;

			layersActive = new LinkedListFixedSize.Doubly(n);
			layersInactive = new LinkedListFixedSize.Doubly(n);
			layersHeadActive = new int[n];
			layersHeadInactive = new int[n];
		}

		void recomputeLabels() {
			// Global labels heuristic
			// perform backward BFS from sink on edges with flow < capacity (residual)
			// perform another one from source to init unreachable vertices

			layersActive.clear();
			layersInactive.clear();
			Arrays.fill(layersHeadActive, LinkedListFixedSize.None);
			Arrays.fill(layersHeadInactive, LinkedListFixedSize.None);
			maxLayerActive = 0;
			maxLayerInactive = 0;

			BitSet visited = relabelVisited;
			IntPriorityQueue queue = relabelQueue;
			assert visited.isEmpty();
			assert queue.isEmpty();

			Arrays.fill(label, n);

			visited.set(sink);
			label[sink] = 0;
			visited.set(source);
			label[source] = n;
			queue.enqueue(sink);
			while (!queue.isEmpty()) {
				int v = queue.dequeueInt();
				int vLabel = label[v];
				for (EdgeIter eit = g.inEdges(v).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					if (!isResidual(e))
						continue;
					int u = eit.source();
					if (visited.get(u))
						continue;
					label[u] = vLabel + 1;
					onVertexLabelReCompute(u, label[u]);
					visited.set(u);
					queue.enqueue(u);
				}
			}
			visited.clear();

			relabelsSinceLastLabelsRecompute = 0;
		}

		void onVertexLabelReCompute(int u, int newLabel) {
			// reset edge iterator
			edgeIters[u] = g.outEdges(u).iterator();
			if (hasExcess(u))
				addToLayerActive(u, newLabel);
			else
				addToLayerInactive(u, newLabel);
		}

		void addToLayerActive(int u, int layer) {
			assert u != source && u != sink;
			if (layersHeadActive[layer] != LinkedListFixedSize.None)
				layersActive.connect(u, layersHeadActive[layer]);
			layersHeadActive[layer] = u;

			if (maxLayerActive < layer)
				maxLayerActive = layer;
		}

		void addToLayerInactive(int u, int layer) {
			assert u != source && u != sink;
			if (layersHeadInactive[layer] != LinkedListFixedSize.None)
				layersInactive.connect(u, layersHeadInactive[layer]);
			layersHeadInactive[layer] = u;

			if (maxLayerInactive < layer)
				maxLayerInactive = layer;
		}

		void removeFromLayerActive(int u, int layer) {
			if (layersHeadActive[layer] == u)
				layersHeadActive[layer] = layersActive.next(u);
			layersActive.disconnect(u);
		}

		void removeFromLayerInactive(int u, int layer) {
			if (layersHeadInactive[layer] == u)
				layersHeadInactive[layer] = layersInactive.next(u);
			layersInactive.disconnect(u);
		}

		void activate(int v) {
			int l = label[v];
			removeFromLayerInactive(v, l);
			addToLayerActive(v, l);
		}

		abstract void pushAsMuchFromSource();

		// abstract void push(int e, double f);

		void relabel(int u, int newLabel) {
			assert newLabel < n;
			int oldLabel = label[u];
			label[u] = newLabel;

			if (layersHeadActive[oldLabel] == LinkedListFixedSize.None
					&& layersHeadInactive[oldLabel] == LinkedListFixedSize.None) {
				emptyLayerGap(oldLabel);
				label[u] = n;
			}

			relabelsSinceLastLabelsRecompute++;
		}

		void emptyLayerGap(int emptyLayer) {
			// Gap heuristic
			// Set labels of all vertices in layers > emptyLayer to infinity (n)
			int maxLayer = this.maxLayerActive;
			for (int layer = emptyLayer + 1; layer <= maxLayer; layer++) {
				int head = layersHeadActive[layer];
				if (head == LinkedListFixedSize.None)
					continue;
				for (IntIterator it = layersActive.iterator(head); it.hasNext();) {
					int u = it.nextInt();
					layersActive.disconnect(u);
					label[u] = n;
				}
				layersHeadActive[layer] = LinkedListFixedSize.None;
			}
			this.maxLayerActive = emptyLayer - 1;

			maxLayer = this.maxLayerInactive;
			for (int layer = emptyLayer + 1; layer <= maxLayer; layer++) {
				int head = layersHeadInactive[layer];
				if (head == LinkedListFixedSize.None)
					continue;
				for (IntIterator it = layersInactive.iterator(head); it.hasNext();) {
					int u = it.nextInt();
					layersInactive.disconnect(u);
					label[u] = n;
				}
				layersHeadInactive[layer] = LinkedListFixedSize.None;
			}
			this.maxLayerInactive = emptyLayer - 1;
		}

		abstract void discharge(int u);

		abstract double constructResult();

		abstract boolean hasMoreVerticesToDischarge();

		abstract int nextVertexToDischarge();

		private void calcMaxPreflow() {
			recomputeLabels();
			pushAsMuchFromSource();
			while (hasMoreVerticesToDischarge()) {
				int u = nextVertexToDischarge();
				if (label[u] >= n)
					continue;
				assert hasExcess(u);
				removeFromLayerActive(u, label[u]);
				discharge(u);
				if (label[u] < n)
					addToLayerInactive(u, label[u]);

				if (relabelsSinceLastLabelsRecompute >= labelsReComputeThreshold)
					recomputeLabels();
			}
		}

		void convertPreflowToFlow() {
			final byte Unvisited = 0;
			final byte OnPath = 1;
			final byte Visited = 2;
			byte[] vState = new byte[n];

			// int[] parent = new int[n];
			int[] parent = label; // reuse array

			// int[] topoNext = new int[n];
			int[] topoNext = layersHeadActive; // reuse array
			int topoEnd = -1, topoBegin = -1;

			// reuse edgeIters array
			for (int u = 0; u < n; u++)
				edgeIters[u] = g.outEdges(u).iterator();

			for (int root = 0; root < n; root++) {
				if (vState[root] != Unvisited || !hasExcess(root) || root == source || root == sink)
					continue;
				vState[root] = OnPath;
				dfs: for (int u = root;;) {
					edgeIteration: for (; edgeIters[u].hasNext(); edgeIters[u].nextInt()) {
						int e = edgeIters[u].peekNext();
						if (!hasNegativeFlow(e))
							continue;
						int v = g.edgeTarget(e);
						if (vState[v] == Unvisited) {
							vState[v] = OnPath;
							parent[v] = u;
							u = v;
							continue dfs;
						}
						if (vState[v] == OnPath) {
							// cycle found, find the minimum flow on it
							// remove delta from all edges of the cycle
							eliminateCycle(e);

							// back out the DFS up to the first saturated edge
							int backOutTo = u;
							for (v = g.edgeTarget(edgeIters[u].peekNext()); v != u; v = g.edgeTarget(e)) {
								e = edgeIters[v].peekNext();
								if (vState[v] != Unvisited && hasNegativeFlow(e))
									continue;
								vState[g.edgeTarget(e)] = Unvisited;
								if (vState[v] != Unvisited)
									backOutTo = v;
							}
							if (backOutTo != u) {
								u = backOutTo;
								edgeIters[u].nextInt();
								break edgeIteration;
							}
						}
					} /* edgeIteration */

					if (!edgeIters[u].hasNext()) {
						// scan of u is complete
						vState[u] = Visited;
						if (u != source) {
							if (topoBegin == -1) {
								assert topoEnd == -1;
								topoBegin = topoEnd = u;
							} else {
								topoNext[u] = topoBegin;
								topoBegin = u;
							}
						}
						if (u == root)
							break dfs;
						u = parent[u];
						edgeIters[u].nextInt();
					}
				} /* DFS */
			} /* DFS roots */

			// All cycles were eliminated, and we calculated a topological order of the
			// vertices. Iterate over them using this order and return all excess flow to
			// source.
			if (topoBegin != -1)
				eliminateExcessWithTopologicalOrder(topoBegin, topoEnd, topoNext);
		}

		/* eliminated a cycle found during the DFS of convertPreflowToFlow */
		/* the cycle can be found by following e and edgeIters[u].peekNext() */
		abstract void eliminateCycle(int e);

		abstract void eliminateExcessWithTopologicalOrder(int topoBegin, int topoEnd, int[] topoNext);

		double computeMaxFlow() {
			// first phase
			calcMaxPreflow();
			// second phase
			convertPreflowToFlow();
			return constructResult();
		}

		Cut computeMinimumCut() {
			// first phase
			calcMaxPreflow();
			// no need for second phase
			// find the unreachable vertices from sink

			BitSet visited = relabelVisited;
			IntPriorityQueue queue = relabelQueue;
			assert visited.isEmpty();
			assert queue.isEmpty();

			visited.set(sink);
			queue.enqueue(sink);
			while (!queue.isEmpty()) {
				int v = queue.dequeueInt();
				for (EdgeIter eit = g.inEdges(v).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					if (!isResidual(e))
						continue;
					int u = eit.source();
					if (visited.get(u))
						continue;
					visited.set(u);
					queue.enqueue(u);
				}
			}
			assert !visited.get(source);
			IntList cut = new IntArrayList(n - visited.cardinality());
			for (int u = 0; u < n; u++)
				if (!visited.get(u))
					cut.add(u);
			visited.clear();
			return new CutImpl(gOrig, cut);
		}

		abstract boolean hasExcess(int u);

		abstract boolean isResidual(int e);

		abstract boolean isSaturated(int e);

		abstract boolean hasNegativeFlow(int e);

	}

	static abstract class WorkerDouble extends Worker {
		final double[] flow;
		final double[] capacity;

		final double[] excess;

		private static final double EPS = 0.0001;

		WorkerDouble(IndexGraph gOrig, FlowNetwork net, int source, int sink) {
			super(gOrig, net, source, sink);

			flow = new double[g.edges().size()];
			capacity = new double[g.edges().size()];
			initCapacitiesAndFlows(flow, capacity);

			excess = new double[n];
		}

		@Override
		void pushAsMuchFromSource() {
			for (EdgeIter eit = g.outEdges(source).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.target();
				double f = getResidualCapacity(e);
				if (f > 0 && label[source] > label[v]) {
					if (v != sink && !hasExcess(v))
						activate(v);
					push(e, f);
				}
			}
		}

		private void push(int e, double f) {
			assert f > 0;

			int t = twin[e];
			flow[e] += f;
			flow[t] -= f;
			assert flow[e] <= capacity[e] + EPS;
			assert flow[t] <= capacity[t] + EPS;

			int u = g.edgeSource(e), v = g.edgeTarget(e);
			excess[u] -= f;
			excess[v] += f;
		};

		@Override
		void discharge(int u) {
			for (EdgeIter it = edgeIters[u];;) {
				int e = it.peekNext();
				int v = g.edgeTarget(e);
				double eAccess = getResidualCapacity(e);
				if (eAccess > EPS && label[u] == label[v] + 1) {
					if (v != sink && !hasExcess(v))
						activate(v);

					// e is admissible, push
					if (excess[u] > eAccess) {
						// saturating push
						push(e, eAccess);
						// Due to floating points, need to check again we have something to push
						if (!hasExcess(u))
							return;
					} else {
						// non-saturating push
						push(e, excess[u]);
						assert !hasExcess(u);
						return;
					}
				}
				it.nextInt();
				if (!it.hasNext()) {
					// Finished iterating over all vertex edges.
					// relabel and Reset iterator
					relabel(u, label[u] + 1);
					if (label[u] >= n)
						break;
					it = edgeIters[u] = g.outEdges(u).iterator();
					assert it.hasNext();
				}
			}
		}

		@Override
		void eliminateCycle(int e) {
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			assert hasNegativeFlow(e);
			double f = -flow[e];
			for (;;) {
				e = edgeIters[v].peekNext();
				assert hasNegativeFlow(e);
				f = Math.min(f, -flow[e]);
				if (v == u)
					break;
				v = g.edgeTarget(e);
			}

			// remove delta from all edges of the cycle
			for (v = u;;) {
				e = edgeIters[v].peekNext();
				int t = twin[e];
				flow[e] += f;
				flow[t] -= f;
				v = g.edgeTarget(e);
				if (v == u)
					break;
			}
		}

		@Override
		void eliminateExcessWithTopologicalOrder(int topoBegin, int topoEnd, int[] topoNext) {
			for (int u = topoBegin;; u = topoNext[u]) {
				for (int e : g.outEdges(u)) {
					if (!hasExcess(u))
						break;
					double f = flow[e];
					if (f < 0)
						push(e, Math.min(excess[u], -f));
				}
				if (u == topoEnd)
					break;
			}
		}

		@Override
		double constructResult() {
			return constructResult(flow);
		}

		@Override
		boolean hasExcess(int u) {
			return excess[u] > EPS;
		}

		double getResidualCapacity(int e) {
			return capacity[e] - flow[e];
		}

		@Override
		boolean isResidual(int e) {
			return getResidualCapacity(e) > EPS;
		}

		@Override
		boolean isSaturated(int e) {
			return getResidualCapacity(e) <= EPS;
		}

		@Override
		boolean hasNegativeFlow(int e) {
			return flow[e] < 0;
		}
	}

	static abstract class WorkerInt extends Worker {
		final int[] flow;
		final int[] capacity;

		final int[] excess;

		WorkerInt(IndexGraph gOrig, FlowNetwork.Int net, int source, int sink) {
			super(gOrig, net, source, sink);

			flow = new int[g.edges().size()];
			capacity = new int[g.edges().size()];
			initCapacitiesAndFlows(flow, capacity);

			excess = new int[n];
		}

		@Override
		void pushAsMuchFromSource() {
			for (EdgeIter eit = g.outEdges(source).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.target();
				int f = getResidualCapacity(e);
				if (f > 0 && label[source] > label[v]) {
					if (v != sink && !hasExcess(v))
						activate(v);
					push(e, f);
				}
			}
		}

		private void push(int e, int f) {
			assert f > 0;

			int t = twin[e];
			flow[e] += f;
			flow[t] -= f;
			assert flow[e] <= capacity[e];
			assert flow[t] <= capacity[t];

			int u = g.edgeSource(e), v = g.edgeTarget(e);
			excess[u] -= f;
			excess[v] += f;
		}

		@Override
		void discharge(int u) {
			for (EdgeIter it = edgeIters[u];;) {
				int e = it.peekNext();
				int v = g.edgeTarget(e);
				int eAccess = getResidualCapacity(e);
				if (eAccess > 0 && label[u] == label[v] + 1) {
					if (v != sink && !hasExcess(v))
						activate(v);

					// e is admissible, push
					if (excess[u] > eAccess) {
						// saturating push
						push(e, eAccess);
					} else {
						// non-saturating push
						push(e, excess[u]);
						assert !hasExcess(u);
						return;
					}
				}
				it.nextInt();
				if (!it.hasNext()) {
					// Finished iterating over all vertex edges.
					// relabel and Reset iterator
					relabel(u, label[u] + 1);
					if (label[u] >= n)
						break;
					it = edgeIters[u] = g.outEdges(u).iterator();
					assert it.hasNext();
				}
			}
		}

		@Override
		void eliminateCycle(int e) {
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			assert hasNegativeFlow(e);
			int f = -flow[e];
			for (;;) {
				e = edgeIters[v].peekNext();
				assert hasNegativeFlow(e);
				f = Math.min(f, -flow[e]);
				if (v == u)
					break;
				v = g.edgeTarget(e);
			}

			// remove delta from all edges of the cycle
			for (v = u;;) {
				e = edgeIters[v].peekNext();
				int t = twin[e];
				flow[e] += f;
				flow[t] -= f;
				v = g.edgeTarget(e);
				if (v == u)
					break;
			}
		}

		@Override
		void eliminateExcessWithTopologicalOrder(int topoBegin, int topoEnd, int[] topoNext) {
			for (int u = topoBegin;; u = topoNext[u]) {
				for (int e : g.outEdges(u)) {
					if (!hasExcess(u))
						break;
					int f = flow[e];
					if (f < 0)
						push(e, Math.min(excess[u], -f));
				}
				if (u == topoEnd)
					break;
			}
		}

		@Override
		double constructResult() {
			return constructResult(flow);
		}

		@Override
		boolean hasExcess(int u) {
			return excess[u] > 0;
		}

		int getResidualCapacity(int e) {
			return capacity[e] - flow[e];
		}

		@Override
		boolean isResidual(int e) {
			return getResidualCapacity(e) > 0;
		}

		@Override
		boolean isSaturated(int e) {
			return getResidualCapacity(e) == 0;
		}

		@Override
		boolean hasNegativeFlow(int e) {
			return flow[e] < 0;
		}
	}

}
