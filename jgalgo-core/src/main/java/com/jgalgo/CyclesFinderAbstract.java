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
package com.jgalgo;

import java.util.Iterator;
import java.util.Objects;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;

abstract class CyclesFinderAbstract implements CyclesFinder {

	@Override
	public Iterator<Path> findAllCycles(Graph g) {
		if (g instanceof IndexGraph)
			return findAllCycles((IndexGraph) g);

		IndexGraph iGraph = g.indexGraph();
		IndexIdMap viMap = g.indexGraphVerticesMap();
		IndexIdMap eiMap = g.indexGraphEdgesMap();

		Iterator<Path> indexResult = findAllCycles(iGraph);
		return new ResultFromIndexResult(indexResult, viMap, eiMap);
	}

	abstract Iterator<Path> findAllCycles(IndexGraph g);

	private static class ResultFromIndexResult implements Iterator<Path> {

		private final Iterator<Path> res;
		private final IndexIdMap viMap;
		private final IndexIdMap eiMap;

		ResultFromIndexResult(Iterator<Path> res, IndexIdMap viMap, IndexIdMap eiMap) {
			this.res = Objects.requireNonNull(res);
			this.viMap = Objects.requireNonNull(viMap);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		@Override
		public boolean hasNext() {
			return res.hasNext();
		}

		@Override
		public Path next() {
			return PathImpl.pathFromIndexPath(res.next(), viMap, eiMap);
		}

	}

}
