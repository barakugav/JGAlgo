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

package com.jgalgo.alg.span;

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import java.util.Objects;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.ds.IntReferenceableHeap;
import com.jgalgo.internal.ds.ReferenceableHeap;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Fredman and Tarjan’s minimum spanning tree algorithm.
 *
 * <p>
 * The algorithm runs in iterations. In each iteration, multiple runs of the {@link MinimumSpanningTreePrim} algorithm
 * will be run (sequently) on the vertices: instead of growing the tree of Prim's algorithm until it connect all
 * vertices, we grow the heap that is used to order the out going edges until it reaches a certain size limit. Once the
 * heap reached the limit, we start Prim's algorithm from another vertex until the new heap reaches the limit, and
 * repeat that until we have a spanning forest. Than, we <i>contract</i> each tree to a single super vertex, and we
 * advance to the next iteration.
 *
 * <p>
 * The algorithm runs in \(O(m \log^* n)\) time and uses linear space. In practice, {@link MinimumSpanningTreePrim}
 * usually out-preform this algorithm. Note that only undirected graphs are supported.
 *
 * <p>
 * Based on "Fibonacci Heaps and Their Uses in Improved Network Optimization Algorithms" by M.L. Fredman and R.E.
 * Tarjan.
 *
 * @author Barak Ugav
 */
public class MinimumSpanningTreeFredmanTarjan extends MinimumSpanningTreeAbstract {

	private ReferenceableHeap.Builder heapBuilder = ReferenceableHeap.builder();

	/**
	 * Construct a new MST algorithm object.
	 *
	 * <p>
	 * Please prefer using {@link MinimumSpanningTree#newInstance()} to get a default implementation for the
	 * {@link MinimumSpanningTree} interface.
	 */
	public MinimumSpanningTreeFredmanTarjan() {}

	/**
	 * Set the implementation of the heap used by this algorithm.
	 *
	 * @param heapBuilder a builder for heaps used by this algorithm
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
	protected MinimumSpanningTree.IResult computeMinimumSpanningTree(IndexGraph g, IWeightFunction w) {
		Assertions.onlyUndirected(g);
		int n = g.vertices().size(), m = g.edges().size();
		if (n == 0)
			return MinimumSpanningTrees.IndexResult.Empty;

		// following variables are used to maintain the super vertices

		int[] V = new int[n]; // (vertex -> super vertex) of the current iteration
		int[] VNext = new int[n]; // (super vertex -> super vertex next) new labels for next iteration
		int ni = n; // number of super vertices in the current iteration

		// to be able to iterate over all edges of a super vertex, we maintain for each
		// super vertex a list of the vertices that were contracted to create it. The
		// list is stored as 3 arrays:
		int[] vListBegin = new int[n]; // (super vertex -> first vertex in the list) of the current iteration
		int[] vListEnd = new int[n]; // (super vertex -> last vertex in the list) of the current iteration
		int[] vListNext = new int[n]; // (vertex -> next vertex in the list)
		int[] vListBeginNext = new int[n]; // vListBegin of the next iteration
		int[] vListEndNext = new int[n]; // vListEnd of the next iteration

		// init each vertex to be its own super vertex
		for (int v : range(n)) {
			V[v] = vListBegin[v] = vListEnd[v] = v;
			vListNext[v] = -1;
		}

		// following variables are used for the Prim algorithm on the super vertices

		int[] vTree = new int[n]; // (super vertex -> tree index)
		int[] treeVertices = new int[n]; // stack of super vertices in current built tree

		// heap of edges going out of the current tree, one edge in per super vertex
		IntReferenceableHeap heap = (IntReferenceableHeap) heapBuilder.build(int.class, void.class, w);
		int heapSize = 0;
		// (super vertex -> heap element) for fast decreaseKey
		IntReferenceableHeap.Ref[] vHeapElm = new IntReferenceableHeap.Ref[n];

		IntArrayList mst = new IntArrayList(n - 1);
		for (int niNext;; ni = niNext) {
			int kExp = 2 * m / ni;
			int k = kExp < Integer.SIZE ? 1 << kExp : Integer.MAX_VALUE;
			niNext = 0;

			Arrays.fill(vTree, 0, ni, -1);
			for (int r : range(ni)) {
				if (vTree[r] >= 0)
					continue;

				int treeSize = 0;
				int connectedTree = -1;

				treeLoop: for (int U = r;;) {
					vTree[U] = r;
					treeVertices[treeSize++] = U;
					vHeapElm[U] = null;

					// decrease edges keys if a better one is found
					for (int u = vListBegin[U]; u >= 0; u = vListNext[u]) {
						// for each vertex in the super vertex, iterate over all edges
						for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
							int e = eit.nextInt();
							int v = V[eit.targetInt()];

							// edge from current tree to itself
							if (vTree[v] == r)
								continue;

							IntReferenceableHeap.Ref heapElm = vHeapElm[v];
							if (heapElm == null) {
								heapElm = vHeapElm[v] = heap.insert(e);
								heapSize++;
								if (heapSize > k)
									break treeLoop;
							} else if (w.compare(e, heapElm.key()) < 0)
								heap.decreaseKey(heapElm, e);
						}
					}

					// find next lightest edge
					int e, v, vt;
					for (;;) {
						if (heap.isEmpty())
							// reached all vertices from current root, continue to next tree
							break treeLoop;
						e = heap.extractMin().key();
						heapSize--;

						v = V[g.edgeSource(e)];
						if ((vt = vTree[v]) != r)
							break;
						v = V[g.edgeTarget(e)];
						if ((vt = vTree[v]) != r)
							break;
					}

					// add lightest edge to MST
					mst.add(e);
					U = v;
					if (vt >= 0) {
						// connected to an existing tree, take it's super vertex label for next iter
						connectedTree = VNext[vt];
						break;
					}
				}

				int newV; // super vertex label for next iteration for all vertices in tree
				int listEnd; // last vertex in super vertex vertices list
				if (connectedTree >= 0) {
					newV = connectedTree;
					listEnd = vListEndNext[connectedTree];
				} else {
					newV = niNext++;
					listEnd = -1;
				}

				// contract tree to super vertex, assign new label and concatenate vertices list
				for (int v; treeSize-- > 0; listEnd = vListEnd[v]) {
					v = treeVertices[treeSize];
					VNext[v] = newV;

					if (listEnd < 0) {
						// set new super vertex vertices list begin
						vListBeginNext[newV] = vListBegin[v];
					} else {
						// concatenate current vertex vertices list with the prev one
						vListNext[listEnd] = vListBegin[v];
					}
				}
				// set new super vertex vertices list end
				vListEndNext[newV] = listEnd;

				Arrays.fill(vHeapElm, 0, ni, null);
				heap.clear();
				heapSize = 0;
			}

			// assign new super vertices' vertices list
			int[] temp = vListBegin;
			vListBegin = vListBeginNext;
			vListBeginNext = temp;
			temp = vListEnd;
			vListEnd = vListEndNext;
			vListEndNext = temp;

			// If we failed to contract the graph, we are done
			if (ni == niNext)
				break;

			// update super vertex labels for all vertices
			for (int v : range(n))
				V[v] = VNext[V[v]];

		}

		IntSet mstSet = ImmutableIntArraySet.withNaiveContains(mst.elements(), 0, mst.size());
		return newIndexResult(mstSet);
	}

}
