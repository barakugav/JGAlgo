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

import java.util.BitSet;
import java.util.Iterator;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Tarjan's algorithm for finding all cycles in a directed graph.
 * <p>
 * The algorithm runs in \(O((n+m)(c+1))\) time and \(O(n)\) space where \(c\) is the number of simple cycles in the
 * graph.
 * <p>
 * Based on the paper 'Enumeration of the elementary circuits of a directed graph' by Robert Tarjan.
 *
 * @author Barak Ugav
 */
class CyclesFinderTarjan extends CyclesFinderAbstract {

	/**
	 * Create a new cycles finder algorithm object.
	 */
	CyclesFinderTarjan() {}

	@Override
	Iterator<Path> findAllCycles(IndexGraph g) {
		Assertions.Graphs.onlyDirected(g);
		return new Iterator<>() {

			final int n = g.vertices().size();
			int startV = 0;
			final IntArrayList path = new IntArrayList();
			final IntArrayList markedStack = new IntArrayList();
			final BitSet isMarked = new BitSet(n);
			final Stack<EdgeIter> edgeIterStack = new ObjectArrayList<>();
			/**
			 * In the paper, there is a boolean flag 'f' in each recursive call of the backtrack function. The flag is
			 * set to true if a cycle was found in the current function call or its successors calls. This invariant
			 * allow us to store a single int to represent all these flags, which is the deepest depth of the recursion
			 * in which a cycle was found. For any depth smaller than this depth, the flag is also true. When we back up
			 * to the store depth, we decrease it by one.
			 */
			int cycleFoundDepth = -1;
			Path nextCycle;

			{
				isMarked.set(startV);
				markedStack.push(startV);
				edgeIterStack.push(g.outEdges(startV).iterator());

				advance();
			}

			private void advance() {
				if (startV >= n) {
					nextCycle = null;
					return;
				}
				for (;;) {
					currentStartVLoop: while (!edgeIterStack.isEmpty()) {
						for (EdgeIter it = edgeIterStack.top(); it.hasNext();) {
							int e = it.nextInt();
							int v = it.target();
							if (v < startV)
								continue;
							if (v == startV) {
								path.push(e);
								nextCycle = new PathImpl(g, startV, startV, new IntArrayList(path));
								path.popInt();
								cycleFoundDepth = path.size();
								return;

							} else if (!isMarked.get(v)) {
								path.push(e);
								isMarked.set(v);
								markedStack.push(v);
								edgeIterStack.push(g.outEdges(v).iterator());
								continue currentStartVLoop;
							}
						}

						assert cycleFoundDepth <= path.size();
						boolean cycleFound = cycleFoundDepth == path.size();

						int u;
						if (path.isEmpty()) {
							u = startV;
						} else {
							u = g.edgeTarget(path.popInt());
						}

						if (cycleFound) {
							while (markedStack.topInt() != u) {
								int w = markedStack.popInt();
								isMarked.clear(w);
							}
							markedStack.popInt();
							isMarked.clear(u);
							cycleFoundDepth--;
						}
						edgeIterStack.pop();
					}

					path.clear();
					markedStack.clear();
					isMarked.clear();
					startV++;
					if (startV >= n) {
						nextCycle = null;
						return;
					}
					isMarked.set(startV);
					markedStack.push(startV);
					edgeIterStack.push(g.outEdges(startV).iterator());
				}
			}

			@Override
			public boolean hasNext() {
				return nextCycle != null;
			}

			@Override
			public Path next() {
				Assertions.Iters.hasNext(this);
				Path ret = nextCycle;
				advance();
				return ret;
			}
		};
	}

}
