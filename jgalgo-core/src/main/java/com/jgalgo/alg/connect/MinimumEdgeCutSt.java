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
import com.jgalgo.alg.IVertexBiPartition;
import com.jgalgo.alg.VertexBiPartition;
import com.jgalgo.alg.flow.MaximumFlow;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * Minimum Edge-Cut algorithm with terminal vertices (source-sink, S-T).
 *
 * <p>
 * Given a graph \(G=(V,E)\), an edge cut is a partition of \(V\) into two sets \(C, \bar{C} = V \setminus C\). Given an
 * edge weight function, the weight of an edge-cut \((C,\bar{C})\) is the weight sum of all edges \((u,v)\) such that
 * \(u\) is in \(C\) and \(v\) is in \(\bar{C}\). There are two variants of the problem to find a minimum weight
 * edge-cut: (1) With terminal vertices, and (2) without terminal vertices. In the variant with terminal vertices, we
 * are given two special vertices {@code source (S)} and {@code sink (T)} and we need to find the minimum edge-cut
 * \((C,\bar{C})\) such that the {@code source} is in \(C\) and the {@code sink} is in \(\bar{C}\). In the variant
 * without terminal vertices (also called 'global edge-cut') we need to find the minimal cut among all possible cuts,
 * and \(C,\bar{C}\) simply must not be empty.
 *
 * <p>
 * Algorithms implementing this interface compute the minimum edge-cut given two terminal vertices, {@code source (S)}
 * and {@code sink (T)}. To enumerate <b>all</b> minimum edge-cuts between two terminal vertices, use
 * {@link MinimumEdgeCutAllSt}. For the global variant (without terminal vertices), see {@link MinimumEdgeCutGlobal}.
 *
 * <p>
 * The cardinality (unweighted) minimum edge-cut between two vertices is equal to the (local) edge connectivity of these
 * two vertices. If the graph is directed, the edge connectivity between \(u\) and \(v\) is the minimum of the minimum
 * edge-cut between \(u\) and \(v\) and the minimum edge-cut between \(v\) and \(u\).
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * @see    <a href="https://en.wikipedia.org/wiki/Minimum_cut">Wikipedia</a>
 * @see    MinimumEdgeCutGlobal
 * @see    MinimumVertexCutSt
 * @author Barak Ugav
 */
public interface MinimumEdgeCutSt {

	/**
	 * Compute the minimum edge-cut in a graph between two terminal vertices.
	 *
	 * <p>
	 * Given a graph \(G=(V,E)\), an edge-cut is a partition of \(V\) into twos sets \(C, \bar{C} = V \setminus C\). The
	 * return value of this function is a partition into these two sets.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, a {@link IVertexBiPartition} object will be returned. In that case, its
	 * better to pass a {@link IWeightFunction} as {@code w} to avoid boxing/unboxing.
	 *
	 * @param  <V>                      the vertices type
	 * @param  <E>                      the edges type
	 * @param  g                        a graph
	 * @param  w                        an edge weight function
	 * @param  source                   a special vertex that will be in \(C\)
	 * @param  sink                     a special vertex that will be in \(\bar{C}\)
	 * @return                          the cut that was computed
	 * @throws IllegalArgumentException if the source and the sink are the same vertex
	 */
	<V, E> VertexBiPartition<V, E> computeMinimumCut(Graph<V, E> g, WeightFunction<E> w, V source, V sink);

	/**
	 * Compute the minimum edge-cut in a graph between two sets of vertices.
	 *
	 * <p>
	 * Given a graph \(G=(V,E)\), an edge-cut is a partition of \(V\) into twos sets \(C, \bar{C} = V \setminus C\). The
	 * return value of this function is a partition into these two sets.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, a {@link IVertexBiPartition} object will be returned. In that case, its
	 * better to pass a {@link IWeightFunction} as {@code w}, and {@link IntCollection} as {@code sources} and
	 * {@code sinks} to avoid boxing/unboxing.
	 *
	 * @param  <V>                      the vertices type
	 * @param  <E>                      the edges type
	 * @param  g                        a graph
	 * @param  w                        an edge weight function
	 * @param  sources                  special vertices that will be in \(C\)
	 * @param  sinks                    special vertices that will be in \(\bar{C}\)
	 * @return                          the minimum cut between the two sets
	 * @throws IllegalArgumentException if a vertex is both a source and a sink, or if a vertex appear twice in the
	 *                                      source or sinks sets
	 */
	<V, E> VertexBiPartition<V, E> computeMinimumCut(Graph<V, E> g, WeightFunction<E> w, Collection<V> sources,
			Collection<V> sinks);

	/**
	 * Create a new minimum S-T edge-cut algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link MinimumEdgeCutSt} object.
	 *
	 * @return a default implementation of {@link MinimumEdgeCutSt}
	 */
	static MinimumEdgeCutSt newInstance() {
		MaximumFlow maxFlowAlg = MaximumFlow.newInstance();
		if (maxFlowAlg instanceof MinimumEdgeCutSt) {
			return (MinimumEdgeCutSt) maxFlowAlg;
		} else {
			return newFromMaximumFlow(maxFlowAlg);
		}
	}

	/**
	 * Create a new minimum edge-cut algorithm using a maximum flow algorithm.
	 *
	 * <p>
	 * By first computing a maximum flow between the source and the sink, the minimum edge-cut can be realized from the
	 * maximum flow without increasing the asymptotical running time of the maximum flow algorithm running time.
	 *
	 * @param  maxFlowAlg a maximum flow algorithm
	 * @return            a minimum edge-cut algorithm based on the provided maximum flow algorithm
	 */
	static MinimumEdgeCutSt newFromMaximumFlow(MaximumFlow maxFlowAlg) {
		return MinimumEdgeCutUtils.buildFromMaxFlow(maxFlowAlg);
	}

}
