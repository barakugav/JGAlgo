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
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.NoSuchVertexException;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Fastutil;
import com.jgalgo.internal.util.MemoryReuse;
import it.unimi.dsi.fastutil.ints.IntList;

class TopologicalOrderAlgoImpl extends TopologicalOrderAlgoAbstract {

	private final MemoryReuse.IntArr intDegreeAllocator = new MemoryReuse.IntArr();
	private final MemoryReuse.IntArr queueAllocator = new MemoryReuse.IntArr();

	@Override
	TopologicalOrderAlgo.IResult computeTopologicalSorting(IndexGraph g) {
		Assertions.onlyDirected(g);
		int n = g.vertices().size();
		int[] inDegree = intDegreeAllocator.alloc(n);
		int[] queue = queueAllocator.alloc(n);
		int queueSize = 0;
		int[] topolSort = new int[n];
		int topolSortSize = 0;

		// calc in degree of all vertices
		// Find vertices with zero in degree and insert them to the queue
		for (int v : range(n)) {
			inDegree[v] = g.inEdges(v).size();
			if (inDegree[v] == 0)
				queue[queueSize++] = v;
		}

		// Poll vertices from the queue and "remove" each one from the tree and add new
		// zero in degree vertices to the queue
		while (queueSize > 0) {
			int u = queue[--queueSize];
			topolSort[topolSortSize++] = u;
			for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				eit.nextInt();
				int v = eit.targetInt();
				if (--inDegree[v] == 0)
					queue[queueSize++] = v;
			}
		}

		if (topolSortSize != n)
			throw new IllegalArgumentException("G is not a directed acyclic graph (DAG)");

		return new Res(topolSort);
	}

	private static class Res implements TopologicalOrderAlgo.IResult {

		private final IntList orderedVertices;
		private int[] vertexOrderIndex;

		Res(int[] topolSort) {
			orderedVertices = Fastutil.list(topolSort);
		}

		@Override
		public IntList orderedVertices() {
			return orderedVertices;
		}

		@Override
		public int vertexOrderIndex(int vertex) {
			if (vertexOrderIndex == null) {
				vertexOrderIndex = new int[orderedVertices.size()];
				for (int i : range(orderedVertices.size()))
					vertexOrderIndex[orderedVertices.getInt(i)] = i;
			}
			if (!(0 <= vertex && vertex < vertexOrderIndex.length))
				throw NoSuchVertexException.ofIndex(vertex);
			return vertexOrderIndex[vertex];
		}

	}

}
