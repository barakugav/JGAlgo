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

package com.jgalgo.alg.path;

import com.jgalgo.alg.traversal.BfsIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Assertions;

/**
 * Single Source Shortest Path for cardinality weight function.
 *
 * <p>
 * The cardinality length of a path is the number of edges in it. The cardinality shortest path from a source vertex to
 * some other vertex is the path with the minimum number of edges. A simple BFS is performed from the source vertex
 * until all vertices that can be reached are reached. The algorithm runs in linear time.
 *
 * @see    BfsIter
 * @author Barak Ugav
 */
public class ShortestPathSingleSourceCardinality extends ShortestPathSingleSourceAbstract {

	/**
	 * Create a cardinality BFS-based SSSP algorithm.
	 *
	 * <p>
	 * Please prefer using {@link ShortestPathSingleSource#newInstance()} to get a default implementation for the
	 * {@link ShortestPathSingleSource} interface, or {@link ShortestPathSingleSource#builder()} for more customization
	 * options.
	 */
	public ShortestPathSingleSourceCardinality() {}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the weight function {@code w} is not {@code null} or
	 *                                      {@link IWeightFunction#CardinalityWeightFunction}
	 */
	@Override
	protected ShortestPathSingleSource.IResult computeShortestPaths(IndexGraph g, IWeightFunction w, int source) {
		Assertions.onlyCardinality(w);
		IndexResult res = new IndexResult(g, source);
		for (BfsIter.Int it = BfsIter.newInstance(g, source); it.hasNext();) {
			int v = it.nextInt();
			res.distances[v] = it.layer();
			res.backtrack[v] = it.lastEdgeInt();
		}
		return res;
	}

}
