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
package com.jgalgo.alg.shortestpath;

import com.jgalgo.alg.common.IPath;
import com.jgalgo.alg.traversal.BfsIter;
import com.jgalgo.graph.IEdgeIter;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.NoSuchVertexException;
import com.jgalgo.internal.util.Assertions;
import com.jgalgo.internal.util.FIFOQueueIntNoReduce;
import com.jgalgo.internal.util.IntPair;
import it.unimi.dsi.fastutil.ints.Int2LongMap;
import it.unimi.dsi.fastutil.ints.Int2LongOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntArrays;
import it.unimi.dsi.fastutil.ints.IntLists;
import it.unimi.dsi.fastutil.ints.IntPriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectDoublePair;

/**
 * Compute the shortest path between a source and a target using bidirectional breadth-first search.
 *
 * <p>
 * The algorithm can be used with cardinality weight functions only, similar to
 * {@link ShortestPathSingleSourceCardinality}. Different from the single source algorithm, this algorithm uses two BFS
 * originated from the source and target vertices, instead of a single BFS. The algorithm terminate (roughly) when the
 * two BFS trees meet. In practice, this algorithm can be much faster than the single source algorithm, especially for
 * large graphs.
 *
 * @see    BfsIter
 * @author Barak Ugav
 */
public class ShortestPathStBidirectionalBfs extends ShortestPathStAbstract {

	/**
	 * Create a algorithm for computing a shortest path between a source and a target with respect to a cardinality
	 * weight function.
	 *
	 * <p>
	 * Please prefer using {@link ShortestPathSt#newInstance()} to get a default implementation for the
	 * {@link ShortestPathSt} interface.
	 */
	public ShortestPathStBidirectionalBfs() {}

	@Override
	protected ObjectDoublePair<IPath> computeShortestPathAndWeight(IndexGraph g, IWeightFunction w, int source,
			int target) {
		if (!g.vertices().contains(source))
			throw NoSuchVertexException.ofIndex(source);
		if (!g.vertices().contains(target))
			throw NoSuchVertexException.ofIndex(target);
		if (source == target)
			return ObjectDoublePair.of(IPath.valueOf(g, source, target, IntLists.emptyList()), 0);
		Assertions.onlyCardinality(w);

		final long InfoNone = info(-2, -1);
		Int2LongMap infoS = new Int2LongOpenHashMap();
		Int2LongMap infoT = new Int2LongOpenHashMap();
		infoS.defaultReturnValue(InfoNone);
		infoT.defaultReturnValue(InfoNone);
		infoS.put(source, info(-1, 0));
		infoT.put(target, info(-1, 0));
		IntPriorityQueue queueS = new FIFOQueueIntNoReduce();
		IntPriorityQueue queueT = new FIFOQueueIntNoReduce();
		queueS.enqueue(source);
		queueT.enqueue(target);

		int mu = Integer.MAX_VALUE;
		int middle = -1;
		while (!queueS.isEmpty() && !queueT.isEmpty()) {

			int uS = queueS.dequeueInt();
			int uT = queueT.dequeueInt();
			int uDistanceS = distance(infoS.get(uS));
			int uDistanceT = distance(infoT.get(uT));

			for (IEdgeIter eit = g.outEdges(uS).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.targetInt();
				if (infoS.containsKey(v))
					continue;
				int vDistanceS = uDistanceS + 1;
				long vInfoT = infoT.get(v);
				if (vInfoT != InfoNone) {
					if (mu > vDistanceS + distance(vInfoT)) {
						mu = vDistanceS + distance(vInfoT);
						middle = v;
					}
				}
				infoS.put(v, info(e, vDistanceS));
				queueS.enqueue(v);
			}
			for (IEdgeIter eit = g.inEdges(uT).iterator(); eit.hasNext();) {
				int e = eit.nextInt();
				int v = eit.sourceInt();
				if (infoT.containsKey(v))
					continue;
				int vDistanceT = uDistanceT + 1;
				long vInfoS = infoS.get(v);
				if (vInfoS != InfoNone) {
					if (mu > distance(vInfoS) + vDistanceT) {
						mu = distance(vInfoS) + vDistanceT;
						middle = v;
					}
				}
				infoT.put(v, info(e, vDistanceT));
				queueT.enqueue(v);
			}

			if (uDistanceS + uDistanceT >= mu)
				break;
		}

		if (middle < 0)
			return null;
		IntArrayList path = new IntArrayList();

		/* add edges from source to middle */
		if (g.isDirected()) {
			for (int u = middle, e; u != source; u = g.edgeSource(e))
				path.add(e = backtrack(infoS.get(u)));
		} else {
			for (int u = middle, e; u != source; u = g.edgeEndpoint(e, u))
				path.add(e = backtrack(infoS.get(u)));
		}
		IntArrays.reverse(path.elements(), 0, path.size());

		/* add edges from middle to target */
		if (g.isDirected()) {
			for (int u = middle, e; u != target; u = g.edgeTarget(e))
				path.add(e = backtrack(infoT.get(u)));
		} else {
			for (int u = middle, e; u != target; u = g.edgeEndpoint(e, u))
				path.add(e = backtrack(infoT.get(u)));
		}
		return ObjectDoublePair.of(IPath.valueOf(g, source, target, path), mu);
	}

	static long info(int backtrack, int distance) {
		return IntPair.of(backtrack, distance);
	}

	static int backtrack(long info) {
		return IntPair.first(info);
	}

	static int distance(long info) {
		return IntPair.second(info);
	}

}
