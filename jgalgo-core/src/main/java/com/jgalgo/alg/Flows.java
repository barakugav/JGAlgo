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

import java.util.Collection;
import java.util.Objects;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.NoSuchEdgeException;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSet;

class Flows {

	private Flows() {}

	static class FlowImpl implements IFlow {

		private final IndexGraph g;
		private final double[] flow;

		FlowImpl(IndexGraph g, double[] flow) {
			this.g = g;
			this.flow = flow;
		}

		@Override
		public double getFlow(int e) {
			if (e < 0 || e >= flow.length)
				throw NoSuchEdgeException.ofIndex(e);
			return flow[e];
		}

		@Override
		public double getSupply(int vertex) {
			return getSupplySubset(IntSet.of(vertex));
		}

		@Override
		public double getSupplySubset(Collection<Integer> vertices) {
			IntCollection vs = IntAdapters.asIntCollection(vertices);
			double sum = 0;
			if (g.isDirected()) {
				for (int v : vs) {
					for (int e : g.outEdges(v))
						sum += getFlow(e);
					for (int e : g.inEdges(v))
						sum -= getFlow(e);
				}
			} else {
				for (int v : vs) {
					for (int e : g.outEdges(v)) {
						if (v != g.edgeTarget(e)) {
							sum += getFlow(e);
						} else if (v != g.edgeSource(e)) {
							sum -= getFlow(e);
						}
					}
				}
			}
			return sum;
		}

		@Override
		public double getTotalCost(WeightFunction<Integer> cost) {
			IWeightFunction cost0 = WeightFunctions.asIntGraphWeightFunc(cost);
			double sum = 0;
			if (WeightFunction.isCardinality(cost0)) {
				for (int m = g.edges().size(), e = 0; e < m; e++)
					sum += getFlow(e);

			} else if (WeightFunction.isInteger(cost0)) {
				IWeightFunctionInt costInt = (IWeightFunctionInt) cost;
				for (int m = g.edges().size(), e = 0; e < m; e++)
					sum += getFlow(e) * costInt.weightInt(e);

			} else {
				for (int m = g.edges().size(), e = 0; e < m; e++)
					sum += getFlow(e) * cost0.weight(e);
			}
			return sum;
		}
	}

	@SuppressWarnings("unchecked")
	static <V, E> Flow<V, E> flowFromIndexFlow(Graph<V, E> g, IFlow indexFlow) {
		assert !(g instanceof IndexGraph);
		if (g instanceof IntGraph) {
			return (Flow<V, E>) new IntFlowFromIndexFlow((IntGraph) g, indexFlow);
		} else {
			return new ObjFlowFromIndexFlow<>(g, indexFlow);
		}
	}

	private static class ObjFlowFromIndexFlow<V, E> implements Flow<V, E> {

		private final IFlow indexFlow;
		private final IndexIdMap<V> viMap;
		private final IndexIdMap<E> eiMap;

		ObjFlowFromIndexFlow(Graph<V, E> g, IFlow indexFlow) {
			this.indexFlow = indexFlow;
			this.viMap = g.indexGraphVerticesMap();
			this.eiMap = g.indexGraphEdgesMap();
		}

		@Override
		public double getFlow(E e) {
			return indexFlow.getFlow(eiMap.idToIndex(e));
		}

		@Override
		public double getSupply(V vertex) {
			return indexFlow.getSupply(viMap.idToIndex(vertex));
		}

		@Override
		public double getSupplySubset(Collection<V> vertices) {
			return indexFlow.getSupplySubset(IndexIdMaps.idToIndexCollection(vertices, viMap));
		}

		@Override
		public double getTotalCost(WeightFunction<E> cost) {
			return indexFlow.getTotalCost(IndexIdMaps.idToIndexWeightFunc(cost, eiMap));
		}
	}

	private static class IntFlowFromIndexFlow implements IFlow {

		private final IFlow indexFlow;
		private final IndexIntIdMap viMap;
		private final IndexIntIdMap eiMap;

