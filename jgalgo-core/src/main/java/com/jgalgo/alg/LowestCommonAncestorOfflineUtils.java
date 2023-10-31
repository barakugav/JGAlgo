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
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import it.unimi.dsi.fastutil.objects.ObjectList;

class LowestCommonAncestorOfflineUtils {

	static abstract class AbstractImpl implements LowestCommonAncestorOffline {

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> LowestCommonAncestorOffline.Result<V, E> findLCAs(Graph<V, E> tree, V root,
				LowestCommonAncestorOffline.Queries<V, E> queries) {
			if (tree instanceof IndexGraph && queries instanceof LowestCommonAncestorOffline.IQueries) {
				return (LowestCommonAncestorOffline.Result<V, E>) findLCAs((IndexGraph) tree,
						((Integer) root).intValue(), (LowestCommonAncestorOffline.IQueries) queries);

			} else if (tree instanceof IntGraph) {
				IndexGraph iGraph = tree.indexGraph();
				IndexIntIdMap viMap = ((IntGraph) tree).indexGraphVerticesMap();
				int iRoot = viMap.idToIndex(((Integer) root).intValue());

				LowestCommonAncestorOffline.IQueries iQueries;
				if (queries instanceof LowestCommonAncestorOffline.IQueries) {
					iQueries = new IndexQueriesFromIntQueries((LowestCommonAncestorOffline.IQueries) queries, viMap);
				} else {
					iQueries = new IndexQueriesFromObjQueries<>(queries, tree.indexGraphVerticesMap());
				}

				LowestCommonAncestorOffline.IResult indexResult = findLCAs(iGraph, iRoot, iQueries);
				return (LowestCommonAncestorOffline.Result<V, E>) new IntResultFromIndexResult(indexResult, viMap);

			} else {
				IndexGraph iGraph = tree.indexGraph();
				IndexIdMap<V> viMap = tree.indexGraphVerticesMap();
				int iRoot = viMap.idToIndex(root);
				LowestCommonAncestorOffline.IQueries iQueries = new IndexQueriesFromObjQueries<>(queries, viMap);

				LowestCommonAncestorOffline.IResult indexResult = findLCAs(iGraph, iRoot, iQueries);
				return new ObjResultFromIndexResult<>(indexResult, viMap);
			}
		}

		abstract LowestCommonAncestorOffline.IResult findLCAs(IndexGraph tree, int root,
				LowestCommonAncestorOffline.IQueries queries);

	}

	static class ObjQueriesImpl<V, E> implements LowestCommonAncestorOffline.Queries<V, E> {
		private final ObjectList<V> qs;

		ObjQueriesImpl() {
			qs = new ObjectArrayList<>();
		}

		@Override
		public void addQuery(V u, V v) {
			qs.add(u);
			qs.add(v);
		}

		@Override
		public V getQuerySource(int idx) {
			return qs.get(idx * 2 + 0);
		}

		@Override
		public V getQueryTarget(int idx) {
			return qs.get(idx * 2 + 1);
		}

		@Override
		public int size() {
			return qs.size() / 2;
		}

		@Override
		public void clear() {
			qs.clear();
		}
	}

	static class IntQueriesImpl implements LowestCommonAncestorOffline.IQueries {
		private final LongList qs;

		IntQueriesImpl() {
			qs = new LongArrayList();
		}

		@Override
		public void addQuery(int u, int v) {
			qs.add(JGAlgoUtils.longPack(u, v));
		}

		@Override
		public int getQuerySourceInt(int idx) {
			return JGAlgoUtils.long2low(qs.getLong(idx));
		}

		@Override
		public int getQueryTargetInt(int idx) {
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

	static class IndexQueriesFromObjQueries<V, E> implements LowestCommonAncestorOffline.IQueries {
		private final LowestCommonAncestorOffline.Queries<V, E> qs;
		private final IndexIdMap<V> viMap;

		IndexQueriesFromObjQueries(LowestCommonAncestorOffline.Queries<V, E> qs, IndexIdMap<V> viMap) {
			this.qs = Objects.requireNonNull(qs);
			this.viMap = Objects.requireNonNull(viMap);
		}

		@Override
		public void addQuery(int u, int v) {
			qs.addQuery(viMap.indexToId(u), viMap.indexToId(v));
		}

		@Override
		public int getQuerySourceInt(int idx) {
			return viMap.idToIndex(qs.getQuerySource(idx));
		}

		@Override
		public int getQueryTargetInt(int idx) {
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

	static class IndexQueriesFromIntQueries implements LowestCommonAncestorOffline.IQueries {
		private final LowestCommonAncestorOffline.IQueries qs;
		private final IndexIntIdMap viMap;

		IndexQueriesFromIntQueries(LowestCommonAncestorOffline.IQueries qs, IndexIntIdMap viMap) {
			this.qs = Objects.requireNonNull(qs);
			this.viMap = Objects.requireNonNull(viMap);
		}

		@Override
		public void addQuery(int u, int v) {
			qs.addQuery(viMap.indexToIdInt(u), viMap.indexToIdInt(v));
		}

		@Override
		public int getQuerySourceInt(int idx) {
			return viMap.idToIndex(qs.getQuerySourceInt(idx));
		}

		@Override
		public int getQueryTargetInt(int idx) {
			return viMap.idToIndex(qs.getQueryTargetInt(idx));
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

	static class ResultImpl implements LowestCommonAncestorOffline.IResult {

		private final int[] res;

		ResultImpl(int[] res) {
			this.res = res;
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

	private static class ObjResultFromIndexResult<V, E> implements LowestCommonAncestorOffline.Result<V, E> {

		private final LowestCommonAncestorOffline.IResult indexRes;
		private final IndexIdMap<V> viMap;

		ObjResultFromIndexResult(LowestCommonAncestorOffline.IResult indexRes, IndexIdMap<V> viMap) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.viMap = Objects.requireNonNull(viMap);
		}

		@Override
		public V getLca(int queryIdx) {
			int vIdx = indexRes.getLcaInt(queryIdx);
			return vIdx == -1 ? null : viMap.indexToId(vIdx);
		}

		@Override
		public int size() {
			return indexRes.size();
		}
	}

	private static class IntResultFromIndexResult implements LowestCommonAncestorOffline.IResult {

		private final LowestCommonAncestorOffline.IResult indexRes;
		private final IndexIntIdMap viMap;

		IntResultFromIndexResult(LowestCommonAncestorOffline.IResult indexRes, IndexIntIdMap viMap) {
			this.indexRes = Objects.requireNonNull(indexRes);
			this.viMap = Objects.requireNonNull(viMap);
		}

		@Override
		public int getLcaInt(int queryIdx) {
			int vIdx = indexRes.getLcaInt(queryIdx);
			return vIdx == -1 ? -1 : viMap.indexToIdInt(vIdx);
		}

		@Override
		public int size() {
			return indexRes.size();
		}
	}

}
