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
package com.jgalgo.alg.planar;

import java.util.Optional;
import com.jgalgo.graph.Graph;

/**
 * Algorithm testing whether a graph is planar or not.
 *
 * <p>
 * A planar graph is a graph that can be embedded in the plane, i.e., it can be drawn on the plane in such a way that
 * its edges intersect only at their endpoints. In other words, it can be drawn in such a way that no edges cross each
 * other.
 *
 * <p>
 * If a graph is planar, an combinatorial embedding of the graph is returned, representing the order of edges around
 * each vertex. If the graph is not planar, some algorithms are able to compute a Kuratowski subdivision of the graph is
 * returned. A Kuratowski subdivision is a subdivision of the graph that contains a subgraph that is a K5 (complete
 * graph on 5 vertices) or K3,3 (complete bipartite graph on 3 + 3 vertices) as a minor.
 *
 * @see    PlanarEmbedding
 * @see    <a href="https://en.wikipedia.org/wiki/Planar_graph">Wikipedia</a>
 * @author Barak Ugav
 */
public interface PlanarityTester {

	/**
	 * Checks if a graph is planar or not.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   the graph to check
	 * @return     true if the graph is planar, false otherwise
	 */
	default <V, E> boolean isPlanar(Graph<V, E> g) {
		return computePlanarity(g).isPlanar();
	}

	/**
	 * Checks if a graph is planar and compute an embedding for it.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   the graph to check
	 * @return     the result of the planarity test
	 */
	default <V, E> PlanarityTester.Result<V, E> computePlanarity(Graph<V, E> g) {
		return computePlanarity(g, false);
	}

	/**
	 * Checks if a graph is planar and compute an embedding for it, optionally adding embedding information as edges
	 * weights.
	 *
	 * @param  <V>             the vertices type
	 * @param  <E>             the edges type
	 * @param  g               the graph to check
	 * @param  addEdgesWeights if the graph is planar and this parameter is true, the embedding information is added as
	 *                             edges weights with {@link PlanarEmbedding#EdgeOrderWeightKey} key, so it can later
	 *                             be retrieved using {@link PlanarEmbedding#getExistingEmbedding}.
	 * @return                 the result of the planarity test
	 */
	<V, E> PlanarityTester.Result<V, E> computePlanarity(Graph<V, E> g, boolean addEdgesWeights);

	interface Result<V, E> {
		/**
		 * Whether the graph is planar or not.
		 *
		 * @return whether the graph is planar or not
		 */
		boolean isPlanar();

		/**
		 * Get the embedding of the graph in case the graph is planar.
		 *
		 * @return                       the planar embedding of the graph
		 * @throws IllegalStateException if the graph is not planar
		 */
		PlanarEmbedding<V, E> getEmbedding();

		/**
		 * Get the Kuratowski subdivision in the graph, proving it is not planar.
		 *
		 * <p>
		 * Some algorithms do not support computing this subdivision, and in that case will return an empty optional.
		 *
		 * @return                       the Kuratowski subdivision in the graph, proving it is not planar
		 * @throws IllegalStateException if the graph is planar
		 */
		Optional<Graph<V, E>> getKuratowskiSubdivision();
	}

}
