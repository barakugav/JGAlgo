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
package com.jgalgo.alg.flow;

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import com.jgalgo.alg.flow.Flows.ResidualGraph;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.ds.LinkedListFixedSize;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntStack;

/**
 * Minimum-cost flow computation using the cost-scaling algorithm with partial-augmentations push-relabel variant.
 *
 * <p>
 * The algorithm runs in \(O(n^2 m \log (n C))\) where \(C\) is the maximum (absolute) edge cost.
 *
 * <p>
 * Based on 'Efficient implementations of minimum-cost flow algorithms' by Z. Kiraly, P. Kovacs (2012).
 *
 * @see    MaximumFlowPushRelabelPartialAugment
 * @author Barak Ugav
 */
class MinimumCostFlowCostScaling extends MinimumCostFlows.AbstractImplBasedSupply {

	private static final int alpha = 16;

	@Override
	IFlow computeMinCostFlow(IndexGraph g, IWeightFunction capacity, IWeightFunction cost, IWeightFunction supply) {
		if (!(WeightFunction.isInteger(capacity) && WeightFunction.isInteger(supply)))
			throw new IllegalArgumentException("only integer capacities and flows are supported");
		if (!WeightFunction.isInteger(cost))
			throw new IllegalArgumentException("only integer costs are supported");
		return new Worker(g, (IWeightFunctionInt) capacity, (IWeightFunctionInt) cost, (IWeightFunctionInt) supply)
				.solve();
	}

	private static class Worker {

		/* g is a residual graph, duplicating each edge in the original graph */
		private final IndexGraph g;
		private final IndexGraph gOrig;
		private final ResidualGraph resGraph;
		private final IWeightFunctionInt capacity;
		private final IWeightFunctionInt costOrig;

		/* per-edge information */
		private final int[] residualCapacity;
		private final long[] cost;
		private final int[] twin;

		/* 'potential' is similar to 'label' in the push-relabel max-flow algorithms */
		private final long[] potential;
		private final int[] excess;
		private long eps;

		/* we use a simple FIFO queue for the active vertices */
		private final IntPriorityQueue activeQueue;

		/* DFS path used during partial-augmentation during discharge */
		private final IntArrayList path;
		/* Bitmap of the vertices on the path, for fast cycle detection */
		private final Bitmap onPath;
		/* Per vertex iterator, corresponding to 'current edge' in the paper */
		private final IEdgeIter[] edgeIter;

		/* The maximum length of an augmentation path, similar to {@link MaximumFlowPushRelabelPartialAugment} */
		private static final int MAX_AUGMENT_PATH_LENGTH = 4;

		/* global updated should be performed each O(n) relabels */
		private int relabelsSinceLastGlobalUpdate;
		private final int globalUpdateThreshold;

		/* Potential refinement heuristic fields */
		private final IntStack topologicalOrder;
		private final int[] rank;
		private final int rankUpperBound;
		private final LinkedListFixedSize.Doubly buckets;
		private final int[] bucketsHeads;
		/* Potential refinement doesn't seems to be worth it in the early rounds, skip them */
		private static final int POTENTIAL_REFINEMENT_ITERATION_SKIP = 2;

