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
package com.jgalgo;

import java.util.NoSuchElementException;
import it.unimi.dsi.fastutil.ints.IntIterator;

class ContractableGraph {

	private final Graph g;
	private final UnionFind uf;
	private final int[] findToSuperV;
	private final LinkedListFixedSize.Singly vs;
	private final int[] head;
	private final int[] tail;
	private int numV;

	ContractableGraph(Graph g) {
		ArgumentCheck.onlyUndirected(g);
		this.g = g;
		int n = g.vertices().size();
		uf = UnionFind.newBuilder().expectedSize(n).build();
		vs = new LinkedListFixedSize.Singly(n);
		findToSuperV = new int[n];
		head = new int[n];
		tail = new int[n];
		for (int v = 0; v < n; v++)
			findToSuperV[uf.make()] = head[v] = tail[v] = v;
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
			throw new IllegalArgumentException();

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

	ContractableGraph.EdgeIter edgesOut(int U) {
		checkSuperVertex(U);
		return new ContractableGraph.EdgeIter() {

			final IntIterator vit = superVertexVertices(U);
			com.jgalgo.EdgeIterImpl eit = com.jgalgo.EdgeIterImpl.Empty;
			int source = -1, target = -1;

			boolean eitAdvance() {
				for (; eit.hasNext(); eit.nextInt()) {
					int e = eit.peekNext();
					if (uf.find(g.edgeSource(e)) != uf.find(g.edgeTarget(e)))
						return true;
				}
				return false;
			}

			@Override
			public boolean hasNext() {
				if (eit.hasNext())
					return true;
				for (; vit.hasNext();) {
					eit = (com.jgalgo.EdgeIterImpl) g.edgesOut(vit.nextInt());
					if (eitAdvance())
						return true;
				}
				return false;
			}

			@Override
			public int nextInt() {
				if (!hasNext())
					throw new NoSuchElementException();
				int e = eit.nextInt();
				source = eit.source();
				target = eit.target();
				eitAdvance();
				return e;
			}

			@Override
			public int source() {
				return findToSuperV[uf.find(source)];
			}

			@Override
			public int target() {
				return findToSuperV[uf.find(target)];
			}

			@Override
			public int sourceOriginal() {
				return source;
			}

			@Override
			public int targetOriginal() {
				return target;
			}
		};
	}

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		s.append('{');

		boolean firstVertex = true;
		for (int U = 0; U < numV; U++) {
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
			for (ContractableGraph.EdgeIter eit = edgesOut(U); eit.hasNext();) {
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
		if (!(0 <= U && U < numV))
			throw new IndexOutOfBoundsException(U);
	}

	static interface EdgeIter extends com.jgalgo.EdgeIter {
		int sourceOriginal();

		int targetOriginal();
	}

}
