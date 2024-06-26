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

import com.jgalgo.alg.common.IVertexPartition;
import com.jgalgo.alg.common.VertexPartition;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IntGraph;

/**
 * Strongly Connected components algorithm.
 *
 * <p>
 * Given a graph \(G=(V,E)\), two vertices \(u,v \in V\) are strongly connected if there is a path from \(u\) to \(v\)
 * and from \(v\) to \(u\). A strongly connected component is a maximal set of vertices that each pair in the set is
 * strongly connected. This definition hold for both directed and undirected graphs. For undirected graphs, the strongly
 * and weakly connected components are identical. The set of vertices \(V\) can be partitioned into disjoint strongly
 * connected components.
 *
 * <p>
 * The {@linkplain VertexPartition#blocksGraph() blocks graph} of a strongly connected components partition is a
 * directed acyclic graph, where each vertex is a strongly connected component, and there is an edge from \(B_1\) to
 * \(B_2\) if there is an edge \((u, v)\) in \(G\) such that \(u \in B_1\) and \(v \in B_2\). This blocks graph is
 * sometimes called the condensation of \(G\).
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * @see    WeaklyConnectedComponentsAlgo
 * @see    BiConnectedComponentsAlgo
 * @see    KVertexConnectedComponentsAlgo
 * @author Barak Ugav
 */
public interface StronglyConnectedComponentsAlgo {

	/**
	 * Find all strongly connected components in a graph.
	 *
	 * <p>
	 * A strongly connected component is a maximal set of vertices for which for any pair of vertices \(u, v\) in the
	 * set there exist a path from \(u\) to \(v\) and from \(v\) to \(u\).
	 *
	 * <p>
	 * If {@code g} is {@link IntGraph}, the returned object is {@link IVertexPartition}.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   a graph
	 * @return     a result object containing the partition of the vertices into strongly connected components
	 */
	<V, E> VertexPartition<V, E> findStronglyConnectedComponents(Graph<V, E> g);

	/**
	 * Check whether a graph is strongly connected.
	 *
	 * <p>
	 * A graph is strongly connected if there is a path from any vertex to any other vertex. Namely if the the whole
	 * graph is a single strongly connected component.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   a graph
	 * @return     {@code true} if the graph is strongly connected, {@code false} otherwise
	 */
	<V, E> boolean isStronglyConnected(Graph<V, E> g);

	/**
	 * Create a new strongly connected components algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link StronglyConnectedComponentsAlgo} object.
	 *
	 * @return a default implementation of {@link StronglyConnectedComponentsAlgo}
	 */
	static StronglyConnectedComponentsAlgo newInstance() {
		return new StronglyConnectedComponentsTarjan();
	}

}
