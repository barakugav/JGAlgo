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
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;

class ShortestPathSTs {

	private ShortestPathSTs() {}

	abstract static class AbstractImpl implements ShortestPathST {

		@SuppressWarnings({ "unchecked", "rawtypes" })
		@Override
		public <V, E> ObjectDoublePair<Path<V, E>> computeShortestPathAndWeight(Graph<V, E> g, WeightFunction<E> w,
				V source, V target) {
			if (g instanceof IndexGraph) {
				IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
				int source0 = ((Integer) source).intValue(), target0 = ((Integer) target).intValue();
				return (ObjectDoublePair) computeShortestPathAndWeight((IndexGraph) g, w0, source0, target0);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
				int iSource = viMap.idToIndex(source);
				int iTarget = viMap.idToIndex(target);

				ObjectDoublePair<IPath> indexPath = NegativeCycleException
						.runAndConvertException(g, () -> computeShortestPathAndWeight(iGraph, iw, iSource, iTarget));
				return indexPath == null ? null
						: ObjectDoublePair.of(Path.pathFromIndexPath(g, indexPath.first()), indexPath.secondDouble());
			}
		}

		abstract ObjectDoublePair<IPath> computeShortestPathAndWeight(IndexGraph g, IWeightFunction w, int source,
				int target);

	}

}
