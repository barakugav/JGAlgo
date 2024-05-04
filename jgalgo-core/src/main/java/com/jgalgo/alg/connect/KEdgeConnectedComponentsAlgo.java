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

import com.jgalgo.alg.IVertexPartition;
import com.jgalgo.alg.VertexPartition;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IntGraph;

/**
 * An algorithm that compute the k-edge connected components of a graph.
 *
 * <p>
 * Given a graph \(G = (V, E)\) and an integer \(k\), a k-edge connected component is a maximal subgraph \(G' = (V',
 * E')\) of \(G\) such that for every pair of vertices \(u, v \in V'\) there are at least \(k\) edge-disjoint paths
 * between \(u\) and \(v\) in \(G'\). In other words, a k-edge connected component is a subgraph of \(G\) in which every
 * pair of vertices remains connected even if we allow to remove \(k-1\) edges from the graph. For \(k=1\) the problem
 * is identical to finding the strongly connected components of the graph. Note that the k-edge disjoint paths may share
 * vertices.
 *
 * <p>
 * To compute the edge connectivity between two specific edges, use {@link MinimumEdgeCutSt}, as the minimum cardinality
 * edge-cut of a graph is equal to its edge connectivity. To compute the
 * <a href="https://mathworld.wolfram.com/EdgeConnectivity.html">edge connectivity</a> of a graph, use
 * {@link MinimumEdgeCutGlobal}, as the minimum global cardinality edge-cut of a graph is equal to its edge
 * connectivity.
 *
 * <p>
 * For k-vertex connected components, see {@link KVertexConnectedComponentsAlgo}.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * @see    <a href= "https://en.wikipedia.org/wiki/K-edge-connected_graph">Wikipedia</a>
 * @see    KVertexConnectedComponentsAlgo
 * @author Barak Ugav
 */
public interface KEdgeConnectedComponentsAlgo {

	/**
	 * Compute the k-edge connected components of a graph.
	 *
	 * <p>
	 * The algorithm will return a {@link VertexPartition} object that represents the k-edge connected components of the
	 * graph. The partition will contain exactly one block for each k-edge connected component. The vertices of each
	 * block are the vertices of the component.
	 *
	 * <p>
	 * If {@code g} is an {@link IntGraph}, a {@link IVertexPartition} object will be returned.
	 *
	 * @param  <V> the vertices type
	 * @param  <E> the edges type
	 * @param  g   a graph
	 * @param  k   the \(k\) parameter, which define the number of edges that must be removed to disconnect each
	 *                 returned connected component
	 * @return     a {@link VertexPartition} object that represents the k-edge connected components of the graph
	 */
	<V, E> VertexPartition<V, E> computeKEdgeConnectedComponents(Graph<V, E> g, int k);

	/**
	 * Create a new k-edge connected components algorithm object.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link KEdgeConnectedComponentsAlgo} object.
	 *
	 * @return a default implementation of {@link KEdgeConnectedComponentsAlgo}
	 */
	static KEdgeConnectedComponentsAlgo newInstance() {
		return new KEdgeConnectedComponentsWang();
	}

}
