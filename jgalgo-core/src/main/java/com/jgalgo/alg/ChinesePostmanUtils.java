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

class ChinesePostmanUtils {

	private ChinesePostmanUtils() {}

	abstract static class AbstractImpl implements ChinesePostman {

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> Path<V, E> computeShortestEdgeVisitorCircle(Graph<V, E> g, WeightFunction<E> w) {
			if (g instanceof IndexGraph) {
				IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
				return (Path<V, E>) computeShortestEdgeVisitorCircle((IndexGraph) g, w0);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
				IPath indexPath = computeShortestEdgeVisitorCircle(iGraph, iw);
				return Paths.pathFromIndexPath(g, indexPath);
			}
		}

		abstract IPath computeShortestEdgeVisitorCircle(IndexGraph g, IWeightFunction w);

	}

}
