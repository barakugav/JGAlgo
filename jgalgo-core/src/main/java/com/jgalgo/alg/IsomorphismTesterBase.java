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
import com.jgalgo.alg.IsomorphismTesters.IntMappingFromIndexMapping;
import com.jgalgo.alg.IsomorphismTesters.ObjMappingFromIndexMapping;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.IterTools;

interface IsomorphismTesterBase extends IsomorphismTester {

	@Override
	default <V1, E1, V2, E2> boolean isIsomorphic(Graph<V1, E1> g1, Graph<V2, E2> g2) {
		if (g1 instanceof IndexGraph && g2 instanceof IndexGraph) {
			return isIsomorphic((IndexGraph) g1, (IndexGraph) g2);
		} else {
			return isIsomorphic(g1.indexGraph(), g2.indexGraph());
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	default <V1, E1, V2, E2> Optional<IsomorphismTester.Mapping<V1, E1, V2, E2>> isomorphicMapping(Graph<V1, E1> g1,
			Graph<V2, E2> g2) {
		if (g1 instanceof IndexGraph && g2 instanceof IndexGraph) {
			return (Optional) isomorphicMapping((IndexGraph) g1, (IndexGraph) g2);
		} else {
			IndexGraph ig1 = g1.indexGraph(), ig2 = g2.indexGraph();
			Optional<IsomorphismTester.IMapping> iMapping = isomorphicMapping(ig1, ig2);
			return iMapping.map(m -> mappingFromIndexMapping(g1, g2, m));
		}
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	default <V1, E1, V2, E2> Iterator<IsomorphismTester.Mapping<V1, E1, V2, E2>> isomorphicMappingsIter(
			Graph<V1, E1> g1, Graph<V2, E2> g2) {
		if (g1 instanceof IndexGraph && g2 instanceof IndexGraph) {
			return (Iterator) isomorphicMappingsIter((IndexGraph) g1, (IndexGraph) g2);
		} else {
			IndexGraph ig1 = g1.indexGraph(), ig2 = g2.indexGraph();
			Iterator<IsomorphismTester.IMapping> iMappingsIter = isomorphicMappingsIter(ig1, ig2);
			return IterTools.map(iMappingsIter, m -> mappingFromIndexMapping(g1, g2, m));
		}
	}

	default boolean isIsomorphic(IndexGraph g1, IndexGraph g2) {
		return isomorphicMapping(g1, g2).isPresent();
	}

	default Optional<IsomorphismTester.IMapping> isomorphicMapping(IndexGraph g1, IndexGraph g2) {
		Iterator<IsomorphismTester.IMapping> iter = isomorphicMappingsIter(g1, g2);
		return iter.hasNext() ? Optional.of(iter.next()) : Optional.empty();
	}

	abstract Iterator<IsomorphismTester.IMapping> isomorphicMappingsIter(IndexGraph g1, IndexGraph g2);

	@SuppressWarnings("unchecked")
	private static <V1, E1, V2, E2> IsomorphismTester.Mapping<V1, E1, V2, E2> mappingFromIndexMapping(Graph<V1, E1> g1,
			Graph<V2, E2> g2, IsomorphismTester.IMapping indexMapping) {
		assert !(g1 instanceof IndexGraph && g2 instanceof IndexGraph);
		if (g1 instanceof IntGraph && g2 instanceof IntGraph) {
			return (IsomorphismTester.Mapping<V1, E1, V2, E2>) new IntMappingFromIndexMapping(indexMapping,
					(IntGraph) g1, (IntGraph) g2);
		} else {
			return new ObjMappingFromIndexMapping<>(indexMapping, g1, g2);
		}
	}

}
