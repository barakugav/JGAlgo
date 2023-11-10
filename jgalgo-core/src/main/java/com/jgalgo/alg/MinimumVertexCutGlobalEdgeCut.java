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

import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Minimum Vertex-Cut algorithm using Global Minimum Edge-Cut algorithm.
 *
 * <p>
 * An auxiliary graph is constructed from the original graph \(G=(V,E)\) by replacing each vertex \(v\) with two
 * vertices \(v_0\) and \(v_1\) connected with an edge \((v_0,v_1)\), and each edge \(e=(u,v)\) with an edge with either
 * one or two edges. If the original graph is directed, then the edge \((u,v)\) is replaced with the edge \((u_1,v_0)\).
 * If the original graph is undirected, then the edge \((u,v)\) is replaced with the edges \((u_1,v_0)\) and
 * \((v_1,u_0)\). The edges connecting the vertices \(v_0\) and \(v_1\) are weighted with the weight of the vertex \(v\)
 * in the original graph. The edges connecting the vertices \(u_1\) and \(v_0\) are weighted with a large enough weight
 * such that the minimum edge-cut in the auxiliary graph will not contain any of these edges. The minimum edge-cut in
 * the auxiliary graph is computed using the {@link MinimumEdgeCutGlobal} algorithm. The minimum vertex-cut in the
 * original graph is the set of vertices corresponding to the edges in the minimum edge-cut in the auxiliary graph.
 *
 * @author Barak Ugav
 */
class MinimumVertexCutGlobalEdgeCut extends MinimumVertexCutUtils.AbstractImplGlobal {

	private final MinimumEdgeCutGlobal minEdgeCutAlgo = MinimumEdgeCutGlobal.newInstance();

	@Override
	IntSet computeMinimumCut(IndexGraph g, IWeightFunction w) {
		final int n = g.vertices().size();
		final int m = g.edges().size();

		Pair<IndexGraph, IWeightFunction> auxiliaryGraph = MinimumVertexCutUtils.auxiliaryGraph(g, w);
		IndexGraph g0 = auxiliaryGraph.first();
		IWeightFunction w0 = auxiliaryGraph.second();
		final int verticesEdgesThreshold = g.isDirected() ? m : 2 * m;

		IntSet edgeCut = (IntSet) minEdgeCutAlgo.computeMinimumCut(g0, w0).crossEdges();
		assert edgeCut.intStream().allMatch(e -> e >= verticesEdgesThreshold);

		int[] vertexCut = edgeCut.toIntArray();
		for (int i = 0; i < vertexCut.length; i++)
			vertexCut[i] -= verticesEdgesThreshold;

		Bitmap vertexCutBitmap = Bitmap.fromOnes(n, vertexCut);
		return new ImmutableIntArraySet(vertexCut) {
			@Override
			public boolean contains(int v) {
				return 0 <= v && v < n && vertexCutBitmap.get(v);
			}
		};
	}

}
