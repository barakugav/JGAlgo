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

import java.util.BitSet;
import java.util.Collection;
import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.IntContainers;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Minimum edge vertex cover algorithm.
 * <p>
 * Given a graph \(G=(V,E)\) an <i>edge cover</i> is a set \(S \subseteq E\) for which for any vertex \(v \in V\) at
 * least one of the edges adjacent to \(v\) is in \(S\). Given an edge weight function \(w:E \rightarrow R\), the weight
 * of an edge cover is the weight sum of the edges in the cover. The minimum edge cover is the edge cover with the
 * minimum weight. In contrast to the {@link VertexCover} problem which is NP-hard, the edge cover problem can be solved
 * in polynomial time.
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * @see    VertexCover
 * @author Barak Ugav
 */
public interface EdgeCover {

	/**
	 * Compute a minimum edge cover of a graph with respect to an edge weight function.
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned object is {@link EdgeCover.IResult}. If {@code g} is
	 * {@link IntGraph}, prefer to pass {@link IWeightFunction} for best performance.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   a graph
	 * @param  w   an edge weight function
	 * @return     a minimum edge cover
	 */
	<V, E> EdgeCover.Result<V, E> computeMinimumEdgeCover(Graph<V, E> g, WeightFunction<E> w);

	/**
	 * A result object of {@link EdgeCover} computation.
	 * <p>
	 * The result object is basically the set of edges that form the cover.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @author     Barak Ugav
	 */
	static interface Result<V, E> {

		/**
		 * Get the edges which are included in the cover.
		 *
		 * @return the edges that are included in the cover
		 */
		Set<E> edges();

		/**
		 * Check whether a edge is included in the cover.
		 *
		 * @param  edge a graph edge identifier
		 * @return      {@code true} if {@code edge} is included in the cover
		 */
		boolean isInCover(E edge);
	}

	/**
	 * A result object of {@link EdgeCover} computation for {@link IntGraph}.
	 *
	 * @author Barak Ugav
	 */
	static interface IResult extends EdgeCover.Result<Integer, Integer> {

		@Override
		IntSet edges();

		/**
		 * Check whether a edge is included in the cover.
		 *
		 * @param  edge a graph edge identifier
		 * @return      {@code true} if {@code edge} is included in the cover
		 */
		boolean isInCover(int edge);

		@Deprecated
		@Override
		default boolean isInCover(Integer edge) {
			return isInCover(edge.intValue());
		}
	}

	/**
	 * Check whether a set of edges is a edge cover of a graph.
	 * <p>
	 * A set of edges is an edge cover of a graph if for every vertex has at least one adjacent edge which is in the
	 * set. In addition, the collection of the edges must not contain duplicates.
	 *
	 * @param  <V>   the vertices type
	 * @param  <E>   the edges type
	 * @param  g     a graph
	 * @param  edges a collection of edges that should cover all the vertices in the graph
	 * @return       {@code true} if {@code edges} is an edge cover of {@code g}
	 */
	@SuppressWarnings("unchecked")
	static <V, E> boolean isCover(Graph<V, E> g, Collection<E> edges) {
		IndexGraph ig;
		IntCollection edges0;
		if (g instanceof IndexGraph) {
			ig = (IndexGraph) g;
			edges0 = IntContainers.toIntCollection((Collection<Integer>) edges);
		} else {
			ig = g.indexGraph();
			edges0 = IndexIdMaps.idToIndexCollection(edges, g.indexGraphEdgesMap());
		}
		final int n = ig.vertices().size();
		final int m = ig.edges().size();
		BitSet coverEdges = new BitSet(m);
		for (int e : edges0) {
			if (!ig.edges().contains(e))
				throw new IllegalArgumentException("invalid edge index " + e);
			if (coverEdges.get(e))
				throw new IllegalArgumentException("edge with index " + e + " is included more than once in the cover");
			coverEdges.set(e);
		}

		if (ig.isDirected()) {
			vertexLoop: for (int v = 0; v < n; v++) {
				for (int e : ig.outEdges(v))
					if (coverEdges.get(e))
						continue vertexLoop;
				for (int e : ig.inEdges(v))
					if (coverEdges.get(e))
						continue vertexLoop;
				return false;
			}
		} else {
			vertexLoop: for (int v = 0; v < n; v++) {
				for (int e : ig.outEdges(v))
					if (coverEdges.get(e))
						continue vertexLoop;
				return false;
			}
		}
		return true;
	}

	/**
	 * Create a new edge cover algorithm object.
	 * <p>
	 * This is the recommended way to instantiate a new {@link EdgeCover} object. The {@link EdgeCover.Builder} might
	 * support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link EdgeCover}
	 */
	static EdgeCover newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new edge cover algorithm builder.
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link EdgeCover} objects
	 */
	static EdgeCover.Builder newBuilder() {
		return () -> {
			EdgeCover cardinalityAlgo = new EdgeCoverCardinality();
			EdgeCover weightedAlgo = new EdgeCoverWeighted();
			return new EdgeCover() {
				@Override
				public <V, E> Result<V, E> computeMinimumEdgeCover(Graph<V, E> g, WeightFunction<E> w) {
					boolean isCardinality = w == null || w == IWeightFunction.CardinalityWeightFunction;
					if (isCardinality) {
						return cardinalityAlgo.computeMinimumEdgeCover(g, null);
					} else {
						return weightedAlgo.computeMinimumEdgeCover(g, w);
					}
				}
			};
		};
	}

	/**
	 * A builder for {@link EdgeCover} algorithms.
	 *
	 * @see    EdgeCover#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for minimum edge cover computation.
		 *
		 * @return a new minimum edge cover algorithm
		 */
		EdgeCover build();

		/**
		 * <b>[TL;DR Don't call me!]</b> Set an option.
		 * <p>
		 * The builder might support different options to customize its implementation. These options never change the
		 * behavior of the algorithm, only its internal implementation. The possible options are not exposed as 'public'
		 * because they are not part of the API and may change in the future.
		 * <p>
		 * These options are mainly for debug and benchmark purposes.
		 *
		 * @param  key   the option key
		 * @param  value the option value
		 * @return       this builder
		 */
		default EdgeCover.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
