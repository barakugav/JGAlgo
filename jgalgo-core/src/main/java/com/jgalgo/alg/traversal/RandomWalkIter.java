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
package com.jgalgo.alg.traversal;

import java.util.Iterator;
import com.jgalgo.alg.RandomizedAlgorithm;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexIdMap;
import com.jgalgo.graph.IndexIdMaps;
import com.jgalgo.graph.IntGraph;
import com.jgalgo.graph.NoSuchVertexException;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctions;
import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * Random walk iterator.
 *
 * <p>
 * A random walk iterator is an iterator that starts at a given vertex and randomly choose an edge to traverse at each
 * step. The iterator returns the vertices visited by the random walk in the order they were visited. The iterator may
 * return the same vertex multiple times. The iterator can keep advancing as long as there are out going edges (with non
 * zero weight in the weighted case) from the current vertex. Each out going edge can be chosen with equal probability,
 * or with probability proportional to a given edge weight function.
 *
 * <p>
 * To get a deterministic random walk, set the iterator seed using {@link RandomWalkIter#setSeed(long)}.
 *
 * @see        <a href="https://en.wikipedia.org/wiki/Random_walk">Wikipedia</a>
 * @param  <V> the vertices type
 * @param  <E> the edges type
 * @author     Barak Ugav
 */
public interface RandomWalkIter<V, E> extends Iterator<V>, RandomizedAlgorithm {

	/**
	 * Check whether there is a vertex the iterator can advance to from the current vertex.
	 */
	@Override
	public boolean hasNext();

	/**
	 * Advance the iterator from the current vertex along one of its out edges and return the vertex reached.
	 */
	@Override
	public V next();

	/**
	 * Get the edge that led to the last vertex returned by {@link #next()}.
	 *
	 * <p>
	 * The behavior is undefined if {@link #next()} was not called yet.
	 *
	 * @return the edge that led to the last vertex returned by {@link #next()}
	 */
	public E lastEdge();

	/**
	 * A random walk iterator for {@link IntGraph}.
	 *
	 * @author Barak Ugav
	 */
	static interface Int extends RandomWalkIter<Integer, Integer>, IntIterator {

		/**
		 * Advance the iterator from the current vertex along one of its out edges and return the vertex reached.
		 */
		@Override
		public int nextInt();

		/**
		 * {@inheritDoc}
		 *
		 * @deprecated Please use the corresponding type-specific method instead.
		 */
		@Deprecated
		@Override
		default Integer next() {
			return IntIterator.super.next();
		}

		/**
		 * Get the edge that led to the last vertex returned by {@link #nextInt()}.
		 *
		 * <p>
		 * The behavior is undefined if {@link #nextInt()} was not called yet.
		 *
		 * @return the edge that led to the last vertex returned by {@link #nextInt()}
		 */
		public int lastEdgeInt();

		/**
		 * {@inheritDoc}
		 *
		 * @deprecated Please use {@link #lastEdgeInt()} instead to avoid un/boxing.
		 */
		@Deprecated
		@Override
		default Integer lastEdge() {
			return Integer.valueOf(lastEdgeInt());
		}
	}

	/**
	 * Create a new random walk iterator.
	 *
	 * <p>
	 * If the graph is an {@link IntGraph}, the returned iterator will be an instance of {@link RandomWalkIter.Int}.
	 *
	 * @param  <V>                   the vertices type
	 * @param  <E>                   the edges type
	 * @param  g                     a graph
	 * @param  source                a source vertex
	 * @return                       a new random walk iterator
	 * @throws NoSuchVertexException if {@code source} is not a vertex in {@code g}
	 */
	@SuppressWarnings("unchecked")
	static <V, E> RandomWalkIter<V, E> newInstance(Graph<V, E> g, V source) {
		if (g instanceof IndexGraph) {
			int src = ((Integer) source).intValue();
			return (RandomWalkIter<V, E>) new RandomWalkIters.UnweightedIndexIter((IndexGraph) g, src);

		} else {
			IndexGraph ig = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			int src = viMap.idToIndex(source);
			RandomWalkIter.Int indexIter = new RandomWalkIters.UnweightedIndexIter(ig, src);
			return RandomWalkIters.fromIndexIter(indexIter, g);
		}
	}

	/**
	 * Create a new weighted random walk iterator.
	 *
	 * <p>
	 * The iterator will choose the next vertex to advance to with probability proportional to the weight of the edge
	 * that led to it. The edges weights must be non negative.
	 *
	 * <p>
	 * If the graph is an {@link IntGraph}, the returned iterator will be an instance of {@link RandomWalkIter.Int}.
	 *
	 * @param  <V>                   the vertices type
	 * @param  <E>                   the edges type
	 * @param  g                     a graph
	 * @param  source                a source vertex
	 * @param  weights               an edge weight function
	 * @return                       a new weighted random walk iterator
	 * @throws NoSuchVertexException if {@code source} is not a vertex in {@code g}
	 */
	@SuppressWarnings("unchecked")
	static <V, E> RandomWalkIter<V, E> newInstance(Graph<V, E> g, V source, WeightFunction<E> weights) {
		if (WeightFunction.isCardinality(weights))
			return RandomWalkIter.newInstance(g, source);

		if (g instanceof IndexGraph) {
			int src = ((Integer) source).intValue();
			IWeightFunction w0 = WeightFunctions.asIntGraphWeightFunc((WeightFunction<Integer>) weights);
			return (RandomWalkIter<V, E>) new RandomWalkIters.WeightedIndexIter((IndexGraph) g, src, w0);

		} else {
			IndexGraph ig = g.indexGraph();
			IndexIdMap<V> viMap = g.indexGraphVerticesMap();
			IndexIdMap<E> eiMap = g.indexGraphEdgesMap();
			int src = viMap.idToIndex(source);
			IWeightFunction w0 = IndexIdMaps.idToIndexWeightFunc(weights, eiMap);
			RandomWalkIter.Int indexIter = new RandomWalkIters.WeightedIndexIter(ig, src, w0);
			return RandomWalkIters.fromIndexIter(indexIter, g);
		}
	}

}
