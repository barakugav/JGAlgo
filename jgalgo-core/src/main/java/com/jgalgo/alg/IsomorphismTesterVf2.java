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
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import com.jgalgo.graph.Graphs;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.IterTools;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectIterators;

/**
 * Vf2 algorithm for testing isomorphism of two graphs.
 *
 * <p>
 * Based on 'An Improved Algorithm for Matching Large Graphs' by L. P. Cordella, P. Foggia, C. Sansone and M. Vento.
 *
 * @author Barak Ugav
 */
class IsomorphismTesterVf2 extends IsomorphismTesterAbstract {

	@Override
	Iterator<IsomorphismTester.IMapping> isomorphicMappingsIter(IndexGraph g1, IndexGraph g2) {
		Assertions.Graphs.noParallelEdges(g1, "parallel edges are not supported");
		Assertions.Graphs.noParallelEdges(g2, "parallel edges are not supported");
		final int n = g1.vertices().size();
		final int m = g1.edges().size();
		if (n != g2.vertices().size() || m != g2.edges().size() || g1.isDirected() != g2.isDirected())
			return Collections.emptyIterator();
		if (n == 0) {
			assert m == 0;
			return ObjectIterators
					.singleton(new MappingImpl(IntArrays.DEFAULT_EMPTY_ARRAY, IntArrays.DEFAULT_EMPTY_ARRAY));
		}
		if (m == 0) {
			Iterator<IntList> verticesPermutations =
					JGAlgoUtils.permutations(IntList.of(g1.vertices().toIntArray())).iterator();
			return IterTools.map(verticesPermutations, permutation -> {
				int[] core1 = permutation.toIntArray();
				return new MappingImpl(core1, IntArrays.DEFAULT_EMPTY_ARRAY);
			});
		}
		if (g1.isDirected()) {
			return new IsomorphismIterDirected(g1, g2);
		} else {
			return new IsomorphismIterUndirected(g1, g2);
		}
	}

	private abstract static class IsomorphismIterBase implements Iterator<IsomorphismTester.IMapping> {

		final IndexGraph g1;
		final IndexGraph g2;
		final int n;

		private final Bitmap hasSelfEdge1;
		private final Bitmap hasSelfEdge2;

		final int[] core1;
		final int[] core2;
		int stateDepth;
		final int[] statePrevV1;
		final int[] statePrevV2;
		final IntIterator[] stateNextV1Iter;
		final int[] stateNextV2;

		int nextVisitIdx = 1;
		final int[] visit;
		final int[] visitData;

		boolean nextIsValid;
		int lastV1 = -1, lastV2 = -1;

		static final int None = -1;

		IsomorphismIterBase(IndexGraph g1, IndexGraph g2) {
			this.g1 = g1;
			this.g2 = g2;
			n = g1.vertices().size();

			IntSet selfEdges1 = Graphs.selfEdges(g1);
			IntSet selfEdges2 = Graphs.selfEdges(g2);
			if (selfEdges1.isEmpty()) {
				hasSelfEdge1 = null;
			} else {
				hasSelfEdge1 = new Bitmap(n);
				for (int e : selfEdges1)
					hasSelfEdge1.set(g1.edgeSource(e));
			}
			if (selfEdges2.isEmpty()) {
				hasSelfEdge2 = null;
			} else {
				hasSelfEdge2 = new Bitmap(n);
				for (int e : selfEdges2)
					hasSelfEdge2.set(g2.edgeSource(e));
			}

			core1 = new int[n];
			core2 = new int[n];
			Arrays.fill(core1, None);
			Arrays.fill(core2, None);

			visit = new int[n];
			visitData = new int[n];

			stateDepth = 1;
			statePrevV1 = new int[n + 1];
			statePrevV2 = new int[n + 1];
			stateNextV1Iter = new IntIterator[n + 1];
			stateNextV2 = new int[n + 1];
		}

		abstract boolean advance();

		@Override
		public boolean hasNext() {
			return nextIsValid;
		}

