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
import it.unimi.dsi.fastutil.ints.AbstractIntSet;
import it.unimi.dsi.fastutil.ints.Int2IntMap;
import it.unimi.dsi.fastutil.ints.Int2IntMaps;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;

abstract class GraphHashmapMultiAbstract extends GraphBaseMutable {

	int[] edgeNext;
	int[] edgePrev;
	private final DataContainer.Int edgeNextContainer;
	private final DataContainer.Int edgePrevContainer;

	static final Int2IntMap[] EMPTY_MAP_ARRAY = new Int2IntMap[0];
	static final Int2IntMap EmptyEdgeMap;

	static {
		Int2IntMap emptyEdgeMap = new Int2IntOpenHashMap(0);
		emptyEdgeMap.defaultReturnValue(-1);
		// emptyEdgeMap = Int2IntMaps.unmodifiable(emptyEdgeMap);
		EmptyEdgeMap = emptyEdgeMap;
	}

	GraphHashmapMultiAbstract(GraphBaseMutable.Capabilities capabilities, int expectedVerticesNum,
			int expectedEdgesNum) {
		super(capabilities, expectedVerticesNum, expectedEdgesNum);
		edgeNextContainer = newEdgesIntContainer(-1, newArr -> edgeNext = newArr);
		edgePrevContainer = newEdgesIntContainer(-1, newArr -> edgePrev = newArr);
	}

	GraphHashmapMultiAbstract(GraphBaseMutable.Capabilities capabilities, IndexGraph g, boolean copyVerticesWeights,
			boolean copyEdgesWeights) {
		super(capabilities, g, copyVerticesWeights, copyEdgesWeights);

		if (g instanceof GraphHashmapMultiAbstract) {
			GraphHashmapMultiAbstract g0 = (GraphHashmapMultiAbstract) g;

			edgeNextContainer = copyEdgesContainer(g0.edgeNextContainer, newArr -> edgeNext = newArr);
			edgePrevContainer = copyEdgesContainer(g0.edgePrevContainer, newArr -> edgePrev = newArr);
		} else {
			edgeNextContainer = newEdgesIntContainer(-1, newArr -> edgeNext = newArr);
			edgePrevContainer = newEdgesIntContainer(-1, newArr -> edgePrev = newArr);
		}
	}

	GraphHashmapMultiAbstract(GraphBaseMutable.Capabilities capabilities, IndexGraphBuilderImpl builder) {
		super(capabilities, builder);
		edgeNextContainer = newEdgesIntContainer(-1, newArr -> edgeNext = newArr);
		edgePrevContainer = newEdgesIntContainer(-1, newArr -> edgePrev = newArr);
	}

	@Override
	public void clearEdges() {
		edgeNextContainer.clear();
		edgePrevContainer.clear();
		super.clearEdges();
	}

	void removeAllEdgesInList(int firstEdge) {
		for (;;) {
			int nextEdge = edgeNext[firstEdge];
			if (nextEdge < 0) {
				removeEdge(firstEdge);
				break;
			}
			if (firstEdge == edges().size() - 1)
				firstEdge = nextEdge; /* the first edge index will be swapped with the removed edge */
			removeEdge(nextEdge);
		}
	}

	private abstract class EdgeIterBase implements IEdgeIter {

		// either the source or target of the iterator
		final int vertex;
		private Iterator<Int2IntMap.Entry> eit;
		private int nextEdge = -1;
		private int nextEndpoint;
		private int prevEdge = -1;
		int prevEndpoint;
		private Int2IntMap clonedMap;
		// private final Int2IntMap originalMap;

		EdgeIterBase(int vertex, Int2IntMap edges) {
			this.vertex = vertex;
			// originalMap = edges;
			eit = Int2IntMaps.fastIterator(edges);
			advance();
		}

		private void advance() {
			if (nextEdge >= 0)
				return;
			if (eit.hasNext()) {
				Int2IntMap.Entry entry = eit.next();
				nextEndpoint = entry.getIntKey();
				nextEdge = entry.getIntValue();
				assert nextEdge >= 0;
			}
		}

		@Override
		public boolean hasNext() {
			return nextEdge >= 0;
		}

