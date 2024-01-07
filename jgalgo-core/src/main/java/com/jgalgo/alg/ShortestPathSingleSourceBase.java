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
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;

interface ShortestPathSingleSourceBase extends ShortestPathSingleSource {

	@SuppressWarnings("unchecked")
	@Override
	default <V, E> ShortestPathSingleSource.Result<V, E> computeShortestPaths(Graph<V, E> g, WeightFunction<E> w,
			V source) {
		if (g instanceof IndexGraph) {
			IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
			int source0 = ((Integer) source).intValue();
			return (ShortestPathSingleSource.Result<V, E>) computeShortestPaths((IndexGraph) g, w0, source0);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			int iSource = viMap.idToIndex(source);
			ShortestPathSingleSource.IResult indexResult =
					NegativeCycleException.runAndConvertException(g, () -> computeShortestPaths(iGraph, iw, iSource));
			return resultFromIndexResult(g, indexResult);
		}
	}

	ShortestPathSingleSource.IResult computeShortestPaths(IndexGraph g, IWeightFunction w, int source);

	@SuppressWarnings("unchecked")
	private static <V, E> ShortestPathSingleSource.Result<V, E> resultFromIndexResult(Graph<V, E> g,
			ShortestPathSingleSource.IResult indexResult) {
		assert !(g instanceof IndexGraph);
		if (g instanceof IntGraph) {
			return (ShortestPathSingleSource.Result<V, E>) new ShortestPathSingleSourceUtils.IntResultFromIndexResult(
					(IntGraph) g, indexResult);
		} else {
			return new ShortestPathSingleSourceUtils.ObjResultFromIndexResult<>(g, indexResult);
		}
	}

}
