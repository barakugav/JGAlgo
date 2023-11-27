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
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

abstract class GraphHashmapMultiAbstract extends GraphBaseMutable {

	@SuppressWarnings("unchecked")
	static final Int2ObjectMap<int[]>[] EMPTY_MAP_ARRAY = new Int2ObjectMap[0];
	static final int[] EmptyEdgeArr = new int[] { 0 };
	static final Int2ObjectMap<int[]> EmptyEdgeMap;
	static {
		Int2ObjectMap<int[]> emptyEdgeMap = new Int2ObjectOpenHashMap<>(0);
		emptyEdgeMap.defaultReturnValue(EmptyEdgeArr);
		// emptyEdgeMap = Int2ObjectMaps.unmodifiable(emptyEdgeMap);
		EmptyEdgeMap = emptyEdgeMap;
	}

	GraphHashmapMultiAbstract(GraphBaseMutable.Capabilities capabilities, int expectedVerticesNum,
			int expectedEdgesNum) {
		super(capabilities, expectedVerticesNum, expectedEdgesNum);
	}

	GraphHashmapMultiAbstract(GraphBaseMutable.Capabilities capabilities, IndexGraph g, boolean copyVerticesWeights,
			boolean copyEdgesWeights) {
		super(capabilities, g, copyVerticesWeights, copyEdgesWeights);
	}

	GraphHashmapMultiAbstract(GraphBaseMutable.Capabilities capabilities, IndexGraphBuilderImpl builder) {
		super(capabilities, builder);
	}

	static int edgeIndexInArr(int[] edgesArr, int edge) {
		for (int edgesNum = edgesArr[0], i = 1;; i++) {
			assert i <= edgesNum;
			if (edgesArr[i] == edge)
				return i;
		}
	}

	private abstract class EdgeIterBase implements IEdgeIter {

		// either the source or target of the iterator
		final int vertex;

		private Iterator<Int2ObjectMap.Entry<int[]>> endpointIter;

		private int endpoint;
		private int[] endpointEdges;
		private int endpointEdgesNum;
		/* index in range [1, endpointEdgesNum], rather than the usual [0, endpointEdgesNum) */
		private int endpointEdgeIdx = 1;

		private int prevEdge = -1;
		int prevEndpoint = -1;
		private Int2ObjectMap<int[]> clonedMap;
		private final Int2ObjectMap<int[]> originalMap;

		EdgeIterBase(int vertex, Int2ObjectMap<int[]> edges) {
			this.vertex = vertex;
			originalMap = edges;
			endpointIter = Int2ObjectMaps.fastIterator(edges);
			advance();
		}

		private void advance() {
			if (endpointEdgeIdx <= endpointEdgesNum)
				return;
			if (endpointIter.hasNext()) {
				Int2ObjectMap.Entry<int[]> entry = endpointIter.next();
				endpoint = entry.getIntKey();
				endpointEdges = entry.getValue();
				endpointEdgesNum = endpointEdges[0];
				endpointEdgeIdx = 1;
				assert endpointEdgesNum > 0;
			} else {
				endpointEdgesNum = 0;
				endpointEdgeIdx = 1;
			}
		}

		@Override
		public boolean hasNext() {
			return endpointEdgeIdx <= endpointEdgesNum;
		}

		@Override
		public int nextInt() {
			Assertions.Iters.hasNext(this);
			prevEdge = endpointEdges[endpointEdgeIdx++];
			prevEndpoint = endpoint;
			advance();
			return prevEdge;
		}

		@Override
		public int peekNextInt() {
			Assertions.Iters.hasNext(this);
			return endpointEdges[endpointEdgeIdx];
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
				clonedMap = new Int2ObjectOpenHashMap<>(1);
				clonedMap.defaultReturnValue(EmptyEdgeArr);
				while (endpointIter.hasNext()) {
					Int2ObjectMap.Entry<int[]> entry = endpointIter.next();
					clonedMap.put(entry.getIntKey(), entry.getValue());
				}
				endpointIter = Int2ObjectMaps.fastIterator(clonedMap);
			}

			removeEdge(prevEdge);
			prevEdge = -1;

			if (endpointEdgeIdx > 1) {
				endpointEdgeIdx--;
				endpointEdges = originalMap.get(endpoint);
				endpointEdgesNum--;
				assert endpointEdgesNum == endpointEdges[0];
			}
		}
	}

	class EdgeIterOut extends EdgeIterBase {

		EdgeIterOut(int source, Int2ObjectMap<int[]> edges) {
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

		EdgeIterIn(int target, Int2ObjectMap<int[]> edges) {
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

	class SourceTargetEdgeSet extends AbstractIntSet implements IEdgeSet {

		final int source;
		final int target;
		int[] edgesArr;

		SourceTargetEdgeSet(int source, int target, Int2ObjectMap<int[]>[] edgesOut) {
			this.source = source;
			this.target = target;
			this.edgesArr = edgesOut[source].get(target);
		}

		@Override
		public int size() {
			return edgesArr[0];
		}

		@Override
		public boolean contains(int edge) {
			return 0 <= edge && edge < edges().size() && source == source(edge) && target == target(edge);
		}

		@Override
		public boolean remove(int edge) {
			if (!contains(edge))
				return false;
			removeEdge(edge);
			return true;
		}

		@Override
		public IEdgeIter iterator() {
			return new IEdgeIter() {

				int edgesNum = edgesArr[0];
				int edgeIdx = 1; /* index in range [1, edgesNum], rather than the usual [0, edgesNum) */

				@Override
				public boolean hasNext() {
					return edgeIdx <= edgesNum;
				}

				@Override
				public int nextInt() {
					Assertions.Iters.hasNext(this);
					return edgesArr[edgeIdx++];
				}

				@Override
				public int peekNextInt() {
					Assertions.Iters.hasNext(this);
					return edgesArr[edgeIdx];
				}

				@Override
				public int sourceInt() {
					return source;
				}

				@Override
				public int targetInt() {
					return target;
				}
			};
		}
	}

	static Int2ObjectMap<int[]> ensureEdgesMapMutable(Int2ObjectMap<int[]>[] edgesMaps, int idx) {
		if (edgesMaps[idx] == EmptyEdgeMap) {
			edgesMaps[idx] = new Int2ObjectOpenHashMap<>();
			edgesMaps[idx].defaultReturnValue(EmptyEdgeArr);
		}
		return edgesMaps[idx];
	}

}
