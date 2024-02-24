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
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.ds.IndexHeapDouble;
import com.jgalgo.internal.ds.IndexHeapInt;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Prim's minimum spanning tree algorithm.
 *
 * <p>
 * The algorithm maintain a tree and repeatedly adds the lightest edge that connect a vertex from tree to the reset of
 * the vertices. The algorithm is similar to Dijkstra shortest paths algorithm in its idea, and it also uses a heap that
 * is updated using {@code decreaseKey()}.
 *
 * <p>
 * The running time of Prim's algorithm is \(O(m + n \log n)\) and it uses linear space. It's running time is very good
 * it practice and can be used as a first choice for {@link MinimumSpanningTree} algorithm. Note that only undirected
 * graphs are supported.
 *
 * <p>
 * Based on "Shortest connection networks And some generalizations" by Prim, R. C. (1957).
 *
 * @see    <a href="https://en.wikipedia.org/wiki/Prim%27s_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
class MinimumSpanningTreePrim implements MinimumSpanningTreeBase {

	/**
	 * Construct a new MST algorithm object.
	 */
	MinimumSpanningTreePrim() {}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not undirected
	 */
	@Override
	public MinimumSpanningTree.IResult computeMinimumSpanningTree(IndexGraph g, IWeightFunction w) {
		Assertions.onlyUndirected(g);
		int n = g.vertices().size();
		if (n == 0)
			return MinimumSpanningTrees.IndexResult.Empty;
		if (WeightFunction.isInteger(w)) {
			return computeMSTInt(g, (IWeightFunctionInt) w);
		} else {
			return computeMSTDouble(g, w);
		}
	}

	private static MinimumSpanningTree.IResult computeMSTDouble(IndexGraph g, IWeightFunction w) {
		final int n = g.vertices().size();
		IndexHeapDouble heap = IndexHeapDouble.newInstance(n);
		int[] edgeToV = new int[n];
		Bitmap visited = new Bitmap(n);

		IntArrayList mst = new IntArrayList(n - 1);
		for (int root : range(n)) {
			if (visited.get(root))
				continue;

			treeLoop: for (int u = root;;) {
				visited.set(u);

				/* decrease edges keys if a better one is found */
				for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.targetInt();
					if (visited.get(v))
						continue;

					double ew = w.weight(e);
					if (!heap.isInserted(v)) {
						heap.insert(v, ew);
						edgeToV[v] = e;
					} else if (ew < heap.key(v)) {
						heap.decreaseKey(v, ew);
						edgeToV[v] = e;
					}
				}

				/* find next lightest edge */
				if (heap.isEmpty())
					/* reached all vertices from current root, continue to next tree */
					break treeLoop;
				int v = heap.extractMin();
				assert !visited.get(v);
				int e = edgeToV[v];

				/* add lightest edge to MST */
				mst.add(e);
				u = v;
			}
		}
		IntSet mstSet = ImmutableIntArraySet.withNaiveContains(mst.elements(), 0, mst.size());
		return new MinimumSpanningTrees.IndexResult(mstSet);
	}



	private static MinimumSpanningTree.IResult computeMSTInt(IndexGraph g, IWeightFunctionInt w) {
		final int n = g.vertices().size();
		IndexHeapInt heap = IndexHeapInt.newInstance(n);
		int[] edgeToV = new int[n];
		Bitmap visited = new Bitmap(n);

		IntArrayList mst = new IntArrayList(n - 1);
		for (int root : range(n)) {
			if (visited.get(root))
				continue;

			treeLoop: for (int u = root;;) {
				visited.set(u);

				/* decrease edges keys if a better one is found */
				for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.targetInt();
					if (visited.get(v))
						continue;

					int ew = w.weightInt(e);
					if (!heap.isInserted(v)) {
						heap.insert(v, ew);
						edgeToV[v] = e;
					} else if (ew < heap.key(v)) {
						heap.decreaseKey(v, ew);
						edgeToV[v] = e;
					}
				}

				/* find next lightest edge */
				if (heap.isEmpty())
					/* reached all vertices from current root, continue to next tree */
					break treeLoop;
				int v = heap.extractMin();
				assert !visited.get(v);
				int e = edgeToV[v];

				/* add lightest edge to MST */
				mst.add(e);
				u = v;
			}
		}
		IntSet mstSet = ImmutableIntArraySet.withNaiveContains(mst.elements(), 0, mst.size());
		return new MinimumSpanningTrees.IndexResult(mstSet);
	}

}
