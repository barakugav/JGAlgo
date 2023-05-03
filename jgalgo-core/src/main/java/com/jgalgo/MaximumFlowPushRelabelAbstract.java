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
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

abstract class MaximumFlowPushRelabelAbstract implements MaximumFlow, MinimumCutST {
	private static final Object EdgeRefWeightKey = new Object();
	private static final Object EdgeRevWeightKey = new Object();
	private static final Object FlowWeightKey = new Object();
	private static final Object CapacityWeightKey = new Object();

	abstract WorkerDouble newWorkerDouble(DiGraph gOrig, FlowNetwork net, int source, int sink);

	abstract WorkerInt newWorkerInt(DiGraph gOrig, FlowNetwork.Int net, int source, int sink);

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
			return newWorkerInt((DiGraph) g, (FlowNetwork.Int) net, source, sink).computeMaxFlow();
		} else {
			return newWorkerDouble((DiGraph) g, net, source, sink).computeMaxFlow();
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not directed
	 */
	@Override
	public IntList computeMinimumCut(Graph g, EdgeWeightFunc w, int source, int sink) {
		if (!(g instanceof DiGraph))
			throw new IllegalArgumentException("only directed graphs are supported");
		if (w instanceof EdgeWeightFunc.Int) {
			EdgeWeightFunc.Int wInt = (EdgeWeightFunc.Int) w;
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
			return newWorkerInt((DiGraph) g, net, source, sink).computeMinimumCut();
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
			return newWorkerDouble((DiGraph) g, net, source, sink).computeMinimumCut();
		}
	}

	static abstract class Worker {
		final DiGraph g;
		final DiGraph gOrig;
		final int n;

		final FlowNetwork net;
		final int source;
		final int sink;

		final Weights.Int edgeRef;
		final Weights.Int twin;

		final int[] label;
		final EdgeIterImpl[] edgeIters;

		private final BitSet relabelVisited;
		private final IntPriorityQueue relabelQueue;
		private int relabelsSinceLastLabelsRecompute;
		private final int labelsReComputeThreshold;

		final LinkedListDoubleArrayFixedSize layersActive;
		final int[] layersHeadActive;
		int maxLayerActive;

		private final LinkedListDoubleArrayFixedSize layersInactive;
		private final int[] layersHeadInactive;
		private int maxLayerInactive;

		Worker(DiGraph gOrig, FlowNetwork net, int source, int sink) {
			if (source == sink)
				throw new IllegalArgumentException("Source and sink can't be the same vertex");
			this.gOrig = gOrig;
			this.net = net;
			this.source = source;
			this.sink = sink;

			n = gOrig.vertices().size();
			g = new GraphArrayDirected(n);
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

			label = new int[n];
			edgeIters = new EdgeIterImpl[n];

			relabelVisited = new BitSet(n);
			relabelQueue = new IntArrayFIFOQueue();
			labelsReComputeThreshold = n;

			layersActive = LinkedListDoubleArrayFixedSize.newInstance(n);
			layersInactive = LinkedListDoubleArrayFixedSize.newInstance(n);
			layersHeadActive = new int[n];
			layersHeadInactive = new int[n];
		}

		void recomputeLabels() {
			// Global labels heuristic
			// perform backward BFS from sink on edges with flow < capacity (residual)
			// perform another one from source to init unreachable vertices

			layersActive.clear();
			layersInactive.clear();
			Arrays.fill(layersHeadActive, LinkedListDoubleArrayFixedSize.None);
			Arrays.fill(layersHeadInactive, LinkedListDoubleArrayFixedSize.None);
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
				for (EdgeIter eit = g.edgesIn(v); eit.hasNext();) {
					int e = eit.nextInt();
					if (!isResidual(e))
						continue;
					int u = eit.u();
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
			edgeIters[u] = (EdgeIterImpl) g.edgesOut(u);
			if (hasExcess(u))
				addToLayerActive(u, newLabel);
			else
				addToLayerInactive(u, newLabel);
		}

		void addToLayerActive(int u, int layer) {
			assert u != source && u != sink;
			if (layersHeadActive[layer] != LinkedListDoubleArrayFixedSize.None)
				layersActive.connect(u, layersHeadActive[layer]);
			layersHeadActive[layer] = u;

			if (maxLayerActive < layer)
				maxLayerActive = layer;
		}

		void addToLayerInactive(int u, int layer) {
			assert u != source && u != sink;
			if (layersHeadInactive[layer] != LinkedListDoubleArrayFixedSize.None)
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

			if (layersHeadActive[oldLabel] == LinkedListDoubleArrayFixedSize.None
					&& layersHeadInactive[oldLabel] == LinkedListDoubleArrayFixedSize.None) {
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
				if (head == LinkedListDoubleArrayFixedSize.None)
					continue;
				for (IntIterator it = layersActive.iterator(head); it.hasNext();) {
					int u = it.nextInt();
					layersActive.disconnect(u);
					label[u] = n;
				}
				layersHeadActive[layer] = LinkedListDoubleArrayFixedSize.None;
			}
			this.maxLayerActive = emptyLayer - 1;

			maxLayer = this.maxLayerInactive;
			for (int layer = emptyLayer + 1; layer <= maxLayer; layer++) {
				int head = layersHeadInactive[layer];
				if (head == LinkedListDoubleArrayFixedSize.None)
					continue;
				for (IntIterator it = layersInactive.iterator(head); it.hasNext();) {
					int u = it.nextInt();
					layersInactive.disconnect(u);
					label[u] = n;
				}
				layersHeadInactive[layer] = LinkedListDoubleArrayFixedSize.None;
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
				edgeIters[u] = (EdgeIterImpl) g.edgesOut(u);

			for (int root = 0; root < n; root++) {
				if (vState[root] != Unvisited || !hasExcess(root) || root == source || root == sink)
					continue;
				vState[root] = OnPath;
				dfs: for (int u = root;;) {
					edgeIteration: for (; edgeIters[u].hasNext(); edgeIters[u].nextInt()) {
						int e = edgeIters[u].peekNext();
						if (isOriginalEdge(e) || !isResidual(e))
							continue;
						int v = g.edgeTarget(e);
						if (vState[v] == Unvisited) {
							vState[v] = OnPath;
							parent[v] = u;
							u = v;
							break edgeIteration;
						}
						if (vState[v] == OnPath) {
							// cycle found, find the minimum flow on it
							// remove delta from all edges of the cycle
							eliminateCycle(e);

							// back out the DFS up to the first saturated edge
							int backOutTo = u;
							for (v = g.edgeTarget(edgeIters[u].peekNext()); v != u; v = g.edgeTarget(e)) {
								e = edgeIters[v].peekNext();
								if (vState[v] != Unvisited && !isSaturated(e))
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

		IntList computeMinimumCut() {
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
				for (EdgeIter eit = g.edgesIn(v); eit.hasNext();) {
					int e = eit.nextInt();
					if (!isResidual(e))
						continue;
					int u = eit.u();
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
			return cut;
		}

		abstract boolean hasExcess(int u);

		abstract boolean isResidual(int e);

		abstract boolean isSaturated(int e);

		boolean isOriginalEdge(int e) {
			return g.edgeSource(e) == gOrig.edgeSource(edgeRef.getInt(e));
		}

	}

	static abstract class WorkerDouble extends Worker {
		final Weights.Double flow;
		final Weights.Double capacity;

		final double[] excess;

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

			excess = new double[n];
		}

		@Override
		void pushAsMuchFromSource() {
			for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.v();
				double f = getResidualCapacity(e);
				if (f > 0 && label[source] > label[v]) {
					if (v != sink && !hasExcess(v))
						activate(v);
					push(e, f);
				}
			}
		}

		private void push0(int e, double f) {
			assert f > 0;

			int rev = twin.getInt(e);
			flow.set(e, flow.getDouble(e) + f);
			flow.set(rev, flow.getDouble(rev) - f);
			assert flow.getDouble(e) <= capacity.getDouble(e) + EPS;
			assert flow.getDouble(rev) <= capacity.getDouble(rev) + EPS;

			int u = g.edgeSource(e), v = g.edgeTarget(e);
			excess[u] -= f;
			excess[v] += f;
		};

		void push(int e, double f) {
			push0(e, f);
		}

		@Override
		void discharge(int u) {
			for (EdgeIterImpl it = edgeIters[u];;) {
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
					it = edgeIters[u] = (EdgeIterImpl) g.edgesOut(u);
					assert it.hasNext();
				}
			}
		}

		@Override
		void eliminateCycle(int e) {
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			double delta = getResidualCapacity(e);
			for (;;) {
				e = edgeIters[v].peekNext();
				delta = Math.min(delta, getResidualCapacity(e));
				if (v == u)
					break;
				v = g.edgeTarget(e);
			}

			// remove delta from all edges of the cycle
			for (v = u;;) {
				e = edgeIters[v].peekNext();
				int eTwin = twin.getInt(e);
				flow.set(e, flow.getDouble(e) + delta);
				flow.set(eTwin, flow.getDouble(eTwin) - delta);
				v = g.edgeTarget(e);
				if (v == u)
					break;
			}
		}

		@Override
		void eliminateExcessWithTopologicalOrder(int topoBegin, int topoEnd, int[] topoNext) {
			for (int u = topoBegin;; u = topoNext[u]) {
				for (EdgeIter eit = g.edgesOut(u); hasExcess(u) && eit.hasNext();) {
					int e = eit.nextInt();
					if (!isOriginalEdge(e) && isResidual(e)) {
						double f = Math.min(excess[u], getResidualCapacity(e));
						push0(e, f);
					}
				}
				if (u == topoEnd)
					break;
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
		boolean hasExcess(int u) {
			return excess[u] > EPS;
		}

		double getResidualCapacity(int e) {
			return capacity.getDouble(e) - flow.getDouble(e);
		}

		@Override
		boolean isResidual(int e) {
			return getResidualCapacity(e) > EPS;
		}

		@Override
		boolean isSaturated(int e) {
			return getResidualCapacity(e) <= EPS;
		}
	}

	static abstract class WorkerInt extends Worker {
		final Weights.Int flow;
		final Weights.Int capacity;

		final int[] excess;

		WorkerInt(DiGraph gOrig, FlowNetwork.Int net, int source, int sink) {
			super(gOrig, net, source, sink);

			flow = g.addEdgesWeights(FlowWeightKey, int.class);
			capacity = g.addEdgesWeights(CapacityWeightKey, int.class);
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				flow.set(e, 0);
				capacity.set(e, isOriginalEdge(e) ? net.getCapacityInt(edgeRef.getInt(e)) : 0);
			}

			excess = new int[n];
		}

		@Override
		void pushAsMuchFromSource() {
			for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.v();
				int f = getResidualCapacity(e);
				if (f > 0 && label[source] > label[v]) {
					if (v != sink && !hasExcess(v))
						activate(v);
					push(e, f);
				}
			}
		}

		private void push0(int e, int f) {
			assert f > 0;

			int rev = twin.getInt(e);
			flow.set(e, flow.getInt(e) + f);
			flow.set(rev, flow.getInt(rev) - f);
			assert flow.getInt(e) <= capacity.getInt(e);
			assert flow.getInt(rev) <= capacity.getInt(rev);

			int u = g.edgeSource(e), v = g.edgeTarget(e);
			excess[u] -= f;
			excess[v] += f;
		}

		void push(int e, int f) {
			push0(e, f);
		};

		@Override
		void discharge(int u) {
			for (EdgeIterImpl it = edgeIters[u];;) {
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
					it = edgeIters[u] = (EdgeIterImpl) g.edgesOut(u);
					assert it.hasNext();
				}
			}
		}

		@Override
		void eliminateCycle(int e) {
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			int delta = getResidualCapacity(e);
			for (;;) {
				e = edgeIters[v].peekNext();
				delta = Math.min(delta, getResidualCapacity(e));
				if (v == u)
					break;
				v = g.edgeTarget(e);
			}

			// remove delta from all edges of the cycle
			for (v = u;;) {
				e = edgeIters[v].peekNext();
				int eTwin = twin.getInt(e);
				flow.set(e, flow.getInt(e) + delta);
				flow.set(eTwin, flow.getInt(eTwin) - delta);
				v = g.edgeTarget(e);
				if (v == u)
					break;
			}
		}

		@Override
		void eliminateExcessWithTopologicalOrder(int topoBegin, int topoEnd, int[] topoNext) {
			for (int u = topoBegin;; u = topoNext[u]) {
				for (EdgeIter eit = g.edgesOut(u); hasExcess(u) && eit.hasNext();) {
					int e = eit.nextInt();
					if (!isOriginalEdge(e) && isResidual(e)) {
						int f = Math.min(excess[u], getResidualCapacity(e));
						push0(e, f);
					}
				}
				if (u == topoEnd)
					break;
			}
		}

		@Override
		double constructResult() {
			FlowNetwork.Int net = (FlowNetwork.Int) this.net;
			for (IntIterator it = g.edges().iterator(); it.hasNext();) {
				int e = it.nextInt();
				if (isOriginalEdge(e))
					net.setFlow(edgeRef.getInt(e), flow.getInt(e));
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
		boolean hasExcess(int u) {
			return excess[u] > 0;
		}

		int getResidualCapacity(int e) {
			return capacity.getInt(e) - flow.getInt(e);
		}

		@Override
		boolean isResidual(int e) {
			return getResidualCapacity(e) > 0;
		}

		@Override
		boolean isSaturated(int e) {
			return getResidualCapacity(e) == 0;
		}
	}

}
