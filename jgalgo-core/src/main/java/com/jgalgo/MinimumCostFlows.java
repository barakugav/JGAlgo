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
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.Weights;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.doubles.DoubleArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntList;

class MinimumCostFlows {

	private static abstract class AbstractImplBase implements MinimumCostFlow {

		@Override
		public void computeMinCostMaxFlow(Graph g, FlowNetwork net, WeightFunction cost, int source, int sink) {
			if (g instanceof IndexGraph) {
				computeMinCostMaxFlow((IndexGraph) g, net, cost, source, sink);
				return;
			}

			IndexGraph iGraph = g.indexGraph();
			IndexIdMap viMap = g.indexGraphVerticesMap();
			IndexIdMap eiMap = g.indexGraphEdgesMap();
			FlowNetwork iNet = FlowNetworks.indexNetFromNet(net, eiMap);
			WeightFunction iCost = IndexIdMaps.idToIndexWeightFunc(cost, eiMap);
			int iSource = viMap.idToIndex(source);
			int iSink = viMap.idToIndex(sink);

			computeMinCostMaxFlow(iGraph, iNet, iCost, iSource, iSink);
		}

		@Override
		public void computeMinCostMaxFlow(Graph g, FlowNetwork net, WeightFunction cost, WeightFunction lowerBound,
				int source, int sink) {
			if (g instanceof IndexGraph) {
				computeMinCostMaxFlow((IndexGraph) g, net, cost, lowerBound, source, sink);
				return;
			}

			IndexGraph iGraph = g.indexGraph();
			IndexIdMap viMap = g.indexGraphVerticesMap();
			IndexIdMap eiMap = g.indexGraphEdgesMap();
			FlowNetwork iNet = FlowNetworks.indexNetFromNet(net, eiMap);
			WeightFunction iCost = IndexIdMaps.idToIndexWeightFunc(cost, eiMap);
			WeightFunction iLowerBound = IndexIdMaps.idToIndexWeightFunc(lowerBound, eiMap);
			int iSource = viMap.idToIndex(source);
			int iSink = viMap.idToIndex(sink);

			computeMinCostMaxFlow(iGraph, iNet, iCost, iLowerBound, iSource, iSink);
		}

		@Override
		public void computeMinCostMaxFlow(Graph g, FlowNetwork net, WeightFunction cost, IntCollection sources,
				IntCollection sinks) {
			if (g instanceof IndexGraph) {
				computeMinCostMaxFlow((IndexGraph) g, net, cost, sources, sinks);
				return;
			}

			IndexGraph iGraph = g.indexGraph();
			IndexIdMap viMap = g.indexGraphVerticesMap();
			IndexIdMap eiMap = g.indexGraphEdgesMap();
			FlowNetwork iNet = FlowNetworks.indexNetFromNet(net, eiMap);
			WeightFunction iCost = IndexIdMaps.idToIndexWeightFunc(cost, eiMap);
			IntCollection iSources = IndexIdMaps.idToIndexCollection(sources, viMap);
			IntCollection iSinks = IndexIdMaps.idToIndexCollection(sinks, viMap);

			computeMinCostMaxFlow(iGraph, iNet, iCost, iSources, iSinks);
		}

		@Override
		public void computeMinCostMaxFlow(Graph g, FlowNetwork net, WeightFunction cost, WeightFunction lowerBound,
				IntCollection sources, IntCollection sinks) {
			if (g instanceof IndexGraph) {
				computeMinCostMaxFlow((IndexGraph) g, net, cost, lowerBound, sources, sinks);
				return;
			}

			IndexGraph iGraph = g.indexGraph();
			IndexIdMap viMap = g.indexGraphVerticesMap();
			IndexIdMap eiMap = g.indexGraphEdgesMap();
			FlowNetwork iNet = FlowNetworks.indexNetFromNet(net, eiMap);
			WeightFunction iCost = IndexIdMaps.idToIndexWeightFunc(cost, eiMap);
			WeightFunction iLowerBound = IndexIdMaps.idToIndexWeightFunc(lowerBound, eiMap);
			IntCollection iSources = IndexIdMaps.idToIndexCollection(sources, viMap);
			IntCollection iSinks = IndexIdMaps.idToIndexCollection(sinks, viMap);

			computeMinCostMaxFlow(iGraph, iNet, iCost, iLowerBound, iSources, iSinks);
		}

