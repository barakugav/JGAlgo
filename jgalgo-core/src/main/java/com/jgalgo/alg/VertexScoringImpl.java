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

import java.util.Objects;
import com.jgalgo.graph.IndexIdMap;

class VertexScoringImpl {

	static class ResultImpl implements VertexScoring {

		private final double[] scores;

		ResultImpl(double[] scores) {
			this.scores = scores;
		}

		@Override
		public double vertexScore(int vertex) {
			return scores[vertex];
		}

	}

	static class ResultFromIndexResult implements VertexScoring {

		private final VertexScoring res;
		private final IndexIdMap viMap;

		ResultFromIndexResult(VertexScoring res, IndexIdMap viMap) {
			this.res = Objects.requireNonNull(res);
			this.viMap = Objects.requireNonNull(viMap);
		}

		@Override
		public double vertexScore(int vertex) {
			return res.vertexScore(viMap.idToIndex(vertex));
		}

	}

}