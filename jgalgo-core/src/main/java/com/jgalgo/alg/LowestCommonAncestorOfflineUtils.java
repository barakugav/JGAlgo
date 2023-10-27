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
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

class LowestCommonAncestorOfflineUtils {

	static abstract class AbstractImpl implements LowestCommonAncestorOffline {

		@Override
		public LowestCommonAncestorOffline.Result findLCAs(Graph tree, int root,
				LowestCommonAncestorOffline.Queries queries) {
			if (tree instanceof IndexGraph)
				return findLCAs((IndexGraph) tree, root, queries);

			IndexGraph iGraph = tree.indexGraph();
			IndexIdMap viMap = tree.indexGraphVerticesMap();
			int iRoot = viMap.idToIndex(root);
			LowestCommonAncestorOffline.Queries iQueries = new IndexQueriesFromQueries(queries, viMap);

			LowestCommonAncestorOffline.Result indexResult = findLCAs(iGraph, iRoot, iQueries);
			return new ResultFromIndexResult(indexResult, viMap);
		}

		abstract LowestCommonAncestorOffline.Result findLCAs(IndexGraph tree, int root,
				LowestCommonAncestorOffline.Queries queries);

	}

	static class QueriesImpl implements LowestCommonAncestorOffline.Queries {
		private final LongList qs;

		QueriesImpl() {
			qs = new LongArrayList();
		}

		@Override
		public void addQuery(int u, int v) {
			qs.add(JGAlgoUtils.longPack(u, v));
		}

		@Override
		public int getQuerySource(int idx) {
			return JGAlgoUtils.long2low(qs.getLong(idx));
		}

		@Override
		public int getQueryTarget(int idx) {
			return JGAlgoUtils.long2high(qs.getLong(idx));
		}

		@Override
		public int size() {
			return qs.size();
		}

		@Override
		public void clear() {
			qs.clear();
		}
	}

	static class IndexQueriesFromQueries implements LowestCommonAncestorOffline.Queries {
		private final LowestCommonAncestorOffline.Queries qs;
		private final IndexIdMap viMap;

		IndexQueriesFromQueries(LowestCommonAncestorOffline.Queries qs, IndexIdMap viMap) {
			this.qs = Objects.requireNonNull(qs);
			this.viMap = Objects.requireNonNull(viMap);
		}

		@Override
		public void addQuery(int u, int v) {
			qs.addQuery(viMap.indexToId(u), viMap.indexToId(v));
		}

		@Override
		public int getQuerySource(int idx) {
			return viMap.idToIndex(qs.getQuerySource(idx));
		}

		@Override
		public int getQueryTarget(int idx) {
			return viMap.idToIndex(qs.getQueryTarget(idx));
		}

		@Override
		public int size() {
			return qs.size();
		}

		@Override
		public void clear() {
			qs.clear();
		}
	}

	static class ResultImpl implements LowestCommonAncestorOffline.Result {

		private final int[] res;

		ResultImpl(int[] res) {
			this.res = res;
		}

		@Override
		public int getLca(int queryIdx) {
			return res[queryIdx];
		}

		@Override
		public int size() {
			return res.length;
		}

	}

	private static class ResultFromIndexResult implements LowestCommonAncestorOffline.Result {

		private final LowestCommonAncestorOffline.Result res;
		private final IndexIdMap viMap;

		ResultFromIndexResult(LowestCommonAncestorOffline.Result res, IndexIdMap viMap) {
			this.res = Objects.requireNonNull(res);
			this.viMap = Objects.requireNonNull(viMap);
		}

		@Override
		public int getLca(int queryIdx) {
			int vIdx = res.getLca(queryIdx);
			return vIdx == -1 ? -1 : viMap.indexToId(vIdx);
		}

		@Override
		public int size() {
			return res.size();
		}
	}

}
