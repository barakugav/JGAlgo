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
import java.util.function.BiPredicate;
import java.util.function.IntBinaryOperator;
import com.jgalgo.alg.IsomorphismTesters.IntMappingFromIndexMapping;
import com.jgalgo.alg.IsomorphismTesters.ObjMappingFromIndexMapping;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.IterTools;

interface IsomorphismTesterBase extends IsomorphismTester {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	default <V1, E1, V2, E2> Iterator<IsomorphismTester.Mapping<V1, E1, V2, E2>> isomorphicMappingsIter(
			Graph<V1, E1> g1, Graph<V2, E2> g2, BiPredicate<? super V1, ? super V2> vertexMatcher,
			BiPredicate<? super E1, ? super E2> edgeMatcher) {
		if (g1 instanceof IndexGraph && g2 instanceof IndexGraph) {
			IntBinaryOperator vMatcher = asIntBiMatcher((BiPredicate<? super Integer, ? super Integer>) vertexMatcher);
			IntBinaryOperator eMatcher = asIntBiMatcher((BiPredicate<? super Integer, ? super Integer>) edgeMatcher);
			return (Iterator) isomorphicMappingsIter((IndexGraph) g1, (IndexGraph) g2, vMatcher, eMatcher);

		} else {
			IndexGraph ig1 = g1.indexGraph(), ig2 = g2.indexGraph();
			IntBinaryOperator vMatcher =
					mapMatcher(vertexMatcher, g1.indexGraphVerticesMap(), g2.indexGraphVerticesMap());
			IntBinaryOperator eMatcher = mapMatcher(edgeMatcher, g1.indexGraphEdgesMap(), g2.indexGraphEdgesMap());
			Iterator<IsomorphismTester.IMapping> iMappingsIter = isomorphicMappingsIter(ig1, ig2, vMatcher, eMatcher);
			return IterTools.map(iMappingsIter, m -> mappingFromIndexMapping(g1, g2, m));
		}
	}

	/*
	 * There is no int-specific interface for BiPredicate, we use IntBinaryOperator which return 0 for false, and any
	 * other value for true
	 */
	Iterator<IsomorphismTester.IMapping> isomorphicMappingsIter(IndexGraph g1, IndexGraph g2,
			IntBinaryOperator vertexMatcher, IntBinaryOperator edgeMatcher);

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

	private static IntBinaryOperator asIntBiMatcher(BiPredicate<? super Integer, ? super Integer> matcher) {
		return matcher == null ? null : (a, b) -> matcher.test(Integer.valueOf(a), Integer.valueOf(b)) ? 1 : 0;
	}

	private static <K1, K2> IntBinaryOperator mapMatcher(BiPredicate<? super K1, ? super K2> matcher,
			IndexIdMap<K1> map1, IndexIdMap<K2> map2) {
		return matcher == null ? null
				: (aIdx, bIdx) -> matcher.test(map1.indexToId(aIdx), map2.indexToId(bIdx)) ? 1 : 0;
	}

}