		@Override
		public int nextInt() {
			Assertions.hasNext(this);
			prevEdge = nextEdge;
			prevEndpoint = nextEndpoint;
			nextEdge = edgeNext[prevEdge];
			advance();
			return prevEdge;
		}

		@Override
		public int peekNextInt() {
			Assertions.hasNext(this);
			return nextEdge;
		}

		@Override
		public void remove() {
			if (prevEdge < 0)
				throw new IllegalStateException();

			/*
			 * remove() is hard to implement due to two reasons: (1) if we want to provide peekNext(), we can not use
			 * HashMap.Iterator.remove(), as the map iterator already iterated over the next (peek) element, so we are
			 * forced to use HashMap.remove() which can not be used during iteration, so we clone the map. (2) If we
			 * choose to clone the map, it will not be updated when edges are swapped, so we have to update it manually.
			 */
			if (clonedMap == null) {
				clonedMap = new Int2IntOpenHashMap(0);
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
						if (GraphHashmapMultiAbstract.this.source(lastEdge) == vertex)
							clonedMap.replace(GraphHashmapMultiAbstract.this.target(lastEdge), lastEdge, prevEdge);
					} else {
						assert this instanceof EdgeIterIn;
						if (GraphHashmapMultiAbstract.this.target(lastEdge) == vertex)
							clonedMap.replace(GraphHashmapMultiAbstract.this.source(lastEdge), lastEdge, prevEdge);
					}
				} else {
					int lastSource = GraphHashmapMultiAbstract.this.source(lastEdge);
					int lastTarget = GraphHashmapMultiAbstract.this.target(lastEdge);
					if (lastSource == vertex) {
						clonedMap.replace(lastTarget, lastEdge, prevEdge);
					} else if (lastTarget == vertex) {
						clonedMap.replace(lastSource, lastEdge, prevEdge);
					}
				}
				if (nextEdge == lastEdge)
					nextEdge = prevEdge;
			}

			removeEdge(prevEdge);
			prevEdge = -1;
		}
	}

	final class EdgeIterOut extends EdgeIterBase {

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

	final class EdgeIterIn extends EdgeIterBase {

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

	abstract class SourceTargetEdgeSet extends AbstractIntSet implements IEdgeSet {

		final int source;
		final int target;

		SourceTargetEdgeSet(int source, int target) {
			this.source = source;
			this.target = target;
		}

		abstract Int2IntMap edgesMap(int source);

		@Override
		public int size() {
			int s = 0;
			for (int e = edgesMap(source).get(target); e >= 0; e = edgeNext[e])
				s++;
			return s;
		}

		@Override
		public boolean remove(int edge) {
			if (!contains(edge))
				return false;
			removeEdge(edge);
			return true;
		}

		@Override
		public void clear() {
			int firstEdge = edgesMap(source).get(target);
			if (firstEdge >= 0)
				removeAllEdgesInList(firstEdge);
		}

		@Override
		public IEdgeIter iterator() {
			return new IEdgeIter() {

				int nextEdge = edgesMap(source).get(target);
				int prevEdge;

				@Override
				public boolean hasNext() {
					return nextEdge >= 0;
				}

				@Override
				public int nextInt() {
					Assertions.hasNext(this);
					prevEdge = nextEdge;
					nextEdge = edgeNext[prevEdge];
					return prevEdge;
				}

				@Override
				public int peekNextInt() {
					Assertions.hasNext(this);
					return nextEdge;
				}

				@Override
				public int sourceInt() {
					return source;
				}

				@Override
				public int targetInt() {
					return target;
				}

				@Override
				public void remove() {
					if (prevEdge < 0)
						throw new IllegalStateException();
					int lastEdge = edges().size() - 1;
					if (nextEdge == lastEdge)
						nextEdge = prevEdge; /* the next edge index will be swapped with the removed edge */
					removeEdge(prevEdge);
				}
			};
		}
	}

	static Int2IntMap ensureEdgesMapMutable(Int2IntMap[] edgesMaps, int idx) {
		if (edgesMaps[idx] == EmptyEdgeMap) {
			edgesMaps[idx] = new Int2IntOpenHashMap(0);
			edgesMaps[idx].defaultReturnValue(-1);
		}
		return edgesMaps[idx];
	}

}
