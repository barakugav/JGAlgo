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
import java.util.Objects;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.data.LinkedListFixedSize;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

/**
 * The push-relabel maximum flow algorithm with FIFO ordering.
 * <p>
 * The push-relabel algorithm maintain a "preflow" and gradually converts it into a maximum flow by moving flow locally
 * between neighboring nodes using <i>push</i> operations under the guidance of an admissible network maintained by
 * <i>relabel</i> operations.
 * <p>
 * Different variants of the push relabel algorithm exists, mostly differing in the order the vertices with excess (more
 * in-going than out-going flow) are examined. The implementation support four different such ordering:
 * <ul>
 * <li>FIFO - active vertices are examined by the order they became active. Using this ordering achieve \(O(n^3)\) time
 * complexity.</li>
 * <li>Highest First - active vertices are examined in decreasing order of their label. Using this ordering achieve
 * \(O(n^2 \sqrt{m})\) time complexity.</li>
 * <li>Lowest First - active vertices are examined in increasing order of their label. Using this ordering achieve
 * \(O(n^2 m)\) time complexity.</li>
 * <li>Move To Front - the active vertices are stored in a linked list, and the next examined vertex is the list head.
 * When an active vertex is relabeled it is moved to the list head. Using this ordering achieve \(O(n^3)\) time
 * complexity.</li>
 * </ul>
 * In addition to the order of the active vertices, the algorithm can be implemented in by pushing from an active vertex
 * to one of its neighbor vertices, namely by pushing along a single edge, or by pushing from an active vertex along a
 * path of admissable edges.
 * <p>
 * Heuristics are crucial for the practical running time of push-relabel algorithm, and this implementation uses the
 * 'global relabeling', 'gap' and 'incremental restart' (optional) heuristics.
 * <p>
 * This algorithm can be implemented with better time theoretical bound using dynamic trees, but in practice it has
 * little to non advantages. See {@link MaximumFlowPushRelabelDynamicTrees}.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Push%E2%80%93relabel_maximum_flow_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
class MaximumFlowPushRelabel extends MaximumFlowAbstract.WithoutResidualGraph {

	private static enum ActiveOrderPolicy {
		FIFO, HighestFirst, LowestFirst, MoveToFront;
	}

	private static enum DischargePolicy {
		SingleStep,

		/**
		 * Based on 'The Partial Augment–Relabel Algorithm for the Maximum Flow Problem' by Andrew V. Goldberg.
		 */
		PartialAugment
	}

	private final ActiveOrderPolicy activeOrderPolicy;
	private final DischargePolicy dischargePolicy;

	private MaximumFlowPushRelabel(ActiveOrderPolicy activeOrderPolicy, DischargePolicy dischargePolicy) {
		this.activeOrderPolicy = Objects.requireNonNull(activeOrderPolicy);
		this.dischargePolicy = Objects.requireNonNull(dischargePolicy);
	}

	static MaximumFlowPushRelabel newInstanceFifo() {
		return new MaximumFlowPushRelabel(ActiveOrderPolicy.FIFO, DischargePolicy.SingleStep);
	}

	static MaximumFlowPushRelabel newInstanceHighestFirst() {
		return new MaximumFlowPushRelabel(ActiveOrderPolicy.HighestFirst, DischargePolicy.SingleStep);
	}

	static MaximumFlowPushRelabel newInstanceLowestFirst() {
		return new MaximumFlowPushRelabel(ActiveOrderPolicy.LowestFirst, DischargePolicy.SingleStep);
	}

	static MaximumFlowPushRelabel newInstanceMoveToFront() {
		return new MaximumFlowPushRelabel(ActiveOrderPolicy.MoveToFront, DischargePolicy.SingleStep);
	}

	static MaximumFlowPushRelabel newInstancePartialAugment() {
		return new MaximumFlowPushRelabel(ActiveOrderPolicy.HighestFirst, DischargePolicy.PartialAugment);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not directed
	 */
	@Override
	double computeMaximumFlow(IndexGraph g, FlowNetwork net, int source, int sink) {
		if (net instanceof FlowNetwork.Int) {
			return new WorkerInt(g, (FlowNetwork.Int) net, source, sink, activeOrderPolicy, dischargePolicy)
					.computeMaxFlow();
		} else {
			return new WorkerDouble(g, net, source, sink, activeOrderPolicy, dischargePolicy).computeMaxFlow();
		}
	}

	@Override
	public Cut computeMinimumCut(IndexGraph g, WeightFunction w, int source, int sink) {
		FlowNetwork net = flowNetFromEdgeWeights(w);
		if (w instanceof WeightFunction.Int) {
			return new WorkerInt(g, (FlowNetwork.Int) net, source, sink, activeOrderPolicy, dischargePolicy)
					.computeMinimumCut();
		} else {
			return new WorkerDouble(g, net, source, sink, activeOrderPolicy, dischargePolicy).computeMinimumCut();
		}
	}

	private static FlowNetwork flowNetFromEdgeWeights(WeightFunction w) {
		if (w instanceof WeightFunction.Int) {
			WeightFunction.Int wInt = (WeightFunction.Int) w;
			FlowNetwork.Int net = new FlowNetwork.Int() {

				@Override
				public int getCapacityInt(int edge) {
					return wInt.weightInt(edge);
				}

				@Override
				public void setCapacity(int edge, int capacity) {
					throw new UnsupportedOperationException();
				}

				@Override
				public int getFlowInt(int edge) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void setFlow(int edge, int flow) {
					throw new UnsupportedOperationException();
				}

			};
			return net;
		} else {
			FlowNetwork net = new FlowNetwork() {

				@Override
				public double getCapacity(int edge) {
					return w.weight(edge);
				}

				@Override
				public void setCapacity(int edge, double capacity) {
					throw new UnsupportedOperationException();
				}

				@Override
				public double getFlow(int edge) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void setFlow(int edge, double flow) {
					throw new UnsupportedOperationException();
				}

			};
			return net;
		}
	}

	static abstract class Worker extends MaximumFlowAbstract.WithoutResidualGraph.Worker {

		final int[] label;
		final EdgeIter[] outEdgeIters;
		final EdgeIter[] inEdgeIters;

		private final BitSet relabelVisited;
		private final IntPriorityQueue relabelQueue;
		private int relabelsSinceLastLabelsRecompute;
		private final int labelsReComputeThreshold;

		private final LinkedListFixedSize.Doubly layers;
		final int[] layersHeadActive;
		private final int[] layersHeadInactive;
		int maxLayerActive;
		private int maxLayerInactive;

		private static final boolean HeuristicIncrementalRestart = Boolean.parseBoolean("false");
		private int minTouchedLayer;

		private final ActiveOrderPolicyImpl activeOrderPolicy;
		private final DischargePolicyImpl dischargePolicy;

		Worker(IndexGraph gOrig, FlowNetwork net, int source, int sink, ActiveOrderPolicy activeOrderPolicy,
				DischargePolicy dischargePolicy) {
			super(gOrig, net, source, sink);

			label = new int[n];
			outEdgeIters = new EdgeIter[n];
			inEdgeIters = directed ? new EdgeIter[n] : null;

			relabelVisited = new BitSet(n);
			relabelQueue = new FIFOQueueIntNoReduce();
			labelsReComputeThreshold = n;

			layers = new LinkedListFixedSize.Doubly(n);
			layersHeadActive = new int[n];
			layersHeadInactive = new int[n];

			this.activeOrderPolicy = ActiveOrderPolicyImpl.newInstance(this, activeOrderPolicy);
			this.dischargePolicy = DischargePolicyImpl.newInstance(this, dischargePolicy);
		}

		void recomputeLabels() {
			// Global labels heuristic
			// perform backward BFS from sink on edges with flow < capacity (residual)
			// perform another one from source to init unreachable vertices

			activeOrderPolicy.beforeRecomputeLabels();

			layers.clear();
			Arrays.fill(layersHeadActive, LinkedListFixedSize.None);
			Arrays.fill(layersHeadInactive, LinkedListFixedSize.None);
			maxLayerActive = 0;
			maxLayerInactive = 0;

			BitSet visited = relabelVisited;
			IntPriorityQueue queue = relabelQueue;
			assert visited.isEmpty();
			assert queue.isEmpty();

			if (HeuristicIncrementalRestart && minTouchedLayer >= 2) {
				assert minTouchedLayer != n;
				for (int u = 0; u < n; u++) {
					int l = label[u];
					if (l < minTouchedLayer) {
						if (u == sink)
							continue;
						onVertexLabelReCompute(u, l);
						visited.set(u);
						if (l == minTouchedLayer - 1)
							queue.enqueue(u);

					} else {
						label[u] = n;
					}
				}
				assert !queue.isEmpty();
			} else {
				Arrays.fill(label, n);
				label[sink] = 0;
				queue.enqueue(sink);
			}
			if (HeuristicIncrementalRestart)
				minTouchedLayer = n;

			// label[sink] = 0;
			assert label[sink] == 0;
			visited.set(sink);
			// label[source] = n;
			assert label[source] == n;
			visited.set(source);

			if (directed) {
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
						outEdgeIters[u] = g.outEdges(u).iterator();
						inEdgeIters[u] = g.inEdges(u).iterator();
						onVertexLabelReCompute(u, label[u]);
						visited.set(u);
						queue.enqueue(u);
					}
					for (EdgeIter eit = g.outEdges(v).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						if (!hasFlow(e))
							continue;
						int u = eit.target();
						if (visited.get(u))
							continue;
						label[u] = vLabel + 1;
						outEdgeIters[u] = g.outEdges(u).iterator();
						inEdgeIters[u] = g.inEdges(u).iterator();
						onVertexLabelReCompute(u, label[u]);
						visited.set(u);
						queue.enqueue(u);
					}
				}
			} else {
				while (!queue.isEmpty()) {
					int v = queue.dequeueInt();
					int vLabel = label[v];
					for (int e : g.outEdges(v)) {
						int u;
						if (v == g.edgeSource(e)) {
							if (!isTwinResidualUndirected(e))
								continue;
							u = g.edgeTarget(e);
						} else {
							assert v == g.edgeTarget(e);
							if (!isResidual(e))
								continue;
							u = g.edgeSource(e);
						}
						if (visited.get(u))
							continue;
						label[u] = vLabel + 1;
						outEdgeIters[u] = g.outEdges(u).iterator();
						onVertexLabelReCompute(u, label[u]);
						visited.set(u);
						queue.enqueue(u);
					}
				}
			}
			visited.clear();

			relabelsSinceLastLabelsRecompute = 0;
			activeOrderPolicy.afterRecomputeLabels();
		}

		void touchVertex(int v) {
			if (HeuristicIncrementalRestart && minTouchedLayer > label[v])
				minTouchedLayer = label[v];
		}

		void touchLayer(int layer) {
			if (HeuristicIncrementalRestart && minTouchedLayer > layer)
				minTouchedLayer = layer;
		}

		void onVertexLabelReCompute(int u, int newLabel) {
			if (hasExcess(u))
				addToLayerActive(u, newLabel);
			else
				addToLayerInactive(u, newLabel);
			activeOrderPolicy.afterVertexLabelReCompute(u);
		}

		void addToLayerActive(int u, int layer) {
			assert u != source && u != sink;
			if (layersHeadActive[layer] != LinkedListFixedSize.None)
				layers.connect(u, layersHeadActive[layer]);
			layersHeadActive[layer] = u;

			if (maxLayerActive < layer)
				maxLayerActive = layer;
			touchLayer(layer);
		}

		void addToLayerInactive(int u, int layer) {
			assert u != source && u != sink;
			if (layersHeadInactive[layer] != LinkedListFixedSize.None)
				layers.connect(u, layersHeadInactive[layer]);
			layersHeadInactive[layer] = u;

			if (maxLayerInactive < layer)
				maxLayerInactive = layer;
			touchLayer(layer);
		}

		void removeFromLayerActive(int u, int layer) {
			if (layersHeadActive[layer] == u)
				layersHeadActive[layer] = layers.next(u);
			layers.disconnect(u);
			touchLayer(layer);
		}

		void removeFromLayerInactive(int u, int layer) {
			if (layersHeadInactive[layer] == u)
				layersHeadInactive[layer] = layers.next(u);
			layers.disconnect(u);
			touchLayer(layer);
		}

		void activate(int v) {
			assert v != source && v != sink;
			int l = label[v];
			removeFromLayerInactive(v, l);
			addToLayerActive(v, l);
			activeOrderPolicy.afterActivate(v);
		}

		void deactivate(int v) {
			assert v != source && v != sink;
			int l = label[v];
			removeFromLayerActive(v, l);
			addToLayerInactive(v, l);
		}

		abstract void pushAsMuchFromSource();

		// abstract void push(int e, double f);

		void relabel(int u, int newLabel) {
			assert newLabel < n;
			int oldLabel = label[u];
			assert oldLabel < newLabel;
			label[u] = newLabel;
			touchLayer(oldLabel);

			if (layersHeadActive[oldLabel] == LinkedListFixedSize.None
					&& layersHeadInactive[oldLabel] == LinkedListFixedSize.None) {
				emptyLayerGap(oldLabel);
				label[u] = n;
			}

			relabelsSinceLastLabelsRecompute++;

			activeOrderPolicy.afterRelabel(u);
		}

		void emptyLayerGap(int emptyLayer) {
			// Gap heuristic
			// Set labels of all vertices in layers > emptyLayer to infinity (n)
			for (int layer = emptyLayer + 1; layer <= maxLayerActive; layer++) {
				int head = layersHeadActive[layer];
				if (head == LinkedListFixedSize.None)
					continue;
				for (IntIterator it = layers.iterator(head); it.hasNext();) {
					int u = it.nextInt();
					layers.disconnect(u);
					label[u] = n;
				}
				layersHeadActive[layer] = LinkedListFixedSize.None;
			}
			maxLayerActive = emptyLayer - 1;

			for (int layer = emptyLayer + 1; layer <= maxLayerInactive; layer++) {
				int head = layersHeadInactive[layer];
				if (head == LinkedListFixedSize.None)
					continue;
				for (IntIterator it = layers.iterator(head); it.hasNext();) {
					int u = it.nextInt();
					layers.disconnect(u);
					label[u] = n;
				}
				layersHeadInactive[layer] = LinkedListFixedSize.None;
			}
			maxLayerInactive = emptyLayer - 1;
		}

		private static abstract class DischargePolicyImpl {

			abstract void dischargeDirected(int u);

			abstract void dischargeUndirected(int u);

			static DischargePolicyImpl newInstance(MaximumFlowPushRelabel.Worker worker, DischargePolicy policy) {
				assert worker instanceof MaximumFlowPushRelabel.WorkerInt
						|| worker instanceof MaximumFlowPushRelabel.WorkerDouble;
				boolean isInt = worker instanceof MaximumFlowPushRelabel.WorkerInt;
				switch (policy) {
					case SingleStep:
						if (isInt) {
							return new DischargePolicyImpl.SingleStepInt((MaximumFlowPushRelabel.WorkerInt) worker);
						} else {
							return new DischargePolicyImpl.SingleStepDouble(
									(MaximumFlowPushRelabel.WorkerDouble) worker);
						}
					case PartialAugment:
						if (isInt) {
							return new DischargePolicyImpl.PartialAugmentInt((MaximumFlowPushRelabel.WorkerInt) worker);
						} else {
							return new DischargePolicyImpl.PartialAugmentDouble(
									(MaximumFlowPushRelabel.WorkerDouble) worker);
						}
					default:
						throw new IllegalArgumentException("unknown active policy: " + policy);
				}
			}

			final MaximumFlowPushRelabel.Worker worker;

			DischargePolicyImpl(MaximumFlowPushRelabel.Worker worker) {
				this.worker = Objects.requireNonNull(worker);
			}

			private static class SingleStepInt extends DischargePolicyImpl {

				SingleStepInt(MaximumFlowPushRelabel.WorkerInt worker) {
					super(worker);
				}

				MaximumFlowPushRelabel.WorkerInt worker() {
					return (MaximumFlowPushRelabel.WorkerInt) worker;
				}

				@Override
				void dischargeDirected(int u) {
					for (;;) {
						for (EdgeIter it = worker.outEdgeIters[u]; it.hasNext(); it.nextInt()) {
							int e = it.peekNext();
							int v = worker.g.edgeTarget(e);
							int eAccess = worker().getResidualCapacity(e);
							if (eAccess > 0 && worker.label[u] == worker.label[v] + 1) {
								if (v != worker.sink && !worker.hasExcess(v))
									worker.activate(v);

								// e is admissible, push
								if (worker().excess[u] > eAccess) {
									// saturating push
									worker().push(e, eAccess);
								} else {
									// non-saturating push
									worker().push(e, worker().excess[u]);
									assert !worker.hasExcess(u);
									return;
								}
							}
						}
						for (EdgeIter it = worker.inEdgeIters[u]; it.hasNext(); it.nextInt()) {
							int e = it.peekNext();
							int v = worker.g.edgeSource(e);
							int eAccess = worker().flow(e);
							if (eAccess > 0 && worker.label[u] == worker.label[v] + 1) {
								if (v != worker.sink && !worker.hasExcess(v))
									worker.activate(v);

								// e is admissible, push
								if (worker().excess[u] > eAccess) {
									// saturating push
									worker().push(e, -eAccess);
								} else {
									// non-saturating push
									worker().push(e, -worker().excess[u]);
									assert !worker.hasExcess(u);
									return;
								}
							}
						}
						// Finished iterating over all vertex edges.
						// relabel and Reset iterator
						worker.relabel(u, worker.label[u] + 1);
						if (worker.label[u] >= worker.n)
							break;
						worker.outEdgeIters[u] = worker.g.outEdges(u).iterator();
						worker.inEdgeIters[u] = worker.g.inEdges(u).iterator();
					}
				}

				@Override
				void dischargeUndirected(int u) {
					for (EdgeIter it = worker.outEdgeIters[u];;) {
						int e = it.peekNext();
						if (u == worker.g.edgeSource(e)) {
							int v = worker.g.edgeTarget(e);
							int eAccess = worker().getResidualCapacity(e);
							if (eAccess > 0 && worker.label[u] == worker.label[v] + 1) {
								if (v != worker.sink && !worker.hasExcess(v))
									worker.activate(v);

								// e is admissible, push
								if (worker().excess[u] > eAccess) {
									// saturating push
									worker().push(e, eAccess);
								} else {
									// non-saturating push
									worker().push(e, worker().excess[u]);
									assert !worker.hasExcess(u);
									return;
								}
							}
						} else {
							assert u == worker.g.edgeTarget(e);
							int v = worker.g.edgeSource(e);
							int eAccess = worker().getTwinResidualCapacity(e);
							assert eAccess >= 0;
							if (eAccess > 0 && worker.label[u] == worker.label[v] + 1) {
								if (v != worker.sink && !worker.hasExcess(v))
									worker.activate(v);

								// e is admissible, push
								if (worker().excess[u] > eAccess) {
									// saturating push
									worker().push(e, -eAccess);
								} else {
									// non-saturating push
									worker().push(e, -worker().excess[u]);
									assert !worker.hasExcess(u);
									return;
								}
							}
						}
						it.nextInt();
						if (!it.hasNext()) {
							// Finished iterating over all vertex edges.
							// relabel and Reset iterator
							worker.relabel(u, worker.label[u] + 1);
							if (worker.label[u] >= worker.n)
								break;
							it = worker.outEdgeIters[u] = worker.g.outEdges(u).iterator();
							assert it.hasNext();
						}
					}
				}

			}

			private static class SingleStepDouble extends DischargePolicyImpl {

				SingleStepDouble(MaximumFlowPushRelabel.WorkerDouble worker) {
					super(worker);
				}

				MaximumFlowPushRelabel.WorkerDouble worker() {
					return (MaximumFlowPushRelabel.WorkerDouble) worker;
				}

				@Override
				void dischargeDirected(int u) {
					for (;;) {
						for (EdgeIter it = worker.outEdgeIters[u]; it.hasNext(); it.nextInt()) {
							int e = it.peekNext();
							int v = worker.g.edgeTarget(e);
							double eAccess = worker().getResidualCapacity(e);
							if (eAccess > WorkerDouble.EPS && worker.label[u] == worker.label[v] + 1) {
								if (v != worker.sink && !worker.hasExcess(v))
									worker.activate(v);

								// e is admissible, push
								if (worker().excess[u] > eAccess) {
									// saturating push
									worker().push(e, eAccess);
									// Due to floating points, need to check again we have something to push
									if (!worker.hasExcess(u))
										return;
								} else {
									// non-saturating push
									worker().push(e, worker().excess[u]);
									assert !worker.hasExcess(u);
									return;
								}
							}
						}
						for (EdgeIter it = worker.inEdgeIters[u]; it.hasNext(); it.nextInt()) {
							int e = it.peekNext();
							int v = worker.g.edgeSource(e);
							double eAccess = worker().flow(e);
							if (eAccess > WorkerDouble.EPS && worker.label[u] == worker.label[v] + 1) {
								if (v != worker.sink && !worker.hasExcess(v))
									worker.activate(v);

								// e is admissible, push
								if (worker().excess[u] > eAccess) {
									// saturating push
									worker().push(e, -eAccess);
									// Due to floating points, need to check again we have something to push
									if (!worker.hasExcess(u))
										return;
								} else {
									// non-saturating push
									worker().push(e, -worker().excess[u]);
									assert !worker.hasExcess(u);
									return;
								}
							}
						}
						// Finished iterating over all vertex edges.
						// relabel and Reset iterator
						worker.relabel(u, worker.label[u] + 1);
						if (worker.label[u] >= worker.n)
							break;
						worker.outEdgeIters[u] = worker.g.outEdges(u).iterator();
						worker.inEdgeIters[u] = worker.g.inEdges(u).iterator();
					}
				}

				@Override
				void dischargeUndirected(int u) {
					for (EdgeIter it = worker.outEdgeIters[u];;) {
						int e = it.peekNext();
						if (u == worker.g.edgeSource(e)) {
							int v = worker.g.edgeTarget(e);
							double eAccess = worker().getResidualCapacity(e);
							if (eAccess > WorkerDouble.EPS && worker.label[u] == worker.label[v] + 1) {
								if (v != worker.sink && !worker.hasExcess(v))
									worker.activate(v);

								// e is admissible, push
								if (worker().excess[u] > eAccess) {
									// saturating push
									worker().push(e, eAccess);
									// Due to floating points, need to check again we have something to push
									if (!worker.hasExcess(u))
										return;
								} else {
									// non-saturating push
									worker().push(e, worker().excess[u]);
									assert !worker.hasExcess(u);
									return;
								}
							}
						} else {
							int v = worker.g.edgeSource(e);
							double eAccess = worker().capacity[e] + worker().flow(e);
							if (eAccess > WorkerDouble.EPS && worker.label[u] == worker.label[v] + 1) {
								if (v != worker.sink && !worker.hasExcess(v))
									worker.activate(v);

								// e is admissible, push
								if (worker().excess[u] > eAccess) {
									// saturating push
									worker().push(e, -eAccess);
									// Due to floating points, need to check again we have something to push
									if (!worker.hasExcess(u))
										return;
								} else {
									// non-saturating push
									worker().push(e, -worker().excess[u]);
									assert !worker.hasExcess(u);
									return;
								}
							}
						}
						it.nextInt();
						if (!it.hasNext()) {
							// Finished iterating over all vertex edges.
							// relabel and Reset iterator
							worker.relabel(u, worker.label[u] + 1);
							if (worker.label[u] >= worker.n)
								break;
							it = worker.outEdgeIters[u] = worker.g.outEdges(u).iterator();
							assert it.hasNext();
						}
					}
				}
			}

			private static abstract class PartialAugmentBase extends DischargePolicyImpl {

				static final int MAX_AUGMENT_PATH_LENGTH = 4;
				final IntArrayList path = new IntArrayList(MAX_AUGMENT_PATH_LENGTH);

				PartialAugmentBase(MaximumFlowPushRelabel.Worker worker) {
					super(worker);
				}

				@Override
				void dischargeDirected(int searchSource) {
					assert worker.hasExcess(searchSource);
					assert path.isEmpty();

					dfs: for (int u = searchSource;;) {
						int uLabel = worker.label[u];

						for (EdgeIter it = worker.outEdgeIters[u]; it.hasNext(); it.nextInt()) {
							int e = it.peekNext();
							int v = worker.g.edgeTarget(e);
							boolean isAdmissible = worker.isResidual(e) && uLabel == worker.label[v] + 1;
							if (isAdmissible) {
								path.add(e);
								if (path.size() == MAX_AUGMENT_PATH_LENGTH || v == worker.sink) {
									if (v != worker.sink && !worker.hasExcess(v))
										worker.activate(v);

									/* push along the admissible path */
									assert !path.isEmpty();
									int firstNonResidual = pushOnPathDirected(searchSource);

									/* If the 'source' does not have any excess, we are done */
									if (!worker.hasExcess(searchSource)) {
										path.clear();
										return;
									}

									/* back up in the DFS tree until all edges in the path are admissible */
									path.removeElements(firstNonResidual, path.size());
									// assert path.intStream().allMatch(worker::isResidual);
									if (path.isEmpty()) {
										u = searchSource;
									} else {
										int lastEdge = path.getInt(path.size() - 1);
										int u1 = worker.g.edgeSource(lastEdge);
										int u2 = worker.g.edgeTarget(lastEdge);
										u = worker.label[u1] < worker.label[u2] ? u1 : u2;
									}
								} else {
									/* advance in the DFS */
									u = v;
								}
								continue dfs;
							}
						}
						for (EdgeIter it = worker.inEdgeIters[u]; it.hasNext(); it.nextInt()) {
							int e = it.peekNext();
							int v = worker.g.edgeSource(e);
							boolean isAdmissible = worker.hasFlow(e) && uLabel == worker.label[v] + 1;
							if (isAdmissible) {
								path.add(e);
								if (path.size() == MAX_AUGMENT_PATH_LENGTH || v == worker.sink) {
									if (v != worker.sink && !worker.hasExcess(v))
										worker.activate(v);

									/* push along the admissible path */
									assert !path.isEmpty();
									int firstNonResidual = pushOnPathDirected(searchSource);

									/* If the 'source' does not have any excess, we are done */
									if (!worker.hasExcess(searchSource)) {
										path.clear();
										return;
									}

									/* back up in the DFS tree until all edges in the path are admissible */
									path.removeElements(firstNonResidual, path.size());
									// assert path.intStream().allMatch(worker::isResidual);
									if (path.isEmpty()) {
										u = searchSource;
									} else {
										int lastEdge = path.getInt(path.size() - 1);
										int u1 = worker.g.edgeSource(lastEdge);
										int u2 = worker.g.edgeTarget(lastEdge);
										u = worker.label[u1] < worker.label[u2] ? u1 : u2;
									}
								} else {
									/* advance in the DFS */
									u = v;
								}
								continue dfs;
							}
						}

						// Finished iterating over all vertex edges.
						// relabel and Reset iterator
						boolean isSearchSource = u == searchSource, hasExcess = true;
						if (!isSearchSource) {
							hasExcess = worker.hasExcess(u);
							if (hasExcess) {
								worker.removeFromLayerActive(u, uLabel);
							} else {
								worker.removeFromLayerInactive(u, uLabel);
							}
						}

						worker.relabel(u, uLabel + 1);
						if ((uLabel = worker.label[u]) >= worker.n) {
							/*
							 * empty layer gap heuristic was activated, all vertices on the path have greater or equal
							 * label as u, all are unreachable from sink
							 */
							path.clear();
							return;
						}

						if (!isSearchSource) {
							if (hasExcess) {
								worker.addToLayerActive(u, uLabel);
							} else {
								worker.addToLayerInactive(u, uLabel);
							}
						}

						worker.outEdgeIters[u] = worker.g.outEdges(u).iterator();
						worker.inEdgeIters[u] = worker.g.inEdges(u).iterator();
						assert worker.outEdgeIters[u].hasNext();
						assert worker.inEdgeIters[u].hasNext();
						if (!isSearchSource) {
							assert !path.isEmpty();
							int lastEdge = path.popInt();
							u = worker.g.edgeEndpoint(lastEdge, u);
						}
					}
				}

				@Override
				void dischargeUndirected(int searchSource) {
					assert worker.hasExcess(searchSource);
					assert path.isEmpty();

					dfs: for (int u = searchSource;;) {
						int uLabel = worker.label[u];
						for (EdgeIter it = worker.outEdgeIters[u];;) {
							int e = it.peekNext();
							int v;
							boolean isAdmissible;
							if (u == worker.g.edgeSource(e)) {
								v = worker.g.edgeTarget(e);
								isAdmissible = worker.isResidual(e) && uLabel == worker.label[v] + 1;
							} else {
								assert u == worker.g.edgeTarget(e);
								v = worker.g.edgeSource(e);
								isAdmissible = worker.isTwinResidualUndirected(e) && uLabel == worker.label[v] + 1;
							}
							if (isAdmissible) {
								path.add(e);
								if (path.size() == MAX_AUGMENT_PATH_LENGTH || v == worker.sink) {
									if (v != worker.sink && !worker.hasExcess(v))
										worker.activate(v);

									/* push along the admissible path */
									assert !path.isEmpty();
									int firstNonResidual = pushOnPathUndirected(searchSource);

									/* If the 'source' does not have any excess, we are done */
									if (!worker.hasExcess(searchSource)) {
										path.clear();
										return;
									}

									/* back up in the DFS tree until all edges in the path are admissible */
									path.removeElements(firstNonResidual, path.size());
									// assert path.intStream().allMatch(worker::isResidual);
									if (path.isEmpty()) {
										u = searchSource;
									} else {
										int lastEdge = path.getInt(path.size() - 1);
										int u1 = worker.g.edgeSource(lastEdge);
										int u2 = worker.g.edgeTarget(lastEdge);
										u = worker.label[u1] < worker.label[u2] ? u1 : u2;
									}
								} else {
									/* advance in the DFS */
									u = v;
								}
								continue dfs;
							}
							it.nextInt();
							if (!it.hasNext()) {
								// Finished iterating over all vertex edges.
								// relabel and Reset iterator

								boolean isSearchSource = u == searchSource, hasExcess = true;
								if (!isSearchSource) {
									hasExcess = worker.hasExcess(u);
									if (hasExcess) {
										worker.removeFromLayerActive(u, uLabel);
									} else {
										worker.removeFromLayerInactive(u, uLabel);
									}
								}

								worker.relabel(u, uLabel + 1);
								if ((uLabel = worker.label[u]) >= worker.n) {
									/*
									 * empty layer gap heuristic was activated, all vertices on the path have greater or
									 * equal label as u, all are unreachable from sink
									 */
									path.clear();
									return;
								}

								if (!isSearchSource) {
									if (hasExcess) {
										worker.addToLayerActive(u, uLabel);
									} else {
										worker.addToLayerInactive(u, uLabel);
									}
								}

								it = worker.outEdgeIters[u] = worker.g.outEdges(u).iterator();
								assert it.hasNext();
								if (!isSearchSource) {
									assert !path.isEmpty();
									int lastEdge = path.popInt();
									u = worker.g.edgeEndpoint(lastEdge, u);
								}
								continue dfs;
							}
						}
					}
				}

				/* return the first index of non-residual edge after the push */
				abstract int pushOnPathDirected(int pathSource);

				/* return the first index of non-residual edge after the push */
				abstract int pushOnPathUndirected(int pathSource);
			}

			private static class PartialAugmentDouble extends PartialAugmentBase {

				PartialAugmentDouble(MaximumFlowPushRelabel.WorkerDouble worker) {
					super(worker);
				}

				MaximumFlowPushRelabel.WorkerDouble worker() {
					return (MaximumFlowPushRelabel.WorkerDouble) worker;
				}

				@Override
				int pushOnPathDirected(int pathSource) {
					assert !path.isEmpty();
					int u, v = pathSource;
					final byte Inactive = 0;
					final byte Active = 1;
					final byte SOURCE = 2;
					byte uMark = SOURCE;
					int firstNonResidual = -1;
					for (int i = 0; i < path.size(); i++) {
						int e = path.getInt(i);
						u = v;
						double f;
						boolean vWasActive;
						if (u == worker.g.edgeSource(e)) {
							v = worker.g.edgeTarget(e);
							vWasActive = worker.hasExcess(v);
							f = Math.min(worker().excess[u], worker().residualCapacity[e]);
							assert f >= 0;
							worker().residualCapacity[e] -= f;
							assert worker().residualCapacity[e] >= 0;

							if (firstNonResidual == -1 && !worker.isResidual(e))
								firstNonResidual = i;
						} else {
							assert u == worker.g.edgeTarget(e);
							v = worker.g.edgeSource(e);
							vWasActive = worker.hasExcess(v);
							f = Math.min(worker().excess[u], worker().flow(e));
							assert f >= 0;
							worker().residualCapacity[e] += f;
							assert worker().residualCapacity[e] <= worker().capacity[e];

							if (firstNonResidual == -1 && !worker.hasFlow(e))
								firstNonResidual = i;
						}

						worker().excess[u] -= f;
						worker().excess[v] += f;
						assert worker().excess[u] >= 0;
						assert worker().excess[v] >= 0;

						if (uMark == Active) {
							if (!worker.hasExcess(u))
								worker.deactivate(u);
						} else if (uMark == Inactive) {
							if (worker.hasExcess(u))
								worker.activate(u);
						}
						uMark = vWasActive ? Active : Inactive;
					}
					return firstNonResidual;
				}

				@Override
				int pushOnPathUndirected(int pathSource) {
					assert !path.isEmpty();
					int u, v = pathSource;
					final byte Inactive = 0;
					final byte Active = 1;
					final byte SOURCE = 2;
					byte uMark = SOURCE;
					int firstNonResidual = -1;
					for (int i = 0; i < path.size(); i++) {
						int e = path.getInt(i);
						u = v;
						double f;
						boolean vWasActive;
						if (u == worker.g.edgeSource(e)) {
							v = worker.g.edgeTarget(e);
							vWasActive = worker.hasExcess(v);
							f = Math.min(worker().excess[u], worker().residualCapacity[e]);
							assert f >= 0;
							worker().residualCapacity[e] -= f;
							assert worker().residualCapacity[e] >= 0;

							if (firstNonResidual == -1 && !worker.isResidual(e))
								firstNonResidual = i;
						} else {
							assert u == worker.g.edgeTarget(e);
							v = worker.g.edgeSource(e);
							vWasActive = worker.hasExcess(v);
							double twinResidualCapacity = 2 * worker().capacity[e] - worker().residualCapacity[e];
							f = Math.min(worker().excess[u], twinResidualCapacity);
							assert f >= 0;
							worker().residualCapacity[e] += f;

							if (firstNonResidual == -1 && !worker.isTwinResidualUndirected(e))
								firstNonResidual = i;
						}

						worker().excess[u] -= f;
						worker().excess[v] += f;
						assert worker().excess[u] >= 0;
						assert worker().excess[v] >= 0;

						if (uMark == Active) {
							if (!worker.hasExcess(u))
								worker.deactivate(u);
						} else if (uMark == Inactive) {
							if (worker.hasExcess(u))
								worker.activate(u);
						}
						uMark = vWasActive ? Active : Inactive;
					}
					return firstNonResidual;
				}
			}

			private static class PartialAugmentInt extends PartialAugmentBase {

				PartialAugmentInt(MaximumFlowPushRelabel.WorkerInt worker) {
					super(worker);
				}

				MaximumFlowPushRelabel.WorkerInt worker() {
					return (MaximumFlowPushRelabel.WorkerInt) worker;
				}

				@Override
				int pushOnPathDirected(int pathSource) {
					assert !path.isEmpty();
					int u, v = pathSource;
					final byte Inactive = 0;
					final byte Active = 1;
					final byte SOURCE = 2;
					byte uMark = SOURCE;
					int firstNonResidual = -1;
					for (int i = 0; i < path.size(); i++) {
						int e = path.getInt(i);
						u = v;
						int f;
						boolean vWasActive;
						if (u == worker.g.edgeSource(e)) {
							v = worker.g.edgeTarget(e);
							vWasActive = worker.hasExcess(v);
							f = Math.min(worker().excess[u], worker().residualCapacity[e]);
							assert f >= 0;
							worker().residualCapacity[e] -= f;
							assert worker().residualCapacity[e] >= 0;

							if (firstNonResidual == -1 && !worker.isResidual(e))
								firstNonResidual = i;
						} else {
							assert u == worker.g.edgeTarget(e);
							v = worker.g.edgeSource(e);
							vWasActive = worker.hasExcess(v);
							f = Math.min(worker().excess[u], worker().flow(e));
							assert f >= 0;
							worker().residualCapacity[e] += f;
							assert worker().residualCapacity[e] <= worker().capacity[e];

							if (firstNonResidual == -1 && !worker.hasFlow(e))
								firstNonResidual = i;
						}

						worker().excess[u] -= f;
						worker().excess[v] += f;
						assert worker().excess[u] >= 0;
						assert worker().excess[v] >= 0;

						if (uMark == Active) {
							if (!worker.hasExcess(u))
								worker.deactivate(u);
						} else if (uMark == Inactive) {
							if (worker.hasExcess(u))
								worker.activate(u);
						}
						uMark = vWasActive ? Active : Inactive;
					}
					return firstNonResidual;
				}

				@Override
				int pushOnPathUndirected(int pathSource) {
					assert !path.isEmpty();
					int u, v = pathSource;
					final byte Inactive = 0;
					final byte Active = 1;
					final byte SOURCE = 2;
					byte uMark = SOURCE;
					int firstNonResidual = -1;
					for (int i = 0; i < path.size(); i++) {
						int e = path.getInt(i);
						u = v;
						int f;
						boolean vWasActive;
						if (u == worker.g.edgeSource(e)) {
							v = worker.g.edgeTarget(e);
							vWasActive = worker.hasExcess(v);
							f = Math.min(worker().excess[u], worker().residualCapacity[e]);
							assert f >= 0;
							worker().residualCapacity[e] -= f;
							assert worker().residualCapacity[e] >= 0;

							if (firstNonResidual == -1 && !worker.isResidual(e))
								firstNonResidual = i;
						} else {
							assert u == worker.g.edgeTarget(e);
							v = worker.g.edgeSource(e);
							vWasActive = worker.hasExcess(v);
							int twinResidualCapacity = 2 * worker().capacity[e] - worker().residualCapacity[e];
							f = Math.min(worker().excess[u], twinResidualCapacity);
							assert f >= 0;
							worker().residualCapacity[e] += f;

							if (firstNonResidual == -1 && !worker.isTwinResidualUndirected(e))
								firstNonResidual = i;
						}

						worker().excess[u] -= f;
						worker().excess[v] += f;
						assert worker().excess[u] >= 0;
						assert worker().excess[v] >= 0;

						if (uMark == Active) {
							if (!worker.hasExcess(u))
								worker.deactivate(u);
						} else if (uMark == Inactive) {
							if (worker.hasExcess(u))
								worker.activate(u);
						}
						uMark = vWasActive ? Active : Inactive;
					}
					return firstNonResidual;
				}
			}
		}

		abstract double constructResult();

		private void calcMaxPreflow() {
			recomputeLabels();
			pushAsMuchFromSource();
			if (directed) {
				while (activeOrderPolicy.hasMoreVerticesToDischarge()) {
					int u = activeOrderPolicy.nextVertexToDischarge();
					if (label[u] >= n)
						continue;
					assert hasExcess(u);

					removeFromLayerActive(u, label[u]);
					dischargePolicy.dischargeDirected(u);
					if (label[u] < n)
						addToLayerInactive(u, label[u]);

					if (relabelsSinceLastLabelsRecompute >= labelsReComputeThreshold)
						recomputeLabels();
				}
			} else {
				while (activeOrderPolicy.hasMoreVerticesToDischarge()) {
					int u = activeOrderPolicy.nextVertexToDischarge();
					if (label[u] >= n)
						continue;
					assert hasExcess(u);

					removeFromLayerActive(u, label[u]);
					dischargePolicy.dischargeUndirected(u);
					if (label[u] < n)
						addToLayerInactive(u, label[u]);

					if (relabelsSinceLastLabelsRecompute >= labelsReComputeThreshold)
						recomputeLabels();
				}
			}
		}

		private static abstract class ActiveOrderPolicyImpl {

			abstract boolean hasMoreVerticesToDischarge();

			abstract int nextVertexToDischarge();

			void afterActivate(int v) {}

			void beforeRecomputeLabels() {}

			void afterRecomputeLabels() {}

			void afterRelabel(int v) {}

			void afterVertexLabelReCompute(int v) {}

			static ActiveOrderPolicyImpl newInstance(MaximumFlowPushRelabel.Worker worker, ActiveOrderPolicy policy) {
				switch (policy) {
					case FIFO:
						return new ActiveOrderPolicyImpl.FIFO(worker);
					case HighestFirst:
						return new ActiveOrderPolicyImpl.HighestFirst(worker);
					case LowestFirst:
						return new ActiveOrderPolicyImpl.LowestFirst(worker);
					case MoveToFront:
						return new ActiveOrderPolicyImpl.MoveToFront(worker);
					default:
						throw new IllegalArgumentException("unknown active policy: " + policy);
				}
			}

			final MaximumFlowPushRelabel.Worker worker;

			ActiveOrderPolicyImpl(MaximumFlowPushRelabel.Worker worker) {
				this.worker = Objects.requireNonNull(worker);
			}

			private static class FIFO extends ActiveOrderPolicyImpl {

				private final IntPriorityQueue activeQueue = new FIFOQueueIntNoReduce();

				FIFO(MaximumFlowPushRelabel.Worker worker) {
					super(worker);
				}

				@Override
				boolean hasMoreVerticesToDischarge() {
					return !activeQueue.isEmpty();
				}

				@Override
				int nextVertexToDischarge() {
					return activeQueue.dequeueInt();
				}

				@Override
				void afterActivate(int v) {
					activeQueue.enqueue(v);
				}
			}

			private static class HighestFirst extends ActiveOrderPolicyImpl {

				HighestFirst(MaximumFlowPushRelabel.Worker worker) {
					super(worker);
				}

				@Override
				boolean hasMoreVerticesToDischarge() {
					for (; worker.maxLayerActive > 0; worker.maxLayerActive--)
						if (worker.layersHeadActive[worker.maxLayerActive] != LinkedListFixedSize.None)
							return true;
					return false;
				}

				@Override
				int nextVertexToDischarge() {
					for (; worker.maxLayerActive > 0; worker.maxLayerActive--)
						if (worker.layersHeadActive[worker.maxLayerActive] != LinkedListFixedSize.None)
							return worker.layersHeadActive[worker.maxLayerActive];
					throw new IllegalStateException();
				}
			}

			private static class LowestFirst extends ActiveOrderPolicyImpl {

				int minLayerActive;

				LowestFirst(MaximumFlowPushRelabel.Worker worker) {
					super(worker);
				}

				@Override
				void beforeRecomputeLabels() {
					minLayerActive = 0;
				}

				@Override
				void afterActivate(int v) {
					if (minLayerActive > worker.label[v])
						minLayerActive = worker.label[v];
				}

				@Override
				boolean hasMoreVerticesToDischarge() {
					for (; minLayerActive < worker.n; minLayerActive++)
						if (worker.layersHeadActive[minLayerActive] != LinkedListFixedSize.None)
							return true;
					return false;
				}

				@Override
				int nextVertexToDischarge() {
					for (; minLayerActive < worker.n; minLayerActive++)
						if (worker.layersHeadActive[minLayerActive] != LinkedListFixedSize.None)
							return worker.layersHeadActive[minLayerActive];
					throw new IllegalStateException();
				}
			}

			private static class MoveToFront extends ActiveOrderPolicyImpl {

				private final LinkedListFixedSize.Doubly vertices;
				private int listHead = LinkedListFixedSize.None;
				private JGAlgoUtils.IterPeekable.Int listIter;

				MoveToFront(MaximumFlowPushRelabel.Worker worker) {
					super(worker);
					int n = worker.g.vertices().size();
					vertices = new LinkedListFixedSize.Doubly(n);
				}

				@Override
				void beforeRecomputeLabels() {
					vertices.clear();
					listHead = LinkedListFixedSize.None;
					listIter = null;
				}

				@Override
				void afterRecomputeLabels() {
					listIter = listHead != LinkedListFixedSize.None ? vertices.iterator(listHead)
							: JGAlgoUtils.IterPeekable.Int.Empty;
				}

				@Override
				void afterRelabel(int v) {
					// move to front
					if (v != listHead) {
						vertices.disconnect(v);
						vertices.connect(v, listHead);
						listHead = v;
					}
					listIter = vertices.iterator(listHead);
				}

				@Override
				void afterVertexLabelReCompute(int v) {
					if (listHead != LinkedListFixedSize.None)
						vertices.connect(v, listHead);
					listHead = v;
				}

				@Override
				boolean hasMoreVerticesToDischarge() {
					for (; listIter.hasNext(); listIter.nextInt())
						if (worker.hasExcess(listIter.peekNext()))
							return true;
					return false;
				}

				@Override
				int nextVertexToDischarge() {
					int v;
					while (listIter.hasNext())
						if (worker.hasExcess(v = listIter.nextInt()))
							return v;
					throw new IllegalStateException();
				}
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

			if (directed) {
				// reuse inEdgeIters array
				for (int u = 0; u < n; u++)
					inEdgeIters[u] = g.inEdges(u).iterator();

				for (int root = 0; root < n; root++) {
					if (vState[root] != Unvisited || !hasExcess(root) || root == source || root == sink)
						continue;
					vState[root] = OnPath;
					dfs: for (int u = root;;) {
						edgeIteration: for (; inEdgeIters[u].hasNext(); inEdgeIters[u].nextInt()) {
							int e = inEdgeIters[u].peekNext();
							if (!hasFlow(e))
								continue;
							int v = g.edgeSource(e);
							if (vState[v] == Unvisited) {
								vState[v] = OnPath;
								parent[v] = u;
								u = v;
								continue dfs;
							}
							if (vState[v] == OnPath) {
								// cycle found, find the minimum flow on it
								// remove delta from all edges of the cycle
								eliminateCycleDirected(e);

								// back out the DFS up to the first saturated edge
								int backOutTo = u;
								for (int vw, w; v != u; v = w) {
									vw = inEdgeIters[v].peekNext();
									w = g.edgeSource(vw);
									if (vState[v] != Unvisited && hasFlow(vw))
										continue;
									vState[w] = Unvisited;
									if (vState[v] != Unvisited)
										backOutTo = v;
								}
								if (backOutTo != u) {
									u = backOutTo;
									inEdgeIters[u].nextInt();
									break edgeIteration;
								}
							}
						} /* edgeIteration */

						if (!inEdgeIters[u].hasNext()) {
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
							inEdgeIters[u].nextInt();
						}
					} /* DFS */
				} /* DFS roots */

			} else {
				// reuse outEdgeIters array
				for (int u = 0; u < n; u++)
					outEdgeIters[u] = g.outEdges(u).iterator();

				for (int root = 0; root < n; root++) {
					if (vState[root] != Unvisited || !hasExcess(root) || root == source || root == sink)
						continue;
					vState[root] = OnPath;
					dfs: for (int u = root;;) {
						edgeIteration: for (; outEdgeIters[u].hasNext(); outEdgeIters[u].nextInt()) {
							int e = outEdgeIters[u].peekNext();
							int v;
							if (u == g.edgeSource(e)) {
								if (!hasNegativeFlow(e))
									continue;
								v = g.edgeTarget(e);
							} else {
								assert u == g.edgeTarget(e);
								if (!hasFlow(e))
									continue;
								v = g.edgeSource(e);
							}
							if (vState[v] == Unvisited) {
								vState[v] = OnPath;
								parent[v] = u;
								u = v;
								continue dfs;
							}
							if (vState[v] == OnPath) {
								// cycle found, find the minimum flow on it
								// remove delta from all edges of the cycle
								eliminateCycleUndirected(u, e);

								// back out the DFS up to the first saturated edge
								int backOutTo = u;
								for (int vw, w; v != u; v = w) {
									vw = outEdgeIters[v].peekNext();
									if (v == g.edgeSource(vw)) {
										w = g.edgeTarget(vw);
										if (vState[v] != Unvisited && hasNegativeFlow(vw))
											continue;
									} else {
										assert v == g.edgeTarget(vw);
										w = g.edgeSource(vw);
										if (vState[v] != Unvisited && hasFlow(vw))
											continue;
									}
									vState[w] = Unvisited;
									if (vState[v] != Unvisited)
										backOutTo = v;
								}
								if (backOutTo != u) {
									u = backOutTo;
									outEdgeIters[u].nextInt();
									break edgeIteration;
								}
							}
						} /* edgeIteration */

						if (!outEdgeIters[u].hasNext()) {
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
							outEdgeIters[u].nextInt();
						}
					} /* DFS */
				} /* DFS roots */
			}

			// All cycles were eliminated, and we calculated a topological order of the
			// vertices. Iterate over them using this order and return all excess flow to
			// source.
			if (topoBegin != -1)
				eliminateExcessWithTopologicalOrder(topoBegin, topoEnd, topoNext);
			// for (int u = 0; u < n; u++)
			// if (u != source && u != sink)
			// assert !hasExcess(u);
		}

		/* eliminated a cycle found during the DFS of convertPreflowToFlow */
		/* the cycle can be found by following e and edgeIters[u].peekNext() */
		abstract void eliminateCycleDirected(int e);

		abstract void eliminateCycleUndirected(int cycleSource, int e);

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
			if (directed) {
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
					for (EdgeIter eit = g.outEdges(v).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						if (!hasFlow(e))
							continue;
						int u = eit.target();
						if (visited.get(u))
							continue;
						visited.set(u);
						queue.enqueue(u);
					}
				}
			} else {
				while (!queue.isEmpty()) {
					int v = queue.dequeueInt();
					for (int e : g.outEdges(v)) {
						int u;
						if (v == g.edgeSource(e)) {
							if (!isTwinResidualUndirected(e))
								continue;
							u = g.edgeTarget(e);
						} else {
							assert v == g.edgeTarget(e);
							if (!isResidual(e))
								continue;
							u = g.edgeSource(e);
						}
						if (visited.get(u))
							continue;
						visited.set(u);
						queue.enqueue(u);
					}
				}
			}
			assert !visited.get(source);
			BitSet cut = new BitSet(n);
			for (int n = g.vertices().size(), u = 0; u < n; u++)
				if (!visited.get(u))
					cut.set(u);
			visited.clear();
			return new CutImpl(g, cut);
		}

		abstract boolean hasExcess(int u);

		abstract boolean isResidual(int e);

		abstract boolean isTwinResidualUndirected(int e);

		abstract boolean isSaturated(int e);

		abstract boolean hasFlow(int e);

		abstract boolean hasNegativeFlow(int e);

	}

	static class WorkerDouble extends Worker {
		final double[] capacity;
		final double[] residualCapacity;

		final double[] excess;

		static final double EPS = 0.0001;

		WorkerDouble(IndexGraph gOrig, FlowNetwork net, int source, int sink, ActiveOrderPolicy activeOrderPolicy,
				DischargePolicy dischargePolicy) {
			super(gOrig, net, source, sink, activeOrderPolicy, dischargePolicy);

			capacity = new double[g.edges().size()];
			initCapacities(capacity);
			residualCapacity = capacity.clone();

			excess = new double[n];
		}

		@Override
		void pushAsMuchFromSource() {
			if (directed) {
				for (EdgeIter eit = g.outEdges(source).iterator(); eit.hasNext();) {
					int e = eit.nextInt(), v;
					double f = getResidualCapacity(e);
					if (f > 0 && label[source] > label[v = eit.target()]) {
						if (v != sink && !hasExcess(v))
							activate(v);
						push(e, f);
					}
				}
			} else {
				for (int e : g.outEdges(source)) {
					int v;
					if (source == g.edgeSource(e)) {
						double f = getResidualCapacity(e);
						if (f > 0 && label[source] > label[v = g.edgeTarget(e)]) {
							if (v != sink && !hasExcess(v))
								activate(v);
							push(e, f);
						}
					} else {
						assert source == g.edgeTarget(e);
						double f = getTwinResidualCapacity(e);
						if (f > 0 && label[source] > label[v = g.edgeSource(e)]) {
							if (v != sink && !hasExcess(v))
								activate(v);
							push(e, -f);
						}
					}
				}
			}
		}

		private void push(int e, double f) {
			residualCapacity[e] -= f;
			assert residualCapacity[e] >= -EPS;

			int u = g.edgeSource(e), v = g.edgeTarget(e);
			excess[u] -= f;
			excess[v] += f;
		};

		@Override
		void eliminateCycleDirected(int e) {
			assert directed;
			int u = g.edgeTarget(e);
			int v = g.edgeSource(e);
			assert hasFlow(e);
			double f = flow(e);
			for (;;) {
				e = inEdgeIters[v].peekNext();
				assert hasFlow(e);
				f = Math.min(f, flow(e));
				if (v == u)
					break;
				v = g.edgeSource(e);
			}
			assert f > 0;

			// remove delta from all edges of the cycle
			for (v = u;;) {
				e = inEdgeIters[v].peekNext();
				residualCapacity[e] += f;
				v = g.edgeSource(e);
				if (v == u)
					break;
			}
		}

		@Override
		void eliminateCycleUndirected(int cycleSource, int e) {
			int u = cycleSource;
			int v;
			double f;
			if (u == g.edgeSource(e)) {
				v = g.edgeTarget(e);
				assert hasNegativeFlow(e);
				f = -flow(e);
			} else {
				assert u == g.edgeTarget(e);
				v = g.edgeSource(e);
				assert hasFlow(e);
				f = flow(e);
			}
			for (int w;;) {
				e = outEdgeIters[v].peekNext();
				double ef = flow(e);
				if (v == g.edgeSource(e)) {
					assert ef < 0;
					ef = -ef;
					w = g.edgeTarget(e);
				} else {
					assert v == g.edgeTarget(e);
					assert ef > 0;
					w = g.edgeSource(e);
				}
				f = Math.min(f, ef);
				if (v == u)
					break;
				v = w;
			}
			assert f > 0;

			// remove delta from all edges of the cycle
			for (v = u;;) {
				e = outEdgeIters[v].peekNext();
				if (v == g.edgeSource(e)) {
					residualCapacity[e] -= f;
					v = g.edgeTarget(e);
				} else {
					assert v == g.edgeTarget(e);
					residualCapacity[e] += f;
					v = g.edgeSource(e);
				}
				if (v == u)
					break;
			}
		}

		@Override
		void eliminateExcessWithTopologicalOrder(int topoBegin, int topoEnd, int[] topoNext) {
			if (directed) {
				for (int u = topoBegin;; u = topoNext[u]) {
					for (int e : g.inEdges(u)) {
						if (!hasExcess(u))
							break;
						double f = flow(e);
						if (f > 0)
							push(e, -Math.min(excess[u], f));
					}
					if (u == topoEnd)
						break;
				}
			} else {
				for (int u = topoBegin;; u = topoNext[u]) {
					for (int e : g.outEdges(u)) {
						if (!hasExcess(u))
							break;
						double f = flow(e);
						if (u == g.edgeSource(e)) {
							if (f < 0)
								push(e, Math.min(excess[u], -f));
						} else {
							assert u == g.edgeTarget(e);
							if (f > 0)
								push(e, -Math.min(excess[u], f));
						}
					}
					if (u == topoEnd)
						break;
				}
			}
		}

		@Override
		double constructResult() {
			return constructResult(capacity, residualCapacity);
		}

		@Override
		boolean hasExcess(int u) {
			return excess[u] > EPS;
		}

		double getResidualCapacity(int e) {
			return residualCapacity[e];
		}

		double flow(int e) {
			return capacity[e] - residualCapacity[e];
		}

		@Override
		boolean isResidual(int e) {
			return getResidualCapacity(e) > EPS;
		}

		@Override
		boolean isTwinResidualUndirected(int e) {
			return getTwinResidualCapacity(e) > EPS;
		}

		double getTwinResidualCapacity(int e) {
			assert !directed;
			return 2 * capacity[e] - residualCapacity[e];
		}

		@Override
		boolean isSaturated(int e) {
			return getResidualCapacity(e) <= EPS;
		}

		@Override
		boolean hasNegativeFlow(int e) {
			assert !directed;
			return flow(e) < -EPS;
		}

		@Override
		boolean hasFlow(int e) {
			return flow(e) > EPS;
		}

	}

	static class WorkerInt extends Worker {
		final int[] residualCapacity;
		final int[] capacity;

		final int[] excess;

		WorkerInt(IndexGraph gOrig, FlowNetwork.Int net, int source, int sink, ActiveOrderPolicy activeOrderPolicy,
				DischargePolicy dischargePolicy) {
			super(gOrig, net, source, sink, activeOrderPolicy, dischargePolicy);

			capacity = new int[g.edges().size()];
			initCapacities(capacity);
			residualCapacity = capacity.clone();

			excess = new int[n];
		}

		@Override
		void pushAsMuchFromSource() {
			if (directed) {
				for (EdgeIter eit = g.outEdges(source).iterator(); eit.hasNext();) {
					int e = eit.nextInt(), v;
					int f = getResidualCapacity(e);
					if (f > 0 && label[source] > label[v = eit.target()]) {
						if (v != sink && !hasExcess(v))
							activate(v);
						push(e, f);
					}
				}
			} else {
				for (int e : g.outEdges(source)) {
					int v;
					if (source == g.edgeSource(e)) {
						int f = getResidualCapacity(e);
						if (f > 0 && label[source] > label[v = g.edgeTarget(e)]) {
							if (v != sink && !hasExcess(v))
								activate(v);
							push(e, f);
						}
					} else {
						assert source == g.edgeTarget(e);
						int f = getTwinResidualCapacity(e);
						if (f > 0 && label[source] > label[v = g.edgeSource(e)]) {
							if (v != sink && !hasExcess(v))
								activate(v);
							push(e, -f);
						}
					}
				}
			}
		}

		private void push(int e, int f) {
			residualCapacity[e] -= f;
			assert residualCapacity[e] >= 0;

			int u = g.edgeSource(e), v = g.edgeTarget(e);
			excess[u] -= f;
			excess[v] += f;
		}

		@Override
		void eliminateCycleDirected(int e) {
			assert directed;
			int u = g.edgeTarget(e);
			int v = g.edgeSource(e);
			assert hasFlow(e);
			int f = flow(e);
			for (;;) {
				e = inEdgeIters[v].peekNext();
				assert hasFlow(e);
				f = Math.min(f, flow(e));
				if (v == u)
					break;
				v = g.edgeSource(e);
			}
			assert f > 0;

			// remove delta from all edges of the cycle
			for (v = u;;) {
				e = inEdgeIters[v].peekNext();
				residualCapacity[e] += f;
				v = g.edgeSource(e);
				if (v == u)
					break;
			}
		}

		@Override
		void eliminateCycleUndirected(int cycleSource, int e) {
			int u = cycleSource;
			int v;
			int f;
			if (u == g.edgeSource(e)) {
				v = g.edgeTarget(e);
				assert hasNegativeFlow(e);
				f = -flow(e);
			} else {
				assert u == g.edgeTarget(e);
				v = g.edgeSource(e);
				assert hasFlow(e);
				f = flow(e);
			}
			for (int w;;) {
				e = outEdgeIters[v].peekNext();
				int ef = flow(e);
				if (v == g.edgeSource(e)) {
					assert ef < 0;
					ef = -ef;
					w = g.edgeTarget(e);
				} else {
					assert v == g.edgeTarget(e);
					assert ef > 0;
					w = g.edgeSource(e);
				}
				f = Math.min(f, ef);
				if (v == u)
					break;
				v = w;
			}
			assert f > 0;

			// remove delta from all edges of the cycle
			for (v = u;;) {
				e = outEdgeIters[v].peekNext();
				if (v == g.edgeSource(e)) {
					residualCapacity[e] -= f;
					v = g.edgeTarget(e);
				} else {
					assert v == g.edgeTarget(e);
					residualCapacity[e] += f;
					v = g.edgeSource(e);
				}
				if (v == u)
					break;
			}
		}

		@Override
		void eliminateExcessWithTopologicalOrder(int topoBegin, int topoEnd, int[] topoNext) {
			if (directed) {
				for (int u = topoBegin;; u = topoNext[u]) {
					for (int e : g.inEdges(u)) {
						if (!hasExcess(u))
							break;
						int f = flow(e);
						if (f > 0)
							push(e, -Math.min(excess[u], f));
					}
					if (u == topoEnd)
						break;
				}
			} else {
				for (int u = topoBegin;; u = topoNext[u]) {
					for (int e : g.outEdges(u)) {
						if (!hasExcess(u))
							break;
						int f = flow(e);
						if (u == g.edgeSource(e)) {
							if (f < 0)
								push(e, Math.min(excess[u], -f));
						} else {
							assert u == g.edgeTarget(e);
							if (f > 0)
								push(e, -Math.min(excess[u], f));
						}
					}
					if (u == topoEnd)
						break;
				}
			}
		}

		@Override
		double constructResult() {
			return constructResult(capacity, residualCapacity);
		}

		@Override
		boolean hasExcess(int u) {
			return excess[u] > 0;
		}

		int getResidualCapacity(int e) {
			return residualCapacity[e];
		}

		int flow(int e) {
			return capacity[e] - residualCapacity[e];
		}

		@Override
		boolean isResidual(int e) {
			return getResidualCapacity(e) > 0;
		}

		@Override
		boolean isTwinResidualUndirected(int e) {
			return getTwinResidualCapacity(e) > 0;
		}

		int getTwinResidualCapacity(int e) {
			assert !directed;
			return 2 * capacity[e] - residualCapacity[e];
		}

		@Override
		boolean isSaturated(int e) {
			return getResidualCapacity(e) == 0;
		}

		@Override
		boolean hasNegativeFlow(int e) {
			assert !directed;
			return flow(e) < 0;
		}

		@Override
		boolean hasFlow(int e) {
			return flow(e) > 0;
		}

	}

}