		IntFlowFromIndexFlow(IntGraph g, IFlow indexFlow) {
			this.indexFlow = indexFlow;
			this.viMap = g.indexGraphVerticesMap();
			this.eiMap = g.indexGraphEdgesMap();
		}

		@Override
		public double getFlow(int e) {
			return indexFlow.getFlow(eiMap.idToIndex(e));
		}

		@Override
		public double getSupply(int vertex) {
			return indexFlow.getSupply(viMap.idToIndex(vertex));
		}

		@Override
		public double getSupplySubset(Collection<Integer> vertices) {
			return indexFlow.getSupplySubset(IndexIdMaps.idToIndexCollection(vertices, viMap));
		}

		@Override
		public double getTotalCost(WeightFunction<Integer> cost) {
			return indexFlow.getTotalCost(IndexIdMaps.idToIndexWeightFunc(cost, eiMap));
		}
	}

	static class ResidualGraph {
		final IndexGraph gOrig;
		final IndexGraph g;
		final int[] edgeRef;
		final int[] twin;

		ResidualGraph(IndexGraph gOrig, IndexGraph g, int[] edgeRef, int[] twin) {
			this.gOrig = gOrig;
			this.g = g;
			this.edgeRef = edgeRef;
			this.twin = twin;
		}

		boolean isOriginalEdge(int e) {
			int eOrig = edgeRef[e];
			return eOrig != -1 && g.edgeSource(e) == gOrig.edgeSource(eOrig);
		}

		static class Builder {

			private final IndexGraphBuilder gBuilder;
			private final IndexGraph gOrig;
			private final IntArrayList edgeRef;
			private final IntArrayList twin;

			Builder(IndexGraph gOrig) {
				this.gOrig = Objects.requireNonNull(gOrig);
				gBuilder = IndexGraphBuilder.newDirected();
				edgeRef = new IntArrayList(gOrig.edges().size() * 2);
				twin = new IntArrayList(gOrig.edges().size() * 2);
			}

			void addAllOriginalEdges() {
				assert gBuilder.vertices().isEmpty();
				gBuilder.expectedVerticesNum(gOrig.vertices().size());
				for (int n = gOrig.vertices().size(), u = 0; u < n; u++) {
					int vBuilder = gBuilder.addVertex();
					assert u == vBuilder;
				}

				assert gBuilder.edges().isEmpty();
				gBuilder.expectedEdgesNum(gOrig.edges().size() * 2);
				for (int m = gOrig.edges().size(), e = 0; e < m; e++) {
					int u = gOrig.edgeSource(e), v = gOrig.edgeTarget(e);
					if (u != v)
						addEdge(u, v, e);
				}
			}

			int addVertex() {
				return gBuilder.addVertex();
			}

			void addEdge(int u, int v, int e) {
				int e1Builder = gBuilder.addEdge(u, v);
				int e2Builder = gBuilder.addEdge(v, u);
				assert e1Builder == edgeRef.size();
				edgeRef.add(e);
				assert e2Builder == edgeRef.size();
				edgeRef.add(e);
				assert e1Builder == twin.size();
				twin.add(e2Builder);
				assert e2Builder == twin.size();
				twin.add(e1Builder);
			}

			ResidualGraph build() {
				IndexGraphBuilder.ReIndexedGraph reindexedGraph = gBuilder.reIndexAndBuild(false, true);
				IndexGraph g = reindexedGraph.graph();
				final int m = g.edges().size();
				int[] edgeRefTemp = edgeRef.elements();
				int[] twinTemp = twin.elements();
				int[] edgeRef = new int[m];
				int[] twin = new int[m];
				if (reindexedGraph.edgesReIndexing().isPresent()) {
					IndexGraphBuilder.ReIndexingMap eIdxMap = reindexedGraph.edgesReIndexing().get();
					for (int eBuilder = 0; eBuilder < m; eBuilder++) {
						edgeRef[eBuilder] = edgeRefTemp[eIdxMap.reIndexedToOrig(eBuilder)];
						twin[eBuilder] = eIdxMap.origToReIndexed(twinTemp[eIdxMap.reIndexedToOrig(eBuilder)]);
					}
				} else {
					for (int eBuilder = 0; eBuilder < m; eBuilder++) {
						edgeRef[eBuilder] = edgeRefTemp[eBuilder];
						twin[eBuilder] = twinTemp[eBuilder];
					}
				}
				return new ResidualGraph(gOrig, g, edgeRef, twin);
			}

		}

	}

