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
import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.internal.util.IterTools;
import it.unimi.dsi.fastutil.ints.IntSet;

interface MaximalCliquesEnumeratorBase extends MaximalCliquesEnumerator {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	default <V, E> Iterator<Set<V>> maximalCliquesIter(Graph<V, E> g) {
		if (g instanceof IndexGraph) {
			return (Iterator) maximalCliquesIter((IndexGraph) g);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			Iterator<IntSet> indexResult = maximalCliquesIter(iGraph);
			return IterTools.map(indexResult, iSet -> IndexIdMaps.indexToIdSet(iSet, viMap));
		}
	}

	Iterator<IntSet> maximalCliquesIter(IndexGraph g);

}
