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

import java.util.List;
import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IntGraph;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Finds the k-vertex connected components of a graph.
 *
 * <p>
 * Given a graph \(G = (V, E)\) and an integer \(k\), we say that \(G\) is k-vertex connected if it has at least \(k +
 * 1\) vertices and remains connected after removing any \(k - 1\) vertices. If \(G\) is a clique of size \(k + 1\),
 * then \(G\) is k-vertex connected. The k-vertex connected components of \(G\) are the maximal k-vertex connected
 * subgraphs of \(G\). Note that for a general \(k\), the k-vertex connected components of a graph are not disjoint, and
 * their union is not necessarily the entire graph. For \(k = 1\), the k-vertex connected components are the
 * {@linkplain StronglyConnectedComponentsAlgo (strongly) connected components} of \(G\). For \(k = 2\), the k-vertex
 * connected components are the {@linkplain BiConnectedComponentsAlgo bi-connected components} of \(G\). Isolated
 * vertices (with no edges) are considered to be 0-vertex connected components (also can be treated as a clique of size
 * 1).
 *
 * <p>
 * For k-edge connected components, see {@link KEdgeConnectedComponentsAlgo}.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * @see    <a href="https://en.wikipedia.org/wiki/K-vertex-connected_graph">Wikipedia</a>
 * @see    StronglyConnectedComponentsAlgo
 * @see    BiConnectedComponentsAlgo
 * @see    KEdgeConnectedComponentsAlgo
 * @author Barak Ugav
 */
public interface KVertexConnectedComponentsAlgo {

	/**
	 * Find all k-vertex connected components in a graph.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, the returned list will be a list of {@link IntSet}.
	 *
	 * @param  <V>                      the vertices type
	 * @param  <E>                      the edges type
	 * @param  g                        a graph
	 * @param  k                        the k value, non negative
	 * @return                          a list of the k-connected components
	 * @throws IllegalArgumentException if {@code k} is negative
	 */
	<V, E> List<Set<V>> findKVertexConnectedComponents(Graph<V, E> g, int k);

	/**
	 * Create a new k-connected components algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link KVertexConnectedComponentsAlgo} object. implementations.
	 *
	 * @return a default implementation of {@link KVertexConnectedComponentsAlgo}
	 */
	static KVertexConnectedComponentsAlgo newInstance() {
		return new KVertexConnectedComponentsWhiteMoody();
	}

}
