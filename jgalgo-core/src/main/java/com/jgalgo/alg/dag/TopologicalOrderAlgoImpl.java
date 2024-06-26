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

package com.jgalgo.alg.dag;

import static com.jgalgo.internal.util.Range.range;
import java.util.Optional;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Assertions;

/**
 * A simple algorithm that compute a topological order in a DAG graph.
 *
 * <p>
 * The algorithm perform iterations while maintaining the in-degree of each vertex. At each iteration, a vertex \(u\)
 * with in-degree zero is added as the next vertex in the result topological order, and the vertex is 'removed' from the
 * graph conceptually, practically the algorithm decrease the in-degree of each vertex reachable by one of \(u\)'s out
 * going edges. If there is no vertex with zero in-degree before all vertices were added to the topological sort, there
 * is a cycle and no topological order exists.
 *
 * <p>
 * The algorithm is linear in both space and running time.
 *
 * @author Barak Ugav
 */
public class TopologicalOrderAlgoImpl extends TopologicalOrderAlgoAbstract {

	/**
	 * Create a new topological order algorithm.
	 *
	 * <p>
	 * Please prefer using {@link TopologicalOrderAlgo#newInstance()} to get a default implementation for the
	 * {@link TopologicalOrderAlgo} interface.
	 */
	public TopologicalOrderAlgoImpl() {}

	@Override
	protected Optional<TopologicalOrderAlgo.IResult> computeTopologicalSortingIfExist(IndexGraph g) {
		Assertions.onlyDirected(g);
		int n = g.vertices().size();
		int[] inDegree = new int[n];
		// Same array is used both as a queue and as the result
		int[] topolSort = new int[n];
		int beginIdx = 0, endIdx = 0;

		// calc in-degree of all vertices
		// Find vertices with zero in-degree and insert them to the queue
		for (int v : range(n))
			if ((inDegree[v] = g.inEdges(v).size()) == 0)
				topolSort[endIdx++] = v;

		// Poll vertices from the queue and "remove" each one from the graph and add the new
		// zero in-degree vertices to the queue
		while (beginIdx < endIdx) {
			int u = topolSort[beginIdx++];
			for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				eit.nextInt();
				int v = eit.targetInt();
				if (--inDegree[v] == 0)
					topolSort[endIdx++] = v;
			}
		}

		return beginIdx == n ? Optional.of(new IndexResult(topolSort)) : Optional.empty();
	}

}
