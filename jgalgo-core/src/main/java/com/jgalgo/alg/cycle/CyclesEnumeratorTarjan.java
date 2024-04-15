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

import java.util.Iterator;
import com.jgalgo.alg.path.IPath;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * Tarjan's algorithm for enumeration all cycles in a directed graph.
 *
 * <p>
 * The algorithm runs in \(O((n+m)(c+1))\) time and \(O(n)\) space where \(c\) is the number of simple cycles in the
 * graph.
 *
 * <p>
 * Based on the paper 'Enumeration of the elementary circuits of a directed graph' by Robert Tarjan.
 *
 * @author Barak Ugav
 */
public class CyclesEnumeratorTarjan extends CyclesEnumeratorAbstract {

	/**
	 * Create a new cycles enumeration algorithm object.
	 *
	 * <p>
	 * Please prefer using {@link CyclesEnumerator#newInstance()} to get a default implementation for the
	 * {@link CyclesEnumerator} interface.
	 */
	public CyclesEnumeratorTarjan() {}

	@Override
	protected Iterator<IPath> cyclesIter(IndexGraph g) {
		Assertions.onlyDirected(g);
		return new Iterator<>() {

			final int n = g.vertices().size();
			int startV = 0;
			final IntArrayList path = new IntArrayList();
			final IntArrayList markedStack = new IntArrayList();
			final Bitmap isMarked = new Bitmap(n);
			final Stack<IEdgeIter> edgeIterStack = new ObjectArrayList<>();
			/**
			 * In the paper, there is a boolean flag 'f' in each recursive call of the backtrack function. The flag is
			 * set to true if a cycle was found in the current function call or its successors calls. This invariant
			 * allow us to store a single int to represent all these flags, which is the deepest depth of the recursion
			 * in which a cycle was found. For any depth smaller than this depth, the flag is also true. When we back up
			 * to the store depth, we decrease it by one.
			 */
			int cycleFoundDepth = -1;
			IPath nextCycle;

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
						for (IEdgeIter it = edgeIterStack.top(); it.hasNext();) {
							int e = it.nextInt();
							int v = it.targetInt();
							if (v < startV)
								continue;
							if (v == startV) {
								path.push(e);
								nextCycle = IPath.valueOf(g, startV, startV, new IntArrayList(path));
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
			public IPath next() {
				Assertions.hasNext(this);
				IPath ret = nextCycle;
				advance();
				return ret;
			}
		};
	}

}
