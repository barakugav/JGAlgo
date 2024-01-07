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

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;

interface MinimumDirectedSpanningTreeBase extends MinimumDirectedSpanningTree {

	@SuppressWarnings("unchecked")
	@Override
	default <V, E> MinimumSpanningTree.Result<V, E> computeMinimumDirectedSpanningTree(Graph<V, E> g,
			WeightFunction<E> w, V root) {
		if (g instanceof IndexGraph) {
			IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
			int root0 = ((Integer) root).intValue();
			return (MinimumSpanningTree.Result<V, E>) computeMinimumDirectedSpanningTree((IndexGraph) g, w0, root0);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			int iRoot = viMap.idToIndex(root);
			MinimumSpanningTree.IResult indexResult = computeMinimumDirectedSpanningTree(iGraph, iw, iRoot);
			return MinimumSpanningTrees.resultFromIndexResult(g, indexResult);
		}
	}

	MinimumSpanningTree.IResult computeMinimumDirectedSpanningTree(IndexGraph g, IWeightFunction w, int root);

}
