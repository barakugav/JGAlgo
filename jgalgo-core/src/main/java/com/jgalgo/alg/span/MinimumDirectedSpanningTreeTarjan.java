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

package com.jgalgo.alg.span;

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import java.util.Objects;
import com.jgalgo.alg.common.IPath;
import com.jgalgo.alg.common.IVertexPartition;
import com.jgalgo.alg.connect.StronglyConnectedComponentsAlgo;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.ds.IntReferenceableHeap;
import com.jgalgo.internal.ds.ReferenceableHeap;
import com.jgalgo.internal.ds.UnionFindValue;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntStack;

/**
 * Tarjan's minimum directed spanning tree algorithm.
 *
 * <p>
 * The algorithm run in \(O(m \log n)\) time and uses linear space.
 *
 * <p>
 * Based on 'Finding optimum branchings' by R. E. Tarjan.
 *
 * @author Barak Ugav
 */
public class MinimumDirectedSpanningTreeTarjan extends MinimumDirectedSpanningTreeAbstract {

	private ReferenceableHeap.Builder heapBuilder = ReferenceableHeap.builder();
	private final StronglyConnectedComponentsAlgo sccAlg = StronglyConnectedComponentsAlgo.newInstance();

	/**
	 * Construct a new MDST algorithm object.
	 *
	 * <p>
	 * Please prefer using {@link MinimumDirectedSpanningTree#newInstance()} to get a default implementation for the
	 * {@link MinimumDirectedSpanningTree} interface.
	 */
	public MinimumDirectedSpanningTreeTarjan() {}

	/**
	 * Set the implementation of the heap used by this algorithm.
	 *
	 * @param heapBuilder a builder for heaps used by this algorithm
	 */
	void setHeapBuilder(ReferenceableHeap.Builder heapBuilder) {
		this.heapBuilder = Objects.requireNonNull(heapBuilder);
	}

	@Override
	protected MinimumSpanningTree.IResult computeMinimumDirectedSpanningTree(IndexGraph g, IWeightFunction w,
			int root) {
		Assertions.onlyDirected(g);
		if (g.vertices().size() == 0 || g.edges().size() == 0)
			return MinimumSpanningTrees.IndexResult.Empty;

		IntSet vertices = IPath.reachableVertices(g, root);
		if (vertices.size() == g.vertices().size()) {
			/* all vertices are reachable from the root */
			final int artificialEdgesThreshold = g.edges().size();
			IVertexPartition connectivityRes = (IVertexPartition) sccAlg.findStronglyConnectedComponents(g);
			if (connectivityRes.numberOfBlocks() > 1) {
				g = g.copy(); // we must copy because we add new edges
				addEdgesUntilStronglyConnected(g, root, connectivityRes, artificialEdgesThreshold);
			}
			ContractedGraph contractedGraph = contract(g, null, w, artificialEdgesThreshold);
			int[] mdstEdges = expand(g, contractedGraph, root, artificialEdgesThreshold);
			return newIndexResult(mdstEdges);

		} else {
			/* not all vertices are reachable from the root, operate on the subgraph of these vertices */
			IndexGraphBuilder builder = IndexGraphBuilder.directed();

			builder.ensureVertexCapacity(vertices.size());
			int[] vRefToOrig = vertices.toIntArray();
			int[] vOrigToRef = new int[g.vertices().size()];
			Arrays.fill(vOrigToRef, -1);
			for (int v : vRefToOrig)
				vOrigToRef[v] = builder.addVertexInt();
			root = vOrigToRef[root];

			int subGraphEdgesNum = 0;
			for (int e : range(g.edges().size()))
				if (vertices.contains(g.edgeSource(e)) && vertices.contains(g.edgeTarget(e)))
					subGraphEdgesNum++;
			int[] edgeRef = new int[subGraphEdgesNum];
			builder.ensureEdgeCapacity(subGraphEdgesNum);
			for (int e : range(g.edges().size())) {
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				if (u != v && vertices.contains(u) && vertices.contains(v))
					edgeRef[builder.addEdge(vOrigToRef[u], vOrigToRef[v])] = e;
			}

			g = builder.buildMutable();

			final int artificialEdgesThreshold = g.edges().size();
			IVertexPartition connectivityRes = (IVertexPartition) sccAlg.findStronglyConnectedComponents(g);
			if (connectivityRes.numberOfBlocks() > 1)
				addEdgesUntilStronglyConnected(g, root, connectivityRes, artificialEdgesThreshold);

			ContractedGraph contractedGraph = contract(g, edgeRef, w, artificialEdgesThreshold);
			int[] mdstEdges = expand(g, contractedGraph, root, artificialEdgesThreshold);
			for (int i : range(mdstEdges.length))
				mdstEdges[i] = edgeRef[mdstEdges[i]];
			return newIndexResult(mdstEdges);
		}
	}

