package com.jgalgo;

import java.util.BitSet;
import java.util.NoSuchElementException;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

public class BFSIter implements IntIterator {

	private final Graph g;
	private final BitSet visited;
	private final IntPriorityQueue queue;
	private int inEdge;
	private int layer;
	private int firstVInLayer;

	public BFSIter(Graph g, int source) {
		this(g, new int[] { source });
	}

	public BFSIter(Graph g, int[] sources) {
		if (sources.length == 0)
			throw new IllegalArgumentException();
		this.g = g;
		int n = g.vertices().size();
		visited = new BitSet(n);
		queue = new IntArrayFIFOQueue();
		inEdge = -1;
		layer = -1;

		for (int source : sources) {
			visited.set(source);
			queue.enqueue(source);
			queue.enqueue(-1);
		}
		firstVInLayer = sources[0];
	}

	@Override
	public boolean hasNext() {
		return !queue.isEmpty();
	}

	@Override
	public int nextInt() {
		if (!hasNext())
			throw new NoSuchElementException();
		final int u = queue.dequeueInt();
		inEdge = queue.dequeueInt();
		if (u == firstVInLayer) {
			layer++;
			firstVInLayer = -1;
		}

		for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
			int e = eit.nextInt();
			int v = eit.v();
			if (visited.get(v))
				continue;
			visited.set(v);
			queue.enqueue(v);
			queue.enqueue(e);
			if (firstVInLayer == -1)
				firstVInLayer = v;
		}

		return u;
	}

	public int inEdge() {
		return inEdge;
	}

	public int layer() {
		return layer;
	}
}