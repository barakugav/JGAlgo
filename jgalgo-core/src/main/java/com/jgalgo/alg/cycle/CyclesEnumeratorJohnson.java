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

package com.jgalgo.alg.cycle;

import static com.jgalgo.internal.util.Range.range;
import java.util.Iterator;
import com.jgalgo.alg.common.IPath;
import com.jgalgo.alg.common.IVertexPartition;
import com.jgalgo.alg.connect.StronglyConnectedComponentsAlgo;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.ints.IntStack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Johnson's algorithm for finding all cycles in a directed graph.
 *
 * <p>
 * The algorithm runs in \(O((n+m)(c+1))\) time and \(O(n + m)\) space where \(c\) is the number of simple cycles in the
 * graph.
 *
 * <p>
 * Based on the paper 'finding all the elementary circuits of a directed graph' by Donald b. Johnson.
 *
 * @author Barak Ugav
 */
public class CyclesEnumeratorJohnson extends CyclesEnumeratorAbstract {

	private final StronglyConnectedComponentsAlgo ccAlg = StronglyConnectedComponentsAlgo.newInstance();

	/**
	 * Create a new cycles finder algorithm object.
	 *
	 * <p>
	 * Please prefer using {@link CyclesEnumerator#newInstance()} to get a default implementation for the
	 * {@link CyclesEnumerator} interface.
	 */
	public CyclesEnumeratorJohnson() {}

	@Override
	protected Iterator<IPath> cyclesIter(IndexGraph g) {
		Assertions.onlyDirected(g);
		Assertions.noParallelEdges(g, "graphs with parallel edges are not supported");
		final int n = g.vertices().size();

		return new Iterator<>() {

			int startV;
			StronglyConnectedComponent scc;
			final Bitmap isBlocked;
			final IntSet[] blockingSet;
			final IntStack unblockStack = new IntArrayList();
			final IntArrayList path = new IntArrayList();
			final Stack<IEdgeIter> edgeIterStack = new ObjectArrayList<>();
			/**
			 * In the paper, there is a boolean flag in each recursive call of the backtrack function. The flag is set
			 * to true if a cycle was found in the current function call or its successors calls. This invariant allow
			 * us to store a single int to represent all these flags, which is the deepest depth of the recursion in
			 * which a cycle was found. For any depth smaller than this depth, the flag is also true. When we back up to
			 * the store depth, we decrease it by one.
			 */
			int cycleFoundDepth = -1;
			IPath nextCycle;

			{
				isBlocked = new Bitmap(n);
				blockingSet = new IntSet[n];
				for (int u : range(n))
					blockingSet[u] = new IntOpenHashSet();

				for (startV = 0; startV < n; startV++) {
					chooseSCCInSubGraph();
					if (scc != null) {
						edgeIterStack.push(g.outEdges(startV).iterator());
						isBlocked.set(startV);
						advance();
						break;
					}
				}
			}

			private void advance() {
				if (startV >= n) {
					nextCycle = null;
					return;
				}
				for (;;) {
					currentStartVLoop: while (!edgeIterStack.isEmpty()) {
						for (IEdgeIter it = edgeIterStack.top(); it.hasNext();) {
							int e = it.nextInt();
							int v = it.targetInt();
							if (!scc.contains(v))
								continue;
							if (v == startV) {
								path.push(e);
								nextCycle = IPath.valueOf(g, startV, startV, new IntArrayList(path));
								path.popInt();
								cycleFoundDepth = path.size();
								return;

							} else if (!isBlocked.get(v)) {
								path.push(e);
								edgeIterStack.push(g.outEdges(v).iterator());
								isBlocked.set(v);
								continue currentStartVLoop;
							}
						}

						assert cycleFoundDepth <= path.size();
						boolean cycleFound = cycleFoundDepth == path.size();

						int u = path.isEmpty() ? startV : g.edgeTarget(path.popInt());
						if (cycleFound) {
							unblock(u);
							cycleFoundDepth--;
						} else {
							for (IEdgeIter it = g.outEdges(u).iterator(); it.hasNext();) {
								it.nextInt();
								int v = it.targetInt();
								if (!scc.contains(v))
									continue;
								blockingSet[v].add(u);
							}
						}
						edgeIterStack.pop();
					}

					do {
						startV++;
						if (startV >= n) {
							nextCycle = null;
							return;
						}
						chooseSCCInSubGraph();
					} while (scc == null);
					reset();
					edgeIterStack.push(g.outEdges(startV).iterator());
					isBlocked.set(startV);
				}
			}

			private void reset() {
				isBlocked.clear();
				assert unblockStack.isEmpty();
				for (int u : range(n))
					blockingSet[u].clear();
				assert path.isEmpty();
			}

			private void unblock(int v) {
				isBlocked.clear(v);
				for (;;) {
					for (int u : blockingSet[v]) {
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

			private void chooseSCCInSubGraph() {
				int nFull = g.vertices().size();
				int subToFull = startV;
				int nSub = nFull - subToFull;

				IndexGraphBuilder gSubBuilder = IndexGraphBuilder.directed();
				gSubBuilder.addVertices(range(nSub));
				for (int uSub : range(nSub)) {
					int uFull = uSub + subToFull;
					for (IEdgeIter it = g.outEdges(uFull).iterator(); it.hasNext();) {
						it.nextInt();
						int vSub = it.targetInt() - subToFull;
						if (vSub >= 0)
							gSubBuilder.addEdge(uSub, vSub);
					}
				}
				IndexGraph gSub = gSubBuilder.reIndexAndBuild(false, true).graph;

				IVertexPartition connectivityResult = (IVertexPartition) ccAlg.findStronglyConnectedComponents(gSub);

				for (;; startV++) {
					if (startV >= nFull) {
						scc = null;
						return;
					}
					int uSub = startV - subToFull;
					int ccIdx = connectivityResult.vertexBlock(uSub);
					if (connectivityResult.blockVertices(ccIdx).size() > 1 || hasSelfEdge(gSub, uSub)) {
						scc = new StronglyConnectedComponent(subToFull, connectivityResult, ccIdx);
						return;
					}
				}
			}

			@Override
			public boolean hasNext() {
				return nextCycle != null;
			}

			@Override
			public IPath next() {
				Assertions.hasNext(this);
				IPath ret = nextCycle;
				advance();
				return ret;
			}
		};
	}

	private static boolean hasSelfEdge(IndexGraph g, int u) {
		return g.containsEdge(u, u);
	}

	private static class StronglyConnectedComponent {

		private final int subToFull;
		private final IVertexPartition connectivityResult;
		private final int ccIdx;

		StronglyConnectedComponent(int subToFull, IVertexPartition connectivityResult, int ccIdx) {
			this.subToFull = subToFull;
			this.connectivityResult = connectivityResult;
			this.ccIdx = ccIdx;
		}

		boolean contains(int v) {
			int vSub = v - subToFull;
			if (vSub < 0)
				return false;
			return ccIdx == connectivityResult.vertexBlock(vSub);
		}

	}

}
