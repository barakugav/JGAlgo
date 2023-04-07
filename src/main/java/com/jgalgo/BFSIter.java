package com.jgalgo;

import java.util.NoSuchElementException;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.longs.LongArrayFIFOQueue;
import it.unimi.dsi.fastutil.longs.LongPriorityQueue;

public class BFSIter implements IntIterator {

	private final Graph g;
	private final boolean[] visited;
	private final LongPriorityQueue queue;
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
		visited = new boolean[n];
		queue = new LongArrayFIFOQueue(n * 2);
		inEdge = -1;
		layer = -1;

		for (int source : sources) {
			visited[source] = true;
			queue.enqueue(toQueueEntry(source, -1));
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
		long entry = queue.dequeueLong();
		final int u = queueEntryToV(entry);
		inEdge = queueEntryToE(entry);
		if (u == firstVInLayer) {
			layer++;
			firstVInLayer = -1;
		}

		for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
			int e = eit.nextInt();
			int v = eit.v();
			if (visited[v])
				continue;
			visited[v] = true;
			queue.enqueue(toQueueEntry(v, e));
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

	private static long toQueueEntry(int v, int e) {
		return ((v & 0xffffffffL) << 32) | ((e & 0xffffffffL) << 0);
	}

	private static int queueEntryToV(long entry) {
		return (int) ((entry >> 32) & 0xffffffff);
	}

	private static int queueEntryToE(long entry) {
		return (int) ((entry >> 0) & 0xffffffff);
	}
}