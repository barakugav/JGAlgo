package com.jgalgo;

import java.util.Arrays;
import java.util.BitSet;

import com.jgalgo.Utils.IterPickable;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

class MaximumFlowPushRelabelAbstract {
	private static final Object EdgeRefWeightKey = new Object();
	private static final Object EdgeRevWeightKey = new Object();
	private static final Object FlowWeightKey = new Object();
	private static final Object CapacityWeightKey = new Object();

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
		final IterPickable.Int[] edgeIters;

		private final BitSet relabelVisited;
		private final IntPriorityQueue relabelQueue;
		private int relabelsSinceLastLabelsRecompute;
		private final int labelsReComputeThreshold;

		private final LinkedListDoubleArrayFixedSize layers;
		final int[] layersHead;
		private int maxLayer;
		// private int minLayer;

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
			edgeIters = new IterPickable.Int[n];

			relabelVisited = new BitSet(n);
			relabelQueue = new IntArrayFIFOQueue();
			labelsReComputeThreshold = n;

			layers = LinkedListDoubleArrayFixedSize.newInstance(n);
			layersHead = new int[n];
		}

		void recomputeLabels() {
			// Global labels heuristic
			// perform backward BFS from sink on edges with flow < capacity (residual)
			// perform another one from source to init unreachable vertices

			layers.clear();
			Arrays.fill(layersHead, LinkedListDoubleArrayFixedSize.None);
			maxLayer = 0;
			// minLayer = n;

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
			edgeIters[u] = new IterPickable.Int(g.edgesOut(u));
			addToLayer(u, newLabel);
		}

		void addToLayer(int u, int layer) {
			if (layersHead[layer] != LinkedListDoubleArrayFixedSize.None)
				layers.connect(u, layersHead[layer]);
			layersHead[layer] = u;

			if (maxLayer < layer)
				maxLayer = layer;
			// if (minLayer > layer)
			// minLayer = layer;
		}

		void removeFromLayer(int u, int layer) {
			if (layersHead[layer] == u)
				layersHead[layer] = layers.next(u);
			layers.disconnect(u);
		}

		abstract void pushAsMuchFromSource();

		// abstract void push(int e, double f);

		void relabel(int u, int newLabel) {
			assert newLabel < n;
			int oldLabel = label[u];
			label[u] = newLabel;

			removeFromLayer(u, oldLabel);
			addToLayer(u, newLabel);
			if (layersHead[oldLabel] == LinkedListDoubleArrayFixedSize.None)
				emptyLayerGap(oldLabel);

			relabelsSinceLastLabelsRecompute++;
		}

		void emptyLayerGap(int emptyLayer) {
			// Gap heuristic
			// Set labels of all vertices in layers > emptyLayer to infinity (n)
			int maxLayer = this.maxLayer;
			for (int layer = emptyLayer + 1; layer <= maxLayer; layer++) {
				int head = layersHead[layer];
				if (head == LinkedListDoubleArrayFixedSize.None)
					continue;
				for (IntIterator it = layers.iterator(head); it.hasNext();) {
					int u = it.nextInt();
					layers.disconnect(u);
					label[u] = n;
				}
				layersHead[layer] = LinkedListDoubleArrayFixedSize.None;
			}
			this.maxLayer = emptyLayer - 1;
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
				discharge(u);
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
			int[] topoNext = layersHead; // reuse array
			int topoEnd = -1, topoBegin = -1;

			// reuse edgeIters array
			for (int u = 0; u < n; u++)
				edgeIters[u] = new IterPickable.Int(g.edgesOut(u));

			for (int root = 0; root < n; root++) {
				if (vState[root] != Unvisited || !hasExcess(root) || root == source || root == sink)
					continue;
				vState[root] = OnPath;
				dfs: for (int u = root;;) {
					edgeIteration: for (; edgeIters[u].hasNext(); edgeIters[u].nextInt()) {
						int e = edgeIters[u].pickNext();
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
							for (v = g.edgeTarget(edgeIters[u].pickNext()); v != u; v = g.edgeTarget(e)) {
								e = edgeIters[v].pickNext();
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
		/* the cycle can be found by following e and edgeIters[u].pickNext() */
		abstract void eliminateCycle(int e);

		abstract void eliminateExcessWithTopologicalOrder(int topoBegin, int topoEnd, int[] topoNext);

		double computeMaxFlow() {
			// first phase
			calcMaxPreflow();
			// second phase
			convertPreflowToFlow();
			return constructResult();
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
				double f = getResidualCapacity(e);
				if (f > 0 && label[source] > label[g.edgeTarget(e)])
					push(e, f);
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
			if (!hasExcess(u))
				return;
			for (IterPickable.Int it = edgeIters[u];;) {
				int e = it.pickNext();
				double eAccess = getResidualCapacity(e);
				if (eAccess > EPS && label[u] == label[g.edgeTarget(e)] + 1) {
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
					it = edgeIters[u] = new IterPickable.Int(g.edgesOut(u));
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
				e = edgeIters[v].pickNext();
				delta = Math.min(delta, getResidualCapacity(e));
				if (v == u)
					break;
				v = g.edgeTarget(e);
			}

			// remove delta from all edges of the cycle
			for (v = u;;) {
				e = edgeIters[v].pickNext();
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
				int f = getResidualCapacity(e);
				if (f > 0 && label[source] > label[g.edgeTarget(e)])
					push(e, f);
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
			if (!hasExcess(u))
				return;
			for (IterPickable.Int it = edgeIters[u];;) {
				int e = it.pickNext();
				int eAccess = getResidualCapacity(e);
				if (eAccess > 0 && label[u] == label[g.edgeTarget(e)] + 1) {
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
					it = edgeIters[u] = new IterPickable.Int(g.edgesOut(u));
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
				e = edgeIters[v].pickNext();
				delta = Math.min(delta, getResidualCapacity(e));
				if (v == u)
					break;
				v = g.edgeTarget(e);
			}

			// remove delta from all edges of the cycle
			for (v = u;;) {
				e = edgeIters[v].pickNext();
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
