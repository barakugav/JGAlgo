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
import java.util.Objects;
import java.util.function.IntBinaryOperator;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.IterTools;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntIterators;
import it.unimi.dsi.fastutil.objects.ObjectIterators;

/**
 * Vf2 algorithm for testing isomorphism of two graphs.
 *
 * <p>
 * Based on 'An Improved Algorithm for Matching Large Graphs' by L. P. Cordella, P. Foggia, C. Sansone and M. Vento. The
 * paper denote the smaller graph as G2 and the bigger graph as G1 in (induced) sub graph isomorphism. We use the
 * opposite notation, which seems more suitable as the returned mapping is from G1 to G2.
 *
 * @author Barak Ugav
 */
class IsomorphismTesterVf2 implements IsomorphismTesterBase {

	@Override
	public Iterator<IsomorphismIMapping> isomorphicMappingsIter(IndexGraph g1, IndexGraph g2, IsomorphismType type,
			IntBinaryOperator vertexMatcher, IntBinaryOperator edgeMatcher) {
		Assertions.noParallelEdges(g1, "parallel edges are not supported");
		Assertions.noParallelEdges(g2, "parallel edges are not supported");
		if (g1.isDirected() != g2.isDirected())
			throw new IllegalArgumentException("directed/undirected graphs mismatch");
		Objects.requireNonNull(type);

		final int n1 = g1.vertices().size();
		final int n2 = g2.vertices().size();
		final int m1 = g1.edges().size();
		final int m2 = g2.edges().size();

		if (type == IsomorphismType.Full && (n1 != n2 || m1 != m2))
			return Collections.emptyIterator();

		if (n1 > n2 || m1 > m2 || (n1 == n2 && m1 != m2 && type != IsomorphismType.SubGraph))
			return Collections.emptyIterator();

		if (n1 == 0) {
			assert m1 == 0;
			return ObjectIterators
					.singleton(new IsomorphismTesters.IndexMapping(g1, g2, IntArrays.DEFAULT_EMPTY_ARRAY,
							IntArrays.DEFAULT_EMPTY_ARRAY));
		}

		if (g1.isDirected()) {
			return new IsomorphismIterDirected(g1, g2, type, vertexMatcher, edgeMatcher);
		} else {
			return new IsomorphismIterUndirected(g1, g2, type, vertexMatcher, edgeMatcher);
		}
	}

	private abstract static class IsomorphismIterBase implements Iterator<IsomorphismIMapping> {

		final IndexGraph g1;
		final IndexGraph g2;
		final int n1, n2;
		private final IntBinaryOperator vertexMatcher;
		private final IntBinaryOperator edgeMatcher;
		final IsomorphismType type;
		final boolean subGraph;
		final boolean inducedSubGraph;

		final int[] core1;
		final int[] core2;
		int stateDepth;
		final int[] statePrevV1;
		final int[] statePrevV2;
		final int[] stateNextV1;
		final IntIterator[] stateNextV2Iter;

		int nextVisitIdx = 1;
		final int[] visit;
		final int[] visitData;

		int[] nextMapping;

		static final int None = -1;

		IsomorphismIterBase(IndexGraph g1, IndexGraph g2, IsomorphismType type, IntBinaryOperator vertexMatcher,
				IntBinaryOperator edgeMatcher) {
			this.g1 = g1;
			this.g2 = g2;
			n1 = g1.vertices().size();
			n2 = g2.vertices().size();
			this.type = type;
			this.vertexMatcher = vertexMatcher;
			this.edgeMatcher = edgeMatcher;

			assert n1 <= n2;
			subGraph = type == IsomorphismType.SubGraph || n1 < n2;
			inducedSubGraph = Objects.requireNonNull(type) != IsomorphismType.SubGraph;

			core1 = new int[n1];
			core2 = new int[n2];
			Arrays.fill(core1, None);
			Arrays.fill(core2, None);

			visit = new int[/* max(n1,n2) */ n2];
			visitData = new int[/* max(n1,n2) */ n2];

			stateDepth = 1;
			final int maxStateDepth = n1 + 1;
			statePrevV1 = new int[maxStateDepth];
			statePrevV2 = new int[maxStateDepth];
			stateNextV1 = new int[maxStateDepth];
			stateNextV2Iter = new IntIterator[maxStateDepth];
		}

