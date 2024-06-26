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
package com.jgalgo.alg.connect;

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import com.jgalgo.alg.common.IVertexPartition;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;

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
public class StronglyConnectedComponentsTarjan extends StronglyConnectedComponentsAlgoAbstract {

	/**
	 * Create a new instance of the algorithm.
	 *
	 * <p>
	 * Please prefer using {@link StronglyConnectedComponentsAlgo#newInstance()} to get a default implementation for the
	 * {@link StronglyConnectedComponentsAlgo} interface.
	 */
	public StronglyConnectedComponentsTarjan() {}

	@Override
	protected IVertexPartition findStronglyConnectedComponentsDirected(IndexGraph g) {
		return findStronglyConnectedComponentsDirected(g, false);
	}

	private static IVertexPartition findStronglyConnectedComponentsDirected(IndexGraph g, boolean stopAfterOneBlock) {
		final int n = g.vertices().size();

		int[] visitIdx = new int[n];
		int[] lowLink = new int[n];
		IntStack s = new IntArrayList();

		int compNum = 0;
		int[] comp = new int[n];
		Arrays.fill(comp, -1);

		IEdgeIter[] edges = new IEdgeIter[n];

		/* start a DFS from each root */
		int nextVisitIdx = 1;
		for (int root : range(n)) {
			if (visitIdx[root] != 0)
				continue;
			int depth = 0;
			visitIdx[root] = nextVisitIdx++;
			lowLink[root] = visitIdx[root]; /* smallest reachable visit index */
			s.push(root);
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
						s.push(v);
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
					for (final int c = compNum++;;) {
						int v = s.popInt();
						comp[v] = c;
						if (v == u)
							break;
					}
					if (stopAfterOneBlock) {
						if (Arrays.stream(comp).anyMatch(c -> c < 0))
							return null;
						return IVertexPartition.fromArray(g, comp, compNum);
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

		return IVertexPartition.fromArray(g, comp, compNum);
	}

	@Override
	protected boolean isStronglyConnectedDirected(IndexGraph g) {
		return findStronglyConnectedComponentsDirected(g, true) != null;
	}

}
