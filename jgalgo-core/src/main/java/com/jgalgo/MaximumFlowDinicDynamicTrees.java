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
import java.util.function.ObjDoubleConsumer;
import com.jgalgo.DynamicTree.MinEdge;
import com.jgalgo.IDStrategy.Fixed;
import com.jgalgo.Utils.Stack;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

/**
 * Dinic's algorithm for maximum flow using dynamic trees.
 * <p>
 * Using {@link DynamicTree} the algorithm of Dinic to maximum flow problem is implemented in time \(O(m n \log n)\) and
 * linear space. In practice, the (relative) complicated implementation of dynamic trees have little gain in the overall
 * performance, and its probably better to use some variant of the {@link MaximumFlowPushRelabel}, which has worse
 * theoretically bounds, but runs faster in practice.
 *
 * @see    MaximumFlowDinic
 * @see    DynamicTree
 * @author Barak Ugav
 */
public class MaximumFlowDinicDynamicTrees implements MaximumFlow {

	private final DebugPrintsManager debug = new DebugPrintsManager(false);
	private static final double EPS = 0.0001;
	private static final Object EdgeRefWeightKey = new Object();

	/**
	 * Create a new maximum flow algorithm object.
	 */
	public MaximumFlowDinicDynamicTrees() {}

	/**
	 * {@inheritDoc}
	 *
	 * @throws IllegalArgumentException if the graph is not directed
	 */
	@Override
	public double computeMaximumFlow(Graph g, FlowNetwork net, int source, int sink) {
		ArgumentCheck.onlyDirected(g);
		ArgumentCheck.sourceSinkNotTheSame(source, sink);
		Graph gOrig = g;
		debug.println("\t", getClass().getSimpleName());

		double maxCapacity = 100;
		for (IntIterator it = gOrig.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			maxCapacity = Math.max(maxCapacity, net.getCapacity(e));
		}

		g = referenceGraph(gOrig, net);
		Weights<Ref> edgeRef = g.getEdgesWeights(EdgeRefWeightKey);
		final int n = g.vertices().size();
		GraphBuilder builder = new GraphBuilderImpl.LinkedDirected();
		Graph L = builder.setVerticesNum(n).setEdgesIDStrategy(Fixed.class).build();
		Weights<Ref> edgeRefL = L.addEdgesWeights(EdgeRefWeightKey, Ref.class);
		IntPriorityQueue bfsQueue = new IntArrayFIFOQueue();
		int[] level = new int[n];
		DynamicTree dt = new DynamicTreeSplay(maxCapacity * 10);
		DynamicTree.Node[] vToDt = new DynamicTree.Node[n];
		Stack<DynamicTree.Node> cleanupStack = new Stack<>();

		int[] edgeToParent = new int[n];
		Arrays.fill(edgeToParent, -1);

		for (;;) {
			debug.println("calculating residual network");
			L.clearEdges();

			/* Calc the sub graph non saturated edges from source to sink using BFS */
			final int unvisited = Integer.MAX_VALUE;
			Arrays.fill(level, unvisited);
			bfsQueue.clear();
			level[source] = 0;
			bfsQueue.enqueue(source);
			bfs: while (!bfsQueue.isEmpty()) {
				int u = bfsQueue.dequeueInt();
				if (u == sink)
					break bfs;
				int lvl = level[u];
				for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
					int e = eit.nextInt();
					int v = eit.target();
					Ref eData = edgeRef.get(e);
					if (eData.flow >= net.getCapacity(eData.orig) || level[v] <= lvl)
						continue;
					edgeRefL.set(L.addEdge(u, v), eData);
					if (level[v] != unvisited)
						continue;
					level[v] = lvl + 1;
					bfsQueue.enqueue(v);
				}
			}
			if (level[sink] == unvisited)
				break; /* All paths to sink are saturated */
			debug.println("sink level: " + level[sink]);

			dt.clear();
			for (int u = 0; u < n; u++)
				(vToDt[u] = dt.makeTree()).setNodeData(Integer.valueOf(u));

			ObjDoubleConsumer<Ref> updateFlow = (e, weight) -> {
				double f = net.getCapacity(e.orig) - e.flow - weight;
				e.flow += f;
				e.rev.flow -= f;
				assert e.flow <= net.getCapacity(e.orig) + EPS;
				assert e.rev.flow >= 0 - EPS;
			};

			calcBlockFlow: for (;;) {
				int v = dt.findRoot(vToDt[source]).<Integer>getNodeData().intValue();
				if (v == sink) {

					/* Augment */
					debug.println("Augment");
					MinEdge min = dt.findMinEdge(vToDt[source]);
					dt.addWeight(vToDt[source], -min.weight());

					/* Delete all saturated edges */
					debug.println("Delete");
					do {
						int e = edgeToParent[min.source().<Integer>getNodeData().intValue()];
						assert vToDt[L.edgeSource(e)] == min.source();
						Ref ref = edgeRefL.get(e);
						L.removeEdge(e);

						updateFlow.accept(ref, 0);
						dt.cut(min.source());

						min = dt.findMinEdge(vToDt[source]);
					} while (min != null && Math.abs(min.weight()) < EPS);

				} else if (!L.edgesOut(v).hasNext()) {

					/* Retreat */
					debug.println("Retreat");
					if (v == source)
						break calcBlockFlow;
					for (EdgeIter eit = L.edgesIn(v); eit.hasNext();) {
						int e = eit.nextInt();
						int u = eit.source();
						if (vToDt[u].getParent() != vToDt[v])
							continue; /* If the edge is not in the DT, ignore */

						MinEdge m = dt.findMinEdge(vToDt[u]);
						assert e == edgeToParent[m.source().<Integer>getNodeData().intValue()];
						Ref ref = edgeRefL.get(e);
						updateFlow.accept(ref, m.weight());

						dt.cut(m.source());
					}
					L.removeEdgesInOf(v);

				} else {
					/* Advance */
					debug.println("Advance");
					EdgeIter eit = L.edgesOut(v);
					int e = eit.nextInt();
					Ref eRef = edgeRefL.get(e);
					dt.link(vToDt[eit.source()], vToDt[eit.target()], net.getCapacity(eRef.orig) - eRef.flow);
					edgeToParent[eit.source()] = e;
				}
			}

			/* Cleanup all the edges that stayed in the DT */
			for (int u = 0; u < n; u++) {
				for (DynamicTree.Node uDt = vToDt[u], pDt; (pDt = uDt.getParent()) != null; uDt = pDt)
					cleanupStack.push(uDt);
				while (!cleanupStack.isEmpty()) {
					DynamicTree.Node uDt = cleanupStack.pop();
					assert uDt.getParent() == dt.findRoot(uDt);
					MinEdge m = dt.findMinEdge(uDt);
					Ref ref = edgeRefL.get(edgeToParent[m.source().<Integer>getNodeData().intValue()]);
					updateFlow.accept(ref, m.weight());
					dt.cut(m.source());
				}
			}
		}

