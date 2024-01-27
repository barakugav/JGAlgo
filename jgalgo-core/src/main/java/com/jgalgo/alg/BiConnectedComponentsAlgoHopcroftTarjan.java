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
import java.util.List;
import java.util.function.Function;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Hopcroft-Tarjan algorithm for bi-connected components.
 *
 * <p>
 * The algorithm performs a DFS and look for edges from vertices with greater depth to vertices with lower depth,
 * indicating for to separates paths between them: one using the DFS tree, and the other using the edge. The algorithm
 * runs in linear time and space.
 *
 * <p>
 * Based on 'Algorithm 447: efficient algorithms for graph manipulation' by Hopcroft, J. and Tarjan, R. 1973.
 *
 * @author Barak Ugav
 */
class BiConnectedComponentsAlgoHopcroftTarjan implements BiConnectedComponentsAlgoBase {

	@Override
	public BiConnectedComponentsAlgo.IResult findBiConnectedComponents(IndexGraph g) {
		Assertions.onlyUndirected(g);

		final int n = g.vertices().size();
		int[] depths = new int[n];
		int[] edgePath = new int[n];
		int[] lowpoint = new int[n];
		IEdgeIter[] edgeIters = new IEdgeIter[n];
		// TODO DFS stack class

		IntArrayList edgeStack = new IntArrayList();

		Arrays.fill(depths, -1);

		var biccVerticesFromBiccEdgesState = new Object() {
			Bitmap visited = new Bitmap(n);
		};
		Function<int[], int[]> biccVerticesFromBiccEdges = biccsEdges -> {
			Bitmap visited = biccVerticesFromBiccEdgesState.visited;
			assert visited.isEmpty();
			int biccVerticesCount = 0;
			for (int e : biccsEdges)
				for (int w : new int[] { g.edgeSource(e), g.edgeTarget(e) })
					if (visited.set(w))
						biccVerticesCount++;
			for (int e : biccsEdges)
				for (int w : new int[] { g.edgeSource(e), g.edgeTarget(e) })
					visited.clear(w);
			int[] biccVertices = new int[biccVerticesCount];
			biccVerticesCount = 0;
			for (int e : biccsEdges)
				for (int w : new int[] { g.edgeSource(e), g.edgeTarget(e) })
					if (visited.set(w))
						biccVertices[biccVerticesCount++] = w;
			assert biccVertices.length == biccVerticesCount;
			for (int w : biccVertices)
				visited.clear(w);
			return biccVertices;
		};

		// Bitmap separatingVertex = new Bitmap(n);
		List<Pair<int[], int[]>> biccs = new ObjectArrayList<>();
		IntList biccEdgesTemp = new IntArrayList();

		for (int root : range(n)) {
			if (depths[root] >= 0)
				continue;
			int depth = 0;
			depths[root] = depth;
			lowpoint[depth] = depth;
			edgeIters[depth] = g.outEdges(root).iterator();

			int rootChildrenCount = 0;

			dfs: for (int u = root;;) {
				final int parent = depth == 0 ? -1 : g.edgeEndpoint(edgePath[depth - 1], u);

				for (IEdgeIter eit = edgeIters[depth]; eit.hasNext();) {
					int e = eit.nextInt();

					int v = eit.targetInt();
					final int vDepth = depths[v];
					if (vDepth < 0) { /* unvisited */
						edgeStack.push(e);
						edgePath[depth] = e;
						depth++;
						depths[v] = depth;
						lowpoint[depth] = depth;
						edgeIters[depth] = g.outEdges(v).iterator();
						u = v;
						continue dfs;

					} else if (v != parent && vDepth < depth) {
						edgeStack.push(e);
						lowpoint[depth] = Math.min(lowpoint[depth], vDepth);
					}
				}

				if (depth > 0) {
					depth--;
					if (lowpoint[depth + 1] >= depth) {
						// separatingVertex.set(parent);

						assert biccEdgesTemp.isEmpty();
						for (int lastEdge = edgePath[depth]; !edgeStack.isEmpty();) {
							int e = edgeStack.popInt();
							biccEdgesTemp.add(e);
							if (e == lastEdge)
								break;
						}
						assert !biccEdgesTemp.isEmpty();
						int[] biccEdges = biccEdgesTemp.toIntArray();
						int[] biccVertices = biccVerticesFromBiccEdges.apply(biccEdges);
						biccs.add(Pair.of(biccVertices, biccEdges));
						biccEdgesTemp.clear();
					} else {
						lowpoint[depth] = Math.min(lowpoint[depth], lowpoint[depth + 1]);
					}

					if (depth == 0)
						rootChildrenCount++;
					u = parent;

				} else {
					assert u == root;
					// if (rootChildrenCount > 1)
					// separatingVertex.set(root);

					if (rootChildrenCount == 0) {
						biccs.add(Pair.of(new int[] { root }, IntArrays.DEFAULT_EMPTY_ARRAY));

					} else if (!edgeStack.isEmpty()) {
						int[] biccEdges = edgeStack.toIntArray();
						int[] biccVertices = biccVerticesFromBiccEdges.apply(biccEdges);
						edgeStack.clear();
						biccs.add(Pair.of(biccVertices, biccEdges));

					}
					break;
				}
			}
		}

		return new BiConnectedComponentsAlgos.IndexResult(g, biccs);
	}

}