		@Override
		public IsomorphismTester.IMapping next() {
			Assertions.Iters.hasNext(this);
			IsomorphismTester.IMapping mapping = new MappingImpl(core1.clone(), computeEdgeMapping());

			core1[lastV1] = None;
			core2[lastV2] = None;
			nextIsValid = advance();

			return mapping;
		}

		private int[] computeEdgeMapping() {
			final int n = g1.vertices().size();
			final int m = g1.edges().size();
			int[] edges1To2Map = new int[m];

			for (int u1 = 0; u1 < n; u1++) {
				for (IEdgeIter eit = g1.outEdges(u1).iterator(); eit.hasNext();) {
					int e1 = eit.nextInt();
					int v1 = eit.targetInt();
					visit[v1] = e1;
				}
				int u2 = core1[u1];
				for (IEdgeIter eit = g2.outEdges(u2).iterator(); eit.hasNext();) {
					int e2 = eit.nextInt();
					int v2 = eit.targetInt();
					int v1 = core2[v2];
					int e1 = visit[v1];
					edges1To2Map[e1] = e2;
				}
			}
			nextVisitIdx = 1;
			Arrays.fill(visit, -1);
			return edges1To2Map;
		}

		boolean hasSelfEdge1(int vertex) {
			return hasSelfEdge1 != null && hasSelfEdge1.get(vertex);
		}

		boolean hasSelfEdge2(int vertex) {
			return hasSelfEdge2 != null && hasSelfEdge2.get(vertex);
		}
	}

	private static class IsomorphismIterDirected extends IsomorphismIterBase {

		private final int[] in1;
		private final int[] in2;
		private final int[] out1;
		private final int[] out2;

		private final int[] stateT1OutSize;
		private final int[] stateT2OutSize;
		private final int[] stateT1InSize;
		private final int[] stateT2InSize;

		IsomorphismIterDirected(IndexGraph g1, IndexGraph g2) {
			super(g1, g2);

			in1 = new int[n];
			in2 = new int[n];
			out1 = new int[n];
			out2 = new int[n];

			stateT1OutSize = new int[n + 1];
			stateT2OutSize = new int[n + 1];
			stateT1InSize = new int[n + 1];
			stateT2InSize = new int[n + 1];
			newState(None, None, 0, 0, 0, 0);

			nextIsValid = advance();
		}

		private void newState(int v1, int v2, int t1OutSize, int t2OutSize, int t1InSize, int t2InSize) {
			statePrevV1[stateDepth] = v1;
			statePrevV2[stateDepth] = v2;
			stateT1OutSize[stateDepth] = t1OutSize;
			stateT2OutSize[stateDepth] = t2OutSize;
			stateT1InSize[stateDepth] = t1InSize;
			stateT2InSize[stateDepth] = t2InSize;

			assert t1OutSize < n;
			assert t2OutSize < n;
			assert t1InSize < n;
			assert t2InSize < n;

			IntIterator nextV1Iter;
			int nextV2;
			if ((t1OutSize == 0 ^ t2OutSize == 0) || (t1InSize == 0 ^ t2InSize == 0)) {
				nextV1Iter = IntIterators.EMPTY_ITERATOR;
				nextV2 = None;

			} else {
				if (t1OutSize != 0) {
					/* P(s)=T^{out}_1 (s) \times \{\min T^{out}_2(s)\} */
					nextV1Iter = IterTools.filter(range(n).iterator(), u -> core1[u] == None && out1[u] > 0);
					nextV2 = IterTools.filter(range(n).iterator(), u -> core2[u] == None && out2[u] > 0).nextInt();

				} else if (t1InSize != 0) {
					/* P(s)=T^{in}_1 (s) \times \{\min T^{in}_2(s)\} */
					nextV1Iter = IterTools.filter(range(n).iterator(), u -> core1[u] == None && in1[u] > 0);
					nextV2 = IterTools.filter(range(n).iterator(), u -> core2[u] == None && in2[u] > 0).nextInt();

				} else {
					/* P(s)=(N_1 - M_1(s)) \times \{\min (N_2 - M_2)\} */
					nextV1Iter = IterTools.filter(range(n).iterator(), u -> core1[u] == None);
					nextV2 = IterTools.filter(range(n).iterator(), u -> core2[u] == None).nextInt();
				}
				assert nextV1Iter.hasNext();
			}
			stateNextV1Iter[stateDepth] = nextV1Iter;
			stateNextV2[stateDepth] = nextV2;
		}