		Worker(IndexGraph gOrig, IWeightFunctionInt capacity, IWeightFunctionInt costOrig, IWeightFunctionInt supply) {
			Assertions.onlyDirected(gOrig);
			Assertions.flowCheckSupply(gOrig, supply);
			this.gOrig = gOrig;
			this.capacity = capacity;
			this.costOrig = costOrig;

			/* Build the residual graph by duplicating each edge in the original graph */
			ResidualGraph.Builder b = new ResidualGraph.Builder(gOrig);
			b.addAllOriginalEdges();
			resGraph = b.build();
			g = resGraph.g;
			final int[] edgeRef = resGraph.edgeRef;
			twin = resGraph.twin;
			final int n = g.vertices().size();
			final int m = g.edges().size();

			potential = new long[n];
			excess = new int[n];
			/* we don't init excess to the 'supply' function because the circulation will zero it */

			cost = new long[m];
			long maxCost = 1;
			for (int e : range(m)) {
				long c = costOrig.weightInt(edgeRef[e]);
				c = resGraph.isOriginalEdge(e) ? c : -c;
				/* multiply all costs by \alpha n so costs will be integers when we scale them */
				cost[e] = c * n * alpha;
				maxCost = Math.max(maxCost, cost[e]);
			}
			eps = maxCost / alpha;

			/* Find a valid circulation that satisfy the supply, without considering costs */
			FlowCirculation circulationAlgo = new FlowCirculationPushRelabel();
			IFlow circulation = (IFlow) circulationAlgo.computeCirculation(gOrig, capacity, supply);

			/* init residual capacities */
			residualCapacity = new int[m];
			for (int e : range(m)) {
				int eRef = edgeRef[e];
				if (resGraph.isOriginalEdge(e)) {
					residualCapacity[e] = capacity.weightInt(eRef) - (int) circulation.getFlow(eRef);
				} else {
					residualCapacity[e] = (int) circulation.getFlow(eRef);
				}
			}

			activeQueue = new FIFOQueueIntNoReduce(n);
			path = new IntArrayList(MAX_AUGMENT_PATH_LENGTH);
			onPath = new Bitmap(n);
			edgeIter = new IEdgeIter[n];

			globalUpdateThreshold = n;

			topologicalOrder = new IntArrayList(n);
			rank = new int[n];
			rankUpperBound = alpha * n;
			buckets = new LinkedListFixedSize.Doubly(n + 2);
			bucketsHeads = new int[rankUpperBound];
		}

		IFlow solve() {
			solveWithPartialAugment();

			final int n = g.vertices().size();
			for (int u : range(n))
				potential[u] /= n * alpha;

			long maxPotential = 0;
			for (int u : range(n))
				if (maxPotential < potential[u])
					maxPotential = potential[u];
			if (maxPotential != 0)
				for (int u : range(n))
					potential[u] -= maxPotential;

			final int[] edgeRef = resGraph.edgeRef;
			double[] flow = new double[gOrig.edges().size()];
			for (int e : range(g.edges().size())) {
				if (resGraph.isOriginalEdge(e)) {
					int eRef = edgeRef[e];
					int cap = capacity.weightInt(eRef);
					flow[eRef] = cap - residualCapacity[e];
				}
			}

			MinimumCostFlows.saturateNegativeCostSelfEdges(gOrig, capacity, costOrig, flow);
			return new Flows.FlowImpl(gOrig, flow);
		}

		private void solveWithPartialAugment() {
			for (int epsIter = 0; eps >= 1; eps = ((eps < alpha && eps > 1) ? 1 : eps / alpha), epsIter++) {
				if (epsIter >= POTENTIAL_REFINEMENT_ITERATION_SKIP)
					if (potentialRefinement())
						continue;

				/* Saturate all edges with negative cost */
				for (int u : range(g.vertices().size())) {
					long uPotential = potential[u];
					for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						int delta = residualCapacity[e];
						if (delta > 0) {
							int v = eit.targetInt();
							if (cost[e] + uPotential - potential[v] < 0) {
								/* we found a negative cost edge, saturate it */
								excess[u] -= delta;
								excess[v] += delta;
								residualCapacity[e] = 0;
								residualCapacity[twin[e]] += delta;
							}
						}
					}
				}

				/* Find all active vertices */
				assert activeQueue.isEmpty();
				for (int u : range(g.vertices().size())) {
					if (excess[u] > 0)
						activeQueue.enqueue(u);
					edgeIter[u] = g.outEdges(u).iterator();
				}

				activeVerticesLoop: for (;;) {
					/* Find next active vertex in FIFO queue */
					final int searchSource;
					for (;;) {
						if (activeQueue.isEmpty())
							break activeVerticesLoop;
						int v = activeQueue.dequeueInt();
						if (excess[v] > 0) {
							searchSource = v;
							break;
						}
					}

					/* Discharge the vertex, namely push its excess to other vertices */
					dischargePartialAugment(searchSource);
					if (excess[searchSource] > 0)
						activeQueue.enqueue(searchSource);

					if (relabelsSinceLastGlobalUpdate >= globalUpdateThreshold)
						globalUpdate();
				}
			}
		}

