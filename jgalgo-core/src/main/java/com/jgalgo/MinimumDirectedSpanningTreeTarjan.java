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
import java.util.BitSet;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.ds.HeapReferenceable;
import com.jgalgo.internal.ds.UnionFindValue;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntStack;

/**
 * Tarjan's minimum directed spanning tree algorithm.
 * <p>
 * The algorithm run in \(O(m \log n)\) time and uses linear space.
 * <p>
 * Based on 'Finding optimum branchings' by R. E. Tarjan.
 *
 * @author Barak Ugav
 */
class MinimumDirectedSpanningTreeTarjan extends MinimumSpanningTreeUtils.AbstractDirected {

	private HeapReferenceable.Builder<Integer, Void> heapBuilder =
			HeapReferenceable.newBuilder().keysTypePrimitive(int.class).valuesTypeVoid();
	private final ConnectedComponentsAlgo ccAlg = ConnectedComponentsAlgo.newBuilder().build();
	private static final double HeavyEdgeWeight = Double.MAX_VALUE;

	/**
	 * Construct a new MDST algorithm object.
	 */
	MinimumDirectedSpanningTreeTarjan() {}

	/**
	 * Set the implementation of the heap used by this algorithm.
	 *
	 * @param heapBuilder a builder for heaps used by this algorithm
	 */
	void setHeapBuilder(HeapReferenceable.Builder<?, ?> heapBuilder) {
		this.heapBuilder = heapBuilder.keysTypePrimitive(int.class).valuesTypeVoid();
	}

	MinimumSpanningTree.Result computeMinimumSpanningTree(IndexGraph g, WeightFunction w) {
		Assertions.Graphs.onlyDirected(g);
		if (g.vertices().size() == 0 || g.edges().size() == 0)
			return MinimumSpanningTreeUtils.ResultImpl.Empty;
		IndexGraph g0 = g.copy(); // we must copy because we add new vertices and edges
		final int artificialEdgesThreshold = g0.edges().size();

		// Connect new root to all vertices
		int n = g0.vertices().size(), r = g0.addVertex();
		for (int v = 0; v < n; v++) {
			int e = g0.addEdge(r, v);
			assert e >= artificialEdgesThreshold;
		}

		// Calc MST on new graph
		ContractedGraph contractedGraph = contract(g0, w, artificialEdgesThreshold);
		return expand(g0, contractedGraph, r, artificialEdgesThreshold);
	}

	@Override
	MinimumSpanningTree.Result computeMinimumDirectedSpanningTree(IndexGraph g, WeightFunction w, int root) {
		Assertions.Graphs.onlyDirected(g);
		if (g.vertices().size() == 0 || g.edges().size() == 0)
			return MinimumSpanningTreeUtils.ResultImpl.Empty;
		IndexGraph g0 = g.copy(); // we must copy because we add new vertices and edges
		final int artificialEdgesThreshold = g0.edges().size();

		// TODO in case the original graph is already strongly connected, there is no need to copy

		ContractedGraph contractedGraph = contract(g0, w, artificialEdgesThreshold);
		return expand(g0, contractedGraph, root, artificialEdgesThreshold);
	}

	private static MinimumSpanningTree.Result expand(IndexGraph g, ContractedGraph cg, int root,
			int artificialEdgesThreshold) {
		int[] inEdge = new int[cg.n];

		IntStack roots = new IntArrayList();
		roots.push(root);

		while (!roots.isEmpty()) {
			int r = roots.popInt();
			int e = cg.inEdge[r];
			int v = g.edgeTarget(e);
			inEdge[v] = e;

			int upTo = r != root ? cg.parent[r] : -1;
			for (int prevChild = -1; v != upTo; v = cg.parent[prevChild = v]) {
				int child = cg.child[v];
				if (child == -1)
					continue;
				for (int c = child;;) {
					if (c != prevChild)
						roots.push(c);
					c = cg.brother[c];
					if (c == -1 || c == child)
						break;
				}
			}
		}

		IntCollection mst = new IntArrayList(cg.n - 1);
		for (int v = 0; v < cg.n; v++) {
			int e = inEdge[v];
			if (v != root && e < artificialEdgesThreshold)
				mst.add(e);
		}
		return new MinimumSpanningTreeUtils.ResultImpl(mst);
	}

