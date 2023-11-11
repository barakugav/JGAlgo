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
package com.jgalgo.internal.util;

import java.util.Iterator;
import java.util.Random;
import java.util.Set;
import com.jgalgo.alg.BipartiteGraphs;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.GraphFactory;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IntGraphFactory;
import com.jgalgo.graph.Weights;
import com.jgalgo.graph.WeightsBool;
import com.jgalgo.graph.WeightsInt;
import com.jgalgo.internal.ds.UnionFind;
import it.unimi.dsi.fastutil.booleans.Boolean2ObjectFunction;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

public class RandomGraphBuilder {

	private final Random rand;
	private int n;
	private int sn;
	private int tn;
	private int m;
	private boolean bipartite;
	private boolean directed;
	private boolean parallelEdges;
	private boolean selfEdges;
	private boolean cycles;
	private boolean connected;
	private Boolean2ObjectFunction<Graph<Integer, Integer>> impl;

	public RandomGraphBuilder(long seed) {
		rand = new Random(seed);
		n = sn = tn = m = 0;
		bipartite = false;
		parallelEdges = false;
		selfEdges = false;
		cycles = false;
		connected = false;

		graphImpl(rand.nextBoolean());
	}

	public RandomGraphBuilder n(int n) {
		this.n = n;
		return this;
	}

	public RandomGraphBuilder sn(int sn) {
		this.sn = sn;
		return this;
	}

	public RandomGraphBuilder tn(int tn) {
		this.tn = tn;
		return this;
	}

	public RandomGraphBuilder m(int m) {
		this.m = m;
		return this;
	}

	public RandomGraphBuilder bipartite(boolean bipartite) {
		this.bipartite = bipartite;
		return this;
	}

	public RandomGraphBuilder directed(boolean directed) {
		this.directed = directed;
		return this;
	}

	public RandomGraphBuilder parallelEdges(boolean parallelEdges) {
		this.parallelEdges = parallelEdges;
		return this;
	}

	public RandomGraphBuilder selfEdges(boolean selfEdges) {
		this.selfEdges = selfEdges;
		return this;
	}

	public RandomGraphBuilder cycles(boolean cycles) {
		this.cycles = cycles;
		return this;
	}

	public RandomGraphBuilder connected(boolean connected) {
		this.connected = connected;
		return this;
	}

	public RandomGraphBuilder graphImpl(Boolean2ObjectFunction<Graph<Integer, Integer>> impl) {
		this.impl = impl;
		return this;
	}

	public RandomGraphBuilder graphImpl(boolean intGraph) {
		if (intGraph) {
			impl = direct -> IntGraphFactory.newUndirected().setDirected(direct).newGraph();
		} else {
			impl = direct -> GraphFactory.<Integer, Integer>newUndirected().setDirected(direct).newGraph();
		}
		return this;
	}

