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
import java.util.Objects;
import java.util.Random;
import java.util.stream.IntStream;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A greedy algorithm for computing a minimum dominating set.
 *
 * <p>
 * The algorithm is randomized algorithm that adds vertices to the dominating set one by one. In each iteration, a
 * random vertex is sampled with probability proportional to the number of vertices it will dominate.
 *
 * @author Barak Ugav
 */
class DominatingSetAlgoGreedy implements DominatingSetAlgoBase, RandomizedAlgorithm {

	private final Random rand = new Random();

	@Override
	public void setSeed(long seed) {
		rand.setSeed(seed);
	}

	@Override
	public IntSet computeMinimumDominationSet(IndexGraph g, IWeightFunction weightFunc,
			EdgeDirection dominanceDirection) {
		Assertions.onlyCardinality(weightFunc);
		Objects.requireNonNull(dominanceDirection);
		final int n = g.vertices().size();
		if (n == 0)
			return IntSet.of();
		return new Worker(g).solve(dominanceDirection);
	}

	private class Worker {

		private final IndexGraph g;
		private final Bitmap dominated;
		private final int[] edges;
		private final int[] edgeToIdx;
		private int edgesNum;

		Worker(IndexGraph g) {
			this.g = g;
			final int n = g.vertices().size();
			boolean directed = g.isDirected();
			int[] visited = new int[n];
			Arrays.fill(visited, -1);
			IntArrayList edges0 = new IntArrayList();
			edgeToIdx = new int[g.edges().size()];
			Arrays.fill(edgeToIdx, -1);
			if (directed) {
				for (int u : range(n)) {
					for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						int v = eit.targetInt();
						if (u == v || visited[v] == u)
							continue;
						visited[v] = u;
						assert edgeToIdx[e] == -1;
						edgeToIdx[e] = edges0.size();
						edges0.add(e);
					}
				}
			} else {
				for (int u : range(n)) {
					for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
						int e = eit.nextInt();
						if (u == g.edgeSource(e))
							continue;
						int v = eit.targetInt();
						if (u == v || visited[v] == u)
							continue;
						visited[v] = u;
						assert edgeToIdx[e] == -1;
						edgeToIdx[e] = edges0.size();
						edges0.add(e);
					}
				}
			}
			edges = edges0.elements();
			edgesNum = edges0.size();
			dominated = new Bitmap(n);
		}

		IntSet solve(EdgeDirection dominanceDirection) {
			boolean directed = g.isDirected();
			final int n = g.vertices().size();
			Bitmap dominating = new Bitmap(n);

			while (edgesNum > 0) {
				int e = edges[rand.nextInt(edgesNum)];
				int u;
				if (!directed || dominanceDirection == EdgeDirection.All) {
					assert !dominated.get(g.edgeSource(e)) || !dominated.get(g.edgeTarget(e));
					u = rand.nextBoolean() ? g.edgeSource(e) : g.edgeTarget(e);
					if (directed) {
						for (IEdgeIter uEdges = g.outEdges(u).iterator(); uEdges.hasNext();) {
							uEdges.nextInt();
							int v = uEdges.targetInt();
							if (u == v || dominated.get(v))
								continue;
							dominated.set(v);
							removeInEdges2(v);
							removeOutEdges2(v);
						}
						for (IEdgeIter uEdges = g.inEdges(u).iterator(); uEdges.hasNext();) {
							uEdges.nextInt();
							int v = uEdges.sourceInt();
							if (u == v || dominated.get(v))
								continue;
							dominated.set(v);
							removeInEdges2(v);
							removeOutEdges2(v);
						}
						removeInEdges(u);
						removeOutEdges(u);

					} else {
						for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
							eit.nextInt();
							int v = eit.targetInt();
							if (u == v || dominated.get(v))
								continue;
							dominated.set(v);
							removeInEdges2(v);
						}
						removeOutEdges(u);
					}

				} else if (dominanceDirection == EdgeDirection.Out) {
					assert !dominated.get(g.edgeTarget(e));
					u = g.edgeSource(e);
					for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
						eit.nextInt();
						int v = eit.targetInt();
						if (u == v || dominated.get(v))
							continue;
						dominated.set(v);
						removeInEdges(v);
					}
					removeInEdges(u); /* no need to dominate u anymore */

				} else {
					assert dominanceDirection == EdgeDirection.In;
					assert !dominated.get(g.edgeSource(e));
					u = g.edgeTarget(e);
					for (IEdgeIter eit = g.inEdges(u).iterator(); eit.hasNext();) {
						eit.nextInt();
						int v = eit.sourceInt();
						if (u == v || dominated.get(v))
							continue;
						dominated.set(v);
						removeOutEdges(v);
					}
					removeOutEdges(u); /* no need to dominate u anymore */
				}
				dominating.set(u);
				dominated.set(u);
			}

			/* some vertices might not be dominated if they have no in/out edges */
			if (!directed || dominanceDirection == EdgeDirection.In) {
				assert range(n)
						.filter(u -> !dominated.get(u))
						.allMatch(u -> g
								.outEdges(u)
								.intStream()
								.filter(e -> g.edgeSource(e) != g.edgeTarget(e))
								.findAny()
								.isEmpty());

			} else if (dominanceDirection == EdgeDirection.Out) {
				assert range(n)
						.filter(u -> !dominated.get(u))
						.allMatch(u -> g
								.inEdges(u)
								.intStream()
								.filter(e -> g.edgeSource(e) != g.edgeTarget(e))
								.findAny()
								.isEmpty());

			} else {
				assert dominanceDirection == EdgeDirection.All;
				assert range(n)
						.filter(u -> !dominated.get(u))
						.allMatch(u -> IntStream
								.concat(g.outEdges(u).intStream(), g.inEdges(u).intStream())
								.filter(e -> g.edgeSource(e) != g.edgeTarget(e))
								.findAny()
								.isEmpty());
			}
			for (int u : range(n))
				if (!dominated.get(u))
					dominating.set(u);

			return ImmutableIntArraySet.ofBitmap(dominating);
		}

		private void removeOutEdges(int source) {
			for (IEdgeIter eit = g.outEdges(source).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int idx = edgeToIdx[e];
				if (idx != -1)
					removeEdge(e, idx);
			}
		}

		private void removeOutEdges2(int source) {
			for (IEdgeIter eit = g.outEdges(source).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				if (!dominated.get(eit.targetInt()))
					continue;
				int idx = edgeToIdx[e];
				if (idx != -1)
					removeEdge(e, idx);
			}
		}

		private void removeInEdges(int target) {
			for (IEdgeIter eit = g.inEdges(target).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int idx = edgeToIdx[e];
				if (idx != -1)
					removeEdge(e, idx);
			}
		}

		private void removeInEdges2(int target) {
			for (IEdgeIter eit = g.inEdges(target).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				if (!dominated.get(eit.sourceInt()))
					continue;
				int idx = edgeToIdx[e];
				if (idx != -1)
					removeEdge(e, idx);
			}
		}

		private void removeEdge(int edge, int edgeIdx) {
			int swappedEdge = edges[edgeIdx] = edges[edgesNum - 1];
			edges[edgesNum - 1] = -1;
			edgeToIdx[swappedEdge] = edgeIdx;
			edgeToIdx[edge] = -1;
			edgesNum--;
		}

	}

}
