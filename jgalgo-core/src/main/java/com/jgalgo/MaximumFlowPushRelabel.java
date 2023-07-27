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
import it.unimi.dsi.fastutil.ints.IntCollection;
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
class MaximumFlowPushRelabel extends MaximumFlowAbstract {

	private static enum ActiveOrderPolicy {
		FIFO, HighestFirst, LowestFirst, MoveToFront;
	}

	private static enum DischargePolicy {
		SingleStep, PartialAugment
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
	double computeMaximumFlow(IndexGraph g, FlowNetwork net, IntCollection sources, IntCollection sinks) {
		if (net instanceof FlowNetwork.Int) {
			return new WorkerInt(g, (FlowNetwork.Int) net, sources, sinks, activeOrderPolicy, dischargePolicy)
					.computeMaxFlow();
		} else {
			return new WorkerDouble(g, net, sources, sinks, activeOrderPolicy, dischargePolicy).computeMaxFlow();
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

	@Override
	public Cut computeMinimumCut(IndexGraph g, WeightFunction w, IntCollection sources, IntCollection sinks) {
		FlowNetwork net = flowNetFromEdgeWeights(w);
		if (w instanceof WeightFunction.Int) {
			return new WorkerInt(g, (FlowNetwork.Int) net, sources, sinks, activeOrderPolicy, dischargePolicy)
					.computeMinimumCut();
		} else {
			return new WorkerDouble(g, net, sources, sinks, activeOrderPolicy, dischargePolicy).computeMinimumCut();
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

	static abstract class Worker extends MaximumFlowAbstract.Worker {

		final int[] label;
		final EdgeIter[] edgeIters;

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
			edgeIters = new EdgeIter[n];

			relabelVisited = new BitSet(n);
			relabelQueue = new FIFOQueueIntNoReduce();
			labelsReComputeThreshold = n;

			layers = new LinkedListFixedSize.Doubly(n);
			layersHeadActive = new int[n];
			layersHeadInactive = new int[n];

			this.activeOrderPolicy = ActiveOrderPolicyImpl.newInstance(this, activeOrderPolicy);
			this.dischargePolicy = DischargePolicyImpl.newInstance(this, dischargePolicy);
		}

		Worker(IndexGraph gOrig, FlowNetwork net, IntCollection sources, IntCollection sinks,
				ActiveOrderPolicy activeOrderPolicy, DischargePolicy dischargePolicy) {
			super(gOrig, net, sources, sinks);

			label = new int[n];
			edgeIters = new EdgeIter[n];

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
			// reset edge iterator
			edgeIters[u] = g.outEdges(u).iterator();
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

			abstract void discharge(int u);

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
				void discharge(int u) {
					for (EdgeIter it = worker.edgeIters[u];;) {
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
						it.nextInt();
						if (!it.hasNext()) {
							// Finished iterating over all vertex edges.
							// relabel and Reset iterator
							worker.relabel(u, worker.label[u] + 1);
							if (worker.label[u] >= worker.n)
								break;
							it = worker.edgeIters[u] = worker.g.outEdges(u).iterator();
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
				void discharge(int u) {
					for (EdgeIter it = worker.edgeIters[u];;) {
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
						it.nextInt();
						if (!it.hasNext()) {
							// Finished iterating over all vertex edges.
							// relabel and Reset iterator
							worker.relabel(u, worker.label[u] + 1);
							if (worker.label[u] >= worker.n)
								break;
							it = worker.edgeIters[u] = worker.g.outEdges(u).iterator();
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
				void discharge(int searchSource) {
					assert worker.hasExcess(searchSource);
					assert path.isEmpty();

					dfs: for (int u = searchSource;;) {
						int uLabel = worker.label[u];
						for (EdgeIter it = worker.edgeIters[u];;) {
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
									int firstNonResidual = pushOnPath(searchSource);

									/* If the 'source' does not have any excess, we are done */
									if (!worker.hasExcess(searchSource)) {
										path.clear();
										return;
									}

									/* back up in the DFS tree until all edges in the path are admissible */
									path.removeElements(firstNonResidual, path.size());
									assert path.intStream().allMatch(worker::isResidual);
									u = path.isEmpty() ? searchSource
											: worker.g.edgeTarget(path.getInt(path.size() - 1));
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

								it = worker.edgeIters[u] = worker.g.outEdges(u).iterator();
								assert it.hasNext();
								if (!isSearchSource) {
									assert !path.isEmpty();
									int lastEdge = path.popInt();
									assert u == worker.g.edgeTarget(lastEdge);
									u = worker.g.edgeSource(lastEdge);
								}
								continue dfs;
							}
						}
					}
				}

				/* return the first index of non-residual edge after the push */
				abstract int pushOnPath(int pathSource);
			}

			private static class PartialAugmentDouble extends PartialAugmentBase {

				PartialAugmentDouble(MaximumFlowPushRelabel.WorkerDouble worker) {
					super(worker);
				}

				MaximumFlowPushRelabel.WorkerDouble worker() {
					return (MaximumFlowPushRelabel.WorkerDouble) worker;
				}

				@Override
				int pushOnPath(int pathSource) {
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
						v = worker.g.edgeTarget(e);
						double f = Math.min(worker().excess[u], worker().residualCapacity[e]);
						assert f >= 0;
						boolean vWasActive = worker.hasExcess(v);

						int t = worker.twin[e];
						worker().residualCapacity[e] -= f;
						worker().residualCapacity[t] += f;
						assert worker().residualCapacity[e] >= -WorkerDouble.EPS;
						assert worker().residualCapacity[t] >= -WorkerDouble.EPS;
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

						if (firstNonResidual == -1 && !worker.isResidual(e))
							firstNonResidual = i;
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
				int pushOnPath(int pathSource) {
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
						v = worker.g.edgeTarget(e);
						int f = Math.min(worker().excess[u], worker().residualCapacity[e]);
						assert f >= 0;
						boolean vWasActive = worker.hasExcess(v);

						int t = worker.twin[e];
						worker().residualCapacity[e] -= f;
						worker().residualCapacity[t] += f;
						assert worker().residualCapacity[e] >= 0;
						assert worker().residualCapacity[t] >= 0;
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

						if (firstNonResidual == -1 && !worker.isResidual(e))
							firstNonResidual = i;
					}
					return firstNonResidual;
				}
			}
		}

		abstract double constructResult();

		private void calcMaxPreflow() {
			recomputeLabels();
			pushAsMuchFromSource();
			while (activeOrderPolicy.hasMoreVerticesToDischarge()) {
				int u = activeOrderPolicy.nextVertexToDischarge();
				if (label[u] >= n)
					continue;
				assert hasExcess(u);
				removeFromLayerActive(u, label[u]);
				dischargePolicy.discharge(u);
				if (label[u] < n)
					addToLayerInactive(u, label[u]);

				if (relabelsSinceLastLabelsRecompute >= labelsReComputeThreshold)
					recomputeLabels();
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
							for (int vw, w; v != u; v = w) {
								vw = edgeIters[v].peekNext();
								w = g.edgeTarget(vw);
								if (vState[v] != Unvisited && hasNegativeFlow(vw))
									continue;
								vState[w] = Unvisited;
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
			BitSet cut = new BitSet(n);
			for (int n = gOrig.vertices().size(), u = 0; u < n; u++)
				if (!visited.get(u))
					cut.set(u);
			visited.clear();
			return new CutImpl(gOrig, cut);
		}

		abstract boolean hasExcess(int u);

		abstract boolean isResidual(int e);

		abstract boolean isSaturated(int e);

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

		WorkerDouble(IndexGraph gOrig, FlowNetwork net, IntCollection sources, IntCollection sinks,
				ActiveOrderPolicy activeOrderPolicy, DischargePolicy dischargePolicy) {
			super(gOrig, net, sources, sinks, activeOrderPolicy, dischargePolicy);

			capacity = new double[g.edges().size()];
			initCapacities(capacity);
			residualCapacity = capacity.clone();

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
			residualCapacity[e] -= f;
			residualCapacity[t] += f;
			assert residualCapacity[e] >= -EPS;
			assert residualCapacity[t] >= -EPS;

			int u = g.edgeSource(e), v = g.edgeTarget(e);
			excess[u] -= f;
			excess[v] += f;
		};

		@Override
		void eliminateCycle(int e) {
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			assert hasNegativeFlow(e);
			double f = -flow(e);
			for (;;) {
				e = edgeIters[v].peekNext();
				assert hasNegativeFlow(e);
				f = Math.min(f, -flow(e));
				if (v == u)
					break;
				v = g.edgeTarget(e);
			}

			// remove delta from all edges of the cycle
			for (v = u;;) {
				e = edgeIters[v].peekNext();
				int t = twin[e];
				residualCapacity[e] -= f;
				residualCapacity[t] += f;
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
					double f = flow(e);
					if (f < 0)
						push(e, Math.min(excess[u], -f));
				}
				if (u == topoEnd)
					break;
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
		boolean isSaturated(int e) {
			return getResidualCapacity(e) <= EPS;
		}

		@Override
		boolean hasNegativeFlow(int e) {
			return flow(e) < 0;
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

		WorkerInt(IndexGraph gOrig, FlowNetwork.Int net, IntCollection sources, IntCollection sinks,
				ActiveOrderPolicy activeOrderPolicy, DischargePolicy dischargePolicy) {
			super(gOrig, net, sources, sinks, activeOrderPolicy, dischargePolicy);

			capacity = new int[g.edges().size()];
			initCapacities(capacity);
			residualCapacity = capacity.clone();

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
			residualCapacity[e] -= f;
			residualCapacity[t] += f;
			assert residualCapacity[e] >= 0;
			assert residualCapacity[t] >= 0;

			int u = g.edgeSource(e), v = g.edgeTarget(e);
			excess[u] -= f;
			excess[v] += f;
		}

		@Override
		void eliminateCycle(int e) {
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			assert hasNegativeFlow(e);
			int f = -flow(e);
			for (;;) {
				e = edgeIters[v].peekNext();
				assert hasNegativeFlow(e);
				f = Math.min(f, -flow(e));
				if (v == u)
					break;
				v = g.edgeTarget(e);
			}

			// remove delta from all edges of the cycle
			for (v = u;;) {
				e = edgeIters[v].peekNext();
				int t = twin[e];
				residualCapacity[e] -= f;
				residualCapacity[t] += f;
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
					int f = flow(e);
					if (f < 0)
						push(e, Math.min(excess[u], -f));
				}
				if (u == topoEnd)
					break;
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
		boolean isSaturated(int e) {
			return getResidualCapacity(e) == 0;
		}

		@Override
		boolean hasNegativeFlow(int e) {
			return flow(e) < 0;
		}
	}

}