		@Override
		boolean advance() {
			dfs: while (stateDepth > 0) {
				// IntList mapped1 = new IntArrayList(range(n).filter(u -> core1[u] != None).iterator());
				// IntList range1 = new IntArrayList(mapped1.intStream().map(u -> core1[u]).iterator());
				// IntList mapped2 = new IntArrayList(range(n).filter(u -> core2[u] != None).iterator());
				// IntList range2 = new IntArrayList(mapped2.intStream().map(u -> core2[u]).iterator());
				// assert mapped1.size() == new IntOpenHashSet(mapped1).size();
				// assert mapped2.size() == new IntOpenHashSet(mapped2).size();
				// assert range1.size() == new IntOpenHashSet(range1).size();
				// assert range2.size() == new IntOpenHashSet(range2).size();
				// assert new IntOpenHashSet(mapped1).equals(new IntOpenHashSet(range2));
				// assert new IntOpenHashSet(mapped2).equals(new IntOpenHashSet(range1));

				// int[] t1Out = range(n)
				// .filter(v -> core1[v] != None)
				// .flatMap(v -> g1.outEdges(v).intStream().map(g1::edgeTarget))
				// .filter(v -> core1[v] == None)
				// .distinct()
				// .toArray();
				// int[] t1In = range(n)
				// .filter(v -> core1[v] != None)
				// .flatMap(v -> g1.inEdges(v).intStream().map(g1::edgeSource))
				// .filter(v -> core1[v] == None)
				// .distinct()
				// .toArray();
				// int[] t2Out = range(n)
				// .filter(v -> core2[v] != None)
				// .flatMap(v -> g2.outEdges(v).intStream().map(g2::edgeTarget))
				// .filter(v -> core2[v] == None)
				// .distinct()
				// .toArray();
				// int[] t2In = range(n)
				// .filter(v -> core2[v] != None)
				// .flatMap(v -> g2.inEdges(v).intStream().map(g2::edgeSource))
				// .filter(v -> core2[v] == None)
				// .distinct()
				// .toArray();
				// assert state.t1OutSize == t1Out.length;
				// assert state.t1InSize == t1In.length;
				// assert state.t2OutSize == t2Out.length;
				// assert state.t2InSize == t2In.length;

				final int v2 = stateNextV2[stateDepth];
				for (IntIterator v1Iter = stateNextV1Iter[stateDepth]; v1Iter.hasNext();) {
					final int v1 = v1Iter.nextInt();
					if (!isFeasibleMatchVertices(v1, v2))
						continue;

					/* match v1 to v2 and update state */
					core1[v1] = v2;
					core2[v2] = v1;
					if (stateDepth == n) {
						lastV1 = v1;
						lastV2 = v2;
						return true; /* found a valid full matching */
					}
					stateDepth++;
					updateState(v1, v2);

					if (!isFeasibleCurrentState()) {
						popLastState();
						continue;
					}
					continue dfs;
				}
				popLastState();
			}
			return false;
		}

