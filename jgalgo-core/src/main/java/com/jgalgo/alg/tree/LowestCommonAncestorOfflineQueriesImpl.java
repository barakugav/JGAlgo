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
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.internal.util.IntPair;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

class LowestCommonAncestorOfflineQueriesImpl {

	private LowestCommonAncestorOfflineQueriesImpl() {}

	static final class ObjQueriesImpl<V, E> implements LowestCommonAncestorOffline.Queries<V, E> {

		private final LowestCommonAncestorOffline.IQueries indexQueries = new IntQueriesImpl();
		private final IndexIdMap<V> viMap;

		ObjQueriesImpl(Graph<V, E> g) {
			assert !(g instanceof IndexGraph);
			viMap = g.indexGraphVerticesMap();
		}

		@Override
		public void addQuery(V u, V v) {
			indexQueries.addQuery(viMap.idToIndex(u), viMap.idToIndex(v));
		}

		@Override
		public V getQuerySource(int idx) {
			return viMap.indexToId(indexQueries.getQuerySourceInt(idx));
		}

		@Override
		public V getQueryTarget(int idx) {
			return viMap.indexToId(indexQueries.getQueryTargetInt(idx));
		}

		@Override
		public int size() {
			return indexQueries.size();
		}

		@Override
		public void clear() {
			indexQueries.clear();
		}
	}

	static class IntQueriesImpl implements LowestCommonAncestorOffline.IQueries {
		private final LongList qs;

		IntQueriesImpl() {
			qs = new LongArrayList();
		}

		@Override
		public void addQuery(int u, int v) {
			qs.add(IntPair.of(u, v));
		}

		@Override
		public int getQuerySourceInt(int idx) {
			return IntPair.first(qs.getLong(idx));
		}

		@Override
		public int getQueryTargetInt(int idx) {
			return IntPair.second(qs.getLong(idx));
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

	static <V, E> LowestCommonAncestorOffline.IQueries indexQueriesFromQueries(Graph<V, E> g,
			LowestCommonAncestorOffline.Queries<V, E> queries) {
		assert !(g instanceof IndexGraph);
		if (queries instanceof ObjQueriesImpl) {
			ObjQueriesImpl<V, E> q0 = (ObjQueriesImpl<V, E>) queries;
			if (q0.viMap != g.indexGraphVerticesMap())
				throw new IllegalArgumentException("queries object was created with different graph");
			return q0.indexQueries;
		}

		LowestCommonAncestorOffline.IQueries qs = LowestCommonAncestorOffline.IQueries.newInstance(g.indexGraph());
		if (g instanceof IntGraph && queries instanceof LowestCommonAncestorOffline.IQueries) {
			IndexIntIdMap viMap = ((IntGraph) g).indexGraphVerticesMap();
			LowestCommonAncestorOffline.IQueries queries0 = (LowestCommonAncestorOffline.IQueries) queries;
			for (int q : range(queries.size())) {
				int u = queries0.getQuerySourceInt(q), v = queries0.getQueryTargetInt(q);
				qs.addQuery(viMap.idToIndex(u), viMap.idToIndex(v));
			}

		} else {
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			for (int q : range(queries.size())) {
				V u = queries.getQuerySource(q), v = queries.getQueryTarget(q);
				qs.addQuery(viMap.idToIndex(u), viMap.idToIndex(v));
			}
		}
		return qs;
	}

}
