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
package com.jgalgo.alg.isomorphism;

import java.util.Iterator;
import java.util.Optional;
import java.util.function.BiPredicate;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IntGraph;

/**
 * Tester that check whether two graphs are isomorphic.
 *
 * <p>
 * Given two graphs, an isomorphism is a mapping function that maps the first graph vertices to the second graph
 * vertices, while preserving the structure of the graph. There are few variants of the problem:
 *
 * <ul>
 * <li><b>Full</b>: Given two graphs \(G_1 = (V_1, E_1)\) and \(G_2 = (V_2, E_2)\), a full isomorphism is a
 * <b>bijective</b> function \(m: V_1 \rightarrow V_2\) such that \((u, v) \in E_1\) <b>if and only if</b> \((m(u),
 * m(v)) \in E_2\). In the case of a directed graph, the function must preserve the direction of the edges. Note that
 * full isomorphism can only exists between graphs with the same number of vertices and edges, as the vertex mapping is
 * bijective.</li>
 * <li><b>Induced subgraph</b>: Given two graphs \(G_1 = (V_1, E_1)\) and \(G_2 = (V_2, E_2)\), an induced subgraph
 * isomorphism is an <b>injective</b> function \(m: V_1 \rightarrow V_2\) such that \((u, v) \in E_1\) <b>if and only
 * if</b> \((m(u), m(v)) \in E_2\). In the case of a directed graph, the function must preserve the direction of the
 * edges. The first graph \(G_1\) is the smaller graph, and the second graph \(G_2\) is the bigger graph, namely there
 * is an induced sub graph of {@code g2} that is isomorphic to {@code g1}. Note that induced subgraph isomorphism can
 * only exists between graphs \(G_1\) and \(G_2\) if \(|V_1| \leq |V_2|\) and \(|E_1| \leq |E_2|\). There may be
 * vertices of \(G_2\) that are not mapped to any vertex of \(G_1\). Full isomorphism between two graphs can be seen as
 * a special case of induced subgraph isomorphism where the the number of vertices and edges is the same.</li>
 * <li><b>Subgraph</b>: Given two graphs \(G_1 = (V_1, E_1)\) and \(G_2 = (V_2, E_2)\), a subgraph isomorphism is an
 * <b>injective</b> function \(m: V_1 \rightarrow V_2\) such that if \((u, v) \in E_1\) <b>than</b> \((m(u), m(v)) \in
 * E_2\). In the case of a directed graph, the function must preserve the direction of the edges. The first graph
 * \(G_1\) is the smaller graph, and the second graph \(G_2\) is the bigger graph, namely there is a sub graph of
 * {@code g2} that is isomorphic to {@code g1}. Note that subgraph isomorphism can only exists between graphs \(G_1\)
 * and \(G_2\) if \(|V_1| \leq |V_2|\) and \(|E_1| \leq |E_2|\). There may be vertices of \(G_2\) that are not mapped to
 * any vertex of \(G_1\), and edges of \(G_2\) that are not mapped to any edge of \(G_1\) (even if they are connecting
 * mapped vertices of \(G_2\)).</li>
 * </ul>
 *
 * <p>
 * All the methods in this interface accept two graphs {@code g1} and {@code g2} and check for a <b>sub graph</b>
 * isomorphism between them, where {@code g1} is the smaller graph, and {@code g2} is the bigger graph. The methods
 * accept a {@code boolean} flag that indicates whether to search for a <i>sub graph</i> isomorphism or an <i>induced
 * sub graph</i> isomorphism. To check for a full isomorphism, use induced sub graph isomorphism and make sure that the
 * number of vertices of {@code g1} and {@code g2} are the same.
 *
 * <p>
 * The full isomorphism problem which asks whether two graphs are isomorphic is one of few standard problems in
 * computational complexity theory belonging to NP, but not known to belong to either of its well-known subsets: P and
 * NP-complete. The sub graph isomorphism variants are NP-complete.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * @see    IsomorphismMapping
 * @see    <a href= "https://en.wikipedia.org/wiki/Graph_isomorphism">Wikipedia</a>
 * @author Barak Ugav
 */
public interface IsomorphismTester {