		@Override
		public void computeMinCostFlow(Graph g, FlowNetwork net, WeightFunction cost, WeightFunction demand) {
			if (g instanceof IndexGraph) {
				computeMinCostFlow((IndexGraph) g, net, cost, demand);
				return;
			}

			IndexGraph iGraph = g.indexGraph();
			IndexIdMap viMap = g.indexGraphVerticesMap();
			IndexIdMap eiMap = g.indexGraphEdgesMap();
			FlowNetwork iNet = FlowNetworks.indexNetFromNet(net, eiMap);
			WeightFunction iCost = IndexIdMaps.idToIndexWeightFunc(cost, eiMap);
			WeightFunction iDemand = IndexIdMaps.idToIndexWeightFunc(demand, viMap);

			computeMinCostFlow(iGraph, iNet, iCost, iDemand);
		}

		@Override
		public void computeMinCostFlow(Graph g, FlowNetwork net, WeightFunction cost, WeightFunction lowerBound,
				WeightFunction demand) {
			if (g instanceof IndexGraph) {
				computeMinCostFlow((IndexGraph) g, net, cost, lowerBound, demand);
				return;
			}

			IndexGraph iGraph = g.indexGraph();
			IndexIdMap viMap = g.indexGraphVerticesMap();
			IndexIdMap eiMap = g.indexGraphEdgesMap();
			FlowNetwork iNet = FlowNetworks.indexNetFromNet(net, eiMap);
			WeightFunction iCost = IndexIdMaps.idToIndexWeightFunc(cost, eiMap);
			WeightFunction iLowerBound = IndexIdMaps.idToIndexWeightFunc(lowerBound, eiMap);
			WeightFunction iDemand = IndexIdMaps.idToIndexWeightFunc(demand, viMap);

			computeMinCostFlow(iGraph, iNet, iCost, iLowerBound, iDemand);
		}

		abstract void computeMinCostMaxFlow(IndexGraph g, FlowNetwork net, WeightFunction cost, int source, int sink);

		abstract void computeMinCostMaxFlow(IndexGraph g, FlowNetwork net, WeightFunction cost,
				WeightFunction lowerBound, int source, int sink);

		abstract void computeMinCostMaxFlow(IndexGraph g, FlowNetwork net, WeightFunction cost, IntCollection sources,
				IntCollection sinks);

		abstract void computeMinCostMaxFlow(IndexGraph g, FlowNetwork net, WeightFunction cost,
				WeightFunction lowerBound, IntCollection sources, IntCollection sinks);

		abstract void computeMinCostFlow(IndexGraph g, FlowNetwork net, WeightFunction cost, WeightFunction demand);

		abstract void computeMinCostFlow(IndexGraph g, FlowNetwork net, WeightFunction cost, WeightFunction lowerBound,
				WeightFunction demand);

	}

	static abstract class AbstractImpl extends AbstractImplBase {

		@Override
		void computeMinCostMaxFlow(IndexGraph g, FlowNetwork net, WeightFunction cost, WeightFunction lowerBound,
				int source, int sink) {
			computeMinCostMaxFlow(g, net, cost, lowerBound, IntList.of(source), IntList.of(sink));
		}

