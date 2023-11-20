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
package com.jgalgo.alg.planar;

import java.util.Optional;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;

public abstract class PlanarityTesterAbstract implements PlanarityTester {

	public PlanarityTesterAbstract() {}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> Optional<PlanarEmbedding<V, E>> findPlanarEmbedding(Graph<V, E> g, boolean addAsEdgesWeights) {
		if (g instanceof IndexGraph) {
			return findPlanarEmbedding((IndexGraph) g, addAsEdgesWeights)
					.map(embeddings -> (PlanarEmbedding<V, E>) embeddings);
		} else {
			IndexGraph ig = g.indexGraph();
			Optional<IPlanarEmbedding> indexEmbedding = findPlanarEmbedding(ig, addAsEdgesWeights);
			return indexEmbedding.map(embedding -> PlanarEmbeddings.embeddingFromIndexEmbedding(g, embedding));
		}
	}

	protected abstract Optional<IPlanarEmbedding> findPlanarEmbedding(IndexGraph g, boolean addAsEdgesWeights);

}