	/**
	 * Get an induced sub graph isomorphism mapping between two graphs if one exists.
	 *
	 * <p>
	 * Given two graphs \(G_1 = (V_1, E_1)\) and \(G_2 = (V_2, E_2)\), an induced subgraph isomorphism is an
	 * <b>injective</b> function \(m: V_1 \rightarrow V_2\) such that \((u, v) \in E_1\) <b>if and only if</b> \((m(u),
	 * m(v)) \in E_2\). In the case of a directed graph, the function must preserve the direction of the edges. The
	 * first graph \(G_1\) is the smaller graph, and the second graph \(G_2\) is the bigger graph, namely there is an
	 * induced sub graph of {@code g2} that is isomorphic to {@code g1}. Note that induced subgraph isomorphism can only
	 * exists between graphs \(G_1\) and \(G_2\) if \(|V_1| \leq |V_2|\) and \(|E_1| \leq |E_2|\). There may be vertices
	 * of \(G_2\) that are not mapped to any vertex of \(G_1\). In such case the
	 * {@linkplain IsomorphismMapping#inverse() inverse} of the returned mapping may not map all vertices and edges of
	 * {@code g2}, see {@link IsomorphismMapping}.
	 *
	 * <p>
	 * If {@code g1} and {@code g2} have the same number of vertices, then searching for an induced sub graph
	 * isomorphism is equivalent to searching for a full isomorphism. In full isomorphism, the mapping is bijective, and
	 * all vertices and edges of {@code g2} are mapped to vertices and edges of {@code g1}.
	 *
	 * <p>
	 * To find a sub graph isomorphism, use {@link #isomorphicMapping(Graph, Graph, boolean)} with the induced flag set
	 * to {@code false}.
	 *
	 * <p>
	 * Note that the type of vertices and edges of the two graphs may be different. Only the structure of the graphs is
	 * considered.
	 *
	 * <p>
	 * If both {@code g1} and {@code g2} are instances of {@link IntGraph}, the optional return value will be an
	 * instance of {@link IsomorphismIMapping}.
	 *
	 * @param  <V1>                     the type of vertices of the first graph
	 * @param  <E1>                     the type of edges of the first graph
	 * @param  <V2>                     the type of vertices of the second graph
	 * @param  <E2>                     the type of edges of the second graph
	 * @param  g1                       the first graph
	 * @param  g2                       the second graph
	 * @return                          an induced sub graph isomorphism mapping between the two graphs if one exists,
	 *                                  {@code Optional.empty()} otherwise. The returned mapping maps vertices and edges
	 *                                  from the first graph to vertices and edges of the second graph. The inverse
	 *                                  mapping can be obtained by calling {@link IsomorphismMapping#inverse()}.
	 * @throws IllegalArgumentException if {@code g1} is directed and {@code g2} is undirected, or vice versa
	 */
	default <V1, E1, V2, E2> Optional<IsomorphismMapping<V1, E1, V2, E2>> isomorphicMapping(Graph<V1, E1> g1,
			Graph<V2, E2> g2) {
		return isomorphicMapping(g1, g2, true);
	}

	/**
	 * Get a sub graph isomorphism mapping between two graphs if one exists, optionally induced.
	 *
	 * <p>
	 * Given two graphs \(G_1 = (V_1, E_1)\) and \(G_2 = (V_2, E_2)\), a subgraph isomorphism is an <b>injective</b>
	 * function \(m: V_1 \rightarrow V_2\) such that if \((u, v) \in E_1\) <b>than</b> \((m(u), m(v)) \in E_2\). In the
	 * case of a directed graph, the function must preserve the direction of the edges. An induced subgraph isomorphism
	 * is same as above, but the mapping also satisfies \((u, v) \in E_1\) <b>if and only if</b> \((m(u), m(v)) \in
	 * E_2\). The first graph \(G_1\) is the smaller graph, and the second graph \(G_2\) is the bigger graph, namely
	 * there is a sub graph of {@code g2} that is isomorphic to {@code g1}. Note that subgraph isomorphism can only
	 * exists between graphs \(G_1\) and \(G_2\) if \(|V_1| \leq |V_2|\) and \(|E_1| \leq |E_2|\). There may be vertices
	 * of \(G_2\) that are not mapped to any vertex of \(G_1\), and if non-induced sub graph isomorphism is searched,
	 * also edges of \(G_2\) that are not mapped to any edge of \(G_1\) (even if they are connecting mapped vertices of
	 * \(G_2\)). In such case the {@linkplain IsomorphismMapping#inverse() inverse} of the returned mapping may not map
	 * all vertices and edges of {@code g2}, see {@link IsomorphismMapping}.
	 *
	 * <p>
	 * Note that the type of vertices and edges of the two graphs may be different. Only the structure of the graphs is
	 * considered.
	 *
	 * <p>
	 * If both {@code g1} and {@code g2} are instances of {@link IntGraph}, the optional return value will be an
	 * instance of {@link IsomorphismIMapping}.
	 *
	 * @param  <V1>                     the type of vertices of the first graph
	 * @param  <E1>                     the type of edges of the first graph
	 * @param  <V2>                     the type of vertices of the second graph
	 * @param  <E2>                     the type of edges of the second graph
	 * @param  g1                       the first graph. If sub graph isomorphism is searched, {@code g1} is the smaller
	 *                                      graph, namely the method search for a mapping from {@code g1} to a sub graph
	 *                                      of {@code g2}
	 * @param  g2                       the second graph. If sub graph isomorphism is searched, {@code g2} is the bigger
	 *                                      graph, namely the method search for a mapping from {@code g1} to a sub graph
	 *                                      of {@code g2}
	 * @param  induced                  whether to search for an induced sub graph isomorphism or a sub graph
	 *                                      isomorphism. See the {@linkplain IsomorphismTester interface} documentation
	 *                                      for more details
	 * @return                          a (optionally induced) sub graph isomorphism mapping between the two graphs if
	 *                                  one exists, {@code Optional.empty()} otherwise. The returned mapping maps
	 *                                  vertices and edges from the first graph to vertices and edges of the second
	 *                                  graph. The inverse mapping can be obtained by calling
	 *                                  {@link IsomorphismMapping#inverse()}.
	 * @throws IllegalArgumentException if {@code g1} is directed and {@code g2} is undirected, or vice versa
	 */
	default <V1, E1, V2, E2> Optional<IsomorphismMapping<V1, E1, V2, E2>> isomorphicMapping(Graph<V1, E1> g1,
			Graph<V2, E2> g2, boolean induced) {
		Iterator<IsomorphismMapping<V1, E1, V2, E2>> iter = isomorphicMappingsIter(g1, g2, induced, null, null);
		return iter.hasNext() ? Optional.of(iter.next()) : Optional.empty();
	}

