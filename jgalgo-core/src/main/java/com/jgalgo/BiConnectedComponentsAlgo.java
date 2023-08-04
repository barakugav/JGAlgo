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
package com.jgalgo;

import com.jgalgo.graph.Graph;
import com.jgalgo.internal.util.BuilderAbstract;
import it.unimi.dsi.fastutil.ints.IntCollection;

/**
 * An algorithm that compute the bi-connected components of a graph.
 * <p>
 * Given a graph \(G=(V,E)\) a bi-connected component \(B \subseteq V\) is a maximal set of connected vertices that
 * remain connected even after removing any single vertex of \(B\). In particular, an isolated vertex is considered a
 * bi-connected component, along with an isolated pair of vertices connected by a single edge. This algorithm compute
 * all the maximal bi-connected components of the graph. Note that the bi-connected components are not disjoint, namely
 * a single vertex can be included in multiple bi-connected components, differing from the regular connected components
 * of a graph.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/Biconnected_component">Wikipedia</a>
 * @see    ConnectedComponentsAlgo
 * @author Barak Ugav
 */
public interface BiConnectedComponentsAlgo {

	/**
	 * Compute all maximal bi-connected components of a graph.
	 *
	 * @param  g a graph
	 * @return   a result object containing the bi-connected components of the graph
	 */
	BiConnectedComponentsAlgo.Result computeBiConnectivityComponents(Graph g);

	/**
	 * A result object of a {@link BiConnectedComponentsAlgo} computation.
	 * <p>
	 * Each bi-connected components is labeled by an integer in range {@code [0, getNumberOfBiCcs())}.
	 * <p>
	 * Note that the bi-connected components are not disjoint, namely a single vertex can be included in multiple
	 * bi-connected components, differing from the regular connected components of a graph.
	 *
	 * @author Barak Ugav
	 */
	static interface Result {

		/**
		 * Get the bi-connected components a vertex is contained in.
		 *
		 * @param  vertex                    a vertex in the graph
		 * @return                           the labels of the bi-connected components containing the vertex
		 * @throws IndexOutOfBoundsException if {@code vertex} is not a valid vertex identifier in the graph
		 */
		IntCollection getVertexBiCcs(int vertex);

		/**
		 * Get the number of bi-connected components computed in the graph.
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
		IntCollection getBiCcVertices(int biccIdx);

		/**
		 * Get the edges contained in a single bi-connected component.
		 * <p>
		 * An edge \((u,v)\) is said to bi contained in a bi-connected component \(B\) if both \(u\) and \(v\) are in
		 * \(B\).
		 *
		 * @param  biccIdx                   an index of a bi-connected component
		 * @return                           all the edges that are contained in the bi-connected component
		 * @throws IndexOutOfBoundsException if {@code biccIdx} is not in range {@code [0, getNumberOfBiCcs())}
		 */
		IntCollection getBiCcEdges(int biccIdx);

	}

	/**
	 * Create a new bi-connected components algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link BiConnectedComponentsAlgo} object.
	 *
	 * @return a new builder that can build {@link BiConnectedComponentsAlgo} objects
	 */
	static BiConnectedComponentsAlgo.Builder newBuilder() {
		return BiConnectedComponentsAlgoHopcroftTarjan::new;
	}

	/**
	 * A builder for {@link BiConnectedComponentsAlgo} objects.
	 *
	 * @see    BiConnectedComponentsAlgo#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder extends BuilderAbstract<BiConnectedComponentsAlgo.Builder> {

		/**
		 * Create a new algorithm object for bi-connected components computation.
		 *
		 * @return a new bi-connected components algorithm
		 */
		BiConnectedComponentsAlgo build();

	}

}
