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

import java.util.List;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.JGAlgoConfigImpl;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * All pairs cardinality shortest path algorithm.
 *
 * <p>
 * The cardinality length of a path is the number of edges in it. The cardinality shortest path from a source vertex to
 * some other vertex is the path with the minimum number of edges. This algorithm compute the cardinality shortest path
 * between each pair of vertices in a graph. The algorithm simple perform {@link ShortestPathSingleSourceCardinality}
 * \(n\) times.
 *
 * <p>
 * This algorithm runs in \(O(n(n+m))\).
 *
 * @see    ShortestPathSingleSourceCardinality
 * @author Barak Ugav
 */
public class ShortestPathAllPairsCardinality extends ShortestPathAllPairsAbstract {

	private boolean parallel = JGAlgoConfigImpl.ParallelByDefault;
	private static final int PARALLEL_VERTICES_THRESHOLD = 32;

	/**
	 * Create a APSP algorithm for cardinality weight function only.
	 *
	 * <p>
	 * Please prefer using {@link ShortestPathAllPairs#newInstance()} to get a default implementation for the
	 * {@link ShortestPathAllPairs} interface, or {@link ShortestPathAllPairs#builder()} for more customization options.
	 */
	public ShortestPathAllPairsCardinality() {}

	@Override
	protected ShortestPathAllPairs.IResult computeAllShortestPaths(IndexGraph g, IWeightFunction w) {
		Assertions.onlyCardinality(w);
		return computeSubsetCardinalityShortestPaths(g, g.vertices(), true);
	}

	@Override
	protected ShortestPathAllPairs.IResult computeSubsetShortestPaths(IndexGraph g, IntCollection verticesSubset,
			IWeightFunction w) {
		Assertions.onlyCardinality(w);
		return computeSubsetCardinalityShortestPaths(g, verticesSubset, false);
	}

	ShortestPathAllPairs.IResult computeSubsetCardinalityShortestPaths(IndexGraph g, IntCollection verticesSubset,
			boolean allVertices) {
		final int verticesSubsetSize = verticesSubset.size();
		final ShortestPathSingleSource.IResult[] ssspResults = new ShortestPathSingleSource.IResult[verticesSubsetSize];
		int[] vToSubsetIdx = ShortestPathAllPairsAbstract.indexVerticesSubset(g, allVertices ? null : verticesSubset);

		ForkJoinPool pool = JGAlgoUtils.getPool();
		if (verticesSubsetSize < PARALLEL_VERTICES_THRESHOLD || !parallel || pool.getParallelism() <= 1) {
			/* sequential */
			ShortestPathSingleSource sssp = ShortestPathSingleSource.builder().cardinality(true).build();
			for (int source : verticesSubset)
				ssspResults[vToSubsetIdx[source]] =
						(ShortestPathSingleSource.IResult) sssp.computeShortestPaths(g, null, Integer.valueOf(source));

		} else {
			/* parallel */
			List<RecursiveAction> tasks = new ObjectArrayList<>(verticesSubsetSize);
			ThreadLocal<ShortestPathSingleSource> sssp =
					ThreadLocal.withInitial(() -> ShortestPathSingleSource.builder().cardinality(true).build());
			for (int source : verticesSubset) {
				final int source0 = source;
				tasks
						.add(JGAlgoUtils
								.recursiveAction(() -> ssspResults[vToSubsetIdx[source0]] =
										(ShortestPathSingleSource.IResult) sssp
												.get()
												.computeShortestPaths(g, null, Integer.valueOf(source0))));
			}
			for (RecursiveAction task : tasks)
				pool.execute(task);
			for (RecursiveAction task : tasks)
				task.join();
		}

		if (allVertices) {
			return ShortestPathAllPairsAbstract.IndexResult.fromSsspResult(g, ssspResults);
		} else {
			return new ShortestPathAllPairsAbstract.IndexResultVerticesSubsetFromSssp(ssspResults, vToSubsetIdx);
		}
	}

}