		private void dischargePartialAugment(int searchSource) {

			/*
			 * We perform a DFS from the 'searchSource' vertex, using only admissible edges. If we manage to reach path
			 * of length MAX_AUGMENT_PATH_LENGTH, or we reached a vertex with negative excess, or we closed a cycle, we
			 * push flow on the DFS path. If we were not able to advance the search from some vertex, we 'relabel' it
			 * (change it potential) and back up the DFS.
			 */

			assert path.isEmpty();
			assert onPath.isEmpty();
			onPath.set(searchSource);
			dfs: for (int u = searchSource;;) {

				/* Find an admissible edge from u */
				long uPotential = potential[u];
				long minReducedCost = Long.MAX_VALUE;
				assert edgeIter[u].hasNext();
				final int firstEdge = edgeIter[u].peekNextInt();
				for (IEdgeIter eit = edgeIter[u]; /* eit.hasNext() */;) {
					assert eit.hasNext();
					int e = eit.peekNextInt();
					if (isResidual(e)) {
						int v = g.edgeTarget(e);
						long reducedCost = cost[e] + uPotential - potential[v];
						if (reducedCost < 0) {
							/* Extend the DFS search by the found edge */
							path.add(e);

							if (path.size() == MAX_AUGMENT_PATH_LENGTH || excess[v] < 0 || onPath.get(v)) {
								/* augmentation path (maybe cycle) was found */
								pushOnPath(searchSource);
								return;
							}

							/* continue down in the DFS */
							u = v;
							onPath.set(v);
							continue dfs;

						} else {
							/* Cache the minimum change in the potential required for the edge to become admissible */
							minReducedCost = Math.min(minReducedCost, reducedCost);
						}
					}

					/* Advance to the next edge */
					eit.nextInt();
					if (!eit.hasNext()) {
						/* we finish iterating over all edges of the vertex, relabel it */

						/* Find the minimum change in the potential required for an admissible edge to appear */
						if (u != searchSource) {
							assert !path.isEmpty();
							int lastEdge = path.getInt(path.size() - 1);
							int lastTwin = twin[lastEdge];
							long reducedCost = cost[lastTwin] + uPotential - potential[g.edgeTarget(lastTwin)];
							minReducedCost = Math.min(minReducedCost, reducedCost);
						}
						for (IEdgeIter eit2 = g.outEdges(u).iterator();;) {
							assert eit2.hasNext();
							int e2 = eit2.nextInt();
							if (e2 == firstEdge)
								break;
							if (isResidual(e2)) {
								long reducedCost = cost[e2] + uPotential - potential[g.edgeTarget(e2)];
								minReducedCost = Math.min(minReducedCost, reducedCost);
							}

						}
						assert minReducedCost != Long.MAX_VALUE;

						/* 'relabel', change potential */
						potential[u] -= minReducedCost + eps;
						relabelsSinceLastGlobalUpdate++;
						/* Reset u iterator */
						edgeIter[u] = g.outEdges(u).iterator();
						assert edgeIter[u].hasNext();

						if (u != searchSource) {
							/* step up once in the DFS path */
							assert !path.isEmpty();
							int lastEdge = path.popInt();
							assert u == g.edgeTarget(lastEdge);
							assert onPath.get(u);
							onPath.clear(u);
							u = g.edgeSource(lastEdge);
						}
						continue dfs;
					}
				}
			}
		}

		private void pushOnPath(int searchSource) {
			assert !path.isEmpty();
			assert searchSource == g.edgeSource(path.getInt(0));
			int u, v = searchSource;
			for (int e : path) {
				u = v;
				v = g.edgeTarget(e);
				onPath.clear(v);
				int delta = Math.min(residualCapacity[e], excess[u]);

				int t = twin[e];
				residualCapacity[e] -= delta;
				residualCapacity[t] += delta;
				excess[u] -= delta;
				excess[v] += delta;
				if (excess[v] > 0 && excess[v] <= delta)
					activeQueue.enqueue(v);
			}
			onPath.clear(searchSource);
			assert onPath.isEmpty();
			path.clear();
			// TODO optimization, backup dfs until all edges in path are admissible
		}

