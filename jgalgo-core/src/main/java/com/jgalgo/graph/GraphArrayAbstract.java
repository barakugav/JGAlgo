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

import java.util.Arrays;
import com.jgalgo.internal.util.Assertions;

abstract class GraphArrayAbstract extends GraphBaseMutable {

	GraphArrayAbstract(GraphBaseMutable.Capabilities capabilities, int expectedVerticesNum, int expectedEdgesNum) {
		super(capabilities, expectedVerticesNum, expectedEdgesNum);
	}

	GraphArrayAbstract(GraphBaseMutable.Capabilities capabilities, IndexGraph g, boolean copyVerticesWeights,
			boolean copyEdgesWeights) {
		super(capabilities, g, copyVerticesWeights, copyEdgesWeights);
	}

	GraphArrayAbstract(GraphBaseMutable.Capabilities capabilities, IndexGraphBuilderImpl builder) {
		super(capabilities, builder);
	}

	static void addEdgeToList(int[][] edges, int[] edgesNum, int w, int e) {
		int[] es = edges[w];
		int num = edgesNum[w];
		if (es.length <= num) {
			es = Arrays.copyOf(es, Math.max(es.length * 2, 2));
			edges[w] = es;
		}
		es[num] = e;
		edgesNum[w] = num + 1;
	}

	static int edgeIndexOf(int[] edges, int edgesNum, int e) {
		for (int i = 0;; i++) {
			assert i < edgesNum;
			if (edges[i] == e)
				return i;
		}
	}

	static void removeEdgeFromList(int[][] edges, int[] edgesNum, int w, int e) {
		int[] es = edges[w];
		int num = edgesNum[w];
		int i = edgeIndexOf(es, num, e);
		es[i] = es[num - 1];
		edgesNum[w] = num - 1;
	}

	private abstract class EdgeIterBase implements IEdgeIter {

		private final int[] edges;
		private int count;
		private int idx;
		int lastEdge = -1;

		EdgeIterBase(int[] edges, int count) {
			this.edges = edges;
			this.count = count;
		}

		@Override
		public boolean hasNext() {
			return idx < count;
		}

		@Override
		public int nextInt() {
			Assertions.Iters.hasNext(this);
			return lastEdge = edges[idx++];
		}

		@Override
		public int peekNextInt() {
			Assertions.Iters.hasNext(this);
			return edges[idx];
		}

		@Override
		public void remove() {
			if (lastEdge == -1)
				throw new IllegalStateException();
			removeEdge(lastEdge);
			/*
			 * The edge will be removed from entry idx-1 in edges[], go back and decrease the edges count.
			 */
			count--;
			idx--;
			lastEdge = -1;
		}

	}

	abstract class EdgeIterOut extends EdgeIterBase {

		final int source;

		EdgeIterOut(int source, int[] edges, int count) {
			super(edges, count);
			this.source = source;
		}

		@Override
		public int sourceInt() {
			return source;
		}
	}

	abstract class EdgeIterIn extends EdgeIterBase {

		final int target;

		EdgeIterIn(int target, int[] edges, int count) {
			super(edges, count);
			this.target = target;
		}

		@Override
		public int targetInt() {
			return target;
		}
	}

}
