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
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.NoSuchVertexException;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * An algorithm that compute the bi-connected components of a graph.
 *
 * <p>
 * Given a graph \(G=(V,E)\) a bi-connected component \(B \subseteq V\) is a maximal set of connected vertices that
 * remain connected even after removing any single vertex of \(B\). In particular, an isolated vertex is considered a
 * bi-connected component, along with an isolated pair of vertices connected by a single edge. This algorithm compute
 * all the maximal bi-connected components of the graph. Note that the bi-connected components are not disjoint, namely
 * a single vertex can be included in multiple bi-connected components, differing from the regular connected components
 * of a graph.
 *
 * <p>
 * For a general k-vertex connected components, see {@link KVertexConnectedComponentsAlgo}.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Biconnected_component">Wikipedia</a>
 * @see    StronglyConnectedComponentsAlgo
 * @see    KVertexConnectedComponentsAlgo
 * @see    WeaklyConnectedComponentsAlgo
 * @author Barak Ugav
 */
public interface BiConnectedComponentsAlgo {

	/**
	 * Compute all maximal bi-connected components of a graph.
	 *
	 * <p>
	 * If an {@link IntGraph} is passed as an argument {@link BiConnectedComponentsAlgo.IResult} is returned.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   a graph
	 * @return     a result object containing the bi-connected components of the graph
	 */
	<V, E> BiConnectedComponentsAlgo.Result<V, E> findBiConnectedComponents(Graph<V, E> g);

	/**
	 * A result object of a {@link BiConnectedComponentsAlgo} computation.
	 *
	 * <p>
	 * Each bi-connected components is labeled by an integer in range {@code [0, getNumberOfBiCcs())}.
	 *
	 * <p>
	 * Note that the bi-connected components are not disjoint, namely a single vertex can be included in multiple
	 * bi-connected components, differing from the regular connected components of a graph.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @author     Barak Ugav
	 */
	static interface Result<V, E> {

		/**
		 * Get the bi-connected components a vertex is contained in.
		 *
		 * @param  vertex                a vertex in the graph
		 * @return                       the labels of the bi-connected components containing the vertex
		 * @throws NoSuchVertexException if {@code vertex} is not a valid vertex identifier in the graph
		 */
		IntSet getVertexBiCcs(V vertex);

		/**
		 * Get the number of bi-connected components computed in the graph.
		 *
		 * <p>
		 * Each bi-connected component is labeled by an integer in range {@code [0, getNumberOfBiCcs())}.
		 *
		 * @return the number of bi-connected components
		 */
		int getNumberOfBiCcs();

		/**
		 * Get the vertices contained in a single bi-connected component.
		 *
		 * @param  biccIdx                   an index of a bi-connected component
		 * @return                           all the vertices that are contained in the bi-connected component
		 * @throws IndexOutOfBoundsException if {@code biccIdx} is not in range {@code [0, getNumberOfBiCcs())}
		 */
		Set<V> getBiCcVertices(int biccIdx);

		/**
		 * Get the edges contained in a single bi-connected component.
		 *
		 * <p>
		 * An edge \((u,v)\) is said to bi contained in a bi-connected component \(B\) if both \(u\) and \(v\) are in
		 * \(B\).
		 *
		 * @param  biccIdx                   an index of a bi-connected component
		 * @return                           all the edges that are contained in the bi-connected component
		 * @throws IndexOutOfBoundsException if {@code biccIdx} is not in range {@code [0, getNumberOfBiCcs())}
		 */
		Set<E> getBiCcEdges(int biccIdx);

		/**
		 * Check whether a vertex is a cut-vertex.
		 *
		 * <p>
		 * A cut vertex is a vertex whose removal disconnects the graph. In the context of bi-connected components, a
		 * cut vertex is also a vertex that belongs to more than one bi-connected component. These vertices are also
		 * called articulation points, or separating vertices.
		 *
		 * @param  vertex a vertex in the graph
		 * @return        {@code true} if {@code vertex} is a cut-vertex, {@code false} otherwise
		 */
		boolean isCutVertex(V vertex);

		/**
		 * Get all the cut vertices in the graph.
		 *
		 * <p>
		 * A cut vertex is a vertex whose removal disconnects the graph. In the context of bi-connected components, a
		 * cut vertex is also a vertex that belongs to more than one bi-connected component. These vertices are also
		 * called articulation points, or separating vertices.
		 *
		 * @return all the cut vertices in the graph
		 */
		Set<V> getCutVertices();

		/**
		 * Get the graph of the bi-connected components.
		 *
		 * <p>
		 * The vertices of the graph are the bi-connected components indices, and there is an edge between two
		 * bi-connected components if they share a (cut) vertex. There are no cycles in the graph, namely its a forest.
		 *
		 * @return the graph of the bi-connected components
		 */
		IntGraph getBlockGraph();

	}

	/**
	 * A result object of a {@link BiConnectedComponentsAlgo} computation for {@link IntGraph}.
	 *
	 * @author Barak Ugav
	 */
	static interface IResult extends Result<Integer, Integer> {

		/**
		 * Get the bi-connected components a vertex is contained in.
		 *
		 * @param  vertex                a vertex in the graph
		 * @return                       the labels of the bi-connected components containing the vertex
		 * @throws NoSuchVertexException if {@code vertex} is not a valid vertex identifier in the graph
		 */
		IntSet getVertexBiCcs(int vertex);

		@Deprecated
		@Override
		default IntSet getVertexBiCcs(Integer vertex) {
			return getVertexBiCcs((vertex).intValue());
		}

		@Override
		IntSet getBiCcVertices(int biccIdx);

		@Override
		IntSet getBiCcEdges(int biccIdx);

		/**
		 * Check whether a vertex is a cut-vertex.
		 *
		 * <p>
		 * A cut vertex is a vertex whose removal disconnects the graph. In the context of bi-connected components, a
		 * cut vertex is also a vertex that belongs to more than one bi-connected component. These vertices are also
		 * called articulation points, or separating vertices.
		 *
		 * @param  vertex a vertex in the graph
		 * @return        {@code true} if {@code vertex} is a cut-vertex, {@code false} otherwise
		 */
		boolean isCutVertex(int vertex);

		@Deprecated
		@Override
		default boolean isCutVertex(Integer vertex) {
			return isCutVertex((vertex).intValue());
		}

		@Override
		IntSet getCutVertices();

	}

	/**
	 * Create a new bi-connected components algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link BiConnectedComponentsAlgo} object.
	 *
	 * @return a default implementation of {@link BiConnectedComponentsAlgo}
	 */
	static BiConnectedComponentsAlgo newInstance() {
		return new BiConnectedComponentsAlgoHopcroftTarjan();
	}

}
