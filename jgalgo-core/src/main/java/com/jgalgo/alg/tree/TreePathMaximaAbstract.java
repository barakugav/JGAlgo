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
package com.jgalgo.alg.tree;

import static com.jgalgo.internal.util.Range.range;
import java.util.Objects;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;

/**
 * Abstract class for TPM computations.
 *
 * <p>
 * The class implements the interface by solving the problem on the {@linkplain Graph#indexGraph() index graph} and then
 * maps the results back to the original graph. The implementation for {@linkplain IndexGraph index graphs} is abstract
 * and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class TreePathMaximaAbstract implements TreePathMaxima {

	/**
	 * Default constructor.
	 */
	public TreePathMaximaAbstract() {}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> TreePathMaxima.Result<V, E> computeHeaviestEdgeInTreePaths(Graph<V, E> tree, WeightFunction<E> w,
			TreePathMaxima.Queries<V, E> queries) {
		if (tree instanceof IndexGraph) {
			IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
			TreePathMaxima.IQueries queries0 =
					asIntQueries((TreePathMaxima.Queries<Integer, Integer>) queries, tree.indexGraph());
			return (TreePathMaxima.Result<V, E>) computeHeaviestEdgeInTreePaths((IndexGraph) tree, w0, queries0);

		} else {
			IndexGraph iGraph = tree.indexGraph();
			IndexIdMap<E> eiMap = tree.indexGraphEdgesMap();
			IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			TreePathMaxima.IQueries iQueries = TreePathMaximaQueriesImpl.indexQueriesFromQueries(tree, queries);
			TreePathMaxima.IResult indexResult = computeHeaviestEdgeInTreePaths(iGraph, iw, iQueries);
			return resultFromIndexResult(tree, indexResult);
		}
	}

	protected abstract TreePathMaxima.IResult computeHeaviestEdgeInTreePaths(IndexGraph tree, IWeightFunction w,
			TreePathMaxima.IQueries queries);

	/**
	 * Result of a TPM algorithm for {@link IndexGraph}.
	 *
	 * @author Barak Ugav
	 */
	protected static class IndexResult implements TreePathMaxima.IResult {

		private final int[] res;

		/**
		 * Create a new result object for an index graph.
		 *
		 * @param preQueryResult an array of edge indices, each representing the maximal weighted edge along the path of
		 *                           the corresponding query. The array is not copied, so the caller should not modify
		 *                           it.
		 */
		public IndexResult(int[] preQueryResult) {
			this.res = preQueryResult;
		}

		@Override
		public int getHeaviestEdgeInt(int queryIdx) {
			return res[queryIdx];
		}

		@Override
		public int size() {
			return res.length;
		}

	}

	private static class ObjResultFromIndexResult<V, E> implements TreePathMaxima.Result<V, E> {

		private final TreePathMaxima.IResult indexRes;
		private final IndexIdMap<E> eiMap;

		ObjResultFromIndexResult(Graph<V, E> g, TreePathMaxima.IResult indexRes) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.eiMap = g.indexGraphEdgesMap();
		}

		@Override
		public E getHeaviestEdge(int queryIdx) {
			int eIdx = indexRes.getHeaviestEdgeInt(queryIdx);
			return eiMap.indexToIdIfExist(eIdx);
		}

		@Override
		public int size() {
			return indexRes.size();
		}
	}

	private static class IntResultFromIndexResult implements TreePathMaxima.IResult {

		private final TreePathMaxima.IResult indexRes;
		private final IndexIntIdMap eiMap;

		IntResultFromIndexResult(IntGraph g, TreePathMaxima.IResult indexRes) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.eiMap = g.indexGraphEdgesMap();
		}

		@Override
		public int getHeaviestEdgeInt(int queryIdx) {
			int eIdx = indexRes.getHeaviestEdgeInt(queryIdx);
			return eiMap.indexToIdIfExistInt(eIdx);
		}

		@Override
		public int size() {
			return indexRes.size();
		}
	}

	@SuppressWarnings("unchecked")
	private static <V, E> TreePathMaxima.Result<V, E> resultFromIndexResult(Graph<V, E> g,
			TreePathMaxima.IResult indexResult) {
		assert !(g instanceof IndexGraph);
		if (g instanceof IntGraph) {
			return (TreePathMaxima.Result<V, E>) new IntResultFromIndexResult((IntGraph) g, indexResult);
		} else {
			return new ObjResultFromIndexResult<>(g, indexResult);
		}
	}

	private static TreePathMaxima.IQueries asIntQueries(TreePathMaxima.Queries<Integer, Integer> qs, IntGraph g) {
		if (qs instanceof TreePathMaxima.IQueries) {
			return (TreePathMaxima.IQueries) qs;
		}

		TreePathMaxima.IQueries qs2 = TreePathMaxima.IQueries.newInstance(g);
		for (int q : range(qs.size()))
			qs2.addQuery(qs.getQuerySource(q).intValue(), qs.getQueryTarget(q).intValue());
		return qs2;
	}

}
