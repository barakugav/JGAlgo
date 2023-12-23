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

import com.jgalgo.alg.MinimumVertexCutUtils.AuxiliaryGraph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.Bitmap;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import it.unimi.dsi.fastutil.ints.IntSet;

/**
 * Minimum Vertex-Cut algorithm with terminal vertices (source-sink, S-T) using Minimum Edge-Cut algorithm.
 *
 * <p>
 * An auxiliary graph is constructed from the original graph \(G=(V,E)\) by replacing each vertex \(v\) with two
 * vertices \(v_0\) and \(v_1\) connected with an edge \((v_0,v_1)\), and each edge \(e=(u,v)\) with an edge with either
 * one or two edges. If the original graph is directed, then the edge \((u,v)\) is replaced with the edge \((u_1,v_0)\).
 * If the original graph is undirected, then the edge \((u,v)\) is replaced with the edges \((u_1,v_0)\) and
 * \((v_1,u_0)\). The edges connecting the vertices \(v_0\) and \(v_1\) are weighted with the weight of the vertex \(v\)
 * in the original graph. The edges connecting the vertices \(u_1\) and \(v_0\) are weighted with a large enough weight
 * such that the minimum edge-cut in the auxiliary graph will not contain any of these edges. The minimum edge-cut in
 * the auxiliary graph is computed using the {@link MinimumEdgeCutST} algorithm. The minimum vertex-cut in the original
 * graph is the set of vertices corresponding to the edges in the minimum edge-cut in the auxiliary graph.
 *
 * @author Barak Ugav
 */
class MinimumVertexCutSTEdgeCut extends MinimumVertexCutUtils.AbstractImplST {

	private final MinimumEdgeCutST minEdgeCutAlgo = MinimumEdgeCutST.newInstance();

	@Override
	IntSet computeMinimumCut(IndexGraph g, IWeightFunction w, int source, int sink) {
		AuxiliaryGraph auxiliaryGraph = new AuxiliaryGraph(g, w);
		int[] vertexCut = computeMinCut(g, source, sink, auxiliaryGraph);
		if (vertexCut == null)
			return null;

		final int n = g.vertices().size();
		return ImmutableIntArraySet.ofBitmap(Bitmap.fromOnes(n, vertexCut));
	}

	int[] computeMinCut(IndexGraph g, int source, int sink, AuxiliaryGraph auxiliaryGraph) {
		if (g.containsEdge(source, sink))
			return null;

		IndexGraph g0 = auxiliaryGraph.graph;
		IWeightFunction w0 = auxiliaryGraph.weights;

		IntSet edgesCut = (IntSet) minEdgeCutAlgo
				.computeMinimumCut(g0, w0, Integer.valueOf(source * 2 + 1), Integer.valueOf(sink * 2 + 0))
				.crossEdges();
		int[] cut = edgesCut.toIntArray();
		auxiliaryGraph.edgeCutToVertexCut(cut);
		return cut;
	}

}
