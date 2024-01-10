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
 * Given two graphs \(G_1 = (V_1, E_1)\) and \(G_2 = (V_2, E_2)\), an isomorphism is a bijective function \(f: V_1
 * \rightarrow V_2\) such that \((u, v) \in E_1\) if and only if \((f(u), f(v)) \in E_2\). If such a function exists,
 * then the graphs are called isomorphic. In the case of a directed graph, the function must preserve the direction of
 * the edges. There may be more than one isomorphism (mapping) between two graphs, and there may be none.
 *
 * <p>
 * In particular, two empty graphs are consider isomorphic with a single valid mapping, the empty one. Two graphs with
 * different number of vertices or edges are not isomorphic.
 *
 * <p>
 * The isomorphism problem which asks whether two graphs are isomorphic is one of few standard problems in computational
 * complexity theory belonging to NP, but not known to belong to either of its well-known subsets: P and NP-complete.
 *
 * <p>
 * Use {@link #newInstance()} to get a default implementation of this interface.
 *
 * @author Barak Ugav
 */
public interface IsomorphismTester {

	/**
	 * Get an isomorphism mapping between two graphs if one exists.
	 *
	 * <p>
	 * Given two graphs \(G_1 = (V_1, E_1)\) and \(G_2 = (V_2, E_2)\), an isomorphism is a bijective function \(f: V_1
	 * \rightarrow V_2\) such that \((u, v) \in E_1\) if and only if \((f(u), f(v)) \in E_2\). If such a function
	 * exists, then the graphs are called isomorphic. In the case of a directed graph, the function must preserve the
	 * direction of the edges. There may be more than one isomorphism (mapping) between two graphs, in which case one of
	 * them is returned.
	 *
	 * <p>
	 * Note that the type of vertices and edges of the two graphs may be different. Only the structure of the graphs is
	 * considered.
	 *
	 * <p>
	 * If both {@code g1} and {@code g2} are instances of {@link IntGraph}, the optional return value will be an
	 * instance of {@link IsomorphismIMapping}.
	 *
	 * @param  <V1> the type of vertices of the first graph
	 * @param  <E1> the type of edges of the first graph
	 * @param  <V2> the type of vertices of the second graph
	 * @param  <E2> the type of edges of the second graph
	 * @param  g1   the first graph
	 * @param  g2   the second graph
	 * @return      an isomorphism mapping between the two graphs if one exists, {@code Optional.empty()} otherwise. The
	 *              returned mapping maps vertices and edges from the first graph to vertices and edges of the second
	 *              graph. The inverse mapping can be obtained by calling {@link IsomorphismMapping#inverse()}.
	 */
	default <V1, E1, V2, E2> Optional<IsomorphismMapping<V1, E1, V2, E2>> isomorphicMapping(Graph<V1, E1> g1,
			Graph<V2, E2> g2) {
		Iterator<IsomorphismMapping<V1, E1, V2, E2>> iter = isomorphicMappingsIter(g1, g2, null, null);
		return iter.hasNext() ? Optional.of(iter.next()) : Optional.empty();
	}

	/**
	 * Get an iterator over all isomorphism mappings between two graphs.
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
	 * @param  <V1> the type of vertices of the first graph
	 * @param  <E1> the type of edges of the first graph
	 * @param  <V2> the type of vertices of the second graph
	 * @param  <E2> the type of edges of the second graph
	 * @param  g1   the first graph
	 * @param  g2   the second graph
	 * @return      an iterator over all isomorphism mappings between the two graphs. The returned mappings maps
	 *              vertices and edges from the first graph to vertices and edges of the second graph. The inverse
	 *              mapping can be obtained by calling {@link IsomorphismMapping#inverse()}.
	 */
	default <V1, E1, V2, E2> Iterator<IsomorphismMapping<V1, E1, V2, E2>> isomorphicMappingsIter(Graph<V1, E1> g1,
			Graph<V2, E2> g2) {
		return isomorphicMappingsIter(g1, g2, null, null);
	}

	/**
	 * Get an iterator over all isomorphism mappings between two graphs, with vertex and/or edge matchers.
	 *
	 * <p>
	 * Given two graphs \(G_1 = (V_1, E_1)\) and \(G_2 = (V_2, E_2)\), an isomorphism is a bijective function \(f: V_1
	 * \rightarrow V_2\) such that \((u, v) \in E_1\) if and only if \((f(u), f(v)) \in E_2\). If such a function
	 * exists, then the graphs are called isomorphic. In the case of a directed graph, the function must preserve the
	 * direction of the edges.
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
	 * considered, along with the filtering predicates if provided.
	 *
	 * <p>
	 * If both {@code g1} and {@code g2} are instances of {@link IntGraph}, the returned iterator will iterate over
	 * objects of {@link IsomorphismIMapping}.
	 *
	 * @param  <V1>          the type of vertices of the first graph
	 * @param  <E1>          the type of edges of the first graph
	 * @param  <V2>          the type of vertices of the second graph
	 * @param  <E2>          the type of edges of the second graph
	 * @param  g1            the first graph
	 * @param  g2            the second graph
	 * @param  vertexMatcher a predicate that filters pairs of vertices, one from each graph, that are not allowed to be
	 *                           mapped to each other. For a given pair \(v_1,v_2\) where \(v_1 \in V_1\) and \(v_2 \in
	 *                           V_2\), if the matcher returns {@code false}, then the pair is not considered for
	 *                           mapping. If {@code null}, all pairs of vertices are allowed.
	 * @param  edgeMatcher   a predicate that filters pairs of edges, one from each graph, that are not allowed to be
	 *                           mapped to each other. For a given pair \(e_1,e_2\) where \(e_1 \in E_1\) and \(e_2 \in
	 *                           E_2\), if the matcher returns {@code false}, then the pair is not considered for
	 *                           mapping. If {@code null}, all pairs of edges are allowed.
	 * @return               an iterator over all isomorphism mappings between the two graphs. The returned mappings
	 *                       maps vertices and edges from the first graph to vertices and edges of the second graph. The
	 *                       inverse mapping can be obtained by calling {@link IsomorphismMapping#inverse()}.
	 */
	<V1, E1, V2, E2> Iterator<IsomorphismMapping<V1, E1, V2, E2>> isomorphicMappingsIter(Graph<V1, E1> g1,
			Graph<V2, E2> g2, BiPredicate<? super V1, ? super V2> vertexMatcher,
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