	/**
	 * Get an iterator over all the induced sub graph isomorphism mappings between two graphs.
	 *
	 * <p>
	 * Given two graphs \(G_1 = (V_1, E_1)\) and \(G_2 = (V_2, E_2)\), an induced subgraph isomorphism is an
	 * <b>injective</b> function \(m: V_1 \rightarrow V_2\) such that \((u, v) \in E_1\) <b>if and only if</b> \((m(u),
	 * m(v)) \in E_2\). In the case of a directed graph, the function must preserve the direction of the edges. The
	 * first graph \(G_1\) is the smaller graph, and the second graph \(G_2\) is the bigger graph, namely there is an
	 * induced sub graph of {@code g2} that is isomorphic to {@code g1}. Note that induced subgraph isomorphism can only
	 * exists between graphs \(G_1\) and \(G_2\) if \(|V_1| \leq |V_2|\) and \(|E_1| \leq |E_2|\). There may be vertices
	 * of \(G_2\) that are not mapped to any vertex of \(G_1\). In such case the
	 * {@linkplain IsomorphismMapping#inverse() inverse} of the returned mappings may not map all vertices and edges of
	 * {@code g2}, see {@link IsomorphismMapping}.
	 *
	 * <p>
	 * If {@code g1} and {@code g2} have the same number of vertices, then searching for an induced sub graph
	 * isomorphism is equivalent to searching for a full isomorphism. In full isomorphism, the mapping is bijective, and
	 * all vertices and edges of {@code g2} are mapped to vertices and edges of {@code g1}.
	 *
	 * <p>
	 * To get an iterator over all sub graph isomorphisms, use {@link #isomorphicMappingsIter(Graph, Graph, boolean)}
	 * with the induced flag set to {@code false}.
	 *
	 * <p>
	 * Note that the type of vertices and edges of the two graphs may be different. Only the structure of the graphs is
	 * considered.
	 *
	 * <p>
	 * If both {@code g1} and {@code g2} are instances of {@link IntGraph}, the returned iterator will iterate over
	 * objects of {@link IsomorphismIMapping}.
	 *
	 * @param  <V1>                     the type of vertices of the first graph
	 * @param  <E1>                     the type of edges of the first graph
	 * @param  <V2>                     the type of vertices of the second graph
	 * @param  <E2>                     the type of edges of the second graph
	 * @param  g1                       the first graph
	 * @param  g2                       the second graph
	 * @return                          an iterator over all the induced sub graph isomorphism mappings between the two
	 *                                  graphs. The returned mappings maps vertices and edges from the first graph to
	 *                                  vertices and edges of the second graph. The inverse mapping can be obtained by
	 *                                  calling {@link IsomorphismMapping#inverse()}.
	 * @throws IllegalArgumentException if {@code g1} is directed and {@code g2} is undirected, or vice versa
	 */
	default <V1, E1, V2, E2> Iterator<IsomorphismMapping<V1, E1, V2, E2>> isomorphicMappingsIter(Graph<V1, E1> g1,
			Graph<V2, E2> g2) {
		return isomorphicMappingsIter(g1, g2, true, null, null);
	}