		@Override
		void computeMinCostMaxFlow(IndexGraph gOrig, FlowNetwork netOrig, WeightFunction costOrig,
				WeightFunction lowerBound, IntCollection sources, IntCollection sinks) {
			Assertions.Graphs.onlyDirected(gOrig);
			checkLowerBound(gOrig, netOrig, lowerBound);

			/*
			 * To solve the problem of minimum-cost maximum-flow between a set of sources and sinks, with a flow lower
			 * bound for each edge, we perform a reduction to min-cost max-flow between a single source and a sink sink
			 * without lower bounds. To get rid of the lower bound, remove from each edge capacity its lower bound, and
			 * add/remove demand from the edge endpoints. This reduction is slightly more complicated than the others,
			 * as some vertices (the sources/sinks) require 'infinite' demand, while others (other vertices with demand)
			 * require finite demand. We create a new graph with all the vertices and edges of the original graph, with
			 * addition of a new source and sink, and connect the source to the sources with high capacity edges, the
			 * source to vertices with a positive demand with capacity equal to the demand, the sinks to the sink with
			 * high capacity edges and lastly the vertices with negative demand to the sink with capacity equal to the
			 * demand.
			 */

			/* For each edge with lower bound add/remove demand to the edge endpoints */
			WeightFunction demand = computeDemand(gOrig, netOrig, lowerBound, null);

			/* Add all original vertices and edges */
			IndexGraphBuilder builder = IndexGraphBuilder.newDirected();
			for (int n = gOrig.vertices().size(), v = 0; v < n; v++)
				builder.addVertex();
			for (int m = gOrig.edges().size(), e = 0; e < m; e++)
				builder.addEdge(gOrig.edgeSource(e), gOrig.edgeTarget(e));
			/* any edge with index smaller than this threshold is an original edge of the graph */
			final int origEdgesThreshold = builder.edges().size();

			/* determine a great enough capacity ('infinite') for edges to sources (from sinks) */
			double maxCap = 0;
			for (int m = gOrig.edges().size(), e = 0; e < m; e++)
				maxCap = Math.max(maxCap, netOrig.getCapacity(e) - lowerBound.weight(e));
			final double sourceSinkEdgeCapacity = maxCap * gOrig.vertices().size();

			/* Add two artificial terminal vertices, a source and a sink */
			final int source = builder.addVertex();
			final int sink = builder.addVertex();

			DoubleArrayList capacities = new DoubleArrayList();
			BitSet origSourcesSinksSet = new BitSet(gOrig.vertices().size());
			/* Connect the source to the sources with high capacity edges */
			for (int s : sources) {
				builder.addEdge(source, s);
				capacities.add(sourceSinkEdgeCapacity);
				assert !origSourcesSinksSet.get(s);
				origSourcesSinksSet.set(s);
			}
			/* Connect the sinks to the sink with high capacity edges */
			for (int t : sinks) {
				builder.addEdge(t, sink);
				capacities.add(sourceSinkEdgeCapacity);
				assert !origSourcesSinksSet.get(t);
				origSourcesSinksSet.set(t);
			}
			/*
			 * Any edge with index smaller than this threshold and equal or greater than origEdgesThreshold is an edge
			 * connect source-sources or sinks-sink. Any edge with index greater or equal to this threshold is an edge
			 * connecting a source to a vertex with positive demand or a vertex with negative demand to a sink.
			 */
			final int sourcesSinksThreshold = builder.edges().size();

			/*
			 * Connect the source to all vertices with positive demand and the vertices with negative demand to the sink
			 */
			for (int n = gOrig.vertices().size(), v = 0; v < n; v++) {
				if (origSourcesSinksSet.get(v))
					continue;
				double d = demand.weight(v);
				if (d > 0) {
					builder.addEdge(source, v);
					capacities.add(d);
				} else if (d < 0) {
					builder.addEdge(v, sink);
					capacities.add(-d);
				}
			}

			IndexGraph g = builder.build();

			/*
			 * Create a network for the new graph by storing capacities and flows of the artificial edges and by
			 * reducing the capacities of edges by their lower bound
			 */
			FlowNetwork net = new FlowNetwork() {
				double[] caps = capacities.elements();
				double[] flows = new double[capacities.size()];

				@Override
				public double getCapacity(int edge) {
					if (edge < origEdgesThreshold)
						return netOrig.getCapacity(edge) - lowerBound.weight(edge);
					return caps[edge - origEdgesThreshold];
				}

				@Override
				public void setCapacity(int edge, double capacity) {
					throw new UnsupportedOperationException();
				}

				@Override
				public double getFlow(int edge) {
					if (edge < origEdgesThreshold)
						return netOrig.getFlow(edge) - lowerBound.weight(edge);
					return flows[edge - origEdgesThreshold];
				}

				@Override
				public void setFlow(int edge, double flow) {
					if (edge < origEdgesThreshold) {
						netOrig.setFlow(edge, flow + lowerBound.weight(edge));
					} else {
						flows[edge - origEdgesThreshold] = flow;
					}
				}
			};

			/*
			 * Create a cost function for the new graph: original edges have their original costs, big negative cost for
			 * edges that connect vertices with demand as we must satisfy them, and zero cost for edges connecting
			 * source-sources or sinks-sink
			 */
			double maxCost = 0;
			for (int m = gOrig.edges().size(), e = 0; e < m; e++)
				maxCost = Math.max(maxCost, Math.abs(costOrig.weight(e)));
			final double demandEdgeCost = -maxCost * gOrig.vertices().size();
			WeightFunction cost = e -> {
				if (e < origEdgesThreshold)
					return costOrig.weight(e); /* original edge */
				if (e < sourcesSinksThreshold)
					return 0; /* edge to source/sink */
				return demandEdgeCost; /* edge to a non source/sink vertex with demand */
			};

			/* Compute a min-cost max-flow in the new graph and network */
			computeMinCostMaxFlow(g, net, cost, source, sink);
		}