		private boolean isFeasibleMatchVertices(int v1, int v2) {
			/*
			 * check that out edges connecting v1 and other mapped vertices of g1 can be mapped to out edges connecting
			 * v2 and other mapped vertices of g2
			 */
			int visitIdx = nextVisitIdx++;
			for (IEdgeIter eit = g1.outEdges(v1).iterator(); eit.hasNext();) {
				eit.nextInt();
				int w1 = eit.targetInt();
				int w2 = core1[w1];
				if (w2 != None) {
					visit[w1] = visitIdx;
					visitData[w1] = w2;
				}
			}
			for (IEdgeIter eit = g2.outEdges(v2).iterator(); eit.hasNext();) {
				eit.nextInt();
				int w2 = eit.targetInt();
				int w1 = core2[w2];
				if (w1 != None) {
					if (visit[w1] != visitIdx)
						return false;
					if (visitData[w1] != w2)
						return false;
				}
			}

			/*
			 * check that in edges connecting v1 and other mapped vertices of g1 can be mapped to in edges connecting v2
			 * and other mapped vertices of g2
			 */
			visitIdx = nextVisitIdx++;
			for (IEdgeIter eit = g1.inEdges(v1).iterator(); eit.hasNext();) {
				eit.nextInt();
				int u1 = eit.sourceInt();
				int u2 = core1[u1];
				if (u2 != None) {
					visit[u1] = visitIdx;
					visitData[u1] = u2;
				}
			}
			for (IEdgeIter eit = g2.inEdges(v2).iterator(); eit.hasNext();) {
				eit.nextInt();
				int u2 = eit.sourceInt();
				int u1 = core2[u2];
				if (u1 != None) {
					if (visit[u1] != visitIdx)
						return false;
					if (visitData[u1] != u2)
						return false;
				}
			}

			if (hasSelfEdge1(v1) != hasSelfEdge2(v2))
				return false;

			return true;
		}

		private boolean isFeasibleCurrentState() {
			if (stateT1OutSize[stateDepth] != stateT2OutSize[stateDepth])
				return false;
			if (stateT1InSize[stateDepth] != stateT2InSize[stateDepth])
				return false;
			return true;
		}

		private void updateState(int v1, int v2) {
			final int depth = stateDepth - 1;
			// assert range(n).allMatch(v -> out1[v] < depth);
			// assert range(n).allMatch(v -> in1[v] < depth);
			// assert range(n).allMatch(v -> out2[v] < depth);
			// assert range(n).allMatch(v -> in2[v] < depth);
			int t1OutSize = stateT1OutSize[depth];
			int t2OutSize = stateT2OutSize[depth];
			int t1InSize = stateT1InSize[depth];
			int t2InSize = stateT2InSize[depth];
			if (in1[v1] == 0) {
				in1[v1] = depth;
			} else {
				t1InSize--;
			}
			if (out1[v1] == 0) {
				out1[v1] = depth;
			} else {
				t1OutSize--;
			}
			if (in2[v2] == 0) {
				in2[v2] = depth;
			} else {
				t2InSize--;
			}
			if (out2[v2] == 0) {
				out2[v2] = depth;
			} else {
				t2OutSize--;
			}
			for (IEdgeIter eit = g1.outEdges(v1).iterator(); eit.hasNext();) {
				eit.nextInt();
				int w = eit.targetInt();
				if (out1[w] == 0) {
					out1[w] = depth;
					t1OutSize++;
				}
			}
			for (IEdgeIter eit = g1.inEdges(v1).iterator(); eit.hasNext();) {
				eit.nextInt();
				int u = eit.sourceInt();
				if (in1[u] == 0) {
					in1[u] = depth;
					t1InSize++;
				}
			}
			for (IEdgeIter eit = g2.outEdges(v2).iterator(); eit.hasNext();) {
				eit.nextInt();
				int w = eit.targetInt();
				if (out2[w] == 0) {
					out2[w] = depth;
					t2OutSize++;
				}
			}
			for (IEdgeIter eit = g2.inEdges(v2).iterator(); eit.hasNext();) {
				eit.nextInt();
				int u = eit.sourceInt();
				if (in2[u] == 0) {
					in2[u] = depth;
					t2InSize++;
				}
			}

			// int[] t1Out = range(n)
			// .filter(v -> core1[v] != None)
			// .flatMap(v -> g1.outEdges(v).intStream().map(g1::edgeTarget))
			// .filter(v -> core1[v] == None)
			// .distinct()
			// .toArray();
			// int[] t1In = range(n)
			// .filter(v -> core1[v] != None)
			// .flatMap(v -> g1.inEdges(v).intStream().map(g1::edgeSource))
			// .filter(v -> core1[v] == None)
			// .distinct()
			// .toArray();
			// int[] t2Out = range(n)
			// .filter(v -> core2[v] != None)
			// .flatMap(v -> g2.outEdges(v).intStream().map(g2::edgeTarget))
			// .filter(v -> core2[v] == None)
			// .distinct()
			// .toArray();
			// int[] t2In = range(n)
			// .filter(v -> core2[v] != None)
			// .flatMap(v -> g2.inEdges(v).intStream().map(g2::edgeSource))
			// .filter(v -> core2[v] == None)
			// .distinct()
			// .toArray();
			// assert t1OutSize == t1Out.length;
			// assert t1InSize == t1In.length;
			// assert t2OutSize == t2Out.length;
			// assert t2InSize == t2In.length;

			newState(v1, v2, t1OutSize, t2OutSize, t1InSize, t2InSize);
		}

