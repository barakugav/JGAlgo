package com.ugav.algo;

import java.util.Iterator;
import java.util.NoSuchElementException;

abstract class GraphArrayAbstractOld<E> extends GraphAbstractOld<E> {

	int n;
	int m;

	@SuppressWarnings("rawtypes")
	static final Edge[][] EDGES_EMPTY = new Edge[0][];
	@SuppressWarnings("rawtypes")
	static final Edge[] EDGES_LIST_EMPTY = new Edge[0];
	static final int[] EDGES_LEN_EMPTY = new int[0];

	public GraphArrayAbstractOld(int n) {
		if (n < 0)
			throw new IllegalArgumentException();
		this.n = n;
		m = 0;
	}

	@Override
	public int vertices() {
		return n;
	}

	@Override
	public void clear() {
		clearEdges();
		n = 0;
	}

	abstract class EdgeItrBase implements Iterator<Edge<E>> {

		int u;
		int idx;
		Edge<E> toRemoveEdge;
		int toRemoveIdx;
		private final Edge<E>[][] edgesArr;

		EdgeItrBase(Edge<E>[][] edgesArr, int u) {
			this.edgesArr = edgesArr;
			this.u = u;
			idx = 0;
			toRemoveEdge = null;
		}

		@Override
		public Edge<E> next() {
			if (!hasNext())
				throw new NoSuchElementException();
			return toRemoveEdge = edgesArr[u][toRemoveIdx = idx++];
		}

		@Override
		public void remove() {
			if (toRemoveEdge == null)
				throw new IllegalStateException();
			removeEdge(toRemoveEdge, toRemoveIdx);
			m--;
			toRemoveEdge = null;
			idx--;
		}

		abstract void removeEdge(Edge<E> e, int edgeIdx);

	}

}
