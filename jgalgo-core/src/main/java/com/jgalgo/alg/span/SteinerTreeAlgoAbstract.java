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
package com.jgalgo.alg.span;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import com.jgalgo.internal.util.IntAdapters;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntSets;

/**
 * Abstract class for computing Steiner trees in graphs.
 *
 * <p>
 * The class implements the interface by solving the problem on the index graph and then maps the results back to the
 * original graph. The implementation for the index graph is abstract and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class SteinerTreeAlgoAbstract implements SteinerTreeAlgo {

	/**
	 * Default constructor.
	 */
	public SteinerTreeAlgoAbstract() {}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> SteinerTreeAlgo.Result<V, E> computeSteinerTree(Graph<V, E> g, WeightFunction<E> w,
			Collection<V> terminals) {
		if (g instanceof IndexGraph) {
			IntCollection terminals0 = IntAdapters.asIntCollection((Collection<Integer>) terminals);
			IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
			return (SteinerTreeAlgo.Result<V, E>) computeSteinerTree((IndexGraph) g, w0, terminals0);

		} else {
			IndexGraph iGraph = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			IntCollection iTerminals = IndexIdMaps.idToIndexCollection(terminals, viMap);

			SteinerTreeAlgo.IResult indexResult = computeSteinerTree(iGraph, iw, iTerminals);
			return resultFromIndexResult(g, indexResult);
		}
	}

	protected abstract SteinerTreeAlgo.IResult computeSteinerTree(IndexGraph g, IWeightFunction w,
			IntCollection terminals);

	/**
	 * A result object for the Steiner tree computation in an index graph.
	 *
	 * @author Barak Ugav
	 */
	protected static class IndexResult implements SteinerTreeAlgo.IResult {

		private final IntSet edges;
		static final SteinerTreeAlgo.IResult Empty = new IndexResult(IntArrays.EMPTY_ARRAY);

		public IndexResult(IntSet edges) {
			this.edges = IntSets.unmodifiable(Objects.requireNonNull(edges));
		}

		public IndexResult(int[] edges) {
			this.edges = ImmutableIntArraySet.withNaiveContains(edges);
		}

		@Override
		public IntSet edges() {
			return edges;
		}

		@Override
		public String toString() {
			return edges().toString();
		}

	}

	private static class ObjResultFromIndexResult<V, E> implements SteinerTreeAlgo.Result<V, E> {

		private final SteinerTreeAlgo.IResult indexRes;
		private final IndexIdMap<E> eiMap;

		ObjResultFromIndexResult(Graph<V, E> g, SteinerTreeAlgo.IResult res) {
			this.indexRes = Objects.requireNonNull(res);
			this.eiMap = g.indexGraphEdgesMap();
		}

		@Override
		public Set<E> edges() {
			return IndexIdMaps.indexToIdSet(indexRes.edges(), eiMap);
		}
	}

	private static class IntResultFromIndexResult implements SteinerTreeAlgo.IResult {

		private final SteinerTreeAlgo.IResult indexRes;
		private final IndexIntIdMap eiMap;

		IntResultFromIndexResult(IntGraph g, SteinerTreeAlgo.IResult res) {
			this.indexRes = Objects.requireNonNull(res);
			this.eiMap = g.indexGraphEdgesMap();
		}

		@Override
		public IntSet edges() {
			return IndexIdMaps.indexToIdSet(indexRes.edges(), eiMap);
		}
	}

	@SuppressWarnings("unchecked")
	private static <V, E> SteinerTreeAlgo.Result<V, E> resultFromIndexResult(Graph<V, E> g,
			SteinerTreeAlgo.IResult indexResult) {
		assert !(g instanceof IndexGraph);
		if (g instanceof IntGraph) {
			return (SteinerTreeAlgo.Result<V, E>) new IntResultFromIndexResult((IntGraph) g, indexResult);
		} else {
			return new ObjResultFromIndexResult<>(g, indexResult);
		}
	}

}
