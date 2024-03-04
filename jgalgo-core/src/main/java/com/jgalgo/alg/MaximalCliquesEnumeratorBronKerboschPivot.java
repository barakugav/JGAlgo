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

import static com.jgalgo.internal.util.Range.range;
import java.util.Iterator;
import java.util.List;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;

/**
 * The Bron-Kerbosch algorithm for Maximal cliques with the pivot heuristic.
 *
 * <p>
 * Based on 'Algorithm 457: finding all cliques of an undirected graph' by Coen Bron and Joep Kerbosch.
 *
 * @author Barak Ugav
 */
class MaximalCliquesEnumeratorBronKerboschPivot extends MaximalCliquesEnumerators.AbstractImpl {

	@Override
	Iterator<IntSet> maximalCliquesIter(IndexGraph g) {
		Assertions.onlyUndirected(g);

		return new Iterator<>() {

			final int n = g.vertices().size();
			final Bitmap edges;

			final IntList currentClique = new IntArrayList();
			final Stack<IntList> potentialStack = new ObjectArrayList<>();
			final Stack<IntList> potentialToIterStack = new ObjectArrayList<>();
			final Stack<IntList> excludedStack = new ObjectArrayList<>();
			boolean hasNextClique;

			{
				edges = new Bitmap(n * n);
				for (int e : range(g.edges().size())) {
					int u = g.edgeSource(e);
					int v = g.edgeTarget(e);
					if (u == v)
						continue;
					edges.set(u * n + v);
					edges.set(v * n + u);
				}

				IntArrayList potential = new IntArrayList(g.vertices());
				IntList excluded = new IntArrayList();
				IntList potentialFiltered = filterPotential(potential, excluded);
				potentialStack.push(potential);
				potentialToIterStack.push(potentialFiltered);
				excludedStack.push(excluded);

				findNextClique();
			}

			private boolean containsEdge(int u, int v) {
				return edges.get(u * n + v);
			}

			@Override
			public boolean hasNext() {
				return hasNextClique;
			}

			@Override
			public IntSet next() {
				Assertions.hasNext(this);
				IntSet ret = ImmutableIntArraySet.withNaiveContains(currentClique.toIntArray());
				removeLastInsertedVertex(potentialStack.top(), potentialToIterStack.top(), excludedStack.top());
				findNextClique();
				return ret;
			}

			private void findNextClique() {
				IntList potential = potentialStack.top();
				IntList potentialToIter = potentialToIterStack.top();
				IntList excluded = excludedStack.top();

				recursionLoop: for (;;) {
					assert !potential.isEmpty() || !excluded.isEmpty();

					while (!potentialToIter.isEmpty()) { /* for each v in P \ N(pivot) */
						int v = potentialToIter.getInt(potentialToIter.size() - 1);

						currentClique.add(v);
						IntArrayList nextPotential = new IntArrayList();
						for (int u : potential)
							if (containsEdge(u, v))
								nextPotential.add(u);
						IntList nextExcluded = new IntArrayList();
						for (int u : excluded)
							if (containsEdge(u, v))
								nextExcluded.add(u);
						if (nextPotential.isEmpty() && nextExcluded.isEmpty()) {
							hasNextClique = true;
							return;
						}

						boolean skip = false;
						excludedLoop: for (int w : excluded) {
							for (int u : potential)
								if (!containsEdge(u, w))
									continue excludedLoop;
							skip = true;
							break;
						}
						if (!skip) {
							potentialStack.push(potential = nextPotential);
							potentialToIterStack.push(potentialToIter = filterPotential(nextPotential, nextExcluded));
							excludedStack.push(excluded = nextExcluded);
							continue recursionLoop;
						}
						removeLastInsertedVertex(potential, potentialToIter, excluded);
					}

					potentialStack.pop();
					potentialToIterStack.pop();
					excludedStack.pop();
					if (potentialStack.isEmpty()) {
						hasNextClique = false;
						return;
					}
					potential = potentialStack.top();
					potentialToIter = potentialToIterStack.top();
					excluded = excludedStack.top();
					removeLastInsertedVertex(potential, potentialToIter, excluded);
				}
			}

			private void removeLastInsertedVertex(IntList potential, IntList potentialToIter, IntList excluded) {
				int v = currentClique.removeInt(currentClique.size() - 1); /* R.remove(v); */
				int lastPotential1 = potential.removeInt(potential.size() - 1); /* P.remove(v); */
				int lastPotential2 = potentialToIter.removeInt(potentialToIter.size() - 1); /* P.remove(v); */
				assert lastPotential1 == v;
				assert lastPotential2 == v;
				excluded.add(v);
			}

			private IntList filterPotential(IntArrayList potential, IntList excluded) {
				int pivot = choosePivot(potential, excluded);
				int[] potentialArr = potential.elements();
				for (int left = 0, right = potential.size() - 1;;) {
					if (left > right) {
						IntList potentialFiltered =
								new IntArrayList(potentialArr, right + 1, potential.size() - right - 1);
						assert potentialFiltered.intStream().allMatch(u -> !containsEdge(pivot, u));
						assert new IntArrayList(potentialArr, 0, left)
								.intStream()
								.allMatch(u -> containsEdge(pivot, u));
						return potentialFiltered;
					}

					if (!containsEdge(pivot, potentialArr[right])) {
						right--;
					} else if (containsEdge(pivot, potentialArr[left])) {
						left++;
					} else {
						IntArrays.swap(potentialArr, left, right);
						left++;
						right--;
					}
				}
			}

			private int choosePivot(IntList potential, IntList excluded) {
				int pivot = -1;
				int maxPotentialNeighbors = -1;
				for (IntList l : List.of(potential, excluded))
					for (int u : l) {
						int potentialNeighbors = 0;
						for (int v : potential)
							if (containsEdge(u, v))
								potentialNeighbors++;
						if (potentialNeighbors > maxPotentialNeighbors) {
							maxPotentialNeighbors = potentialNeighbors;
							pivot = u;
						}
					}

				return pivot;
			}

		};
	}

}
