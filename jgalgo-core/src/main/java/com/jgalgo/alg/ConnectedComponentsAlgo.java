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

import com.jgalgo.graph.Graph;

/**
 * Connected components algorithm.
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * @author Barak Ugav
 */
public interface ConnectedComponentsAlgo {

	/**
	 * Find all (strongly) connected components in a graph.
	 * <p>
	 * A (strongly) connected component is a maximal set of vertices for which for any pair of vertices \(u, v\) in the
	 * set there exist a path from \(u\) to \(v\) and from \(v\) to \(u\).
	 *
	 * @param  g a graph
	 * @return   a result object containing the partition of the vertices into (strongly) connected components
	 */
	VertexPartition findConnectedComponents(Graph g);

	/**
	 * Compute all weakly connected components in a directed graph.
	 * <p>
	 * Given a directed graph, if we replace all the directed edges with undirected edges and compute the (strong)
	 * connected components in the result undirected graph.
	 *
	 * @param  g a graph
	 * @return   a result object containing the partition of the vertices into weakly connected components
	 */
	VertexPartition findWeaklyConnectedComponents(Graph g);

	/**
	 * Create a new connected components algorithm object.
	 * <p>
	 * This is the recommended way to instantiate a new {@link ConnectedComponentsAlgo} object. The
	 * {@link ConnectedComponentsAlgo.Builder} might support different options to obtain different implementations.
	 *
	 * @return a default implementation of {@link ConnectedComponentsAlgo}
	 */
	static ConnectedComponentsAlgo newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new connected algorithm builder.
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link ConnectedComponentsAlgo} objects
	 */
	static ConnectedComponentsAlgo.Builder newBuilder() {
		return ConnectedComponentsAlgoImpl::new;
	}

	/**
	 * A builder for {@link ConnectedComponentsAlgo} objects.
	 *
	 * @see    ConnectedComponentsAlgo#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for connected components computation.
		 *
		 * @return a new connected components algorithm
		 */
		ConnectedComponentsAlgo build();

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
		default ConnectedComponentsAlgo.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
