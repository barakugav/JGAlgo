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
package com.jgalgo.alg.cover;

import static com.jgalgo.internal.util.Range.range;
import java.util.Arrays;
import com.jgalgo.alg.match.IMatching;
import com.jgalgo.alg.match.MatchingAlgo;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IWeightFunctionInt;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.graph.IndexGraphBuilder;
import com.jgalgo.graph.WeightFunction;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * A simply algorithm for computing a minimum weighted edge cover using a minimum weighted perfect matching algorithm.
 *
 * <p>
 * The algorithm uses a reduction to a minimum weighted perfect matching problem: a graph \(G'\) is constructed, by
 * duplicating the graph \(G\), namely duplicating each vertex \(v\) to two vertices \(v_0\) and \(v_1\) and adding an
 * edge between \(u_0\) and \(v_0\) and between \(u_1\) and \(v_1\) for each edge \((u, v)\) in \(G\). In addition,
 * between each pair of vertices \(v_0\) and \(v_1\) in \(G'\) an edge is added with weight equal to the minimum weight
 * of an edge incident to \(v\) in \(G\). The minimum weighted perfect matching in \(G'\) is computed and the edges
 * corresponding to the matching are added to the edge cover.
 *
 * <p>
 * The algorithm running time and space are dominated by the minimum weighted perfect matching algorithm used. Other
 * than that, the algorithm use linear space and time.
 *
 * @author Barak Ugav
 */
public class EdgeCoverWeighted extends EdgeCoverAbstract {

	private final MatchingAlgo matchingAlgo = MatchingAlgo.newInstance();

	/**
	 * Create a new weighted edge cover algorithm object.
	 *
	 * <p>
	 * Please prefer using {@link EdgeCover#newInstance()} to get a default implementation for the {@link EdgeCover}
	 * interface.
	 */
	public EdgeCoverWeighted() {}

	@Override
	protected IntSet computeMinimumEdgeCover(IndexGraph g, IWeightFunction w) {
		w = IWeightFunction.replaceNullWeightFunc(w);
		final int n = g.vertices().size();
		final int m = g.edges().size();

		IndexGraphBuilder gb = IndexGraphBuilder.undirected();
		gb.addVertices(range(n * 2));
		gb.ensureEdgeCapacity(m * 2 + n);
		for (int e : range(m)) {
			int u = g.edgeSource(e), v = g.edgeTarget(e);
			gb.addEdge(u * 2 + 0, v * 2 + 0);
			gb.addEdge(u * 2 + 1, v * 2 + 1);
		}
		final int vvEdgeThreshold = gb.edges().size();
		for (int v : range(n))
			gb.addEdge(v * 2 + 0, v * 2 + 1);
		IndexGraph g2 = gb.build();

		IWeightFunction w2;
		int[] minAdjacentEdge = new int[n];
		Arrays.fill(minAdjacentEdge, -1);
		if (WeightFunction.isInteger(w)) {
			IWeightFunctionInt wInt = (IWeightFunctionInt) w;
			int[] minAdjacentWeight = new int[n];
			Arrays.fill(minAdjacentWeight, Integer.MAX_VALUE);
			for (int e : range(m)) {
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				int ew = wInt.weightInt(e);
				if (minAdjacentWeight[u] > ew) {
					minAdjacentWeight[u] = ew;
					minAdjacentEdge[u] = e;
				}
				if (minAdjacentWeight[v] > ew) {
					minAdjacentWeight[v] = ew;
					minAdjacentEdge[v] = e;
				}
			}
			IWeightFunctionInt w2Int =
					e -> e < vvEdgeThreshold ? wInt.weightInt(e / 2) : 2 * minAdjacentWeight[e - vvEdgeThreshold];
			w2 = w2Int;

		} else {
			double[] minAdjacentWeight = new double[n];
			Arrays.fill(minAdjacentWeight, Double.POSITIVE_INFINITY);
			for (int e : range(m)) {
				int u = g.edgeSource(e), v = g.edgeTarget(e);
				double ew = w.weight(e);
				if (minAdjacentWeight[u] > ew) {
					minAdjacentWeight[u] = ew;
					minAdjacentEdge[u] = e;
				}
				if (minAdjacentWeight[v] > ew) {
					minAdjacentWeight[v] = ew;
					minAdjacentEdge[v] = e;
				}
			}
			IWeightFunction w0 = w;
			w2 = e -> e < vvEdgeThreshold ? w0.weight(e / 2) : 2 * minAdjacentWeight[e - vvEdgeThreshold];
		}
		for (int v : range(n))
			if (minAdjacentEdge[v] < 0)
				throw new IllegalArgumentException("no edge cover exists, vertex with index " + v + " has no edges");

		IMatching matching = (IMatching) matchingAlgo.computeMinimumPerfectMatching(g2, w2);
		Bitmap cover = new Bitmap(m);
		for (int e : range(m))
			if (matching.containsEdge(e * 2 + 0))
				cover.set(e);
		for (int v : range(n))
			if (matching.containsEdge(vvEdgeThreshold + v))
				cover.set(minAdjacentEdge[v]);
		return ImmutableIntArraySet.withBitmap(cover);
	}

}
