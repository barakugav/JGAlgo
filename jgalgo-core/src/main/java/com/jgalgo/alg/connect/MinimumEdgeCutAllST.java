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

import java.util.Iterator;
import java.util.List;
import com.jgalgo.alg.IVertexBiPartition;
import com.jgalgo.alg.VertexBiPartition;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Minimum Edge-Cut algorithm that finds all minimum edge-cuts in a graph between two terminal vertices (source-sink,
 * S-T).
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
 * Algorithms implementing this interface compute <b>all</b> minimum edge-cuts given two terminal vertices,
 * {@code source (S)} and {@code sink (T)}. For a single minimum edge-cut, use {@link MinimumEdgeCutST}. For the global
 * variant (without terminal vertices), see {@link MinimumEdgeCutGlobal}.
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
 * @see    MinimumEdgeCutST
 * @see    MinimumEdgeCutGlobal
 * @see    MinimumVertexCutST
 * @author Barak Ugav
 */
public interface MinimumEdgeCutAllST {

	/**
	 * Iterate over all the minimum edge-cuts in a graph between two terminal vertices.
	 *
	 * <p>
	 * Given a graph \(G=(V,E)\), an edge-cut is a partition of \(V\) into twos sets \(C, \bar{C} = V \setminus C\). The
	 * return value of this function is an iterator over all the partitions to these two sets with minimum weight.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, the returned iterator will iterate over {@link IVertexBiPartition} objects.
	 * In that case, its better to pass a {@link IWeightFunction} as {@code w} to avoid boxing/unboxing.
	 *
	 * @param  <V>                      the vertices type
	 * @param  <E>                      the edges type
	 * @param  g                        a graph
	 * @param  w                        an edge weight function
	 * @param  source                   the source vertex
	 * @param  sink                     the sink vertex
	 * @return                          an iterator over all the minimum edge-cuts
	 * @throws IllegalArgumentException if the source and the sink are the same vertex
	 */
	<V, E> Iterator<VertexBiPartition<V, E>> minimumCutsIter(Graph<V, E> g, WeightFunction<E> w, V source, V sink);

	/**
	 * Compute all the minimum edge-cuts in a graph between two terminal vertices.
	 *
	 * <p>
	 * Given a graph \(G=(V,E)\), an edge-cut is a partition of \(V\) into twos sets \(C, \bar{C} = V \setminus C\). The
	 * return value of this function is a list containing all the partitions to these two sets with minimum weight.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, the returned list will contain {@link IVertexBiPartition} objects. In that
	 * case, its better to pass a {@link IWeightFunction} as {@code w} to avoid boxing/unboxing.
	 *
	 * @param  <V>                      the vertices type
	 * @param  <E>                      the edges type
	 * @param  g                        a graph
	 * @param  w                        an edge weight function
	 * @param  source                   the source vertex
	 * @param  sink                     the sink vertex
	 * @return                          a list of all the minimum edge-cuts
	 * @throws IllegalArgumentException if the source and the sink are the same vertex
	 */
	default <V, E> List<VertexBiPartition<V, E>> allMinimumCuts(Graph<V, E> g, WeightFunction<E> w, V source, V sink) {
		return new ObjectArrayList<>(minimumCutsIter(g, w, source, sink));
	}

	/**
	 * Create a new minimum S-T all edge-cuts algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link MinimumEdgeCutAllST} object.
	 *
	 * @return a default implementation of {@link MinimumEdgeCutAllST}
	 */
	static MinimumEdgeCutAllST newInstance() {
		return new MinimumEdgeCutAllSTPicardQueyranne();
	}

}