	/**
	 * Get an iterator over all the sub graph isomorphism mappings between two graphs, optionally induced.
	 *
	 * <p>
	 * Given two graphs \(G_1 = (V_1, E_1)\) and \(G_2 = (V_2, E_2)\), a subgraph isomorphism is an <b>injective</b>
	 * function \(m: V_1 \rightarrow V_2\) such that if \((u, v) \in E_1\) <b>than</b> \((m(u), m(v)) \in E_2\). In the
	 * case of a directed graph, the function must preserve the direction of the edges. An induced subgraph isomorphism
	 * is same as above, but the mapping also satisfies \((u, v) \in E_1\) <b>if and only if</b> \((m(u), m(v)) \in
	 * E_2\). The first graph \(G_1\) is the smaller graph, and the second graph \(G_2\) is the bigger graph, namely
	 * there is a sub graph of {@code g2} that is isomorphic to {@code g1}. Note that subgraph isomorphism can only
	 * exists between graphs \(G_1\) and \(G_2\) if \(|V_1| \leq |V_2|\) and \(|E_1| \leq |E_2|\). There may be vertices
	 * of \(G_2\) that are not mapped to any vertex of \(G_1\), and if non-induced sub graph isomorphism is searched,
	 * also edges of \(G_2\) that are not mapped to any edge of \(G_1\) (even if they are connecting mapped vertices of
	 * \(G_2\)). In such case the {@linkplain IsomorphismMapping#inverse() inverse} of the returned mappings may not map
	 * all vertices and edges of {@code g2}, see {@link IsomorphismMapping}.
	 *
	 * <p>
	 * Note that the type of vertices and edges of the two graphs may be different. Only the structure of the graphs is
	 * considered.
	 *
	 * <p>
	 * If both {@code g1} and {@code g2} are instances of {@link IntGraph}, the returned iterator will iterate over
	 * objects of {@link IsomorphismIMapping}.
	 *
	 * @param  <V1>                     the type of vertices of the first graph
	 * @param  <E1>                     the type of edges of the first graph
	 * @param  <V2>                     the type of vertices of the second graph
	 * @param  <E2>                     the type of edges of the second graph
	 * @param  g1                       the first graph. If sub graph isomorphism is searched, {@code g1} is the smaller
	 *                                      graph, namely the method search for a mapping from {@code g1} to a sub graph
	 *                                      of {@code g2}
	 * @param  g2                       the second graph. If sub graph isomorphism is searched, {@code g2} is the bigger
	 *                                      graph, namely the method search for a mapping from {@code g1} to a sub graph
	 *                                      of {@code g2}
	 * @param  induced                  whether to search for an induced sub graph isomorphism or a sub graph
	 *                                      isomorphism. See the {@linkplain IsomorphismTester interface} documentation
	 *                                      for more details
	 * @return                          an iterator over all the (optionally induced) sub graph isomorphism mappings
	 *                                  between the two graphs. The returned mappings maps vertices and edges from the
	 *                                  first graph to vertices and edges of the second graph. The inverse mapping can
	 *                                  be obtained by calling {@link IsomorphismMapping#inverse()}.
	 * @throws IllegalArgumentException if {@code g1} is directed and {@code g2} is undirected, or vice versa
	 */
	default <V1, E1, V2, E2> Iterator<IsomorphismMapping<V1, E1, V2, E2>> isomorphicMappingsIter(Graph<V1, E1> g1,
			Graph<V2, E2> g2, boolean induced) {
		return isomorphicMappingsIter(g1, g2, induced, null, null);
	}

