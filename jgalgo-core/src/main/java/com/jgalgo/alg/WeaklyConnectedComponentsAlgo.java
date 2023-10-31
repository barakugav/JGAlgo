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

import com.jgalgo.graph.IntGraph;

/**
 * Weakly Connected components algorithm.
 * <p>
 * Given a graph \(G=(V,E)\), two vertices \(u,v \in V\) are weakly connected if there is an <i>undirected</i> path from
 * \(u\) to \(v\). A weakly connected component is a maximal set of vertices that each pair in the set is weakly
 * connected. This definition hold for both directed and undirected graphs. For undirected graphs, the strongly and
 * weakly connected components are identical. The set of vertices \(V\) can be partitioned into disjoint weakly
 * connected components.
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface. A builder obtained via
 * {@link #newBuilder()} may support different options to obtain different implementations.
 *
 * @see    StronglyConnectedComponentsAlgo
 * @author Barak Ugav
 */
public interface WeaklyConnectedComponentsAlgo {

	/**
	 * Compute all weakly connected components in a graph.
	 * <p>
	 * Given a directed graph, if we replace all the directed edges with undirected edges and compute the (strongly)
	 * connected components in the result undirected graph.
	 *
	 * @param  g a graph
	 * @return   a result object containing the partition of the vertices into weakly connected components
	 */
	IVertexPartition findWeaklyConnectedComponents(IntGraph g);

	/**
	 * Check whether a graph is weakly connected.
	 * <p>
	 * A graph is weakly connected if there is an <i>undirected</i> path from any vertex to any other vertex. Namely if
	 * the the whole graph is a single weakly connected component.
	 *
	 * @param  g a graph
	 * @return   {@code true} if the graph is weakly connected, {@code false} otherwise
	 */
	boolean isWeaklyConnected(IntGraph g);

	/**
	 * Create a new weakly connected components algorithm object.
	 * <p>
	 * This is the recommended way to instantiate a new {@link WeaklyConnectedComponentsAlgo} object. The
	 * {@link WeaklyConnectedComponentsAlgo.Builder} might support different options to obtain different
	 * implementations.
	 *
	 * @return a default implementation of {@link WeaklyConnectedComponentsAlgo}
	 */
	static WeaklyConnectedComponentsAlgo newInstance() {
		return newBuilder().build();
	}

	/**
	 * Create a new weakly connected algorithm builder.
	 * <p>
	 * Use {@link #newInstance()} for a default implementation.
	 *
	 * @return a new builder that can build {@link WeaklyConnectedComponentsAlgo} objects
	 */
	static WeaklyConnectedComponentsAlgo.Builder newBuilder() {
		return WeaklyConnectedComponentsAlgoImpl::new;
	}

	/**
	 * A builder for {@link WeaklyConnectedComponentsAlgo} objects.
	 *
	 * @see    WeaklyConnectedComponentsAlgo#newBuilder()
	 * @author Barak Ugav
	 */
	static interface Builder {

		/**
		 * Create a new algorithm object for weakly connected components computation.
		 *
		 * @return a new connected components algorithm
		 */
		WeaklyConnectedComponentsAlgo build();

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
		default WeaklyConnectedComponentsAlgo.Builder setOption(String key, Object value) {
			throw new IllegalArgumentException("unknown option key: " + key);
		}
	}

}
