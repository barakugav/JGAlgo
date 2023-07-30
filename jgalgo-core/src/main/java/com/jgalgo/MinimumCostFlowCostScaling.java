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
import com.jgalgo.FlowNetworks.ResidualGraph;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.data.LinkedListFixedSize;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntStack;

/**
 * Minimum-cost flow computation using the cost-scaling algorithm with partial-augmentations push-relabel variant.
 * <p>
 * The algorithm runs in \(O(n^2 m \log (n C))\) where \(C\) is the maximum (absolute) edge cost.
 * <p>
 * Based on 'Efficient implementations of minimum-cost flow algorithms' by Z. Kiraly, P. Kovacs (2012).
 *
 * @see    MaximumFlowPushRelabelPartialAugment
 * @author Barak Ugav
 */
class MinimumCostFlowCostScaling extends MinimumCostFlows.AbstractImpl {

	private static final int alpha = 16;

	@Override
	void computeMinCostFlow(IndexGraph gOrig, FlowNetwork net, WeightFunction cost, WeightFunction supply) {
		if (!(net instanceof FlowNetwork.Int && supply instanceof WeightFunction.Int))
			throw new IllegalArgumentException("only integer capacities and flows are supported");
		if (!(cost instanceof WeightFunction.Int))
			throw new IllegalArgumentException("only integer costs are supported");
		new Worker(gOrig, (FlowNetwork.Int) net, (WeightFunction.Int) cost, (WeightFunction.Int) supply).solve();
	}

	private static class Worker {

		/* g is a residual graph, duplicating each edge in the original graph */
		private final IndexGraph g;
		private final ResidualGraph resGraph;
		private final FlowNetwork.Int net;

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
		private final BitSet onPath;
		/* Per vertex iterator, corresponding to 'current edge' in the paper */
		private final EdgeIter[] edgeIter;

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

		Worker(IndexGraph gOrig, FlowNetwork.Int net, WeightFunction.Int costOrig, WeightFunction.Int supply) {
			Assertions.Graphs.onlyDirected(gOrig);
			Assertions.Flows.checkSupply(gOrig, supply);
			this.net = net;

			/* Build the residual graph by duplicating each edge in the original graph */
			FlowNetworks.ResidualGraph.Builder b = new FlowNetworks.ResidualGraph.Builder(gOrig);
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
			for (int e = 0; e < m; e++) {
				long c = costOrig.weightInt(edgeRef[e]);
				c = resGraph.isOriginalEdge(e) ? c : -c;
				/* multiply all costs by \alpha n so costs will be integers when we scale them */
				cost[e] = c * n * alpha;
				maxCost = Math.max(maxCost, cost[e]);
			}
			eps = maxCost / alpha;

			/* Find a valid circulation that satisfy the supply, without considering costs */
			FlowCirculation circulation = new FlowCirculationPushRelabel();
			circulation.computeCirculation(gOrig, net, supply);

			/* init residual capacities */
			residualCapacity = new int[m];
			for (int e = 0; e < m; e++) {
				int eRef = edgeRef[e];
				if (resGraph.isOriginalEdge(e)) {
					residualCapacity[e] = net.getCapacityInt(eRef) - net.getFlowInt(eRef);
				} else {
					residualCapacity[e] = net.getFlowInt(eRef);
				}
			}

			activeQueue = new FIFOQueueIntNoReduce(n);
			path = new IntArrayList(MAX_AUGMENT_PATH_LENGTH);
			onPath = new BitSet(n);
			edgeIter = new EdgeIter[n];

			globalUpdateThreshold = n;

			topologicalOrder = new IntArrayList(n);
			rank = new int[n];
			rankUpperBound = alpha * n;
			buckets = new LinkedListFixedSize.Doubly(n + 1);
			bucketsHeads = new int[rankUpperBound];
		}

		void solve() {
			solveWithPartialAugment();

			for (int n = g.vertices().size(), u = 0; u < n; u++)
				potential[u] /= n * alpha;

			long maxPotential = 0;
			for (int n = g.vertices().size(), u = 0; u < n; u++)
				if (maxPotential < potential[u])
					maxPotential = potential[u];
			if (maxPotential != 0)
				for (int n = g.vertices().size(), u = 0; u < n; u++)
					potential[u] -= maxPotential;

			final int edgeRef[] = resGraph.edgeRef;
			for (int m = g.edges().size(), e = 0; e < m; e++) {
				if (resGraph.isOriginalEdge(e)) {
					int eRef = edgeRef[e];
					int capacity = net.getCapacityInt(eRef);
					net.setFlow(eRef, capacity - residualCapacity[e]);
				}
			}
		}

