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

import java.util.function.IntFunction;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.ds.UnionFind;
import com.jgalgo.internal.util.Bitmap;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

class LowestCommonAncestorOfflineUnionFind extends LowestCommonAncestorOfflineUtils.AbstractImpl {

	@Override
	LowestCommonAncestorOffline.IResult findLCAs(IndexGraph tree, int root,
			LowestCommonAncestorOffline.IQueries queries) {
		final int n = tree.vertices().size();
		int[] res = new int[queries.size()];

		int[] queriesNum = new int[n];
		int queriesNumCount = 0;
		for (int qNum = queries.size(), q = 0; q < qNum; q++) {
			int u = queries.getQuerySourceInt(q), v = queries.getQueryTargetInt(q);
			if (u != v) {
				queriesNum[u]++;
				queriesNum[v]++;
				queriesNumCount += 2;
			} else {
				res[q] = u;
			}
		}
		int[] perVertexQueries = new int[queriesNumCount];
		int[] perVertexQueriesOffset = new int[n + 1];
		perVertexQueriesOffset[0] = 0;
		for (int v = 1; v < n; v++)
			perVertexQueriesOffset[v] = perVertexQueriesOffset[v - 1] + queriesNum[v - 1];
		for (int qNum = queries.size(), q = 0; q < qNum; q++) {
			int u = queries.getQuerySourceInt(q), v = queries.getQueryTargetInt(q);
			if (u != v) {
				perVertexQueries[perVertexQueriesOffset[u]++] = q;
				perVertexQueries[perVertexQueriesOffset[v]++] = q;
			}
		}
		for (int v = n; v > 0; v--)
			perVertexQueriesOffset[v] = perVertexQueriesOffset[v - 1];
		perVertexQueriesOffset[0] = 0;

		IntFunction<IntIterator> vertexQueries = v -> new IntIterator() {
			int idx = perVertexQueriesOffset[v];
			int end = perVertexQueriesOffset[v + 1];

			@Override
			public boolean hasNext() {
				return idx < end;
			}

			@Override
			public int nextInt() {
				return perVertexQueries[idx++];
			}
		};

		UnionFind uf = UnionFind.newBuilder().expectedSize(n).build();
		int[] ufRoot = new int[n];
		for (int v = 0; v < n; v++) {
			uf.make();
			ufRoot[v] = v;
		}

		Bitmap mark = new Bitmap(queries.size());

		IntStack stack = new IntArrayList();
		IntStack parentEdgeStack = new IntArrayList();
		Stack<IEdgeIter> edgeIterStack = new ObjectArrayList<>();

		stack.push(root);
		parentEdgeStack.push(-1);
		edgeIterStack.push(tree.outEdges(root).iterator());

		dfs: for (;;) {
			int u = stack.topInt();
			int parentEdge = parentEdgeStack.topInt();
			for (IEdgeIter eit = edgeIterStack.top(); eit.hasNext();) {
				int e = eit.nextInt();
				if (e != parentEdge) {
					int v = eit.targetInt();
					stack.push(v);
					parentEdgeStack.push(e);
					edgeIterStack.push(tree.outEdges(v).iterator());
					continue dfs;
				}
			}

			for (IntIterator qit = vertexQueries.apply(u); qit.hasNext();) {
				int q = qit.nextInt();
				if (mark.get(q)) {
					if (u == queries.getQuerySourceInt(q)) {
						res[q] = ufRoot[uf.find(queries.getQueryTargetInt(q))];
					} else {
						assert u == queries.getQueryTargetInt(q);
						res[q] = ufRoot[uf.find(queries.getQuerySourceInt(q))];
					}

				} else {
					mark.set(q);
				}
			}

			stack.popInt();
			parentEdgeStack.popInt();
			edgeIterStack.pop();
			if (stack.isEmpty())
				break dfs;

			int parent = stack.topInt();
			ufRoot[uf.union(parent, u)] = parent;
		}
		return new LowestCommonAncestorOfflineUtils.ResultImpl(res);
	}
}
