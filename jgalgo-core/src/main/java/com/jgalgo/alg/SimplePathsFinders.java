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
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;

class SimplePathsFinders {

	private SimplePathsFinders() {}

	abstract static class AbstractImpl implements SimplePathsFinder {

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public <V, E> Iterator<Path<V, E>> findAllSimplePaths(Graph<V, E> g, V source, V target) {
			if (g instanceof IndexGraph) {
				int source0 = ((Integer) source).intValue(), target0 = ((Integer) target).intValue();
				return (Iterator) findAllSimplePaths((IndexGraph) g, source0, target0);

			} else {
				IndexGraph iGraph = g.indexGraph();
				IndexIdMap<V> viMap = g.indexGraphVerticesMap();
				int iSource = viMap.idToIndex(source);
				int iTarget = viMap.idToIndex(target);
				Iterator<IPath> indexResult = findAllSimplePaths(iGraph, iSource, iTarget);
				return PathImpl.iterFromIndexIter(g, indexResult);
			}
		}

		abstract Iterator<IPath> findAllSimplePaths(IndexGraph g, int source, int target);

	}

}
