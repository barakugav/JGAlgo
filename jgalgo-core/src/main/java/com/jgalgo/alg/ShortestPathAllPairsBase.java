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

import java.util.Collection;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntCollection;

interface ShortestPathAllPairsBase extends ShortestPathAllPairs {

	@SuppressWarnings("unchecked")
	@Override
	default <V, E> ShortestPathAllPairs.Result<V, E> computeAllShortestPaths(Graph<V, E> g, WeightFunction<E> w) {
		if (g instanceof IndexGraph) {
			IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
			return (ShortestPathAllPairs.Result<V, E>) computeAllShortestPaths((IndexGraph) g, w0);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			ShortestPathAllPairs.IResult indexResult =
					NegativeCycleException.runAndConvertException(g, () -> computeAllShortestPaths(iGraph, iw));
			return resultFromIndexResult(g, indexResult);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	default <V, E> ShortestPathAllPairs.Result<V, E> computeSubsetShortestPaths(Graph<V, E> g,
			Collection<V> verticesSubset, WeightFunction<E> w) {
		if (g instanceof IndexGraph) {
			IntCollection verticesSubset0 = IntAdapters.asIntCollection((Collection<Integer>) verticesSubset);
			IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
			return (ShortestPathAllPairs.Result<V, E>) computeSubsetShortestPaths((IndexGraph) g, verticesSubset0, w0);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IntCollection iVerticesSubset = IndexIdMaps.idToIndexCollection(verticesSubset, viMap);
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			ShortestPathAllPairs.IResult indexResult = NegativeCycleException
					.runAndConvertException(g, () -> computeSubsetShortestPaths(iGraph, iVerticesSubset, iw));
			return resultFromIndexResult(g, indexResult);
		}
	}

	ShortestPathAllPairs.IResult computeAllShortestPaths(IndexGraph g, IWeightFunction w);

	ShortestPathAllPairs.IResult computeSubsetShortestPaths(IndexGraph g, IntCollection verticesSubset,
			IWeightFunction w);

	@SuppressWarnings("unchecked")
	private static <V, E> ShortestPathAllPairs.Result<V, E> resultFromIndexResult(Graph<V, E> g,
			ShortestPathAllPairs.IResult indexResult) {
		assert !(g instanceof IndexGraph);
		if (g instanceof IntGraph) {
			return (ShortestPathAllPairs.Result<V, E>) new ShortestPathAllPairsUtils.IntResultFromIndexResult(
					(IntGraph) g, indexResult);
		} else {
			return new ShortestPathAllPairsUtils.ObjResultFromIndexResult<>(g, indexResult);
		}
	}

}
