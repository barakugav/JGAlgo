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
import java.util.Iterator;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import com.jgalgo.internal.util.IntAdapters;
import com.jgalgo.internal.util.IterTools;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

class MinimumEdgeCutUtils {

	private MinimumEdgeCutUtils() {}

	abstract static class AbstractImplST implements MinimumEdgeCutST {

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> VertexBiPartition<V, E> computeMinimumCut(Graph<V, E> g, WeightFunction<E> w, V source, V sink) {
			if (g instanceof IndexGraph) {
				IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
				int source0 = ((Integer) source).intValue(), sink0 = ((Integer) sink).intValue();
				return (VertexBiPartition<V, E>) computeMinimumCut((IndexGraph) g, w0, source0, sink0);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
				int iSource = viMap.idToIndex(source);
				int iSink = viMap.idToIndex(sink);
				IVertexBiPartition indexCut = computeMinimumCut(iGraph, iw, iSource, iSink);
				return VertexBiPartitions.partitionFromIndexPartition(g, indexCut);
			}
		}

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> VertexBiPartition<V, E> computeMinimumCut(Graph<V, E> g, WeightFunction<E> w,
				Collection<V> sources, Collection<V> sinks) {
			if (g instanceof IndexGraph) {
				IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
				IntCollection sources0 = IntAdapters.asIntCollection((Collection<Integer>) sources);
				IntCollection sinks0 = IntAdapters.asIntCollection((Collection<Integer>) sinks);
				return (VertexBiPartition<V, E>) computeMinimumCut((IndexGraph) g, w0, sources0, sinks0);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
				IntCollection iSources = IndexIdMaps.idToIndexCollection(sources, viMap);
				IntCollection iSinks = IndexIdMaps.idToIndexCollection(sinks, viMap);
				IVertexBiPartition indexCut = computeMinimumCut(iGraph, iw, iSources, iSinks);
				return VertexBiPartitions.partitionFromIndexPartition(g, indexCut);
			}
		}

		abstract IVertexBiPartition computeMinimumCut(IndexGraph g, IWeightFunction w, int sources, int sinks);

		abstract IVertexBiPartition computeMinimumCut(IndexGraph g, IWeightFunction w, IntCollection sources,
				IntCollection sinks);

	}

	abstract static class AbstractImplAllST implements MinimumEdgeCutAllST {

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public <V, E> Iterator<VertexBiPartition<V, E>> minimumCutsIter(Graph<V, E> g, WeightFunction<E> w, V source,
				V sink) {
			if (g instanceof IndexGraph) {
				IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
				int source0 = ((Integer) source).intValue();
				int sink0 = ((Integer) sink).intValue();
				return (Iterator) minimumCutsIter((IndexGraph) g, w0, source0, sink0);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
				int iSource = viMap.idToIndex(source);
				int iSink = viMap.idToIndex(sink);
				Iterator<IVertexBiPartition> indexIter = minimumCutsIter(iGraph, iw, iSource, iSink);
				return IterTools.map(indexIter,
						iPartition -> VertexBiPartitions.partitionFromIndexPartition(g, iPartition));
			}
		}

		abstract Iterator<IVertexBiPartition> minimumCutsIter(IndexGraph g, IWeightFunction w, int source, int sink);

	}

	abstract static class AbstractImplGlobal implements MinimumEdgeCutGlobal {

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> VertexBiPartition<V, E> computeMinimumCut(Graph<V, E> g, WeightFunction<E> w) {
			if (g instanceof IndexGraph) {
				return (VertexBiPartition<V, E>) computeMinimumCut((IndexGraph) g,
						WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w));

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);

