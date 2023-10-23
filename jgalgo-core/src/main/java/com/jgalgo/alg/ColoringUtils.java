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

import java.util.Arrays;
import java.util.Objects;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;

class ColoringUtils {

	static class ResultImpl implements ColoringAlgo.Result {

		int colorsNum;
		final int[] colors;

		ResultImpl(IndexGraph g) {
			colors = new int[g.vertices().size()];
			Arrays.fill(colors, -1);
		}

		@Override
		public int colorsNum() {
			return colorsNum;
		}

		@Override
		public int colorOf(int vertex) {
			return colors[vertex];
		}

	}

	static abstract class AbstractImpl implements ColoringAlgo {

		@Override
		public ColoringAlgo.Result computeColoring(Graph g) {
			if (g instanceof IndexGraph)
				return computeColoring((IndexGraph) g);

			IndexGraph iGraph = g.indexGraph();
			IndexIdMap viMap = g.indexGraphVerticesMap();

			ColoringAlgo.Result indexResult = computeColoring(iGraph);
			return new ResultFromIndexResult(indexResult, viMap);
		}

		abstract ColoringAlgo.Result computeColoring(IndexGraph g);

	}

	static class ResultFromIndexResult implements ColoringAlgo.Result {

		private final ColoringAlgo.Result res;
		private final IndexIdMap viMap;

		ResultFromIndexResult(ColoringAlgo.Result res, IndexIdMap viMap) {
			this.res = Objects.requireNonNull(res);
			this.viMap = Objects.requireNonNull(viMap);
		}

		@Override
		public int colorsNum() {
			return res.colorsNum();
		}

		@Override
		public int colorOf(int vertex) {
			return res.colorOf(viMap.idToIndex(vertex));
		}

	}

}