		private void globalUpdate() {
			final int n = g.vertices().size();
			final int bucketEnd = n + 1;

			/* Set all ranks of vertices with demand to zero, and other vertices to rankUpperBound */
			Arrays.fill(bucketsHeads, bucketEnd);
			int excessSum = 0;
			int demandVerticesListHead = bucketEnd;
			for (int v : range(n)) {
				if (excess[v] < 0) {
					rank[v] = 0;
					buckets.setNext(v, demandVerticesListHead);
					buckets.setPrev(demandVerticesListHead, v);
					demandVerticesListHead = v;
				} else {
					excessSum += excess[v];
					rank[v] = rankUpperBound;
				}
			}
			if (excessSum == 0)
				return;
			bucketsHeads[0] = demandVerticesListHead;

			int r;
			for (r = 0; r < rankUpperBound; r++) {
				while (bucketsHeads[r] != bucketEnd) {
					int u = bucketsHeads[r];
					bucketsHeads[r] = buckets.next(u);

					long uPotential = potential[u];
					for (IEdgeIter it = edgeIter[u];;) {
						if (!it.hasNext()) {
							edgeIter[u] = g.outEdges(u).iterator();
							break;
						}
						/* e is an in-edge of u */
						int e = twin[it.nextInt()];
						if (!isResidual(e))
							continue;
						int v = g.edgeSource(e);
						int vRankOld = rank[v];
						if (r >= vRankOld)
							continue;

						long nrc = (cost[e] + potential[v] - uPotential) / eps;
						int vRankNew = vRankOld;
						if (nrc < rankUpperBound)
							vRankNew = r + 1 + (int) nrc;

						/* Change the rank of v */
						if (vRankNew < vRankOld) {
							rank[v] = vRankNew;
							edgeIter[v] = g.outEdges(v).iterator();

							if (vRankOld < rankUpperBound) {
								if (bucketsHeads[vRankOld] == v) {
									bucketsHeads[vRankOld] = buckets.next(v);
								} else {
									buckets.disconnect(v);
								}
							}

							buckets.connect(v, bucketsHeads[vRankNew]);
							bucketsHeads[vRankNew] = v;
						}
					}

					if (excess[u] > 0) {
						excessSum -= excess[u];
						if (excessSum <= 0)
							/* no active vertices, we are done */
							break;
					}
				}
				if (excessSum <= 0)
					break;
			}

			/* relabel vertices */
			for (int v : range(n)) {
				int k = Math.min(rank[v], r);
				if (k > 0) {
					potential[v] -= eps * k;
					edgeIter[v] = g.outEdges(v).iterator();
				}
			}

			relabelsSinceLastGlobalUpdate = 0;
		}

