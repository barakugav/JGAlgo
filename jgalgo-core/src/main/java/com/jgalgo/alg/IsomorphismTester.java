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
 * vertices, while preserving the structure of the graph. There are few variants of the problem, such as 'full' or 'sub
 * graph' isomorphism, see {@link IsomorphismType}. A full isomorphism maps all vertices and edges, while subgraph
 * isomorphism maps all vertices and edges of a single graph to a subset of the vertices and edges of the other graph.
 * All the methods of this interface accept two graphs and check if there is an isomorphism between them. In case the
 * checked isomorphism is one of the sub graph types, the first graph {@code g1} is the smaller graph, and the second
 * graph {@code g2} is the bigger graph, namely the methods search for a mapping from {@code g1} to a sub graph of
 * {@code g2}. Further details are given in the documentation of each method.
 *
 * <p>
 * The full isomorphism problem which asks whether two graphs are isomorphic is one of few standard problems in
 * computational complexity theory belonging to NP, but not known to belong to either of its well-known subsets: P and
 * NP-complete. The sub graph types are NP-complete.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * @see    IsomorphismMapping
 * @see    IsomorphismType
 * @see    <a href= "https://en.wikipedia.org/wiki/Graph_isomorphism">Wikipedia</a>
 * @author Barak Ugav
 */
public interface IsomorphismTester {

	/**
	 * Get a {@linkplain IsomorphismType#Full full} isomorphism mapping between two graphs if one exists.
	 *
	 * <p>
	 * Given two graphs \(G_1 = (V_1, E_1)\) and \(G_2 = (V_2, E_2)\), an isomorphism is a bijective function \(f: V_1
	 * \rightarrow V_2\) such that \((u, v) \in E_1\) if and only if \((f(u), f(v)) \in E_2\). If such a function
	 * exists, then the graphs are called isomorphic. In the case of a directed graph, the function must preserve the
	 * direction of the edges. There may be more than one isomorphism (mapping) between two graphs, in which case one of
	 * them is returned. In particular, only graphs with the same number of vertices and edges can be
	 * ({@linkplain IsomorphismType#Full full}) isomorphic.
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
	 * @return                          an isomorphism mapping between the two graphs if one exists,
	 *                                  {@code Optional.empty()} otherwise. The returned mapping maps vertices and edges
	 *                                  from the first graph to vertices and edges of the second graph. The inverse
	 *                                  mapping can be obtained by calling {@link IsomorphismMapping#inverse()}.
	 * @throws IllegalArgumentException if {@code g1} is directed and {@code g2} is undirected, or vice versa
	 */
	default <V1, E1, V2, E2> Optional<IsomorphismMapping<V1, E1, V2, E2>> isomorphicMapping(Graph<V1, E1> g1,
			Graph<V2, E2> g2) {
		return isomorphicMapping(g1, g2, IsomorphismType.Full);
	}

	/**
	 * Get an isomorphism mapping between two graphs if one exists of the given type.
	 *
	 * <p>
	 * Given two graphs, an isomorphism is a mapping functions that maps the first graph vertices to the second graph
	 * vertices, while preserving the structure of the graph. There are few variants of the problem, such as 'full' or
	 * 'sub graph' isomorphism, see {@link IsomorphismType}. The type of isomorphism is given as a parameter to this
	 * method. If the type is not {@linkplain IsomorphismType#Full full}, namely it is one of the sub graph types, then
	 * the first graph {@code g1} is the smaller graph, and the second graph {@code g2} is the bigger graph, and the
	 * method search for a mapping from {@code g1} to a sub graph of {@code g2}. In such case the
	 * {@linkplain IsomorphismMapping#inverse() inverse} of the returned mapping may not map all vertices and edges of
	 * {@code g2}, see {@link IsomorphismMapping}.
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
	 * @param  type                     the type of isomorphism
	 * @return                          an isomorphism mapping between the two graphs if one exists of the given type,
	 *                                  {@code Optional.empty()} otherwise. The returned mapping maps vertices and edges
	 *                                  from the first graph to vertices and edges of the second graph. The inverse
	 *                                  mapping can be obtained by calling {@link IsomorphismMapping#inverse()}.
	 * @throws IllegalArgumentException if {@code g1} is directed and {@code g2} is undirected, or vice versa
	 */
	default <V1, E1, V2, E2> Optional<IsomorphismMapping<V1, E1, V2, E2>> isomorphicMapping(Graph<V1, E1> g1,
			Graph<V2, E2> g2, IsomorphismType type) {
		Iterator<IsomorphismMapping<V1, E1, V2, E2>> iter = isomorphicMappingsIter(g1, g2, type, null, null);
		return iter.hasNext() ? Optional.of(iter.next()) : Optional.empty();
	}

