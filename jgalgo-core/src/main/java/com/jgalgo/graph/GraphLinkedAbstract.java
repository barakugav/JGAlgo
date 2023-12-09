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

import com.jgalgo.internal.util.Assertions;

abstract class GraphLinkedAbstract extends GraphBaseMutable {

	GraphLinkedAbstract(GraphBaseMutable.Capabilities capabilities, int expectedVerticesNum, int expectedEdgesNum) {
		super(capabilities, expectedVerticesNum, expectedEdgesNum);
	}

	GraphLinkedAbstract(GraphBaseMutable.Capabilities capabilities, IndexGraph g, boolean copyVerticesWeights,
			boolean copyEdgesWeights) {
		super(capabilities, g, copyVerticesWeights, copyEdgesWeights);
	}

	GraphLinkedAbstract(GraphBaseMutable.Capabilities capabilities, IndexGraphBuilderImpl builder) {
		super(capabilities, builder);
	}

	abstract class EdgeItr implements IEdgeIter {

		private int next;
		int last;

		EdgeItr(int p) {
			this.next = p;
		}

		abstract int nextEdge(int n);

		@Override
		public boolean hasNext() {
			return next >= 0;
		}

		@Override
		public int nextInt() {
			Assertions.Iters.hasNext(this);
			next = nextEdge(last = next);
			return last;
		}

		@Override
		public int peekNextInt() {
			Assertions.Iters.hasNext(this);
			return next;
		}

		@Override
		public void remove() {
			if (last == -1)
				throw new IllegalStateException();
			if (last == edges.size - 1) {
				removeEdgeLast(last);
			} else {
				int s = edges.size - 1;
				edgeSwapAndRemove(last, s);
				if (next == s)
					next = last;
			}
			last = -1;
		}
	}

}
