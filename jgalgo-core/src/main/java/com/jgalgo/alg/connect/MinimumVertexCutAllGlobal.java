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
import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Minimum Vertex-Cut algorithm that finds all minimum vertex-cuts in a graph (global vertex-cut).
 *
 * <p>
 * Given a graph \(G=(V,E)\), a vertex cut (or separating set) is a set of vertices \(C\) whose removal transforms \(G\)
 * into a disconnected graph. In case the graph is a clique of size \(k\), any vertex set of size \(k-1\) is considered
 * by convention a vertex cut of the graph. Given a vertex weight function, the weight of a vertex-cut \(C\) is the
 * weight sum of all vertices in \(C\). There are two variants of the problem to find a minimum weight vertex-cut: (1)
 * With terminal vertices, and (2) without terminal vertices. In the variant with terminal vertices, we are given two
 * special vertices {@code source (S)} and {@code sink (T)} and we need to find the minimum vertex-cut \(C\) such that
 * such that the {@code source} and the {@code sink} are not in the same connected components after the removal of the
 * vertices of \(C\). In the variant without terminal vertices (also called 'global vertex-cut') we need to find the
 * minimal cut among all possible cuts, and the removal of the vertices of \(C\) should simply disconnect the graph (or
 * make it trivial, containing a single vertex).
 *
 * <p>
 * Algorithms implementing this interface compute <b>all</b> minimum vertex-cuts without terminal vertices. For a single
 * minimum cut global cut, use {@link MinimumVertexCutGlobal}. For the variant with terminal vertices, see
 * {@link MinimumVertexCutSt2} or {@link MinimumVertexCutAllSt2}.
 *
 * <p>
 * The cardinality (unweighted) global minimum vertex-cut is equal to the vertex connectivity of a graph.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * @see    MinimumVertexCutGlobal
 * @see    MinimumVertexCutAllSt2
 * @see    MinimumVertexCutSt2
 * @author Barak Ugav
 */
public interface MinimumVertexCutAllGlobal {

	/**
	 * Iterate over all the minimum vertex-cuts in a graph.
	 *
	 * <p>
	 * Given a graph \(G=(V,E)\), a vertex-cut is a set of vertices whose removal disconnect graph into more than one
	 * connected components.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, the returned iterator will iterate over {@link IntSet} objects. In that
	 * case, its better to pass a {@link IWeightFunction} as {@code w} to avoid boxing/unboxing.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   the graph
	 * @param  w   a vertex weight function
	 * @return     an iterator over all the minimum vertex-cuts in a graph
	 */
	<V, E> Iterator<Set<V>> minimumCutsIter(Graph<V, E> g, WeightFunction<V> w);

	/**
	 * Find all the minimum vertex-cuts in a graph.
	 *
	 * <p>
	 * Given a graph \(G=(V,E)\), a vertex-cut is a set of vertices whose removal disconnect graph into more than one
	 * connected components.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, the returned list will contain {@link IntSet} objects. In that case, its
	 * better to pass a {@link IWeightFunction} as {@code w} to avoid boxing/unboxing.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   the graph
	 * @param  w   a vertex weight function
	 * @return     a list of all the minimum vertex-cuts in a graph
	 */
	default <V, E> List<Set<V>> allMinimumCuts(Graph<V, E> g, WeightFunction<V> w) {
		return new ObjectArrayList<>(minimumCutsIter(g, w));
	}

	/**
	 * Create a new global minimum all vertex-cuts algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link MinimumVertexCutAllGlobal} object.
	 *
	 * @return a default implementation of {@link MinimumVertexCutAllGlobal}
	 */
	static MinimumVertexCutAllGlobal newInstance() {
		return new MinimumVertexCutAllGlobalKanevsky();
	}

}
