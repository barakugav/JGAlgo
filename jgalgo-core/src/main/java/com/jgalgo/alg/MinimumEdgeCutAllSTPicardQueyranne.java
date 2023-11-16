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
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Picard-Queyranne algorithm for enumerating all the minimum edge cuts between two terminal nodes.
 *
 * <p>
 * Based on 'On the structure of all minimum cuts in a network and applications' by 'Picard, Queyranne (1985).
 *
 * @author Barak Ugav
 */
class MinimumEdgeCutAllSTPicardQueyranne extends MinimumEdgeCutUtils.AbstractImplAllST {

	private final MaximumFlow maxFlowAlgo = MaximumFlow.newInstance();
	private final ClosuresEnumerator closuresAlgo = ClosuresEnumerator.newInstance();
	private static final double EPS = 0.0001;

	@Override
	Iterator<IVertexBiPartition> minimumCutsIter(IndexGraph g, IWeightFunction w, int source, int sink) {
		final int n = g.vertices().size();

		/* Compute maximum flow in the graph, with the weight function as capacity func */
		IFlow maxFlow = (IFlow) maxFlowAlgo.computeMaximumFlow(g, w, Integer.valueOf(source), Integer.valueOf(sink));

		if (w == null)
			w = IWeightFunction.CardinalityWeightFunction;

		/* Identify all the vertices reachable from the source and the reverse-reachable from the sink */
		Bitmap reachableFromSource = new Bitmap(n);
		Bitmap reverseReachableFromSink = new Bitmap(n);
		IntPriorityQueue queue = new FIFOQueueIntNoReduce();
		reachableFromSource.set(source);
		queue.enqueue(source);
		if (g.isDirected()) {
			while (!queue.isEmpty()) {
				int u = queue.dequeueInt();
				for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					double ef = maxFlow.getFlow(e);
					double ew = w.weight(e);
					if (!(ef < ew - EPS))
						continue; /* not a residual edge */
					int v = eit.targetInt();
					if (reachableFromSource.get(v))
						continue;
					reachableFromSource.set(v);
					queue.enqueue(v);
				}
				for (IEdgeIter eit = g.inEdges(u).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					double ef = maxFlow.getFlow(e);
					if (!(ef > EPS))
						continue; /* not a residual edge */
					int v = eit.sourceInt();
					if (reachableFromSource.get(v))
						continue;
					reachableFromSource.set(v);
					queue.enqueue(v);
				}
			}
		} else {
			while (!queue.isEmpty()) {
				int u = queue.dequeueInt();
				for (int e : g.outEdges(u)) {
					int u0 = g.edgeSource(e), v0 = g.edgeTarget(e);
					double f = maxFlow.getFlow(e);
					double ew = w.weight(e);
					int v;
					if (u == u0) {
						if (!(f < ew - EPS))
							continue; /* not a residual edge */
						v = v0;
					} else {
						assert u == v0;
						if (!(f > -ew + EPS))
							continue; /* not a residual edge */
						v = u0;
					}
					if (reachableFromSource.get(v))
						continue;
					reachableFromSource.set(v);
					queue.enqueue(v);
				}
			}
		}
		reverseReachableFromSink.set(sink);
		queue.enqueue(sink);
		if (g.isDirected()) {
			while (!queue.isEmpty()) {
				int u = queue.dequeueInt();
				for (IEdgeIter eit = g.inEdges(u).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					double ef = maxFlow.getFlow(e);
					double ew = w.weight(e);
					if (!(ef < ew - EPS))
						continue; /* not a residual edge */
					int v = eit.sourceInt();
					if (reverseReachableFromSink.get(v))
						continue;
					reverseReachableFromSink.set(v);
					queue.enqueue(v);
				}
				for (IEdgeIter eit = g.outEdges(u).iterator(); eit.hasNext();) {
					int e = eit.nextInt();
					double ef = maxFlow.getFlow(e);
					if (!(ef > EPS))
						continue; /* not a residual edge */
					int v = eit.targetInt();
					if (reverseReachableFromSink.get(v))
						continue;
					reverseReachableFromSink.set(v);
					queue.enqueue(v);
				}
			}
		} else {
			while (!queue.isEmpty()) {
				int u = queue.dequeueInt();
				for (int e : g.outEdges(u)) {
					int u0 = g.edgeSource(e), v0 = g.edgeTarget(e);
					double f = maxFlow.getFlow(e);
					double ew = w.weight(e);
					int v;
					if (u == u0) {
						if (!(f > -ew + EPS))
							continue; /* not a residual edge */
						v = v0;
					} else {
						assert u == v0;
						if (!(f < ew - EPS))
							continue; /* not a residual edge */
						v = u0;
					}
					if (reverseReachableFromSink.get(v))
						continue;
					reverseReachableFromSink.set(v);
					queue.enqueue(v);
				}
			}
		}

		/* Remove blocks that always belong to the closure of the source and sink */
		Bitmap removedVertices = new Bitmap(n);
		removedVertices.or(reachableFromSource);
		removedVertices.or(reverseReachableFromSink);

		/* Build the residual network of the graph */
		IndexGraphBuilder residual0 = IndexGraphBuilder.newDirected();
		residual0.expectedVerticesNum(n);
		int[] vToResV = new int[n];
		int[] resVToV = new int[n];
		Arrays.fill(vToResV, -1);
		Arrays.fill(resVToV, -1);
		for (int v = 0; v < n; v++) {
			if (removedVertices.get(v))
				continue;
			int resV = residual0.addVertex();
			vToResV[v] = resV;
			resVToV[resV] = v;
		}
		if (g.isDirected()) {
			for (int m = g.edges().size(), e = 0; e < m; e++) {
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				if (u == v)
					continue;
				int resU = vToResV[u], resV = vToResV[v];
				if (resU == -1 || resV == -1)
					continue;
				double ef = maxFlow.getFlow(e);
				double ew = w.weight(e);
				if (ef < ew - EPS)
					residual0.addEdge(resU, resV);
				if (ef > EPS)
					residual0.addEdge(resV, resU);
			}

		} else {
			for (int m = g.edges().size(), e = 0; e < m; e++) {
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				if (u == v)
					continue;
				int resU = vToResV[u], resV = vToResV[v];
				if (resU == -1 || resV == -1)
					continue;
				double f = maxFlow.getFlow(e);
				double ew = w.weight(e);
				if (f < ew - EPS)
					residual0.addEdge(resU, resV);
				if (f > -ew + EPS)
					residual0.addEdge(resV, resU);
			}
		}
		IndexGraph residual = residual0.build();

		return new Iterator<>() {

			@SuppressWarnings({ "rawtypes", "unchecked" })
			private final Iterator<IntSet> closuresIter = (Iterator) closuresAlgo.closuresIter(residual);
			private IVertexBiPartition nextCut = new VertexBiPartitions.FromBitmap(g, reachableFromSource);

			@Override
			public boolean hasNext() {
				return nextCut != null;
			}

			@Override
			public IVertexBiPartition next() {
				Assertions.Iters.hasNext(this);
				IVertexBiPartition ret = nextCut;

				if (closuresIter.hasNext()) {
					Bitmap cut = new Bitmap(n);
					for (int resV : closuresIter.next())
						cut.set(resVToV[resV]);
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