		private void popLastState() {
			final int prevV1 = statePrevV1[stateDepth];
			final int prevV2 = statePrevV2[stateDepth];
			if (prevV1 != None) {
				final int prevDepth = stateDepth - 1;
				for (IEdgeIter eit = g1.outEdges(prevV1).iterator(); eit.hasNext();) {
					eit.nextInt();
					int w = eit.targetInt();
					if (out1[w] == prevDepth)
						out1[w] = 0;
				}
				for (IEdgeIter eit = g1.inEdges(prevV1).iterator(); eit.hasNext();) {
					eit.nextInt();
					int u = eit.sourceInt();
					if (in1[u] == prevDepth)
						in1[u] = 0;
				}
				for (IEdgeIter eit = g2.outEdges(prevV2).iterator(); eit.hasNext();) {
					eit.nextInt();
					int w = eit.targetInt();
					if (out2[w] == prevDepth)
						out2[w] = 0;
				}
				for (IEdgeIter eit = g2.inEdges(prevV2).iterator(); eit.hasNext();) {
					eit.nextInt();
					int u = eit.sourceInt();
					if (in2[u] == prevDepth)
						in2[u] = 0;
				}
				if (in1[prevV1] == prevDepth)
					in1[prevV1] = 0;
				if (out1[prevV1] == prevDepth)
					out1[prevV1] = 0;
				if (in2[prevV2] == prevDepth)
					in2[prevV2] = 0;
				if (out2[prevV2] == prevDepth)
					out2[prevV2] = 0;
				core1[prevV1] = None;
				core2[prevV2] = None;
			}

			// assert range(n).allMatch(v -> out1[v] < prevDepth);
			// assert range(n).allMatch(v -> in1[v] < prevDepth);
			// assert range(n).allMatch(v -> out2[v] < prevDepth);
			// assert range(n).allMatch(v -> in2[v] < prevDepth);
			stateDepth--;
		}
	}

	private static class IsomorphismIterUndirected extends IsomorphismIterBase {

		private final int[] out1;
		private final int[] out2;

		private final int[] stateT1OutSize;
		private final int[] stateT2OutSize;

		IsomorphismIterUndirected(IndexGraph g1, IndexGraph g2) {
			super(g1, g2);

			out1 = new int[n];
			out2 = new int[n];

			stateT1OutSize = new int[n + 1];
			stateT2OutSize = new int[n + 1];
			newState(None, None, 0, 0);

			nextIsValid = advance();
		}

