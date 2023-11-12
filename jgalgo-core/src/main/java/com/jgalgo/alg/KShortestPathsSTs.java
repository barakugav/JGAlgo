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

import java.util.ArrayList;
import java.util.List;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;

class KShortestPathsSTs {

	private KShortestPathsSTs() {}

	abstract static class AbstractImpl implements KShortestPathsST {

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public <V, E> List<Path<V, E>> computeKShortestPaths(Graph<V, E> g, WeightFunction<E> w, V source, V target,
				int k) {
			if (g instanceof IndexGraph) {
				IndexGraph iGraph = (IndexGraph) g;
				IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
				int source0 = ((Integer) source).intValue(), target0 = ((Integer) target).intValue();
				return (List) computeKShortestPaths(iGraph, w0, source0, target0, k);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
				int iSource = viMap.idToIndex(source);
				int iTarget = viMap.idToIndex(target);
				List<IPath> indexResult = computeKShortestPaths(iGraph, iw, iSource, iTarget, k);
				List<Path<V, E>> result = new ArrayList<>(indexResult.size());
				for (IPath p : indexResult)
					result.add(PathImpl.pathFromIndexPath(g, p));
				return result;
			}
		}

		abstract List<IPath> computeKShortestPaths(IndexGraph g, IWeightFunction w, int source, int target, int k);

	}

}
