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
import com.jgalgo.FlowNetworks.ResidualGraph;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

/**
 * Compute the minimum-cost (max) flow in a flow network using cycle canceling.
 * <p>
 * Firstly, a maximum flow is computed using {@link MaximumFlow}. Then, the residual graph is constructed from the max
 * flow (containing only non-saturated edges), and negative cycles (with respect to the cost function) are eliminated
 * from it repeatedly until no negative cycles remain.
 * <p>
 * Based on 'A Primal Method for Minimal Cost Flows with Applications to the Assignment and Transportation Problems' by
 * M Klein (1966).
 *
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

		private final IndexGraph g;
		private final ResidualGraph resGraph;
		private final FlowNetwork.Int net;

		private final int[] residualCapacity;
		private final int[] cost;
		private final int[] twin;

		private final int[] potential;
		private final int[] excess;
		private int eps;

		private final IntPriorityQueue activeQueue;
		private final IntArrayList path;
		private final BitSet onPath;
		private final EdgeIter[] edgeIter;

		private int relabelsSinceLastGlobalUpdate;
		private final int globalUpdateThreshold;

		private static final int MAX_AUGMENT_PATH_LENGTH = 4;
		private static final int POTENTIAL_REFINEMENT_ITERATION_SKIP = 2;

		Worker(IndexGraph gOrig, FlowNetwork.Int net, WeightFunction.Int costOrig, WeightFunction.Int supply) {
			Assertions.Graphs.onlyDirected(gOrig);
			this.net = net;

			// TODO verify valid supply

			FlowNetworks.ResidualGraph.Builder b = new FlowNetworks.ResidualGraph.Builder(gOrig);
			b.addAllOriginalEdges();
			resGraph = b.build();
			g = resGraph.g;
			final int[] edgeRef = resGraph.edgeRef;
			twin = resGraph.twin;
			final int n = g.vertices().size();
			final int m = g.edges().size();

			potential = new int[n];
			excess = new int[n];
			/* we don't init excess to the 'supply' function because the circulation will zero it */

			cost = new int[m];
			int maxCost = 1;
			for (int e = 0; e < m; e++) {
				int c = costOrig.weightInt(edgeRef[e]);
				c = resGraph.isOriginalEdge(e) ? c : -c;
				/* multiply all costs by \alpha n so costs will be integers when we scale them */
				cost[e] = c * n * alpha;
				maxCost = Math.max(maxCost, cost[e]);
			}
			eps = maxCost / alpha;

			FlowCirculation circulation = new FlowCirculationPushRelabel();
			circulation.computeCirculation(gOrig, net, supply);

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
		}

		void solve() {
			startAugment();

			for (int n = g.vertices().size(), u = 0; u < n; u++)
				potential[u] /= n * alpha;

			int maxPotential = 0;
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

		private void startAugment() {
			for (int eps_iter = 0; eps >= 1; eps = ((eps < alpha && eps > 1) ? 1 : eps / alpha), eps_iter++) {
				if (eps_iter >= POTENTIAL_REFINEMENT_ITERATION_SKIP)
					if (potentialRefinement())
						continue;

				// Saturate arcs not satisfying the optimality condition
				for (int n = g.vertices().size(), u = 0; u < n; u++) {
					int uPotential = potential[u];
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
				// Find active nodes (i.e. nodes with positive excess)
				// Initialize the next arcs
				assert activeQueue.isEmpty();
				for (int n = g.vertices().size(), u = 0; u < n; u++) {
					if (excess[u] > 0)
						activeQueue.enqueue(u);
					edgeIter[u] = g.outEdges(u).iterator();
				}

				activeVerticesLoop: for (;;) {
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

					discharge(searchSource);
					if (excess[searchSource] > 0)
						activeQueue.enqueue(searchSource);

					if (relabelsSinceLastGlobalUpdate >= globalUpdateThreshold)
						globalUpdate();
				}
			}
		}

		private void discharge(int searchSource) {
			assert path.isEmpty();
			assert onPath.isEmpty();
			onPath.set(searchSource);
			dfs: for (int tip = searchSource;;) {
				int tipPotential = potential[tip];
				int minResidualCost = Integer.MAX_VALUE;
				assert edgeIter[tip].hasNext();
				final int firstEdge = edgeIter[tip].peekNext();
				for (EdgeIter eit = edgeIter[tip]; /* eit.hasNext() */;) {
					assert eit.hasNext();
					int e = eit.peekNext();
					if (residualCapacity[e] > 0) {
						int v = g.edgeTarget(e);
						int residualCost = cost[e] + tipPotential - potential[v];
						if (residualCost < 0) {
							path.add(e);
							if (path.size() == MAX_AUGMENT_PATH_LENGTH || excess[v] < 0 || onPath.get(v)) {
								/* augmentation path (maybe cycle) was found */
								pushOnPath(searchSource);
								return;
							}

							/* continue down in the DFS */
							tip = v;
							onPath.set(v);
							continue dfs;
						} else if (minResidualCost > residualCost) {
							minResidualCost = residualCost;
						}
					}
					eit.nextInt();
					if (!eit.hasNext()) {
						/* we finish iterating over all edges of the vertex, relabel it */
						if (tip != searchSource) {
							assert !path.isEmpty();
							int lastEdge = path.getInt(path.size() - 1);
							int lastTwin = twin[lastEdge];
							int residualCost = cost[lastTwin] + tipPotential - potential[g.edgeTarget(lastTwin)];
							if (minResidualCost > residualCost)
								minResidualCost = residualCost;
						}
						for (EdgeIter eit2 = g.outEdges(tip).iterator();;) {
							assert eit2.hasNext();
							int e2 = eit2.nextInt();
							if (e2 == firstEdge)
								break;
							if (residualCapacity[e2] > 0) {
								int residualCost = cost[e2] + tipPotential - potential[g.edgeTarget(e2)];
								if (minResidualCost > residualCost)
									minResidualCost = residualCost;
							}

						}
						/* actual 'relabel' */
						assert minResidualCost != Integer.MAX_VALUE;
						potential[tip] -= minResidualCost + eps;
						relabelsSinceLastGlobalUpdate++;
						/* reset tip iterator */
						edgeIter[tip] = g.outEdges(tip).iterator();
						assert edgeIter[tip].hasNext();

						if (tip != searchSource) {
							/* step up in the DFS path */
							assert !path.isEmpty();
							int lastEdge = path.popInt();
							assert tip == g.edgeTarget(lastEdge);
							assert onPath.get(tip);
							onPath.clear(tip);
							tip = g.edgeSource(lastEdge);
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
			assert onPath.get(searchSource);
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
			// TODO optimization, implement this func
			return false;
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

		int sourcesOutCapacity = 0;
		int sinksInCapacity = 0;
		for (int source : sources)
			for (int e : gOrig.outEdges(source))
				sourcesOutCapacity += netOrigInt.getCapacityInt(e);
		for (int sink : sinks)
			for (int e : gOrig.inEdges(sink))
				sinksInCapacity += netOrigInt.getCapacityInt(e);
		final int hugeCapacity = 1 + Math.max(10, Math.max(sourcesOutCapacity, sinksInCapacity));

		int costSum = 0;
		for (int m = gOrig.edges().size(), e = 0; e < m; e++)
			costSum += Math.abs(costOrigInt.weightInt(e));
		final int hugeCost = 1 + costSum;

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