	/**
	 * Get an iterator over all the sub graph isomorphism mappings between two graphs, optionally induced, with vertex
	 * and/or edge matchers.
	 *
	 * <p>
	 * Given two graphs \(G_1 = (V_1, E_1)\) and \(G_2 = (V_2, E_2)\), a subgraph isomorphism is an <b>injective</b>
	 * function \(m: V_1 \rightarrow V_2\) such that if \((u, v) \in E_1\) <b>than</b> \((m(u), m(v)) \in E_2\). In the
	 * case of a directed graph, the function must preserve the direction of the edges. An induced subgraph isomorphism
	 * is same as above, but the mapping also satisfies \((u, v) \in E_1\) <b>if and only if</b> \((m(u), m(v)) \in
	 * E_2\). The first graph \(G_1\) is the smaller graph, and the second graph \(G_2\) is the bigger graph, namely
	 * there is a sub graph of {@code g2} that is isomorphic to {@code g1}. Note that subgraph isomorphism can only
	 * exists between graphs \(G_1\) and \(G_2\) if \(|V_1| \leq |V_2|\) and \(|E_1| \leq |E_2|\). There may be vertices
	 * of \(G_2\) that are not mapped to any vertex of \(G_1\), and if non-induced sub graph isomorphism is searched,
	 * also edges of \(G_2\) that are not mapped to any edge of \(G_1\) (even if they are connecting mapped vertices of
	 * \(G_2\)). In such case the {@linkplain IsomorphismMapping#inverse() inverse} of the returned mappings may not map
	 * all vertices and edges of {@code g2}, see {@link IsomorphismMapping}.
	 *
	 * <p>
	 * In addition to the structure of the graphs, this method also takes two predicates that filter pairs of vertices
	 * and edges, one from each graph, that are not allowed to be mapped to each other. For a given pair \(v_1,v_2\)
	 * where \(v_1 \in V_1\) and \(v_2 \in V_2\), if the vertex matcher returns {@code false}, then the pair is not
	 * considered for mapping. The edge matchers is used similarly. If a matcher is {@code null}, all pairs of vertices
	 * or edges are allowed. The matchers allow to compare other properties of the vertices/edge besides their
	 * structure, such as weights.
	 *
	 * <p>
	 * Note that the type of vertices and edges of the two graphs may be different. Only the structure of the graphs is
	 * considered, along with the matchers, if provided.
	 *
	 * <p>
	 * If both {@code g1} and {@code g2} are instances of {@link IntGraph}, the returned iterator will iterate over
	 * objects of {@link IsomorphismIMapping}.
	 *
	 * @param  <V1>                     the type of vertices of the first graph
	 * @param  <E1>                     the type of edges of the first graph
	 * @param  <V2>                     the type of vertices of the second graph
	 * @param  <E2>                     the type of edges of the second graph
	 * @param  g1                       the first graph. If sub graph isomorphism is searched, {@code g1} is the smaller
	 *                                      graph, namely the method search for a mapping from {@code g1} to a sub graph
	 *                                      of {@code g2}
	 * @param  g2                       the second graph. If sub graph isomorphism is searched, {@code g2} is the bigger
	 *                                      graph, namely the method search for a mapping from {@code g1} to a sub graph
	 *                                      of {@code g2}
	 * @param  induced                  whether to search for an induced sub graph isomorphism or a sub graph
	 *                                      isomorphism. See the {@linkplain IsomorphismTester interface} documentation
	 *                                      for more details
	 * @param  vertexMatcher            a predicate that filters pairs of vertices, one from each graph, that are not
	 *                                      allowed to be mapped to each other. For a given pair \(v_1,v_2\) where \(v_1
	 *                                      \in V_1\) and \(v_2 \in V_2\), if the matcher returns {@code false}, then
	 *                                      the pair is not considered for mapping. If {@code null}, all pairs of
	 *                                      vertices are allowed.
	 * @param  edgeMatcher              a predicate that filters pairs of edges, one from each graph, that are not
	 *                                      allowed to be mapped to each other. For a given pair \(e_1,e_2\) where \(e_1
	 *                                      \in E_1\) and \(e_2 \in E_2\), if the matcher returns {@code false}, then
	 *                                      the pair is not considered for mapping. If {@code null}, all pairs of edges
	 *                                      are allowed.
	 * @return                          an iterator over all the (optionally induced) sub graph isomorphism mappings
	 *                                  between the two graphs. The returned mappings maps vertices and edges from the
	 *                                  first graph to vertices and edges of the second graph. The inverse mapping can
	 *                                  be obtained by calling {@link IsomorphismMapping#inverse()}.
	 * @throws IllegalArgumentException if {@code g1} is directed and {@code g2} is undirected, or vice versa
	 */
	<V1, E1, V2, E2> Iterator<IsomorphismMapping<V1, E1, V2, E2>> isomorphicMappingsIter(Graph<V1, E1> g1,
			Graph<V2, E2> g2, boolean induced, BiPredicate<? super V1, ? super V2> vertexMatcher,
			BiPredicate<? super E1, ? super E2> edgeMatcher);

	/**
	 * Create a new isomorphism tester.
	 *
	 * <p>
	 * This is the recommended way to instantiate a new {@link IsomorphismTester} object.
	 *
	 * @return a default implementation of {@link IsomorphismTester}
	 */
	static IsomorphismTester newInstance() {
		return new IsomorphismTesterVf2();
	}

}