	private static int[] expand(IndexGraph g, ContractedGraph cg, int root, int artificialEdgesThreshold) {
		int[] inEdge = new int[cg.n];
		Arrays.fill(inEdge, -1);

		IntStack stack = new IntArrayList();
		assert cg.child[root] < 0;
		for (int v = cg.parent[root], prevChild = root; v >= 0; v = cg.parent[prevChild = v]) {
			for (int child = cg.child[v], c = child;;) {
				assert c >= 0;
				if (c != prevChild)
					stack.push(c);
				c = cg.brother[c];
				if (c == child)
					break;
			}
		}
		while (!stack.isEmpty()) {
			int r = stack.popInt();
			int e = cg.inEdge[r];
			int u = g.edgeTarget(e);
			inEdge[u] = e;

			assert cg.child[u] < 0;
			for (int v = cg.parent[u], prevChild = u; v != cg.parent[r]; v = cg.parent[prevChild = v]) {
				if (cg.child[v] >= 0) {
					for (int child = cg.child[v], c = child;;) {
						assert c >= 0;
						if (c != prevChild)
							stack.push(c);
						c = cg.brother[c];
						if (c == child)
							break;
					}
				}
			}
		}

		int mstSize = 0;
		for (int v : range(cg.n))
			if (v != root && inEdge[v] < artificialEdgesThreshold)
				mstSize++;
		int[] mst = new int[mstSize];
		for (int e, i = 0, v = 0; v < cg.n; v++)
			if (v != root && (e = inEdge[v]) < artificialEdgesThreshold)
				mst[i++] = e;
		return mst;
	}

	private static void addEdgesUntilStronglyConnected(IndexGraph g, int root, IVertexPartition connectivityRes,
			int artificialEdgesThreshold) {
		/*
		 * All the vertices in the graph are assumed to be reachable from the root, so we add a single edge from each
		 * strongly connected component (except the one containing the root) to the root.
		 */

		int blockNum = connectivityRes.numberOfBlocks();
		int[] blockToV = new int[blockNum];
		Arrays.fill(blockToV, -1);
		int n = g.vertices().size();
		for (int b, v = 0; v < n; v++)
			if (blockToV[b = connectivityRes.vertexBlock(v)] < 0)
				blockToV[b] = v;

		int rootBlock = connectivityRes.vertexBlock(root);
		blockLoop: for (int b : range(blockNum)) {
			if (b == rootBlock)
				continue; /* avoid self edges */
			for (IEdgeIter eit = g.outEdges(blockToV[b]).iterator(); eit.hasNext();) {
				eit.nextInt();
				if (connectivityRes.vertexBlock(eit.targetInt()) == rootBlock)
					continue blockLoop; /* V already connected to root, avoid parallel edges */
			}
			int e = g.addEdge(blockToV[b], root);
			assert e >= artificialEdgesThreshold;
		}
	}

	private ContractedGraph contract(IndexGraph g, int[] edgeRef, IWeightFunction wOrig, int artificialEdgesThreshold) {
		assert sccAlg.isStronglyConnected(g);

		int n = g.vertices().size();
		int VMaxNum = n * 2; // max super vertex number

		UnionFindValue uf = UnionFindValue.newInstance();
		uf.makeMany(n);
		int[] ufIdxToV = new int[VMaxNum];
		for (int v : range(n))
			ufIdxToV[v] = v;

		IWeightFunction w;
		if (WeightFunction.isInteger(wOrig)) {
			IWeightFunctionInt wInt = (IWeightFunctionInt) wOrig;
			long hugeWeight0 = Math.max(1, range(artificialEdgesThreshold).mapToLong(wInt::weightInt).max().orElse(0));
			final int hugeWeight = hugeWeight0 > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) hugeWeight0;
			if (edgeRef == null) {
				w = e -> (e < artificialEdgesThreshold ? wOrig.weight(e) : hugeWeight) + uf.getValue(g.edgeTarget(e));
			} else {
				w = e -> (e < artificialEdgesThreshold ? wOrig.weight(edgeRef[e]) : hugeWeight)
						+ uf.getValue(g.edgeTarget(e));
			}
		} else {
			final double hugeWeight =
					Math.max(1, range(artificialEdgesThreshold).mapToDouble(wOrig::weight).max().orElse(0));
			if (edgeRef == null) {
				w = e -> (e < artificialEdgesThreshold ? wOrig.weight(e) : hugeWeight) + uf.getValue(g.edgeTarget(e));
			} else {
				w = e -> (e < artificialEdgesThreshold ? wOrig.weight(edgeRef[e]) : hugeWeight)
						+ uf.getValue(g.edgeTarget(e));
			}
		}

		IntReferenceableHeap[] heap = new IntReferenceableHeap[VMaxNum];
		for (int v : range(n))
			heap[v] = (IntReferenceableHeap) heapBuilder.build(int.class, void.class, w);
		for (int v : range(n))
			for (int e : g.inEdges(v))
				if (g.edgeSource(e) != g.edgeTarget(e))
					heap[v].insert(e);

		int[] parent = new int[VMaxNum];
		int[] child = new int[VMaxNum];
		int[] brother = new int[VMaxNum];
		Arrays.fill(parent, -1);
		Arrays.fill(child, -1);
		Arrays.fill(brother, -1);
		int[] inEdge = new int[VMaxNum];

		Bitmap onPath = new Bitmap(VMaxNum);
		final int startVertex = 0;
		onPath.set(startVertex);

		for (int a = startVertex;;) {
			// Find minimum edge that enters a
			int u, e;
			do {
				// Assuming the graph is strongly connected, if heap is empty we are done
				if (heap[a].isEmpty())
					return new ContractedGraph(n, parent, child, brother, inEdge);
				e = heap[a].extractMin().key();
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
				IntReferenceableHeap cHeap =
						heap[c] = (IntReferenceableHeap) heapBuilder.build(int.class, void.class, w);
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
				} while (parent[a] < 0);

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