		/* Construct result */
		for (IntIterator it = g.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			Ref data = edgeRef.get(e);
			if (g.edgeSource(e) == gOrig.edgeSource(data.orig))
				net.setFlow(data.orig, data.flow);
		}
		double totalFlow = 0;
		for (EdgeIter eit = g.edgesOut(source); eit.hasNext();) {
			int e = eit.nextInt();
			Ref data = edgeRef.get(e);
			if (g.edgeSource(e) == gOrig.edgeSource(data.orig))
				totalFlow += data.flow;
		}
		for (EdgeIter eit = g.edgesIn(source); eit.hasNext();) {
			int e = eit.nextInt();
			Ref data = edgeRef.get(e);
			if (g.edgeSource(e) == gOrig.edgeSource(data.orig))
				totalFlow -= data.flow;
		}
		return totalFlow;
	}

	private static Graph referenceGraph(Graph g0, FlowNetwork net) {
		Graph g = new GraphArrayDirected(g0.vertices().size());
		Weights<Ref> edgeRef = g.addEdgesWeights(EdgeRefWeightKey, Ref.class);
		for (IntIterator it = g0.edges().iterator(); it.hasNext();) {
			int e = it.nextInt();
			int u = g0.edgeSource(e), v = g0.edgeTarget(e);
			Ref ref = new Ref(e, 0), refRev = new Ref(e, net.getCapacity(e));
			int e1 = g.addEdge(u, v);
			int e2 = g.addEdge(v, u);
			edgeRef.set(e1, ref);
			edgeRef.set(e2, refRev);
			refRev.rev = ref;
			ref.rev = refRev;
		}
		return g;
	}

	private static class Ref {

		final int orig;
		Ref rev;
		double flow;

		Ref(int e, double flow) {
			orig = e;
			rev = null;
			this.flow = flow;
		}

		@Override
		public String toString() {
			return "R(" + orig + ")";
		}

	}

}
