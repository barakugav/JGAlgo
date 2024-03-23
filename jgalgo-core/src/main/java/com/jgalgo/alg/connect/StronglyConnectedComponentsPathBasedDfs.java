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
import com.jgalgo.alg.IVertexPartition;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;

/**
 * Path based DFS implementation of Dijkstra's strongly connected components algorithm.
 *
 * <p>
 * The algorithm run in linear time and use linear space.
 *
 * <p>
 * Based on 'A Discipline of Programming' by Edsger W. Dijkstra, 1976.
 *
 * @author Barak Ugav
 */
class StronglyConnectedComponentsPathBasedDfs extends ConnectedComponentsUtils.AbstractStronglyConnectedComponentsAlgo {

	@Override
	IVertexPartition findStronglyConnectedComponentsDirected(IndexGraph g) {
		return findStronglyConnectedComponentsDirected(g, false);
	}

	private static IVertexPartition findStronglyConnectedComponentsDirected(IndexGraph g, boolean stopAfterOneBlock) {
		final int n = g.vertices().size();
		IntStack s = new IntArrayList();
		IntStack p = new IntArrayList();
		int[] dfsPath = new int[n];
		IEdgeIter[] edges = new IEdgeIter[n];
		// TODO DFS stack class

		int[] comp = new int[n];
		Arrays.fill(comp, -1);
		int compNum = 0;

		int[] c = new int[n];
		int cNext = 1;

		for (int root : range(n)) {
			if (comp[root] >= 0)
				continue;
			dfsPath[0] = root;
			edges[0] = g.outEdges(root).iterator();
			c[root] = cNext++;
			s.push(root);
			p.push(root);

			dfs: for (int depth = 0;;) {
				for (IEdgeIter eit = edges[depth]; eit.hasNext();) {
					eit.nextInt();
					int v = eit.targetInt();
					if (c[v] == 0) {
						c[v] = cNext++;
						s.push(v);
						p.push(v);

						dfsPath[++depth] = v;
						edges[depth] = g.outEdges(v).iterator();
						continue dfs;
					} else if (comp[v] < 0)
						while (c[p.topInt()] > c[v])
							p.popInt();
				}
				int u = dfsPath[depth];
				if (p.topInt() == u) {
					int v;
					do {
						v = s.popInt();
						comp[v] = compNum;
					} while (v != u);
					if (stopAfterOneBlock) {
						for (int w : range(n))
							if (comp[w] < 0)
								return null;
						return IVertexPartition.fromArray(g, comp, compNum);
					}
					compNum++;
					p.popInt();
				}

				edges[depth] = null;
				if (depth-- == 0)
					break;
			}
		}
		return IVertexPartition.fromArray(g, comp, compNum);
	}

	@Override
	boolean isStronglyConnected(IndexGraph g) {
		return findStronglyConnectedComponentsDirected(g, true) != null;
	}

}
