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
import java.util.Set;
import com.jgalgo.alg.MinimumVertexCutUtils.AuxiliaryGraph;
import com.jgalgo.graph.IWeightFunction;
import com.jgalgo.graph.IndexGraph;
import com.jgalgo.internal.util.ImmutableIntArraySet;
import com.jgalgo.internal.util.IterTools;
import com.jgalgo.internal.util.JGAlgoUtils;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;

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
	Iterator<IntSet> minimumCutsIter(IndexGraph g, IWeightFunction w, int source, int sink) {
		AuxiliaryGraph auxiliaryGraph = new AuxiliaryGraph(g, w);
		if (g.getEdge(source, sink) != -1)
			return null;

		IndexGraph g0 = auxiliaryGraph.graph;
		IWeightFunction w0 = auxiliaryGraph.weights;

		@SuppressWarnings({ "rawtypes", "unchecked" })
		Iterator<IVertexBiPartition> edgesCuts = (Iterator) minEdgeCutAlgo.minimumCutsIter(g0, w0,
				Integer.valueOf(source * 2 + 1), Integer.valueOf(sink * 2 + 0));

		/*
		 * The iterator returned by the minimum edge cut algorithm returns cuts which are unique with respect to the
		 * vertices set of the cut. We map each cut to the cross edges of the cut (in the auxiliary graph), which than
		 * map to vertices in the original graph. Although the vertices of these edge cuts are be different (guaranteed
		 * by the iterator), the cross edges may be the same. We filter out cuts with the same cross edges by storing a
		 * set of cuts. This is unfortunate as we require exponential space. I could not find a way to avoid this. If we
		 * already using exponential space, there is no need to work hard an implement the algorithm in Iterator form,
		 * its easier to compute all cuts in one run.
		 */
		Set<IntSet> cuts = new ObjectOpenHashSet<>();
		while (edgesCuts.hasNext()) {
			int[] cut = edgesCuts.next().crossEdges().toIntArray();
			auxiliaryGraph.edgeCutToVertexCut(cut);
			cuts.add(ImmutableIntArraySet.withNaiveContains(cut));
		}

		/* The queue iterator stores the cuts and use less and less memory as the elements are consumed */
		Iterator<IntSet> cutsIter = JGAlgoUtils.queueIter(cuts);
		cuts.clear();
		final int n = g.vertices().size();
		return IterTools.map(cutsIter, cut -> ImmutableIntArraySet.ofBitmap(cut, n));
	}

}