		@Override
		void computeMinCostFlow(IndexGraph gOrig, FlowNetwork netOrig, WeightFunction costOrig, WeightFunction demand) {
			Assertions.Graphs.onlyDirected(gOrig);
			checkDemand(gOrig, demand);

			/*
			 * To solve the minimum cost flow of given demand we use a reduction to minimum-cost maximum-flow between
			 * two terminal vertices, source and sink. We add an edge from the source to each vertex with positive
			 * demand with capacity equal to the demand, and an edge from each vertex with negative demand to the sink
			 * with capacity equal to the demand.
			 */

			/* Add all original vertices and edges */
			IndexGraphBuilder builder = IndexGraphBuilder.newDirected();
			for (int n = gOrig.vertices().size(), v = 0; v < n; v++)
				builder.addVertex();
			for (int m = gOrig.edges().size(), e = 0; e < m; e++)
				builder.addEdge(gOrig.edgeSource(e), gOrig.edgeTarget(e));
			/* any edge with index greater than this threshold is not an original edge of the graph */
			final int origEdgesThreshold = builder.edges().size();

			/* Add two artificial vertices, source and sink */
			final int source = builder.addVertex();
			final int sink = builder.addVertex();

			/* Connect the source to vertices with positive demand and vertices with negative demand to the sink */
			DoubleArrayList capacities = new DoubleArrayList();
			for (int n = gOrig.vertices().size(), v = 0; v < n; v++) {
				double d = demand.weight(v);
				if (d > 0) {
					builder.addEdge(source, v);
					capacities.add(d);
				} else if (d < 0) {
					builder.addEdge(v, sink);
					capacities.add(-d);
				}
			}

			IndexGraph g = builder.build();

			/*
			 * Create a network for the new graph by using two new arrays for the artificial edges capacities and flows
			 */
			FlowNetwork net = new FlowNetwork() {
				double[] caps = capacities.elements();
				double[] flows = new double[capacities.size()];

				@Override
				public double getCapacity(int edge) {
					return edge < origEdgesThreshold ? netOrig.getCapacity(edge) : caps[edge - origEdgesThreshold];
				}

				@Override
				public void setCapacity(int edge, double capacity) {
					throw new UnsupportedOperationException();
				}

				@Override
				public double getFlow(int edge) {
					return edge < origEdgesThreshold ? netOrig.getFlow(edge) : flows[edge - origEdgesThreshold];
				}

				@Override
				public void setFlow(int edge, double flow) {
					if (edge < origEdgesThreshold) {
						netOrig.setFlow(edge, flow);
					} else {
						flows[edge - origEdgesThreshold] = flow;
					}
				}
			};
			/*
			 * All the artificial edges should not have a cost, if its possible to satisfy the demand they will be
			 * saturated anyway
			 */
			WeightFunction cost = e -> e < origEdgesThreshold ? costOrig.weight(e) : 0;

			/* Compute a minimum-cost maximum-flow between the two artificial vertices */
			computeMinCostMaxFlow(g, net, cost, source, sink);
		}

