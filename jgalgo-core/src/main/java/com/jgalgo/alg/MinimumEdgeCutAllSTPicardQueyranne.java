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
import java.util.Iterator;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

/**
 * Picard-Queyranne algorithm for enumerating all the minimum edge cuts between two terminal nodes.
 *
 * <p>
 * Based on 'On the structure of all minimum cuts in a network and applications' by 'Picard, Queyranne (1985).
 *
 * @author Barak Ugav
 */
class MinimumEdgeCutAllSTPicardQueyranne extends MinimumEdgeCutAllSTUtils.AbstractImpl {

	private final MaximumFlow maximumFlow = MaximumFlow.newInstance();
	private final StronglyConnectedComponentsAlgo sccAlgo = StronglyConnectedComponentsAlgo.newInstance();
	private final DagClosureIterSchrageBaker closureAlgo = new DagClosureIterSchrageBaker();
	private static final double EPS = 0.0001;

	@Override
	Iterator<IVertexBiPartition> computeAllMinimumCuts(IndexGraph g, IWeightFunction w, int source, int sink) {
		/* Compute maximum flow in the graph, with the weight function as capacity func */
		IFlow maxFlow = (IFlow) maximumFlow.computeMaximumFlow(g, w, Integer.valueOf(source), Integer.valueOf(sink));

		/* Build the residual network of the graph */
		final int n = g.vertices().size();
		IndexGraphBuilder residual0 = IndexGraphBuilder.newDirected();
		residual0.expectedVerticesNum(n);
		for (int v = 0; v < n; v++)
			residual0.addVertex();
		if (g.isDirected()) {
			for (int m = g.edges().size(), e = 0; e < m; e++) {
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				if (u == v)
					continue;
				double ef = maxFlow.getFlow(e);
				double ew = w.weight(e);
				if (ef < ew - EPS)
					residual0.addEdge(u, v);
				if (ef > EPS)
					residual0.addEdge(v, u);
			}

		} else {
			for (int m = g.edges().size(), e = 0; e < m; e++) {
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				if (u == v)
					continue;
				double f = maxFlow.getFlow(e);
				double ew = w.weight(e);
				if (f < ew - EPS)
					residual0.addEdge(u, v);
				if (f > -ew + EPS)
					residual0.addEdge(v, u);
			}
		}
		IndexGraph residual = residual0.build();

		/* Compute the strongly connected components in the residual network */
		IVertexPartition sccs = (IVertexPartition) sccAlgo.findStronglyConnectedComponents(residual);
		final int sccNum = sccs.numberOfBlocks();

		/* Identify all the vertices reachable from the source and the reverse-reachable from the sink */
		/*
		 * TODO: we can identify the reachable vertices before constructing the residual graph, speeding up the strongly
		 * connected components computation.
		 */
		Bitmap reachableFromSource = new Bitmap(n);
		Bitmap reverseReachableFromSink = new Bitmap(n);
		IntPriorityQueue queue = new FIFOQueueIntNoReduce();
		reachableFromSource.set(source);
		queue.enqueue(source);
		while (!queue.isEmpty()) {
			int u = queue.dequeueInt();
			for (IEdgeIter eit = residual.outEdges(u).iterator(); eit.hasNext();) {
				eit.nextInt();
				int v = eit.targetInt();
				if (reachableFromSource.get(v))
					continue;
				reachableFromSource.set(v);
				queue.enqueue(v);
			}
		}
		reverseReachableFromSink.set(sink);
		queue.enqueue(sink);
		while (!queue.isEmpty()) {
			int u = queue.dequeueInt();
			for (IEdgeIter eit = residual.inEdges(u).iterator(); eit.hasNext();) {
				eit.nextInt();
				int v = eit.sourceInt();
				if (reverseReachableFromSink.get(v))
					continue;
				reverseReachableFromSink.set(v);
				queue.enqueue(v);
			}
		}

		/* Remove blocks that always belong to the closure of the source and sink */
		Bitmap removedBlocks = new Bitmap(sccNum);
		for (int v : reachableFromSource)
			removedBlocks.set(sccs.vertexBlock(v));
		for (int v : reverseReachableFromSink)
			removedBlocks.set(sccs.vertexBlock(v));

		/* Build the SCC graph created by contracting each strongly connected components to a super vertex */
		residual0.clear(); /* Reuse graph builder */
		IndexGraphBuilder sccGraph0 = residual0;
		sccGraph0.expectedVerticesNum(sccNum);
		int[] sccvToScc = new int[sccNum];
		int[] sccToSccv = new int[sccNum];
		Arrays.fill(sccToSccv, -1);
		for (int b = 0; b < sccNum; b++) {
			if (!removedBlocks.get(b)) {
				int sccIdx = sccGraph0.addVertex();
				sccvToScc[sccIdx] = b;
				sccToSccv[b] = sccIdx;
			}
		}
		Bitmap seenBlocks = new Bitmap(sccNum);
		IntList seenBlockList = new IntArrayList();
		for (int b1Idx = 0; b1Idx < sccGraph0.vertices().size(); b1Idx++) {
			int b1 = sccvToScc[b1Idx];
			for (int u : sccs.blockVertices(b1)) {
				for (IEdgeIter eit = residual.outEdges(u).iterator(); eit.hasNext();) {
					eit.nextInt();
					int b2 = sccs.vertexBlock(eit.targetInt());
					if (b1 == b2 || seenBlocks.get(b2) || removedBlocks.get(b2))
						continue;
					seenBlocks.set(b2);
					seenBlockList.add(b2);
					sccGraph0.addEdge(b1Idx, sccToSccv[b2]);
				}
			}
			seenBlocks.clearAllUnsafe(seenBlockList);
			seenBlockList.clear();
		}
		IndexGraph sccGraph = sccGraph0.build();

		/* all closures in the scc graph are min edge cuts between s-t */
		return new Iterator<>() {

			private IVertexBiPartition nextCut = new VertexBiPartitions.FromBitmap(g, reachableFromSource);
			private final Iterator<Bitmap> closuresIter = closureAlgo.enumerateAllClosures(sccGraph);

			@Override
			public boolean hasNext() {
				return nextCut != null;
			}

			@Override
			public IVertexBiPartition next() {
				Assertions.Iters.hasNext(this);
				IVertexBiPartition ret = nextCut;

				if (closuresIter.hasNext()) {
					Bitmap closure = closuresIter.next();
					Bitmap cut = new Bitmap(n);
					for (int blk : closure)
						for (int v : sccs.blockVertices(sccvToScc[blk]))
							cut.set(v);
					for (int v : reachableFromSource)
						cut.set(v);
					nextCut = new VertexBiPartitions.FromBitmap(g, cut);
				} else {
					nextCut = null;
				}

				return ret;
			}

		};
	}

}
