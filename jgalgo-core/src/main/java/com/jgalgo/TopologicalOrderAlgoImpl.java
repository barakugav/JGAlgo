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

package com.jgalgo;

import java.util.Arrays;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.IndexGraph;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

class TopologicalOrderAlgoImpl extends TopologicalOrderAlgoAbstract {

	@Override
	TopologicalOrderAlgo.Result computeTopologicalSorting(IndexGraph g) {
		Assertions.Graphs.onlyDirected(g);
		int n = g.vertices().size();
		int[] inDegree = new int[n];
		IntPriorityQueue queue = new IntArrayFIFOQueue();
		int[] topolSort = new int[n];
		int topolSortSize = 0;

		// calc in degree of all vertices
		Arrays.fill(inDegree, 0, n, 0);
		for (int v = 0; v < n; v++)
			inDegree[v] = g.inEdges(v).size();

		// Find vertices with zero in degree and insert them to the queue
		assert queue.isEmpty();
		for (int v = 0; v < n; v++)
			if (inDegree[v] == 0)
				queue.enqueue(v);

		// Poll vertices from the queue and "remove" each one from the tree and add new
		// zero in degree vertices to the queue
		while (!queue.isEmpty()) {
			int u = queue.dequeueInt();
			topolSort[topolSortSize++] = u;
			for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				eit.nextInt();
				int v = eit.target();
				if (--inDegree[v] == 0)
					queue.enqueue(v);
			}
		}

		if (topolSortSize != n)
			throw new IllegalArgumentException("G is not a directed acyclic graph (DAG)");

		return () -> IntIterators.wrap(topolSort);
	}

}
