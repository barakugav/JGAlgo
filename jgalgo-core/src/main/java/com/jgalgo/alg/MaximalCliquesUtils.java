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
import com.jgalgo.graph.IndexIdMaps;
import it.unimi.dsi.fastutil.ints.IntSet;

class MaximalCliquesUtils {

	static abstract class AbstractImpl implements MaximalCliques {

		@Override
		public Iterator<IntSet> iterateMaximalCliques(IntGraph g) {
			if (g instanceof IndexGraph)
				return iterateMaximalCliques((IndexGraph) g);

			IndexGraph iGraph = g.indexGraph();
			IndexIntIdMap viMap = g.indexGraphVerticesMap();
			Iterator<IntSet> indexResult = iterateMaximalCliques(iGraph);
			return new ResultFromIndexResult(indexResult, viMap);
		}

		abstract Iterator<IntSet> iterateMaximalCliques(IndexGraph g);

	}

	private static class ResultFromIndexResult implements Iterator<IntSet> {

		private final Iterator<IntSet> indexResult;
		private final IndexIntIdMap viMap;

		ResultFromIndexResult(Iterator<IntSet> indexResult, IndexIntIdMap viMap) {
			this.indexResult = indexResult;
			this.viMap = viMap;
		}

		@Override
		public boolean hasNext() {
			return indexResult.hasNext();
		}

		@Override
		public IntSet next() {
			return IndexIdMaps.indexToIdSet(indexResult.next(), viMap);
		}

	}

}
