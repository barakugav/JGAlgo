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

import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Minimum Vertex-Cut algorithm without terminal vertices.
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
 * Algorithms implementing this interface compute the global minimum vertex-cut without terminal vertices.
 *
 * <p>
 * The cardinality (unweighted) global minimum vertex-cut is equal to the vertex connectivity of a graph.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * @see    MinimumVertexCutST
 * @see    MinimumEdgeCutGlobal
 * @author Barak Ugav
 */
public interface MinimumVertexCutGlobal {

	/**
	 * Compute the global minimum vertex-cut in a graph.
	 *
	 * <p>
	 * Given a graph \(G=(V,E)\), an vertex-cut is a set of vertices whose removal disconnect graph into more than one
	 * connected components.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, a {@link IntSet} object will be returned. In that case, its better to pass a
	 * {@link IWeightFunction} as {@code w} to avoid boxing/unboxing.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   the graph
	 * @param  w   a vertex weight function
	 * @return     the global minimum vertex-cut
	 */
	<V, E> Set<V> computeMinimumCut(Graph<V, E> g, WeightFunction<V> w);

	/**
	 * Create a new minimum global vertex-cut algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link MinimumVertexCutGlobal} object. The
	 * {@link MinimumVertexCutGlobal.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link MinimumVertexCutGlobal}
	 */
	static MinimumVertexCutGlobal newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new global minimum vertex-cut algorithm builder.
	 *
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link MinimumVertexCutGlobal} objects
	 */
	static MinimumVertexCutGlobal.Builder newBuilder() {
		return MinimumVertexCutGlobalEsfahanianHakimi::new;
	}

	/**
	 * A builder for {@link MinimumVertexCutGlobal} objects.
	 *
	 * @see    MinimumVertexCutGlobal#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder extends AlgorithmBuilderBase {

		/**
		 * Create a new algorithm object for global minimum vertex-cut computation.
		 *
		 * @return a new minimum vertex-cut algorithm
		 */
		MinimumVertexCutGlobal build();
	}
}
