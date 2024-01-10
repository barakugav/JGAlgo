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

import com.jgalgo.graph.NoSuchEdgeException;
import com.jgalgo.graph.NoSuchVertexException;

/**
 * A mapping between two graphs that preserves the structure of the graphs.
 *
 * <p>
 * Given two graphs \(G_1 = (V_1, E_1)\) and \(G_2 = (V_2, E_2)\), an isomorphism is a bijective function \(f: V_1
 * \rightarrow V_2\) such that \((u, v) \in E_1\) if and only if \((f(u), f(v)) \in E_2\). If such a function exists,
 * then the graphs are called isomorphic. In the case of a directed graph, the function must preserve the direction of
 * the edges.
 *
 * <p>
 * Although an isomorphism mapping is a bijective function, the interface only maps vertices and edges from the first
 * graph to vertices and edges of the second graph. The inverse mapping can be obtained by calling {@link #inverse()}.
 *
 * @param  <V1> the type of vertices of the first graph
 * @param  <E1> the type of edges of the first graph
 * @param  <V2> the type of vertices of the second graph
 * @param  <E2> the type of edges of the second graph
 * @author      Barak Ugav
 */
public interface IsomorphismMapping<V1, E1, V2, E2> {

	/**
	 * Map a vertex from the first graph to a vertex of the second graph.
	 *
	 * @param  vertex                the vertex to map
	 * @return                       the mapped vertex
	 * @throws NoSuchVertexException if the vertex does not exist in the first graph
	 */
	V2 mapVertex(V1 vertex);

	/**
	 * Map an edge from the first graph to an edge of the second graph.
	 *
	 * @param  edge                the edge to map
	 * @return                     the mapped edge
	 * @throws NoSuchEdgeException if the edge does not exist in the first graph
	 */
	E2 mapEdge(E1 edge);

	/**
	 * Get the inverse mapping.
	 *
	 * @return the inverse mapping
	 */
	IsomorphismMapping<V2, E2, V1, E1> inverse();
}