		private void solveWithPartialAugment() {
			for (int epsIter = 0; eps >= 1; eps = ((eps < alpha && eps > 1) ? 1 : eps / alpha), epsIter++) {
				if (epsIter >= POTENTIAL_REFINEMENT_ITERATION_SKIP)
					if (potentialRefinement())
						continue;

				/* Saturate all edges with negative cost */
				for (int n = g.vertices().size(), u = 0; u < n; u++) {
					long uPotential = potential[u];
					for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						int delta = residualCapacity[e];
						if (delta > 0) {
							int v = eit.target();
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

				/* Find all active nodes */
				assert activeQueue.isEmpty();
				for (int n = g.vertices().size(), u = 0; u < n; u++) {
					if (excess[u] > 0)
						activeQueue.enqueue(u);
					edgeIter[u] = g.outEdges(u).iterator();
				}

				activeVerticesLoop: for (;;) {
					/* Find next active node in FIFO queue */
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
			 * We perform a DFS from the 'searchSource' node, using only admissible edges. If we manage to reach path of
			 * length MAX_AUGMENT_PATH_LENGTH, or we reached a vertex with negative excess, or we closed a cycle, we
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
				final int firstEdge = edgeIter[u].peekNext();
				for (EdgeIter eit = edgeIter[u]; /* eit.hasNext() */;) {
					assert eit.hasNext();
					int e = eit.peekNext();
					if (residualCapacity[e] > 0) {
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
						for (EdgeIter eit2 = g.outEdges(u).iterator();;) {
							assert eit2.hasNext();
							int e2 = eit2.nextInt();
							if (e2 == firstEdge)
								break;
							if (residualCapacity[e2] > 0) {
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
			// TODO optimization, implement this func

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
						if (residualCapacity[e] <= 0)
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
							if (residualCapacity[e] <= 0)
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
			BitSet visited = new BitSet(n);
			BitSet processed = new BitSet(n);
			int[] backtrack = new int[n];

			for (int v = 0; v < n; v++)
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
					for (EdgeIter it = edgeIter[u];; it.nextInt()) {
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
						int e = it.peekNext();
						if (residualCapacity[e] <= 0)
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
								delta = Math.min(delta, residualCapacity[edgeIter[w].peekNext()]);
							}

							/* Augment along the cycle */
							residualCapacity[e] -= delta;
							residualCapacity[twin[e]] += delta;
							for (int w = u; w != v;) {
								w = backtrack[w];
								int ca = edgeIter[w].peekNext();
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
	}

	@Override
	void computeMinCostMaxFlow(IndexGraph gOrig, FlowNetwork net, WeightFunction cost, int source, int sink) {
		computeMinCostMaxFlow(gOrig, net, cost, IntList.of(source), IntList.of(sink));
	}

	@Override
	void computeMinCostMaxFlow(IndexGraph gOrig, FlowNetwork netOrig, WeightFunction costOrig, IntCollection sources,
			IntCollection sinks) {
		Assertions.Graphs.onlyDirected(gOrig);
		Assertions.Flows.sourcesSinksNotTheSame(sources, sinks);

		if (!(netOrig instanceof FlowNetwork.Int))
			throw new IllegalArgumentException("only integer capacities and flows are supported");
		if (!(costOrig instanceof WeightFunction.Int))
			throw new IllegalArgumentException("only integer costs are supported");
		FlowNetwork.Int netOrigInt = (FlowNetwork.Int) netOrig;
		WeightFunction.Int costOrigInt = (WeightFunction.Int) costOrig;

		final int hugeCapacity = FlowNetworks.hugeCapacity(gOrig, netOrigInt, sources, sinks);

		final int hugeCost = hugeCost(gOrig, costOrigInt);

		IndexGraphBuilder builder = IndexGraphBuilder.newDirected();
		for (int n = gOrig.vertices().size(), v = 0; v < n; v++)
			builder.addVertex();
		for (int m = gOrig.edges().size(), e = 0; e < m; e++)
			builder.addEdge(gOrig.edgeSource(e), gOrig.edgeTarget(e));
		/* any edge with index smaller than this threshold is an original edge of the graph */
		final int origEdgesThreshold = builder.edges().size();

		int source = builder.addVertex();
		int sink = builder.addVertex();
		for (int v : sources)
			builder.addEdge(source, v);
		for (int v : sinks)
			builder.addEdge(v, sink);
		/*
		 * Any edge with index smaller than this threshold and equal or greater than origEdgesThreshold is an edge
		 * connect source-sources or sinks-sink. Any edge with index greater or equal to this threshold is an edge
		 * connecting the super source and the super sink.
		 */
		final int sourcesSinksThreshold = builder.edges().size();

		builder.addEdge(source, sink);
		builder.addEdge(sink, source);

		IndexGraph g = builder.build();

		FlowNetwork.Int net = new FlowNetwork.Int() {
			int[] flows = new int[builder.edges().size() - origEdgesThreshold];

			@Override
			public int getCapacityInt(int edge) {
				return edge < origEdgesThreshold ? netOrigInt.getCapacityInt(edge) : hugeCapacity;
			}

			@Override
			public void setCapacity(int edge, int capacity) {
				throw new UnsupportedOperationException();
			}

			@Override
			public int getFlowInt(int edge) {
				return edge < origEdgesThreshold ? netOrigInt.getFlowInt(edge) : flows[edge - origEdgesThreshold];
			}

			@Override
			public void setFlow(int edge, int flow) {
				if (edge < origEdgesThreshold) {
					netOrigInt.setFlow(edge, flow);
				} else {
					flows[edge - origEdgesThreshold] = flow;
				}
			}
		};
		WeightFunction.Int cost = e -> {
			if (e < origEdgesThreshold)
				return costOrigInt.weightInt(e);
			if (e < sourcesSinksThreshold)
				return 0;
			return hugeCost;
		};
		WeightFunction.Int supply = v -> {
			if (v == source)
				return hugeCapacity;
			if (v == sink)
				return -hugeCapacity;
			return 0;
		};

		computeMinCostFlow(g, net, cost, supply);
	}

}