				IVertexBiPartition indexCut = computeMinimumCut(iGraph, iw);
				return VertexBiPartitions.partitionFromIndexPartition(g, indexCut);
			}
		}

		abstract IVertexBiPartition computeMinimumCut(IndexGraph g, IWeightFunction w);

	}

	static IVertexBiPartition computeMinimumCutUsingMaxFlow(IndexGraph g, IWeightFunction w, int source, int sink,
			MaximumFlow maxFlowAlg) {
		IFlow flow = (IFlow) maxFlowAlg.computeMaximumFlow(g, w, Integer.valueOf(source), Integer.valueOf(sink));
		return minCutFromMaxFlow(g, IntLists.singleton(source), w, flow);
	}

	static IVertexBiPartition computeMinimumCutUsingMaxFlow(IndexGraph g, IWeightFunction w, IntCollection sources,
			IntCollection sinks, MaximumFlow maxFlowAlg) {
		IFlow flow = (IFlow) maxFlowAlg.computeMaximumFlow(g, w, sources, sinks);
		return minCutFromMaxFlow(g, sources, w, flow);
	}

	private static IVertexBiPartition minCutFromMaxFlow(IndexGraph g, IntCollection sources, IWeightFunction capacity,
			IFlow flow) {
		final int n = g.vertices().size();
		Bitmap visited = new Bitmap(n);
		IntPriorityQueue queue = new FIFOQueueIntNoReduce();

		/* perform a BFS from source and use only non saturated edges */
		final double eps = 0.00001;
		for (int source : sources) {
			visited.set(source);
			queue.enqueue(source);
		}

		if (g.isDirected()) {
			while (!queue.isEmpty()) {
				int u = queue.dequeueInt();

				for (IEdgeIter it = g.outEdges(u).iterator(); it.hasNext();) {
					int e = it.nextInt();
					int v = it.targetInt();
					if (visited.get(v))
						continue;
					if (Math.abs(capacity.weight(e) - flow.getFlow(e)) < eps)
						continue; // saturated edge
					visited.set(v);
					queue.enqueue(v);
				}
				/*
				 * We don't have any guarantee that the graph has a twin edge for each edge, so we iterate over the
				 * in-edges and search for edges with non zero flow which imply an existent of an out edge in the
				 * residual network
				 */
				for (IEdgeIter it = g.inEdges(u).iterator(); it.hasNext();) {
					int e = it.nextInt();
					int v = it.sourceInt();
					if (visited.get(v))
						continue;
					if (flow.getFlow(e) < eps)
						continue; // saturated edge
					visited.set(v);
					queue.enqueue(v);
				}
			}
		} else {
			while (!queue.isEmpty()) {
				int u = queue.dequeueInt();

				for (IEdgeIter it = g.outEdges(u).iterator(); it.hasNext();) {
					int e = it.nextInt();
					int v = it.targetInt();
					if (visited.get(v))
						continue;
					double directedFlow = flow.getFlow(e) * (g.edgeSource(e) == u ? +1 : -1);
					if (Math.abs(capacity.weight(e) - directedFlow) < eps)
						continue; // saturated edge
					visited.set(v);
					queue.enqueue(v);
				}
			}
		}

		return new VertexBiPartitions.FromBitmap(g, visited);
	}

	static MinimumEdgeCutST buildFromMaxFlow(MaximumFlow maxFlowAlg) {
		return new AbstractImplST() {

			@Override
			public IVertexBiPartition computeMinimumCut(IndexGraph g, IWeightFunction w, int source, int sink) {
				return computeMinimumCutUsingMaxFlow(g, w, source, sink, maxFlowAlg);
			}

			@Override
			public IVertexBiPartition computeMinimumCut(IndexGraph g, IWeightFunction w, IntCollection sources,
					IntCollection sinks) {
				return computeMinimumCutUsingMaxFlow(g, w, sources, sinks, maxFlowAlg);
			}

		};
	}

	static MinimumEdgeCutGlobal globalMinCutFromStMinCut(MinimumEdgeCutST stMinCut) {
		return new AbstractImplGlobal() {
			@Override
			IVertexBiPartition computeMinimumCut(IndexGraph g, IWeightFunction w) {
				final int n = g.vertices().size();
				if (n < 2)
					throw new IllegalArgumentException("no valid cut in graphs with less than two vertices");
				w = WeightFunctions.localEdgeWeightFunction(g, w);

				IVertexBiPartition bestCut = null;
				double bestCutWeight = Double.MAX_VALUE;
				final int source = 0;
				for (int sink = 1; sink < n; sink++) {
					IVertexBiPartition cut = (IVertexBiPartition) stMinCut.computeMinimumCut(g, w,
							Integer.valueOf(source), Integer.valueOf(sink));
					double cutWeight = w.weightSum(cut.crossEdges());
					if (bestCutWeight > cutWeight) {
						bestCutWeight = cutWeight;
						bestCut = cut;
					}
				}
				assert bestCut != null;
				return bestCut;
			}
		};
	}

}