	/**
	 * Get an iterator over all {@linkplain IsomorphismType#Full full} isomorphism mappings between two graphs.
	 *
	 * <p>
	 * Given two graphs \(G_1 = (V_1, E_1)\) and \(G_2 = (V_2, E_2)\), an isomorphism is a bijective function \(f: V_1
	 * \rightarrow V_2\) such that \((u, v) \in E_1\) if and only if \((f(u), f(v)) \in E_2\). If such a function
	 * exists, then the graphs are called isomorphic. In the case of a directed graph, the function must preserve the
	 * direction of the edges.
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
	 * @return                          an iterator over all isomorphism mappings between the two graphs. The returned
	 *                                  mappings maps vertices and edges from the first graph to vertices and edges of
	 *                                  the second graph. The inverse mapping can be obtained by calling
	 *                                  {@link IsomorphismMapping#inverse()}.
	 * @throws IllegalArgumentException if {@code g1} is directed and {@code g2} is undirected, or vice versa
	 */
	default <V1, E1, V2, E2> Iterator<IsomorphismMapping<V1, E1, V2, E2>> isomorphicMappingsIter(Graph<V1, E1> g1,
			Graph<V2, E2> g2) {
		return isomorphicMappingsIter(g1, g2, IsomorphismType.Full, null, null);
	}

	/**
	 * Get an iterator over all isomorphism mappings between two graphs of the given type.
	 *
	 * <p>
	 * Given two graphs, an isomorphism is a mapping functions that maps the first graph vertices to the second graph
	 * vertices, while preserving the structure of the graph. There are few variants of the problem, such as 'full' or
	 * 'sub graph' isomorphism, see {@link IsomorphismType}. The type of isomorphism is given as a parameter to this
	 * method. If the type is not {@linkplain IsomorphismType#Full full}, namely it is one of the sub graph types, then
	 * the first graph {@code g1} is the smaller graph, and the second graph {@code g2} is the bigger graph, and the
	 * method search for a mapping from {@code g1} to a sub graph of {@code g2}. In such case the
	 * {@linkplain IsomorphismMapping#inverse() inverse} of the returned mappings may not map all vertices and edges of
	 * {@code g2}, see {@link IsomorphismMapping}.
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
	 * @param  type                     the type of isomorphism
	 * @return                          an iterator over all isomorphism mappings between the two graphs. The returned
	 *                                  mappings maps vertices and edges from the first graph to vertices and edges of
	 *                                  the second graph. The inverse mapping can be obtained by calling
	 *                                  {@link IsomorphismMapping#inverse()}.
	 * @throws IllegalArgumentException if {@code g1} is directed and {@code g2} is undirected, or vice versa
	 */
	default <V1, E1, V2, E2> Iterator<IsomorphismMapping<V1, E1, V2, E2>> isomorphicMappingsIter(Graph<V1, E1> g1,
			Graph<V2, E2> g2, IsomorphismType type) {
		return isomorphicMappingsIter(g1, g2, type, null, null);
	}

	/**
	 * Get an iterator over all isomorphism mappings between two graphs of the given type, with vertex and/or edge
	 * matchers.
	 *
	 * <p>
	 * Given two graphs, an isomorphism is a mapping functions that maps the first graph vertices to the second graph
	 * vertices, while preserving the structure of the graph. There are few variants of the problem, such as 'full' or
	 * 'sub graph' isomorphism, see {@link IsomorphismType}. The type of isomorphism is given as a parameter to this
	 * method. If the type is not {@linkplain IsomorphismType#Full full}, namely it is one of the sub graph types, then
	 * the first graph {@code g1} is the smaller graph, and the second graph {@code g2} is the bigger graph, and the
	 * method search for a mapping from {@code g1} to a sub graph of {@code g2}. In such case the
	 * {@linkplain IsomorphismMapping#inverse() inverse} of the returned mappings may not map all vertices and edges of
	 * {@code g2}, see {@link IsomorphismMapping}.
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
	 * @param  type                     the type of isomorphism
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
	 * @return                          an iterator over all isomorphism mappings between the two graphs. The returned
	 *                                  mappings maps vertices and edges from the first graph to vertices and edges of
	 *                                  the second graph. The inverse mapping can be obtained by calling
	 *                                  {@link IsomorphismMapping#inverse()}.
	 * @throws IllegalArgumentException if {@code g1} is directed and {@code g2} is undirected, or vice versa
	 */
	<V1, E1, V2, E2> Iterator<IsomorphismMapping<V1, E1, V2, E2>> isomorphicMappingsIter(Graph<V1, E1> g1,
			Graph<V2, E2> g2, IsomorphismType type, BiPredicate<? super V1, ? super V2> vertexMatcher,
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
