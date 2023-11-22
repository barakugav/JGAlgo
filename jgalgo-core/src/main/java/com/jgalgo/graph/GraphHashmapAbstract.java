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

import java.util.Iterator;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

abstract class GraphHashmapAbstract extends GraphBaseWithEdgeEndpointsContainer {

	static final Int2IntMap[] EMPTY_MAP_ARRAY = new Int2IntMap[0];

	GraphHashmapAbstract(GraphBaseMutable.Capabilities capabilities, int expectedVerticesNum, int expectedEdgesNum) {
		super(capabilities, expectedVerticesNum, expectedEdgesNum);
	}

	GraphHashmapAbstract(GraphBaseMutable.Capabilities capabilities, IndexGraph g, boolean copyVerticesWeights,
			boolean copyEdgesWeights) {
		super(capabilities, g, copyVerticesWeights, copyEdgesWeights);
	}

	GraphHashmapAbstract(GraphBaseMutable.Capabilities capabilities, IndexGraphBuilderImpl builder) {
		super(capabilities, builder);
	}

	@Override
	public IEdgeSet getEdges(int source, int target) {
		int edge = getEdge(source, target);
		return new Graphs.EdgeSetSourceTargetSingleton(this, source, target, edge);
	}

	private abstract class EdgeIterBase implements IEdgeIter {

		// either the source or target of the iterator
		final int vertex;
		Iterator<Int2IntMap.Entry> eit;
		int nextEdge = -1;
		int nextEndpoint = -1;
		int prevEdge = -1;
		int prevEndpoint = -1;
		Int2IntMap clonedMap;

		EdgeIterBase(int vertex, Int2IntMap edges) {
			this.vertex = vertex;
			eit = Int2IntMaps.fastIterator(edges);
			advance();
		}

		private void advance() {
			if (eit.hasNext()) {
				Int2IntMap.Entry entry = eit.next();
				nextEndpoint = entry.getIntKey();
				nextEdge = entry.getIntValue();
			} else {
				nextEdge = -1;
				nextEndpoint = -1;
			}
		}

		@Override
		public boolean hasNext() {
			return nextEdge != -1;
		}

		@Override
		public int nextInt() {
			Assertions.Iters.hasNext(this);
			prevEdge = nextEdge;
			prevEndpoint = nextEndpoint;
			advance();
			return prevEdge;
		}

		@Override
		public int peekNextInt() {
			Assertions.Iters.hasNext(this);
			return nextEdge;
		}

		@Override
		public void remove() {
			if (prevEdge == -1)
				throw new IllegalStateException();

			/*
			 * remove() is hard to implement due to two reasons: (1) if we want to provide peekNext(), we can not use
			 * HashMap.Iterator.remove(), as the map iterator already iterated over the next (peek) element, so we are
			 * forced to use HashMap.remove() which can not be used during iteration, so we clone the map. (2) If we
			 * choose to clone the map, it will not be updated when edges are swapped, so we have to update it manually.
			 */
			if (clonedMap == null) {
				clonedMap = new Int2IntOpenHashMap(1);
				clonedMap.defaultReturnValue(-1);
				while (eit.hasNext()) {
					Int2IntMap.Entry entry = eit.next();
					clonedMap.put(entry.getIntKey(), entry.getIntValue());
				}
				eit = Int2IntMaps.fastIterator(clonedMap);
			}
			int lastEdge = edges().size() - 1;
			if (prevEdge != lastEdge) {
				if (isDirected()) {
					if (this instanceof EdgeIterOut) {
						if (edgeSource(lastEdge) == vertex) {
							int oldVal = clonedMap.replace(edgeTarget(lastEdge), prevEdge);
							assert oldVal == -1 || oldVal == lastEdge;
						}
					} else {
						assert this instanceof EdgeIterIn;
						if (edgeTarget(lastEdge) == vertex) {
							int oldVal = clonedMap.replace(edgeSource(lastEdge), prevEdge);
							assert oldVal == -1 || oldVal == lastEdge;
						}
					}
				} else {
					int lastSource = edgeSource(lastEdge), lastTarget = edgeTarget(lastEdge);
					if (lastSource == vertex) {
						int oldVal = clonedMap.replace(lastTarget, prevEdge);
						assert oldVal == -1 || oldVal == lastEdge;
					} else if (lastTarget == vertex) {
						int oldVal = clonedMap.replace(lastSource, prevEdge);
						assert oldVal == -1 || oldVal == lastEdge;
					}
				}
				if (nextEdge == lastEdge)
					nextEdge = prevEdge;
			}

			removeEdge(prevEdge);
			prevEdge = -1;
		}
	}

	class EdgeIterOut extends EdgeIterBase {

		EdgeIterOut(int source, Int2IntMap edges) {
			super(source, edges);
		}

		@Override
		public int sourceInt() {
			return vertex;
		}

		@Override
		public int targetInt() {
			return prevEndpoint;
		}
	}

	class EdgeIterIn extends EdgeIterBase {

		EdgeIterIn(int target, Int2IntMap edges) {
			super(target, edges);
		}

		@Override
		public int sourceInt() {
			return prevEndpoint;
		}

		@Override
		public int targetInt() {
			return vertex;
		}
	}

	static Int2IntMap ensureEdgesMapMutable(Int2IntMap[] edgesArr, int idx) {
		if (edgesArr[idx] == JGAlgoUtils.EMPTY_INT2INT_MAP_DEFVAL_NEG_ONE) {
			edgesArr[idx] = new Int2IntOpenHashMap();
			edgesArr[idx].defaultReturnValue(-1);
		}
		return edgesArr[idx];
	}

}
