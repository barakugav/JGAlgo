package com.jgalgo;

import java.util.NoSuchElementException;
import java.util.Random;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntList;

abstract class RandomWalkIter implements IntIterator {

	final Graph g;
	private final int[][] edges;
	private int v;
	private final Random rand;

	private RandomWalkIter(Graph g, int source, Random rand) {
		this.g = g;
		edges = new int[g.vertices().size()][];
		v = source;
		this.rand = rand;
		edges[v] = getEdges(v);
	}

	public static RandomWalkIter createWithAllEdges(Graph g, int source) {
		return new IterWithAllEdges(g, source, new Random());
	}

	public static RandomWalkIter createWithAllEdges(Graph g, int source, long seed) {
		return new IterWithAllEdges(g, source, new Random(seed));
	}

	public static RandomWalkIter createWithoutSelfLoops(Graph g, int source) {
		return new IterWithoutSelfLoops(g, source, new Random());
	}

	public static RandomWalkIter createWithoutSelfLoops(Graph g, int source, long seed) {
		return new IterWithoutSelfLoops(g, source, new Random(seed));
	}

	public static RandomWalkIter createWithUniformNeighborDist(Graph g, int source) {
		return new IterUniformNeighborDist(g, source, new Random());
	}

	public static RandomWalkIter createWithUniformNeighborDist(Graph g, int source, long seed) {
		return new IterUniformNeighborDist(g, source, new Random(seed));
	}

	@Override
	public boolean hasNext() {
		return edges[v].length == 0;
	}

	@Override
	public int nextInt() {
		if (!hasNext())
			throw new NoSuchElementException();
		int[] es = edges[v];
		int eIdx = rand.nextInt(es.length);
		int e = es[eIdx];
		int ret = v;
		v = g.edgeEndpoint(e, v);
		if (edges[v] == null)
			edges[v] = getEdges(v);
		return ret;
	}

	abstract int[] getEdges(int u);

	private static class IterWithAllEdges extends RandomWalkIter {
		IterWithAllEdges(Graph g, int source, Random rand) {
			super(g, source, rand);
		}

		@Override
		int[] getEdges(int u) {
			IntList es = new IntArrayList();
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();)
				es.add(eit.nextInt());
			return es.toIntArray();
		}
	}

	private static class IterWithoutSelfLoops extends RandomWalkIter {
		IterWithoutSelfLoops(Graph g, int source, Random rand) {
			super(g, source, rand);
		}

		@Override
		int[] getEdges(int u) {
			IntList es = new IntArrayList();
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				int e = eit.nextInt();
				if (g.edgeEndpoint(e, u) != u)
					es.add(e);
			}
			return es.toIntArray();
		}
	}

	private static class IterUniformNeighborDist extends RandomWalkIter {
		private final int[] visits;
		private int lastVisitIdx;

		IterUniformNeighborDist(Graph g, int source, Random rand) {
			super(g, source, rand);
			visits = new int[g.vertices().size()];
		}

		@Override
		int[] getEdges(int u) {
			int visitIdx = ++lastVisitIdx;
			IntList es = new IntArrayList();
			for (EdgeIter eit = g.edgesOut(u); eit.hasNext();) {
				int e = eit.nextInt();
				int v = g.edgeEndpoint(e, u);
				if (visits[v] == visitIdx)
					continue; /* already added */
				visits[v] = visitIdx;
				es.add(e);
			}
			return es.toIntArray();
		}
	}

}