	public Graph<Integer, Integer> build() {
		IntList vertices = new IntArrayList();
		final Graph<Integer, Integer> g;
		WeightsBool<Integer> partition = null;
		if (!bipartite) {
			if (n < 0 || m < 0)
				throw new IllegalStateException();
			g = impl.get(directed);
			for (int i = 0; i < n; i++) {
				int v = i + 1;
				g.addVertex(Integer.valueOf(v));
				vertices.add(v);
			}
		} else {
			if (sn < 0 || tn < 0)
				throw new IllegalStateException();
			if ((sn == 0 || tn == 0) && m != 0)
				throw new IllegalStateException();
			n = sn + tn;
			g = impl.get(directed);
			for (int i = 0; i < n; i++) {
				int v = i + 1;
				g.addVertex(Integer.valueOf(v));
				vertices.add(v);
			}
			partition = g.addVerticesWeights(BipartiteGraphs.VertexBiPartitionWeightKey, boolean.class);

			Iterator<Integer> vit = vertices.iterator();
			for (int u = 0; u < sn; u++)
				partition.set(vit.next(), true);
			for (int u = 0; u < tn; u++)
				partition.set(vit.next(), false);
		}
		if (n == 0)
			return g;
		if (!directed && !cycles && m >= n)
			throw new IllegalArgumentException();
		if (!cycles && selfEdges)
			throw new IllegalArgumentException();
		if (!parallelEdges) {
			long limit;
			if (bipartite)
				limit = n <= 16 ? sn * tn : ((long) sn) * tn * 2 / 3;
			else
				limit = n <= 16 ? (n - 1) * n / 2 : ((long) n) * n / 3;
			if (m > limit)
				throw new IllegalArgumentException("too much edges for random sampling (limit=" + limit + ")");
		}

		Set<IntList> existingEdges = new ObjectOpenHashSet<>();
		UnionFind uf = UnionFind.newBuilder().expectedSize(n).build();
		WeightsInt<Integer> vertexToUf = Weights.createExternalVerticesWeights(g, int.class, Integer.valueOf(-1));
		for (Integer v : g.vertices()) {
			int ufIdx = uf.make();
			vertexToUf.set(v, ufIdx);
		}
		int componentsNum = n;
		WeightsBool<Integer> reachableFromRoot = Weights.createExternalVerticesWeights(g, boolean.class);
		reachableFromRoot.set(g.vertices().iterator().next(), true);
		int reachableFromRootCount = 1;
		IntPriorityQueue queue = new FIFOQueueIntNoReduce();

		int dagRoot = g.vertices().iterator().next().intValue();
		IntList dagOrder = new IntArrayList(g.vertices());
		dagOrder.rem(dagRoot);
		IntLists.shuffle(dagOrder, rand);
		dagOrder.add(0, dagRoot);
		Int2IntMap vToDagIdx = new Int2IntOpenHashMap(n);
		for (int i = 0; i < n; i++)
			vToDagIdx.put(dagOrder.getInt(i), i);

		while (true) {
			boolean done = true;
			if (g.edges().size() < m)
				done = false;
			if (connected) {
				if (!directed && componentsNum > 1)
					done = false;
				else if (directed && reachableFromRootCount < n)
					done = false;
			}
			if (done)
				break;

			int u, v;

			if (!bipartite) {
				u = Graphs.randVertex(g, rand).intValue();
				v = Graphs.randVertex(g, rand).intValue();
				if (directed && !cycles) {
					int uDagIdx = vToDagIdx.get(u);
					int vDagIdx = vToDagIdx.get(v);
					if (uDagIdx > vDagIdx) {
						int temp = u;
						u = v;
						v = temp;
					}
				}
			} else {
				do {
					u = Graphs.randVertex(g, rand).intValue();
					v = Graphs.randVertex(g, rand).intValue();
				} while (partition.get(Integer.valueOf(u)) == partition.get(Integer.valueOf(v)));
			}

			// avoid self edges
			if (!selfEdges && u == v)
				continue;

			// avoid double edges
			if (!parallelEdges) {
				int ut = u, vt = v;
				if (!directed && ut > vt) {
					int temp = ut;
					ut = vt;
					vt = temp;
				}
				if (!existingEdges.add(IntList.of(ut, vt)))
					continue;
			}

			// keep track of number of connected components
			if (!cycles || connected) {
				if (!directed) {
					int uComp = uf.find(vertexToUf.get(Integer.valueOf(u)));
					int vComp = uf.find(vertexToUf.get(Integer.valueOf(v)));

					// avoid cycles
					if (!cycles && uComp == vComp)
						continue;

					if (uComp != vComp)
						componentsNum--;
					uf.union(uComp, vComp);
				} else if (connected) {
					if (reachableFromRoot.get(Integer.valueOf(u)) && !reachableFromRoot.get(Integer.valueOf(v))) {
						reachableFromRoot.set(Integer.valueOf(v), true);
						reachableFromRootCount++;

						queue.enqueue(v);
						while (!queue.isEmpty()) {
							int p = queue.dequeueInt();

							for (EdgeIter<Integer, Integer> eit = g.outEdges(Integer.valueOf(p)).iterator(); eit
									.hasNext();) {
								eit.next();
								Integer pv = eit.target();
								if (reachableFromRoot.get(pv))
									continue;
								reachableFromRoot.set(pv, true);
								reachableFromRootCount++;
								queue.enqueue(pv.intValue());
							}
						}

					}
				}
			}
			g.addEdge(Integer.valueOf(u), Integer.valueOf(v), Integer.valueOf(g.edges().size() + 1));
		}
		return g;
	}

}
