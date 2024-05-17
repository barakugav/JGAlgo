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
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;

/**
 * Abstract class for offline LCA computation.
 *
 * <p>
 * The class implements the interface by solving the problem on the {@linkplain Graph#indexGraph() index graph} and then
 * maps the results back to the original graph. The implementation for {@linkplain IndexGraph index graphs} is abstract
 * and left to the subclasses.
 *
 * @author Barak Ugav
 */
public abstract class LowestCommonAncestorOfflineAbstract implements LowestCommonAncestorOffline {

	/**
	 * Default constructor.
	 */
	public LowestCommonAncestorOfflineAbstract() {}

	@SuppressWarnings("unchecked")
	@Override
	public <V, E> LowestCommonAncestorOffline.Result<V, E> findLowestCommonAncestors(Graph<V, E> tree, V root,
			LowestCommonAncestorOffline.Queries<V, E> queries) {
		if (tree instanceof IndexGraph) {
			LowestCommonAncestorOffline.IQueries queries0 =
					asIntQueries((LowestCommonAncestorOffline.Queries<Integer, Integer>) queries);
			return (LowestCommonAncestorOffline.Result<V, E>) findLowestCommonAncestors((IndexGraph) tree,
					((Integer) root).intValue(), queries0);

		} else {
			IndexGraph iGraph = tree.indexGraph();
			IndexIdMap<V> viMap = tree.indexGraphVerticesMap();
			int iRoot = viMap.idToIndex(root);
			LowestCommonAncestorOffline.IQueries iQueries =
					LowestCommonAncestorOfflineQueriesImpl.indexQueriesFromQueries(tree, queries);
			LowestCommonAncestorOffline.IResult indexResult = findLowestCommonAncestors(iGraph, iRoot, iQueries);
			return resultFromIndexResult(tree, indexResult);
		}
	}

	protected abstract LowestCommonAncestorOffline.IResult findLowestCommonAncestors(IndexGraph tree, int root,
			LowestCommonAncestorOffline.IQueries queries);

	/**
	 * Result of a offline LCA algorithm for {@link IndexGraph}.
	 *
	 * @author Barak Ugav
	 */
	protected static class IndexResult implements LowestCommonAncestorOffline.IResult {

		private final int[] res;

		/**
		 * Create a new result object for an index graph.
		 *
		 * @param preQueryResult an array of vertices indices, each representing the LCA of the corresponding query. The
		 *                           array is not copied, so the caller should not modify it.
		 */
		public IndexResult(int[] preQueryResult) {
			this.res = preQueryResult;
		}

		@Override
		public int getLcaInt(int queryIdx) {
			return res[queryIdx];
		}

		@Override
		public int size() {
			return res.length;
		}
	}

	@SuppressWarnings("unchecked")
	private static <V, E> LowestCommonAncestorOffline.Result<V, E> resultFromIndexResult(Graph<V, E> g,
			LowestCommonAncestorOffline.IResult indexResult) {
		assert !(g instanceof IndexGraph);
		if (g instanceof IntGraph) {
			return (LowestCommonAncestorOffline.Result<V, E>) new IntResultFromIndexResult((IntGraph) g, indexResult);
		} else {
			return new ObjResultFromIndexResult<>(g, indexResult);
		}
	}

	private static class ObjResultFromIndexResult<V, E> implements LowestCommonAncestorOffline.Result<V, E> {

		private final LowestCommonAncestorOffline.IResult indexRes;
		private final IndexIdMap<V> viMap;

		ObjResultFromIndexResult(Graph<V, E> g, LowestCommonAncestorOffline.IResult indexRes) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.viMap = g.indexGraphVerticesMap();
		}

		@Override
		public V getLca(int queryIdx) {
			int vIdx = indexRes.getLcaInt(queryIdx);
			return viMap.indexToIdIfExist(vIdx);
		}

		@Override
		public int size() {
			return indexRes.size();
		}
	}

	private static class IntResultFromIndexResult implements LowestCommonAncestorOffline.IResult {

		private final LowestCommonAncestorOffline.IResult indexRes;
		private final IndexIntIdMap viMap;

		IntResultFromIndexResult(IntGraph g, LowestCommonAncestorOffline.IResult indexRes) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.viMap = g.indexGraphVerticesMap();
		}

		@Override
		public int getLcaInt(int queryIdx) {
			int vIdx = indexRes.getLcaInt(queryIdx);
			return viMap.indexToIdIfExistInt(vIdx);
		}

		@Override
		public int size() {
			return indexRes.size();
		}
	}

	static LowestCommonAncestorOffline.IQueries asIntQueries(LowestCommonAncestorOffline.Queries<Integer, Integer> qs) {
		if (qs instanceof LowestCommonAncestorOffline.IQueries) {
			return (LowestCommonAncestorOffline.IQueries) qs;
		}

		LowestCommonAncestorOffline.IQueries qs2 = LowestCommonAncestorOffline.IQueries.newInstance();
		for (int q : range(qs.size()))
			qs2.addQuery(qs.getQuerySource(q).intValue(), qs.getQueryTarget(q).intValue());
		return qs2;
	}

}
