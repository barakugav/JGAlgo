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
import java.util.Objects;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.ds.DoubleIntReferenceableHeap;
import com.jgalgo.internal.ds.IntIntReferenceableHeap;
import com.jgalgo.internal.ds.ReferenceableHeap;
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

	private ReferenceableHeap.Builder heapBuilder = ReferenceableHeap.builder();

	/**
	 * Construct a new MST algorithm object.
	 */
	MinimumSpanningTreePrim() {}

	/**
	 * Set the implementation of the heap used by this algorithm.
	 *
	 * @param heapBuilder a builder for heaps used by this algorithm //
	 */
	void setHeapBuilder(ReferenceableHeap.Builder heapBuilder) {
		this.heapBuilder = Objects.requireNonNull(heapBuilder);
	}

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

	private MinimumSpanningTree.IResult computeMSTDouble(IndexGraph g, IWeightFunction w) {
		final int n = g.vertices().size();
		DoubleIntReferenceableHeap heap = (DoubleIntReferenceableHeap) heapBuilder.build(double.class, int.class);
		DoubleIntReferenceableHeap.Ref[] verticesPtrs = new DoubleIntReferenceableHeap.Ref[n];
		Bitmap visited = new Bitmap(n);

		IntArrayList mst = new IntArrayList(n - 1);
		for (int r : range(n)) {
			if (visited.get(r))
				continue;

			treeLoop: for (int u = r;;) {
				visited.set(u);
				verticesPtrs[u] = null;

				/* decrease edges keys if a better one is found */
				for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.targetInt();
					if (visited.get(v))
						continue;

					double ew = w.weight(e);
					DoubleIntReferenceableHeap.Ref vPtr = verticesPtrs[v];
					if (vPtr == null)
						verticesPtrs[v] = heap.insert(ew, e);
					else if (ew < vPtr.key()) {
						heap.decreaseKey(vPtr, ew);
						vPtr.setValue(e);
					}
				}

				/* find next lightest edge */
				int e, v;
				for (;;) {
					if (heap.isEmpty())
						/* reached all vertices from current root, continue to next tree */
						break treeLoop;
					e = heap.extractMin().value();
					if (!visited.get(v = g.edgeSource(e)))
						break;
					if (!visited.get(v = g.edgeTarget(e)))
						break;
				}

				/* add lightest edge to MST */
				mst.add(e);
				u = v;
			}
		}
		// Help GC
		Arrays.fill(verticesPtrs, 0, n, null);
		IntSet mstSet = ImmutableIntArraySet.withNaiveContains(mst.elements(), 0, mst.size());
		return new MinimumSpanningTrees.IndexResult(mstSet);
	}

	private MinimumSpanningTree.IResult computeMSTInt(IndexGraph g, IWeightFunctionInt w) {
		final int n = g.vertices().size();
		IntIntReferenceableHeap heap = (IntIntReferenceableHeap) heapBuilder.build(int.class, int.class);
		IntIntReferenceableHeap.Ref[] verticesPtrs = new IntIntReferenceableHeap.Ref[n];
		Bitmap visited = new Bitmap(n);

		IntArrayList mst = new IntArrayList(n - 1);
		for (int r : range(n)) {
			if (visited.get(r))
				continue;

			treeLoop: for (int u = r;;) {
				visited.set(u);
				verticesPtrs[u] = null;

				/* decrease edges keys if a better one is found */
				for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.targetInt();
					if (visited.get(v))
						continue;

					int ew = w.weightInt(e);
					IntIntReferenceableHeap.Ref vPtr = verticesPtrs[v];
					if (vPtr == null)
						verticesPtrs[v] = heap.insert(ew, e);
					else if (ew < vPtr.key()) {
						heap.decreaseKey(vPtr, ew);
						vPtr.setValue(e);
					}
				}

				/* find next lightest edge */
				if (heap.isEmpty())
					/* reached all vertices from current root, continue to next tree */
					break treeLoop;
				int e = heap.extractMin().value();
				int v = g.edgeSource(e);
				if (visited.get(v)) {
					v = g.edgeTarget(e);
					assert !visited.get(v);
				}

				/* add lightest edge to MST */
				mst.add(e);
				u = v;
			}
		}
		// Help GC
		Arrays.fill(verticesPtrs, 0, n, null);
		IntSet mstSet = ImmutableIntArraySet.withNaiveContains(mst.elements(), 0, mst.size());
		return new MinimumSpanningTrees.IndexResult(mstSet);
	}

}