		private boolean potentialRefinement() {
			for (;;) {
				boolean topologicalOrderFound = computeTopologicalOrder();
				if (!topologicalOrderFound)
					return false;

				Arrays.fill(rank, 0);
				final int bucketEnd = g.vertices().size();
				Arrays.fill(bucketsHeads, bucketEnd);

				int maxRank = 0;
				while (!topologicalOrder.isEmpty()) {
					int u = topologicalOrder.popInt();

					int uRank = rank[u];
					long uPotential = potential[u];
					for (int e : g.outEdges(u)) {
						if (!isResidual(e))
							continue;
						int v = g.edgeTarget(e);
						long reducedCost = cost[e] + uPotential - potential[v];
						if (reducedCost >= 0)
							continue;
						long k = (long) ((-reducedCost - 0.5) / eps);
						if (k < rankUpperBound) {
							int vRankNew = uRank + (int) k;
							rank[v] = Math.max(rank[v], vRankNew);
						}
					}

					if (uRank > 0) {
						maxRank = Math.max(maxRank, uRank);
						buckets.connect(u, bucketsHeads[uRank]);
						bucketsHeads[uRank] = u;
					}
				}

				if (maxRank == 0)
					/* current flow is epsilon-optimal, we are done */
					return true;

				for (int r = maxRank; r > 0; r--) {
					while (bucketsHeads[r] != bucketEnd) {
						int u = bucketsHeads[r];
						bucketsHeads[r] = buckets.next(u);

						long uPotential = potential[u];
						for (int e : g.outEdges(u)) {
							if (!isResidual(e))
								continue;
							int v = g.edgeTarget(e);
							int vRankOld = rank[v];
							if (vRankOld >= r)
								continue;

							long reducedCost = cost[e] + uPotential - potential[v];
							int vRankNew;
							if (reducedCost < 0) {
								vRankNew = r;
							} else {
								long k = reducedCost / eps;
								vRankNew = 0;
								if (k < rankUpperBound)
									vRankNew = r - 1 - (int) k;
							}

							if (vRankNew > vRankOld) {
								rank[v] = vRankNew;

								if (vRankOld > 0) {
									if (bucketsHeads[vRankOld] == v) {
										bucketsHeads[vRankOld] = buckets.next(v);
									} else {
										buckets.disconnect(v);
									}
								}

								buckets.connect(v, bucketsHeads[vRankNew]);
								bucketsHeads[vRankNew] = v;
							}
						}

						/* update potential of u */
						potential[u] -= r * eps;
					}
				}
			}
		}

		/* returns true if a full topological order was computed in the admissible network */
		private boolean computeTopologicalOrder() {
			final int n = g.vertices().size();
			Bitmap visited = new Bitmap(n);
			Bitmap processed = new Bitmap(n);
			int[] backtrack = new int[n];

			for (int v : range(n))
				edgeIter[v] = g.outEdges(v).iterator();

			assert topologicalOrder.isEmpty();
			for (int root = 0; root < n; ++root) {
				if (visited.get(root))
					continue;

				/* Perform a DFS from the current root, trying to find cycles */
				backtrack[root] = -1;
				dfs: for (int u = root;;) {
					visited.set(u);
					long uPotential = potential[u];
					for (IEdgeIter it = edgeIter[u];; it.nextInt()) {
						if (!it.hasNext()) {
							/* No admissible edge from u, go up once in the DFS path */
							assert !processed.get(u);
							processed.set(u);
							topologicalOrder.push(u);
							u = backtrack[u];
							if (u < 0)
								/* No more paths from current root */
								break dfs;

							/* advance edge iterator to next child */
							assert edgeIter[u].hasNext();
							edgeIter[u].nextInt();
							continue dfs;
						}

						/* Use only admissible edges */
						int e = it.peekNextInt();
						if (!isResidual(e))
							continue;
						int v = g.edgeTarget(e);
						if (cost[e] + uPotential - potential[v] >= 0)
							continue;

						if (!visited.get(v)) {
							/* Continue down the DFS in v */
							visited.set(v);
							backtrack[v] = u;
							u = v;
							continue dfs;

						}
						if (!processed.get(v)) {
							/* a cycle was found, no valid topological order exists */

							/* Find the minimum residual capacity along the cycle */
							int delta = residualCapacity[e];
							for (int w = u; w != v;) {
								w = backtrack[w];
								delta = Math.min(delta, residualCapacity[edgeIter[w].peekNextInt()]);
							}

							/* Augment along the cycle */
							residualCapacity[e] -= delta;
							residualCapacity[twin[e]] += delta;
							for (int w = u; w != v;) {
								w = backtrack[w];
								int ca = edgeIter[w].peekNextInt();
								residualCapacity[ca] -= delta;
								residualCapacity[twin[ca]] += delta;
							}

							((IntList) topologicalOrder).clear();
							return false;
						}
					}
				}
			}
			return true;
		}

		private boolean isResidual(int e) {
			return residualCapacity[e] > 0;
		}

	}

}
