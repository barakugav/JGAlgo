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
package com.jgalgo.alg.cycle;

import java.util.Optional;
import com.jgalgo.alg.common.IPath;
import com.jgalgo.alg.common.Path;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;

/**
 * Abstract class for computing a shortest edge visitor circle in a graph.
 *
 * <p>
 * The class implements the interface by solving the problem on the {@linkplain Graph#indexGraph() index graph} and then
 * maps the results back to the original graph. The implementation for {@linkplain IndexGraph index graphs} is abstract
 * and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class ChinesePostmanAbstract implements ChinesePostman {

	/**
	 * Default constructor.
	 */
	public ChinesePostmanAbstract() {}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> Optional<Path<V, E>> computeShortestEdgeVisitorCircleIfExist(Graph<V, E> g, WeightFunction<E> w) {
		if (g instanceof IndexGraph) {
			IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
			return computeShortestEdgeVisitorCircleIfExist((IndexGraph) g, w0).map(path -> (Path<V, E>) path);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			Optional<IPath> indexPath = computeShortestEdgeVisitorCircleIfExist(iGraph, iw);
			return indexPath.map(path -> Path.pathFromIndexPath(g, path));
		}
	}

	protected abstract Optional<IPath> computeShortestEdgeVisitorCircleIfExist(IndexGraph g, IWeightFunction w);

}
