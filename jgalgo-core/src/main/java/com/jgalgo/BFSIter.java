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

import java.util.BitSet;
import java.util.NoSuchElementException;

import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;

/**
 * Bread first search (BFS) iterator.
 * <p>
 * The BFS iterator is used to iterate over the vertices of a graph in a bread first manner, namely by the cardinality
 * distance of the vertices from some source(s) vertex. The iterator will visit every vertex \(v\) for which there is a
 * path from the source(s) to \(v\). Each such vertex will be visited exactly once.
 * <p>
 * The graph should not be modified during the BFS iteration.
 *
 * <pre> {@code
 * Graph g = ...;
 * int sourceVertex = ...;
 * for (BFSIter iter = new BFSIter(g, sourceVertex); iter.hasNext();) {
 *     int v = iter.nextInt();
 *     int e = iter.inEdge();
 *     int layer = iter.layer();
 *     System.out.println("Reached vertex " + v + " at layer " + layer + " using edge " + e);
 * }
 * }</pre>
 *
 * @see    DFSIter
 * @see    <a href= "https://en.wikipedia.org/wiki/Breadth-first_search">Wikipedia</a>
 * @author Barak Ugav
 */
public class BFSIter implements IntIterator {

	private final Graph g;
	private final BitSet visited;
	private final IntPriorityQueue queue;
	private int inEdge;
	private int layer;
	private int firstVInLayer;

	/**
	 * Create a BFS iterator rooted at a single source vertex.
	 *
	 * @param g      a graph
	 * @param source a vertex in the graph from which the search will start from.
	 */
	public BFSIter(Graph g, int source) {
		this(g, new int[] { source });
	}

	/**
	 * Create a BFS iterator rooted at multiple sources vertices.
	 *
	 * @param  g                        a graph
	 * @param  sources                  multiple sources vertices in the graph from which the search will start from.
	 * @throws IllegalArgumentException if the sources array is empty
	 */
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

	/**
	 * Check whether there is more vertices to iterate over.
	 */
	@Override
	public boolean hasNext() {
		return !queue.isEmpty();
	}

	/**
	 * Advance the iterator and return a vertex that was not visited by the iterator yet.
	 */
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

	/**
	 * Get the edge that led to the last vertex returned by {@link nextInt}.
	 * <p>
	 * The behavior is undefined if {@link nextInt} was not called yet.
	 *
	 * @return the edge that led to the last vertex returned by {@link nextInt}
	 */
	public int inEdge() {
		return inEdge;
	}

	/**
	 * Get the layer of the last vertex returned by {@link nextInt}.
	 * <p>
	 * The layer of a vertex is the cardinality distance, the number of edges in the path, from the source(s) to the
	 * vertex.
	 * <p>
	 * The behavior is undefined if {@link nextInt} was not called yet.
	 *
	 * @return the layer of the last vertex returned by {@link nextInt}.
	 */
	public int layer() {
		return layer;
	}
}