		abstract void advance();

		@Override
		public boolean hasNext() {
			return nextMapping != null;
		}

		@Override
		public IsomorphismIMapping next() {
			Assertions.hasNext(this);
			IsomorphismIMapping mapping =
					new IsomorphismTesters.IndexMapping(g1, g2, nextMapping, computeEdgeMapping(nextMapping));
			advance();
			return mapping;
		}

		private int[] computeEdgeMapping(int[] vMapping) {
			int[] eMapping = new int[g1.edges().size()];

			Arrays.fill(visit, -1);
			for (final int u1 : range(g1.vertices().size())) {
				final int u2 = vMapping[u1];
				final int visitIdx = u1;
				for (IEdgeIter eit = g1.outEdges(u1).iterator(); eit.hasNext();) {
					int e1 = eit.nextInt();
					int v1 = eit.targetInt();
					int v2 = vMapping[v1];
					visit[v2] = visitIdx;
					visitData[v2] = e1;
				}
				for (IEdgeIter eit = g2.outEdges(u2).iterator(); eit.hasNext();) {
					int e2 = eit.nextInt();
					int v2 = eit.targetInt();
					if (visit[v2] == visitIdx) {
						int e1 = visitData[v2];
						eMapping[e1] = e2;						
					}
				}
			}
			Arrays.fill(visit, 0);
			nextVisitIdx = 1;

			return eMapping;
		}

		boolean canMatchVertices(int v1, int v2) {
			return vertexMatcher == null || vertexMatcher.applyAsInt(v1, v2) != 0;
		}

