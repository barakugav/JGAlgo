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

package com.jgalgo.alg.span;

import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Abstract class for computing a minimum spanning trees in undirected graphs.
 *
 * <p>
 * The class implements the interface by solving the problem on the {@linkplain Graph#indexGraph() index graph} and then
 * maps the results back to the original graph. The implementation for {@linkplain IndexGraph index graphs} is abstract
 * and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class MinimumSpanningTreeAbstract implements MinimumSpanningTree {

	/**
	 * Default constructor.
	 */
	public MinimumSpanningTreeAbstract() {}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> MinimumSpanningTree.Result<V, E> computeMinimumSpanningTree(Graph<V, E> g, WeightFunction<E> w) {
		if (g instanceof IndexGraph) {
			IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
			return (MinimumSpanningTree.Result<V, E>) computeMinimumSpanningTree((IndexGraph) g, w0);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			MinimumSpanningTree.IResult indexResult = computeMinimumSpanningTree(iGraph, iw);
			return MinimumSpanningTrees.resultFromIndexResult(g, indexResult);
		}
	}

	protected abstract MinimumSpanningTree.IResult computeMinimumSpanningTree(IndexGraph g, IWeightFunction w);

	protected static MinimumSpanningTree.IResult newIndexResult(IntSet edges) {
		return new MinimumSpanningTrees.IndexResult(edges);
	}

	protected static MinimumSpanningTree.IResult newIndexResult(int[] edges) {
		return new MinimumSpanningTrees.IndexResult(edges);
	}

}
