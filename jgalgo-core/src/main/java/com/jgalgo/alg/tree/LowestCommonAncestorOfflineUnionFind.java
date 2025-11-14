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

/**
 * Offline LCA algorithm based on Union-Find data structure.
 *
 * <p>
 * Given a tree and a set of pairs of vertices, the algorithm computes the lowest common ancestor of each pair. It
 * traverse the graph by recursively on each sub tree, from the bottom up. For a node \(u\) currently processed, it
 * calls the recursive function on each of its child \(v\) sequentially, and union \(u\) with \(v\), until all the
 * subtree of \(u\) is a single set in the union-find data structure. Then, for each query \((u, w)\), if \(w\) was
 * already processed, the LCA is the root of the set containing \(w\).
 *
 * <p>
 * The algorithm uses linear space and runs in \(O(n + q) \alpha (n + q)\) time, where \(n\) is the number of vertices
 * in the tree, \(q\) is the number of queries, and \(\alpha\) is the inverse Ackermann function.
 *
 * <p>
 * Based on 'Applications of Path Compression on Balanced Trees' by Tarjan (1979).
 *
 * @author Barak Ugav
 */
public class LowestCommonAncestorOfflineUnionFind extends LowestCommonAncestorOfflineAbstract {

	/**
	 * Create a new offline LCA algorithm object.
	 *
	 * <p>
	 * Please prefer using {@link LowestCommonAncestorOffline#newInstance()} to get a default implementation for the
	 * {@link LowestCommonAncestorOffline} interface.
	 */
	public LowestCommonAncestorOfflineUnionFind() {}

	@Override
	protected LowestCommonAncestorOffline.IResult findLowestCommonAncestors(IndexGraph tree, int root,
			LowestCommonAncestorOffline.IQueries queries) {
		final int n = tree.vertices().size();
		int[] res = new int[queries.size()];

		/* Sort the queries by vertex using a linear bucket sort */
		int[] queriesNum = new int[n];
		int queriesNumCount = 0;
		for (int q : range(queries.size())) {
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
		for (int v : range(1, n))
			perVertexQueriesOffset[v] = perVertexQueriesOffset[v - 1] + queriesNum[v - 1];
		for (int q : range(queries.size())) {
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

		UnionFind uf = UnionFind.newInstance();
		uf.makeMany(n);
		int[] ufRoot = range(n).toIntArray();

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
		return new IndexResult(res);
	}
}
