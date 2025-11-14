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
package com.jgalgo.graph;

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import java.util.Optional;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.JGAlgoUtils.Variant2;
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

abstract class GraphCsrAbstractUnindexed extends GraphCsrBase {

	final int[] edgesOut;

	final boolean fastLookup;
	private final Int2IntMap[] edgesLookupTable;
	private final int[] edgesLookupNext;

	GraphCsrAbstractUnindexed(boolean directed, IndexGraphBuilderImpl builder, ProcessedEdges processEdges,
			boolean fastLookup) {
		super(directed, Variant2.ofB(builder), processEdges, Optional.empty(), true, true);
		edgesOut = processEdges.edgesOut;

		this.fastLookup = fastLookup;
		edgesLookupTable = fastLookup ? new Int2IntMap[vertices().size()] : null;
		edgesLookupNext = fastLookup && containsParallelEdges() ? new int[edges().size()] : null;
		initLookupTables();
	}

	GraphCsrAbstractUnindexed(boolean directed, IndexGraph g, boolean copyVerticesWeights, boolean copyEdgesWeights,
			boolean fastLookup) {
		super(directed, g, copyVerticesWeights, copyEdgesWeights);

		if (g instanceof GraphCsrAbstractUnindexed) {
			GraphCsrAbstractUnindexed gCsr = (GraphCsrAbstractUnindexed) g;
			edgesOut = gCsr.edgesOut;

		} else {
			final int n = g.vertices().size();
			final int m = g.edges().size();
			int edgesOutArrLen;
			if (isDirected()) {
				edgesOutArrLen = m;
			} else {
				edgesOutArrLen = 0;
				for (int u : range(n))
					edgesOutArrLen += g.outEdges(u).size();
			}
			edgesOut = new int[edgesOutArrLen];

			int eIdx = 0;
			for (int u : range(n)) {
				edgesOutBegin[u] = eIdx;
				for (int e : g.outEdges(u))
					edgesOut[eIdx++] = e;
			}
			edgesOutBegin[n] = edgesOutArrLen;
		}

		this.fastLookup = fastLookup;
		if (!fastLookup) {
			edgesLookupTable = null;
			edgesLookupNext = null;

		} else if (g instanceof GraphCsrAbstractUnindexed && ((GraphCsrAbstractUnindexed) g).fastLookup) {
			edgesLookupTable = ((GraphCsrAbstractUnindexed) g).edgesLookupTable;
			edgesLookupNext = ((GraphCsrAbstractUnindexed) g).edgesLookupNext;

		} else {
			edgesLookupTable = new Int2IntMap[vertices().size()];
			edgesLookupNext = containsParallelEdges() ? new int[edges().size()] : null;
			initLookupTables();
		}
	}

	private void initLookupTables() {
		if (!fastLookup)
			return;

		final int n = vertices.size;
		final int m = edges.size;
		for (int u : range(n)) {
			int edgesNum = outEdges(u).size();
			if (edgesNum == 0) {
				edgesLookupTable[u] = EmptyEdgeMap;
			} else {
				edgesLookupTable[u] = new Int2IntOpenHashMap(edgesNum);
				edgesLookupTable[u].defaultReturnValue(-1);
			}
		}
		if (!containsParallelEdges()) {
			if (isDirected()) {
				for (int e : range(m)) {
					int u = source(e), v = target(e);
					int oldEdge = edgesLookupTable[u].put(v, e);
					assert oldEdge < 0;
				}
			} else {
				for (int e : range(m)) {
					int u = source(e), v = target(e);
					int oldEdge1 = edgesLookupTable[u].put(v, e);
					int oldEdge2 = edgesLookupTable[v].put(u, e);
					assert oldEdge1 < 0 && (oldEdge2 < 0 || (u == v && oldEdge2 == e));
				}
			}
		} else {
			Arrays.fill(edgesLookupNext, -1);
			if (isDirected()) {
				for (int e : range(m)) {
					int u = source(e), v = target(e);
					int oldEdge = edgesLookupTable[u].put(v, e);
					if (oldEdge >= 0)
						edgesLookupNext[e] = oldEdge;
				}
			} else {
				for (int e : range(m)) {
					int u = source(e), v = target(e);
					int firstEdge = edgesLookupTable[u].putIfAbsent(v, e);
					if (firstEdge < 0) {
						/* e is the first edge between u and v, update v's map */
						int oldVal = edgesLookupTable[v].put(u, e);
						assert oldVal < 0 || (u == v && oldVal == e);
					} else {
						/* keep firstEdge as head, insert new edge after it */
						edgesLookupNext[e] = edgesLookupNext[firstEdge];
						edgesLookupNext[firstEdge] = e;
					}
				}
			}
		}
	}

	int fastGetEdge(int source, int target) {
		return edgesLookupTable[source].get(target);
	}

	@Override
	public IEdgeSet getEdges(int source, int target) {
		if (!fastLookup)
			return super.getEdges(source, target);
		checkVertex(source);
		checkVertex(target);

		if (!containsParallelEdges()) {
			int edge = fastGetEdge(source, target);
			return new Graphs.EdgeSetSourceTargetSingleEdge(this, source, target, edge);
		}

		return new SourceTargetEdgesSetFastLookup(source, target);
	}

	private class SourceTargetEdgesSetFastLookup extends AbstractIntSet implements IEdgeSet {

		private final int source, target;

		SourceTargetEdgesSetFastLookup(int source, int target) {
			this.source = source;
			this.target = target;
		}

		@Override
		public int size() {
			int s = 0;
			for (int e = edgesLookupTable[source].get(target); e >= 0; e = edgesLookupNext[e])
				s++;
			return s;
		}

		@Override
		public boolean isEmpty() {
			return edgesLookupTable[source].get(target) < 0;
		}

		@Override
		public boolean contains(int key) {
			if (!edges().contains(key))
				return false;
			int s = source(key), t = target(key);
			return s == source && t == target || (!isDirected() && s == target && t == source);
		}

		@Override
		public IEdgeIter iterator() {
			return new SourceTargetEdgesIterFastLookup(source, target, edgesLookupTable[source].get(target),
					edgesLookupNext);
		}
	}

	private static class SourceTargetEdgesIterFastLookup implements EdgeIters.IBase {

		private final int source, target;
		private int edge;
		private final int[] edgeNext;

		SourceTargetEdgesIterFastLookup(int source, int target, int firstEdge, int[] edgeNext) {
			this.source = source;
			this.target = target;
			this.edge = firstEdge;
			this.edgeNext = edgeNext;
		}

		@Override
		public boolean hasNext() {
			return edge >= 0;
		}

		@Override
		public int nextInt() {
			Assertions.hasNext(this);
			int e = edge;
			edge = edgeNext[e];
			return e;
		}

		@Override
		public int peekNextInt() {
			Assertions.hasNext(this);
			return edge;
		}

		@Override
		public int sourceInt() {
			return source;
		}

		@Override
		public int targetInt() {
			return target;
		}
	}
}
