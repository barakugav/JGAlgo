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
import java.util.List;
import java.util.Objects;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IndexIntIdMap;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.IntPair;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import it.unimi.dsi.fastutil.longs.LongList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

class TreePathMaximaUtils {

	abstract static class AbstractImpl implements TreePathMaxima {

		@SuppressWarnings("unchecked")
		@Override
		public <V, E> TreePathMaxima.Result<V, E> computeHeaviestEdgeInTreePaths(Graph<V, E> tree, WeightFunction<E> w,
				TreePathMaxima.Queries<V, E> queries) {
			if (tree instanceof IndexGraph && queries instanceof TreePathMaxima.IQueries) {
				IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) w);
				TreePathMaxima.IQueries queries0 = (TreePathMaxima.IQueries) queries;
				return (TreePathMaxima.Result<V, E>) computeHeaviestEdgeInTreePaths((IndexGraph) tree, w0, queries0);

			} else {
				IndexGraph iGraph = tree.indexGraph();
				IndexIdMap<E> eiMap = tree.indexGraphEdgesMap();
				IWeightFunction iw = IndexIdMaps.idToIndexWeightFunc(w, eiMap);
				TreePathMaxima.IQueries iQueries = indexQueriesFromQueries(tree, queries);
				TreePathMaxima.IResult indexResult = computeHeaviestEdgeInTreePaths(iGraph, iw, iQueries);
				return resultFromIndexResult(tree, indexResult);
			}
		}

		abstract TreePathMaxima.IResult computeHeaviestEdgeInTreePaths(IndexGraph tree, IWeightFunction w,
				TreePathMaxima.IQueries queries);

	}

	static class ObjQueriesImpl<V, E> implements TreePathMaxima.Queries<V, E> {
		private final List<V> qs;

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

	static class IntQueriesImpl implements TreePathMaxima.IQueries {
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

	static class ResultImpl implements TreePathMaxima.IResult {

		private final int[] res;

		ResultImpl(int[] res) {
			this.res = res;
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

	static class IndexQueriesFromObjQueries<V, E> implements TreePathMaxima.IQueries {
		private final TreePathMaxima.Queries<V, E> qs;
		private final IndexIdMap<V> viMap;

		IndexQueriesFromObjQueries(Graph<V, E> g, TreePathMaxima.Queries<V, E> qs) {
			this.qs = Objects.requireNonNull(qs);
			this.viMap = g.indexGraphVerticesMap();
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

	static class IndexQueriesFromIntQueries implements TreePathMaxima.IQueries {
		private final TreePathMaxima.IQueries qs;
		private final IndexIntIdMap viMap;

		IndexQueriesFromIntQueries(IntGraph g, TreePathMaxima.IQueries qs) {
			this.qs = Objects.requireNonNull(qs);
			this.viMap = g.indexGraphVerticesMap();
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

	private static <V, E> TreePathMaxima.IQueries indexQueriesFromQueries(Graph<V, E> g,
			TreePathMaxima.Queries<V, E> queries) {
		assert !(g instanceof IndexGraph);
		if (g instanceof IntGraph && queries instanceof TreePathMaxima.IQueries) {
			return new IndexQueriesFromIntQueries((IntGraph) g, (TreePathMaxima.IQueries) queries);
		} else {
			return new IndexQueriesFromObjQueries<>(g, queries);
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

	static boolean verifyMst(IndexGraph g, IWeightFunction w, IntCollection mstEdges, TreePathMaxima tpmAlgo) {
		Assertions.onlyUndirected(g);
		int n = g.vertices().size();
		IndexGraphBuilder mstBuilder = IndexGraphBuilder.undirected();
		mstBuilder.ensureVertexCapacity(n);
		mstBuilder.ensureEdgeCapacity(mstEdges.size());

		mstBuilder.addVertices(range(n));
		double[] mstWeights = new double[mstEdges.size()];
		for (int e : mstEdges) {
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			int ne = mstBuilder.addEdge(u, v);
			mstWeights[ne] = w.weight(e);
		}
		IndexGraph mst = mstBuilder.build();
		if (!Trees.isTree(mst))
			return false;

		TreePathMaxima.IQueries queries = TreePathMaxima.IQueries.newInstance();
		for (int e : range(g.edges().size())) {
			int u = g.edgeSource(e);
			int v = g.edgeTarget(e);
			if (u != v)
				queries.addQuery(u, v);
		}
		IWeightFunction w0 = e -> mstWeights[e];
		TreePathMaxima.IResult tpmResults =
				(TreePathMaxima.IResult) tpmAlgo.computeHeaviestEdgeInTreePaths(mst, w0, queries);

		int i = 0;
		for (int e : range(g.edges().size())) {
			if (g.edgeSource(e) == g.edgeTarget(e))
				continue;
			int mstEdge = tpmResults.getHeaviestEdgeInt(i++);
			if (mstEdge < 0 || w.weight(e) < mstWeights[mstEdge])
				return false;
		}
		return true;
	}

}