		@Override
		void computeMinCostFlow(IndexGraph g, FlowNetwork netOrig, WeightFunction cost, WeightFunction lowerBound,
				WeightFunction demand) {
			Assertions.Graphs.onlyDirected(g);
			checkLowerBound(g, netOrig, lowerBound);
			checkDemand(g, demand);

			/*
			 * To solve the minimum cost flow for a given demand and edges lower bounds, we perform a reduction to the
			 * problem with given demand without any edges flow lower bounds. For each edge with lower bound we subtract
			 * the lower bound from the capacity of the edge, and add/remove demand to the edge endpoints.
			 */

			/* Create a network by subtracting the lower bound from each edge capacity */
			FlowNetwork net = new FlowNetwork() {
				@Override
				public double getCapacity(int edge) {
					return netOrig.getCapacity(edge) - lowerBound.weight(edge);
				}

				@Override
				public void setCapacity(int edge, double capacity) {
					throw new UnsupportedOperationException();
				}

				@Override
				public double getFlow(int edge) {
					return netOrig.getFlow(edge) - lowerBound.weight(edge);
				}

				@Override
				public void setFlow(int edge, double flow) {
					netOrig.setFlow(edge, flow + lowerBound.weight(edge));
				}
			};

			/* For each edge with lower bound we add/remove demand from the end endpoints */
			WeightFunction demand2 = computeDemand(g, netOrig, lowerBound, demand);

			/* Solve the reduction problem with only demand without edges lower bounds */
			computeMinCostFlow(g, net, cost, demand2);
		}

		private static WeightFunction computeDemand(IndexGraph g, FlowNetwork netOrig, WeightFunction lowerBound,
				WeightFunction demand) {
			Weights.Double demand2 = Weights.createExternalVerticesWeights(g, double.class);
			if (demand != null) {
				for (int n = g.vertices().size(), v = 0; v < n; v++)
					demand2.set(v, demand.weight(v));
			}
			if (lowerBound != null) {
				for (int m = g.edges().size(), e = 0; e < m; e++) {
					double l = lowerBound.weight(e);
					netOrig.setFlow(e, l);
					int u = g.edgeSource(e), v = g.edgeTarget(e);
					demand2.set(u, demand2.getDouble(u) + l);
					demand2.set(v, demand2.getDouble(v) - l);
				}
			}
			return demand2;
		}

		static void checkLowerBound(IndexGraph g, FlowNetwork net, WeightFunction lowerBound) {
			for (int m = g.edges().size(), e = 0; e < m; e++) {
				double l = lowerBound.weight(e);
				if (!(0 <= l && l <= net.getCapacity(e)))
					throw new IllegalArgumentException("Lower bound must be in [0, capacity] for edge " + e);
			}
		}

		static void checkDemand(IndexGraph g, WeightFunction demand) {
			double sum = 0;
			for (int n = g.vertices().size(), v = 0; v < n; v++) {
				double d = demand.weight(v);
				if (!Double.isFinite(d))
					throw new IllegalArgumentException("Demand must be non-negative for vertex " + v);
				sum += d;
			}
			if (sum != 0)
				throw new IllegalArgumentException("Sum of demand must be zero");
		}

	}

}
