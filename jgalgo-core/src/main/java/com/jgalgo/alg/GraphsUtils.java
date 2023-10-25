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

package com.jgalgo.alg;

import java.util.Collection;
import com.jgalgo.graph.EdgeIter;
import com.jgalgo.graph.Graph;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.graph.WeightFunctionInt;
import it.unimi.dsi.fastutil.ints.IntCollection;
import it.unimi.dsi.fastutil.ints.IntIterable;
import it.unimi.dsi.fastutil.ints.IntIterator;

/**
 * Static methods class for {@linkplain Graph graphs}.
 *
 * @author Barak Ugav
 */
public class GraphsUtils {

	private GraphsUtils() {}

	static int[] calcDegree(IndexGraph g, IntCollection edges) {
		int[] degree = new int[g.vertices().size()];
		for (int e : edges) {
			degree[g.edgeSource(e)]++;
			degree[g.edgeTarget(e)]++;
		}
		return degree;
	}

	/**
	 * Check whether a graph contain self edges.
	 * <p>
	 * A self edge is an edge whose source and target is the same vertex.
	 *
	 * @param  g a graph
	 * @return   {@code true} if the graph contain at least one self edge, else {@code false}
	 */
	public static boolean containsSelfEdges(Graph g) {
		if (!g.getCapabilities().selfEdges())
			return false;
		IndexGraph ig = g.indexGraph();
		for (int n = ig.vertices().size(), u = 0; u < n; u++) {
			for (EdgeIter eit = ig.outEdges(u).iterator(); eit.hasNext();) {
				eit.nextInt();
				if (u == eit.target())
					return true;
			}
		}
		return false;
	}

	/**
	 * Check whether a graph contain parallel edges.
	 * <p>
	 * Two parallel edges are edges that have the same source and target vertices.
	 *
	 * @param  g a graph
	 * @return   {@code true} if the graph contain at least one pair of parallel edges, else {@code false}
	 */
	public static boolean containsParallelEdges(Graph g) {
		if (!g.getCapabilities().parallelEdges())
			return false;
		IndexGraph ig = g.indexGraph();
		int n = ig.vertices().size();
		int[] lastVisit = new int[n];
		for (int u = 0; u < n; u++) {
			final int visitIdx = u + 1;
			for (EdgeIter eit = ig.outEdges(u).iterator(); eit.hasNext();) {
				eit.nextInt();
				int v = eit.target();
				if (lastVisit[v] == visitIdx)
					return true;
				lastVisit[v] = visitIdx;
			}
		}
		return false;
	}

	static double weightSum(IntIterable collection, WeightFunction w) {
		if (w == null || w == WeightFunction.CardinalityWeightFunction && collection instanceof Collection<?>)
			return ((Collection<?>) collection).size();
		return weightSum(collection.iterator(), w);
	}

	static double weightSum(IntIterator it, WeightFunction w) {
		if (w == null || w == WeightFunction.CardinalityWeightFunction) {
			int cardinality = 0;
			for (; it.hasNext(); it.nextInt())
				cardinality++;
			return cardinality;
		}

		if (w instanceof WeightFunctionInt) {
			WeightFunctionInt wInt = (WeightFunctionInt) w;
			int sum = 0;
			while (it.hasNext())
				sum += wInt.weightInt(it.nextInt());
			return sum;

		} else {
			double sum = 0;
			while (it.hasNext())
				sum += w.weight(it.nextInt());
			return sum;
		}
	}

}
