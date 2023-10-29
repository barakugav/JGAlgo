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
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIntIdMap;

abstract class CyclesFinderAbstract implements CyclesFinder {

	@Override
	public Iterator<Path> findAllCycles(IntGraph g) {
		if (g instanceof IndexGraph)
			return findAllCycles((IndexGraph) g);

		IndexGraph iGraph = g.indexGraph();
		IndexIntIdMap viMap = g.indexGraphVerticesMap();
		IndexIntIdMap eiMap = g.indexGraphEdgesMap();

		Iterator<Path> indexResult = findAllCycles(iGraph);
		return new PathImpl.IterFromIndexIter(indexResult, viMap, eiMap);
	}

	abstract Iterator<Path> findAllCycles(IndexGraph g);

}
