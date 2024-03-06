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
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.MemoryReuse;

/**
 * Tarjan's strongly connected components algorithm.
 *
 * <p>
 * The algorithm run in linear time and use linear space.
 *
 * <p>
 * Based on 'Depth-first search and linear graph algorithms' by Robert Tarjan, 1972.
 *
 * @author Barak Ugav
 */
class StronglyConnectedComponentsTarjan extends ConnectedComponentsUtils.AbstractStronglyConnectedComponentsAlgo {

	private final MemoryReuse.IntArr visitIdxAllocator = new MemoryReuse.IntArr();
	private final MemoryReuse.IntArr lowLinkAllocator = new MemoryReuse.IntArr();
	private final MemoryReuse.IntArr stackAllocator = new MemoryReuse.IntArr();
	private final MemoryReuse.ObjArr<IEdgeIter> edgesAllocator = new MemoryReuse.ObjArr<>(new IEdgeIter[0]);

	@Override
	IVertexPartition findStronglyConnectedComponentsDirected(IndexGraph g) {
		return findStronglyConnectedComponentsDirected(g, false);
	}

	private IVertexPartition findStronglyConnectedComponentsDirected(IndexGraph g, boolean stopAfterOneBlock) {
		final int n = g.vertices().size();

		int[] visitIdx = visitIdxAllocator.alloc(n, 0);
		int[] lowLink = lowLinkAllocator.alloc(n, 0);
		int[] stack = stackAllocator.alloc(n);
		int stackSize = 0;

		int compNum = 0;
		int[] comp = new int[n];
		Arrays.fill(comp, -1);

		IEdgeIter[] edges = edgesAllocator.alloc(n);

		/* start a DFS from each root */
		int nextVisitIdx = 1;
		for (int root : range(n)) {
			if (visitIdx[root] != 0)
				continue;
			int depth = 0;
			visitIdx[root] = nextVisitIdx++;
			lowLink[root] = visitIdx[root]; /* smallest reachable visit index */
			stack[stackSize++] = root;
			edges[depth] = g.outEdges(root).iterator();

			/* perform DFS from root */
			dfs: for (int u = root;;) {
				/* scan outgoing edges */
				for (IEdgeIter eit = edges[depth]; eit.hasNext();) {
					eit.nextInt();
					int v = eit.targetInt();
					if (visitIdx[v] == 0) { /* unvisited, tree edge */
						visitIdx[v] = nextVisitIdx++;
						lowLink[v] = visitIdx[v];
						stack[stackSize++] = v;
						edges[++depth] = g.outEdges(v).iterator();
						u = v;
						continue dfs;
					}
					if (comp[v] < 0) /* do not belong to any scc, still on the stack */
						if (lowLink[u] > visitIdx[v]) /* weird line */
							lowLink[u] = visitIdx[v];
				}

				/* u is a 'root' of a strongly connected component */
				if (lowLink[u] == visitIdx[u]) {
					int c = compNum++;
					for (;;) {
						int v = stack[--stackSize];
						comp[v] = c;
						if (v == u)
							break;
					}
					if (stopAfterOneBlock) {
						for (int w : range(n))
							if (comp[w] < 0)
								return null;
						return new VertexPartitions.Impl(g, compNum, comp);
					}
				}

				/* go one vertex up in the dfs stack */
				if (depth == 0)
					break;
				depth--;
				int child = u;
				u = edges[depth].sourceInt();
				if (lowLink[u] > lowLink[child])
					lowLink[u] = lowLink[child];
			}
		}

		return new VertexPartitions.Impl(g, compNum, comp);
	}

	@Override
	boolean isStronglyConnected(IndexGraph g) {
		return findStronglyConnectedComponentsDirected(g, true) != null;
	}

}
