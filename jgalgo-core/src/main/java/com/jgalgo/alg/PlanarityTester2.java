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

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 *
 * @author Barak Ugav
 */
class PlanarityTester2 extends PlanarityTesters.AbstractImpl {

	@Override
	IPlanarEmbedding findPlanarEmbedding(IndexGraph g, boolean addAsEdgesWeights) {
		Assertions.Graphs.onlyUndirected(g);

		assert BiConnectedComponentsAlgo.newInstance().findBiConnectedComponents(g).getNumberOfBiCcs() == 1;

		final int n = g.vertices().size();
		final int m = g.edges().size();
		int[] dfsIndex = new int[n];
		Arrays.fill(dfsIndex, -1);
		int nextDfsIndex = 0;
		int[] vsSortedByDfsIndex = new int[n];
		int[] lowpoint = new int[n];

		int[] parent = new int[n];
		Arrays.fill(parent, -1);
		// int[] childrenNum = new int[n];

		// int[] firstInEdge = new int[n];
		// int[] nextInEdge = new int[m];
		// Arrays.fill(firstInEdge, -1);
		// Arrays.fill(nextInEdge, -1);
		IntList[] inEdges = new IntList[n];
		IntList[] outEdges = new IntList[n];
		for (int v = 0; v < n; v++) {
			inEdges[v] = new IntArrayList();
			outEdges[v] = new IntArrayList();
		}
		// TODO: increase of array lists, implement as linked lists in a single continues int array

		Stack<IEdgeIter> stack = new ObjectArrayList<>();
		final int root = 0;
		dfsIndex[root] = nextDfsIndex++;
		lowpoint[root] = dfsIndex[root];
		vsSortedByDfsIndex[dfsIndex[root]] = root;
		stack.push(g.outEdges(root).iterator());

		dfs: for (int u = root;;) {
			for (IEdgeIter eit = stack.top(); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.targetInt();

				if (dfsIndex[v] == 0) {
					/* a DFS tree edge */
					parent[v] = u;
					// childrenNum[u]++;
					dfsIndex[v] = nextDfsIndex++;
					lowpoint[v] = dfsIndex[v];
					vsSortedByDfsIndex[dfsIndex[v]] = v;
					stack.push(g.outEdges(v).iterator());
					u = v;
					continue dfs;

				} else if (dfsIndex[v] < dfsIndex[u]) {
					/* a back edge */
					if (lowpoint[u] > dfsIndex[v])
						lowpoint[u] = dfsIndex[v];
					/* add e to B_in(v) */
					inEdges[v].add(e);
					outEdges[u].add(e);
					// nextInEdge[e] = firstInEdge[v];
					// firstInEdge[v] = e;
				}
			}

			/* backtrack DFS one vertex up */
			stack.pop();
			if (stack.isEmpty())
				break;
			int v = u;
			u = parent[v];
			if (lowpoint[u] > lowpoint[v])
				lowpoint[u] = lowpoint[v];
		}
		assert nextDfsIndex == n;

		// TODO sort children by lowpoint in linear time
		// int[] verticesSortedByLowpoint = stableBucketSort(range(n).toIntArray(), v -> lowpoint[v], n - 1);
		IntList[] children = new IntList[n];
		for (int v = 0; v < n; v++)
			children[v] = new IntArrayList();
		for (int v = 0; v < n; v++)
			if (parent[v] != -1)
				children[parent[v]].add(v);
		for (int v = 0; v < n; v++)
			children[v].sort((v1, v2) -> Integer.compare(lowpoint[v1], lowpoint[v2]));

		int[] h = range(n).map(
				v -> outEdges[v].intStream().map(e -> dfsIndex[g.edgeEndpoint(e, v)]).min().orElse(Integer.MAX_VALUE))
				.toArray();

		for (int dfsIdx = n - 1; dfsIdx >= 0; dfsIdx--) {
			int v = vsSortedByDfsIndex[dfsIdx];

		}

		return null;
	}

	// private static int[] stableBucketSort(int[] elements, IntUnaryOperator key, int maxKey) {
	// int[] buckets = new int[maxKey + 1];
	// for (int e : elements)
	// buckets[key.applyAsInt(e)]++;
	// for (int offset = 0, b = 0;; b++) {
	// if (b == maxKey) {
	// assert offset == elements.length;
	// buckets[b] = elements.length;
	// break;
	// }
	// int bucketSize = buckets[b];
	// buckets[b] = offset;
	// offset += bucketSize;
	// }
	// int[] sorted = new int[elements.length];
	// for (int e : elements)
	// sorted[buckets[key.applyAsInt(e)]++] = e;
	// return sorted;
	// }

}
