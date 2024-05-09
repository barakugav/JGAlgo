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
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;

class VertexScoringImpl {

	static class IndexResult implements IVertexScoring {

		private final double[] scores;

		IndexResult(double[] scores) {
			this.scores = scores;
		}

		@Override
		public double vertexScore(int vertex) {
			return scores[vertex];
		}
	}

	static class ObjResultFromIndexResult<V, E> implements VertexScoring<V, E> {

		private final IVertexScoring indexRes;
		private final IndexIdMap<V> viMap;

		ObjResultFromIndexResult(Graph<V, E> g, IVertexScoring indexRes) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.viMap = g.indexGraphVerticesMap();
		}

		@Override
		public double vertexScore(V vertex) {
			return indexRes.vertexScore(viMap.idToIndex(vertex));
		}
	}

	static class IntResultFromIndexResult implements IVertexScoring {

		private final IVertexScoring indexRes;
		private final IndexIntIdMap viMap;

		IntResultFromIndexResult(IntGraph g, IVertexScoring indexRes) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.viMap = g.indexGraphVerticesMap();
		}

		@Override
		public double vertexScore(int vertex) {
			return indexRes.vertexScore(viMap.idToIndex(vertex));
		}
	}

	@SuppressWarnings("unchecked")
	static <V, E> VertexScoring<V, E> resultFromIndexResult(Graph<V, E> g, IVertexScoring indexRes) {
		assert !(g instanceof IndexIdMap);
		if (g instanceof IntGraph) {
			return (VertexScoring<V, E>) new IntResultFromIndexResult((IntGraph) g, indexRes);
		} else {
			return new ObjResultFromIndexResult<>(g, indexRes);
		}
	}

}