	static double hugeCapacity(IndexGraph g, IWeightFunction capacity, IntCollection sources, IntCollection sinks) {
		if (WeightFunction.isInteger(capacity))
			return hugeCapacityLong(g, (IWeightFunctionInt) capacity, sources, sinks);

		double sourcesOutCapacity = 0;
		double sinksOutCapacity = 0;
		for (int s : sources)
			for (int e : g.outEdges(s))
				sourcesOutCapacity += capacity.weight(e);
		for (int s : sinks)
			for (int e : g.inEdges(s))
				sinksOutCapacity += capacity.weight(e);
		return Math.max(sourcesOutCapacity, sinksOutCapacity) + 1;
	}

	static int hugeCapacity(IndexGraph g, IWeightFunctionInt capacity, IntCollection sources, IntCollection sinks) {
		long hugeCapacity = hugeCapacityLong(g, capacity, sources, sinks);
		int hugeCapacityInt = (int) hugeCapacity;
		if (hugeCapacityInt != hugeCapacity)
			throw new AssertionError("integer overflow, huge capacity can't fit in 32bit int");
		return hugeCapacityInt;
	}

	static long hugeCapacityLong(IndexGraph g, IWeightFunctionInt capacity, IntCollection sources,
			IntCollection sinks) {
		long sourcesOutCapacity = 0;
		long sinksOutCapacity = 0;
		for (int s : sources)
			for (int e : g.outEdges(s))
				sourcesOutCapacity += capacity.weightInt(e);
		for (int s : sinks)
			for (int e : g.inEdges(s))
				sinksOutCapacity += capacity.weightInt(e);
		return Math.max(sourcesOutCapacity, sinksOutCapacity) + 1;
	}

	static double vertexMaxSupply(IndexGraph g, IWeightFunction capacity, int v) {
		if (WeightFunction.isInteger(capacity))
			return vertexMaxSupply(g, (IWeightFunctionInt) capacity, v);

		double maxSupply = 0;
		for (int e : g.outEdges(v))
			maxSupply += capacity.weight(e);
		return maxSupply;
	}

	static int vertexMaxSupply(IndexGraph g, IWeightFunctionInt capacity, int v) {
		long maxSupply = 0;
		for (int e : g.outEdges(v))
			maxSupply += capacity.weightInt(e);
		int maxSupplyInt = (int) maxSupply;
		if (maxSupplyInt != maxSupply)
			throw new AssertionError("integer overflow, vertex max supply can't fit in 32bit int");
		return maxSupplyInt;
	}

	static double vertexMaxDemand(IndexGraph g, IWeightFunction capacity, int v) {
		if (WeightFunction.isInteger(capacity))
			return vertexMaxDemand(g, (IWeightFunctionInt) capacity, v);

		double maxDemand = 0;
		for (int e : g.inEdges(v))
			maxDemand += capacity.weight(e);
		return maxDemand;
	}

	static int vertexMaxDemand(IndexGraph g, IWeightFunctionInt capacity, int v) {
		long maxDemand = 0;
		for (int e : g.inEdges(v))
			maxDemand += capacity.weightInt(e);
		int maxDemandInt = (int) maxDemand;
		if (maxDemandInt != maxDemand)
			throw new AssertionError("integer overflow, vertex max supply can't fit in 32bit int");
		return maxDemandInt;
	}

}
