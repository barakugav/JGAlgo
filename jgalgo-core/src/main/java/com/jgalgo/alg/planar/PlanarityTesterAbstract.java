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

import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Supplier;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IntGraph;

public abstract class PlanarityTesterAbstract implements PlanarityTester {

	public PlanarityTesterAbstract() {}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> PlanarityTester.Result<V, E> findPlanarEmbedding(Graph<V, E> g, boolean addAsEdgesWeights) {
		if (g instanceof IndexGraph) {
			return (PlanarityTester.Result<V, E>) findPlanarEmbedding((IndexGraph) g, addAsEdgesWeights);
		} else {
			IndexGraph ig = g.indexGraph();
			PlanarityTester.Result<Integer, Integer> indexResult = findPlanarEmbedding(ig, addAsEdgesWeights);
			if (indexResult.isPlanar()) {
				return Result.planar(() -> {
					IPlanarEmbedding indexEmbedding = (IPlanarEmbedding) indexResult.getEmbedding();
					return PlanarEmbeddings.embeddingFromIndexEmbedding(g, indexEmbedding);
				});
			} else {
				return Result.nonPlanar(() -> {
					Optional<Graph<Integer, Integer>> kuratowskiSubdivisionOpt = indexResult.getKuratowskiSubdivision();
					if (!kuratowskiSubdivisionOpt.isPresent())
						return Optional.empty();
					IntGraph subGraph = (IntGraph) kuratowskiSubdivisionOpt.get();
					Set<V> subGraphVertices = IndexIdMaps.indexToIdSet(subGraph.vertices(), g.indexGraphVerticesMap());
					Set<E> subGraphEdges = IndexIdMaps.indexToIdSet(subGraph.edges(), g.indexGraphEdgesMap());
					return Optional.of(g.subGraphCopy(subGraphVertices, subGraphEdges));
				});

			}
		}
	}

	protected abstract PlanarityTester.Result<Integer, Integer> findPlanarEmbedding(IndexGraph g,
			boolean addAsEdgesWeights);

	static class Result<V, E> implements PlanarityTester.Result<V, E> {
		private final boolean isPlanar;
		private final Supplier<PlanarEmbedding<V, E>> embeddingSupplier;
		private final Supplier<Optional<Graph<V, E>>> kuratowskiSubdivisionSupplier;
		private Optional<PlanarEmbedding<V, E>> embedding;
		private Optional<Optional<Graph<V, E>>> kuratowskiSubdivision;

		private Result(boolean isPlanar, Supplier<PlanarEmbedding<V, E>> embeddingSupplier,
				Supplier<Optional<Graph<V, E>>> kuratowskiSubdivisionSupplier) {
			this.isPlanar = isPlanar;
			this.embeddingSupplier = embeddingSupplier;
			this.kuratowskiSubdivisionSupplier = kuratowskiSubdivisionSupplier;
			this.embedding = Optional.empty();
			this.kuratowskiSubdivision = Optional.empty();
		}

		static <V, E> Result<V, E> planar(Supplier<PlanarEmbedding<V, E>> embedding) {
			return new Result<>(true, Objects.requireNonNull(embedding), null);
		}

		static <V, E> Result<V, E> nonPlanar(Supplier<Optional<Graph<V, E>>> kuratowskiSubdivision) {
			return new Result<>(false, null, Objects.requireNonNull(kuratowskiSubdivision));
		}

		@Override
		public boolean isPlanar() {
			return isPlanar;
		}

		@Override
		public PlanarEmbedding<V, E> getEmbedding() {
			if (!isPlanar)
				throw new IllegalStateException("Graph is not planar");
			if (!embedding.isPresent())
				embedding = Optional.of(embeddingSupplier.get());
			return embedding.get();
		}

		@Override
		public Optional<Graph<V, E>> getKuratowskiSubdivision() {
			if (isPlanar)
				throw new IllegalStateException("Graph is planar");
			if (!kuratowskiSubdivision.isPresent())
				kuratowskiSubdivision = Optional.of(kuratowskiSubdivisionSupplier.get());
			return kuratowskiSubdivision.get();
		}
	}

}