		boolean canMatchEdges(int e1, int e2) {
			return edgeMatcher == null || edgeMatcher.applyAsInt(e1, e2) != 0;
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

		IsomorphismIterDirected(IndexGraph g1, IndexGraph g2, IsomorphismType type, IntBinaryOperator vertexMatcher,
				IntBinaryOperator edgeMatcher) {
			super(g1, g2, type, vertexMatcher, edgeMatcher);

			in1 = new int[n1];
			in2 = new int[n2];
			out1 = new int[n1];
			out2 = new int[n2];

			final int maxStateDepth = n1 + 1;
			stateT1OutSize = new int[maxStateDepth];
			stateT2OutSize = new int[maxStateDepth];
			stateT1InSize = new int[maxStateDepth];
			stateT2InSize = new int[maxStateDepth];
			newState(None, None, 0, 0, 0, 0);

			advance();
		}

		private void newState(int v1, int v2, int t1OutSize, int t2OutSize, int t1InSize, int t2InSize) {
			statePrevV1[stateDepth] = v1;
			statePrevV2[stateDepth] = v2;
			stateT1OutSize[stateDepth] = t1OutSize;
			stateT2OutSize[stateDepth] = t2OutSize;
			stateT1InSize[stateDepth] = t1InSize;
			stateT2InSize[stateDepth] = t2InSize;

			assert t1OutSize < n1;
			assert t2OutSize < n2;
			assert t1InSize < n1;
			assert t2InSize < n2;

			int nextV1;
			IntIterator nextV2Iter;
			/*
			 * "In case that only one of the in-terminal sets or only one of the out-terminal sets is empty, it can be
			 * demonstrated that the state s cannot be part of a matching, and it is not further explored." Although
			 * this is stated in the paper, this does not seem to be correct for non-full isomorphism.
			 */
			if (type == IsomorphismType.Full
					&& ((t1OutSize == 0 ^ t2OutSize == 0) || (t1InSize == 0 ^ t2InSize == 0))) {
				nextV1 = None;
				nextV2Iter = IntIterators.EMPTY_ITERATOR;

			} else {
				if (t1OutSize != 0 && t2OutSize != 0) {
					/* P(s)=T^{out}_1 (s) \times \{\min T^{out}_2(s)\} */
					/* we use the opposite notation of G1 G2 than what used in the paper (G1 is the smaller graph) */
					nextV1 = IterTools.filter(range(n1).iterator(), u1 -> core1[u1] < 0 && out1[u1] > 0).nextInt();
					nextV2Iter = IterTools.filter(range(n2).iterator(), u2 -> core2[u2] < 0 && out2[u2] > 0);

				} else if (t1InSize != 0 && t2InSize != 0) {
					/* P(s)=T^{in}_1 (s) \times \{\min T^{in}_2(s)\} */
					/* we use the opposite notation of G1 G2 than what used in the paper (G1 is the smaller graph) */
					nextV1 = IterTools.filter(range(n1).iterator(), u1 -> core1[u1] < 0 && in1[u1] > 0).nextInt();
					nextV2Iter = IterTools.filter(range(n2).iterator(), u2 -> core2[u2] < 0 && in2[u2] > 0);

				} else {
					/* P(s)=(N_1 - M_1(s)) \times \{\min (N_2 - M_2)\} */
					nextV1 = IterTools.filter(range(n1).iterator(), u1 -> core1[u1] < 0).nextInt();
					nextV2Iter = IterTools.filter(range(n2).iterator(), u2 -> core2[u2] < 0);
				}
				assert nextV2Iter.hasNext();
			}
			stateNextV1[stateDepth] = nextV1;
			stateNextV2Iter[stateDepth] = nextV2Iter;
		}

		@Override
		void advance() {
			dfs: while (stateDepth > 0) {
				final int v1 = stateNextV1[stateDepth];
				for (IntIterator v2Iter = stateNextV2Iter[stateDepth]; v2Iter.hasNext();) {
					final int v2 = v2Iter.nextInt();
					if (!isFeasibleMatchVertices(v1, v2))
						continue;

					if (stateDepth == n1) {
						/* found a valid full matching */
						nextMapping = core1.clone();
						nextMapping[v1] = v2;
						return;
					}
					/* match v1 to v2 and update state */
					core1[v1] = v2;
					core2[v2] = v1;
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
			nextMapping = null;
		}

		private boolean isFeasibleMatchVertices(int v1, int v2) {
			/* check user custom matcher */
			if (!canMatchVertices(v1, v2))
				return false;

			/*
			 * check that out edges connecting v1 and other mapped vertices of g1 can be mapped to out edges connecting
			 * v2 and other mapped vertices of g2
			 */
			int visitIdx = nextVisitIdx++;
			int edgeCount = 0;
			for (IEdgeIter eit = g1.outEdges(v1).iterator(); eit.hasNext();) {
				int e1 = eit.nextInt();
				int w1 = eit.targetInt();
				int w2 = core1[w1];
				if (w2 < 0 && v1 != w1)
					continue;
				visit[w1] = visitIdx;
				visitData[w1] = e1;
				edgeCount++;

			}
			for (IEdgeIter eit = g2.outEdges(v2).iterator(); eit.hasNext();) {
				int e2 = eit.nextInt();
				int w2 = eit.targetInt();
				int w1 = core2[w2];
				if (w1 < 0 && v2 != w2)
					continue;
				if (v2 == w2)
					w1 = v1;
				if (visit[w1] == visitIdx) {
					int e1 = visitData[w1];
					if (!canMatchEdges(e1, e2))
						return false;
					edgeCount--;

				} else if (inducedSubGraph) {
					/* there is no edge (v1,w1) matching e2 */
					return false;
				}
			}
			if (edgeCount != 0)
				/* there are some edges connecting v1 in g1 without match in g2 */
				return false;

			/*
			 * check that in edges connecting v1 and other mapped vertices of g1 can be mapped to in edges connecting v2
			 * and other mapped vertices of g2
			 */
			visitIdx = nextVisitIdx++;
			for (IEdgeIter eit = g1.inEdges(v1).iterator(); eit.hasNext();) {
				int e1 = eit.nextInt();
				int u1 = eit.sourceInt();
				int u2 = core1[u1];
				if (u2 < 0)
					continue;
				visit[u1] = visitIdx;
				visitData[u1] = e1;
				edgeCount++;
			}
			for (IEdgeIter eit = g2.inEdges(v2).iterator(); eit.hasNext();) {
				int e2 = eit.nextInt();
				int u2 = eit.sourceInt();
				int u1 = core2[u2];
				if (u1 < 0)
					continue;
				if (visit[u1] == visitIdx) {
					int e1 = visitData[u1];
					if (!canMatchEdges(e1, e2))
						return false;
					edgeCount--;

				} else if (inducedSubGraph) {
					/* there is no edge (u1,v1) matching e2 */
					return false;
				}
			}
			if (edgeCount != 0)
				/* there are some edges connecting v1 in g1 without match in g2 */
				return false;

			return true;
		}

		private boolean isFeasibleCurrentState() {
			if (subGraph) {
				if (stateT1OutSize[stateDepth] > stateT2OutSize[stateDepth])
					return false;
				if (stateT1InSize[stateDepth] > stateT2InSize[stateDepth])
					return false;
			} else {
				if (stateT1OutSize[stateDepth] != stateT2OutSize[stateDepth])
					return false;
				if (stateT1InSize[stateDepth] != stateT2InSize[stateDepth])
					return false;
			}
			return true;
		}

		private void updateState(int v1, int v2) {
			final int depth = stateDepth - 1;
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

			newState(v1, v2, t1OutSize, t2OutSize, t1InSize, t2InSize);
		}

		private void popLastState() {
			final int prevV1 = statePrevV1[stateDepth];
			final int prevV2 = statePrevV2[stateDepth];
			if (prevV1 >= 0) {
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
			stateDepth--;
		}
	}

	private static class IsomorphismIterUndirected extends IsomorphismIterBase {

		private final int[] out1;
		private final int[] out2;

		private final int[] stateT1OutSize;
		private final int[] stateT2OutSize;

		IsomorphismIterUndirected(IndexGraph g1, IndexGraph g2, IsomorphismType type, IntBinaryOperator vertexMatcher,
				IntBinaryOperator edgeMatcher) {
			super(g1, g2, type, vertexMatcher, edgeMatcher);

			out1 = new int[n1];
			out2 = new int[n2];

			final int maxStateDepth = n1 + 1;
			stateT1OutSize = new int[maxStateDepth];
			stateT2OutSize = new int[maxStateDepth];
			newState(None, None, 0, 0);

			advance();
		}

		private void newState(int v1, int v2, int t1OutSize, int t2OutSize) {
			statePrevV1[stateDepth] = v1;
			statePrevV2[stateDepth] = v2;
			stateT1OutSize[stateDepth] = t1OutSize;
			stateT2OutSize[stateDepth] = t2OutSize;

			assert t1OutSize < n1;
			assert t2OutSize < n2;
			int nextV1;
			IntIterator nextV2Iter;
			/*
			 * "In case that only one of the in-terminal sets or only one of the out-terminal sets is empty, it can be
			 * demonstrated that the state s cannot be part of a matching, and it is not further explored." Although
			 * this is stated in the paper, this does not seem to be correct for non-full isomorphism.
			 */
			if (type == IsomorphismType.Full && (t1OutSize == 0 ^ t2OutSize == 0)) {
				nextV1 = None;
				nextV2Iter = IntIterators.EMPTY_ITERATOR;

			} else {
				if (t1OutSize != 0 && t2OutSize != 0) {
					/* P(s)=T^{out}_1 (s) \times \{\min T^{out}_2(s)\} */
					/* we use the opposite notation of G1 G2 than what used in the paper (G1 is the smaller graph) */
					nextV1 = IterTools.filter(range(n1).iterator(), u1 -> core1[u1] < 0 && out1[u1] > 0).nextInt();
					nextV2Iter = IterTools.filter(range(n2).iterator(), u2 -> core2[u2] < 0 && out2[u2] > 0);

				} else {
					/* P(s)=(N_1 - M_1(s)) \times \{\min (N_2 - M_2)\} */
					/* we use the opposite notation of G1 G2 than what used in the paper (G1 is the smaller graph) */
					nextV1 = IterTools.filter(range(n1).iterator(), u1 -> core1[u1] < 0).nextInt();
					nextV2Iter = IterTools.filter(range(n2).iterator(), u2 -> core2[u2] < 0);
				}
				assert nextV2Iter.hasNext();
			}
			stateNextV1[stateDepth] = nextV1;
			stateNextV2Iter[stateDepth] = nextV2Iter;
		}

		@Override
		void advance() {
			dfs: while (stateDepth > 0) {
				final int v1 = stateNextV1[stateDepth];
				for (IntIterator v2Iter = stateNextV2Iter[stateDepth]; v2Iter.hasNext();) {
					final int v2 = v2Iter.nextInt();
					if (!isFeasibleMatchVertices(v1, v2))
						continue;

					if (stateDepth == n1) {
						/* found a valid full matching */
						nextMapping = core1.clone();
						nextMapping[v1] = v2;
						return;
					}
					/* match v1 to v2 and update state */
					core1[v1] = v2;
					core2[v2] = v1;
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
			nextMapping = null;
		}

		private boolean isFeasibleMatchVertices(int v1, int v2) {
			/* check user custom matcher */
			if (!canMatchVertices(v1, v2))
				return false;

			/*
			 * check that out edges connecting v1 and other mapped vertices of g1 can be mapped to out edges connecting
			 * v2 and other mapped vertices of g2
			 */
			int visitIdx = nextVisitIdx++;
			int edgeCount = 0;
			for (IEdgeIter eit = g1.outEdges(v1).iterator(); eit.hasNext();) {
				int e1 = eit.nextInt();
				int w1 = eit.targetInt();
				int w2 = core1[w1];
				if (w2 < 0 && v1 != w1)
					continue;
				visit[w1] = visitIdx;
				visitData[w1] = e1;
				edgeCount++;
			}
			for (IEdgeIter eit = g2.outEdges(v2).iterator(); eit.hasNext();) {
				int e2 = eit.nextInt();
				int w2 = eit.targetInt();
				int w1 = core2[w2];
				if (w1 < 0 && v2 != w2)
					continue;
				if (v2 == w2)
					w1 = v1;
				if (visit[w1] == visitIdx) {
					int e1 = visitData[w1];
					if (!canMatchEdges(e1, e2))
						return false;
					edgeCount--;

				} else if (inducedSubGraph) {
					/* there is no edge (v1,w1) matching e2 */
					return false;
				}
			}
			if (edgeCount != 0)
				/* there are some edges connecting v1 in g1 without match in g2 */
				return false;

			return true;
		}

		private boolean isFeasibleCurrentState() {
			if (subGraph) {
				if (stateT1OutSize[stateDepth] > stateT2OutSize[stateDepth])
					return false;
			} else {
				if (stateT1OutSize[stateDepth] != stateT2OutSize[stateDepth])
					return false;
			}
			return true;
		}

		private void updateState(int v1, int v2) {
			final int depth = stateDepth - 1;
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

			newState(v1, v2, t1OutSize, t2OutSize);
		}

		private void popLastState() {
			final int prevV1 = statePrevV1[stateDepth];
			final int prevV2 = statePrevV2[stateDepth];
			if (prevV1 >= 0) {
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

			stateDepth--;
		}

	}

}
