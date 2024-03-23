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
import com.jgalgo.internal.util.Bitmap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntStack;

class WeaklyConnectedComponentsAlgoImpl extends ConnectedComponentsUtils.AbstractWeaklyConnectedComponentsAlgo {

	@Override
	IVertexPartition findWeaklyConnectedComponents(IndexGraph g) {
		final boolean directed = g.isDirected();
		final int n = g.vertices().size();
		int[] comp = new int[n];
		Arrays.fill(comp, -1);
		int compNum = 0;

		IntStack stack = new IntArrayList();
		for (int root : range(n)) {
			if (comp[root] >= 0)
				continue;
			final int compIdx = compNum++;
			stack.push(root);
			comp[root] = compIdx;

			while (!stack.isEmpty()) {
				int u = stack.popInt();

				for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					int v = eit.targetInt();
					if (comp[v] >= 0) {
						assert comp[v] == compIdx;
						continue;
					}
					comp[v] = compIdx;
					stack.push(v);
				}

				if (directed) {
					for (IEdgeIter eit = g.inEdges(u).iterator(); eit.hasNext();) {
						eit.nextInt();
						int v = eit.sourceInt();
						if (comp[v] >= 0) {
							assert comp[v] == compIdx;
							continue;
						}
						comp[v] = compIdx;
						stack.push(v);
					}
				}
			}
		}
		return IVertexPartition.fromArray(g, comp, compNum);
	}

	@Override
	boolean isWeaklyConnected(IndexGraph g) {
		final boolean directed = g.isDirected();
		final int n = g.vertices().size();
		Bitmap visited = new Bitmap(n);
		int visitedCount = 0;

		IntStack stack = new IntArrayList();
		int root = 0;
		stack.push(root);

		while (!stack.isEmpty()) {
			int u = stack.popInt();

			for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
				eit.nextInt();
				int v = eit.targetInt();
				if (visited.get(v))
					continue;
				visited.set(v);
				visitedCount++;
				stack.push(v);
			}

			if (directed) {
				for (IEdgeIter eit = g.inEdges(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					int v = eit.sourceInt();
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
