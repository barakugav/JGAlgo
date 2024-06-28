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
package com.jgalgo.alg.connect;

import java.util.Collection;
import com.jgalgo.alg.common.IVertexBiPartition;
import com.jgalgo.alg.common.VertexBiPartition;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Abstract class for computing the minimum edge cut between two vertices in a graph.
 *
 * <p>
 * The class implements the interface by solving the problem on the {@linkplain Graph#indexGraph() index graph} and then
 * maps the results back to the original graph. The implementation for {@linkplain IndexGraph index graphs} is abstract
 * and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class MinimumEdgeCutStAbstract implements MinimumEdgeCutSt {

	/**
	 * Default constructor.
	 */
	public MinimumEdgeCutStAbstract() {}

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
			return VertexBiPartition.partitionFromIndexPartition(g, indexCut);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> VertexBiPartition<V, E> computeMinimumCut(Graph<V, E> g, WeightFunction<E> w, Collection<V> sources,
			Collection<V> sinks) {
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
			return VertexBiPartition.partitionFromIndexPartition(g, indexCut);
		}
	}

	/**
	 * Compute the minimum edge-cut in a graph between two terminal vertices.
	 *
	 * @see           #computeMinimumCut(Graph, WeightFunction, Object, Object)
	 * @param  g      a graph
	 * @param  w      an edge weight function
	 * @param  source a special vertex that will be in \(C\)
	 * @param  sink   a special vertex that will be in \(\bar{C}\)
	 * @return        the cut that was computed
	 */
	protected abstract IVertexBiPartition computeMinimumCut(IndexGraph g, IWeightFunction w, int source, int sink);

	/**
	 * Compute the minimum edge-cut in a graph between two sets of vertices.
	 *
	 * @see            #computeMinimumCut(Graph, WeightFunction, Collection, Collection)
	 * @param  g       a graph
	 * @param  w       an edge weight function
	 * @param  sources special vertices that will be in \(C\)
	 * @param  sinks   special vertices that will be in \(\bar{C}\)
	 * @return         the minimum cut between the two sets
	 */
	protected abstract IVertexBiPartition computeMinimumCut(IndexGraph g, IWeightFunction w, IntCollection sources,
			IntCollection sinks);

}