		private void newState(int v1, int v2, int t1OutSize, int t2OutSize) {
			statePrevV1[stateDepth] = v1;
			statePrevV2[stateDepth] = v2;
			stateT1OutSize[stateDepth] = t1OutSize;
			stateT2OutSize[stateDepth] = t2OutSize;

			assert t1OutSize < n;
			assert t2OutSize < n;

			IntIterator nextV1Iter;
			int nextV2;
			if ((t1OutSize == 0 ^ t2OutSize == 0)) {
				nextV1Iter = IntIterators.EMPTY_ITERATOR;
				nextV2 = None;

			} else {
				if (t1OutSize != 0) {
					/* P(s)=T^{out}_1 (s) \times \{\min T^{out}_2(s)\} */
					nextV1Iter = IterTools.filter(range(n).iterator(), u -> core1[u] == None && out1[u] > 0);
					nextV2 = IterTools.filter(range(n).iterator(), u -> core2[u] == None && out2[u] > 0).nextInt();

				} else {
					/* P(s)=(N_1 - M_1(s)) \times \{\min (N_2 - M_2)\} */
					nextV1Iter = IterTools.filter(range(n).iterator(), u -> core1[u] == None);
					nextV2 = IterTools.filter(range(n).iterator(), u -> core2[u] == None).nextInt();
				}
				assert nextV1Iter.hasNext();
			}
			stateNextV1Iter[stateDepth] = nextV1Iter;
			stateNextV2[stateDepth] = nextV2;
		}

		@Override
		boolean advance() {
			dfs: while (stateDepth > 0) {
				// IntList mapped1 = new IntArrayList(range(n).filter(u -> core1[u] != None).iterator());
				// IntList range1 = new IntArrayList(mapped1.intStream().map(u -> core1[u]).iterator());
				// IntList mapped2 = new IntArrayList(range(n).filter(u -> core2[u] != None).iterator());
				// IntList range2 = new IntArrayList(mapped2.intStream().map(u -> core2[u]).iterator());
				// assert mapped1.size() == new IntOpenHashSet(mapped1).size();
				// assert mapped2.size() == new IntOpenHashSet(mapped2).size();
				// assert range1.size() == new IntOpenHashSet(range1).size();
				// assert range2.size() == new IntOpenHashSet(range2).size();
				// assert new IntOpenHashSet(mapped1).equals(new IntOpenHashSet(range2));
				// assert new IntOpenHashSet(mapped2).equals(new IntOpenHashSet(range1));

				// int[] t1Out = range(n)
				// .filter(v -> core1[v] != None)
				// .flatMap(v -> g1.outEdges(v).intStream().map(g1::edgeTarget))
				// .filter(v -> core1[v] == None)
				// .distinct()
				// .toArray();
				// int[] t1In = range(n)
				// .filter(v -> core1[v] != None)
				// .flatMap(v -> g1.inEdges(v).intStream().map(g1::edgeSource))
				// .filter(v -> core1[v] == None)
				// .distinct()
				// .toArray();
				// int[] t2Out = range(n)
				// .filter(v -> core2[v] != None)
				// .flatMap(v -> g2.outEdges(v).intStream().map(g2::edgeTarget))
				// .filter(v -> core2[v] == None)
				// .distinct()
				// .toArray();
				// int[] t2In = range(n)
				// .filter(v -> core2[v] != None)
				// .flatMap(v -> g2.inEdges(v).intStream().map(g2::edgeSource))
				// .filter(v -> core2[v] == None)
				// .distinct()
				// .toArray();
				// assert state.t1OutSize == t1Out.length;
				// assert state.t1InSize == t1In.length;
				// assert state.t2OutSize == t2Out.length;
				// assert state.t2InSize == t2In.length;

				final int v2 = stateNextV2[stateDepth];
				for (IntIterator v1Iter = stateNextV1Iter[stateDepth]; v1Iter.hasNext();) {
					final int v1 = v1Iter.nextInt();
					if (!isFeasibleMatchVertices(v1, v2))
						continue;

					/* match v1 to v2 and update state */
					core1[v1] = v2;
					core2[v2] = v1;
					if (stateDepth == n) {
						lastV1 = v1;
						lastV2 = v2;
						return true; /* found a valid full matching */
					}
					stateDepth++;
					updateState(v1, v2);

					if (!isFeasibleCurrentState()) {
						popLastState();
						continue;
					}
					continue dfs;
				}
				popLastState();
			}
			return false;
		}

