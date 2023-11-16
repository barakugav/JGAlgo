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

import java.util.Iterator;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Schrage-Baker algorithm for enumerating all the closure subsets in a directed graph.
 *
 * <p>
 * Based on 'Dynamic Programming Solution of Sequencing Problems with Precedence Constraints' by Linus Schrage and
 * Kenneth R. Baker (1978).
 *
 * @author Barak Ugav
 */
class ClosuresEnumeratorSchrageBaker extends ClosuresEnumerators.AbstractImpl {

	private final StronglyConnectedComponentsAlgo sccAlgo = StronglyConnectedComponentsAlgo.newInstance();
	private final TopologicalOrderAlgo topoAlgo = TopologicalOrderAlgo.newInstance();

	@Override
	Iterator<IntSet> closuresIter(IndexGraph g) {
		Assertions.Graphs.onlyDirected(g);
		final int n = g.vertices().size();

		/* Build the condensation graph */
		IVertexPartition sccs = (IVertexPartition) sccAlgo.findStronglyConnectedComponents(g);
		final int sccNum = sccs.numberOfBlocks();
		IndexGraphBuilder sccGraph0 = IndexGraphBuilder.newDirected();
		sccGraph0.expectedVerticesNum(sccNum);
		for (int b = 0; b < sccNum; b++)
			sccGraph0.addVertex();
		Bitmap seenBlocks = new Bitmap(sccNum);
		IntList seenBlockList = new IntArrayList();
		for (int b1 = 0; b1 < sccNum; b1++) {
			for (int u : sccs.blockVertices(b1)) {
				for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					int b2 = sccs.vertexBlock(eit.targetInt());
					if (b1 == b2 || seenBlocks.get(b2))
						continue;
					seenBlocks.set(b2);
					seenBlockList.add(b2);
					sccGraph0.addEdge(b1, b2);
				}
			}
			seenBlocks.clearAllUnsafe(seenBlockList);
			seenBlockList.clear();
		}
		IndexGraph sccGraph = sccGraph0.build();

		/* Find all closures in the DAG condensation graph and map the sets to the original vertices */
		return JGAlgoUtils.iterMap(closuresIterDag(sccGraph), blkIter -> {
			Bitmap closure = new Bitmap(n);
			for (int blk : JGAlgoUtils.iterable(blkIter))
				for (int v : sccs.blockVertices(blk))
					closure.set(v);
			return ImmutableIntArraySet.ofBitmap(closure);
		});
	}

	private Iterator<IntIterator> closuresIterDag(IndexGraph g) {
		final int n = g.vertices().size();

		int[] topoIdxToV =
				((TopologicalOrderAlgo.IResult) topoAlgo.computeTopologicalSorting(g)).orderedVertices().toIntArray();
		IntArrays.reverse(topoIdxToV);
		int[] vToTopoIndex = new int[n];
		for (int topoIdx = 0; topoIdx < n; topoIdx++)
			vToTopoIndex[topoIdxToV[topoIdx]] = topoIdx;

		Bitmap m = new Bitmap(n);
		return new Iterator<>() {
			int nextClearBit = 0;

			@Override
			public boolean hasNext() {
				return nextClearBit < n;
			}

			@Override
			public IntIterator next() {
				/* Find the smallest positive integer j for which m(j)=0; call it i */
				int i = nextClearBit;

				/* (if m(j)=1 for j=1,...,n then all subsets have been enumerated) */
				Assertions.Iters.hasNext(this);

				/* Set m(i)=1 */
				m.set(i);

				/* For j=i-1 to 1 step -1 */
				cleanLoop: for (int j = i - 1; j >= 0; j--) {
					/* If m(j)=1 and j is in R(j) */
					/* m(j) is always 1, i is the smallest index for which m(i)=0 // if (!m.get(j)) continue; */
					for (IEdgeIter eit = g.inEdges(topoIdxToV[j]).iterator(); eit.hasNext();) {
						eit.nextInt();
						int predecessor = eit.sourceInt();
						assert j < vToTopoIndex[predecessor];
						if (m.get(vToTopoIndex[predecessor]))
							continue cleanLoop;
					}
					/* Let m(j)=0 */
					m.clear(j);
				}

				nextClearBit = m.nextClearBit(0);

				return JGAlgoUtils.iterMapInt(m.iterator(), topoIdx -> topoIdxToV[topoIdx]);
			}
		};
	}

}
