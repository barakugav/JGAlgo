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

import java.util.ArrayList;
import java.util.BitSet;
import java.util.Iterator;
import java.util.List;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntStack;
import it.unimi.dsi.fastutil.objects.ObjectIntPair;

/**
 * Johnson's algorithm for finding all cycles in a directed graph.
 * <p>
 * The algorithm runs in \(O((n+m)(c+1))\) time and \(O(n + m)\) space where \(c\) is the number of simple cycles in the
 * graph.
 * <p>
 * Based on the paper 'finding all the elementary circuits of a directed graph' by Donald b. Johnson.
 *
 * @author Barak Ugav
 */
public class CyclesFinderJohnson implements CyclesFinder {

	private final ConnectedComponentsAlgo ccAlg = ConnectedComponentsAlgo.newBuilder().build();

	/**
	 * Create a new cycles finder algorithm object.
	 */
	public CyclesFinderJohnson() {}

	@Override
	public Iterator<Path> findAllCycles(Graph g) {
		ArgumentCheck.onlyDirected(g);
		if (GraphsUtils.containsParallelEdges(g))
			throw new IllegalArgumentException("graphs with self loops are not supported");
		int n = g.vertices().size();
		Worker worker = new Worker(g);
		for (int startIdx = 0; startIdx < n; startIdx++) {
			ObjectIntPair<StronglyConnectedComponent> p = chooseSCCInSubGraph(g, startIdx);
			if (p == null)
				break;
			StronglyConnectedComponent scc = p.first();
			startIdx = p.secondInt();
			worker.findAllCycles(startIdx, scc);
			worker.reset();
		}
		/* TODO: the intention of returning an iterator is to avoid storing all cycles in memory */
		return worker.cycles.iterator();
	}

	private static class Worker {

		private final Graph g;
		private final BitSet isBlocked;
		private final IntSet[] blockingSet;
		private final IntStack unblockStack = new IntArrayList();
		private final IntStack path = new IntArrayList();
		private final List<Path> cycles = new ArrayList<>();

		Worker(Graph g) {
			this.g = g;
			int n = g.vertices().size();
			isBlocked = new BitSet(n);
			blockingSet = new IntSet[n];
			for (int u = 0; u < n; u++)
				blockingSet[u] = new IntOpenHashSet();
		}

		private void reset() {
			int n = g.vertices().size();
			isBlocked.clear();
			assert unblockStack.isEmpty();
			for (int u = 0; u < n; u++)
				blockingSet[u].clear();
			assert path.isEmpty();
		}

		private boolean findAllCycles(int startV, StronglyConnectedComponent scc) {
			boolean cycleFound = false;

			int u = path.isEmpty() ? startV : g.edgeTarget(path.topInt());
			assert scc.contains(u);
			isBlocked.set(u);

			for (EdgeIter it = g.edgesOut(u); it.hasNext();) {
				int e = it.nextInt();
				int v = it.target();
				if (!scc.contains(v))
					continue;
				if (v == startV) {
					path.push(e);
					cycles.add(new Path(g, startV, startV, new IntArrayList((IntList) path)));
					path.popInt();
					cycleFound = true;
				} else if (!isBlocked.get(v)) {
					path.push(e);
					if (findAllCycles(startV, scc))
						cycleFound = true;
					path.popInt();
				}
			}
			if (cycleFound) {
				unblock(u);
			} else {
				for (EdgeIter it = g.edgesOut(u); it.hasNext();) {
					it.nextInt();
					int v = it.target();
					if (!scc.contains(v))
						continue;
					blockingSet[v].add(u);
				}
			}
			return cycleFound;
		}

		private void unblock(int v) {
			isBlocked.clear(v);
			for (;;) {
				for (IntIterator it = blockingSet[v].iterator(); it.hasNext();) {
					int u = it.nextInt();
					if (isBlocked.get(u)) {
						isBlocked.clear(u);
						unblockStack.push(u);
					}
				}
				blockingSet[v].clear();

				if (unblockStack.isEmpty())
					break;
				v = unblockStack.popInt();
			}
		}
	}

	private ObjectIntPair<StronglyConnectedComponent> chooseSCCInSubGraph(Graph g, int startIdx) {
		int nFull = g.vertices().size();
		int subToFull = startIdx;
		int nSub = nFull - subToFull;
		Graph gSub = GraphBuilder.newDirected().expectedVerticesNum(nSub).build();
		for (int i = 0; i < nSub; i++)
			gSub.addVertex();
		for (int uSub = 0; uSub < nSub; uSub++) {
			int uFull = uSub + subToFull;
			for (EdgeIter it = g.edgesOut(uFull); it.hasNext();) {
				it.nextInt();
				int vSub = it.target() - subToFull;
				if (vSub >= 0)
					gSub.addEdge(uSub, vSub);
			}
		}

		ConnectedComponentsAlgo.Result connectivityResult = ccAlg.computeConnectivityComponents(gSub);

		for (; startIdx < nFull; startIdx++) {
			int uSub = startIdx - subToFull;
			int ccIdx = connectivityResult.getVertexCc(uSub);
			if (connectivityResult.getCcVertices(ccIdx).size() > 1 || hasSelfEdge(gSub, uSub))
				break;
		}
		if (startIdx >= nFull)
			return null;
		int ccIdx = connectivityResult.getVertexCc(startIdx - subToFull);
		return ObjectIntPair.of(new StronglyConnectedComponent(subToFull, connectivityResult, ccIdx), startIdx);
	}

	private static boolean hasSelfEdge(Graph g, int u) {
		for (EdgeIter it = g.edgesOut(u); it.hasNext();) {
			it.nextInt();
			if (it.target() == u)
				return true;
		}
		return false;
	}

	private static class StronglyConnectedComponent {

		private final int subToFull;
		private final ConnectedComponentsAlgo.Result connectivityResult;
		private final int ccIdx;

		StronglyConnectedComponent(int subToFull, ConnectedComponentsAlgo.Result connectivityResult, int ccIdx) {
			this.subToFull = subToFull;
			this.connectivityResult = connectivityResult;
			this.ccIdx = ccIdx;
		}

		boolean contains(int v) {
			int vSub = v - subToFull;
			if (vSub < 0)
				return false;
			return ccIdx == connectivityResult.getVertexCc(vSub);
		}

	}

}
