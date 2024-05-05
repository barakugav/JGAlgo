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
package com.jgalgo.alg.path;

import java.util.function.IntToDoubleFunction;
import java.util.function.ToDoubleFunction;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;

/**
 * Abstract class for computing shortest path between a source and a target with a heuristic.
 *
 * <p>
 * The class implements the interface by solving the problem on the index graph and then maps the results back to the
 * original graph. The implementation for the index graph is abstract and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class ShortestPathHeuristicStAbstract implements ShortestPathHeuristicSt {

	/**
	 * Default constructor.
	 */
	public ShortestPathHeuristicStAbstract() {}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> Path<V, E> computeShortestPath(Graph<V, E> g, WeightFunction<E> w, V source, V target,
			ToDoubleFunction<V> vHeuristic) {
		if (g instanceof IndexGraph) {
			IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
			int source0 = ((Integer) source).intValue();
			int target0 = ((Integer) target).intValue();
			ToDoubleFunction<Integer> vHeuristic0 = (ToDoubleFunction<Integer>) vHeuristic;
			IntToDoubleFunction vHeuristic1 = v -> vHeuristic0.applyAsDouble(Integer.valueOf(v));
			return (Path<V, E>) computeShortestPath((IndexGraph) g, w0, source0, target0, vHeuristic1);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			int iSource = viMap.idToIndex(source);
			int iTarget = viMap.idToIndex(target);
			IntToDoubleFunction indexVHeuristic = vIdx -> vHeuristic.applyAsDouble(viMap.indexToId(vIdx));
			IPath indexPath = NegativeCycleException
					.runAndConvertException(g,
							() -> computeShortestPath(iGraph, iw, iSource, iTarget, indexVHeuristic));
			return Path.pathFromIndexPath(g, indexPath);
		}
	}

	@Override
	public IPath computeShortestPath(IntGraph g, IWeightFunction w, int source, int target,
			IntToDoubleFunction vHeuristic) {
		if (g instanceof IndexGraph) {
			IntToDoubleFunction vHeuristic1 = v -> vHeuristic.applyAsDouble(v);
			return computeShortestPath((IndexGraph) g, w, source, target, vHeuristic1);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIntIdMap viMap = g.indexGraphVerticesMap();
			IndexIntIdMap eiMap = g.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc((WeightFunction<Integer>) w, eiMap);
			int iSource = viMap.idToIndex(source);
			int iTarget = viMap.idToIndex(target);
			IntToDoubleFunction indexVHeuristic = vIdx -> vHeuristic.applyAsDouble(viMap.indexToIdInt(vIdx));
			IPath indexPath = NegativeCycleException
					.runAndConvertException(g,
							() -> computeShortestPath(iGraph, iw, iSource, iTarget, indexVHeuristic));
			return (IPath) Path.pathFromIndexPath(g, indexPath);
		}
	}

	protected abstract IPath computeShortestPath(IndexGraph g, IWeightFunction w, int source, int target,
			IntToDoubleFunction vHeuristic);

}
