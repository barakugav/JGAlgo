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
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;

class TreePathMaximaUtils {

	static abstract class AbstractImpl implements TreePathMaxima {

		@Override
		public TreePathMaxima.Result computeHeaviestEdgeInTreePaths(Graph tree, WeightFunction w,
				TreePathMaxima.Queries queries) {
			if (tree instanceof IndexGraph)
				return computeHeaviestEdgeInTreePaths((IndexGraph) tree, w, queries);

			IndexGraph iGraph = tree.indexGraph();
			IndexIdMap viMap = tree.indexGraphVerticesMap();
			IndexIdMap eiMap = tree.indexGraphEdgesMap();
			WeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
			TreePathMaxima.Queries iQueries = new IndexQueriesFromQueries(queries, viMap);

			TreePathMaxima.Result indexResult = computeHeaviestEdgeInTreePaths(iGraph, iw, iQueries);
			return new ResultFromIndexResult(indexResult, eiMap);
		}

		abstract TreePathMaxima.Result computeHeaviestEdgeInTreePaths(IndexGraph tree, WeightFunction w,
				TreePathMaxima.Queries queries);

	}

	static class QueriesImpl implements TreePathMaxima.Queries {
		private final LongList qs;

		QueriesImpl() {
			qs = new LongArrayList();
		}

		@Override
		public void addQuery(int u, int v) {
			qs.add(JGAlgoUtils.longCompose(u, v));
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

	static class ResultImpl implements TreePathMaxima.Result {

		private final int[] res;

		ResultImpl(int[] res) {
			this.res = res;
		}

		@Override
		public int getHeaviestEdge(int queryIdx) {
			return res[queryIdx];
		}

		@Override
		public int size() {
			return res.length;
		}

	}

	static class IndexQueriesFromQueries implements TreePathMaxima.Queries {
		private final TreePathMaxima.Queries qs;
		private final IndexIdMap viMap;

		IndexQueriesFromQueries(TreePathMaxima.Queries qs, IndexIdMap viMap) {
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

	private static class ResultFromIndexResult implements TreePathMaxima.Result {

		private final TreePathMaxima.Result res;
		private final IndexIdMap eiMap;

		ResultFromIndexResult(TreePathMaxima.Result res, IndexIdMap eiMap) {
			this.res = Objects.requireNonNull(res);
			this.eiMap = Objects.requireNonNull(eiMap);
		}

		@Override
		public int getHeaviestEdge(int queryIdx) {
			int eIdx = res.getHeaviestEdge(queryIdx);
			return eIdx == -1 ? -1 : eiMap.indexToId(eIdx);
		}

		@Override
		public int size() {
			return res.size();
		}
	}

	static boolean verifyMST(IndexGraph g, WeightFunction w, IntCollection mstEdges, TreePathMaxima tpmAlgo) {
		Assertions.Graphs.onlyUndirected(g);
		int n = g.vertices().size();
		IndexGraphBuilder mstBuilder = IndexGraphBuilder.newUndirected();
		mstBuilder.expectedVerticesNum(n);
		mstBuilder.expectedEdgesNum(mstEdges.size());

		for (int v = 0; v < n; v++) {
			int vBuilder = mstBuilder.addVertex();
			assert v == vBuilder;
		}
		double[] mstWeights = new double[mstEdges.size()];
		for (int e : mstEdges) {
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			int ne = mstBuilder.addEdge(u, v);
			mstWeights[ne] = w.weight(e);
		}
		IndexGraph mst = mstBuilder.build();
		if (!Trees.isTree(mst))
			return false;

		TreePathMaxima.Queries queries = TreePathMaxima.Queries.newInstance();
		for (int m = g.edges().size(), e = 0; e < m; e++) {
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			if (u != v)
				queries.addQuery(u, v);
		}
		TreePathMaxima.Result tpmResults = tpmAlgo.computeHeaviestEdgeInTreePaths(mst, e -> mstWeights[e], queries);

		int i = 0;
		for (int m = g.edges().size(), e = 0; e < m; e++) {
			if (g.edgeSource(e) == g.edgeTarget(e))
				continue;
			int mstEdge = tpmResults.getHeaviestEdge(i++);
			if (mstEdge == -1 || w.weight(e) < mstWeights[mstEdge])
				return false;
		}
		return true;
	}

}