	private void addEdgesUntilStronglyConnected(IndexGraph g, int artificialEdgesThreshold) {
		ConnectedComponentsAlgo.Result connectivityRes = ccAlg.computeConnectivityComponents(g);
		int N = connectivityRes.getNumberOfCcs();
		if (N <= 1)
			return;

		int[] V2v = new int[N];
		Arrays.fill(V2v, -1);
		int n = g.vertices().size();
		for (int v = 0; v < n; v++) {
			int V = connectivityRes.getVertexCc(v);
			if (V2v[V] == -1)
				V2v[V] = v;
		}

		ccLoop: for (int V = 0; V < N; V++) {
			int vNext = V < N - 1 ? V + 1 : 0;
			for (EdgeIter eit = g.outEdges(V2v[V]).iterator(); eit.hasNext();) {
				eit.nextInt();
				if (connectivityRes.getVertexCc(eit.target()) == vNext)
					continue ccLoop; /* V already connected to vNext */
			}
			int e = g.addEdge(V2v[V], V2v[vNext]);
			assert e >= artificialEdgesThreshold;
		}
	}

	private ContractedGraph contract(IndexGraph g, WeightFunction wOrig, int artificialEdgesThreshold) {
		addEdgesUntilStronglyConnected(g, artificialEdgesThreshold);

		int n = g.vertices().size();
		int VMaxNum = n * 2; // max super vertex number

		UnionFindValue uf = UnionFindValue.newBuilder().expectedSize(n).build();
		int[] ufIdxToV = new int[VMaxNum];
		for (int v = 0; v < n; v++)
			ufIdxToV[uf.make()] = v;

		WeightFunction w =
				e -> (e < artificialEdgesThreshold ? wOrig.weight(e) : HeavyEdgeWeight) + uf.getValue(g.edgeTarget(e));
		@SuppressWarnings("unchecked")
		HeapReferenceable<Integer, Void>[] heap = new HeapReferenceable[VMaxNum];
		for (int v = 0; v < n; v++)
			heap[v] = heapBuilder.build(w);
		for (int v = 0; v < n; v++)
			for (int e : g.inEdges(v))
				heap[v].insert(Integer.valueOf(e));

		int[] parent = new int[VMaxNum];
		int[] child = new int[VMaxNum];
		int[] brother = new int[VMaxNum];
		Arrays.fill(parent, -1);
		Arrays.fill(child, -1);
		Arrays.fill(brother, -1);
		int[] inEdge = new int[VMaxNum];

		BitSet onPath = new BitSet(VMaxNum);
		final int startVertex = 0;
		onPath.set(startVertex);

		for (int a = startVertex;;) {
			// Find minimum edge that enters a
			int u, e;
			do {
				// Assuming the graph is strongly connected, if heap is empty we are done
				if (heap[a].isEmpty())
					return new ContractedGraph(n, parent, child, brother, inEdge);
				e = heap[a].extractMin().key().intValue();
				u = ufIdxToV[uf.find(g.edgeSource(e))];
			} while (a == u);

			// Store the minimum edge entering a
			inEdge[a] = e;

			// Subtract w(e) from the weights of all edges entering a
			uf.addValue(a, -w.weight(e));

			if (!onPath.get(u)) {
				// Extend list
				brother[u] = a;
				onPath.set(u);
				a = u;
			} else {
				// Create new super vertex
				int c = uf.make();
				HeapReferenceable<Integer, Void> cHeap = heap[c] = heapBuilder.build(w);
				brother[c] = brother[u];
				brother[u] = a;
				child[c] = a;

				// Contract all children
				do {
					parent[a] = c;
					uf.union(c, a);
					onPath.clear(a);
					cHeap.meld(heap[a]);
					heap[a] = null;
					a = brother[a];
				} while (parent[a] == -1);

				ufIdxToV[uf.find(c)] = c;

				onPath.set(c);
				a = c;
			}
		}
	}

	private static class ContractedGraph {

		final int n;
		final int[] parent;
		final int[] child;
		final int[] brother;
		final int[] inEdge;

		ContractedGraph(int n, int[] parent, int[] child, int[] brother, int[] inEdge) {
			this.n = n;
			this.parent = parent;
			this.child = child;
			this.brother = brother;
			this.inEdge = inEdge;
		}

	}

}
