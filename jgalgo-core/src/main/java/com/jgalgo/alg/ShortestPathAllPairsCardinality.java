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

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.internal.JGAlgoConfigImpl;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * All pairs cardinality shortest path algorithm.
 * <p>
 * The cardinality length of a path is the number of edges in it. The cardinality shortest path from a source vertex to
 * some other vertex is the path with the minimum number of edges. This algorithm compute the cardinality shortest path
 * between each pair of vertices in a graph. The algorithm simple perform {@link ShortestPathSingleSourceCardinality}
 * \(n\) times.
 * <p>
 * This algorithm runs in \(O(n(n+m))\).
 *
 * @see    ShortestPathSingleSourceCardinality
 * @author Barak Ugav
 */
class ShortestPathAllPairsCardinality extends ShortestPathAllPairsUtils.AbstractImpl {

	private boolean parallel = JGAlgoConfigImpl.ParallelByDefault;
	private static final int PARALLEL_VERTICES_THRESHOLD = 32;

	/**
	 * Create a new APSP algorithm object.
	 */
	ShortestPathAllPairsCardinality() {}

	@Override
	ShortestPathAllPairs.IResult computeAllCardinalityShortestPaths(IndexGraph g) {
		return computeSubsetCardinalityShortestPaths(g, g.vertices(), true);
	}

	@Override
	ShortestPathAllPairs.IResult computeSubsetCardinalityShortestPaths(IndexGraph g, IntCollection verticesSubset) {
		return computeSubsetCardinalityShortestPaths(g, verticesSubset, false);
	}

	ShortestPathAllPairs.IResult computeSubsetCardinalityShortestPaths(IndexGraph g, IntCollection verticesSubset,
			boolean allVertices) {
		final int verticesSubsetSize = verticesSubset.size();
		final ShortestPathSingleSource.IResult[] ssspResults = new ShortestPathSingleSource.IResult[verticesSubsetSize];
		int[] vToResIdx = ShortestPathAllPairsUtils.vToResIdx(g, allVertices ? null : verticesSubset);

		ForkJoinPool pool = JGAlgoUtils.getPool();
		if (verticesSubsetSize < PARALLEL_VERTICES_THRESHOLD || !parallel || pool.getParallelism() <= 1) {
			/* sequential */
			ShortestPathSingleSource sssp = ShortestPathSingleSource.newBuilder().setCardinality(true).build();
			for (int source : verticesSubset)
				ssspResults[vToResIdx[source]] =
						(ShortestPathSingleSource.IResult) sssp.computeCardinalityShortestPaths(g, source);

		} else {
			/* parallel */
			List<RecursiveAction> tasks = new ObjectArrayList<>(verticesSubsetSize);
			ThreadLocal<ShortestPathSingleSource> sssp =
					ThreadLocal.withInitial(() -> ShortestPathSingleSource.newBuilder().setCardinality(true).build());
			for (int source : verticesSubset) {
				final int source0 = source;
				tasks.add(JGAlgoUtils.recursiveAction(() -> ssspResults[vToResIdx[source0]] =
						(ShortestPathSingleSource.IResult) sssp.get().computeCardinalityShortestPaths(g, source0)));
			}
			for (RecursiveAction task : tasks)
				pool.execute(task);
			for (RecursiveAction task : tasks)
				task.join();
		}

		if (allVertices) {
			return new ShortestPathAllPairsUtils.ResFromSSSP.AllVertices(ssspResults);
		} else {
			return new ShortestPathAllPairsUtils.ResFromSSSP.VerticesSubset(ssspResults, vToResIdx);
		}
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the weight function {@code w} is not {@code null} or
	 *                                      {@link IWeightFunction#CardinalityWeightFunction}
	 */
	@Override
	ShortestPathAllPairs.IResult computeAllShortestPaths(IndexGraph g, IWeightFunction w) {
		if (!(w == null || w == IWeightFunction.CardinalityWeightFunction))
			throw new IllegalArgumentException("only cardinality shortest paths are supported");
		return computeAllCardinalityShortestPaths(g);
	}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the weight function {@code w} is not {@code null} or
	 *                                      {@link IWeightFunction#CardinalityWeightFunction}
	 */
	@Override
	ShortestPathAllPairs.IResult computeSubsetShortestPaths(IndexGraph g, IntCollection verticesSubset,
			IWeightFunction w) {
		if (!(w == null || w == IWeightFunction.CardinalityWeightFunction))
			throw new IllegalArgumentException("only cardinality shortest paths are supported");
		return computeSubsetCardinalityShortestPaths(g, verticesSubset);
	}

}
