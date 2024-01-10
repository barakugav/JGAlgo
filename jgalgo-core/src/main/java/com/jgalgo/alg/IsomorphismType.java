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

/**
 * A type of isomorphism between two graphs.
 *
 * <p>
 * Given two graphs, an isomorphism is a mapping functions that maps the first graph vertices to the second graph
 * vertices, while preserving the structure of the graph. There are few variants, described in the different types of
 * isomorphism.
 *
 * @see    IsomorphismTester
 * @see    IsomorphismMapping
 * @see    <a href= "https://en.wikipedia.org/wiki/Graph_isomorphism">Wikipedia</a>
 * @author Barak Ugav
 */
public enum IsomorphismType {

	/**
	 * Full isomorphism between two graphs.
	 *
	 * <p>
	 * Given two graphs \(G_1 = (V_1, E_1)\) and \(G_2 = (V_2, E_2)\), a full isomorphism is a <b>bijective</b> function
	 * \(m: V_1 \rightarrow V_2\) such that \((u, v) \in E_1\) <b>if and only if</b> \((m(u), m(v)) \in E_2\). In the
	 * case of a directed graph, the function must preserve the direction of the edges. Note that full isomorphism can
	 * only exists between graphs with the same number of vertices and edges, as the vertex mapping is bijective.
	 */
	Full,

	/**
	 * Induced subgraph isomorphism between two graphs.
	 *
	 * <p>
	 * Given two graphs \(G_1 = (V_1, E_1)\) and \(G_2 = (V_2, E_2)\), an induced subgraph isomorphism is an
	 * <b>injective</b> function \(m: V_1 \rightarrow V_2\) such that \((u, v) \in E_1\) <b>if and only if</b> \((m(u),
	 * m(v)) \in E_2\). In the case of a directed graph, the function must preserve the direction of the edges. The
	 * first graph \(G_1\) is the bigger graph, and the second graph \(G_2\) is the smaller graph, namely there is an
	 * induced sub graph of {@code g1} that is isomorphic to {@code g2}. Note that induced subgraph isomorphism can only
	 * exists between graphs \(G_1\) and \(G_2\) if \(|V_1| \geq |V_2|\) and \(|E_1| \geq |E_2|\). There may be vertices
	 * of \(G_1\) that are not mapped to any vertex of \(G_2\). Full isomorphism between two graphs can be seen as a
	 * special case of induced subgraph isomorphism where the the number of vertices and edges is the same.
	 */
	InducedSubGraph,

	/**
	 * Subgraph isomorphism between two graphs.
	 *
	 * <p>
	 * Given two graphs \(G_1 = (V_1, E_1)\) and \(G_2 = (V_2, E_2)\), a subgraph isomorphism is an <b>injective</b>
	 * function \(m: V_1 \rightarrow V_2\) such that \((u, v) \in E_1\) <b>if</b> \((m(u), m(v)) \in E_2\). In the case
	 * of a directed graph, the function must preserve the direction of the edges. The first graph \(G_1\) is the bigger
	 * graph, and the second graph \(G_2\) is the smaller graph, namely there is a sub graph of {@code g1} that is
	 * isomorphic to {@code g2}. Note that subgraph isomorphism can only exists between graphs \(G_1\) and \(G_2\) if
	 * \(|V_1| \geq |V_2|\) and \(|E_1| \geq |E_2|\). There may be vertices of \(G_1\) that are not mapped to any vertex
	 * of \(G_2\), and edges of \(G_1\) that are not mapped to any edge of \(G_2\) (even if they are connecting mapped
	 * vertices of \(G_1\)).
	 */
	SubGraph,

}
