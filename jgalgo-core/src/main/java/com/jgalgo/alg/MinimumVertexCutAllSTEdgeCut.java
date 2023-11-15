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

import java.util.Iterator;
import com.jgalgo.alg.MinimumVertexCutUtils.AuxiliaryGraph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * All Minimum Vertex-Cuts algorithm with terminal vertices (source-sink, S-T) using All Minimum Edge-Cuts algorithm.
 *
 * <p>
 * An auxiliary graph is constructed from the original graph \(G=(V,E)\) by replacing each vertex \(v\) with two
 * vertices \(v_0\) and \(v_1\) connected with an edge \((v_0,v_1)\), and each edge \(e=(u,v)\) with an edge with either
 * one or two edges. If the original graph is directed, then the edge \((u,v)\) is replaced with the edge \((u_1,v_0)\).
 * If the original graph is undirected, then the edge \((u,v)\) is replaced with the edges \((u_1,v_0)\) and
 * \((v_1,u_0)\). The edges connecting the vertices \(v_0\) and \(v_1\) are weighted with the weight of the vertex \(v\)
 * in the original graph. The edges connecting the vertices \(u_1\) and \(v_0\) are weighted with a large enough weight
 * such that the minimum edge-cut in the auxiliary graph will not contain any of these edges. The minimum edge-cuts in
 * the auxiliary graph is computed using the {@link MinimumEdgeCutAllST} algorithm. The minimum vertex-cuts in the
 * original graph is the set of vertices corresponding to the edges in the minimum edge-cuts in the auxiliary graph.
 *
 * @author Barak Ugav
 */
class MinimumVertexCutAllSTEdgeCut extends MinimumVertexCutUtils.AbstractImplAllST {

	private final MinimumEdgeCutAllST minEdgeCutAlgo = MinimumEdgeCutAllST.newInstance();

	@Override
	Iterator<IntSet> computeAllMinimumCuts(IndexGraph g, IWeightFunction w, int source, int sink) {
		AuxiliaryGraph auxiliaryGraph = new AuxiliaryGraph(g, w);
		if (g.getEdge(source, sink) != -1)
			return null;

		IndexGraph g0 = auxiliaryGraph.graph;
		IWeightFunction w0 = auxiliaryGraph.weights;
		final int verticesEdgesThreshold = auxiliaryGraph.verticesEdgesThreshold;

		@SuppressWarnings({ "rawtypes", "unchecked" })
		Iterator<IVertexBiPartition> edgesCuts = (Iterator) minEdgeCutAlgo.computeAllMinimumCuts(g0, w0,
				Integer.valueOf(source * 2 + 1), Integer.valueOf(sink * 2 + 0));

		return JGAlgoUtils.iterMap(edgesCuts, edgesCut -> {
			assert edgesCut.crossEdges().intStream().allMatch(e -> e >= verticesEdgesThreshold);

			int[] vertexCut = edgesCut.crossEdges().toIntArray();
			for (int i = 0; i < vertexCut.length; i++)
				vertexCut[i] -= verticesEdgesThreshold;

			final int n = g.vertices().size();
			return ImmutableIntArraySet.ofBitmap(Bitmap.fromOnes(n, vertexCut));
		});
	}

}
