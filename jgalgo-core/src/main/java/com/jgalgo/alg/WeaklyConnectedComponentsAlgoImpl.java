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

import java.util.Arrays;
import java.util.BitSet;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.IndexGraph;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;

class WeaklyConnectedComponentsAlgoImpl extends ConnectedComponentsUtils.AbstractWeaklyConnectedComponentsAlgo {

	@Override
	VertexPartition findWeaklyConnectedComponents(IndexGraph g) {
		final boolean directed = g.isDirected();
		final int n = g.vertices().size();
		int[] comp = new int[n];
		Arrays.fill(comp, -1);
		int compNum = 0;

		IntStack stack = new IntArrayList();
		for (int root = 0; root < n; root++) {
			if (comp[root] != -1)
				continue;
			final int compIdx = compNum++;
			stack.push(root);
			comp[root] = compIdx;

			while (!stack.isEmpty()) {
				int u = stack.popInt();

				for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					int v = eit.target();
					if (comp[v] != -1) {
						assert comp[v] == compIdx;
						continue;
					}
					comp[v] = compIdx;
					stack.push(v);
				}

				if (directed) {
					for (EdgeIter eit = g.inEdges(u).iterator(); eit.hasNext();) {
						eit.nextInt();
						int v = eit.source();
						if (comp[v] != -1) {
							assert comp[v] == compIdx;
							continue;
						}
						comp[v] = compIdx;
						stack.push(v);
					}
				}
			}
		}
		return new VertexPartitions.ImplIndex(g, compNum, comp);
	}

	@Override
	boolean isWeaklyConnected(IndexGraph g) {
		final boolean directed = g.isDirected();
		final int n = g.vertices().size();
		BitSet visited = new BitSet(n);
		int visitedCount = 0;

		IntStack stack = new IntArrayList();
		int root = 0;
		stack.push(root);

		while (!stack.isEmpty()) {
			int u = stack.popInt();

			for (EdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				eit.nextInt();
				int v = eit.target();
				if (visited.get(v))
					continue;
				visited.set(v);
				visitedCount++;
				stack.push(v);
			}

			if (directed) {
				for (EdgeIter eit = g.inEdges(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					int v = eit.source();
					if (visited.get(v))
						continue;
					visited.set(v);
					visitedCount++;
					stack.push(v);
				}
			}
		}
		return visitedCount == n;
	}

}
