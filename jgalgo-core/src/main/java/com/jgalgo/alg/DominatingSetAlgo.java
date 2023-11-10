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
import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * An algorithm for computing a minimum dominating set.
 *
 * <p>
 * Given a graph \(G = (V, E)\), a dominating set is a subset \(D \subseteq V\) such that for every \(v \in V\), either
 * \(v \in D\) or there is an edge \((u, v) \in E\) such that \(u \in D\). The minimum dominating set problem is to find
 * a dominating set of minimum size or weight. The problem is NP-hard. Algorithms implementing this interfaces are
 * heuristics or fixed ration approximation algorithms.
 *
 * <p>
 * In a directed graph, the 'dominance' should be defined with respect to either the in-edges, out-edges or in-edges +
 * out-edges of the vertices. This is specified as a {@link EdgeDirection} parameter to
 * {@link #computeMinimumDominationSet(Graph, WeightFunction, EdgeDirection)}, which yields different types of
 * dominating sets.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * @author Barak Ugav
 */
public interface DominatingSetAlgo {

	/**
	 * Compute a minimum dominating set of the graph with respect to the in-degree + out-degree of the vertices.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   a graph
	 * @param  w   a vertex weight function
	 * @return     a minimum dominating set of the graph
	 */
	default <V, E> Set<V> computeMinimumDominationSet(Graph<V, E> g, WeightFunction<V> w) {
		return computeMinimumDominationSet(g, w, EdgeDirection.All);
	}

	/**
	 * Compute a minimum dominating set of the graph with respect to the given edges direction.
	 *
	 * @param  <V>                the vertices type
	 * @param  <E>                the edges type
	 * @param  g                  a graph
	 * @param  w                  a vertex weight function
	 * @param  dominanceDirection the direction of the edges to consider
	 * @return                    a minimum dominating set of the graph
	 */
	<V, E> Set<V> computeMinimumDominationSet(Graph<V, E> g, WeightFunction<V> w, EdgeDirection dominanceDirection);

	/**
	 * Check whether a given set of vertices is a dominating set of a graph.
	 *
	 * @param  <V>                the vertices type
	 * @param  <E>                the edges type
	 * @param  g                  a graph
	 * @param  dominatingSet      a set of vertices
	 * @param  dominanceDirection the direction of the edges to consider, if {@code null} then {@link EdgeDirection#All}
	 *                                is used
	 * @return                    true if the given set is a dominating set of the graph
	 */
	@SuppressWarnings("unchecked")
	static <V, E> boolean isDominatingSet(Graph<V, E> g, Collection<V> dominatingSet,
			EdgeDirection dominanceDirection) {
		if (dominanceDirection == null)
			dominanceDirection = EdgeDirection.All;

		IndexGraph g0;
		IntCollection dominatingSet0;
		if (g instanceof IndexGraph) {
			g0 = (IndexGraph) g;
			dominatingSet0 = IntAdapters.asIntCollection((Collection<Integer>) dominatingSet);
		} else {
			g0 = g.indexGraph();
			dominatingSet0 = IndexIdMaps.idToIndexCollection(dominatingSet, g.indexGraphVerticesMap());
		}

		final boolean directed = g.isDirected();
		final int n = g0.vertices().size();
		Bitmap dominatingSetBitmap = new Bitmap(n);
		Bitmap dominated = new Bitmap(n);
		for (int v : dominatingSet0) {
			if (dominatingSetBitmap.get(v))
				throw new IllegalArgumentException("duplicate vertex in dominating set: " + v);
			dominatingSetBitmap.set(v);
			dominated.set(v);

			if (!directed || dominanceDirection == EdgeDirection.Out) {
				for (IEdgeIter eit = g0.outEdges(v).iterator(); eit.hasNext();) {
					eit.nextInt();
					dominated.set(eit.targetInt());
				}

			} else if (dominanceDirection == EdgeDirection.In) {
				for (IEdgeIter eit = g0.inEdges(v).iterator(); eit.hasNext();) {
					eit.nextInt();
					dominated.set(eit.sourceInt());
				}

			} else {
				assert dominanceDirection == EdgeDirection.All;
				for (IEdgeIter eit = g0.outEdges(v).iterator(); eit.hasNext();) {
					eit.nextInt();
					dominated.set(eit.targetInt());
				}
				for (IEdgeIter eit = g0.inEdges(v).iterator(); eit.hasNext();) {
					eit.nextInt();
					dominated.set(eit.sourceInt());
				}
			}
		}
		return dominated.cardinality() == n;
	}

	/**
	 * Create a new dominating set algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link DominatingSetAlgo} object. The
	 * {@link DominatingSetAlgo.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link DominatingSetAlgo}
	 */
	static DominatingSetAlgo newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new dominating set algorithm builder.
	 *
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link DominatingSetAlgo} objects
	 */
	static DominatingSetAlgo.Builder newBuilder() {
		return DominatingSetAlgoGreedy::new;
	}

	/**
	 * A builder for {@link DominatingSetAlgo} algorithms.
	 *
	 * @see    DominatingSetAlgo#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for minimum dominating set computation.
		 *
		 * @return a new minimum dominating set algorithm
		 */
		DominatingSetAlgo build();

		/**
		 * <b>[TL;DR Don't call me!]</b> Set an option.
		 *
		 * <p>
		 * The builder might support different options to customize its implementation. These options never change the
		 * behavior of the algorithm, only its internal implementation. The possible options are not exposed as 'public'
		 * because they are not part of the API and may change in the future.
		 *
		 * <p>
		 * These options are mainly for debug and benchmark purposes.
		 *
		 * @param  key   the option key
		 * @param  value the option value
		 * @return       this builder
		 */
		default DominatingSetAlgo.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
