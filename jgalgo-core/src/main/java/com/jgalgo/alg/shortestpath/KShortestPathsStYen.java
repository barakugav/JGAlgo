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
package com.jgalgo.alg.shortestpath;

import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.ds.IndexHeapDouble;
import com.jgalgo.internal.util.Bitmap;
import it.unimi.dsi.fastutil.ints.IntList;

/**
 * Yen's algorithm for computing the K shortest paths between two vertices in a graph.
 *
 * <p>
 * This implementation is based on the paths compressed tree (the {@linkplain KShortestPathsStBasedPathsTree base
 * class}), which looks very different from the original algorithm, but it is indeed the same algorithm. This way of
 * maintaining the paths is more efficient than the original algorithm, and improvements such as Lawler's are not
 * necessary.
 *
 * <p>
 * The algorithms runs in \(O(nk(m+n \log n))\) time.
 *
 * @see    <a href="https://en.wikipedia.org/wiki/Yen%27s_algorithm">Wikipedia</a>
 * @author Barak Ugav
 */
public class KShortestPathsStYen extends KShortestPathsStBasedPathsTree {

	/**
	 * Create a new instance of the algorithm.
	 *
	 * <p>
	 * Please prefer using {@link KShortestPathsSt#newInstance()} to get a default implementation for the
	 * {@link KShortestPathsSt} interface.
	 */
	public KShortestPathsStYen() {}

	@Override
	protected KShortestPathsStBasedPathsTree.ShortestPathSubroutine newShortestPathSubroutine(IndexGraph g,
			IWeightFunction w, int target, Bitmap edgesMask) {
		return ShortestPathSubroutine.newInstance(g, w, target, edgesMask);
	}

	private static class ShortestPathSubroutine extends KShortestPathsStBasedPathsTree.ShortestPathSubroutine {

		ShortestPathSubroutine(IndexGraph g, IWeightFunction w, int target, Bitmap edgesMask, IndexHeapDouble heapS,
				IndexHeapDouble heapT) {
			super(g, w, target, edgesMask, heapS, heapT);
		}

		static ShortestPathSubroutine newInstance(IndexGraph g, IWeightFunction w, int target, Bitmap edgesMask) {
			final int n = g.vertices().size();
			IndexHeapDouble heapS = IndexHeapDouble.newInstance(n);
			IndexHeapDouble heapT = IndexHeapDouble.newInstance(n);
			return new ShortestPathSubroutine(g, w, target, edgesMask, heapS, heapT);
		}

		@Override
		public FastReplacementAlgoResult computeBestDeviationPath(int source, IntList prevSp, int maxDeviationPoint) {
			/*
			 * Always fall back to computing a deviation path from all possible deviation points. the base class fall
			 * back algorithm is actually Yen's algorithm.
			 */
			return FastReplacementAlgoResult.ofFailure();
		}
	}

}
