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

/**
 * Connectivity components algorithm.
 *
 * @author Barak Ugav
 */
public interface ConnectivityAlgorithm {

	/**
	 * Find all (strongly) connected components in a graph.
	 * <p>
	 * A (strongly) connected component is a maximal set of vertices for which for any pair of vertices \(u, v\) in the
	 * set there exist a path from \(u\) to \(v\) and from \(v\) to \(u\).
	 *
	 * @param  g a graph
	 * @return   a result object containing the partition of the vertices into (strongly) connected components
	 */
	ConnectivityAlgorithm.Result computeConnectivityComponents(Graph g);

	/**
	 * Result object for connectivity components calculation.
	 * <p>
	 * The result object contains the partition of the vertices into the connectivity components (strongly for directed
	 * graph). Each connectivity component (CC) is assigned a unique integer number in range [0, ccNum), and each vertex
	 * can be queried for its CC using {@link #getVertexCc(int)}.
	 *
	 * @author Barak Ugav
	 */
	static interface Result {

		/**
		 * Get the connectivity component containing a vertex.
		 *
		 * @param  v a vertex in the graph
		 * @return   index of the connectivity component containing the vertex, in range [0, ccNum)
		 */
		public int getVertexCc(int v);

		/**
		 * Get the number of connectivity components in the graph.
		 *
		 * @return the number of connectivity components in the graph, non negative number
		 */
		public int getNumberOfCC();
	}

	/**
	 * Create a new connectivity algorithm builder.
	 * <p>
	 * This is the recommended way to instantiate a new {@link ConnectivityAlgorithm} object.
	 *
	 * @return a new builder that can build {@link ConnectivityAlgorithm} objects
	 */
	static ConnectivityAlgorithm.Builder newBuilder() {
		return new ConnectivityAlgorithmImpl.Builder();
	}

	/**
	 * A builder for {@link ConnectivityAlgorithm} objects.
	 *
	 * @see    ConnectivityAlgorithm#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for connectivity components computation.
		 *
		 * @return a new connectivity components algorithm
		 */
		ConnectivityAlgorithm build();
	}

}
