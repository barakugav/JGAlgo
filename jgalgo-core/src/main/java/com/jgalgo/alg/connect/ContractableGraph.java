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
package com.jgalgo.alg.connect;

import static com.jgalgo.internal.util.Range.range;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.ds.LinkedList;
import com.jgalgo.internal.ds.UnionFind;
import com.jgalgo.internal.util.Assertions;
import it.unimi.dsi.fastutil.ints.IntIterator;

class ContractableGraph {

	private final IndexGraph g;
	private final UnionFind uf;
	private final int[] findToSuperV;
	private final LinkedList.Singly vs;
	private final int[] head;
	private final int[] tail;
	private int numV;

	ContractableGraph(IndexGraph g) {
		Assertions.onlyUndirected(g);
		this.g = g;
		final int n = g.vertices().size();
		uf = UnionFind.newInstance();
		uf.makeMany(n);
		vs = new LinkedList.Singly(n);
		findToSuperV = range(n).toIntArray();
		head = range(n).toIntArray();
		tail = range(n).toIntArray();
		numV = n;
	}

	int numberOfSuperVertices() {
		return numV;
	}

	void contract(int U, int V) {
		checkSuperVertex(U);
		checkSuperVertex(V);
		int uTail = tail[U], vHead = head[V];
		if (uf.find(uTail) == uf.find(vHead))
			throw new IllegalArgumentException("U and V are already in the same super vertices");

		findToSuperV[uf.union(uTail, vHead)] = U;
		vs.setNext(uTail, vHead);
		tail[U] = tail[V];
		numV--;

		// remove V
		if (V != numV) {
			findToSuperV[uf.find(head[numV])] = V;
			head[V] = head[numV];
			tail[V] = tail[numV];
		}
	}

	IntIterator superVertexVertices(int U) {
		checkSuperVertex(U);
		return vs.iterator(head[U]);
	}

	ContractableGraph.EdgeIter outEdges(int U) {
		checkSuperVertex(U);
		return new ContractableGraph.EdgeIter() {

			final IntIterator vit = superVertexVertices(U);
			com.jgalgo.graph.IEdgeIter eit;
			int nextEdge = -1;
			int nextSourceOrig = -1, nextTargetOrig = -1;
			int sourceOrig = -1, targetOrig = -1;
			int nextTarget = -1;
			int target = -1;

			{
				if (vit.hasNext()) {
					eit = g.outEdges(vit.nextInt()).iterator();
					eitAdvance();
				}
			}

			void eitAdvance() {
				for (;;) {
					while (eit.hasNext()) {
						int e = eit.nextInt();
						int s = g.edgeSource(e), t = g.edgeTarget(e);
						int S = findToSuperV[uf.find(s)], T = findToSuperV[uf.find(t)];
						if (S != T) {
							nextEdge = e;
							if (U == S) {
								nextSourceOrig = s;
								nextTargetOrig = t;
								nextTarget = T;
							} else {
								assert U == T;
								nextSourceOrig = t;
								nextTargetOrig = s;
								nextTarget = S;
							}
							return;
						}
					}
					if (!vit.hasNext()) {
						nextEdge = -1;
						return;
					}
					eit = g.outEdges(vit.nextInt()).iterator();
				}
			}

			@Override
			public boolean hasNext() {
				return nextEdge >= 0;
			}

			@Override
			public int nextInt() {
				Assertions.hasNext(this);
				final int e = nextEdge;
				sourceOrig = nextSourceOrig;
				targetOrig = nextTargetOrig;
				target = nextTarget;
				eitAdvance();
				return e;
			}

			@Override
			public int source() {
				return U;
			}

			@Override
			public int target() {
				return target;
			}

			@Override
			public int sourceOriginal() {
				return sourceOrig;
			}

			@Override
			public int targetOriginal() {
				return targetOrig;
			}
		};
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append('{');

		boolean firstVertex = true;
		for (int U : range(numV)) {
			if (firstVertex) {
				firstVertex = false;
			} else {
				s.append(", ");
			}

			s.append('V').append(U).append('<');
			for (IntIterator it = superVertexVertices(U);;) {
				s.append(it.nextInt());
				if (!it.hasNext())
					break;
				s.append(',').append(' ');
			}
			s.append('>');

			s.append(": [");
			boolean firstEdge = true;
			for (ContractableGraph.EdgeIter eit = outEdges(U); eit.hasNext();) {
				int e = eit.nextInt();
				if (firstEdge) {
					firstEdge = false;
				} else {
					s.append(", ");
				}
				s.append(e).append('(');
				s.append(eit.sourceOriginal()).append("(").append(eit.source()).append("), ");
				s.append(eit.targetOriginal()).append("(").append(eit.target()).append("))");
			}
			s.append(']');
		}
		s.append('}');
		return s.toString();
	}

	private void checkSuperVertex(int U) {
		Assertions.checkVertex(U, numV);
	}

	static interface EdgeIter extends IntIterator {
		int source();

		int target();

		int sourceOriginal();

		int targetOriginal();
	}

}