		private boolean isFeasibleMatchVertices(int v1, int v2) {
			/*
			 * check that out edges connecting v1 and other mapped vertices of g1 can be mapped to out edges connecting
			 * v2 and other mapped vertices of g2
			 */
			int visitIdx = nextVisitIdx++;
			for (IEdgeIter eit = g1.outEdges(v1).iterator(); eit.hasNext();) {
				eit.nextInt();
				int w1 = eit.targetInt();
				int w2 = core1[w1];
				if (w2 != None) {
					visit[w1] = visitIdx;
					visitData[w1] = w2;
				}
			}
			for (IEdgeIter eit = g2.outEdges(v2).iterator(); eit.hasNext();) {
				eit.nextInt();
				int w2 = eit.targetInt();
				int w1 = core2[w2];
				if (w1 != None) {
					if (visit[w1] != visitIdx)
						return false;
					if (visitData[w1] != w2)
						return false;
				}
			}

			if (hasSelfEdge1(v1) != hasSelfEdge2(v2))
				return false;

			return true;
		}

		private boolean isFeasibleCurrentState() {
			if (stateT1OutSize[stateDepth] != stateT2OutSize[stateDepth])
				return false;
			return true;
		}

		private void updateState(int v1, int v2) {
			final int depth = stateDepth - 1;
			// assert range(n).allMatch(v -> out1[v] < depth);
			// assert range(n).allMatch(v -> out2[v] < depth);
			int t1OutSize = stateT1OutSize[depth];
			int t2OutSize = stateT2OutSize[depth];
			if (out1[v1] == 0) {
				out1[v1] = depth;
			} else {
				t1OutSize--;
			}
			if (out2[v2] == 0) {
				out2[v2] = depth;
			} else {
				t2OutSize--;
			}
			for (IEdgeIter eit = g1.outEdges(v1).iterator(); eit.hasNext();) {
				eit.nextInt();
				int w = eit.targetInt();
				if (out1[w] == 0) {
					out1[w] = depth;
					t1OutSize++;
				}
			}
			for (IEdgeIter eit = g2.outEdges(v2).iterator(); eit.hasNext();) {
				eit.nextInt();
				int w = eit.targetInt();
				if (out2[w] == 0) {
					out2[w] = depth;
					t2OutSize++;
				}
			}

			// int[] t1Out = range(n)
			// .filter(v -> core1[v] != None)
			// .flatMap(v -> g1.outEdges(v).intStream().map(g1::edgeTarget))
			// .filter(v -> core1[v] == None)
			// .distinct()
			// .toArray();
			// int[] t2Out = range(n)
			// .filter(v -> core2[v] != None)
			// .flatMap(v -> g2.outEdges(v).intStream().map(g2::edgeTarget))
			// .filter(v -> core2[v] == None)
			// .distinct()
			// .toArray();
			// assert t1OutSize == t1Out.length;
			// assert t2OutSize == t2Out.length;

			newState(v1, v2, t1OutSize, t2OutSize);
		}

		private void popLastState() {
			final int prevV1 = statePrevV1[stateDepth];
			final int prevV2 = statePrevV2[stateDepth];
			if (prevV1 != None) {
				final int prevDepth = stateDepth - 1;
				for (IEdgeIter eit = g1.outEdges(prevV1).iterator(); eit.hasNext();) {
					eit.nextInt();
					int w = eit.targetInt();
					if (out1[w] == prevDepth)
						out1[w] = 0;
				}
				for (IEdgeIter eit = g2.outEdges(prevV2).iterator(); eit.hasNext();) {
					eit.nextInt();
					int w = eit.targetInt();
					if (out2[w] == prevDepth)
						out2[w] = 0;
				}
				if (out1[prevV1] == prevDepth)
					out1[prevV1] = 0;
				if (out2[prevV2] == prevDepth)
					out2[prevV2] = 0;
				core1[prevV1] = None;
				core2[prevV2] = None;
			}

			// assert range(n).allMatch(v -> out1[v] < prevDepth);
			// assert range(n).allMatch(v -> out2[v] < prevDepth);
			stateDepth--;
		}

	}

}
